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

package io.getlime.push.repository.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Database entity representing a message inbox.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "push_inbox")
public class InboxMessageEntity implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "read")
    private boolean read;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_read")
    private Date timestampRead;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InboxMessageEntity)) return false;
        InboxMessageEntity that = (InboxMessageEntity) o;
        return id.equals(that.id)
                && userId.equals(that.userId)
                && subject.equals(that.subject)
                && body.equals(that.body)
                && timestampCreated.equals(that.timestampCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, subject, body, timestampCreated);
    }
}
