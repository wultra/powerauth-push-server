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

package io.getlime.push.repository.converter;

import io.getlime.push.model.entity.InboxMessage;
import io.getlime.push.model.entity.ListOfInboxMessages;
import io.getlime.push.model.request.CreateInboxMessageRequest;
import io.getlime.push.model.response.GetInboxMessageDetailResponse;
import io.getlime.push.repository.model.InboxMessageEntity;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Converter for inbox related entities.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Component
public class InboxMessageConverter {

    /**
     * Convert from API request model to database entity.
     *
     * @param id Random UUID identifier.
     * @param userId User ID.
     * @param appId Application ID.
     * @param source Request model.
     * @param date Date on which the message should be created.
     * @return Database entity.
     */
    public InboxMessageEntity convert(UUID id, String userId, String appId, CreateInboxMessageRequest source, Date date) {
        if (source == null) {
            return null;
        }
        final InboxMessageEntity destination = new InboxMessageEntity();
        destination.setInboxId(id.toString());
        destination.setUserId(userId);
        destination.setAppId(appId);
        destination.setSubject(source.getSubject());
        destination.setBody(source.getBody());
        destination.setTimestampCreated(date);
        return destination;
    }

    /**
     * Convert from database entity to API detail response.
     *
     * @param source Database entity.
     * @return API response (detail).
     */
    public GetInboxMessageDetailResponse convertResponse(InboxMessageEntity source) {
        if (source == null) {
            return null;
        }
        final GetInboxMessageDetailResponse destination = new GetInboxMessageDetailResponse();
        destination.setId(source.getInboxId());
        destination.setSubject(source.getSubject());
        destination.setBody(source.getBody());
        destination.setRead(source.isRead());
        destination.setTimestampCreated(source.getTimestampCreated());
        return destination;
    }

    /**
     * Convert database entity to basic API model entity.
     *
     * @param source Database entity.
     * @return API model entity.
     */
    public InboxMessage convert(InboxMessageEntity source) {
        if (source == null) {
            return null;
        }
        final InboxMessage destination = new InboxMessage();
        destination.setId(source.getInboxId());
        destination.setSubject(source.getSubject());
        destination.setRead(source.isRead());
        destination.setTimestampCreated(source.getTimestampCreated());
        return destination;
    }

    /**
     * Convert list of database entities to response with API entities and appropriate paging.
     *
     * @param source List of database entities.
     * @param page Page.
     * @param size Size.
     * @return List of API entities with paging set.
     */
    public ListOfInboxMessages convert(List<InboxMessageEntity> source, Integer page, Integer size) {
        if (source == null) {
            return null;
        }
        final ListOfInboxMessages destination = new ListOfInboxMessages();
        for (InboxMessageEntity entity : source) {
            final InboxMessage inboxMessage = convert(entity);
            destination.add(inboxMessage);
        }
        return destination;
    }

}
