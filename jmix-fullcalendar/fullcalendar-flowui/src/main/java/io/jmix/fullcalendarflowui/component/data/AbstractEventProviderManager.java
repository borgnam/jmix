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

package io.jmix.fullcalendarflowui.component.data;

import com.vaadin.flow.data.provider.KeyMapper;
import io.jmix.core.annotation.Internal;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.serialization.serializer.FullCalendarSerializer;
import io.jmix.fullcalendarflowui.component.serialization.serializer.FullCalendarSerializer.FullCalendarDataSerializer;
import org.springframework.lang.Nullable;

/**
 * INTERNAL.
 * <p>
 * The manager of concrete event provider. It is a connector between event source in client-side and server's
 * {@link BaseCalendarEventProvider}.
 */
@Internal
public abstract class AbstractEventProviderManager {

    protected final BaseCalendarEventProvider eventProvider;
    protected final String sourceId;

    protected final String jsFunctionName;
    protected final KeyMapper<Object> eventKeyMapper = new KeyMapper<>();

    protected FullCalendarDataSerializer dataSerializer;
    protected FullCalendar fullCalendar;

    public AbstractEventProviderManager(BaseCalendarEventProvider eventProvider,
                                        FullCalendarSerializer serializer,
                                        FullCalendar fullCalendar,
                                        String jsFunctionName) {
        this.eventProvider = eventProvider;
        this.fullCalendar = fullCalendar;
        this.jsFunctionName = jsFunctionName;

        this.sourceId = generateSourceId(eventProvider);

        this.dataSerializer = serializer.createDataSerializer(sourceId, eventKeyMapper);
        this.dataSerializer.setTimeZoneSupplier(fullCalendar::getTimeZone);
    }

    /**
     * @return event provider
     */
    public BaseCalendarEventProvider getEventProvider() {
        return eventProvider;
    }

    /**
     * @return event provider's ID that is used in client-side
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * @return a JS function that should be invoked to add event provider to component at the client-side
     */
    public String getJsFunctionName() {
        return this.jsFunctionName;
    }

    /**
     * @param clientId ID of event from client-side
     * @return calendar event or {@code null} if there is no event with the provided ID
     */
    @Nullable
    public abstract CalendarEvent getCalendarEvent(String clientId);

    protected String generateSourceId(BaseCalendarEventProvider eventProvider) {
        return eventProvider.getId() + "-" + EventProviderUtils.generateId();
    }
}
