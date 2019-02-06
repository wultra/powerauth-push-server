package io.getlime.push.validator;

import io.getlime.push.controller.web.model.form.RemoveCredentialsForm;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class for remove credentials form.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class RemoveCredentialsFormValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return RemoveCredentialsForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nullable Object target, @NonNull Errors errors) {
        RemoveCredentialsForm removeCredentialsForm = (RemoveCredentialsForm) target;
        if (removeCredentialsForm == null) {
            errors.reject("removeCredentialsForm.missing");
            return;
        }
        if (removeCredentialsForm.getId() == null) {
            errors.rejectValue("id", "removeCredentialsForm.id.missing");
            return;
        }
        if (removeCredentialsForm.getId() < 1) {
            errors.rejectValue("id", "removeCredentialsForm.id.negative");
            return;
        }
    }
}
