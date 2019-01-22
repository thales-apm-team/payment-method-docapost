package com.payline.payment.docapost.service.impl;

import com.payline.payment.docapost.bean.PaymentResponseSuccessAdditionalData;
import com.payline.payment.docapost.bean.rest.request.RequestBuilderFactory;
import com.payline.payment.docapost.bean.rest.request.mandate.SddOrderCreateRequest;
import com.payline.payment.docapost.bean.rest.request.signature.SetCodeRequest;
import com.payline.payment.docapost.bean.rest.request.signature.TerminateSignatureRequest;
import com.payline.payment.docapost.bean.rest.response.ResponseBuilderFactory;
import com.payline.payment.docapost.bean.rest.response.error.XmlErrorResponse;
import com.payline.payment.docapost.bean.rest.response.mandate.AbstractXmlResponse;
import com.payline.payment.docapost.bean.rest.response.mandate.WSDDOrderDTOResponse;
import com.payline.payment.docapost.bean.rest.response.signature.SetCodeResponse;
import com.payline.payment.docapost.bean.rest.response.signature.TerminateSignatureResponse;
import com.payline.payment.docapost.exception.InvalidRequestException;
import com.payline.payment.docapost.service.PaymentServiceStep;
import com.payline.payment.docapost.utils.ActionRequestResponse;
import com.payline.payment.docapost.utils.DocapostLocalParam;
import com.payline.payment.docapost.utils.DocapostUtils;
import com.payline.payment.docapost.utils.config.ConfigEnvironment;
import com.payline.payment.docapost.utils.config.ConfigProperties;
import com.payline.payment.docapost.utils.http.DocapostHttpClient;
import com.payline.payment.docapost.utils.http.StringResponse;
import com.payline.payment.docapost.utils.i18n.I18nService;
import com.payline.payment.docapost.utils.type.WSRequestResultEnum;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.Message;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

import static com.payline.payment.docapost.utils.DocapostConstants.*;
import static com.payline.pmapi.bean.common.Message.MessageType.SUCCESS;

