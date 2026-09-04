package com.figaf.content.converter;

import com.figaf.content.converter.data.ConversionTestData;
import com.figaf.content.converter.data.FlatToXmlConversionTestDataArgumentsProvider;
import com.figaf.content.converter.enumeration.ContentConversionType;
import com.figaf.content.converter.enumeration.LineEnding;
import com.figaf.content.converter.xml.FlatToXmlContentConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class FlatToXmlContentConverterTest {

    @ParameterizedTest
    @ArgumentsSource(FlatToXmlConversionTestDataArgumentsProvider.class)
    void test_convert_withByteArrayInput(ConversionTestData conversionTestData) throws IOException {
        log.debug("#test_convert_withByteArrayInput: conversionTestData={}", conversionTestData);

        ContentConverter contentConverter = ContentConverterFactory.initializeContentConverter(conversionTestData.getConversionConfig().getContentConversionType());
        byte[] actualConvertedFile = contentConverter.convert(
            conversionTestData.getInputDocument(),
            conversionTestData.getConversionConfig()
        );
        FileUtils.writeByteArrayToFile(
            Paths.get(conversionTestData.getTestDataFolderPath().toString(), "actual-output.xml").toFile(),
            actualConvertedFile
        );

        assertArrayEquals(
            conversionTestData.getExpectedConvertedDocument(),
            actualConvertedFile,
            "The converted file does not match the expected output."
        );
    }

    @Test
    void test_convert_withKeyFieldNotInFirstPosition_programmaticConfig() throws IOException {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setLineEnding(LineEnding.CRLF);
        conversionConfig.setRecordsetStructure("Header,1,Positions,*");
        conversionConfig.setDocumentName("MT_OrderData_Test");
        conversionConfig.setDocumentNamespace("urn:test:sorders:anonymous");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setRecordsetNamespace("");
        conversionConfig.setIgnoreRecordsetName(false);
        conversionConfig.setTargetFileName(null);
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("SRTYPE");

        ConversionConfig.SectionParameters headerParameters = new ConversionConfig.SectionParameters();
        headerParameters.setFieldFixedLengths("4,1,1,4,3,2,20,20,10,35,8,8,8,100,4");
        headerParameters.setFieldNames("SORG,SITYPE,SRTYPE,SOTYPE,SOREAS,SESC,SCUST1,SCUST2,SCUST3,SCPO,SEDTE,SODTE,SRDTE,SNDES1,STERM");
        headerParameters.setKeyFieldValue("H");

        ConversionConfig.SectionParameters positionsParameters = new ConversionConfig.SectionParameters();
        positionsParameters.setFieldFixedLengths("4,1,1,18,11,3,1,13");
        positionsParameters.setFieldNames("SORG,SITYPE,SRTYPE,SPROD,SQORD,SGRAN,STYPE,SPRICE");
        positionsParameters.setKeyFieldValue("P");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Header", headerParameters);
        sectionParameters.put("Positions", positionsParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        byte[] inputDocument = FileUtils.readFileToByteArray(
            Paths.get("src/test/resources/testdata/flat-to-xml", "txt-to-xml-fixed-key-field-not-first", "input.txt").toFile()
        );
        byte[] expectedConvertedFile = FileUtils.readFileToByteArray(
            Paths.get("src/test/resources/testdata/flat-to-xml", "txt-to-xml-fixed-key-field-not-first", "expected-output.xml").toFile()
        );

        ContentConverter contentConverter = ContentConverterFactory.initializeContentConverter(conversionConfig.getContentConversionType());
        byte[] actualConvertedFile = contentConverter.convert(inputDocument, conversionConfig);

        assertArrayEquals(
            expectedConvertedFile,
            actualConvertedFile,
            "The converted file does not match the expected output."
        );
    }

    @Test
    void test_convert_withMissingLastFieldsError() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-missing-last-fields-ignore"),
            true
        );
        conversionTestData.getConversionConfig().getSectionParameters().get("Header").setMissingLastFields("error");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        ContentConversionException exception = assertThrows(
            ContentConversionException.class,
            () -> contentConverter.convert(conversionTestData.getInputDocument(), conversionTestData.getConversionConfig())
        );

        String causeMessage = exception.getCause().getMessage();
        assertTrue(
            causeMessage.contains("SNDES1") && causeMessage.contains("missingLastFields=error"),
            "Unexpected error message: " + causeMessage
        );
    }

    @Test
    void test_convert_withMissingLastFieldsNotSet() throws IOException {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-missing-last-fields-ignore"),
            true
        );
        conversionTestData.getConversionConfig().getSectionParameters().get("Header").setMissingLastFields(null);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        byte[] actualConvertedFile = contentConverter.convert(
            conversionTestData.getInputDocument(),
            conversionTestData.getConversionConfig()
        );

        byte[] expectedConvertedFile = FileUtils.readFileToByteArray(
            Paths.get("src/test/resources/testdata/flat-to-xml", "txt-to-xml-fixed-key-field-not-first", "expected-output.xml").toFile()
        );
        assertArrayEquals(
            expectedConvertedFile,
            actualConvertedFile,
            "Without missingLastFields the converter must create empty elements for the missing trailing fields."
        );
    }

    @Test
    void test_convert_withAdditionalLastFieldsError() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        conversionTestData.getConversionConfig().getSectionParameters().get("Header").setAdditionalLastFields("error");
        String inputWithSurplus = new String(conversionTestData.getInputDocument(), UTF_8).replaceFirst("\r\n", "EXTRA\r\n");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        ContentConversionException exception = assertThrows(
            ContentConversionException.class,
            () -> contentConverter.convert(inputWithSurplus, conversionTestData.getConversionConfig())
        );

        String causeMessage = exception.getCause().getMessage();
        assertTrue(
            causeMessage.contains("additionalLastFields=error"),
            "Unexpected error message: " + causeMessage
        );
    }

    @Test
    void test_convert_withAdditionalLastFieldsErrorAndTrailingPadding() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        conversionTestData.getConversionConfig().getSectionParameters().get("Header").setAdditionalLastFields("error");
        // trailing blanks are padding to a record width, not surplus content — the conversion must not fail
        String inputWithPaddedHeader = new String(conversionTestData.getInputDocument(), UTF_8).replaceFirst("\r\n", "   \r\n");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert(inputWithPaddedHeader, conversionTestData.getConversionConfig());

        assertEquals(
            new String(conversionTestData.getExpectedConvertedDocument(), UTF_8),
            actualConvertedFile,
            "A line padded with trailing blanks past the declared structure must convert like the unpadded line."
        );
    }

    @Test
    void test_convert_withAdditionalLastFieldsErrorAndSurplusFollowedByPadding() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        conversionTestData.getConversionConfig().getSectionParameters().get("Header").setAdditionalLastFields("error");
        String inputWithSurplus = new String(conversionTestData.getInputDocument(), UTF_8).replaceFirst("\r\n", "EXTRA   \r\n");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        ContentConversionException exception = assertThrows(
            ContentConversionException.class,
            () -> contentConverter.convert(inputWithSurplus, conversionTestData.getConversionConfig())
        );

        assertTrue(
            exception.getCause().getMessage().contains("additionalLastFields=error"),
            "Real surplus content must still fail, even when it is followed by trailing blanks. Message: " + exception.getCause().getMessage()
        );
    }

    @Test
    void test_convert_withAdditionalLastFieldsNotSet() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        String inputWithSurplus = new String(conversionTestData.getInputDocument(), UTF_8).replaceFirst("\r\n", "EXTRA\r\n");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert(inputWithSurplus, conversionTestData.getConversionConfig());

        assertEquals(
            new String(conversionTestData.getExpectedConvertedDocument(), UTF_8),
            actualConvertedFile,
            "Without additionalLastFields the surplus content must be ignored and the output must stay unchanged."
        );
    }

    @Test
    void test_convert_withLineMatchingNoSubstructure() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        conversionTestData.getConversionConfig().setFailOnUnmatchedLines(true);
        String inputWithUnknownRecordType = new String(conversionTestData.getInputDocument(), UTF_8)
            .replace("BB01OP4012345000003", "BB01OX4012345000003");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        ContentConversionException exception = assertThrows(
            ContentConversionException.class,
            () -> contentConverter.convert(inputWithUnknownRecordType, conversionTestData.getConversionConfig())
        );

        String causeMessage = exception.getCause().getMessage();
        assertTrue(
            causeMessage.contains("matches no substructure") && causeMessage.contains("BB01OX4012345000003"),
            "Unexpected error message: " + causeMessage
        );
    }

    @Test
    void test_convert_withLineMatchingNoSubstructureSkippedByDefault() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        String inputWithUnknownRecordType = new String(conversionTestData.getInputDocument(), UTF_8)
            .replace("BB01OP4012345000003", "BB01OX4012345000003");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert(inputWithUnknownRecordType, conversionTestData.getConversionConfig());

        assertEquals(
            6,
            StringUtils.countMatches(actualConvertedFile, "<SPROD>"),
            "By default the unmatched line must be skipped like SAP PI does, the remaining six Positions lines must be converted."
        );
    }

    @Test
    void test_convert_withBlankLinesIgnoredInStrictMode() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        conversionTestData.getConversionConfig().setFailOnUnmatchedLines(true);
        String inputWithBlankLines = new String(conversionTestData.getInputDocument(), UTF_8)
            .replaceFirst("\r\n", "\r\n\r\n   \r\n") + "\r\n";

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert(inputWithBlankLines, conversionTestData.getConversionConfig());

        assertEquals(
            new String(conversionTestData.getExpectedConvertedDocument(), UTF_8),
            actualConvertedFile,
            "Empty and whitespace-only lines must be ignored even with failOnUnmatchedLines enabled."
        );
    }

    @Test
    void test_convert_withMissingLastFieldsErrorOnPositions() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        conversionTestData.getConversionConfig().getSectionParameters().get("Positions").setMissingLastFields("error");
        String inputWithTruncatedPosition = new String(conversionTestData.getInputDocument(), UTF_8)
            .replace("BB01OP4012345000001     00000024000PC R0000001000001", "BB01OP4012345000001     00000024000PC R");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        ContentConversionException exception = assertThrows(
            ContentConversionException.class,
            () -> contentConverter.convert(inputWithTruncatedPosition, conversionTestData.getConversionConfig())
        );

        String causeMessage = exception.getCause().getMessage();
        assertTrue(
            causeMessage.contains("SPRICE") && causeMessage.contains("missingLastFields=error"),
            "Unexpected error message: " + causeMessage
        );
    }

    @Test
    void test_convert_withMissingLastFieldsIgnoreOnPositions() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        conversionTestData.getConversionConfig().getSectionParameters().get("Positions").setMissingLastFields("ignore");
        String inputWithTruncatedPosition = new String(conversionTestData.getInputDocument(), UTF_8)
            .replace("BB01OP4012345000001     00000024000PC R0000001000001", "BB01OP4012345000001     00000024000PC R");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert(inputWithTruncatedPosition, conversionTestData.getConversionConfig());

        assertEquals(
            6,
            StringUtils.countMatches(actualConvertedFile, "<SPRICE>"),
            "The truncated Positions line must not produce an SPRICE element, the six complete lines must."
        );
        assertEquals(
            7,
            StringUtils.countMatches(actualConvertedFile, "<STYPE>"),
            "STYPE is still present in the truncated line and must be created for all seven Positions lines."
        );
    }

    @Test
    void test_convert_withAdditionalLastFieldsErrorOnPositions() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        conversionTestData.getConversionConfig().getSectionParameters().get("Positions").setAdditionalLastFields("error");
        String inputWithSurplusPosition = new String(conversionTestData.getInputDocument(), UTF_8)
            .replace("BB01OP4012345000001     00000024000PC R0000001000001", "BB01OP4012345000001     00000024000PC R0000001000001EXTRA");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        ContentConversionException exception = assertThrows(
            ContentConversionException.class,
            () -> contentConverter.convert(inputWithSurplusPosition, conversionTestData.getConversionConfig())
        );

        String causeMessage = exception.getCause().getMessage();
        assertTrue(
            causeMessage.contains("additionalLastFields=error") && causeMessage.contains("EXTRA"),
            "Unexpected error message: " + causeMessage
        );
    }

    @Test
    void test_convert_withKeyFieldAtDifferentPositionPerSubstructure() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("A,1,B,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("KEY");

        ConversionConfig.SectionParameters sectionA = new ConversionConfig.SectionParameters();
        sectionA.setFieldNames("F1,KEY,F2");
        sectionA.setFieldFixedLengths("2,1,3");
        sectionA.setKeyFieldValue("A");

        ConversionConfig.SectionParameters sectionB = new ConversionConfig.SectionParameters();
        sectionB.setFieldNames("F1,F2,KEY,F3");
        sectionB.setFieldFixedLengths("2,3,1,4");
        sectionB.setKeyFieldValue("B");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("A", sectionA);
        sectionParameters.put("B", sectionB);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("xxAyyy\nqqwwwBzzzz", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<KEY>A</KEY>") && actualConvertedFile.contains("<F2>yyy</F2>"),
            "The first line must be matched to substructure A via KEY at offset 2. Actual output: " + actualConvertedFile
        );
        assertTrue(
            actualConvertedFile.contains("<KEY>B</KEY>") && actualConvertedFile.contains("<F3>zzzz</F3>"),
            "The second line must be matched to substructure B via KEY at offset 5. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withKeyFieldNameAndFieldSeparator() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Header,1,Item,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("TYPE");

        ConversionConfig.SectionParameters headerParameters = new ConversionConfig.SectionParameters();
        headerParameters.setFieldNames("COL1,COL2,TYPE,COL3");
        headerParameters.setFieldSeparator(";");
        headerParameters.setKeyFieldValue("H");

        ConversionConfig.SectionParameters itemParameters = new ConversionConfig.SectionParameters();
        itemParameters.setFieldNames("COL1,TYPE,COL2");
        itemParameters.setFieldSeparator(";");
        itemParameters.setKeyFieldValue("I");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Header", headerParameters);
        sectionParameters.put("Item", itemParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("a;b;H;c\nd;I;e", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<TYPE>H</TYPE>") && actualConvertedFile.contains("<COL3>c</COL3>"),
            "The first line must be matched to Header via the third column. Actual output: " + actualConvertedFile
        );
        assertTrue(
            actualConvertedFile.contains("<TYPE>I</TYPE>") && actualConvertedFile.contains("<COL2>e</COL2>"),
            "The second line must be matched to Item via the second column. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withKeyFieldNameNotInFieldNames_fallsBackToSectionNamePrefix() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("HDR,1,POS,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("NO_SUCH_FIELD");

        ConversionConfig.SectionParameters hdrParameters = new ConversionConfig.SectionParameters();
        hdrParameters.setFieldNames("F1,F2");
        hdrParameters.setFieldFixedLengths("3,3");
        hdrParameters.setKeyFieldValue("H");

        ConversionConfig.SectionParameters posParameters = new ConversionConfig.SectionParameters();
        posParameters.setFieldNames("F1,F2");
        posParameters.setFieldFixedLengths("3,3");
        posParameters.setKeyFieldValue("P");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("HDR", hdrParameters);
        sectionParameters.put("POS", posParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("HDRabc\nPOSdef", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<HDR>") && actualConvertedFile.contains("<POS>"),
            "Lines starting with the substructure name must still be matched via the legacy prefix fallback. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withKeyFieldNameNotInFieldNames_fallsBackToKeyFieldValuePrefix() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("HR,1,PR,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        // the channel names the key field RECTYPE, but both substructures call their first field KEY,
        // so the key field value can never be extracted and the legacy prefix matching must still apply
        conversionConfig.setKeyFieldName("RECTYPE");

        ConversionConfig.SectionParameters hrParameters = new ConversionConfig.SectionParameters();
        hrParameters.setFieldNames("KEY,ID1,Doc");
        hrParameters.setFieldFixedLengths("2,3,4");
        hrParameters.setKeyFieldValue("HD");

        ConversionConfig.SectionParameters prParameters = new ConversionConfig.SectionParameters();
        prParameters.setFieldNames("KEY,TYPE,ID,NAME");
        prParameters.setFieldFixedLengths("2,2,5,6");
        prParameters.setKeyFieldValue("PS");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("HR", hrParameters);
        sectionParameters.put("PR", prParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        // neither line starts with its substructure name, both start with their keyFieldValue
        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("HD2130003\nPSAG123532DANIEL", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<HR>") && actualConvertedFile.contains("<Doc>0003</Doc>"),
            "The first line must be matched to HR via the legacy keyFieldValue prefix. Actual output: " + actualConvertedFile
        );
        assertTrue(
            actualConvertedFile.contains("<PR>") && actualConvertedFile.contains("<NAME>2DANIE</NAME>"),
            "The second line must be matched to PR via the legacy keyFieldValue prefix. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withKeyFieldNameNotInFieldNames_producesTheSameOutputAsWithoutKeyFieldName() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("more-than-one-recordset-to-xml-key-field-name-unknown"),
            true
        );

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        byte[] actualConvertedFile = contentConverter.convert(
            conversionTestData.getInputDocument(),
            conversionTestData.getConversionConfig()
        );

        assertArrayEquals(
            conversionTestData.getExpectedConvertedDocument(),
            actualConvertedFile,
            "xml.keyfieldName=RECTYPE matches no field name of any substructure, so it carries no information. "
                + "The result must stay the same as for the identical channel without xml.keyfieldName."
        );
    }

    @Test
    void test_convert_withKeyFieldNameKnownToOneSubstructureOnly() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Header,1,Trailer,1");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("TYPE");

        ConversionConfig.SectionParameters headerParameters = new ConversionConfig.SectionParameters();
        headerParameters.setFieldNames("F1,TYPE,F2");
        headerParameters.setFieldFixedLengths("2,1,3");
        headerParameters.setKeyFieldValue("H");

        // Trailer has no TYPE field, so its lines can be identified only by the keyFieldValue prefix.
        // That fallback must stay active for Trailer even when another substructure extracted a value from the line
        ConversionConfig.SectionParameters trailerParameters = new ConversionConfig.SectionParameters();
        trailerParameters.setFieldNames("T1,T2");
        trailerParameters.setFieldFixedLengths("2,2");
        trailerParameters.setKeyFieldValue("TR");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Header", headerParameters);
        sectionParameters.put("Trailer", trailerParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        // for the line "TR99" the Header substructure extracts '9' as TYPE (no match); Trailer extracts nothing
        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("xxHyyy\nTR99", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<Header>") && actualConvertedFile.contains("<TYPE>H</TYPE>"),
            "The first line must be matched to Header via the key field. Actual output: " + actualConvertedFile
        );
        assertTrue(
            actualConvertedFile.contains("<Trailer>") && actualConvertedFile.contains("<T1>TR</T1>"),
            "The trailer line must still be matched via the keyFieldValue prefix, although Header extracted a value from it. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withAmbiguousKeyFieldMatchFollowsRecordsetStructureOrder() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Header,1,Positions,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("KEY");

        ConversionConfig.SectionParameters headerParameters = new ConversionConfig.SectionParameters();
        headerParameters.setFieldNames("F1,KEY,F2");
        headerParameters.setFieldFixedLengths("2,1,3");
        headerParameters.setKeyFieldValue("H");

        ConversionConfig.SectionParameters positionsParameters = new ConversionConfig.SectionParameters();
        positionsParameters.setFieldNames("F1,KEY,F2");
        positionsParameters.setFieldFixedLengths("3,1,2");
        positionsParameters.setKeyFieldValue("Y");

        // the line matches both substructures (KEY=H at offset 2 for Header, KEY=Y at offset 3 for Positions).
        // The section map is deliberately built in the opposite order — recordsetStructure must decide
        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Positions", positionsParameters);
        sectionParameters.put("Header", headerParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("XXHYYY", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<Header>") && !actualConvertedFile.contains("<Positions>"),
            "When two substructures match the same line, the one listed first in recordsetStructure must win. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withAmbiguousPrefixMatchFollowsRecordsetStructureOrder() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("A,1,B,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);

        ConversionConfig.SectionParameters sectionA = new ConversionConfig.SectionParameters();
        sectionA.setFieldNames("KEY,REST");
        sectionA.setFieldFixedLengths("2,4");
        sectionA.setKeyFieldValue("HD");

        ConversionConfig.SectionParameters sectionB = new ConversionConfig.SectionParameters();
        sectionB.setFieldNames("KEY,REST");
        sectionB.setFieldFixedLengths("1,5");
        sectionB.setKeyFieldValue("H");

        // "HD1234" starts with both keyFieldValues (HD and H); the section map is built in the opposite order
        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("B", sectionB);
        sectionParameters.put("A", sectionA);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("HD1234", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<A>") && !actualConvertedFile.contains("<B>"),
            "When two keyFieldValue prefixes match the same line, the substructure listed first in recordsetStructure must win. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withQuoteInFixedLengthKeyFieldValue() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("A,1,B,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("KEY");

        // fixed-length content is taken from the line as it is, so a keyFieldValue containing
        // a quote character must be compared as it is, without enclosure handling
        ConversionConfig.SectionParameters sectionA = new ConversionConfig.SectionParameters();
        sectionA.setFieldNames("KEY,REST");
        sectionA.setFieldFixedLengths("2,4");
        sectionA.setKeyFieldValue("\"H");

        ConversionConfig.SectionParameters sectionB = new ConversionConfig.SectionParameters();
        sectionB.setFieldNames("KEY,REST");
        sectionB.setFieldFixedLengths("2,4");
        sectionB.setKeyFieldValue("ZZ");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("A", sectionA);
        sectionParameters.put("B", sectionB);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("\"HABCD", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<A>") && actualConvertedFile.contains("<REST>ABCD</REST>"),
            "A fixed-length key field value containing a quote must match as it is. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withKeyFieldNameAndEnclosedKeyFieldValue() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Header,1,Item,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("TYPE");

        ConversionConfig.SectionParameters headerParameters = new ConversionConfig.SectionParameters();
        headerParameters.setFieldNames("TYPE,Name");
        headerParameters.setFieldSeparator(",");
        headerParameters.setKeyFieldValue("H");

        ConversionConfig.SectionParameters itemParameters = new ConversionConfig.SectionParameters();
        itemParameters.setFieldNames("TYPE,Name");
        itemParameters.setFieldSeparator(",");
        itemParameters.setKeyFieldValue("I");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Header", headerParameters);
        sectionParameters.put("Item", itemParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        // the key field values are enclosed in quotes in the data, but keyFieldValue is configured
        // without them (H/I); the enclosure signs are removed from both sides before comparing,
        // so this spelling must match too
        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("\"H\",\"Smith\"\n\"I\",\"Jones, Bob\"", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<Header>") && actualConvertedFile.contains("<Name>Smith</Name>"),
            "The first line must be matched to Header via the unquoted key field value. Actual output: " + actualConvertedFile
        );
        assertTrue(
            actualConvertedFile.contains("<Item>") && actualConvertedFile.contains("<Name>Jones, Bob</Name>"),
            "The second line must be matched to Item via the unquoted key field value. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withKeyFieldNameAfterEnclosedValueContainingSeparator() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("A,1,B,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("Num");

        ConversionConfig.SectionParameters sectionA = new ConversionConfig.SectionParameters();
        sectionA.setFieldNames("TYPE,Name,Num");
        sectionA.setFieldSeparator(",");
        sectionA.setKeyFieldValue("42");

        ConversionConfig.SectionParameters sectionB = new ConversionConfig.SectionParameters();
        sectionB.setFieldNames("TYPE,Name,Num");
        sectionB.setFieldSeparator(",");
        sectionB.setKeyFieldValue("43");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("A", sectionA);
        sectionParameters.put("B", sectionB);
        conversionConfig.setSectionParameters(sectionParameters);

        // the separator inside "Smith, John" must not shift the position of the key field Num
        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("\"H\",\"Smith, John\",\"42\"\n\"D\",\"Doe\",\"43\"", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<A>") && actualConvertedFile.contains("<Name>Smith, John</Name>"),
            "The first line must be matched to A via the key field value 42 at index 2. Actual output: " + actualConvertedFile
        );
        assertTrue(
            actualConvertedFile.contains("<B>") && actualConvertedFile.contains("<Name>Doe</Name>"),
            "The second line must be matched to B via the key field value 43 at index 2. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withKeyFieldValueStoredWithEnclosureSigns() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Header,1,Item,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("TYPE");

        ConversionConfig.SectionParameters headerParameters = new ConversionConfig.SectionParameters();
        headerParameters.setFieldNames("TYPE,Name");
        headerParameters.setFieldSeparator(",");
        headerParameters.setKeyFieldValue("\"H\"");

        ConversionConfig.SectionParameters itemParameters = new ConversionConfig.SectionParameters();
        itemParameters.setFieldNames("TYPE,Name");
        itemParameters.setFieldSeparator(",");
        itemParameters.setKeyFieldValue("\"I\"");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Header", headerParameters);
        sectionParameters.put("Item", itemParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        // real PI channels may store the keyFieldValue with the enclosure signs included
        // (see the csv-with-quotes fixture), so "H"/"I" must match too
        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("\"H\",\"Smith\"\n\"I\",\"Jones, Bob\"", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<Header>") && actualConvertedFile.contains("<Name>Smith</Name>"),
            "The first line must be matched to Header although keyFieldValue is stored with the quotes. Actual output: " + actualConvertedFile
        );
        assertTrue(
            actualConvertedFile.contains("<Item>") && actualConvertedFile.contains("<Name>Jones, Bob</Name>"),
            "The second line must be matched to Item although keyFieldValue is stored with the quotes. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withCustomEnclosureSign() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Line,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setBeautifyOutput(true);

        ConversionConfig.SectionParameters lineParameters = new ConversionConfig.SectionParameters();
        lineParameters.setFieldNames("TYPE,Name");
        lineParameters.setFieldSeparator(",");
        lineParameters.setEnclosureSign("'");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Line", lineParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("'H','Smith, John'", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<TYPE>H</TYPE>") && actualConvertedFile.contains("<Name>Smith, John</Name>"),
            "The single quotes must be treated as the enclosure sign and removed from the values. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withKeyFieldNameAndCustomEnclosureSign() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Header,1,Item,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("TYPE");

        ConversionConfig.SectionParameters headerParameters = new ConversionConfig.SectionParameters();
        headerParameters.setFieldNames("TYPE,Name");
        headerParameters.setFieldSeparator(",");
        headerParameters.setEnclosureSign("'");
        // stored with the custom enclosure signs, like the real PI channel stores "H"
        headerParameters.setKeyFieldValue("'H'");

        ConversionConfig.SectionParameters itemParameters = new ConversionConfig.SectionParameters();
        itemParameters.setFieldNames("TYPE,Name");
        itemParameters.setFieldSeparator(",");
        itemParameters.setEnclosureSign("'");
        // stored without the enclosure signs — both spellings must match
        itemParameters.setKeyFieldValue("I");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Header", headerParameters);
        sectionParameters.put("Item", itemParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("'H','Smith, John'\n'I','Jones'", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<Header>") && actualConvertedFile.contains("<Name>Smith, John</Name>"),
            "The first line must be matched to Header via the custom enclosure sign, keyFieldValue stored with the signs. Actual output: " + actualConvertedFile
        );
        assertTrue(
            actualConvertedFile.contains("<Item>") && actualConvertedFile.contains("<Name>Jones</Name>"),
            "The second line must be matched to Item via the custom enclosure sign, keyFieldValue stored without the signs. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withFieldContentFormattingNothingOnFixedLength() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Line,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setBeautifyOutput(true);

        ConversionConfig.SectionParameters lineParameters = new ConversionConfig.SectionParameters();
        lineParameters.setFieldNames("F1,F2");
        lineParameters.setFieldFixedLengths("2,6");
        lineParameters.setFieldContentFormatting("nothing");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Line", lineParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("AB  12  ", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<F2>  12  </F2>"),
            "With fieldContentFormatting=nothing the fixed length padding must be kept. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withFieldContentFormattingDefaultTrimsSeparatorValues() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Line,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setBeautifyOutput(true);

        ConversionConfig.SectionParameters lineParameters = new ConversionConfig.SectionParameters();
        lineParameters.setFieldNames("F1,F2");
        lineParameters.setFieldSeparator(";");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Line", lineParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("  x  ;  y  ", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<F1>x</F1>") && actualConvertedFile.contains("<F2>y</F2>"),
            "By default (fieldContentFormatting=trim) the values must be trimmed, like SAP does. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withFieldContentFormattingNothingOnSeparator() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Line,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setBeautifyOutput(true);

        ConversionConfig.SectionParameters lineParameters = new ConversionConfig.SectionParameters();
        lineParameters.setFieldNames("F1,F2");
        lineParameters.setFieldSeparator(";");
        lineParameters.setFieldContentFormatting("nothing");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Line", lineParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("  x  ;  y  ", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<F1>  x  </F1>") && actualConvertedFile.contains("<F2>  y  </F2>"),
            "With fieldContentFormatting=nothing the values must be left unaltered. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withMissingLastFieldsErrorOnSeparatorSection() {
        ConversionConfig conversionConfig = buildSeparatorSectionConfig();
        conversionConfig.getSectionParameters().get("Line").setMissingLastFields("error");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        ContentConversionException exception = assertThrows(
            ContentConversionException.class,
            () -> contentConverter.convert("a;b", conversionConfig)
        );

        String causeMessage = exception.getCause().getMessage();
        assertTrue(
            causeMessage.contains("F3") && causeMessage.contains("missingLastFields=error"),
            "Unexpected error message: " + causeMessage
        );
    }

    @Test
    void test_convert_withMissingLastFieldsIgnoreOnSeparatorSection() {
        ConversionConfig conversionConfig = buildSeparatorSectionConfig();
        conversionConfig.getSectionParameters().get("Line").setMissingLastFields("ignore");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("a;b", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<F2>b</F2>") && !actualConvertedFile.contains("<F3"),
            "With missingLastFields=ignore the missing field must be left out of the XML, like in the fixed-length path. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withMissingLastFieldsErrorAndTrailingSeparator() {
        ConversionConfig conversionConfig = buildSeparatorSectionConfig();
        conversionConfig.getSectionParameters().get("Line").setMissingLastFields("error");

        // "a;b;" has three fields, the last one empty — it must not count as missing
        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("a;b;", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<F3/>"),
            "A trailing empty field must count as present, not as missing. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withAdditionalLastFieldsErrorOnSeparatorSection() {
        ConversionConfig conversionConfig = buildSeparatorSectionConfig();
        conversionConfig.getSectionParameters().get("Line").setAdditionalLastFields("error");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        ContentConversionException exception = assertThrows(
            ContentConversionException.class,
            () -> contentConverter.convert("q;w;e;r;t", conversionConfig)
        );

        String causeMessage = exception.getCause().getMessage();
        assertTrue(
            causeMessage.contains("(5 > 3)") && causeMessage.contains("additionalLastFields=error"),
            "Unexpected error message: " + causeMessage
        );
    }

    @Test
    void test_convert_withAdditionalLastFieldsErrorAndDelimiterTerminatedLine() {
        ConversionConfig conversionConfig = buildSeparatorSectionConfig();
        conversionConfig.getSectionParameters().get("Line").setAdditionalLastFields("error");

        // a delimiter-terminated line: every field is followed by the separator — complete, not surplus
        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("a;b;c;;", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<F3>c</F3>"),
            "Blank trailing fields must not count as surplus content. Actual output: " + actualConvertedFile
        );
    }

    @Test
    void test_convert_withSectionNamePrefixRescuingKeyFieldValueMismatch() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("HD,1,ZZ,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setRecordsetName("Record");
        conversionConfig.setBeautifyOutput(true);
        conversionConfig.setKeyFieldName("KEY");
        conversionConfig.setFailOnUnmatchedLines(true);

        ConversionConfig.SectionParameters hdParameters = new ConversionConfig.SectionParameters();
        hdParameters.setFieldNames("KEY,REST");
        hdParameters.setFieldFixedLengths("2,4");
        hdParameters.setKeyFieldValue("XX");

        ConversionConfig.SectionParameters zzParameters = new ConversionConfig.SectionParameters();
        zzParameters.setFieldNames("KEY,REST");
        zzParameters.setFieldFixedLengths("2,4");
        zzParameters.setKeyFieldValue("ZZ");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("HD", hdParameters);
        sectionParameters.put("ZZ", zzParameters);
        conversionConfig.setSectionParameters(sectionParameters);

        // "HD1234" carries the key field value HD, which contradicts the configured XX — but the line starts
        // with the substructure name HD, and the name prefix must still assign it, like 2.1.1 did. Real channels
        // rely on this rescue (the KK/KK3 substructure of the more-than-one-recordset-to-xml fixture)
        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert("XX5678\nHD1234", conversionConfig);

        assertTrue(
            actualConvertedFile.contains("<REST>5678</REST>") && actualConvertedFile.contains("<REST>1234</REST>"),
            "Both lines must become HD records: the first via the key field, the second via the name prefix. Actual output: " + actualConvertedFile
        );
    }

    private static ConversionConfig buildSeparatorSectionConfig() {
        ConversionConfig conversionConfig = new ConversionConfig();
        conversionConfig.setContentConversionType(ContentConversionType.FLAT_TO_XML);
        conversionConfig.setRecordsetStructure("Line,*");
        conversionConfig.setDocumentName("Doc");
        conversionConfig.setDocumentNamespace("urn:test");
        conversionConfig.setBeautifyOutput(true);

        ConversionConfig.SectionParameters lineParameters = new ConversionConfig.SectionParameters();
        lineParameters.setFieldNames("F1,F2,F3");
        lineParameters.setFieldSeparator(";");

        Map<String, ConversionConfig.SectionParameters> sectionParameters = new LinkedHashMap<>();
        sectionParameters.put("Line", lineParameters);
        conversionConfig.setSectionParameters(sectionParameters);
        return conversionConfig;
    }

    // it's enough to test only one dataset because it provides the full coverage of String processing
    @Test
    void test_convert_withStringInput() {
        Path csvToXmlPath = Paths.get("csv-to-xml");
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            csvToXmlPath,
            true
        );

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert(
            new String(conversionTestData.getInputDocument(), UTF_8),
            conversionTestData.getConversionConfig()
        );
        String expectedConvertedFile = new String(conversionTestData.getExpectedConvertedDocument(), UTF_8);

        assertEquals(
            expectedConvertedFile,
            actualConvertedFile,
            "The converted file does not match the expected output."
        );
    }
}
