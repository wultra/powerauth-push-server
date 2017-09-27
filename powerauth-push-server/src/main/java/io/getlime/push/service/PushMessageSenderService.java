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
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageSendResult;
import io.getlime.push.repository.AppCredentialRepository;
import io.getlime.push.repository.PushDeviceRepository;
import io.getlime.push.repository.PushMessageRepository;
import io.getlime.push.repository.model.AppCredentialEntity;
import io.getlime.push.repository.model.PushDeviceEntity;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
public class PushMessageSenderService {
    private AppCredentialRepository appCredentialRepository;
    private PushDeviceRepository pushDeviceRepository;
    private PushMessageRepository pushMessageRepository;
    private PushServiceConfiguration pushServiceConfiguration;

    @Autowired
    public PushMessageSenderService(AppCredentialRepository appCredentialRepository,
                                    PushDeviceRepository pushDeviceRepository,
                                    PushMessageRepository pushMessageRepository,
                                    PushServiceConfiguration pushServiceConfiguration) {
        this.appCredentialRepository = appCredentialRepository;
        this.pushDeviceRepository = pushDeviceRepository;
        this.pushMessageRepository = pushMessageRepository;
        this.pushServiceConfiguration = pushServiceConfiguration;
    }


    /**
     * Send push notifications to given application.
     * @param appId App ID used for addressing push messages. Required so that appropriate APNs/FCM credentials can be obtained.
     * @param pushMessageList List with push message objects.
     * @return Result of this batch sending.
     * @throws InterruptedException In case sending is interrupted.
     * @throws IOException In case certificate data cannot be read.
     */
    public PushMessageSendResult send(Long appId, List<PushMessage> pushMessageList) throws  PushServerException {
        AppCredentialEntity credentials = appCredentialRepository.findFirstByAppId(appId);
        if (credentials == null) {
            throw new IllegalArgumentException("Application not found");
        }
        final String iosTopic = credentials.getIosBundle();
        final ApnsClient apnsClient = prepareApnsClient(credentials);
        final FcmClient fcmClient = prepareFcmClient(credentials.getAndroidServerKey());
        final Phaser phaser = new Phaser(1);
        final PushMessageSendResult result = new PushMessageSendResult();

        // Send push message batch
        for (PushMessage pushMessage : pushMessageList) {
            List<PushDeviceEntity> devices = getPushDevices(appId, pushMessage);
            for (final PushDeviceEntity device : devices) {
                final PushMessageEntity sentMessage = storePushMessageInDatabase(pushMessage, device.getId());
                // Check if given push is not personal, or if it is, that device is in active state.
                // This avoids sending personal notifications to devices that are blocked or removed.
                if (!pushMessage.getPersonal() || device.getActive()) {
                    phaser.register();
                    String platform = device.getPlatform();
                    if (platform.equals(PushDeviceEntity.Platform.iOS)) {
                        sendMessageToIos(iosTopic, apnsClient, phaser, result, pushMessage, device, sentMessage);
                    }
                    else if (platform.equals(PushDeviceEntity.Platform.Android)) {
                        sendMessageToAndroidImpl(fcmClient, phaser, result, pushMessage, device, sentMessage);
                    }
                }
            }
        }
        phaser.arriveAndAwaitAdvance();
        return result;
    }

