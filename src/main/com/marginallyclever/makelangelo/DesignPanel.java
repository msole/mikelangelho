package com.marginallyclever.makelangelo;


import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

import com.marginallyclever.fbp.FBPComponent;
import com.marginallyclever.fbp.FBPConnector;
import com.marginallyclever.fbp.FBPLayoutManager;
import com.marginallyclever.fbp.FBPPanel;


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
	
	protected void addConn(FBPComponent outBound,FBPComponent inBound) throws Exception {
	    if(outBound.getConnectionType()!=FBPComponent.OUTBOUND) throw new Exception("Must attach connection head to outbound component.");
	    if(inBound.getConnectionType()!=FBPComponent.INBOUND) throw new Exception("Must attach connection tail to inbound component.");

	    FBPConnector c = new FBPConnector(outBound,inBound);
	    System.out.println("Adding connection");
	    conns.add(c);
	}

	protected FBPPanel addPanel() {
		FBPPanel d = new FBPPanel();
		panels.add(d);
		add(d);
		d.setBounds(250+panels.size()*105,250+panels.size()*105,100, 100);		 

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
	
	public static void main(String[] argv) throws Exception {
	    JFrame f = new JFrame();
	    f.setSize(900, 900);
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
			title.add(new JLabel("A0"));
			a0.addFBPComponent(title);
			
			//a0.addFBPComponent(new FBPComponent(FBPComponent.OUTBOUND));
			
			title = new FBPComponent(new JSlider(0,50),FBPComponent.OUTBOUND);
			a0.addFBPComponent(title);
			
			a1.addFBPComponent(new FBPComponent(new JLabel("B"),FBPComponent.INBOUND));
			a1.addFBPComponent(new FBPComponent(new JLabel("C"),FBPComponent.OUTBOUND));
			a2.addFBPComponent(new FBPComponent(new JLabel("D"),FBPComponent.INBOUND));
			a2.addFBPComponent(new FBPComponent(new JLabel("E"),FBPComponent.OUTBOUND));
			a3.addFBPComponent(new FBPComponent(new JLabel("F"),FBPComponent.INBOUND));
			a3.addFBPComponent(new FBPComponent(new JLabel("G"),FBPComponent.INBOUND));
		    
			p.addConn(a0.getOutbound(0),a1.getInbound(0));
			p.addConn(a0.getOutbound(0),a2.getInbound(0));
			p.addConn(a1.getOutbound(0),a3.getInbound(0));
			p.addConn(a2.getOutbound(0),a3.getInbound(1));
			
			//p.invalidate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
