
package com.example.clientonenat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import com.example.clientonenat.R;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ClientOneNAT extends Activity {

	private InetAddress clientIP;
	TextView tv,tv3;
	EditText myN,peN;
	DatagramSocket clientSocket = null;
	private int socketTimeout=10000;
	String peer;
	boolean standAlone;
	String tag = "NAT";
	Button bt;
	String clip4 = "0";
	String lport = "0";
	byte[] outBuf = new byte[2048];
	byte[] inBuf = new byte[2048];
	DatagramPacket sendPacket;
	InetAddress serverIPAddress;
	int port = 2020;
	String data = "";
	int i;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		super.onCreate(savedInstanceState);
		bt = (Button) findViewById(R.id.button1);

		setContentView(R.layout.activity_main);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void Register(View v) {
		

		starts();

	}

	public void cont(View v) {
		tv = (TextView) findViewById(R.id.textView1);
		bt = (Button) findViewById(R.id.button1);
		
	}

	
	public void starts() {
		ClientOneNAT c = new ClientOneNAT();
		tv = (TextView) findViewById(R.id.textView1);
		tv3=(TextView) findViewById(R.id.textView3);
		myN = (EditText) findViewById(R.id.editText1);
		peN = (EditText) findViewById(R.id.editText2);
		// tv.setText(myN.getText().toString());

		/*
		 * WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
		 * List<WifiConfiguration> l = wim.getConfiguredNetworks();
		 * WifiConfiguration wc = l.get(0); String
		 * add=(Formatter.formatIpAddress
		 * (wim.getConnectionInfo().getIpAddress()));
		 */

		bt = (Button) findViewById(R.id.button1);

		String add = getLocalIpAddress();
		System.out.println("Local Ip is " + add);
		c.createHole(true, add, tv,tv3, myN, peN,bt);

	}

	// public void register()

	private void createHole(boolean standAlone, String add, TextView tv,TextView tv3,
			EditText myN, EditText peN,Button bt) {
		// InetAddress serverIPAddress = InetAddress.getByName("192.168.40.1");

		try {

			// clientIP = InetAddress.getByName(add);

			// String clip4=clientIP.getHostAddress();
			// Log.i("NAT 3", add);
			// byte[] addrInByte =
			// createInternetAddressFromString(relayServerIP);//{(byte) 192,
			// (byte) 168, 40, 1};

			clientSocket = new DatagramSocket();

			add = getLocalIpAddress();

			clientIP = InetAddress.getByName(add);
			serverIPAddress = InetAddress.getByName("107.20.232.29");
			clip4 = clientIP.getHostAddress();
			lport = Integer.toString(clientSocket.getLocalPort());

			// System.out.println("Try to send: "+data+" TO: "+serverIPAddress+" ON PORT: "+port);
			// first send UDP packet to the relay server

			clientSocket.setReuseAddress(true);
			clientSocket.setSoTimeout(socketTimeout);

			System.out.println("Local Port is : " + lport);
			String myNum = "0";

			myNum = myN.getText().toString();

			// **********************************Register**************************
			data = "Register" + "," + myNum + "-" + clip4 + ";" + lport + "!";
			System.out.println("Try to Regester: " + data + " TO: "
					+ serverIPAddress + " ON PORT: " + port);
			// tv.setText("Try to Regester: "+data+" TO: "+serverIPAddress+" ON PORT: "+port);

			outBuf = data.getBytes();

			sendPacket = new DatagramPacket(outBuf, outBuf.length,
					serverIPAddress, port);

			clientSocket.send(sendPacket);

			System.out.println(" send: " + data + " TO: " + serverIPAddress
					+ " ON PORT: " + port);

			DatagramPacket receivePacket = new DatagramPacket(inBuf,
					inBuf.length);
			clientSocket.receive(receivePacket);
			String regData = null;
			// regData = new String(receivePacket.getData());

			regData = new String(receivePacket.getData(), 0,
					receivePacket.getLength());
			System.out.println("Received data " + regData);
			tv.setText("Regestered peers are " + regData);

			
		//**************************************************************	
			String peNum = "0";

			peNum = peN.getText().toString();
			
			String data = "Connect" + "," + peNum + "-" + clip4 + ";" + lport
					+ "!";
			outBuf = data.getBytes();
			sendPacket = new DatagramPacket(outBuf, outBuf.length,
					serverIPAddress, port);

			// clientSocket = new DatagramSocket();
			clientSocket.send(sendPacket);
			String peer = receivePacket(clientSocket);
			
			
			
			
			
			clientSocket.close();
			
			initSession(peer,tv,tv3);
			
			
			
			
			
			
			
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block

		} catch (SocketTimeoutException e) {
			tv.setText("Socket Timedout, Try Again");

			bt.setEnabled(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private synchronized void sendPacket(DatagramSocket socket,
			InetAddress destIPAddress, int port, String data)
			throws IOException {
		byte[] sendData = new byte[data.length()];
		sendData = data.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, destIPAddress, port);
		socket.send(sendPacket);
	}

	private synchronized String receivePacket(DatagramSocket socket)
			throws SocketTimeoutException, IOException {
		byte[] receiveData = new byte[50];
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		socket.receive(receivePacket);
		String reply = new String(receivePacket.getData(), 0,
				receivePacket.getLength());
		System.out.println(" GOT FROM SERVER:" + reply);
		return reply;
	}

	private synchronized void initSession(String peer, final TextView tv,final TextView tv3)
			throws IOException {
		String destIP = peer.substring(0, peer.indexOf(':'));
		String destPort = peer.substring(peer.indexOf(':') + 1,
				peer.indexOf('-'));
		final InetAddress destIPAddress = InetAddress.getByName(destIP);
		final int remotePort = Integer.valueOf(destPort);
		final int natPort = Integer
				.valueOf(peer.substring(peer.indexOf('-') + 1));
		// tv.append("In the Init");

		
		tv.setText("");
		tv3.setText("");
		//*******************************************
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
		    	
		    	if (i!=10) { 
		    		 String tt=(sender (destIPAddress, remotePort));
		    		tv.append(i+" "+tt +"\n");
		    		//handler.postDelayed(this, 500);
		    		tt=(receiver (natPort,tv));
		    		tv3.append(i+" "+tt+"\n");
		    		i++;
		    		
		    		if (i==5) {tv.setText("");
		        	tv3.setText("");}
		                handler.postDelayed(this, 5000);
		            }
		        }
		    }, 5000);
		
		//***************************************************
		
		
		
		
		

		
		/*  String tt; 
		  for (int i=0; i<5; i++) { 
			  tt=(sender (destIPAddress, remotePort));  
		  tv.append(tt+"\n");
		  
		 
		  tt=(receiver (natPort,tv));
		  tv.append(tt+"\n");
		 
		 } */
		 
	}

	public String receiver(int natPort, TextView tv) {
		String data = "0";
		try {
			DatagramSocket serverSocket = new DatagramSocket(natPort);
			serverSocket.setSoTimeout(socketTimeout);
			byte[] receiveData = new byte[50];
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			// System.out.println(" listening on port: " + natPort);

			serverSocket.receive(receivePacket);

			data = new String(receivePacket.getData(), 0,
					receivePacket.getLength());
			System.out.println(" ===> " + data);
			
			serverSocket.close();
		}catch (SocketTimeoutException e) {
			System.out.println("Receiver time out");
			
		} 
		catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "Received "+data;
	}

	public String sender(InetAddress destIPAddress, int port) {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(socketTimeout);
			sendPacket(clientSocket, destIPAddress, port, "Hello From Nexus ");
			System.out.println("Sent  to " + destIPAddress + " :" + port
					+ " Hello From From Nexus ");

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "Send Hello From From Nexus";
	}

	public String getLocalIpAddress() {
		String ipv4;
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					// for getting IPV4 format
					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils
									.isIPv4Address(ipv4 = inetAddress
											.getHostAddress())) {

						String ip = inetAddress.getHostAddress().toString();

						// return inetAddress.getHostAddress().toString();
						return ipv4;
					}
				}
			}
		} catch (Exception ex) {
			Log.e("IP Address", ex.toString());
		}
		return null;
	}

	public void stop(View v) {

		this.finish();

	}

	public void send(View v) {
		tv = (TextView) findViewById(R.id.textView1);
		tv.setText("intconnect()");
		// intconnect();

	}

}
