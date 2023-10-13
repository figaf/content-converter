package com.figaf.content.converter.domain.processor;

import com.figaf.content.converter.domain.file.FileCreator;
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
public class XmlProcessor {

    public byte[] processXmlDocument(Document xmlDocument, String testDataFolderName)
            throws TransformerException, IOException {
        log.debug("#processXmlDocument: xmlDocument={}, testDataFolderName={}", xmlDocument, testDataFolderName);
        File outputFile = writeXmlContentToFile(xmlDocument, testDataFolderName);
        return loadFile(outputFile);
    }

    private File writeXmlContentToFile(Document xmlDocument, String testDataFolderName)
            throws TransformerException, IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        File outputFile = FileCreator.createOutputFile(testDataFolderName);
        DOMSource source = new DOMSource(xmlDocument);
        StreamResult streamResult = new StreamResult(outputFile);
        transformer.transform(source, streamResult);

        return outputFile;
    }

    private byte[] loadFile(File file) throws IOException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            byte[] bytes = new byte[(int) file.length()];
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
