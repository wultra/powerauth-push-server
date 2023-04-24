/*
 * Copyright 2016 Wultra s.r.o.
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

import io.getlime.push.repository.model.PushMessageEntity;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts push sending status to database column value and vice versa.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Component
@Converter
public class PushMessageStatusConverter implements AttributeConverter<PushMessageEntity.Status, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PushMessageEntity.Status status) {
        return status.getStatus();
    }

    @Override
    public PushMessageEntity.Status convertToEntityAttribute(Integer integer) {
        return switch (integer) {
            case 0 -> PushMessageEntity.Status.PENDING;
            case 1 -> PushMessageEntity.Status.SENT;
            default -> PushMessageEntity.Status.FAILED;
        };
    }
}
