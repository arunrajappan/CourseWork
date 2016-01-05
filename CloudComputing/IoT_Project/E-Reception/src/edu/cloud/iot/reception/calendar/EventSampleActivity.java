package edu.cloud.iot.reception.calendar;

import edu.cloud.iot.reception.R;
import edu.cloud.iot.reception.main.GridActivity;
import edu.cloud.iot.reception.main.LicenseInstructionActivity;
import edu.cloud.iot.reception.ocr.ScanLicense;
import edu.cloud.iot.reception.ocr.VoiceRecognitionActivity;
import edu.cloud.iot.reception.qrcode.QRActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.CalendarScopes;

import android.R.color;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public final class EventSampleActivity extends Activity implements
		OnItemClickListener {

	static final String TAG = "EventSampleactivity";

	private static final Level LOGGING_LEVEL = Level.OFF;

	ArrayAdapter<EventInfo> eventadaptor;

	private ListView listView;

	EventModel model = new EventModel();

	com.google.api.services.calendar.Calendar client;

	int numAsyncTasks;

	GoogleAccountCredential credential;

	private static final String PREF_ACCOUNT_NAME = "accountName";

	static String VISITOR_NAME;

	final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

	private static final int SUBMIT_EVENT = 0;

	private static final int BACK = 1;

	static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;

	static final int REQUEST_AUTHORIZATION = 1;

	static final int REQUEST_ACCOUNT_PICKER = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// enable logging
		Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
		// view and menu
		setContentView(R.layout.eventlist);
		listView = (ListView) findViewById(R.id.eventlist);
		Intent intent = getIntent();
		VISITOR_NAME = intent.getStringExtra("visitor_name");
		Toast msg = Toast.makeText(getBaseContext(), VISITOR_NAME,
				Toast.LENGTH_LONG);
		msg.show();
		credential = GoogleAccountCredential.usingOAuth2(this,
				Collections.singleton(CalendarScopes.CALENDAR));
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME,
				null));
		// Calendar client
		client = new com.google.api.services.calendar.Calendar.Builder(
				transport, jsonFactory, credential).setApplicationName(
				"calendar-local").build();
		listView.setOnItemClickListener(this);
	}

	public static String getVistorName() {
		return VISITOR_NAME;
	}

	void refreshView() {
		eventadaptor = new ArrayAdapter<EventInfo>(this,
				android.R.layout.simple_list_item_1, model.toSortedArray()) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// by default it uses toString; override to use summary instead
				TextView view = (TextView) super.getView(position, convertView,
						parent);
				TextView tv = (TextView) view;
				tv.setTextColor(Color.WHITE);
				EventInfo calendarInfo = getItem(position);
				view.setText(calendarInfo.summary);
				return view;
			}
		};

		listView.setAdapter(eventadaptor);
	}

	void showGooglePlayServicesAvailabilityErrorDialog(
			final int connectionStatusCode) {
		runOnUiThread(new Runnable() {
			public void run() {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						connectionStatusCode, EventSampleActivity.this,
						REQUEST_GOOGLE_PLAY_SERVICES);
				dialog.show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (checkGooglePlayServicesAvailable()) {
			haveGooglePlayServices();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_GOOGLE_PLAY_SERVICES:
			if (resultCode == Activity.RESULT_OK) {
				haveGooglePlayServices();
			} else {
				checkGooglePlayServicesAvailable();
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				AsyncLoadEvents.run(this);
			} else {
				chooseAccount();
			}
			break;
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == Activity.RESULT_OK && data != null
					&& data.getExtras() != null) {
				String accountName = data.getExtras().getString(
						AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential.setSelectedAccountName(accountName);
					SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_ACCOUNT_NAME, accountName);
					editor.commit();
					AsyncLoadEvents.run(this);
				}
			}
			break;

		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, SUBMIT_EVENT, 0, R.string.submit);
		menu.add(0, BACK, 0, R.string.back);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int calendarIndex = (int) info.id;
		if (calendarIndex < eventadaptor.getCount()) {
			final EventInfo calendarInfo = eventadaptor.getItem(calendarIndex);
			switch (item.getItemId()) {
			case SUBMIT_EVENT:
				Toast.makeText(getApplicationContext(), calendarInfo.id,
						Toast.LENGTH_LONG).show();
				Intent i = new Intent();
				// i.putExtra("calendarId",calendarInfo.id);
				// i.putExtra("calendar",(Serializable)client);
				i.setClass(this, EventSampleActivity.class);
				startActivity(i);
			}
		}
		return super.onContextItemSelected(item);
	}

	/** Check that Google Play services APK is installed and up to date. */
	private boolean checkGooglePlayServicesAvailable() {
		final int connectionStatusCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
			return false;
		}
		return true;
	}

	private void haveGooglePlayServices() {
		// check if there is already an account selected
		if (credential.getSelectedAccountName() == null) {
			// ask user to choose account
			chooseAccount();
		} else {
			// load calendars
			AsyncLoadEvents.run(this);
		}
	}

	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.form, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		EventInfo entry = (EventInfo) parent.getAdapter().getItem(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final String location = entry.location;
		String message = "Name : " + VISITOR_NAME + "\n" + "Room No : "
				+ location;
		builder.setMessage(message).setTitle(entry.summary);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intent = new Intent(EventSampleActivity.this,
						QRActivity.class);
				intent.putExtra("location", location);
				startActivity(intent);
				
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();

	}

	public void goHome(View view) {
		Intent intent = new Intent(view.getContext(), GridActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public void goBack(View view) {
		Intent intent = new Intent(view.getContext(),
				VoiceRecognitionActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();

	}
}
