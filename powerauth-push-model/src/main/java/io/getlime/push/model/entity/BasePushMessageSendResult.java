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

package io.getlime.push.model.entity;

import io.getlime.push.model.enumeration.Mode;

/**
 * Base push message sending result.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class BasePushMessageSendResult {

    private final Mode mode;

    /**
     * Default constructor.
     */
    public BasePushMessageSendResult() {
        this.mode = Mode.SYNCHRONOUS;
    }

    /**
     * Primary constructor with mode.
     * @param mode Mode.
     */
    public BasePushMessageSendResult(Mode mode) {
        this.mode = mode;
    }

    /**
     * Get mode.
     * @return Mode.
     */
    public Mode getMode() {
        return mode;
    }

}
