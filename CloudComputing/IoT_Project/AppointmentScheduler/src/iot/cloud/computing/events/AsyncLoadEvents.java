package iot.cloud.computing.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

class AsyncLoadEvents extends EventAsyncTask {

	AsyncLoadEvents(EventSampleActivity eventSample) {
		super(eventSample);
	}

	@Override
  protected void doInBackground() throws IOException {

	Event event = new Event();
	EventModel m = EventModel.getInstance();
	event.setSummary(m.getTitle());
	event.setLocation(m.getLocation());

	
	DateTime start = new DateTime(m.getStartDateTime(), TimeZone.getTimeZone("UTC"));
	event.setStart(new EventDateTime().setDateTime(start));
	DateTime end = new DateTime(m.getEndDateTime(), TimeZone.getTimeZone("UTC"));
	event.setEnd(new EventDateTime().setDateTime(end));
	event.setDescription(m.getDescription());
	Event createdEvent = client.events().insert("primary", event).execute();
}

	static void run(EventSampleActivity eventSample) {
		new AsyncLoadEvents(eventSample).execute();
	}
}
