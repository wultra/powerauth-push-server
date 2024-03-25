/*
 * Copyright 2024 Wultra s.r.o.
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
package io.getlime.push.service.hms.request;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;

/**
 * HMS (Huawei Mobile Services) json mapping object.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 * @see <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References/https-send-api-0000001050986197#EN-US_TOPIC_0000001562768322__p1324218481619">Huawei documentation</a>
 */
@Getter
@SuperBuilder
@Jacksonized
public class Message {

    /**
     * Custom message payload. The value can be a JSON string for notification messages, and can be a common string or JSON string for data messages.
     */
    private final String data;

    private final Notification notification;

    private final AndroidConfig android;

    private final ApnsConfig apns;

    private final WebPushConfig webpush;

    @Builder.Default
    private final List<String> token = new ArrayList<>();

    private final String topic;

    private final String condition;
}
