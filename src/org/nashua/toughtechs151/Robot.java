package org.nashua.toughtechs151;

import java.io.IOException;
import java.net.Socket;

public class Robot implements ConnectionListener {
	public static void main(String[] args) {
		try {
			new Robot();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private ClientConnection conn;
	public Data d;
	public Robot() throws Exception{
		conn = new ClientConnection("127.0.0.1",1735,this);
		d = new Data(conn);
		d.updateCAN(1, "Test", "1%", "5V");
		d.updateCAN(2, "Test2", "2%", "5V");
		d.updatePWM("Input", 2, "Bluh", ""+System.currentTimeMillis()%5+"V");
	}
	public void onConnect(Socket s) {
		System.out.println("[Connected]");
	}
	public void onDisconnect(Socket s) {
		System.out.println("[Disconnected]");
	}
	public void onDataRecieved(Socket s, String str) {
		if (str.equals("[Disconnect]")) try {s.close();} catch (IOException e) {}
		System.out.println(str);
	}
}
