package com.payline.payment.docapost.service.impl;

import com.payline.payment.docapost.bean.rest.common.Debtor;
import com.payline.payment.docapost.bean.rest.request.RequestBuilderFactory;
import com.payline.payment.docapost.bean.rest.request.mandate.MandateCreateRequest;
import com.payline.payment.docapost.bean.rest.request.signature.InitiateSignatureRequest;
import com.payline.payment.docapost.bean.rest.request.signature.SendOtpRequest;
import com.payline.payment.docapost.bean.rest.response.ResponseBuilderFactory;
import com.payline.payment.docapost.bean.rest.response.error.XmlErrorResponse;
import com.payline.payment.docapost.bean.rest.response.mandate.AbstractXmlResponse;
import com.payline.payment.docapost.bean.rest.response.mandate.WSMandateDTOResponse;
import com.payline.payment.docapost.bean.rest.response.signature.InitiateSignatureResponse;
import com.payline.payment.docapost.bean.rest.response.signature.SendOtpResponse;
import com.payline.payment.docapost.exception.InvalidRequestException;
import com.payline.payment.docapost.service.PaymentServiceStep;
import com.payline.payment.docapost.utils.ActionRequestResponse;
import com.payline.payment.docapost.utils.DocapostFormUtils;
import com.payline.payment.docapost.utils.DocapostLocalParam;
import com.payline.payment.docapost.utils.config.ConfigEnvironment;
import com.payline.payment.docapost.utils.config.ConfigProperties;
import com.payline.payment.docapost.utils.http.DocapostHttpClient;
import com.payline.payment.docapost.utils.http.StringResponse;
import com.payline.payment.docapost.utils.type.WSRequestResultEnum;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.docapost.utils.DocapostConstants.*;

