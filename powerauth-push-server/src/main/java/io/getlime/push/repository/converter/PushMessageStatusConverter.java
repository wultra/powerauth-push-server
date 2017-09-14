/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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

package io.getlime.push.repository.converter;

import io.getlime.push.repository.model.PushMessage;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converts push sending status to database column value and vice versa.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Component
@Converter
public class PushMessageStatusConverter implements AttributeConverter<PushMessage.Status, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PushMessage.Status status) {
        return status.getStatus();
    }

    @Override
    public PushMessage.Status convertToEntityAttribute(Integer integer) {
        switch (integer) {
            case 0:
                return PushMessage.Status.PENDING;
            case 1:
                return PushMessage.Status.SENT;
            default:
                return PushMessage.Status.FAILED;
        }
    }
}
