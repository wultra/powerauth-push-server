/*
 * Copyright 2024 Wultra s.r.o.
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
 *
 */

package io.getlime.push.controller.rest;

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.enumeration.ActivationStatus;
import com.wultra.security.powerauth.client.model.response.GetActivationStatusResponse;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.enumeration.ApnsEnvironment;
import io.getlime.push.model.enumeration.MobilePlatform;
import io.getlime.push.model.request.CreateDeviceRequest;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.PushDeviceRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.repository.model.PushDeviceRegistrationEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test for {@link PushDeviceController}.
 *
 * @author Jan Pesek, jan.pesek@wultra.com
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PushDeviceControllerTest {

    @Autowired
    private PushDeviceController tested;

    @Autowired
    private PushDeviceRepository deviceRepository;

    @Autowired
    private AppCredentialsRepository appCredentialsRepository;

    @MockBean
    private PowerAuthClient powerAuthClient;

    @Test
    void testCreateDevice() throws Exception {
        createAppCredentials("my_app");
        when(powerAuthClient.getActivationStatus("a1"))
                .thenReturn(createActivationStatusResponse("a1"));


        final CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId("my_app");
        request.setActivationId("a1");
        request.setToken("t1");
        request.setPlatform(MobilePlatform.APNS);
        request.setEnvironment(ApnsEnvironment.PRODUCTION);

        tested.createDevice(new ObjectRequest<>(request));

        final List<PushDeviceRegistrationEntity> entities = deviceRepository.findByActivationId("a1");
        assertEquals(1, entities.size());
        assertEquals("a1", entities.get(0).getActivationId());
        assertEquals("t1", entities.get(0).getPushToken());
        assertEquals(ApnsEnvironment.PRODUCTION.getKey(), entities.get(0).getEnvironment());
    }

    @Test
    void testCreateDevice_missingApplication() throws Exception {
        createAppCredentials("my_app");
        when(powerAuthClient.getActivationStatus("a2"))
                .thenReturn(createActivationStatusResponse("a2"));

        final CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId("non_existent");
        request.setActivationId("a2");
        request.setToken("t0");
        request.setPlatform(MobilePlatform.ANDROID);

        assertThrows(PushServerException.class, () -> tested.createDevice(new ObjectRequest<>(request)));

        final List<PushDeviceRegistrationEntity> entities = deviceRepository.findByActivationId("a2");
        assertEquals(0, entities.size());
    }

    @Test
    void testCreateDevice_parallel() throws Exception {
        createAppCredentials("my_app");
        when(powerAuthClient.getActivationStatus("a3"))
                .thenReturn(createActivationStatusResponse("a3"));

        final int nThreads = 5;
        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        final List<Callable<Response>> callableTasks = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            callableTasks.add(() -> {
                final CreateDeviceRequest request = new CreateDeviceRequest();
                request.setAppId("my_app");
                request.setActivationId("a3");
                request.setToken("token");
                request.setPlatform(MobilePlatform.IOS);
                return tested.createDevice(new ObjectRequest<>(request));
            });
        }

        executorService.invokeAll(callableTasks).forEach(future -> assertDoesNotThrow(() -> future.get()));
        executorService.shutdown();

        final List<PushDeviceRegistrationEntity> entities = deviceRepository.findByActivationId("a3");
        assertEquals(1, entities.size());
    }

    private GetActivationStatusResponse createActivationStatusResponse(final String activationId) {
        final GetActivationStatusResponse activationResponse = new GetActivationStatusResponse();
        activationResponse.setActivationId(activationId);
        activationResponse.setActivationStatus(ActivationStatus.ACTIVE);
        activationResponse.setUserId("joe");
        return activationResponse;
    }

    private AppCredentialsEntity createAppCredentials(String appName) {
        final AppCredentialsEntity credentials = new AppCredentialsEntity();
        credentials.setAppId(appName);
        return appCredentialsRepository.save(credentials);
    }

}
