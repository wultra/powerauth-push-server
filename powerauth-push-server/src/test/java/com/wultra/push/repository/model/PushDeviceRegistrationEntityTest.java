/*
 * Copyright 2024 Wultra s.r.o.
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
package com.wultra.push.repository.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test {@link PushDeviceRegistrationEntity}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@DataJpaTest
@ActiveProfiles("test")
@Sql
class PushDeviceRegistrationEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void testConvertPlatform_load() {
        final var tested = entityManager.find(PushDeviceRegistrationEntity.class, 1L);

        assertEquals(Platform.IOS, tested.getPlatform());
    }

    @Test
    void testConvertPlatform_save() {
        final var tested = new PushDeviceRegistrationEntity();
        tested.setAppCredentials(entityManager.getReference(AppCredentialsEntity.class, 1L));
        tested.setPlatform(Platform.ANDROID);
        tested.setTimestampLastRegistered(new Date());
        tested.setPushToken("token1");

        final Long id = entityManager.merge(tested).getId();

        final Object result = entityManager.createNativeQuery("select platform from push_device_registration where id=:id")
                .setParameter("id", id)
                .getSingleResult();

        assertEquals("android", result);
    }

    @Test
    void testConvertPlatform_invalidMapping() {
        final Exception result = assertThrows(PersistenceException.class,
                () -> entityManager.find(PushDeviceRegistrationEntity.class, 2L));

        assertEquals("Error attempting to apply AttributeConverter", result.getMessage());
        assertEquals("No mapping for platform: xxx", result.getCause().getMessage());
    }
}