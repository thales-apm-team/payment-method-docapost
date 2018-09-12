package com.payline.payment.docapost.bean.rest.response.mandate;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.payline.payment.docapost.bean.rest.common.Debtor;
import com.payline.payment.docapost.bean.rest.response.AbstractXmlResponse;

/**
 * Created by Thales on 29/08/2018.
 */
@XmlRootElement(name = "WSMandateDTO")
@XmlAccessorType(XmlAccessType.FIELD)
public class MandateCreateResponse extends AbstractXmlResponse {

    // Original fields from request
    @XmlElement(name = "creditorId")
    private String creditorId;

    @XmlElement(name = "flowName")
    private String flowName;

    @XmlElement(name = "rum")
    private String rum;

    @XmlElement(name = "recurrent")
    private Boolean recurrent;

    @XmlElement(name = "contextIdentifier")
    private String contextIdentifier;

    @XmlElement(name = "language")
    private String language;

    @XmlElement(name = "debtor")
    private Debtor debtor;

    // Added response fields
    @XmlElement(name = "creditorIcs")
    private String creditorIcs;

    @XmlElement(name = "status")
    private String status;

    @XmlElement(name = "mode")
    private String mode;

    /**
     * Public default constructor
     */
    public MandateCreateResponse() { }

    public String getCreditorId() {
        return creditorId;
    }

    public void setCreditorId(String creditorId) {
        this.creditorId = creditorId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getRum() {
        return rum;
    }

    public void setRum(String rum) {
        this.rum = rum;
    }

    public Boolean isRecurrent() {
        return recurrent;
    }

    public void setRecurrent(Boolean recurrent) {
        this.recurrent = recurrent;
    }

    public String getContextIdentifier() {
        return contextIdentifier;
    }

    public void setContextIdentifier(String contextIdentifier) {
        this.contextIdentifier = contextIdentifier;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Debtor getDebtor() {
        return debtor;
    }

    public void setDebtor(Debtor debtor) {
        this.debtor = debtor;
    }

    public String getCreditorIcs() {
        return creditorIcs;
    }

    public void setCreditorIcs(String creditorIcs) {
        this.creditorIcs = creditorIcs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("***** MandateCreateResponse info\n");

        result.append("creditorId : " + creditorId + "\n");
        result.append("creditorIcs : " + creditorIcs + "\n");
        result.append("flowName : " + flowName + "\n");
        result.append("rum : " + rum + "\n");
        result.append("recurrent : " + recurrent + "\n");
        result.append("contextIdentifier : " + contextIdentifier + "\n");
        result.append("language : " + language + "\n");
        result.append("status : " + status + "\n");
        result.append("mode : " + mode + "\n");
        result.append(debtor.toString() + "\n");

        return result.toString();
    }

    //******************************************************************************************************************
    //***** BUILDER
    public static final class Builder {
        public MandateCreateResponse fromXml(String xmlContent) {
            return (MandateCreateResponse) parse(MandateCreateResponse.class, xmlContent);
        }
    }
    //***** BUILDER
    //******************************************************************************************************************

}