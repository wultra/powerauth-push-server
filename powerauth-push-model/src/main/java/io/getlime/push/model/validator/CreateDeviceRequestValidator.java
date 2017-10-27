package io.getlime.push.model.validator;

import io.getlime.push.model.request.CreateDeviceRequest;

public class CreateDeviceRequestValidator {
    public static String validate(CreateDeviceRequest request) {
        if (request == null) {
            return "Empty request";
        } else if (request.getAppId() == null) {
            return "Empty app ID";
        } else if (request.getPlatform() == null) {
            return "Unassigned platform";
        } else if (request.getToken() == null) {
            return "Empty token";
        }
        return null;
    }
}
