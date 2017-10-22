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

package io.getlime.push.model.customization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.getlime.push.model.entity.ListOfUsers;
import io.getlime.push.model.response.ListOfUsersFromCampaignResponse;
import io.getlime.push.model.response.ListOfUsersPagedResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer used in retrieving users in paged response
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
public class CustomPagedListOfUsersDeserializer extends JsonDeserializer<ListOfUsersPagedResponse<ListOfUsersFromCampaignResponse>>{

    @Override
    public ListOfUsersPagedResponse<ListOfUsersFromCampaignResponse>
    deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        String status = node.get("status").asText();
        int page = node.get("page").intValue();
        int size = node.get("size").intValue();
        Long campaignId = node.get("responseObject").get("campaignId").longValue();
        List<String> users = new ArrayList<>();
        for(JsonNode user : node.get("responseObject").get("users")) {
            users.add(user.asText());
        }
        ListOfUsersFromCampaignResponse listOfUsersFromCampaignResponse = new ListOfUsersFromCampaignResponse();
        ListOfUsers listOfUsers = new ListOfUsers();
        listOfUsers.addAll(users);
        listOfUsersFromCampaignResponse.setUsers(listOfUsers);
        listOfUsersFromCampaignResponse.setCampaignId(campaignId);
        ListOfUsersPagedResponse<ListOfUsersFromCampaignResponse> listOfUsersPagedResponse = new ListOfUsersPagedResponse<>(listOfUsersFromCampaignResponse);
        listOfUsersPagedResponse.setPage(page);
        listOfUsersPagedResponse.setSize(size);
        listOfUsersPagedResponse.setStatus(status);
        return listOfUsersPagedResponse;
    }
}
