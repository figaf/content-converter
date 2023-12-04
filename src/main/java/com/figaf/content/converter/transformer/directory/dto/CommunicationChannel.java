package com.figaf.content.converter.transformer.directory.dto;

import com.figaf.content.converter.transformer.directory.dto.global.LONGDescription;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;


/**
 * <p>Java class for CommunicationChannel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CommunicationChannel">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MasterLanguage" type="{http://sap.com/xi/BASIS/Global}LanguageCode"/>
 *         &lt;element name="AdministrativeData" type="{http://sap.com/xi/BASIS}ObjectAdministrativeData" minOccurs="0"/>
 *         &lt;element name="Description" type="{http://sap.com/xi/BASIS/Global}LONG_Description" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="CommunicationChannelID" type="{http://sap.com/xi/BASIS}CommunicationChannelID"/>
 *         &lt;element name="AdapterMetadata" type="{http://sap.com/xi/BASIS}DesignObjectID" minOccurs="0"/>
 *         &lt;element name="Direction" type="{http://sap.com/xi/BASIS}CommunicationChannelDirection"/>
 *         &lt;element name="TransportProtocol" type="{http://sap.com/xi/BASIS/Global}LANGUAGEINDEPENDENT_SHORT_Name" minOccurs="0"/>
 *         &lt;element name="TransportProtocolVersion" type="{http://sap.com/xi/BASIS/Global}LANGUAGEINDEPENDENT_SHORT_Name" minOccurs="0"/>
 *         &lt;element name="MessageProtocol" type="{http://sap.com/xi/BASIS/Global}LANGUAGEINDEPENDENT_SHORT_Name" minOccurs="0"/>
 *         &lt;element name="MessageProtocolVersion" type="{http://sap.com/xi/BASIS/Global}LANGUAGEINDEPENDENT_SHORT_Name" minOccurs="0"/>
 *         &lt;element name="AdapterEngineName" type="{http://sap.com/xi/BASIS}AdapterEngineName" minOccurs="0"/>
 *         &lt;element name="AdapterSpecificAttribute" type="{http://sap.com/xi/BASIS}GenericProperty" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AdapterSpecificTableAttribute" type="{http://sap.com/xi/BASIS}GenericPropertyTable" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ModuleProcess" type="{http://sap.com/xi/BASIS}ModuleProcess" minOccurs="0"/>
 *         &lt;element name="SenderIdentifier" type="{http://sap.com/xi/BASIS}ChannelAdditionalIdentifier" minOccurs="0"/>
 *         &lt;element name="ReceiverIdentifier" type="{http://sap.com/xi/BASIS}ChannelAdditionalIdentifier" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommunicationChannel", propOrder = {
    "masterLanguage",
    "administrativeData",
    "description",
    "communicationChannelID",
    "adapterMetadata",
    "direction",
    "transportProtocol",
    "transportProtocolVersion",
    "messageProtocol",
    "messageProtocolVersion",
    "adapterEngineName",
    "adapterSpecificAttribute",
    "adapterSpecificTableAttribute",
    "moduleProcess",
    "senderIdentifier",
    "receiverIdentifier"
})
public class CommunicationChannel {

    @XmlElement(name = "MasterLanguage", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")
    protected String masterLanguage;
    @XmlElement(name = "AdministrativeData")
    protected ObjectAdministrativeData administrativeData;
    @XmlElement(name = "Description")
    protected List<LONGDescription> description;
    @XmlElement(name = "CommunicationChannelID", required = true)
    protected CommunicationChannelID communicationChannelID;
    @XmlElement(name = "AdapterMetadata")
    protected DesignObjectID adapterMetadata;
    @XmlElement(name = "Direction", required = true)
    @XmlSchemaType(name = "token")
    protected CommunicationChannelDirection direction;
    @XmlElement(name = "TransportProtocol")
    protected String transportProtocol;
    @XmlElement(name = "TransportProtocolVersion")
    protected String transportProtocolVersion;
    @XmlElement(name = "MessageProtocol")
    protected String messageProtocol;
    @XmlElement(name = "MessageProtocolVersion")
    protected String messageProtocolVersion;
    @XmlElement(name = "AdapterEngineName")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String adapterEngineName;
    @XmlElement(name = "AdapterSpecificAttribute")
    protected List<GenericProperty> adapterSpecificAttribute;
    @XmlElement(name = "AdapterSpecificTableAttribute")
    protected List<GenericPropertyTable> adapterSpecificTableAttribute;
    @XmlElement(name = "ModuleProcess")
    protected ModuleProcess moduleProcess;
    @XmlElement(name = "SenderIdentifier")
    protected ChannelAdditionalIdentifier senderIdentifier;
    @XmlElement(name = "ReceiverIdentifier")
    protected ChannelAdditionalIdentifier receiverIdentifier;

    /**
     * Gets the value of the masterLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMasterLanguage() {
        return masterLanguage;
    }

    /**
     * Sets the value of the masterLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMasterLanguage(String value) {
        this.masterLanguage = value;
    }

    /**
     * Gets the value of the administrativeData property.
     * 
     * @return
     *     possible object is
     *     {@link ObjectAdministrativeData }
     *     
     */
    public ObjectAdministrativeData getAdministrativeData() {
        return administrativeData;
    }

    /**
     * Sets the value of the administrativeData property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectAdministrativeData }
     *     
     */
    public void setAdministrativeData(ObjectAdministrativeData value) {
        this.administrativeData = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LONGDescription }
     * 
     * 
     */
    public List<LONGDescription> getDescription() {
        if (description == null) {
            description = new ArrayList<LONGDescription>();
        }
        return this.description;
    }

    /**
     * Gets the value of the communicationChannelID property.
     * 
     * @return
     *     possible object is
     *     {@link CommunicationChannelID }
     *     
     */
    public CommunicationChannelID getCommunicationChannelID() {
        return communicationChannelID;
    }

    /**
     * Sets the value of the communicationChannelID property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommunicationChannelID }
     *     
     */
    public void setCommunicationChannelID(CommunicationChannelID value) {
        this.communicationChannelID = value;
    }

    /**
     * Gets the value of the adapterMetadata property.
     * 
     * @return
     *     possible object is
     *     {@link DesignObjectID }
     *     
     */
    public DesignObjectID getAdapterMetadata() {
        return adapterMetadata;
    }

    /**
     * Sets the value of the adapterMetadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link DesignObjectID }
     *     
     */
    public void setAdapterMetadata(DesignObjectID value) {
        this.adapterMetadata = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link CommunicationChannelDirection }
     *     
     */
    public CommunicationChannelDirection getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommunicationChannelDirection }
     *     
     */
    public void setDirection(CommunicationChannelDirection value) {
        this.direction = value;
    }

    /**
     * Gets the value of the transportProtocol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransportProtocol() {
        return transportProtocol;
    }

    /**
     * Sets the value of the transportProtocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransportProtocol(String value) {
        this.transportProtocol = value;
    }

    /**
     * Gets the value of the transportProtocolVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransportProtocolVersion() {
        return transportProtocolVersion;
    }

    /**
     * Sets the value of the transportProtocolVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransportProtocolVersion(String value) {
        this.transportProtocolVersion = value;
    }

    /**
     * Gets the value of the messageProtocol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageProtocol() {
        return messageProtocol;
    }

    /**
     * Sets the value of the messageProtocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageProtocol(String value) {
        this.messageProtocol = value;
    }

    /**
     * Gets the value of the messageProtocolVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageProtocolVersion() {
        return messageProtocolVersion;
    }

    /**
     * Sets the value of the messageProtocolVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageProtocolVersion(String value) {
        this.messageProtocolVersion = value;
    }

    /**
     * Gets the value of the adapterEngineName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdapterEngineName() {
        return adapterEngineName;
    }

    /**
     * Sets the value of the adapterEngineName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdapterEngineName(String value) {
        this.adapterEngineName = value;
    }

    /**
     * Gets the value of the adapterSpecificAttribute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the adapterSpecificAttribute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdapterSpecificAttribute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GenericProperty }
     * 
     * 
     */
    public List<GenericProperty> getAdapterSpecificAttribute() {
        if (adapterSpecificAttribute == null) {
            adapterSpecificAttribute = new ArrayList<GenericProperty>();
        }
        return this.adapterSpecificAttribute;
    }

    /**
     * Gets the value of the adapterSpecificTableAttribute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the adapterSpecificTableAttribute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdapterSpecificTableAttribute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GenericPropertyTable }
     * 
     * 
     */
    public List<GenericPropertyTable> getAdapterSpecificTableAttribute() {
        if (adapterSpecificTableAttribute == null) {
            adapterSpecificTableAttribute = new ArrayList<GenericPropertyTable>();
        }
        return this.adapterSpecificTableAttribute;
    }

    /**
     * Gets the value of the moduleProcess property.
     * 
     * @return
     *     possible object is
     *     {@link ModuleProcess }
     *     
     */
    public ModuleProcess getModuleProcess() {
        return moduleProcess;
    }

    /**
     * Sets the value of the moduleProcess property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModuleProcess }
     *     
     */
    public void setModuleProcess(ModuleProcess value) {
        this.moduleProcess = value;
    }

    /**
     * Gets the value of the senderIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link ChannelAdditionalIdentifier }
     *     
     */
    public ChannelAdditionalIdentifier getSenderIdentifier() {
        return senderIdentifier;
    }

    /**
     * Sets the value of the senderIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChannelAdditionalIdentifier }
     *     
     */
    public void setSenderIdentifier(ChannelAdditionalIdentifier value) {
        this.senderIdentifier = value;
    }

    /**
     * Gets the value of the receiverIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link ChannelAdditionalIdentifier }
     *     
     */
    public ChannelAdditionalIdentifier getReceiverIdentifier() {
        return receiverIdentifier;
    }

    /**
     * Sets the value of the receiverIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChannelAdditionalIdentifier }
     *     
     */
    public void setReceiverIdentifier(ChannelAdditionalIdentifier value) {
        this.receiverIdentifier = value;
    }

    public boolean containsPassword() {
        for (ParameterGroup parameterGroup : getModuleProcess().getParameterGroup()) {
            for (RestrictedGenericProperty property : parameterGroup.getParameter()) {
                if (propertyIsPassword(property)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<String, RestrictedGenericProperty> buildPasswordParametersMap() {
        Map<String, RestrictedGenericProperty> channelModelReadByHmiGenericPropertyMap = new HashMap<>();
        for (ParameterGroup parameterGroup : moduleProcess.getParameterGroup()) {
            List<RestrictedGenericProperty> properties = parameterGroup.getParameter();
            for (RestrictedGenericProperty property : properties) {
                if (propertyIsPassword(property)) {
                    channelModelReadByHmiGenericPropertyMap.put(format("%s|%s", parameterGroup.getParameterGroupID(), property.getName()), property);
                }
            }
        }
        return channelModelReadByHmiGenericPropertyMap;
    }

    public static boolean propertyIsPassword(RestrictedGenericProperty property) {
        return property.getName().startsWith("pwd") || property.getName().startsWith("cryptedpassword");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CommunicationChannel that = (CommunicationChannel) o;

        return new EqualsBuilder()
                .append(communicationChannelID, that.communicationChannelID)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(communicationChannelID)
                .toHashCode();
    }

    @Override
    public String toString() {
        return communicationChannelID.toString();
    }
}
