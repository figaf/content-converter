package com.figaf.content.converter.transformer.directory.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;


/**
 * <p>Java class for ProcessStep complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessStep">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ModuleName" type="{http://sap.com/xi/BASIS/Global}LANGUAGEINDEPENDENT_Name"/>
 *         &lt;element name="ModuleType" type="{http://sap.com/xi/BASIS}ModuleTypeCode" minOccurs="0"/>
 *         &lt;element name="ParameterGroupID" type="{http://sap.com/xi/BASIS}ParameterGroupID" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessStep", propOrder = {
    "moduleName",
    "moduleType",
    "parameterGroupID"
})
public class ProcessStep implements Serializable {

    @XmlElement(name = "ModuleName", required = true)
    protected String moduleName;
    @XmlElement(name = "ModuleType")
    @XmlSchemaType(name = "token")
    protected ModuleTypeCode moduleType;
    @XmlElement(name = "ParameterGroupID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String parameterGroupID;

    /**
     * Gets the value of the moduleName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Sets the value of the moduleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModuleName(String value) {
        this.moduleName = value;
    }

    /**
     * Gets the value of the moduleType property.
     * 
     * @return
     *     possible object is
     *     {@link ModuleTypeCode }
     *     
     */
    public ModuleTypeCode getModuleType() {
        return moduleType;
    }

    /**
     * Sets the value of the moduleType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModuleTypeCode }
     *     
     */
    public void setModuleType(ModuleTypeCode value) {
        this.moduleType = value;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProcessStep that = (ProcessStep) o;

        return new EqualsBuilder()
                .append(moduleName, that.moduleName)
                .append(parameterGroupID, that.parameterGroupID)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(moduleName)
                .append(parameterGroupID)
                .toHashCode();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private ProcessStep payload;

        private Builder() {
            payload = new ProcessStep();
        }

        public Builder moduleName(String moduleName) {
            this.payload.setModuleName(moduleName);
            return this;
        }

        public Builder moduleType(ModuleTypeCode moduleType) {
            this.payload.setModuleType(moduleType);
            return this;
        }

        public Builder parameterGroupID(String parameterGroupID) {
            this.payload.setParameterGroupID(parameterGroupID);
            return this;
        }

        public ProcessStep build() {
            return payload;
        }
    }
}
