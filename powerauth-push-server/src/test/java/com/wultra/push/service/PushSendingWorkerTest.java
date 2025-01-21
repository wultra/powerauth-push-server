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
package com.wultra.push.service;

import com.google.firebase.messaging.MessagingErrorCode;
import com.wultra.core.rest.client.base.RestClientException;
import com.wultra.push.configuration.PushServiceConfiguration;
import com.wultra.push.errorhandling.exceptions.FcmMissingTokenException;
import com.wultra.push.model.entity.PushMessageAttributes;
import com.wultra.push.model.entity.PushMessageBody;
import com.wultra.push.model.enumeration.Priority;
import com.wultra.push.service.fcm.FcmClient;
import com.wultra.push.service.fcm.FcmModelConverter;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

/**
 * Tests of {@link PushSendingWorker}
 *
 * @author Jan Dusil, jan.dusil@wultra.com
 */
@SpringBootTest
@ActiveProfiles("test")
class PushSendingWorkerTest {

    @Mock
    private FcmClient fcmClient;

    @Mock
    private PushSendingCallback callback;

    @Mock
    private PushServiceConfiguration pushServiceConfiguration;

    @Mock
    private FcmModelConverter fcmModelConverter;

    @InjectMocks
    private PushSendingWorker tested;

    @Test
    void testSendMessageToFcmError() throws FcmMissingTokenException {
        final RestClientException simulatedException = new RestClientException("Simulated INVALID_ARGUMENT error");
        when(pushServiceConfiguration.isFcmDataNotificationOnly()).thenReturn(false);
        when(fcmModelConverter.convertExceptionToErrorCode(simulatedException)).thenReturn(MessagingErrorCode.INVALID_ARGUMENT);
        doAnswer(invocation -> {
            final Consumer<Throwable> onError = invocation.getArgument(3);
            onError.accept(simulatedException);
            return null;
        }).when(fcmClient).exchange(any(), anyBoolean(), any(), any());

        tested.sendMessageToFcm(fcmClient, new PushMessageBody(), new PushMessageAttributes(), Priority.HIGH, "dummyToken", callback);
        verify(callback).didFinishSendingMessage(PushSendingCallback.Result.FAILED_DELETE);
    }

}

