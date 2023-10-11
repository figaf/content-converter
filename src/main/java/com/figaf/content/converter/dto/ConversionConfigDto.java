package com.figaf.content.converter.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ConversionConfigDto {

    private String recordsetStructure;

    private String documentName;

    private String recordsetName;

    private String recordsetStructureOrder;

    private String recordsetNamespace;

    private String documentNamespace;

    private boolean ignoreRecordsetName;

    private Map<String, SectionParameters> sectionParameters;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class SectionParameters {
        private String fieldFixedLengths;
        private String fieldNames;
        private String keyFieldValue;
        private String fieldSeparator;
    }
}


