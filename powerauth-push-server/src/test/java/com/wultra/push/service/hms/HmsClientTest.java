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
package com.wultra.push.service.hms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.push.configuration.PushServiceConfiguration;
import com.wultra.push.repository.model.AppCredentialsEntity;
import com.wultra.push.service.hms.request.Message;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration end-to-end test for {@link HmsClient}.
 * Disabled for maven by default.
 * First, credentials must be filled in {@code application-external-service.properties}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@SpringBootTest
@ActiveProfiles({"external-service", "test"})
@EnableConfigurationProperties
@Tag("external-service")
@ConditionalOnProperty("hms.projectId")
class HmsClientTest {

    @Value("${hms.projectId}")
    private String projectId;

    @Value("${hms.clientId}")
    private String clientId;

    @Value("${hms.clientSecret}")
    private String clientSecret;

    @Autowired
    private PushServiceConfiguration pushServiceConfiguration;

    @Test
    void testSendMessage() throws Exception {
        Assumptions.assumeTrue(StringUtils.isNotBlank(projectId), "HMS projectId must be configured");
        Assumptions.assumeTrue(StringUtils.isNotBlank(clientId), "HMS clientId must be configured");
        Assumptions.assumeTrue(StringUtils.isNotBlank(clientSecret), "HMS clientSecret must be configured");

        final AppCredentialsEntity credentials = new AppCredentialsEntity();
        credentials.setHmsProjectId(projectId);
        credentials.setHmsClientId(clientId);
        credentials.setHmsClientSecret(clientSecret);

        final HmsClient tested = new HmsClient(pushServiceConfiguration, credentials);

        // https://developer.huawei.com/consumer/en/doc/HMSCore-Guides/rest-sample-code-0000001050040242
        final String json = """
                {
                    "notification": {
                        "title": "test title",
                        "body": "test body"
                    },
                    "android": {
                        "collapse_key": -1,
                        "urgency": "NORMAL",
                        "category": "IM",
                        "ttl": "86400s",
                        "bi_tag": "testtag",
                        "notification": {
                            "image": "https://res.vmallres.com/pimages//common/config/logo/SXppnESYv4K11DBxDFc2.png",
                            "icon": "/raw/ic_launcher2",
                            "color": "#AACCDD",
                            "sound": "/raw/shake",
                            "default_sound": false,
                            "importance": "NORMAL",
                            "click_action": {
                                "type": 3
                            },
                            "body_loc_key": "demo_title_new2",
                            "body_loc_args": [
                                "BODY_LOCAL_ARGS_A",
                                "BODY_LOCAL_ARGS_B",
                                "BODY_LOCAL_ARGS_C"
                            ],
                            "title_loc_key": "demo_title_new2",
                            "title_loc_args": [
                                "TITLE_LOCAL_ARGS_A",
                                "TITLE_LOCAL_ARGS_B",
                                "TITLE_LOCAL_ARGS_C"
                            ],
                            "channel_id": "test_channel_id",
                            "notify_summary": "Summary",
                            "style": 1,
                            "big_title": "the big title",
                            "big_body": "the big body",
                            "notify_id": 123456,
                            "group": "mygroup",
                            "badge": {
                                "add_num": 1,
                                "class": "com.huawei.demo.push.HuaweiPushApiExample"
                            },
                            "foreground_show": false,
                            "ticker": "I am a ticker",
                            "when": "2014-10-02T15:01:23.045123456Z",
                            "use_default_vibrate": false,
                            "use_default_light": false,
                            "visibility": "PUBLIC",
                            "vibrate_config": [
                                "1",
                                "3"
                            ],
                            "light_settings": {
                                "color": {
                                    "alpha": 0,
                                    "red": 0,
                                    "green": 1,
                                    "blue": 0.1
                                },
                                "light_on_duration": "3.5S",
                                "light_off_duration": "5S"
                            }
                        }
                    },
                    "token": [
                        "token1"
                    ]
                }
                """;

        final Message message = new ObjectMapper().readValue(json, Message.class);

        final HmsSendResponse result = tested.sendMessage(message, false).block();

        assertNotNull(result);
        assertEquals("80000000", result.code());
    }

}