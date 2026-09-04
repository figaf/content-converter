package com.figaf.content.converter.xml;

import com.figaf.content.converter.ConversionConfig;
import com.figaf.content.converter.utils.XMLUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
            populateElementWithSeparator(document, element, inputFileLine, fieldNames, sectionParameters.getValue());
        } else {
            log.trace("parameters contain indication for fixed length content, line={}", inputFileLine);
            populateElementWithFixedLengthContent(document, element, inputFileLine, fieldNames, sectionParameters.getValue());
        }
        return element;
    }

    private void populateElementWithSeparator(Document doc, Element recordElement, String line, String[] fieldNames, ConversionConfig.SectionParameters sectionParameters) {
        String[] fieldValues = splitLineIntoFieldValues(line, sectionParameters, true);

        // mirrors SAP FCC NameA.additionalLastFields for separator structures: more fields than fieldNames declares.
        // Blank trailing fields are ignored — a delimiter-terminated line like "a;b;c;" is complete, not surplus
        if ("error".equalsIgnoreCase(sectionParameters.getAdditionalLastFields())) {
            int contentFieldCount = fieldValues.length;
            while (contentFieldCount > 0 && StringUtils.isBlank(fieldValues[contentFieldCount - 1])) {
                contentFieldCount--;
            }
            if (contentFieldCount > fieldNames.length) {
                throw new IllegalArgumentException(String.format(
                    "Line has more fields than declared (%d > %d) and additionalLastFields=error, line=%s",
                    contentFieldCount, fieldNames.length, line
                ));
            }
        }

        // mirrors SAP FCC NameA.missingLastFields, same semantics as the fixed-length path
        String missingLastFields = sectionParameters.getMissingLastFields();
        boolean trimFieldContent = shouldTrimFieldContent(sectionParameters);
        for (int i = 0; i < fieldNames.length; i++) {
            if (i >= fieldValues.length) {
                if ("error".equalsIgnoreCase(missingLastFields)) {
                    throw new IllegalArgumentException(String.format(
                        "Line has fewer fields than declared: field '%s' is missing and missingLastFields=error, line=%s",
                        fieldNames[i].trim(), line
                    ));
                }
                if ("ignore".equalsIgnoreCase(missingLastFields)) {
                    // ignore: the remaining fields are absent from the line, leave them out of the XML structure
                    break;
                }
            }
            Element fieldElement = XMLUtils.createElement(doc, null, sanitizeTagName(fieldNames[i]));
            String value = (i < fieldValues.length) ? fieldValues[i] : "";
            if (trimFieldContent) {
                value = value.trim();
            }
            XMLUtils.appendChild(fieldElement, doc.createTextNode(value));
            XMLUtils.appendChild(recordElement, fieldElement);
        }
    }

    /**
     * Splits a line into field values: separators inside an enclosed value do not split it, and the
     * enclosure signs are removed. Matches SAP FCC only for well-formed input with the default
     * enclosureConversion=YES — the known differences are listed in the README. When no sign is
     * configured, a double quote is assumed (historical behavior of this library).
     * Used both for filling the fields and for extracting the key field value, so that record type
     * matching and output always read the same columns.
     */
    static String[] splitLineIntoFieldValues(String line, ConversionConfig.SectionParameters sectionParameters) {
        return splitLineIntoFieldValues(line, sectionParameters, false);
    }

    /**
     * With keepTrailingEmptyFields, a line like "a;b;" yields three values — the trailing empty field
     * counts as present. Used by the field filling, so that missingLastFields/additionalLastFields
     * count fields the way CSV means them. Key field extraction keeps the dropping behavior.
     */
    static String[] splitLineIntoFieldValues(String line, ConversionConfig.SectionParameters sectionParameters, boolean keepTrailingEmptyFields) {
        String fieldSeparator = sectionParameters.getFieldSeparator();
        String[] fieldValues = line.split(Pattern.quote(fieldSeparator), keepTrailingEmptyFields ? -1 : 0);
        String enclosureSign = resolveEnclosureSign(sectionParameters);
        String enclosureSignEnd = resolveEnclosureSignEnd(sectionParameters, enclosureSign);
        boolean anyValueContainsEnclosureSign = Arrays.stream(fieldValues)
            .anyMatch(value -> value.contains(enclosureSign) || value.contains(enclosureSignEnd));
        if (anyValueContainsEnclosureSign) {
            fieldValues = processSplitValues(fieldValues, fieldSeparator, enclosureSign, enclosureSignEnd);
        }
        return fieldValues;
    }

    /**
     * Removes the configured enclosure signs from a value. Used to normalize the configured keyFieldValue:
     * real PI channels store it with or without the signs ("H" or H), both must match.
     */
    static String removeEnclosureSigns(String value, ConversionConfig.SectionParameters sectionParameters) {
        String enclosureSign = resolveEnclosureSign(sectionParameters);
        return removeEnclosureSigns(value, enclosureSign, resolveEnclosureSignEnd(sectionParameters, enclosureSign));
    }

    private static String resolveEnclosureSign(ConversionConfig.SectionParameters sectionParameters) {
        return StringUtils.isNotEmpty(sectionParameters.getEnclosureSign())
            ? sectionParameters.getEnclosureSign()
            : DOUBLE_QUOTE;
    }

    private static String resolveEnclosureSignEnd(ConversionConfig.SectionParameters sectionParameters, String enclosureSign) {
        return StringUtils.isNotEmpty(sectionParameters.getEnclosureSignEnd())
            ? sectionParameters.getEnclosureSignEnd()
            : enclosureSign;
    }

    private void populateElementWithFixedLengthContent(Document doc, Element recordElement, String line, String[] fieldNames, ConversionConfig.SectionParameters sectionParameters) {
        // Split the fieldFixedLengths to get an array of integers
        int[] lengths = Arrays.stream(sectionParameters.getFieldFixedLengths().split(COMMA))
            .mapToInt(Integer::parseInt)
            .toArray();

        // mirrors SAP FCC NameA.missingLastFields: how to handle a line with fewer fields than declared.
        // The library default is "add" (missing trailing fields become empty elements) to keep backward compatibility
        String missingLastFields = sectionParameters.getMissingLastFields();

        // mirrors SAP FCC NameA.additionalLastFields (SAP defaults to error for fixed lengths, this library keeps
        // ignore for backward compatibility). Trailing blanks are padding, not surplus content
        if ("error".equalsIgnoreCase(sectionParameters.getAdditionalLastFields())) {
            int totalDeclaredLength = Arrays.stream(lengths).sum();
            int contentLength = StringUtils.stripEnd(line, null).length();
            if (contentLength > totalDeclaredLength) {
                throw new IllegalArgumentException(String.format(
                    "Line is longer than the declared structure (%d > %d) and additionalLastFields=error, line=%s",
                    contentLength, totalDeclaredLength, line
                ));
            }
        }

        boolean trimFieldContent = shouldTrimFieldContent(sectionParameters);
        int currentPos = 0;
        for (int i = 0; i < lengths.length && i < fieldNames.length; i++) {
            if (currentPos >= line.length() && ("ignore".equalsIgnoreCase(missingLastFields) || "error".equalsIgnoreCase(missingLastFields))) {
                if ("error".equalsIgnoreCase(missingLastFields)) {
                    throw new IllegalArgumentException(String.format(
                        "Line is shorter than the declared structure: field '%s' is missing and missingLastFields=error, line=%s",
                        fieldNames[i].trim(), line
                    ));
                }
                // ignore: the remaining fields are absent from the line, leave them out of the XML structure
                break;
            }

            // Extract the substring based on the fixed length.
            int endPos = currentPos + lengths[i];
            String fieldValue = line.substring(Math.min(currentPos, line.length()), Math.min(endPos, line.length()));
            if (trimFieldContent) {
                fieldValue = fieldValue.trim();
            }

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

    // mirrors SAP FCC NameA.fieldContentFormatting: trim (default) or nothing
    private static boolean shouldTrimFieldContent(ConversionConfig.SectionParameters sectionParameters) {
        return !"nothing".equalsIgnoreCase(sectionParameters.getFieldContentFormatting());
    }

    private static String[] processSplitValues(String[] splitValues, String fieldSeparator, String enclosureSign, String enclosureSignEnd) {
        List<String> resultList = new ArrayList<>();
        boolean insideEnclosure = false;
        StringBuilder combinedValue = new StringBuilder();
        for (String splitValue : splitValues) {
            if (insideEnclosure) {
                combinedValue.append(fieldSeparator);
            }

            String clearedFromEnclosureSignsValue = removeEnclosureSigns(splitValue, enclosureSign, enclosureSignEnd);
            combinedValue.append(clearedFromEnclosureSignsValue);

            if (splitValue.startsWith(enclosureSign) && !insideEnclosure) {
                insideEnclosure = true;
            }

            if (splitValue.endsWith(enclosureSignEnd) && insideEnclosure) {
                resultList.add(combinedValue.toString());
                combinedValue.setLength(0);
                insideEnclosure = false;
            } else if (!insideEnclosure) {
                resultList.add(clearedFromEnclosureSignsValue);
                combinedValue.setLength(0);
            }
        }

        // an enclosure that is never closed runs to the end of the line, like in common CSV parsers —
        // dropping it would silently lose every field after the unbalanced sign
        if (insideEnclosure) {
            resultList.add(combinedValue.toString());
        }

        return resultList.toArray(new String[0]);
    }

    private static String removeEnclosureSigns(String value, String enclosureSign, String enclosureSignEnd) {
        String clearedValue = value.replace(enclosureSign, "");
        if (!enclosureSignEnd.equals(enclosureSign)) {
            clearedValue = clearedValue.replace(enclosureSignEnd, "");
        }
        return clearedValue;
    }
}
