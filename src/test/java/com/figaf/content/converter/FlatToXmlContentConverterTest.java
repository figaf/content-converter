package com.figaf.content.converter;

import com.figaf.content.converter.data.ConversionTestData;
import com.figaf.content.converter.data.FlatToXmlConversionTestDataArgumentsProvider;
import com.figaf.content.converter.xml.FlatToXmlContentConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
public class FlatToXmlContentConverterTest {

    @ParameterizedTest
    @ArgumentsSource(FlatToXmlConversionTestDataArgumentsProvider.class)
    void test_convert(ConversionTestData conversionTestData) throws ContentConversionException, IOException {
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
}
