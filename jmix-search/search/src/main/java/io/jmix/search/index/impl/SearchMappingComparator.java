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

package io.jmix.search.index.impl;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SearchMappingComparator {

    public static final String PROPERTIES_KEY = "properties";

    public ComparingState compare(Map<String, Object> searchIndexMapping, Map<String, Object> applicationMapping) {
        return innerCompare((Map<String, Object>) searchIndexMapping.get(PROPERTIES_KEY), (Map<String, Object>) applicationMapping.get(PROPERTIES_KEY));
    }

    ComparingState innerCompare(Map<String, Object> searchIndexMapping, Map<String, Object> applicationMapping) {
        return applicationMapping.equals(searchIndexMapping) ? ComparingState.EQUAL : ComparingState.NOT_COMPATIBLE;
    }
}
