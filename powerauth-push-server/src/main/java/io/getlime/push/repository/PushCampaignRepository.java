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

import io.getlime.push.repository.model.PushCampaignEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Repository interface used to access push campaign database.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Repository
@Transactional
public interface PushCampaignRepository extends CrudRepository<PushCampaignEntity, Long>{

    /**
     * Finds all push campaigns that are in given send state.
     * @param sent Flag indicating if only sent campaigns should be returned, or only not sent campaigns.
     * @return Collection of campaigns in given sent state.
     */
    List<PushCampaignEntity> findAllBySent(Boolean sent);

}
