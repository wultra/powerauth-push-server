package io.getlime.push.model.validator;

import io.getlime.push.model.entity.PushMessage;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class PushMessageValidator {

    public static String validatePushMessage(PushMessage pushMessage) {
        if (pushMessage.getBody() == null) {
            return "Push message payload must not be null";
        }
        if (pushMessage.getUserId() == null) {
            return "Push message must contain a user";
        }
        return null;
    }

}
