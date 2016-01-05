package com.cloud;

/*
 * The following project is the Android app that uses bluetooth signals to determine a user's location
 * on a fixed map. Once in range, it will unlock the lock to the door.
 * 
 * Press the start button to begin the program.
 * 
 * The map is broken up into a grid.
 * Phase 0 is bluetooth module 1
 * Phase 1 is bluetooth module 2
 * Phase 2 is bluetooth module 3
 * Phase 3 is bluetooth module 4
 * 
 * Each Phase has two sections.
 * 
 * Code was referenced from both the bluetooth sample code linked by the TA as well as the RFO Basic source code.
 * The RFO Basic source code uses the BluetoothChatService sample code
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Map extends Activity implements OnInitListener {

	double device1current, device1past, device2current, device2past,
			device3current, device3past, device4current, device4past,
			devicelock, devicelockpast;
	double px, py = 0; //users location

	int phase = 0;
	int subphase = 0;
	int refreshCnt = 0;
	int refreshCnt2 = 0;
	BluetoothDevice lock;
	BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
	BluetoothSocket mmSocket;

	InputStream inStream;
	DataInputStream dataIS;
	DataOutputStream dataOS;
	OutputStream outStream;
	ConnectedThread read;
	Timer pClock;
	ImageView location;
	ImageView destination;
	ImageView left,down;
	ImageView success;
	String endpoint;
	private TextToSpeech tts;
	String message;
	List<Vertex> path;
	String roomNo;
	// Necessary UUID number to connect to the lock
	public static final UUID id = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		Intent intnt = getIntent();
		String endpoint = intnt.getStringExtra("endpoints");
		tts = new TextToSpeech(this, this);
		roomNo  = endpoint.split("//")[1];
		Toast.makeText(getBaseContext(), roomNo, Toast.LENGTH_SHORT).show();
		message = "Your destination is room number"+roomNo;
		speakOut(message);
		location = (ImageView)findViewById(R.id.imageView2);
		destination = (ImageView)findViewById(R.id.imageView3);
		left = (ImageView)findViewById(R.id.left);
		down = (ImageView)findViewById(R.id.down);
		
		drawDestinationImage(roomNo);
		down.setX(-50);
		down.setY(300);
		location.setX(-50);
		location.setY(0);
		success = (ImageView)findViewById(R.id.imageView4);

		Dijkstra dijkstra = new Dijkstra("0",roomNo);
		
		path = dijkstra.getShortestPathTo(roomNo);
		Toast.makeText(getBaseContext(), path.toString(),Toast.LENGTH_SHORT).show();
		
		
        System.out.println("Path: " + path);
        
        
		registerReceiver(receiver, new IntentFilter(
				BluetoothDevice.ACTION_FOUND));

		Button start = (Button) findViewById(R.id.button1);
		// When "start" is pressed, the adapter searches for nearby devices
		start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		        //begin timer that periodically looks at the signal around the user
				pClock = new Timer();
				pClock.scheduleAtFixedRate(new CalcTask(), 0, 1000);
			}
		});
	}


	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
						Short.MIN_VALUE);
				String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
				System.out.println(name);
				Log.v("NAME : : : : : ", name);
				
				// If the device is the lock, save the device for future
				// conections
				if (name.equals("BlueRadios11045D")) {
					 
					device1past = device1current;
					device1current = rssi;
					System.out.println("BlueRadios11045D" + rssi);
					
					// Phase 1 Code
					// TextView rssi_msg = (TextView)
					// findViewById(R.id.textView1);
					// print the distance of the device
					// rssi_msg.setText(rssi_msg.getText() + name + " => " +
					// device1current + "dBm\n" + "Past: " + device1past +
					// " dBm\n");
				} else if (name.equals("BlueRadios11037E")) {

					// System.out.println("dev2");
					device2past = device2current;
					device2current = rssi;
					System.out.println("BlueRadios11037E" + rssi);

				} else if (name.equals("BlueRadios110331"))// ("BlueRadios110394"))
				{
					// System.out.println("dev3");
					device3past = device3current;
					device3current = rssi;
					System.out.println("BlueRadios110331" + rssi);
				} else if (name.equals("BlueRadios1102DD")) {
					//System.out.println("dev2");
					device4past = device4current;
					device4current = rssi;
					System.out.println("BlueRadios1102DD" + rssi);

				}
				// This one is the lock!!!!
				else if (name.equals("BlueRadios1104CE"))// BlueRadios110394
				{
					//System.out.println("dev2");
					devicelockpast = devicelock;
					devicelock = rssi;
					System.out.println("BlueRadios1104CE" + rssi);
					lock = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					

				}
				
			//	System.out.println("HELP!");

			/*	 TextView rssi_msg = (TextView) findViewById(R.id.textView1);
			//	 print the distance of each device found to screen
				if (refreshCnt == 25) {
					refreshCnt = 0;
					rssi_msg.setText(name + " => " + rssi + "dBm\n");
				} 
				else {
					refreshCnt++;
					rssi_msg.setText(rssi_msg.getText() + name + " => " + rssi
							+ "dBm\n");
				}
				*/
				
				
				
				
			}
			//System.out.println("HELP2");
		}
	};

	// Initiate connections to the bluetooth lock device if it exists
	public void connectToLock() {
		System.out.println("lock is null");
		if (lock != null) {
			System.out.println("lock is BlueRadios110394");
			connect();
		}
	}
	
	public void drawSuccess(){
		success.setVisibility(View.VISIBLE);
	}
	
	public void drawImage(){
		
		switch (phase){
        case 0:
                if (subphase == 0)
                {
                	
                        location.setY(0);
                }
                else
                {
                        location.setY(140);
                }
                break;
        case 1:
                if (subphase == 0)
                {
                        location.setY(280);
                }
                else
                {
                        location.setY(500);
                }
                break;
        case 2:
                if (subphase == 0)
                {
                        location.setY(650);
                }
                else
                {
                        location.setY(770);
                }
                break;
        case 3:
                if (subphase == 0)
                {
                        location.setY(900);
                }
                else
                {
                        location.setY(1060);
                }
                break;
        case 4:
                if (subphase == 0)
                	
                {
                		if("4.721".equals(roomNo)) {
                				left.setVisibility(View.VISIBLE);
                				left.setX(450);
                				left.setY(1330);
                		}
                			
            		        location.setY(1200);
                }
                else
                {
                        location.setY(1350);
                }
                break;

		}
		
		location.setVisibility(View.VISIBLE);
		
	};
	
