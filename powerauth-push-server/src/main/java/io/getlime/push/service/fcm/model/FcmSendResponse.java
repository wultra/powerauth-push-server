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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.getlime.push.service.fcm.model.base.FcmResult;

import java.util.List;

/**
 * Class containing response body from FCM server
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
public class FcmSendResponse {
    @JsonProperty(value = "multicast_id")
    private Long multicastId;

    /**
     * number of sent messages
     */
    private int success;

    /**
     * number of rejected messages
     */
    private int failure;

    /**
     * number of sent messages whose tokens have to be updated
     */
    @JsonProperty(value = "canonical_ids")
    private int canonicalIds;

    /**
     * concrete results
     */
    @JsonProperty(value = "results")
    private List<FcmResult> fcmResults;

    public Long getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(Long multicastId) {
        this.multicastId = multicastId;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCanonicalIds() {
        return canonicalIds;
    }

    public void setCanonicalIds(int canonicalIds) {
        this.canonicalIds = canonicalIds;
    }

    public List<FcmResult> getFcmResults() {
        return fcmResults;
    }

    public void setFcmResults(List<FcmResult> fcmResults) {
        this.fcmResults = fcmResults;
    }
}
