package edu.cloud.iot.reception.ocr;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import edu.cloud.iot.reception.R;
import edu.cloud.iot.reception.main.GridActivity;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

//import java.awt.image.BufferedImage;

public class FaceRecognitionActivity extends ActionBarActivity {
	// Global Variable
	private CameraSurfaceView cameraView;
	private ImageView imageResult;
	private FrameLayout frameNew;
	private Button snapPhoto;
	private boolean takePicture = true;
	private Bitmap image = null;
	private byte[] imageBytes, licenseBytes, compareImageBytes;
	ArrayList<String> ocrResponse;
	Bundle extras;

	// Face Recognnition
	private HttpClient httpClient;
	// frApiKey = "2682be8a2bda45e8bc324326c3c1402f",
	// frApiSecret="1d483a7f2c6e477ab7d1aa40853c3179"
	// frApiKey = "4480afa9b8b364e30ba03819f3e9eff5",
	// frApiSecret="Pz9VFT8AP3g_Pz8_dz84cRY_bz8_Pz8M "
	private static String frApiKey = "4480afa9b8b364e30ba03819f3e9eff5",
			frApiSecret = "Pz9VFT8AP3g_Pz8_dz84cRY_bz8_Pz8M ",
			frUids = "iot.project01@iotproject";
	public String strResponse;
	public JSONObject jsonResponse;
	private AsyncFaceRecognition asyncFaceRecog;
	private int SET_COMPARE_IMAGE = 01, SET_BASE_IMAGE = 02;
	String licenseImgPath, captureImgPath;
	String imageExt = ".jpg";
	Bitmap.CompressFormat bitmapFormat = Bitmap.CompressFormat.JPEG;
	int compressHeight = 200, compressWidth = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		// setContentView(R.layout.fragment_camera);
		Log.i("EDebug::FaceRecog OnCreate: ", "In OnCreate");
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		setUpCamera();
		Intent intent = getIntent();
		// String message = intent.getStringExtra("Check1");
		extras = intent.getExtras();
		// message = "Check1::"+ (String) extras.get("Check1");
		Bundle b = (Bundle) extras.get("list");
		// ArrayList<String> al = (ArrayList<String>) b.get("al");
		ocrResponse = (ArrayList<String>) b.get("ocrresponse");
		licenseBytes = (byte[]) b.get("licensebytes");
		licenseImgPath = (String) b.get("licenseImgPath");

