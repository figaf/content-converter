package com.figaf.content.converter.transformer;


import com.figaf.content.converter.ConversionConfig;
import com.figaf.content.converter.transformer.directory.IntegrationDirectoryUtils;
import com.figaf.content.converter.transformer.directory.dto.*;
import com.figaf.content.converter.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

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
