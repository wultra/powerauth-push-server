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
package com.wultra.push.controller.rest;

import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.core.rest.model.base.response.Response;
import com.wultra.push.errorhandling.exceptions.AppNotFoundException;
import com.wultra.push.errorhandling.exceptions.InboxMessageNotFoundException;
import com.wultra.push.model.base.PagedResponse;
import com.wultra.push.model.entity.ListOfInboxMessages;
import com.wultra.push.model.request.CreateInboxMessageRequest;
import com.wultra.push.model.request.ReadAllInboxMessagesRequest;
import com.wultra.push.model.request.ReadInboxMessageRequest;
import com.wultra.push.model.response.GetInboxMessageCountResponse;
import com.wultra.push.model.response.GetInboxMessageDetailResponse;
import com.wultra.push.service.InboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    /**
     * Default constructor of the controller with inbox service injection.
     *
     * @param inboxService Inbox service.
     */
    @Autowired
    public InboxController(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    /**
     * Post a message to the inbox.
     *
     * @param request Request with a posted message detail.
     * @return Response with a new message detail.
     * @throws AppNotFoundException In case an application for which this message was intended was not found.
     */
    @PostMapping("messages")
    @Operation(summary = "Post a message to inbox", description = "Post a message to inbox to be delivered to users")
    public ObjectResponse<GetInboxMessageDetailResponse> postMessage(
            @Valid @RequestBody ObjectRequest<CreateInboxMessageRequest> request) throws AppNotFoundException {
        return new ObjectResponse<>(inboxService.postMessage(request.getRequestObject()));
    }

    /**
     * Get the list of messages.
     *
     * @param userId User ID.
     * @param applications Applications to fetch the detail for.
     * @param onlyUnread Flag indicating if the message was read.
     * @param pageable Paging object.
     * @return Paged response with the messages.
     * @throws AppNotFoundException In case an application specified in the request intended was not found.
     */
    @GetMapping("messages/list")
    @Operation(summary = "List messages in inbox", description = "List messages in inbox of a user")
    public PagedResponse<ListOfInboxMessages> fetchMessageListForUser(
            @NotNull @Size(min = 1, max = 255) @RequestParam("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("applications") @Schema(type = "string", example = "app-id-01,app-id-02") String applications,
            @RequestParam(value = "onlyUnread", required = false, defaultValue = "false") boolean onlyUnread,
            @ParameterObject Pageable pageable) throws AppNotFoundException {
        return new PagedResponse<>(inboxService.fetchMessageListForUser(userId, Arrays.asList(applications.split(",")), onlyUnread, pageable), pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * Obtain unread message count for a user.
     * @param userId User ID.
     * @param appId App ID.
     * @return Response with message count.
     * @throws AppNotFoundException In case an application specified in the request intended was not found.
     */
    @GetMapping("messages/count")
    @Operation(summary = "Count messages in inbox", description = "Count messages in inbox of a user")
    public ObjectResponse<GetInboxMessageCountResponse> fetchMessageCountForUser(
            @NotNull @Size(min = 1, max = 255) @RequestParam("userId") String userId,
            @NotNull @Size(min = 1, max = 255) @RequestParam("appId") String appId) throws AppNotFoundException {
        return new ObjectResponse<>(inboxService.fetchMessageCountForUser(userId, appId));
    }

    /**
     * Mark all messages in the inbox as read.
     *
     * @param request Request with user and app specification.
     * @return OK response.
     * @throws AppNotFoundException In case an application specified in the request intended was not found.
     */
    @PostMapping("messages/read-all")
    @Operation(summary = "Mark all messages as read", description = "Mark all messages as read for a user")
    public Response readAllMessages(
            @Valid @RequestBody ObjectRequest<ReadAllInboxMessagesRequest> request) throws AppNotFoundException {
        final ReadAllInboxMessagesRequest requestObject = request.getRequestObject();
        inboxService.readAllMessages(requestObject.getUserId(), requestObject.getAppId());
        return new Response();
    }

    /**
     * Fetch message detail.
     *
     * @param inboxId Inbox message ID.
     * @return Detail of a message.
     * @throws InboxMessageNotFoundException In case a given message was not found.
     */
    @GetMapping("messages/detail")
    @Operation(summary = "Get a message detail", description = "Fetch given inbox message detail")
    public ObjectResponse<GetInboxMessageDetailResponse> fetchMessageDetail(
            @NotNull @RequestParam("id") String inboxId) throws InboxMessageNotFoundException {
        return new ObjectResponse<>(inboxService.fetchMessageDetail(inboxId));
    }

    /**
     * Mark a message as read.
     * @param request Request specifying which message should be marked as read.
     * @return Detail of a message.
     * @throws InboxMessageNotFoundException In case a given message was not found.
     */
    @PostMapping("messages/read")
    @Operation(summary = "Mark a message as read", description = "Mark given inbox message as read")
    public ObjectResponse<GetInboxMessageDetailResponse> readMessage(
            @Valid @RequestBody ObjectRequest<ReadInboxMessageRequest> request) throws InboxMessageNotFoundException {
        final ReadInboxMessageRequest requestObject = request.getRequestObject();
        return new ObjectResponse<>(inboxService.readMessage(requestObject.getInboxId()));
    }

}
