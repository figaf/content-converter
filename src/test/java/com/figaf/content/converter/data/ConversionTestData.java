package com.figaf.content.converter.data;

import com.figaf.content.converter.dto.ConversionConfigDto;
import com.figaf.content.converter.exception.ApplicationException;
import com.figaf.content.converter.transformer.IntegrationDirectoryUtils;
import com.figaf.integration.directory.dto.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
@ToString(of = {"testDataFolderName", "conversionConfigDto"})
public class ConversionTestData {

    private String testDataFolderName;
    private byte[] inputFileBytes;
    private byte[] expectedOutboundFileBytes;
    private ConversionConfigDto conversionConfigDto;

    public static ConversionTestData parseFolder(Path folderPath) {
        log.debug("#parseFolder: folderPath={}", folderPath);
        try (Stream<Path> paths = Files.list(folderPath)) {
            List<Path> testFiles = paths
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            ConversionTestData conversionTestData = new ConversionTestData();
            conversionTestData.setTestDataFolderName(folderPath.getFileName().toString());
            return createConversionTestData(testFiles, conversionTestData);
        } catch (Exception e) {
            log.error("Error trying to parse folder: {}", e.getMessage(), e);
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    private static ConversionTestData createConversionTestData(List<Path> testFiles, ConversionTestData conversionTestData) {
        for (Path testFile : testFiles) {
            String fileName = testFile.getFileName().toString();
            try {
                if (fileName.contains("input")) {
                    conversionTestData.setInputFileBytes(Files.readAllBytes(testFile));
                } else if (fileName.contains("output")) {
                    conversionTestData.setExpectedOutboundFileBytes(Files.readAllBytes((testFile)));
                } else {
                    CommunicationChannel communicationChannel = IntegrationDirectoryUtils.deserializeCommunicationChannel(Files.readAllBytes((testFile)));
                    conversionTestData.setConversionConfigDto(createConversionConfigDto(communicationChannel));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return conversionTestData;
    }

    private static ConversionConfigDto createConversionConfigDto(CommunicationChannel communicationChannel) {
        ConversionConfigDto conversionConfigDto = new ConversionConfigDto();
        for (GenericPropertyTable table : communicationChannel.getAdapterSpecificTableAttribute()) {
            if ("file.conversionParameters".equals(table.getName())) {
                Map<String, ConversionConfigDto.SectionParameters> sectionParametersMap = new HashMap<>();
                for (GenericTableRow genericTableRow : table.getValueTableRow()) {
                    Map<String, String> columnNameToValue = new HashMap<>();
                    for (GenericTableRowTableCell cell : genericTableRow.getValueTableCell()) {
                        columnNameToValue.put(cell.getColumnName(), cell.getValue());
                    }
                    String paramPrefix = columnNameToValue.get("file.addConvParamName").split("\\.")[0];
                    ConversionConfigDto.SectionParameters sectionParameters = sectionParametersMap.getOrDefault(paramPrefix, new ConversionConfigDto.SectionParameters());
                    String addConvParamNameValue = columnNameToValue.get("file.addConvParamName");
                    String addConvParamValue = columnNameToValue.get("file.addConvParamValue");

                    Map<String, BiConsumer<ConversionConfigDto.SectionParameters, String>> parameterSetters = new HashMap<>();
                    parameterSetters.put(paramPrefix + ".fieldFixedLengths", ConversionConfigDto.SectionParameters::setFieldFixedLengths);
                    parameterSetters.put(paramPrefix + ".fieldNames", ConversionConfigDto.SectionParameters::setFieldNames);
                    parameterSetters.put(paramPrefix + ".keyFieldValue", ConversionConfigDto.SectionParameters::setKeyFieldValue);
                    parameterSetters.put(paramPrefix + ".fieldSeparator", ConversionConfigDto.SectionParameters::setFieldSeparator);
                    if (parameterSetters.containsKey(addConvParamNameValue)) {
                        parameterSetters.get(addConvParamNameValue).accept(sectionParameters, addConvParamValue);
                        sectionParametersMap.put(paramPrefix, sectionParameters);
                    } else if (addConvParamNameValue.equals("ignoreRecordsetName")) {
                        conversionConfigDto.setIgnoreRecordsetName(true);
                    }
                }
                conversionConfigDto.setSectionParameters(sectionParametersMap);
            }
        }

        for (GenericProperty property : communicationChannel.getAdapterSpecificAttribute()) {
            switch (property.getName()) {
                case "xml.documentName":
                    conversionConfigDto.setDocumentName(property.getPropertyValue().getValue());
                    break;
                case "xml.documentNamespace":
                    conversionConfigDto.setDocumentNamespace(property.getPropertyValue().getValue());
                    break;
                case "xml.recordsetStructure":
                    conversionConfigDto.setRecordsetStructure(property.getPropertyValue().getValue());
                case "xml.recordsetStructureOrder":
                    conversionConfigDto.setRecordsetStructureOrder(property.getPropertyValue().getValue());
                    break;
                case "xml.recordsetName":
                    conversionConfigDto.setRecordsetName(property.getPropertyValue().getValue());
                    break;
                case "xml.recordsetNamespace":
                    conversionConfigDto.setRecordsetNamespace(property.getPropertyValue().getValue());
                    break;
            }
        }

        return conversionConfigDto;
    }
}
