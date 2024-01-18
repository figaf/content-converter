package com.figaf.content.converter;

import com.figaf.content.converter.data.ConversionTestData;
import com.figaf.content.converter.data.XmToFlatConversionTestDataArgumentsProvider;
import com.figaf.content.converter.xml.XmlToFlatContentConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Slf4j
public class XmlToFlatContentConverterTest {

    @ParameterizedTest
    @ArgumentsSource(XmToFlatConversionTestDataArgumentsProvider.class)
    void test_convert_withByteArrayInput(ConversionTestData conversionTestData) throws IOException {
        log.debug("#test_convert_withByteArrayInput: conversionTestData={}", conversionTestData);

        ContentConverter contentConverter = new XmlToFlatContentConverter();
        byte[] actualConvertedFile = contentConverter.convert(
            conversionTestData.getInputDocument(),
            conversionTestData.getConversionConfig()
        );
        FileUtils.writeByteArrayToFile(
            Paths.get(conversionTestData.getTestDataFolderPath().toString(), "actual-output.txt").toFile(),
            actualConvertedFile
        );

        assertArrayEquals(
            conversionTestData.getExpectedConvertedDocument(),
            actualConvertedFile,
            "The converted file does not match the expected output."
        );
    }
}