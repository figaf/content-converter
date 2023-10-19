package com.figaf.content.converter;

import com.figaf.content.converter.data.ConversionTestData;
import com.figaf.content.converter.data.FlatToXmlConversionTestDataArgumentsProvider;
import com.figaf.content.converter.xml.FlatToXmlContentConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@Slf4j
public class FlatToXmlContentConverterTest {

    @ParameterizedTest
    @ArgumentsSource(FlatToXmlConversionTestDataArgumentsProvider.class)
    void test_convert_withByteArrayInput(ConversionTestData conversionTestData) throws IOException {
        log.debug("#testMessageMappingConverter: conversionTestData={}", conversionTestData);

        ContentConverter contentConverter = new FlatToXmlContentConverter();
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

    // it's enough to test only one dataset because it provides the full coverage of String processing
    @Test
    void test_convert_withStringInput() {
        ConversionTestData conversionTestData = FlatToXmlConversionTestDataArgumentsProvider.buildConversionTestData(
            Paths.get("csv-to-xml"),
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
