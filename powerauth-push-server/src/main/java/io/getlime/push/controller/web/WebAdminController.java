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

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.powerauth.soap.GetApplicationListResponse;
import io.getlime.push.controller.web.model.form.*;
import io.getlime.push.controller.web.model.view.PushServerApplication;
import io.getlime.push.model.SendPushMessageRequest;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.entity.PushSendResult;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.model.AppCredentials;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class representing web admin interface for push server. Web interface allows listing
 * and managing Push Server credentials.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
public class WebAdminController {

    private AppCredentialsRepository credentialsRepository;
    private PowerAuthServiceClient client;

    @Autowired
    public WebAdminController(AppCredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @Autowired
    void setClient(PowerAuthServiceClient client) {
        this.client = client;
    }

    // Web Admin Screens

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String redirect() {
        return "redirect:/web/admin/app/list";
    }

    @RequestMapping(value = "web/admin/app/list", method = RequestMethod.GET)
    public String listApplications(Map<String, Object> model) {
        final Iterable<AppCredentials> appCredentials = credentialsRepository.findAll();
        final List<PushServerApplication> appList = new ArrayList<>();
        for (AppCredentials appCredential: appCredentials) {
            PushServerApplication app = new PushServerApplication();
            app.setId(appCredential.getId());
            app.setAppId(appCredential.getAppId());
            app.setAppName(client.getApplicationDetail(appCredential.getAppId()).getApplicationName());
            app.setIos(appCredential.getIosPrivateKey() != null);
            app.setAndroid(appCredential.getAndroidServerKey() != null);
            appList.add(app);
        }
        model.put("applications", appList);
        return "applications";
    }

    @RequestMapping(value = "web/admin/app/create", method = RequestMethod.GET)
    public String createApplication(Map<String, Object> model) {
        // Get all applications in PA2.0 Server
        final List<GetApplicationListResponse.Applications> applicationList = client.getApplicationList();

        // Get all applications that are already set up
        final Iterable<AppCredentials> appCredentials = credentialsRepository.findAll();

        // Compute intersection by app ID
        Set<Long> identifiers = new HashSet<>();
        for (AppCredentials appCred: appCredentials) {
            identifiers.add(appCred.getAppId());
        }
        final List<GetApplicationListResponse.Applications> intersection = new ArrayList<>();
        for (GetApplicationListResponse.Applications app : applicationList) {
            if (!identifiers.contains(app.getId())) {
                intersection.add(app);
            }
        }

        // Pass data to the model
        model.put("applications", intersection);
        return "applicationCreate";
    }

    @RequestMapping(value = "web/admin/app/{id}/edit", method = RequestMethod.GET)
    public String editApplication(@PathVariable Long id, Map<String, Object> model) {
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        PushServerApplication app = new PushServerApplication();
        app.setId(appCredentials.getId());
        app.setAppId(appCredentials.getAppId());
        app.setIos(appCredentials.getIosPrivateKey() != null);
        app.setAndroid(appCredentials.getAndroidServerKey() != null);
        app.setAppName(client.getApplicationDetail(appCredentials.getAppId()).getApplicationName());
        model.put("application", app);
        return "applicationEdit";
    }

    @RequestMapping(value = "web/admin/app/{id}/ios/upload")
    public String uploadIosCredentials(@PathVariable Long id, Map<String, Object> model) {
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        PushServerApplication app = new PushServerApplication();
        app.setId(appCredentials.getId());
        app.setAppId(appCredentials.getAppId());
        app.setIos(appCredentials.getIosPrivateKey() != null);
        app.setAndroid(appCredentials.getAndroidServerKey() != null);
        app.setAppName(client.getApplicationDetail(appCredentials.getAppId()).getApplicationName());
        UploadIosCredentialsForm form = (UploadIosCredentialsForm) model.get("form");
        if (form == null) {
            model.put("bundle", appCredentials.getIosBundle());
            model.put("keyId", appCredentials.getIosKeyId());
            model.put("teamId", appCredentials.getIosTeamId());
        } else {
            model.put("bundle", form.getBundle());
            model.put("keyId", form.getKeyId());
            model.put("teamId", form.getTeamId());
        }
        model.put("application", app);
        return "applicationIosUpload";
    }

    @RequestMapping(value = "web/admin/app/{id}/android/upload")
    public String uploadAndroidCredentials(@PathVariable Long id, Map<String, Object> model) {
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        PushServerApplication app = new PushServerApplication();
        app.setId(appCredentials.getId());
        app.setAppId(appCredentials.getAppId());
        app.setIos(appCredentials.getIosPrivateKey() != null);
        app.setAndroid(appCredentials.getAndroidServerKey() != null);
        app.setAppName(client.getApplicationDetail(appCredentials.getAppId()).getApplicationName());
        UploadAndroidCredentialsForm form = (UploadAndroidCredentialsForm) model.get("form");
        if (form == null) {
            model.put("bundle", appCredentials.getAndroidBundle());
            model.put("token", appCredentials.getAndroidServerKey());
        } else {
            model.put("bundle", form.getBundle());
            model.put("token", form.getToken());
        }
        model.put("application", app);
        return "applicationAndroidUpload";
    }

    @RequestMapping(value = "web/admin/message/create", method = RequestMethod.GET)
    public String createPushMessage(Map<String, Object> model) {
        final Iterable<AppCredentials> appCredentials = credentialsRepository.findAll();
        final List<PushServerApplication> appList = new ArrayList<>();
        for (AppCredentials appCredential: appCredentials) {
            PushServerApplication app = new PushServerApplication();
            app.setId(appCredential.getId());
            app.setAppId(appCredential.getAppId());
            app.setAppName(client.getApplicationDetail(appCredential.getAppId()).getApplicationName());
            appList.add(app);
        }
        model.put("applications", appList);
        return "pushMessageCreate";
    }

    // Action Handlers

    @RequestMapping(value = "web/admin/app/create/do.submit", method = RequestMethod.POST)
    public String actionCreateApplication(@Valid AppCreateForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:web/admin/app/create";
        }
        AppCredentials appCredentials = new AppCredentials();
        appCredentials.setAppId(form.getAppId());
        AppCredentials newAppCredentials = credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + newAppCredentials.getId() + "/edit";
    }

