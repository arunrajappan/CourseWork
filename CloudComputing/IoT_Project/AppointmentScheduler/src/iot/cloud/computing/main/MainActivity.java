package iot.cloud.computing.main;

import iot.cloud.computing.R;
import iot.cloud.computing.events.EventModel;
import iot.cloud.computing.events.EventSampleActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView mDateDisplay;
	private Button mPickDate;
	private TextView mTimeDisplay;
	private Button mPickTime;

	private TextView mDateDisplay1;
	private Button mPickDate1;
	private TextView mTimeDisplay1;
	private Button mPickTime1;

	private EditText titleEdit;
	private EditText locationEdit;
	private EditText inviteesEdit;
	private EditText smsEdit;
	
	private Button submit;

	private int mYear;
	private int mMonth;
	private int mDay;

	private int mhour;
	private int mminute;

	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;
	static final int DATE_DIALOG_ID_END = 2;
	static final int TIME_DIALOG_ID_END = 3;

	String title, location, starttime, startdate, enddate, endtime, invitees,smsNumber;
	static final String smsBody = "Your Meeting has been Confirmed";
	Date startdatetime,enddatetime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDateDisplay = (TextView) findViewById(R.id.date);
		mPickDate = (Button) findViewById(R.id.datepicker);
		mTimeDisplay = (TextView) findViewById(R.id.time);
		mPickTime = (Button) findViewById(R.id.timepicker);

		mDateDisplay1 = (TextView) findViewById(R.id.date1);
		mPickDate1 = (Button) findViewById(R.id.datepicker1);
		mTimeDisplay1 = (TextView) findViewById(R.id.time1);
		mPickTime1 = (Button) findViewById(R.id.timepicker1);

		titleEdit = (EditText) findViewById(R.id.editTextTitle);
		locationEdit = (EditText) findViewById(R.id.editTextLocation);
		inviteesEdit = (EditText) findViewById(R.id.editTextInvitees);
		smsEdit = (EditText) findViewById(R.id.editSMS);
		
		submit = (Button) findViewById(R.id.submit);

		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		mhour = c.get(Calendar.HOUR_OF_DAY);
		mminute = c.get(Calendar.MINUTE);

		updateDate();updateDate1();updatetime();updatetime1();
		// Pick time's click event listener
		mPickTime.setOnClickListener(new View.OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(TIME_DIALOG_ID);
			}

		});

		// PickDate's click event listener
		mPickDate.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);

			}
		});

		mPickTime1.setOnClickListener(new View.OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(TIME_DIALOG_ID_END);
			}

		});

		// PickDate's click event listener
		mPickDate1.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID_END);

			}
		});

		submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				Intent intent = new Intent(MainActivity.this,
						EventSampleActivity.class);
				title = titleEdit.getText().toString();
				location = locationEdit.getText().toString();
				starttime = mTimeDisplay.getText().toString();
				startdate = mDateDisplay.getText().toString();
				endtime = mTimeDisplay1.getText().toString();
				enddate = mDateDisplay1.getText().toString();
				invitees = inviteesEdit.getText().toString();
				smsNumber = smsEdit.getText().toString();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
				String dateStartString = startdate+" "+starttime;
				String dateEndString = enddate+" "+endtime;
				try {
					startdatetime = sdf.parse(dateStartString);
					enddatetime = sdf.parse(dateStartString);
				} catch (ParseException e) {
					Toast.makeText(MainActivity.this, "Error in date and time", Toast.LENGTH_SHORT).show();
				}
				EventModel event = EventModel.getInstance();
				event.setTitle(title);
				event.setLocation(location);
				event.setStartDateTime(startdatetime);
				event.setEndDateTime(enddatetime);
				event.setDescription(invitees);
				Intent i = new Intent(MainActivity.this,
						EventSampleActivity.class);
				i.putExtra("eventObject", event);
				startActivity(i);
				if(smsNumber != null){
					sendSMS(smsNumber);
				
				}

			}

			private void sendSMS(String smsNumber) {
				// TODO Auto-generated method stub
					  String sms = smsBody;
		 
					  try {
						SmsManager smsManager = SmsManager.getDefault();
						smsManager.sendTextMessage(smsNumber, null, sms, null, null);
						Toast.makeText(getApplicationContext(), "SMS Sent!",
									Toast.LENGTH_LONG).show();
					  } catch (Exception e) {
						Toast.makeText(getApplicationContext(),
							"SMS faild, please try again later!",
							Toast.LENGTH_LONG).show();
						e.printStackTrace();
					  }
				}	
			
		});
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// -------------------------------------------update
	// date----------------------------------------//
	private void updateDate() {

		mDateDisplay.setText(new StringBuilder()
				// Month is 0 based so add 1
				.append(mDay).append("/").append(mMonth + 1).append("/")
				.append(mYear).append(" "));
		// showDialog(TIME_DIALOG_ID);

	}

	// -------------------------------------------update
	// time----------------------------------------//
	public void updatetime() {
		mTimeDisplay.setText(new StringBuilder().append(pad(mhour)).append(":")
				.append(pad(mminute)));
	}

	private void updateDate1() {

		mDateDisplay1.setText(new StringBuilder()
				// Month is 0 based so add 1
				.append(mDay).append("/").append(mMonth + 1).append("/")
				.append(mYear).append(" "));
		// showDialog(TIME_DIALOG_ID);

	}

	// -------------------------------------------update
	// time----------------------------------------//
	public void updatetime1() {
		mTimeDisplay1.setText(new StringBuilder().append(pad(mhour))
				.append(":").append(pad(mminute)));
	}

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	// Datepicker dialog generation

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDate();
		}
	};

	// Timepicker dialog generation
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mhour = hourOfDay;
			mminute = minute;
			updatetime();
		}

	};

	private DatePickerDialog.OnDateSetListener mDateSetListener1 = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDate1();
		}
	};

	// Timepicker dialog generation
	private TimePickerDialog.OnTimeSetListener mTimeSetListener1 = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mhour = hourOfDay;
			mminute = minute;
			updatetime1();
		}

	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);

		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListener, mhour, mminute,
					false);

		case DATE_DIALOG_ID_END:
			return new DatePickerDialog(this, mDateSetListener1, mYear, mMonth,
					mDay);

		case TIME_DIALOG_ID_END:
			return new TimePickerDialog(this, mTimeSetListener1, mhour,
					mminute, false);

		}
		return null;
	}

}
