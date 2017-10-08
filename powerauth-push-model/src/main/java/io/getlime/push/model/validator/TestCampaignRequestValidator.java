package io.getlime.push.model.validator;

import io.getlime.push.model.request.TestCampaignRequest;

public class TestCampaignRequestValidator {
    public static String validate(TestCampaignRequest request) {
        if (request == null) {
            return "Empty request";
        } else if (request.getUserId() == null) {
            return "Empty user ID";
        }
        return null;
    }
}
