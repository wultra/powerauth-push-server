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

package io.getlime.push.service;

import io.getlime.push.errorhandling.exceptions.AppNotFoundException;
import io.getlime.push.errorhandling.exceptions.InboxMessageNotFoundException;
import io.getlime.push.model.entity.ListOfInboxMessages;
import io.getlime.push.model.request.CreateInboxMessageRequest;
import io.getlime.push.model.response.GetInboxMessageCountResponse;
import io.getlime.push.model.response.GetInboxMessageDetailResponse;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.InboxRepository;
import io.getlime.push.repository.converter.InboxMessageConverter;
import io.getlime.push.repository.model.InboxMessageEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for tasks related to the inbox.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
@Slf4j
public class InboxService {

    private final InboxRepository inboxRepository;
    private final AppCredentialsRepository appCredentialsRepository;
    private final InboxMessageConverter inboxMessageConverter;

    @Autowired
    public InboxService(InboxRepository inboxRepository, AppCredentialsRepository appCredentialsRepository, InboxMessageConverter inboxMessageConverter) {
        this.inboxRepository = inboxRepository;
        this.appCredentialsRepository = appCredentialsRepository;
        this.inboxMessageConverter = inboxMessageConverter;
    }

    @Transactional
    public GetInboxMessageDetailResponse postMessage(String userId, String appId, CreateInboxMessageRequest request) throws AppNotFoundException {
        checkAppId(appId);
        final InboxMessageEntity messageEntity = inboxMessageConverter.convert(UUID.randomUUID(), userId, appId, request, new Date());
        final InboxMessageEntity savedMessageEntity = inboxRepository.save(messageEntity);
        logger.info("Posted new inbox message for user: {}, message ID: {}", userId, messageEntity.getInboxId());
        return inboxMessageConverter.convertResponse(savedMessageEntity);
    }

    @Transactional(readOnly=true)
    public ListOfInboxMessages fetchMessageListForUser(String userId, String appId, boolean onlyUnread, Pageable pageable) throws AppNotFoundException {
        checkAppId(appId);
        final List<InboxMessageEntity> messageEntities;
        if (onlyUnread) {
            messageEntities = inboxRepository.findAllByUserIdAndAppIdAndReadOrderByTimestampCreatedDesc(userId, appId, false, pageable);
        } else {
            messageEntities = inboxRepository.findAllByUserIdAndAppIdOrderByTimestampCreatedDesc(userId, appId, pageable);
        }
        return inboxMessageConverter.convert(messageEntities, pageable.getPageNumber(), pageable.getPageSize());
    }

    @Transactional(readOnly=true)
    public GetInboxMessageDetailResponse fetchMessageDetail(String userId, String appId, String id) throws InboxMessageNotFoundException, AppNotFoundException {
        checkAppId(appId);
        final Optional<InboxMessageEntity> messageEntity = inboxRepository.findFirstByInboxIdAndUserIdAndAppId(id, userId, appId);
        if (!messageEntity.isPresent()) {
            throw new InboxMessageNotFoundException("Unable to fetch message: " + id + ", for user: " + userId + ".");
        }
        return inboxMessageConverter.convertResponse(messageEntity.get());
    }

    @Transactional(readOnly=true)
    public GetInboxMessageCountResponse fetchMessageCountForUser(String userId, String appId) throws AppNotFoundException {
        checkAppId(appId);
        final long countUnread = inboxRepository.countAllByUserIdAndAppIdAndRead(userId, appId,false);
        return new GetInboxMessageCountResponse(countUnread);
    }

    @Transactional
    public GetInboxMessageDetailResponse readMessage(String userId, String appId, String inboxId) throws InboxMessageNotFoundException, AppNotFoundException {
        checkAppId(appId);
        final Optional<InboxMessageEntity> messageEntity = inboxRepository.findFirstByInboxIdAndUserIdAndAppId(inboxId, userId, appId);
        if (!messageEntity.isPresent()) {
            throw new InboxMessageNotFoundException("Unable to mark message: " + inboxId + ", for user: " + userId + " as read.");
        }
        final InboxMessageEntity inboxMessage = messageEntity.get();
        if (!inboxMessage.isRead()) { // do not call repository save if there is no change.
            inboxMessage.setRead(true);
            logger.info("Marked inbox message as read for user: {}, message ID: {}", userId, inboxId);
            final InboxMessageEntity savedInboxMessage = inboxRepository.save(inboxMessage);
            return inboxMessageConverter.convertResponse(savedInboxMessage);
        } else {
            return inboxMessageConverter.convertResponse(inboxMessage);
        }
    }

    @Transactional
    public void readAllMessages(String userId) {
        int countRead = inboxRepository.markAllAsRead(userId);
        logger.info("Marked all inbox messages as read for user: {}, count: {}", userId, countRead);
    }

    private void checkAppId(String appId) throws AppNotFoundException {
        appCredentialsRepository.findFirstByAppId(appId).orElseThrow(
                () -> new AppNotFoundException("Application was not found: " + appId));
    }
}
