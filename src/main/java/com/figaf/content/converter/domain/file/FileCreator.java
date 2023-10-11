package com.figaf.content.converter.domain.file;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class FileCreator {

    private final static String PRODUCED_FILE_TITLE = "src/test/resources/testdata/";
    private final static String OUTPUT = "/output.xml";

    public static File createOutputFile(String testDataFolderName) throws IOException {
        log.debug("#createUniqueFile: inputFileName={}", testDataFolderName);
        String uniqueFileName = PRODUCED_FILE_TITLE + testDataFolderName + OUTPUT;
        File outputFile = new File(uniqueFileName);
        if (!outputFile.exists()) {
            Files.createFile(Paths.get(uniqueFileName));
        }
        return outputFile;
    }
}
