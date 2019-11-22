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

package io.getlime.push.client;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Class used for testing client-server methods
 * All tests cover each method from {@link PushServerClient}.
 * Methods are named with suffix "Test" and are just compared with expected server responses.
 * Using in memory H2 create/drop database.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test-multiple-activations.properties")
public class PushServerClientTestMultipleActivations {

    private static final String MOCK_ACTIVATION_ID = "11111111-1111-1111-1111-111111111111";
    private static final String MOCK_ACTIVATION_ID_2 = "22222222-2222-2222-2222-222222222222";
    private static final Long MOCK_APPLICATION_ID = 1L;
    private static final String MOCK_PUSH_TOKEN = "1234567890987654321234567890";

    @MockBean
    private PushServerClient pushServerClient;

    @LocalServerPort
    private int port;

    @Before
    public void setPushServerClientsUrl() {
        pushServerClient = new PushServerClient("http://localhost:" + port);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void createDeviceWithMultipleActivationsTest() throws Exception {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(MOCK_ACTIVATION_ID);
        activationIds.add(MOCK_ACTIVATION_ID_2);
        boolean result = pushServerClient.createDeviceForActivations(MOCK_APPLICATION_ID, MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds);
        assertTrue(result);
    }
}
