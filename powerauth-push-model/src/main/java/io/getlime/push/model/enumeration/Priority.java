/*
 * Copyright 2021 Wultra s.r.o.
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

/**
 * Enum representing the priority of push notifications.
 * 
 * @author Petr Dvorak, petr@wultra.com
 */
public enum Priority {
    /**
     * Default push message priority. Maps to "DEFAULT" on Android and "CONSERVE_POWER" on iOS.
     */
    DEFAULT,

    /**
     * High push message priority. Maps to "HIGH" on Android and "IMMEDIATE" on iOS.
     */
    HIGH
}
