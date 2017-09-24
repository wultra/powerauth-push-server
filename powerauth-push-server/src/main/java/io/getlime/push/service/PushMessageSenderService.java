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
import io.getlime.push.model.validator.PushMessageValidator;
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
import javax.net.ssl.SSLException;
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
     *
     * @param appId App ID used for addressing push messages. Required so that appropriate APNs/FCM credentials can be obtained.
     * @param pushMessageList List with push message objects.
     * @return Result of this batch sending.
     * @throws InterruptedException In case sending is interrupted.
     * @throws IOException In case certificate data cannot be read.
     */
    public PushMessageSendResult send(Long appId, List<PushMessage> pushMessageList) throws InterruptedException, IOException, PushServerException {

        // Fetch application credentials
        final AppCredentialEntity credentials = getAppCredentials(appId);

        // Prepare clients
        final ApnsClient apnsClient = prepareApnsClient(credentials.getIosPrivateKey(), credentials.getIosTeamId(), credentials.getIosKeyId());
        final FcmClient fcmClient = prepareFcmClient(credentials.getAndroidServerKey());

        // Prepare synchronization primitive for parallel push message sending
        final Phaser phaser = new Phaser(1);

        // Prepare result object
        final PushMessageSendResult result = new PushMessageSendResult();

        // Send push message batch
        for (PushMessage pushMessage : pushMessageList) {
            List<PushDeviceEntity> devices = getPushDevices(appId, pushMessage.getUserId(), pushMessage.getActivationId());

            // Iterate over all devices for given user
            for (final PushDeviceEntity device : devices) {
                final PushMessageEntity sentMessage = storePushMessageInDatabase(pushMessage, device.getId());

                // Check if given push is not personal, or if it is, that device is in active state.
                // This avoids sending personal notifications to devices that are blocked or removed.
                if (!pushMessage.getPersonal() || device.getActive()) {
                    phaser.register();
                    String platform = device.getPlatform();

                    // Decide if the device is iOS or Android and send message accordingly
                    if (platform.equals(PushDeviceEntity.Platform.iOS)) {
                        sendMessageToIos(apnsClient, pushMessage, device.getPushToken(), credentials.getIosBundle(), new PushSendingCallback() {
                            @Override
                            public void didFinishSendingMessage(Result sendingResult) {

                                switch (sendingResult) {
                                    case OK: {
                                        sentMessage.setStatus(PushMessageEntity.Status.SENT);
                                        result.getIos().setSent(result.getIos().getSent() + 1);
                                        pushMessageRepository.save(sentMessage);
                                        break;
                                    }
                                    case PENDING: {
                                        sentMessage.setStatus(PushMessageEntity.Status.PENDING);
                                        result.getIos().setPending(result.getIos().getPending() + 1);
                                        pushMessageRepository.save(sentMessage);
                                        break;
                                    }
                                    case FAILED: {
                                        sentMessage.setStatus(PushMessageEntity.Status.FAILED);
                                        result.getIos().setFailed(result.getIos().getFailed() + 1);
                                        pushMessageRepository.save(sentMessage);
                                        break;
                                    }
                                    case FAILED_DELETE: {
                                        sentMessage.setStatus(PushMessageEntity.Status.FAILED);
                                        result.getIos().setFailed(result.getIos().getFailed() + 1);
                                        pushDeviceRepository.delete(device);
                                    }
                                }

                                result.getIos().setTotal(result.getIos().getTotal() + 1);
                                phaser.arriveAndDeregister();
                            }
                        });
                    }
                    else if (platform.equals(PushDeviceEntity.Platform.Android)) {
                        sendMessageToAndroid(fcmClient, pushMessage, device.getPushToken(), new PushSendingCallback() {
                            @Override
                            public void didFinishSendingMessage(Result sendingResult) {

                                switch (sendingResult) {
                                    case OK: {
                                        result.getAndroid().setSent(result.getAndroid().getSent() + 1);
                                        sentMessage.setStatus(PushMessageEntity.Status.SENT);
                                        pushMessageRepository.save(sentMessage);
                                        break;
                                    }
                                    case PENDING: {
                                        sentMessage.setStatus(PushMessageEntity.Status.PENDING);
                                        result.getAndroid().setPending(result.getAndroid().getPending() + 1);
                                        pushMessageRepository.save(sentMessage);
                                        break;
                                    }
                                    case FAILED: {
                                        result.getAndroid().setFailed(result.getAndroid().getFailed() + 1);
                                        sentMessage.setStatus(PushMessageEntity.Status.FAILED);
                                        pushMessageRepository.save(sentMessage);
                                        break;
                                    }
                                    case FAILED_DELETE: {
                                        result.getAndroid().setFailed(result.getAndroid().getFailed() + 1);
                                        sentMessage.setStatus(PushMessageEntity.Status.FAILED);
                                        pushDeviceRepository.delete(device);
                                        break;
                                    }
                                }

                                result.getAndroid().setTotal(result.getAndroid().getTotal() + 1);
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

    // Send message to iOS platform
    private void sendMessageToIos(final ApnsClient apnsClient, final PushMessage pushMessage, final String pushToken, final String iosTopic, final PushSendingCallback callback) throws InterruptedException, PushServerException {

        validatePushMessage(pushMessage);

        final String token = TokenUtil.sanitizeTokenString(pushToken);
        final String payload = buildApnsPayload(pushMessage);
        Date validUntil = pushMessage.getMessage().getValidUntil();
        final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, iosTopic, payload, validUntil, DeliveryPriority.IMMEDIATE, pushMessage.getMessage().getCollapseKey());
        final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = apnsClient.sendNotification(pushNotification);

        sendNotificationFuture.addListener(new GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>>() {

            @Override
            public void operationComplete(Future<PushNotificationResponse<SimpleApnsPushNotification>> future) throws Exception {
                try {
                    final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = future.get();
                    if (pushNotificationResponse != null) {
                        if (!pushNotificationResponse.isAccepted()) {
                            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the APNs gateway: " + pushNotificationResponse.getRejectionReason());
                            if (pushNotificationResponse.getRejectionReason().equals("BadDeviceToken")) {
                                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "\t... due to bad device token value.");
                                callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                            } else if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "\t... and the token is invalid as of " + pushNotificationResponse.getTokenInvalidationTimestamp());
                                callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                            }
                        } else {
                            callback.didFinishSendingMessage(PushSendingCallback.Result.OK);
                        }
                    } else {
                        Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the APNs gateway: unknown error, will retry");
                        callback.didFinishSendingMessage(PushSendingCallback.Result.PENDING);
                    }
                } catch (final ExecutionException e) {
                    if (e.getCause() instanceof ClientNotConnectedException) {
                        apnsClient.getReconnectionFuture().await();
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
                    }
                }
            }

        });
    }

    // Send message to Android platform
    private void sendMessageToAndroid(final FcmClient fcmClient, final PushMessage pushMessage, final String pushToken, final PushSendingCallback callback) throws PushServerException {

        validatePushMessage(pushMessage);

        FcmSendRequest request = new FcmSendRequest();
        request.setTo(pushToken);
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

        future.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the FCM gateway:" + throwable.getLocalizedMessage());
                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, throwable.getLocalizedMessage());
                callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
            }
            @Override
            public void onSuccess(ResponseEntity<String> stringResponseEntity) { // TODO: Implement processing of the response body
                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.INFO, stringResponseEntity.getBody());
                callback.didFinishSendingMessage(PushSendingCallback.Result.OK);
            }
        });
    }

    // Lookup application credentials by appID and throw exception in case application is not found.
    private AppCredentialEntity getAppCredentials(Long appId) {
        AppCredentialEntity credentials = appCredentialRepository.findFirstByAppId(appId);
        if (credentials == null) {
            throw new IllegalArgumentException("Application not found");
        }
        return credentials;
    }

    // Return list of devices related to given user or activation ID (if present). List of devices is related to particular application as well.
    private List<PushDeviceEntity> getPushDevices(Long appId, String userId, String activationId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("No userId was specified");
        }
        List<PushDeviceEntity> devices;
        if (activationId != null) { // in case the message should go to the specific device
            devices = pushDeviceRepository.findByUserIdAndAppIdAndActivationId(userId, appId, activationId);
        } else {
            devices = pushDeviceRepository.findByUserIdAndAppId(userId, appId);
        }
        return devices;
    }

    // Prepare and connect APNS client.
    private ApnsClient prepareApnsClient(byte[] apnsPrivateKey, String teamId, String keyId) throws IOException, InterruptedException {
        final ApnsClientBuilder apnsClientBuilder = new ApnsClientBuilder();
        setApnsClientProxy(apnsClientBuilder);
        try {
            ApnsSigningKey key = ApnsSigningKey.loadFromInputStream(new ByteArrayInputStream(apnsPrivateKey), teamId, keyId);
            apnsClientBuilder.setSigningKey(key);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Invalid private key");
            throw new IllegalArgumentException("Invalid private key");
        }
        final ApnsClient apnsClient = apnsClientBuilder.build();
        final Future<Void> future;
        if (pushServiceConfiguration.isApnsUseDevelopment()) {
            future = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
        } else {
            future = apnsClient.connect(ApnsClient.PRODUCTION_APNS_HOST);
        }
        future.await();
        return apnsClient;
    }

    // Prepare and connect FCM client
    private FcmClient prepareFcmClient(String serverKey) {
        FcmClient client = new FcmClient(serverKey);
        if (pushServiceConfiguration.isFcmProxyEnabled()) {
            String proxyUrl = pushServiceConfiguration.getFcmProxyUrl();
            int proxyPort = pushServiceConfiguration.getFcmProxyPort();
            String proxyUsername = pushServiceConfiguration.getFcmProxyUsername();
            String proxyPassword = pushServiceConfiguration.getFcmProxyPassword();
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

    // Prepare proxy settings for APNs
    private void setApnsClientProxy(ApnsClientBuilder apnsClientBuilder) throws SSLException {
        if (pushServiceConfiguration.isApnsProxyEnabled()) {
            String proxyUrl = pushServiceConfiguration.getApnsProxyUrl();
            int proxyPort = pushServiceConfiguration.getApnsProxyPort();
            String proxyUsername = pushServiceConfiguration.getApnsProxyUsername();
            String proxyPassword = pushServiceConfiguration.getApnsProxyPassword();
            if (proxyUsername != null && proxyUsername.isEmpty()) {
                proxyUsername = null;
            }
            if (proxyPassword != null && proxyPassword.isEmpty()) {
                proxyPassword = null;
            }
            ProxyHandlerFactory proxyHandlerFactory = new HttpProxyHandlerFactory(new InetSocketAddress(proxyUrl, proxyPort), proxyUsername, proxyPassword);
            apnsClientBuilder.setProxyHandlerFactory(proxyHandlerFactory);
        }
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

    // Use validator to check there are no errors in push message
    private void validatePushMessage(PushMessage pushMessage) throws PushServerException {
        String error = PushMessageValidator.validatePushMessage(pushMessage);
        if (error != null) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.WARNING, error);
            throw new PushServerException(error);
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
