package com.marginallyclever.fbp;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;

// based on https://docs.oracle.com/javase/tutorial/uiswing/examples/layout/CustomLayoutDemoProject/src/layout/DiagonalLayout.java
public class FBPLayoutManager implements LayoutManager {
	private int vgap;
	private int minWidth = 0, minHeight = 0;
	private int preferredWidth = 0, preferredHeight = 0;
	private boolean sizeUnknown = true;

	private int minX = 0, maxX = 0, minY = 0, maxY = 0;


	public FBPLayoutManager() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		// TODO Auto-generated method stub
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		Dimension dim = new Dimension(0, 0);
	    int nComps = parent.getComponentCount();
	    
	    setSizes(parent);
	
	    //Always add the container's insets!
	    Insets insets = parent.getInsets();
	    dim.width = preferredWidth + insets.left + insets.right;
	    dim.height = preferredHeight + insets.top + insets.bottom;
	
	    sizeUnknown = false;
	    
	    System.out.println(nComps + " elements, preferred="+dim.toString());
	    return dim;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		Dimension dim = new Dimension(0, 0);
	    
	    //Always add the container's insets!
	    Insets insets = parent.getInsets();
	    dim.width = minWidth + insets.left + insets.right;
	    dim.height = minHeight + insets.top + insets.bottom;
	
	    sizeUnknown = false;
	
	    int nComps = parent.getComponentCount();
	    System.out.println(nComps + " elements, min="+dim.toString());
	    return dim;
	}

	@Override
	public void layoutContainer(Container parent) {
		Dimension d = preferredLayoutSize(parent);
		parent.setSize(d);
	}
	 
	// find the size of parent.
    private void setSizes(Container parent) {
        int nComps = parent.getComponentCount();
        Dimension d = null;

        /*
        //Reset preferred/minimum width and height.
        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        maxY = Integer.MIN_VALUE;
        for (int i = 0; i < nComps; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                d = c.getPreferredSize();
                Point p = c.getLocation();
                if(minX > p.x) minX = p.x;
                if(minY > p.y) minY = p.y;
                int fx = p.x + d.width;
                int fy = p.y + d.height;
                if(maxX < fx) maxX = fx;
                if(maxY < fy) maxY = fy;
            }
        }*/
        d = parent.getSize();
        //if(maxX < d.width) maxX = d.width;
        //if(maxY < d.height) maxY = d.height;
        minX=0;
        minY=0;
        maxX = d.width;
        maxY = d.height;
        
        minWidth = maxX-minX;
        minHeight = maxY-minY;
        preferredWidth = maxX;
        preferredHeight = maxY;
    }
}
