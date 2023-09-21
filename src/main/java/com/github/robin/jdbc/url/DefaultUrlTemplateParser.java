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

    private static final String MACRO_NAME = "jdbcUrlsFrom";
    public static final String MACRO_CODE = String.format(
                    "#macro( %s $inputList )\n" +
                    "#foreach( $value in $inputList )$!bodyContent\n#end\n" +
                    "#end\n", MACRO_NAME);
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

            String fullTemplate = MACRO_CODE + urlTemplate;

            Template template = Template.parseFrom(new StringReader(fullTemplate));

            String templateResult = template.evaluate(context).trim();

            return extractUrlsFromTemplateResult(templateResult);

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

    private static List<String> extractUrlsFromTemplateResult(String templateResult) throws URLTemplateException {

        String templateResultLines;
        if (templateResult.contains(URL_SEPARATOR)) {
            templateResultLines = templateResult;
        } else {
            templateResultLines = templateResult.replaceAll("(jdbc:\\w+:)", "\n$1");
        }


        List<String> urls = Arrays.stream(templateResultLines.trim().split(URL_SEPARATOR))
                .map(String::trim)
                .collect(Collectors.toList());

        LOGGER.debug("Template evaluated to {} URLs: {}", urls.size(), urls);

        switch (urls.size()) {
            case 0:
                throw new URLTemplateException("Template evaluation yielded no URL");

            case 1:
                throw new URLTemplateException(
                        "Template evaluation yielded one line: multiple lines are expected. "
                                + "(Try using #@" + MACRO_NAME + " macro. "
                                + "The only returned URL is: " + urls.get(0));

            default:
                return urls;
        }
    }

}
