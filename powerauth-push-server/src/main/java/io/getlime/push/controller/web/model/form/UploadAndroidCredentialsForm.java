/*
 * Copyright 2018 Lime - HighTech Solutions s.r.o.
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

package io.getlime.push.controller.web.model.form;

import org.springframework.web.multipart.MultipartFile;

/**
 * Form sent when uploading FCM credentials for the application.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UploadAndroidCredentialsForm {

    private String projectId;

    private MultipartFile privateKey;

    /**
     * Get Android project ID record.
     * @return Android project ID record.
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Set Android project ID record.
     * @param projectId Android project ID record.
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * Get Android private key.
     * @return Android Private key.
     */
    public MultipartFile getPrivateKey() {
        return privateKey;
    }

    /**
     * Set Android private key.
     * @param privateKey Android private key.
     */
    public void setPrivateKey(MultipartFile privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String toString() {
        return "UploadAndroidCredentialsForm{" +
                "projectId='" + projectId + '\'' +
                ", privateKey=******" + '\'' +
                '}';
    }
}
