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
public class XmToFlatConversionTestDataArgumentsProvider implements ArgumentsProvider {

    protected static final String TEST_DATA = "src/test/resources/testdata/xml-to-flat/";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
            Arguments.of(buildConversionTestData(
                Paths.get("multiple-recordset-elements-to-txt"), true
            )),
            Arguments.of(buildConversionTestData(
                Paths.get("header-line-without-fieldSeparator-to-txt"), true
            )),
            Arguments.of(buildConversionTestData(
                Paths.get("header-line-with-fieldSeparator-to-txt"), true
            ))
        );
    }

    public static ConversionTestData buildConversionTestData(Path folderPath, boolean beautifyOutput) {
        Path testDataFolder = Paths.get(TEST_DATA + folderPath);
        return ConversionTestData.createTestData(testDataFolder, beautifyOutput);
    }
}