package com.figaf.content.converter.domain.file;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class FileContentWriter {

    public static byte[] creatXmlOutputFile(
            Document document,
            String testDataFolderName,
            Document xmlDocument
    ) throws TransformerException, IOException {
        log.debug("#writeXMLToFile: document={}, testDataFolderName={}", document, testDataFolderName);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        File outputFile = FileCreator.createOutputFile(testDataFolderName);
        DOMSource source = new DOMSource(xmlDocument);
        StreamResult result = new StreamResult(outputFile);
        transformer.transform(source, result);

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(outputFile.toPath()))) {
            byte[] bytes = new byte[(int) outputFile.length()];
            bufferedInputStream.read(bytes);
            return bytes;
        }
    }

}
