package io.getlime.push.model.validator;

import io.getlime.push.model.request.UpdateDeviceStatusRequest;

public class UpdateDeviceStatusRequestValidator {
    public static String validate(UpdateDeviceStatusRequest request) {
        if (request == null) {
            return "Empty request body";
        } else if (request.getActivationId() == null) {
            return "Empty activation ID";
        }
        return null;
    }
}
