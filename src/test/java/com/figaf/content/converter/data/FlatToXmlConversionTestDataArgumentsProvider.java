package com.figaf.content.converter.data;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class FlatToXmlConversionTestDataArgumentsProvider implements ArgumentsProvider {

    protected static final String TEST_DATA = "src/test/resources/testdata/";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
            Arguments.of(buildConversionTestData(
                Paths.get("csv-to-xml"), true
            )),
            Arguments.of(buildConversionTestData(
                Paths.get("csv-to-xml-minified"), false
            )),
            Arguments.of(buildConversionTestData(
                Paths.get("mixed-to-xml"), true
            )),
            Arguments.of(buildConversionTestData(
                Paths.get("txt-to-xml-fixed"), true
            )),
            Arguments.of(buildConversionTestData(
                Paths.get("txt-to-xml-fixed-no-recordset"), true
            )),
            Arguments.of(buildConversionTestData(
                Paths.get("more-than-one-recordset-to-xml"), true
            )),
            Arguments.of(buildConversionTestData(
                Paths.get("csv-with-quotes"), true
            ))
        );
    }

    public static ConversionTestData buildConversionTestData(Path folderPath, boolean beautifyOutput) {
        Path testDataFolder = Paths.get(TEST_DATA + folderPath);
        return ConversionTestData.createTestData(testDataFolder, beautifyOutput);
    }
}
