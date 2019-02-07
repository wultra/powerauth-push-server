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
