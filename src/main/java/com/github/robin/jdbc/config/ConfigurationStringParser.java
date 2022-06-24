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


package com.github.robin.jdbc.config;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

final class ConfigurationStringParser {

    private ConfigurationStringParser() {
        throw new AssertionError("static utility class");
    }

    private static final String CONFIGURATION_STRING_ENTRY_DELIMITER = ";";

    private static final Set<String> VALID_CONFIGURATION_KEYS = Arrays.stream(ConfigurationEntry.values())
            .map(ConfigurationEntry::getKey)
            .collect(Collectors.toSet());

    static Properties parseStringToProperties(String configurationString) throws MisconfigurationException {
        Properties properties = new Properties();

        if (configurationString != null) {
            StringTokenizer st = new StringTokenizer(configurationString, CONFIGURATION_STRING_ENTRY_DELIMITER);
            while (st.hasMoreTokens()) {
                String keyValuePair = st.nextToken();
                String[] keyAndValue = keyValuePair.split("=", 2);

                if (keyAndValue.length == 1) {
                    throw MisconfigurationException.forMessage(
                            "Configuration string '%s' cannot be interpreted as '=' "
                                    + "character separated key-value pairs. Could not parse: " + keyValuePair,
                            configurationString);
                }


                String key = keyAndValue[0];
                String value = keyAndValue[1];

                if (!VALID_CONFIGURATION_KEYS.contains(key)) {
                    throw MisconfigurationException.forMessage("Unknown configuration key: '%s'.", key);
                }

                if (properties.containsKey(key)) {
                    /* configuration string contains duplicated entries;
                     * e.g. "foo=x;foo=y"
                     * Fail-fast in such cases by throwing exception
                     */
                    throw MisconfigurationException.forMessage(
                            "Configuration string contains duplicated key '%s'.",
                            key);
                }

                properties.setProperty(key, value);
            }
        }
        return properties;
    }
}
