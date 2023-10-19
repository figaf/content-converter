package com.figaf.content.converter;

public class ContentConversionException extends RuntimeException {

    public ContentConversionException(String message) {
        super(message);
    }

    public ContentConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
