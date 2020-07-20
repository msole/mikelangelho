package com.marginallyclever.fbp;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class FBPTitleBlock extends FBPComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    // 2D Point representing the coordinate where mouse is, relative parent container
    protected Point anchorPoint;

	protected FBPTitleBlock(JComponent child, int t) {
		super(child, t);
	}

	protected FBPTitleBlock(JComponent child) {
		super(child);
	}

	protected FBPTitleBlock() {
		super(new JLabel("Anonymous"),NONE);
	}

	public FBPTitleBlock(String name) {
		super(new JLabel(name),NONE);

		addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
            	System.out.println("titleBlock::move "+name);
            	
                anchorPoint = e.getPoint();
                setCursor(Cursor.getDefaultCursor());
                redispatch(e);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
            	System.out.println("titleBlock::drag "+name);
            	
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                int anchorX = anchorPoint.x;
                int anchorY = anchorPoint.y;

                Component p = getParent();
                Component p2 = p.getParent();
                if(p!=null && p2 != null) {
	                Point parentOnScreen = p2.getLocationOnScreen();
	                Point mouseOnScreen = e.getLocationOnScreen();
	                Point position = new Point( mouseOnScreen.x - parentOnScreen.x - anchorX, 
	                							mouseOnScreen.y - parentOnScreen.y - anchorY);
	                p.setLocation(position);
	                p2.repaint();
                }
            }
			
			protected void redispatch(MouseEvent e) {
		        Component source = (Component) e.getSource();
		        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, source.getParent());
		        source.getParent().dispatchEvent(parentEvent);
			}
        });
	}

	protected FBPTitleBlock(int t) throws Exception {
		super(t);
	}

}
