package com.figaf.content.converter.transformer.directory.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ParameterGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParameterGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ParameterGroupID" type="{http://sap.com/xi/BASIS}ParameterGroupID"/>
 *         &lt;element name="Parameter" type="{http://sap.com/xi/BASIS}RestrictedGenericProperty" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterGroup", propOrder = {
    "parameterGroupID",
    "parameter"
})
public class ParameterGroup implements Serializable {

    @XmlElement(name = "ParameterGroupID", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String parameterGroupID;
    @XmlElement(name = "Parameter", required = true)
    protected List<RestrictedGenericProperty> parameter;

    /**
     * Gets the value of the parameterGroupID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParameterGroupID() {
        return parameterGroupID;
    }

    /**
     * Sets the value of the parameterGroupID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParameterGroupID(String value) {
        this.parameterGroupID = value;
    }

    /**
     * Gets the value of the parameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RestrictedGenericProperty }
     * 
     * 
     */
    public List<RestrictedGenericProperty> getParameter() {
        if (parameter == null) {
            parameter = new ArrayList<RestrictedGenericProperty>();
        }
        return this.parameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ParameterGroup that = (ParameterGroup) o;

        return new EqualsBuilder()
                .append(parameterGroupID, that.parameterGroupID)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(parameterGroupID)
                .toHashCode();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private ParameterGroup payload;

        private Builder() {
            payload = new ParameterGroup();
        }

        public Builder parameterGroupID(String parameterGroupID) {
            this.payload.setParameterGroupID(parameterGroupID);
            return this;
        }

        public Builder parameter(List<RestrictedGenericProperty> parameter) {
            this.payload.getParameter().addAll(parameter);
            return this;
        }

        public ParameterGroup build() {
            return payload;
        }
    }
}
