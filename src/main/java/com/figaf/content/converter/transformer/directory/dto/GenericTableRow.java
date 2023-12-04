package com.figaf.content.converter.transformer.directory.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for GenericTableRow complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericTableRow">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ValueTableCell" type="{http://sap.com/xi/BASIS}GenericTableRowTableCell" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericTableRow", propOrder = {
    "valueTableCell"
})
public class GenericTableRow implements Serializable {

    @XmlElement(name = "ValueTableCell")
    protected List<GenericTableRowTableCell> valueTableCell;

    /**
     * Gets the value of the valueTableCell property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valueTableCell property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValueTableCell().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GenericTableRowTableCell }
     * 
     * 
     */
    public List<GenericTableRowTableCell> getValueTableCell() {
        if (valueTableCell == null) {
            valueTableCell = new ArrayList<GenericTableRowTableCell>();
        }
        return this.valueTableCell;
    }

}
