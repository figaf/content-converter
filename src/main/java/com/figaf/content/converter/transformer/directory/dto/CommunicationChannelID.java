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
 * <p>Java class for CommunicationChannelID complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CommunicationChannelID">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PartyID" type="{http://sap.com/xi/BASIS}CommunicationPartyID" minOccurs="0"/>
 *         &lt;element name="ComponentID" type="{http://sap.com/xi/BASIS}CommunicationComponentComponentID"/>
 *         &lt;element name="ChannelID" type="{http://sap.com/xi/BASIS}CommunicationChannelChannelID"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommunicationChannelID", propOrder = {
    "partyID",
    "componentID",
    "channelID"
})
public class CommunicationChannelID {

    @XmlElement(name = "PartyID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String partyID;
    @XmlElement(name = "ComponentID", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String componentID;
    @XmlElement(name = "ChannelID", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String channelID;

    /**
     * Gets the value of the partyID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartyID() {
        return partyID;
    }

    /**
     * Sets the value of the partyID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartyID(String value) {
        this.partyID = value;
    }

    /**
     * Gets the value of the componentID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentID() {
        return componentID;
    }

    /**
     * Sets the value of the componentID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentID(String value) {
        this.componentID = value;
    }

    /**
     * Gets the value of the channelID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannelID() {
        return channelID;
    }

    /**
     * Sets the value of the channelID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannelID(String value) {
        this.channelID = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CommunicationChannelID that = (CommunicationChannelID) o;

        return new EqualsBuilder()
                .append(normalize(partyID), normalize(that.partyID))
                .append(normalize(componentID), normalize(that.componentID))
                .append(normalize(channelID), normalize(that.channelID))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(normalize(partyID))
                .append(normalize(componentID))
                .append(normalize(channelID))
                .toHashCode();
    }

    @Override
    public String toString() {
        return format("%s|%s|%s",
            defaultIfBlank(partyID, ""),
            defaultIfBlank(componentID, ""),
            defaultIfBlank(channelID, "")
        );

    }
}
