package io.getlime.push.validator;

import io.getlime.push.controller.web.model.form.ComposePushMessageForm;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class for compose push message form.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class ComposePushMessageFormValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return ComposePushMessageForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nullable Object target, @NonNull Errors errors) {
        ComposePushMessageForm composePushMessageForm = (ComposePushMessageForm) target;
        if (composePushMessageForm == null) {
            errors.reject("composePushMessageForm.missing");
            return;
        }
        if (composePushMessageForm.getAppId() == null) {
            errors.rejectValue("appId", "composePushMessageForm.appId.missing");
        }
        if (composePushMessageForm.getAppId() < 1) {
            errors.rejectValue("appId", "composePushMessageForm.appId.negative");
        }
        if (composePushMessageForm.getUserId() == null) {
            errors.rejectValue("userId", "composePushMessageForm.userId.missing");
        }
        if (composePushMessageForm.getUserId().length() < 2) {
            errors.rejectValue("userId", "composePushMessageForm.userId.tooShort");
        }
        if (composePushMessageForm.getTitle() == null) {
            errors.rejectValue("title", "composePushMessageForm.title.missing");
        }
        if (composePushMessageForm.getTitle().length() < 2) {
            errors.rejectValue("title", "composePushMessageForm.title.tooShort");
        }
        if (composePushMessageForm.getBody() == null) {
            errors.rejectValue("body", "composePushMessageForm.body.missing");
        }
        if (composePushMessageForm.getBody().length() < 2) {
            errors.rejectValue("body", "composePushMessageForm.body.tooShort");
        }
    }
}
