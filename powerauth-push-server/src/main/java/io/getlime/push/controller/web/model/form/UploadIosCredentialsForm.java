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

package io.getlime.push.controller.web.model.form;

import org.springframework.web.multipart.MultipartFile;

/**
 * Form sent when uploading iOS / APNs credentials for the application.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class UploadIosCredentialsForm {

    private String bundle;

    private MultipartFile privateKey;

    private String teamId;

    private String keyId;

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public MultipartFile getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(MultipartFile privateKey) {
        this.privateKey = privateKey;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    @Override
    public String toString() {
        return "UploadIosCredentialsForm{" +
                "bundle='" + bundle + '\'' +
                ", privateKey=******" + '\'' +
                ", teamId='" + teamId + '\'' +
                ", keyId='" + keyId + '\'' +
                '}';
    }
}
