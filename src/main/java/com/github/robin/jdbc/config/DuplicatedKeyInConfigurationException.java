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

 
package com.github.robin.jdbc.config;

/**
 * Thrown to indicate that a method has been passed configuration with the same key
 * being duplicated.
 */
public class DuplicatedKeyInConfigurationException extends MisconfigurationException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>DuplicatedKeyInConfigurationException</code> with the
     * specified detail message.
     *
     * @param   message   the detail message.
     */
    public DuplicatedKeyInConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs an <code>DuplicatedKeyInConfigurationException</code> with the String formatted with
     * arguments as detail message.
     *
     * @param format the string to format to generate the detail message string
     * @param args argument to be used when formatting the detail message string
     *
     * @return a <code>DuplicatedKeyInConfigurationException</code> with detail message built from the parameters
     */
    public static DuplicatedKeyInConfigurationException forMessage(String format, Object... args) {
        return new DuplicatedKeyInConfigurationException(String.format(format, args));
    }

}
