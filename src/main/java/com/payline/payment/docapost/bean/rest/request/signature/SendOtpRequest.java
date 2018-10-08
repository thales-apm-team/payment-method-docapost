package com.payline.payment.docapost.bean.rest.request.signature;

import static com.payline.payment.docapost.utils.DocapostConstants.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.payline.payment.docapost.TmpTestData;
import com.payline.payment.docapost.bean.rest.request.WSSignature;
import com.payline.payment.docapost.exception.InvalidRequestException;
import com.payline.payment.docapost.utils.DocapostLocalParam;
import com.payline.payment.docapost.utils.DocapostUtils;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

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

        public SendOtpRequest fromPaylineRequest(PaymentRequest paylineRequest, DocapostLocalParam docapostLocalParam) throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest(paylineRequest, docapostLocalParam);

            SendOtpRequest request = new SendOtpRequest(
                    paylineRequest.getContractConfiguration().getContractProperties().get( CONTRACT_CONFIG__CREDITOR_ID ).getValue(),
                    docapostLocalParam.getMandateRum(),
                    docapostLocalParam.getTransactionId()
            );

            return request;

        }

        private void checkInputRequest(PaymentRequest paylineRequest, DocapostLocalParam docapostLocalParam) throws InvalidRequestException  {
            if ( paylineRequest == null ) {
                throw new InvalidRequestException( "Request must not be null" );
            }

            if ( paylineRequest.getContractConfiguration() == null
                    || paylineRequest.getContractConfiguration().getContractProperties() == null ) {
                throw new InvalidRequestException( "Contract configuration properties object must not be null" );
            }
            Map<String, ContractProperty> contractProperties = paylineRequest.getContractConfiguration().getContractProperties();
            if ( contractProperties.get( CONTRACT_CONFIG__CREDITOR_ID ) == null ) {
                throw new InvalidRequestException( "Missing contract configuration property: creditor id" );
            }

            if ( docapostLocalParam == null
                    || DocapostUtils.isEmpty(docapostLocalParam.getMandateRum()) ) {
                throw new InvalidRequestException( "Missing mandatory property: mandate rum" );
            }

            if ( docapostLocalParam == null
                    || DocapostUtils.isEmpty(docapostLocalParam.getTransactionId()) ) {
                throw new InvalidRequestException( "Missing mandatory property: transaction id" );
            }

        }

    }
    //***** BUILDER
    //******************************************************************************************************************

}