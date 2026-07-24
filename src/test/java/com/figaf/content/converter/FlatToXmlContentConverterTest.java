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
    void test_convert_withLineMatchingNoSubstructureSkippedWhenEnabled() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("txt-to-xml-fixed-key-field-not-first"),
            true
        );
        conversionTestData.getConversionConfig().setSkipUnmatchedLines(true);
        String inputWithUnknownRecordType = new String(conversionTestData.getInputDocument(), UTF_8)
            .replace("BB01OP4012345000003", "BB01OX4012345000003");

        ContentConverter contentConverter = new FlatToXmlContentConverter();
        String actualConvertedFile = contentConverter.convert(inputWithUnknownRecordType, conversionTestData.getConversionConfig());

        assertEquals(
            6,
            StringUtils.countMatches(actualConvertedFile, "<SPROD>"),
            "With skipUnmatchedLines enabled the unmatched line must be skipped, the remaining six Positions lines must be converted."
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
