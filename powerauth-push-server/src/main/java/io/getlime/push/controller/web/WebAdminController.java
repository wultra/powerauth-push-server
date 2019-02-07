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

package io.getlime.push.controller.web;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.push.controller.web.model.form.*;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.entity.PushServerApplication;
import io.getlime.push.model.response.CreateApplicationResponse;
import io.getlime.push.model.response.GetApplicationDetailResponse;
import io.getlime.push.model.response.GetApplicationListResponse;
import io.getlime.push.validator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

/**
 * Class representing web admin interface for push server. Web interface allows listing
 * and managing Push Server credentials.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
public class WebAdminController {

    private static final Logger logger = LoggerFactory.getLogger(WebAdminController.class);

    private final PushServerClient pushServerClient;
    private AppCreateFormValidator appCreateFormValidator;
    private ComposePushMessageFormValidator composePushMessageFormValidator;
    private RemoveCredentialsFormValidator removeCredentialsFormValidator;
    private UploadAndroidCredentialsFormValidator uploadAndroidCredentialsFormValidator;
    private UploadIosCredentialsFormValidator uploadIosCredentialsFormValidator;

    @Autowired
    public WebAdminController(PushServerClient pushServerClient) {
        this.pushServerClient = pushServerClient;
    }

    @Autowired
    public void setAppCreateFormValidator(AppCreateFormValidator appCreateFormValidator) {
        this.appCreateFormValidator = appCreateFormValidator;
    }

    @Autowired
    public void setComposePushMessageFormValidator(ComposePushMessageFormValidator composePushMessageFormValidator) {
        this.composePushMessageFormValidator = composePushMessageFormValidator;
    }

    @Autowired
    public void setRemoveCredentialsFormValidator(RemoveCredentialsFormValidator removeCredentialsFormValidator) {
        this.removeCredentialsFormValidator = removeCredentialsFormValidator;
    }

    @Autowired
    public void setUploadAndroidCredentialsFormValidator(UploadAndroidCredentialsFormValidator uploadAndroidCredentialsFormValidator) {
        this.uploadAndroidCredentialsFormValidator = uploadAndroidCredentialsFormValidator;
    }

    @Autowired
    public void setUploadIosCredentialsFormValidator(UploadIosCredentialsFormValidator uploadIosCredentialsFormValidator) {
        this.uploadIosCredentialsFormValidator = uploadIosCredentialsFormValidator;
    }

    @InitBinder("appCreateForm")
    protected void initAppCreateFormBinder(WebDataBinder binder) {
        binder.setValidator(appCreateFormValidator);
    }

    @InitBinder("composePushMessageForm")
    protected void initComposePushMessageFormBinder(WebDataBinder binder) {
        binder.setValidator(composePushMessageFormValidator);
    }

    @InitBinder("removeCredentialsForm")
    protected void initRemoveCredentialsFormBinder(WebDataBinder binder) {
        binder.setValidator(removeCredentialsFormValidator);
    }

    @InitBinder("uploadAndroidCredentialsForm")
    protected void initUploadAndroidCredentialsFormBinder(WebDataBinder binder) {
        binder.setValidator(uploadAndroidCredentialsFormValidator);
    }

    @InitBinder("uploadIosCredentialsForm")
    protected void initUploadIosCredentialsFormBinder(WebDataBinder binder) {
        binder.setValidator(uploadIosCredentialsFormValidator);
    }

    // Web Admin Screens

    /**
     * Redirect to application list.
     * @return Redirect.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String redirect() {
        return "redirect:/web/admin/app/list";
    }

    /**
     * List applications.
     * @param model Page model.
     * @return Applications page.
     */
    @RequestMapping(value = "web/admin/app/list", method = RequestMethod.GET)
    public String listApplications(Map<String, Object> model) {
        try {
            ObjectResponse<GetApplicationListResponse> appListResponse = pushServerClient.getApplicationList();
            model.put("applications", appListResponse.getResponseObject().getApplicationList());
            return "applications";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Prepare create application page.
     * @param model Page model.
     * @return Application create page.
     */
    @RequestMapping(value = "web/admin/app/create", method = RequestMethod.GET)
    public String createApplication(Map<String, Object> model) {
        try {
            ObjectResponse<GetApplicationListResponse> appListResponse = pushServerClient.getUnconfiguredApplicationList();
            model.put("applications", appListResponse.getResponseObject().getApplicationList());
            return "applicationCreate";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Prepare edit application page.
     * @param id Application credentials entity ID.
     * @param model Page model.
     * @return Application edit page.
     */
    @RequestMapping(value = "web/admin/app/{id}/edit", method = RequestMethod.GET)
    public String editApplication(@PathVariable Long id, Map<String, Object> model) {
        if (id == null) {
            return "error";
        }
        try {
            ObjectResponse<GetApplicationDetailResponse> appResponse = pushServerClient.getApplicationDetail(id, false, false);
            model.put("application", appResponse.getResponseObject().getApplication());
            return "applicationEdit";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Prepare upload of iOS configuration.
     * @param id Application credentials entity ID.
     * @param model Page model.
     * @return The iOS upload page.
     */
    @RequestMapping(value = "web/admin/app/{id}/ios/upload")
    public String uploadIosCredentials(@PathVariable Long id, Map<String, Object> model) {
        if (id == null) {
            return "error";
        }
        try {
            ObjectResponse<GetApplicationDetailResponse> appResponse = pushServerClient.getApplicationDetail(id, true, false);
            GetApplicationDetailResponse objectResponse = appResponse.getResponseObject();
            PushServerApplication app = objectResponse.getApplication();
            UploadIosCredentialsForm form = (UploadIosCredentialsForm) model.get("form");
            if (form == null) {
                model.put("bundle", objectResponse.getIosBundle());
                model.put("keyId", objectResponse.getIosKeyId());
                model.put("teamId", objectResponse.getIosTeamId());
            } else {
                model.put("bundle", form.getBundle());
                model.put("keyId", form.getKeyId());
                model.put("teamId", form.getTeamId());
            }
            model.put("application", app);
            return "applicationIosUpload";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Prepare upload of Android configuration.
     * @param id Application credentials entity ID.
     * @param model Page model.
     * @return The Android upload page.
     */
    @RequestMapping(value = "web/admin/app/{id}/android/upload")
    public String uploadAndroidCredentials(@PathVariable Long id, Map<String, Object> model) {
        if (id == null) {
            return "error";
        }
        try {
            ObjectResponse<GetApplicationDetailResponse> appResponse = pushServerClient.getApplicationDetail(id, false, true);
            GetApplicationDetailResponse objectResponse = appResponse.getResponseObject();
            PushServerApplication app = objectResponse.getApplication();
            UploadAndroidCredentialsForm form = (UploadAndroidCredentialsForm) model.get("form");
            if (form == null) {
                model.put("projectId", objectResponse.getAndroidProjectId());
            } else {
                model.put("projectId", form.getProjectId());
                model.put("privateKey", form.getPrivateKey());
            }
            model.put("application", app);
            return "applicationAndroidUpload";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Prepare create push message.
     * @param model Page model.
     * @return Create push message page.
     */
    @RequestMapping(value = "web/admin/message/create", method = RequestMethod.GET)
    public String createPushMessage(Map<String, Object> model) {
        try {
            ObjectResponse<GetApplicationListResponse> appListResponse = pushServerClient.getApplicationList();
            model.put("applications", appListResponse.getResponseObject().getApplicationList());
            return "pushMessageCreate";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    // Action Handlers

    /**
     * Create application.
     * @param appCreateForm Application create form.
     * @param bindingResult Validation result.
     * @param model Page model.
     * @return Page with details.
     */
    @RequestMapping(value = "web/admin/app/create/do.submit", method = RequestMethod.POST)
    public String actionCreateApplication(@Valid AppCreateForm appCreateForm, BindingResult bindingResult, Map<String, Object> model) {
        if (bindingResult.hasErrors()) {
            return "redirect:web/admin/app/create";
        }
        try {
            ObjectResponse<CreateApplicationResponse> createAppResponse = pushServerClient.createApplication(appCreateForm.getAppId());
            return "redirect:/web/admin/app/" + createAppResponse.getResponseObject().getId() + "/edit";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Update iOS configuration.
     * @param id Application credentials entity ID.
     * @param uploadIosCredentialsForm Upload iOS credentials form.
     * @param bindingResult Validation result.
     * @param model Page model.
     * @param attr Redirect attributes.
     * @return Details page.
     */
    @RequestMapping(value = "web/admin/app/{id}/ios/upload/do.submit", method = RequestMethod.POST)
    public String actionUploadIosCredentials(@PathVariable Long id, @Valid UploadIosCredentialsForm uploadIosCredentialsForm, BindingResult bindingResult, Map<String, Object> model, RedirectAttributes attr) {
        if (id == null) {
            return "error";
        }
        if (bindingResult.hasErrors()) {
            attr.addFlashAttribute("fields", bindingResult);
            attr.addFlashAttribute("form", uploadIosCredentialsForm);
            return "redirect:/web/admin/app/" + id + "/ios/upload";
        }
        try {
            pushServerClient.updateIos(id, uploadIosCredentialsForm.getBundle(), uploadIosCredentialsForm.getKeyId(), uploadIosCredentialsForm.getTeamId(), uploadIosCredentialsForm.getPrivateKey().getBytes());
            return "redirect:/web/admin/app/" + id + "/edit";
        } catch (PushServerClientException | IOException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Remove iOS configuration.
     * @param removeCredentialsForm Remove iOS credentials form.
     * @param bindingResult Validation result.
     * @param id Application credentials entity ID.
     * @param model Page model.
     * @return Details page.
     */
    @RequestMapping(value = "web/admin/app/{id}/ios/remove/do.submit", method = RequestMethod.POST)
    public String actionRemoveCredentials(@Valid RemoveCredentialsForm removeCredentialsForm, BindingResult bindingResult, @PathVariable Long id, Map<String, Object> model) {
        if (bindingResult.hasErrors() || (id == null || !id.equals(removeCredentialsForm.getId()))) {
            return "error";
        }
        try {
            pushServerClient.removeIos(id);
            return "redirect:/web/admin/app/" + id  + "/edit";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Upload Android configuration.
     * @param id Application credentials entity ID.
     * @param uploadAndroidCredentialsForm Upload Android credentials form.
     * @param bindingResult Validation result.
     * @param model Page model.
     * @param attr Redirect attributes.
     * @return Details page.
     */
    @RequestMapping(value = "web/admin/app/{id}/android/upload/do.submit", method = RequestMethod.POST)
    public String actionUploadAndroidCredentials(@PathVariable Long id, @Valid UploadAndroidCredentialsForm uploadAndroidCredentialsForm, BindingResult bindingResult, Map<String, Object> model, RedirectAttributes attr) {
        if (id == null) {
            return "error";
        }
        if (bindingResult.hasErrors()) {
            attr.addFlashAttribute("fields", bindingResult);
            attr.addFlashAttribute("form", uploadAndroidCredentialsForm);
            return "redirect:/web/admin/app/" + id + "/android/upload";
        }
        try {
            pushServerClient.updateAndroid(id, uploadAndroidCredentialsForm.getProjectId(), uploadAndroidCredentialsForm.getPrivateKey().getBytes());
            return "redirect:/web/admin/app/" + id + "/edit";
        } catch (PushServerClientException | IOException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Remove Android configuration.
     * @param id Application credentials entity ID.
     * @param model Page model.
     * @return Details page.
     */
    @RequestMapping(value = "web/admin/app/{id}/android/remove/do.submit", method = RequestMethod.POST)
    public String actionRemoveAndroidCredentials(@Valid RemoveCredentialsForm removeCredentialsForm, BindingResult bindingResult, @PathVariable Long id, Map<String, Object> model) {
        if (bindingResult.hasErrors() || (id == null || !id.equals(removeCredentialsForm.getId()))) {
            return "error";
        }
        try {
            pushServerClient.removeAndroid(id);
            return "redirect:/web/admin/app/" + id + "/edit";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

    /**
     * Create push message.
     * @param composePushMessageForm Compose push message form.
     * @param bindingResult Validation result.
     * @param model Page model.
     * @param attr Redirect attributes.
     * @return Create push message page.
     */
    @RequestMapping(value = "web/admin/message/create/do.submit", method = RequestMethod.POST)
    public String actionCreatePushMessage(@Valid ComposePushMessageForm composePushMessageForm, BindingResult bindingResult, Map<String, Object> model, RedirectAttributes attr) {
        if (bindingResult.hasErrors()) {
            attr.addFlashAttribute("fields", bindingResult);
            attr.addFlashAttribute("form", composePushMessageForm);
            return "redirect:/web/admin/message/create";
        }
        try {
            final PushMessage message = new PushMessage();
            message.setUserId(composePushMessageForm.getUserId());
            final PushMessageBody messageBody = new PushMessageBody();
            messageBody.setTitle(composePushMessageForm.getTitle());
            messageBody.setBody(composePushMessageForm.getBody());
            messageBody.setSound(composePushMessageForm.isSound() ? "default" : null);
            message.setBody(messageBody);
            pushServerClient.sendPushMessage(composePushMessageForm.getAppId(), message);
            return "redirect:/web/admin/message/create";
        } catch (PushServerClientException ex) {
            model.put("message", ex.getMessage());
            logger.error(ex.getMessage());
            return "error";
        }
    }

}
