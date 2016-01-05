package edu.cloud.iot.reception.calendar;

import android.os.AsyncTask;
import android.view.View;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.IOException;

import edu.cloud.iot.reception.R;
import edu.cloud.iot.reception.utlis.Utils;

abstract class EventAsyncTask extends AsyncTask<Void, Void, Boolean> {

	final EventSampleActivity activity;
	final EventModel model;
	final com.google.api.services.calendar.Calendar client;

	// private View progressBar = null;

	EventAsyncTask(EventSampleActivity activity) {
		this.activity = activity;
		model = activity.model;
		client = activity.client;
		// progressBar = activity.findViewById(R.id.title_refresh_progress);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		activity.numAsyncTasks++;
		// progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected final Boolean doInBackground(Void... ignored) {
		try {
			doInBackground();
			return true;
		} catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
			activity.showGooglePlayServicesAvailabilityErrorDialog(availabilityException
					.getConnectionStatusCode());
		} catch (UserRecoverableAuthIOException userRecoverableException) {
			activity.startActivityForResult(
					userRecoverableException.getIntent(),
					activity.REQUEST_AUTHORIZATION);
		} catch (IOException e) {
			Utils.logAndShow(activity, EventSampleActivity.TAG, e);
		}
		return false;
	}

	@Override
	protected final void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		if (0 == --activity.numAsyncTasks) {
			// progressBar.setVisibility(View.GONE);
		}
		if (success) {
			activity.refreshView();
		}
	}

	abstract protected void doInBackground() throws IOException;
}
