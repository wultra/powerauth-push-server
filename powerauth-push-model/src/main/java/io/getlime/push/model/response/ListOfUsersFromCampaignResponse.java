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

package io.getlime.push.model.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.getlime.push.model.customization.CustomPagedListOfUsersDeserializer;
import io.getlime.push.model.entity.ListOfUsers;

/**
 * Response used for getting a list of users from certain campaign
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@JsonDeserialize(using = CustomPagedListOfUsersDeserializer.class)
public class ListOfUsersFromCampaignResponse {
    private Long campaignId;
    private ListOfUsers users;

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public ListOfUsers getUsers() {
        return users;
    }

    public void setUsers(ListOfUsers users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListOfUsersFromCampaignResponse that = (ListOfUsersFromCampaignResponse) o;

        if (campaignId != null ? !campaignId.equals(that.campaignId) : that.campaignId != null) return false;
        return users != null ? users.equals(that.users) : that.users == null;
    }

    @Override
    public int hashCode() {
        int result = campaignId != null ? campaignId.hashCode() : 0;
        result = 31 * result + (users != null ? users.hashCode() : 0);
        return result;
    }
}
