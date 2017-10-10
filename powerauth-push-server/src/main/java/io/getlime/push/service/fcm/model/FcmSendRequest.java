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

package io.getlime.push.service.fcm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.getlime.push.service.fcm.FcmNotification;

import java.util.Map;

/**
 * Class representing a FCM send message request.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FcmSendRequest {

    /**
     * Device token that is going to get a notification.
     */
    private String to;

    /**
     * Attribute of @link io.getlime.push.model.entity.PushMessageBody
     */
    @JsonProperty(value = "collapse_key")
    private String collapseKey;

    /**
     * Attribute of @link io.getlime.push.model.entity.PushMessageBody as extras
     */
    private Map<String, Object> data;

    /**
     * Attributes of @link io.getlime.push.model.entity.PushMessageBody
     */
    private FcmNotification notification;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCollapseKey() {
        return collapseKey;
    }

    public void setCollapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public FcmNotification getNotification() {
        return notification;
    }

    public void setNotification(FcmNotification notification) {
        this.notification = notification;
    }
}
