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

import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.auth.ApnsSigningKey;
import com.turo.pushy.apns.proxy.HttpProxyHandlerFactory;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageAttributes;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.entity.PushMessageSendResult;
import io.getlime.push.model.validator.PushMessageValidator;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.PushDeviceRepository;
import io.getlime.push.repository.dao.PushMessageDAO;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.repository.model.PushDeviceRegistrationEntity;
import io.getlime.push.repository.model.PushMessageEntity;
import io.getlime.push.service.batch.storage.AppCredentialStorageMap;
import io.getlime.push.service.fcm.FcmClient;
import io.getlime.push.service.fcm.FcmNotification;
import io.getlime.push.service.fcm.model.FcmSendRequest;
import io.getlime.push.service.fcm.model.FcmSendResponse;
import io.getlime.push.service.fcm.model.base.FcmResult;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.net.ssl.SSLException;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for sending push notifications to devices based on platform.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Service
public class PushMessageSenderService {

    private AppCredentialsRepository appCredentialsRepository;
    private PushDeviceRepository pushDeviceRepository;
    private PushMessageDAO pushMessageDAO;
    private PushServiceConfiguration pushServiceConfiguration;
    private AppCredentialStorageMap appRelatedPushClientMap = new AppCredentialStorageMap();

    @Autowired
    public PushMessageSenderService(AppCredentialsRepository appCredentialsRepository,
                                    PushDeviceRepository pushDeviceRepository,
                                    PushMessageDAO pushMessageDAO,
                                    PushServiceConfiguration pushServiceConfiguration) {
        this.appCredentialsRepository = appCredentialsRepository;
        this.pushDeviceRepository = pushDeviceRepository;
        this.pushMessageDAO = pushMessageDAO;
        this.pushServiceConfiguration = pushServiceConfiguration;
    }

