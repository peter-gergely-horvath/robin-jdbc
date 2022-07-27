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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.VelocityException;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;


public final class DefaultUrlTemplateParser implements UrlTemplateParser {

    private static final String URL_SEPARATOR = "\n";

    private static final DefaultUrlTemplateParser INSTANCE = new DefaultUrlTemplateParser();


    public static DefaultUrlTemplateParser getInstance() {
        return INSTANCE;
    }

    @Override
    public List<String> getURLs(String urlTemplate, Properties properties) throws URLTemplateException {

        try {
            Map<String, Object> context = new HashMap<>();
            properties.forEach((key, value) -> context.put(String.valueOf(key), value));


            VelocityContext velocityContext = new VelocityContext(context);
            StringWriter stringWriter = new StringWriter();
            StringReader reader = new StringReader(urlTemplate);

            Velocity.evaluate(velocityContext, stringWriter, "URL Template", reader);
            String expressionOutput = stringWriter.toString();


            List<String> urls = Arrays.stream(expressionOutput.split(URL_SEPARATOR))
                    .map(String::trim)
                    .collect(Collectors.toList());

            if (urls.size() == 0) {
                throw new URLTemplateException("Template evaluation yielded no URL");
            } else if (urls.size() == 1) {
                throw new URLTemplateException("Template evaluation yielded one line "
                        + "(remember to add '\n' at the end of template): " + urls.get(0));
            }

            return urls;
        } catch (VelocityException vex) {
            throw new URLTemplateException(
                    "Template evaluation failed: ensure the template adheres to Velocity template syntax", vex);
        }

    }

}
