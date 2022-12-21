/*
 * Copyright 2022 Wultra s.r.o.
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
import io.getlime.push.errorhandling.exceptions.AppNotFoundException;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.InboxMessageNotFoundException;
import io.getlime.push.model.base.PagedResponse;
import io.getlime.push.model.request.CreateInboxMessageRequest;
import io.getlime.push.model.response.GetInboxMessageCountResponse;
import io.getlime.push.model.response.GetInboxMessageDetailResponse;
import io.getlime.push.model.entity.ListOfInboxMessages;
import io.getlime.push.service.InboxService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Controller for message inbox.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Validated
@RestController
@RequestMapping(value = "inbox")
public class InboxController {

    private final InboxService inboxService;

    @Autowired
    public InboxController(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @PostMapping("users/{userId}")
    public ObjectResponse<GetInboxMessageDetailResponse> postMessage(
            @NotNull @Size(min = 1, max = 255) @PathVariable("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("appId") String appId,
            @Valid @RequestBody ObjectRequest<CreateInboxMessageRequest> request) throws AppNotFoundException {
        return new ObjectResponse<>(inboxService.postMessage(userId, appId, request.getRequestObject()));
    }

    @GetMapping("users/{userId}")
    public PagedResponse<ListOfInboxMessages> fetchMessageListForUser(
            @NotNull @Size(min = 1, max = 255) @PathVariable("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("appId") String appId,
            @RequestParam(value = "onlyUnread", required = false, defaultValue = "false") boolean onlyUnread,
            @ParameterObject Pageable pageable) throws AppNotFoundException {
        return new PagedResponse<>(inboxService.fetchMessageListForUser(userId, appId, onlyUnread, pageable), pageable.getPageNumber(), pageable.getPageSize());
    }

    @GetMapping("users/{userId}/count")
    public ObjectResponse<GetInboxMessageCountResponse> fetchMessageCountForUser(
            @NotNull @Size(min = 1, max = 255) @PathVariable("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("appId") String appId) throws AppNotFoundException {
        return new ObjectResponse<>(inboxService.fetchMessageCountForUser(userId, appId));
    }

    @GetMapping("users/{userId}/messages/{id}")
    public ObjectResponse<GetInboxMessageDetailResponse> fetchMessageDetail(
            @NotNull @Size(min = 1, max = 255) @PathVariable("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("appId") String appId,
            @NotNull @PathVariable("id") String inboxId) throws InboxMessageNotFoundException, AppNotFoundException {
        return new ObjectResponse<>(inboxService.fetchMessageDetail(userId, appId, inboxId));
    }

    @GetMapping("messages/{id}")
    public ObjectResponse<GetInboxMessageDetailResponse> fetchMessageDetail(
            @NotNull @PathVariable("id") String messageId) throws InboxMessageNotFoundException, AppNotFoundException {
        return new ObjectResponse<>(inboxService.fetchMessageDetail(messageId));
    }

    @PutMapping("users/{userId}/messages/{id}/read")
    public ObjectResponse<GetInboxMessageDetailResponse> readMessage(
            @NotNull @Size(min = 1, max = 255) @PathVariable("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("appId") String appId,
            @NotNull @Size(min = 1, max = 255) @PathVariable("id") String inboxId) throws InboxMessageNotFoundException, AppNotFoundException {
        return new ObjectResponse<>(inboxService.readMessage(userId, appId, inboxId));
    }

    @PutMapping("users/{userId}/messages/read-all")
    public Response readAllMessages(
            @NotNull @Size(min = 1, max = 255) @PathVariable("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("appId") String appId) throws AppNotFoundException {
        inboxService.readAllMessages(userId, appId);
        return new Response();
    }

}