		try {
			// ocrResponse = new ArrayList<String>();
			// ocrResponse.add("﻿V& LIMITED TERM");
			// ocrResponse.add("IDENTIFICATION CARD");
			// ocrResponse.add("Lid 37998217");
			// ocrResponse.add("La iss 12/20/2013 4b Exp 10/30/2016");
			// ocrResponse.add("13 DOB 10/29/1991");
			// ocrResponse.add("1ARRABOLU");
			// ocrResponse.add("2 VEERA VENKATA RAVI TEJA");
			// ocrResponse.add("8 7777 MCCALLUM BLVD #316");
			// ocrResponse.add( "RICHARDSON TX 75252");
			// ocrResponse.add("16 Hgt 5-11	15	Sex	M	16	E");
			// ocrResponse.add("5 DD 01114301126250715756");

			// Bitmap baseImg =
			// BitmapFactory.decodeFile(String.valueOf(Environment.getExternalStoragePublicDirectory((
			// Environment.DIRECTORY_PICTURES + "/IOT/dule00.jpg"))));
			// ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// baseImg.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			// licenseBytes = bos.toByteArray();
			// bos.flush();
			// bos.close();
			// Log.i("EDebug::compareImageBytes = ",""+licenseBytes.length);
			//
			// Bitmap compImg =
			// BitmapFactory.decodeFile(String.valueOf(Environment.getExternalStoragePublicDirectory((
			// Environment.DIRECTORY_PICTURES + "/IOT/dule02.jpg"))));
			// ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
			// compImg.compress(Bitmap.CompressFormat.JPEG, 100, bos2);
			// compareImageBytes = bos2.toByteArray();
			// bos2.flush();
			// bos2.close();
			// Log.i("EDebug::compareImageBytes = ",""+compareImageBytes.length);
		} catch (Exception ex) {
			Log.e("EDebug::OnCreate Error - ",
					"Error::" + ex.getLocalizedMessage() + "::"
							+ ex.getMessage());
		}
		// Log.i("EDebug::FaceRecog OnCreate: ", message);
		Log.i("EDebug::FaceRecog OnCreate: licenseImgPath = ", ""
				+ licenseImgPath);
		Log.i("EDebug::FaceRecog OnCreate: ocrResponse=",
				"" + ocrResponse.toString());
		Log.i("EDebug::FaceRecog OnCreate: licenseBytes = ", ""
				+ licenseBytes.length);
	}

	private void setUpCamera() {
		cameraView = new CameraSurfaceView(getBaseContext());
		cameraView.parent = this;
		cameraView.frontCameraRequired = true;
		// cameraView.setCameraDisplayOrientation(this,0);//Camera.CameraInfo.CAMERA_FACING_BACK);
		imageResult = new ImageView(getApplicationContext());
		imageResult.setBackgroundColor(Color.GRAY);
		frameNew = (FrameLayout) findViewById(R.id.flCamera);
		snapPhoto = (Button) findViewById(R.id.bCapture);
		Log.v("EDebug::FaceRecognitionActivity", "frameNew Value = " + frameNew);
		frameNew.addView(imageResult);
		frameNew.addView(cameraView);
		frameNew.bringChildToFront(cameraView);

	}

	public Camera.PictureCallback jpegHandler = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] bytes, Camera camera) {
			// compareImageBytes = bytes;
			// bytes = compareImageBytes;
			try {
				image = ShrinkBitmap(bytes, compressWidth, compressHeight);
			} catch (Exception ex) {
				Log.e("EDebug::OnCreate Error - ",
						"Error::" + ex.getLocalizedMessage() + "::"
								+ ex.getMessage() + "::"
								+ Log.getStackTraceString(ex));
				BitmapFactory.Options imageOpts = new BitmapFactory.Options();
				imageOpts.inSampleSize = 2; // for 1/2 the image to be loaded
				image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
						imageOpts);
				image = Bitmap.createScaledBitmap(image, compressWidth,
						compressHeight, false);
			}

			// image = toGrayscale(image);
			android.graphics.Matrix mat = new android.graphics.Matrix();
			if (cameraView.cameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					mat.postRotate(0); // angle is the desired angle you wish to
										// rotate
				} else {
					mat.postRotate(270); // angle is the desired angle you wish
											// to rotate
				}

			} else {
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					mat.postRotate(0); // angle is the desired angle you wish to
										// rotate
				} else {
					mat.postRotate(90); // angle is the desired angle you wish
										// to rotate
				}
			}
			image = Bitmap.createBitmap(image, 0, 0, image.getWidth(),
					image.getHeight(), mat, true);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			image.compress(bitmapFormat, 100, bos);
			imageBytes = bos.toByteArray();
			// imageBytes = licenseBytes;
			Log.e("EDebug::PictureCallback 1-", "imageBytes.lenght::"
					+ imageBytes.length);

			try {
				bos.flush();
				bos.close();

			} catch (Exception ex) {
				Log.e("EDebug::PictureCallback Error - ",
						"Error::" + ex.getLocalizedMessage());
			}
			// imageBytes = bytes;
			image = BitmapFactory.decodeByteArray(imageBytes, 0,
					imageBytes.length);
			Log.e("EDebug::PictureCallback 2-", "imageBytes.lenght::"
					+ imageBytes.length);
			imageResult.setImageBitmap(image);
			frameNew.bringChildToFront(imageResult);
			snapPhoto.setText("Re-Capture");
			takePicture = false;
			// cameraView.camera.setDisplayOrientation(90);

			// Store Capture image
			int imageNum2 = 0;
			File imagesFolder2 = new File(
					String.valueOf(Environment
							.getExternalStoragePublicDirectory((Environment.DIRECTORY_PICTURES + "/IOT/"))));
			imagesFolder2.mkdirs();// <----
			String fileName2 = "image_" + String.valueOf(imageNum2) + imageExt;
			File output2 = new File(imagesFolder2, fileName2);
			while (output2.exists()) {
				imageNum2++;
				fileName2 = "image_" + String.valueOf(imageNum2) + imageExt;
				output2 = new File(imagesFolder2, fileName2);
			}
			captureImgPath = fileName2;
			Uri uriSavedImage2 = Uri.fromFile(output2);
			OutputStream imageFileOS2;
			try {
				imageFileOS2 = getContentResolver().openOutputStream(
						uriSavedImage2);
				imageFileOS2.write(imageBytes);
				imageFileOS2.flush();
				imageFileOS2.close();
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
				Log.v("EDebug::PictureCallback", ex.getMessage());
			}

		}
	};

	public void saveImage(String pPath, String pDir, Bitmap img) {
		try {
			FileOutputStream fos = new FileOutputStream(pPath + pDir);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			// Compress Bitmap Image
			image.compress(Bitmap.CompressFormat.JPEG, 50, bos);
			bos.flush();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.v("EDebug::FaceRecognitionActivity", ex.getMessage());
		}
	}

	public void captureHandler(View view) {
		if (takePicture) {
			Toast.makeText(getBaseContext(), "Capturing Image..",
					Toast.LENGTH_LONG).show();
			cameraView.capture(jpegHandler);
		} else {
			takePicture = true;
			cameraView.camera.startPreview();

			frameNew.bringChildToFront(cameraView);
			snapPhoto.setText("Capture");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			Bitmap img = (Bitmap) data.getExtras().get("data");
			imageResult.setImageBitmap(img);
		}
		if (requestCode == SET_COMPARE_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			Bitmap compImg = BitmapFactory.decodeFile(picturePath);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			compImg.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			compareImageBytes = bos.toByteArray();

			// ImageView imageView = (ImageView) findViewById(R.id.imgView);
			// imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
		}
	}

	public Bitmap toGrayscale(Bitmap bmpOriginal) {
		int height = bmpOriginal.getHeight();
		int width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);

		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	private Bitmap ShrinkBitmap(byte[] imgByte, int width, int height) {

		BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
		bmpFactoryOptions.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0,
				imgByte.length, bmpFactoryOptions);

		int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
				/ (float) height);
		int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
				/ (float) width);

		if (heightRatio > 1 || widthRatio > 1) {
			if (heightRatio > widthRatio) {
				bmpFactoryOptions.inSampleSize = heightRatio;
			} else {
				bmpFactoryOptions.inSampleSize = widthRatio;
			}
		}
		Log.i("EDebug::ShrinkBitmap() - ", "bmpFactoryOptions.inSampleSize="
				+ bmpFactoryOptions.inSampleSize);

		bmpFactoryOptions.inJustDecodeBounds = false;
		// bmpFactoryOptions.inSampleSize = 16;
		bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length,
				bmpFactoryOptions);
		return bitmap;
	}

	public void nextHandler(View view) {
		// call AsynTask to perform network operation on separate thread
		try {
			asyncFaceRecog = new AsyncFaceRecognition();
			asyncFaceRecog.setParent(this);
			// asyncFaceRecog.setBaseFile(imageBytes);
			// asyncFaceRecog.setCompareFile(imageBytes);
			Log.i("EDebug::ResponseOutput",
					"Triggering FaceRecognition service..");
			Log.i("EDebug::VisitorImageByteArray", "Visitor Image --> "
					+ imageBytes.toString());
			Log.i("EDebug::LicenseImageByteArray", "License Image --> "
					+ licenseBytes.toString());
			// asyncFaceRecog.setBaseFile(licenseBytes);
			// asyncFaceRecog.setCompareFile(imageBytes);
			asyncFaceRecog.asyncState = "processing";
			asyncFaceRecog.execute("");
			// jsonResponse = asyncFaceRecog.getResponse();

			// Log.i("ResponseOutput", jsonResponse.toString(1));
		} catch (Exception ex) {
			Log.i("EDebug::FaceRecogError", "ERROR" + ex.getLocalizedMessage());
			ex.printStackTrace();
		}
		// while(asyncFaceRecog.asyncState.equalsIgnoreCase("processing")){
		// Log.i("EDebug:: (While Loop) asyncFaceRecog.asyncState = ",asyncFaceRecog.asyncState+"");
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				while (asyncFaceRecog.asyncState.equalsIgnoreCase("processing")) {
					Log.i("EDebug:: Thread - asyncFaceRecog.asyncState = ", ""
							+ asyncFaceRecog.asyncState);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}, 500);
	}

	public void postFaceMatchRequest() {
		try {

			Log.i("EDebug:: postFaceMatchRequest():asyncFaceRecog.asyncState = ",
					asyncFaceRecog.asyncState + "");
			if (asyncFaceRecog.asyncState.equalsIgnoreCase("success")) {
				if (asyncFaceRecog.dSimilarity > 50) {
					Toast.makeText(getBaseContext(),
							"Identity verified Successfully!",
							Toast.LENGTH_LONG);
					Intent vrFormIntent = new Intent(this,
							VoiceRecognitionActivity.class);
					Bundle extra = new Bundle();
					extra.putSerializable("ocrresponse", ocrResponse);
					vrFormIntent.putExtra("list", extra);
					cameraView.camera.stopPreview();
					cameraView.camera.release();
					cameraView.camera = null;
					cameraView = null;
					startActivity(vrFormIntent);
				} else {
					Toast.makeText(getBaseContext(),
							"Identity Not verified!  Please try again.",
							Toast.LENGTH_LONG);
				}
			} else {
				Toast.makeText(
						getBaseContext(),
						"Error Occured while verifying your identity!  Please try again.",
						Toast.LENGTH_LONG);
			}

		} catch (Exception ex) {
			Log.i("EDebug:: postFaceMatchRequest():ERROR = ",
					ex.getLocalizedMessage() + "::" + ex.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_camera,
					container, false);
			return rootView;
		}
	}

	public void goHome(View view) {
		Intent intent = new Intent(this, GridActivity.class);
		startActivity(intent);
		finish();
	}

}
