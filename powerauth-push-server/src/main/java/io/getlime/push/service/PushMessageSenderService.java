/*
 * Copyright 2016 Wultra s.r.o.
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

import com.eatthepath.pushy.apns.ApnsClient;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.*;
import io.getlime.push.model.enumeration.ApnsEnvironment;
import io.getlime.push.model.enumeration.Mode;
import io.getlime.push.model.enumeration.Priority;
import io.getlime.push.model.validator.PushMessageValidator;
import io.getlime.push.repository.PushDeviceRepository;
import io.getlime.push.repository.dao.PushMessageDAO;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.repository.model.Platform;
import io.getlime.push.repository.model.PushDeviceRegistrationEntity;
import io.getlime.push.repository.model.PushMessageEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Phaser;

/**
 * Class responsible for sending push notifications to devices based on platform.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Slf4j
@AllArgsConstructor
@Service
public class PushMessageSenderService {

    private final PushSendingWorker pushSendingWorker;
    private final PushDeviceRepository pushDeviceRepository;
    private final PushMessageDAO pushMessageDAO;
    private final LoadingCache<String, AppRelatedPushClient> appRelatedPushClientCache;
    private final PushServiceConfiguration configuration;

    /**
     * Send push notifications to given application.
     *
     * @param appId App ID used for addressing push messages. Required so that appropriate APNs/FCM credentials can be obtained.
     * @param mode Specifies if the message sending should be synchronous or asynchronous.
     * @param pushMessageList List with push message objects.
     * @return Result of this batch sending.
     * @throws PushServerException In case push message sending fails.
     */
    public BasePushMessageSendResult sendPushMessage(final String appId, final Mode mode, List<PushMessage> pushMessageList) throws PushServerException {
        final AppRelatedPushClient pushClient = prepareClients(appId);

        // Prepare synchronization primitive for parallel push message sending
        final Phaser phaser = new Phaser(1);

        // Prepare result object
        final PushMessageSendResult sendResult = new PushMessageSendResult(mode);

        // Send push message batch
        for (PushMessage pushMessage : pushMessageList) {

            // Validate push message before sending
            validatePushMessage(pushMessage);

            // Fetch connected devices
            final AppCredentialsEntity appCredentials = pushClient.getAppCredentials();
            final List<PushDeviceRegistrationEntity> devices = getPushDevices(appCredentials.getId(), pushMessage.getUserId(), pushMessage.getActivationId());

            // Iterate over all devices for given user
            for (final PushDeviceRegistrationEntity device : devices) {
                // Store push message, in case storing of messages is disabled null value is returned
                final PushMessageEntity pushMessageObject = storePushMessage(pushMessage, device);

                // Check if given push is not personal, or if it is, that device is in active state.
                // This avoids sending personal notifications to devices that are blocked or removed.
                final boolean isMessagePersonal = pushMessage.getAttributes() != null && pushMessage.getAttributes().getPersonal();
                final boolean isDeviceActive = device.getActive();
                if (!isMessagePersonal || isDeviceActive) {

                    // Register phaser for synchronization
                    registerPhaserForMode(phaser, mode);

                    final Platform platform = device.getPlatform();
                    if (platform == Platform.IOS || platform == Platform.APNS) {
                        final ApnsEnvironment environment = resolveApnsEnvironment(device.getEnvironment(), appCredentials.getApnsEnvironment());
                        if (environment == null) {
                            logger.error("Push message cannot be sent because APNs environment configuration failed. Check configuration of application property 'powerauth.push.service.apns.useDevelopment'.");
                            arriveAndDeregisterPhaserForMode(phaser, mode);
                            continue;
                        }
                        final ApnsClient apnsClient = environment == ApnsEnvironment.PRODUCTION ? pushClient.getApnsClientProduction() : pushClient.getApnsClientDevelopment();
                        final PushMessageSendResult.PlatformResult platformResult = sendResult.getApns();
                        pushSendingWorker.sendMessageToApns(apnsClient, pushMessage.getBody(), pushMessage.getAttributes(), pushMessage.getPriority(), device.getPushToken(), pushClient.getAppCredentials().getApnsBundle(), createPushSendingCallback(mode, device, platformResult, pushMessageObject, phaser));
                    } else if (platform == Platform.ANDROID || platform == Platform.FCM) {
                        if (pushClient.getFcmClient() == null) {
                            logger.error("Push message cannot be sent to FCM because FCM is not configured in push server.");
                            arriveAndDeregisterPhaserForMode(phaser, mode);
                            continue;
                        }
                        final String token = device.getPushToken();
                        final PushMessageSendResult.PlatformResult platformResult = sendResult.getFcm();
                        pushSendingWorker.sendMessageToFcm(pushClient.getFcmClient(), pushMessage.getBody(), pushMessage.getAttributes(), pushMessage.getPriority(), token, createPushSendingCallback(mode, device, platformResult, pushMessageObject, phaser));
                    } else if (platform == Platform.HUAWEI || platform == Platform.HMS) {
                        if (pushClient.getHmsClient() == null) {
                            logger.error("Push message cannot be sent to HMS because HMS is not configured in push server.");
                            arriveAndDeregisterPhaserForMode(phaser, mode);
                            continue;
                        }
                        final String token = device.getPushToken();
                        final PushMessageSendResult.PlatformResult platformResult = sendResult.getHms();
                        pushSendingWorker.sendMessageToHms(pushClient.getHmsClient(), pushMessage.getBody(), pushMessage.getAttributes(), pushMessage.getPriority(), token, createPushSendingCallback(mode, device, platformResult, pushMessageObject, phaser));
                    }
                }
            }
        }
        phaser.arriveAndAwaitAdvance();
        return mode == Mode.SYNCHRONOUS ? sendResult : new BasePushMessageSendResult(mode);
    }

    private PushSendingCallback createPushSendingCallback(final Mode mode, final PushDeviceRegistrationEntity device, final PushMessageSendResult.PlatformResult platformResult, final PushMessageEntity pushMessageObject, final Phaser phaser) {
        return sendingResult -> {
            try {
                switch (sendingResult) {
                    case OK -> {
                        platformResult.setSent(platformResult.getSent() + 1);
                        updateStatusAndPersist(pushMessageObject, PushMessageEntity.Status.SENT);
                    }
                    case PENDING -> {
                        platformResult.setPending(platformResult.getPending() + 1);
                        updateStatusAndPersist(pushMessageObject, PushMessageEntity.Status.PENDING);
                    }
                    case FAILED -> {
                        platformResult.setFailed(platformResult.getFailed() + 1);
                        updateStatusAndPersist(pushMessageObject, PushMessageEntity.Status.FAILED);
                    }
                    case FAILED_DELETE -> {
                        platformResult.setFailed(platformResult.getFailed() + 1);
                        updateStatusAndPersist(pushMessageObject, PushMessageEntity.Status.FAILED);
                        pushDeviceRepository.delete(device);
                    }
                }
                platformResult.setTotal(platformResult.getTotal() + 1);
            } catch (Exception e) {
                logger.error("System error when sending notification: {}", e.getMessage(), e);
            } finally {
                arriveAndDeregisterPhaserForMode(phaser, mode);
            }
        };
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
    public void sendCampaignMessage(String appId, Platform platform, ApnsEnvironment environment, String token, PushMessageBody pushMessageBody, String userId, Long deviceId, String activationId) throws PushServerException {
        sendCampaignMessage(appId, platform, environment, token, pushMessageBody, null, Priority.HIGH, userId, deviceId, activationId);
    }

    /**
     * Send push message content with related message attributes to provided device (platform and token) using
     * credentials for given application. Return the result in the callback.
     *
     * @param appId App ID.
     * @param platform Mobile platform (APNs, FCM, HMS).
     * @param environment APNs environment (optional).
     * @param token Push message token.
     * @param pushMessageBody Push message body.
     * @param attributes Push message attributes.
     * @param priority Push message priority.
     * @param userId User to be notified by the campaign message (used for logging purpose).
     * @param deviceId Device owned by the user to be notified about the campaign message (used for logging purpose).
     * @param activationId Activation ID associated with the device (used for logging purpose).
     * @throws PushServerException In case any issue happens while sending the push message. Detailed information about
     * the error can be found in exception message.
     */
    public void sendCampaignMessage(final String appId, final Platform platform, final ApnsEnvironment environment, final String token, final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final Priority priority, final String userId, final Long deviceId, final String activationId) throws PushServerException {
        final AppRelatedPushClient pushClient = prepareClients(appId);

        final PushMessageEntity pushMessageObject = pushMessageDAO.storePushMessageObject(pushMessageBody, attributes, userId, activationId, deviceId);

        switch (platform) {
            case IOS, APNS -> {
                final String environmentAppConfig = pushClient.getAppCredentials().getApnsEnvironment();
                final ApnsEnvironment apnsEnvironment = environment != null ? resolveApnsEnvironment(environment.getKey(), environmentAppConfig) : resolveApnsEnvironment(null, environmentAppConfig);
                if (apnsEnvironment == null) {
                    logger.error("Campaign push message cannot be sent because APNs environment configuration failed. Check configuration of application property 'powerauth.push.service.apns.useDevelopment'.");
                    return;
                }
                final ApnsClient apnsClient = apnsEnvironment == ApnsEnvironment.PRODUCTION ? pushClient.getApnsClientProduction() : pushClient.getApnsClientDevelopment();
                pushSendingWorker.sendMessageToApns(apnsClient, pushMessageBody, attributes, priority, token, pushClient.getAppCredentials().getApnsBundle(), createPushSendingCallback(token, pushMessageObject, pushClient));
            }
            case ANDROID, FCM ->
                    pushSendingWorker.sendMessageToFcm(pushClient.getFcmClient(), pushMessageBody, attributes, priority, token, createPushSendingCallback(token, pushMessageObject, pushClient));
            case HUAWEI, HMS ->
                    pushSendingWorker.sendMessageToHms(pushClient.getHmsClient(), pushMessageBody, attributes, priority, token, createPushSendingCallback(token, pushMessageObject, pushClient));
        }
    }

    private PushSendingCallback createPushSendingCallback(final String token, final PushMessageEntity pushMessageObject, final AppRelatedPushClient pushClient) {
        return result -> {
            switch (result) {
                case OK -> updateStatusAndPersist(pushMessageObject, PushMessageEntity.Status.SENT);
                case PENDING -> updateStatusAndPersist(pushMessageObject, PushMessageEntity.Status.PENDING);
                case FAILED -> updateStatusAndPersist(pushMessageObject, PushMessageEntity.Status.FAILED);
                case FAILED_DELETE -> {
                    updateStatusAndPersist(pushMessageObject, PushMessageEntity.Status.FAILED);
                    pushDeviceRepository.deleteAllByAppCredentialsIdAndPushToken(pushClient.getAppCredentials().getId(), token);
                }
            }
        };
    }

    // Return list of devices related to given user or activation ID (if present). List of devices is related to particular application as well.
    private List<PushDeviceRegistrationEntity> getPushDevices(Long appCredentialsId, String userId, String activationId) throws PushServerException {
        if (userId == null || userId.isEmpty()) {
            logger.error("No userId was specified");
            throw new PushServerException("No userId was specified");
        }

        final List<PushDeviceRegistrationEntity> devices;
        if (activationId != null) { // in case the message should go to the specific device
            devices = pushDeviceRepository.findByUserIdAndAppCredentialsIdAndActivationId(userId, appCredentialsId, activationId);
        } else {
            devices = pushDeviceRepository.findByUserIdAndAppCredentialsId(userId, appCredentialsId);
        }

        if (devices.isEmpty()) {
            logger.warn("No device found for userId={}, appCredentialsId={}, activationId={}", userId, appCredentialsId, activationId);
        }

        return devices;
    }

    private AppRelatedPushClient prepareClients(String appId) throws PushServerException {
        final AppRelatedPushClient pushClient = appRelatedPushClientCache.get(appId);
        if (pushClient == null) {
            throw new PushServerException("AppCredentials not found: " + appId);
        }
        return pushClient;
    }


    // Use validator to check there are no errors in push message
    private void validatePushMessage(PushMessage pushMessage) throws PushServerException {
        final String error = PushMessageValidator.validatePushMessage(pushMessage);
        if (error != null) {
            logger.warn(error);
            throw new PushServerException(error);
        }
    }

    // Store push message in case storing of push messages is enabled
    private PushMessageEntity storePushMessage(PushMessage pushMessage, PushDeviceRegistrationEntity device) throws PushServerException {
        if (configuration.isMessageStorageEnabled()) {
            return pushMessageDAO.storePushMessageObject(pushMessage.getBody(), pushMessage.getAttributes(), pushMessage.getUserId(), pushMessage.getActivationId(), device.getId());
        }
        return null;
    }

    // Update push message status and persist it in case entity is not null
    private void updateStatusAndPersist(PushMessageEntity pushMessageObject, PushMessageEntity.Status status) {
        if (pushMessageObject != null) {
            pushMessageObject.setStatus(status);
            pushMessageDAO.save(pushMessageObject);
        }
    }

    private ApnsEnvironment resolveApnsEnvironment(final String environmentDevice, final String environmentAppConfig) {
        if (environmentDevice != null) {
            final ApnsEnvironment envDevice = ApnsEnvironment.fromString(environmentDevice);
            if (!configuration.isApnsUseDevelopment() && envDevice == ApnsEnvironment.DEVELOPMENT) {
                logger.warn("Invalid configuration: server is configured with APNs in production mode, however push device is registered in development mode.");
                return null;
            }
            return envDevice;
        }
        // Fallback in case device was registered without environment, either use application credentials configuration or global server setting as last fallback
        if ((environmentAppConfig != null && environmentAppConfig.equals(ApnsEnvironment.DEVELOPMENT.getKey()))
                || configuration.isApnsUseDevelopment()) {
            return ApnsEnvironment.DEVELOPMENT;
        }
        return ApnsEnvironment.PRODUCTION;
    }

    /**
     * Arrive and deregister phaser based on the mode. For {@link Mode#SYNCHRONOUS}, the method provides deregistration. Otherwise,
     * for {@link Mode#ASYNCHRONOUS}, it is a noop method.
     *
     * @param phaser Phaser.
     * @param mode Mode.
     */
    private static void arriveAndDeregisterPhaserForMode(Phaser phaser, Mode mode) {
        if (mode == Mode.SYNCHRONOUS && phaser != null) {
            phaser.arriveAndDeregister();
        }
    }

    /**
     * Register phaser based on the mode. For {@link Mode#SYNCHRONOUS}, the method provides registration. Otherwise,
     * for {@link Mode#ASYNCHRONOUS}, it is a noop method.
     *
     * @param phaser Phaser.
     * @param mode Mode.
     */
    private static void registerPhaserForMode(Phaser phaser, Mode mode) {
        if (mode == Mode.SYNCHRONOUS && phaser != null) {
            phaser.register();
        }
    }

}
