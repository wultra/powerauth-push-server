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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
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

    /**
     * Inbox identifier, used as public message ID.
     */
    @Column(name = "inbox_id", nullable = false)
    private String inboxId;

    /**
     * User ID.
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * Mapping to the relation table between inbox messages and applications (credentials).
     */
    @ManyToMany
    @JoinTable(
            name = "push_inbox_app",
            joinColumns = @JoinColumn(name = "inbox_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "app_credentials_id")
    )
    @ToString.Exclude
    private List<AppCredentialsEntity> applications;

    /**
     * Message type.
     */
    @Column(name = "type", nullable = false)
    private String type;

    /**
     * Message subject.
     */
    @Column(name = "subject", nullable = false)
    private String subject;

    /**
     * Message summary.
     */
    @Column(name = "summary", nullable = false)
    private String summary;

    /**
     * Message body.
     */
    @Column(name = "body", nullable = false)
    private String body;

    /**
     * Flag indicating if the message was read.
     */
    @Column(name = "read")
    private boolean read;

    /**
     * Timestamp the message was created.
     */
    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    /**
     * Timestamp the message was read.
     */
    @Column(name = "timestamp_read")
    private Date timestampRead;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InboxMessageEntity)) return false;
        final InboxMessageEntity that = (InboxMessageEntity) o;
        return inboxId.equals(that.inboxId)
                && userId.equals(that.userId)
                && applications.equals(that.applications)
                && type.equals(that.type)
                && subject.equals(that.subject)
                && summary.equals(that.summary)
                && body.equals(that.body)
                && timestampCreated.equals(that.timestampCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inboxId, userId, applications, type, subject, summary, body, timestampCreated);
    }
}
