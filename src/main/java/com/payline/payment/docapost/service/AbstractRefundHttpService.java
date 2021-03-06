package com.payline.payment.docapost.service;

import com.payline.payment.docapost.exception.InvalidRequestException;
import com.payline.payment.docapost.utils.ActionRequestResponse;
import com.payline.payment.docapost.utils.http.DocapostHttpClient;
import com.payline.payment.docapost.utils.http.StringResponse;
import com.payline.payment.docapost.utils.type.WSRequestResultEnum;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This abstract service handles the common issues encountered when sending, receiving and processing a {@link RefundRequest} (or subclass)
 * It delegates the specific parts to the classes that will extends it, through the abstract methods.
 * This way, most of the exception handling can be done here, once.
 */
public abstract class AbstractRefundHttpService<T extends RefundRequest> {

    private static final Logger logger = LogManager.getLogger(AbstractRefundHttpService.class);

    private static final String HTTP_NULL_RESPONSE_ERROR_MESSAGE = "The HTTP response or its body is null and should not be";
    private static final String DEFAULT_ERROR_CODE = "no code transmitted";

    protected DocapostHttpClient httpClient;

    protected AbstractRefundHttpService() {
        this.httpClient = DocapostHttpClient.getInstance();

    }

    /**
     * Builds the request, sends it through HTTP using the httpClient and recovers the response.
     *
     * @param refundRequest The input request provided by Payline
     * @return The {@link StringResponse} from the HTTP call
     * @throws IOException             Can be thrown while sending the HTTP request
     * @throws InvalidRequestException Thrown if the input request in not valid
     */
    public abstract StringResponse createSendRequest(T refundRequest) throws IOException, InvalidRequestException, URISyntaxException;

    /**
     * Process the success response from the HTTP call.
     * It focuses on business aspect of the processing : the technical part has already been done by {@link #processRequest(RefundRequest)} .
     *
     * @param response The {@link StringResponse} from the HTTP call, which HTTP code is 200 and which body is not null.
     * @return The {@link RefundResponse}
     */
    public abstract RefundResponse processResponseSuccess(StringResponse response);

    /**
     * Process the failure response from the HTTP call.
     * It focuses on business aspect of the processing : the technical part has already been done by {@link #processRequest(RefundRequest)} .
     *
     * @param response The {@link StringResponse} from the HTTP call, which HTTP code is not 200 and which body is not null.
     * @return The {@link RefundResponse}
     */
    public abstract RefundResponse processResponseFailure(StringResponse response);

    /**
     * Process a {@link RefundRequest} (or subclass), handling all the generic error cases
     *
     * @param refundRequest The input request from Payline
     * @return The corresponding {@link RefundResponse}
     */
    protected RefundResponse processRequest(T refundRequest) {
        try {

            // Mandate the child class to create and send the request (which is specific to each implementation)
            StringResponse response = this.createSendRequest(refundRequest);

            if (response == null) {

                logger.debug("SctOrderCreateRequest StringResponse is null !");
                logger.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);
                return buildRefundResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
            }

            logger.debug("SctOrderCreateRequest StringResponse : {}", response.toString());

            switch (ActionRequestResponse.checkResponse(response)) {
                case OK_200:
                    // Mandate the child class to process the request when it's OK (which is specific to each implementation)
                    return this.processResponseSuccess(response);
                case OTHER_CODE:
                    logger.error("An HTTP error occurred while sending the request: " + response.getMessage());
                    if (response.getContent() == null || response.getContent().isEmpty()) {
                        return buildRefundResponseFailure(Integer.toString(response.getCode()), FailureCause.COMMUNICATION_ERROR);
                    } else {
                        return this.processResponseFailure(response);
                    }
                default:
                    logger.error("The HTTP response or its body is null and should not be");
                    return buildRefundResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
            }

        } catch (InvalidRequestException e) {
            logger.error("The input payment request is invalid", e);
            return buildRefundResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INVALID_DATA);
        } catch (IOException e) {
            logger.error("An IOException occurred while sending the HTTP request or receiving the response", e);
            return buildRefundResponseFailure(DEFAULT_ERROR_CODE, FailureCause.COMMUNICATION_ERROR);
        } catch (Exception e) {
            logger.error("An unexpected error occurred", e);
            return buildRefundResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }

    }

    /**
     * Utility method to instantiate {@link RefundResponseSuccess} objects, using the class' builder.
     *
     * @param statusCode            The status code
     * @param partnerTransactionId  The partner transaction id
     * @return The instantiated object
     */
    protected RefundResponseSuccess buildRefundResponseSuccess(String statusCode, String partnerTransactionId) {
        return RefundResponseSuccess
                .RefundResponseSuccessBuilder
                .aRefundResponseSuccess()
                .withStatusCode(statusCode)
                .withPartnerTransactionId(partnerTransactionId)
                .build();
    }

    /**
     * Utility method to instantiate {@link RefundResponseFailure
     * } objects, using the class' builder.
     *
     * @param wsRequestResult The enum representing the error code and the failure cause
     * @return The instantiated object
     */
    protected RefundResponseFailure buildPaymentResponseFailure(WSRequestResultEnum wsRequestResult) {
        return RefundResponseFailure.RefundResponseFailureBuilder.aRefundResponseFailure()
                .withFailureCause(wsRequestResult.getPaylineFailureCause())
                .withErrorCode(wsRequestResult.getDocapostErrorCode())
                .build();
    }

    /**
     * Utility method to instantiate {@link RefundResponseFailure} objects, using the class' builder.
     *
     * @param errorCode    The error code
     * @param failureCause The failure cause
     * @return The instantiated object
     */
    protected RefundResponseFailure buildRefundResponseFailure(String errorCode, FailureCause failureCause) {
        return RefundResponseFailure.RefundResponseFailureBuilder.aRefundResponseFailure()
                .withFailureCause(failureCause)
                .withErrorCode(errorCode)
                .build();
    }

}
