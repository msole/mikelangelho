package com.marginallyclever.fbp;


import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;


public class DesignPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<FBPPanel> panels = new ArrayList<FBPPanel>();
	ArrayList<FBPConnector> conns = new ArrayList<FBPConnector>();

    // 2D Point representing the coordinate where mouse is, relative parent container
    protected Point anchorPoint;
    
	public DesignPanel() {
		super();
		setLayout(new FBPLayoutManager());
		setBackground(UIManager.getColor("InternalFrame.background"));
		addDragListeners();
	}
	
	protected void addConnection(FBPComponent outBound,FBPComponent inBound) throws Exception {
	    if(outBound.getConnectionType()!=FBPComponent.OUTBOUND) throw new Exception("Must attach connection head to outbound component.");
	    if(inBound.getConnectionType()!=FBPComponent.INBOUND) throw new Exception("Must attach connection tail to inbound component.");

	    FBPConnector c = new FBPConnector(outBound,inBound);
	    System.out.println("Adding connection");
	    conns.add(c);
	    repaint();
	}

	protected FBPPanel addPanel() {
		FBPPanel d = new FBPPanel();
		d.setLocation(10+panels.size()*105,10+panels.size()*105);
		//d.setBounds(10+panels.size()*105,10+panels.size()*105,100, 100);
		add(d);
		panels.add(d);

		return d;
	}
	
    // Add Mouse Motion Listener with drag function
    private void addDragListeners() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
            	// remember the cursor position before a drag begins
                anchorPoint = e.getPoint();
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            	// find the relative movement (dx,dy)
                int anchorX = anchorPoint.x;
                int anchorY = anchorPoint.y;
                int dx = e.getX() - anchorX;
                int dy = e.getY() - anchorY;
                // cursor moved in drag, don't forget.
                anchorPoint.x = e.getX();
                anchorPoint.y = e.getY();

                // move all draggable components.  this is the same as moving the world, sort of.
                for( FBPPanel d : panels ) {
                	Rectangle r = d.getBounds();
                	r.x += dx;
                	r.y += dy;
                	d.setBounds(r);
                }
            }
        });
    }
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Point p = getLocationOnScreen();
		g.translate(-p.x,-p.y);
		for( FBPConnector c : conns ) {
			c.paint(g);
		}
		g.translate(p.x,p.y);
	}
	
	public static void testSizes() {
		JComponent [] c = {
				new JLabel("A"),
				new JButton("B"),
				new JSlider(0,100),
				new JComboBox<String>(new String[] {"1","2","3"}),
				new JCheckBox("D"),
				new JTextField("E"),
				new JTextArea(4,20),
		};
		int i=0;
		for( JComponent cc : c ) {
			Dimension d = cc.getPreferredSize();
			System.out.println(i+"="+d.width+","+d.height);
			FBPComponent f = new FBPComponent(cc);
			f.setSize(f.getPreferredSize());
			System.out.println(i+"-2="+f.getWidth()+","+f.getHeight());
			
			i++;
		}
	}
	
	public static void main(String[] argv) throws Exception {
		testSizes();
		
	    JFrame f = new JFrame();
	    f.setSize(800, 800);
	    f.setTitle("DesignPanel test");
	    f.setVisible(true);
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    DesignPanel p = new DesignPanel();
	    f.add(p);
		
		FBPPanel a0 = p.addPanel();
		FBPPanel a1 = p.addPanel();	
		FBPPanel a2 = p.addPanel();	
		FBPPanel a3 = p.addPanel();
		p.addPanel();
		
		try {
			FBPComponent title = new FBPComponent();
			title.setChild(new JLabel("A0"));
			a0.addFBPComponent(title);
			a0.addFBPComponent(new FBPComponent());
			title = new FBPComponent(new JSlider(0,50),FBPComponent.OUTBOUND);
			a0.addFBPComponent(title);
			
			a1.addFBPComponent(new FBPComponent(new JLabel("B"),FBPComponent.INBOUND));
			a1.addFBPComponent(new FBPComponent(new JLabel("C"),FBPComponent.OUTBOUND));
			a2.addFBPComponent(new FBPComponent(new JLabel("D"),FBPComponent.INBOUND));
			a2.addFBPComponent(new FBPComponent(new JLabel("E"),FBPComponent.OUTBOUND));
			a3.addFBPComponent(new FBPComponent(new JLabel("F"),FBPComponent.INBOUND));
			a3.addFBPComponent(new FBPComponent(new JLabel("G"),FBPComponent.INBOUND));
		    
			p.addConnection(a0.getOutbound(0),a1.getInbound(0));
			p.addConnection(a0.getOutbound(0),a2.getInbound(0));
			p.addConnection(a1.getOutbound(0),a3.getInbound(0));
			p.addConnection(a2.getOutbound(0),a3.getInbound(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
