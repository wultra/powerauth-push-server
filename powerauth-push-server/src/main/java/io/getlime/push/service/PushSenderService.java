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

package io.getlime.push.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turo.pushy.apns.*;
import com.turo.pushy.apns.auth.ApnsSigningKey;
import com.turo.pushy.apns.proxy.HttpProxyHandlerFactory;
import com.turo.pushy.apns.proxy.ProxyHandlerFactory;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;
import io.getlime.push.configuration.PowerAuthPushServiceConfiguration;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushSendResult;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.DeviceRegistrationRepository;
import io.getlime.push.repository.PushMessageRepository;
import io.getlime.push.repository.model.AppCredentials;
import io.getlime.push.repository.model.DeviceRegistration;
import io.getlime.push.repository.model.PushMessageEntity;
import io.getlime.push.service.fcm.FcmClient;
import io.getlime.push.service.fcm.FcmNotification;
import io.getlime.push.service.fcm.FcmSendRequest;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for sending push notifications to APNs service.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Service
public class PushSenderService {

    private AppCredentialsRepository appCredentialsRepository;
    private DeviceRegistrationRepository deviceRegistrationRepository;
    private PushMessageRepository pushMessageRepository;
    private PowerAuthPushServiceConfiguration pushServiceConfiguration;

    /**
     * Constructor that autowires required repositories.
     * @param appCredentialsRepository Repository with app credentials.
     * @param deviceRegistrationRepository Repository with device registrations.
     * @param pushMessageRepository Repository with logged push messages.
     * @param pushServiceConfiguration Push Service Configuration
     */
    @Autowired
    public PushSenderService(
            AppCredentialsRepository appCredentialsRepository,
            DeviceRegistrationRepository deviceRegistrationRepository,
            PushMessageRepository pushMessageRepository,
            PowerAuthPushServiceConfiguration pushServiceConfiguration
    ) {
        this.appCredentialsRepository = appCredentialsRepository;
        this.deviceRegistrationRepository = deviceRegistrationRepository;
        this.pushMessageRepository = pushMessageRepository;
        this.pushServiceConfiguration = pushServiceConfiguration;
    }

    private ApnsClientBuilder prepareApnsClient() throws SSLException {
        // Prepare APNs client builder
        final ApnsClientBuilder apnsClientBuilder = new ApnsClientBuilder();

        // Prepare proxy settings for APNs
        if (pushServiceConfiguration.isApnsProxyEnabled()) {
            String proxyUrl = pushServiceConfiguration.getApnsProxyUrl();
            int proxyPort = pushServiceConfiguration.getApnsProxyPort();
            String proxyUsername = pushServiceConfiguration.getApnsProxyUsername();
            String proxyPassword = pushServiceConfiguration.getApnsProxyPassword();

            // Null the credentials, if they are empty
            if (proxyUsername != null && proxyUsername.isEmpty()) {
                proxyUsername = null;
            }
            if (proxyPassword != null && proxyPassword.isEmpty()) {
                proxyPassword = null;
            }

            ProxyHandlerFactory proxyHandlerFactory = new HttpProxyHandlerFactory(new InetSocketAddress(proxyUrl, proxyPort), proxyUsername, proxyPassword);
            apnsClientBuilder.setProxyHandlerFactory(proxyHandlerFactory);
        }

        // Build a client and connect
        return apnsClientBuilder;
    }

    private FcmClient prepareFcmClient(String serverKey) {
        // Prepare and connect FCM client
        FcmClient client = new FcmClient(serverKey);
        if (pushServiceConfiguration.isFcmProxyEnabled()) {
            String proxyUrl = pushServiceConfiguration.getFcmProxyUrl();
            int proxyPort = pushServiceConfiguration.getFcmProxyPort();
            String proxyUsername = pushServiceConfiguration.getFcmProxyUsername();
            String proxyPassword = pushServiceConfiguration.getFcmProxyPassword();

            // Null the credentials, if they are empty
            if (proxyUsername != null && proxyUsername.isEmpty()) {
                proxyUsername = null;
            }
            if (proxyPassword != null && proxyPassword.isEmpty()) {
                proxyPassword = null;
            }

            client.setProxy(proxyUrl, proxyPort, proxyUsername, proxyPassword);
        }
        return client;
    }

