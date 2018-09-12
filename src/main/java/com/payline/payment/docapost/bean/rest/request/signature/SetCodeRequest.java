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
public class SetCodeRequest extends WSSignatureRequest implements WSSignature {

    private String transactionId;
    private String signatureId;
    private String otp;

    /**
     * Constructor
     */
    public SetCodeRequest(String creditorId,
                          String mandateRum,
                          String transactionId,
                          String signatureId,
                          String otp) {

        super(creditorId, mandateRum);

        this.transactionId  = transactionId;
        this.signatureId    = signatureId;
        this.otp            = otp;

    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getSignatureId() {
        return signatureId;
    }

    public String getOtp() {
        return otp;
    }

    @Override
    public Map<String, String> getRequestBodyMap() {

        Map<String, String> bodyMap = new HashMap<>();

        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__CREDITOR_ID, this.getCreditorId());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__MANDATE_RUM, this.getMandateRum());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__TRANSACTION_ID, this.getTransactionId());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__SIGNATURE_ID, this.getSignatureId());
        bodyMap.put(SIGNATURE_WS_REQUEST_FIELD__OTP, this.getOtp());

        return bodyMap;

    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("***** TerminateSignatureRequest info\n");

        result.append("creditorId : " + creditorId + "\n");
        result.append("mandateRum : " + mandateRum + "\n");
        result.append("transactionId : " + transactionId + "\n");
        result.append("signatureId : " + signatureId + "\n");
        result.append("otp : " + otp + "\n");

        return result.toString();
    }

    //******************************************************************************************************************
    //***** BUILDER
    public static final class Builder {

        public SetCodeRequest fromPaylineRequest(/*PaylineRequest request*/) throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest();

            // TODO : get value from Payline request
            SetCodeRequest request = new SetCodeRequest(
                    TmpTestData.getInstance().creditorId,
                    TmpTestData.getInstance().rum,
                    TmpTestData.getInstance().transactionId,
                    TmpTestData.getInstance().signatureId,
                    TmpTestData.getInstance().otp
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