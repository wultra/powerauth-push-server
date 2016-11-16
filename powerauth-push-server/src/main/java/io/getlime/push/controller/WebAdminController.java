package io.getlime.push.controller;

import io.getlime.powerauth.soap.GetApplicationListResponse;
import io.getlime.push.controller.model.PushServerApplication;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.model.AppCredentials;
import io.getlime.security.soap.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
            app.setIos(appCredential.getIos() != null);
            app.setAndroid(appCredential.getAndroid() != null);
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
        app.setIos(appCredentials.getIos() != null);
        app.setAndroid(appCredentials.getAndroid() != null);
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
        app.setIos(appCredentials.getIos() != null);
        app.setAndroid(appCredentials.getAndroid() != null);
        app.setAppName(client.getApplicationDetail(appCredentials.getAppId()).getApplicationName());
        model.put("bundle", appCredentials.getIosBundle());
        model.put("application", app);
        return "applicationIosUpload";
    }

    @RequestMapping(value = "web/admin/app/{id}/android/upload")
    public String uploadAndroidCredentials(@PathVariable Long id, Map<String, Object> model) {
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        PushServerApplication app = new PushServerApplication();
        app.setId(appCredentials.getId());
        app.setAppId(appCredentials.getAppId());
        app.setIos(appCredentials.getIos() != null);
        app.setAndroid(appCredentials.getAndroid() != null);
        app.setAppName(client.getApplicationDetail(appCredentials.getAppId()).getApplicationName());
        model.put("bundle", appCredentials.getAndroidBundle());
        model.put("application", app);
        return "applicationAndroidUpload";
    }

    // Action Handlers

    @RequestMapping(value = "web/admin/app/create/do.submit", method = RequestMethod.POST)
    public String actionCreateApplication(@RequestParam(value = "id") Long id) {
        AppCredentials appCredentials = new AppCredentials();
        appCredentials.setAppId(id);
        AppCredentials newAppCredentials = credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + newAppCredentials.getId() + "/edit";
    }

    @RequestMapping(value = "web/admin/app/edit/do.submit", method = RequestMethod.POST)
    public String actionEditApplication() {
        return "redirect:/web/admin/app/list";
    }

    @RequestMapping(value = "web/admin/app/delete/do.submit", method = RequestMethod.POST)
    public String actionDeleteApplication(@RequestParam(value = "id") String id, Map<String, Object> model) {
        return "redirect:/web/admin/app/list";
    }

    @RequestMapping(value = "web/admin/app/{id}/ios/upload/do.submit", method = RequestMethod.POST)
    public String actionUploadIosCredentials(
            @PathVariable Long id,
            @RequestParam("bundle") String bundle,
            @RequestParam("certificate") MultipartFile file,
            @RequestParam("password") String password) {
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        try {
            appCredentials.setIos(file.getBytes());
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        appCredentials.setIosPassword(password);
        appCredentials.setIosBundle(bundle);
        credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + id + "/edit";
    }

    @RequestMapping(value = "web/admin/app/{id}/ios/remove/do.submit", method = RequestMethod.POST)
    public String actionRemoveIosCredentials(@PathVariable Long id) {
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        appCredentials.setIos(null);
        appCredentials.setIosPassword(null);
        appCredentials.setIosBundle(null);
        credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + id + "/edit";
    }

    @RequestMapping(value = "web/admin/app/{id}/android/upload/do.submit", method = RequestMethod.POST)
    public String actionUploadAndroidCredentials(
            @PathVariable Long id,
            @RequestParam("bundle") String bundle,
            @RequestParam("token") String token) {
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        appCredentials.setAndroid(token);
        appCredentials.setAndroidBundle(bundle);
        credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + id + "/edit";
    }

    @RequestMapping(value = "web/admin/app/{id}/android/remove/do.submit", method = RequestMethod.POST)
    public String actionRemoveAndroidCredentials(@PathVariable Long id) {
        final AppCredentials appCredentials = credentialsRepository.findOne(id);
        appCredentials.setAndroid(null);
        appCredentials.setAndroidBundle(null);
        credentialsRepository.save(appCredentials);
        return "redirect:/web/admin/app/" + id + "/edit";
    }

}
