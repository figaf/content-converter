package com.figaf.content.converter.xml;

import com.figaf.content.converter.ConversionConfig;
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

    private static final String DOUBLE_QUOTE = "\"";
    private static final String COMMA = ",";

    public boolean shouldNotSkipRecordsetCreation(ConversionConfig conversionConfig, boolean singleKeyMapping) {
        return !singleKeyMapping && !conversionConfig.isIgnoreRecordsetName();
    }

    public boolean shouldCreateNewRecordsetForMultipleKeyRecords(
        ConversionConfig conversionConfig,
        boolean singleKeyMapping,
        Map<String, ConversionConfig.SectionParameters> keyRecordToSectionParameters,
        String firstKeyRecord
    ) {
        return !conversionConfig.isIgnoreRecordsetName() &&
            !singleKeyMapping &&
            keyRecordToSectionParameters.keySet().iterator().next().equals(firstKeyRecord);
    }

    public void createNodesFromInputLine(
        String inputLine,
        Document document,
        Element root,
        Map<String, ConversionConfig.SectionParameters> keyRecordToSectionParameters,
        Element recordSetTag,
        boolean singleKeyMapping
    ) {
        for (Map.Entry<String, ConversionConfig.SectionParameters> sectionParameters : keyRecordToSectionParameters.entrySet()) {
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
        Map.Entry<String, ConversionConfig.SectionParameters> sectionParameters
    ) {
        Element element = XMLUtils.createElement(document, null, sectionParameters.getKey());
        String[] fieldNames = sectionParameters.getValue().getFieldNames().split(COMMA);
        String fieldSeparator = sectionParameters.getValue().getFieldSeparator();

        if (fieldSeparator != null) {
            log.trace("parameters contain indication for creation of csv based node, line={}", inputFileLine);
            populateElementWithSeparator(document, element, inputFileLine, fieldNames, fieldSeparator);
        } else {
            log.trace("parameters contain indication for fixed length content, line={}", inputFileLine);
            populateElementWithFixedLengthContent(document, element, inputFileLine, fieldNames, sectionParameters.getValue().getFieldFixedLengths());
        }
        return element;
    }

    private void populateElementWithSeparator(Document doc, Element recordElement, String line, String[] fieldNames, String fieldSeparator) {
        String[] fieldValues = line.split(fieldSeparator);
        boolean anyElementContainsQuotes = Arrays.stream(fieldValues)
            .anyMatch(value -> value.contains(DOUBLE_QUOTE));
        if (anyElementContainsQuotes) {
            fieldValues = processSplitValues(fieldValues, fieldSeparator);
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
        int[] lengths = Arrays.stream(fieldFixedLengths.split(COMMA))
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

    private static String[] processSplitValues(String[] splitValues, String fieldSeparator) {
        List<String> resultList = new ArrayList<>();
        boolean insideQuotes = false;
        StringBuilder combinedValue = new StringBuilder();
        for (String splitValue : splitValues) {
            if (insideQuotes) {
                combinedValue.append(fieldSeparator);
            }

            String clearedFromQuotesValue = splitValue.replaceAll(DOUBLE_QUOTE, "");
            combinedValue.append(clearedFromQuotesValue);

            if (splitValue.startsWith(DOUBLE_QUOTE) && !insideQuotes) {
                insideQuotes = true;
            }

            if (splitValue.endsWith(DOUBLE_QUOTE) && insideQuotes) {
                resultList.add(combinedValue.toString());
                combinedValue.setLength(0);
                insideQuotes = false;
            } else if (!insideQuotes) {
                resultList.add(clearedFromQuotesValue);
                combinedValue.setLength(0);
            }
        }

        return resultList.toArray(new String[0]);
    }
}
