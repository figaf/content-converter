package com.figaf.content.converter.enumaration;

import lombok.Getter;

@Getter
public enum LineEnding {

    LF("\n"),
    CRLF("\r\n"),
    CR("\r"),
    AUTO(System.lineSeparator());

    private final String separator;

    LineEnding(String separator) {
        this.separator = separator;
    }
}