    @RequestMapping(value = "web/admin/app/{id}/ios/upload/do.submit", method = RequestMethod.POST)
    public String actionUploadIosCredentials(@PathVariable Long id, @Valid UploadIosCredentialsForm form, BindingResult bindingResult, RedirectAttributes attr) {
        if (bindingResult.hasErrors()) {
            attr.addFlashAttribute("fields", bindingResult);
            attr.addFlashAttribute("form", form);
            return "redirect:/web/admin/app/" + id + "/ios/upload";
        }
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        try {
            appCredentials.setIosPrivateKey(form.getPrivateKey().getBytes());
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        appCredentials.setIosTeamId(form.getTeamId());
        appCredentials.setIosKeyId(form.getKeyId());
        appCredentials.setIosBundle(form.getBundle());
        credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + id + "/edit";
    }

    @RequestMapping(value = "web/admin/app/{id}/ios/remove/do.submit", method = RequestMethod.POST)
    public String actionRemoveIosCredentials(@Valid RemoveIosCredentialsForm form, @PathVariable Long id, BindingResult bindingResult) {
        if (bindingResult.hasErrors() || (id == null || !id.equals(form.getId()))) {
            return "error";
        }
        final AppCredentials appCredentials = credentialsRepository.findOne(form.getId());
        appCredentials.setIosPrivateKey(null);
        appCredentials.setIosTeamId(null);
        appCredentials.setIosKeyId(null);
        appCredentials.setIosBundle(null);
        AppCredentials newAppCredentials = credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + newAppCredentials.getId()  + "/edit";
    }

    @RequestMapping(value = "web/admin/app/{id}/android/upload/do.submit", method = RequestMethod.POST)
    public String actionUploadAndroidCredentials(@PathVariable Long id, @Valid UploadAndroidCredentialsForm form, BindingResult bindingResult, RedirectAttributes attr) {
        if (bindingResult.hasErrors()) {
            attr.addFlashAttribute("fields", bindingResult);
            attr.addFlashAttribute("form", form);
            return "redirect:/web/admin/app/" + id + "/android/upload";
        }
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        appCredentials.setAndroidServerKey(form.getToken());
        appCredentials.setAndroidBundle(form.getBundle());
        credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + id + "/edit";
    }

    @RequestMapping(value = "web/admin/app/{id}/android/remove/do.submit", method = RequestMethod.POST)
    public String actionRemoveAndroidCredentials(@PathVariable Long id) {
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        appCredentials.setAndroidServerKey(null);
        appCredentials.setAndroidBundle(null);
        credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + id + "/edit";
    }

    @RequestMapping(value = "web/admin/message/create/do.submit", method = RequestMethod.POST)
    public String actionCreatePushMessage(@Valid ComposePushMessageForm form, BindingResult bindingResult, RedirectAttributes attr, HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            attr.addFlashAttribute("fields", bindingResult);
            attr.addFlashAttribute("form", form);
            return "redirect:/web/admin/message/create";
        }
        SendPushMessageRequest request = new SendPushMessageRequest();
        request.setAppId(form.getAppId());
        PushMessage push = new PushMessage();
        push.setUserId(form.getUserId());
        PushMessageBody body = new PushMessageBody();
        body.setTitle(form.getTitle());
        body.setBody(form.getBody());
        body.setSound(form.isSound() ? "default" : null);
        push.setMessage(body);
        request.setPush(push);
        HttpEntity<ObjectRequest<SendPushMessageRequest>> requestEntity = new HttpEntity<>(new ObjectRequest<>(request));
        RestTemplate template = new RestTemplate();
        String baseUrl = String.format("%s://%s:%d/%s",httpRequest.getScheme(),  httpRequest.getServerName(), httpRequest.getServerPort(), httpRequest.getContextPath());
        template.exchange(baseUrl + "/push/message/send", HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ObjectResponse<PushSendResult>>() {});
        return "redirect:/web/admin/message/create";
    }

}
