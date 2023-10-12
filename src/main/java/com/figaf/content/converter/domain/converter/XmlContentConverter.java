package com.figaf.content.converter.domain.converter;

import com.figaf.content.converter.domain.file.FileContentWriter;
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

        if (shouldNotSkipRecordsetCreation(conversionConfigDto, singleKeyMapping)) {
            recordSetTag = XMLUtils.createElement(document, recordsetNamespace, recordSetName);
            XMLUtils.appendChild(root, recordSetTag);
        }
        boolean isFirstKeyRecordEncounter = true;

        for (String inputLine : fileInputLines) {
            Map<String, ConversionConfigDto.SectionParameters> keyRecordToSectionParameters = determineKeyRecordToSectionParameters(inputLine, conversionConfigDto, singleKeyMapping);

            if (shouldCreateNewRecordsetForMultipleKeyRecords(
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

            createNodesFromInputLine(
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

    private boolean shouldNotSkipRecordsetCreation(ConversionConfigDto conversionConfigDto, boolean singleKeyMapping) {
        return !singleKeyMapping && !conversionConfigDto.isIgnoreRecordsetName();
    }

    private boolean shouldCreateNewRecordsetForMultipleKeyRecords(
            ConversionConfigDto conversionConfigDto,
            boolean singleKeyMapping,
            Map<String, ConversionConfigDto.SectionParameters> keyRecordToSectionParameters,
            String firstKeyRecord
    ) {
        return !conversionConfigDto.isIgnoreRecordsetName() &&
                !singleKeyMapping &&
                keyRecordToSectionParameters.keySet().iterator().next().equals(firstKeyRecord);
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

    private void createNodesFromInputLine(
            String inputLine,
            Document document,
            Element root,
            Map<String, ConversionConfigDto.SectionParameters> keyRecordToSectionParameters,
            Element recordSetTag,
            boolean singleKeyMapping
    ) {

        for (Map.Entry<String, ConversionConfigDto.SectionParameters> sectionParameters : keyRecordToSectionParameters.entrySet()) {
            Element node = createNodeFromSectionParameters(document, inputLine, sectionParameters);
            if (!singleKeyMapping && recordSetTag != null) {
                XMLUtils.appendChild(recordSetTag, node);
            } else {
                XMLUtils.appendChild(root, node);
            }
        }
    }

    private Element createNodeFromSectionParameters(
            Document document,
            String inputFileLine,
            Map.Entry<String, ConversionConfigDto.SectionParameters> sectionParameters
    ) {
        Element element = XMLUtils.createElement(document, null, sectionParameters.getKey());
        String[] fieldNames = sectionParameters.getValue().getFieldNames().split(",");
        String fieldSeparator = sectionParameters.getValue().getFieldSeparator();

        if (fieldSeparator != null) {
            log.debug("parameters contain indication for creation of csv based node, line={}", inputFileLine);
            populateElementWithSeparator(document, element, inputFileLine, fieldNames, fieldSeparator);
        } else {
            log.debug("parameters contain indication for fixed length content, line={}", inputFileLine);
            populateElementWithFixedLengthContent(document, element, inputFileLine, fieldNames, sectionParameters.getValue().getFieldFixedLengths());
        }
        return element;
    }

    private void populateElementWithSeparator(Document doc, Element recordElement, String line, String[] fieldNames, String fieldSeparator) {
        String[] fieldValues = line.split(fieldSeparator);
        boolean anyElementContainsQuotes = Arrays.stream(fieldValues)
                .anyMatch(value -> value.contains("\""));
        if (anyElementContainsQuotes) {
            fieldValues = processSplitValues(fieldValues, fieldSeparator.charAt(0));
        }
        for (int i = 0; i < fieldNames.length; i++) {
            Element fieldElement = XMLUtils.createElement(doc, null, sanitizeTagName(fieldNames[i]));
            String value = (i < fieldValues.length) ? fieldValues[i] : "";
            XMLUtils.appendChild(fieldElement, doc.createTextNode(value));
            XMLUtils.appendChild(recordElement, fieldElement);
        }
    }

    private void populateElementWithFixedLengthContent(Document doc, Element recordElement, String line, String[] fieldNames, String fieldFixedLengths) {
        // Split the fieldFixedLengths to get an array of integers
        int[] lengths = Arrays.stream(fieldFixedLengths.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

        int currentPos = 0;
        for (int i = 0; i < lengths.length && i < fieldNames.length; i++) {
            // Extract the substring based on the fixed length
            int endPos = currentPos + lengths[i];
            String fieldValue = line.substring(currentPos, Math.min(endPos, line.length()));

            // Create the XML node with the field name and value
            Element fieldElement = XMLUtils.createElement(doc, null, sanitizeTagName(fieldNames[i]));
            XMLUtils.appendChild(fieldElement, doc.createTextNode(fieldValue));
            XMLUtils.appendChild(recordElement, fieldElement);

            // Update the current position
            currentPos = endPos;
        }
    }

    private String sanitizeTagName(String input) {
        return input.trim().replaceAll(" ", "");
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

    private static String[] processSplitValues(String[] splitValues, char fieldSeparator) {
        List<String> resultList = new ArrayList<>();

        StringBuilder combinedValue = new StringBuilder();
        boolean insideQuotes = false;

        for (String value : splitValues) {
            if (insideQuotes) {
                combinedValue.append(fieldSeparator).append(value);
            } else {
                combinedValue = new StringBuilder(value);
            }

            if (value.startsWith("\"")) {
                insideQuotes = true;
            }

            if (value.endsWith("\"") && insideQuotes) {
                insideQuotes = false;
                resultList.add(combinedValue.toString().replaceAll("\"", ""));
            } else if (!insideQuotes) {
                resultList.add(value.replaceAll("\"", ""));
            }
        }

        return resultList.toArray(new String[0]);
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

