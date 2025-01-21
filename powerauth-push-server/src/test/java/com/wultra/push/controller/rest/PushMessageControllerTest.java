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
package com.wultra.push.controller.rest;

import com.wultra.push.model.entity.PushMessage;
import com.wultra.push.model.enumeration.Mode;
import com.wultra.push.service.PushMessageSenderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link PushMessageController}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PushMessageControllerTest {

    @MockBean
    private PushMessageSenderService pushMessageSenderService;

    @Captor
    private ArgumentCaptor<List<PushMessage>> pushMessagesCaptor;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendPushMessage() throws Exception {
        mockMvc.perform(post("/push/message/send")
                        .content("""
                                {
                                  "requestObject": {
                                    "appId": "mobile-app",
                                    "mode": "SYNCHRONOUS",
                                    "message": {
                                      "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412",
                                      "userId": "123",
                                      "priority": "HIGH",
                                      "attributes": {
                                        "personal": true,
                                        "silent": true
                                      },
                                      "body": {
                                        "title": "Balance update",
                                        "titleLocKey": "balance.update.title",
                                        "titleLocArgs": [],
                                        "body": "Your balance is now $745.00",
                                        "bodyLocKey": "balance.update.body",
                                        "bodyLocArgs": [],
                                        "badge": 3,
                                        "sound": "default",
                                        "icon": "custom-icon",
                                        "category": "balance-update",
                                        "collapseKey": "balance-update",
                                        "validUntil": "2017-12-11T21:22:29.923Z",
                                        "extras": {
                                          "_comment": "Any custom data.",
                                          "_foo": null
                                        }
                                      }
                                    }
                                  }
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

        verify(pushMessageSenderService).sendPushMessage(eq("mobile-app"), eq(Mode.SYNCHRONOUS), pushMessagesCaptor.capture());

        final List<PushMessage> pushMessages = pushMessagesCaptor.getValue();
        assertEquals(1, pushMessages.size());

        final PushMessage pushMessage = pushMessages.iterator().next();
        final Map<String, Object> extras = pushMessage.getBody().getExtras();
        assertEquals("Any custom data.", extras.get("_comment"));
        assertNull(extras.get("_foo"), "Null value is binded correctly, should be removed before sending to APNS.");
    }

}
