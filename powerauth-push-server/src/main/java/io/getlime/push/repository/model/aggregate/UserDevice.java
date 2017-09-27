package io.getlime.push.repository.model.aggregate;

/**
 * Object used in sending campaign
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class UserDevice {

    private String userId;
    private Long campaignId;
    private Long appId;
    private String platform;
    private String token;

    public UserDevice(String userId, Long campaignId, Long appId, String platform, String token) {
        this.userId = userId;
        this.campaignId = campaignId;
        this.appId = appId;
        this.platform = platform;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
