package com.figaf.content.converter.directory.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


/**
 * <p>Java class for ModuleTypeCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ModuleTypeCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="Remote Enterprise Bean"/>
 *     &lt;enumeration value="Local Enterprise Bean"/>
 *     &lt;enumeration value="Java Library"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ModuleTypeCode")
@XmlEnum
public enum ModuleTypeCode implements Serializable {

    @XmlEnumValue("Remote Enterprise Bean")
    REMOTE_ENTERPRISE_BEAN("Remote Enterprise Bean"),
    @XmlEnumValue("Local Enterprise Bean")
    LOCAL_ENTERPRISE_BEAN("Local Enterprise Bean"),
    @XmlEnumValue("Java Library")
    JAVA_LIBRARY("Java Library");
    private final String value;

    ModuleTypeCode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ModuleTypeCode fromValue(String v) {
        for (ModuleTypeCode c: ModuleTypeCode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
