/*
 * Copyright (c) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.cloud.iot.reception.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

class EventModel {

	private final Map<String, EventInfo> events = new HashMap<String, EventInfo>();

	int size() {
		synchronized (events) {
			return events.size();
		}
	}

	void remove(String id) {
		synchronized (events) {
			events.remove(id);
		}
	}

	EventInfo get(String id) {
		synchronized (events) {
			return events.get(id);
		}
	}

	void add(Event calendarToAdd) {
		synchronized (events) {
			// EventInfo found = get(calendarToAdd.getId());
			EventInfo found = get(calendarToAdd.getId());
			try {
				if (calendarToAdd.getDescription() != null
						&& calendarToAdd.getDescription().toLowerCase()
								.contains(EventSampleActivity.VISITOR_NAME)) {
					// events.put(calendarToAdd.getId(), new
					// EventInfo(calendarToAdd));
					events.put(calendarToAdd.getId(), new EventInfo(
							calendarToAdd));
				}
			} catch (NullPointerException e) {
				Log.d("EVENTMODEL", calendarToAdd.getSummary());
			}
			// else {
			// found.update(calendarToAdd);
			// }
			//
		}
	}

	// void add(Event calendarToAdd) {
	// synchronized (events) {
	// List<Event> items = events.getItems();
	// for (Event event : items) {
	// System.out.println(event.getSummary());
	// }
	// }
	// }

	void reset(Events calendarsToAdd) {
		synchronized (events) {
			events.clear();
			List<Event> items = calendarsToAdd.getItems();
			for (Event event : items) {
				add(event);
			}
		}
	}

	public EventInfo[] toSortedArray() {
		synchronized (events) {
			List<EventInfo> result = new ArrayList<EventInfo>();
			for (EventInfo calendar : events.values()) {
				result.add(calendar.clone());
			}
			Collections.sort(result);
			return result.toArray(new EventInfo[0]);
		}
	}
}
