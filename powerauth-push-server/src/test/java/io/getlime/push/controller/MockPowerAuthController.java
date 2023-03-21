/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.push.controller;

import com.wultra.security.powerauth.client.v3.*;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.push.client.PushServerTestClientFactory;
import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.model.exception.CryptoProviderException;
import io.getlime.security.powerauth.crypto.lib.util.KeyConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Mock controller for PowerAuth services.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("powerauth-java-server/rest/v3")
@Profile("test")
public class MockPowerAuthController {

    private static final Logger logger = LoggerFactory.getLogger(MockPowerAuthController.class);

    private final Set<String> blockedActivations = new HashSet<>();

    @PostMapping("application/list")
    public ObjectResponse<GetApplicationListResponse> getApplicationList() {
        final GetApplicationListResponse response = new GetApplicationListResponse();
        final GetApplicationListResponse.Applications app = new GetApplicationListResponse.Applications();
        app.setApplicationId(PushServerTestClientFactory.TEST_APPLICATION_NAME);
        response.getApplications().add(app);
        return new ObjectResponse<>(response);
    }

    @PostMapping("application/create")
    public ObjectResponse<CreateApplicationResponse> createApplication() {
        final CreateApplicationResponse response = new CreateApplicationResponse();
        return new ObjectResponse<>(response);
    }

    @PostMapping("application/detail")
    public ObjectResponse<GetApplicationDetailResponse> getApplicationDetail() {
        final GetApplicationDetailResponse response = new GetApplicationDetailResponse();
        response.setApplicationId(PushServerTestClientFactory.TEST_APPLICATION_NAME);
        final GetApplicationDetailResponse.Versions version = new GetApplicationDetailResponse.Versions();
        version.setApplicationVersionId(PushServerTestClientFactory.TEST_APPLICATION_VERSION);
        version.setSupported(true);
        version.setApplicationKey("key");
        version.setApplicationSecret("secret");
        response.getVersions().add(version);
        try {
            final KeyPair keyPair = new KeyGenerator().generateKeyPair();
            final KeyConvertor keyConvertor = new KeyConvertor();
            response.setMasterPublicKey(Base64.getEncoder().encodeToString(keyConvertor.convertPublicKeyToBytes(keyPair.getPublic())));
        } catch (CryptoProviderException e) {
            // Exception cannot occur when cryptography provider is set correctly
            logger.error(e.getMessage(), e);
        }
        return new ObjectResponse<>(response);
    }

    @PostMapping("activation/status")
    public ObjectResponse<GetActivationStatusResponse> getActivationStatus(@RequestBody ObjectRequest<GetActivationStatusRequest> request) {
        final GetActivationStatusResponse response = new GetActivationStatusResponse();
        response.setActivationId(request.getRequestObject().getActivationId());
        if (blockedActivations.contains(request.getRequestObject().getActivationId())) {
            response.setActivationStatus(ActivationStatus.BLOCKED);
        } else {
            response.setActivationStatus(ActivationStatus.ACTIVE);
        }
        return new ObjectResponse<>(response);
    }

    @PostMapping("activation/init")
    public ObjectResponse<InitActivationResponse> initActivation(@RequestBody ObjectRequest<InitActivationRequest> request) {
        final InitActivationResponse response = new InitActivationResponse();
        response.setActivationId(UUID.randomUUID().toString());
        response.setActivationCode("1111-2222-3333-4444");
        return new ObjectResponse<>(response);
    }

    @PostMapping("activation/prepare")
    public ObjectResponse<PrepareActivationResponse> prepareActivation(@RequestBody ObjectRequest<GetActivationStatusRequest> request) {
        final PrepareActivationResponse response = new PrepareActivationResponse();
        response.setActivationId(""); // enough to pass the checks
        response.setActivationStatus(ActivationStatus.PENDING_COMMIT);
        response.setUserId(PushServerTestClientFactory.TEST_USER_ID);
        response.setApplicationId(PushServerTestClientFactory.TEST_APPLICATION_NAME);
        return new ObjectResponse<>(response);
    }

    @PostMapping("activation/commit")
    public ObjectResponse<CommitActivationResponse> commitActivation(@RequestBody ObjectRequest<CommitActivationRequest> request) {
        final CommitActivationResponse response = new CommitActivationResponse();
        response.setActivationId(request.getRequestObject().getActivationId());
        response.setActivated(true);
        return new ObjectResponse<>(response);
    }

    @PostMapping("activation/block")
    public ObjectResponse<BlockActivationResponse> blockActivation(@RequestBody ObjectRequest<BlockActivationRequest> request) {
        final BlockActivationResponse response = new BlockActivationResponse();
        response.setActivationId(request.getRequestObject().getActivationId());
        blockedActivations.add(request.getRequestObject().getActivationId());
        response.setActivationStatus(ActivationStatus.BLOCKED);
        return new ObjectResponse<>(response);
    }

    @PostMapping("activation/unblock")
    public ObjectResponse<UnblockActivationResponse> unblockActivation(@RequestBody ObjectRequest<UnblockActivationRequest> request) {
        final UnblockActivationResponse response = new UnblockActivationResponse();
        response.setActivationId(request.getRequestObject().getActivationId());
        blockedActivations.remove(request.getRequestObject().getActivationId());
        response.setActivationStatus(ActivationStatus.ACTIVE);
        return new ObjectResponse<>(response);
    }

}
