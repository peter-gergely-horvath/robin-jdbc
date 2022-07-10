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

import java.sql.DriverPropertyInfo;

public enum ConfigurationEntry {

    ATTEMPT_COUNT("attemptCount", Integer.toString(Configuration.ATTEMPT_ALL),
            "Number of servers to try connecting before giving up. To attempt all servers: "
                    + Configuration.ATTEMPT_ALL + ".") {
        @Override
        void setConfiguration(Configuration config, String value) throws MisconfigurationException {
            if (value != null && value.trim().length() != 0) {

                try {
                    int retryCount = Integer.parseInt(value);
                    if (retryCount < 1 && retryCount != Configuration.ATTEMPT_ALL) {
                        throw InvalidConfigurationValueException
                                .forMessage("Value for %s must be a positive integer, or %s but was '%s'",
                                        this.key, Configuration.ATTEMPT_ALL, value);
                    }

                    config.setAttemptCount(retryCount);
                } catch (NumberFormatException nfe) {
                    throw InvalidConfigurationValueException
                            .forMessage("Value for %s must be a valid integer, but was '%s'", this.key, value);
                }

            }
        }
    };

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getKey() {
        return key;
    }

    //CHECKSTYLE.OFF: VisibilityModifier
    protected final String key;
    //CHECKSTYLE.ON: VisibilityModifier
    private final String defaultValue;
    private final String description;

    ConfigurationEntry(String key, String defaultValue, String description) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.description = String.format("%s Default is: '%s'", description, defaultValue);
    }

    abstract void setConfiguration(Configuration config, String value) throws MisconfigurationException;

    public DriverPropertyInfo getDriverPropertyInfo() {
        DriverPropertyInfo driverPropertyInfo = new DriverPropertyInfo(this.key, defaultValue);
        driverPropertyInfo.description = this.description;
        return driverPropertyInfo;
    }
}