    /**
     * Send push notifications to given application.
     * @param appId App ID used for addressing push messages. Required so that appropriate APNs/FCM credentials can be obtained.
     * @param pushMessageList List with push message objects.
     * @return Result of this batch sending.
     * @throws InterruptedException In case sending is interrupted.
     * @throws IOException In case certificate data cannot be read.
     */
    public PushSendResult send(Long appId, List<PushMessage> pushMessageList) throws InterruptedException, IOException {

        // Get APNs and FCM credentials
        AppCredentials credentials = this.appCredentialsRepository.findFirstByAppId(appId);

        // Is there such app?
        if (credentials == null) {
            throw new IllegalArgumentException("Application not found");
        }

        // Prepare client for APNs
        final String iosTopic = credentials.getIosBundle();
        final ApnsClientBuilder apnsClientBuilder = prepareApnsClient();
        try {
            ApnsSigningKey key = ApnsSigningKey.loadFromInputStream(new ByteArrayInputStream(credentials.getIosPrivateKey()), credentials.getIosTeamId(), credentials.getIosKeyId());
            apnsClientBuilder.setSigningKey(key);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            Logger.getLogger(PushSenderService.class.getName()).log(Level.SEVERE, "Invalid private key");
            throw new IllegalArgumentException("Invalid private key");
        }
        final ApnsClient apnsClient = apnsClientBuilder.build();
        final Future<Void> connectFuture;
        if (pushServiceConfiguration.isApnsUseDevelopment()) {
            connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
        } else {
            connectFuture = apnsClient.connect(ApnsClient.PRODUCTION_APNS_HOST);
        }
        connectFuture.await();

        // Prepare client for FCM
        final FcmClient fcmClient = prepareFcmClient(credentials.getAndroidServerKey());

        // Prepare a phaser for async sending synchronization
        final Phaser phaser = new Phaser(1);

        // Prepare a result object
        final PushSendResult result = new PushSendResult();

        // Send push message batch
        for (PushMessage pushMessage : pushMessageList) {

            // Get the message user ID
            String userId = pushMessage.getUserId();
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("No userId was specified");
            }

            // Get user device registrations
            String activationId = pushMessage.getActivationId();
            List<DeviceRegistration> registrations;
            if (activationId != null) { // in case the message should go to the specific device
                registrations = deviceRegistrationRepository.findByUserIdAndAppIdAndActivationId(userId, appId, activationId);
            } else {
                registrations = deviceRegistrationRepository.findByUserIdAndAppId(userId, appId);
            }

            // Send push messages to given devices
            for (final DeviceRegistration registration : registrations) {

                // Store the message in the database
                final PushMessageEntity sentMessage = this.storePushMessageInDatabase(pushMessage, registration.getId());

                // Check if given push is not personal, or if it is, that registration is in active state.
                // This avoids sending personal notifications to registrations that are blocked or removed.
                if (!pushMessage.getPersonal() || registration.getActive()) {

                    phaser.register();

                    // Send a push message to the provided mobile platform.
                    String platform = registration.getPlatform();
                    if (platform.equals(DeviceRegistration.Platform.iOS)) { // iOS - APNs

                        final String token = TokenUtil.sanitizeTokenString(registration.getPushToken());
                        final String payload = buildApnsPayload(pushMessage);
                        Date validUntil = pushMessage.getMessage().getValidUntil();

                        final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, iosTopic, payload, validUntil, DeliveryPriority.IMMEDIATE, pushMessage.getMessage().getCollapseKey());

                        final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = apnsClient.sendNotification(pushNotification);

                        sendNotificationFuture.addListener(new GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>>() {

                            @Override
                            public void operationComplete(Future<PushNotificationResponse<SimpleApnsPushNotification>> future) throws Exception {
                                try {
                                    final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = future.get();

                                    result.getIos().setTotal(result.getIos().getTotal() + 1);

                                    if (pushNotificationResponse != null) {
                                        if (!pushNotificationResponse.isAccepted()) {
                                            Logger.getLogger(PushSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the APNs gateway: " + pushNotificationResponse.getRejectionReason());
                                            sentMessage.setStatus(PushMessageEntity.Status.FAILED);
                                            pushMessageRepository.save(sentMessage);

                                            result.getIos().setFailed(result.getIos().getFailed() + 1);

                                            if (pushNotificationResponse.getRejectionReason().equals("BadDeviceToken")) {
                                                deviceRegistrationRepository.delete(registration);
                                                Logger.getLogger(PushSenderService.class.getName()).log(Level.SEVERE, "\t... due to bad device token value.");
                                            }

                                            if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                                                deviceRegistrationRepository.delete(registration);
                                                Logger.getLogger(PushSenderService.class.getName()).log(Level.SEVERE, "\t... and the token is invalid as of " + pushNotificationResponse.getTokenInvalidationTimestamp());
                                            }
                                        } else {
                                            sentMessage.setStatus(PushMessageEntity.Status.SENT);
                                            pushMessageRepository.save(sentMessage);
                                            result.getIos().setSent(result.getIos().getSent() + 1);
                                        }
                                    } else {
                                        Logger.getLogger(PushSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the APNs gateway: unknown error, will retry");
                                        sentMessage.setStatus(PushMessageEntity.Status.PENDING);
                                        pushMessageRepository.save(sentMessage);
                                    }
                                } catch (final ExecutionException e) {
                                    if (e.getCause() instanceof ClientNotConnectedException) {
                                        apnsClient.getReconnectionFuture().await();
                                    }
                                } finally {
                                    phaser.arriveAndDeregister();
                                }
                            }

                        });

                    } else if (platform.equals(DeviceRegistration.Platform.Android)) { // Android - FCM

                        FcmSendRequest request = new FcmSendRequest();
                        request.setTo(registration.getPushToken());
                        request.setData(pushMessage.getMessage().getExtras());
                        request.setCollapseKey(pushMessage.getMessage().getCollapseKey());

                        if (!pushMessage.getSilent()) {
                            FcmNotification notification = new FcmNotification();
                            notification.setTitle(pushMessage.getMessage().getTitle());
                            notification.setBody(pushMessage.getMessage().getBody());
                            notification.setSound(pushMessage.getMessage().getSound());
                            notification.setTag(pushMessage.getMessage().getCategory());
                            request.setNotification(notification);
                        }

                        final ListenableFuture<ResponseEntity<String>> future = fcmClient.exchange(request);
                        result.getAndroid().setTotal(result.getAndroid().getTotal() + 1);
                        future.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
                            @Override
                            public void onFailure(Throwable throwable) {
                                sentMessage.setStatus(PushMessageEntity.Status.FAILED);
                                pushMessageRepository.save(sentMessage);
                                result.getAndroid().setFailed(result.getAndroid().getFailed() + 1);
                                Logger.getLogger(PushSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the FCM gateway:" + throwable.getLocalizedMessage());
                                Logger.getLogger(PushSenderService.class.getName()).log(Level.INFO, throwable.getLocalizedMessage());
                                phaser.arriveAndDeregister();
                            }

                            @Override
                            public void onSuccess(ResponseEntity<String> stringResponseEntity) {
                                sentMessage.setStatus(PushMessageEntity.Status.SENT);
                                pushMessageRepository.save(sentMessage);
                                result.getAndroid().setSent(result.getAndroid().getSent() + 1);
                                phaser.arriveAndDeregister();
                            }
                        });

                    }
                }
            }

        }

        phaser.arriveAndAwaitAdvance();

        return result;
    }

    /**
     * Stores a push message in the database table `push_message`.
     * @param pushMessage Push message to be stored.
     * @param registrationId Device registration ID to be used for this message.
     * @return New database entity with push message information.
     * @throws JsonProcessingException In case message body JSON serialization fails.
     */
    private PushMessageEntity storePushMessageInDatabase(PushMessage pushMessage, Long registrationId) throws JsonProcessingException {
        // Store the message in database
        PushMessageEntity entity = new PushMessageEntity();
        entity.setDeviceRegistrationId(registrationId);
        entity.setUserId(pushMessage.getUserId());
        entity.setActivationId(pushMessage.getActivationId());
        entity.setEncrypted(pushMessage.getEncrypted());
        entity.setPersonal(pushMessage.getPersonal());
        entity.setSilent(pushMessage.getSilent());
        entity.setStatus(PushMessageEntity.Status.PENDING);
        entity.setTimestampCreated(new Date());
        ObjectMapper mapper = new ObjectMapper();
        String messageBody = mapper.writeValueAsString(pushMessage.getMessage());
        entity.setMessageBody(messageBody);
        return pushMessageRepository.save(entity);
    }

    /**
     * Method to build APNs message payload.
     * @param push Push message object with APNs data.
     * @return String with APNs JSON payload.
     */
    private String buildApnsPayload(PushMessage push) {
        final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
        payloadBuilder.setAlertTitle(push.getMessage().getTitle());
        payloadBuilder.setAlertBody(push.getMessage().getBody());
        payloadBuilder.setBadgeNumber(push.getMessage().getBadge());
        payloadBuilder.setCategoryName(push.getMessage().getCategory());
        payloadBuilder.setSoundFileName(push.getMessage().getSound());
        payloadBuilder.setContentAvailable(push.getSilent());
        //payloadBuilder.setThreadId(push.getMessage().getCollapseKey());
        Map<String, Object> extras = push.getMessage().getExtras();
        if (extras != null) {
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                payloadBuilder.addCustomProperty(entry.getKey(), entry.getValue());
            }
        }
        return payloadBuilder.buildWithDefaultMaximumLength();
    }

}
