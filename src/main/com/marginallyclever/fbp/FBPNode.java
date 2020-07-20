package com.marginallyclever.fbp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

/**
 * special thanks to https://www.codeproject.com/Articles/116088/Draggable-Components-in-Java-Swing
 * @author Dan Royer
 * @since 7.24.0
 *
 */
public class FBPNode extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    protected ArrayList<FBPComponent> components = new ArrayList<FBPComponent>();
    protected boolean denyDrag;
    protected String myName;
    
    protected FBPNode() {
    	super();
    }
    
    public FBPNode(String name) {
    	this();
    	
        addDragListeners();
        //setBackground(new Color(240,240,240));
        setBorder(new LineBorder(Color.BLACK,1));
        setBackground(UIManager.getColor("Panel.background"));
        setOpaque(true);
        setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
        addFBPComponent(new FBPTitleBlock(name));
        myName=name;
    }
    
    public String getMyName() {
    	return myName;
    }
    
    // Add Mouse Motion Listener with drag function
    private void addDragListeners() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
            	//System.out.println("node::move "+myName);
                redispatch(e);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
            	//System.out.println("node::drag "+myName);
                redispatch(e);
            }
			
			protected void redispatch(MouseEvent e) {
		        Component source = (Component) e.getSource();
		        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, source.getParent());
		        source.getParent().dispatchEvent(parentEvent);
			}
        });
    	
        addMouseListener(new MouseListener() {
			@Override
            public void mouseClicked(MouseEvent e) {
				//System.out.println("node::click");
				redispatch(e);
            }

			@Override
			public void mousePressed(MouseEvent e) {
				//System.out.println("node::press");
				redispatch(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				//System.out.println("node::release");
				redispatch(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				System.out.println("node::enter");
				//redispatch(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				System.out.println("node::exit");
				//redispatch(e);
			}
			
			protected void redispatch(MouseEvent e) {
		        Component source = (Component) e.getSource();
		        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, source.getParent());
		        source.getParent().dispatchEvent(parentEvent);
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
