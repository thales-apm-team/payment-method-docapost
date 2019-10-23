package com.payline.payment.docapost.utils;

import com.payline.payment.docapost.bean.rest.request.signature.SendOtpRequest;
import com.payline.payment.docapost.utils.config.ConfigEnvironment;
import com.payline.payment.docapost.utils.config.ConfigProperties;
import com.payline.payment.docapost.utils.i18n.I18nService;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.paymentform.bean.field.*;
import com.payline.pmapi.bean.paymentform.bean.field.specific.PaymentFormInputFieldIban;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.payline.payment.docapost.utils.DocapostConstants.*;
import static com.payline.payment.docapost.utils.PluginUtils.URL_DELIMITER;

public class DocapostFormUtils {

    private I18nService i18n = I18nService.getInstance();
    private static final Logger LOGGER = LogManager.getLogger(DocapostFormUtils.class);
    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred: {}";


    /**
     * Create a form to input phone number and iban
     *
     * @return a CustomForm composed by a field PaymentFormInputFieldIban and a field PaymentFormDisplayFieldText
     */
    public static CustomForm createEmptyIbanPhonePaymentForm(Locale locale) {
        I18nService i18n = I18nService.getInstance();

        PaymentFormInputFieldIban ibanForm = PaymentFormInputFieldIban
                .IbanFieldBuilder
                .anIbanField()
                .withKey(IBAN_KEY)
                .withLabel(i18n.getMessage(OTP_IBAN_PHONE_TEXT_SET_IBAN, locale))
                .withRequired(IBAN_REQUIRED)
                .withRequiredErrorMessage(i18n.getMessage(IBAN_REQUIRED_ERROR_MESSAGE, locale))
                .build();

        PaymentFormInputFieldText phoneForm = PaymentFormInputFieldText
                .PaymentFormFieldTextBuilder
                .aPaymentFormFieldText()
                .withInputType(InputType.TEL)
                .withFieldIcon(PHONE_FIELD_ICON)
                .withKey(PHONE_KEY)
                .withLabel(i18n.getMessage(PHONE_LABEL, locale))
                .withPlaceholder(PHONE_PLACEHOLDER)
                .withRequired(PHONE_REQUIRED)
                .withRequiredErrorMessage(i18n.getMessage(PHONE_REQUIRED_ERROR_MESSAGE, locale))
                .withSecured(PHONE_SECURED)
                .withValidation(PHONE_VALIDATION)
                .withValidationErrorMessage(i18n.getMessage(PHONE_VALIDATION_MESSAGE, locale))
                .withInputType(INPUT_TYPE)
                .build();

        PaymentFormDisplayFieldText inputPhoneInfo = PaymentFormDisplayFieldText
                .PaymentFormDisplayFieldTextBuilder
                .aPaymentFormDisplayFieldText()
                .withContent(i18n.getMessage(OTP_IBAN_PHONE_TEXT_SET_PHONE_INFO, locale))
                .build();

        List<PaymentFormField> customFields = new ArrayList<>();
        customFields.add(ibanForm);
        customFields.add(phoneForm);
        customFields.add(inputPhoneInfo);

        return CustomForm
                .builder()
                .withCustomFields(customFields)
                .withButtonText(i18n.getMessage(CUSTOMFORM_TEXT, locale))
//                .withDescription(i18n.getMessage(CUSTOMFORM_DESCRIPTION, locale))
                .withDescription("")
                .withDisplayButton(DISPLAY_CUSTOMFORM_BUTTON)
                .build();

    }

