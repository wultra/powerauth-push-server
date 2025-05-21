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
package io.getlime.push.model.entity;

/**
 * Extra attributes of the push message.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class PushMessageAttributes {

    private Boolean silent = false;
    private Boolean personal = false;

    /**
     * Specifies if the message should be silent - it does not play any sound and trigger any displayable message.
     * Default value if false.
     * @return True if the message should be silent, false otherwise.  Default value if false.
     */
    public Boolean getSilent() {
        return silent;
    }

    /**
     * Set if the message should be silent - it does not play any sound and trigger any displayable message.
     * Default value if false.
     * @param silent True if the message should be silent, false otherwise. Default value if false.
     */
    public void setSilent(Boolean silent) {
        this.silent = silent;
    }

    /**
     * Specifies if the message is personal. Personal messages are delivered to provided recipient only in case
     * associated PowerAuth 2.0 activations are in active state. They are not delivered in case of any other
     * activation states. Default value if false.
     * @return True if the message is personal, false otherwise. Default value if false.
     */
    public Boolean getPersonal() {
        return personal;
    }

    /**
     * Set if the message is personal. Personal messages are delivered to provided recipient only in case
     * associated PowerAuth 2.0 activations are in active state. They are not delivered in case of any other
     * activation states.  Default value if false.
     * @param personal True if the message is personal, false otherwise.  Default value if false.
     */
    public void setPersonal(Boolean personal) {
        this.personal = personal;
    }

}
