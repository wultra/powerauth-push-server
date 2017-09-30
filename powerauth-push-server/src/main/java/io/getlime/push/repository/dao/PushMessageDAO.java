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

package io.getlime.push.repository.dao;

import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessageAttributes;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.repository.PushMessageRepository;
import io.getlime.push.repository.model.PushMessageEntity;
import io.getlime.push.repository.serialization.JSONSerialization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class PushMessageDAO {

    private PushMessageRepository pushMessageRepository;

    @Autowired
    public PushMessageDAO(PushMessageRepository pushMessageRepository) {
        this.pushMessageRepository = pushMessageRepository;
    }

    /**
     * Stores a push message in the database table `push_message`.
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
            entity.setEncrypted(pushMessageAttributes.getEncrypted());
            entity.setPersonal(pushMessageAttributes.getPersonal());
            entity.setSilent(pushMessageAttributes.getSilent());
        }
        entity.setStatus(PushMessageEntity.Status.PENDING);
        entity.setTimestampCreated(new Date());
        String messageBody = JSONSerialization.serializePushMessageBody(pushMessageBody);
        entity.setMessageBody(messageBody);
        return pushMessageRepository.save(entity);
    }

    public <S extends PushMessageEntity> S save(S s) {
        return pushMessageRepository.save(s);
    }
}
