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

package io.jmix.search.index.annotation;

import java.lang.annotation.*;

import static io.jmix.search.index.annotation.DynamicAttributes.ReferenceFieldsIndexingMode.NONE;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@FieldMappingAnnotation
public @interface DynamicAttributes {
    String[] includeCategories() default "";

    String[] excludeProperties() default "";

    ReferenceFieldsIndexingMode referenceFieldsIndexingMode() default NONE;

    enum ReferenceFieldsIndexingMode {
        NONE, INSTANCE_NAME_ONLY
    }
}
