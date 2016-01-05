package edu.cloud.iot.reception.ocr;

/**
 * Created by Arunkumar on 3/16/14.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;
import static android.hardware.Camera.PictureCallback;

public class CameraSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {
	// Global variables
	private SurfaceHolder sHolder;
	public Camera camera = null;
	// Camera.CameraInfo.
	int cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
	boolean firstCall = true;
	Activity parent;
	boolean frontCameraRequired = false;

	public CameraSurfaceView(Context context) {
		super(context);
		sHolder = getHolder();
		sHolder.addCallback(this);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		Log.i("EDebug::CameraSurface:Open ",
				Camera.CameraInfo.CAMERA_FACING_FRONT + "::"
						+ Build.VERSION.SDK_INT);
		Log.i("EDebug::CameraDebug", "" + Camera.getNumberOfCameras());
		Log.i("EDebug::CameraDebug", "" + Camera.CameraInfo.CAMERA_FACING_FRONT);
		if (Camera.getNumberOfCameras() > 3 && frontCameraRequired) {
			cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
		} else {
			cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		camera = Camera.open(cameraID);// Camera.CameraInfo.CAMERA_FACING_FRONT);
		Log.i("EDebug::camera", "" + camera + "::"
				+ camera.getParameters().getSupportedFocusModes().toString());
		// get Camera parameters
		if (cameraID != Camera.CameraInfo.CAMERA_FACING_FRONT
				&& Camera.getNumberOfCameras() > 1) {
			try {
				Camera.Parameters params = camera.getParameters();
				// set the focus mode
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				// params.setFocusMode(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
				// set Camera parameters
				camera.setParameters(params);
			} catch (Exception ex) {
				Log.e("EDebug::surfaceCreated Error - ",
						"Error::" + ex.getLocalizedMessage() + "::"
								+ Log.getStackTraceString(ex));
			}
		}
		if (firstCall) {
			setCameraDisplayOrientation(parent, cameraID);
			// camera.setDisplayOrientation(90);
			firstCall = false;
		}

		try {

			camera.setPreviewDisplay(sHolder);
		} catch (Exception ex) {
			ex.getMessage();
		}
		AutoFocusCallBackImpl autoFocusCallBack = new AutoFocusCallBackImpl();
		capturePic = false;
		// camera.autoFocus(autoFocusCallBack);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void setCameraDisplayOrientation(Activity activity, int cameraId) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
			int width, int height) {
		// Camera.Parameters param = camera.getParameters();
		// param.setPreviewSize(width, height);
		// camera.setParameters(param);
		// camera.startPreview();
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.
		Camera.Parameters mParameters = camera.getParameters();
		List<Camera.Size> sizes = mParameters.getSupportedPictureSizes();
		Camera.Size optimalSize = getOptimalSize(sizes, width, height);
		setCameraDisplayOrientation(parent, cameraID);
		if (optimalSize != null
				&& !mParameters.getPictureSize().equals(optimalSize))
			mParameters.setPictureSize(optimalSize.width, optimalSize.height);

		if (sHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			camera.stopPreview();
			Log.i("EDebug::CheckPreview", "Preview Stopped");
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			camera.setPreviewDisplay(sHolder);
			camera.startPreview();

		} catch (Exception e) {
			Log.d("EDebug::" + this.getClass().getName(),
					"Error starting camera preview: " + e.getMessage());
		}
		AutoFocusCallBackImpl autoFocusCallBack = new AutoFocusCallBackImpl();
		// camera.Parameters.
		capturePic = false;
		// camera.autoFocus(autoFocusCallBack);

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		try {
			camera.stopPreview();
		} catch (Exception ex) {
			Log.e("EDebug::ScanLicense: SurfaceDestroyed ",
					"" + ex.getLocalizedMessage());
		}
		camera = null;
	}

	PictureCallback jpegHandler;
	boolean capturePic = false;

	public boolean capture(PictureCallback jpegHandler) {
		// camera.setDisplayOrientation(0);
		// start autofocus if it was-not started
		this.jpegHandler = jpegHandler;
		camera.takePicture(null, null, jpegHandler);
		AutoFocusCallBackImpl autoFocusCallBack = new AutoFocusCallBackImpl();
		// camera.Parameters.
		capturePic = true;
		// /camera.autoFocus(autoFocusCallBack);

		return true;
	}

	private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {

		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return optimalSize;

	}

	private class AutoFocusCallBackImpl implements Camera.AutoFocusCallback {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			// bIsAutoFocused = success; //update the flag used in onKeyDown()
			Log.i("EDebug::TAG", "Inside autofocus callback. autofocused="
					+ success);
			if (capturePic) {
				// camera.takePicture(null,null,jpegHandler);
			}// play the autofocus sound
				// MediaPlayer.create(FaceRecognitionActivity.this,
				// android.R.raw.auto_focus).start();
		}
	}
}
