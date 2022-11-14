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

import javax.persistence.*;
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

    /**
     * Entity ID.
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_app_inbox", sequenceName = "push_inbox_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_app_inbox")
    private Long id;

    @Column(name = "inbox_id", nullable = false)
    private String inboxId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "app_id", nullable = false)
    private String appId;

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
        final InboxMessageEntity that = (InboxMessageEntity) o;
        return inboxId.equals(that.inboxId)
                && userId.equals(that.userId)
                && appId.equals(that.appId)
                && subject.equals(that.subject)
                && body.equals(that.body)
                && timestampCreated.equals(that.timestampCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inboxId, userId, appId, subject, body, timestampCreated);
    }
}
