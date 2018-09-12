package com.payline.payment.docapost.bean.rest.request.signature;

import com.payline.payment.docapost.TmpTestData;
import com.payline.payment.docapost.bean.rest.request.WSSignature;
import com.payline.payment.docapost.exception.InvalidRequestException;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.docapost.utils.DocapostConstants.*;

/**
 * Created by Thales on 05/09/2018.
 */
public class TerminateSignatureRequest extends WSSignatureRequest implements WSSignature {

    private String transactionId;
    private Boolean success;

    /**
     * Constructor
     */
    public TerminateSignatureRequest(String creditorId,
                                     String mandateRum,
                                     String transactionId,
                                     Boolean success) {

        super(creditorId, mandateRum);

        this.transactionId  = transactionId;
        this.success        = success;

    }

    public String getTransactionId() {
        return transactionId;
    }

    public Boolean getSuccess() {
        return success;
    }

    @Override
    public Map<String, String> getRequestBodyMap() {

        Map<String, String> bodyMap = new HashMap<>();

        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__CREDITOR_ID, this.getCreditorId());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__MANDATE_RUM, this.getMandateRum());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__TRANSACTION_ID, this.getTransactionId());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__SUCCESS, String.valueOf(this.getSuccess().booleanValue()));

        return bodyMap;

    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("***** TerminateSignatureRequest info\n");

        result.append("creditorId : " + creditorId + "\n");
        result.append("mandateRum : " + mandateRum + "\n");
        result.append("transactionId : " + transactionId + "\n");
        result.append("success : " + success + "\n");

        return result.toString();
    }

    //******************************************************************************************************************
    //***** BUILDER
    public static final class Builder {

        public TerminateSignatureRequest fromPaylineRequest(/*PaylineRequest request*/) throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest();

            // TODO : get value from Payline request
            TerminateSignatureRequest request = new TerminateSignatureRequest(
                    TmpTestData.getInstance().creditorId,
                    TmpTestData.getInstance().rum,
                    TmpTestData.getInstance().transactionId,
                    TmpTestData.getInstance().signatureAccess
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