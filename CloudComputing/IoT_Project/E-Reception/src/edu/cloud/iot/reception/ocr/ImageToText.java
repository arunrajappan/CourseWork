package edu.cloud.iot.reception.ocr;

/**
 * Created by Arunkumar on 3/22/14.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * 
 * @author Arunkumar
 */
public class ImageToText {

	Bitmap orgImage, grayImage;
	// Name of application you created
	public static final String APPLICATION_ID = "iot.project05";
	// Password should be sent to your e-mail after application was created
	public static final String PASSWORD = "eJAEUgxeOdidz1r+0DVNfigN ";
	byte[] bOrgImage, bGrayImage;
	String serverUrl = "http://cloud.ocrsdk.com", taskId = "recognize";
	String language = "English", outputFormat = "txt";
	ArrayList<String> ocrResponse;
	String urlParams = String.format("language=%s&exportFormat=%s", language,
			outputFormat);
	String sResponse;
	Task task1;
	ScanLicense parent;
	boolean imageProcessed = false;

	public ImageToText() {

	}

	public String getResponseString() {
		return sResponse;
	}

	public boolean processCompleted() {
		return imageProcessed;
	}

	public ArrayList<String> startOCR(byte[] pImg) {
		imageProcessed = false;
		this.bOrgImage = pImg;
		ocrResponse = null;
		ocrResponse = new ArrayList<String>();
		Log.i("EDebug::ImageToText || startOCR() ", "Init OCR..");
		InputStream in = new ByteArrayInputStream(bOrgImage);
		try {
			// orgImage = BitmapFactory.decodeByteArray(bOrgImage, 0,
			// bOrgImage.length);
			// // ImageFilter filter = new GrayFilter(true, 50);
			// // ImageProducer producer = new
			// FilteredImageSource(orgImage.getSource(), filter);
			// // Image outImg =
			// Toolkit.getDefaultToolkit().createImage(producer);
			// // grayImage = new BufferedImage(outImg.getWidth(null),
			// outImg.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
			//
			// grayImage = toGrayscale(orgImage);
			// ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// grayImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			// bGrayImage = bos.toByteArray();
			bGrayImage = bOrgImage;
			// System.out.println(bGrayImage.length);
			// bos.flush();
			// bos.close();
			Log.i("EDebug::ImageToText || originalImageSize = ",
					bOrgImage.length + "::grayImageSize = " + bGrayImage.length);
			performRecognition();
			imageProcessed = true;

		} catch (Exception ex) {
			Log.e("EDebug::ImageToText Error - ",
					"Error::" + ex.getLocalizedMessage() + "::"
							+ ex.getMessage() + "::"
							+ Log.getStackTraceString(ex));
			imageProcessed = false;
			// Logger.getLogger(ImageToText.class.getName()).log(Level.SEVERE,
			// null, ex);
		}
		imageProcessed = true;
		return ocrResponse;

	}

	private void performRecognition() throws Exception {
		Task task = null;
		task = processImage();
		task1 = task;
		waitAndDownloadResult(task1);

	}

	/**
	 * Convert bitmap to the grayscale
	 * 
	 * @param bmpOriginal
	 *            Original bitmap
	 * @return Grayscale bitmap
	 */
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

	// see http://androidsnippets.com/convert-bitmap-to-grayscale

	/**
	 * Wait until task processing finishes
	 */
	private Task waitForCompletion(Task task) throws Exception {
		while (task.isTaskActive()) {
			Thread.sleep(2000);

			System.out.println("Waiting..");
			task = getTaskStatus(task.Id);
		}
		return task;
	}

	public void setParent(ScanLicense pParent) {
		parent = pParent;
	}

	/**
	 * Wait until task processing finishes and download result.
	 */
	private void waitAndDownloadResult(Task task) throws Exception {
		task = waitForCompletion(task);

		if (task.Status == Task.TaskStatus.Completed) {
			System.out.println("Downloading..");
			downloadResult(task);
			System.out.println("Ready");
		} else if (task.Status == Task.TaskStatus.NotEnoughCredits) {
			System.out.println("Not enough credits to process document. "
					+ "Please add more pages to your application's account.");
		} else {
			System.out.println("Task failed");
		}

	}

