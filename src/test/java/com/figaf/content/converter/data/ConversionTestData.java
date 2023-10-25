package com.figaf.content.converter.data;

import com.figaf.content.converter.ConversionConfig;
import com.figaf.content.converter.directory.IntegrationDirectoryUtils;
import com.figaf.content.converter.directory.dto.*;
import com.figaf.content.converter.utils.CommonUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
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
                            CommunicationChannel communicationChannel = IntegrationDirectoryUtils.deserializeCommunicationChannel(Files.readAllBytes((testFile)));
                            conversionTestData.setConversionConfig(createConversionConfigDto(communicationChannel));
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

    private static ConversionConfig createConversionConfigDto(CommunicationChannel communicationChannel) {
        ConversionConfig conversionConfig = new ConversionConfig();
        for (GenericPropertyTable table : communicationChannel.getAdapterSpecificTableAttribute()) {
            if ("file.conversionParameters".equals(table.getName())) {
                Map<String, ConversionConfig.SectionParameters> sectionParametersMap = new HashMap<>();
                for (GenericTableRow genericTableRow : table.getValueTableRow()) {
                    Map<String, String> columnNameToValue = new HashMap<>();
                    for (GenericTableRowTableCell cell : genericTableRow.getValueTableCell()) {
                        columnNameToValue.put(cell.getColumnName(), cell.getValue());
                    }
                    String addConvParamNameValue = CommonUtils.isBlank(columnNameToValue.get("file.addConvParamName")) ? "" : columnNameToValue.get("file.addConvParamName").trim();
                    String paramPrefix = addConvParamNameValue.split("\\.")[0];
                    ConversionConfig.SectionParameters sectionParameters = sectionParametersMap.getOrDefault(paramPrefix, new ConversionConfig.SectionParameters());
                    String addConvParamValue = CommonUtils.isBlank(columnNameToValue.get("file.addConvParamValue")) ? "" : columnNameToValue.get("file.addConvParamValue").trim();

                    Map<String, BiConsumer<ConversionConfig.SectionParameters, String>> parameterSetters = new HashMap<>();
                    parameterSetters.put(paramPrefix + ".fieldFixedLengths", ConversionConfig.SectionParameters::setFieldFixedLengths);
                    parameterSetters.put(paramPrefix + ".fieldNames", ConversionConfig.SectionParameters::setFieldNames);
                    parameterSetters.put(paramPrefix + ".keyFieldValue", ConversionConfig.SectionParameters::setKeyFieldValue);
                    parameterSetters.put(paramPrefix + ".fieldSeparator", ConversionConfig.SectionParameters::setFieldSeparator);
                    if (parameterSetters.containsKey(addConvParamNameValue)) {
                        parameterSetters.get(addConvParamNameValue).accept(sectionParameters, addConvParamValue);
                        sectionParametersMap.put(paramPrefix, sectionParameters);
                    } else if (addConvParamNameValue.equals("ignoreRecordsetName")) {
                        conversionConfig.setIgnoreRecordsetName(true);
                    }
                }
                conversionConfig.setSectionParameters(sectionParametersMap);
            }
        }

        for (GenericProperty property : communicationChannel.getAdapterSpecificAttribute()) {
            String trimmedPropertyValue = CommonUtils.isBlank(property.getPropertyValue().getValue()) ? "" : property.getPropertyValue().getValue().trim();
            switch (property.getName()) {
                case "xml.documentName":
                    conversionConfig.setDocumentName(trimmedPropertyValue);
                    break;
                case "xml.documentNamespace":
                    conversionConfig.setDocumentNamespace(trimmedPropertyValue);
                    break;
                case "xml.recordsetStructure":
                    validateRecordsetStructure(trimmedPropertyValue);
                    conversionConfig.setRecordsetStructure(trimmedPropertyValue);
                    break;
                case "xml.recordsetName":
                    conversionConfig.setRecordsetName(trimmedPropertyValue);
                    break;
                case "xml.recordsetNamespace":
                    conversionConfig.setRecordsetNamespace(trimmedPropertyValue);
                    break;
            }
        }

        return conversionConfig;
    }

    private static void validateRecordsetStructure(String value) {
        if (CommonUtils.isBlank(value)) {
            throw new IllegalArgumentException("recordsetStructure shouldn't be empty");
        }
    }
}
