/*
 * Copyright 2020 Wultra s.r.o.
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

package io.getlime.push.service.apns;

/**
 * APNs rejection reason codes, documented on the Apple site:
 * <a href="https://developer.apple.com/documentation/usernotifications/setting_up_a_remote_notification_server/handling_notification_responses_from_apns">Handling Notification Responses from APNs</a>
 *
 * @author Petr Dvorak, petr@wultra
 */
public enum ApnsRejectionReason {

    /**
     * BadDeviceToken
     */
    BAD_DEVICE_TOKEN("BadDeviceToken"),

    /**
     * DeviceTokenNotForTopic
     */
    DEVICE_TOKEN_NOT_FOR_TOPIC("DeviceTokenNotForTopic"),

    /**
     * TopicDisallowed
     */
    TOPIC_DISALLOWED("TopicDisallowed"),

    /**
     * BadCollapseId
     */
    BAD_COLLAPSE_ID("BadCollapseId"),

    /**
     * BadExpirationDate
     */
    BAD_EXPIRATION_DATE("BadExpirationDate"),

    /**
     * BadMessageId
     */
    BAD_MESSAGE_ID("BadMessageId"),

    /**
     * BadPriority
     */
    BAD_PRIORITY("BadPriority"),

    /**
     * BadTopic
     */
    BAD_TOPIC("BadTopic"),

    /**
     * DuplicateHeaders
     */
    DUPLICATE_HEADERS("DuplicateHeaders"),

    /**
     * IdleTimeout
     */
    IDLE_TIMEOUT("IdleTimeout"),

    /**
     * InvalidPushType
     */
    INVALID_PUSH_TYPE("InvalidPushType"),

    /**
     * MissingDeviceToken
     */
    MISSING_DEVICE_TOKEN("MissingDeviceToken"),

    /**
     * MissingTopic
     */
    MISSING_TOPIC("MissingTopic"),

    /**
     * PayloadEmpty
     */
    PAYLOAD_EMPTY("PayloadEmpty"),

    /**
     * BadCertificate
     */
    BAD_CERTIFICATE("BadCertificate"),

    /**
     * BadCertificateEnvironment
     */
    BAD_CERTIFICATE_ENVIRONMENT("BadCertificateEnvironment"),

    /**
     * ExpiredProviderToken
     */
    EXPIRED_PROVIDER_TOKEN("ExpiredProviderToken"),

    /**
     * Forbidden
     */
    FORBIDDEN("Forbidden"),

    /**
     * InvalidProviderToken
     */
    INVALID_PROVIDER_TOKEN("InvalidProviderToken"),

    /**
     * MissingProviderToken
     */
    MISSING_PROVIDER_TOKEN("MissingProviderToken"),

    /**
     * BadPath
     */
    BAD_PATH("BadPath"),

    /**
     * MethodNotAllowed
     */
    METHOD_NOT_ALLOWED("MethodNotAllowed"),

    /**
     * Unregistered
     */
    UNREGISTERED("Unregistered"),

    /**
     * PayloadTooLarge
     */
    PAYLOAD_TOO_LARGE("PayloadTooLarge"),

    /**
     * TooManyProviderTokenUpdates
     */
    TOO_MANY_PROVIDER_TOKEN_UPDATES("TooManyProviderTokenUpdates"),

    /**
     * TooManyRequests
     */
    TOO_MANY_REQUESTS("TooManyRequests"),

    /**
     * InternalServerError
     */
    INTERNAL_SERVER_ERROR("InternalServerError"),

    /**
     * ServiceUnavailable
     */
    SERVICE_UNAVAILABLE("ServiceUnavailable"),

    /**
     * Shutdown
     */
    SHUTDOWN("Shutdown");


    private final String reasonText;

    /**
     * Constructor with the reason text.
     * @param reasonText Reason text.
     */
    ApnsRejectionReason(final String reasonText) {
        this.reasonText = reasonText;
    }

    /**
     * Get reason text.
     * @return Reason text.
     */
    public String getReasonText() {
        return this.reasonText;
    }

    /**
     * Check if the internal reason text is equal to provided string.
     * @param reasonText Reason text to be checked.
     * @return True if the internal reason text is equal to provided string, false otherwise.
     */
    public boolean isEqualToText(String reasonText) {
        return this.reasonText.equals(reasonText);
    }

}
