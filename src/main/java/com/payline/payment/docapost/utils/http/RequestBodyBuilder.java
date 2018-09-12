package com.payline.payment.docapost.utils.http;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.util.Map;

/**
 * Created by Thales on 06/09/2018.
 */
public class RequestBodyBuilder {

    Map<String, String> formData;

    /**
     * Default public constructor
     */
    public RequestBodyBuilder() { }

    public RequestBodyBuilder withFormData(Map<String, String> formData) {
        this.formData = formData;
        return this;
    }

    public RequestBody build() {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (entry.getValue() != null) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

}