package iot.cloud.computing.events;

import java.io.Serializable;
import java.util.Date;

import com.google.api.client.util.Objects;

public class EventModel implements Serializable {

private static EventModel eventmodel;
	
	String title;
	String location;
	Date startDateTime;
	Date endDateTime;
	
	String description;


	public static EventModel getInstance(){
		if(eventmodel == null) {
			eventmodel =  new EventModel();
		}
		return eventmodel;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(EventModel.class).add("title", title)
				.add("location", location).add("startdatetime", startDateTime)
				.add("enddatetime", endDateTime).add("description", description)
				.toString();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	

	public Date getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}

	public Date getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	
}
