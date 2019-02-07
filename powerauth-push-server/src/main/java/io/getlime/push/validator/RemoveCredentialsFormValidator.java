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
