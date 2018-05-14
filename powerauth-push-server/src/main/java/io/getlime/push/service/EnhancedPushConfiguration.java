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
import com.turo.pushy.apns.auth.ApnsSigningKey;
import com.turo.pushy.apns.proxy.HttpProxyHandlerFactory;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessageAttributes;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.service.fcm.FcmClient;
import io.getlime.push.service.fcm.FcmNotification;
import io.getlime.push.service.fcm.model.FcmSendRequest;
import io.getlime.push.service.fcm.model.FcmSendResponse;
import io.getlime.push.service.fcm.model.base.FcmResult;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnhancedPushConfiguration {
    PushServiceConfiguration pushServiceConfiguration;

    public EnhancedPushConfiguration(PushServiceConfiguration pushServiceConfiguration) {
        this.pushServiceConfiguration = pushServiceConfiguration;
    }

    // Send message to Android platform
    void sendMessageToAndroid(final FcmClient fcmClient, final PushMessageBody pushMessageBody, final PushMessageAttributes attributes, final String pushToken, final PushSendingCallback callback) {

        FcmSendRequest request = new FcmSendRequest();
        request.setTo(pushToken);
        request.setData(pushMessageBody.getExtras());
        request.setCollapseKey(pushMessageBody.getCollapseKey());
        if (pushServiceConfiguration.isFcmDataNotificationOnly()) { //notification only through data map
            FcmNotification notification = new FcmNotification();
            notification.setTitle(pushMessageBody.getTitle());
            notification.setBody(pushMessageBody.getBody());
            notification.setIcon(pushMessageBody.getIcon());
            notification.setSound(pushMessageBody.getSound());
            notification.setTag(pushMessageBody.getCategory());
            request.getData().put("_notification", notification);
        } else if (attributes == null || !attributes.getSilent()) { // if there are no attributes, assume the message is not silent
            FcmNotification notification = new FcmNotification();
            notification.setTitle(pushMessageBody.getTitle());
            notification.setBody(pushMessageBody.getBody());
            notification.setIcon(pushMessageBody.getIcon());
            notification.setSound(pushMessageBody.getSound());
            notification.setTag(pushMessageBody.getCategory());
            request.setNotification(notification);
        }

        final ListenableFuture<ResponseEntity<FcmSendResponse>> future;
        try {
            future = fcmClient.exchange(request);
        } catch (Throwable t) { // In case of some catastrophic error
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification sending failed: " + t.getMessage(), t);
            callback.didFinishSendingMessage(PushSendingCallback.Result.FAILED, null);
            return;
        }

        future.addCallback(new ListenableFutureCallback<ResponseEntity<FcmSendResponse>>() {

            @Override
            public void onFailure(Throwable throwable) {
                Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Notification rejected by the FCM gateway: " + throwable.getMessage(), throwable);
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
                            Map<String, Object> contextData = new HashMap<String, Object>();
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

    // Prepare and connect APNS client.
    ApnsClient prepareApnsClient(byte[] apnsPrivateKey, String teamId, String keyId) throws PushServerException {
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
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            throw new PushServerException("Invalid private key", e);
        }
        try {
            return apnsClientBuilder.build();
        } catch (SSLException e) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            throw new PushServerException("SSL problem", e);
        }
    }

    // Prepare and connect FCM client
    FcmClient prepareFcmClient(String serverKey) {
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
    HttpProxyHandlerFactory apnsClientProxy() {
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
}