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

package com.github.robin.jdbc.url;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;

public class UrlPatternParserTest {

    private UrlPatternParser urlPatternParser;

    @BeforeTest
    public void beforeTest() {
        urlPatternParser = UrlPatternParser.getInstance();
    }

    @Test
    public void testSimplePattern() {

        List<String> urls = urlPatternParser.getURLs("jdbc:h2:mem:foobar0[1-3]");

        Assert.assertEquals(3, urls.size());

        Assert.assertEquals("jdbc:h2:mem:foobar01", urls.get(0));
        Assert.assertEquals("jdbc:h2:mem:foobar02", urls.get(1));
        Assert.assertEquals("jdbc:h2:mem:foobar03", urls.get(2));

    }


}
