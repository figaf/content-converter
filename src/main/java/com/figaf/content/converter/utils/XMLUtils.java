package com.figaf.content.converter.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XMLUtils {

    public static Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    public static Element createElement(Document document, String tagName) {
        return document.createElement(tagName);
    }

    public static Element createElement(Document document, String namespace, String tagName) {
        return (namespace == null || namespace.isEmpty())
                ? createElement(document, tagName)
                : document.createElementNS(namespace, "ns:" + tagName);
    }

    public static void appendChild(Document document, Element element) {
        document.appendChild(element);
    }

    public static void appendChild(Element parent, Element child) {
        parent.appendChild(child);
    }

    public static void appendChild(Element parent, Text child) {
        parent.appendChild(child);
    }

    public static byte[] writeDocumentToByteArray(Document xmlDocument, boolean beautify) throws TransformerException {
        if (xmlDocument == null) {
            throw new IllegalArgumentException("Provided XML document is null.");
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;

        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new TransformerException("Failed to create a new transformer instance.", e);
        }

        if (beautify) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamResult streamResult = new StreamResult(byteArrayOutputStream);
        DOMSource domSource = new DOMSource(xmlDocument);

        try {
            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            throw new TransformerException("Error transforming the DOM source.", e);
        }

        return byteArrayOutputStream.toByteArray();
    }
}