package io.getlime.push.model.validator;

import io.getlime.push.model.request.DeleteDeviceRequest;

public class DeleteDeviceRequestValidator {
    public static String validate(DeleteDeviceRequest request) {
        if (request == null) {
            return "Empty request";
        } else if (request.getAppId() == null) {
            return "Empty app ID";
        } else if (request.getToken() == null) {
            return "Empty token";
        }
        return null;
    }
}
