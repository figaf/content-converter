package com.figaf.content.converter;

public class ContentConversionException extends Exception {

    public ContentConversionException(String message) {
        super(message);
    }

    public ContentConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
