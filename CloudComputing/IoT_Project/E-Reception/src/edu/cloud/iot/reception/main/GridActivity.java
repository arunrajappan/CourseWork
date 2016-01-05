package edu.cloud.iot.reception.main;

import java.util.Locale;

import edu.cloud.iot.reception.R;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class GridActivity extends Activity implements
		android.speech.tts.TextToSpeech.OnInitListener {
	private TextToSpeech tts;
	String message = "";
	Button call, scan, about_us, exit;
	private Intent intent;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		tts = new TextToSpeech(this, this);
		message = "Hi, Welcome to intelligent building system. Please click scan button to enter the proceed for ID Verification. "
				+ "You may feel free to click call button for help. "
				+ " We would be excited to talk about us once you click the globe button. "
				+ "You can exit anytime clicking the exit button. ";
		speakOut(message);
		scan = (Button) findViewById(R.id.button1);
		call = (Button) findViewById(R.id.button2);
		about_us = (Button) findViewById(R.id.button3);
		exit = (Button) findViewById(R.id.button4);

		// On Click event for Single Gridview Item
		PhoneCallListener phoneListener = new PhoneCallListener();
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener,
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	public void goScan(View view) {
		intent = new Intent(GridActivity.this, LicenseInstructionActivity.class);
		startActivity(intent);
		tts.shutdown();
		finish();
	}

	public void goCall(View view) {
		intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:19725108115"));
		tts.shutdown();
		startActivity(intent);
		
	}

	public void goAbout(View view) {
		Uri uriUrl = Uri.parse("http://www.google.com/");
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
		tts.shutdown();
		startActivity(launchBrowser);
	}

	public void goExit(View view) {
		System.exit(0);
		int pid = android.os.Process.myPid();
		tts.shutdown();
		android.os.Process.killProcess(pid);
		finish();
	}

	class PhoneCallListener extends PhoneStateListener {

		private boolean isPhoneCalling = false;

		String LOG_TAG = "LOGGING 123";

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			if (TelephonyManager.CALL_STATE_RINGING == state) {
				// phone ringing
				Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
			}

			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				// active
				Log.i(LOG_TAG, "OFFHOOK");
				isPhoneCalling = true;
			}

			if (TelephonyManager.CALL_STATE_IDLE == state) {
				// run when class initial and phone call ended,
				// need detect flag from CALL_STATE_OFFHOOK
				Log.i(LOG_TAG, "IDLE");

				if (isPhoneCalling) {

					Log.i(LOG_TAG, "restart app");

					// restart app
					Intent i = GridActivity.this.getPackageManager()
							.getLaunchIntentForPackage(
									getBaseContext().getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					tts.shutdown();
					startActivity(i);
					isPhoneCalling = false;
				}
			}
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
				Toast.makeText(getApplication(),
						"Text to Speech is not Supported", Toast.LENGTH_SHORT)
						.show();
			} else {
				speakOut(message);
			}
		} else {
			Log.e("TTS", "Initilization Failed!");
		}
	}

	private void speakOut(String message) {
		String text = message.toString();
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

}