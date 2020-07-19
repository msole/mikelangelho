package com.marginallyclever.fbp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * The link between two FBPConnectionPoint.
 * @author Dan Royer
 *
 */
public class FBPConnection {
	public FBPComponent in;
	public FBPComponent out;
	
	public FBPConnection() {}
	
	public FBPConnection(FBPComponent inBound,FBPComponent outBound) {
		this();
		in = inBound;
		out = outBound;
	}
	
	public void paint(Graphics g) {
		if(in==null || out==null) return;

		Color oldColor = g.getColor();
		g.setColor(Color.BLUE);

		try {
			Point pa = in.getConnectionPoint();
			Point pb = out.getConnectionPoint();
			g.drawLine(	pa.x, pa.y,
				    	pb.x, pb.y);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			g.setColor(oldColor);
		}
	}
}
