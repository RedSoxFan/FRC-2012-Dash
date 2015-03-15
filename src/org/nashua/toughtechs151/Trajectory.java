package org.nashua.toughtechs151;
import java.applet.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
public class Trajectory extends Applet implements ActionListener, ItemListener{
	private static final long serialVersionUID = 1L;
	String text;
	TextField rField, hField, vField;
	Button ok, clear;
	Choice hoop;
	double angle1=0, angle2=0, maxRange=0, sqrt1, sqrt2;
	double r,h,hoopH,v;
	boolean calcNow=false, calcPossible=true;
	public void init(){		
		this.setSize(300,300);
		this.setLayout(null);
		
		rField=new TextField(10);
		rField.addActionListener(this);
		rField.setBounds(55,30,75,20);	
		add(rField);		
		
		vField=new TextField(10);
		vField.addActionListener(this);
		vField.setBounds(55,rField.getY()+30,75,20);	
		add(vField);
		
		hField=new TextField(10);
		hField.addActionListener(this);
		hField.setBounds(100,vField.getY()+30,75,20);	
		add(hField);
		
		ok=new Button("Confirm");
		ok.addActionListener(this);
		ok.setBounds(hField.getX(),hField.getY()+30,50,25);
		add(ok);
		
		clear=new Button("Clear");
		clear.addActionListener(this);
		clear.setBounds(ok.getX()+60,ok.getY(),50,25);
		add(clear);
		
		rField.setText("0.0");
		hField.setText("0.0");
		vField.setText("0.0");
		
		hoop=new Choice();
		hoop.add("Hoop 1");
		hoop.add("Hoop 2");
		hoop.add("Hoop 3");
		hoop.setBounds(rField.getX()+100,rField.getY(),75,20);
		hoop.addItemListener(this);
		add(hoop);
	}
	public void paint(Graphics g){
		g.drawString("Angle Calculator: Distances in Feet",20,rField.getY()-15);
		g.drawString("Range: ",10,rField.getY()+15);
		g.drawString("Launch Height: ",10,hField.getY()+15);
		g.drawString("Speed: ",10,vField.getY()+15);
			
		if(calcNow){
			varsToMeters();
			
			sqrt1=Math.pow(v,4);
			sqrt2=9.81*(9.81*Math.pow(r,2)+2*hoopH*Math.pow(v,2));
			maxRange=ft_M(Math.pow(v,2)/9.81,true);
			g.drawString("At 0 Height..MaxRange: "+maxRange,10,ok.getY()+64);
			if(sqrt1<=sqrt2){
				calcPossible=false;
			}
			if(calcPossible){
				angle1=calculate(r,hoopH,v,1);
				angle2=calculate(r,hoopH,v,-1);
				g.drawString("Angle1: "+angle1,10,ok.getY()+40);
				g.drawString("Angle2: "+angle2,10,ok.getY()+52);
			}else{
				g.drawString("Angle1: Trajectory Unacceptable",10,ok.getY()+40);
				g.drawString("Angle2: Trajectory Unacceptable",10,ok.getY()+52);
			}
		}
	}
	public void varsToMeters(){
		r=ft_M(r,false);
		v=ft_M(v,false);
		h=ft_M(h,false);
		hoopH=ft_M(hoopH/12.,false);
		hoopH=hoopH-h;
	}
	public static double ft_M(double length, boolean inverse){
		return inverse?length*3.28:length/3.28;
	}
	public static double calculate(double r, double h, double v, int neg){
			double angle=0;
		angle=Math.toDegrees(Math.atan((Math.pow(v,2)+neg*Math.sqrt(Math.pow(v,4)-9.81*(9.81*Math.pow(r,2)+2*h*Math.pow(v,2))))/(9.81*r)));
		return angle;
	}
	public void itemStateChanged(ItemEvent e){
		int selection=hoop.getSelectedIndex();
		if(e.getSource()==hoop){
			if(selection==0){
				hoopH=28;
			}else if(selection==1){
				hoopH=61;
			}else if(selection==2){
				hoopH=98;
			}
		}
		calcNow=false;
		calcPossible=true;
	}
	public void actionPerformed(ActionEvent e) {
			if(e.getSource()==ok){
				text=rField.getText();
				r=Double.parseDouble(text);
				text=hField.getText();
				h=Double.parseDouble(text);
				text=vField.getText();
				v=Double.parseDouble(text);
				calcNow=true;
				calcPossible=true;
			}
			if(e.getSource()==clear){
				rField.setText("0.0");
				hField.setText("0.0");
				vField.setText("0.0");
				calcNow=false;
				calcPossible=true;
			}
			repaint();
	}
}
