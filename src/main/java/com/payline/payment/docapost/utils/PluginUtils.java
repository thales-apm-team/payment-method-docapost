package com.payline.payment.docapost.utils;

import com.payline.payment.docapost.exception.InvalidRequestException;
import com.payline.payment.docapost.utils.config.ConfigEnvironment;
import com.payline.pmapi.bean.ActionRequest;
import com.payline.pmapi.bean.Request;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;

import java.util.Map;

public class PluginUtils {


    public static final String URL_DELIMITER = "/";

    private PluginUtils() {
        // ras.
    }

    public static boolean isEmpty(String s) {

        return s == null || s.isEmpty();
    }

    public static <T> T requireNonNull(T obj, String message) throws InvalidRequestException {
        if (obj == null) {
            throw new InvalidRequestException(message);
        }
        return obj;
    }

    public static <T> T requireNonNull(Map map, String key, String err) throws InvalidRequestException {
        return PluginUtils.requireNonNull((T) map.get(key), err);
    }

    public static ConfigEnvironment getEnvironnement(ActionRequest actionRequest) {
        return actionRequest.getEnvironment().isSandbox() ? ConfigEnvironment.DEV : ConfigEnvironment.PROD;
    }

    public static ConfigEnvironment getEnvironnement(Request request) {
        return request.getEnvironment().isSandbox() ? ConfigEnvironment.DEV : ConfigEnvironment.PROD;
    }

    public static ConfigEnvironment getEnvironnement(ContractParametersCheckRequest contractParametersCheckRequest) {
        return contractParametersCheckRequest.getEnvironment().isSandbox() ? ConfigEnvironment.DEV : ConfigEnvironment.PROD;
    }

}