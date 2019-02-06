package io.getlime.push.validator;

import io.getlime.push.controller.web.model.form.AppCreateForm;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class for application create form.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class AppCreateFormValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return AppCreateForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nullable Object target, @NonNull Errors errors) {
        AppCreateForm appCreateForm = (AppCreateForm) target;
        if (appCreateForm == null) {
            errors.reject("appCreateForm.missing");
            return;
        }
        if (appCreateForm.getAppId() == null) {
            errors.rejectValue("appId", "appCreateForm.appId.missing");
            return;
        }
        if (appCreateForm.getAppId() < 1) {
            errors.rejectValue("appId", "appCreateForm.appId.negative");
            return;
        }
    }
}
