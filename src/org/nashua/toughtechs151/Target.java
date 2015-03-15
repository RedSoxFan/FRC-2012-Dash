package org.nashua.toughtechs151;

import java.awt.Point;

public class Target {
	public enum Hoop {
		TOP,MIDDLE_UNKNOWN,MIDDLE_LEFT,MIDDLE_RIGHT,BOTTOM,UNKNOWN;
		public String toString(){
			return name().startsWith("MIDDLE_")?"M"+name().split("_")[1].substring(0,1):name().substring(0,3);
		}
		public int getHeight() {
			return this==TOP?98:this==BOTTOM?28:61;
		}
	}
	public double x,y,w,h,cx,cy,distance,offset,hypot;
	public Hoop hoop;
	public double ratio;
	public static final double MIN_RATIO = 1;
	public static final double MAX_RATIO = 2;
	public static final double MAX_WIDTH = 315;
	public static final double MAX_HEIGHT = 235;
	public static final double MIN_WIDTH = 24;
	public static final double MIN_HEIGHT = 18;
	public Target(double x, double y, double w, double h, double cx, double cy) {
		this.x=x;
		this.y=y;
		this.w=w;
		this.h=h;
		this.cx=cx;
		this.cy=cy;
		this.distance=0;
		this.offset=0;
		ratio=w*1.0/h;
		this.hoop = Hoop.UNKNOWN;
	}
	public boolean contains(int[] point) {
		return contains(new Point(point[0],point[1]));
	}
	public boolean contains(Point point) {
		return point.x>=x&&point.x<=x+w&&point.y>=y&&point.y<=y+h;
	}
	public String toString() {
		return String.format("Target[x=%s,y=%s,w=%s,h=%s,hoop=%s]",x,y,w,h,hoop);
	}
}
