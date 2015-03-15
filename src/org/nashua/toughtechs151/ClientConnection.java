package org.nashua.toughtechs151;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.TimerTask;

public class ClientConnection extends Connection {
	private Socket sock;
	private BufferedReader reader;
	private PrintWriter writer;
	public ClientConnection(String host, int port, ConnectionListener cl) throws Exception {
		super();
		if (cl!=null) addConnectionListener(cl);
		final Connection con = this;
		sock = new Socket(host,port);
		sock.setKeepAlive(true);
		sock.setSoTimeout(0);
		reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		writer = new PrintWriter(sock.getOutputStream());
		t.scheduleAtFixedRate(new TimerTask(){
			public void run() {
				/* Check For Data And Remove Old Connections */
				if (sock!=null) {
					if (sock.isConnected()) {
						try {
							con.onDataRecieved(sock,reader.readLine());
						} catch (IOException e) {
							
						}
					} else {
						con.onDisconnect(sock);
						try {
							sock.close();
						} catch (IOException e) {
						}
						sock = null;
					}
				}
				
			}
		},1,100);
		onConnect(sock);
	}
	public void sendData(String str){
		writer.println(str);
		writer.flush();
	}
	public void disconnect() {
		try {
			if (sock!=null) sock.close();
		} catch (Exception e) {
		}
	}
}
