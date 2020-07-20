package com.marginallyclever.fbp;


import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class FBPPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<FBPNode> panels = new ArrayList<FBPNode>();
	ArrayList<FBPConnection> conns = new ArrayList<FBPConnection>();

    // 2D Point representing the coordinate where mouse is, relative parent container
    protected Point anchorPoint;
    
    protected boolean isConnecting;
    protected FBPArrow start, end;
    
	public FBPPanel() {
		super();
		setLayout(null);
		setBackground(UIManager.getColor("InternalFrame.background"));
		addDragListeners();
		isConnecting=false;
	}
	
	protected void addConnection(FBPComponent outBound,FBPComponent inBound) throws Exception {
	    if(outBound.getConnectionType()!=FBPComponent.OUTBOUND) throw new Exception("Must attach connection head to outbound component.");
	    if(inBound.getConnectionType()!=FBPComponent.INBOUND) throw new Exception("Must attach connection tail to inbound component.");

	    FBPConnection c = new FBPConnection(outBound,inBound);
	    System.out.println("Adding connection");
	    conns.add(c);
	    repaint();
	}
	
	protected void removeConnection(FBPComponent arg0) {
		for(FBPConnection c : conns) {
			if(c.in==arg0 || c.out==arg0) {
				conns.remove(c);
				return;
			}
		}
	}

	protected FBPNode addPanel(String name) {
		FBPNode d = new FBPNode(name);
		d.setLocation(10+panels.size()*105,10+panels.size()*105);
		add(d);
		panels.add(d);

		return d;
	}
	
    // Add Mouse Motion Listener with drag function
    private void addDragListeners() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
        		//System.out.println("panel::move");
            	if(isConnecting) {
            		repaint();
            		return;
            	}
            	
            	// remember the cursor position before a drag begins
                anchorPoint = e.getPoint();
                setCursor(Cursor.getDefaultCursor());
				redispatch(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
            	if(isConnecting) {
            		//System.out.println("panel::connect");
            		repaint();
            		return;
            	}
        		//System.out.println("panel::drag");
            	
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
                for( FBPNode d : panels ) {
                	Rectangle r = d.getBounds();
                	r.x += dx;
                	r.y += dy;
                	d.setBounds(r);
                }
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
				//System.out.println("panel::click");
				redispatch(e);
            }

			@Override
			public void mousePressed(MouseEvent e) {
				//System.out.println("panel::press");
				if(isConnecting) return;  // should be impossible?
				
				start = getFBPArrowAt(e.getX(), e.getY());
				// only start connecting if cursor is on an arrow.
				if(start!=null) {
					FBPComponent cStart = (FBPComponent)start.getParent();
					// kill any connection going to the component that owns this arrow.
					removeConnection(cStart);
					
					FBPNode nStart = (FBPNode)cStart.getParent();
					System.out.println("connection started "+nStart.getMyName());
					isConnecting=true;
					return;
				}
				
				redispatch(e);
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				System.out.println("panel::release");
				if(!isConnecting) return;  // don't care
				isConnecting=false;
				
				end = getFBPArrowAt(event.getX(), event.getY());
				if(end!=null) {
					FBPComponent cEnd = (FBPComponent)end.getParent();
					FBPNode nEnd = (FBPNode)cEnd.getParent();
					System.out.println("connection finished "+nEnd.getMyName());
					
					FBPComponent a = (FBPComponent)start.getParent();
					FBPComponent b = (FBPComponent)end.getParent();
					if(a.getConnectionType() != b.getConnectionType()) {
						// must be opposite types
						if(a!=b) {
							// cannot connect to self
							try {
								if(a.getConnectionType()==FBPComponent.INBOUND) {
									addConnection(b,a);
	        					} else {
									addConnection(a,b);
	        					}
							} catch(Exception exception) {
								exception.printStackTrace();
							}
						}
					}
				}
				repaint();
				redispatch(event);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				System.out.println("panel::enter");
				redispatch(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				System.out.println("panel::exit");
				redispatch(e);
			}
			
			protected void redispatch(MouseEvent e) {
		        Component source = (Component) e.getSource();
		        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, source.getParent());
		        source.getParent().dispatchEvent(parentEvent);
			}
        });
    }
	
    protected FBPArrow getFBPArrowAt(int x,int y) {
		Component maybeArrow = SwingUtilities.getDeepestComponentAt(this, x, y);
		if(maybeArrow instanceof FBPArrow && maybeArrow.isEnabled()) {
			if(maybeArrow.getParent() instanceof FBPComponent) {
				// definitely enabled arrow
				return (FBPArrow)maybeArrow;
			}
		}
		return null;
    }
    
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Point p = getLocationOnScreen();
		g.translate(-p.x,-p.y);
		for( FBPConnection c : conns ) {
			c.paint(g);
		}
		g.translate(p.x,p.y);
		
		if(isConnecting) {

			Color oldColor = g.getColor();
			g.setColor(Color.BLUE);

			try {
				Point pa = ((FBPComponent)start.getParent()).getConnectionPoint();
				Point pb = MouseInfo.getPointerInfo().getLocation();
				Point pc = getLocationOnScreen();
				g.translate(-pc.x, -pc.y);
				g.drawLine(	pa.x, pa.y,
					    	pb.x, pb.y);
				g.translate(pc.x, pc.y);
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				g.setColor(oldColor);
			}
		}
		
	}
	
	public static void testSizes() {
		JComponent [] c = {
				new JLabel("A"),
				new JButton("B"),
				new JSlider(0,100),
				new JComboBox<String>(new String[] {"1","2","3"}),
				new JCheckBox("D"),
				new JTextField("E"),
				new JTextArea(4,20),
		};
		int i=0;
		for( JComponent cc : c ) {
			Dimension d = cc.getPreferredSize();
			System.out.println(i+"="+d.width+","+d.height);
			FBPComponent f = new FBPComponent(cc);
			f.setSize(f.getPreferredSize());
			System.out.println(i+"-2="+f.getWidth()+","+f.getHeight());
			
			i++;
		}
	}
	
	public static void main(String[] argv) throws Exception {
		testSizes();
		
	    JFrame f = new JFrame();
	    f.setSize(800, 800);
	    f.setTitle("DesignPanel test");
	    f.setVisible(true);
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    FBPPanel p = new FBPPanel();
	    f.add(p);
		
		FBPNode a0 = p.addPanel("A0");
		FBPNode a1 = p.addPanel("A1");	
		FBPNode a2 = p.addPanel("A2");	
		FBPNode a3 = p.addPanel("A3");
		p.addPanel("A4");
		
		try {
			FBPComponent title = new FBPComponent();
			title.setChild(new JLabel("A0"));
			a0.addFBPComponent(title);
			a0.addFBPComponent(new FBPComponent());
			title = new FBPComponent(new JSlider(0,50),FBPComponent.OUTBOUND);
			a0.addFBPComponent(title);
			
			a1.addFBPComponent(new FBPComponent(new JLabel("B"),FBPComponent.INBOUND));
			a1.addFBPComponent(new FBPComponent(new JLabel("C"),FBPComponent.OUTBOUND));
			a2.addFBPComponent(new FBPComponent(new JLabel("D"),FBPComponent.INBOUND));
			a2.addFBPComponent(new FBPComponent(new JLabel("E"),FBPComponent.OUTBOUND));
			a3.addFBPComponent(new FBPComponent(new JLabel("F"),FBPComponent.INBOUND));
			a3.addFBPComponent(new FBPComponent(new JLabel("G"),FBPComponent.INBOUND));
		    
			p.addConnection(a0.getOutbound(0),a1.getInbound(0));
			p.addConnection(a0.getOutbound(0),a2.getInbound(0));
			p.addConnection(a1.getOutbound(0),a3.getInbound(0));
			p.addConnection(a2.getOutbound(0),a3.getInbound(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		p.revalidate();
	}
}
