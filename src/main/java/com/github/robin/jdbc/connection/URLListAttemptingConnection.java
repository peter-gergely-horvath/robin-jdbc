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

import com.github.robin.jdbc.config.Configuration;
import com.github.robin.jdbc.config.ConfigurationFactory;
import com.github.robin.jdbc.config.MisconfigurationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

abstract class URLListAttemptingConnection extends DelegatingConnection {

    private final Configuration configuration;
    private final Properties connectionProperties;

    private final ReentrantLock connectionLock = new ReentrantLock();
    private Connection delegateConnection = null;

    protected URLListAttemptingConnection(
            ConfigurationFactory configurationFactory, String config, Properties properties)
            throws MisconfigurationException {

        this.configuration = configurationFactory.newConfiguration(config, properties);
        this.connectionProperties = properties;
    }

    @Override
    protected final Connection getDelegate() throws SQLException {
        try {
            // TODO: make wait time configurable?
            final boolean couldAcquireLock = connectionLock.tryLock(90, TimeUnit.SECONDS);
            if (!couldAcquireLock) {
                throw new SQLException("Timeout waiting for connection");
            }

            try {
                if (delegateConnection == null) {
                    delegateConnection = connectToDelegate();
                }

                return delegateConnection;

            } finally {
                connectionLock.unlock();
            }


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("interrupted", e);
        }
    }

    private Connection connectToDelegate() throws SQLException {
        List<String> urlList = getURLs();

        final int attemptCount;
        if (configuration.getAttemptCount() == Configuration.ATTEMPT_ALL) {
            attemptCount = urlList.size();
        } else {
            attemptCount = Math.min(configuration.getAttemptCount(), urlList.size());
        }

        List<SQLException> caughtExceptions = new LinkedList<>();

        for (int i = 0; i < attemptCount; ++i) {
            try {
                String url = urlList.get(i);
                return DriverManager.getConnection(url, connectionProperties);

            } catch (SQLException sqlException) {
                caughtExceptions.add(sqlException);
            }
        }

        SQLException sqlException =
                new SQLException("Could not connect to any of the URLs or attempt count exceeded");

        for (Exception caughtException : caughtExceptions) {
            sqlException.addSuppressed(caughtException);
        }

        throw sqlException;
    }

    protected abstract List<String> getURLs();
}
