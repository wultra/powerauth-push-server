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

package io.getlime.push.controller.rest;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.model.response.ServiceStatusResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Class representing controller used for service and maintenance purpose.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@RequestMapping(value = "push/service")
public class ServiceController {

    private final PushServiceConfiguration pushServiceConfiguration;
    private BuildProperties buildProperties;

    @Autowired
    public ServiceController(PushServiceConfiguration pushServiceConfiguration) {
        this.pushServiceConfiguration = pushServiceConfiguration;
    }

    @Autowired(required = false) // otherwise Unit tests will fail ...
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    /**
     * Controller resource with system information.
     * @return System status info.
     */
    @GetMapping(value = "status")
    @ApiOperation(value = "Service status",
                  notes = "Send a system status response, with basic information about the running application.")
    public ObjectResponse<ServiceStatusResponse> getServiceStatus() {
        ServiceStatusResponse response = new ServiceStatusResponse();
        response.setApplicationName(pushServiceConfiguration.getPushServerName());
        response.setApplicationDisplayName(pushServiceConfiguration.getPushServerDisplayName());
        response.setApplicationEnvironment(pushServiceConfiguration.getPushServerEnvironment());
        response.setTimestamp(new Date());
        if (buildProperties != null) {
            response.setVersion(buildProperties.getVersion());
            response.setBuildTime(Date.from(buildProperties.getTime()));
        }
        return new ObjectResponse<>(response);
    }
}
