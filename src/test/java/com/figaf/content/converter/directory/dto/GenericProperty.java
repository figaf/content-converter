package com.figaf.content.converter.directory.dto;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;


/**
 * <p>Java class for GenericProperty complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GenericProperty">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://sap.com/xi/BASIS}PropertyName"/>
 *         &lt;element name="Namespace" type="{http://sap.com/xi/BASIS}NamespaceURI" minOccurs="0"/>
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
@XmlType(name = "GenericProperty", propOrder = {
    "name",
    "namespace",
    "propertyValue",
    "isPassword",
    "secStoreId",
    "controlPwd"
})
public class GenericProperty implements Serializable {

    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "Namespace")
    @XmlSchemaType(name = "anyURI")
    protected String namespace;
    @XmlElement(name = "Value")
    protected GenericPropertyValue propertyValue;
    @XmlAttribute
    protected Boolean isPassword;
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
     * Gets the value of the namespace property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the value of the namespace property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNamespace(String value) {
        this.namespace = value;
    }

    public GenericPropertyValue getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(GenericPropertyValue propertyValue) {
        this.propertyValue = propertyValue;
    }

    public Boolean getIsPassword() {
        return isPassword;
    }

    public void setIsPassword(Boolean isPassword) {
        this.isPassword = isPassword;
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

        GenericProperty that = (GenericProperty) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(namespace, that.namespace)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(namespace)
                .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("");
        if (StringUtils.isNotBlank(namespace)) {
            sb.append(namespace).append("/");
        }
        sb.append(name).append(": ").append(propertyValue).append(", ");
        sb.append("isPassword").append(": ").append(isPassword).append(", ");
        sb.append("secStoreId").append(": ").append(secStoreId).append(", ");
        sb.append("controlPwd").append(": ").append(controlPwd);
        return sb.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private GenericProperty payload;

        private Builder() {
            payload = new GenericProperty();
        }

        public Builder namespace(String namespace) {
            this.payload.setNamespace(namespace);
            return this;
        }

        public Builder name(String name) {
            this.payload.setName(name);
            return this;
        }

        public Builder value(GenericPropertyValue value) {
            this.payload.setPropertyValue(value);
            return this;
        }

        public Builder isPassword(Boolean isPassword) {
            this.payload.setIsPassword(isPassword);
            return this;
        }

        public Builder secStoreId(String secStoreId) {
            this.payload.setSecStoreId(secStoreId);
            return this;
        }

        public Builder controlPwd(String controlPwd) {
            this.payload.setControlPwd(controlPwd);
            return this;
        }

        public GenericProperty build() {
            return payload;
        }
    }
}
