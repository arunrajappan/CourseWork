package edu.cloud.iot.reception.ocr;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.cloud.iot.reception.R;
import edu.cloud.iot.reception.calendar.EventSampleActivity;
import edu.cloud.iot.reception.main.GridActivity;

public class VoiceRecognitionActivity extends Activity implements
		android.speech.tts.TextToSpeech.OnInitListener {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

	private ImageButton ibFirstName;
	private ImageButton ibLastName;
	private ImageButton ibDlNumber;
	private ImageButton ibAddress;
	private EditText etFirstName;
	private EditText etLastName;
	private EditText etDlNumber;
	private EditText etAddress;
	private TextToSpeech talker;
	View viewClicked;
	private Bundle extras;
	ArrayList<String> ocrResponse;
	private byte[] licenseBytes;
	String firstName, lastName, address, drivingNumber;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Log.i("EDebug:: VoiceRecognitionActivity:", "In OnCreate()");
		try {
			setContentView(R.layout.activity_voice_recognition);
			etFirstName = (EditText) findViewById(R.id.etFirstName);
			etLastName = (EditText) findViewById(R.id.etLastName);
			etDlNumber = (EditText) findViewById(R.id.etDlNumber);
			etAddress = (EditText) findViewById(R.id.etAddress);
			ibFirstName = (ImageButton) findViewById(R.id.ibFirstName);
			ibLastName = (ImageButton) findViewById(R.id.ibLastName);
			ibDlNumber = (ImageButton) findViewById(R.id.ibDlNumber);
			ibAddress = (ImageButton) findViewById(R.id.ibAddress);
			talker = new TextToSpeech(this, this);

			Intent intent = getIntent();
			// String message = intent.getStringExtra("Check1");
			extras = intent.getExtras();
			// message = "Check1::"+ (String) extras.get("Check1");
			Bundle b = (Bundle) extras.get("list");
			// ArrayList<String> al = (ArrayList<String>) b.get("al");
			ocrResponse = (ArrayList<String>) b.get("ocrresponse");
			Log.e("EDebug::onCreate()- ocrresponse = ", ocrResponse.toString()
					+ ":: ocrResponse.size() = " + ocrResponse.size());
			// licenseBytes = (byte[]) b.get("licensebytes");
			firstName = "First Name";
			lastName = "Last Name";

			if (ocrResponse.size() >= 8) {
				firstName = ocrResponse.get(6);
				lastName = ocrResponse.get(7);
				address = ocrResponse.get(8) + " " + ocrResponse.get(9);
				drivingNumber = ocrResponse.get(3);
				Log.e("EDebug::onCreate()- firstName = ", firstName + "::"
						+ lastName);
				etFirstName.setText(firstName);
				etLastName.setText(lastName);
				etAddress.setText(address);
				etDlNumber.setText(drivingNumber);
			}
		} catch (Exception ex) {
			Log.i("EDebug:: OnCreate():ERROR = ",
					ex.getLocalizedMessage() + "::" + ex.getMessage() + "::"
							+ Log.getStackTraceString(ex));

		}
	}

	public void checkVoiceRecognition() {
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			ibFirstName.setEnabled(false);
			Toast.makeText(this, "Voice recognizer not present",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void speak(View view) {
		viewClicked = view;
		Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
		intent.putExtra("calling_package", getClass().getPackage().getName());
		if (viewClicked.equals(ibFirstName)) {
			intent.putExtra("android.speech.extra.PROMPT", etFirstName
					.getText().toString());
		}
		if (viewClicked.equals(ibLastName)) {
			intent.putExtra("android.speech.extra.PROMPT", etLastName.getText()
					.toString());
		}
		if (viewClicked.equals(ibDlNumber)) {
			intent.putExtra("android.speech.extra.PROMPT", etDlNumber.getText()
					.toString());
		}
		if (viewClicked.equals(ibAddress)) {
			intent.putExtra("android.speech.extra.PROMPT", etAddress.getText()
					.toString());
		}
		intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "web_search");
		intent.putExtra("android.speech.extra.MAX_RESULTS", 1);
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)

			// If Voice recognition is successful then it returns RESULT_OK
			if (resultCode == RESULT_OK) {

				ArrayList<String> textMatchList = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				if (!textMatchList.isEmpty()) {
					// If first Match contains the 'search' word
					// Then start web search.
					if (((String) textMatchList.get(0)).contains("search")) {

						String searchQuery = textMatchList.get(0).replace(
								"search", " ");
						Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
						search.putExtra(SearchManager.QUERY, searchQuery);
						startActivity(search);
					} else {
						if (viewClicked.equals(ibFirstName)) { // text field to
																// show the
																// visitor last
																// name
							etFirstName.setText((CharSequence) textMatchList
									.get(0));
						}
						if (viewClicked.equals(ibLastName)) { // text field to
																// show the ID
																// number
							etLastName.setText((CharSequence) textMatchList
									.get(0));
						}
						if (viewClicked.equals(ibDlNumber)) { // text field to
																// show the
																// Address
							etDlNumber.setText((CharSequence) textMatchList
									.get(0));
						}
						if (viewClicked.equals(ibAddress)) { // text field to
																// show the
																// visitor first
																// name
							etAddress.setText((CharSequence) textMatchList
									.get(0));
						}
					}

				}
				// Result code for various error.
			} else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
				showToastMessage("Audio Error");
			} else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
				showToastMessage("Client Error");
			} else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
				showToastMessage("Network Error");
			} else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
				showToastMessage("No Match");
			} else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
				showToastMessage("Server Error");
			}
		super.onActivityResult(requestCode, resultCode, data);
	}

	void showToastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	protected void onDestroy() {
		if (talker != null) {
			talker.stop();
			talker.shutdown();
		}
		super.onDestroy();
	}

	public void nextHandler(View view) {
		ArrayList<String> formData = new ArrayList<String>();
		String vfirstName, vLastName, vDLNumber, vAddress;
		vfirstName = etFirstName.getText().toString();
		vLastName = etLastName.getText().toString();
		vDLNumber = etDlNumber.getText().toString();
		vAddress = etAddress.getText().toString();
		// formData.add(0,vfirstName);
		// formData.add(1,vLastName);
		// formData.add(2,vDLNumber);
		// formData.add(3,vAddress);
		// Bundle extra = new Bundle();
		// extra.putSerializable("formdata",formData);
		// Intent formIntent = new Intent(this, FormActivity.class);
		// formIntent.putExtra("formdata",formData);
		String name = vfirstName + " " + vLastName;
		Intent intent = new Intent(this, EventSampleActivity.class);
		intent.putExtra("visitor_name", name.toLowerCase());
		startActivity(intent);
		Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub

		if (status == TextToSpeech.SUCCESS) {
			talker.setLanguage(Locale.getDefault());
		} else {
			talker = null;
			Toast.makeText(this, "Failed to initialize TTS engine.",
					Toast.LENGTH_SHORT).show();
		}
		talker.speak(
				"Please verify your identity details, click the speaker button to make changes",
				0, null);
		Toast.makeText(this, "Please follow the voice guidance",
				Toast.LENGTH_SHORT).show();
	}

	public void goHome(View view) {
		Intent intent = new Intent(this, GridActivity.class);
		startActivity(intent);
		finish();
	}
}
