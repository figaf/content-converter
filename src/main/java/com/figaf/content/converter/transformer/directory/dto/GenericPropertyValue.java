package com.figaf.content.converter.transformer.directory.dto;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

/**
 * @author Arsenii Istlentev
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericPropertyValue")
public class GenericPropertyValue implements Serializable {

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String type;

    @XmlValue
    private String value;

    public GenericPropertyValue() {
    }

    public GenericPropertyValue(String value) {
        this.value = value;
    }

    public GenericPropertyValue(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "GenericPropertyValue{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
