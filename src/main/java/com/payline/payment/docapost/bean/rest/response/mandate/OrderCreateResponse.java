package com.payline.payment.docapost.bean.rest.response.mandate;

import com.payline.payment.docapost.bean.rest.response.AbstractXmlResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Thales on 29/08/2018.
 */
@XmlRootElement(name = "WSDDOrderDTO")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderCreateResponse extends AbstractXmlResponse {

    // Original fields from request
    @XmlElement(name = "creditorId")
    private String creditorId;

    @XmlElement(name = "rum")
    private String rum;

    @XmlElement(name = "amount")
    private Float amount;

    @XmlElement(name = "label")
    private String label;

    // Added response fields
    @XmlElement(name = "e2eId")
    private String e2eId;

    @XmlElement(name = "dueDate")
    private String dueDate;

    @XmlElement(name = "remitDate")
    private String remitDate;

    @XmlElement(name = "sequence")
    private String sequence;

    @XmlElement(name = "status")
    private String status;

    @XmlElement(name = "identifier")
    private String identifier;

    /**
     * Public default constructor
     */
    public OrderCreateResponse() { }

    public String getCreditorId() {
        return creditorId;
    }

    public void setCreditorId(String creditorId) {
        this.creditorId = creditorId;
    }

    public String getRum() {
        return rum;
    }

    public void setRum(String rum) {
        this.rum = rum;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getE2eId() {
        return e2eId;
    }

    public void setE2eId(String e2eId) {
        this.e2eId = e2eId;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getRemitDate() {
        return remitDate;
    }

    public void setRemitDate(String remitDate) {
        this.remitDate = remitDate;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("***** OrderCreateResponse info\n");

        result.append("creditorId : " + creditorId + "\n");
        result.append("rum : " + rum + "\n");
        result.append("amount : " + amount + "\n");
        result.append("label : " + label + "\n");
        result.append("e2eId : " + e2eId + "\n");
        result.append("dueDate : " + dueDate + "\n");
        result.append("remitDate : " + remitDate + "\n");
        result.append("sequence : " + sequence + "\n");
        result.append("status : " + status + "\n");
        result.append("identifier : " + identifier + "\n");

        return result.toString();
    }

    //******************************************************************************************************************
    //***** BUILDER
    public static final class Builder {
        public OrderCreateResponse fromXml(String xmlContent) {
            return (OrderCreateResponse) parse(OrderCreateResponse.class, xmlContent);
        }
    }
    //***** BUILDER
    //******************************************************************************************************************

}