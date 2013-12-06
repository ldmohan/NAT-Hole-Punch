//package relayServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;


public class RelayServer {

	private Set<Clients> clientList = new HashSet<Clients>();

	public static void main(String[] args) {
		RelayServer rs = new RelayServer();
		try {
			rs.startServer();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startServer() throws IOException, InterruptedException {
		DatagramSocket listSocket = new DatagramSocket(2020);
		// DatagramSocket serverSocket = new DatagramSocket(12345);
		// byte[] receiveRegData = new byte[50];
		byte[] buffer = new byte[1024];
		//Set<Clients> clientList = new HashSet<Clients>();

		
		while (true) {
			
			DatagramPacket receivePacket = new DatagramPacket(buffer,buffer.length);
			System.out.println("Server is Listening...on Port : 2020");
			listSocket.receive(receivePacket);
			String regData = null;
			String inData = null;
			// regData = new String(receivePacket.getData());
			 Clients client = new Clients();

			regData = new String(receivePacket.getData(), 0, receivePacket.getLength());

			//System.out.println("Received data " + regData);

			String request = null, num = null, clIP = null, clPort = null;
			request = regData.substring(0, regData.indexOf(','));
		//	num = regData.substring(regData.indexOf(',') + 1,regData.indexOf('-'));
			clIP = regData.substring(regData.indexOf('-') + 1,regData.indexOf(';'));
			clPort = regData.substring(regData.indexOf(';') + 1,regData.indexOf('!'));

	
			if (request.equals("Register")) {
			 num = regData.substring(regData.indexOf(',') + 1,regData.indexOf('-'));

				boolean status = true;
				InetAddress incomingIPAddress = receivePacket.getAddress();
				String recadd = incomingIPAddress.getHostAddress();
				int port = receivePacket.getPort();
				int size = clientList.size();
				System.out.println("Receive Request  " +request + " from the number "+num);
				for (Clients cl : clientList) {
					if (cl.getNum().equals(num)) {
						//System.out.println("Found " + cl.getNum());
						cl.setIp(recadd);
						cl.setPort(port);
						cl.setClIP(clIP);
						cl.setClPort(clPort);
						status = false;
						break;
					}
				}
				if (status) {
					//Clients client = new Clients(); Set<Clients> clientList
					client.setIp(recadd);
					client.setNum(num);
					client.setPort(port);
					client.setClIP(clIP);
					client.setClPort(clPort);
					clientList.add(client);
				}
				String finalData = "";
				for (Clients cl : clientList) {

			System.out.println("Regestered Clients---->  Number " + cl.getNum() + " Public Socket "+ cl.getIP()+ ":"+cl.getPort()+ " Private Socket "+cl.getClIP()+":"+cl.getClPort());
					// System.out.println("Regestered Clients are "+
					// cl.getNum()+"  Local IP "+cl.getClIp()+"Local port "+cl.getClPort());
					// System.out.println("Regestered Clients are "+
					// cl.getNum()+"  with port "+cl.getPort());
					String data = cl.getNum();
					finalData = finalData + "\n" + data;
				}

				byte[] sendData = new byte[1024];
				sendData = finalData.getBytes();
				InetAddress outAdd = incomingIPAddress;// InetAddress.getByName(client.getIP());


				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, outAdd, port);
				listSocket.send(sendPacket);

				receivePacket.setLength(0);

			}

			if (request.equals("Connect")) {
			num = regData.substring(regData.indexOf(',') + 1,regData.indexOf('-'));
			inData=regData.substring(regData.indexOf('-') + 1,regData.indexOf('!'));
				InetAddress incomingIPAddress1 = receivePacket.getAddress();
				String recadd1 = incomingIPAddress1.getHostAddress();
				int port1 = receivePacket.getPort();
				Clients sourceComp = new Clients();
				sourceComp.setIp(recadd1);
				sourceComp.setClIP(clIP);
				sourceComp.setClPort(clPort);
				sourceComp.setPort(port1);
				//clientList.add(sourceComp);

				Clients destDevice = new Clients();

				destDevice.setNum(num);
				//System.out.println("Checking: " + sourceComp.getIP() + ":"+ sourceComp.getPort() + " TO: " + destDevice.getNum()); Set<Clients> clientList

				Clients matchedDevice = getMatched(destDevice);
				if (matchedDevice != null) {
				System.out.println("Destination Found ........");
					
					
					 if(matchedDevice.getIP().equals(sourceComp.getIP())){
					 System.out.println("From behind the same NAT");
					 
					 sendPacketSame(listSocket, matchedDevice, sourceComp);
					 Thread.sleep(1000); 
					 sendPacketSame(listSocket,sourceComp, matchedDevice); 
					clientList.remove(sourceComp);
					clientList.remove(client);
 
					 } else
					{

						sendPacket(listSocket, matchedDevice, sourceComp);
						Thread.sleep(100);
						sendPacket(listSocket, sourceComp, matchedDevice);

						clientList.remove(sourceComp);
						clientList.remove(client);

					}
					
				} else {
					System.out.println("Destination not Found .....");
					
					clientList.remove(sourceComp);
					clientList.remove(client);

				}
				receivePacket.setLength(buffer.length);
				clientList.remove(sourceComp);
			}
		}
		
	}

	private synchronized Clients getMatched(Clients comp) {
		for (Clients c : clientList) {
			if (c.getNum().equals(comp.getNum())) {
				return c;
			}
		}
		return null;
	}

	private synchronized void sendPacket(DatagramSocket socket,	Clients natDeviceIn, Clients natDeviceOut) throws IOException {

		byte[] sendData = new byte[1024];
		InetAddress homeIPAddress = InetAddress.getByName(natDeviceIn.getIP());
		int homePort = natDeviceIn.getPort();
		// now the remote destination
		InetAddress destIPAddress = InetAddress.getByName(natDeviceOut.getIP());
		int destPort = natDeviceOut.getPort();
		String data = destIPAddress.getHostAddress() + ":" + destPort + "-" + homePort;
		sendData = data.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, homeIPAddress, homePort);
		socket.send(sendPacket);
		 System.out.println("Send details "+data);

	}

	private synchronized void sendPacketSame(DatagramSocket socket, Clients natDeviceIn, Clients natDeviceOut) throws IOException {
		byte[] sendData = new byte[1048];
		InetAddress homeIPAddress = InetAddress.getByName(natDeviceIn.getIP());
		int homePort = natDeviceIn.getPort();
		InetAddress destIPAddress = InetAddress.getByName(natDeviceOut.getClIP());
		int destPort = Integer.parseInt(natDeviceOut.getClPort());
		int clport = Integer.parseInt(natDeviceIn.getClPort());
		String data = destIPAddress.getHostAddress() + ":" + destPort + "-"+ clport;
		sendData = data.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, homeIPAddress, homePort);
		socket.send(sendPacket);
		System.out.println("Send details "+data);
	}

	public class Clients {
		int port = 0;
		String ip = "0";
		String num = "0";
		String clIp = "0";
		String clport = "0";

		Clients() {

		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public void setNum(String num) {
			this.num = num;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public void setClPort(String port) {
			this.clport = port;
		}

		public void setClIP(String ip) {
			this.clIp = ip;
		}

		public String getIP() {
			return this.ip;
		}

		public String getNum() {
			return this.num;
		}

		public int getPort() {
			return this.port;

		}

		public String getClIP() {
			return this.clIp;

		}

		public String getClPort() {
			return this.clport;

		}

	}

}
