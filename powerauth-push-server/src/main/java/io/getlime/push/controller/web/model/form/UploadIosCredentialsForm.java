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

package io.getlime.push.controller.web.model.form;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Form sent when uploading iOS / APNs credentials for the application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class UploadIosCredentialsForm {

    @NotNull
    @Size(min = 2)
    @Pattern(flags = { Pattern.Flag.CASE_INSENSITIVE }, regexp = "^[a-z][a-z0-9_-]*(\\.[a-z0-9_-]+)+[0-9a-z_-]$")
    private String bundle;

    @NotNull
    private MultipartFile privateKey;

    @NotNull
    private String teamId;

    @NotNull
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
                ", privateKey=" + privateKey +
                ", teamId='" + teamId + '\'' +
                ", keyId='" + keyId + '\'' +
                '}';
    }
}
