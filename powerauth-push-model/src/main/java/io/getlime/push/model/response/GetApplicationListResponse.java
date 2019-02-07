/*
 * Copyright 2018 Wultra s.r.o.
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

import io.getlime.push.model.entity.PushServerApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Get application list response.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class GetApplicationListResponse {

    private List<PushServerApplication> applicationList = new ArrayList<>();

    /**
     * Default constructor.
     */
    public GetApplicationListResponse() {
    }

    /**
     * Constructor with application list.
     * @param applicationList Application list.
     */
    public GetApplicationListResponse(List<PushServerApplication> applicationList) {
        this.applicationList = applicationList;
    }

    /**
     * Get application list.
     * @return Application list.
     */
    public List<PushServerApplication> getApplicationList() {
        return applicationList;
    }

    /**
     * Set application list.
     * @param applicationList Application list.
     */
    public void setApplicationList(List<PushServerApplication> applicationList) {
        this.applicationList = applicationList;
    }
}
