package com.payline.payment.docapost.utils;

/**
 * Created by Thales on 29/08/2018.
 */
public class DocapostConstants {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static final String CONFIG__HTTP_CONNECT_TIMEOUT                     = "http.connectTimeout";
    public static final String CONFIG__HTTP_WRITE_TIMEOUT                       = "http.writeTimeout";
    public static final String CONFIG__HTTP_READ_TIMEOUT                        = "http.readTimeout";
    public static final String CONFIG__SCHEME = "docapost.scheme";
    public static final String CONFIG__HOST                                     = "docapost.host";
    public static final String CONFIG__PATH_WSMANDATE_MANDATE_CREATE            = "docapost.mandate.create.path";
    public static final String CONFIG__PATH_WSMANDATE_ORDER_CREATE              = "docapost.order.create.path";
    public static final String CONFIG__PATH_WSSIGNATURE_INITIATE_SIGNATURE      = "docapost.initiate.signature.path";
    public static final String CONFIG__PATH_WSSIGNATURE_TERMINATE_SIGNATURE     = "docapost.terminate.signature.path";
    public static final String CONFIG__PATH_WSSIGNATURE_SEND_OTP                = "docapost.send.otp.path";
    public static final String CONFIG__PATH_WSSIGNATURE_SET_CODE                = "docapost.set.code.path";

    public static final String SIGNATURE_WS_REQUEST_FIELD__CREDITOR_ID      = "creditorId";
    public static final String SIGNATURE_WS_REQUEST_FIELD__MANDATE_RUM      = "mandateRum";
    public static final String SIGNATURE_WS_REQUEST_FIELD__TRANSACTION_ID   = "transactionId";
    public static final String SIGNATURE_WS_REQUEST_FIELD__SIGNATURE_ID     = "signatureId";
    public static final String SIGNATURE_WS_REQUEST_FIELD__OTP              = "otp";
    public static final String SIGNATURE_WS_REQUEST_FIELD__SUCCESS          = "success";

    public static final String MANDATE_WS_XML__SEPALIA_ERROR        = "<sepalia>";
    public static final String MANDATE_WS_XML__MANDATE_CREATE_DTO   = "<WSMandateDTO>";
    public static final String MANDATE_WS_XML__ORDER_CREATE_DTO     = "<WSDDOrderDTO>";

}