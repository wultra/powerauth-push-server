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

import io.getlime.push.repository.model.InboxMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing message inbox.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Repository
public interface InboxRepository extends PagingAndSortingRepository<InboxMessageEntity, String> {

    /**
     * Find all messages for given user ID.
     * @param userId User ID.
     * @param pageable Paging parameters.
     * @return List of messages for user ID.
     */
    List<InboxMessageEntity> findAllByUserIdOrderByTimestampCreatedDesc(String userId, Pageable pageable);

    /**
     * Find messages for given user ID in a given read state.
     * @param userId User ID.
     * @param read Should the query return read messages, or those that are not read?
     * @param pageable Paging parameters.
     * @return List of messages for user ID with provided read state.
     */
    List<InboxMessageEntity> findAllByUserIdAndReadOrderByTimestampCreatedDesc(String userId, boolean read, Pageable pageable);

    /**
     * Find first message with given ID and user ID.
     * @param id Message ID.
     * @param userId User ID.
     * @return First message matching ID and user ID.
     */
    Optional<InboxMessageEntity> findFirstByIdAndUserId(String id, String userId);

    /**
     * Return how many there are records for given user ID with provided read state.
     * @param userId User ID.
     * @param read Read status.
     * @return Count of messages for given user in provided read state.
     */
    long countAllByUserIdAndRead(String userId, boolean read);

}
