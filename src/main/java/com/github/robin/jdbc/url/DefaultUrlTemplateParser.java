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
import com.google.escapevelocity.ParseException;
import com.google.escapevelocity.Template;

import java.io.IOException;
import java.io.StringReader;

import org.slf4j.*;

import java.util.*;
import java.util.stream.Collectors;


public final class DefaultUrlTemplateParser implements UrlTemplateParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUrlTemplateParser.class);

    private static final String URL_SEPARATOR = "\n";

    private static final DefaultUrlTemplateParser INSTANCE = new DefaultUrlTemplateParser();


    public static DefaultUrlTemplateParser getInstance() {
        return INSTANCE;
    }

    @Override
    public List<String> getURLs(String urlTemplate, Properties properties) throws URLTemplateException {

        try {
            LOGGER.debug("Template String: {}", urlTemplate.replaceAll(URL_SEPARATOR, "\\\\n"));
            if (LOGGER.isTraceEnabled()) {
                if (properties != null) {
                    LOGGER.debug("Received {} properties", properties.size());
                    properties.forEach((key, value) ->
                            LOGGER.trace("Property '{}'={}", key, value));
                } else {
                    LOGGER.debug("Properties are null");
                }
            }

            Map<String, Object> context = new HashMap<>();
            if (properties != null) {
                properties.forEach((key, value) -> context.put(String.valueOf(key), value));
            }

            Template template = Template.parseFrom(new StringReader(urlTemplate));
            String expressionOutput = template.evaluate(context);

            List<String> urls = Arrays.stream(expressionOutput.split(URL_SEPARATOR))
                    .map(String::trim)
                    .collect(Collectors.toList());

            LOGGER.debug("Template evaluated to {} URLs: {}", urls.size(), urls);

            if (urls.size() == 0) {
                throw new URLTemplateException("Template evaluation yielded no URL");
            } else if (urls.size() == 1) {
                throw new URLTemplateException(
                        "Template evaluation yielded one line: multiple lines are expected "
                                + "(remember to add '\n' at the end of template): " + urls.get(0));
            }

            return urls;
        } catch (ParseException pex) {
            LOGGER.error("User-defined Velocity template is invalid: {}", urlTemplate);

            throw new URLTemplateException(
                    "Template evaluation failed: ensure the template adheres to Velocity template syntax", pex);
        } catch (IOException ioe) {
            LOGGER.error("I/O error constructing template: {}", urlTemplate);

            throw new URLTemplateException(
                    "I/O error constructing template", ioe);

        }
    }

}
