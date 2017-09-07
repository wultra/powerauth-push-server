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
package io.getlime.push.controller.rest;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushSendResult;
import io.getlime.push.model.request.SendBatchMessageRequest;
import io.getlime.push.model.request.SendPushMessageRequest;
import io.getlime.push.service.PushSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller responsible for processes related to push notification sending.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "push/message")
public class PushSendingController {

    @Autowired
    private PushSenderService pushSenderService;

    /**
     * Send a single push message.
     * @param request Send push message request.
     * @return Response with message sending results.
     */
    @RequestMapping(value = "send", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<PushSendResult> sendPushMessage(@RequestBody ObjectRequest<SendPushMessageRequest> request) throws PushServerException {

        if (request.getRequestObject() == null || request.getRequestObject().getPush() == null || request.getRequestObject().getAppId() == null) {
            throw new PushServerException("Invalid or empty input data");
        }

        final Long appId = request.getRequestObject().getAppId();
        final List<PushMessage> pushMessageList = new ArrayList<>();
        pushMessageList.add(request.getRequestObject().getPush());
        PushSendResult result;
        try {
            result = pushSenderService.send(appId, pushMessageList);
        } catch (InterruptedException | IOException e) {
            throw new PushServerException(e.getMessage());
        }

        return new ObjectResponse<>(result);
    }

    /**
     * Send a batch of push messages.
     * @param request Request with push message batch.
     * @return Response with message sending results.
     */
    @RequestMapping(value = "batch/send", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<PushSendResult> sendPushMessageBatch(@RequestBody ObjectRequest<SendBatchMessageRequest> request) throws PushServerException {

        if (request.getRequestObject() == null || request.getRequestObject().getBatch() == null || request.getRequestObject().getAppId() == null) {
            throw new PushServerException("Invalid or empty input data");
        }

        final Long appId = request.getRequestObject().getAppId();
        final List<PushMessage> batch = request.getRequestObject().getBatch();

        if (batch.size() > 20) {
            throw new PushServerException("Too many messages in batch - do no send more than 20 messages at once to avoid server congestion.");
        }

        PushSendResult result;
        try {
            result = pushSenderService.send(appId, batch);
        } catch (InterruptedException | IOException e) {
            throw new PushServerException(e.getMessage());
        }

        return new ObjectResponse<>(result);
    }

}
