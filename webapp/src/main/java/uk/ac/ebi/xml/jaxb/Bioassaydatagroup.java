//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.29 at 03:34:24 PM GMT 
//


package uk.ac.ebi.xml.jaxb;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}id"/>
 *         &lt;element ref="{}name"/>
 *         &lt;element ref="{}bioassaydatacubes"/>
 *         &lt;element ref="{}arraydesignprovider"/>
 *         &lt;element ref="{}dataformat"/>
 *         &lt;element ref="{}bioassays"/>
 *         &lt;element ref="{}isderived"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "id",
    "name",
    "bioassaydatacubes",
    "arraydesignprovider",
    "dataformat",
    "bioassays",
    "isderived"
})
@XmlRootElement(name = "bioassaydatagroup")
public class Bioassaydatagroup {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected BigInteger bioassaydatacubes;
    @XmlElement(required = true)
    protected Arraydesignprovider arraydesignprovider;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String dataformat;
    @XmlElement(required = true)
    protected BigInteger bioassays;
    @XmlElement(required = true)
    protected BigInteger isderived;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

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
     * Gets the value of the bioassaydatacubes property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBioassaydatacubes() {
        return bioassaydatacubes;
    }

    /**
     * Sets the value of the bioassaydatacubes property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBioassaydatacubes(BigInteger value) {
        this.bioassaydatacubes = value;
    }

    /**
     * Gets the value of the arraydesignprovider property.
     * 
     * @return
     *     possible object is
     *     {@link Arraydesignprovider }
     *     
     */
    public Arraydesignprovider getArraydesignprovider() {
        return arraydesignprovider;
    }

    /**
     * Sets the value of the arraydesignprovider property.
     * 
     * @param value
     *     allowed object is
     *     {@link Arraydesignprovider }
     *     
     */
    public void setArraydesignprovider(Arraydesignprovider value) {
        this.arraydesignprovider = value;
    }

    /**
     * Gets the value of the dataformat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataformat() {
        return dataformat;
    }

    /**
     * Sets the value of the dataformat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataformat(String value) {
        this.dataformat = value;
    }

    /**
     * Gets the value of the bioassays property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBioassays() {
        return bioassays;
    }

    /**
     * Sets the value of the bioassays property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBioassays(BigInteger value) {
        this.bioassays = value;
    }

    /**
     * Gets the value of the isderived property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getIsderived() {
        return isderived;
    }

    /**
     * Sets the value of the isderived property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setIsderived(BigInteger value) {
        this.isderived = value;
    }

}