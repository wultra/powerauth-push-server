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
package io.getlime.push.model.request;

/**
 * Get application credentials entity detail request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class GetApplicationDetailRequest {

    private Long id;
    private boolean includeIos;
    private boolean includeAndroid;

    /**
     * Default constructor.
     */
    public GetApplicationDetailRequest() {
    }

    /**
     * Constructor with application credentials entity ID.
     * @param id Application credentials entity ID.
     */
    public GetApplicationDetailRequest(Long id) {
        this.id = id;
    }

    /**
     * Constructor with details.
     * @param id Application credentials entity ID.
     * @param includeIos Whether to include iOS details.
     * @param includeAndroid Whether to include Android details.
     */
    public GetApplicationDetailRequest(Long id, boolean includeIos, boolean includeAndroid) {
        this.id = id;
        this.includeIos = includeIos;
        this.includeAndroid = includeAndroid;
    }

    /**
     * Get application credentials entity ID.
     * @return Application credentials entity ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set application credentials entity ID.
     * @param id Application credentials entity ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get whether to include iOS details.
     * @return Whether to include iOS details.
     */
    public boolean getIncludeIos() {
        return includeIos;
    }

    /**
     * Set whether to include iOS details.
     * @param includeIos Whether to include iOS details.
     */
    public void setIncludeIos(boolean includeIos) {
        this.includeIos = includeIos;
    }

    /**
     * Get whether to include Android details.
     * @return Whether to include Android details.
     */
    public boolean getIncludeAndroid() {
        return includeAndroid;
    }

    /**
     * Set whgether to include Android details.
     * @param includeAndroid Whether to include Android details.
     */
    public void setIncludeAndroid(boolean includeAndroid) {
        this.includeAndroid = includeAndroid;
    }
}