	public Task getTaskStatus(String taskId) throws Exception {
		URL url = new URL(serverUrl + "/getTaskStatus?taskId=" + taskId);

		HttpURLConnection connection = openGetConnection(url);
		return getResponse(connection);
	}

	public void downloadResult(Task task) throws Exception {
		if (task.Status != Task.TaskStatus.Completed) {
			throw new IllegalArgumentException("Invalid task status");
		}
		System.out.println("task.DownloadUrl" + task.DownloadUrl);
		if (task.DownloadUrl == null) {
			throw new IllegalArgumentException(
					"Cannot download result without url");
		}

		URL url = new URL(task.DownloadUrl);
		URLConnection connection = url.openConnection(); // do not use
		// authenticated
		// connection

		BufferedInputStream reader = new BufferedInputStream(
				connection.getInputStream());

		// FileOutputStream out = new FileOutputStream("E:\\Proj2\\result.txt");

		byte[] data = new byte[1024];
		int count;
		sResponse = "Response: ";
		// Systemr.out.println(new String(reader, "UTF-8"));
		while ((count = reader.read(data)) != -1) {
			// out.write(data, 0, count);
			String line = new String(data, 0, count);

			sResponse = sResponse + line;
			System.out.println(ocrResponse);
			StringTokenizer st = new StringTokenizer(line, "\r\n");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				ocrResponse.add(token);
				Log.i("EDebug::ImageToText || OCRResponse: ", token);
			}
		}

	}

	public Task processImage() throws Exception {
		Task task = null;
		URL url = new URL(serverUrl + "/processImage?" + urlParams);
		HttpURLConnection connection = openPostConnection(url);

		connection.setRequestProperty("Content-Length",
				Integer.toString(bGrayImage.length));
		connection.getOutputStream().write(bGrayImage);

		return getResponse(connection);

	}

	private HttpURLConnection openPostConnection(URL url) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		setupAuthorization(connection);
		connection
				.setRequestProperty("Content-Type", "applicaton/octet-stream");

		return connection;
	}

	private HttpURLConnection openGetConnection(URL url) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		// connection.setRequestMethod("GET");
		setupAuthorization(connection);
		return connection;
	}

	private void setupAuthorization(URLConnection connection) {
		String authString = "Basic: " + encodeUserPassword();
		authString = authString.replaceAll("\n", "");
		connection.addRequestProperty("Authorization", authString);
	}

	private String encodeUserPassword() {
		String toEncode = APPLICATION_ID + ":" + PASSWORD;
		return Base64.encode(toEncode);
	}

	/**
	 * Read server response from HTTP connection and return task description.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	private Task getResponse(HttpURLConnection connection) throws Exception {
		int responseCode = connection.getResponseCode();

		Log.i("EDebug::responseCode = ", responseCode + "");
		if (responseCode == 200) {
			InputStream inputStream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			return new Task(reader);
		} else if (responseCode == 401) {
			throw new Exception(
					"HTTP 401 Unauthorized. Please check your application id and password");
		} else if (responseCode == 407) {
			throw new Exception("HTTP 407. Proxy authentication error");
		} else {
			String message = "";
			try {
				InputStream errorStream = connection.getErrorStream();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(errorStream));

				// Parse xml error response
				InputSource source = new InputSource();
				source.setCharacterStream(reader);
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(source);

				NodeList error = doc.getElementsByTagName("error");
				Element err = (Element) error.item(0);

				message = err.getTextContent();
			} catch (Exception e) {
				throw new Exception("Error getting server response");
			}

			throw new Exception("Error: " + message);
		}
	}

	public static void main(String[] args) {
		byte[] bImage;
		// try {
		// BufferedImage bi = ImageIO.read(new File("E:\\Proj2\\License4.jpg"));
		// ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// ImageIO.write(bi, "jpg", bos);
		// bos.flush();
		// bImage = bos.toByteArray();
		// bos.close();
		// ImageToText it = new ImageToText(bImage);
		//
		// } catch (Exception ex) {
		// Logger.getLogger(ImageToText.class.getName()).log(Level.SEVERE, null,
		// ex);
		// }
	}

}
