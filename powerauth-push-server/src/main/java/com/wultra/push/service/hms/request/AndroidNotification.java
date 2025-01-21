/*
 * Copyright 2024 Wultra s.r.o.
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
package com.wultra.push.service.hms.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HMS (Huawei Mobile Services) json mapping object.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Getter
@SuperBuilder
@Jacksonized
public class AndroidNotification {

    private final String title;

    private final String body;

    private final String icon;

    private final String color;

    private final String sound;

    @JsonProperty("default_sound")
    private final boolean defaultSound;

    private final String tag;

    @JsonProperty("click_action")
    private final ClickAction clickAction;

    @JsonProperty("body_loc_key")
    private final String bodyLocKey;

    @Builder.Default
    @JsonProperty("body_loc_args")
    private final List<String> bodyLocArgs = new ArrayList<>();

    @JsonProperty("title_loc_key")
    private final String titleLocKey;

    @Builder.Default
    @JsonProperty("title_loc_args")
    private final List<String> titleLocArgs = new ArrayList<>();

    @JsonProperty("multi_lang_key")
    private final Map<String, Object> multiLangKey;

    @JsonProperty("channel_id")
    private final String channelId;

    @JsonProperty("notify_summary")
    private final String notifySummary;

    private final String image;

    private final Integer style;

    @JsonProperty("big_title")
    private final String bigTitle;

    @JsonProperty("big_body")
    private final String bigBody;

    @JsonProperty("auto_clear")
    private final Integer autoClear;

    @JsonProperty("notify_id")
    private final Integer notifyId;

    private final String group;

    private final BadgeNotification badge;

    private final String ticker;

    @JsonProperty("auto_cancel")
    private final boolean autoCancel;

    private final String when;

    @JsonProperty("local_only")
    private final Boolean localOnly;

    private final Importance importance;

    @JsonProperty("use_default_vibrate")
    private final boolean useDefaultVibrate;

    @JsonProperty("use_default_light")
    private final boolean useDefaultLight;

    @Builder.Default
    @JsonProperty("vibrate_config")
    private final List<String> vibrateConfig = new ArrayList<>();

    private final String visibility;

    @JsonProperty("light_settings")
    private final LightSettings lightSettings;

    @JsonProperty("foreground_show")
    private final boolean foregroundShow;

    @JsonProperty("inbox_content")
    private final List<String> inboxContent;

    private final List<Button> buttons;

    @JsonProperty("profile_id")
    private final String profileId;

    public enum Importance {
        LOW,
        NORMAL,
        HIGH
    }

}
