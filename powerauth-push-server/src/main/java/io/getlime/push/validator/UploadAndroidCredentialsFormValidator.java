package io.getlime.push.validator;

import io.getlime.push.controller.web.model.form.UploadAndroidCredentialsForm;
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
public class UploadAndroidCredentialsFormValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UploadAndroidCredentialsForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nullable Object target, @NonNull Errors errors) {
        UploadAndroidCredentialsForm uploadAndroidCredentialsForm = (UploadAndroidCredentialsForm) target;
        if (uploadAndroidCredentialsForm == null) {
            errors.reject("uploadAndroidCredentialsForm.missing");
            return;
        }
        if (uploadAndroidCredentialsForm.getProjectId() == null) {
            errors.rejectValue("projectId", "uploadAndroidCredentialsForm.projectId.missing");
        }
        if (uploadAndroidCredentialsForm.getProjectId().length() < 6) {
            errors.rejectValue("projectId", "uploadAndroidCredentialsForm.projectId.tooShort");
        }
        if (uploadAndroidCredentialsForm.getPrivateKey() == null || uploadAndroidCredentialsForm.getPrivateKey().isEmpty()) {
            errors.rejectValue("privateKey", "uploadAndroidCredentialsForm.privateKey.missing");
        }
    }
}
