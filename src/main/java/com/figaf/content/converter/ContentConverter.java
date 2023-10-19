package com.figaf.content.converter;


public interface ContentConverter {

    byte[] convert(
        byte[] document,
        ConversionConfig conversionConfig
    );

    String convert(
        String document,
        ConversionConfig conversionConfig
    );
}

