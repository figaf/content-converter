package com.figaf.content.converter.domain.converter;

import com.figaf.content.converter.domain.processor.XmlProcessor;
import com.figaf.content.converter.domain.strategy.NodeCreationStrategy;
import com.figaf.content.converter.dto.ConversionConfigDto;
import com.figaf.content.converter.utils.XMLUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.*;

@Slf4j
public class XmlContentConverter implements ContentConverter {

    private final NodeCreationStrategy nodeCreationStrategy;

    public XmlContentConverter(NodeCreationStrategy nodeCreationStrategy) {
        this.nodeCreationStrategy = nodeCreationStrategy;
    }

    @Override
    public byte[] createConvertedFile(ConversionConfigDto conversionConfigDto, List<String> parsedInputFileLines, String testDataFolderName) throws ParserConfigurationException, IOException, TransformerException {
        log.debug("#createConvertedFile: conversionConfigDto={}, parsedInputFileLines={}, testDataFolderName={}", conversionConfigDto, parsedInputFileLines, testDataFolderName);
        ensureValidConversionArgs(conversionConfigDto, parsedInputFileLines);
        Document xmlDocument = createXMLDocumentFromFlattenedInput(parsedInputFileLines, conversionConfigDto);
        XmlProcessor xmlProcessor = new XmlProcessor();
        return xmlProcessor.processXmlDocument(xmlDocument, testDataFolderName);
    }

    private Document createXMLDocumentFromFlattenedInput(List<String> fileInputLines, ConversionConfigDto conversionConfigDto) throws ParserConfigurationException {
        Document document = initializeDocument(conversionConfigDto);
        Element root = document.getDocumentElement();
        Map<String, String> parseRecordsetStructure = parseRecordsetStructure(conversionConfigDto.getRecordsetStructure());
        Element recordSetTag = determineRecordSetTag(document, conversionConfigDto, parseRecordsetStructure.size() == 1);

        if (recordSetTag != null) {
            XMLUtils.appendChild(root, recordSetTag);
        }

        processInputLines(
                fileInputLines,
                document,
                root,
                conversionConfigDto,
                parseRecordsetStructure,
                recordSetTag
        );

        return document;
    }

    private Document initializeDocument(ConversionConfigDto conversionConfigDto) throws ParserConfigurationException {
        Document document = XMLUtils.createDocument();
        Element root = XMLUtils.createElement(document, conversionConfigDto.getDocumentNamespace(), conversionConfigDto.getDocumentName());
        XMLUtils.appendChild(document, root);
        return document;
    }

    private Element determineRecordSetTag(Document document, ConversionConfigDto conversionConfigDto, boolean singleKeyMapping) {
        if (nodeCreationStrategy.shouldNotSkipRecordsetCreation(conversionConfigDto, singleKeyMapping)) {
            String recordSetName = StringUtils.isEmpty(conversionConfigDto.getRecordsetName()) ? "Recordset" : conversionConfigDto.getRecordsetName();
            String recordsetNamespace = StringUtils.isEmpty(conversionConfigDto.getRecordsetNamespace()) ? "" : conversionConfigDto.getRecordsetNamespace();
            return XMLUtils.createElement(document, recordsetNamespace, recordSetName);
        }
        return null;
    }

    private void processInputLines(
            List<String> fileInputLines,
            Document document,
            Element root,
            ConversionConfigDto conversionConfigDto,
            Map<String, String> parseRecordsetStructure,
            Element recordSetTag
    ) {
        String firstKeyRecord = parseRecordsetStructure.keySet().iterator().next();
        boolean isFirstKeyRecordEncounter = true;
        boolean singleKeyMapping = parseRecordsetStructure.size() == 1;

        for (String inputLine : fileInputLines) {
            Map<String, ConversionConfigDto.SectionParameters> keyRecordToSectionParameters = determineKeyRecordToSectionParameters(inputLine, conversionConfigDto, singleKeyMapping);

            if (nodeCreationStrategy.shouldCreateNewRecordsetForMultipleKeyRecords(
                    conversionConfigDto,
                    singleKeyMapping,
                    keyRecordToSectionParameters,
                    firstKeyRecord
            ) && !isFirstKeyRecordEncounter) {

                String recordsetNamespace = StringUtils.isEmpty(conversionConfigDto.getRecordsetNamespace()) ? "" : conversionConfigDto.getRecordsetNamespace();
                String recordSetName = StringUtils.isEmpty(conversionConfigDto.getRecordsetName()) ? "Recordset" : conversionConfigDto.getRecordsetName();
                recordSetTag = XMLUtils.createElement(document, recordsetNamespace, recordSetName);
                XMLUtils.appendChild(root, recordSetTag);
            }
            isFirstKeyRecordEncounter = false;

            nodeCreationStrategy.createNodesFromInputLine(inputLine, document, root, keyRecordToSectionParameters, recordSetTag, singleKeyMapping);
        }
    }

    private Map<String, ConversionConfigDto.SectionParameters> determineKeyRecordToSectionParameters(String inputFileLine, ConversionConfigDto conversionConfigDto, boolean singleKeyMapping) {
        if (singleKeyMapping) {
            return conversionConfigDto.getSectionParameters();
        }

        for (Map.Entry<String, ConversionConfigDto.SectionParameters> keyToSectionParameters : conversionConfigDto.getSectionParameters().entrySet()) {
            if (inputFileLine.startsWith(keyToSectionParameters.getKey()) || inputFileLine.startsWith(keyToSectionParameters.getValue().getKeyFieldValue())) {
                return Collections.singletonMap(keyToSectionParameters.getKey(), keyToSectionParameters.getValue());
            }
        }

        return Collections.emptyMap();
    }

    public Map<String, String> parseRecordsetStructure(String recordsetStructure) {
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

    private void ensureValidConversionArgs(ConversionConfigDto conversionConfigDto, List<String> parsedInputFileLines) {
        List<String> errorMessages = new ArrayList<>();

        if (StringUtils.isEmpty(conversionConfigDto.getRecordsetStructure())) {
            errorMessages.add("Recordset structure is missing.");
        }
        if (CollectionUtils.isEmpty(conversionConfigDto.getSectionParameters())) {
            errorMessages.add("No section parameters provided.");
        }
        if (CollectionUtils.isEmpty(parsedInputFileLines)) {
            errorMessages.add("No valid input lines found.");
        }

        if (!errorMessages.isEmpty()) {
            String combinedErrorMessage = String.join(" ", errorMessages);
            log.error("#ensureValidConversionArgs: combinedErrorMessage={}", combinedErrorMessage);
            throw new IllegalArgumentException(combinedErrorMessage);
        }
    }
}

