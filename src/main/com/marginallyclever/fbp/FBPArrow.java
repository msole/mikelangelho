package com.marginallyclever.fbp;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

public class FBPArrow extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int SIZE=8;

	public FBPArrow() {
		super();
		setPreferredSize(new Dimension(SIZE,SIZE));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(isEnabled()) {
			// draw connection point (if any)
			int x=0, y=getHeight()/2;
	
			g.translate(x, y);
			g.drawPolygon(new int[] {0,SIZE-1,0}, new int[] {-SIZE/2,0,SIZE/2}, 3);
			g.translate(-x, -y);
		}
	}
}
