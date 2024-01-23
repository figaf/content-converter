package com.figaf.content.converter.data;

import com.figaf.content.converter.ConversionConfig;
import com.figaf.content.converter.enumeration.LineEnding;
import com.figaf.content.converter.transformer.ConfigurationTransformer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
@ToString(of = {"testDataFolderPath", "conversionConfig"})
public class ConversionTestData {

    private Path testDataFolderPath;
    private byte[] inputDocument;
    private byte[] expectedConvertedDocument;
    private ConversionConfig conversionConfig;

    public static ConversionTestData createTestData(Path testDataFolderPath, boolean beautifyOutput) {
        log.debug("#parseFolder: testDataFolderPath={}", testDataFolderPath);
        ConversionTestData conversionTestData = new ConversionTestData();
        conversionTestData.setTestDataFolderPath(testDataFolderPath);
        enrichConversionTestDataWithDocumentsAndConfig(conversionTestData);
        conversionTestData.getConversionConfig().setBeautifyOutput(beautifyOutput);
        return conversionTestData;
    }

    private static void enrichConversionTestDataWithDocumentsAndConfig(ConversionTestData conversionTestData) {
        try (Stream<Path> paths = Files.list(conversionTestData.getTestDataFolderPath())) {
            paths
                .filter(Files::isRegularFile)
                .forEach(testFile -> {
                    String fileName = testFile.getFileName().toString();
                    try {
                        if (fileName.contains("input")) {
                            conversionTestData.setInputDocument(Files.readAllBytes(testFile));
                        } else if (fileName.contains("expected-output")) {
                            conversionTestData.setExpectedConvertedDocument(Files.readAllBytes((testFile)));
                        } else if (fileName.equals("channel.xml")) {
                            ConfigurationTransformer configurationTransformer = new ConfigurationTransformer();
                            conversionTestData.setConversionConfig(configurationTransformer.createConversionConfigFromCommunicationChannel(Files.readAllBytes((testFile)), LineEnding.AUTO));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        } catch (Exception e) {
            log.error("Error trying to parse folder: {}", e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
