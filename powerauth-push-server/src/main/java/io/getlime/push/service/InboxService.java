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
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.repository.model.InboxMessageEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
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
    public GetInboxMessageDetailResponse postMessage(CreateInboxMessageRequest request) throws AppNotFoundException {
        final List<AppCredentialsEntity> apps = fetchAppsForAppIds(request.getApplications());
        if (apps.size() != request.getApplications().size()) {
            logger.info("Application list received: {}, apps configured in the system: {}.", request.getApplications(), apps.stream().map(AppCredentialsEntity::getAppId));
            throw new AppNotFoundException("Application list contained an app that is not configured in the system.");
        }
        final String userId = request.getUserId();
        final InboxMessageEntity messageEntity = inboxMessageConverter.convert(UUID.randomUUID(), userId, request, apps, new Date());
        final InboxMessageEntity savedMessageEntity = inboxRepository.save(messageEntity);
        logger.info("Posted new inbox message for user: {}, message ID: {}", userId, messageEntity.getInboxId());
        return inboxMessageConverter.convertResponse(savedMessageEntity);
    }

    @Transactional(readOnly=true)
    public ListOfInboxMessages fetchMessageListForUser(String userId, List<String> appIds, boolean onlyUnread, Pageable pageable) throws AppNotFoundException {
        final List<AppCredentialsEntity> apps = fetchAppsForAppIds(appIds);
        if (apps.size() != appIds.size()) {
            logger.info("Application list received: {}, apps configured in the system: {}.", appIds, apps.stream().map(AppCredentialsEntity::getAppId));
            throw new AppNotFoundException("Application list contained an app that is not configured in the system.");
        }

        final List<InboxMessageEntity> messageEntities;
        if (onlyUnread) {
            messageEntities = inboxRepository.findAllByUserIdAndApplicationsContainingAndReadOrderByTimestampCreatedDesc(userId, appIds, false, pageable);
        } else {
            messageEntities = inboxRepository.findInboxMessagesForUserAndApplications(userId, appIds, pageable);
        }
        return inboxMessageConverter.convert(messageEntities);
    }

    @Transactional(readOnly=true)
    public GetInboxMessageDetailResponse fetchMessageDetail(String inboxId) throws InboxMessageNotFoundException {
        final InboxMessageEntity messageEntity = inboxRepository.findFirstByInboxId(inboxId).orElseThrow(() ->
                new InboxMessageNotFoundException("Unable to fetch message: " + inboxId + "."));
        return inboxMessageConverter.convertResponse(messageEntity);
    }

    @Transactional(readOnly=true)
    public GetInboxMessageCountResponse fetchMessageCountForUser(String userId, String appId) throws AppNotFoundException {
        final AppCredentialsEntity app = fetchAppForAppId(appId);
        final long countUnread = inboxRepository.countAllByUserIdAndApplicationsContainingAndRead(userId, app,false);
        return new GetInboxMessageCountResponse(countUnread);
    }

    @Transactional
    public GetInboxMessageDetailResponse readMessage(String inboxId) throws InboxMessageNotFoundException {
        final InboxMessageEntity inboxMessage = inboxRepository.findFirstByInboxId(inboxId).orElseThrow(() ->
                new InboxMessageNotFoundException("Unable to mark message: " + inboxId + " as read."));
        if (!inboxMessage.isRead()) { // do not call repository save if there is no change.
            inboxMessage.setRead(true);
            inboxMessage.setTimestampRead(new Date());
            logger.info("Marked inbox message as read for message ID: {}", inboxId);
            final InboxMessageEntity savedInboxMessage = inboxRepository.save(inboxMessage);
            return inboxMessageConverter.convertResponse(savedInboxMessage);
        } else {
            return inboxMessageConverter.convertResponse(inboxMessage);
        }
    }

    @Transactional
    public void readAllMessages(String userId, String appId) throws AppNotFoundException {
        final AppCredentialsEntity appCredentialsEntity = fetchAppForAppId(appId);
        int countRead = inboxRepository.markAllAsRead(userId, appCredentialsEntity, new Date());
        logger.info("Marked all inbox messages as read for user: {}, count: {}", userId, countRead);
    }

    private AppCredentialsEntity fetchAppForAppId(String appId) throws AppNotFoundException {
        return appCredentialsRepository.findFirstByAppId(appId).orElseThrow(
                () -> new AppNotFoundException("Application was not found: " + appId));
    }

    private List<AppCredentialsEntity> fetchAppsForAppIds(List<String> apps) {
        return appCredentialsRepository.findAllByAppIdIn(apps);
    }

}
