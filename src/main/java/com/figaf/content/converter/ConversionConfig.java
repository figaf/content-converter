package com.figaf.content.converter;

import com.figaf.content.converter.enumaration.ContentConversionType;
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
     * The name of the recordset, included in the XML schema.Has effect only for Flat->XML conversion
     */
    private String recordsetName;

    /**
     * The namespace appended to the name of the recordset structure.
     */
    private String recordsetNamespace;

    /**
     * The target file name in case output its not xml.Has effect only for XML->Flat conversion
     */
    private String targetFileName;

    /**
     * The namespace added to the name of the document.
     */
    private String documentNamespace;

    /**
     * If set to true, the Recordset element is not inserted in the XML structure.Has effect only for Flat->XML conversion
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
     * identification field for choosing conversion implementation.
     */
    private ContentConversionType contentConversionType;
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
         * Names of the structure columns.Has effect only for Flat->XML conversion
         */
        private String fieldNames;

        /**
         * The value of the key field for the structure.Has effect only for Flat->XML conversion
         */
        private String keyFieldValue;

        /**
         * The character string used as a separator between the individual columns.
         */
        private String fieldSeparator;

        /**
         * This parameter is set to control whether the generated text file will include a header line with column names,
         * and if so, how it is formatted. The parameter can take several values:
         * 0 – No header line is included in the text file.
         * 1 – A header line with column names from the XML document is included.
         * 2 – Similar to 1, but followed by a blank line.
         * 3 – A header line is specified as NameA.headerLine in the configuration and applied to the file.
         * 4 – Similar to 3, but followed by a blank line.
         * Has effect only for XML->Flat conversion
         */
        private String addHeaderLine;

        /**
         * Specify the header line that is generated in the text file if NameA.addHeaderLine has the value 3 or 4
         * Has effect only for XML->Flat conversion
         */
        private String headerLine;

        /**
         *
         * NameA.fixedLengthTooShortHandling
         * Specify how you want the system to respond when column widths in the actual document exceed those defined in NameA.fieldFixedLengths.
         * The following values are permitted:
         * Error
         * Document processing is cancelled.
         * Cut
         * The value is cut to the maximum permitted length.
         * Ignore
         * The value is accepted even though its length exceeds the permitted value. Subsequent columns are moved accordingly.
         * Has effect only for XML->Flat conversion
         */
        private String fixedLengthTooShortHandling;

        /**
         * If you specify a character string here, the system places it before the first column.Has effect only for XML->Flat conversion
         */
        private String beginSeparator;

        /**
         * If you enter a character string here, the system adds it to the last column as a closing character.Has effect only for XML->Flat conversion
         */
        private String endSeparator;
    }
}
