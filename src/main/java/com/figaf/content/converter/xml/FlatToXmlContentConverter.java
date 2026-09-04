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

    // allowed values of the enum-like substructure parameters; anything else silently behaves as the
    // default, so it is reported once per conversion, like an unresolvable keyFieldName
    private static final List<String> MISSING_LAST_FIELDS_VALUES = Arrays.asList("add", "ignore", "error");
    private static final List<String> ADDITIONAL_LAST_FIELDS_VALUES = Arrays.asList("ignore", "error");
    private static final List<String> FIELD_CONTENT_FORMATTING_VALUES = Arrays.asList("trim", "nothing");

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
        warnIfKeyFieldNameMatchesNoField(conversionConfig);
        warnOnUnrecognizedSectionParameterValues(conversionConfig);
        return createXMLDocumentFromFlattenedInput(flatFileLines, conversionConfig);
    }

    private Document createXMLDocumentFromFlattenedInput(
        List<String> flatFileLines,
        ConversionConfig conversionConfig
    ) throws ParserConfigurationException {
        Document document = initializeDocument(conversionConfig);
        Element root = document.getDocumentElement();
        Map<String, String> parseRecordsetStructure = parseRecordsetStructure(conversionConfig.getRecordsetStructure());
        Map<String, ConversionConfig.SectionParameters> orderedSectionParameters =
            orderSectionParameters(conversionConfig.getSectionParameters(), parseRecordsetStructure.keySet());
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
            orderedSectionParameters,
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
        Map<String, ConversionConfig.SectionParameters> orderedSectionParameters,
        Element recordSetTag
    ) {
        String firstKeyRecord = parseRecordsetStructure.keySet().iterator().next();
        boolean isFirstKeyRecordEncounter = true;
        boolean singleKeyMapping = parseRecordsetStructure.size() == 1;

        for (String inputLine : fileInputLines) {
            // blank lines carry no data and can never match a substructure, so they are always ignored
            // regardless of failOnUnmatchedLines (SAP does not document blank line handling)
            if (StringUtils.isBlank(inputLine)) {
                log.warn("Ignoring a blank line");
                continue;
            }
            Map<String, ConversionConfig.SectionParameters> keyRecordToSectionParameters = determineKeyRecordToSectionParameters(inputLine, conversionConfig, orderedSectionParameters, singleKeyMapping);
            if (keyRecordToSectionParameters.isEmpty()) {
                if (conversionConfig.isFailOnUnmatchedLines()) {
                    throw new IllegalArgumentException(format(
                        "Cannot determine the record type of the line '%s': the line matches no substructure (keyFieldName=%s)",
                        inputLine,
                        conversionConfig.getKeyFieldName()
                    ));
                }
                log.warn("Skipping line '{}': the line matches no substructure (keyFieldName={})",
                    inputLine,
                    conversionConfig.getKeyFieldName()
                );
                continue;
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

    private Map<String, ConversionConfig.SectionParameters> determineKeyRecordToSectionParameters(
        String inputFileLine,
        ConversionConfig conversionConfig,
        Map<String, ConversionConfig.SectionParameters> orderedSectionParameters,
        boolean singleKeyMapping
    ) {
        if (singleKeyMapping) {
            return orderedSectionParameters;
        }

        boolean keyFieldNameProvided = StringUtils.isNotBlank(conversionConfig.getKeyFieldName());
        Set<String> keysWithExtractedValue = new HashSet<>();
        if (keyFieldNameProvided) {
            for (Map.Entry<String, ConversionConfig.SectionParameters> keyToSectionParameters : orderedSectionParameters.entrySet()) {
                String actualKeyFieldValue = extractKeyFieldValue(inputFileLine, keyToSectionParameters.getValue(), conversionConfig.getKeyFieldName());
                if (actualKeyFieldValue == null) {
                    continue;
                }
                keysWithExtractedValue.add(keyToSectionParameters.getKey());
                String keyFieldValue = keyToSectionParameters.getValue().getKeyFieldValue();
                // a substructure without keyFieldValue can never match by key field
                if (keyFieldValue == null) {
                    continue;
                }
                if (StringUtils.isNotEmpty(keyToSectionParameters.getValue().getFieldSeparator())) {
                    // separator structures: the extracted value has the enclosure signs already removed, so they
                    // are removed from the configured keyFieldValue too — a real PI channel may store "H" or H.
                    // A fixed-length extraction keeps the line content as is, so the configured value must too
                    keyFieldValue = NodeCreationStrategy.removeEnclosureSigns(keyFieldValue, keyToSectionParameters.getValue()).trim();
                }
                if (actualKeyFieldValue.trim().equals(keyFieldValue)) {
                    return Collections.singletonMap(keyToSectionParameters.getKey(), keyToSectionParameters.getValue());
                }
            }
        }

        // Legacy prefix matching, kept for configs created before keyFieldName support. It runs even when
        // keyFieldName is set, so key-field matching is NOT exclusive: a line whose key field value matched
        // nothing can still get a record type here. Two rules:
        // 1. keyFieldValue prefix: skipped for a substructure whose key field value was read but did not match
        //    (a short marker like "H" could match an unrelated line). Kept for a substructure whose key field
        //    could not be read at all, so a keyFieldName typo cannot break a config that works without it.
        // 2. Section-NAME prefix: always active — real channels rely on it even with a contradicting
        //    keyFieldValue (see the KK/KK3 substructure of the more-than-one-recordset-to-xml fixture)
        for (Map.Entry<String, ConversionConfig.SectionParameters> keyToSectionParameters : orderedSectionParameters.entrySet()) {
            if (inputFileLine.startsWith(keyToSectionParameters.getKey())
                || (!keysWithExtractedValue.contains(keyToSectionParameters.getKey())
                    && keyToSectionParameters.getValue().getKeyFieldValue() != null
                    && inputFileLine.startsWith(keyToSectionParameters.getValue().getKeyFieldValue()))) {
                return Collections.singletonMap(keyToSectionParameters.getKey(), keyToSectionParameters.getValue());
            }
        }

        return Collections.emptyMap();
    }

    /**
     * Orders the configured substructures by their position in recordsetStructure, so that record type
     * matching never depends on the accidental iteration order of the configured section map. Sections
     * that are not part of recordsetStructure keep their original relative order at the end.
     */
    private Map<String, ConversionConfig.SectionParameters> orderSectionParameters(
        Map<String, ConversionConfig.SectionParameters> sectionParameters,
        Set<String> recordsetStructureKeys
    ) {
        Map<String, ConversionConfig.SectionParameters> orderedSectionParameters = new LinkedHashMap<>();
        for (String key : recordsetStructureKeys) {
            ConversionConfig.SectionParameters parameters = sectionParameters.get(key);
            if (parameters != null) {
                orderedSectionParameters.put(key, parameters);
            }
        }
        for (Map.Entry<String, ConversionConfig.SectionParameters> entry : sectionParameters.entrySet()) {
            orderedSectionParameters.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return orderedSectionParameters;
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
            // same splitting as the field-filling path: an enclosed separator must not shift the key field index
            String[] fieldValues = NodeCreationStrategy.splitLineIntoFieldValues(inputFileLine, sectionParameters);
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

    private void warnIfKeyFieldNameMatchesNoField(ConversionConfig conversionConfig) {
        if (StringUtils.isBlank(conversionConfig.getKeyFieldName()) || conversionConfig.getSectionParameters() == null) {
            return;
        }
        for (ConversionConfig.SectionParameters sectionParameters : conversionConfig.getSectionParameters().values()) {
            if (StringUtils.isBlank(sectionParameters.getFieldNames())) {
                continue;
            }
            for (String fieldName : sectionParameters.getFieldNames().split(",")) {
                if (fieldName.trim().equals(conversionConfig.getKeyFieldName())) {
                    return;
                }
            }
        }
        log.warn("keyFieldName '{}' is not a field name in any substructure; "
            + "record types will fall back to prefix matching", conversionConfig.getKeyFieldName());
    }

    private void warnOnUnrecognizedSectionParameterValues(ConversionConfig conversionConfig) {
        if (conversionConfig.getSectionParameters() == null) {
            return;
        }
        for (Map.Entry<String, ConversionConfig.SectionParameters> entry : conversionConfig.getSectionParameters().entrySet()) {
            ConversionConfig.SectionParameters sectionParameters = entry.getValue();
            warnOnUnrecognizedValue(entry.getKey(), "missingLastFields", sectionParameters.getMissingLastFields(), MISSING_LAST_FIELDS_VALUES, "add");
            warnOnUnrecognizedValue(entry.getKey(), "additionalLastFields", sectionParameters.getAdditionalLastFields(), ADDITIONAL_LAST_FIELDS_VALUES, "ignore");
            warnOnUnrecognizedValue(entry.getKey(), "fieldContentFormatting", sectionParameters.getFieldContentFormatting(), FIELD_CONTENT_FORMATTING_VALUES, "trim");
        }
    }

    private void warnOnUnrecognizedValue(String sectionKey, String parameterName, String value, List<String> allowedValues, String appliedDefault) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        for (String allowedValue : allowedValues) {
            if (allowedValue.equalsIgnoreCase(value)) {
                return;
            }
        }
        log.warn("{} '{}' of substructure '{}' is not one of {}; it behaves as the default '{}'",
            parameterName, value, sectionKey, allowedValues, appliedDefault);
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

