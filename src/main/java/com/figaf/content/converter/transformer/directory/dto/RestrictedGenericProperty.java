package com.figaf.content.converter.transformer.directory.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;


/**
 * <p>Java class for RestrictedGenericProperty complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RestrictedGenericProperty">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://sap.com/xi/BASIS}GenericPropertyID"/>
 *         &lt;element name="Value" type="{http://sap.com/xi/BASIS/Global}LANGUAGEINDEPENDENT_Text" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RestrictedGenericProperty", propOrder = {
    "name",
    "value"
})
public class RestrictedGenericProperty implements Serializable {

    @XmlElement(name = "Name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String name;
    @XmlElement(name = "Value")
    protected String value;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlAttribute
    protected String type;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlAttribute
    protected String secStoreId;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlAttribute
    protected String controlPwd;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSecStoreId() {
        return secStoreId;
    }

    public void setSecStoreId(String secStoreId) {
        this.secStoreId = secStoreId;
    }

    public String getControlPwd() {
        return controlPwd;
    }

    public void setControlPwd(String controlPwd) {
        this.controlPwd = controlPwd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RestrictedGenericProperty that = (RestrictedGenericProperty) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private RestrictedGenericProperty restrictedGenericProperty;

        private Builder() {
            restrictedGenericProperty = new RestrictedGenericProperty();
        }

        public Builder name(String name) {
            this.restrictedGenericProperty.setName(name);
            return this;
        }

        public Builder value(String value) {
            this.restrictedGenericProperty.setValue(value);
            return this;
        }

        public Builder type(String type) {
            this.restrictedGenericProperty.setType(type);
            return this;
        }

        public Builder secStoreId(String secStoreId) {
            this.restrictedGenericProperty.setSecStoreId(secStoreId);
            return this;
        }

        public Builder controlPwd(String controlPwd) {
            this.restrictedGenericProperty.setControlPwd(controlPwd);
            return this;
        }

        public RestrictedGenericProperty build() {
            return restrictedGenericProperty;
        }
    }
}
