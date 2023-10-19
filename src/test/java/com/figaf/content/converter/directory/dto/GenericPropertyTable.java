package com.figaf.content.converter.directory.dto;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for GenericPropertyTable complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericPropertyTable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://sap.com/xi/BASIS}PropertyName"/>
 *         &lt;element name="Namespace" type="{http://sap.com/xi/BASIS}NamespaceURI" minOccurs="0"/>
 *         &lt;element name="ValueTableRow" type="{http://sap.com/xi/BASIS}GenericTableRow" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericPropertyTable", propOrder = {
    "name",
    "namespace",
    "valueTableRow"
})
public class GenericPropertyTable implements Serializable {

    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "Namespace")
    @XmlSchemaType(name = "anyURI")
    protected String namespace;
    @XmlElement(name = "ValueTableRow")
    protected List<GenericTableRow> valueTableRow;

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
     * Gets the value of the valueTableRow property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valueTableRow property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValueTableRow().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GenericTableRow }
     * 
     * 
     */
    public List<GenericTableRow> getValueTableRow() {
        if (valueTableRow == null) {
            valueTableRow = new ArrayList<GenericTableRow>();
        }
        return this.valueTableRow;
    }

}