    /**
     * Send push notifications to given application.
     *
     * @param appId App ID used for addressing push messages. Required so that appropriate APNs/FCM credentials can be obtained.
     * @param pushMessageList List with push message objects.
     * @return Result of this batch sending.
     */
    @Transactional
    public PushMessageSendResult sendPushMessage(final Long appId, List<PushMessage> pushMessageList) throws PushServerException {
        // Prepare clients
        AppRelatedPushClient pushClient = prepareClients(appId);

        // Prepare synchronization primitive for parallel push message sending
        final Phaser phaser = new Phaser(1);

        // Prepare result object
        final PushMessageSendResult sendResult = new PushMessageSendResult();

        // Send push message batch
        for (PushMessage pushMessage : pushMessageList) {

            // Validate push message before sending
            validatePushMessage(pushMessage);

            // Fetch connected devices
            List<PushDeviceRegistrationEntity> devices = getPushDevices(appId, pushMessage.getUserId(), pushMessage.getActivationId());

            // Iterate over all devices for given user
            for (final PushDeviceRegistrationEntity device : devices) {
                final PushMessageEntity pushMessageObject = pushMessageDAO.storePushMessageObject(pushMessage.getBody(), pushMessage.getAttributes(), pushMessage.getUserId(), pushMessage.getActivationId(), device.getId());

                // Check if given push is not personal, or if it is, that device is in active state.
                // This avoids sending personal notifications to devices that are blocked or removed.
                boolean isMessagePersonal = pushMessage.getAttributes() != null && pushMessage.getAttributes().getPersonal();
                boolean isDeviceActive = device.getActive();
                if (!isMessagePersonal || isDeviceActive) {

                    // Register phaser for synchronization
                    phaser.register();

                    // Decide if the device is iOS or Android and send message accordingly
                    String platform = device.getPlatform();
                    if (platform.equals(PushDeviceRegistrationEntity.Platform.iOS)) {
                        sendMessageToIos(pushClient.getApnsClient(), pushMessage.getBody(), pushMessage.getAttributes(), device.getPushToken(), pushClient.getAppCredentials().getIosBundle(), new PushSendingCallback() {
                            @Override
                            public void didFinishSendingMessage(Result result, Map<String, Object> contextData) {
                                switch (result) {
                                    case OK: {
                                        sendResult.getIos().setSent(sendResult.getIos().getSent() + 1);
                                        pushMessageObject.setStatus(PushMessageEntity.Status.SENT);
                                        pushMessageDAO.save(pushMessageObject);
                                        break;
                                    }
                                    case PENDING: {
                                        sendResult.getIos().setPending(sendResult.getIos().getPending() + 1);
                                        pushMessageObject.setStatus(PushMessageEntity.Status.PENDING);
                                        pushMessageDAO.save(pushMessageObject);
                                        break;
                                    }
                                    case FAILED: {
                                        sendResult.getIos().setFailed(sendResult.getIos().getFailed() + 1);
                                        pushMessageObject.setStatus(PushMessageEntity.Status.FAILED);
                                        pushMessageDAO.save(pushMessageObject);
                                        break;
                                    }
                                    case FAILED_DELETE: {
                                        sendResult.getIos().setFailed(sendResult.getIos().getFailed() + 1);
                                        pushMessageObject.setStatus(PushMessageEntity.Status.FAILED);
                                        pushMessageDAO.save(pushMessageObject);
                                        pushDeviceRepository.delete(device);
                                        break;
                                    }
                                }
                                sendResult.getIos().setTotal(sendResult.getIos().getTotal() + 1);
                                phaser.arriveAndDeregister();
                            }
                        });
                    } else if (platform.equals(PushDeviceRegistrationEntity.Platform.Android)) {
                        final String token = device.getPushToken();
                        sendMessageToAndroid(pushClient.getFcmClient(), pushMessage.getBody(), pushMessage.getAttributes(), token, new PushSendingCallback() {
                            @Override
                            public void didFinishSendingMessage(Result sendingResult, Map<String, Object> contextData) {
                                switch (sendingResult) {
                                    case OK: {
                                        sendResult.getAndroid().setSent(sendResult.getAndroid().getSent() + 1);
                                        pushMessageObject.setStatus(PushMessageEntity.Status.SENT);
                                        pushMessageDAO.save(pushMessageObject);
                                        updateFcmTokenIfNeeded(appId, token, contextData);
                                        break;
                                    }
                                    case PENDING: {
                                        sendResult.getAndroid().setPending(sendResult.getAndroid().getPending() + 1);
                                        pushMessageObject.setStatus(PushMessageEntity.Status.PENDING);
                                        pushMessageDAO.save(pushMessageObject);
                                        break;
                                    }
                                    case FAILED: {
                                        sendResult.getAndroid().setFailed(sendResult.getAndroid().getFailed() + 1);
                                        pushMessageObject.setStatus(PushMessageEntity.Status.FAILED);
                                        pushMessageDAO.save(pushMessageObject);
                                        break;
                                    }
                                    case FAILED_DELETE: {
                                        sendResult.getAndroid().setFailed(sendResult.getAndroid().getFailed() + 1);
                                        pushMessageObject.setStatus(PushMessageEntity.Status.FAILED);
                                        pushMessageDAO.save(pushMessageObject);
                                        pushDeviceRepository.delete(device);
                                        break;
                                    }
                                }
                                sendResult.getAndroid().setTotal(sendResult.getAndroid().getTotal() + 1);
                                phaser.arriveAndDeregister();
                            }
                        });
                    }
                }
            }
        }
        phaser.arriveAndAwaitAdvance();
        return sendResult;
    }

    /**
     * Send push message content with related message attributes to provided device (platform and token) using
     * credentials for given application. Return the result in the callback.
     *
     * @param appId App ID.
     * @param platform Mobile platform (iOS, Android).
     * @param token Push message token.
     * @param pushMessageBody Push message body.
     * @throws PushServerException In case any issue happens while sending the push message. Detailed information about
     *                             the error can be found in exception message.
     */
    @Transactional
    public void sendCampaignMessage(Long appId, String platform, String token, PushMessageBody pushMessageBody, String userId, Long deviceId, String activationId) throws PushServerException {
        sendCampaignMessage(appId, platform, token, pushMessageBody, null, userId, deviceId, activationId);
    }

