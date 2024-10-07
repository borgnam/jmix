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

package io.jmix.searchopensearch.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.IndexConfiguration;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.opensearch.client.opensearch.indices.IndexSettingsAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("search_OpenSearchIndexSettingsProvider")
public class OpenSearchIndexSettingsProvider {

    protected final OpenSearchClient client;

    protected final List<OpenSearchIndexSettingsConfigurer> customConfigurers;
    protected final List<OpenSearchIndexSettingsConfigurer> systemConfigurers;

    protected final OpenSearchIndexSettingsConfigurationContext context;

    protected final Map<Class<?>, IndexSettings> effectiveIndexSettings;

    protected final IndexSettings commonIndexSettings;
    protected final IndexSettingsAnalysis commonAnalysisSettings;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OpenSearchIndexSettingsProvider(List<OpenSearchIndexSettingsConfigurer> configurers, OpenSearchClient client) {
        this.client = client;

        this.customConfigurers = new ArrayList<>();
        this.systemConfigurers = new ArrayList<>();
        prepareConfigurers(configurers);

        this.context = configureContext();

        this.commonIndexSettings = context.getCommonIndexSettingsBuilder().build();
        this.commonAnalysisSettings = context.getCommonAnalysisBuilder().build();
        this.effectiveIndexSettings = new ConcurrentHashMap<>();
    }

    public IndexSettings getSettingsForIndex(IndexConfiguration indexConfiguration) {
        Class<?> entityClass = indexConfiguration.getEntityClass();
        IndexSettings effectiveIndexSettings = this.effectiveIndexSettings.get(entityClass);

        if (effectiveIndexSettings == null) {
            Map<Class<?>, IndexSettings.Builder> indexSettingsBuilders = context.getAllSpecificIndexSettingsBuilders();
            IndexSettings indexSettings;
            if (indexSettingsBuilders.containsKey(entityClass)) {
                // Merge common and entity-specific index settings
                IndexSettings.Builder indexSettingsBuilder = indexSettingsBuilders.get(entityClass);
                indexSettings = indexSettingsBuilder.build();

                ObjectNode commonIndexSettingsNode = toObjectNode(commonIndexSettings);
                ObjectNode indexSettingsNode = toObjectNode(indexSettings);
                ObjectNode mergedIndexSettingsNode = JsonNodeFactory.instance.objectNode();
                mergedIndexSettingsNode.setAll(commonIndexSettingsNode);
                indexSettingsNode.fieldNames().forEachRemaining(childName -> {
                    JsonNode specificChildNode = indexSettingsNode.get(childName);
                    JsonNode baseChildNode = mergedIndexSettingsNode.path(childName);
                    if (specificChildNode.isObject()) {
                        ObjectNode specificChildObjectNode = (ObjectNode) specificChildNode;
                        if (baseChildNode.isObject()) {
                            ((ObjectNode) baseChildNode).setAll(specificChildObjectNode);
                        } else {
                            mergedIndexSettingsNode.set(childName, specificChildObjectNode);
                        }
                    }
                });

                JsonpMapper jsonpMapper = client._transport().jsonpMapper();
                JsonProvider jsonProvider = jsonpMapper.jsonProvider();
                try (StringReader reader = new StringReader(indexSettingsNode.toString())) {
                    JsonParser parser = jsonProvider.createParser(reader);
                    indexSettings = IndexSettings._DESERIALIZER.deserialize(parser, jsonpMapper);
                }
            } else {
                indexSettings = commonIndexSettings;
            }

            Map<Class<?>, IndexSettingsAnalysis.Builder> analysisBuilders = context.getAllSpecificAnalysisBuilders();
            IndexSettingsAnalysis analysisSettings;
            if (analysisBuilders.containsKey(entityClass)) {
                // Merge common and entity-specific analysis settings
                ObjectNode commonAnalysisSettingsNode = toObjectNode(commonAnalysisSettings);
                IndexSettingsAnalysis.Builder analysisBuilder = analysisBuilders.get(entityClass);
                analysisSettings = analysisBuilder.build();

                ObjectNode analysisSettingsNode = toObjectNode(analysisSettings);
                ObjectNode mergedAnalysisNode = JsonNodeFactory.instance.objectNode();
                mergedAnalysisNode.setAll(commonAnalysisSettingsNode);
                analysisSettingsNode.fieldNames().forEachRemaining(childName -> {
                    JsonNode specificChildNode = analysisSettingsNode.get(childName);
                    JsonNode baseChildNode = mergedAnalysisNode.path(childName);
                    if (specificChildNode.isObject()) {
                        ObjectNode specificChildObjectNode = (ObjectNode) specificChildNode;
                        if (baseChildNode.isObject()) {
                            ((ObjectNode) baseChildNode).setAll(specificChildObjectNode);
                        } else {
                            mergedAnalysisNode.set(childName, specificChildObjectNode);
                        }
                    }
                });

                JsonpMapper jsonpMapper = client._transport().jsonpMapper();
                JsonProvider jsonProvider = jsonpMapper.jsonProvider();
                try (StringReader reader = new StringReader(mergedAnalysisNode.toString())) {
                    JsonParser parser = jsonProvider.createParser(reader);
                    analysisSettings = IndexSettingsAnalysis._DESERIALIZER.deserialize(parser, jsonpMapper);
                }
            } else {
                analysisSettings = commonAnalysisSettings;
            }

            effectiveIndexSettings = new IndexSettings.Builder()
                    .index(indexSettings)
                    .analysis(analysisSettings)
                    .build();

            this.effectiveIndexSettings.put(entityClass, effectiveIndexSettings);
        }
        return effectiveIndexSettings;
    }

    protected OpenSearchIndexSettingsConfigurationContext configureContext() {
        OpenSearchIndexSettingsConfigurationContext context = new OpenSearchIndexSettingsConfigurationContext();
        systemConfigurers.forEach(configurer -> configurer.configure(context));
        customConfigurers.forEach(configurer -> configurer.configure(context));
        return context;
    }

    protected void prepareConfigurers(List<OpenSearchIndexSettingsConfigurer> configurers) {
        configurers.forEach(configurer -> {
            if (configurer.isSystem()) {
                systemConfigurers.add(configurer);
            } else {
                customConfigurers.add(configurer);
            }
        });
    }

    // TODO extract to platform-specific utils
    protected JsonNode toJsonNode(JsonpSerializable object) {
        StringWriter stringWriter = new StringWriter();
        JsonpMapper mapper = client._transport().jsonpMapper();
        JsonGenerator generator = mapper.jsonProvider().createGenerator(stringWriter);
        object.serialize(generator, mapper);
        generator.close();
        String stringValue = stringWriter.toString();

        try {
            return objectMapper.readTree(stringValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to generate JsonNode", e);
        }
    }

    protected ObjectNode toObjectNode(JsonpSerializable object) {
        JsonNode jsonNode = toJsonNode(object);
        if (jsonNode.isObject()) {
            return (ObjectNode) jsonNode;
        } else {
            throw new RuntimeException("Unable to convert provided object to ObjectNode: JsonNode type is '" + jsonNode.getNodeType() + "'");
        }
    }
}