    /**
     * Create a paymentForm with
     * - link to download mandate
     * - link to resend otp code
     * - link to CGV
     *
     * @return a CustomForm composed by a field PaymentFormInputFieldIban and a field PaymentFormDisplayFieldText
     */
    public static CustomForm createOTPPaymentForm(DocapostLocalParam docapostLocalParam, PaymentRequest request, SendOtpRequest sendOtpRequest) {

        Locale locale = request.getLocale();
        I18nService i18n = I18nService.getInstance();
        String phone = request.getPaymentFormContext().getPaymentFormParameter().get(FORM_FIELD_PHONE);

        PaymentFormDisplayFieldText otpText = PaymentFormDisplayFieldText
                .PaymentFormDisplayFieldTextBuilder
                .aPaymentFormDisplayFieldText()
                .withContent(i18n.getMessage(OTP_FORM_TEXT_OTP, locale))
                .build();

        PaymentFormDisplayFieldText setOtpText = PaymentFormDisplayFieldText
                .PaymentFormDisplayFieldTextBuilder
                .aPaymentFormDisplayFieldText()
                .withContent(i18n.getMessage(OTP_FORM_TEXT_SET_OTP, locale) + " " + phone)
                .build();

        PaymentFormInputFieldText otpForm = PaymentFormInputFieldText
                .PaymentFormFieldTextBuilder
                .aPaymentFormFieldText()
                .withInputType(InputType.NUMBER)
                .withFieldIcon(FieldIcon.PHONE)
                .withKey(OTP_FORM_KEY)
                .withLabel(i18n.getMessage(OTP_FORM_LABEL, locale))
                .withPlaceholder(OTP_FORM_PLACEHOLDER)
                .withRequired(false)
                .withRequiredErrorMessage(i18n.getMessage(OTP_FORM_REQUIRED_ERROR_MESSAGE, locale))
                .withSecured(false)
                .withValidation(OTP_FORM_VALIDATION)
                .withValidationErrorMessage(i18n.getMessage(OTP_FORM_VALIDATION_ERROR_MESSAGE, locale))
                .withValue(OTP_FORM_VALUE)
                .build();

        PaymentFormInputFieldCheckbox acceptCondition = PaymentFormInputFieldCheckbox
                .PaymentFormFieldCheckboxBuilder
                .aPaymentFormFieldCheckbox()
                .withRequired(true)
                .withLabel(i18n.getMessage(OTP_FORM_CHECKBOX_ACCEPT_CONDITION, locale))
                .withRequiredErrorMessage(i18n.getMessage(ACCEPT_CONDITION_REQUIRED_ERROR_MESSAGE, locale))
                .withKey(ACCEPT_CONDITION_KEY)
                .withPrechecked(ACCEPT_CONDITION_PRECHECKED)
                .withSecured(ACCEPT_CONDITION_SECURED)
                .build();

        List<PaymentFormField> customFields = new ArrayList<>();
        customFields.add(otpText);
        customFields.add(setOtpText);
        customFields.add(otpForm);
        customFields.add(acceptCondition);

        return CustomForm
                .builder()
                .withCustomFields(customFields)
                .withDescription(i18n.getMessage(CUSTOMFORM_TEXT_SIGN_DESCRIPTION, locale))
                .withButtonText(i18n.getMessage(CUSTOMFORM_TEXT_SIGN, locale))
                .withDisplayButton(true)
                .build();
    }


    /**
     * Generate the URL to download the mandate document
     *
     * @param env        ConfigEnvironment
     * @param creditorId creditor id
     * @param mandateRum RUM
     * @return URI Syntax Exception
     */
    private static URL getDownloadMandateLinkUrl(ConfigEnvironment env, String creditorId, String mandateRum) {

        URL url = null;

        try {

            String strUrl = ConfigProperties.get(CONFIG_SCHEME, env)
                    + "://"
                    + ConfigProperties.get(CONFIG_HOST, env)
                    + URL_DELIMITER
                    + ConfigProperties.get(CONFIG_PATH_WSMANDATE_MANDATE_PDFTPL)
                    + URL_DELIMITER
                    + creditorId
                    + URL_DELIMITER
                    + mandateRum;

            LOGGER.debug("Mandate download URL : " + strUrl);

            url = new URL(strUrl);

        } catch (MalformedURLException e) {
            LOGGER.error(UNEXPECTED_ERROR_MESSAGE, e);
        }

        return url;

    }

    /**
     * Generate the URL to download the mandate document
     *
     * @return url
     */
    private static URL getResendOtpLinkUrl(ConfigEnvironment env, String creditorId, String mandateRum, String
            transactionId) {

        URL url = null;

        try {

            String query = "creditorId=" + creditorId
                    + "&mandateRum=" + mandateRum
                    + "&transactionId=" + transactionId;

            String strUrl = ConfigProperties.get(CONFIG_SCHEME, env)
                    + "://"
                    + ConfigProperties.get(CONFIG_HOST, env)
                    + URL_DELIMITER
                    + ConfigProperties.get(CONFIG_PATH_WSSIGNATURE_SEND_OTP)
                    + "?"
                    + URLEncoder.encode(query, StandardCharsets.UTF_8.name());

            LOGGER.debug("Mandate download URL : " + strUrl);

            url = new URL(strUrl);

        } catch (UnsupportedEncodingException | MalformedURLException e) {
            LOGGER.error(UNEXPECTED_ERROR_MESSAGE, e);

        }

        return url;

    }


}
