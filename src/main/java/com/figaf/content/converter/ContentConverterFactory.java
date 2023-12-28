package com.figaf.content.converter;

import com.figaf.content.converter.enumaration.ContentConversionType;
import com.figaf.content.converter.xml.FlatToXmlContentConverter;
import com.figaf.content.converter.xml.NodeCreationStrategy;
import com.figaf.content.converter.xml.XmlToFlatContentConverter;

public class ContentConverterFactory {

    public static ContentConverter initializeContentConverter(ContentConversionType contentConversionType) {

        switch (contentConversionType) {
            case FLAT_TO_XML: {
                return new FlatToXmlContentConverter(new NodeCreationStrategy());
            }
            case XML_TO_FLAT: {
                return new XmlToFlatContentConverter();
            }
            default: {
                throw new ContentConversionException(String.format("contentConversionType %s is not supported", contentConversionType));
            }
        }
    }
}
