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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ByteArrayOutputStream;

@Slf4j
public class XmlProcessor {

    public byte[] processXmlDocument(Document xmlDocument, String testDataFolderName)
            throws TransformerException, IOException {
        log.debug("#processXmlDocument: xmlDocument={}, testDataFolderName={}", xmlDocument, testDataFolderName);
        Path testDataFolderDir = Paths.get(testDataFolderName);
        if (Files.exists(testDataFolderDir) && Files.isDirectory(testDataFolderDir)) {
            File outputFile = writeXmlContentToFile(xmlDocument, testDataFolderName);
            return loadFile(outputFile);
        } else {
            return loadFromDomSource(xmlDocument);
        }
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

    private byte[] loadFromDomSource(Document xmlDocument) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamResult streamResult = new StreamResult(byteArrayOutputStream);
        DOMSource domSource = new DOMSource(xmlDocument);
        transformer.transform(domSource, streamResult);
        return byteArrayOutputStream.toByteArray();
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
