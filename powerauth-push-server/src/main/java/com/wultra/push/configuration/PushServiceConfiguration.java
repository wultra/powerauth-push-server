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

package com.wultra.push.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for push server properties.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@ConfigurationProperties("ext")
@Getter
@Setter
public class PushServiceConfiguration {

    /**
     * Push server name.
     */
    @Value("${powerauth.push.service.applicationName}")
    private String pushServerName;

    /**
     * Push server display name.
     */
    @Value("${powerauth.push.service.applicationDisplayName}")
    private String pushServerDisplayName;

    /**
     * Push server environment.
     */
    @Value("${powerauth.push.service.applicationEnvironment}")
    private String pushServerEnvironment;

    // APNs Configuration

    /**
     * Flag indicating if a development or production environment should be used for APNs.
     * {@code true} in case APNs should use DEV environment, {@code false} for PROD.
     */
    @Value("${powerauth.push.service.apns.useDevelopment}")
    private boolean apnsUseDevelopment;

    /**
     * Whether proxy should be used for APNs.
     */
    @Value("${powerauth.push.service.apns.proxy.enabled}")
    private boolean apnsProxyEnabled;

    /**
     * APNs proxy host.
     */
    @Value("${powerauth.push.service.apns.proxy.host}")
    private String apnsProxyHost;

    /**
     * APNs proxy port.
     */
    @Value("${powerauth.push.service.apns.proxy.port}")
    private int apnsProxyPort;

    /**
     * APNs proxy username.
     */
    @Value("${powerauth.push.service.apns.proxy.username}")
    private String apnsProxyUsername;

    /**
     * APNs proxy password.
     */
    @Value("${powerauth.push.service.apns.proxy.password}")
    private String apnsProxyPassword;

    // FCM Configuration

    /**
     * Flag indicating if proxy is enabled for FCM communication.
     */
    @Value("${powerauth.push.service.fcm.proxy.enabled}")
    private boolean fcmProxyEnabled;

    /**
     * FCM proxy URL.
     */
    @Value("${powerauth.push.service.fcm.proxy.host}")
    private String fcmProxyHost;

    /**
     * FCM proxy port.
     */
    @Value("${powerauth.push.service.fcm.proxy.port}")
    private int fcmProxyPort;

    /**
     * FCM proxy username.
     */
    @Value("${powerauth.push.service.fcm.proxy.username}")
    private String fcmProxyUsername;

    /**
     * FCM proxy password.
     */
    @Value("${powerauth.push.service.fcm.proxy.password}")
    private String fcmProxyPassword;

    /**
     * Get status if notification is set to be sent only through data map
     * True in case FCM notification should always be a "data" notification, even for messages with title and message, false otherwise.
     */
    @Value("${powerauth.push.service.fcm.dataNotificationOnly}")
    private boolean fcmDataNotificationOnly;

    /**
     * FCM send message endpoint URL.
     */
    @Value("${powerauth.push.service.fcm.sendMessageUrl}")
    private String fcmSendMessageUrl;

    /**
     * Flag indicating if proxy is enabled for HMS communication.
     */
    @Value("${powerauth.push.service.hms.proxy.enabled}")
    private boolean hmsProxyEnabled;

    /**
     * HMS proxy URL.
     */
    @Value("${powerauth.push.service.hms.proxy.host}")
    private String hmsProxyHost;

    /**
     * HMS proxy port.
     */
    @Value("${powerauth.push.service.hms.proxy.port}")
    private int hmsProxyPort;

    /**
     * HMS proxy username.
     */
    @Value("${powerauth.push.service.hms.proxy.username}")
    private String hmsProxyUsername;

    /**
     * HMS proxy password.
     */
    @Value("${powerauth.push.service.hms.proxy.password}")
    private String hmsProxyPassword;

    /**
     * Get status if notification is set to be sent only through data map
     * True in case HMS notification should always be a "data" notification, even for messages with title and message, false otherwise.
     */
    @Value("${powerauth.push.service.hms.dataNotificationOnly}")
    private boolean hmsDataNotificationOnly;

    /**
     * HMS send message endpoint URL.
     */
    @Value("${powerauth.push.service.hms.sendMessageUrl}")
    private String hmsSendMessageUrl;

    /**
     * HMS OAuth service URL to obtain an access token.
     */
    @Value("${powerauth.push.service.hms.tokenUrl}")
    private String hmsTokenUrl;

    /**
     * The batch size used while sending a push campaign.
     */
    @Value("${powerauth.push.service.campaign.batchSize}")
    private int campaignBatchSize;

    /**
     * Whether to store messages.
     */
    @Value("${powerauth.push.service.message.storage.enabled}")
    private boolean messageStorageEnabled;

    /**
     *  Whether multiple activations are enabled per registered device.
     */
    @Value("${powerauth.push.service.registration.multipleActivations.enabled}")
    private boolean registrationOfMultipleActivationsEnabled;

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
     * HMS connect timeout.
     */
    @Value("${powerauth.push.service.hms.connect.timeout}")
    private Duration hmsConnectTimeout;

    /**
     * HMS maximum duration allowed between each network-level read operations.
     */
    @Value("${powerauth.push.service.hms.response.timeout}")
    private Duration hmsResponseTimeout;

    /**
     * HMS ConnectionProvider max idle time.
     */
    @Value("${powerauth.push.service.hms.max-idle-time}")
    private Duration hmsMaxIdleTime;

    /**
     * APNS concurrent connections.
     */
    @Value("${powerauth.push.service.apns.concurrentConnections}")
    private int concurrentConnections;

    /**
     * Interval specifying the frequency of APNS ping calls in idle state.
     */
    @Value("${powerauth.push.service.apns.idlePingInterval}")
    private long idlePingInterval;

    /**
     * Java security CA certs file password.
     */
    @Value("${powerauth.push.java.cacerts.password}")
    private String javaCaCertificatesPassword;

}
