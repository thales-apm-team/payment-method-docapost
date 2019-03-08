package com.payline.payment.docapost.service.impl;

import com.payline.payment.docapost.bean.PaymentResponseSuccessAdditionalData;
import com.payline.payment.docapost.bean.rest.request.RequestBuilderFactory;
import com.payline.payment.docapost.bean.rest.request.mandate.MandateCreateRequest;
import com.payline.payment.docapost.bean.rest.request.mandate.SddOrderCreateRequest;
import com.payline.payment.docapost.bean.rest.request.signature.InitiateSignatureRequest;
import com.payline.payment.docapost.bean.rest.request.signature.SendOtpRequest;
import com.payline.payment.docapost.bean.rest.request.signature.SetCodeRequest;
import com.payline.payment.docapost.bean.rest.request.signature.TerminateSignatureRequest;
import com.payline.payment.docapost.bean.rest.response.error.XmlErrorResponse;
import com.payline.payment.docapost.bean.rest.response.mandate.AbstractXmlResponse;
import com.payline.payment.docapost.bean.rest.response.mandate.WSDDOrderDTOResponse;
import com.payline.payment.docapost.exception.InvalidRequestException;
import com.payline.payment.docapost.utils.DocapostLocalParam;
import com.payline.payment.docapost.utils.DocapostUtils;
import com.payline.payment.docapost.utils.config.ConfigEnvironment;
import com.payline.payment.docapost.utils.http.DocapostHttpClient;
import com.payline.payment.docapost.utils.http.StringResponse;
import com.payline.payment.docapost.utils.type.WSRequestResultEnum;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
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
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.docapost.TestUtils.createCompletePaymentRequest;
import static com.payline.payment.docapost.TestUtils.createCustomPaymentRequestStep3;
import static com.payline.payment.docapost.service.PaymentServiceStep.DEFAULT_ERROR_CODE;
import static com.payline.payment.docapost.utils.DocapostConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceStep03Test {

    @InjectMocks
    @Spy
    private PaymentServiceStep03 paymentServiceStep03;

    @Mock
    private DocapostHttpClient httpClient;

    private PaymentRequest paymentRequest;
    private ConfigEnvironment env;
    private DocapostLocalParam docapostLocalParam;
    private String credentials;

    @Before
    public void setup() {

        env = ConfigEnvironment.DEV;

        // Initialize docapost local param with data from step 2
        docapostLocalParam = DocapostLocalParam.getInstance();
        docapostLocalParam.setMandateRum("expMandateRum");
        docapostLocalParam.setTransactionId("expTransactionId");
        docapostLocalParam.setSignatureId("expSignatureId");

        credentials = "Basic cGF5bGluZUBkb2NhcG9zdC5mcjpKOltlZjhkY2NtYQ==";

    }

    @Test
    public void testGetOrderCreateResponse_withXmlResponseOk() {

        // XML response message with data
        String response =
                "<WSDDOrderDTO>\n" +
                "   <label>label</label>\n" +
                "   <dueDate>dueDate</dueDate>\n" +
                "   <e2eId>e2eId</e2eId>\n" +
                "   <remitDate>remitDate</remitDate>\n" +
                "   <sequence>sequence</sequence>\n" +
                "   <identifier>identifier</identifier>\n" +
                "   <rum>rum</rum>\n" +
                "   <creditorId>creditorId</creditorId>\n" +
                "   <status>status</status>\n" +
                "   <amount>100.0</amount>\n" +
                "</WSDDOrderDTO>";

        AbstractXmlResponse abstractXmlResponse = paymentServiceStep03.getOrderCreateResponse(response);

        Assert.assertNotNull(abstractXmlResponse);
        Assert.assertTrue(abstractXmlResponse instanceof WSDDOrderDTOResponse);

        WSDDOrderDTOResponse wsddOrderDTOResponse = (WSDDOrderDTOResponse) abstractXmlResponse;

        Assert.assertEquals("label", wsddOrderDTOResponse.getLabel());
        Assert.assertEquals("dueDate", wsddOrderDTOResponse.getDueDate());
        Assert.assertEquals("e2eId", wsddOrderDTOResponse.getE2eId());
        Assert.assertEquals("remitDate", wsddOrderDTOResponse.getRemitDate());
        Assert.assertEquals("sequence", wsddOrderDTOResponse.getSequence());
        Assert.assertEquals("identifier", wsddOrderDTOResponse.getIdentifier());
        Assert.assertEquals("rum", wsddOrderDTOResponse.getRum());
        Assert.assertEquals("creditorId", wsddOrderDTOResponse.getCreditorId());
        Assert.assertEquals("status", wsddOrderDTOResponse.getStatus());
        Assert.assertEquals(new Float(100.00f), wsddOrderDTOResponse.getAmount());

    }

    @Test
    public void testGetOrderCreateResponse_withXmlResponseKo() {

        // XML response message with error
        String response =
                "<sepalia>\n" +
                "   <exception code=\"CREDITOR_NOT_FOUND\">CREDITOR_NOT_FOUND: PAYLINE</exception>\n" +
                "</sepalia>";

        AbstractXmlResponse abstractXmlResponse = paymentServiceStep03.getOrderCreateResponse(response);

        Assert.assertNotNull(abstractXmlResponse);
        Assert.assertTrue(abstractXmlResponse instanceof XmlErrorResponse);

        XmlErrorResponse xmlErrorResponse = (XmlErrorResponse) abstractXmlResponse;

        Assert.assertNotNull(xmlErrorResponse.getException());
        Assert.assertEquals("CREDITOR_NOT_FOUND", xmlErrorResponse.getException().getCode());
        Assert.assertEquals("CREDITOR_NOT_FOUND: PAYLINE", xmlErrorResponse.getException().getValue());

    }

    @Test
    public void testGetOrderCreateResponse_withWrongXmlResponseFormat() {

        // XML response message with wrong data tag
        String response = "<wrongtag></wrongtag>";

        AbstractXmlResponse abstractXmlResponse = paymentServiceStep03.getOrderCreateResponse(response);

        Assert.assertNull(abstractXmlResponse);

    }

    @Test
    public void testGetOrderCreateResponse_withEmptyXmlResponse() {

        // XML response message with empty data
        String response = "";

        AbstractXmlResponse abstractXmlResponse = paymentServiceStep03.getOrderCreateResponse(response);

        Assert.assertNull(abstractXmlResponse);

    }

    @Test
    public void testProcessPayment_KoOnSetCode() throws IOException, URISyntaxException {

        PaymentResponseFailure expectedSetCodePaymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(FailureCause.INVALID_DATA)
                .withErrorCode("ERROR CODE")
                .build();

        Mockito.doReturn(expectedSetCodePaymentResponse).when(paymentServiceStep03).setCode(any(SetCodeRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep03.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        // On a MandateCreate call with error, the method must return and not continue
        Mockito.verify(paymentServiceStep03, Mockito.times(1)).setCode(any(SetCodeRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep03, Mockito.times(0)).terminateSignature(any(TerminateSignatureRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep03, Mockito.times(0)).orderCreate(any(SddOrderCreateRequest.class), anyString(), anyString(), anyString());

        // In the error response, we must retrieve the data from expectedPaymentResponseFailure returned from MandateCreate
        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(expectedSetCodePaymentResponse.getFailureCause(), paymentResponseFailure.getFailureCause());
        Assert.assertEquals(expectedSetCodePaymentResponse.getErrorCode(), paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_KoOnTerminateSignature() throws IOException, URISyntaxException {

        PaymentResponseFailure expectedSetCodePaymentResponse = null;

        Mockito.doReturn(expectedSetCodePaymentResponse).when(paymentServiceStep03).setCode(any(SetCodeRequest.class), anyString(), anyString(), anyString());

        PaymentResponseFailure expectedTerminateSignaturePaymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(FailureCause.INVALID_DATA)
                .withErrorCode("ERROR CODE")
                .build();

        Mockito.doReturn(expectedTerminateSignaturePaymentResponse).when(paymentServiceStep03).terminateSignature(any(TerminateSignatureRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep03.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        // On a MandateCreate call with error, the method must return and not continue
        Mockito.verify(paymentServiceStep03, Mockito.times(1)).setCode(any(SetCodeRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep03, Mockito.times(1)).terminateSignature(any(TerminateSignatureRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep03, Mockito.times(0)).orderCreate(any(SddOrderCreateRequest.class), anyString(), anyString(), anyString());

        // In the error response, we must retrieve the data from expectedPaymentResponseFailure returned from MandateCreate
        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(expectedTerminateSignaturePaymentResponse.getFailureCause(), paymentResponseFailure.getFailureCause());
        Assert.assertEquals(expectedTerminateSignaturePaymentResponse.getErrorCode(), paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_KoOnOrderCreate() throws IOException, URISyntaxException {

        PaymentResponseFailure expectedSetCodePaymentResponse = null;

        Mockito.doReturn(expectedSetCodePaymentResponse).when(paymentServiceStep03).setCode(any(SetCodeRequest.class), anyString(), anyString(), anyString());

        PaymentResponseFailure expectedTerminateSignaturePaymentResponse = null;

        Mockito.doReturn(expectedTerminateSignaturePaymentResponse).when(paymentServiceStep03).terminateSignature(any(TerminateSignatureRequest.class), anyString(), anyString(), anyString());

        PaymentResponseFailure expectedOrderCreatePaymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(FailureCause.INVALID_DATA)
                .withErrorCode("ERROR CODE")
                .build();

        Mockito.doReturn(expectedOrderCreatePaymentResponse).when(paymentServiceStep03).orderCreate(any(SddOrderCreateRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep03.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        // On a MandateCreate call with error, the method must return and not continue
        Mockito.verify(paymentServiceStep03, Mockito.times(1)).setCode(any(SetCodeRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep03, Mockito.times(1)).terminateSignature(any(TerminateSignatureRequest.class), anyString(), anyString(), anyString());
        Mockito.verify(paymentServiceStep03, Mockito.times(1)).orderCreate(any(SddOrderCreateRequest.class), anyString(), anyString(), anyString());

        // In the error response, we must retrieve the data from expectedPaymentResponseFailure returned from MandateCreate
        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(expectedOrderCreatePaymentResponse.getFailureCause(), paymentResponseFailure.getFailureCause());
        Assert.assertEquals(expectedOrderCreatePaymentResponse.getErrorCode(), paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_KoOnIOException() throws IOException, URISyntaxException {

        Mockito.doThrow(IOException.class).when(paymentServiceStep03).setCode(any(SetCodeRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep03.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(FailureCause.COMMUNICATION_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("no code transmitted", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_KoOnException() throws IOException, URISyntaxException {

        Mockito.doThrow(URISyntaxException.class).when(paymentServiceStep03).setCode(any(SetCodeRequest.class), anyString(), anyString(), anyString());

        // Initialize the payment request
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep03.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        Assert.assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("no code transmitted", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testProcessPayment_Ok() throws IOException, URISyntaxException {

        // Initialize the payment request
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        PaymentResponseFailure expectedSetCodePaymentResponse = null;

        Mockito.doReturn(expectedSetCodePaymentResponse).when(paymentServiceStep03).setCode(any(SetCodeRequest.class), anyString(), anyString(), anyString());

        // A signatureSuccess status to true is set during SetCode WS call
        docapostLocalParam.setSignatureSuccess(true);
        
        PaymentResponseFailure expectedTerminateSignaturePaymentResponse = null;

        Mockito.doReturn(expectedTerminateSignaturePaymentResponse).when(paymentServiceStep03).terminateSignature(any(TerminateSignatureRequest.class), anyString(), anyString(), anyString());

        PaymentResponseFailure expectedOrderCreatePaymentResponse = null;

        Mockito.doReturn(expectedOrderCreatePaymentResponse).when(paymentServiceStep03).orderCreate(any(SddOrderCreateRequest.class), anyString(), anyString(), anyString());

        // A status true is set during SetCode WS call
        docapostLocalParam.setOrderStatus("expOrderStatus");

        // Call the service's method to test
        PaymentResponse paymentResponse = paymentServiceStep03.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);

        Assert.assertNotNull(paymentResponse);
        Assert.assertTrue(paymentResponse instanceof PaymentResponseSuccess);

        PaymentResponseSuccess paymentResponseSuccess = (PaymentResponseSuccess) paymentResponse;

        Assert.assertEquals(paymentRequest.getTransactionId(), paymentResponseSuccess.getPartnerTransactionId());
        Assert.assertEquals(docapostLocalParam.getOrderStatus(), paymentResponseSuccess.getStatusCode());

        PaymentResponseSuccessAdditionalData expAdditionalData = DocapostUtils.buildPaymentResponseSuccessAdditionalData(docapostLocalParam);

        String transactionAdditionalData = paymentResponseSuccess.getTransactionAdditionalData();

        Assert.assertNotNull(paymentResponse);
        Assert.assertEquals(expAdditionalData.toJson(), transactionAdditionalData);

    }

    @Test
    public void testSetCode_Ok() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse setCodeStringResponse = new StringResponse();
        setCodeStringResponse.setCode(200);
        setCodeStringResponse.setMessage("OK");
        setCodeStringResponse.setContent( "{}");

        Mockito.doReturn(setCodeStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        // Initialize the SetCodeRequest
        SetCodeRequest setCodeRequest = RequestBuilderFactory.buildSetCodeRequest(paymentRequest);

        // Call the method to test (only the SendOtpRequest is needed)
        PaymentResponse setCodePaymentResponse = paymentServiceStep03.setCode(
                setCodeRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNull(setCodePaymentResponse);

    }

    @Test
    public void testSetCode_KoOnStringResponseNull() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse setCodeStringResponse = null;

        Mockito.doReturn(setCodeStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        // Initialize the SetCodeRequest
        SetCodeRequest setCodeRequest = RequestBuilderFactory.buildSetCodeRequest(paymentRequest);

        // Call the method to test (only the SendOtpRequest is needed)
        PaymentResponse sendOtpPaymentResponse = paymentServiceStep03.setCode(
                setCodeRequest,
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
    public void testSetCode_KoOnHttpCallError500() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse setCodeStringResponse = new StringResponse();
        setCodeStringResponse.setCode(500);
        setCodeStringResponse.setMessage("KO");
        setCodeStringResponse.setContent("");

        Mockito.doReturn(setCodeStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        // Initialize the SetCodeRequest
        SetCodeRequest setCodeRequest = RequestBuilderFactory.buildSetCodeRequest(paymentRequest);

        // Call the method to test (only the SendOtpRequest is needed)
        PaymentResponse sendOtpPaymentResponse = paymentServiceStep03.setCode(
                setCodeRequest,
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
    public void testSetCode_KoOnHttpCallError200WithData() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse setCodeStringResponse = new StringResponse();
        setCodeStringResponse.setCode(200);
        setCodeStringResponse.setMessage("KO");
        setCodeStringResponse.setContent(
                "{\"errors\": [{\n" +
                "   \"reason\": \"CREDITOR_NOT_FOUND\",\n" +
                "   \"message\": \"creditorId=MARCHAND2\",\n" +
                "   \"parameters\": [],\n" +
                "   \"id\": 1548248065213\n" +
                "}]}"
        );

        Mockito.doReturn(setCodeStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        // Initialize the SetCodeRequest
        SetCodeRequest setCodeRequest = RequestBuilderFactory.buildSetCodeRequest(paymentRequest);

        // Call the method to test (only the SendOtpRequest is needed)
        PaymentResponse sendOtpPaymentResponse = paymentServiceStep03.setCode(
                setCodeRequest,
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

    @Test
    public void testTerminateSignature_Ok() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse terminateSignatureStringResponse = new StringResponse();
        terminateSignatureStringResponse.setCode(200);
        terminateSignatureStringResponse.setMessage("OK");
        terminateSignatureStringResponse.setContent( "{}");

        Mockito.doReturn(terminateSignatureStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setSignatureSuccess(true);

        // Initialize the TerminateSignatureRequest
        TerminateSignatureRequest terminateSignatureRequest = RequestBuilderFactory.buildTerminateSignatureRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the TerminateSignatureRequest is needed)
        PaymentResponse terminateSignaturePaymentResponse = paymentServiceStep03.terminateSignature(
                terminateSignatureRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNull(terminateSignaturePaymentResponse);

    }

    @Test
    public void testTerminateSignature_KoOnStringResponseNull() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse terminateSignatureStringResponse = null;

        Mockito.doReturn(terminateSignatureStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setSignatureSuccess(true);

        // Initialize the TerminateSignatureRequest
        TerminateSignatureRequest terminateSignatureRequest = RequestBuilderFactory.buildTerminateSignatureRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the TerminateSignatureRequest is needed)
        PaymentResponse terminateSignaturePaymentResponse = paymentServiceStep03.terminateSignature(
                terminateSignatureRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(terminateSignaturePaymentResponse);
        Assert.assertTrue(terminateSignaturePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) terminateSignaturePaymentResponse;

        Assert.assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("no code transmitted", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testTerminateSignature_KoOnHttpCallError500() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse terminateSignatureStringResponse = new StringResponse();
        terminateSignatureStringResponse.setCode(500);
        terminateSignatureStringResponse.setMessage("KO");
        terminateSignatureStringResponse.setContent("");

        Mockito.doReturn(terminateSignatureStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setSignatureSuccess(true);

        // Initialize the TerminateSignatureRequest
        TerminateSignatureRequest terminateSignatureRequest = RequestBuilderFactory.buildTerminateSignatureRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the TerminateSignatureRequest is needed)
        PaymentResponse terminateSignaturePaymentResponse = paymentServiceStep03.terminateSignature(
                terminateSignatureRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(terminateSignaturePaymentResponse);
        Assert.assertTrue(terminateSignaturePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) terminateSignaturePaymentResponse;

        Assert.assertEquals(FailureCause.COMMUNICATION_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("500", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testTerminateSignature_KoOnHttpCallError200WithData() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse terminateSignatureStringResponse = new StringResponse();
        terminateSignatureStringResponse.setCode(200);
        terminateSignatureStringResponse.setMessage("KO");
        terminateSignatureStringResponse.setContent(
                "{\"errors\": [{\n" +
                "   \"reason\": \"CREDITOR_NOT_FOUND\",\n" +
                "   \"message\": \"creditorId=MARCHAND2\",\n" +
                "   \"parameters\": [],\n" +
                "   \"id\": 1548248065213\n" +
                "}]}"
        );

        Mockito.doReturn(terminateSignatureStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                any(Map.class),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setSignatureSuccess(true);

        // Initialize the TerminateSignatureRequest
        TerminateSignatureRequest terminateSignatureRequest = RequestBuilderFactory.buildTerminateSignatureRequest(paymentRequest, docapostLocalParam);

        // Call the method to test (only the TerminateSignatureRequest is needed)
        PaymentResponse terminateSignaturePaymentResponse = paymentServiceStep03.terminateSignature(
                terminateSignatureRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(terminateSignaturePaymentResponse);
        Assert.assertTrue(terminateSignaturePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) terminateSignaturePaymentResponse;

        Assert.assertEquals(FailureCause.PARTNER_UNKNOWN_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals(WSRequestResultEnum.PARTNER_UNKNOWN_ERROR.getDocapostErrorCode(), paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testOrderCreate_Ok() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse orderCreateStringResponse = new StringResponse();
        orderCreateStringResponse.setCode(200);
        orderCreateStringResponse.setMessage("OK");
        orderCreateStringResponse.setContent(
                "<WSDDOrderDTO>\n" +
                    "   <label>label</label>\n" +
                    "   <dueDate>dueDate</dueDate>\n" +
                    "   <e2eId>e2eId</e2eId>\n" +
                    "   <remitDate>remitDate</remitDate>\n" +
                    "   <sequence>sequence</sequence>\n" +
                    "   <identifier>identifier</identifier>\n" +
                    "   <rum>rum</rum>\n" +
                    "   <creditorId>creditorId</creditorId>\n" +
                    "   <status>status</status>\n" +
                    "   <amount>100.0</amount>\n" +
                    "</WSDDOrderDTO>"
        );

        Mockito.doReturn(orderCreateStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setSignatureSuccess(true);

        // Initialize the SddOrderCreateRequest
        SddOrderCreateRequest orderCreateRequest = RequestBuilderFactory.buildSddOrderCreateRequest(paymentRequest);

        // Call the method to test (only the SddOrderCreateRequest is needed)
        PaymentResponse orderCreatePaymentResponse = paymentServiceStep03.orderCreate(
                orderCreateRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNull(orderCreatePaymentResponse);

    }

    @Test
    public void testOrderCreate_KoOnStringResponseNull() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse orderCreateStringResponse = null;

        Mockito.doReturn(orderCreateStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setSignatureSuccess(true);

        // Initialize the SddOrderCreateRequest
        SddOrderCreateRequest orderCreateRequest = RequestBuilderFactory.buildSddOrderCreateRequest(paymentRequest);

        // Call the method to test (only the SddOrderCreateRequest is needed)
        PaymentResponse orderCreatePaymentResponse = paymentServiceStep03.orderCreate(
                orderCreateRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(orderCreatePaymentResponse);
        Assert.assertTrue(orderCreatePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) orderCreatePaymentResponse;

        Assert.assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("no code transmitted", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testOrderCreate_KoOnHttpCallError500() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse orderCreateStringResponse = new StringResponse();
        orderCreateStringResponse.setCode(500);
        orderCreateStringResponse.setMessage("KO");
        orderCreateStringResponse.setContent("");

        Mockito.doReturn(orderCreateStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setSignatureSuccess(true);

        // Initialize the SddOrderCreateRequest
        SddOrderCreateRequest orderCreateRequest = RequestBuilderFactory.buildSddOrderCreateRequest(paymentRequest);

        // Call the method to test (only the SddOrderCreateRequest is needed)
        PaymentResponse orderCreatePaymentResponse = paymentServiceStep03.orderCreate(
                orderCreateRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(orderCreatePaymentResponse);
        Assert.assertTrue(orderCreatePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) orderCreatePaymentResponse;

        Assert.assertEquals(FailureCause.COMMUNICATION_ERROR, paymentResponseFailure.getFailureCause());
        Assert.assertEquals("500", paymentResponseFailure.getErrorCode());

    }

    @Test
    public void testOrderCreate_KoOnHttpCallError400WithData() throws IOException, URISyntaxException, InvalidRequestException {

        // Initialize http call StringResponse
        StringResponse orderCreateStringResponse = new StringResponse();
        orderCreateStringResponse.setCode(400);
        orderCreateStringResponse.setMessage("KO");
        orderCreateStringResponse.setContent(
                "<sepalia>\n" +
                "   <exception code=\"CREDITOR_NOT_FOUND\">CREDITOR_NOT_FOUND: PAYLINE</exception>\n" +
                "</sepalia>"
        );

        Mockito.doReturn(orderCreateStringResponse).when(httpClient).doPost(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );

        // Initialize the PaymentRequest
        createCompletePaymentRequestAndCompleteWithStep2Data();

        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);
        docapostLocalParam.setSignatureSuccess(true);

        // Initialize the SddOrderCreateRequest
        SddOrderCreateRequest orderCreateRequest = RequestBuilderFactory.buildSddOrderCreateRequest(paymentRequest);

        // Call the method to test (only the SddOrderCreateRequest is needed)
        PaymentResponse orderCreatePaymentResponse = paymentServiceStep03.orderCreate(
                orderCreateRequest,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
        );

        Assert.assertNotNull(orderCreatePaymentResponse);
        Assert.assertTrue(orderCreatePaymentResponse instanceof PaymentResponseFailure);

        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) orderCreatePaymentResponse;

        Assert.assertEquals(FailureCause.INVALID_DATA, paymentResponseFailure.getFailureCause());
        Assert.assertEquals(WSRequestResultEnum.CREDITOR_NOT_FOUND.getDocapostErrorCode(), paymentResponseFailure.getErrorCode());

    }

    private void createCompletePaymentRequestAndCompleteWithStep2Data() {
        paymentRequest = createCompletePaymentRequest();
        paymentRequest.getRequestContext().getRequestData().put(CONTEXT_DATA_MANDATE_RUM, "expMandateRum");
        paymentRequest.getRequestContext().getRequestData().put(CONTEXT_DATA_TRANSACTION_ID, "expTransactionId");
        paymentRequest.getRequestContext().getRequestData().put(CONTEXT_DATA_SIGNATURE_ID, "expSignatureId");
    }

}
