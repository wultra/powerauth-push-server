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

package io.getlime.push.model.request;

import io.getlime.push.model.entity.PushMessageBody;
import io.swagger.annotations.ApiModelProperty;

/**
 * Request object used for creating a campaign.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */


public class CreateCampaignRequest {

    @ApiModelProperty(notes = "ADd asd a as as")
    private Long appId;

    private PushMessageBody message;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public PushMessageBody getMessage() {
        return message;
    }

    public void setMessage(PushMessageBody message) {
        this.message = message;
    }
}
