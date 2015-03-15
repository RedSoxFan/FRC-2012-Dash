package org.nashua.toughtechs151;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;

public abstract class Connection {
	private ArrayList<ConnectionListener> listeners = new ArrayList<ConnectionListener>();
	protected Timer t = new Timer();
	public Connection(){
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				disconnect();
			}
		});
	}
	public void addConnectionListener(ConnectionListener cl){
		listeners.add(cl);
	}
	public void removeConnectionListener(ConnectionListener cl){
		listeners.remove(cl);
	}
	public ConnectionListener[] getConnectionListeners() {
		return listeners.toArray(new ConnectionListener[listeners.size()]);
	}
	public void setConnectionListeners(ConnectionListener[] cl) {
		removeAllConnectionListeners();
		for (ConnectionListener c:cl) addConnectionListener(c);
	}
	public void removeAllConnectionListeners(){
		listeners.clear();
	}
	protected void onConnect(Socket sock) {
		for (ConnectionListener c:listeners) c.onConnect(sock);
	}
	protected void onDisconnect(Socket sock) {
		for (ConnectionListener c:listeners) c.onDisconnect(sock);
	}
	protected void onDataRecieved(Socket sock, String str){
		for (ConnectionListener c:listeners) c.onDataRecieved(sock,str);
	}
	public abstract void sendData(String str);
	public abstract void disconnect();
}
