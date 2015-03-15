package org.nashua.toughtechs151;

public class Data {
	private Connection con;
	public Data(Connection co){
		con=co;
	}
	public void updateCAN(int i, String name, String speed, String temp) {
		con.sendData(String.format("CAN[%s,%s,%s,%s]",i,name,speed,temp));
	}
	public void updatePWM(String put,int i, String name, String value) {
		con.sendData(String.format("PWM[%s,%s,%s,%s]",put,i,name,value));
	}
	public void updateInfo(String str, String value) {
		con.sendData(String.format("INFO[%s,%s]",str,value));
	}
}
