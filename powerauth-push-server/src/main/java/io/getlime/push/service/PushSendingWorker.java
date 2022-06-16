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
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.wultra.core.rest.client.base.RestClientException;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.FcmMissingTokenException;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessageAttributes;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.enumeration.Priority;
import io.getlime.push.service.apns.ApnsRejectionReason;
import io.getlime.push.service.fcm.FcmClient;
import io.getlime.push.service.fcm.FcmModelConverter;
import io.getlime.push.service.fcm.model.FcmSuccessResponse;
import io.getlime.push.util.CaCertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PushSendingWorker {

    private static final Logger logger = LoggerFactory.getLogger(PushSendingWorker.class);

    // FCM data only notification keys
    private static final String FCM_NOTIFICATION_KEY            = "_notification";

    // Expected response String from FCM
    private static final String FCM_RESPONSE_VALID_REGEXP       = "projects/.+/messages/.+";


    // Maximum Android TTL value in seconds, see: https://firebase.google.com/docs/cloud-messaging/concept-options#ttl
    private static final int ANDROID_TTL_SECONDS_MAX            = 2_419_200;

    private final PushServiceConfiguration pushServiceConfiguration;
    private final FcmModelConverter fcmConverter;
    private final CaCertUtil caCertUtil;

    /**
     * Constructor with push service configuration, model converter and CA certificate utility class.
     * @param pushServiceConfiguration Push service configuration.
     * @param fcmConverter FCM converter class.
     * @param caCertUtil CA certificate utility class.
     */
    @Autowired
    public PushSendingWorker(PushServiceConfiguration pushServiceConfiguration, FcmModelConverter fcmConverter, CaCertUtil caCertUtil) {
        this.pushServiceConfiguration = pushServiceConfiguration;
        this.fcmConverter = fcmConverter;
        this.caCertUtil = caCertUtil;
    }

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
     * Send message to Android platform.
     * @param fcmClient Instance of the FCM client used for sending the notifications.
     * @param pushMessageBody Push message contents.
     * @param attributes Push message attributes.
     * @param priority Push message priority.
     * @param pushToken Push token used to deliver the message.
     * @param callback Callback that is called after the asynchronous executions is completed.
     */
    void sendMessageToAndroid(final FcmClient fcmClient, final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final Priority priority, final String pushToken, final PushSendingCallback callback) {

        // Build Android message
        final Message message = buildAndroidMessage(pushMessageBody, attributes, priority, pushToken);

        // Extraction of FCM success response
        final Consumer<ResponseEntity<FcmSuccessResponse>> onSuccess = responseEntity -> {
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
        };

        // Callback when FCM request fails
        final Consumer<Throwable> onError = t -> {
            if (t instanceof RestClientException) {
                final MessagingErrorCode errorCode = fcmConverter.convertExceptionToErrorCode((RestClientException) t);
                logger.warn("FCM server returned error response: {}.", ((RestClientException) t).getResponse());
                switch (errorCode) {
                    case UNREGISTERED:
                        logger.info("Push message rejected by FCM gateway, device registration for token: {} is invalid and will be removed. Error: {}", pushToken, errorCode);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                        return;

                    case UNAVAILABLE:
                    case INTERNAL:
                    case QUOTA_EXCEEDED:
                        // TODO - implement throttling of messages, see:
                        // https://firebase.google.com/docs/cloud-messaging/admin/errors
                        logger.warn("Push message rejected by FCM gateway, message status set to PENDING. Error: {}", errorCode);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.PENDING);
                        return;

                    case SENDER_ID_MISMATCH:
                    case THIRD_PARTY_AUTH_ERROR:
                    case INVALID_ARGUMENT:
                        logger.warn("Push message rejected by FCM gateway. Error: {}", errorCode);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
                        return;

                    default:
                        logger.error("Unexpected error code received from FCM gateway. Error: {}", errorCode);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
                        return;
                }
            }

            // Unexpected errors
            logger.error("Unexpected error occurred while sending push message: {}.", t.getMessage());
            logger.debug("Exception details:", t);
            callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED);
        };

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
     * Build Android Message object from Push message body.
     * @param pushMessageBody Push message body.
     * @param attributes Push message attributes.
     * @param priority Push message priority.
     * @param pushToken Push token.
     * @return Android Message object.
     */
    private Message buildAndroidMessage(final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final Priority priority, final String pushToken) {
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

        // Calculate TTL and set it if the TTL is within reasonable limits
        final Instant validUntil = pushMessageBody.getValidUntil();
        if (validUntil != null) {
            final long validUntilMs = validUntil.toEpochMilli();
            final long currentTimeMs = System.currentTimeMillis();
            final long ttlInSeconds = (validUntilMs - currentTimeMs) / 1000;

            if (ttlInSeconds > 0 && ttlInSeconds < ANDROID_TTL_SECONDS_MAX) {
                androidConfigBuilder.setTtl(ttlInSeconds);
            }
        }

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
        } else if (attributes == null || !attributes.getSilent()) { // if there are no attributes, assume the message is not silent
            androidConfigBuilder.setNotification(notification);
        }

        return Message.builder()
                .setToken(pushToken)
                .putAllData(data)
                .setAndroidConfig(androidConfigBuilder.build())
                .build();
    }

    // iOS related methods

    /**
     * Prepare and connect APNs client.
     *
     * @param teamId APNs team ID.
     * @param keyId APNs key ID.
     * @param apnsPrivateKey Bytes of the APNs private key (contents of the *.p8 file).
     * @param environment APNs environment. The "development" or "production" values can be used to override global
     *                    settings. If "null" or unknown value is passed, the global configuration is used.
     * @return New instance of APNs client.
     * @throws PushServerException In case an error occurs (private key is invalid, unable to connect
     *   to APNs service due to SSL issue, ...).
     */
    ApnsClient prepareApnsClient(String teamId, String keyId, byte[] apnsPrivateKey, String environment) throws PushServerException {
        final ApnsClientBuilder apnsClientBuilder = new ApnsClientBuilder()
                .setProxyHandlerFactory(apnsClientProxy())
                .setConcurrentConnections(pushServiceConfiguration.getConcurrentConnections())
                .setConnectionTimeout(Duration.ofMillis(pushServiceConfiguration.getApnsConnectTimeout()))
                .setIdlePingInterval(Duration.ofMillis(pushServiceConfiguration.getIdlePingInterval()))
                .setTrustedServerCertificateChain(caCertUtil.allCerts());

        // Determine the APNs environment by looking at per-app config first and if no recognized value is present,
        // use the default configuration. Note that "equalsIgnoreCase" optimizes for null parameter, so the first two
        // if-else branches are performed quickly (we do not need to worry about the fact that "null" will likely be
        // the most common value there).
        if ("development".equalsIgnoreCase(environment)) {
            logger.info("Using APNs development host.");
            apnsClientBuilder.setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST);
        } else if ("production".equalsIgnoreCase(environment)) {
            logger.info("Using APNs production host.");
            apnsClientBuilder.setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST);
        } else {
            if (environment != null) {
                logger.warn("Invalid APNS host environment specified: \"{}\". Use \"development\" or \"production\".", environment);
            }
            if (pushServiceConfiguration.isApnsUseDevelopment()) {
                logger.info("Using APNs development host by applying the global push server configuration.");
                apnsClientBuilder.setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST);
            } else {
                logger.info("Using APNs production host by applying the global push server configuration.");
                apnsClientBuilder.setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST);
            }
        }

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
     * @param iosTopic APNs topic, usually same as bundle ID.
     * @param callback Callback that is called after the asynchronous executions is completed.
     */
    void sendMessageToIos(final ApnsClient apnsClient, final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final Priority priority, final String pushToken, final String iosTopic, final PushSendingCallback callback) {

        final String token = TokenUtil.sanitizeTokenString(pushToken);
        final boolean isSilent = attributes != null && attributes.getSilent(); // In case there are no attributes, the message is not silent
        final String payload = buildApnsPayload(pushMessageBody, isSilent);
        final Instant validUntil = pushMessageBody.getValidUntil();
        final PushType pushType = isSilent ? PushType.BACKGROUND : PushType.ALERT; // iOS 13 and higher requires apns-push-type value to be set
        final DeliveryPriority deliveryPriority = (Priority.NORMAL == priority) ? DeliveryPriority.CONSERVE_POWER : DeliveryPriority.IMMEDIATE;
        final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, iosTopic, payload, validUntil, deliveryPriority, pushType, pushMessageBody.getCollapseKey());
        final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = apnsClient.sendNotification(pushNotification);

        sendNotificationFuture.whenCompleteAsync((response, cause) -> {
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
                        logger.warn("Notification was sent to incorrect topic: {}.", iosTopic);
                        logger.debug("Deleting push token: {}", pushToken);
                        callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
                    } else if (ApnsRejectionReason.TOPIC_DISALLOWED.isEqualToText(rejectionReason)) {
                        logger.warn("Notification was sent to incorrect topic: {}.", iosTopic);
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
        });
    }

    /**
     * Method to build APNs message payload.
     *
     * @param push     Push message object with APNs data.
     * @param isSilent Indicates if the message is silent or not.
     * @return String with APNs JSON payload.
     */
    private String buildApnsPayload(PushMessageBody push, boolean isSilent) {
        final ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
        if (!isSilent) { // include alert, body, sound and category only in case push message is not silent.
            payloadBuilder
                    .setAlertTitle(push.getTitle())
                    .setLocalizedAlertTitle(push.getTitleLocKey(), push.getTitleLocArgs())
                    .setAlertBody(push.getBody())
                    .setLocalizedAlertMessage(push.getBodyLocKey(), push.getBodyLocArgs())
                    .setSound(push.getSound())
                    .setCategoryName(push.getCategory());
        }
        payloadBuilder
                .setBadgeNumber(push.getBadge())
                .setContentAvailable(isSilent)
                .setThreadId(push.getCollapseKey());
        final Map<String, Object> extras = push.getExtras();
        if (extras != null) {
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                payloadBuilder.addCustomProperty(entry.getKey(), entry.getValue());
            }
        }
        return payloadBuilder.build();
    }
}