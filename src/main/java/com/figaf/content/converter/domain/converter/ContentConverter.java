package com.figaf.content.converter.domain.converter;

import com.figaf.content.converter.dto.ConversionConfigDto;
import com.figaf.content.converter.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

public interface ContentConverter {

    byte[] createConvertedFile(
            ConversionConfigDto conversionConfigDto,
            List<String> parsedInputFileLines,
            String testDataFolderName
    ) throws ParserConfigurationException, IOException, TransformerException;
}

