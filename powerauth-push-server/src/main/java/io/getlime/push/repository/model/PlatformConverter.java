/*
 * Copyright 2024 Wultra s.r.o.
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
package io.getlime.push.repository.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hibernate.MappingException;

/**
 * {@link Platform} converter for {@link PushDeviceRegistrationEntity#getPlatform()}.
 *
 * @implNote Using {@code @Enumerated(EnumType.STRING)} is not powerful enough, lower-cases need to be kept because of backward compatibility.
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Converter
class PlatformConverter implements AttributeConverter<Platform, String> {

    @Override
    public String convertToDatabaseColumn(final Platform attribute) {
        return switch (attribute) {
            case IOS -> "ios";
            case ANDROID -> "android";
        };
    }

    @Override
    public Platform convertToEntityAttribute(final String dbData) {
        return switch (dbData) {
            case "ios" -> Platform.IOS;
            case "android" -> Platform.ANDROID;
            default -> throw new MappingException("No mapping for platform: " + dbData);
        };
    }
}
