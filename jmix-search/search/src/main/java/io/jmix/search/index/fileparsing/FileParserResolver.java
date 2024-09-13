/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.search.index.fileparsing;

import org.apache.tika.parser.Parser;

import java.util.List;

/**
 * Is a part of the extendable engine the gives an ability to implement custom file parser resolvers and to support
 * custom file types or to modify behavior of existing file parser resolvers.
 */
public interface FileParserResolver {

    /**
     * Returns a collection of supported extensions of the supported file type. E.g. ["xlsx", "XLSX", "DOCX", "DOCX"].
     * @return collection of supported extensions
     */
    List<String> getExtension();

    /**
     * Returns an instance of a file parser that is returned for the extensions being returned by
     * {@link #getExtension()} method.
     * @return an instance of a file parser
     */
    Parser getParser();
}
