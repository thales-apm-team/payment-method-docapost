package com.payline.payment.docapost.utils.config;

public enum ConfigEnvironment {

    DEV("dev"),
    PROD("prod");

    private String prefix;

    ConfigEnvironment(String prefix ) {
        this.prefix = prefix;
    }

    public String getPrefix(){
        return this.prefix;
    }

}
