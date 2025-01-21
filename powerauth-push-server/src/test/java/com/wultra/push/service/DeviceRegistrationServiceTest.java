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
 */

package com.wultra.push.service;

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.enumeration.ActivationStatus;
import com.wultra.security.powerauth.client.model.response.GetActivationStatusResponse;
import com.wultra.push.errorhandling.exceptions.PushServerException;
import com.wultra.push.model.enumeration.MobilePlatform;
import com.wultra.push.model.request.CreateDeviceForActivationsRequest;
import com.wultra.push.model.request.CreateDeviceRequest;
import com.wultra.push.model.request.UpdateDeviceStatusRequest;
import com.wultra.push.repository.AppCredentialsRepository;
import com.wultra.push.repository.PushDeviceRepository;
import com.wultra.push.repository.model.AppCredentialsEntity;
import com.wultra.push.repository.model.Platform;
import com.wultra.push.repository.model.PushDeviceRegistrationEntity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link DeviceRegistrationService}
 *
 * @author Jan Pesek, jan.pesek@wultra.com
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeviceRegistrationServiceTest {

    private static final String APP_NAME = "my_app";

    @Autowired
    private DeviceRegistrationService tested;

    @Autowired
    private PushDeviceRepository deviceRepository;

    @Autowired
    private AppCredentialsRepository appCredentialsRepository;

    @MockBean
    private PowerAuthClient powerAuthClient;

    @Test
    void testCreateOrUpdateDevice_createNew() throws Exception {
        final AppCredentialsEntity credentials = createAppCredentials(APP_NAME);
        when(powerAuthClient.getActivationStatus("a1"))
                .thenReturn(createActivationStatusResponse("a1"));

        final CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId(APP_NAME);
        request.setActivationId("a1");
        request.setToken("t1");
        request.setPlatform(MobilePlatform.FCM);

        tested.createOrUpdateDevice(request, credentials);

        assertRegistrationExists("a1", "t1");
    }

    @Test
    @DirtiesContext
    void testCreateDevice_parallel() throws Exception {
        when(powerAuthClient.getActivationStatus("a1"))
                .thenReturn(createActivationStatusResponse("a1"));

        final int nThreads = 5;
        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        final List<Callable<Void>> callableTasks = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            callableTasks.add(() -> {
                final AppCredentialsEntity credentials = createAppCredentials(APP_NAME);

                final CreateDeviceRequest request = new CreateDeviceRequest();
                request.setAppId(APP_NAME);
                request.setActivationId("a1");
                request.setToken("t1");
                request.setPlatform(MobilePlatform.APNS);
                tested.createOrUpdateDevice(request, credentials);
                return null;
            });
        }

        executorService.invokeAll(callableTasks).forEach(future -> assertDoesNotThrow(() -> future.get()));
        executorService.shutdown();

        assertRegistrationExists("a1", "t1");
    }

    @Test
    @Sql
    void testCreateOrUpdateDevice_multipleRecords() throws Exception {
        final AppCredentialsEntity credentials = appCredentialsRepository.findById(1L).get();
        when(powerAuthClient.getActivationStatus("a1"))
                .thenReturn(createActivationStatusResponse("a1"));

        final CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId(APP_NAME);
        request.setActivationId("a1");
        request.setToken("t1");
        request.setPlatform(MobilePlatform.FCM);

        final PushServerException ex = assertThrows(PushServerException.class,
                () -> tested.createOrUpdateDevice(request, credentials));
        assertEquals("Multiple device registrations found for push token. Use the /push/device/create/multi endpoint for this scenario.", ex.getMessage());
    }

    @Test
    void testCreateOrUpdateDevices_createNew() throws Exception {
        final AppCredentialsEntity credentials = createAppCredentials(APP_NAME);
        when(powerAuthClient.getActivationStatus("a1"))
                .thenReturn(createActivationStatusResponse("a1"));
        when(powerAuthClient.getActivationStatus("a2"))
                .thenReturn(createActivationStatusResponse("a2"));

        final CreateDeviceForActivationsRequest request = new CreateDeviceForActivationsRequest();
        request.setAppId(APP_NAME);
        request.getActivationIds().addAll(List.of("a1", "a2"));
        request.setToken("t1");
        request.setPlatform(MobilePlatform.FCM);

        tested.createOrUpdateDevices(request, credentials);

        assertRegistrationExists("a1", "t1");
        assertRegistrationExists("a2", "t1");
    }

    @Test
    void testCreateOrUpdateDevices_withDuplicities() throws Exception {
        final AppCredentialsEntity credentials = createAppCredentials(APP_NAME);
        when(powerAuthClient.getActivationStatus("a1"))
                .thenReturn(createActivationStatusResponse("a1"));

        final CreateDeviceForActivationsRequest request = new CreateDeviceForActivationsRequest();
        request.setAppId(APP_NAME);
        request.getActivationIds().addAll(List.of("a1", "a1"));
        request.setToken("t1");
        request.setPlatform(MobilePlatform.FCM);

        tested.createOrUpdateDevices(request, credentials);

        assertRegistrationExists("a1", "t1");
    }

    @Test
    @Sql
    void testCreateOrUpdateDevices_update() throws Exception {
        final AppCredentialsEntity credentials = appCredentialsRepository.findById(1L).get();
        when(powerAuthClient.getActivationStatus("a1"))
                .thenReturn(createActivationStatusResponse("a1"));
        when(powerAuthClient.getActivationStatus("a2"))
                .thenReturn(createActivationStatusResponse("a2"));

        final CreateDeviceForActivationsRequest request = new CreateDeviceForActivationsRequest();
        request.setAppId(APP_NAME);
        request.getActivationIds().addAll(List.of("a1", "a2"));
        request.setToken("t1_new");
        request.setPlatform(MobilePlatform.FCM);

        tested.createOrUpdateDevices(request, credentials);

        assertRegistrationExists("a1", "t1_new");
        assertRegistrationExists("a2", "t1_new");
    }

    @Test
    @Sql
    void testCreateOrUpdateDevices_multipleRecords() throws Exception {
        final AppCredentialsEntity credentials = createAppCredentials(APP_NAME);
        when(powerAuthClient.getActivationStatus("a1"))
                .thenReturn(createActivationStatusResponse("a1"));

        final CreateDeviceForActivationsRequest request = new CreateDeviceForActivationsRequest();
        request.setAppId(APP_NAME);
        request.getActivationIds().add("a1");
        request.setToken("t1");
        request.setPlatform(MobilePlatform.FCM);

        assertRegistrationExists("a_other", "t1");
        assertRegistrationExists("a_different", "t1");

        tested.createOrUpdateDevices(request, credentials);

        assertRegistrationDoesNotExist("a_other");
        assertRegistrationDoesNotExist("a_different");
        assertRegistrationExists("a1", "t1");
    }

    @Test
    void testUpdateStatus_statusInRequest() throws Exception {
        final PushDeviceRegistrationEntity device = new PushDeviceRegistrationEntity();
        device.setActivationId("a1");
        device.setAppCredentials(createAppCredentials(APP_NAME));
        device.setTimestampLastRegistered(new Date());
        device.setPlatform(Platform.APNS);
        device.setPushToken("t1");
        device.setActive(false);
        deviceRepository.save(device);

        final UpdateDeviceStatusRequest request = new UpdateDeviceStatusRequest();
        request.setActivationId("a1");
        request.setActivationStatus(ActivationStatus.ACTIVE);

        tested.updateStatus(request);

        final List<PushDeviceRegistrationEntity> entities2 = deviceRepository.findByActivationId("a1");
        assertEquals(1, entities2.size());
        assertTrue(entities2.get(0).getActive());
    }

    @Test
    void testUpdateStatus_missingStatusInRequest() throws Exception {
        final PushDeviceRegistrationEntity device = new PushDeviceRegistrationEntity();
        device.setActivationId("a1");
        device.setAppCredentials(createAppCredentials(APP_NAME));
        device.setTimestampLastRegistered(new Date());
        device.setPlatform(Platform.APNS);
        device.setPushToken("t1");
        device.setActive(false);
        deviceRepository.save(device);

        when(powerAuthClient.getActivationStatus("a1"))
                .thenReturn(createActivationStatusResponse("a1"));

        final List<PushDeviceRegistrationEntity> entities = deviceRepository.findByActivationId("a1");
        assertEquals(1, entities.size());
        assertFalse(entities.get(0).getActive());

        final UpdateDeviceStatusRequest request = new UpdateDeviceStatusRequest();
        request.setActivationId("a1");

        tested.updateStatus(request);
        verify(powerAuthClient).getActivationStatus("a1");

        final List<PushDeviceRegistrationEntity> entities2 = deviceRepository.findByActivationId("a1");
        assertEquals(1, entities2.size());
        assertTrue(entities2.get(0).getActive());
    }

    @Test
    @Sql
    void testDelete() {
        tested.delete(APP_NAME, "t1");
        assertRegistrationDoesNotExist("a1");
        assertRegistrationDoesNotExist("a2");
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

    private void assertRegistrationExists(final String activationId, final String token) {
        final List<PushDeviceRegistrationEntity> entities = deviceRepository.findByActivationId(activationId);
        assertEquals(1, entities.size());
        assertEquals(activationId, entities.get(0).getActivationId());
        assertEquals(token, entities.get(0).getPushToken());
    }

    private void assertRegistrationDoesNotExist(final String activationId) {
        final List<PushDeviceRegistrationEntity> entities = deviceRepository.findByActivationId(activationId);
        assertEquals(0, entities.size());
    }

}
