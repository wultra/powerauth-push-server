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

import io.getlime.push.repository.model.PushDeviceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Repository interface used to access device registration database.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Repository
@Transactional
public interface PushDeviceRepository extends CrudRepository<PushDeviceEntity, Long> {

    /**
     * Find first device for given app ID and push token.
     * @param appId App ID.
     * @param pushToken Push token.
     * @return Device registration with provided values.
     */
    PushDeviceEntity findFirstByAppIdAndPushToken(Long appId, String pushToken);

    /**
     * Find all device registrations by given activation ID. In normal case, the list will contain only one value.
     * @param activationId Activation ID.
     * @return List of device registrations.
     */
    List<PushDeviceEntity> findByActivationId(String activationId);

    /**
     * Find all device registration by given user ID and app ID. This list represents all devices that a single user
     * has registered.
     * @param userId User ID.
     * @param appId App ID.
     * @return List of device registrations.
     */
    List<PushDeviceEntity> findByUserIdAndAppId(String userId, Long appId);

    /**
     * Find all device registration by given user ID, app ID and activation ID. This list should contain one record
     * only under normal circumstances.
     * @param userId User ID.
     * @param appId App ID.
     * @param activationId Activation ID.
     * @return List of device registrations.
     */
    List<PushDeviceEntity> findByUserIdAndAppIdAndActivationId(String userId, Long appId, String activationId);


}
