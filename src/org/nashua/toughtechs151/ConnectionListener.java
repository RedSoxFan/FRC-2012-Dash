package org.nashua.toughtechs151;

import java.net.Socket;

public interface ConnectionListener {
	public void onConnect(Socket s);
	public void onDisconnect(Socket s);
	public void onDataRecieved(Socket s, String str);
}
