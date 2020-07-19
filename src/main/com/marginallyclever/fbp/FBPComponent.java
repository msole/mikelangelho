package com.marginallyclever.fbp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

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
		//setBorder(BorderFactory.createLineBorder(Color.RED,1));
        setLayout(new BorderLayout());
        addArrows();
        setChild(child);
	}

	public FBPComponent(JComponent child) {
		this(child,NONE);
	}
	
	public FBPComponent() {
		this(new JLabel("null"),NONE);
	}
	
	public FBPComponent(int t) throws Exception {
		this(new JLabel("null"),t);
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
	
	protected void addArrows() {
        FBPArrow inbound=new FBPArrow();
        FBPArrow outbound=new FBPArrow();
    	add(inbound,BorderLayout.WEST);
    	add(outbound,BorderLayout.EAST);
        if(connectionType!=INBOUND) inbound.setEnabled(false);
        if(connectionType!=OUTBOUND) outbound.setEnabled(false);
	}
	
	public Component setChild(Component comp) {
		removeAll();
		addArrows();
		add(comp,BorderLayout.CENTER);
		
		// recalculate size
		int count = getComponentCount();
		int w = 0;
		int h = 0;
		for(int i=0; i<count; ++i) {
			Dimension d = getComponent(i).getPreferredSize();
			w += d.width;
			if( h<d.height ) h=d.height;
		}
		// add insets
		Insets in = getInsets();
		Dimension d2 = getPreferredSize();
		d2.width = w+in.right+in.left;
		d2.height = h+in.top+in.bottom;
    	setPreferredSize(d2);
    	setMinimumSize(d2);
    	setSize(d2);
		validate();
		
		return comp;
	}
}
