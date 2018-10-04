package com.payline.payment.docapost.utils;

import com.payline.payment.docapost.TmpTestData;

import static com.payline.payment.docapost.utils.DocapostConstants.*;

import org.apache.commons.codec.binary.Base64;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DocapostUtils {

    public static boolean isEmpty(String s) {
        boolean result = false;
        if (s == null || s.isEmpty()) {
            result = true;
        }
        return result;
    }

    public static String generateMandateRum() {
        return new SimpleDateFormat(MANDATE_RUM_GENERATION_DATE_FORMAT).format(new Date());
    }

    public static String generateBasicCredentials(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String credentials = "Basic " + new String(encodedAuth);
        return credentials;
    }

}