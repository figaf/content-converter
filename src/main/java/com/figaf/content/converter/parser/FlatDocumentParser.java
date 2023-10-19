package com.figaf.content.converter.parser;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class FlatDocumentParser {

    public static List<String> readLines(byte[] flatDocument) {
        try (InputStream bais = new ByteArrayInputStream(flatDocument);
            BufferedReader reader = new BufferedReader(new InputStreamReader(bais, StandardCharsets.UTF_8))
        ) {
            List<String> lines = reader.lines().collect(toList());
            log.debug("Flat document has {} lines", lines.size());
            return lines;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Couldn't split flat document to the list of lines: " + ex.getMessage(), ex);
        }
    }
}
