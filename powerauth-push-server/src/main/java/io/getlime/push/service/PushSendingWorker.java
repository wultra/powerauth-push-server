/*
 * Copyright 2018 Wultra s.r.o.
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

import com.eatthepath.pushy.apns.*;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import com.eatthepath.pushy.apns.proxy.HttpProxyHandlerFactory;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import com.wultra.core.rest.client.base.RestClientException;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.FcmMissingTokenException;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessageAttributes;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.enumeration.ApnsEnvironment;
import io.getlime.push.model.enumeration.Priority;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.service.apns.ApnsRejectionReason;
import io.getlime.push.service.fcm.FcmClient;
import io.getlime.push.service.fcm.FcmModelConverter;
import io.getlime.push.service.fcm.model.FcmSuccessResponse;
import io.getlime.push.service.hms.HmsClient;
import io.getlime.push.service.hms.HmsSendResponse;
import io.getlime.push.service.hms.request.AndroidNotification.Importance;
import io.getlime.push.service.hms.request.ClickAction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.opentelemetry.context.Context;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;


/**
 * Service responsible for sending push notifications.
 *
 * @author Petr Dvorak, petr@wulta.com
 */
@Service
@AllArgsConstructor
@Slf4j
public class PushSendingWorker {

    // FCM data only notification keys
    private static final String FCM_NOTIFICATION_KEY            = "_notification";

    // Expected response String from FCM
    private static final String FCM_RESPONSE_VALID_REGEXP       = "projects/.+/messages/.+";


    // Maximum Android TTL value in seconds, see: https://firebase.google.com/docs/cloud-messaging/concept-options#ttl
    private static final int ANDROID_TTL_SECONDS_MAX            = 2_419_200;

    private final PushServiceConfiguration pushServiceConfiguration;
    private final FcmModelConverter fcmConverter;
    private final CaCertificateService caCertificateService;

    // Android related methods

    /**
     * Prepares an FCM service client with a provided server key.
     *
     * @param projectId FCM project ID.
     * @param privateKey FCM private key.
     * @return A new instance of FCM client.
     */
    FcmClient prepareFcmClient(String projectId, byte[] privateKey) throws PushServerException {
        final FcmClient fcmClient = new FcmClient(projectId, privateKey, pushServiceConfiguration, fcmConverter);
        if (pushServiceConfiguration.isFcmProxyEnabled()) {
            String proxyHost = pushServiceConfiguration.getFcmProxyHost();
            int proxyPort = pushServiceConfiguration.getFcmProxyPort();
            String proxyUsername = pushServiceConfiguration.getFcmProxyUsername();
            String proxyPassword = pushServiceConfiguration.getFcmProxyPassword();
            if (proxyUsername != null && proxyUsername.isEmpty()) {
                proxyUsername = null;
            }
            if (proxyPassword != null && proxyPassword.isEmpty()) {
                proxyPassword = null;
            }
            fcmClient.setProxySettings(proxyHost, proxyPort, proxyUsername, proxyPassword);
        }
        fcmClient.initializeRestClient();
        final String fcmUrl = pushServiceConfiguration.getFcmSendMessageUrl();
        if (fcmUrl.contains("projects/%s/")) {
            // Initialize Google Credential for production FCM URL
            fcmClient.initializeGoogleCredential();
            // Configure project ID in FCM URL in case the project ID parameter is expected in configured URL
            fcmClient.setFcmSendMessageUrl(String.format(fcmUrl, projectId));
        } else {
            // Set FCM url as is (for testing)
            fcmClient.setFcmSendMessageUrl(fcmUrl);
        }
        return fcmClient;
    }

    /**
     * Prepares an HMS (Huawei Mobile Services) service client.
     *
     * @param credentials Credentials.
     * @return A new instance of HMS client.
     */
    HmsClient prepareHmsClient(final AppCredentialsEntity credentials) {
        logger.info("Initializing HmsClient");
        return new HmsClient(pushServiceConfiguration, credentials);
    }

