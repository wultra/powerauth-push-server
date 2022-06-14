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

import io.getlime.push.repository.model.AppCredentialsEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

/**
 * Repository interface used to access app credentials database.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Repository
@Transactional
public interface AppCredentialsRepository extends CrudRepository<AppCredentialsEntity, Long> {

    /**
     * Find app push service credentials for given app ID.
     * @param appId App ID (PowerAuth).
     * @return Push service app credentials.
     */
    Optional<AppCredentialsEntity> findFirstByAppId(String appId);

}
