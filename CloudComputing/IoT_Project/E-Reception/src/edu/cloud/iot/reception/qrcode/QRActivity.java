package edu.cloud.iot.reception.qrcode;



import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import edu.cloud.iot.reception.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class QRActivity extends Activity{

	
	 String  URL = 
		        "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=";
	 String content  = null;
		    ImageView imageView;
		 
		    /** Called when the activity is first created. */
		    @Override
		    public void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);
		        setContentView(R.layout.activity_qr_generated);
		        imageView = (ImageView) findViewById(R.id.qrimage);
		        Intent intent = getIntent();
		        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		       content = intent.getExtras().getString("location");
		       Toast.makeText(QRActivity.this,content, Toast.LENGTH_LONG).show();
		        if(content != null){
		        	URL = URL + content;       	
		        }
		        else {
		        URL = URL+"scanagain";
		        }
		        // Create an object for subclass of AsyncTask
		        GetXMLTask task = new GetXMLTask();
		        // Execute the task
		        task.execute(new String[] { URL });
		    }
		 
		    private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {
		        @Override
		        protected Bitmap doInBackground(String... urls) {
		            Bitmap map = null;
		            for (String url : urls) {
		                map = downloadImage(url);
		            }
		            return map;
		        }
		 
		        // Sets the Bitmap returned by doInBackground
		        @Override
		        protected void onPostExecute(Bitmap result) {
		            imageView.setImageBitmap(result);
		        }
		 
		        // Creates Bitmap from InputStream and returns it
		        private Bitmap downloadImage(String url) {
		            Bitmap bitmap = null;
		            InputStream stream = null;
		            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		            bmOptions.inSampleSize = 1;
		 
		            try {
		                stream = getHttpConnection(url);
		                bitmap = BitmapFactory.
		                        decodeStream(stream, null, bmOptions);
		                stream.close();
		            } catch (IOException e1) {
		                e1.printStackTrace();
		            }
		            return bitmap;
		        }
		 
		        // Makes HttpURLConnection and returns InputStream
		        private InputStream getHttpConnection(String urlString)
		                throws IOException {
		            InputStream stream = null;
		            URL url = new URL(urlString);
		            URLConnection connection = url.openConnection();
		 
		            try {
		                HttpURLConnection httpConnection = (HttpURLConnection) connection;
		                httpConnection.setRequestMethod("GET");
		                httpConnection.connect();
		 
		                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
		                    stream = httpConnection.getInputStream();
		                }
		            } catch (Exception ex) {
		                ex.printStackTrace();
		            }
		            return stream;
		        }
		    }
		    
			public void goExit(View view) {
				System.exit(0);
				int pid = android.os.Process.myPid();
				android.os.Process.killProcess(pid);
				finish();
			}

}