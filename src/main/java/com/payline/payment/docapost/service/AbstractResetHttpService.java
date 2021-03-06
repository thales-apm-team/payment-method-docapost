package com.payline.payment.docapost.service;

import com.payline.payment.docapost.exception.InvalidRequestException;
import com.payline.payment.docapost.utils.ActionRequestResponse;
import com.payline.payment.docapost.utils.http.DocapostHttpClient;
import com.payline.payment.docapost.utils.http.StringResponse;
import com.payline.payment.docapost.utils.type.WSRequestResultEnum;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.reset.request.ResetRequest;
import com.payline.pmapi.bean.reset.response.ResetResponse;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseFailure;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This abstract service handles the common issues encountered when sending, receiving and processing a {@link ResetRequest} (or subclass)
 * It delegates the specific parts to the classes that will extends it, through the abstract methods.
 * This way, most of the exception handling can be done here, once.
 */
public abstract class AbstractResetHttpService<T extends ResetRequest> {

    private static final Logger logger = LogManager.getLogger(AbstractResetHttpService.class);

    private static final String HTTP_NULL_RESPONSE_ERROR_MESSAGE = "The HTTP response or its body is null and should not be";
    private static final String DEFAULT_ERROR_CODE = "no code transmitted";

    protected DocapostHttpClient httpClient;

    protected AbstractResetHttpService() {
        this.httpClient = DocapostHttpClient.getInstance();
    }

    /**
     * Builds the request, sends it through HTTP using the httpClient and recovers the response.
     *
     * @param resetRequest The input request provided by Payline
     * @return The {@link StringResponse} from the HTTP call
     * @throws IOException             Can be thrown while sending the HTTP request
     * @throws InvalidRequestException Thrown if the input request in not valid
     */
    public abstract StringResponse createSendRequest(T resetRequest) throws IOException, InvalidRequestException, URISyntaxException;

    /**
     * Process the response from the HTTP call.
     * It focuses on business aspect of the processing : the technical part has already been done by {@link #processRequest(ResetRequest)} .
     *
     * @param response The {@link StringResponse} from the HTTP call, which HTTP code is 200 and which body is not null.
     * @return The {@link ResetResponse}
     */
    public abstract ResetResponse processResponseSuccess(StringResponse response);


    /**
     * Process the response from the HTTP call.
     * It focuses on business aspect of the processing : the technical part has already been done by {@link #processRequest(ResetRequest)} .
     *
     * @param response The {@link StringResponse} from the HTTP call, which HTTP code is not 200 and which body is not null.
     * @return The {@link ResetResponse}
     */
    public abstract ResetResponse processResponseFailure(StringResponse response);

    /**
     * Process a {@link ResetRequest} (or subclass), handling all the generic error cases
     *
     * @param resetRequest The input request from Payline
     * @return The corresponding {@link ResetRequest}
     */
    protected ResetResponse processRequest(T resetRequest) {
        try {

            // Mandate the child class to create and send the request (which is specific to each implementation)
            StringResponse response = this.createSendRequest(resetRequest);

            if (response == null) {

                logger.debug("SddOrderCancelRequest StringResponse is null !");
                logger.error(HTTP_NULL_RESPONSE_ERROR_MESSAGE);
                return buildResetResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
            }

            logger.debug("SddOrderCancelRequest StringResponse : {}", response.toString());

            switch (ActionRequestResponse.checkResponse(response)) {
                case OK_200:
                    // Mandate the child class to process the request when it's OK (which is specific to each implementation)
                    return this.processResponseSuccess(response);
                case OTHER_CODE:
                    logger.error("An HTTP error occurred while sending the request: " + response.getMessage());
                    if (response.getContent() == null || response.getContent().isEmpty()) {
                        return buildResetResponseFailure(Integer.toString(response.getCode()), FailureCause.COMMUNICATION_ERROR);
                    } else {
                        return this.processResponseFailure(response);
                    }
                default:
                    logger.error("The HTTP response or its body is null and should not be");
                    return buildResetResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
            }

        } catch (InvalidRequestException e) {
            logger.error("The input payment request is invalid", e);
            return buildResetResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INVALID_DATA);
        } catch (IOException e) {
            logger.error("An IOException occurred while sending the HTTP request or receiving the response", e);
            return buildResetResponseFailure(DEFAULT_ERROR_CODE, FailureCause.COMMUNICATION_ERROR);
        } catch (Exception e) {
            logger.error("An unexpected error occurred", e);
            return buildResetResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }

    }

    /**
     * Utility method to instantiate {@link ResetResponseSuccess} objects, using the class' builder.
     *
     * @param statusCode            The status code
     * @param partnerTransactionId  The partner transaction id
     * @return The instantiated object
     */
    protected ResetResponseSuccess buildResetResponseSuccess(String statusCode, String partnerTransactionId) {
        return ResetResponseSuccess
                .ResetResponseSuccessBuilder
                .aResetResponseSuccess()
                .withStatusCode(statusCode)
                .withPartnerTransactionId(partnerTransactionId)
                .build();
    }

    /**
     * Utility method to instantiate {@link ResetResponseFailure
     * } objects, using the class' builder.
     *
     * @param wsRequestResult The enum representing the error code and the failure cause
     * @return The instantiated object
     */
    protected ResetResponseFailure buildResetResponseFailure(WSRequestResultEnum wsRequestResult) {
        return ResetResponseFailure.ResetResponseFailureBuilder.aResetResponseFailure()
                .withFailureCause(wsRequestResult.getPaylineFailureCause())
                .withErrorCode(wsRequestResult.getDocapostErrorCode())
                .build();
    }

    /**
     * Utility method to instantiate {@link ResetResponseFailure} objects, using the class' builder.
     *
     * @param errorCode    The error code
     * @param failureCause The failure cause
     * @return The instantiated object
     */
    protected ResetResponseFailure buildResetResponseFailure(String errorCode, FailureCause failureCause) {
        return ResetResponseFailure.ResetResponseFailureBuilder.aResetResponseFailure()
                .withFailureCause(failureCause)
                .withErrorCode(errorCode)
                .build();
    }

}