    /**
     * Send message to Android platform.
     * @param fcmClient Instance of the FCM client used for sending the notifications.
     * @param pushMessageBody Push message contents.
     * @param attributes Push message attributes.
     * @param priority Push message priority.
     * @param pushToken Push token used to deliver the message.
     * @param callback Callback that is called after the asynchronous executions is completed.
     */
    void sendMessageToFcm(final FcmClient fcmClient, final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final Priority priority, final String pushToken, final PushSendingCallback callback) {

        final Message message = buildFcmMessage(pushMessageBody, attributes, priority, pushToken);

        // Extraction of FCM success response
        final Consumer<ResponseEntity<FcmSuccessResponse>> onSuccess = Context.current().wrapConsumer(responseEntity -> {
            final FcmSuccessResponse response = responseEntity.getBody();
            if (response != null && response.getName() != null) {
                if (response.getName().matches(FCM_RESPONSE_VALID_REGEXP)) {
                    logger.info("Notification sent successfully, response: {}.", response.getName());
                    callback.didFinishSendingMessage(PushSendingCallback.Result.OK);
                } else {
                    logger.error("Invalid response received from FCM, notification sending failed - unexpected response name: {}.", response.getName());
                    callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
                }
            } else {
                // This state should not happen, only in case when response from server is invalid
                logger.error("Invalid response received from FCM, notification sending failed - empty or invalid response.");
                callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
            }
        });

        // Callback when FCM request fails
        final Consumer<Throwable> onError = Context.current().wrapConsumer(t -> {
            if (t instanceof final RestClientException restClientException) {
                final MessagingErrorCode errorCode = fcmConverter.convertExceptionToErrorCode(restClientException);
                logger.warn("FCM server returned error response: {}.", (restClientException).getResponse());
                switch (errorCode) {
                    case UNREGISTERED, INVALID_ARGUMENT -> {
                        logger.info("Push message rejected by FCM gateway, device registration for token: {} is invalid and will be removed. Error: {}", pushToken, errorCode);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                        return;
                    }
                    case UNAVAILABLE, INTERNAL, QUOTA_EXCEEDED -> {
                        // TODO - implement throttling of messages, see:
                        // https://firebase.google.com/docs/cloud-messaging/admin/errors
                        logger.warn("Push message rejected by FCM gateway, message status set to PENDING. Error: {}", errorCode);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.PENDING);
                        return;
                    }
                    case SENDER_ID_MISMATCH, THIRD_PARTY_AUTH_ERROR -> {
                        logger.warn("Push message rejected by FCM gateway. Error: {}", errorCode);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
                        return;
                    }
                    default -> {
                        logger.error("Unexpected error code received from FCM gateway. Error: {}", errorCode);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
                        return;
                    }
                }
            }

            // Unexpected errors
            logger.error("Unexpected error occurred while sending push message: {}.", t.getMessage());
            logger.debug("Exception details:", t);
            callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
        });

