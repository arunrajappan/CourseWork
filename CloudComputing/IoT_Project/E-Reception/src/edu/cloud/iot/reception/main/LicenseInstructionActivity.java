package edu.cloud.iot.reception.main;

import java.util.Locale;

import edu.cloud.iot.reception.R;
import edu.cloud.iot.reception.calendar.EventSampleActivity;
import edu.cloud.iot.reception.ocr.ScanLicense;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LicenseInstructionActivity extends Activity implements
		android.speech.tts.TextToSpeech.OnInitListener {
	private Button button;
	private TextToSpeech tts;
	String message = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_instruction);
		button = (Button) findViewById(R.id.button1);
		tts = new TextToSpeech(this, this);
		message = getResources().getString(R.string.license_instruction);
		speakOut(message);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LicenseInstructionActivity.this,
						ScanLicense.class);
				startActivity(intent);
				tts.shutdown();
			}
		});
	}

	public void goHome(View view) {
		Intent intent = new Intent(this, GridActivity.class);
		startActivity(intent);
		finish();
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
