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

import com.wultra.core.rest.model.base.response.Response;
import com.wultra.push.configuration.PushServiceConfiguration;
import com.wultra.push.errorhandling.exceptions.PushServerException;
import com.wultra.push.model.enumeration.ApnsEnvironment;
import com.wultra.push.model.enumeration.MobilePlatform;
import com.wultra.push.model.request.CreateDeviceForActivationsRequest;
import com.wultra.push.model.request.CreateDeviceRequest;
import com.wultra.push.model.request.DeleteDeviceRequest;
import com.wultra.push.model.request.UpdateDeviceStatusRequest;
import com.wultra.push.repository.AppCredentialsRepository;
import com.wultra.push.repository.model.AppCredentialsEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests of {@link PushDeviceService}
 *
 * @author Jan Pesek, jan.pesek@wultra.com
 */
@ExtendWith(MockitoExtension.class)
class PushDeviceServiceTest {

    @Mock
    private AppCredentialsRepository appCredentialsRepository;

    @Mock
    private DeviceRegistrationService deviceRegistrationService;

    @Mock
    private PushServiceConfiguration config;

    @InjectMocks
    private PushDeviceService tested;

    @Test
    void testCreateDevice_success() throws Exception {
        final AppCredentialsEntity credentials = new AppCredentialsEntity();
        when(appCredentialsRepository.findFirstByAppId("my_app"))
                .thenReturn(Optional.of(credentials));

        final CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId("my_app");
        request.setActivationId("a1");
        request.setToken("t1");
        request.setPlatform(MobilePlatform.FCM);

        final Response response = tested.createDevice(request);
        verify(deviceRegistrationService).createOrUpdateDevice(request, credentials);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testCreateDevice_APNs_environment_success() throws Exception {
        final AppCredentialsEntity credentials = new AppCredentialsEntity();
        when(appCredentialsRepository.findFirstByAppId("my_app"))
                .thenReturn(Optional.of(credentials));

        final CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId("my_app");
        request.setActivationId("a1");
        request.setToken("t1");
        request.setPlatform(MobilePlatform.APNS);
        request.setEnvironment(ApnsEnvironment.DEVELOPMENT);

        final Response response = tested.createDevice(request);
        verify(deviceRegistrationService).createOrUpdateDevice(request, credentials);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testCreateDevice_invalidRequest() {
        final PushServerException exception = assertThrows(PushServerException.class,
                () -> tested.createDevice(new CreateDeviceRequest()));
        assertEquals("App ID must not be null.", exception.getMessage());
    }

    @Test
    void testCreateDevice_invalidApp() {
        when(appCredentialsRepository.findFirstByAppId("non_existent"))
                .thenReturn(Optional.empty());

        final CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId("non_existent");
        request.setActivationId("a1");
        request.setToken("t1");
        request.setPlatform(MobilePlatform.FCM);

        final PushServerException exception = assertThrows(PushServerException.class,
                () -> tested.createDevice(request));
        assertEquals("Application with given ID does not exist", exception.getMessage());
    }

    @Test
    void testCreateDeviceMultipleActivations_success() throws Exception {
        when(config.isRegistrationOfMultipleActivationsEnabled())
                .thenReturn(true);
        final AppCredentialsEntity credentials = new AppCredentialsEntity();
        when(appCredentialsRepository.findFirstByAppId("my_app"))
                .thenReturn(Optional.of(credentials));

        final CreateDeviceForActivationsRequest request = new CreateDeviceForActivationsRequest();
        request.setAppId("my_app");
        request.setPlatform(MobilePlatform.FCM);
        request.setToken("t2");
        request.getActivationIds().addAll(List.of("a1", "a2"));

        final Response response = tested.createDeviceMultipleActivations(request);
        verify(deviceRegistrationService).createOrUpdateDevices(request, credentials);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testCreateDeviceMultipleActivations_disabled() {
        when(config.isRegistrationOfMultipleActivationsEnabled())
                .thenReturn(false);

        final PushServerException exception = assertThrows(PushServerException.class,
                () -> tested.createDeviceMultipleActivations(new CreateDeviceForActivationsRequest()));
        assertEquals("Registration of multiple associated activations per device is not enabled.", exception.getMessage());
    }

    @Test
    void testCreateDeviceMultipleActivations_invalidRequest() {
        when(config.isRegistrationOfMultipleActivationsEnabled())
                .thenReturn(true);

        final CreateDeviceForActivationsRequest request = new CreateDeviceForActivationsRequest();

        final PushServerException exception = assertThrows(PushServerException.class,
                () -> tested.createDeviceMultipleActivations(request));
        assertEquals("App ID must not be null.", exception.getMessage());
    }

    @Test
    void testCreateDeviceMultipleActivations_invalidApp() {
        when(config.isRegistrationOfMultipleActivationsEnabled())
                .thenReturn(true);
        when(appCredentialsRepository.findFirstByAppId("non-existent"))
                .thenReturn(Optional.empty());

        final CreateDeviceForActivationsRequest request = new CreateDeviceForActivationsRequest();
        request.setAppId("non-existent");
        request.setPlatform(MobilePlatform.FCM);
        request.setToken("t2");
        request.getActivationIds().addAll(List.of("a1", "a2"));

        final PushServerException exception = assertThrows(PushServerException.class,
                () -> tested.createDeviceMultipleActivations(request));
        assertEquals("Application with given ID does not exist", exception.getMessage());
    }

    @Test
    void testUpdateDeviceStatus_success() throws Exception {
        final UpdateDeviceStatusRequest request = new UpdateDeviceStatusRequest();
        request.setActivationId("a1");

        final Response response = tested.updateDeviceStatus(request);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testUpdateDeviceStatus_invalidRequest() {
        final UpdateDeviceStatusRequest request = new UpdateDeviceStatusRequest();

        final PushServerException exception = assertThrows(PushServerException.class,
                () -> tested.updateDeviceStatus(request));
        assertEquals("Activation ID must not be null.", exception.getMessage());
    }

    @Test
    void testDeleteDevice_success() throws Exception {
        final DeleteDeviceRequest request = new DeleteDeviceRequest();
        request.setAppId("app1");
        request.setToken("token1");

        final Response response = tested.deleteDevice(request);
        assertEquals("OK", response.getStatus());
    }

    @Test
    void testDeleteDevice_invalidRequest() {
        final DeleteDeviceRequest request = new DeleteDeviceRequest();
        request.setAppId("app1");

        final PushServerException exception = assertThrows(PushServerException.class,
                () -> tested.deleteDevice(request));
        assertEquals("Push token must not be null or empty.", exception.getMessage());
    }

}
