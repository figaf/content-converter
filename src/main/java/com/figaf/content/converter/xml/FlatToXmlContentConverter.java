package com.figaf.content.converter.xml;

import com.figaf.content.converter.ContentConversionException;
import com.figaf.content.converter.ContentConverter;
import com.figaf.content.converter.ConversionConfig;
import com.figaf.content.converter.parser.FlatDocumentParser;
import com.figaf.content.converter.utils.XMLUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

import static com.figaf.content.converter.utils.XMLUtils.writeDocumentToByteArray;
import static java.lang.String.format;

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
    ) throws ContentConversionException {
        log.debug("#convert: conversionConfig={}", conversionConfig);
        try {
            validateInputArgs(flatDocument, conversionConfig);
            Document xmlDocument = createXMLDocumentFromFlattenedInput(flatDocument, conversionConfig);
            return writeDocumentToByteArray(xmlDocument, conversionConfig.isBeautifyOutput());
        } catch (Exception ex) {
            throw new ContentConversionException("Couldn't convert flat file to XML", ex);
        }
    }

    private Document createXMLDocumentFromFlattenedInput(
        byte[] flatDocument,
        ConversionConfig conversionConfig
    ) throws ParserConfigurationException {
        List<String> flatDocumentLines = FlatDocumentParser.readLines(flatDocument);
        Document document = initializeDocument(conversionConfig);
        Element root = document.getDocumentElement();
        Map<String, String> parseRecordsetStructure = parseRecordsetStructure(conversionConfig.getRecordsetStructure());
        Element recordSetTag = determineRecordSetTag(document, conversionConfig, parseRecordsetStructure.size() == 1);

        if (recordSetTag != null) {
            XMLUtils.appendChild(root, recordSetTag);
        }

        processInputLines(
            flatDocumentLines,
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

            if (nodeCreationStrategy.shouldCreateNewRecordsetForMultipleKeyRecords(
                conversionConfig,
                singleKeyMapping,
                keyRecordToSectionParameters,
                firstKeyRecord
            ) && !isFirstKeyRecordEncounter) {

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

        for (Map.Entry<String, ConversionConfig.SectionParameters> keyToSectionParameters : conversionConfig.getSectionParameters().entrySet()) {
            if (inputFileLine.startsWith(keyToSectionParameters.getKey()) || inputFileLine.startsWith(keyToSectionParameters.getValue().getKeyFieldValue())) {
                return Collections.singletonMap(keyToSectionParameters.getKey(), keyToSectionParameters.getValue());
            }
        }

        return Collections.emptyMap();
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

    private void validateInputArgs(byte[] document, ConversionConfig conversionConfig) {
        List<String> errorMessages = new ArrayList<>();

        if (StringUtils.isEmpty(conversionConfig.getRecordsetStructure())) {
            errorMessages.add("Recordset structure is missing.");
        }
        if (MapUtils.isEmpty(conversionConfig.getSectionParameters())) {
            errorMessages.add("No section parameters provided.");
        }
        if (ArrayUtils.isEmpty(document)) {
            errorMessages.add("Provided document must be not empty");
        }

        if (!errorMessages.isEmpty()) {
            String combinedErrorMessage = format("Conversion arguments are not valid:\n%s", String.join("\n", errorMessages));
            log.error(combinedErrorMessage);
            throw new IllegalArgumentException(combinedErrorMessage);
        }
    }
}

