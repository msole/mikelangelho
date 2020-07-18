package com.marginallyclever.fbp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

/**
 * special thanks to https://www.codeproject.com/Articles/116088/Draggable-Components-in-Java-Swing
 * @author Dan Royer
 * @since 7.24.0
 *
 */
public class FBPPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    // 2D Point representing the coordinate where mouse is, relative parent container
    protected Point anchorPoint;
    // Default mouse cursor for dragging action
    //protected Cursor draggingCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    // If sets <b>TRUE</b> when dragging component, it will be painted over each other (z-Buffer change)
    protected boolean overbearing = false;

    protected ArrayList<FBPComponent> components = new ArrayList<FBPComponent>();
    protected boolean isIn=false;
    
    
    public FBPPanel() {
    	super();
    	
        addDragListeners();
        //setBackground(new Color(240,240,240));
        setBorder(new LineBorder(Color.BLACK,1));
        setBackground(UIManager.getColor("Panel.background"));
        setOpaque(true);
        setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
    }
    
    // Add Mouse Motion Listener with drag function
    private void addDragListeners() {
        // This handle is a reference to THIS because in next Mouse Adapter "this" is not allowed
        final FBPPanel handle = this;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
            	super.mouseEntered(e);
            	isIn=true;
            	repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
            	super.mouseExited(e);
            	isIn=false;
            	repaint();
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                anchorPoint = e.getPoint();
                setCursor(Cursor.getDefaultCursor());
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                int anchorX = anchorPoint.x;
                int anchorY = anchorPoint.y;

                Point parentOnScreen = getParent().getLocationOnScreen();
                Point mouseOnScreen = e.getLocationOnScreen();
                Point position = new Point( mouseOnScreen.x - parentOnScreen.x - anchorX, 
                							mouseOnScreen.y - parentOnScreen.y - anchorY);
                setLocation(position);

                // Change Z-Buffer if it is "overbearing"
                if (overbearing) {
                    getParent().setComponentZOrder(handle, 0);
                    repaint();
                }
                getParent().repaint();
            }
        });
    }

    // sum the height of all child components.
    // find the minimum width of the widest component.
    // set the preferred size to the resulting values.
    public void recalculateSize() {
    	int count = getComponentCount();
    	int width=0;
    	int height=0;
    	
    	for(int i=0;i<count;++i) {
    		Component c = getComponent(i);
    		Dimension d = c.getPreferredSize();
    		height+=d.height;
    		if(width<d.width) width=d.width;
    	}

		Insets in = getInsets();
		
		Dimension d2 = getPreferredSize();
		d2.width = width+in.right+in.left;
		d2.height = height+in.top+in.bottom;
		
    	setPreferredSize(d2);
    }
    
    public void addFBPComponent(FBPComponent arg0) {
    	add(arg0);
    	
    	components.add(arg0);
    	
    	recalculateSize();
    	
		setSize(getPreferredSize());
		validate();
    }
    
    public void removeFBPComponent(FBPComponent arg0) {
    	components.remove(arg0);
    	remove(arg0);
    }
    
    public FBPComponent getFBPComponent(int index) {
    	return components.get(index);
    }
    
    public FBPComponent getOutbound(int index) {
    	if(index<0) throw new IndexOutOfBoundsException();
    	
    	for( FBPComponent c : components ) {
    		if(c.getConnectionType()==FBPComponent.OUTBOUND) {
    			if(index==0) {
    				return c;
    			}
    			index--;
    		}
    	}
    	throw new IndexOutOfBoundsException();
    }
    
    public FBPComponent getInbound(int index) {
    	if(index<0) throw new IndexOutOfBoundsException();
    	
    	for( FBPComponent c : components ) {
    		if(c.getConnectionType()==FBPComponent.INBOUND) {
    			if(index==0) {
    				return c;
    			}
    			index--;
    		}
    	}
    	throw new IndexOutOfBoundsException();
    }
    
    @Override
    protected void paintComponent(Graphics g) {        
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        if(isOpaque()) {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
