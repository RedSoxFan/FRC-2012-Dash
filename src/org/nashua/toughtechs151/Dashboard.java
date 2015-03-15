package org.nashua.toughtechs151;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileSystemView;


public class Dashboard implements ConnectionListener {
	public static void main(String[] args) {
		try {
			new Dashboard(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private JFrame frame;
	private JPanel panel, /*cpn, pin,*/ pon, ctr, ipn;
	private ArrayList<JLabel> can_labels = new ArrayList<JLabel>();
	private ArrayList<JLabel> pwm_in_labels = new ArrayList<JLabel>();
	private ArrayList<JLabel> pwm_out_labels = new ArrayList<JLabel>();
	private HashMap<String,JLabel> hs = new HashMap<String, JLabel>();
	private ArrayList<Integer[]> draw = new ArrayList<Integer[]>();
	private BufferedImage buf;
	private BufferedImage sbuf;
	private ServerConnection conn;
	private int distance=0;
	private JPanel cop;
	public static HashMap<String,Object> cos = new HashMap<String,Object>();
	public static HashMap<String, Integer[][]> cols;
	public final File f = new File(FileSystemView.getFileSystemView().getHomeDirectory().getPath()+File.separator+"colors.inf");
	public final File sf = new File(FileSystemView.getFileSystemView().getHomeDirectory().getPath()+File.separator+"screenshots"+File.separator+new SimpleDateFormat("yyMMdd_HHmm").format(Calendar.getInstance().getTime()));
	public static int screen = 1;
	boolean conf = false;
	public double vms=0, h=60;
	public String b="Kinect"; 
	public BufferedImage raw = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
	public Dashboard(String[] args) throws Exception{
		if (args.length>0) {
			if (args[0].equals("--conf")||args[0].equals("-c")) conf=true;
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		} 
		if (!sf.exists()) sf.mkdirs();
		frame = new JFrame("Tough Techs 151 Dashboard 2012");
		frame.setIconImage(new ImageIcon(Dashboard.class.getClass().getResource("/tticon.png")).getImage());
		panel = new JPanel();
		panel.setBackground(Color.BLACK);
		panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		panel.setLayout(null);
		int p=5,x=p,w=0;
		/* CAN */
		/*cpn = new JPanel();
		cpn.setLayout(null);
		for (int i=3; i<10; i++) {
			JLabel lab = new JLabel(can(i,"Unused","N/A","N/A","N/A"));
			can_labels.add(lab);
			lab.setForeground(Color.LIGHT_GRAY);
			lab.setBounds(125*((i-3)/4)+10,75*((i-3)%4)+5,115,65);
			cpn.add(lab);
		}
		JLabel clab = new JLabel("<html><b>CAN Jaguars</b></html>",SwingConstants.CENTER);
		can_labels.add(clab);
		clab.setForeground(Color.LIGHT_GRAY);
		clab.setBounds(115*((can_labels.size()-1)/5)+10,60*4,115,65);
		cpn.add(clab);
		cpn.setBackground(Color.DARK_GRAY);
		cpn.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		w=250;
		cpn.setBounds(x,10,w,300);
		panel.add(cpn);
		x+=w+p;*/
		/* PWM Output */
		pon = new JPanel();
		pon.setLayout(null);
		for (int i=1; i<11; i++) {
			JLabel lab = new JLabel(pwm("Output",i,"Unused","N/A",false));
			pwm_out_labels.add(lab);
			lab.setForeground(Color.LIGHT_GRAY);
			lab.setBounds(145*((i-1)/10)+10,25*((i-1)%10)+10,185,15);
			pon.add(lab);
		}
		JLabel polab = new JLabel("<html><b>PWM Output</b></html>",SwingConstants.CENTER);
		pwm_out_labels.add(polab);
		polab.setForeground(Color.LIGHT_GRAY);
		polab.setBounds(185*((pwm_in_labels.size()-1)/10)+10,60*4+10,185,50);
		pon.add(polab);
		pon.setBackground(Color.DARK_GRAY);
		pon.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		w=200+200*((pwm_out_labels.size()-2)/10);
		pon.setBounds(x,10,w,300);
		x+=w+p;
		panel.add(pon);
		/* Info */
		ipn = new JPanel();
		ipn.setLayout(null);
		String[] sy=new String[27];
		//for (int j=0;j<2;j++) for (int i=0;i<12;i++) sy[j*12+i]=""+(j==0?"Driver":"Shooter")+" "+(i+1);
		sy[24]="Distance";sy[25]="Targets Found";sy[26]="LedOn?";
		for (String st:sy) {
			if (st==null) continue; 
			JLabel lab = new JLabel(st+": N/A");
			lab.setForeground(Color.LIGHT_GRAY);
			lab.setBounds(135*(hs.size()/12)+10,(hs.size()%12)*20+17,125,15);
			hs.put(st,lab);
			ipn.add(lab);
		}
		final Object[] c = new Object[]{"Blue","Green","Red","Yellow"};
		final JComboBox cob = new JComboBox(c);
		cob.setBackground(Color.DARK_GRAY);
		cos.put("drop",cob);
		cols = new HashMap<String,Integer[][]>();
		cols.put("blue",new Integer[][]{new Integer[]{125,160},new Integer[]{0,255},new Integer[]{0,255},new Integer[]{230,255},new Integer[]{0,255},new Integer[]{0,255}});
		cols.put("green",new Integer[][]{new Integer[]{63,157},new Integer[]{0,142},new Integer[]{158,249},new Integer[]{0,255},new Integer[]{0,255},new Integer[]{0,255}});
		cols.put("red",new Integer[][]{new Integer[]{0,255},new Integer[]{232,255},new Integer[]{0,255},new Integer[]{0,255},new Integer[]{0,255},new Integer[]{0,255}});
		cols.put("yellow",new Integer[][]{new Integer[]{12,118},new Integer[]{219,255},new Integer[]{0,255},new Integer[]{0,255},new Integer[]{0,255},new Integer[]{0,255}});
		if (f.exists()) load();
		cob.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange()!=ItemEvent.SELECTED) return;
				Integer[][] sl = cols.get(cob.getSelectedItem().toString().toLowerCase());
				JPanel pa = (JPanel)cos.get("Hue");
				((JSpinner)pa.getComponent(1)).setValue(sl[0][0]);
				((JSpinner)pa.getComponent(2)).setValue(sl[0][1]);
				pa = (JPanel)cos.get("Sat");
				((JSpinner)pa.getComponent(1)).setValue(sl[4][0]);
				((JSpinner)pa.getComponent(2)).setValue(sl[4][1]);
				pa = (JPanel)cos.get("Val");
				((JSpinner)pa.getComponent(1)).setValue(sl[5][0]);
				((JSpinner)pa.getComponent(2)).setValue(sl[5][1]);
				pa = (JPanel)cos.get("Red");
				((JSpinner)pa.getComponent(1)).setValue(sl[1][0]);
				((JSpinner)pa.getComponent(2)).setValue(sl[1][1]);
				pa = (JPanel)cos.get("Green");
				((JSpinner)pa.getComponent(1)).setValue(sl[2][0]);
				((JSpinner)pa.getComponent(2)).setValue(sl[2][1]);
				pa = (JPanel)cos.get("Blue");
				((JSpinner)pa.getComponent(1)).setValue(sl[3][0]);
				((JSpinner)pa.getComponent(2)).setValue(sl[3][1]);
				if (!conf) save(c,true);
			}
		});
		cob.setFocusable(false);
		JLabel lab = new JLabel("LED:");
		lab.setBounds(135*(hs.size()/12)+10,(hs.size()%12)*20+23,25,15);
		lab.setForeground(Color.LIGHT_GRAY);
		ipn.add(lab);
		cob.setBounds(135*(hs.size()/12)+35,(hs.size()%12)*20+17,90,25);
		ipn.add(cob);
		hs.put("led", lab);
		hs.put("leds", null);
		final JComboBox bal = new JComboBox(new String[]{"Kinect","Autonomous"});
		bal.setSelectedItem(b.replaceFirst(b.substring(0,1),b.substring(0,1).toUpperCase()));
		bal.setBackground(Color.DARK_GRAY);
		bal.setFocusable(false);
		cos.put("ball",bal);
		bal.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange()!=ItemEvent.SELECTED) return;
				if (!conf) save(c,true);
			}
		});
		JLabel lab1 = new JLabel("Mode:");
		lab1.setBounds(135*(hs.size()/12)+10,(hs.size()%12)*20+17,40,15);
		lab1.setForeground(Color.LIGHT_GRAY);
		//ipn.add(lab1);
		bal.setBounds(135*(hs.size()/12)+40,(hs.size()%12)*20+12,85,25);
		//ipn.add(bal);
		hs.put("bal", lab1);
		hs.put("bals", null);
		JLabel iflab = new JLabel("<html><b>Info Panel</b></html>",SwingConstants.CENTER);
		hs.put("Label",iflab);
		iflab.setForeground(Color.LIGHT_GRAY);
		iflab.setBounds(135*((hs.size()-1)/14)+10,60*4+10,125,50);
		ipn.add(iflab);
		ipn.setBackground(Color.DARK_GRAY);
		ipn.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		w=(int)(140*(Math.ceil(hs.size()/14.0)));
		ipn.setBounds(x,10,w,300);
		x+=w+p;
		panel.add(ipn);
		/* Conf Panel */
		cop = new JPanel();
		cop.setLayout(null);
		String[] s = new String[]{"Hue","Red","Green","Blue","Sat","Val"};
		for (int i=0;i<s.length;i++) {
			String st = s[i];
			JPanel cpan = new JPanel();
			cpan.setBackground(Color.DARK_GRAY);
			lab = new JLabel(st.substring(0,1),SwingConstants.CENTER);
			lab.setForeground(Color.LIGHT_GRAY);
			cpan.add(lab);
			JSpinner min = new JSpinner(new SpinnerNumberModel(cols.get(cob.getSelectedItem().toString().toLowerCase())[i][0].intValue(),0,255,1));
			min.setForeground(Color.LIGHT_GRAY);
			min.setBackground(Color.DARK_GRAY);
			min.setFocusable(false);
			cpan.add(min);
			JSpinner max = new JSpinner(new SpinnerNumberModel(cols.get(cob.getSelectedItem().toString().toLowerCase())[i][1].intValue(),0,255,1));
			max.setForeground(Color.LIGHT_GRAY);
			max.setBackground(Color.DARK_GRAY);
			max.setFocusable(false);
			cpan.add(max);
			cpan.setBounds(135*(cos.size()/14)+10,((cos.size()-1)%14)*33-30,125,33);
			cos.put(st,cpan);
			cop.add(cpan);
		}
		JButton but = new JButton("Save");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				save(c,false);
			}
		});		
		but.setFocusable(false);
		cos.put("Save",but);
		but.setBounds(135*(cos.size()/14)+10,(cos.size()%14)*43-185,125,24);
		cop.add(but);
		final JToggleButton freeze = new JToggleButton("Freeze");
		but.setFocusable(false);
		cos.put("Freeze",freeze);
		freeze.setBounds(135*(cos.size()/14)+10,(cos.size()%14)*43-195,125,24);
		cop.add(freeze);
		JLabel colab = new JLabel("<html><b>Config Panel</b></html>",SwingConstants.CENTER);
		cos.put("Label",colab);
		colab.setForeground(Color.LIGHT_GRAY);
		colab.setBounds(135*((cos.size()-1)/14)+10,60*4+10,125,50);
		cop.add(colab);
		cop.setBackground(Color.DARK_GRAY);
		cop.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		w=(int)(140*(Math.ceil(cos.size()/14.0)));
		if (conf) {
			cop.setBounds(x,10,w,300);
			x+=w+p;
			panel.add(cop);
		}
		ctr = new JPanel();
		JLabel dlab = new JLabel("<html><b>Driver</b></html>",SwingConstants.CENTER);
		dlab.setForeground(Color.LIGHT_GRAY);
		ctr.add(dlab);
		JLabel dcontrol = new JLabel(new ImageIcon(ImageIO.read(Dashboard.class.getClass().getResource("/driver.png")).getScaledInstance(200,124,BufferedImage.SCALE_SMOOTH)));
		ctr.add(dcontrol);
		JLabel slab = new JLabel("<html><b>Shooter</b></html>",SwingConstants.CENTER);
		slab.setForeground(Color.LIGHT_GRAY);
		ctr.add(slab);
		JLabel scontrol = new JLabel(new ImageIcon(ImageIO.read(Dashboard.class.getClass().getResource("/shooter.png")).getScaledInstance(200,124,BufferedImage.SCALE_SMOOTH)));
		ctr.add(scontrol);
		ctr.setBackground(Color.DARK_GRAY);
		ctr.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		w=210;
		ctr.setBounds(x,10,w,300);
		panel.add(ctr);
		x+=w+p;
		/* Camera */
		buf = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
		Graphics g = buf.createGraphics();
		g.setColor(Color.RED);
		g.fillRect(0, 0, 320, 240);
		final JLabel camera = new JLabel(new ImageIcon(buf));
		w=320;
		camera.setBounds(x,10,w,240);
		if (conf) {
			x+=w+p;
			panel.add(camera);
		}
		sbuf = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
		Graphics h = sbuf.createGraphics();
		h.setColor(Color.RED);
		h.fillRect(0, 0, 320, 240);
		final JLabel satcamera = new JLabel(new ImageIcon(buf));
		frame.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent arg0) {
				if (arg0.getX()>=satcamera.getX()&&arg0.getX()<=satcamera.getX()+satcamera.getWidth()&&arg0.getY()>=satcamera.getY()&&arg0.getY()<=satcamera.getY()+satcamera.getHeight()) {
					Color c = new Color(raw.getRGB(arg0.getX()-satcamera.getX(), arg0.getY()-satcamera.getY()));
					float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
					frame.setTitle(String.format("H: %s S: %s V: %s R: %s G: %s B: %s", hsb[0]*255, hsb[1]*255, hsb[2]*255, c.getRed(), c.getGreen(), c.getBlue()));
				}
				else {frame.setTitle("Tough Techs 151 Dashboard 2012");}
			}
			@Override
			public void mouseDragged(MouseEvent arg0) {
				
			}
		});
		w=320;
		satcamera.setBounds(x,10,w,240);
		x+=w+p;
		panel.add(satcamera);
		new Timer().scheduleAtFixedRate(new TimerTask(){
			public void run() {
				try {
					if (!freeze.isSelected()) {
						buf = ImageIO.read(new URL("http://10.1.53.11/axis-cgi/jpg/image.cgi?resolution=320x240"));
						/* ************Screenshot Simulator Start****** */
						//File[] files = new File("C:\\Users\\RedSoxFan\\.smplayer\\screenshots").listFiles();
						//buf = ImageIO.read(files[(int)(Math.random()*files.length)]).getSubimage(993,35,320,240);
						/* *************Screenshot Simulator End******* */
						if (!sf.exists()) sf.mkdirs();
						final File scr = new File(sf.getAbsolutePath()+File.separator+"screen"+screen+".png");
						screen++;
						final BufferedImage sci = new BufferedImage(buf.getWidth()*3,buf.getHeight(),BufferedImage.TYPE_INT_RGB);
						Graphics s = sci.createGraphics();
						s.drawImage(buf,0,0,null);
						sbuf = new BufferedImage(buf.getWidth(), buf.getHeight(), BufferedImage.TYPE_INT_RGB);
						BufferedImage img = new BufferedImage(buf.getWidth(),buf.getHeight(),BufferedImage.TYPE_INT_RGB);
						Graphics g = img.getGraphics();
						g.drawImage(buf,0,0,null);
						raw = new BufferedImage(buf.getWidth(),buf.getHeight(),BufferedImage.TYPE_INT_RGB);
						Graphics r = raw.getGraphics();
						r.drawImage(buf,0,0,null);
						HSVTracker h;
						try {
							h=new HSVTracker(buf,sbuf,3,0.5);
							camera.setIcon(new ImageIcon(buf.getScaledInstance(320, 240, BufferedImage.SCALE_REPLICATE)));
							s.drawImage(buf,buf.getWidth(),0,null);
							satcamera.setIcon(new ImageIcon(h.real));
							s.drawImage(h.real,buf.getWidth()*2,0,null);
							int c=0; 
							distance=0;
							for (Target t:HSVTracker.targets) {
								c+=t==null?0:1;
								distance+=t==null?0:t.distance;
							}
							if (c!=0) distance/=c;						
							hs.get("Targets Found").setText("Targets Found: "+c);
							hs.get("Distance").setText("Distance: "+(distance/12)+"' "+(distance%12)+"\"");
						} catch (Exception e) {e.printStackTrace();}
						new Thread(){
							public void run(){
								try {
									ImageIO.write(sci,"png",scr);
								} catch (IOException e) {
								}
							}
						}.start();
					}
				} catch (Exception e) {
				}
			}
		},1,20);
		/* Logos */
		JLabel tt = new JLabel(new ImageIcon(ImageIO.read(Dashboard.class.getClass().getResource("/ttlogo.png")).getScaledInstance(200, 68, BufferedImage.SCALE_REPLICATE)));
		tt.setBounds(x-270,253,200,65);
		panel.add(tt);
		frame.setSize(new Dimension(x+p,355));
		frame.setResizable(false);
		//frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.setVisible(true);
		frame.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {}
			public void windowClosed(WindowEvent arg0) {
				conn.sendData("[Disconnect]");
				conn.disconnect();
			}
			public void windowActivated(WindowEvent arg0) {}
		});
		conn = new ServerConnection(1735, this);
	}
	public void onConnect(Socket s) {
		System.out.println("[Connected]");
	}
	public void load() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(f));
		String str="";
		while ((str=br.readLine())!=null) {
			str=str.toLowerCase();
			if (str.startsWith("sel=")) {
				((JComboBox)cos.get("drop")).setSelectedItem(str.substring(4,5).toUpperCase()+str.substring(5));
			} else if (str.startsWith("height=")||str.startsWith("vms")) {
				try {
					((JSpinner)((JPanel)cos.get(str.substring(0,str.indexOf("=")))).getComponent(1)).setValue(Double.parseDouble(str.substring(str.indexOf("=")+1)));
				} catch (Exception e) {
					if (str.startsWith("height=")) h=Double.parseDouble(str.substring(str.indexOf("=")+1));
					else if (str.startsWith("vms=")) vms=Double.parseDouble(str.substring(str.indexOf("=")+1));
				}
			} else if (str.startsWith("ball=")) {
				try {
					((JComboBox)cos.get("ball")).setSelectedItem(str.substring(str.indexOf("=")+1));
				} catch (Exception e){
					b=str.substring(str.indexOf("=")+1);
				}
			} else if (str.startsWith("red")||str.startsWith("blue")||str.startsWith("green")||str.startsWith("yellow")) {
				String val = str.substring(str.indexOf("=")+1);
				String[] st = str.substring(0,str.indexOf("=")).split("\\.");
				if (!cols.containsKey(st[0])) cols.put(st[0],new Integer[][]{new Integer[]{0,255},new Integer[]{0,255},new Integer[]{0,255},new Integer[]{0,255},new Integer[]{0,255},new Integer[]{0,255}});
				Integer[][] ob = cols.get(st[0]);
				ob[st[1].equals("hue")?0:st[1].equals("red")?1:st[1].equals("green")?2:st[1].equals("blue")?3:st[1].equals("sat")?4:5][st[2].equals("min")?0:1]=Integer.parseInt(val);
			}
		}
	}
	public void save(Object[] c, boolean silent) {
		try {
			PrintWriter pw = new PrintWriter(f);
			for (Object co:c) {
				co = co.toString().toLowerCase();
				if (((JComboBox)cos.get("drop")).getSelectedItem().toString().toLowerCase().equals(co)) {
					JPanel jp = (JPanel)cos.get("Hue");
					pw.println(""+co+".hue.min="+((JSpinner)jp.getComponent(1)).getValue().toString());
					pw.println(""+co+".hue.max="+((JSpinner)jp.getComponent(2)).getValue().toString());
					jp = (JPanel)cos.get("Red");
					pw.println(""+co+".red.min="+((JSpinner)jp.getComponent(1)).getValue().toString());
					pw.println(""+co+".red.max="+((JSpinner)jp.getComponent(2)).getValue().toString());
					jp = (JPanel)cos.get("Green");
					pw.println(""+co+".green.min="+((JSpinner)jp.getComponent(1)).getValue().toString());
					pw.println(""+co+".green.max="+((JSpinner)jp.getComponent(2)).getValue().toString());
					jp = (JPanel)cos.get("Blue");
					pw.println(""+co+".blue.min="+((JSpinner)jp.getComponent(1)).getValue().toString());
					pw.println(""+co+".blue.max="+((JSpinner)jp.getComponent(2)).getValue().toString());
					jp = (JPanel)cos.get("Sat");
					pw.println(""+co+".sat.min="+((JSpinner)jp.getComponent(1)).getValue().toString());
					pw.println(""+co+".sat.max="+((JSpinner)jp.getComponent(2)).getValue().toString());
					jp = (JPanel)cos.get("Val");
					pw.println(""+co+".val.min="+((JSpinner)jp.getComponent(1)).getValue().toString());
					pw.println(""+co+".val.max="+((JSpinner)jp.getComponent(2)).getValue().toString());
				} else {
					pw.println(""+co+".hue.min="+cols.get(co)[0][0].toString());
					pw.println(""+co+".hue.max="+cols.get(co)[0][1].toString());
					pw.println(""+co+".red.min="+cols.get(co)[1][0].toString());
					pw.println(""+co+".red.max="+cols.get(co)[1][1].toString());
					pw.println(""+co+".green.min="+cols.get(co)[2][0].toString());
					pw.println(""+co+".green.max="+cols.get(co)[2][1].toString());
					pw.println(""+co+".blue.min="+cols.get(co)[3][0].toString());
					pw.println(""+co+".blue.max="+cols.get(co)[3][1].toString());
					pw.println(""+co+".sat.min="+cols.get(co)[4][0].toString());
					pw.println(""+co+".sat.max="+cols.get(co)[4][1].toString());
					pw.println(""+co+".val.min="+cols.get(co)[5][0].toString());
					pw.println(""+co+".val.max="+cols.get(co)[5][1].toString());
				}
			}
			pw.println("sel="+((JComboBox)cos.get("drop")).getSelectedItem().toString().toLowerCase());
			pw.println("ball="+((JComboBox)cos.get("ball")).getSelectedItem());
			pw.flush();
			if (!silent) JOptionPane.showMessageDialog(frame,"Saved");
			load();
		} catch (Exception e) {
			if (!silent) JOptionPane.showMessageDialog(frame,"Error: "+e.getMessage());
			e.printStackTrace();
		}
	}
	public void onDisconnect(Socket s) {
		for (int i=3;i<10;i++) can_labels.get(i-1).setText(can(i,"Unused","N/A","N/A","N/A"));
		for (int i=1;i<5;i++) pwm_in_labels.get(i-1).setText(pwm("Input",i,"Unused","N/A",false));
		for (int i=1;i<5;i++) pwm_out_labels.get(i-1).setText(pwm("Output",i,"Unused","N/A",false));
		for (String st:hs.keySet()) if (!st.equals("Label")) hs.get(st).setText(st+": N/A");
	}
	public void onDataRecieved(Socket s, String str) {
		System.out.println(str);
		String function = str.substring(0,str.indexOf("["));
		String[] args = str.substring(str.indexOf("[")+1,str.indexOf("]")).split(",");
		if (function.equals("CAN")) {
			can_labels.get(Integer.parseInt(args[0])-2).setText(can(Integer.parseInt(args[0]),args[1],args[2],args[3],args[4]));
		} else if (function.equals("Track")) {
			HSVTracker.drive(HSVTracker.targets.get(0),this.conn,buf.getWidth());
		}
		else if (function.equals("PWM")) {
			JLabel l;
			if (args[0].equals("Input")) l=pwm_in_labels.get(Integer.parseInt(args[1])-1);
			else l=pwm_out_labels.get(Integer.parseInt(args[1])-1);
			l.setText(pwm(args[0],Integer.parseInt(args[1]),args[2],args[3],false));
		} else if (function.equals("INFO")) {
			hs.get(args[0]).setText(args[0]+": "+args[1]);
		} else if (function.equals("DRAW")) {
			draw.add(new Integer[]{Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]),Integer.parseInt(args[5]),Integer.parseInt(args[6])});
		} else if (function.equals("CLEAR")) {
			draw.clear();
		} else if (function.equals("ADD")) {
			if (hs.get(args[0])==null) {
				JLabel lab = new JLabel(args[0]+": N/A");
				hs.put(args[0],lab);
				lab.setForeground(Color.LIGHT_GRAY);
				lab.setBounds(10,(hs.size()-1)*20+10,125,15);
				ipn.add(lab);
			} else {
				hs.get(args[0]).setVisible(true);
			}
		} else if (function.equals("VIS")) {
			if (args[0].equals("CAN")) can_labels.get(Integer.parseInt(args[1])-1).setVisible(Boolean.parseBoolean(args[2]));
			else if (args[0].equals("PWM_IN")) pwm_in_labels.get(Integer.parseInt(args[1])-1).setVisible(Boolean.parseBoolean(args[2]));
			else if (args[0].equals("PWM_OUT")) pwm_out_labels.get(Integer.parseInt(args[1])-1).setVisible(Boolean.parseBoolean(args[2]));
			else if (args[0].equals("INFO") && hs.get(args[0])!=null) hs.get(Integer.parseInt(args[1])-1).setVisible(Boolean.parseBoolean(args[2]));
		} else if (function.equals("REQUEST")) {
			String data=null;
			if (args[0].equals("KINECT")) {
				data = b.equals("Kinect")?"1":"0";
			} else if (args[0].startsWith("DISTANCE-T")) {
				int target = Integer.parseInt(""+args[0].charAt(args[0].length()-1));
				Target ta=null;
				switch (target) {
					case 0: /* HIGHEST */
						for (Target t:HSVTracker.targets) {if (t!=null) {data=""+t.distance;break;}}
						if (data==null) data = "0";
						break;
					case 1: /* TOP */
						ta = HSVTracker.targets.get(0);
						data = ""+(ta!=null?ta.distance:0);
						break;
					case 2: /* Middle Left */
						ta = HSVTracker.targets.get(1);
						data = ""+(ta!=null?ta.distance:0);
						break;
					case 3: /* Middle Left */
						ta = HSVTracker.targets.get(1);
						if (ta!=null) data = ""+ta.distance;
						ta = HSVTracker.targets.get(2);
						if (ta!=null) data = ""+ta.distance;
						data="0";
						break;
					case 4: /* Middle Right */
						ta = HSVTracker.targets.get(2);
						data = ""+(ta!=null?ta.distance:0);
						break;
					case 5: /* Bottom */
						ta = HSVTracker.targets.get(3);
						data = ""+(ta!=null?ta.distance:0);
						break;
					case 6: /* LOWEST */
						for (int i=HSVTracker.targets.size()-1;i>=0;i--) {ta=HSVTracker.targets.get(i);if (ta!=null){data=""+ta.distance;break;}}
						if (data==null) data = "0";
						break;
				}
			} else if (args[0].equals("DISTANCE")) {
				data=""+distance;
			} else if (args[0].startsWith("OFFSET-T")) {
				int target = Integer.parseInt(""+args[0].charAt(args[0].length()-1));
				Target ta=null;
				switch (target) {
					case 0: /* HIGHEST */
						for (Target t:HSVTracker.targets) {if (t!=null) {data=""+t.offset;break;}}
						if (data==null) data = "0";
						break;
					case 1: /* TOP */
						ta = HSVTracker.targets.get(0);
						data = ""+(ta!=null?ta.offset:0);
						break;
					case 2: /* Middle Left */
						ta = HSVTracker.targets.get(1);
						data = ""+(ta!=null?ta.offset:0);
						break;
					case 3: /* Middle Left */
						ta = HSVTracker.targets.get(1);
						if (ta!=null) data = ""+ta.offset;
						ta = HSVTracker.targets.get(2);
						if (ta!=null) data = ""+ta.offset;
						data="0";
						break;
					case 4: /* Middle Right */
						ta = HSVTracker.targets.get(2);
						data = ""+(ta!=null?ta.offset:0);
						break;
					case 5: /* Bottom */
						ta = HSVTracker.targets.get(3);
						data = ""+(ta!=null?ta.offset:0);
						break;
					case 6: /* LOWEST */
						for (int i=HSVTracker.targets.size()-1;i>=0;i--) {ta=HSVTracker.targets.get(i);if (ta!=null){data=""+ta.offset;break;}}
						if (data==null) data = "0";
						break;
				}
			} else if (args[0].startsWith("SHOOT-T")) {
				int target = Integer.parseInt(""+args[0].charAt(args[0].length()-1));
				Target ta=null;
				switch (target) {
					case 0: /* HIGHEST */
						for (Target t:HSVTracker.targets) {if (t!=null) {data=""+t.offset;break;}}
						if (data==null) data = "0";
						break;
					case 1: /* TOP */
						ta = HSVTracker.targets.get(0);
						break;
					case 2: /* Middle Left */
						ta = HSVTracker.targets.get(1);
						break;
					case 3: /* Middle Left */
						ta = HSVTracker.targets.get(1);
						if (ta==null) ta = HSVTracker.targets.get(2);
						break;
					case 4: /* Middle Right */
						ta = HSVTracker.targets.get(2);
						break;
					case 5: /* Bottom */
						ta = HSVTracker.targets.get(3);
						break;
					case 6: /* LOWEST */
						for (int i=HSVTracker.targets.size()-1;i>=0;i--) {ta=HSVTracker.targets.get(i);if (ta!=null){break;}}
						break;
				}
				if (ta==null) data="0,0,0";
				else data=String.format("%s,%s~%s",((int)(Math.toDegrees(Math.atan(ta.offset*1.0/ta.distance))*100))/100.0,Trajectory.calculate(Trajectory.ft_M(ta.distance/12.,false),Trajectory.ft_M((ta.hoop.getHeight())/12.,false)-h,vms,0),1);
			} else {
				data = args[0].substring(5);
			}
			if (data!=null) {
				String cmd = "RETURN,"+args[0]+"&"+data;
				System.out.println(cmd);
				conn.sendData(cmd);
			}
		} else {
			System.out.println("??? "+function);
		}
	}
	private String can(int i, String comp, String speed, String input, String output){
		return String.format("<html>Board %s (%s):<br>&nbsp;&nbsp;&nbsp;&nbsp;Speed: %s<br>&nbsp;&nbsp;&nbsp;&nbsp;Input: %s<br>&nbsp;&nbsp;&nbsp;&nbsp;Output: %s</html>",i,comp,speed,input,output);
	}
	private String pwm(String put, int i, String comp, String speed, boolean complex) {
		if (complex) return String.format("<html>PWM %s %s (%s):<br>&nbsp;&nbsp;&nbsp;&nbsp;Value: %s",put,i,comp,speed);
		return String.format("PWM %s (%s): %s",i,comp,speed);
	}
	public void drawBox(BufferedImage buf) {
		Graphics g = buf.getGraphics();
		for (Integer[] b:draw) {
			g.setColor(Color.GREEN);
			g.drawRect(b[1],b[2],b[3],b[4]);
			g.setColor(Color.RED);
			g.drawRect(b[5]-1,b[6]-1,2,2);
		}
	}
}
