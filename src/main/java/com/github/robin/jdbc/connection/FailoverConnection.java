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

package com.github.robin.jdbc.connection;

import com.github.robin.jdbc.config.ConfigurationFactory;
import com.github.robin.jdbc.config.DefaultConfigurationFactory;
import com.github.robin.jdbc.config.MisconfigurationException;

import java.util.List;
import java.util.Properties;

public final class FailoverConnection extends URLListAttemptingConnection {

    private final List<String> urlList;

    public FailoverConnection(String config, List<String> urlList, Properties properties)
            throws MisconfigurationException {
        this(DefaultConfigurationFactory.getInstance(), config, urlList, properties);
    }

    // Visible for testing only
    FailoverConnection(ConfigurationFactory configurationFactory,
                       String config, List<String> urlList, Properties properties)
            throws MisconfigurationException {

        super(configurationFactory, config, properties);
        this.urlList = urlList;
    }

    @Override
    protected List<String> getURLs() {
        // return URL pattern according to the original order
        return urlList;
    }
}
