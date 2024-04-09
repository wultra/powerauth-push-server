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

package io.getlime.push.model.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Object response for deleting a campaign
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Getter
@Setter
public class DeleteCampaignResponse {

    /**
     * If campaign is deleted.
     */
    private boolean deleted;

}
