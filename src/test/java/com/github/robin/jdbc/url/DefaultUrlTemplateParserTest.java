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

import com.github.robin.jdbc.config.URLTemplateException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Properties;

public class DefaultUrlTemplateParserTest {

    private DefaultUrlTemplateParser urlTemplateParser;

    @BeforeTest
    public void beforeTest() {
        urlTemplateParser = DefaultUrlTemplateParser.getInstance();
    }


    @Test
    public void testSimplePattern() throws URLTemplateException {

        String urlPattern = "#foreach( $index in [1..3] )jdbc:h2:mem:foobar0$index\n#end";
        List<String> urls = urlTemplateParser.getUrls(urlPattern, new Properties());

        Assert.assertEquals(3, urls.size());

        Assert.assertEquals("jdbc:h2:mem:foobar01", urls.get(0));
        Assert.assertEquals("jdbc:h2:mem:foobar02", urls.get(1));
        Assert.assertEquals("jdbc:h2:mem:foobar03", urls.get(2));

    }

    @Test
    public void testPatternWithRepeatedVariableReference() throws URLTemplateException {

        String urlPattern = "#foreach( $index in [1..3] )jdbc:h2:mem:foobar0$index$index\n#end";


        List<String> urls = urlTemplateParser.getUrls(urlPattern, new Properties());

        Assert.assertEquals(3, urls.size());

        Assert.assertEquals("jdbc:h2:mem:foobar011", urls.get(0));
        Assert.assertEquals("jdbc:h2:mem:foobar022", urls.get(1));
        Assert.assertEquals("jdbc:h2:mem:foobar033", urls.get(2));

    }

}
