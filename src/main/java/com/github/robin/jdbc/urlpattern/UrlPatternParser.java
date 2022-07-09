/*
 * Copyright (c) 2022 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robin.jdbc.urlpattern;

import java.util.Collections;
import java.util.List;

public final class UrlPatternParser {

    private static final UrlPatternParser INSTANCE = new UrlPatternParser();

    public static UrlPatternParser getInstance() {
        return INSTANCE;
    }

    public List<String> getURLs(String urlPattern) {
        // TODO: implement actual pattern parsing here
        return Collections.singletonList(urlPattern);
    }

}
