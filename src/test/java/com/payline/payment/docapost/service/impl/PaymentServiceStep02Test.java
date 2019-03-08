package com.payline.payment.docapost.service.impl;

import com.payline.payment.docapost.bean.rest.request.RequestBuilderFactory;
import com.payline.payment.docapost.bean.rest.request.mandate.MandateCreateRequest;
import com.payline.payment.docapost.bean.rest.request.signature.InitiateSignatureRequest;
import com.payline.payment.docapost.bean.rest.request.signature.SendOtpRequest;
import com.payline.payment.docapost.bean.rest.response.error.XmlErrorResponse;
import com.payline.payment.docapost.bean.rest.response.mandate.AbstractXmlResponse;
import com.payline.payment.docapost.bean.rest.response.mandate.WSMandateDTOResponse;
import com.payline.payment.docapost.exception.InvalidRequestException;
import com.payline.payment.docapost.utils.DocapostLocalParam;
import com.payline.payment.docapost.utils.config.ConfigEnvironment;
import com.payline.payment.docapost.utils.http.DocapostHttpClient;
import com.payline.payment.docapost.utils.http.StringResponse;
import com.payline.payment.docapost.utils.i18n.I18nService;
import com.payline.payment.docapost.utils.type.WSRequestResultEnum;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormField;
import com.payline.pmapi.bean.paymentform.bean.form.AbstractPaymentForm;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.payline.payment.docapost.TestUtils.createCompletePaymentRequest;
import static com.payline.payment.docapost.TestUtils.createDefaultPaymentRequestStep2;
import static com.payline.payment.docapost.utils.DocapostConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceStep02Test {

    @InjectMocks
    @Spy
    private PaymentServiceStep02 paymentServiceStep02;

    @Mock
    DocapostHttpClient httpClient;

    private PaymentRequest paymentRequest;
    private ConfigEnvironment env;
    private DocapostLocalParam docapostLocalParam;
    private String credentials;

    @Before
    public void setup() {

        env = ConfigEnvironment.DEV;

        paymentRequest = createDefaultPaymentRequestStep2("0606060606");

        docapostLocalParam = DocapostLocalParam.getInstance();

        credentials = "Basic cGF5bGluZUBkb2NhcG9zdC5mcjpKOltlZjhkY2NtYQ==";

    }

    @Test
    public void testGetMandateCreateResponse_withXmlResponseOk() {

        // XML response message with data
        String response =
                "<WSMandateDTO>\n" +
                "   <creditorId>creditorId</creditorId>\n" +
                "   <creditorIcs>creditorIcs</creditorIcs>\n" +
                "   <rum>rum</rum>\n" +
                "   <recurrent>false</recurrent>\n" +
                "   <status>status</status>\n" +
                "   <debtor>\n" +
                "      <lastName>lastName</lastName>\n" +
                "      <bic>bic</bic>\n" +
                "      <iban>iban</iban>\n" +
                "      <firstName>firstName</firstName>\n" +
                "      <street>street</street>\n" +
                "      <postalCode>postalCode</postalCode>\n" +
                "      <town>town</town>\n" +
                "      <phoneNumber>phoneNumber</phoneNumber>\n" +
                "      <countryCode>countryCode</countryCode>\n" +
                "      <complement>complement</complement>\n" +
                "      <complement2>complement2</complement2>\n" +
                "   </debtor>\n" +
                "   <mode>mode</mode>\n" +
                "   <flowName>flowName</flowName>\n" +
                "   <language>language</language>\n" +
                "   <contextIdentifier>contextIdentifier</contextIdentifier>\n" +
                "</WSMandateDTO>";

        AbstractXmlResponse abstractXmlResponse = paymentServiceStep02.getMandateCreateResponse(response);

        Assert.assertNotNull(abstractXmlResponse);
        Assert.assertTrue(abstractXmlResponse instanceof WSMandateDTOResponse);

        WSMandateDTOResponse wsMandateDTOResponse = (WSMandateDTOResponse) abstractXmlResponse;

        Assert.assertEquals("creditorId", wsMandateDTOResponse.getCreditorId());
        Assert.assertEquals("creditorIcs", wsMandateDTOResponse.getCreditorIcs());
        Assert.assertEquals("rum", wsMandateDTOResponse.getRum());
        Assert.assertEquals(new Boolean(false), wsMandateDTOResponse.getRecurrent());
        Assert.assertEquals("status", wsMandateDTOResponse.getStatus());
        Assert.assertEquals("mode", wsMandateDTOResponse.getMode());
        Assert.assertEquals("flowName", wsMandateDTOResponse.getFlowName());
        Assert.assertEquals("language", wsMandateDTOResponse.getLanguage());
        Assert.assertEquals("contextIdentifier", wsMandateDTOResponse.getContextIdentifier());

        Assert.assertNotNull(wsMandateDTOResponse.getDebtor());
        Assert.assertEquals("lastName", wsMandateDTOResponse.getDebtor().getLastName());
        Assert.assertEquals("firstName", wsMandateDTOResponse.getDebtor().getFirstName());
        Assert.assertEquals("bic", wsMandateDTOResponse.getDebtor().getBic());
        Assert.assertEquals("iban", wsMandateDTOResponse.getDebtor().getIban());
        Assert.assertEquals("street", wsMandateDTOResponse.getDebtor().getStreet());
        Assert.assertEquals("complement", wsMandateDTOResponse.getDebtor().getComplement());
        Assert.assertEquals("complement2", wsMandateDTOResponse.getDebtor().getComplement2());
        Assert.assertEquals("postalCode", wsMandateDTOResponse.getDebtor().getPostalCode());
        Assert.assertEquals("town", wsMandateDTOResponse.getDebtor().getTown());
        Assert.assertEquals("phoneNumber", wsMandateDTOResponse.getDebtor().getPhoneNumber());
        Assert.assertEquals("countryCode", wsMandateDTOResponse.getDebtor().getCountryCode());

    }

    @Test
    public void testGetMandateCreateResponse_withXmlResponseKo() {

        // XML response message with error
        String response =
                "<sepalia>\n" +
                "   <exception code=\"CREDITOR_NOT_FOUND\">CREDITOR_NOT_FOUND: PAYLINE</exception>\n" +
                "</sepalia>";

        AbstractXmlResponse abstractXmlResponse = paymentServiceStep02.getMandateCreateResponse(response);

        Assert.assertNotNull(abstractXmlResponse);
        Assert.assertTrue(abstractXmlResponse instanceof XmlErrorResponse);

        XmlErrorResponse xmlErrorResponse = (XmlErrorResponse) abstractXmlResponse;

        Assert.assertNotNull(xmlErrorResponse.getException());
        Assert.assertEquals("CREDITOR_NOT_FOUND", xmlErrorResponse.getException().getCode());
        Assert.assertEquals("CREDITOR_NOT_FOUND: PAYLINE", xmlErrorResponse.getException().getValue());

    }

    @Test
    public void testGetMandateCreateResponse_withWrongXmlResponseFormat() {

        // XML response message with wrong data tag
        String response = "<wrongtag></wrongtag>";

        AbstractXmlResponse abstractXmlResponse = paymentServiceStep02.getMandateCreateResponse(response);

        Assert.assertNull(abstractXmlResponse);

    }

    @Test
    public void testGetMandateCreateResponse_withEmptyXmlResponse() {

        // XML response message with empty data
        String response = "";

        AbstractXmlResponse abstractXmlResponse = paymentServiceStep02.getMandateCreateResponse(response);

        Assert.assertNull(abstractXmlResponse);

    }

    @Test
    public void testProcessPayment_KoOnMandateCreate() throws IOException, URISyntaxException {

        PaymentResponseFailure expectedMandateCreatePaymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(FailureCause.INVALID_DATA)
                .withErrorCode("ERROR CODE")
                .build();

        Mockito.doReturn(expectedMandateCreatePaymentResponse).when(paymentServiceStep02).mandateCreate(any(MandateCreateRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        paymentRequest = createCompletePaymentRequest();

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep02.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        // On a MandateCreate call with error, the method must return and not continue
        Mockito.verify(paymentServiceStep02, Mockito.times(1)).mandateCreate(any(MandateCreateRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep02, Mockito.times(0)).initiateSignature(any(InitiateSignatureRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep02, Mockito.times(0)).sendOtp(any(SendOtpRequest.class), anyString(), anyString(), anyString());

        // In the error response, we must retrieve the data from expectedPaymentResponseFailure returned from MandateCreate
        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(expectedMandateCreatePaymentResponse.getFailureCause(), paymentResponseFailure.getFailureCause());
        Assert.assertEquals(expectedMandateCreatePaymentResponse.getErrorCode(), paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_KoOnInitiateSignature() throws IOException, URISyntaxException {

        PaymentResponse expectedMandateCreatePaymentResponse = null;

        Mockito.doReturn(expectedMandateCreatePaymentResponse).when(paymentServiceStep02).mandateCreate(any(MandateCreateRequest.class), anyString(), anyString(), anyString());

        // A mandateRum is set during MandateCreate WS call
        docapostLocalParam.setMandateRum("expMandateRum");

        PaymentResponseFailure expectedInitiateSignaturePaymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(FailureCause.INVALID_DATA)
                .withErrorCode("ERROR CODE")
                .build();

        Mockito.doReturn(expectedInitiateSignaturePaymentResponse).when(paymentServiceStep02).initiateSignature(any(InitiateSignatureRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        paymentRequest = createCompletePaymentRequest();

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep02.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        // On a MandateCreate call with error, the method must return and not continue
        Mockito.verify(paymentServiceStep02, Mockito.times(1)).mandateCreate(any(MandateCreateRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep02, Mockito.times(1)).initiateSignature(any(InitiateSignatureRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep02, Mockito.times(0)).sendOtp(any(SendOtpRequest.class), anyString(), anyString(), anyString());

        // In the error response, we must retrieve the data from expectedPaymentResponseFailure returned from MandateCreate
        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(expectedInitiateSignaturePaymentResponse.getFailureCause(), paymentResponseFailure.getFailureCause());
        Assert.assertEquals(expectedInitiateSignaturePaymentResponse.getErrorCode(), paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_KoOnSendOtp() throws IOException, URISyntaxException {

        PaymentResponse expectedMandateCreatePaymentResponse = null;

        Mockito.doReturn(expectedMandateCreatePaymentResponse).when(paymentServiceStep02).mandateCreate(any(MandateCreateRequest.class), anyString(), anyString(), anyString());

        // A mandateRum is set during MandateCreate WS call
        docapostLocalParam.setMandateRum("expMandateRum");

        PaymentResponse expectedInitiateSignaturePaymentResponse = null;

        Mockito.doReturn(expectedInitiateSignaturePaymentResponse).when(paymentServiceStep02).initiateSignature(any(InitiateSignatureRequest.class), anyString(), anyString(), anyString());

        // A transactionId is set during InitiateSignature WS call
        docapostLocalParam.setTransactionId("expTransactionId");

        PaymentResponseFailure expectedSendOtpPaymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(FailureCause.INVALID_DATA)
                .withErrorCode("ERROR CODE")
                .build();

        Mockito.doReturn(expectedSendOtpPaymentResponse).when(paymentServiceStep02).sendOtp(any(SendOtpRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        paymentRequest = createCompletePaymentRequest();

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep02.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        // On a MandateCreate call with error, the method must return and not continue
        Mockito.verify(paymentServiceStep02, Mockito.times(1)).mandateCreate(any(MandateCreateRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep02, Mockito.times(1)).initiateSignature(any(InitiateSignatureRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep02, Mockito.times(1)).sendOtp(any(SendOtpRequest.class), anyString(), anyString(), anyString());

        // In the error response, we must retrieve the data from expectedPaymentResponseFailure returned from MandateCreate
        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(expectedSendOtpPaymentResponse.getFailureCause(), paymentResponseFailure.getFailureCause());
        Assert.assertEquals(expectedSendOtpPaymentResponse.getErrorCode(), paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_KoOnIOException() throws IOException, URISyntaxException {

        Mockito.doThrow(IOException.class).when(paymentServiceStep02).mandateCreate(any(MandateCreateRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        paymentRequest = createCompletePaymentRequest();

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep02.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(FailureCause.COMMUNICATION_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("no code transmitted", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_KoOnException() throws IOException, URISyntaxException {

        Mockito.doThrow(URISyntaxException.class).when(paymentServiceStep02).mandateCreate(any(MandateCreateRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        paymentRequest = createCompletePaymentRequest();

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep02.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("no code transmitted", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_Ok() throws IOException, URISyntaxException {

        PaymentResponse expectedMandateCreatePaymentResponse = null;

        Mockito.doReturn(expectedMandateCreatePaymentResponse).when(paymentServiceStep02).mandateCreate(any(MandateCreateRequest.class), anyString(), anyString(), anyString());

        // A mandateRum is set during MandateCreate WS call
        docapostLocalParam.setMandateRum("expMandateRum");

        PaymentResponse expectedInitiateSignaturePaymentResponse = null;

        Mockito.doReturn(expectedInitiateSignaturePaymentResponse).when(paymentServiceStep02).initiateSignature(any(InitiateSignatureRequest.class), anyString(), anyString(), anyString());

        // A transactionId is set during InitiateSignature WS call
        docapostLocalParam.setTransactionId("expTransactionId");

        PaymentResponseFailure expectedSendOtpPaymentResponse = null;

        Mockito.doReturn(expectedSendOtpPaymentResponse).when(paymentServiceStep02).sendOtp(any(SendOtpRequest.class), anyString(), anyString(), anyString());

        // A signatureId is set during SendOtp WS call
        docapostLocalParam.setSignatureId("expSignatureId");

        // Initialize the payment request
        paymentRequest = createCompletePaymentRequest();

        Locale locale = paymentRequest.getLocale();
        I18nService i18n = I18nService.getInstance();

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep02.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFormUpdated);

        PaymentResponseFormUpdated paymentResponseFormUpdated = (PaymentResponseFormUpdated) paymentResponse;

        // PaymentResponseFormUpdated.requestContext.requestData must be not null, not empty and must contains 4 data :
        // - the 3 data from the docapostLocalParam (mandateRum, transactionId and signatureId)
        // - the next PaymentService step (OTP)
        Map<String, String> requestData = paymentResponseFormUpdated.getRequestContext().getRequestData();

        Assert.assertNotNull(requestData);
        Assert.assertTrue(!requestData.isEmpty() && requestData.size() == 4);
        Assert.assertEquals("expMandateRum", requestData.get(CONTEXT_DATA_MANDATE_RUM));
        Assert.assertEquals("expTransactionId", requestData.get(CONTEXT_DATA_TRANSACTION_ID));
        Assert.assertEquals("expSignatureId", requestData.get(CONTEXT_DATA_SIGNATURE_ID));
        Assert.assertEquals(CONTEXT_DATA_STEP_OTP, requestData.get(CONTEXT_DATA_STEP));

        // PaymentResponseFormUpdated.requestContext.sensitiveRequestData must be not null and empty
        Map<String, String> sensitiveRequestData = paymentResponseFormUpdated.getRequestContext().getSensitiveRequestData();

        Assert.assertNotNull(sensitiveRequestData);
        Assert.assertTrue(sensitiveRequestData.isEmpty());

        PaymentFormConfigurationResponse paymentFormConfigurationResponse = paymentResponseFormUpdated.getPaymentFormConfigurationResponse();

        Assert.assertNotNull(paymentFormConfigurationResponse);
        Assert.assertTrue(paymentFormConfigurationResponse instanceof PaymentFormConfigurationResponseSpecific);

        PaymentFormConfigurationResponseSpecific paymentFormConfigurationResponseSpecific = (PaymentFormConfigurationResponseSpecific) paymentFormConfigurationResponse;

        AbstractPaymentForm paymentForm = paymentFormConfigurationResponseSpecific.getPaymentForm();

        Assert.assertNotNull(paymentForm);
        Assert.assertTrue(paymentForm instanceof CustomForm);

        CustomForm customForm = (CustomForm) paymentForm;

        Assert.assertTrue(paymentForm.isDisplayButton());
        Assert.assertEquals(i18n.getMessage(CUSTOMFORM_TEXT_SIGN_DESCRIPTION, locale), paymentForm.getDescription());
        Assert.assertEquals(i18n.getMessage(CUSTOMFORM_TEXT_SIGN, locale), paymentForm.getButtonText());

        List<PaymentFormField> customFields = customForm.getCustomFields();

        Assert.assertNotNull(customFields);
        Assert.assertTrue(!customFields.isEmpty() && customFields.size() == 4);

    }

    @Test
    public void testMandateCreate_Ok() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse mandateCreateStringResponse = new StringResponse();
        mandateCreateStringResponse.setCode(200);
        mandateCreateStringResponse.setMessage("OK");
        mandateCreateStringResponse.setContent(
                "<WSMandateDTO>\n" +
                "   <creditorId>creditorId</creditorId>\n" +
                "   <creditorIcs>creditorIcs</creditorIcs>\n" +
                "   <rum>rum</rum>\n" +
                "   <recurrent>false</recurrent>\n" +
                "   <status>status</status>\n" +
                "   <debtor>\n" +
                "      <lastName>lastName</lastName>\n" +
                "      <bic>bic</bic>\n" +
                "      <iban>iban</iban>\n" +
                "      <firstName>firstName</firstName>\n" +
                "      <street>street</street>\n" +
                "      <postalCode>postalCode</postalCode>\n" +
                "      <town>town</town>\n" +
                "      <phoneNumber>phoneNumber</phoneNumber>\n" +
                "      <countryCode>countryCode</countryCode>\n" +
                "      <complement>complement</complement>\n" +
                "      <complement2>complement2</complement2>\n" +
                "   </debtor>\n" +
                "   <mode>mode</mode>\n" +
                "   <flowName>flowName</flowName>\n" +
                "   <language>language</language>\n" +
                "   <contextIdentifier>contextIdentifier</contextIdentifier>\n" +
                "</WSMandateDTO>"
        );

        Mockito.doReturn(mandateCreateStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        // Initialize the MandateCreateRequest
        MandateCreateRequest mandateCreateRequest = RequestBuilderFactory.buildMandateCreateRequest(paymentRequest);

        // Call the method to test (only the MandateCreateRequest is needed)
        PaymentResponse mandateCreatePaymentResponse = paymentServiceStep02.mandateCreate(
                mandateCreateRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNull(mandateCreatePaymentResponse);

    }

    @Test
    public void testMandateCreate_KoOnStringResponseNull() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse mandateCreateStringResponse = null;

        Mockito.doReturn(mandateCreateStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        // Initialize the MandateCreateRequest
        MandateCreateRequest mandateCreateRequest = RequestBuilderFactory.buildMandateCreateRequest(paymentRequest);

        // Call the method to test (only the MandateCreateRequest is needed)
        PaymentResponse mandateCreatePaymentResponse = paymentServiceStep02.mandateCreate(
                mandateCreateRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(mandateCreatePaymentResponse);
        Assert.assertTrue(mandateCreatePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) mandateCreatePaymentResponse;

        Assert.assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("no code transmitted", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testMandateCreate_KoOnHttpCallError500() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse mandateCreateStringResponse = new StringResponse();
        mandateCreateStringResponse.setCode(500);
        mandateCreateStringResponse.setMessage("KO");
        mandateCreateStringResponse.setContent("");

        Mockito.doReturn(mandateCreateStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        // Initialize the MandateCreateRequest
        MandateCreateRequest mandateCreateRequest = RequestBuilderFactory.buildMandateCreateRequest(paymentRequest);

        // Call the method to test (only the MandateCreateRequest is needed)
        PaymentResponse mandateCreatePaymentResponse = paymentServiceStep02.mandateCreate(
                mandateCreateRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(mandateCreatePaymentResponse);
        Assert.assertTrue(mandateCreatePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) mandateCreatePaymentResponse;

        Assert.assertEquals(FailureCause.COMMUNICATION_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("500", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testMandateCreate_KoOnHttpCallError400WithData() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse mandateCreateStringResponse = new StringResponse();
        mandateCreateStringResponse.setCode(400);
        mandateCreateStringResponse.setMessage("KO");
        mandateCreateStringResponse.setContent(
                "<sepalia>\n" +
                "   <exception code=\"CREDITOR_NOT_FOUND\">CREDITOR_NOT_FOUND: PAYLINE</exception>\n" +
                "</sepalia>"
        );

        Mockito.doReturn(mandateCreateStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        // Initialize the MandateCreateRequest
        MandateCreateRequest mandateCreateRequest = RequestBuilderFactory.buildMandateCreateRequest(paymentRequest);

        // Call the method to test (only the MandateCreateRequest is needed)
        PaymentResponse mandateCreatePaymentResponse = paymentServiceStep02.mandateCreate(
                mandateCreateRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(mandateCreatePaymentResponse);
        Assert.assertTrue(mandateCreatePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) mandateCreatePaymentResponse;

        Assert.assertEquals(FailureCause.INVALID_DATA, paymentResponseFailure.getFailureCause());
        Assert.assertEquals(WSRequestResultEnum.CREDITOR_NOT_FOUND.getDocapostErrorCode(), paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testInitiateSignature_Ok() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse initiateSignatureStringResponse = new StringResponse();
        initiateSignatureStringResponse.setCode(200);
        initiateSignatureStringResponse.setMessage("OK");
        initiateSignatureStringResponse.setContent(
                "{\"transactionId\": \"expTransactionId\"}"
        );

        Mockito.doReturn(initiateSignatureStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setMandateRum("expMandateRum");

        // Initialize the InitiateSignatureRequest
        InitiateSignatureRequest initiateSignatureRequest = RequestBuilderFactory.buildInitiateSignatureRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the InitiateSignatureRequest is needed)
        PaymentResponse initiateSignaturePaymentResponse = paymentServiceStep02.initiateSignature(
                initiateSignatureRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNull(initiateSignaturePaymentResponse);
        Assert.assertEquals("expMandateRum", docapostLocalParam.getMandateRum());
        Assert.assertEquals("expTransactionId", docapostLocalParam.getTransactionId());

    }

    @Test
    public void testInitiateSignature_KoOnStringResponseNull() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse initiateSignatureStringResponse = null;

        Mockito.doReturn(initiateSignatureStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setMandateRum("expMandateRum");

        // Initialize the InitiateSignatureRequest
        InitiateSignatureRequest initiateSignatureRequest = RequestBuilderFactory.buildInitiateSignatureRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the InitiateSignatureRequest is needed)
        PaymentResponse initiateSignaturePaymentResponse = paymentServiceStep02.initiateSignature(
                initiateSignatureRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(initiateSignaturePaymentResponse);
        Assert.assertTrue(initiateSignaturePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) initiateSignaturePaymentResponse;

        Assert.assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("no code transmitted", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testInitiateSignature_KoOnHttpCallError500() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse initiateSignatureStringResponse = new StringResponse();
        initiateSignatureStringResponse.setCode(500);
        initiateSignatureStringResponse.setMessage("KO");
        initiateSignatureStringResponse.setContent("");

        Mockito.doReturn(initiateSignatureStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setMandateRum("expMandateRum");

        // Initialize the InitiateSignatureRequest
        InitiateSignatureRequest initiateSignatureRequest = RequestBuilderFactory.buildInitiateSignatureRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the InitiateSignatureRequest is needed)
        PaymentResponse initiateSignaturePaymentResponse = paymentServiceStep02.initiateSignature(
                initiateSignatureRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(initiateSignaturePaymentResponse);
        Assert.assertTrue(initiateSignaturePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) initiateSignaturePaymentResponse;

        Assert.assertEquals(FailureCause.COMMUNICATION_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("500", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testInitiateSignature_KoOnHttpCallError200WithData() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse initiateSignatureStringResponse = new StringResponse();
        initiateSignatureStringResponse.setCode(200);
        initiateSignatureStringResponse.setMessage("KO");
        initiateSignatureStringResponse.setContent(
                "{\"errors\": [{\n" +
                "   \"reason\": \"CREDITOR_NOT_FOUND\",\n" +
                "   \"message\": \"creditorId=MARCHAND2\",\n" +
                "   \"parameters\": [],\n" +
                "   \"id\": 1548248065213\n" +
                "}]}"
        );

        Mockito.doReturn(initiateSignatureStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setMandateRum("expMandateRum");

        // Initialize the InitiateSignatureRequest
        InitiateSignatureRequest initiateSignatureRequest = RequestBuilderFactory.buildInitiateSignatureRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the InitiateSignatureRequest is needed)
        PaymentResponse initiateSignaturePaymentResponse = paymentServiceStep02.initiateSignature(
                initiateSignatureRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(initiateSignaturePaymentResponse);
        Assert.assertTrue(initiateSignaturePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) initiateSignaturePaymentResponse;

        Assert.assertEquals(FailureCause.PARTNER_UNKNOWN_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals(WSRequestResultEnum.PARTNER_UNKNOWN_ERROR.getDocapostErrorCode(), paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testSendOtp_Ok() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse sendOtpStringResponse = new StringResponse();
        sendOtpStringResponse.setCode(200);
        sendOtpStringResponse.setMessage("OK");
        sendOtpStringResponse.setContent(
                "{\"signatureID\": \"expSignatureId\"}"
        );

        Mockito.doReturn(sendOtpStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setMandateRum("expMandateRum");
        docapostLocalParam.setTransactionId("expTransactionId");

        // Initialize the SendOtpRequest
        SendOtpRequest sendOtpRequest = RequestBuilderFactory.buildSendOtpRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the SendOtpRequest is needed)
        PaymentResponse sendOtpPaymentResponse = paymentServiceStep02.sendOtp(
                sendOtpRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNull(sendOtpPaymentResponse);
        Assert.assertEquals("expMandateRum", docapostLocalParam.getMandateRum());
        Assert.assertEquals("expTransactionId", docapostLocalParam.getTransactionId());
        Assert.assertEquals("expSignatureId", docapostLocalParam.getSignatureId());

    }

    @Test
    public void testSendOtp_KoOnStringResponseNull() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse sendOtpStringResponse = null;

        Mockito.doReturn(sendOtpStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setMandateRum("expMandateRum");
        docapostLocalParam.setTransactionId("expTransactionId");

        // Initialize the SendOtpRequest
        SendOtpRequest sendOtpRequest = RequestBuilderFactory.buildSendOtpRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the SendOtpRequest is needed)
        PaymentResponse sendOtpPaymentResponse = paymentServiceStep02.sendOtp(
                sendOtpRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(sendOtpPaymentResponse);
        Assert.assertTrue(sendOtpPaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) sendOtpPaymentResponse;

        Assert.assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("no code transmitted", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testSendOtp_KoOnHttpCallError500() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse sendOtpStringResponse = new StringResponse();
        sendOtpStringResponse.setCode(500);
        sendOtpStringResponse.setMessage("KO");
        sendOtpStringResponse.setContent("");

        Mockito.doReturn(sendOtpStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setMandateRum("expMandateRum");
        docapostLocalParam.setTransactionId("expTransactionId");

        // Initialize the SendOtpRequest
        SendOtpRequest sendOtpRequest = RequestBuilderFactory.buildSendOtpRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the SendOtpRequest is needed)
        PaymentResponse sendOtpPaymentResponse = paymentServiceStep02.sendOtp(
                sendOtpRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(sendOtpPaymentResponse);
        Assert.assertTrue(sendOtpPaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) sendOtpPaymentResponse;

        Assert.assertEquals(FailureCause.COMMUNICATION_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("500", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testSendOtp_KoOnHttpCallError200WithData() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse sendOtpStringResponse = new StringResponse();
        sendOtpStringResponse.setCode(200);
        sendOtpStringResponse.setMessage("KO");
        sendOtpStringResponse.setContent(
                "{\"errors\": [{\n" +
                "   \"reason\": \"CREDITOR_NOT_FOUND\",\n" +
                "   \"message\": \"creditorId=MARCHAND2\",\n" +
                "   \"parameters\": [],\n" +
                "   \"id\": 1548248065213\n" +
                "}]}"
        );

        Mockito.doReturn(sendOtpStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        paymentRequest = createCompletePaymentRequest();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setMandateRum("expMandateRum");
        docapostLocalParam.setTransactionId("expTransactionId");

        // Initialize the SendOtpRequest
        SendOtpRequest sendOtpRequest = RequestBuilderFactory.buildSendOtpRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the SendOtpRequest is needed)
        PaymentResponse sendOtpPaymentResponse = paymentServiceStep02.sendOtp(
                sendOtpRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(sendOtpPaymentResponse);
        Assert.assertTrue(sendOtpPaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) sendOtpPaymentResponse;

        Assert.assertEquals(FailureCause.PARTNER_UNKNOWN_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals(WSRequestResultEnum.PARTNER_UNKNOWN_ERROR.getDocapostErrorCode(), paymentResponseFailure.getErrorCode());

    }

}
