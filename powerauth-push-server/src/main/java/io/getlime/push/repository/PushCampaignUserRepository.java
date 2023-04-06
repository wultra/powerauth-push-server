/*
 * Copyright 2016 Wultra s.r.o.
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

import io.getlime.push.repository.model.PushCampaignUserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface used to access push campaign users database.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@Repository
@Transactional
public interface PushCampaignUserRepository extends PagingAndSortingRepository<PushCampaignUserEntity, Long> {

    /**
     * Fetches detailed information about the user who is scheduled to given campaign.
     * @param userId User ID.
     * @param campaignId Campaign ID.
     * @return Detailed information about user scheduled in given campaign.
     */
    PushCampaignUserEntity findFirstByUserIdAndCampaignId(String userId, Long campaignId);

    /**
     * Find all users who are added to given campaign. Since there could be many users, this
     * resource is paged.
     * @param campaignId Campaign ID.
     * @param pageable Paging information.
     * @return List of users who are added in given campaign (paged).
     */
    List<PushCampaignUserEntity> findAllByCampaignId(Long campaignId, Pageable pageable);

    /**
     * Delete all users who are associated with given campaign.
     * @param campaignId Campaign ID.
     */
    void deleteByCampaignId(Long campaignId);

    /**
     * Delete user with given ID from campaign with given ID.
     * @param campaignId Campaign ID.
     * @param userId User ID.
     */
    void deleteByCampaignIdAndUserId(Long campaignId, String userId);
}
