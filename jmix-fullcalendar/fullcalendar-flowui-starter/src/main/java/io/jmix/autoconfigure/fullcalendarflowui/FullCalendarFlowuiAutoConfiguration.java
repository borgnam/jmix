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

package io.jmix.autoconfigure.fullcalendarflowui;

import io.jmix.core.CoreConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.fullcalendar.FullCalendarConfiguration;
import io.jmix.fullcalendarflowui.FullCalendarFlowuiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, FlowuiConfiguration.class, FullCalendarConfiguration.class, FullCalendarFlowuiConfiguration.class})
public class FullCalendarFlowuiAutoConfiguration {
}
