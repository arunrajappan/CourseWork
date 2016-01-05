
package iot.cloud.computing.events;



import iot.cloud.computing.R;

import java.util.Collections;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public final class EventSampleActivity extends Activity {


  static final String TAG = "EventSampleactivity";
  
  private static final Level LOGGING_LEVEL = Level.OFF;
  
  ArrayAdapter<EventModel> eventadaptor;
  
  
//  EventModel model = new EventModel();
  
  com.google.api.services.calendar.Calendar client;
  
  int numAsyncTasks;
  
  GoogleAccountCredential credential;
  
  private static final String PREF_ACCOUNT_NAME = "accountName";
  
  
  
  TextView statusMessage;
  
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
    Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
    setContentView(R.layout.activity_display);

//    statusMessage = (TextView) findViewById(R.id.status_message);
    
    Intent intent = getIntent();
    EventModel event = (EventModel)intent.getSerializableExtra("eventObject");
    Toast.makeText(EventSampleActivity.this, event.toString(), Toast.LENGTH_SHORT).show();
    
    credential =
            GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        // Calendar client
        client = new com.google.api.services.calendar.Calendar.Builder(
            transport, jsonFactory, credential).setApplicationName("calendar-local")
            .build();
    }
  

  void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
	    runOnUiThread(new Runnable() {
	      public void run() {
	        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
	            connectionStatusCode, EventSampleActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
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
	        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
	          String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
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
	  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    menu.add(0, SUBMIT_EVENT, 0, R.string.submit);
	    menu.add(0, BACK, 0, R.string.back);
	  }
	  
	  
	  @Override
	  public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    int calendarIndex = (int) info.id;
	    if (calendarIndex < eventadaptor.getCount()) {
	      final EventModel calendarInfo = eventadaptor.getItem(calendarIndex);
	      switch (item.getItemId()) {
	        case SUBMIT_EVENT:
	        	Toast.makeText(getApplicationContext(), calendarInfo.getTitle(), Toast.LENGTH_SHORT).show();
	        	Intent i = new Intent();
	        	i.setClass(this, EventSampleActivity.class);
	        	startActivity(i);
	      }
	    }
	    return super.onContextItemSelected(item);
	  }
	  
	  /** Check that Google Play services APK is installed and up to date. */
	  private boolean checkGooglePlayServicesAvailable() {
	    final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
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
	    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	  }

//		@Override
//		public boolean onCreateOptionsMenu(Menu menu) {
//
//			// Inflate the menu; this adds items to the action bar if it is present.
//			getMenuInflater().inflate(R.menu.form, menu);
//			return true;
//		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == R.id.action_settings) {
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
}
