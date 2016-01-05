package edu.cloud.iot.reception.ocr;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Arunkumar on 3/22/14.
 */
public class AsyncOCR extends AsyncTask<String, Void, String> {
	ImageToText itOCR;
	ArrayList<String> ocrResponse;
	byte[] bImage;
	ScanLicense parent;
	String sResponse;

	public void setScanImage(byte[] pImg) {
		this.bImage = pImg;
	}

	public void setParent(ScanLicense pParent) {
		parent = pParent;
	}

	public ArrayList<String> getOcrResponse() {
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
		return ocrResponse;

	}

	public boolean processCompleted() {
		return itOCR.processCompleted();
	}

	public AsyncOCR() {
		itOCR = null;
		itOCR = new ImageToText();
	}

	public String getOcrResponseString() {
		return sResponse;
	}

	@Override
	protected String doInBackground(String... strings) {
		ocrResponse = null;

		itOCR.setParent(parent);
		// sResponse = itOCR.getResponseString();
		// Toast.makeText(parent.getBaseContext(),sResponse,Toast.LENGTH_LONG);
		// parent.setDebugText(sResponse);
		try {
			Log.i("EDebug::Async: byteImg Length =", bImage.length + "");
			ocrResponse = itOCR.startOCR(bImage);
			return "Success";
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.i("EDebug::AsyncOcr: Error - ",
					"Error1: " + ex.getLocalizedMessage());
			return "Error occured while processing scanned image. ";
		}
	}

	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(String result) {

	}
}
