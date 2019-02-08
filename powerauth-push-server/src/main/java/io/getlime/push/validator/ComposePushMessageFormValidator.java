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
