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

package io.getlime.push.model.response;

import java.util.Date;

/**
 * Response object for a system status call.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class SystemStatusResponse {

    private String applicationName;
    private String applicationDisplayName;
    private String applicationEnvironment;
    private Date timestamp;

    /**
     * Get the application name.
     * @return Application name.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Set the application name.
     * @param applicationName Application name.
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Get the application display name.
     * @return Application display name.
     */
    public String getApplicationDisplayName() {
        return applicationDisplayName;
    }

    /**
     * Set the application display name.
     * @param applicationDisplayName Application display name.
     */
    public void setApplicationDisplayName(String applicationDisplayName) {
        this.applicationDisplayName = applicationDisplayName;
    }

    /**
     * Get application environment name.
     * @return Environment name.
     */
    public String getApplicationEnvironment() {
        return applicationEnvironment;
    }

    /**
     * Set application environment name.
     * @param applicationEnvironment Environment name.
     */
    public void setApplicationEnvironment(String applicationEnvironment) {
        this.applicationEnvironment = applicationEnvironment;
    }

    /**
     * Get current timestamp.
     * @return Timestamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Set current timestamp.
     * @param timestamp Timestamp.
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
