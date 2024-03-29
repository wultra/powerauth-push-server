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

package io.getlime.push.model.response;

import io.getlime.push.model.entity.PushMessageBody;
import lombok.Getter;
import lombok.Setter;

/**
 * Response object used for getting a campaign
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Getter
@Setter
public class CampaignResponse {

    /**
     * Campaign ID.
     */
    private Long id;

    /**
     * Application ID.
     */
    private String appId;

    /**
     * If the message is sent.
     */
    private boolean sent;

    /**
     * Push message body.
     */
    private PushMessageBody message;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CampaignResponse that = (CampaignResponse) o;

        if (sent != that.sent) return false;
        if (!id.equals(that.id)) return false;
        if (!appId.equals(that.appId)) return false;
        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
