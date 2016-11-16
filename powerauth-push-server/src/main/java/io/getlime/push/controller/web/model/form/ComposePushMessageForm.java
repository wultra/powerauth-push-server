package io.getlime.push.controller.web.model.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Form sent when sending a test push message.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class ComposePushMessageForm {

    @NotNull
    private Long appId;

    @NotNull
    @Size(min = 1)
    private String userId;

    @NotNull
    @Size(min = 1)
    private String title;

    @NotNull
    @Size(min = 1)
    private String body;

    @NotNull
    private boolean sound;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isSound() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    @Override public String toString() {
        return "ComposePushMessageForm{" +
                "appId=" + appId +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", sound=" + sound +
                '}';
    }
}
