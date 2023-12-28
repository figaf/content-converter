package com.figaf.content.converter.xml;

import com.figaf.content.converter.ContentConversionException;
import com.figaf.content.converter.ContentConverter;
import com.figaf.content.converter.ConversionConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;


@Slf4j
public class XmlToFlatContentConverter implements ContentConverter {


    @Override
    public byte[] convert(
        byte[] flatDocument,
        ConversionConfig conversionConfig
    ) {
        try {
            validateInputArgs(flatDocument, conversionConfig);
            return convert(byteArrayToDocument(flatDocument), conversionConfig).getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new ContentConversionException("Couldn't convert XML file to txt", ex);
        }
    }

    @Override
    public String convert(
        String xmlDocument,
        ConversionConfig conversionConfig
    ) {
        try {
            validateInputArgs(xmlDocument, conversionConfig);
            return convert(byteArrayToDocument(xmlDocument.getBytes(StandardCharsets.UTF_8)), conversionConfig);
        } catch (Exception ex) {
            throw new ContentConversionException("Couldn't convert XML file to txt", ex);
        }
    }

    private String convert(
        Document inputXml,
        ConversionConfig conversionConfig
    ) {
        log.debug("#convert: conversionConfig={}", conversionConfig);

        StringBuilder txtOutput = new StringBuilder();
        Element root = inputXml.getDocumentElement();
        boolean hasComputedHeader = false;

        NodeList children = root.getChildNodes();
        boolean structureContainsSingleElement = conversionConfig.getSectionParameters().size() == 1;
        int lastElementIndex = findLastElementIndex(children);

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element element = (Element) children.item(i);
                ConversionConfig.SectionParameters params = conversionConfig.getSectionParameters().get(element.getNodeName());
                if (!Optional.ofNullable(params).isPresent()) {
                    log.warn("not found SectionParameters for key {}", element.getNodeName());
                    continue;
                }
                String[] fixedLengths = StringUtils.isBlank(params.getFieldFixedLengths()) ? null : params.getFieldFixedLengths().split(",");
                if (structureContainsSingleElement && !hasComputedHeader) {
                    NodeList childrenOfElement = element.getChildNodes();
                    computeHeaderLine(
                        params,
                        childrenOfElement,
                        fixedLengths,
                        txtOutput
                    );
                    hasComputedHeader = true;
                }

                boolean isLastElement = (i == lastElementIndex);
                processElement(element, params, txtOutput, fixedLengths, isLastElement);
            }
        }
        return txtOutput.toString();
    }

    private int findLastElementIndex(NodeList nodeList) {
        return IntStream.iterate(nodeList.getLength() - 1, i -> i - 1)
            .limit(nodeList.getLength())
            .filter(i -> nodeList.item(i) instanceof Element)
            .findFirst()
            .orElse(-1);
    }

    private void processElement(
        Element element,
        ConversionConfig.SectionParameters params,
        StringBuilder txtOutput,
        String[] fixedLengths,
        boolean isLastElement
    ) {
        log.debug(
            "processElement: element={}, params={}, txtOutput={}, fixedLengths={}",
            element,
            params,
            txtOutput,
            fixedLengths
        );
        NodeList children = element.getChildNodes();

        setBeginSeparator(params, txtOutput);
        appendFormattedChildElements(children, fixedLengths, params, txtOutput);
        setEndSeparator(params, txtOutput, isLastElement);
    }

    private void appendFormattedChildElements(NodeList children, String[] fixedLengths, ConversionConfig.SectionParameters params, StringBuilder txtOutput) {
        int indexOfLengthLimit = 0;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element valueElement = (Element) children.item(i);
                String content = valueElement.getTextContent();

                if (fixedLengths != null && fixedLengths.length > indexOfLengthLimit) {
                    Integer charsLimit = Integer.parseInt(fixedLengths[indexOfLengthLimit]);
                    content = applyFixedLengthHandling(content, params, charsLimit);
                }
                txtOutput.append(content);
                if (hasNextElement(children, i) && StringUtils.isNotBlank(params.getFieldSeparator())) {
                    txtOutput.append(params.getFieldSeparator());
                }
                indexOfLengthLimit++;
            }
        }
    }

    private void setHeaderLine(
        ConversionConfig.SectionParameters params,
        NodeList children,
        MutableBoolean hasComputedHeader,
        boolean structureContainsSingleElement,
        String[] fixedLengths,
        StringBuilder txtOutput
    ) {
        if (structureContainsSingleElement && !hasComputedHeader.booleanValue()) {
            computeHeaderLine(params, children, fixedLengths, txtOutput);
            hasComputedHeader.setTrue();
        }
    }

    private void setBeginSeparator(ConversionConfig.SectionParameters params, StringBuilder txtOutput) {
        if (StringUtils.isNotBlank(params.getBeginSeparator())) {
            txtOutput.append(params.getBeginSeparator());
        }
    }

    private void setEndSeparator(ConversionConfig.SectionParameters params, StringBuilder txtOutput, boolean isLastElement) {
        String finalEndSeparator = StringUtils.isNotBlank(params.getEndSeparator()) ? params.getEndSeparator() : createDefaultEndSeparator(isLastElement);
        txtOutput.append(finalEndSeparator);
    }

    private String createDefaultEndSeparator(boolean isLastElement) {
        return isLastElement ? StringUtils.EMPTY : System.lineSeparator();
    }

    private void computeHeaderLine(ConversionConfig.SectionParameters params, NodeList children, String[] fixedLengths, StringBuilder result) {
        String headerLineOption = params.getAddHeaderLine();
        switch (headerLineOption) {
            case "0":
                break;
            case "1":
                result.append(getHeaderLine(children, params, fixedLengths));
                break;
            case "2":
                result.append(getHeaderLine(children, params, fixedLengths)).append(System.lineSeparator());
                break;
            case "3":
                result.append(params.getHeaderLine());
                break;
            case "4":
                result.append(params.getHeaderLine()).append(System.lineSeparator());
                break;
        }
    }

    private boolean hasNextElement(NodeList nodeList, int currentIndex) {
        for (int i = currentIndex + 1; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    private String getHeaderLine(NodeList children, ConversionConfig.SectionParameters parameters, String[] fixedLengths) {
        String finalFieldSeparator = StringUtils.defaultIfBlank(parameters.getFieldSeparator(), StringUtils.EMPTY);
        boolean isFixedLength = fixedLengths != null && fixedLengths.length > 0;

        StringBuilder header = new StringBuilder();
        int indexOfLengthLimit = 0;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) children.item(i);
                String headerElementName = child.getNodeName();
                if (isFixedLength) {
                    Integer charsLimit = Integer.parseInt(fixedLengths[indexOfLengthLimit]);
                    headerElementName = applyFixedLengthHandling(child.getNodeName(), parameters, charsLimit);
                    header.append(headerElementName);
                    indexOfLengthLimit++;
                    continue;
                }
                header.append(headerElementName);
                if (hasNextElement(children, i) && StringUtils.isNotBlank(finalFieldSeparator)) {
                    header.append(finalFieldSeparator);
                }
            }
        }
        header.append(System.lineSeparator());
        return header.toString();
    }

    private String applyFixedLengthHandling(String content, ConversionConfig.SectionParameters params, Integer charsLimit) {
        String handlingType = params.getFixedLengthTooShortHandling();
        StringBuilder processedContent = new StringBuilder();

        switch (handlingType) {
            case "Error":
                throw new IllegalArgumentException("Content length exceeds fixed length");
            case "Cut":
                if (content.length() > charsLimit) {
                    content = content.substring(0, charsLimit);
                } else {
                    content = String.format("%-" + charsLimit + "s", content);
                }
                break;
            default:
                break;
        }

        processedContent.append(content);
        return processedContent.toString();
    }

    /**
     * Converts a byte array representing an XML document to a Document object.
     *
     * @param flatDocument The byte array containing the XML document.
     * @return The Document object representing the XML document.
     * @throws Exception If an error occurs during parsing.
     */
    public Document byteArrayToDocument(byte[] flatDocument) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        try (ByteArrayInputStream input = new ByteArrayInputStream(flatDocument)) {
            return builder.parse(new InputSource(input));
        }
    }

    private void validateInputArgs(byte[] xmlDocument, ConversionConfig conversionConfig) {
        List<String> errorMessages = new ArrayList<>();

        if (xmlDocument == null || xmlDocument.length == 0) {
            errorMessages.add("xmlDocument must not be empty");
        }

        errorMessages.addAll(validateCommonInputArgs(conversionConfig));
        throwExceptionIfErrorsPresent(errorMessages);
    }

    private void validateInputArgs(String xmlDocument, ConversionConfig conversionConfig) {
        List<String> errorMessages = new ArrayList<>();

        if (StringUtils.isBlank(xmlDocument)) {
            errorMessages.add("xmlDocument must not be empty");
        }

        errorMessages.addAll(validateCommonInputArgs(conversionConfig));
        throwExceptionIfErrorsPresent(errorMessages);
    }

    private List<String> validateCommonInputArgs(ConversionConfig conversionConfig) {
        List<String> errors = new ArrayList<>();

        if (StringUtils.isEmpty(conversionConfig.getRecordsetStructure())) {
            errors.add("Recordset structure is missing.");
        }
        if (conversionConfig.getSectionParameters() == null || conversionConfig.getSectionParameters().isEmpty()) {
            errors.add("No section parameters provided.");
        }
        return errors;
    }

    private void throwExceptionIfErrorsPresent(List<String> errorMessages) {
        if (!errorMessages.isEmpty()) {
            String combinedErrorMessage = String.format("Conversion arguments are not valid:\n%s", String.join("\n", errorMessages));
            log.error(combinedErrorMessage);
            throw new IllegalArgumentException(combinedErrorMessage);
        }
    }
}