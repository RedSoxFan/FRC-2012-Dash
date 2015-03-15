package org.nashua.toughtechs151;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JPanel;
import javax.swing.JSpinner;

import org.nashua.toughtechs151.Target.Hoop;

public class HSVTracker {
	private static int sumX=0,sumY=0,sumC=0;
	public static long s1 = 0;
	public static ArrayList<Target> targets = new ArrayList<Target>();
	public BufferedImage real = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
	private BufferedImage play = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
	public HSVTracker(BufferedImage img, BufferedImage buf, int frames, double percentage){
		real = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		real.getGraphics().drawImage(img,0,0,null);
		play = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		play.getGraphics().drawImage(img,0,0,null);
		long start = System.currentTimeMillis();
		if (s1==0) s1=start;
		threshold(img,buf);
		nearestNeighbor(img,buf);
		for (int y=0;y<buf.getHeight();y++) {
			for (int x=0;x<buf.getWidth();x++) {
				img.setRGB(x, y, buf.getRGB(x,y)!=Color.WHITE.hashCode() ? Color.BLACK.hashCode() : Color.WHITE.hashCode());
			}
		}
		algorithm2(img);
		for (int y=0;y<img.getHeight();y++) {
			for (int x=0;x<img.getWidth();x++) {
				img.setRGB(x, y, img.getRGB(x,y)==Color.MAGENTA.hashCode() ? Color.BLACK.hashCode() : Color.WHITE.hashCode());
				play.setRGB(x, y, img.getRGB(x,y)==Color.MAGENTA.hashCode() ? Color.BLACK.hashCode() : Color.WHITE.hashCode());
			}
		}
		ArrayList<double[][]> targs = findHorizontal(img);
		targs.addAll(findVertical(img));
		while (targs.contains(null)) targs.remove(null);
		targets.clear();
		for (double[][] targ:targs){
			if (targ==null||targ[0]==null|targ[1]==null) continue;
			if (play.getRGB((int)targ[1][0],(int)targ[1][1])!=Color.WHITE.hashCode()||play.getRGB((int)(targ[0][2]/2+targ[0][0]),(int)(targ[0][3]/2+targ[0][1]))!=Color.WHITE.hashCode()) continue;
			double r=targ[0][2]*1.0/targ[0][3];
			if (r>=Target.MIN_RATIO&&r<=Target.MAX_RATIO&&targ[0][2]<Target.MAX_WIDTH&&targ[0][3]<Target.MAX_HEIGHT&&targ[0][2]>Target.MIN_WIDTH&&targ[0][3]>Target.MIN_HEIGHT) {
				Graphics g = play.getGraphics();
				g.setColor(Color.ORANGE);
				g.fillRect((int)targ[0][0],(int)targ[0][1],(int)targ[0][2],(int)targ[0][3]);
				targets.add(new Target(targ[0][0],targ[0][1],targ[0][2],targ[0][3],targ[1][0],targ[1][1]));
			}
		}
		if (targets.size()>0) {
			int aw=0, ah=0;
			for (Target t:targets) {aw+=t.w;ah+=t.h;}
			aw/=targets.size();
			ah/=targets.size();
			ArrayList<Target> ts = new ArrayList<Target>();
			for (Target t:targets) if (Math.abs(t.w-aw)>50||Math.abs(t.h-ah)>50) ts.add(t);
			targets.removeAll(ts);
 		}
		for (int j=1;j<targets.size();j++) {
			Target a = targets.get(j);
			Target b = targets.get(j-1);
			if (a.y!=b.y) {
				targets.set(j-1,a.y>b.y?b:a);
				targets.set(j,a.y>b.y?a:b);
			} else if (a.x!=b.x) {
				targets.set(j-1,a.x>b.x?b:a);
				targets.set(j,a.x>b.x?a:b);
			}
		}
		if (targets.size()==4) {
			targets.get(0).hoop = Hoop.TOP;
			targets.get(1).hoop = Hoop.MIDDLE_LEFT;
			targets.get(2).hoop = Hoop.MIDDLE_RIGHT;
			targets.get(3).hoop = Hoop.BOTTOM;
		} else if (targets.size()==3) {
			Target t0 = targets.get(0);
			Target t1 = targets.get(1);
			Target t2 = targets.get(2);
			targets.clear();
			if (t1.y-t0.y>10) {
				t0.hoop=Hoop.TOP;
				t1.hoop=t2.x-t1.x<-10?Hoop.MIDDLE_RIGHT:Hoop.MIDDLE_LEFT;
				t2.hoop=t2.y-t1.y>10||t2.x-t1.x<-10?Hoop.BOTTOM:Hoop.MIDDLE_RIGHT;
			} else {
				t0.hoop=Hoop.MIDDLE_LEFT;
				t1.hoop=Hoop.MIDDLE_RIGHT;
				t2.hoop=Hoop.BOTTOM;
			}
			targets.add(t0.hoop==Hoop.TOP?t0:null);
			targets.add(t0.hoop==Hoop.MIDDLE_LEFT?t0:t1.hoop==Hoop.MIDDLE_LEFT?t1:null);
			targets.add(t1.hoop==Hoop.MIDDLE_RIGHT?t1:t2.hoop==Hoop.MIDDLE_RIGHT?t2:null);
			targets.add(t2.hoop==Hoop.BOTTOM?t2:null);
		} else if (targets.size()==2) {
			Target t0 = targets.get(0);
			Target t1 = targets.get(1);
			targets.clear();
			if (t1.y-t0.y>10) {
				t0.hoop=Hoop.TOP;
				t1.hoop=t1.x-t0.x<-10?Hoop.MIDDLE_LEFT:t1.x-t0.x>10?Hoop.MIDDLE_RIGHT:Hoop.BOTTOM;
				targets.add(t0);
				targets.add(t1.x-t0.x<-10?t1:null);
				targets.add(t1.x-t0.x>10?t1:null);
				targets.add(t1.x-t0.x>=-10&&t1.x-t0.x<=10?t1:null);
			} else {
				t0.hoop = Hoop.MIDDLE_LEFT;
				t1.hoop = Hoop.MIDDLE_RIGHT;
				targets.add(null);
				targets.add(t0);
				targets.add(t1);
				targets.add(null);
			}
		} else if (targets.size()==1) {
			Target t = targets.get(0);
			t.hoop=Hoop.TOP;
			targets.clear();
			targets.add(null);
			targets.add(t);
			targets.add(null);
			targets.add(null);
		} 
		for (int y=0;y<img.getHeight();y++) {
			for (int x=0;x<img.getWidth();x++) {
				if (img.getRGB(x,y)==Color.BLACK.hashCode()) {
					img.setRGB(x, y, real.getRGB(x, y));
				}
			}
		}
		/* Targets - Top, Middle Left, Middle Right, Bottom */
		for (int i=0;i<targets.size();i++) {
			Target t = targets.get(i);
			if (t==null) continue;
			Graphics g = img.getGraphics();
			Graphics h = real.getGraphics();
			try {
				g.setColor(new Color[]{new Color(255,255,0,128),new Color(0,0,255,128),new Color(255,0,0,128),new Color(0,255,0,128)}[i]);
				h.setColor(new Color[]{new Color(255,255,0,128),new Color(0,0,255,128),new Color(255,0,0,128),new Color(0,255,0,128)}[i]);
			} catch (Exception e){
				g.setColor(new Color(255,0,255,128));
				h.setColor(new Color(255,0,255,128));
			}
			g.fillRect((int)t.x,(int)t.y,(int)t.w,(int)t.h);
			h.fillRect((int)t.x,(int)t.y,(int)t.w,(int)t.h);
			String st = String.format("%s x %s",(int)((t.w*100))/100.0,t.h);
			g.setColor(g.getColor().darker());
			g.setColor(Color.LIGHT_GRAY);
			int w = g.getFontMetrics().stringWidth(st);
			h.setColor(Color.WHITE);
			h.setFont(h.getFont().deriveFont(14f));
			g.drawString(st,(int)(t.x+t.w/2-w/2),(int)(t.y+t.h/2-5));
			w = g.getFontMetrics().stringWidth(""+t.hoop);
			h.drawString(""+t.hoop,(int)(t.x+t.w/2-w/2),(int)(t.y+t.h/2+3));
			//t.distance = ((int)(((20.0/Math.tan(Math.toRadians(22.4)))*(img.getWidth()/2.0/t.w)*100)))/100.0;
			t.hypot = 401.67*(Math.pow(2.7182818284,-0.373*(t.w/20)));
			t.distance = Math.sqrt(Math.pow(t.hypot,2)-Math.pow(t.hoop.getHeight()-32,2));
			t.offset = ((img.getWidth()/2)-(t.w/2+t.x)); // Offset in pixels
			t.offset /= t.w/20; // Offset in inches
			t.offset = (int)(t.offset);
			st = String.format("%s",""+((int)(t.distance/12))+"' "+(t.distance%12)+"\"  "+t.offset);
			w = g.getFontMetrics().stringWidth(st);
			g.drawString(st,(int)(t.x+t.w/2-w/2),(int)(t.y+t.h/2+g.getFontMetrics().getHeight()-5));
		}
		if (targets.size()>0) {
			Graphics g = img.getGraphics();
			g.setColor(Color.ORANGE);
			g.drawLine(img.getWidth()/2, 0, img.getWidth()/2, img.getHeight());
			/*Target targ = targets.get(0);
			if (targ.w-targ.x<156) {
				g.setColor(new Color(255, 127, 0, 128));
				g.fillRect(160, 0, 160, 240);
			} else if (targ.w-targ.x<164) {
				g.setColor(new Color(255, 127, 0, 128));
				g.fillRect(0, 0, 160, 240);
			}*/
		}
		Graphics g = img.getGraphics();
		g.setColor(Color.MAGENTA);
		DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
		String s = df.format(Calendar.getInstance().getTime());
		g.drawString(s, img.getWidth()-g.getFontMetrics().stringWidth(s)-5, img.getHeight()-g.getFontMetrics().getHeight()+10);
		//long end = System.currentTimeMillis();
		//System.out.println("Frame: "+(end-start)+" Milliseconds");
	}
	public static void drive(Target targ, Connection c,int width) {
		double v=(width/2.0-((targ.w/2.0)+targ.x))/(width/2.0); //if v is negative, robot is off-center to right, vice versa
		System.out.println("HSVTracker JagAutoSet Value: "+v);
		c.sendData(String.format("JagAutoSet[%s,%s,%s,%s]",-v,v,-v,v));
	}
	/*private ArrayList<int[][]> trackMiddle(BufferedImage img, int[][] tar) {
		ArrayList<int[][]> targ = new ArrayList<int[][]>();
		targ.addAll(findLine(img.getSubimage(0,tar[0][1]+10,tar[0][0],tar[0][3]-20)));
		targ.addAll(findLine(img.getSubimage(tar[0][0]+tar[0][2],tar[0][1]+10,320-(tar[0][1]+tar[0][2]),tar[0][3]-20)));
		return targ;
	}*/
	private ArrayList<double[][]> findHorizontal(BufferedImage img){
		ArrayList<double[][]> targ = new ArrayList<double[][]>();
		int ty=0;
		for (int y=0;y<img.getHeight();y++) {
			int rc=0;
			for (int x=0;x<img.getWidth();x++) {
				if (img.getRGB(x,y)==Color.BLACK.hashCode()) {
					sumX+=x; sumY+=y; sumC++; rc++;
				}
			}
			if (rc==0&&sumC>0) {
				sumX/=sumC;
				sumY/=sumC;
				if (sumC>175&&img.getRGB(sumX,sumY)!=Color.BLACK.hashCode())
					targ.add(new double[][]{findRect(img,0,img.getWidth(),ty,y+1),new double[]{sumX,sumY}});
				sumC=0;
				sumX=0;
				sumY=0;
				ty = y+1;
			}
		}
		if (sumC>0) {
			sumX/=sumC;
			sumY/=sumC;
			targ.add(new double[][]{findRect(img,0,img.getWidth(),ty,img.getHeight()),new double[]{sumX,sumY}});
		}
		return targ;
	}
	private ArrayList<double[][]> findVertical(BufferedImage img){
		ArrayList<double[][]> targ = new ArrayList<double[][]>();
		int tx = 0;
		for (int x=0;x<img.getWidth();x++) {
			int rc=0;
			for (int y=0;y<img.getHeight();y++) {
				if (img.getRGB(x,y)==Color.BLACK.hashCode()) {
					sumX+=x; sumY+=y; sumC++; rc++;
				}
			}
			if (rc==0&&sumC>0) {
				sumX/=sumC;
				sumY/=sumC;
				if (sumC>200&&img.getRGB(sumX,sumY)!=Color.BLACK.hashCode())
					targ.add(new double[][]{findRect(img,tx,x+1,0,img.getHeight()),new double[]{sumX,sumY}});
				sumC=0;
				sumX=0;
				sumY=0;
				tx=x+1;
			}
		}
		if (sumC>0) {
			sumX/=sumC;
			sumY/=sumC;
			targ.add(new double[][]{findRect(img,tx,img.getWidth(),0,img.getHeight()),new double[]{sumX,sumY}});
		}
		return targ;
	}
	public void algorithm2(BufferedImage img) {
		Graphics g = img.getGraphics();
		for (int x=0;x<img.getWidth();x++) {
			ArrayList<int[]> startStop = new ArrayList<int[]>();
			int s=0,e=0,o=0;
			for (int y=0;y<img.getHeight();y++) {
				e=0;
				if (img.getRGB(x,y)==Color.BLACK.hashCode()&&o==0) {
					o=1;
					s=y;
				}
				else if (img.getRGB(x,y)!=Color.BLACK.hashCode()&&o==1) {
					o=0;
					e=y;
					if (e-s>Target.MIN_HEIGHT) {
						startStop.add(new int[]{s,e});
						g.setColor(Color.MAGENTA);
						g.drawRect(x,s,1,e-s);
					}
				}
			}
		}
		for (int y=0;y<img.getHeight();y++) {
			ArrayList<int[]> startStop = new ArrayList<int[]>();
			int s=0,e=0,o=0;
			for (int x=0;x<img.getWidth();x++) {
				e=0;
				if (img.getRGB(x,y)==Color.BLACK.hashCode()&&o==0) {
					o=1;
					s=x;
				}
				else if (img.getRGB(x,y)!=Color.BLACK.hashCode()&&o==1) {
					o=0;
					e=x;
					if (e-s>Target.MIN_WIDTH) {
						startStop.add(new int[]{s,e});
						g.setColor(Color.MAGENTA);
						g.drawRect(s,y,e-s,1);
					} else {
						g.setColor(Color.GRAY);
					}
				}
			}
		}
	}
	public void threshold(BufferedImage img, BufferedImage buf) {
		//long start = System.currentTimeMillis();
		JPanel hue = (JPanel)Dashboard.cos.get("Hue");
		int hmi = Integer.parseInt(((JSpinner)hue.getComponent(1)).getValue().toString());
		int hma = Integer.parseInt(((JSpinner)hue.getComponent(2)).getValue().toString());
		JPanel sat = (JPanel)Dashboard.cos.get("Sat");
		int smi = Integer.parseInt(((JSpinner)sat.getComponent(1)).getValue().toString());
		int sma = Integer.parseInt(((JSpinner)sat.getComponent(2)).getValue().toString());
		JPanel val = (JPanel)Dashboard.cos.get("Val");
		int vmi = Integer.parseInt(((JSpinner)val.getComponent(1)).getValue().toString());
		int vma = Integer.parseInt(((JSpinner)val.getComponent(2)).getValue().toString());
		JPanel red = (JPanel)Dashboard.cos.get("Red");
		int rmi = Integer.parseInt(((JSpinner)red.getComponent(1)).getValue().toString());
		int rma = Integer.parseInt(((JSpinner)red.getComponent(2)).getValue().toString());
		JPanel green = (JPanel)Dashboard.cos.get("Green");
		int gmi = Integer.parseInt(((JSpinner)green.getComponent(1)).getValue().toString());
		int gma = Integer.parseInt(((JSpinner)green.getComponent(2)).getValue().toString());
		JPanel blue = (JPanel)Dashboard.cos.get("Blue");
		int bmi = Integer.parseInt(((JSpinner)blue.getComponent(1)).getValue().toString());
		int bma = Integer.parseInt(((JSpinner)blue.getComponent(2)).getValue().toString());
		//System.out.println(String.format("Hue (%s - %s) / Red (%s - %s) / Green (%s - %s) / Blue (%s - %s)",hmi,hma,rmi,rma,gmi,gma,bmi,bma));
		//System.out.println(String.format("Saturation (%s - %s) / Value (%s - %s)",smi,sma,vmi,vma));
		for (int r=0;r<img.getHeight();r++) {
			for (int c=0;c<img.getWidth();c++) {
				Color col = new Color(img.getRGB(c,r));
				float[] hsv = Color.RGBtoHSB(col.getRed(),col.getGreen(),col.getBlue(),null);
				if ((hsv[0]>=(hmi/255.0)&&hsv[0]<=(hma/255.0)) // Hue
						&& (hsv[1]>=(smi/255.0)&&hsv[1]<=(sma/255.0)) // Saturation
						&& (hsv[2]>=(vmi/255.0)&&hsv[2]<=(vma/255.0)) // Value
						&& (col.getRed()>=rmi&&col.getRed()<=rma) // Red
						&& (col.getGreen()>=gmi&&col.getGreen()<=gma) // Green
						&& (col.getBlue()>=bmi&&col.getBlue()<=bma) // Blue
				) {
					buf.setRGB(c,r,new Color(hsv[0],hsv[0],hsv[0]).hashCode());
				} else {
					buf.setRGB(c,r,Color.WHITE.hashCode());
					img.setRGB(c,r,Color.WHITE.hashCode());
				}
			}
		}
		//System.out.println("\tthreshold: "+(System.currentTimeMillis()-start)+" Milliseconds");
	}
	public void nearestNeighbor(BufferedImage img, BufferedImage buf){
		//long start = System.currentTimeMillis();
		sumX=0;sumY=0;sumC=0;
		BufferedImage im = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
		int count=0;
		int z=2;
		int[][] v = new int[img.getHeight()][img.getWidth()]; 
		for(int y=0;y<buf.getHeight();y++){
			for(int x=0;x<buf.getWidth();x++){
				for(int yLoc=y-z;yLoc<y+z;yLoc++){
					for(int xLoc=x-z;xLoc<x+z;xLoc++){
						try {
							if (v[yLoc][xLoc]==0) v[yLoc][xLoc]=buf.getRGB(xLoc,yLoc)!=Color.WHITE.hashCode()?1:-1;
							count+=v[yLoc][xLoc]==1?1:0;
						} catch (Exception e){}
					}
				}
				try {
					if(count>=(z*2+1)*1.8){
						im.setRGB(x,y,Color.BLACK.hashCode());
						sumX+=x;sumY+=y;sumC++;
					}else{
						im.setRGB(x,y,Color.WHITE.hashCode());
					}
				} catch (Exception e){}
				count = 0;
			}
		}
		buf.getGraphics().drawImage(im,0,0,null);
		//System.out.println("\tNearest Neighbor: "+(System.currentTimeMillis()-start)+" Milliseconds");
	}
	private double[] findRect(BufferedImage img, int lx, int hx, int ly, int hy){
		try {
			int LX=sumX,RX=sumX,UY=sumY,DY=sumY;
			while(grabPixel(img,LX,sumY,Color.BLACK.hashCode())!=Color.black.hashCode()&&LX>0)LX--;
			while(grabPixel(img,RX,sumY,Color.BLACK.hashCode())!=Color.black.hashCode()&&RX<img.getWidth())RX++;
			while(grabPixel(img,sumX,UY,Color.BLACK.hashCode())!=Color.black.hashCode()&&UY>0)UY--;
			while(grabPixel(img,sumX,DY,Color.BLACK.hashCode())!=Color.black.hashCode()&&DY<img.getHeight())DY++;
			LX=Math.max(LX,lx);
			RX=Math.min(RX,hx);
			UY=Math.max(UY,ly);
			DY=Math.min(DY,hy);
			double width = 0, ax=0;
			for (int y=UY;y<DY;y++){
				int lX=sumX,rX=sumX;
				while(grabPixel(img,lX,y,Color.BLACK.hashCode())!=Color.black.hashCode()&&lX>0)lX--;
				while(grabPixel(img,rX,y,Color.BLACK.hashCode())!=Color.black.hashCode()&&rX<img.getWidth())rX++;
				width+=rX-lX+1;
				ax+=lX;
			}
			width/=DY-UY;
			ax/=DY-UY;
			return new double[]{ax,UY,width,DY-UY};
		} catch (Exception e) {
			return null;
		}
	}
	private int grabPixel(BufferedImage img, int x, int y, int ec){
		// SAFETY METHOD
		try {
			return img.getRGB(x,y);
		} catch (Exception e){
			return ec;
		}
	}
}
