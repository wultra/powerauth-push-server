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

package com.wultra.push.repository.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Class representing campaign sent via push notification.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Entity
@Table(name = "push_campaign")
@Getter
@Setter
public class PushCampaignEntity implements Serializable {

    /**
     * Campaign ID.
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_campaign", sequenceName = "push_campaign_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_campaign")
    private Long id;

    /**
     * App credentials.
     */
    @ManyToOne
    @JoinColumn(name = "app_id", referencedColumnName = "id", nullable = false, updatable = false)
    private AppCredentialsEntity appCredentials;

    /**
     * Message.
     */
    @Column(name = "message", nullable = false, updatable = false, length = 4000)
    private String message;

    /**
     * Flag indicating if campaign was sent.
     */
    @Column(name = "is_sent")
    private boolean sent;

    /**
     * Timestamp the campaign was created.
     */
    @Column(name = "timestamp_created", nullable = false, updatable = false)
    private Date timestampCreated;

    /**
     * Timestamp the campaign was sent.
     */
    @Column(name = "timestamp_sent")
    private Date timestampSent;

    /**
     * Timestamp the campaign was completed.
     */
    @Column(name = "timestamp_completed")
    private Date timestampCompleted;

}
