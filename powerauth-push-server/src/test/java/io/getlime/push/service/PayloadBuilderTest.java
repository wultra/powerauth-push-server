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

import io.getlime.push.model.entity.PushMessageBody;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test {@link PayloadBuilder}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class PayloadBuilderTest {

    @Test
    void testBuildApnsPayload() {
        final Map<String, Object> extras = new HashMap<>();
        extras.put("_comment", "Any custom data.");
        extras.put("_foo", null);

        final PushMessageBody pushMessageBody = new PushMessageBody();
        pushMessageBody.setTitle("Balance update");
        pushMessageBody.setTitleLocKey("balance.update.title");
        pushMessageBody.setTitleLocArgs(new String[0]);
        pushMessageBody.setBody("Your balance is now $745.00");
        pushMessageBody.setBodyLocKey("balance.update.body");
        pushMessageBody.setBodyLocArgs(new String[0]);
        pushMessageBody.setBadge(3);
        pushMessageBody.setSound("default");
        pushMessageBody.setIcon("custom-icon");
        pushMessageBody.setCategory("balance-update");
        pushMessageBody.setCollapseKey("balance-update");
        pushMessageBody.setValidUntil(Instant.parse("2017-12-11T21:22:29.923Z"));
        pushMessageBody.setExtras(extras);

        final String result = PayloadBuilder.buildApnsPayload(pushMessageBody, false);

        assertFalse(result.contains("_foo"));
        assertEquals("""
                {"aps":{"badge":3,"alert":{"loc-key":"balance.update.body","body":"Your balance is now $745.00","title":"Balance update","title-loc-key":"balance.update.title"},"sound":"default","category":"balance-update","thread-id":"balance-update"},"_comment":"Any custom data."}""",
                result);
    }
}