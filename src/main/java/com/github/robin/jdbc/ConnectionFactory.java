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
import com.github.robin.jdbc.connection.FailoverConnection;
import com.github.robin.jdbc.connection.LoadBalancerConnection;
import com.github.robin.jdbc.urlpattern.UrlPatternParser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

class ConnectionFactory {

    private static final ConnectionFactory INSTANCE = new ConnectionFactory(); // thread-safe: no state is held

    ConnectionFactory() {
        this(UrlPatternParser.getInstance());
    }

    // Visible for testing
    ConnectionFactory(UrlPatternParser urlPatternParser) {
        this.urlPatternParser = urlPatternParser;
    }

    static ConnectionFactory getInstance() {
        return INSTANCE;
    }

    private final UrlPatternParser urlPatternParser;

    Connection newConnection(String factoryConfiguration, Properties properties) throws SQLException {

        try {
            String[] connectionTypeAndTheRest = factoryConfiguration.split(":", 2);

            if (connectionTypeAndTheRest.length != 2) {
                throw new MisconfigurationException("Invalid JDBC URL: expected "
                        + "<connection type>:[configuration];<delegate URL pattern>, but was: "
                        + factoryConfiguration);
            }


            String connectionType = connectionTypeAndTheRest[0].toLowerCase(Locale.ENGLISH);

            String rest = connectionTypeAndTheRest[1];

            int beginOfURLPattern = rest.indexOf("jdbc:");
            if (beginOfURLPattern < 0) {
                throw new SQLException("The delegate URL is not found");
            }

            String configurationSection;
            if (beginOfURLPattern > 0) {
                configurationSection = rest.substring(0, beginOfURLPattern);
            } else {
                configurationSection = "";
            }

            String urlPattern  = rest.substring(beginOfURLPattern);

            return newConnection(connectionType, configurationSection, urlPattern, properties);

        } catch (MisconfigurationException mce) {
            throw new SQLException("Configuration error: " + mce.getMessage(), mce);
        }
    }

    private Connection newConnection(String connectionTypeName,
                             String configurationSection,
                             String delegateUrlPattern,
                             Properties properties) throws SQLException, MisconfigurationException {

        ConnectionType connectionType = ConnectionType.getByName(connectionTypeName);

        List<String> urlList = urlPatternParser.getURLs(delegateUrlPattern);

        return connectionType.newConnection(configurationSection, urlList, properties);
    }

    private enum ConnectionType {
        LOADBALANCER("loadbalancer") {
            @Override
            protected Connection newConnection(String config, List<String> urlList, Properties properties)
                    throws MisconfigurationException, SQLException {
                return new LoadBalancerConnection(config, urlList, properties);
            }
        },
        FAILOVER("failover") {
            @Override
            protected Connection newConnection(String config, List<String> urlList, Properties properties)
                    throws MisconfigurationException, SQLException {
                return new FailoverConnection(config, urlList, properties);
            }
        };

        private final String publicName;

        ConnectionType(String publicName) {
            this.publicName = publicName;
        }

        private static ConnectionType getByName(String requestedPublicName) throws MisconfigurationException {
            for (ConnectionType ct : ConnectionType.values()) {
                if (ct.publicName.equals(requestedPublicName)) {
                    return ct;
                }
            }

            throw MisconfigurationException.forMessage("No such connection type: '%s'", requestedPublicName);
        }


        protected abstract Connection newConnection(String config, List<String> urlList, Properties properties)
                throws MisconfigurationException, SQLException;
    }

}
