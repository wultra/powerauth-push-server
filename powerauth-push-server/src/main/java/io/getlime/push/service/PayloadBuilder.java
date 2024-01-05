/*
 * Copyright 2023 Wultra s.r.o.
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

import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import io.getlime.push.model.entity.PushMessageBody;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Convert {@link PushMessageBody} to platform dependant payload.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Slf4j
final class PayloadBuilder {

    private PayloadBuilder() {
        throw new IllegalStateException("Should not be instantiated.");
    }

    /**
     * Method to build APNs message payload.
     *
     * @param push     Push message object with APNs data.
     * @param isSilent Indicates if the message is silent or not.
     * @return String with APNs JSON payload.
     */
    static String buildApnsPayload(final PushMessageBody push, final boolean isSilent) {
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
                if (entry.getValue() != null) {
                    payloadBuilder.addCustomProperty(entry.getKey(), entry.getValue());
                } else {
                    // Workaround for a known Apple issue, the JSON is valid but APNS would not send the push message.
                    logger.debug("Skipping extras key: {} because of null value.", entry.getKey());
                }
            }
        }

        return payloadBuilder.build();
    }
}
