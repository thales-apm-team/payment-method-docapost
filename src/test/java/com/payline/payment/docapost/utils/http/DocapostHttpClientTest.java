package com.payline.payment.docapost.utils.http;

import com.payline.payment.docapost.TestUtils;
import com.payline.payment.docapost.exception.InvalidRequestException;
import com.payline.payment.docapost.utils.DocapostUtils;
import com.payline.payment.docapost.utils.http.DocapostHttpClient;
import com.payline.payment.docapost.utils.http.StringResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;

public class DocapostHttpClientTest {

    @InjectMocks
    DocapostHttpClient client ;
    @Mock
    CloseableHttpClient closableClient;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();


    @Test
    public void testDoGetOK() throws IOException, URISyntaxException {
        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(client, "client", closableClient);

        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "FINE!"));
        Mockito.when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        Mockito.doReturn(httpResponse).when(closableClient).execute(Mockito.any());

        String credentials = DocapostUtils.generateBasicCredentials(TestUtils.GOOD_LOGIN, TestUtils.GOOD_PWD);
        String ics = TestUtils.GOOD_CREDITOR_ID;
        StringResponse response = client.doGet("https", "espaceclient.sepalia.fr", "rcte/api/creditor/" + ics, credentials);

        //Assert we have a response
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getCode());
    }

    @Test
    public void testDoGetKO() throws IOException, URISyntaxException {

        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(client, "client", closableClient);

        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 400, "BAD REQUEST"));
        Mockito.when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        Mockito.doReturn(httpResponse).when(closableClient).execute(Mockito.any());

        String credentials = DocapostUtils.generateBasicCredentials(TestUtils.GOOD_LOGIN, TestUtils.GOOD_PWD);
        String ics = TestUtils.GOOD_CREDITOR_ID;
        StringResponse response = client.doGet("https", "espaceclient.sepalia.fr", "rcte/api/creditor/" + ics, credentials);

        //Assert we have a response
        Assert.assertNotNull(response);
        Assert.assertEquals(400, response.getCode());
    }

    @Test
    public void testDoGetEmptyResponse() throws IOException, URISyntaxException {

        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("Partner response empty");

        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(client, "client", closableClient);
        Mockito.doReturn(null).when(closableClient).execute(Mockito.any());

        String credentials = DocapostUtils.generateBasicCredentials(TestUtils.GOOD_LOGIN, TestUtils.GOOD_PWD);
        String ics = TestUtils.GOOD_CREDITOR_ID;
        StringResponse response = client.doGet("https", "espaceclient.sepalia.fr", "rcte/api/creditor/" + ics, credentials);


    }




    @Test
    public void testDoPostOK() throws IOException, URISyntaxException {

        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(client, "client", closableClient);

        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "FINE"));
        Mockito.when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        Mockito.doReturn(httpResponse).when(closableClient).execute(Mockito.any());

        String credentials = DocapostUtils.generateBasicCredentials(TestUtils.GOOD_LOGIN, TestUtils.GOOD_PWD);
        StringResponse response = client.doPost("https", "espaceclient.sepalia.fr", "rcte/api/mandate/create", "<WSMandateDTO><creditorId>MARCHAND1</creditorId></WSMandateDTO>", credentials);

        //Assert we have a response
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getCode());


    }

    @Test
    public void testDoPostKO() throws IOException, URISyntaxException {

        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(client, "client", closableClient);

        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 400, "Bad Request"));
        Mockito.when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        Mockito.doReturn(httpResponse).when(closableClient).execute(Mockito.any());

        String credentials = DocapostUtils.generateBasicCredentials(TestUtils.GOOD_LOGIN, TestUtils.GOOD_PWD);
        StringResponse response = client.doPost("https", "espaceclient.sepalia.fr", "rcte/api/mandate/create", "<WSMandateDTO><creditorId>MARCHAND1</creditorId></WSMandateDTO>", credentials);

        //Assert we have a response
        Assert.assertNotNull(response);
        Assert.assertEquals(400, response.getCode());
        Assert.assertEquals("Bad Request", response.getMessage());

    }

    @Test
    public void testDoPostEmptyResponse() throws IOException, URISyntaxException {

        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("Partner response empty");

        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(client, "client", closableClient);
        Mockito.doReturn(null).when(closableClient).execute(Mockito.any());

        String credentials = DocapostUtils.generateBasicCredentials(TestUtils.GOOD_LOGIN, TestUtils.GOOD_PWD);
        StringResponse response = client.doPost("https", "espaceclient.sepalia.fr", "rcte/api/mandate/create", "<WSMandateDTO><creditorId>MARCHAND1</creditorId></WSMandateDTO>", credentials);


    }


    @Test
    public void testDoPostArrayKO() throws IOException, URISyntaxException {

        //Mock de l'appel externe
        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(client, "client", closableClient);
        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Bad Request"));
        Mockito.when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        Mockito.doReturn(httpResponse).when(closableClient).execute(Mockito.any());


        String credentials = DocapostUtils.generateBasicCredentials(TestUtils.GOOD_LOGIN, TestUtils.GOOD_PWD);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("parameter0", "value0");
        parameters.put("parameter1", "value1");
        parameters.put("parameter2", "value2");
        parameters.put("parameter3", "value3");

        StringResponse response = client.doPost("https", "espaceclient.sepalia.fr", "rcte/api/mandate/create", parameters, credentials);
        Assert.assertNotNull(response);
        Assert.assertEquals(500, response.getCode());
    }

    @Test
    public void testDoPostArrayOK() throws IOException, URISyntaxException {

        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(client, "client", closableClient);
        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "FINE"));
        Mockito.when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        Mockito.doReturn(httpResponse).when(closableClient).execute(Mockito.any());


        String credentials = DocapostUtils.generateBasicCredentials(TestUtils.GOOD_LOGIN, TestUtils.GOOD_PWD);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("parameter0", "value0");
        parameters.put("parameter1", "value1");
        parameters.put("parameter2", "value2");
        parameters.put("parameter3", "value3");

        StringResponse response = client.doPost("https", "espaceclient.sepalia.fr", "rcte/api/mandate/create", parameters, credentials);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getCode());
    }
}
