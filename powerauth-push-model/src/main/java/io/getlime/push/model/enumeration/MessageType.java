/*
 * Copyright 2023 Wultra s.r.o.
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

package io.getlime.push.model.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing type of the message.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public enum MessageType {

    /**
     * Plain text message type.
     */
    TEXT("text"),
    /**
     * HTML message type.
     */
    HTML("html");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    /**
     * Get value.
     * @return Value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Get type from lowercase string.
     * @param value Lowercase string with the expected enum value.
     * @return Enum value.
     */
    @JsonCreator
    public static MessageType fromLowerCaseString(String value) {
        return MessageType.valueOf(value.toUpperCase());
    }

    /**
     * Converted enum name to lowercase string.
     * @return Lowercased enum name.
     */
    @JsonValue
    public String toLowerCaseString() {
        return toString().toLowerCase();
    }

}
