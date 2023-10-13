package com.figaf.content.converter.domain.converter;

import com.figaf.content.converter.domain.file.FileContentWriter;
import com.figaf.content.converter.domain.strategy.NodeCreationStrategy;
import com.figaf.content.converter.dto.ConversionConfigDto;
import com.figaf.content.converter.exception.ApplicationException;
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

    @Override
    public byte[] createConvertedFile(ConversionConfigDto conversionConfigDto, List<String> parsedInputFileLines, String testDataFolderName) throws ParserConfigurationException, IOException, TransformerException {
        log.debug("#createConvertedFile: conversionConfigDto={}, parsedInputFileLines={}, testDataFolderName={}", conversionConfigDto, parsedInputFileLines, testDataFolderName);
        ensureValidConversionArgs(conversionConfigDto, parsedInputFileLines);
        Document xmlDocument = createXMLDocumentFromFlattenedInput(parsedInputFileLines, conversionConfigDto);
        return FileContentWriter.createXmlOutputFile(xmlDocument, testDataFolderName, xmlDocument);
    }

    private Document createXMLDocumentFromFlattenedInput(List<String> fileInputLines, ConversionConfigDto conversionConfigDto) throws ParserConfigurationException {
        Document document = XMLUtils.createDocument();
        Element root = XMLUtils.createElement(document, conversionConfigDto.getDocumentNamespace(), conversionConfigDto.getDocumentName());
        XMLUtils.appendChild(document, root);

        Map<String, String> keyRecordToFrequency = parseRecordsetStructure(conversionConfigDto.getRecordsetStructure());
        String firstKeyRecord = keyRecordToFrequency.keySet().iterator().next();
        boolean singleKeyMapping = keyRecordToFrequency.size() == 1;
        Element recordSetTag = null;

        String recordSetName = StringUtils.isEmpty(conversionConfigDto.getRecordsetName()) ? "Recordset" : conversionConfigDto.getRecordsetName();
        String recordsetNamespace = StringUtils.isEmpty(conversionConfigDto.getRecordsetNamespace()) ? "" : conversionConfigDto.getRecordsetNamespace();
        log.debug("#createXMLDocumentFromFlattenedInput: singleKeyMapping={}, isIgnoreRecordsetName={}", singleKeyMapping, conversionConfigDto.isIgnoreRecordsetName());

        NodeCreationStrategy nodeCreationStrategy = new NodeCreationStrategy();
        if (nodeCreationStrategy.shouldNotSkipRecordsetCreation(conversionConfigDto, singleKeyMapping)) {
            recordSetTag = XMLUtils.createElement(document, recordsetNamespace, recordSetName);
            XMLUtils.appendChild(root, recordSetTag);
        }
        boolean isFirstKeyRecordEncounter = true;

        for (String inputLine : fileInputLines) {
            Map<String, ConversionConfigDto.SectionParameters> keyRecordToSectionParameters = determineKeyRecordToSectionParameters(inputLine, conversionConfigDto, singleKeyMapping);

            if (nodeCreationStrategy.shouldCreateNewRecordsetForMultipleKeyRecords(
                    conversionConfigDto,
                    singleKeyMapping,
                    keyRecordToSectionParameters,
                    firstKeyRecord
            )) {
                if (isFirstKeyRecordEncounter) {
                    isFirstKeyRecordEncounter = false;
                } else {
                    recordSetTag = XMLUtils.createElement(document, recordsetNamespace, recordSetName);
                    log.debug("proceed to append recordSetTag: recordSetTag={}", recordSetTag);
                    XMLUtils.appendChild(root, recordSetTag);
                }
            }

            nodeCreationStrategy.createNodesFromInputLine(
                    inputLine,
                    document,
                    root,
                    keyRecordToSectionParameters,
                    recordSetTag,
                    singleKeyMapping
            );
        }

        return document;
    }

    private Map<String, ConversionConfigDto.SectionParameters> determineKeyRecordToSectionParameters(String inputFileLine, ConversionConfigDto conversionConfigDto, boolean singleKeyMapping) {
        if (singleKeyMapping) {
            return conversionConfigDto.getSectionParameters();
        }

        Optional<Map.Entry<String, ConversionConfigDto.SectionParameters>> keyRecordMatch = conversionConfigDto.getSectionParameters()
                .entrySet()
                .stream()
                .filter(entry -> inputFileLine.startsWith(entry.getKey()))
                .findFirst();

        if (keyRecordMatch.isPresent()) {
            return Collections.singletonMap(keyRecordMatch.get().getKey(), keyRecordMatch.get().getValue());
        }

        Optional<Map.Entry<String, ConversionConfigDto.SectionParameters>> keyFieldValueMatch = conversionConfigDto.getSectionParameters()
                .entrySet()
                .stream()
                .filter(entry -> inputFileLine.startsWith(entry.getValue().getKeyFieldValue()))
                .findFirst();

        return keyFieldValueMatch
                .map(sectionParametersEntry ->
                        Collections.singletonMap(sectionParametersEntry.getKey(), sectionParametersEntry.getValue()))
                .orElse(Collections.emptyMap());
    }

    public Map<String, String> parseRecordsetStructure(String recordsetStructure) {
        Map<String, String> tagToOccurrence = new LinkedHashMap<>();
        String[] tokens = recordsetStructure.split(",");
        //proceed to next record element and occurrence (KEY1,2) -> (KEY2,*)
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
            throw new ApplicationException(combinedErrorMessage);
        }
    }
}

