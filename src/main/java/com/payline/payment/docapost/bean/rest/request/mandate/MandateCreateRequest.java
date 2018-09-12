package com.payline.payment.docapost.bean.rest.request.mandate;

import com.payline.payment.docapost.TmpTestData;
import com.payline.payment.docapost.bean.rest.common.Debtor;
import com.payline.payment.docapost.exception.InvalidRequestException;

import javax.xml.bind.annotation.*;

/**
 * Created by Thales on 29/08/2018.
 * Cf. http://blog.paumard.org/cours/jaxb-rest/chap02-jaxb-annotations.html §3.3.3
 */
@XmlRootElement(name = "WSMandateDTO")
@XmlType(
        propOrder = {
                "creditorId",
                "flowName",
                "rum",
                "recurrent",
                "contextIdentifier",
                "language",
                "debtor"
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class MandateCreateRequest extends AbstractXmlRequest {

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

    /**
     * Public default constructor
     */
    public MandateCreateRequest() { }

    /**
     * Constructor
     */
    public MandateCreateRequest(String creditorId,
                                String flowName,
                                String rum,
                                Boolean recurrent,
                                String contextIdentifier,
                                String language,
                                Debtor debtor) {

        this.creditorId             = creditorId;
        this.flowName               = flowName;
        this.rum                    = rum;
        this.recurrent              = recurrent;
        this.contextIdentifier      = contextIdentifier;
        this.language               = language;
        this.debtor                 = debtor;

    }

    public String getCreditorId() {
        return creditorId;
    }

    public String getFlowName() {
        return flowName;
    }

    public String getRum() {
        return rum;
    }

    public boolean isRecurrent() {
        return recurrent;
    }

    public String getContextIdentifier() {
        return contextIdentifier;
    }

    public String getLanguage() {
        return language;
    }

    public Debtor getDebtor() {
        return debtor;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("***** MandateCreateRequest info\n");

        result.append("creditorId : " + creditorId + "\n");
        result.append("flowName : " + flowName + "\n");
        result.append("rum : " + rum + "\n");
        result.append("recurrent : " + recurrent + "\n");
        result.append("contextIdentifier : " + contextIdentifier + "\n");
        result.append("language : " + language + "\n");
        result.append(debtor.toString() + "\n");

        return result.toString();
    }

    //******************************************************************************************************************
    //***** BUILDER
    public static final class Builder {

        // FIXME : add Payline request parameter
        public MandateCreateRequest fromPaylineRequest(/*PaylineRequest request*/) throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest();

            // TODO : get value from Payline request
            Debtor debtor = new Debtor()
                    .lastName(TmpTestData.getInstance().debtorLastName)
                    .firstName(TmpTestData.getInstance().debtorFirstName)
                    .iban(TmpTestData.getInstance().debtorIban)
                    .street(TmpTestData.getInstance().debtorStreet)
                    .postalCode(TmpTestData.getInstance().debtorPostalCode)
                    .town(TmpTestData.getInstance().debtorTown)
                    .phoneNumber(TmpTestData.getInstance().debtorPhoneNumber)
                    .countryCode(TmpTestData.getInstance().debtorCountryCode);

            MandateCreateRequest request = new MandateCreateRequest(
                    TmpTestData.getInstance().creditorId,
                    TmpTestData.getInstance().flowName,
                    TmpTestData.getInstance().rum,
                    TmpTestData.getInstance().recurrent,
                    TmpTestData.getInstance().contextIdentifier,
                    TmpTestData.getInstance().language,
                    debtor
            );

            return request;

        }

        // FIXME : add Payline request parameter
        private void checkInputRequest(/*PaylineRequest request*/) throws InvalidRequestException  {
//            if ( request == null ) {
//                throw new InvalidRequestException( "Request must not be null" );
//            }

            // TODO ...

        }

    }
    //***** BUILDER
    //******************************************************************************************************************

}