package edu.cloud.iot.reception.ocr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import edu.cloud.iot.reception.facepp.http.HttpRequests;
import edu.cloud.iot.reception.facepp.http.PostParameters;

import java.io.ByteArrayOutputStream;
import java.util.Timer;

/**
 * Created by Arunkumar on 3/22/14.
 */
public class AsyncFaceRecognition extends AsyncTask<String, Void, String> {
	// public String frApiKey = "2682be8a2bda45e8bc324326c3c1402f",
	// frApiSecret="1d483a7f2c6e477ab7d1aa40853c3179",frUids="iot.project01@iotproject";
	public String frApiKey = "47f78a13fa19b187fd3495058e637ec6",
			frApiSecret = "qBzGTrikLpm7c3EjhAObZKftdd45qCuk",
			frUids = "iot.project01@iotproject";
	byte[] baseFile, compareFile;
	JSONObject jsonResponse;
	FaceRecognitionActivity parent;

	Bitmap img1, img2;
	byte[] bimg1, bimg2;
	String file1 = "file1.jpg", file2 = "file2.jpg";
	String fileName1, fileName2, fileUrl1, fileUrl2;
	JSONObject result2 = null, result1 = null, result = null;
	double dSimilarity = 0.0;
	// private byte[] imageBytes, licenseBytes,compareImageBytes;
	String asyncState = "";
	Timer timer;

	// Getter for response - json object
	public JSONObject getResponse() {
		return jsonResponse;
	}

	// Setter for parent
	public void setParent(FaceRecognitionActivity pParent) {
		this.parent = pParent;
	}

	// Setter for Base image
	public void setBaseFile(byte[] img) {
		baseFile = img;
		Log.i("EDebug::", "BaseFile Set");
	}

	// Setter for compare image
	public void setCompareFile(byte[] img) {
		compareFile = img;
		Log.i("EDebug::", "CompareFile Set");
	}

	@Override
	protected String doInBackground(String... urls) {
		boolean isNWConnected = isConnected();
		Log.i("EDebug::NetworkConnected", "Value = " + isNWConnected);
		// Toast.makeText(parent.getBaseContext(),"Verifying Identity..",Toast.LENGTH_LONG);
		faceRecognition();
		return "";
	}