        // Perform request to FCM asynchronously, either of the consumers is called in case of success or error
        try {
            fcmClient.exchange(message, false, onSuccess, onError);
        } catch (FcmMissingTokenException ex) {
            logger.error("Error occurred: {}", ex.getMessage());
            logger.debug("Exception detail:", ex);
            callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
        }
    }

    /**
     * Send message to Huawei platform.
     *
     * @param hmsClient Instance of the HMS client used for sending the notifications.
     * @param pushMessageBody Push message contents.
     * @param attributes Push message attributes.
     * @param priority Push message priority.
     * @param pushToken Push token used to deliver the message.
     * @param callback Callback that is called after the asynchronous executions is completed.
     * @throws PushServerException In case any issue happens while sending the push message.
     */
    void sendMessageToHms(final HmsClient hmsClient, final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final Priority priority, final String pushToken, final PushSendingCallback callback) throws PushServerException {
        final io.getlime.push.service.hms.request.Message message = buildHmsMessage(pushMessageBody, attributes, priority, pushToken);

        final Consumer<HmsSendResponse> successConsumer = Context.current().wrapConsumer(response -> {
            final String requestId = response.requestId();
            if (HmsClient.SUCCESS_CODE.equals(response.code())) {
                logger.info("Notification sent successfully, request ID: {}", requestId);
                callback.didFinishSendingMessage(PushSendingCallback.Result.OK);
            } else {
                logger.error("Notification sending failed, request ID: {}, code: {}, message: {}", requestId, response.code(), response.msg());
                callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
            }
        });

        final Consumer<Throwable> throwableConsumer = Context.current().wrapConsumer(throwable -> {
            logger.error("Invalid response received from HSM, notification sending failed.", throwable);
            callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
        });

        hmsClient.sendMessage(message, false)
                .subscribe(successConsumer, throwableConsumer);
    }

    /**
     * Build FCM Message object from Push message body.
     * @param pushMessageBody Push message body.
     * @param attributes Push message attributes.
     * @param priority Push message priority.
     * @param pushToken Push token.
     * @return Android Message object.
     */
    private Message buildFcmMessage(final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final Priority priority, final String pushToken) {
        // convert data from Map<String, Object> to Map<String, String>
        final Map<String, Object> extras = pushMessageBody.getExtras();
        final Map<String, String> data = new LinkedHashMap<>();
        if (extras != null) {
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                data.put(entry.getKey(), entry.getValue().toString());
            }
        }

        final AndroidConfig.Builder androidConfigBuilder = AndroidConfig.builder()
                .setCollapseKey(pushMessageBody.getCollapseKey());

        calculateTtl(pushMessageBody.getValidUntil())
                .ifPresent(androidConfigBuilder::setTtl);

        final AndroidNotification.Priority deliveryPriority = (Priority.NORMAL == priority) ?
                AndroidNotification.Priority.DEFAULT : AndroidNotification.Priority.HIGH;

        final AndroidNotification.Builder builder = AndroidNotification.builder()
                .setPriority(deliveryPriority)
                .setTitle(pushMessageBody.getTitle())
                .setTitleLocalizationKey(pushMessageBody.getTitleLocKey())
                .setBody(pushMessageBody.getBody())
                .setBodyLocalizationKey(pushMessageBody.getBodyLocKey())
                .setIcon(pushMessageBody.getIcon())
                .setSound(pushMessageBody.getSound())
                .setTag(pushMessageBody.getCategory());

        if (pushMessageBody.getTitleLocArgs() != null) {
            builder.addAllTitleLocalizationArgs(Arrays.asList(pushMessageBody.getTitleLocArgs()));
        }
        if (pushMessageBody.getBodyLocArgs() != null) {
            builder.addAllBodyLocalizationArgs(Arrays.asList(pushMessageBody.getBodyLocArgs()));
        }

        final AndroidNotification notification = builder.build();

        if (pushServiceConfiguration.isFcmDataNotificationOnly()) { // notification only through data map
            data.put(FCM_NOTIFICATION_KEY, fcmConverter.convertNotificationToString(notification));
        } else if (!isMessageSilent(attributes)) {
            androidConfigBuilder.setNotification(notification);
        }

        final DeliveryPriority deliveryPriorityApns = (Priority.NORMAL == priority) ? DeliveryPriority.CONSERVE_POWER : DeliveryPriority.IMMEDIATE;
        final ApnsConfig apnsConfig = ApnsPayloadBuilder.buildPayloadForFcm(pushMessageBody, isMessageSilent(attributes), deliveryPriorityApns);

        return Message.builder()
                .setToken(pushToken)
                .putAllData(data)
                .setAndroidConfig(androidConfigBuilder.build())
                .setApnsConfig(apnsConfig)
                .build();
    }

    /**
     * Build HMS Message object from Push message body.
     *
     * @param pushMessageBody Push message body.
     * @param attributes Push message attributes.
     * @param priority Push message priority.
     * @param pushToken Push token.
     * @return HMS Message object.
     * @throws PushServerException In case any issue happens while building the push message.
     */
    private io.getlime.push.service.hms.request.Message buildHmsMessage(final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final Priority priority, final String pushToken) throws PushServerException {
        final var androidConfigBuilder = io.getlime.push.service.hms.request.AndroidConfig.builder()
                .collapseKey(NumberUtils.createInteger(pushMessageBody.getCollapseKey()));

        calculateTtl(pushMessageBody.getValidUntil())
                .map(Object::toString)
                .ifPresent(androidConfigBuilder::ttl);

        final Importance importance = (priority == Priority.NORMAL) ? Importance.NORMAL : Importance.HIGH;

        final var notificationBuilder = io.getlime.push.service.hms.request.AndroidNotification.builder()
                .importance(importance)
                .title(pushMessageBody.getTitle())
                .titleLocKey(pushMessageBody.getTitleLocKey())
                .body(pushMessageBody.getBody())
                .bodyLocKey(pushMessageBody.getBodyLocKey())
                .icon(pushMessageBody.getIcon())
                .sound(pushMessageBody.getSound())
                .tag(pushMessageBody.getCategory())
                .clickAction(ClickAction.builder()
                        .type(ClickAction.TYPE_START_APP)
                        .build());

        if (pushMessageBody.getTitleLocArgs() != null) {
            notificationBuilder.titleLocArgs(List.of(pushMessageBody.getTitleLocArgs()));
        }
        if (pushMessageBody.getBodyLocArgs() != null) {
            notificationBuilder.bodyLocArgs(List.of(pushMessageBody.getBodyLocArgs()));
        }

        if (!isMessageSilent(attributes)) {
            androidConfigBuilder.notification(notificationBuilder.build());
        }

        final Map<String, Object> extras = pushMessageBody.getExtras();
        final String data;
        if (extras == null) {
            data = null;
        } else {
            try {
                data = new ObjectMapper().writeValueAsString(extras);
            } catch (JsonProcessingException e) {
                throw new PushServerException("Failed to serialize extras to JSON", e);
            }
        }

        return io.getlime.push.service.hms.request.Message.builder()
                .token(List.of(pushToken))
                .android(androidConfigBuilder.build())
                .data(data)
                .build();
    }

    private static boolean isMessageSilent(final PushMessageAttributes attributes) {
        // if there are no attributes, assume the message is not silent
        return attributes != null && attributes.getSilent();
    }

    /**
     * Calculate TTL and return it if the TTL is within reasonable limits.
     *
     * @param validUntil Valid until.
     * @return TTL in seconds or empty.
     */
    private static Optional<Long> calculateTtl(final Instant validUntil) {
        if (validUntil != null) {
            final long ttlInSeconds = Duration.between(Instant.now(), validUntil).toSeconds();

            if (ttlInSeconds > 0 && ttlInSeconds < ANDROID_TTL_SECONDS_MAX) {
                return Optional.of(ttlInSeconds);
            }
        }
        return Optional.empty();
    }

    // APNs related methods

    /**
     * Prepare and connect APNs client.
     *
     * @param credentials Application Credentials.
     * @return New instance of APNs client.
     * @throws PushServerException In case an error occurs (private key is invalid, unable to connect to APNs service due to SSL issue, ...).
     * @implSpec APNS environment {@code development} or {@code production} values can be used to override global settings.
     * If {@code null} or unknown value is passed, the global configuration is used.
     */
    ApnsClient prepareApnsClient(final AppCredentialsEntity credentials, final ApnsEnvironment environment) throws PushServerException {
        final ApnsClientBuilder apnsClientBuilder = new ApnsClientBuilder()
                .setProxyHandlerFactory(apnsClientProxy())
                .setConcurrentConnections(pushServiceConfiguration.getConcurrentConnections())
                .setConnectionTimeout(Duration.ofMillis(pushServiceConfiguration.getApnsConnectTimeout()))
                .setIdlePingInterval(Duration.ofMillis(pushServiceConfiguration.getIdlePingInterval()))
                .setTrustedServerCertificateChain(caCertificateService.allCerts());

        if (environment == ApnsEnvironment.DEVELOPMENT) {
            apnsClientBuilder.setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST);
        } else {
            apnsClientBuilder.setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST);
        }

        final String teamId = credentials.getApnsTeamId();
        final String keyId = credentials.getApnsKeyId();
        final byte[] apnsPrivateKey = credentials.getApnsPrivateKey();

        try {
            final ApnsSigningKey key = ApnsSigningKey.loadFromInputStream(new ByteArrayInputStream(apnsPrivateKey), teamId, keyId);
            apnsClientBuilder.setSigningKey(key);
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            logger.error("Error occurred: {}", e.getMessage());
            logger.debug("Exception detail:", e);
            throw new PushServerException("Invalid private key.", e);
        }
        try {
            return apnsClientBuilder.build();
        } catch (SSLException e) {
            logger.error("Error occurred: {}", e.getMessage());
            logger.debug("Exception detail:", e);
            throw new PushServerException("SSL problem occurred.", e);
        }
    }

    /**
     * Prepare proxy settings for APNs client.
     *
     * @return Proxy handler factory with correct configuration.
     */
    private HttpProxyHandlerFactory apnsClientProxy() {
        if (pushServiceConfiguration.isApnsProxyEnabled()) {
            final String proxyUrl = pushServiceConfiguration.getApnsProxyHost();
            final int proxyPort = pushServiceConfiguration.getApnsProxyPort();
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

    /**
     * Send message to iOS platform.
     *
     * @param apnsClient APNs client used for sending the push message.
     * @param pushMessageBody Push message content.
     * @param attributes Push message attributes.
     * @param priority Push message priority.
     * @param pushToken Push token.
     * @param apnsTopic APNs topic, usually same as bundle ID.
     * @param callback Callback that is called after the asynchronous executions is completed.
     */
    void sendMessageToApns(final ApnsClient apnsClient, final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final Priority priority, final String pushToken, final String apnsTopic, final PushSendingCallback callback) {

        final String token = TokenUtil.sanitizeTokenString(pushToken);
        final boolean isSilent = attributes != null && attributes.getSilent(); // In case there are no attributes, the message is not silent
        final String payload = ApnsPayloadBuilder.buildPayloadForApns(pushMessageBody, isSilent);
        final Instant validUntil = pushMessageBody.getValidUntil();
        final PushType pushType = isSilent ? PushType.BACKGROUND : PushType.ALERT; // iOS 13 and higher requires apns-push-type value to be set
        final DeliveryPriority deliveryPriority = (Priority.NORMAL == priority) ? DeliveryPriority.CONSERVE_POWER : DeliveryPriority.IMMEDIATE;
        final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, apnsTopic, payload, validUntil, deliveryPriority, pushType, pushMessageBody.getCollapseKey());
        final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = apnsClient.sendNotification(pushNotification);

        sendNotificationFuture.whenCompleteAsync(Context.current().wrapConsumer((response, cause) -> {
            if (response != null) {
                final UUID apnsId = response.getApnsId();
                if (response.isAccepted()) {
                    logger.info("Notification sent successfully, APNs ID: {}.", apnsId);
                    callback.didFinishSendingMessage(PushSendingCallback.Result.OK);
                } else {
                    final Optional<String> rejectionReasonOptional = response.getRejectionReason();
                    String rejectionReason = null;
                    if (rejectionReasonOptional.isPresent()) {
                        rejectionReason = rejectionReasonOptional.get();
                    }

                    if (ApnsRejectionReason.EXPIRED_PROVIDER_TOKEN.isEqualToText(rejectionReason)) {
                        logger.info("Notification rejected by the APNs gateway due to expired push token, APNs ID: {}.", response.getApnsId());
                    } else {
                        logger.info("Notification rejected by the APNs gateway: {}, APNs ID: {}.", rejectionReason != null ? rejectionReason : "UnknownReason", response.getApnsId());
                    }

                    // Determine if the push token should be deleted.
                    if (ApnsRejectionReason.BAD_DEVICE_TOKEN.isEqualToText(rejectionReason)) {
                        logger.debug("Deleting push token: {}.", pushToken);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                    } else if (ApnsRejectionReason.DEVICE_TOKEN_NOT_FOR_TOPIC.isEqualToText(rejectionReason)) {
                        logger.warn("Notification was sent to incorrect topic: {}.", apnsTopic);
                        logger.debug("Deleting push token: {}", pushToken);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                    } else if (ApnsRejectionReason.TOPIC_DISALLOWED.isEqualToText(rejectionReason)) {
                        logger.warn("Notification was sent to incorrect topic: {}.", apnsTopic);
                        logger.debug("Deleting push token: {}.", pushToken);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                    } else if (ApnsRejectionReason.EXPIRED_PROVIDER_TOKEN.isEqualToText(rejectionReason)) {
                        logger.debug("Deleting push token: {}.", pushToken);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                    } else if (ApnsRejectionReason.INVALID_PROVIDER_TOKEN.isEqualToText(rejectionReason)) {
                        logger.debug("Deleting push token: {}.", pushToken);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                    } else if (response.getTokenInvalidationTimestamp().isPresent()) {
                        logger.info("Push token is invalid as of: {}.", response.getTokenInvalidationTimestamp().get());
                        logger.debug("Deleting push token: {}.", pushToken);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                    } else {
                        logger.debug("Sending the push message failed with push token: {}.", pushToken);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
                    }
                }
            } else {
                // In this case, the delivery failed because the future failed, not because APNs rejected the
                // notification payload. This means that we should be able to attempt resending the message.
                logger.error("Push message sending failed. Error: {}", cause.getMessage());
                logger.debug("Exception detail:", cause);
                callback.didFinishSendingMessage(PushSendingCallback.Result.PENDING);
            }
        }));
    }

}