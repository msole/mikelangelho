package com.marginallyclever.artPipeline.nodes;

import java.util.ArrayList;

import com.marginallyclever.artPipeline.TurtleNode;
import com.marginallyclever.artPipeline.TurtleNodePanel;
import com.marginallyclever.artPipeline.nodes.panels.Generator_KochCurve_Panel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Koch Curve fractal
 * @author Dan Royer
 */
public class Generator_KochCurve extends TurtleNode {
	private double xMax = 7;
	private double xMin = -7;
	private double yMax = 7;
	private double yMin = -7;
	private static int order = 4; // controls complexity of curve

	private double maxSize;
	
	@Override
	public String getName() {
		return Translator.get("KochTreeName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_KochCurve.order = order;
	}
	
	@Override
	public TurtleNodePanel getPanel() {
		return new Generator_KochCurve_Panel(this);
	}
	
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		double v = 100;
		xMax = v;
		yMax = v;
		xMin = -v;
		yMin = -v;
		
		double xx = xMax - xMin;
		double yy = yMax - yMin;
		maxSize = xx > yy ? xx : yy;
		
		// move to starting position
		turtle.moveTo(0,-yMax);
		turtle.turn(90);
		
		turtle.penDown();
		drawTriangle(turtle,order, maxSize);

		ArrayList<Turtle> list = new ArrayList<Turtle>();
		list.add(turtle);
		setTurtleResult(list);
		
	    return false;
	}


	// L System tree
	private void drawTriangle(Turtle turtle,int n, double distance) {
		if (n == 0) {
			turtle.forward(distance);
			return;
		}
		drawTriangle(turtle,n-1,distance/3.0f);
		if(n>1) {
			turtle.turn(-60);
			drawTriangle(turtle,n-1,distance/3.0f);
			turtle.turn(120);
			drawTriangle(turtle,n-1,distance/3.0f);
			turtle.turn(-60);
		} else {
			turtle.forward(distance/3.0f);
		}
		drawTriangle(turtle,n-1,distance/3.0f);
	}
}