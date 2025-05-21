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

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.request.CreateDeviceForActivationsRequest;
import io.getlime.push.model.request.CreateDeviceRequest;
import io.getlime.push.model.request.DeleteDeviceRequest;
import io.getlime.push.model.request.UpdateDeviceStatusRequest;
import io.getlime.push.service.PushDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for device registration related business processes.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@RequestMapping(value = "push/device")
@AllArgsConstructor
public class PushDeviceController {

    private final PushDeviceService pushDeviceService;

    /**
     * Create a new device registration.
     * @param request Device registration request.
     * @return Device registration status.
     * @throws PushServerException In case request object is invalid.
     */
    @PostMapping(value = "create")
    @Operation(summary = "Create a device",
                  description = """
                          Create a new device push token (platform specific). The call must include an activation ID, so that the token is associated with given user.Request body should contain application ID, device token, device's platform and an activation ID. If such device already exist, date on last registration is updated and also platform might be changed

                          ---Note: Since this endpoint is usually called by the back-end service, it is not secured in any way. It's the service that calls this endpoint responsibility to assure that the device is somehow authenticated before the push token is assigned with given activation ID, so that there are no incorrect bindings.""")
    public Response createDevice(@RequestBody ObjectRequest<CreateDeviceRequest> request) throws PushServerException {
        return pushDeviceService.createDevice(request.getRequestObject());
    }

    /**
     * Create a new device registration for multiple associated activations.
     * @param request Device registration request.
     * @return Device registration status.
     * @throws PushServerException In case request object is invalid.
     */
    @PostMapping(value = "create/multi")
    @Operation(summary = "Create a device for multiple associated activations",
            description = """
                    Create a new device push token (platform specific). The call must include one or more activation IDs.Request body should contain application ID, device token, device's platform and list of activation IDs. If such device already exist, date on last registration is updated and also platform might be changed

                    ---Note: Since this endpoint is usually called by the back-end service, it is not secured in any way. It's the service that calls this endpoint responsibility to assure that the device is somehow authenticated before the push token is assigned with given activation IDs, so that there are no incorrect bindings.""")
    public Response createDeviceMultipleActivations(@RequestBody ObjectRequest<CreateDeviceForActivationsRequest> request) throws PushServerException {
        return pushDeviceService.createDeviceMultipleActivations(request.getRequestObject());
    }

    /**
     * Update activation status for given device registration.
     * @param request Status update request.
     * @return Status update response.
     * @throws PushServerException In case request object is invalid.
     */
    @RequestMapping(value = "status/update", method = {RequestMethod.POST, RequestMethod.PUT})
    @Operation(summary = "Update device status",
                  description = "Update the status of given device registration based on the associated activation ID. " +
                          "This can help assure that registration is in non-active state and cannot receive personal messages.")
    public Response updateDeviceStatus(@RequestBody UpdateDeviceStatusRequest request) throws PushServerException {
        return pushDeviceService.updateDeviceStatus(request);
    }

    /**
     * Remove device registration with given push token.
     * @param request Remove registration request.
     * @return Removal status response.
     * @throws PushServerException In case request object is invalid.
     */
    @RequestMapping(value = "delete", method = {RequestMethod.POST, RequestMethod.DELETE})
    @Operation(summary = "Delete a device",
                  description = "Remove device identified by application ID and device token. " +
                          "If device identifiers don't match, nothing happens")
    public Response deleteDevice(@RequestBody ObjectRequest<DeleteDeviceRequest> request) throws PushServerException {
        return pushDeviceService.deleteDevice(request.getRequestObject());
    }

}
