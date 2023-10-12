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

    public static byte[] createXmlOutputFile(
            Document document,
            String testDataFolderName,
            Document xmlDocument
    ) throws TransformerException, IOException {
        log.debug("#createXmlOutputFile: document={}, testDataFolderName={}, xmlDocument={}", document, testDataFolderName, xmlDocument);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        File outputFile = FileCreator.createOutputFile(testDataFolderName);
        DOMSource source = new DOMSource(xmlDocument);
        StreamResult streamResult = new StreamResult(outputFile);
        transformer.transform(source, streamResult);

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(outputFile.toPath()))) {
            byte[] bytes = new byte[(int) outputFile.length()];
            int bytesRead = 0;
            while (bytesRead < bytes.length) {
                int result = bufferedInputStream.read(bytes, bytesRead, bytes.length - bytesRead);
                if (result == -1) {
                    break;
                }
                bytesRead += result;
            }
            return bytes;
        }
    }
}