public class PaymentServiceStep02 implements PaymentServiceStep {

    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceStep02.class);

    private final DocapostHttpClient httpClient;

    private DocapostLocalParam docapostLocalParam;

    public PaymentServiceStep02(DocapostHttpClient httpClient) {
        this.httpClient = httpClient;
        this.docapostLocalParam = DocapostLocalParam.getInstance();
    }

    @Override
    public PaymentResponse processPaymentStep(PaymentRequest paymentRequest,
                                              ConfigEnvironment env,
                                              DocapostLocalParam localParam,
                                              String credentials) {

        this.docapostLocalParam = localParam;

        String scheme = ConfigProperties.get(CONFIG_SCHEME, env);
        String host = ConfigProperties.get(CONFIG_HOST, env);

            /*

            NOTE CONCERNANT LES DIFFERENCES AU NIVEAU DES REPONSES AUX APPELS WS SUR LES APIS MANDATE & SIGNATURE:

                Lors d'appels sur l'API MandateWS

                - soit le WS repond correctement avec un code 200 et un body avec un content de la reponse au format XML
                commencant par une balise correspondant au nom de l'objet DTO (par ex. <WSMandateDTO>

                - soit le WS repond en erreur avec un code different de 200 mais dans le cas d'une erreur metier du cote
                partenaire on peut avoir une Bad Request avec un code 400 et un body avec un content de la reponse au
                format XML commencant par la balise <sepalia>

                ==> Le traitement des erreurs metier sur API MandateWS est donc a traiter dans le case OTHER_CODE

                Lors d'appels sur l'API SignatureWS

                - soit le WS repond correctement avec un code 200 pour une reponse OK mais aussi pour les cas d'erreur
                metier cote partenaire et dans ce cas on a un body avec un content de la reponse au format JSON qui
                contient les infos soit de la reponse OK soit de l'erreur metier

                - soit le WS repond en erreur avec un code different de 200 et dans ce cas il n'y a pas de body d'erreur
                metier

                ==> Le traitement des erreurs metier sur API SignatureWS est donc a traiter dans le case OK_200

             */

        try {

            PaymentResponse paymentResponse;

            //######################################################################################################
            //### API MandateWS /api/mandate/create

            // Initialisation de la requete Docapost
            MandateCreateRequest mandateCreateRequest = RequestBuilderFactory.buildMandateCreateRequest(paymentRequest);

            paymentResponse = mandateCreate(mandateCreateRequest, scheme, host, credentials);

            // Si la reponse a mandateCreate est une PaymentResponseFailure, on ne va pas plus loin et on l'a renvoie
            if (paymentResponse != null && paymentResponse instanceof PaymentResponseFailure) {
                return paymentResponse;
            }

            //######################################################################################################
            //### API SignatureWS /api/initiateSignature

            // Initialisation de la requete Docapost
            InitiateSignatureRequest initiateSignatureRequest = RequestBuilderFactory.buildInitiateSignatureRequest(paymentRequest, this.docapostLocalParam);

            paymentResponse = initiateSignature(initiateSignatureRequest, scheme, host, credentials);

            // Si la reponse a initiateSignature est une PaymentResponseFailure, on ne va pas plus loin et on l'a renvoie
            if (paymentResponse != null && paymentResponse instanceof PaymentResponseFailure) {
                return paymentResponse;
            }

            //######################################################################################################
            //### API SignatureWS /api/sendOTP

            // Initialisation de la requete Docapost
            SendOtpRequest sendOtpRequest = RequestBuilderFactory.buildSendOtpRequest(paymentRequest, this.docapostLocalParam);

            paymentResponse = sendOtp(sendOtpRequest, scheme, host, credentials);

            // Si la reponse a sendOTP est une PaymentResponseFailure, on ne va pas plus loin et on l'a renvoie
            if (paymentResponse != null && paymentResponse instanceof PaymentResponseFailure) {
                return paymentResponse;
            }

            //######################################################################################################
            //### BUILD de la PaymentResponse finale PaymentResponseFormUpdated
            //######################################################################################################

            /*

                On doit retourner une reponse de type PaymentResponseFormUpdated pour faire afficher un formulaire de saisie
                avec un champ OTP, des textes d'information, des liens (telechargement du mandat, renvoie du code OTP), des checkbox

             */

            /*
             * Creation d'un customForm contenant
             * - link to download mandate
             * - link to resend otp code
             * - link to CGV
             */
            CustomForm customForm = DocapostFormUtils.createOTPPaymentForm(this.docapostLocalParam, paymentRequest, sendOtpRequest);

            PaymentFormConfigurationResponse paymentFormConfigurationResponse = PaymentFormConfigurationResponseSpecific
                    .PaymentFormConfigurationResponseSpecificBuilder
                    .aPaymentFormConfigurationResponseSpecific()
                    .withPaymentForm(customForm)
                    .build();

            // Pour le step suivant, on doit envoyer :
            // - Le step IBAN_PHONE
            // - La valeur du MandateRum obtenu lors de l'appel /api/mandate/create
            // - La valeur du transactionId obtenu lors de l'appel /api/initiateSignature
            // - la valeur du signatureId obtenu lors de l'appel /api/sendOTP
            Map<String, String> requestContextMap = new HashMap<>();
            requestContextMap.put(CONTEXT_DATA_STEP, CONTEXT_DATA_STEP_OTP);
            requestContextMap.put(CONTEXT_DATA_MANDATE_RUM, this.docapostLocalParam.getMandateRum());
            requestContextMap.put(CONTEXT_DATA_TRANSACTION_ID, this.docapostLocalParam.getTransactionId());
            requestContextMap.put(CONTEXT_DATA_SIGNATURE_ID, this.docapostLocalParam.getSignatureId());
            // Add IBAN, BIC and countryCode to the request context map. We need them at the end of step 3 (see PAYLAPMEXT-123)
            requestContextMap.put(CONTEXT_DATA_BIC, this.docapostLocalParam.getMandateCreateBic());
            requestContextMap.put(CONTEXT_DATA_COUNTRY_CODE, this.docapostLocalParam.getMandateCreateCountryCode());

            Map<String, String> sensitiveRequestContextMap = new HashMap<>();
            sensitiveRequestContextMap.put(CONTEXT_DATA_IBAN, this.docapostLocalParam.getMandateCreateIban());

            RequestContext requestContext = RequestContext
                    .RequestContextBuilder
                    .aRequestContext()
                    .withRequestData(requestContextMap)
                    .withSensitiveRequestData(sensitiveRequestContextMap)
                    .build();

            return PaymentResponseFormUpdated
                    .PaymentResponseFormUpdatedBuilder
                    .aPaymentResponseFormUpdated()
                    .withPaymentFormConfigurationResponse(paymentFormConfigurationResponse)
                    .withRequestContext(requestContext)
                    .build();

        } catch (InvalidRequestException e) {
            LOGGER.error("The input payment request is invalid", e);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INVALID_DATA);
        } catch (IOException e) {
            LOGGER.error("An IOException occurred while sending the HTTP request or receiving the response", e);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.COMMUNICATION_ERROR);
        } catch (Exception e) {
            LOGGER.error(UNEXPECTED_ERROR_MESSAGE, e);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }

    }

    /**
     * Execution et traitement de l'appel MandateWS Docapost /api/mandate/create
     *
     * @param mandateCreateRequest
     * @param scheme
     * @param host
     * @param credentials
     * @return
     * @throws InvalidRequestException
     * @throws IOException
     * @throws URISyntaxException
     */
    protected PaymentResponse mandateCreate(MandateCreateRequest mandateCreateRequest,
                                          String scheme,
                                          String host,
                                          String credentials) throws IOException, URISyntaxException {

        String requestBody, responseBody, path;

        // Generation des donnees du body de la requete
        requestBody = mandateCreateRequest.buildBody();

        LOGGER.debug("MandateCreateRequest XML body : {}", requestBody);

        // Execution de l'appel WS Docapost /api/mandate/create et recuperation de l'information "mandateRum"
        path = ConfigProperties.get(CONFIG_PATH_WSMANDATE_MANDATE_CREATE);
        final StringResponse mandateCreateStringResponse = this.httpClient.doPost(
                scheme,
                host,
                path,
                requestBody,
                credentials
        );

        if (mandateCreateStringResponse == null) {

            LOGGER.debug("MandateCreateRequest StringResponse is null !");
            LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }
        LOGGER.debug("MandateCreateRequest StringResponse  {}", () -> mandateCreateStringResponse.toString());

        responseBody = mandateCreateStringResponse.getContent().trim();

        LOGGER.debug("MandateCreateResponse XML body : {}", responseBody);

        AbstractXmlResponse mandateCreateXmlResponse = getMandateCreateResponse(responseBody);

        switch (ActionRequestResponse.checkResponse(mandateCreateStringResponse)) {

            // Appel sur API MandateWS, le traitement des erreurs metier doit se faire dans le OTHER_CODE (cf. note
            // en haut de page)

            case OK_200:

                if (mandateCreateXmlResponse != null) {

                    if (mandateCreateXmlResponse.isResultOk()) {

                        LOGGER.info("MandateCreateXmlResponse AbstractXmlResponse instance of WSMandateDTOResponse");

                        WSMandateDTOResponse wsMandateDTOResponse = (WSMandateDTOResponse) mandateCreateXmlResponse;

                        LOGGER.debug("WSMandateDTOResponse : {}", () -> wsMandateDTOResponse.toString());

                        // Recuperation du parametre mandateRum
                        this.docapostLocalParam.setMandateRum(wsMandateDTOResponse.getRum());
                        // Recover IBAN, BIC and CountryCode from the response (PAYLAPMEXT-123)
                        if( wsMandateDTOResponse.getDebtor() != null ){
                            Debtor debtor = wsMandateDTOResponse.getDebtor();
                            this.docapostLocalParam.setMandateCreateBic( debtor.getBic() );
                            this.docapostLocalParam.setMandateCreateCountryCode( debtor.getCountryCode() );
                            this.docapostLocalParam.setMandateCreateIban( debtor.getIban() );
                        }
                    }

                } else {

                    return buildPaymentResponseFailure("XML RESPONSE PARSING FAILED", FailureCause.INVALID_DATA);

                }

                break;

            case OTHER_CODE:

                LOGGER.error(HTTP_SENDING_ERROR_MESSAGE + mandateCreateStringResponse.getMessage());

                if (responseBody == null || responseBody.isEmpty()) {

                    return buildPaymentResponseFailure(Integer.toString(mandateCreateStringResponse.getCode()), FailureCause.COMMUNICATION_ERROR);

                }
                else if (mandateCreateXmlResponse != null && !mandateCreateXmlResponse.isResultOk() ){
                    LOGGER.info("MandateCreateXmlResponse AbstractXmlResponse instance of XmlErrorResponse");

                    XmlErrorResponse xmlErrorResponse = (XmlErrorResponse) mandateCreateXmlResponse;

                    LOGGER.debug("MandateCreateXmlResponse error : {}", () -> xmlErrorResponse.toString());

                    // Retrieve the partner error type
                    WSRequestResultEnum wsRequestResult = WSRequestResultEnum.fromDocapostErrorCode(xmlErrorResponse.getException().getCode());

                    return buildPaymentResponseFailure(wsRequestResult);
                }
                else {
                    LOGGER.error("An unknown error was raised by the partner");
                    return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.PARTNER_UNKNOWN_ERROR);
                }

            default:
                LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);
                return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }

        return null;

    }

    /**
     * Execution et traitement de l'appel SignatureWS /api/initiateSignature
     *
     * @param initiateSignatureRequest
     * @param scheme
     * @param host
     * @param credentials
     * @return
     * @throws InvalidRequestException
     * @throws IOException
     * @throws URISyntaxException
     */
    protected PaymentResponse initiateSignature(InitiateSignatureRequest initiateSignatureRequest,
                                              String scheme,
                                              String host,
                                              String credentials) throws IOException, URISyntaxException {

        String responseBody, path;

        // Execution de l'appel WS Docapost /api/initiateSignature et recuperation de l'information "transactionId"
        path = ConfigProperties.get(CONFIG_PATH_WSSIGNATURE_INITIATE_SIGNATURE);
        final StringResponse initiateSignatureStringResponse = this.httpClient.doPost(
                scheme,
                host,
                path,
                initiateSignatureRequest.getRequestBodyMap(),
                credentials
        );

        if (initiateSignatureStringResponse == null) {

            LOGGER.debug("InitiateSignatureRequest StringResponse is null !");
            LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }
        LOGGER.debug("InitiateSignatureRequest StringResponse : {}", () -> initiateSignatureStringResponse.toString());

        switch (ActionRequestResponse.checkResponse(initiateSignatureStringResponse)) {

            // Appel sur API SignatureWS, le traitement des erreurs metier doit se faire dans le OK_200 (cf. note
            // en haut de page)

            case OK_200:

                responseBody = initiateSignatureStringResponse.getContent().trim();

                LOGGER.debug("InitiateSignatureResponse JSON body : {}", responseBody);

                InitiateSignatureResponse initiateSignatureResponse = ResponseBuilderFactory.buildInitiateSignatureResponse(responseBody);

                if (initiateSignatureResponse.isResultOk()) {

                    LOGGER.debug("InitiateSignatureResponse : {}", () -> initiateSignatureResponse.toString());

                    // Recuperation du parametre transactionId
                    this.docapostLocalParam.setTransactionId(initiateSignatureResponse.getTransactionId());

                } else {

                    LOGGER.debug("InitiateSignatureResponse error : {}", () -> initiateSignatureResponse.getErrors().get(0));

                    return buildPaymentResponseFailure(WSRequestResultEnum.PARTNER_UNKNOWN_ERROR);

                }

                break;

            case OTHER_CODE:

                LOGGER.error(HTTP_SENDING_ERROR_MESSAGE + initiateSignatureStringResponse.getMessage());

                return buildPaymentResponseFailure(Integer.toString(initiateSignatureStringResponse.getCode()), FailureCause.COMMUNICATION_ERROR);

            default:

                LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);

                return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);

        }

        return null;

    }

    /**
     * Execution et traitement de l'appel SignatureWS /api/sendOTP
     *
     * @param sendOtpRequest
     * @param scheme
     * @param host
     * @param credentials
     * @return
     * @throws InvalidRequestException
     * @throws IOException
     * @throws URISyntaxException
     */
    protected PaymentResponse sendOtp(SendOtpRequest sendOtpRequest,
                                    String scheme,
                                    String host,
                                    String credentials) throws IOException, URISyntaxException {

        String responseBody, path;

        // Execution de l'appel WS Docapost /api/sendOTP et recuperation de l'information "signatureId"
        path = ConfigProperties.get(CONFIG_PATH_WSSIGNATURE_SEND_OTP);
        final StringResponse sendOTPStringResponse = this.httpClient.doPost(
                scheme,
                host,
                path,
                sendOtpRequest.getRequestBodyMap(),
                credentials
        );

        if (sendOTPStringResponse == null) {

            LOGGER.debug("SendOTPRequest StringResponse is null !");
            LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }
        LOGGER.debug("SendOTPRequest StringResponse : {}", () -> sendOTPStringResponse.toString());

        switch (ActionRequestResponse.checkResponse(sendOTPStringResponse)) {

            // Appel sur API SignatureWS, le traitement des erreurs metier doit se faire dans le OK_200 (cf. note
            // en haut de page)

            case OK_200:

                responseBody = sendOTPStringResponse.getContent().trim();

                LOGGER.debug("SendOTPResponse JSON body : {}", responseBody);

                SendOtpResponse sendOtpResponse = ResponseBuilderFactory.buildSendOtpResponse(responseBody);

                if (sendOtpResponse.isResultOk()) {

                    LOGGER.debug("SendOTPResponse : {}", () -> sendOtpResponse.toString());

                    // Recuperation du parametre transactionId
                    this.docapostLocalParam.setSignatureId(sendOtpResponse.getSignatureId());

                } else {

                    LOGGER.debug("SendOTPResponse error : {}", () -> sendOtpResponse.getErrors().get(0));

                    return buildPaymentResponseFailure(WSRequestResultEnum.PARTNER_UNKNOWN_ERROR);

                }

                break;

            case OTHER_CODE:

                LOGGER.error(HTTP_SENDING_ERROR_MESSAGE + sendOTPStringResponse.getMessage());

                return buildPaymentResponseFailure(Integer.toString(sendOTPStringResponse.getCode()), FailureCause.COMMUNICATION_ERROR);

            default:

                LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);

                return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }

        return null;

    }

    /**
     * Return a AbstractXmlResponse (WSDDOrderDTOResponse or XmlErrorResponse in case of error) based on a XML content
     *
     * @param xmlResponse the XML content
     * @return the AbstractXmlResponse
     */
    protected AbstractXmlResponse getMandateCreateResponse(String xmlResponse) {

        XmlErrorResponse xmlErrorResponse;
        WSMandateDTOResponse mandateCreateResponse;

        if (xmlResponse.contains(MANDATE_WS_XML_SEPALIA_ERROR)) {

            xmlErrorResponse = ResponseBuilderFactory.buildXmlErrorResponse(xmlResponse);

            if (xmlErrorResponse != null) {
                return xmlErrorResponse;
            }

        }

        if (xmlResponse.contains(MANDATE_WS_XML_WS_MANDATE_DTO)) {

            mandateCreateResponse = ResponseBuilderFactory.buildWsMandateDTOResponse(xmlResponse);

            if (mandateCreateResponse != null) {
                return mandateCreateResponse;
            }

        }

        return null;

    }

}
