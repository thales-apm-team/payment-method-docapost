package com.payline.payment.docapost.utils.http;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

import static com.payline.payment.docapost.utils.DocapostConstants.APPLICATION_XML;
import static com.payline.payment.docapost.utils.DocapostConstants.APPLICATION_X_WWW_FORM_URLENCODED;

/**
 * Created by Thales on 06/09/2018.
 */
public class DocapostHttpClient extends HttpClient {

    /**
     * Instantiate a HTTP client.
     *
     * @param connectTimeout Default connect timeout (in seconds) for new connections. A value of 0 means no timeout.
     * @param writeTimeout   Default write timeout (in seconds) for new connections. A value of 0 means no timeout.
     * @param readTimeout    Default read timeout (in seconds) for new connections. A value of 0 means no timeout.
     */
    public DocapostHttpClient(int connectTimeout, int writeTimeout, int readTimeout) {
        super(connectTimeout, writeTimeout, readTimeout);
    }

    /**
     * Send a POST request, with a XML content type.
     *
     * @param scheme URL scheme
     * @param host URL host
     * @param path URL path
     * @param xmlContent The JSON content, as a string
     * @param credential The authentication credential
     * @return The response returned from the HTTP call
     * @throws IOException
     */
    public Response doPost(String scheme, String host, String path, String xmlContent, String credential ) throws IOException {
        RequestBody body = RequestBody.create( MediaType.parse( APPLICATION_XML ), xmlContent );
        return super.doPost( scheme, host, path, body, APPLICATION_XML, credential );
    }

    /**
     * Send a POST request, with a XML content type.
     *
     * @param scheme URL scheme
     * @param host URL host
     * @param path URL path
     * @param body The urlencoded parameter list
     * @param credential The authentication credential
     * @return The response returned from the HTTP call
     * @throws IOException
     */
    public Response doPost(String scheme, String host, String path, Map<String, String> body, String credential ) throws IOException {
        RequestBody requestBody = new RequestBodyBuilder().withFormData(body).build();
        return super.doPost( scheme, host, path, requestBody, APPLICATION_X_WWW_FORM_URLENCODED, credential );
    }

}