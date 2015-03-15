package org.nashua.toughtechs151;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;

public class ServerConnection extends Connection {
	private ServerSocket server;
	protected ArrayList<Socket> socks = new ArrayList<Socket>();
	HashMap<Socket,BufferedReader> readers = new HashMap<Socket, BufferedReader>();
	private HashMap<Socket,PrintWriter> writers = new HashMap<Socket, PrintWriter>();
	private HashMap<Socket,Long> timeouts = new HashMap<Socket, Long>();
	public ServerConnection(int port, ConnectionListener cl) throws Exception {
		super();
		if (cl!=null) addConnectionListener(cl);
		final Connection con = this;
		server = new ServerSocket(port);
		server.setSoTimeout(0);
		new java.util.Timer().schedule(new TimerTask(){
			public void run() {
				while (true) {
					/* Check For Connections */
					Socket s;
					System.out.println("Checking");
					try {
						s = server.accept();
						if (s!=null) {
							System.out.println("[Connected]");
							socks.add(s);
							readers.put(s,new BufferedReader(new InputStreamReader(s.getInputStream())));
							writers.put(s,new PrintWriter(s.getOutputStream()));
							timeouts.put(s,System.currentTimeMillis());
							con.onConnect(s);
						}
					} catch (IOException e) {
						
					}
				}
			}
		},1);
		new java.util.Timer().schedule(new TimerTask(){
			public void run() {
				while (true) {
					/* Check For Data And Remove Old Connections */
					ArrayList<Socket> so = new ArrayList<Socket>();
					try {
						for (Socket s:socks) {
							if (s.isConnected()&&!s.isClosed()){//&&(System.currentTimeMillis()-timeouts.get(s)<5000)) {
								BufferedReader br = readers.get(s);
								try {
									if (br.ready()) {
										String st = readers.get(s).readLine();
										System.out.println(st);
										onDataRecieved(s,st);
										timeouts.put(s,System.currentTimeMillis());
									}
								} catch (IOException e) {
									
								}
							} else {
								so.add(s);
								readers.remove(s);
								writers.remove(s);
								onDisconnect(s);
								try {
									s.close();
								} catch (IOException e) {
									
								}
							}
						}
						for (Socket socky:so) socks.remove(socky);
					} catch (Exception e){}
				}
			}
		},1);
	}
	public void sendData(String str) {
		for (Socket s:socks) sendData(s,str);
	}
	public void sendData(Socket so, String str){
		try {
			PrintWriter pw = writers.get(so);
			while (str.length()!=50) str+="\0";
			pw.print(str);
			pw.flush();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	public void disconnect() {
		for (Socket s:socks)
			try {
				s.close();
			} catch (IOException e) {
				
			}
		socks.clear();
	}
}
