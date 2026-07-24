package com.figaf.content.converter.xml;

import com.figaf.content.converter.ContentConversionException;
import com.figaf.content.converter.ContentConverter;
import com.figaf.content.converter.ConversionConfig;
import com.figaf.content.converter.parser.FlatDocumentParser;
import com.figaf.content.converter.utils.XMLUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static com.figaf.content.converter.utils.XMLUtils.writeDocumentToByteArray;
import static java.lang.String.format;

/**
 * Supported Data Conversion Scenarios to XML:
 * <ul>
 *   <li>CSV with Headers to XML: Transforms CSV data, preserving headers, into standard XML format.</li>
 *   <li>CSV with Headers to Minified XML: Converts CSV data, maintaining headers, into a XML format.</li>
 *   <li>Structured Text to XML: Adapts text with specific markers (e.g., H, D, T) enclosed in double quotes to XML.</li>
 *   <li>Mixed Content to XML: Processes content combining structured text and CSV-like data, identifying keys such as (HD, PR, LI, KK) into a XML format.</li>
 *   <li>Multiple Recordsets to XML: Transforms mixed content with several recordsets into an organized XML structure.</li>
 *   <li>Text-Based Data to XML: Standardizes pure text inputs into XML format.</li>
 *   <li>Plain Text to XML (No Recordset): Converts straightforward text into structured XML, even without recordset elements.</li>
 * </ul>
 */
@Slf4j
public class FlatToXmlContentConverter implements ContentConverter {

    private final NodeCreationStrategy nodeCreationStrategy;

    public FlatToXmlContentConverter() {
        this(new NodeCreationStrategy());
    }

    public FlatToXmlContentConverter(NodeCreationStrategy nodeCreationStrategy) {
        this.nodeCreationStrategy = nodeCreationStrategy;
    }

    @Override
    public byte[] convert(
        byte[] flatDocument,
        ConversionConfig conversionConfig
    ) {
        try {
            Document xmlDocument = convert(FlatDocumentParser.splitToLines(flatDocument), conversionConfig);
            return writeDocumentToByteArray(xmlDocument, conversionConfig.isBeautifyOutput());
        } catch (Exception ex) {
            throw new ContentConversionException("Couldn't convert flat file to XML", ex);
        }
    }

    @Override
    public String convert(
        String flatDocument,
        ConversionConfig conversionConfig
    ) {
        try {
            Document xmlDocument = convert(FlatDocumentParser.splitToLines(flatDocument), conversionConfig);
            return new String(
                writeDocumentToByteArray(xmlDocument, conversionConfig.isBeautifyOutput()),
                StandardCharsets.UTF_8
            );
        } catch (Exception ex) {
            throw new ContentConversionException("Couldn't convert flat file to XML", ex);
        }
    }

    private Document convert(
        List<String> flatFileLines,
        ConversionConfig conversionConfig
    ) throws ParserConfigurationException {
        log.debug("#convert: conversionConfig={}", conversionConfig);
        validateInputArgs(flatFileLines, conversionConfig);
        return createXMLDocumentFromFlattenedInput(flatFileLines, conversionConfig);
    }

    private Document createXMLDocumentFromFlattenedInput(
        List<String> flatFileLines,
        ConversionConfig conversionConfig
    ) throws ParserConfigurationException {
        Document document = initializeDocument(conversionConfig);
        Element root = document.getDocumentElement();
        Map<String, String> parseRecordsetStructure = parseRecordsetStructure(conversionConfig.getRecordsetStructure());
        Element recordSetTag = determineRecordSetTag(document, conversionConfig, parseRecordsetStructure.size() == 1);

        if (recordSetTag != null) {
            XMLUtils.appendChild(root, recordSetTag);
        }

        processInputLines(
            flatFileLines,
            document,
            root,
            conversionConfig,
            parseRecordsetStructure,
            recordSetTag
        );

        return document;
    }

    private Document initializeDocument(ConversionConfig conversionConfig) throws ParserConfigurationException {
        Document document = XMLUtils.createDocument();
        Element root = XMLUtils.createElement(document, conversionConfig.getDocumentNamespace(), conversionConfig.getDocumentName());
        XMLUtils.appendChild(document, root);
        return document;
    }

