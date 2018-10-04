package com.payline.payment.docapost.service;

import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.service.RefundService;

public class RefundServiceImpl implements RefundService {

    @Override
    public RefundResponse refundRequest(RefundRequest refundRequest) {
        return null;
    }

    @Override
    public boolean canMultiple() {
        return false;
    }

    @Override
    public boolean canPartial() {
        return false;
    }

}