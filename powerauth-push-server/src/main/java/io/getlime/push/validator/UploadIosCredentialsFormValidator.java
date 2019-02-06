package io.getlime.push.validator;

import io.getlime.push.controller.web.model.form.UploadIosCredentialsForm;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class for upload Android credentials form.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class UploadIosCredentialsFormValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UploadIosCredentialsForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nullable Object target, @NonNull Errors errors) {
        UploadIosCredentialsForm uploadIosCredentialsForm = (UploadIosCredentialsForm) target;
        if (uploadIosCredentialsForm == null) {
            errors.reject("uploadIosCredentialsForm.missing");
            return;
        }
        if (uploadIosCredentialsForm.getBundle() == null) {
            errors.rejectValue("bundle", "uploadIosCredentialsForm.bundle.missing");
        }
        if (uploadIosCredentialsForm.getBundle().length() < 2) {
            errors.rejectValue("bundle", "uploadIosCredentialsForm.bundle.tooShort");
        }
        if (!uploadIosCredentialsForm.getBundle().matches("^[A-Za-z][A-Za-z0-9_-]*(\\.[A-Za-z0-9_-]+)+[0-9A-Za-z_-]$")) {
            errors.rejectValue("bundle", "uploadIosCredentialsForm.bundle.invalid");
        }
        if (uploadIosCredentialsForm.getPrivateKey() == null || uploadIosCredentialsForm.getPrivateKey().isEmpty()) {
            errors.rejectValue("privateKey", "uploadIosCredentialsForm.privateKey.missing");
        }
        if (uploadIosCredentialsForm.getTeamId() == null || uploadIosCredentialsForm.getTeamId().isEmpty()) {
            errors.rejectValue("teamId", "uploadIosCredentialsForm.teamId.missing");
        }
        if (uploadIosCredentialsForm.getKeyId() == null || uploadIosCredentialsForm.getKeyId().isEmpty()) {
            errors.rejectValue("keyId", "uploadIosCredentialsForm.keyId.missing");
        }
    }
}
