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

import com.github.robin.jdbc.config.*;
import com.github.robin.jdbc.url.DefaultUrlTemplateParser;
import com.github.robin.jdbc.url.UrlTemplateParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;


final class ConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionFactory.class);

    static final String LOAD_BALANCE = "loadbalance";
    static final String FAILOVER = "failover";
    static final String TEMPLATE_PREFIX = "template:";

    private static final ConnectionFactory INSTANCE = new ConnectionFactory(); // thread-safe: no state is held


    static ConnectionFactory getInstance() {
        return INSTANCE;
    }

    private final ConfigurationFactory configurationFactory;
    private final UrlTemplateParser urlTemplateParser;

    ConnectionFactory() {
        this(DefaultConfigurationFactory.getInstance(), DefaultUrlTemplateParser.getInstance());
    }

    // Visible for testing
    ConnectionFactory(ConfigurationFactory configurationFactory, UrlTemplateParser urlTemplateParser) {
        this.configurationFactory = configurationFactory;
        this.urlTemplateParser = urlTemplateParser;
    }

    Connection newConnection(String factoryConfiguration, Properties properties)
            throws SQLException, ConnectionURLSyntaxException {

        Objects.requireNonNull(factoryConfiguration);


        try {
            LOGGER.trace("factoryConfiguration={}", factoryConfiguration);
            LOGGER.trace("properties={}", properties);

            String[] connectionTypeAndTheRest = factoryConfiguration.split(":", 2);

            if (connectionTypeAndTheRest.length != 2) {
                throw ConnectionURLSyntaxException.forMessage(
                        "Connection type is missing from: %s", factoryConfiguration);
            }


            String connectionType = connectionTypeAndTheRest[0].toLowerCase(Locale.ENGLISH);

            String rest = connectionTypeAndTheRest[1];

            int beginOfURLTemplate = rest.indexOf(TEMPLATE_PREFIX);
            if (beginOfURLTemplate < 0) {
                throw ConnectionURLSyntaxException.forMessage(
                        "'%s' expression is missing from the URL", TEMPLATE_PREFIX);
            }

            String configurationSection;
            if (beginOfURLTemplate > 0) {
                configurationSection = rest.substring(0, beginOfURLTemplate);
            } else {
                configurationSection = "";
            }

            String urlTemplate = rest.substring(beginOfURLTemplate + TEMPLATE_PREFIX.length());
            LOGGER.debug("URL Template extracted from connection string: {}", urlTemplate);

            return newConnection(connectionType, configurationSection, urlTemplate, properties);

        } catch (MisconfigurationException mce) {
            throw new SQLException("Configuration error: " + mce.getMessage(), mce);
        } catch (URLTemplateException ute) {
            throw new SQLException("URL template error: " + ute.getMessage(), ute);
        }
    }

    // Visible for testing
    private Connection newConnection(String connectionTypeName,
                                     String configurationSection,
                                     String delegateUrlPattern,
                                     Properties properties)
            throws SQLException, MisconfigurationException, URLTemplateException, ConnectionURLSyntaxException {


        if (connectionTypeName == null || connectionTypeName.trim().equals("")) {
            throw new ConnectionURLSyntaxException("connection type must be specified");
        }

        Configuration configuration = configurationFactory.newConfiguration(configurationSection, properties);


        List<String> urls = urlTemplateParser.getURLs(delegateUrlPattern, properties);

        switch (connectionTypeName.toLowerCase(Locale.ENGLISH)) {
            case LOAD_BALANCE:
                urls = new LinkedList<>(urls);
                Collections.shuffle(urls);
                LOGGER.debug("Connection type is '{}', shuffled URL list: {}", connectionTypeName, urls);

                break;

            case FAILOVER:
                LOGGER.debug("Connection type is '{}', using user-defined URL order: {}", connectionTypeName, urls);
                // no-op, we use the original URL order
                break;

            default:
                throw ConnectionURLSyntaxException.forMessage("connection type must be "
                        + "'%s' or '%s', but was: '%s'", LOAD_BALANCE, FAILOVER, connectionTypeName);
        }

        return connect(urls, properties, configuration);

    }

    private Connection connect(List<String> allUrls,
                               Properties properties,
                               Configuration configuration) throws SQLException {

        final int attemptCount = getAttemptCount(configuration, allUrls.size());
        LOGGER.debug("Will attempt {} URLs out of {}", attemptCount, allUrls.size());

        final List<String> urlsToTry = allUrls.subList(0, attemptCount);

        List<SQLException> caughtExceptions = new LinkedList<>();
        for (String url : urlsToTry) {
            try {
                LOGGER.info("Connecting to URL: {}", url);
                return DriverManager.getConnection(url, properties);

            } catch (SQLException sqlException) {
                LOGGER.warn("Exception connecting to URL: " + url, sqlException);
                caughtExceptions.add(sqlException);
            }
        }

        String errorMessage;
        if (urlsToTry.size() == allUrls.size()) {
            errorMessage = String.format("Could not connect to any of the URLs: %s", urlsToTry);
        } else {
            errorMessage = String.format(
                    "Maximum attempt count, %s reached trying to connect to URLs: %s", urlsToTry.size(), allUrls);
        }
        LOGGER.error(errorMessage);

        SQLException sqlException = new SQLException(errorMessage);

        for (Exception caughtException : caughtExceptions) {
            sqlException.addSuppressed(caughtException);
        }

        throw sqlException;
    }

    private int getAttemptCount(Configuration configuration, int urlCount) {
        final int attemptCount;

        int configuredAttemptCount = configuration.getAttemptCount();
        if (configuredAttemptCount == Configuration.ATTEMPT_ALL) {
            attemptCount = urlCount;
            LOGGER.trace("AttemptCount is {}, probing all {} URLs",
                    Configuration.ATTEMPT_ALL, attemptCount);
        } else {
            attemptCount = Math.min(configuredAttemptCount, urlCount);
            LOGGER.trace("Configured AttemptCount: {}, actual attemptCount: {}",
                    configuredAttemptCount, attemptCount);
        }

        if (attemptCount == 1) {
            LOGGER.warn("Only one URL will be attempted: no no fail-over or load balancing is provided");
        }

        return attemptCount;
    }

}