public void drawDestinationImage(String roomNo){
		
	    if("4.417".equals(roomNo)){
               destination.setY(280);
                
        }
	    else if("4.908".equals(roomNo)){
        	destination.setY(500);
                }
	    
	    else if("4.624".equals(roomNo)){
	    	destination.setX(300);
        	destination.setY(450);
	    	}
        
	    else if("4.623".equals(roomNo)){
        	destination.setY(770);
        }
        else if("4.910".equals(roomNo)){
        	destination.setY(900);
        }
        else if("4.721".equals(roomNo)){
        	destination.setX(450);
        	destination.setY(1200);
                }
        
        else if("4.999".equals(roomNo)){
        	destination.setY(1350);
                }
           		
	    destination.setVisibility(View.VISIBLE);
	}
	
	
	public void connect() {

		BluetoothSocket tmp = null;

		// Get a BluetoothSocket for a connection with the
		// given BluetoothDevice
		try {
			// System.out.println(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			tmp = lock.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		} 
		catch (Exception e) {
			System.out.println("failed to get socket");
		}

		mmSocket = tmp;

		BTAdapter.cancelDiscovery();

		if (mmSocket == null) {
			return;
		}

		// Make a connection to the BluetoothSocket
		try {
			// This is a blocking call and will only return on a
			// successful connection or an exception
			mmSocket.connect();
		} catch (Exception e) {
			// Close the socket
			System.out.println("Failed at connecting.");
			try {
				mmSocket.close();
			} catch (Exception e2) {
				System.out.println("Failed to close.");
			}
			return;
		}
		System.out.println("You are Connected!!!!!");
		writeUnlock();

	}

	// Obtains the input and output streams and proceeds to write the commands
	// to unlock the lock for a brief time
	public void writeUnlock() {
		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		// Get the BluetoothSocket input and output streams
		try {
			tmpIn = mmSocket.getInputStream();
			tmpOut = mmSocket.getOutputStream();
		} catch (Exception e) {
			System.out.println("Streams not created.");
		}

		inStream = tmpIn;
		dataIS = new DataInputStream(inStream);
		outStream = tmpOut;
		dataOS = new DataOutputStream(outStream);
		read = new ConnectedThread(mmSocket);
		read.start();

		try

		{
			Thread.sleep(2000);

			String putstr = "put 19 10";

			if (putstr.length() > 0) {
				// Get the message bytes and tell write them
				byte[] send = new byte[putstr.length() + 1];
				int k = 0;
				for (k = 0; k < putstr.length(); ++k) {
					send[k] = (byte) putstr.charAt(k);
				}
				// appending newline at the end is necessary for communication
				// with the device
				send[k] = (byte) '\n';
				write(send);

			}

			Thread.sleep(2000);
			putstr = "put 2 100";
			if (putstr.length() > 0) {

				byte[] send = new byte[putstr.length() + 1];
				int k = 0;
				for (k = 0; k < putstr.length(); ++k) {
					send[k] = (byte) putstr.charAt(k);

				}
				send[k] = (byte) '\n';
				write(send);
			}
			Thread.sleep(2000);
			putstr = "put 19 90";
			if (putstr.length() > 0) {

				byte[] send = new byte[putstr.length() + 1];
				int k = 0;
				for (k = 0; k < putstr.length(); ++k) {
					send[k] = (byte) putstr.charAt(k);

				}
				send[k] = (byte) '\n';
				write(send);
			}
			Thread.sleep(2000);
			putstr = "put 2 0";

			if (putstr.length() > 0) {

				byte[] send = new byte[putstr.length() + 1];
				int k = 0;
				for (k = 0; k < putstr.length(); ++k) {
					send[k] = (byte) putstr.charAt(k);

				}
				send[k] = (byte) '\n';
				write(send);
			}
			Thread.sleep(2000);
		} catch (Exception e) {
			System.out.println("Sleeping error");
		}

	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object

		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			r = read;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	// calculates where we are on the map based on signal strength
	void calculateDistance() throws IOException {
		System.out.println("Calculating...");

		switch (phase) {
		case 0: {
			double device1avg = 0;
			if (device1current != 0 && device1past != 0)
				device1avg = (device1current + device1past) / 2;
			Log.v("PHASE 0 ",String.valueOf(device1avg));

			if (device1avg <= -60 && device1avg != 0 && subphase == 0) {
				subphase++;
			}
			else if (device1avg <= -50 && device1avg != 0 && subphase == 1) {
				phase++;
				subphase = 0;
			}
			break;
		}
		case 1: {
			double device2avg = 0;
			if (device2current != 0 && device2past != 0)
				device2avg = (device2current + device2past) / 2;
			Log.v("PHASE 1 ",String.valueOf(device2avg));
			if (device2avg >= -65 && device2avg != 0 && subphase == 0)
				subphase++;
			else if (device2avg <= -52 && device2avg != 0 && subphase == 1) {
				phase++;
				subphase = 0;
			}
			break;
		}
		case 2: {
			double device3avg = 0;
			if (device3current != 0 && device3past != 0)
				device3avg = (device3current + device3past) / 2;
			Log.v("PHASE 2 ",String.valueOf(device3avg));
			
			if (device3avg >= -58 && device3avg != 0 && subphase == 0)
				subphase++;
			else if (device3avg <= -52 && device3avg != 0 && subphase == 1) {
				
				phase++;
				subphase = 0;
			}
			break;
		}
//		case 2: {
//			double device3avg = 0;
//			if (device3current != 0 && device3past != 0)
//				device3avg = (device3current + device3past) / 2;
//			Log.v("PHASE 2 ",String.valueOf(device3avg));
//		 if (device3avg <= 0 ) {
//				phase++;
//				subphase = 0;
//			}
//			break;
//		}
		case 3: {
			double device4avg = 0;
			if (device4current != 0 && device4past != 0)
				device4avg = (device4current + device4past) / 2;
			Log.v("PHASE 3 ",String.valueOf(device4avg));
			
			if (device4avg >= -62 && device4avg != 0 && subphase == 0)
				subphase++;
			else if (device4avg <= -53 && device4avg != 0 && subphase == 1) {
				phase++;
				subphase = 0;
			}
			break;
		}
		case 4: {
			double devicelockavg = 0;
			if (devicelock != 0 && devicelockpast != 0)
				devicelockavg = (devicelock + devicelockpast) / 2;
			Log.v("PHASE 4 ",String.valueOf(devicelockavg));
			if (devicelockavg >= -58 && devicelockavg != 0 && subphase == 0)
				subphase++;
			else if (devicelockavg <= -52 && devicelockavg != 0 && subphase == 1) {
				
				runOnUiThread(
			    		new Runnable() {
			    			public void run(){
			    				drawSuccess();		
			    			}
			    		});
				
				
				subphase = 0;
				BTAdapter.cancelDiscovery();
				connectToLock();
				phase = 5;
				pClock.cancel();
				mmSocket.close();

			}
			break;
		}
		default:
			break;
		}
		
		 runOnUiThread(
		    		new Runnable() {
		    			public void run(){
		    				drawImage();		
		    			}
		    		});

		System.out.println("phase: " + phase + "." + subphase);

	}

	

	// }
	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	class ConnectedThread extends Thread {

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (Exception e) {
			}

			inStream = tmpIn;
			dataIS = new DataInputStream(inStream);
			outStream = tmpOut;
			dataOS = new DataOutputStream(outStream);
		}

		public void run() {
			byte[] buffer = new byte[1024];

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					dataIS.read(buffer);
					// System.out.println(new String(buffer));
				} catch (Exception e) {
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {

				synchronized (dataOS) { // prevent overlapping writes
					dataOS.write(buffer);
				}

			} catch (Exception e) {
				System.out.println("write fail " + e.getMessage());
			}
		}
	}

	// This task times the discovery of bluetooth devices around the user
	class CalcTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			try {
				calculateDistance();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (BTAdapter.isDiscovering()){
				BTAdapter.cancelDiscovery();
			}
			BTAdapter.startDiscovery();
		}

	}

	@Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			 
            int result = tts.setLanguage(Locale.US);
 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut(message);
            }
 
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
	}
	 private void speakOut(String message) {
		    tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
	    }
}
