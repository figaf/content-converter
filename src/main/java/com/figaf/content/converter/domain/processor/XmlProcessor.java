package com.figaf.content.converter.domain.processor;

import com.figaf.content.converter.domain.file.FileCreator;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class XmlProcessor {

    public byte[] processXmlDocument(Document xmlDocument, String testDataFolderName) throws TransformerException, IOException {
        log.debug("#processXmlDocument: xmlDocument={}, testDataFolderName={}", xmlDocument, testDataFolderName);
        Path testDataFolderDir = Paths.get(testDataFolderName);
        if (Files.exists(testDataFolderDir) && Files.isDirectory(testDataFolderDir)) {
            File outputFile = writeXmlContentToFile(xmlDocument, testDataFolderName);
            return loadFile(outputFile);
        } else {
            return loadFromDomSource(xmlDocument);
        }
    }

    private File writeXmlContentToFile(Document xmlDocument, String testDataFolderName) throws TransformerException, IOException {
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

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

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

    private byte[] loadFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Provided file is null.");
        }

        if (!file.exists() || !file.canRead()) {
            throw new FileNotFoundException("File does not exist or is not readable: " + file.getAbsolutePath());
        }

        long fileLength = file.length();
        if (fileLength > Integer.MAX_VALUE) {
            throw new IOException("File is too large to be read into memory: " + file.getAbsolutePath());
        }

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            byte[] bytes = new byte[(int) fileLength];
            int bytesRead = 0;
            while (bytesRead < bytes.length) {
                int result = bufferedInputStream.read(bytes, bytesRead, bytes.length - bytesRead);
                if (result == -1) {
                    throw new IOException("Unexpected end of file reached before reading fully: " + file.getAbsolutePath());
                }
                bytesRead += result;
            }
            return bytes;
        } catch (SecurityException e) {
            throw new IOException("Permission denied to read the file: " + file.getAbsolutePath(), e);
        } catch (InvalidPathException | IOException e) {
            throw new IOException("Invalid file path: " + file.getAbsolutePath(), e);
        }
    }
}
