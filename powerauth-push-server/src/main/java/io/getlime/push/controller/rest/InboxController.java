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
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.AppNotFoundException;
import io.getlime.push.errorhandling.exceptions.InboxMessageNotFoundException;
import io.getlime.push.model.base.PagedResponse;
import io.getlime.push.model.entity.ListOfInboxMessages;
import io.getlime.push.model.request.CreateInboxMessageRequest;
import io.getlime.push.model.request.ReadAllInboxMessagesRequest;
import io.getlime.push.model.request.ReadInboxMessageRequest;
import io.getlime.push.model.response.GetInboxMessageCountResponse;
import io.getlime.push.model.response.GetInboxMessageDetailResponse;
import io.getlime.push.service.InboxService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;

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

    @PostMapping("messages")
    public ObjectResponse<GetInboxMessageDetailResponse> postMessage(
            @Valid @RequestBody ObjectRequest<CreateInboxMessageRequest> request) throws AppNotFoundException {
        return new ObjectResponse<>(inboxService.postMessage(request.getRequestObject()));
    }

    @GetMapping("messages/list")
    public PagedResponse<ListOfInboxMessages> fetchMessageListForUser(
            @NotNull @Size(min = 1, max = 255) @RequestParam("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("applications") @Schema(type = "string", example = "app-id-01,app-id-02") String applications,
            @RequestParam(value = "onlyUnread", required = false, defaultValue = "false") boolean onlyUnread,
            @ParameterObject Pageable pageable) throws AppNotFoundException {
        return new PagedResponse<>(inboxService.fetchMessageListForUser(userId, Arrays.asList(applications.split(",")), onlyUnread, pageable), pageable.getPageNumber(), pageable.getPageSize());
    }

    @GetMapping("messages/count")
    public ObjectResponse<GetInboxMessageCountResponse> fetchMessageCountForUser(
            @NotNull @Size(min = 1, max = 255) @RequestParam("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("appId") String appId) throws AppNotFoundException {
        return new ObjectResponse<>(inboxService.fetchMessageCountForUser(userId, appId));
    }

    @PostMapping("messages/read-all")
    public Response readAllMessages(
            @Valid @RequestBody ObjectRequest<ReadAllInboxMessagesRequest> request) throws AppNotFoundException {
        final ReadAllInboxMessagesRequest requestObject = request.getRequestObject();
        inboxService.readAllMessages(requestObject.getUserId(), requestObject.getAppId());
        return new Response();
    }

    @GetMapping("messages/detail")
    public ObjectResponse<GetInboxMessageDetailResponse> fetchMessageDetail(
            @NotNull @RequestParam("id") String inboxId) throws InboxMessageNotFoundException {
        return new ObjectResponse<>(inboxService.fetchMessageDetail(inboxId));
    }

    @PostMapping("messages/read")
    public ObjectResponse<GetInboxMessageDetailResponse> readMessage(
            @Valid @RequestBody ObjectRequest<ReadInboxMessageRequest> request) throws InboxMessageNotFoundException {
        final ReadInboxMessageRequest requestObject = request.getRequestObject();
        return new ObjectResponse<>(inboxService.readMessage(requestObject.getInboxId()));
    }

}