    private Element determineRecordSetTag(Document document, ConversionConfig conversionConfig, boolean singleKeyMapping) {
        if (nodeCreationStrategy.shouldNotSkipRecordsetCreation(conversionConfig, singleKeyMapping)) {
            String recordSetName = StringUtils.isEmpty(conversionConfig.getRecordsetName()) ? "Recordset" : conversionConfig.getRecordsetName();
            String recordsetNamespace = StringUtils.isEmpty(conversionConfig.getRecordsetNamespace()) ? "" : conversionConfig.getRecordsetNamespace();
            return XMLUtils.createElement(document, recordsetNamespace, recordSetName);
        }
        return null;
    }

    private void processInputLines(
        List<String> fileInputLines,
        Document document,
        Element root,
        ConversionConfig conversionConfig,
        Map<String, String> parseRecordsetStructure,
        Element recordSetTag
    ) {
        String firstKeyRecord = parseRecordsetStructure.keySet().iterator().next();
        boolean isFirstKeyRecordEncounter = true;
        boolean singleKeyMapping = parseRecordsetStructure.size() == 1;

        for (String inputLine : fileInputLines) {
            Map<String, ConversionConfig.SectionParameters> keyRecordToSectionParameters = determineKeyRecordToSectionParameters(inputLine, conversionConfig, singleKeyMapping);
            if (keyRecordToSectionParameters.isEmpty()) {
                if (conversionConfig.isSkipUnmatchedLines()) {
                    log.warn("Skipping line '{}': the line matches no substructure (keyFieldName={})",
                        inputLine,
                        conversionConfig.getKeyFieldName()
                    );
                    continue;
                }
                throw new IllegalArgumentException(format(
                    "Cannot determine the record type of the line '%s': the line matches no substructure (keyFieldName=%s)",
                    inputLine,
                    conversionConfig.getKeyFieldName()
                ));
            }

            if (nodeCreationStrategy.shouldCreateNewRecordsetForMultipleKeyRecords(
                conversionConfig,
                singleKeyMapping,
                keyRecordToSectionParameters,
                firstKeyRecord
            ) && !isFirstKeyRecordEncounter
            ) {
                String recordsetNamespace = StringUtils.isEmpty(conversionConfig.getRecordsetNamespace()) ? "" : conversionConfig.getRecordsetNamespace();
                String recordSetName = StringUtils.isEmpty(conversionConfig.getRecordsetName()) ? "Recordset" : conversionConfig.getRecordsetName();
                recordSetTag = XMLUtils.createElement(document, recordsetNamespace, recordSetName);
                XMLUtils.appendChild(root, recordSetTag);
            }
            isFirstKeyRecordEncounter = false;

            nodeCreationStrategy.createNodesFromInputLine(inputLine, document, root, keyRecordToSectionParameters, recordSetTag, singleKeyMapping);
        }
    }

    private Map<String, ConversionConfig.SectionParameters> determineKeyRecordToSectionParameters(String inputFileLine, ConversionConfig conversionConfig, boolean singleKeyMapping) {
        if (singleKeyMapping) {
            return conversionConfig.getSectionParameters();
        }

        boolean keyFieldNameProvided = StringUtils.isNotBlank(conversionConfig.getKeyFieldName());
        if (keyFieldNameProvided) {
            for (Map.Entry<String, ConversionConfig.SectionParameters> keyToSectionParameters : conversionConfig.getSectionParameters().entrySet()) {
                String actualKeyFieldValue = extractKeyFieldValue(inputFileLine, keyToSectionParameters.getValue(), conversionConfig.getKeyFieldName());
                if (actualKeyFieldValue != null && actualKeyFieldValue.trim().equals(keyToSectionParameters.getValue().getKeyFieldValue())) {
                    return Collections.singletonMap(keyToSectionParameters.getKey(), keyToSectionParameters.getValue());
                }
            }
        }

        // legacy matching for configs created before keyFieldName support: works only when the key field is the first field of the line.
        // When keyFieldName is provided and didn't match, only the section-name prefix is still honored (deliberate legacy
        // convention where lines are prefixed with the structure name); matching by keyFieldValue prefix is skipped because
        // a short marker like "H" can accidentally match the beginning of an unrelated line and misclassify it
        for (Map.Entry<String, ConversionConfig.SectionParameters> keyToSectionParameters : conversionConfig.getSectionParameters().entrySet()) {
            if (inputFileLine.startsWith(keyToSectionParameters.getKey())
                || (!keyFieldNameProvided && keyToSectionParameters.getValue().getKeyFieldValue() != null && inputFileLine.startsWith(keyToSectionParameters.getValue().getKeyFieldValue()))) {
                return Collections.singletonMap(keyToSectionParameters.getKey(), keyToSectionParameters.getValue());
            }
        }

        return Collections.emptyMap();
    }

