package com.payline.payment.docapost.bean.rest.request.signature;

import static com.payline.payment.docapost.utils.DocapostConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.payline.payment.docapost.TmpTestData;
import com.payline.payment.docapost.bean.rest.request.WSSignature;
import com.payline.payment.docapost.exception.InvalidRequestException;

/**
 * Created by Thales on 29/08/2018.
 */
public class SendOtpRequest extends WSSignatureRequest implements WSSignature {

    private String transactionId;

    /**
     * Constructor
     */
    public SendOtpRequest(String creditorId,
                          String mandateRum,
                          String transactionId) {

        super(creditorId, mandateRum);

        this.transactionId = transactionId;

    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public Map<String, String> getRequestBodyMap() {

        Map<String, String> bodyMap = new HashMap<>();

        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__CREDITOR_ID, this.getCreditorId());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__MANDATE_RUM, this.getMandateRum());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__TRANSACTION_ID, this.getTransactionId());

        return bodyMap;

    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("***** TerminateSignatureRequest info\n");

        result.append("creditorId : " + creditorId + "\n");
        result.append("mandateRum : " + mandateRum + "\n");
        result.append("transactionId : " + transactionId + "\n");

        return result.toString();
    }

    //******************************************************************************************************************
    //***** BUILDER
    public static final class Builder {

        public SendOtpRequest fromPaylineRequest(/*PaylineRequest request*/) throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest();

            // TODO : get value from Payline request
            SendOtpRequest request = new SendOtpRequest(
                    TmpTestData.getInstance().creditorId,
                    TmpTestData.getInstance().rum,
                    TmpTestData.getInstance().transactionId
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