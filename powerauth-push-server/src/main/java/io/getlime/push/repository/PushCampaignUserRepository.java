/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
import java.util.List;

/**
 * Repository interface used to access push campaign users database.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@Repository
public interface PushCampaignUserRepository extends PagingAndSortingRepository<PushCampaignUserEntity, Long> {

    PushCampaignUserEntity findFirstByUserIdAndCampaignId(String userId, Long campaignId);
    List<PushCampaignUserEntity> findAllByCampaignId(Long campaignId);
    List<PushCampaignUserEntity> findAllByCampaignId(Long campaignId, Pageable pageable);
}
