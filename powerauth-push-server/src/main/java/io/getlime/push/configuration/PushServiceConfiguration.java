/*
 * Copyright 2017 Wultra s.r.o.
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

package io.getlime.push.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@ConfigurationProperties("ext")
public class PushServiceConfiguration {

    @Value("${powerauth.push.service.applicationName}")
    private String pushServerName;

    @Value("${powerauth.push.service.applicationDisplayName}")
    private String pushServerDisplayName;

    @Value("${powerauth.push.service.applicationEnvironment}")
    private String pushServerEnvironment;

    // APNs Configuration

    @Value("${powerauth.push.service.apns.useDevelopment}")
    private boolean apnsUseDevelopment;

    @Value("${powerauth.push.service.apns.proxy.enabled}")
    private boolean apnsProxyEnabled;

    @Value("${powerauth.push.service.apns.proxy.url}")
    private String apnsProxyUrl;

    @Value("${powerauth.push.service.apns.proxy.port}")
    private int apnsProxyPort;

    @Value("${powerauth.push.service.apns.proxy.username}")
    private String apnsProxyUsername;

    @Value("${powerauth.push.service.apns.proxy.password}")
    private String apnsProxyPassword;

    // FCM Configuration

    @Value("${powerauth.push.service.fcm.proxy.enabled}")
    private boolean fcmProxyEnabled;

    @Value("${powerauth.push.service.fcm.proxy.url}")
    private String fcmProxyUrl;

    @Value("${powerauth.push.service.fcm.proxy.port}")
    private int fcmProxyPort;

    @Value("${powerauth.push.service.fcm.proxy.username}")
    private String fcmProxyUsername;

    @Value("${powerauth.push.service.fcm.proxy.password}")
    private String fcmProxyPassword;

    @Value("${powerauth.push.service.fcm.dataNotificationOnly}")
    private boolean fcmDataNotificationOnly;

    @Value("${powerauth.push.service.fcm.sendMessageUrl}")
    private String fcmSendMessageUrl;

    // Campaign Configuration
    @Value("${powerauth.push.service.campaign.batchSize}")
    private int campaignBatchSize;

    // Whether to store messages
    @Value("${powerauth.push.service.message.storage.enabled}")
    private boolean messageStorageEnabled;

    /**
     * FCM connect timeout in milliseconds.
     */
    @Value("${powerauth.push.service.fcm.connect.timeout}")
    private int fcmConnectTimeout;

    /**
     * APNS connect timeout in milliseconds.
     */
    @Value("${powerauth.push.service.apns.connect.timeout}")
    private int apnsConnectTimeout;

    /**
     * Get push server name.
     * @return Push server name.
     */
    public String getPushServerName() {
        return pushServerName;
    }

    /**
     * Set push server name.
     * @param pushServerName Push server name.
     */
    public void setPushServerName(String pushServerName) {
        this.pushServerName = pushServerName;
    }

    /**
     * Get push server display name.
     * @return Push server display name.
     */
    public String getPushServerDisplayName() {
        return pushServerDisplayName;
    }

    /**
     * Set push server display name.
     * @param pushServerDisplayName Push server display name.
     */
    public void setPushServerDisplayName(String pushServerDisplayName) {
        this.pushServerDisplayName = pushServerDisplayName;
    }

    /**
     * Get push server environment.
     * @return Push server environment.
     */
    public String getPushServerEnvironment() {
        return pushServerEnvironment;
    }

    /**
     * Set push server environment.
     * @param pushServerEnvironment Push server environment.
     */
    public void setPushServerEnvironment(String pushServerEnvironment) {
        this.pushServerEnvironment = pushServerEnvironment;
    }

    /**
     * Flag indicating if a development or production environment should be used for APNs.
     * @return True in case APNs should use DEV environment, false for PROD.
     */
    public boolean isApnsUseDevelopment() {
        return apnsUseDevelopment;
    }

    /**
     * Set if development environment should be used, instead of production.
     * @param apnsUseDevelopment True in case APNs should use DEV environment, false for PROD.
     */
    public void setApnsUseDevelopment(boolean apnsUseDevelopment) {
        this.apnsUseDevelopment = apnsUseDevelopment;
    }

    /**
     * Flag indicating if proxy should be used for APNs.
     * @return True if proxy should be used for APNs, false otherwise.
     */
    public boolean isApnsProxyEnabled() {
        return apnsProxyEnabled;
    }

    /**
     * Set if proxy should be used for APNs.
     * @param apnsProxyEnabled True if proxy should be used for APNs, false otherwise.
     */
    public void setApnsProxyEnabled(boolean apnsProxyEnabled) {
        this.apnsProxyEnabled = apnsProxyEnabled;
    }

    /**
     * Get APNs proxy URL address.
     * @return APNs proxy URL address.
     */
    public String getApnsProxyUrl() {
        return apnsProxyUrl;
    }

    /**
     * Set APNs proxy URL address.
     * @param apnsProxyUrl APNs proxy URL address.
     */
    public void setApnsProxyUrl(String apnsProxyUrl) {
        this.apnsProxyUrl = apnsProxyUrl;
    }

    /**
     * Get APNs proxy port.
     * @return APNs proxy port.
     */
    public int getApnsProxyPort() {
        return apnsProxyPort;
    }

    /**
     * Set APNs proxy port.
     * @param apnsProxyPort APNs proxy port.
     */
    public void setApnsProxyPort(int apnsProxyPort) {
        this.apnsProxyPort = apnsProxyPort;
    }

    /**
     * Get APNs proxy username.
     * @return APNs proxy username.
     */
    public String getApnsProxyUsername() {
        return apnsProxyUsername;
    }

    /**
     * Set APNs proxy username.
     * @param apnsProxyUsername APNs proxy username.
     */
    public void setApnsProxyUsername(String apnsProxyUsername) {
        this.apnsProxyUsername = apnsProxyUsername;
    }

    /**
     * Get APNs proxy password.
     * @return APNs proxy password.
     */
    public String getApnsProxyPassword() {
        return apnsProxyPassword;
    }

    /**
     * Set APNs proxy password.
     * @param apnsProxyPassword APNs proxy password.
     */
    public void setApnsProxyPassword(String apnsProxyPassword) {
        this.apnsProxyPassword = apnsProxyPassword;
    }

    /**
     * Flag indicating if proxy is enabled for FCM communication.
     * @return True if FCM uses proxy, false otherwise.
     */
    public boolean isFcmProxyEnabled() {
        return fcmProxyEnabled;
    }

    /**
     * Set if proxy should be used for FCM communication.
     * @param fcmProxyEnabled True if FCM uses proxy, false otherwise.
     */
    public void setFcmProxyEnabled(boolean fcmProxyEnabled) {
        this.fcmProxyEnabled = fcmProxyEnabled;
    }

    /**
     * Get FCM proxy URL.
     * @return FCM proxy URL.
     */
    public String getFcmProxyUrl() {
        return fcmProxyUrl;
    }

    /**
     * Set FCM proxy URL.
     * @param fcmProxyUrl FCM proxy URL.
     */
    public void setFcmProxyUrl(String fcmProxyUrl) {
        this.fcmProxyUrl = fcmProxyUrl;
    }

    /**
     * Get FCM proxy port.
     * @return FCM proxy port.
     */
    public int getFcmProxyPort() {
        return fcmProxyPort;
    }

    /**
     * Set FCM proxy port.
     * @param fcmProxyPort FCM proxy port.
     */
    public void setFcmProxyPort(int fcmProxyPort) {
        this.fcmProxyPort = fcmProxyPort;
    }

    /**
     * Get FCM proxy username.
     * @return FCM proxy username.
     */
    public String getFcmProxyUsername() {
        return fcmProxyUsername;
    }

    /**
     * Set FCM proxy username.
     * @param fcmProxyUsername FCM proxy username.
     */
    public void setFcmProxyUsername(String fcmProxyUsername) {
        this.fcmProxyUsername = fcmProxyUsername;
    }

    /**
     * Get FCM proxy password.
     * @return FCM proxy password.
     */
    public String getFcmProxyPassword() {
        return fcmProxyPassword;
    }

    /**
     * Set FCM proxy password.
     * @param fcmProxyPassword FCM proxy password.
     */
    public void setFcmProxyPassword(String fcmProxyPassword) {
        this.fcmProxyPassword = fcmProxyPassword;
    }

    /**
     * Get status if notification is set to be sent only through data map
     * @return True in case FCM notification should always be a "data" notification, even for messages with title and message, false otherwise.
     */
    public boolean isFcmDataNotificationOnly() {
        return fcmDataNotificationOnly;
    }

    /**
     * Set if notification should be send only through data map
     * @param fcmDataNotificationOnly True in case FCM notification should always be a "data" notification, even for messages with title and message, false otherwise.
     */
    public void setFcmDataNotificationOnly(boolean fcmDataNotificationOnly) {
        this.fcmDataNotificationOnly = fcmDataNotificationOnly;
    }

    /**
     * Get FCM send message endpoint URL.
     * @return FCM send message endpoint URL.
     */
    public String getFcmSendMessageUrl() {
        return fcmSendMessageUrl;
    }

    /**
     * Set FCM send message endpoint URL.
     * @param fcmSendMessageUrl FCM send message endpoint URL.
     */
    public void setFcmSendMessageUrl(String fcmSendMessageUrl) {
        this.fcmSendMessageUrl = fcmSendMessageUrl;
    }

    /**
     * Get the batch size used while sending a push campaign.
     * @return Batch size.
     */
    public int getCampaignBatchSize() {
        return campaignBatchSize;
    }

    /**
     * Set the batch size used while sending a push campaign.
     * @param campaignBatchSize Batch size.
     */
    public void setCampaignBatchSize(int campaignBatchSize) {
        this.campaignBatchSize = campaignBatchSize;
    }

    /**
     * Get whether persistent message storage is enabled.
     * @return Whether persistent message storage is enabled.
     */
    public boolean isMessageStorageEnabled() {
        return messageStorageEnabled;
    }

    /**
     * Set whether persistent message storage is enabled.
     * @param messageStorageEnabled Whether persistent message storage is enabled.
     */
    public void setMessageStorageEnabled(boolean messageStorageEnabled) {
        this.messageStorageEnabled = messageStorageEnabled;
    }

    /**
     * Get FCM connect timeout in milliseconds.
     * @return FCM connect timeout.
     */
    public int getFcmConnectTimeout() {
        return fcmConnectTimeout;
    }

    /**
     * Set FCM connect timeout in milliseconds.
     * @param fcmConnectTimeout FCM connect timeout.
     */
    public void setFcmConnectTimeout(int fcmConnectTimeout) {
        this.fcmConnectTimeout = fcmConnectTimeout;
    }

    /**
     * Get APNS connect timeout in milliseconds.
     * @return APNS connect timeout.
     */
    public int getApnsConnectTimeout() {
        return apnsConnectTimeout;
    }

    /**
     * Set APNS connect timeout in milliseconds.
     * @param apnsConnectTimeout APNS connect timeout.
     */
    public void setApnsConnectTimeout(int apnsConnectTimeout) {
        this.apnsConnectTimeout = apnsConnectTimeout;
    }
}
