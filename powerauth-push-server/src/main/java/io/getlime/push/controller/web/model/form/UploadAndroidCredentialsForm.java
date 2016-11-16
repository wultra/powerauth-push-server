package io.getlime.push.controller.web.model.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Form sent when uploading iOS / APNs credentials for the application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class UploadAndroidCredentialsForm {

    @NotNull
    @Size(min = 2)
    @Pattern(flags = { Pattern.Flag.CASE_INSENSITIVE }, regexp = "^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$")
    private String bundle;

    @NotNull
    @Size(min = 1)
    private String token;

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
