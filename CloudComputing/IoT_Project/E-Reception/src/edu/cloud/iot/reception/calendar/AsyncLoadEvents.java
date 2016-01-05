package edu.cloud.iot.reception.calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

class AsyncLoadEvents extends EventAsyncTask {

	AsyncLoadEvents(EventSampleActivity eventSample) {
		super(eventSample);
	}

	@Override
	protected void doInBackground() throws IOException {
		Events feeds = client.events().list("primary")
				.setFields(EventInfo.FEED_FIELDS).execute();
		model.reset(feeds);
	}

	static void run(EventSampleActivity eventSample) {
		new AsyncLoadEvents(eventSample).execute();
	}
}
