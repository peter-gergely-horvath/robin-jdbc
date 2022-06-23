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

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.testng.Assert.fail;


public class DriverTest {

    @Test
    public void testConnection() {
        try {
            DriverManager.getConnection("jdbc:robin:retryCount=1;jdbc:h2:mem:foobar");

            fail("should have thrown an exception");

        } catch (SQLException sqlException) {
            Assert.assertTrue(sqlException.getMessage().contains("Not implemented yet:"));
        }
    }
}
