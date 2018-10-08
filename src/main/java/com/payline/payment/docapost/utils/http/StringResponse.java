package com.payline.payment.docapost.utils.http;

public class StringResponse extends BeanResponse {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("***** StringResponse info\n");

        result.append("code : " + this.getCode() + "\n");
        result.append("message : " + this.getMessage() + "\n");
        result.append("content : " + content + "\n");

        return result.toString();
    }

}