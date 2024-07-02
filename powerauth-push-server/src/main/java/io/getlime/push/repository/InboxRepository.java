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

package io.getlime.push.repository;

import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.repository.model.InboxMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing message inbox.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Repository
public interface InboxRepository extends PagingAndSortingRepository<InboxMessageEntity, String>, CrudRepository<InboxMessageEntity, String> {

    /**
     * Find all messages for given user ID.
     * @param userId User ID.
     * @param applicationIds Application identifiers.
     * @param pageable Paging parameters.
     * @return List of messages for user ID.
     */
    @Query("SELECT o FROM InboxMessageEntity o WHERE o.id IN (" +
            "SELECT DISTINCT o1.id FROM InboxMessageEntity o1 INNER JOIN o1.applications a " +
            "WHERE o1.userId = :userId AND a.appId IN :applicationIds" +
            ") ORDER BY o.timestampCreated DESC")
    List<InboxMessageEntity> findInboxMessagesForUserAndApplications(String userId, List<String> applicationIds, Pageable pageable);

    /**
     * Find messages for given user ID in a given read state.
     * @param userId User ID.
     * @param applicationIds Application identifiers.
     * @param read Should the query return read messages, or those that are not read?
     * @param pageable Paging parameters.
     * @return List of messages for user ID with provided read state.
     */
    @Query("SELECT o FROM InboxMessageEntity o WHERE o.id IN (" +
            "SELECT DISTINCT o1.id FROM InboxMessageEntity o1 INNER JOIN o1.applications a " +
            "WHERE o1.userId = :userId AND a.appId IN :applicationIds AND o1.isRead = :read" +
            ") ORDER BY o.timestampCreated DESC")
    List<InboxMessageEntity> findAllByUserIdAndApplicationsContainingAndReadOrderByTimestampCreatedDesc(String userId, List<String> applicationIds, boolean read, Pageable pageable);

    /**
     * Find first message with given ID.
     * @param inboxId Message ID.
     * @return First message matching ID.
     */
    Optional<InboxMessageEntity> findFirstByInboxId(String inboxId);

    /**
     * Return how many there are records for given user ID with provided read state.
     * @param userId User ID.
     * @param app Application.
     * @param read Read status.
     * @return Count of messages for given user in provided read state.
     */
    long countAllByUserIdAndApplicationsContainingAndRead(String userId, AppCredentialsEntity app, boolean read);

    /**
     * Mark all user messages as read.
     * @param userId User ID.
     * @param app Application.
     * @param date Date to which mark the messages as read.
     * @return Number of messages which were read.
     */
    @Query("UPDATE InboxMessageEntity i SET i.read = true, i.timestampRead = :date WHERE i.userId = :userId AND :app MEMBER OF i.applications AND i.read = false")
    @Modifying
    int markAllAsRead(String userId, AppCredentialsEntity app, Date date);

}
