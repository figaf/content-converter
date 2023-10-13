package com.figaf.content.converter.domain.strategy;

import com.figaf.content.converter.dto.ConversionConfigDto;
import com.figaf.content.converter.utils.XMLUtils;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class NodeCreationStrategy {

    public  boolean shouldNotSkipRecordsetCreation(ConversionConfigDto conversionConfigDto, boolean singleKeyMapping) {
        return !singleKeyMapping && !conversionConfigDto.isIgnoreRecordsetName();
    }

    public  boolean shouldCreateNewRecordsetForMultipleKeyRecords(
            ConversionConfigDto conversionConfigDto,
            boolean singleKeyMapping,
            Map<String, ConversionConfigDto.SectionParameters> keyRecordToSectionParameters,
            String firstKeyRecord
    ) {
        return !conversionConfigDto.isIgnoreRecordsetName() &&
                !singleKeyMapping &&
                keyRecordToSectionParameters.keySet().iterator().next().equals(firstKeyRecord);
    }

    public void createNodesFromInputLine(
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
}
