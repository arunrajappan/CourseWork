package edu.cloud.iot.reception.ocr;

import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import edu.cloud.iot.reception.R;
import edu.cloud.iot.reception.main.CaptureImageInstructionActivity;
import edu.cloud.iot.reception.main.GridActivity;

public class ScanLicense extends ActionBarActivity {
	// Global Variable
	private CameraSurfaceView cameraView;
	private ImageView imageResult;
	private FrameLayout frameNew;
	private Button snapPhoto;
	private boolean takePicture = true;
	private Bitmap image = null;
	private byte[] imageBytes, baseImageBytes;
	private String licenseImgPath;

	private AsyncOCR asyncOCR;
	private int SET_COMPARE_IMAGE = 01, SET_BASE_IMAGE = 02;
	String imageExt = ".jpg";
	Bitmap.CompressFormat bitmapFormat = Bitmap.CompressFormat.JPEG;
	int compressHeight = 200, compressWidth = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_license);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		setUpCamera();
		try {
			// Bitmap compImg =
			// BitmapFactory.decodeFile(String.valueOf(Environment.getExternalStoragePublicDirectory((
			// Environment.DIRECTORY_PICTURES + "/IOT/veera01.jpg"))));
			// ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// compImg.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			// baseImageBytes = bos.toByteArray();
			// bos.flush();
			// bos.close();
			// Log.i("EDebug::baseImageBytes = ",""+baseImageBytes.length);
		} catch (Exception ex) {
			Log.e("EDebug::OnCreate Error - ",
					"Error::" + ex.getLocalizedMessage() + "::"
							+ Log.getStackTraceString(ex));
		}
	}

	private void setUpCamera() {
		cameraView = new CameraSurfaceView(getApplicationContext());
		cameraView.parent = this;
		cameraView.frontCameraRequired = true;
		// cameraView.setCameraDisplayOrientation(this,0);//Camera.CameraInfo.CAMERA_FACING_BACK);
		imageResult = new ImageView(getApplicationContext());
		imageResult.setBackgroundColor(Color.GRAY);
		frameNew = (FrameLayout) findViewById(R.id.flCamera);
		snapPhoto = (Button) findViewById(R.id.bCapture);
		Log.v("FaceRecognitionActivity", "frameNew Value = " + frameNew);
		frameNew.addView(imageResult);
		frameNew.addView(cameraView);
		frameNew.bringChildToFront(cameraView);
		// asyncOCR.setParent(this);
	}

	public Camera.PictureCallback jpegHandler = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] bytes, Camera camera) {

			// bytes = baseImageBytes;
			// imageBytes = bytes;

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
			byte[] tempBytes = bos.toByteArray();
			imageBytes = bytes;
			image = BitmapFactory.decodeByteArray(tempBytes, 0,
					tempBytes.length);
			Log.i("EDebug:: bytes.length = ", bytes.length
					+ "::imageBytes.length" + imageBytes.length);

			try {
				bos.flush();
				bos.close();
				// android.graphics.Matrix mat = new android.graphics.Matrix();
				// mat.postRotate(90); // angle is the desired angle you wish to
				// rotate
				// image = Bitmap.createBitmap(image, 0, 0, image.getWidth(),
				// image.getHeight(), mat, true);
				// ByteArrayOutputStream bos = new ByteArrayOutputStream();
				// image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				// byte[] imgByte = bos.toByteArray();
				// Log.i("EDebug::ImageScan: ","Byte Image Length - "+imgByte.length);
				// //imageBytes = imgByte;
				// bos.flush();
				// bos.close();
			} catch (Exception e) {
				e.printStackTrace();
				Log.i("EDebug::PictureCallback: ", e.getLocalizedMessage() + "");
			}

			// Store License image
			int imageNum = 0;
			File imagesFolder = new File(
					String.valueOf(Environment
							.getExternalStoragePublicDirectory((Environment.DIRECTORY_PICTURES + "/IOT/"))));
			imagesFolder.mkdirs(); // <----
			String fileName = "license_" + String.valueOf(imageNum) + imageExt;
			File output = new File(imagesFolder, fileName);
			while (output.exists()) {
				imageNum++;
				fileName = "license_" + String.valueOf(imageNum) + imageExt;
				output = new File(imagesFolder, fileName);
			}
			licenseImgPath = fileName;
			Uri uriSavedImage = Uri.fromFile(output);
			OutputStream imageFileOS;
			try {
				imageFileOS = getContentResolver().openOutputStream(
						uriSavedImage);
				imageFileOS.write(tempBytes);
				imageFileOS.flush();
				imageFileOS.close();
			} catch (Exception ex) {

				ex.printStackTrace();
				Log.i("EDebug::PictureCallback",
						ex.getMessage() + "::" + Log.getStackTraceString(ex));
			}

			imageResult.setImageBitmap(image);
			frameNew.bringChildToFront(imageResult);
			snapPhoto.setText("Re-Scan");
			takePicture = false;

			// cameraView.camera.setDisplayOrientation(90);
		}
	};
	TextView debugTextView;
	String debugText;

	public void setDebugText(String txt) {
		// debugTextView = (TextView)findViewById(R.id.debugTest);
		debugText = txt;
		Log.i("EDebug::setDebugText: ", txt + "");
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				debugTextView.setText(debugText);
			}
		});

	}

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
			Log.v("EDebug::FaceRecognitionActivity", ex.getMessage() + "");
		}
	}

	public void captureHandler(View view) {
		if (takePicture) {
			//
			// Toast.makeText(getBaseContext(), "Capturing Image..",
			// Toast.LENGTH_LONG).show();
			cameraView.capture(jpegHandler);
			// cameraView = null;
			// new HttpAsyncTask().execute(this);
			// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// startActivityForResult(intent, 0);
		} else {
			takePicture = true;
			// imageResult.setImageBitmap(null);
			cameraView.camera.startPreview();

			frameNew.bringChildToFront(cameraView);
			snapPhoto.setText("Scan");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			Bitmap img = (Bitmap) data.getExtras().get("data");
			imageResult.setImageBitmap(img);
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
		boolean processedImage = false;
		// call AsynTask to perform network operation on separate thread
		try {
			asyncOCR = new AsyncOCR();
			asyncOCR.setParent(this);
			// asyncOCR = new AsyncOCR();
			// asyncOCR.setParent(this);
			// asyncFaceRecog.setBaseFile(imageBytes);
			// asyncFaceRecog.setCompareFile(imageBytes);
			Log.i("EDebug::ResponseOutput", "Triggering OCR service..");
			// Log.i("ImageByteArray", "Image --> "+imageBytes.length);
			asyncOCR.setScanImage(imageBytes);
			Toast.makeText(getBaseContext(), "Processing image..",
					Toast.LENGTH_LONG).show();
			asyncOCR.execute("");
			while (!asyncOCR.processCompleted()) {
				Log.i("EDebug::OCRProcessStatus: ", "Waiting for Async");
				Toast.makeText(getBaseContext(), "Processing image..",
						Toast.LENGTH_LONG).show();
				Thread.sleep(2000);// Toast.LENGTH_LONG) ;
			}
			processedImage = true;
			// jsonResponse = asyncFaceRecog.getResponse();

			// Log.i("ResponseOutput", jsonResponse.toString(1));
		} catch (Exception ex) {
			Log.i("EDebug::OCRError", "ERROR" + ex.getLocalizedMessage());
			ex.printStackTrace();
		}
		Log.i("EDebug::nextHander processImage = ", "" + processedImage);
		asyncOCR.itOCR.imageProcessed = true;
		if (asyncOCR.itOCR.imageProcessed) {
			String status = "completed";
			// Log.i("EDebug::nextHander status = ",
			// ""+asyncOCR.itOCR.task1.Status);
			if (status.equalsIgnoreCase("completed")) {
				try {

					Intent faceRecogIntent = new Intent(this,
							CaptureImageInstructionActivity.class);
					// faceRecogIntent.putExtra("Check1","Data1");
					Bundle fwdMsg = new Bundle();

					// ArrayList<String> al = (new ArrayList<String>());
					// al.add("Data2");
					// fwdMsg.putSerializable("al", al);
					fwdMsg.putSerializable("ocrresponse",
							asyncOCR.getOcrResponse());
					// Log.i("EDebug::ocrresponse = ",asyncOCR.getOcrResponse().toString());
					fwdMsg.putByteArray("licensebytes", imageBytes);
					fwdMsg.putString("licenseImgPath", licenseImgPath);
					faceRecogIntent.putExtra("list", fwdMsg);
					// Log.i("EDebug::","Starting FaceRecog Activity");
					if (cameraView.camera != null) {
						cameraView.camera.stopPreview();
						cameraView.camera.release();
						cameraView.camera = null;
						cameraView = null;
					}
					Log.i("Scan Activity:: OcrData::", asyncOCR
							.getOcrResponse().toString() + "");
					startActivity(faceRecogIntent);
					Log.i("EDebug::", "Started FaceRecog Activity");
				} catch (Exception ex) {
					Log.i("EDebug::ScanLicense nextClik: ",
							"Error: " + ex.getLocalizedMessage() + "::"
									+ ex.getMessage());
				}
			} else {
				Toast.makeText(this,
						"Error occured! Please process the image again.",
						Toast.LENGTH_LONG).show();
			}

		} else {
			Toast.makeText(this,
					"Error occured! Please process the image again.",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_license, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_scan_license,
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
