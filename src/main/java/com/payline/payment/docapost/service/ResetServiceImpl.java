package com.payline.payment.docapost.service;

import com.payline.pmapi.bean.reset.request.ResetRequest;
import com.payline.pmapi.bean.reset.response.ResetResponse;
import com.payline.pmapi.service.ResetService;

public class ResetServiceImpl implements ResetService {

    @Override
    public ResetResponse resetRequest(ResetRequest resetRequest) {
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