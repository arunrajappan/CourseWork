package edu.cloud.iot.reception.calendar;

import com.google.api.client.util.Objects;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

class EventInfo implements Comparable<EventInfo>, Cloneable {

	static final String FIELDS = "id,summary,description,location";
	static final String FEED_FIELDS = "items(" + FIELDS + ")";

	String id;
	String summary;
	String description;
	String location;

	EventInfo(String id, String summary, String description, String location) {
		this.id = id;
		this.summary = summary;
		this.description = description;
		this.location = location;
	}

	EventInfo(Event calendar) {
		update(calendar);

	}

	@Override
	public String toString() {
		return Objects.toStringHelper(EventInfo.class).add("id", id)
				.add("summary", summary).add("description", description).add("location",location)
				.toString();
	}

	public int compareTo(EventInfo other) {
		if (other.summary != null && summary != null) {
			return summary.compareTo(other.summary);
		}
		return 0;
	}

	@Override
	public EventInfo clone() {
		try {
			return (EventInfo) super.clone();
		} catch (CloneNotSupportedException exception) {
			// should not happen
			throw new RuntimeException(exception);
		}
	}

	void update(Event calendar) {
		id = calendar.getId();
		summary = calendar.getSummary();
		description = calendar.getDescription();
		location = calendar.getLocation();
	}

}
