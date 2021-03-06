package com.payline.payment.docapost.service.impl;

import com.payline.payment.docapost.service.PaymentServiceStep;
import com.payline.payment.docapost.utils.DocapostLocalParam;
import com.payline.payment.docapost.utils.DocapostUtils;
import com.payline.payment.docapost.utils.PluginUtils;
import com.payline.payment.docapost.utils.config.ConfigEnvironment;
import com.payline.payment.docapost.utils.http.DocapostHttpClient;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.service.PaymentService;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import static com.payline.payment.docapost.utils.DocapostConstants.*;

public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);

    private DocapostHttpClient httpClient;

    private DocapostLocalParam docapostLocalParam;

    /**
     * Constructeur
     */
    public PaymentServiceImpl() {
        httpClient = DocapostHttpClient.getInstance();
        docapostLocalParam = DocapostLocalParam.getInstance();
    }

    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {

        PaymentResponse response = null;

        // On recharge en local les parametres contextuels de requete
        docapostLocalParam.restoreFromPaylineRequest(paymentRequest);

        ConfigEnvironment env = PluginUtils.getEnvironnement(paymentRequest);
        String credentials = null;

        // Recuperation de l'information de step (etape du processus)
        String step = paymentRequest.getRequestContext().getRequestData().get(CONTEXT_DATA_STEP);

        LOGGER.debug("PaymentRequest step : " + step);

        //----------------------------------------------------------------------------------------------------------
        //**********************************************************************************************************
        //**********************************************************************************************************
        //**********************************************************************************************************
        // Cas 1 : 1ere reception (contextData.get("step") = null ou vide
        if (PluginUtils.isEmpty(step)) {
            PaymentServiceStep step01 = new PaymentServiceStep01();
            return step01.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);
        }

        // On genere les credentials necessaire pour les appels WS qui vont se faire aux steps 2 et 3 (pas d'appel WS au step 1)
        credentials = generateCredentials(paymentRequest);

        //----------------------------------------------------------------------------------------------------------
        //**********************************************************************************************************
        //**********************************************************************************************************
        //**********************************************************************************************************
        // Cas 2 : 2nde reception (contextData.get("step") = IBAN_PHONE
        if (CONTEXT_DATA_STEP_IBAN_PHONE.equals(step)) {
            PaymentServiceStep step02 = new PaymentServiceStep02(httpClient);
            return step02.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);
        }

        //----------------------------------------------------------------------------------------------------------
        //**********************************************************************************************************
        //**********************************************************************************************************
        //**********************************************************************************************************
        //*** Cas 3 : 3eme reception (contextData.get("step") = 3
        if (CONTEXT_DATA_STEP_OTP.equals(step)) {
            PaymentServiceStep step03 = new PaymentServiceStep03(httpClient);
            return step03.processPaymentStep(paymentRequest, env, docapostLocalParam, credentials);
        }

        return response;

    }

    /**
     * Generation des credentials necessaire a l'authentification lors des appels Ws
     *
     * @param paymentRequest
     * @return
     */
    protected String generateCredentials(PaymentRequest paymentRequest) {

        String credentials;

        // Recuperation des donnees necessaires pour la generation du Header Basic credentials des appels WS
        String authLogin = paymentRequest.getPartnerConfiguration().getProperty(PARTNER_CONFIG_AUTH_LOGIN);
        String authPass = paymentRequest.getPartnerConfiguration().getProperty(PARTNER_CONFIG_AUTH_PASS);

        // Generation des credentials
        credentials = DocapostUtils.generateBasicCredentials(authLogin, authPass);

        return credentials;

    }


}