    // Send message to iOS platform
    private void sendMessageToIos(String iosTopic, final ApnsClient apnsClient, final Phaser phaser, final PushMessageSendResult result, PushMessage pushMessage, final PushDeviceEntity device, final PushMessageEntity sentMessage) {
        final String token = TokenUtil.sanitizeTokenString(device.getPushToken());
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
                            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the APNs gateway: " + pushNotificationResponse.getRejectionReason());
                            sentMessage.setStatus(PushMessageEntity.Status.FAILED);
                            pushMessageRepository.save(sentMessage);
                            result.getIos().setFailed(result.getIos().getFailed() + 1);
                            if (pushNotificationResponse.getRejectionReason().equals("BadDeviceToken")) {
                                pushDeviceRepository.delete(device);
                                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "\t... due to bad device token value.");
                            }
                            if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                                pushDeviceRepository.delete(device);
                                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "\t... and the token is invalid as of " + pushNotificationResponse.getTokenInvalidationTimestamp());
                            }
                        } else {
                            sentMessage.setStatus(PushMessageEntity.Status.SENT);
                            pushMessageRepository.save(sentMessage);
                            result.getIos().setSent(result.getIos().getSent() + 1);
                        }
                    } else {
                        Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the APNs gateway: unknown error, will retry");
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
    }

    // Send message to Android platform
    private void sendMessageToAndroidImpl(FcmClient fcmClient, final Phaser phaser, final PushMessageSendResult result, PushMessage pushMessage, PushDeviceEntity device, final PushMessageEntity sentMessage) {
        FcmSendRequest request = new FcmSendRequest();
        request.setTo(device.getPushToken());
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
                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the FCM gateway:" + throwable.getLocalizedMessage());
                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.INFO, throwable.getLocalizedMessage());
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

    // Return list of devices
    private List<PushDeviceEntity> getPushDevices(Long appId, PushMessage pushMessage) throws PushServerException {
        String userId = pushMessage.getUserId();
        if (userId == null || userId.isEmpty()) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "No userId was specified");
            throw new PushServerException("No userId was specified");
        }
        String activationId = pushMessage.getActivationId();
        List<PushDeviceEntity> devices;
        if (activationId != null) { // in case the message should go to the specific device
            devices = pushDeviceRepository.findByUserIdAndAppIdAndActivationId(userId, appId, activationId);
        } else {
            devices = pushDeviceRepository.findByUserIdAndAppId(userId, appId);
        }
        return devices;
    }

    private ApnsClient prepareApnsClient(AppCredentialEntity credentials) throws PushServerException {
        final ApnsClientBuilder apnsClientBuilder = new ApnsClientBuilder();
        setApnsClientProxy(apnsClientBuilder);
        try {
            ApnsSigningKey key = ApnsSigningKey.loadFromInputStream(new ByteArrayInputStream(credentials.getIosPrivateKey()), credentials.getIosTeamId(), credentials.getIosKeyId());
            apnsClientBuilder.setSigningKey(key);
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Invalid private key");
            throw new PushServerException("Invalid private key");
        }
        try {
            final ApnsClient apnsClient = apnsClientBuilder.build();
            final Future<Void> future;
            if (pushServiceConfiguration.isApnsUseDevelopment()) {
                future = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
            } else {
                future = apnsClient.connect(ApnsClient.PRODUCTION_APNS_HOST);
            }
            future.await();
            return apnsClient;
        } catch (InterruptedException | IOException e) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Unable to connect to APNS service");
            throw new PushServerException("Unable to connect to APNS service");
        }
    }

    // Prepare and connect FCM client
    private FcmClient prepareFcmClient(String serverKey) {
        FcmClient client = new FcmClient(serverKey);
        if (pushServiceConfiguration.isFcmProxyEnabled()) {
            String proxyUrl = pushServiceConfiguration.getFcmProxyUrl();
            int proxyPort = pushServiceConfiguration.getFcmProxyPort();
            String proxyUsername = pushServiceConfiguration.getFcmProxyUsername();
            String proxyPassword = pushServiceConfiguration.getFcmProxyPassword();
            setCredentialsToNull(proxyUsername, proxyPassword);
            client.setProxy(proxyUrl, proxyPort, proxyUsername, proxyPassword);
        }
        return client;
    }

    // Prepare proxy settings for APNs
    private void setApnsClientProxy(ApnsClientBuilder apnsClientBuilder) {
        if (pushServiceConfiguration.isApnsProxyEnabled()) {
            String proxyUrl = pushServiceConfiguration.getApnsProxyUrl();
            int proxyPort = pushServiceConfiguration.getApnsProxyPort();
            String proxyUsername = pushServiceConfiguration.getApnsProxyUsername();
            String proxyPassword = pushServiceConfiguration.getApnsProxyPassword();
            setCredentialsToNull(proxyUsername, proxyPassword);
            ProxyHandlerFactory proxyHandlerFactory = new HttpProxyHandlerFactory(new InetSocketAddress(proxyUrl, proxyPort), proxyUsername, proxyPassword);
            apnsClientBuilder.setProxyHandlerFactory(proxyHandlerFactory);
        }
    }

    private void setCredentialsToNull(String proxyUsername, String proxyPassword) {
        if (proxyUsername != null && proxyUsername.isEmpty()) {
            proxyUsername = null;
        }
        if (proxyPassword != null && proxyPassword.isEmpty()) {
            proxyPassword = null;
        }
    }

    /**
     * Stores a push message in the database table `push_message`.
     * @param pushMessage Push message to be stored.
     * @param registrationId Device registration ID to be used for this message.
     * @return New database entity with push message information.
     * @throws JsonProcessingException In case message body JSON serialization fails.
     */
    private PushMessageEntity storePushMessageInDatabase(PushMessage pushMessage, Long registrationId) throws PushServerException {
        try {
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
            String messageBody = null;
            messageBody = mapper.writeValueAsString(pushMessage.getMessage());
            entity.setMessageBody(messageBody);
            return pushMessageRepository.save(entity);
        } catch (JsonProcessingException e) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Unable to serialize Json");
            throw new PushServerException("Unable to serialize Json");
        }
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
        //payloadBuilder.setThreadId(push.getPushMessage().getCollapseKey());
        Map<String, Object> extras = push.getMessage().getExtras();
        if (extras != null) {
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                payloadBuilder.addCustomProperty(entry.getKey(), entry.getValue());
            }
        }
        return payloadBuilder.buildWithDefaultMaximumLength();
    }

}
