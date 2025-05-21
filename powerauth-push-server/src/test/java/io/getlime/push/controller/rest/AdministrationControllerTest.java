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
package io.getlime.push.controller.rest;

import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link AdministrationController}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdministrationControllerTest {

    @MockBean
    private AppCredentialsRepository appCredentialsRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUpdateHuawei_nullRequestObject() throws Exception {
        mockMvc.perform(post("/admin/app/huawei/update")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void testUpdateHuawei_invalidRequest() throws Exception {
        mockMvc.perform(post("/admin/app/huawei/update")
                        .content("""
                                {
                                  "requestObject": {
                                      "appId": "",
                                      "projectId": "",
                                      "clientId": "",
                                      "clientSecret": ""
                                  }
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void testUpdateHuawei() throws Exception {
        final AppCredentialsEntity appCredential = new AppCredentialsEntity();
        appCredential.setAppId("1");

        when(appCredentialsRepository.findFirstByAppId("1")).thenReturn(Optional.of(appCredential));

        mockMvc.perform(post("/admin/app/huawei/update")
                        .content("""
                                {
                                  "requestObject": {
                                      "appId": "1",
                                      "projectId": "projectId",
                                      "clientId": "oAuth 2.0 client ID",
                                      "clientSecret": "oAuth 2.0 client secret"
                                  }
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void testRemoveHuawei_nullRequestObject() throws Exception {
        mockMvc.perform(post("/admin/app/huawei/remove")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void testRemoveHuawei_invalidRequest() throws Exception {
        mockMvc.perform(post("/admin/app/huawei/remove")
                        .content("""
                                {
                                  "requestObject": {
                                      "appId": ""
                                  }
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void testRemoveHuawei() throws Exception {
        final AppCredentialsEntity appCredential = new AppCredentialsEntity();
        appCredential.setAppId("1");
        appCredential.setHmsClientSecret("clientSecret");
        appCredential.setHmsClientId("clientId");
        appCredential.setHmsProjectId("projectId");

        when(appCredentialsRepository.findFirstByAppId("1")).thenReturn(Optional.of(appCredential));

        mockMvc.perform(post("/admin/app/huawei/remove")
                        .content("""
                                {
                                  "requestObject": {
                                      "appId": "1"
                                  }
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

        final ArgumentCaptor<AppCredentialsEntity> argumentCaptor = ArgumentCaptor.forClass(AppCredentialsEntity.class);
        verify(appCredentialsRepository).save(argumentCaptor.capture());

        final AppCredentialsEntity argumentCaptorValue = argumentCaptor.getValue();
        assertNull(argumentCaptorValue.getHmsClientId());
        assertNull(argumentCaptorValue.getHmsClientSecret());
        assertNull(argumentCaptorValue.getHmsProjectId());
    }

}
