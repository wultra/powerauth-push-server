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

package io.getlime.push.controller.rest;

import io.getlime.push.configuration.PowerAuthPushServiceConfiguration;
import io.getlime.push.model.StatusResponse;
import io.getlime.push.model.SystemStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * Class representing controller used for service and maintenance purpose.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "push/service")
public class ServiceController {

    @Autowired
    private PowerAuthPushServiceConfiguration pushServiceConfiguration;

    /**
     * Basic "hello" controller resource, used to check the service.
     * @return Basic OK response.
     */
    @RequestMapping(value = "hello")
    public @ResponseBody StatusResponse hello() {
        StatusResponse response = new StatusResponse();
        response.setStatus(StatusResponse.OK);
        return response;
    }

    /**
     * Controller resource with system information.
     * @return System status info.
     */
    @RequestMapping(value = "status")
    public @ResponseBody SystemStatusResponse systemStatus() {
        SystemStatusResponse response = new SystemStatusResponse();
        response.setStatus(StatusResponse.OK);
        response.setApplicationName(pushServiceConfiguration.getPushServerName());
        response.setApplicationDisplayName(pushServiceConfiguration.getPushServerDisplayName());
        response.setApplicationEnvironment(pushServiceConfiguration.getPushServerEnvironment());
        response.setTimestamp(new Date());
        return response;
    }

}
