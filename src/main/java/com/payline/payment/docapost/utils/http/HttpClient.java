package com.payline.payment.docapost.utils.http;

import static com.payline.payment.docapost.utils.DocapostConstants.AUTHORIZATION;
import static com.payline.payment.docapost.utils.DocapostConstants.CONTENT_TYPE;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.payline.payment.docapost.TmpTestData;
import okhttp3.*;

/**
 * This utility class provides a basic HTTP client to send requests, using OkHttp library.
 * It must be extended to match each payment method needs.
 */
public abstract class HttpClient {

    private OkHttpClient client;

    /**
     *  Instantiate a HTTP client.
     *
     * @param connectTimeout Default connect timeout (in seconds) for new connections. A value of 0 means no timeout.
     * @param writeTimeout Default write timeout (in seconds) for new connections. A value of 0 means no timeout.
     * @param readTimeout Default read timeout (in seconds) for new connections. A value of 0 means no timeout.
     */
    protected HttpClient(int connectTimeout, int writeTimeout, int readTimeout ) {

        this.client = new OkHttpClient.Builder()
                // FIXME : This authentication method does not work, credential are added as request's header
//                .authenticator(new Authenticator() {
//                    @Override
//                    public Request authenticate(Route route, Response response) throws IOException {
//                        if (response.request().header("Authorization") != null) {
//                            return null; // Give up, we've already attempted to authenticate.
//                        }
//
//                        System.out.println("Authenticating for response: " + response);
//                        System.out.println("Challenges: " + response.challenges());
//                        String credential = Credentials.basic(TmpTestData.AUTH_LOGIN, TmpTestData.AUTH_LOGIN);
//                        return response.request().newBuilder()
//                                .header("Authorization", credential)
//                                .build();
//                    }
//                })
                .connectTimeout( connectTimeout, TimeUnit.SECONDS )
                .writeTimeout( writeTimeout, TimeUnit.SECONDS )
                .readTimeout( readTimeout, TimeUnit.SECONDS )
                .build();

    }

    /**
     * Send a POST request.
     *
     * @param scheme URL scheme
     * @param host URL host
     * @param path URL path
     * @param requestBody Request body
     * @param contentType The content type of the request body
     * @param credential The authentication credential
     * @return The response returned from the HTTP call
     * @throws IOException
     */
    protected Response doPost(String scheme, String host, String path, RequestBody requestBody, String contentType, String credential)
            throws IOException {

        // create url
        HttpUrl url = new HttpUrl.Builder()
                .scheme( scheme )
                .host( host )
                // Use addPathSegments instead of addPathSegment because the path contains / char
                .addPathSegments( path )
                .build();

        // create request
        Request request = new Request.Builder()
                .addHeader( AUTHORIZATION, credential)
                .addHeader( CONTENT_TYPE, contentType )
                .url( url )
                .post( requestBody )
                .build();

        // do the request
        return this.client.newCall( request ).execute();

    }

//    /**
//     * Send a POST request.
//     *
//     * @param scheme URL scheme
//     * @param host URL host
//     * @param path URL path
//     * @param requestBody Request body
//     * @param contentType The content type of the request body
//     * @return The response returned from the HTTP call
//     * @throws IOException
//     */
//    protected Response doPost(String scheme, String host, String path, RequestBody requestBody, String contentType, String credential)
//            throws IOException {
//
//        // create url
//        HttpUrl url = new HttpUrl.Builder()
//                .scheme( scheme )
//                .host( host )
//                // Use addPathSegments instead of addPathSegment because the path contains / char
//                .addPathSegments( path )
//                .build();
//
//        // create request
//        Request request = new Request.Builder()
//                .addHeader( CONTENT_TYPE, contentType )
//                .url( url )
//                .post( requestBody )
//                .build();
//
//        // do the request
//        return this.client.newCall( request ).execute();
//
//    }

}
