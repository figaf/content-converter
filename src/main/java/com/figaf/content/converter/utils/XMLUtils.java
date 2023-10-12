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
}