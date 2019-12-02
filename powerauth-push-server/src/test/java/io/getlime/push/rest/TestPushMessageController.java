/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.push.rest;

import io.getlime.push.service.fcm.model.FcmSuccessResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;

/**
 * Push message controller for testing push message delivery.
 */
@RestController
public class TestPushMessageController {

    @PostMapping("/mockfcm/message:send")
    public FcmSuccessResponse testSendMessage() {
        FcmSuccessResponse response = new FcmSuccessResponse();
        response.setName("projects/test-project/messages/" + new SecureRandom().nextInt(1000000));
        return response;
    }
}
