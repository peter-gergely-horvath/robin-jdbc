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

import com.github.robin.jdbc.config.ConfigurationEntry;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Driver implements java.sql.Driver {

    private static final Logger LOGGER = Logger.getLogger(Driver.class.getName());
    private static final Logger PARENT_LOGGER = Logger.getLogger(Driver.class.getPackage().getName());

    static {
        try {
            DriverManager.registerDriver(new Driver());
            LOGGER.log(Level.FINEST, "Robin JDBC Driver loaded and registered to DriverManager");
        } catch (SQLException ex) {
            String errorMessage = "Could not register to DriverManager: " + Driver.class.getName();
            LOGGER.log(Level.SEVERE, errorMessage, ex);
            throw new RuntimeException(errorMessage, ex);
        }
    }

    private static final String JDBC_URL_PREFIX = "jdbc:robin:";

    private final ConnectionFactory connectionFactory;

    public Driver() {
        this(ConnectionFactory.getInstance());
    }

    Driver(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Connection connect(String url, Properties info) throws SQLException {

        if (!acceptsURL(url)) {
            /*
             * From the JavaDoc of java.sql.Driver.connect(String, Properties):
             *
             * The driver should return "null" if it realizes it is the wrong
             * kind of driver to connect to the given URL.
             */
            return null;
        }

        String factoryConfiguration = url.substring(JDBC_URL_PREFIX.length());

        return connectionFactory.newConnection(factoryConfiguration, info);
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.startsWith(JDBC_URL_PREFIX);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        ConfigurationEntry[] input = ConfigurationEntry.values();
        DriverPropertyInfo[] result = new DriverPropertyInfo[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = input[i].getDriverPropertyInfo();
        }
        return result;
    }

    public int getMajorVersion() {
        return DriverInfo.DRIVER_VERSION_MAJOR;
    }

    public int getMinorVersion() {
        return DriverInfo.DRIVER_VERSION_MINOR;
    }

    public boolean jdbcCompliant() {
        return false;
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return PARENT_LOGGER;
    }
}
