package com.marginallyclever.fbp;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

// based on https://docs.oracle.com/javase/tutorial/uiswing/examples/layout/CustomLayoutDemoProject/src/layout/DiagonalLayout.java
public class FBPLayoutManager implements LayoutManager {
	private int vgap;
	private int minWidth = 0, minHeight = 0;
	private int preferredWidth = 0, preferredHeight = 0;
	private boolean sizeUnknown = true;


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
	
	    return dim;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		Dimension dim = new Dimension(0, 0);
	    int nComps = parent.getComponentCount();
	    
	    //Always add the container's insets!
	    Insets insets = parent.getInsets();
	    dim.width = minWidth + insets.left + insets.right;
	    dim.height = minHeight + insets.top + insets.bottom;
	
	    sizeUnknown = false;
	
	    return dim;
	}

	@Override
	public void layoutContainer(Container parent) {
		// TODO Auto-generated method stub
		
	}
	 
	// find the size of parent.
    private void setSizes(Container parent) {
        int nComps = parent.getComponentCount();
        Dimension d = null;
 
        //Reset preferred/minimum width and height.
        preferredWidth = 0;
        preferredHeight = 0;
        minWidth = 0;
        minHeight = 0;
 
        for (int i = 0; i < nComps; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                d = c.getPreferredSize();
 
                if (i > 0) {
                    preferredWidth += d.width/2;
                    preferredHeight += vgap;
                } else {
                    preferredWidth = d.width;
                }
                preferredHeight += d.height;
 
                minWidth = Math.max(c.getMinimumSize().width, minWidth);
                minHeight = preferredHeight;
            }
        }
    }
}
