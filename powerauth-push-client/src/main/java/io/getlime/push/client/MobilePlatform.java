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

package io.getlime.push.client;

/**
 * Enum representing mobile platforms.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public enum MobilePlatform {

    /**
     * iOS Platform.
     */
    iOS,

    /**
     * Android Platform.
     */
    Android;

    /**
     * Converter convenience method for obtaining String from enum.
     * @return String representation of the enum.
     */
    public String value() {
        if (this.equals(MobilePlatform.iOS)) {
            return "ios";
        } else if (this.equals(MobilePlatform.Android)) {
            return "android";
        } else {
            return "android"; // guess android by default
        }
    }

}
