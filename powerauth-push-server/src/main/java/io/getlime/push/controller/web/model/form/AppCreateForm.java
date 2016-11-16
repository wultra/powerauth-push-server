package io.getlime.push.controller.web.model.form;

import javax.validation.constraints.NotNull;

/**
 * Form sent when creating the application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class AppCreateForm {

    @NotNull
    private Long appId;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    @Override public String toString() {
        return "AppCreateForm{" +
                "appId=" + appId +
                '}';
    }
}
