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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class DriverTest {

    @Test
    public void testInvalidConnectionType() {
        try {
            DriverManager.getConnection("jdbc:robin:foobar:template:" +
                            "#foreach( $index in [1..2] )jdbc:h2:mem:foobar0$index\n#end");

            Assert.fail("Should have thrown an exception");
        } catch (SQLException sqlException) {
            Assert.assertTrue(sqlException.getMessage().contains(
                    "Invalid URL syntax: connection type must be 'loadbalance' or 'failover', but was: 'foobar'"));
        }
    }

    @Test
    public void testNoTemplateExpression() {
        try {
            DriverManager.getConnection("jdbc:robin:foobar:template:jdbc:h2:mem:foobar1");

            Assert.fail("Should have thrown an exception");
        } catch (SQLException sqlException) {
            Assert.assertTrue(sqlException.getMessage().contains(
                    "Template evaluation yielded one line"));
        }
    }

    @Test
    public void testNoTemplateResult() {
        try {
            DriverManager.getConnection("jdbc:robin:foobar:template:" +
                    "#foreach( $index in [1..1] )jdbc:h2:mem:foobar0$index#end");

            Assert.fail("Should have thrown an exception");
        } catch (SQLException sqlException) {
            Assert.assertTrue(sqlException.getMessage().contains(
                    "Template evaluation yielded one line"));
        }
    }

    @Test
    public void testSingleTemplateResult() {
        try {
            DriverManager.getConnection("jdbc:robin:foobar:template:" +
                    "#foreach( $index in [1..1] )jdbc:h2:mem:foobar0$index\n#end");

            Assert.fail("Should have thrown an exception");
        } catch (SQLException sqlException) {
            Assert.assertTrue(sqlException.getMessage().contains(
                    "Template evaluation yielded one line"));
        }
    }

    @Test
    public void testInvalidTemplateResult() {
        try {
            DriverManager.getConnection("jdbc:robin:foobar:template:" +
                    "#foreach( $index in [1..1] )jdbc:h2:mem:foobar0$index\n");

            Assert.fail("Should have thrown an exception");
        } catch (SQLException sqlException) {
            Assert.assertTrue(sqlException.getMessage().contains(
                    "Template evaluation failed: ensure the template adheres to Velocity template syntax"));
        }
    }


    @Test
    public void testSimpleFailover() throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                             "jdbc:robin:failover:template:" +
                                     "#foreach( $index in [1..2] )jdbc:h2:mem:foobar0$index\n#end")) {

            assertConnectedTo(connection, "FOOBAR01");

        }
    }

    @Test
    public void testSimpleLoadBalance() throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:robin:loadbalance:template:" +
                        "#foreach( $index in [1..2] )jdbc:h2:mem:foobar0$index\n#end")) {

            assertConnectedToAny(connection, "FOOBAR01", "FOOBAR02");

        }
    }

    @Test
    public void testMultiLoadBalance() throws SQLException {

        Set<String> expectedDatabaseNames = IntStream.range(1, 9)
                .mapToObj(it -> String.format("FOOBAR%02d", it))
                .collect(Collectors.toSet());

        String connectUrl = "jdbc:robin:loadbalance:template:" +
                "#foreach( $index in [1..8] )jdbc:h2:mem:foobar0$index\n#end";

        HashSet<String> databaseNames = new HashSet<>();

        final int testCount = 1000;
        for (int i=0; i<testCount; i++) {
            try (Connection connection = DriverManager.getConnection(connectUrl)) {

                databaseNames.add(getDatabaseNameFrom(connection));
            }
        }

        Assert.assertEquals(databaseNames, expectedDatabaseNames);
    }



    private static void assertConnectedTo(Connection connection, String expectedDatabaseName)
            throws SQLException {
        String actualDatabaseName = getDatabaseNameFrom(connection);

        Assert.assertEquals(actualDatabaseName, expectedDatabaseName);
    }

    private static void assertConnectedToAny(Connection connection, String... expectedDatabaseNames)
            throws SQLException {

        String actualDatabaseName = getDatabaseNameFrom(connection);

        Set<String> expectedDatabaseNamesSet = new HashSet<>(Arrays.asList(expectedDatabaseNames));

        Assert.assertTrue(expectedDatabaseNamesSet.contains(actualDatabaseName));
    }



    private static String getDatabaseNameFrom(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "select * from INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }
}
