package com.payline.payment.docapost.bean.rest.request.signature;

import com.payline.payment.docapost.TmpTestData;
import com.payline.payment.docapost.bean.rest.request.WSSignature;
import com.payline.payment.docapost.exception.InvalidRequestException;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.docapost.utils.DocapostConstants.SIGNATURE_WS_REQUEST_FIELD__CREDITOR_ID;
import static com.payline.payment.docapost.utils.DocapostConstants.SIGNATURE_WS_REQUEST_FIELD__MANDATE_RUM;

/**
 * Created by Thales on 29/08/2018.
 */
public class InitiateSignatureRequest extends WSSignatureRequest implements WSSignature {

    /**
     * Constructor
     */
    public InitiateSignatureRequest(String creditorId,
                                    String mandateRum) {

        super(creditorId, mandateRum);

    }

    /**
     * Build an attribute map
     * @return
     */
    @Override
    public Map<String, String> getRequestBodyMap() {

        Map<String, String> bodyMap = new HashMap<>();

        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__CREDITOR_ID, this.getCreditorId());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__MANDATE_RUM, this.getMandateRum());

        return bodyMap;

    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("***** InitiateSignatureRequest info\n");

        result.append("creditorId : " + creditorId + "\n");
        result.append("mandateRum : " + mandateRum + "\n");

        return result.toString();
    }

    //******************************************************************************************************************
    //***** BUILDER
    public static final class Builder {

        public InitiateSignatureRequest fromPaylineRequest(/*PaylineRequest request*/) throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest();

            // TODO : get value from Payline request
            InitiateSignatureRequest request = new InitiateSignatureRequest(
                    TmpTestData.getInstance().creditorId,
                    TmpTestData.getInstance().rum
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