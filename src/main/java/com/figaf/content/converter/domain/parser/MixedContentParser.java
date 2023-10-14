package com.figaf.content.converter.domain.parser;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MixedContentParser {

    public List<String> parseMixedContent(byte[] fileInputBytes) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileInputBytes), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.debug("#parseMixedContent produced lines count={}", lines.size());
        return lines;
    }
}
