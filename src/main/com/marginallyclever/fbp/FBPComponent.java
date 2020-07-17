package com.marginallyclever.fbp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;

public class FBPComponent extends JPanel {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int NONE = 0;
	public static final int INBOUND = 1;
	public static final int OUTBOUND = 2;
	
	protected int connectionType=NONE;

	public FBPComponent(JComponent child,int t) {
		super();
		connectionType = t;
		setBorder(BorderFactory.createLineBorder(Color.RED,1));
        setLayout(new GridBagLayout());
        setChild(child);
	}
	public FBPComponent(JComponent child) {
		this(child,NONE);
	}
	
	public FBPComponent() {
		this(new JLabel(""));
	}
	
	public FBPComponent(int t) throws Exception {
		this(new JLabel(""),t);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// draw connection point (if any)
		int x=-1, y=getHeight()/2;

		switch(connectionType) {
		case NONE: return;
		case OUTBOUND:  x=getWidth()-10;  break;
		default: break;
		}
		g.translate(x, y);
		g.drawPolygon(new int[] {0,10,0}, new int[] {-5,0,5}, 3);
		g.translate(-x, -y);
	}
	
	public int getConnectionType() {
		return connectionType;
	}

	// returns location of the connection point relative to the screen
	public Point getConnectionPoint() throws Exception {
		Point p = new Point();
		Rectangle r = getBounds();
		
		switch(connectionType) {
		case INBOUND :  p.setLocation(0      , r.height/2);  break;
		case OUTBOUND:  p.setLocation(r.width, r.height/2);  break;
		default:  throw new Exception("Invalid connection type.");
		}
		
		Point loc = getLocationOnScreen();
		p.x+=loc.x;
		p.y+=loc.y;
		
		return p;
	}
	
	public Component setChild(Component comp) {
		GridBagConstraints gbc = new GridBagConstraints();
		
		if(getComponentCount()==1) {
			remove(0);
		}

		gbc.weightx=1;
		gbc.gridx=0;
		gbc.gridy=0;
		super.add(comp,gbc);
		validate();
		
		return comp;
	}
}