public class PaymentServiceStep03 implements PaymentServiceStep {

    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceStep03.class);

    private final DocapostHttpClient httpClient;

    private DocapostLocalParam docapostLocalParam;

    private I18nService i18n = I18nService.getInstance();

    public PaymentServiceStep03(DocapostHttpClient httpClient) {
        this.httpClient = httpClient;
        this.docapostLocalParam = DocapostLocalParam.getInstance();
    }

    @Override
    public PaymentResponse processPaymentStep(PaymentRequest paymentRequest,
                                              ConfigEnvironment env,
                                              DocapostLocalParam localParam,
                                              String credentials) {

        this.docapostLocalParam = localParam;
        this.docapostLocalParam.setSignatureSuccess(false);

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
            //### API SignatureWS /api/setCode

            // Initialisation de la requete Docapost
            SetCodeRequest setCodeRequest = RequestBuilderFactory.buildSetCodeRequest(paymentRequest);

            paymentResponse = setCode(setCodeRequest, scheme, host, credentials);

            // Si la reponse a setCode est une PaymentResponseFailure, on ne va pas plus loin et on l'a renvoie
            if (paymentResponse != null && paymentResponse instanceof PaymentResponseFailure) {
                return paymentResponse;
            }

            //######################################################################################################
            //### API SignatureWS /api/terminateSignature

            // Initialisation de la requete Docapost
            TerminateSignatureRequest terminateSignatureRequest = RequestBuilderFactory.buildTerminateSignatureRequest(paymentRequest, docapostLocalParam);

            paymentResponse = terminateSignature(terminateSignatureRequest, scheme, host, credentials);

            // Si la reponse a terminatreSignature est une PaymentResponseFailure, on ne va pas plus loin et on l'a renvoie
            if (paymentResponse != null && paymentResponse instanceof PaymentResponseFailure) {
                return paymentResponse;
            }

            //######################################################################################################
            //### API MandateWS /api/order/create

            // Initialisation de la requete Docapost
            SddOrderCreateRequest orderCreateRequest = RequestBuilderFactory.buildSddOrderCreateRequest(paymentRequest);

            paymentResponse = orderCreate(orderCreateRequest, scheme, host, credentials);

            // Si la reponse a orderCreate est une PaymentResponseFailure, on ne va pas plus loin et on l'a renvoie
            if (paymentResponse != null && paymentResponse instanceof PaymentResponseFailure) {
                return paymentResponse;
            }

            //######################################################################################################
            //### BUILD de la PaymentResponse finale PaymentResponseSuccess
            //######################################################################################################

            /*

                On doit retourner une reponse de type PaymentResponseSuccess et retourner toutes les infos (mandateRum, transactionId, signatureId) au niveau des additionalData

             */

            PaymentResponseSuccessAdditionalData paymentResponseSuccessAdditionalData = DocapostUtils.buildPaymentResponseSuccessAdditionalData(docapostLocalParam);

            Locale locale = paymentRequest.getLocale();

            return PaymentResponseSuccess
                    .PaymentResponseSuccessBuilder
                    .aPaymentResponseSuccess()
                    .withPartnerTransactionId(paymentRequest.getTransactionId())
                    .withTransactionAdditionalData(paymentResponseSuccessAdditionalData.toJson())
                    .withStatusCode(docapostLocalParam.getOrderStatus())
                    .withMessage(new Message(SUCCESS, this.i18n.getMessage(PAYMENT_RESPONSE_SUCCESS_MESSAGE, locale)))
                    .withTransactionDetails(new EmptyTransactionDetails())
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
     * Execution et traitement de l'appel SignatureWS /api/setCode
     *
     * @param setCodeRequest
     * @param scheme
     * @param host
     * @param credentials
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    protected PaymentResponse setCode(SetCodeRequest setCodeRequest,
                                      String scheme,
                                      String host,
                                      String credentials) throws IOException, URISyntaxException {

        String responseBody, path;

        // Execution de l'appel WS Docapost /api/setCode
        path = ConfigProperties.get(CONFIG_PATH_WSSIGNATURE_SET_CODE);
        final StringResponse setCodeStringResponse = this.httpClient.doPost(
                scheme,
                host,
                path,
                setCodeRequest.getRequestBodyMap(),
                credentials
        );

        if (setCodeStringResponse == null) {
            LOGGER.debug("SetCodeRequest StringResponse is null !");
            LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }
        LOGGER.debug("SetCodeRequest StringResponse : {}", setCodeStringResponse.toString());

        switch (ActionRequestResponse.checkResponse(setCodeStringResponse)) {

            // Appel sur API SignatureWS, le traitement des erreurs metier doit se faire dans le OK_200 (cf. note
            // en haut de page)

            case OK_200:

                responseBody = setCodeStringResponse.getContent().trim();

                LOGGER.debug("SetCodeResponse JSON body : {}", responseBody);

                SetCodeResponse setCodeResponse = ResponseBuilderFactory.buildSetCodeResponse(responseBody);

                if (setCodeResponse.isResultOk()) {

                    LOGGER.debug("SetCodeResponse : {}", setCodeResponse.toString());

                    // Update du parametre success
                    this.docapostLocalParam.setSignatureSuccess(true);

                } else {

                    LOGGER.debug("SetCodeResponse error : {}", setCodeResponse.getErrors().get(0));

                    return buildPaymentResponseFailure(WSRequestResultEnum.PARTNER_UNKNOWN_ERROR);

                }

                break;

            case OTHER_CODE:

                LOGGER.error(HTTP_SENDING_ERROR_MESSAGE + setCodeStringResponse.getMessage());

                return buildPaymentResponseFailure(Integer.toString(setCodeStringResponse.getCode()), FailureCause.COMMUNICATION_ERROR);

            default:

                LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);

                return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);

        }

        return null;

    }

    /**
     * Execution et traitement de l'appel SignatureWS /api/terminateSignature
     *
     * @param terminateSignatureRequest
     * @param scheme
     * @param host
     * @param credentials
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    protected PaymentResponse terminateSignature(TerminateSignatureRequest terminateSignatureRequest,
                                               String scheme,
                                               String host,
                                               String credentials) throws IOException, URISyntaxException {

        String responseBody, path;

        // Execution de l'appel WS Docapost /api/terminateSignature
        path = ConfigProperties.get(CONFIG_PATH_WSSIGNATURE_TERMINATE_SIGNATURE);
        final StringResponse terminateSignatureStringResponse = this.httpClient.doPost(
                scheme,
                host,
                path,
                terminateSignatureRequest.getRequestBodyMap(),
                credentials
        );

        if (terminateSignatureStringResponse == null) {

            LOGGER.debug("TerminateSignatureRequest StringResponse is null !");
            LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }
        LOGGER.debug("TerminateSignatureRequest StringResponse : {}", terminateSignatureStringResponse.toString());

        switch (ActionRequestResponse.checkResponse(terminateSignatureStringResponse)) {

            // Appel sur API SignatureWS, le traitement des erreurs metier doit se faire dans le OK_200 (cf. note
            // en haut de page)

            case OK_200:

                responseBody = terminateSignatureStringResponse.getContent().trim();

                LOGGER.debug("TerminateSignatureResponse JSON body : {}", responseBody);

                TerminateSignatureResponse terminateSignatureResponse = ResponseBuilderFactory.buildTerminateSignatureResponse(responseBody);

                if (terminateSignatureResponse.isResultOk()) {

                    LOGGER.debug("TerminateSignatureResponse : {}", terminateSignatureResponse.toString());

                    // Nothing to do

                } else {

                    LOGGER.debug("TerminateSignatureResponse error : {}", terminateSignatureResponse.getErrors().get(0));

                    return buildPaymentResponseFailure(WSRequestResultEnum.PARTNER_UNKNOWN_ERROR);

                }

                break;

            case OTHER_CODE:

                LOGGER.error(HTTP_SENDING_ERROR_MESSAGE + terminateSignatureStringResponse.getMessage());

                return buildPaymentResponseFailure(Integer.toString(terminateSignatureStringResponse.getCode()), FailureCause.COMMUNICATION_ERROR);

            default:

                LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);

                return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);

        }

        return null;

    }

    /**
     * Execution et traitement de l'appel MandateWS /api/order/create
     *
     * @param orderCreateRequest
     * @param scheme
     * @param host
     * @param credentials
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    protected PaymentResponse orderCreate(SddOrderCreateRequest orderCreateRequest,
                                        String scheme,
                                        String host,
                                        String credentials) throws IOException, URISyntaxException {

        String requestBody, responseBody, path;

        // Generation des donnees du body de la requete
        requestBody = orderCreateRequest.buildBody();

        LOGGER.debug("SddOrderCreateRequest XML body : {}", requestBody);

        // Execution de l'appel WS Docapost /api/order/create
        path = ConfigProperties.get(CONFIG_PATH_WSMANDATE_ORDER_CREATE);
        final StringResponse orderCreateStringResponse = this.httpClient.doPost(
                scheme,
                host,
                path,
                requestBody,
                credentials
        );

        if (orderCreateStringResponse == null) {

            LOGGER.debug("SddOrderCreateRequest StringResponse is null !");
            LOGGER.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }
        LOGGER.debug("SddOrderCreateRequest StringResponse : {}", orderCreateStringResponse.toString());

        responseBody = orderCreateStringResponse.getContent().trim();

        LOGGER.debug("SddOrderCreateResponse XML body : {}", responseBody);

        AbstractXmlResponse orderCreateXmlResponse = getOrderCreateResponse(responseBody);

        switch (ActionRequestResponse.checkResponse(orderCreateStringResponse)) {

            // Appel sur API MandateWS, le traitement des erreurs metier doit se faire dans le OTHER_CODE (cf. note
            // en haut de page)

            case OK_200:

                if (orderCreateXmlResponse != null) {

                    if (orderCreateXmlResponse.isResultOk()) {

                        LOGGER.info("SddOrderCreateCreateXmlResponse AbstractXmlResponse instance of WSDDOrderDTOResponse");

                        WSDDOrderDTOResponse orderCreateResponse = (WSDDOrderDTOResponse) orderCreateXmlResponse;

                        LOGGER.debug("SddOrderCreateRequest : {}", orderCreateResponse.toString());

                        // Nothing to do
                        docapostLocalParam.setOrderStatus(orderCreateResponse.getStatus());

                    }

                } else {
                    return buildPaymentResponseFailure("XML RESPONSE PARSING FAILED", FailureCause.INVALID_DATA);
                }
                break;

            case OTHER_CODE:

                LOGGER.error(HTTP_SENDING_ERROR_MESSAGE + orderCreateStringResponse.getMessage());

                if (orderCreateStringResponse.getContent() == null || orderCreateStringResponse.getContent().isEmpty()) {

                    return buildPaymentResponseFailure(Integer.toString(orderCreateStringResponse.getCode()), FailureCause.COMMUNICATION_ERROR);

                } else {

                    if (orderCreateXmlResponse != null) {

                        if (!orderCreateXmlResponse.isResultOk()) {

                            LOGGER.info("SddOrderCreateCreateXmlResponse AbstractXmlResponse instance of XmlErrorResponse");

                            XmlErrorResponse xmlErrorResponse = (XmlErrorResponse) orderCreateXmlResponse;

                            LOGGER.debug("SddOrderCreateCreateXmlResponse error : {}", xmlErrorResponse.toString());

                            WSRequestResultEnum wsRequestResult = WSRequestResultEnum.fromDocapostErrorCode(xmlErrorResponse.getException().getCode());

                            return buildPaymentResponseFailure(wsRequestResult);

                        }

                    }

                }

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
    protected static AbstractXmlResponse getOrderCreateResponse(String xmlResponse) {

        XmlErrorResponse xmlErrorResponse;
        WSDDOrderDTOResponse orderCreateResponse;

        if (xmlResponse.contains(MANDATE_WS_XML_SEPALIA_ERROR)) {

            xmlErrorResponse = ResponseBuilderFactory.buildXmlErrorResponse(xmlResponse);

            if (xmlErrorResponse != null) {
                return xmlErrorResponse;
            }

        }

        if (xmlResponse.contains(MANDATE_WS_XML_WS_SDD_ORDER_DTO)) {

            orderCreateResponse = ResponseBuilderFactory.buildWsddOrderDTOResponse(xmlResponse);

            if (orderCreateResponse != null) {
                return orderCreateResponse;
            }

        }

        return null;

    }

}
