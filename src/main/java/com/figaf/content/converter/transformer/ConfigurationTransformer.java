package com.figaf.content.converter.transformer;


import com.figaf.content.converter.ConversionConfig;
import com.figaf.content.converter.enumaration.ContentConversionType;
import com.figaf.content.converter.transformer.directory.IntegrationDirectoryUtils;
import com.figaf.content.converter.transformer.directory.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public class ConfigurationTransformer {

    public ConversionConfig createConversionConfigFromCommunicationChannel(byte[] communicationChannelFile) {
        CommunicationChannel communicationChannel = IntegrationDirectoryUtils.deserializeCommunicationChannel(communicationChannelFile);
        log.debug("#createConversionConfigFromCommunicationChannel: communicationChannelID ={}", communicationChannel.getCommunicationChannelID());
        return createConversionConfigDto(communicationChannel);
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
                    String addConvParamNameValue = StringUtils.isBlank(columnNameToValue.get("file.addConvParamName")) ? "" : columnNameToValue.get("file.addConvParamName").trim();
                    String paramPrefix = addConvParamNameValue.split("\\.")[0];
                    ConversionConfig.SectionParameters sectionParameters = sectionParametersMap.getOrDefault(paramPrefix, new ConversionConfig.SectionParameters());
                    String addConvParamValue = StringUtils.isBlank(columnNameToValue.get("file.addConvParamValue")) ? "" : columnNameToValue.get("file.addConvParamValue").trim();

                    Map<String, BiConsumer<ConversionConfig.SectionParameters, String>> parameterSetters = new HashMap<>();
                    parameterSetters.put(paramPrefix + ".fieldFixedLengths", ConversionConfig.SectionParameters::setFieldFixedLengths);
                    parameterSetters.put(paramPrefix + ".fieldNames", ConversionConfig.SectionParameters::setFieldNames);
                    parameterSetters.put(paramPrefix + ".keyFieldValue", ConversionConfig.SectionParameters::setKeyFieldValue);
                    parameterSetters.put(paramPrefix + ".fieldSeparator", ConversionConfig.SectionParameters::setFieldSeparator);
                    parameterSetters.put(paramPrefix + ".addHeaderLine", ConversionConfig.SectionParameters::setAddHeaderLine);
                    parameterSetters.put(paramPrefix + ".headerLine", ConversionConfig.SectionParameters::setHeaderLine);
                    parameterSetters.put(paramPrefix + ".fixedLengthTooShortHandling", ConversionConfig.SectionParameters::setFixedLengthTooShortHandling);
                    parameterSetters.put(paramPrefix + ".beginSeparator", ConversionConfig.SectionParameters::setBeginSeparator);
                    parameterSetters.put(paramPrefix + ".endSeparator", ConversionConfig.SectionParameters::setEndSeparator);
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
            String trimmedPropertyValue = StringUtils.isBlank(property.getPropertyValue().getValue()) ? "" : property.getPropertyValue().getValue().trim();
            switch (property.getName()) {
                case "xml.documentName":
                    conversionConfig.setDocumentName(trimmedPropertyValue);
                    break;
                case "xml.documentNamespace":
                    conversionConfig.setDocumentNamespace(trimmedPropertyValue);
                    break;
                case "xml.recordsetStructure":
                    conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
                    processRecordsetStructure(trimmedPropertyValue, conversionConfig);
                    break;
                case "file.recordsetStructure":
                    conversionConfig.setContentConversionType(ContentConversionType.XML_TO_FLAT);
                    processRecordsetStructure(trimmedPropertyValue, conversionConfig);
                    break;
                case "xml.recordsetName":
                    conversionConfig.setRecordsetName(trimmedPropertyValue);
                    break;
                case "xml.recordsetNamespace":
                    conversionConfig.setRecordsetNamespace(trimmedPropertyValue);
                    break;
                case "file.targetFileName":
                    conversionConfig.setTargetFileName(trimmedPropertyValue);
                    break;
            }
        }

        return conversionConfig;
    }

    private static void processRecordsetStructure(String trimmedPropertyValue, ConversionConfig conversionConfig) {
        validateRecordsetStructure(trimmedPropertyValue);
        conversionConfig.setRecordsetStructure(trimmedPropertyValue);
    }

    private static void validateRecordsetStructure(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("recordsetStructure shouldn't be empty");
        }
    }
}
