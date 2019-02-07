/*
 * Copyright 2019 Lime - Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
