package com.payline.payment.docapost.bean.rest.request.mandate;

import com.payline.payment.docapost.TmpTestData;
import com.payline.payment.docapost.exception.InvalidRequestException;

import javax.xml.bind.annotation.*;

/**
 * Created by Thales on 29/08/2018.
 */
@XmlRootElement(name = "WSDDOrderDTO")
@XmlType(
        propOrder = {
                "creditorId",
                "rum",
                "amount",
                "label"
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderCreateRequest extends AbstractXmlRequest {

    @XmlElement(name = "creditorId")
    private String creditorId;

    @XmlElement(name = "rum")
    private String rum;

    @XmlElement(name = "amount")
    private Float amount;

    @XmlElement(name = "label")
    private String label;

    /**
     * Public default constructor
     */
    public OrderCreateRequest() { }

    /**
     * Constructor
     */
    public OrderCreateRequest(String creditorId,
                              String rum,
                              Float amount,
                              String label) {

        this.creditorId     = creditorId;
        this.rum            = rum;
        this.amount         = amount;
        this.label          = label;

    }

    public String getCreditorId() {
        return creditorId;
    }

    public String getRum() {
        return rum;
    }

    public Float getAmount() {
        return amount;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("***** OrderCreateRequest info\n");

        result.append("creditorId : " + creditorId + "\n");
        result.append("rum : " + rum + "\n");
        result.append("amount : " + amount + "\n");
        result.append("label : " + label + "\n");

        return result.toString();
    }

    //******************************************************************************************************************
    //***** BUILDER
    public static final class Builder {

        // FIXME : add Payline request parameter
        public OrderCreateRequest fromPaylineRequest() throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest();

            // TODO : get value from Payline request
            OrderCreateRequest request = new OrderCreateRequest(
                    TmpTestData.getInstance().creditorId,
                    TmpTestData.getInstance().rum,
                    TmpTestData.getInstance().amount,
                    TmpTestData.getInstance().label
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