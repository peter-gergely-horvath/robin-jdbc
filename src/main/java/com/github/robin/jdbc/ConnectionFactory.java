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

import com.github.robin.jdbc.config.Configuration;
import com.github.robin.jdbc.config.ConfigurationFactory;
import com.github.robin.jdbc.config.DefaultConfigurationFactory;
import com.github.robin.jdbc.config.MisconfigurationException;
import com.github.robin.jdbc.url.UrlPatternParser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

final class ConnectionFactory {

    private static final Logger LOGGER = Logger.getLogger(ConnectionFactory.class.getName());

    private static final ConnectionFactory INSTANCE = new ConnectionFactory(); // thread-safe: no state is held

    static ConnectionFactory getInstance() {
        return INSTANCE;
    }

    private final ConfigurationFactory configurationFactory;
    private final UrlPatternParser urlPatternParser;

    ConnectionFactory() {
        this(DefaultConfigurationFactory.getInstance(), UrlPatternParser.getInstance());
    }

    // Visible for testing
    ConnectionFactory(ConfigurationFactory configurationFactory, UrlPatternParser urlPatternParser) {
        this.configurationFactory = configurationFactory;
        this.urlPatternParser = urlPatternParser;
    }

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

    // Visible for testing
    private Connection newConnection(String connectionTypeName,
                                     String configurationSection,
                                     String delegateUrlPattern,
                                     Properties properties) throws SQLException, MisconfigurationException {


        if (connectionTypeName == null || connectionTypeName.trim().equals("")) {
            throw new MisconfigurationException("Invalid JDBC URL: connection type must be specified");
        }

        Configuration configuration = configurationFactory.newConfiguration(configurationSection, properties);


        List<String> urls = urlPatternParser.getURLs(delegateUrlPattern);

        String url;
        switch (connectionTypeName.toLowerCase(Locale.ENGLISH)) {
            case "loadbalancer":
                urls = new LinkedList<>(urls);
                Collections.shuffle(urls);

                break;

            case "failover":
                // no-op, we use the original URL order
                break;

            default:
                throw new MisconfigurationException("Invalid JDBC URL: connection type must be "
                        + "'loadbalancer' or 'failover', but was: " + connectionTypeName);

        }

        return connect(urls, properties, configuration);

    }

    private Connection connect(List<String> urls,
                               Properties properties,
                               Configuration configuration) throws SQLException {

        final int attemptCount;
        if (configuration.getAttemptCount() == Configuration.ATTEMPT_ALL) {
            attemptCount = urls.size();
        } else {
            attemptCount = Math.min(configuration.getAttemptCount(), urls.size());
        }

        List<SQLException> caughtExceptions = new LinkedList<>();

        for (int i = 0; i < attemptCount; ++i) {
            try {
                String url = urls.get(i);
                return DriverManager.getConnection(url, properties);

            } catch (SQLException sqlException) {
                caughtExceptions.add(sqlException);
            }
        }

        SQLException sqlException = new SQLException(String.format(
                "Could not connect to any of the URLs, giving up after %s attempted connections",
                caughtExceptions.size()));

        for (Exception caughtException : caughtExceptions) {
            sqlException.addSuppressed(caughtException);
        }

        throw sqlException;
    }

}
