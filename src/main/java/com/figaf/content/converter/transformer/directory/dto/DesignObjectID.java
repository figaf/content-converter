package com.figaf.content.converter.transformer.directory.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import static com.figaf.content.converter.transformer.directory.IntegrationDirectoryUtils.normalize;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;


/**
 * <p>Java class for DesignObjectID complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DesignObjectID">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://sap.com/xi/BASIS}DesignObjectName"/>
 *         &lt;element name="Namespace" type="{http://sap.com/xi/BASIS}DesignObjectNamespace"/>
 *         &lt;element name="SoftwareComponentVersionID" type="{http://sap.com/xi/BASIS}SoftwareComponentVersionID" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DesignObjectID", propOrder = {
    "name",
    "namespace",
    "softwareComponentVersionID"
})
public class DesignObjectID {

    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "Namespace", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String namespace;
    @XmlElement(name = "SoftwareComponentVersionID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String softwareComponentVersionID;

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

    /**
     * Gets the value of the softwareComponentVersionID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoftwareComponentVersionID() {
        return softwareComponentVersionID;
    }

    /**
     * Sets the value of the softwareComponentVersionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoftwareComponentVersionID(String value) {
        this.softwareComponentVersionID = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DesignObjectID that = (DesignObjectID) o;

        return new EqualsBuilder()
                .append(normalize(name), normalize(that.name))
                .append(normalize(namespace), normalize(that.namespace))
                .append(normalize(softwareComponentVersionID), normalize(that.softwareComponentVersionID))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(normalize(name))
                .append(normalize(namespace))
                .append(normalize(softwareComponentVersionID))
                .toHashCode();
    }

    @Override
    public String toString() {
        return format("%s|%s|%s",
            defaultIfBlank(name, ""),
            defaultIfBlank(namespace, ""),
            defaultIfBlank(softwareComponentVersionID, "")
        );
    }
}
