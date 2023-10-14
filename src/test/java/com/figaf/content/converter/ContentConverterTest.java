package com.figaf.content.converter;

import com.figaf.content.converter.data.ConversionTestData;
import com.figaf.content.converter.data.ConversionTestDataArgumentsProvider;
import com.figaf.content.converter.domain.converter.ContentConverter;
import com.figaf.content.converter.domain.converter.XmlContentConverter;
import com.figaf.content.converter.domain.parser.MixedContentParser;
import com.figaf.content.converter.domain.strategy.NodeCreationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ContentConverterTest {

    @ParameterizedTest
    @ArgumentsSource(ConversionTestDataArgumentsProvider.class)
    void testMessageMappingConverter(ConversionTestData conversionTestData) throws ParserConfigurationException, IOException, TransformerException {
        log.debug("#testMessageMappingConverter: conversionTestData={}", conversionTestData);
        MixedContentParser mixedContentParser = new MixedContentParser();
        List<String> parsedInputFileLines = mixedContentParser.parseMixedContent(conversionTestData.getInputFileBytes());
        ContentConverter contentConverter = new XmlContentConverter(new NodeCreationStrategy());
        byte[] xmlConvertedFile = contentConverter.createConvertedFile(conversionTestData.getConversionConfigDto(), parsedInputFileLines, conversionTestData.getTestDataFolderName());

        assertArrayEquals(conversionTestData.getExpectedOutboundFileBytes(), xmlConvertedFile, "The converted file does not match the expected output.");
    }
}