    /**
     * Send push message content with related message attributes to provided device (platform and token) using
     * credentials for given application. Return the result in the callback.
     *
     * @param appId App ID.
     * @param platform Mobile platform (iOS, Android).
     * @param token Push message token.
     * @param pushMessageBody Push message body.
     * @param attributes Push message attributes.
     * @throws PushServerException In case any issue happens while sending the push message. Detailed information about
     * the error can be found in exception message.
     */
    @Transactional
    public void sendCampaignMessage(final Long appId, String platform, final String token, PushMessageBody pushMessageBody, PushMessageAttributes attributes, String userId, Long deviceId, String activationId) throws PushServerException {

        final AppRelatedPushClient pushClient = prepareClients(appId);

        final PushMessageEntity pushMessageObject = pushMessageDAO.storePushMessageObject(pushMessageBody, attributes, userId, activationId, deviceId);

        if (platform.equals(PushDeviceRegistrationEntity.Platform.iOS)) {
            sendMessageToIos(pushClient.getApnsClient(), pushMessageBody, attributes, token, pushClient.getAppCredentials().getIosBundle(), new PushSendingCallback() {
                @Override
                public void didFinishSendingMessage(Result result, Map<String, Object> contextData) {
                    switch (result) {
                        case OK: {
                            pushMessageObject.setStatus(PushMessageEntity.Status.SENT);
                            pushMessageDAO.save(pushMessageObject);
                            break;
                        }
                        case PENDING: {
                            pushMessageObject.setStatus(PushMessageEntity.Status.PENDING);
                            pushMessageDAO.save(pushMessageObject);
                            break;
                        }
                        case FAILED: {
                            pushMessageObject.setStatus(PushMessageEntity.Status.FAILED);
                            pushMessageDAO.save(pushMessageObject);
                            break;
                        }
                        case FAILED_DELETE: {
                            pushMessageObject.setStatus(PushMessageEntity.Status.FAILED);
                            pushMessageDAO.save(pushMessageObject);
                            pushDeviceRepository.delete(pushDeviceRepository.findFirstByAppIdAndPushToken(appId, token));
                            break;
                        }
                    }
                }
            });
        } else if (platform.equals(PushDeviceRegistrationEntity.Platform.Android)) {
            sendMessageToAndroid(pushClient.getFcmClient(), pushMessageBody, attributes, token, new PushSendingCallback() {
                @Override
                public void didFinishSendingMessage(Result result, Map<String, Object> contextData) {
                    switch (result) {
                        case OK: {
                            pushMessageObject.setStatus(PushMessageEntity.Status.SENT);
                            pushMessageDAO.save(pushMessageObject);
                            updateFcmTokenIfNeeded(appId, token, contextData);
                            break;
                        }
                        case PENDING: {
                            pushMessageObject.setStatus(PushMessageEntity.Status.PENDING);
                            pushMessageDAO.save(pushMessageObject);
                            break;
                        }
                        case FAILED: {
                            pushMessageObject.setStatus(PushMessageEntity.Status.FAILED);
                            pushMessageDAO.save(pushMessageObject);
                            break;
                        }
                        case FAILED_DELETE: {
                            pushMessageObject.setStatus(PushMessageEntity.Status.FAILED);
                            pushMessageDAO.save(pushMessageObject);
                            pushDeviceRepository.delete(pushDeviceRepository.findFirstByAppIdAndPushToken(appId, token));
                            break;
                        }
                    }
                }
            });
        }
    }

