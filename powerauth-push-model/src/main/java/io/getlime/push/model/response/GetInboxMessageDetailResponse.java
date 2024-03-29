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

package io.getlime.push.model.response;

import io.getlime.push.model.enumeration.MessageType;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Response with the post message.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class GetInboxMessageDetailResponse {

    private String id;
    private String userId;
    private MessageType type;
    private String subject;
    private String summary;
    private String body;
    private boolean read;
    private Date timestampCreated;
    private Date timestampRead;
    private List<String> applications = new ArrayList<>();

}