	public boolean isConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) parent
				.getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;
	}

	public void faceRecognition() {
		// replace api_key and api_secret here (note)
		asyncState = "processing";
		// TimerTask task = new TimerTask() {
		// public void run() {
		// if (asyncState.equalsIgnoreCase("success")) {
		// Toast.makeText(parent,"Completed verifying identity",Toast.LENGTH_LONG);
		// timer.cancel();
		// } else {
		// if(asyncState.equalsIgnoreCase("error")){
		// Toast.makeText(parent,"Error occurred, please try again.",Toast.LENGTH_LONG);
		// timer.cancel();
		// }
		// if(asyncState.equalsIgnoreCase("processing")){
		// Toast.makeText(parent,"verifying identity..",Toast.LENGTH_LONG);
		// }
		// }
		// }
		// };
		// timer = new Timer();
		// timer.schedule(task, 0, Toast.LENGTH_LONG);
		HttpRequests httpRequests = new HttpRequests(
				"2ba3c8e89a46e2208897747252a8b573",
				"cyFYV6zF2-rEtEh6i8jnH245233Elwnj", true, false);

		try {
			// img1 = ImageIO.read(new File(file1));
			// img2 = ImageIO.read(new File(file2));
			// ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
			// ImageIO.write(img1, "jpeg", bos1);
			// bimg1 = bos1.toByteArray();
			//
			// ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
			// ImageIO.write(img2, "jpeg", bos2);
			// bimg2 = bos2.toByteArray();
			try {
				// parent.licenseImgPath = "dule00.jpg";
				// parent.captureImgPath = "dule02.jpg";
				Bitmap baseImg = BitmapFactory
						.decodeFile(String.valueOf(Environment
								.getExternalStoragePublicDirectory((Environment.DIRECTORY_PICTURES
										+ "/IOT/" + parent.licenseImgPath))));
				baseImg = toGrayscale(baseImg);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				baseImg.compress(parent.bitmapFormat, 100, bos);
				baseFile = bos.toByteArray();
				bos.flush();
				bos.close();
				Log.i("EDebug::baseImageBytes = ", "" + baseFile.length + "::"
						+ parent.licenseImgPath);

				Bitmap compImg = BitmapFactory
						.decodeFile(String.valueOf(Environment
								.getExternalStoragePublicDirectory((Environment.DIRECTORY_PICTURES
										+ "/IOT/" + parent.captureImgPath))));
				compImg = toGrayscale(compImg);
				ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
				compImg.compress(parent.bitmapFormat, 100, bos2);
				compareFile = bos2.toByteArray();
				bos2.flush();
				bos2.close();
				Log.i("EDebug::compareImageBytes = ", "" + compareFile.length
						+ "::" + parent.captureImgPath);
			} catch (Exception ex) {
				Log.e("EDebug::OnCreate Error - ",
						"Error::" + ex.getLocalizedMessage());
			}
			Log.i("EDebug::ResponseOutput_async",
					"======================================================================================");
			result1 = httpRequests.detectionDetect(new PostParameters()
					.setPersonId(file1).setImg(baseFile));
			Log.i("EDebug::result1", result1.toString(1));
			result2 = httpRequests.detectionDetect(new PostParameters()
					.setPersonId(file2).setImg(compareFile));
			Log.i("EDebug::result2", result2.toString(1));
			// System.out.println(result1.toString(1));
			// System.out.println(result1.getJSONArray("face").getJSONObject(0).getString("face_id"));
			// System.out.println(result2.getJSONArray("face").getJSONObject(0).getString("face_id"));
			Log.i("EDebug::ResponseOutput_async",
					"======================================================================================");
			result = httpRequests.recognitionCompare(new PostParameters()
					.setFaceId1(
							result1.getJSONArray("face").getJSONObject(0)
									.getString("face_id")).setFaceId2(
							result2.getJSONArray("face").getJSONObject(0)
									.getString("face_id")));
			// new
			// PostParameters().setPersonId("iotperson").setFaceId("iotface1").setImg(bimg1,
			// fileName1);
			System.out.println();
			System.out.println(result.toString(1));
			Log.i("EDebug::ResponseOutput_async", result.toString(1));

			if (result.has("similarity")) {
				System.out.println(result.getDouble("similarity"));
				dSimilarity = result.getDouble("similarity");
			} else {
				System.out.println(result.getDouble("similarity"));
				dSimilarity = 0.0;
			}
			asyncState = "success";
			// System.out.println(httpRequests.personRemoveFace(new
			// PostParameters().setFaceId(result1.getJSONArray("face").getJSONObject(0).getString("face_id"))));
			// System.out.println(httpRequests.personDelete(new
			// PostParameters().setFaceId(result2.getJSONArray("face").getJSONObject(0).getString("face_id"))));
			// result1 = httpRequests.detectionDetect(new
			// PostParameters().setPersonId(file1).setFaceId(result2.getJSONArray("face").getJSONObject(0).getString("face_id")));

			// httpRequests.personAddFace(new
			// PostParameters().setPersonId("iotperson").setFaceId("iotface2").setImg(bimg2,
			// fileName2));
			// System.out.println(httpRequests.recognitionCompare(new
			// PostParameters().setFaceId1("iotface1").setFaceId2("iotface2")));

		} catch (Exception ex) {
			asyncState = "error";
			Log.i("EDebug::ResponseError", "ERROR- " + ex.getLocalizedMessage());
			ex.printStackTrace();
		}
	}

	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(String result) {
		String msg = "Face Not Verified..!";
		if (dSimilarity > 50) {
			msg = "Face verification Successful..";
		}

		Toast.makeText(parent.getBaseContext(), msg, Toast.LENGTH_LONG).show();
		// etResponse.setText(result);
		parent.strResponse = result;
		Log.i("EDebug::strResponse", result);
		if (!asyncState.equalsIgnoreCase("processing")) {
			parent.postFaceMatchRequest();
		}

	}

	public Bitmap toGrayscale(Bitmap bmpOriginal) {
		final int height = bmpOriginal.getHeight();
		final int width = bmpOriginal.getWidth();

		final Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		final Canvas c = new Canvas(bmpGrayscale);
		final Paint paint = new Paint();
		final ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		final ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

}