    // Send message to iOS platform
    private void sendMessageToIos(final ApnsClient apnsClient, final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final String pushToken, final String iosTopic, final PushSendingCallback callback) throws PushServerException {

        final String token = TokenUtil.sanitizeTokenString(pushToken);
        final String payload = buildApnsPayload(pushMessageBody, attributes == null ? false : attributes.getSilent()); // In case there are no attributes, the message is not silent
        Date validUntil = pushMessageBody.getValidUntil();
        final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, iosTopic, payload, validUntil, DeliveryPriority.IMMEDIATE, pushMessageBody.getCollapseKey());
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
                                callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE, null);
                            } else if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "\t... and the token is invalid as of " + pushNotificationResponse.getTokenInvalidationTimestamp());
                                callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE, null);
                            }
                        } else {
                            callback.didFinishSendingMessage(PushSendingCallback.Result.OK, null);
                        }
                    } else {
                        Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the APNs gateway: unknown error, will retry");
                        callback.didFinishSendingMessage(PushSendingCallback.Result.PENDING, null);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED, null);
                }
            }
        });
    }

    // Send message to Android platform
    private void sendMessageToAndroid(final FcmClient fcmClient, final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final String pushToken, final PushSendingCallback callback) throws PushServerException {

        FcmSendRequest request = new FcmSendRequest();
        request.setTo(pushToken);
        request.setData(pushMessageBody.getExtras());
        request.setCollapseKey(pushMessageBody.getCollapseKey());
        if (attributes == null || !attributes.getSilent()) { // if there are no attributes, assume the message is not silent
            FcmNotification notification = new FcmNotification();
            notification.setTitle(pushMessageBody.getTitle());
            notification.setBody(pushMessageBody.getBody());
            notification.setIcon(pushMessageBody.getIcon());
            notification.setSound(pushMessageBody.getSound());
            notification.setTag(pushMessageBody.getCategory());
            request.setNotification(notification);
        }
        final ListenableFuture<ResponseEntity<FcmSendResponse>> future = fcmClient.exchange(request);

        future.addCallback(new ListenableFutureCallback<ResponseEntity<FcmSendResponse>>() {

            @Override
            public void onFailure(Throwable throwable) {
                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the FCM gateway: " + throwable.getLocalizedMessage());
                callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED, null);
            }

            @Override
            public void onSuccess(ResponseEntity<FcmSendResponse> response) {
                for (FcmResult fcmResult : response.getBody().getFcmResults()) {
                    if (fcmResult.getMessageId() != null) {
                        // no issues, straight sending
                        if (fcmResult.getRegistrationId() == null) {
                            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.INFO, "Notification sent");
                            callback.didFinishSendingMessage(PushSendingCallback.Result.OK, null);
                        } else {
                            // no issues, straight sending + update token (pass it via context)
                            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.INFO, "Notification sent and token has been updated");
                            Map<String, Object> contextData = new HashMap<>();
                            contextData.put(FcmResult.KEY_UPDATE_TOKEN, fcmResult.getRegistrationId());
                            callback.didFinishSendingMessage(PushSendingCallback.Result.OK, contextData);
                        }
                    } else {
                        if (fcmResult.getFcmError() != null) {
                            switch (fcmResult.getFcmError().toLowerCase()) { // make sure to account for case issues
                                // token doesn't exist, remove device registration
                                case "notregistered":
                                    Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the FCM gateway, invalid token, will be deleted: ");
                                    callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE, null);
                                    break;
                                // retry to send later
                                case "unavailable":
                                    Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the FCM gateway, will retry to send: ");
                                    callback.didFinishSendingMessage(PushSendingCallback.Result.PENDING, null);
                                    break;
                                // non-recoverable error, remove device registration
                                default:
                                    Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the FCM gateway, non-recoverable error, will be deleted: ");
                                    callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE, null);
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    // Lookup application credentials by appID and throw exception in case application is not found.
    private AppCredentialsEntity getAppCredentials(Long appId) {
        AppCredentialsEntity credentials = appCredentialsRepository.findFirstByAppId(appId);
        if (credentials == null) {
            throw new IllegalArgumentException("Application not found");
        }
        return credentials;
    }

    // Return list of devices related to given user or activation ID (if present). List of devices is related to particular application as well.
    private List<PushDeviceRegistrationEntity> getPushDevices(Long appId, String userId, String activationId) throws PushServerException {
        if (userId == null || userId.isEmpty()) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "No userId was specified");
            throw new PushServerException("No userId was specified");
        }
        List<PushDeviceRegistrationEntity> devices;
        if (activationId != null) { // in case the message should go to the specific device
            devices = pushDeviceRepository.findByUserIdAndAppIdAndActivationId(userId, appId, activationId);
        } else {
            devices = pushDeviceRepository.findByUserIdAndAppId(userId, appId);
        }
        return devices;
    }

    // Prepare and connect APNS client.
    private ApnsClient prepareApnsClient(byte[] apnsPrivateKey, String teamId, String keyId) throws PushServerException {
        final ApnsClientBuilder apnsClientBuilder = new ApnsClientBuilder();
        apnsClientBuilder.setProxyHandlerFactory(apnsClientProxy());
        if (pushServiceConfiguration.isApnsUseDevelopment()) {
            apnsClientBuilder.setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST);
        } else {
            apnsClientBuilder.setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST);
        }
        try {
            ApnsSigningKey key = ApnsSigningKey.loadFromInputStream(new ByteArrayInputStream(apnsPrivateKey), teamId, keyId);
            apnsClientBuilder.setSigningKey(key);
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, e.getMessage());
            throw new PushServerException("Invalid private key");
        }
        try {
            return apnsClientBuilder.build();
        } catch (SSLException e) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, e.getMessage());
            throw new PushServerException("SSL problem");
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

    // Prepare and cached APNS and FCM clients for provided app
    private AppRelatedPushClient prepareClients(Long appId) throws PushServerException {
        synchronized (this) {
            AppRelatedPushClient pushClient = appRelatedPushClientMap.get(appId);
            if (pushClient == null) {
                final AppCredentialsEntity credentials = getAppCredentials(appId);
                ApnsClient apnsClient = prepareApnsClient(credentials.getIosPrivateKey(), credentials.getIosTeamId(), credentials.getIosKeyId());
                FcmClient fcmClient = prepareFcmClient(credentials.getAndroidServerKey());
                pushClient = new AppRelatedPushClient();
                pushClient.setAppCredentials(credentials);
                pushClient.setApnsClient(apnsClient);
                pushClient.setFcmClient(fcmClient);
                appRelatedPushClientMap.put(appId, pushClient);
            }
            return pushClient;
        }
    }

    // Prepare proxy settings for APNs
    private HttpProxyHandlerFactory apnsClientProxy() {
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
            return new HttpProxyHandlerFactory(new InetSocketAddress(proxyUrl, proxyPort), proxyUsername, proxyPassword);
        }
        return null;
    }


    // Use validator to check there are no errors in push message
    private void validatePushMessage(PushMessage pushMessage) throws PushServerException {
        String error = PushMessageValidator.validatePushMessage(pushMessage);
        if (error != null) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.WARNING, error);
            throw new PushServerException(error);
        }
    }

    // Update FCM token based on context data
    private void updateFcmTokenIfNeeded(Long appId, String token, Map<String, Object> contextData) {
        if (contextData != null) {
            String updatedToken = (String) contextData.get(FcmResult.KEY_UPDATE_TOKEN);
            if (updatedToken != null) {
                PushDeviceRegistrationEntity device = pushDeviceRepository.findFirstByAppIdAndPushToken(appId, token);
                device.setPushToken(updatedToken);
                pushDeviceRepository.save(device);
            }
        }
    }

    /**
     * Method to build APNs message payload.
     *
     * @param push     Push message object with APNs data.
     * @param isSilent Indicates if the message is silent or not.
     * @return String with APNs JSON payload.
     */
    private String buildApnsPayload(PushMessageBody push, boolean isSilent) {
        final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
        payloadBuilder.setAlertTitle(push.getTitle());
        payloadBuilder.setAlertBody(push.getBody());
        payloadBuilder.setBadgeNumber(push.getBadge());
        payloadBuilder.setCategoryName(push.getCategory());
        payloadBuilder.setSoundFileName(push.getSound());
        payloadBuilder.setContentAvailable(isSilent);
        //payloadBuilder.setThreadId(push.getBody().getCollapseKey());
        Map<String, Object> extras = push.getExtras();
        if (extras != null) {
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                payloadBuilder.addCustomProperty(entry.getKey(), entry.getValue());
            }
        }
        return payloadBuilder.buildWithDefaultMaximumLength();
    }
}
