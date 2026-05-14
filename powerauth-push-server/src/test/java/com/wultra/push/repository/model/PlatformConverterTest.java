/*
 * Copyright 2026 Wultra s.r.o.
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
package com.wultra.push.repository.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link PlatformConverter}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
class PlatformConverterTest {

    private final PlatformConverter tested = new PlatformConverter();

    @Test
    void convertToEntityAttribute_unknownValue_throws() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tested.convertToEntityAttribute("xxx"));
        assertEquals("No mapping for platform: xxx", ex.getMessage());
    }

    @Test
    void convertToEntityAttribute_null_returnsNull() {
        assertNull(tested.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_knownValues_areMapped() {
        assertEquals(Platform.IOS, tested.convertToEntityAttribute("ios"));
        assertEquals(Platform.ANDROID, tested.convertToEntityAttribute("android"));
        assertEquals(Platform.HUAWEI, tested.convertToEntityAttribute("huawei"));
        assertEquals(Platform.APNS, tested.convertToEntityAttribute("apns"));
        assertEquals(Platform.FCM, tested.convertToEntityAttribute("fcm"));
        assertEquals(Platform.HMS, tested.convertToEntityAttribute("hms"));
    }

    @Test
    void convertToDatabaseColumn_null_returnsNull() {
        assertNull(tested.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_allEnumValues_areLowerCased() {
        assertEquals("ios", tested.convertToDatabaseColumn(Platform.IOS));
        assertEquals("android", tested.convertToDatabaseColumn(Platform.ANDROID));
        assertEquals("huawei", tested.convertToDatabaseColumn(Platform.HUAWEI));
        assertEquals("apns", tested.convertToDatabaseColumn(Platform.APNS));
        assertEquals("fcm", tested.convertToDatabaseColumn(Platform.FCM));
        assertEquals("hms", tested.convertToDatabaseColumn(Platform.HMS));
    }
}