    private String extractKeyFieldValue(String inputFileLine, ConversionConfig.SectionParameters sectionParameters, String keyFieldName) {
        if (StringUtils.isBlank(sectionParameters.getFieldNames())) {
            return null;
        }
        String[] fieldNames = sectionParameters.getFieldNames().split(",");
        int keyFieldIndex = -1;
        for (int i = 0; i < fieldNames.length; i++) {
            if (fieldNames[i].trim().equals(keyFieldName)) {
                keyFieldIndex = i;
                break;
            }
        }
        if (keyFieldIndex == -1) {
            return null;
        }

        if (StringUtils.isNotEmpty(sectionParameters.getFieldSeparator())) {
            String[] fieldValues = inputFileLine.split(Pattern.quote(sectionParameters.getFieldSeparator()), -1);
            return keyFieldIndex < fieldValues.length ? fieldValues[keyFieldIndex] : null;
        }

        if (StringUtils.isBlank(sectionParameters.getFieldFixedLengths())) {
            return null;
        }
        int[] fieldLengths = Arrays.stream(sectionParameters.getFieldFixedLengths().split(","))
            .mapToInt(fieldLength -> Integer.parseInt(fieldLength.trim()))
            .toArray();
        if (keyFieldIndex >= fieldLengths.length) {
            return null;
        }
        int keyFieldStart = 0;
        for (int i = 0; i < keyFieldIndex; i++) {
            keyFieldStart += fieldLengths[i];
        }
        if (keyFieldStart >= inputFileLine.length()) {
            return null;
        }
        return inputFileLine.substring(keyFieldStart, Math.min(keyFieldStart + fieldLengths[keyFieldIndex], inputFileLine.length()));
    }

    private Map<String, String> parseRecordsetStructure(String recordsetStructure) {
        Map<String, String> tagToOccurrence = new LinkedHashMap<>();
        String[] tokens = recordsetStructure.split(",");

        //ensure even number of tokens
        if (tokens.length % 2 != 0) {
            log.error("Improperly formatted recordsetStructure={}", recordsetStructure);
            throw new IllegalArgumentException(String.join("Improperly formatted recordsetStructure=%s", recordsetStructure));
        }

        for (int i = 0; i < tokens.length; i += 2) {
            tagToOccurrence.put(tokens[i], tokens[i + 1]);
        }

        return tagToOccurrence;
    }

    private void validateInputArgs(List<String> flatFileLines, ConversionConfig conversionConfig) {
        List<String> errorMessages = new ArrayList<>();

        if (StringUtils.isEmpty(conversionConfig.getRecordsetStructure())) {
            errorMessages.add("Recordset structure is missing.");
        }
        if (conversionConfig.getSectionParameters() == null || conversionConfig.getSectionParameters().isEmpty()) {
            errorMessages.add("No section parameters provided.");
        }
        if (flatFileLines == null || flatFileLines.isEmpty()) {
            errorMessages.add("Provided document must be not empty");
        }

        if (!errorMessages.isEmpty()) {
            String combinedErrorMessage = format("Conversion arguments are not valid:\n%s", String.join("\n", errorMessages));
            log.error(combinedErrorMessage);
            throw new IllegalArgumentException(combinedErrorMessage);
        }
    }
}

