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

package io.getlime.push.model.entity;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Class representing a message body - the information that do not serve as a "message descriptor"
 * but rather as payload. This data package is a subject of end-to-end encryption.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class PushMessageBody {

    private String title;
    private String titleLocKey;
    private String[] titleLocArgs;
    private String body;
    private String bodyLocKey;
    private String[] bodyLocArgs;
    private String icon;
    private Integer badge;
    private String sound;
    private String category;
    private String collapseKey;
    private Instant validUntil;
    private Map<String, Object> extras;

    /**
     * Push message title. Short and digestible message stating the message purpose, for example "Balance update".
     * @return Push message title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set push message title. Short and digestible message stating the message purpose, for
     * example "Balance update".
     * @param title Push message title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the title localized key.
     * @return Title localized key.
     */
    public String getTitleLocKey() {
        return titleLocKey;
    }

    /**
     * Set the title localized key.
     * @param titleLocKey Title localized key.
     */
    public void setTitleLocKey(String titleLocKey) {
        this.titleLocKey = titleLocKey;
    }

    /**
     * Get the title localized args.
     * @return Title localized args.
     */
    public String[] getTitleLocArgs() {
        return titleLocArgs;
    }

    /**
     * Set the title localized args.
     * @param titleLocArgs Title localized args.
     */
    public void setTitleLocArgs(String[] titleLocArgs) {
        this.titleLocArgs = titleLocArgs;
    }

    /**
     * Get long message text, used as a notification body. Place your message to this property,
     * for example "Your today's balance is $782.40".
     * @return Notification body text.
     */
    public String getBody() {
        return body;
    }

    /**
     * Set long message text, used as a notification body. Place your message to this property,
     * for example "Your today's balance is $782.40".
     * @param body Notification body text.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Get the body localized key.
     * @return Body localized key.
     */
    public String getBodyLocKey() {
        return bodyLocKey;
    }

    /**
     * Set the body localized key.
     * @param bodyLocKey Body localized key.
     */
    public void setBodyLocKey(String bodyLocKey) {
        this.bodyLocKey = bodyLocKey;
    }

    /**
     * Get the body localized args.
     * @return Body localized args.
     */
    public String[] getBodyLocArgs() {
        return bodyLocArgs;
    }

    /**
     * Set the body localized args.
     * @param bodyLocArgs Body localized args.
     */
    public void setBodyLocArgs(String[] bodyLocArgs) {
        this.bodyLocArgs = bodyLocArgs;
    }

    /**
     * App icon badge value (iOS only).
     * @return App icon badge.
     */
    public Integer getBadge() {
        return badge;
    }

    /**
     * Set app icon badge value (iOS only).
     * @param badge App icon badge.
     */
    public void setBadge(Integer badge) {
        this.badge = badge;
    }

    /**
     * Get the sound name to be played with the notification.
     * @return Sound name.
     */
    public String getSound() {
        return sound;
    }

    /**
     * Set the sound name to be played with the notification.
     * @param sound Sound name.
     */
    public void setSound(String sound) {
        this.sound = sound;
    }

    /**
     * Get the notification category, used to distinguish actions assigned with the notification.
     * @return Notification category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set the notification category, used to distinguish actions assigned with the notification.
     * @param category Notification category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Get the collapse key, used to collapse messages on the server in case messages cannot be delivered and
     * as a tag / thread ID to group messages with the same content type.
     * @return Notification collapse key.
     */
    public String getCollapseKey() {
        return collapseKey;
    }

    /**
     * Set the collapse key, used to collapse messages on the server in case messages cannot be delivered and
     * as a tag / thread ID to group messages with the same content type.
     * @param collapseKey Notification collapse key.
     */
    public void setCollapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
    }

    /**
     * Get notification delivery validity (timestamp message should live to in case it's not delivered immediately).
     * @return Validity timestamp.
     */
    public Instant getValidUntil() {
        return validUntil;
    }

    /**
     * Set notification delivery validity (timestamp message should live to in case it's not delivered immediately).
     * @param validUntil Validity timestamp.
     */
    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * Get the map (Map&lt;String, Object&gt;) with optional message parameters. This is translated to custom parameters
     * on iOS and to data notification payload object on Android.
     * @return Extra attributes.
     */
    public Map<String, Object> getExtras() {
        return extras;
    }

    /**
     * Set the map (Map&lt;String, Object&gt;) with optional message parameters. This is translated to custom parameters
     * on iOS and to data notification payload object on Android.
     * @param extras Extra attributes.
     */
    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }

    /**
     * Get icon.
     * @return Icon.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Set icon.
     * @param icon Icon.
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PushMessageBody that = (PushMessageBody) o;

        if (!Objects.equals(title, that.title)) {
            return false;
        }
        return Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}
