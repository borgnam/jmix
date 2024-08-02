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

package io.jmix.fullcalendarflowui.kit.component.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

public interface LazyCalendarEventProvider extends CalendarEventProvider {

    List<CalendarEvent> onItemsFetch(ItemsFetchContext context);

    class ItemsFetchContext {
        protected LazyCalendarEventProvider eventProvider;

        protected LocalDateTime startDateTime;
        protected LocalDateTime endDateTime;
        protected TimeZone componentTimeZone;

        public ItemsFetchContext(LazyCalendarEventProvider eventProvider,
                                 LocalDateTime startDateTime,
                                 LocalDateTime endDateTime,
                                 TimeZone componentTimeZone) {
            this.eventProvider = eventProvider;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.componentTimeZone = componentTimeZone;
        }

        public LazyCalendarEventProvider getEventProvider() {
            return eventProvider;
        }

        /**
         * Returns date time object that corresponds to system default time zone: {@link TimeZone#getDefault()}.
         *
         * @return left border of visible range in calendar
         */
        public LocalDateTime getStartDateTime() {
            return startDateTime;
        }

        /**
         * Returns date time object that corresponds to system default time zone: {@link TimeZone#getDefault()}.
         *
         * @return right border of visible range in calendar
         */
        public LocalDateTime getEndDateTime() {
            return endDateTime;
        }

        public TimeZone getComponentTimeZone() {
            return componentTimeZone;
        }
    }
}
