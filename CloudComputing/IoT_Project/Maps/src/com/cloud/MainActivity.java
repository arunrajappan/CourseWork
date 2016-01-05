package com.cloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	 public static final int REQUEST_QR_SCAN = 4;
	 TextView textContent;
	 String endpoints;
	    protected void onCreate (Bundle savedInstanceState) {
	        super.onCreate (savedInstanceState);
	        setContentView (R.layout.activity_main);

	        textContent = (TextView) findViewById (R.id.textView);
	        Button buttonIntent = (Button) findViewById (R.id.scan);
	        
	        buttonIntent.setOnClickListener (new OnClickListener () {
	            public void onClick (View v) {
	        				Intent intent = 
	                        new Intent ("com.google.zxing.client.android.SCAN");
	                startActivityForResult(Intent.createChooser (intent
	                        , "Scan with"), REQUEST_QR_SCAN);
	                
	            }
	        });
	    }
	    	    
	    public void onActivityResult (int requestCode, int resultCode
	                , Intent intent) {
	    	 if (requestCode == REQUEST_QR_SCAN) {
	    	        if (resultCode == RESULT_OK) {
	    	            String contents = intent.getStringExtra("SCAN_RESULT");
	    	            textContent.setText ("Room No :"+contents);
	    	            endpoints = contents;
	    	            Intent intnt = new Intent(getBaseContext(),Map.class);
	    		    	intnt.putExtra("endpoints", endpoints);
	    		    	startActivity(intnt);
	    	        }
	    	 }
	    }
	}

