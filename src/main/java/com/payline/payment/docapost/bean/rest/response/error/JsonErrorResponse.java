package com.payline.payment.docapost.bean.rest.response.error;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Thales on 07/09/2018.
 */
public class JsonErrorResponse {

    @SerializedName("reason")
    private String reason;

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("\n");
        result.append("message : " + message + "\n");
        result.append("reason : " + reason + "\n");

        return result.toString();
    }

}