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

package io.getlime.push.repository.dao;

import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessageAttributes;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.repository.PushMessageRepository;
import io.getlime.push.repository.model.PushMessageEntity;
import io.getlime.push.repository.serialization.JsonSerialization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Date;

/**
 * Data access object for PushMessage repo.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Repository
@Transactional
public class PushMessageDAO {

    private final PushMessageRepository pushMessageRepository;
    private final JsonSerialization jsonSerialization;

    /**
     * Constructor with autowired dependencies.
     * @param pushMessageRepository Push message repository.
     * @param jsonSerialization Helper JSON serialization object.
     */
    @Autowired
    public PushMessageDAO(PushMessageRepository pushMessageRepository, JsonSerialization jsonSerialization) {
        this.pushMessageRepository = pushMessageRepository;
        this.jsonSerialization = jsonSerialization;
    }

    /**
     * Stores a push message in the database table `push_message`.
     *
     * @param pushMessageBody Push message body to be stored.
     * @param pushMessageAttributes Attributes of the push message.
     * @param userId User ID.
     * @param activationId Activation ID.
     * @param deviceId Device registration ID to be used for this message.
     * @return New database entity with push message information.
     * @throws PushServerException In case message body JSON serialization fails.
     */
    public PushMessageEntity storePushMessageObject(PushMessageBody pushMessageBody, PushMessageAttributes pushMessageAttributes, String userId, String activationId, Long deviceId) throws PushServerException {
        PushMessageEntity entity = new PushMessageEntity();
        entity.setDeviceId(deviceId);
        entity.setUserId(userId);
        entity.setActivationId(activationId);
        if (pushMessageAttributes != null) {
            entity.setPersonal(pushMessageAttributes.getPersonal());
            entity.setSilent(pushMessageAttributes.getSilent());
        } else {
            entity.setPersonal(false);
            entity.setSilent(false);
        }
        entity.setStatus(PushMessageEntity.Status.PENDING);
        entity.setTimestampCreated(new Date());
        String messageBody = jsonSerialization.serializePushMessageBody(pushMessageBody);
        entity.setMessageBody(messageBody);
        return pushMessageRepository.save(entity);
    }

    /**
     * Save the entity.
     * @param s Original entity.
     * @param <S> Generic type used for the call.
     * @return Saved entity.
     */
    public <S extends PushMessageEntity> S save(S s) {
        return pushMessageRepository.save(s);
    }
}
