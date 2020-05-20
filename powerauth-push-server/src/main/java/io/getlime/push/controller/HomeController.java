/*
 * Copyright 2020 Wultra s.r.o.
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
package io.getlime.push.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Date;

/**
 * Home page controller of the PowerAuth Push Server.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Controller
public class HomeController {

    private BuildProperties buildProperties;

    @Autowired(required = false)
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Add build information
        if (buildProperties != null) {
            model.addAttribute("version", buildProperties.getVersion());
            model.addAttribute("buildTime", Date.from(buildProperties.getTime()));
        }

        return "home";
    }

}
