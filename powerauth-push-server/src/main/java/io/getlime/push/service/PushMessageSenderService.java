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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Phaser;

/**
 * Class responsible for sending push notifications to devices based on platform.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Service
public class PushMessageSenderService {

    private static final Logger logger = LoggerFactory.getLogger(PushMessageSenderService.class);

    private PushSendingWorker pushSendingWorker;
    private AppCredentialsRepository appCredentialsRepository;
    private PushDeviceRepository pushDeviceRepository;
    private PushMessageDAO pushMessageDAO;
    private AppCredentialStorageMap appRelatedPushClientMap;

    @Autowired
    public PushMessageSenderService(AppCredentialsRepository appCredentialsRepository,
                                    PushDeviceRepository pushDeviceRepository,
                                    PushMessageDAO pushMessageDAO,
                                    PushSendingWorker pushSendingWorker,
                                    AppCredentialStorageMap appRelatedPushClientMap) {
        this.appCredentialsRepository = appCredentialsRepository;
        this.pushDeviceRepository = pushDeviceRepository;
        this.pushMessageDAO = pushMessageDAO;
        this.pushSendingWorker = pushSendingWorker;
        this.appRelatedPushClientMap = appRelatedPushClientMap;
    }

    /**
     * Send push notifications to given application.
     *
     * @param appId App ID used for addressing push messages. Required so that appropriate APNs/FCM credentials can be obtained.
     * @param pushMessageList List with push message objects.
     * @return Result of this batch sending.
     * @throws PushServerException In case push message sending fails.
     */
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
                        if (pushClient.getApnsClient() == null) {
                            logger.error("Push message cannot be sent to APNS because APNS is not configured in push server.");
                            phaser.arriveAndDeregister();
                            continue;
                        }
                        pushSendingWorker.sendMessageToIos(pushClient.getApnsClient(), pushMessage.getBody(), pushMessage.getAttributes(), device.getPushToken(), pushClient.getAppCredentials().getIosBundle(), (result) -> {
                            try {
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
                            } catch (Throwable t) {
                                logger.error("System error when sending notification: {}", t.getMessage(), t);
                            } finally {
                                phaser.arriveAndDeregister();
                            }
                        });
                    } else if (platform.equals(PushDeviceRegistrationEntity.Platform.Android)) {
                        if (pushClient.getFcmClient() == null) {
                            logger.error("Push message cannot be sent to FCM because FCM is not configured in push server.");
                            phaser.arriveAndDeregister();
                            continue;
                        }
                        final String token = device.getPushToken();
                        pushSendingWorker.sendMessageToAndroid(pushClient.getFcmClient(), pushMessage.getBody(), pushMessage.getAttributes(), token, (sendingResult) -> {
                            try {
                                switch (sendingResult) {
                                    case OK: {
                                        sendResult.getAndroid().setSent(sendResult.getAndroid().getSent() + 1);
                                        pushMessageObject.setStatus(PushMessageEntity.Status.SENT);
                                        pushMessageDAO.save(pushMessageObject);
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
                            } catch (Throwable t) {
                                logger.error("System error when sending notification: {}", t.getMessage(), t);
                            } finally {
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
     * @param userId User to be notified by the campaign message (used for logging purpose).
     * @param deviceId Device owned by the user to be notified about the campaign message (used for logging purpose).
     * @param activationId Activation ID associated with the device (used for logging purpose).
     * @throws PushServerException In case any issue happens while sending the push message. Detailed information about
     *                             the error can be found in exception message.
     */
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
     * @param userId User to be notified by the campaign message (used for logging purpose).
     * @param deviceId Device owned by the user to be notified about the campaign message (used for logging purpose).
     * @param activationId Activation ID associated with the device (used for logging purpose).
     * @throws PushServerException In case any issue happens while sending the push message. Detailed information about
     * the error can be found in exception message.
     */
    public void sendCampaignMessage(final Long appId, String platform, final String token, PushMessageBody pushMessageBody, PushMessageAttributes attributes, String userId, Long deviceId, String activationId) throws PushServerException {

        final AppRelatedPushClient pushClient = prepareClients(appId);

        final PushMessageEntity pushMessageObject = pushMessageDAO.storePushMessageObject(pushMessageBody, attributes, userId, activationId, deviceId);

        if (platform.equals(PushDeviceRegistrationEntity.Platform.iOS)) {
            pushSendingWorker.sendMessageToIos(pushClient.getApnsClient(), pushMessageBody, attributes, token, pushClient.getAppCredentials().getIosBundle(), (result) -> {
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
            });
        } else if (platform.equals(PushDeviceRegistrationEntity.Platform.Android)) {
            pushSendingWorker.sendMessageToAndroid(pushClient.getFcmClient(), pushMessageBody, attributes, token, (result) -> {
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
            });
        }
    }

    // Lookup application credentials by appID and throw exception in case application is not found.
    private AppCredentialsEntity getAppCredentials(Long appId) throws PushServerException {
        AppCredentialsEntity credentials = appCredentialsRepository.findFirstByAppId(appId);
        if (credentials == null) {
            throw new PushServerException("Application not found");
        }
        return credentials;
    }

    // Return list of devices related to given user or activation ID (if present). List of devices is related to particular application as well.
    private List<PushDeviceRegistrationEntity> getPushDevices(Long appId, String userId, String activationId) throws PushServerException {
        if (userId == null || userId.isEmpty()) {
            logger.error("No userId was specified");
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

    // Prepare and cache APNS and FCM clients for provided app
    private AppRelatedPushClient prepareClients(Long appId) throws PushServerException {
        synchronized (this) {
            AppRelatedPushClient pushClient = appRelatedPushClientMap.get(appId);
            if (pushClient == null) {
                final AppCredentialsEntity credentials = getAppCredentials(appId);
                pushClient = new AppRelatedPushClient();
                if (credentials.getIosPrivateKey() != null) {
                    ApnsClient apnsClient = pushSendingWorker.prepareApnsClient(credentials.getIosTeamId(), credentials.getIosKeyId(), credentials.getIosPrivateKey());
                    pushClient.setApnsClient(apnsClient);
                }
                if (credentials.getAndroidPrivateKey() != null) {
                    FcmClient fcmClient = pushSendingWorker.prepareFcmClient(credentials.getAndroidProjectId(), credentials.getAndroidPrivateKey());
                    pushClient.setFcmClient(fcmClient);
                }
                pushClient.setAppCredentials(credentials);
                appRelatedPushClientMap.put(appId, pushClient);
                logger.info("Creating APNS and FCM clients for app {}", appId);
            }
            return pushClient;
        }
    }


    // Use validator to check there are no errors in push message
    private void validatePushMessage(PushMessage pushMessage) throws PushServerException {
        String error = PushMessageValidator.validatePushMessage(pushMessage);
        if (error != null) {
            logger.warn(error);
            throw new PushServerException(error);
        }
    }

}
