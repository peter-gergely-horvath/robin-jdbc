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

 
package com.github.robin.jdbc;

import com.github.robin.jdbc.config.MisconfigurationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

class ConnectionFactory {

    private static final ConnectionFactory INSTANCE = new ConnectionFactory(); // thread-safe: no state is held

    static ConnectionFactory getInstance() {
        return INSTANCE;
    }

    Connection newConnection(String factoryConfiguration, Properties properties) throws SQLException {

        try {
            int beginOfDelegateUrl = factoryConfiguration.indexOf("jdbc:");
            if (beginOfDelegateUrl < 0) {
                throw new SQLException("The delegate URL is not found");
            }

            String configurationSection = factoryConfiguration.substring(0, beginOfDelegateUrl);
            String delegateUrlExpression = factoryConfiguration.substring(beginOfDelegateUrl);

            return newConnection(configurationSection, delegateUrlExpression, properties);
        } catch (MisconfigurationException mce) {
            throw new SQLException("Configuration error: " + mce.getMessage(), mce);
        }
    }

    private Connection newConnection(String factoryConfiguration, String delegateUrlExpression, Properties properties)
            throws SQLException, MisconfigurationException {

        throw new SQLException(String.format("Not implemented yet: %s, %s, %s",
                factoryConfiguration, delegateUrlExpression, properties));
    }

}
