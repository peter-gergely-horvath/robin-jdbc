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


import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.*;


public class DriverTest {

    @Test
    public void testLoadBalancerWithSingleURL() throws SQLException {
        try (Connection connection =
                     DriverManager.getConnection("jdbc:robin:loadbalancer:attemptCount=3;jdbc:h2:mem:foobar")) {

            String queryResult;
            try (PreparedStatement ps = connection.prepareStatement(
                    "select * from INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    queryResult = rs.getString(1);
                }
            }

            Assert.assertEquals("FOOBAR", queryResult);

        }
    }

    @Test
    public void testLoadbalancerWithSingleURLThatIsInvalid() throws SQLException {
        try {
            DriverManager.getConnection("jdbc:robin:loadbalancer:attemptCount=3;jdbc:foo:bar");

            Assert.fail("Should have thrown an exception");
        } catch (SQLException sqlException) {
            Assert.assertTrue(sqlException.getMessage().contains("Could not connect to any of the URLs"));
        }

    }

    @Test
    public void testFailoverWithSingleURL() throws SQLException {
        try (Connection connection =
                     DriverManager.getConnection("jdbc:robin:failover:attemptCount=3;jdbc:h2:mem:foobar")) {

            String queryResult;
            try (PreparedStatement ps = connection.prepareStatement(
                    "select * from INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    queryResult = rs.getString(1);
                }
            }

            Assert.assertEquals("FOOBAR", queryResult);

        }
    }

    @Test
    public void testFailoverWithSingleURLThatIsInvalid() throws SQLException {
        try {

            DriverManager.getConnection("jdbc:robin:failover:attemptCount=3;jdbc:foo:bar");

            Assert.fail("Should have thrown an exception");
        } catch (SQLException sqlException) {
            Assert.assertTrue(sqlException.getMessage().contains("Could not connect to any of the URLs"));
        }

    }

    @Test
    public void testInvalidWithSingleURL() throws SQLException {
        try {
            DriverManager.getConnection("jdbc:robin:invalid:attemptCount=1;jdbc:h2:mem:foobar");

            Assert.fail("Should have thrown an exception");
        } catch (SQLException sqlException) {
            Assert.assertTrue(sqlException.getMessage().contains(
                    "Invalid JDBC URL: connection type must be 'loadbalancer' or 'failover'"));
        }
    }
}
