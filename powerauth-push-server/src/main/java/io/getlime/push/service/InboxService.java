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

import io.getlime.push.errorhandling.exceptions.InboxMessageNotFoundException;
import io.getlime.push.model.entity.ListOfInboxMessages;
import io.getlime.push.model.request.CreateInboxMessageRequest;
import io.getlime.push.model.response.GetInboxMessageCountResponse;
import io.getlime.push.model.response.GetInboxMessageDetailResponse;
import io.getlime.push.repository.InboxRepository;
import io.getlime.push.repository.converter.InboxMessageConverter;
import io.getlime.push.repository.model.InboxMessageEntity;
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
public class InboxService {

    private final InboxRepository inboxRepository;
    private final InboxMessageConverter inboxMessageConverter;

    @Autowired
    public InboxService(InboxRepository inboxRepository, InboxMessageConverter inboxMessageConverter) {
        this.inboxRepository = inboxRepository;
        this.inboxMessageConverter = inboxMessageConverter;
    }

    @Transactional
    public GetInboxMessageDetailResponse postMessage(String userId, CreateInboxMessageRequest request) {
        final InboxMessageEntity messageEntity = inboxMessageConverter.convert(UUID.randomUUID(), userId, request, new Date());
        final InboxMessageEntity savedMessageEntity = inboxRepository.save(messageEntity);
        return inboxMessageConverter.convertResponse(savedMessageEntity);
    }

    @Transactional(readOnly=true)
    public ListOfInboxMessages fetchMessageListForUser(String userId, boolean onlyUnread, Pageable pageable) {
        final List<InboxMessageEntity> messageEntities;
        if (onlyUnread) {
            messageEntities = inboxRepository.findAllByUserIdAndReadOrderByTimestampCreatedDesc(userId, false, pageable);
        } else {
            messageEntities = inboxRepository.findAllByUserIdOrderByTimestampCreatedDesc(userId, pageable);
        }
        return inboxMessageConverter.convert(messageEntities, pageable.getPageNumber(), pageable.getPageSize());
    }

    @Transactional(readOnly=true)
    public GetInboxMessageDetailResponse fetchMessageDetail(String userId, String id) throws InboxMessageNotFoundException {
        final Optional<InboxMessageEntity> messageEntity = inboxRepository.findFirstByIdAndUserId(id, userId);
        if (!messageEntity.isPresent()) {
            throw new InboxMessageNotFoundException("Unable to fetch message: " + id + ", for user: " + userId + ".");
        }
        return inboxMessageConverter.convertResponse(messageEntity.get());
    }

    @Transactional(readOnly=true)
    public GetInboxMessageCountResponse fetchMessageCountForUser(String userId) {
        final long countUnread = inboxRepository.countAllByUserIdAndRead(userId, false);
        return new GetInboxMessageCountResponse(countUnread);
    }

    @Transactional
    public GetInboxMessageDetailResponse readMessage(String userId, String id) throws InboxMessageNotFoundException {
        final Optional<InboxMessageEntity> messageEntity = inboxRepository.findFirstByIdAndUserId(id, userId);
        if (!messageEntity.isPresent()) {
            throw new InboxMessageNotFoundException("Unable to mark message: " + id + ", for user: " + userId + " as read.");
        }
        final InboxMessageEntity inboxMessage = messageEntity.get();
        if (!inboxMessage.isRead()) { // do not call repository save if there is no change.
            inboxMessage.setRead(true);
            final InboxMessageEntity savedInboxMessage = inboxRepository.save(inboxMessage);
            return inboxMessageConverter.convertResponse(savedInboxMessage);
        } else {
            return inboxMessageConverter.convertResponse(inboxMessage);
        }
    }
}
