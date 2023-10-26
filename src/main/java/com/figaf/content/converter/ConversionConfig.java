package com.figaf.content.converter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Represents the configuration for converting a flat file with complex structures to an XML format.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ConversionConfig {

    /**
     * Defines the sequence and the number of substructures in a recordset.
     */
    private String recordsetStructure;

    /**
     * The name of the XML document, inserted in the message as the main XML tag.
     */
    private String documentName;

    /**
     * The name of the recordset, included in the XML schema.
     */
    private String recordsetName;

    /**
     * The namespace appended to the name of the recordset structure.
     */
    private String recordsetNamespace;

    /**
     * The namespace added to the name of the document.
     */
    private String documentNamespace;

    /**
     * If set to true, the Recordset element is not inserted in the XML structure.
     */
    private boolean ignoreRecordsetName;

    /**
     * A map containing parameters for each specified recordset structure.
     */
    private Map<String, SectionParameters> sectionParameters;

    /**
     * If set to true, beautifies the output XML.
     */
    private boolean beautifyOutput;

    /**
     * Represents the parameters for a specified recordset structure.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class SectionParameters {

        /**
         * Contains the lengths of the structure columns as arguments separated by commas.
         */
        private String fieldFixedLengths;

        /**
         * Names of the structure columns.
         */
        private String fieldNames;

        /**
         * The value of the key field for the structure.
         */
        private String keyFieldValue;

        /**
         * The character string used as a separator between the individual columns.
         */
        private String fieldSeparator;
    }
}
