package com.marginallyclever.artPipeline;

import java.util.ArrayList;

import javax.vecmath.Vector2d;

import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.convenience.turtle.TurtleMove;
import com.marginallyclever.convenience.turtle.TurtleMoveType;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * See https://www.marginallyclever.com/2019/12/lets-talk-about-where-makelangelo-software-is-going-in-2020/
 * @author Dan Royer
 * 
 */
public class ArtPipeline {
	protected ArrayList<ArtPipelineListener> listeners = new ArrayList<ArtPipelineListener>();

	protected MakelangeloRobotSettings lastSettings = null;
	protected Turtle lastTurtle = null;

	private boolean shouldResizeFill;
	private boolean shouldResizeFit;
	private boolean shouldReorder;
	private boolean shouldFlipV;
	private boolean shouldFlipH;
	private boolean shouldSimplify;
	private boolean shouldCrop;
	
	public ArtPipeline() {}
	
	public void addListener(ArtPipelineListener arg0) {
		listeners.add(arg0);
	}
	
	public void removeListener(ArtPipelineListener arg0) {
		listeners.remove(arg0);
	}
	
	private void notifyListeners(ArtPipelineEvent evt) {
		for(ArtPipelineListener p : listeners) p.pipelineEvent(evt);
	}
	
	// assumes extPoint is a point which lies on the infinite extension of targetLine
 	private void extendLine(LineSegment2D targetLine, Point2D extPoint) {
		double newLengthA = lengthSquared(targetLine.a, extPoint);
		double newLengthB = lengthSquared(targetLine.b, extPoint);
		double currentLength = targetLine.lengthSquared();
		
		// Maximize length of target line by replacing the start or end point with the extPoint		
		if(newLengthA > currentLength && newLengthA > newLengthB) {
			// Draw line from targetLine.a to extPoint
			targetLine.b = extPoint;
		} else if(newLengthB > currentLength) {
			// Draw line from extPoint to targetLine.b 
			targetLine.a = extPoint;
		}
	}
	
	/**
	 * Offers to look for a better route through the turtle history that means fewer travel moves.
	 * @param turtle
	 * @param settings
	 */
	private void reorder(Turtle turtle, MakelangeloRobotSettings settings) {
		if(turtle.history.size()==0) return;
		
		System.out.println("reorder() begin");
		System.out.println("  before: "+turtle.history.size()+" instructions.");
		
		// history is made of changes, travels, and draws
		// look at the section between two changes.
		//   look at all pen down moves in the section.
		//     if two pen down moves share a start/end, then they are connected in sequence.
		
		ArrayList<LineSegment2D> originalLines = convertTurtleToLines(turtle);
		int originalCount = originalLines.size();
		System.out.println("  Converted to "+originalCount+" lines.");

		ArrayList<LineSegment2D> uniqueLines = removeDuplicates(originalLines,0.001);
		int uniqueCount = uniqueLines.size();
		int duplicateCount = originalCount - uniqueCount;
		System.out.println("  - "+duplicateCount+" duplicates = "+uniqueCount+" lines.");

		ArrayList<LineSegment2D> orderedLines = greedyReordering(uniqueLines);
		Turtle t = convertLinesToTurtle(orderedLines,1);

		System.out.println("  after: "+t.history.size()+" instructions.");
		turtle.history = t.history;
		System.out.println("reorder() end");
	}

	private ArrayList<LineSegment2D> removeVeryShortSegments(ArrayList<LineSegment2D> toTest, double d) {
		ArrayList<LineSegment2D> toKeep = new ArrayList<LineSegment2D>();
		int count = toTest.size();
		if(count==0) return toKeep;
		
		toKeep.add(toTest.get(0));
		LineSegment2D first = toKeep.get(toKeep.size()-1); 

		for(int i=1;i<count;++i) {
			LineSegment2D second = toTest.get(i);
			// sequential with no jump?
			if(lengthSquared(first.b,second.a)<d) {
				// very short?
				if(lengthSquared(second.a,second.b)<d) {
					first.b=second.b;
					continue;
				}

				// colinear?
				Vector2d firstN = makeUnitVectorFromLineSegment(first);
				Vector2d secondN = makeUnitVectorFromLineSegment(second);
				if(firstN.dot(secondN)>0.9999) {
					first.b=second.b;
					continue;
				}
				second.a=first.b;
			}
			
			toKeep.add(second);
			first = second;
		}
		return toKeep;
	}

	private Vector2d makeUnitVectorFromLineSegment(LineSegment2D line) {
		Vector2d n = new Vector2d();
		n.x = line.b.x - line.a.x;
		n.y = line.b.y - line.a.y;
		n.normalize();
		return n;
	}

	private Turtle convertLinesToTurtle(ArrayList<LineSegment2D> orderedLines,double epsilon) {
		Turtle t = new Turtle();
		if(orderedLines.isEmpty()) return t;
		
		LineSegment2D first = orderedLines.get(0); 
		t.jumpTo(first.a.x,first.a.y);
		Point2D currentPosition = new Point2D(first.b.x, first.b.y);
		
		for( LineSegment2D line : orderedLines ) {
			// change color if needed
			if(line.c!=t.getColor()) {
				t.setColor(line.c);
			}
			
			if(lengthSquared(currentPosition, line.a) > epsilon) {
				// The previous line ends too far from the start point of this line,
				// need to make a travel with the pen up to the start point of this line.
				t.jumpTo(line.a.x,line.a.y);
			} else {
				// The previous line ends close to the start point of this line,
				// so there's no need to go to the start point of this line since the pen is practically there.
				// The start point of this line will be skipped.
				//t.moveTo(line.a.x,line.a.y);
			}
			// Make a pen down move to the end of this line
			t.moveTo(line.b.x,line.b.y);
			currentPosition.set(line.b.x,line.b.y);
		}
		
		return t;
	}

	private ArrayList<LineSegment2D> greedyReordering(ArrayList<LineSegment2D> uniqueLines) {
		System.out.println("greedyReordering()");
		ArrayList<LineSegment2D> orderedLines = new ArrayList<LineSegment2D>();
		if(uniqueLines.isEmpty()) return orderedLines;

		Point2D lastPosition = uniqueLines.get(0).a;
		
		while(!uniqueLines.isEmpty()) {
			double bestD = Double.MAX_VALUE;
			LineSegment2D bestLine = null;
			boolean bestFlip = false;
			
			for( LineSegment2D line : uniqueLines ) {
				// is either end of line closer than our best?
				double dA = lengthSquared(lastPosition, line.a);
				double dB = lengthSquared(lastPosition, line.b);
				double nearest = Math.min(dA, dB);
				if(bestD > nearest) {
					bestD = nearest;
					bestLine = line;
					bestFlip = (dB < dA);
				}
			}
			
			if(bestFlip) bestLine.flip();
			
			uniqueLines.remove(bestLine);
			orderedLines.add(bestLine);
			
			// Start next iteration where current line ends.
			lastPosition = bestLine.b;
		}
		
		return orderedLines;
	}

	private ArrayList<LineSegment2D> removeDuplicates(ArrayList<LineSegment2D> originalLines, double EPSILON2) {
		System.out.println("removeDuplicates()");
		ArrayList<LineSegment2D> uniqueLines = new ArrayList<LineSegment2D>();

		for(LineSegment2D candidateLine : originalLines) {
			boolean isDuplicate = false;
			
			// Compare this line to all the lines previously marked as non-duplicate
			for( LineSegment2D uniqueLine : uniqueLines ) {
				// Check if lines are colinear
				if( uniqueLine.ptLineDistSq(candidateLine.a) < EPSILON2 &&
					uniqueLine.ptLineDistSq(candidateLine.b) < EPSILON2 ) {
					// they are!
					// if they touch or overlap then I have a candidate.
					// measure where the points are relative to each other.
					boolean candidateStartsCloseToUnique = uniqueLine.ptSegDistSq(candidateLine.a) < EPSILON2;
					boolean candidateEndsCloseToUnique = uniqueLine.ptSegDistSq(candidateLine.b) < EPSILON2;
					
					if(candidateStartsCloseToUnique) {
						if(candidateEndsCloseToUnique) {
							// Candidate doesn't add anything which isn't already covered by the unique line.
							// No further action needed.
						} else {
							// Partial overlap, extend uniqueLine
							extendLine(uniqueLine, candidateLine.b);
						}
						isDuplicate = true;
						break;
					} else if(candidateEndsCloseToUnique) {
						// Partial overlap, extend uniqueLine
						extendLine(uniqueLine, candidateLine.a);
						isDuplicate = true;
						break;						
					} else {
						// No match, check remaining lines for duplicates
						continue;
					}
				}
			}
			
			if(!isDuplicate) {
				// candidateLine does not match any line in the list.
				uniqueLines.add(candidateLine);					
			}
		}

		return uniqueLines;
	}

	private ArrayList<LineSegment2D> convertTurtleToLines(Turtle turtle) {
		// build a list of all the pen-down lines while remembering their color.
		ArrayList<LineSegment2D> originalLines = new ArrayList<LineSegment2D>();
		TurtleMove previousMovement=null;
		ColorRGB color = new ColorRGB(0,0,0);

		Log.message("  Found "+turtle.history.size()+" instructions.");
		
		for( TurtleMove m : turtle.history ) {
			switch(m.type) {
			case DRAW:
				if(previousMovement!=null) {
					LineSegment2D line = new LineSegment2D(
							new Point2D(previousMovement.x,previousMovement.y),
							new Point2D(m.x,m.y),
							color);
					if(line.lengthSquared()>0) {
						originalLines.add(line);
					}
				}
				previousMovement = m;
				break;
			case TRAVEL:
				previousMovement = m;
				break;
			case TOOL_CHANGE:
				color = m.getColor();
				break;
			}
		}

		return originalLines;
	}

	@SuppressWarnings("unused")
	private double lengthSquared(TurtleMove a,TurtleMove b) {
		double dx = a.x-b.x;
		double dy = a.y-b.y;
		return MathHelper.lengthSquared(dx, dy); 
	}

	private double lengthSquared(Point2D a,Point2D b) {
		double dx = a.x-b.x;
		double dy = a.y-b.y;
		return dx*dx + dy*dy; 
	}
	
	@SuppressWarnings("unused")
	private boolean thesePointsAreTheSame(Point2D a,Point2D b,double epsilon) {
		if(a==b) return true;
		
		// close enough ?
		double dx = a.x-b.x;
		double dy = a.y-b.y;
		//if(dx*dx>epsilon*epsilon) return false;
		//if(dy*dy>epsilon*epsilon) return false;
		return MathHelper.lengthSquared(dx, dy)<=epsilon*epsilon; 
	}
	
	/**
	 * Optimize by chopping out very short line segments.  It travels the entire 
	 * path and drops any pen-down segment shorter than minimumStepSize.
	 * @param turtle
	 * @param settings
	 */
	private void simplify(Turtle turtle, MakelangeloRobotSettings settings) {
		System.out.println("simplify() begin");

		int os = turtle.history.size();
		
		ArrayList<LineSegment2D> originalLines = convertTurtleToLines(turtle);
		int originalCount = originalLines.size();
		System.out.println("  Converted to "+originalCount+" lines.");

		double minimumStepSize = 1e-1;
		ArrayList<LineSegment2D> longLines = removeVeryShortSegments(originalLines,minimumStepSize); 
		int longCount = longLines.size();
		int shortCount = originalCount - longCount;
		System.out.println("  - "+shortCount+" shorts = "+longCount+" lines.");

		Turtle t = convertLinesToTurtle(longLines,1);
		
		int ns = t.history.size();
		turtle.history = t.history;
		
		System.out.println("simplify() end (was "+os+" is now "+ns+")");
	}

	private void flipV(Turtle turtle, MakelangeloRobotSettings settings) {	
		turtle.scale(1,-1);
	}

	private void flipH(Turtle turtle, MakelangeloRobotSettings settings) {	
		turtle.scale(-1,1);
	}
	
	private void fitToPaper(Turtle turtle, MakelangeloRobotSettings settings,boolean keepAspect) {
		Point2D top = new Point2D();
		Point2D bottom = new Point2D();
		turtle.getBounds(top, bottom); // image bounds

		// find the scale
		double iw = top.x-bottom.x; // image width
		double ih = top.y-bottom.y; // image height
		double pw = settings.getMarginWidth();
		double ph = settings.getMarginHeight();
		double px = (settings.getMarginRight()+settings.getMarginLeft())*0.5;
		double py = (settings.getMarginTop()+settings.getMarginBottom())*0.5;
		double ratioW=1,ratioH=1;
		ratioH = ph/ih;
		ratioW = pw/iw;
		// use > to fill the page.
		
		// and the translation
		double ix = (top.x+bottom.x)*0.5;
		double iy = (top.y+bottom.y)*0.5;
		
		// and apply
		turtle.translate(-ix,-iy);
		if(keepAspect == true) {
			double ratio=Math.min(ratioW, ratioH);
			turtle.scale(ratio,ratio);
		} else {
			turtle.scale(ratioW,ratioH);
		}
		turtle.translate(px,py);
	}
	
	private void rotatePicture(Turtle turtle, MakelangeloRobotSettings settings) {
		double ang=settings.getRotation();
		double refang=settings.getRotationRef();
		double c=Math.cos((ang-refang)*Math.PI/180.0);
		double s=Math.sin((ang-refang)*Math.PI/180.0);

		for( TurtleMove m : turtle.history ) {
			double xn=m.x*c - m.y*s;
			double yn=m.x*s + m.y*c;
			m.x=xn;
			m.y=yn;
		}
		settings.setRotationRef(ang);
	}
	
	protected void cropToPageMargin(Turtle turtle, MakelangeloRobotSettings settings) {
		if(turtle==null) return;
		
		Log.message("cropTurtleToPageMargin() start");

		ArrayList<TurtleMove> oldHistory = turtle.history;
		turtle.history = new ArrayList<TurtleMove>();
		
		// limits we will need for rectangle
		Point2D rMax = new Point2D(settings.getMarginRight(),settings.getMarginTop());
		Point2D rMin = new Point2D(settings.getMarginLeft(),settings.getMarginBottom());
		// working space for clipping
		Point2D P0 = new Point2D(); 
		Point2D P1 = new Point2D(); 
		
		TurtleMove prev=null;
		
		for( TurtleMove m : oldHistory ) {
			switch(m.type) {
			case DRAW:
			case TRAVEL:
				if(prev!=null) {
					P0.set(prev.x, prev.y);
					P1.set(m.x, m.y);
					boolean result = Clipper2D.clipLineToRectangle(P0,P1,rMax,rMin);
					// !result means full crop, do nothing.
					if(result) {
						// partial crop.  Which end(s)?
						boolean startCropped=MathHelper.lengthSquared(P0.x-prev.x, P0.y-prev.y)>1e-8;
						boolean   endCropped=MathHelper.lengthSquared(P1.x-   m.x, P1.y-   m.y)>1e-8;
						
						if(startCropped && endCropped) {
							// crosses rectangle, both ends out.
							turtle.history.add(new TurtleMove(P0.x,P0.y,TurtleMoveType.TRAVEL));
							turtle.history.add(m);
							TurtleMove m2=new TurtleMove(P1.x,P1.y,m.type);
							turtle.history.add(m2);
						} else if(!startCropped && !endCropped) {
							turtle.history.add(m);
						} else if(endCropped) {
							// end cropped, leaving the rectangle
							TurtleMove m2=new TurtleMove(P1.x,P1.y,m.type);
							turtle.history.add(m2);
						} else {
							// start cropped, coming back into rectangle
							turtle.history.add(new TurtleMove(P0.x,P0.y,TurtleMoveType.TRAVEL));
							turtle.history.add(m);
						}
					}
				}
				prev=m;
				
				break;
			default:
				turtle.history.add(m);
				break;
			}
		}
		
		// There may be some dumb travel moves left. (several travels in a row.)
		
		int oldSize= oldHistory.size();
		int newSize= turtle.history.size();
		Log.message("cropTurtleToPageMargin() end (was "+oldSize+" now "+newSize+")");
	}

	public void reprocessTurtle() {
		processTurtle(lastTurtle,lastSettings);
	}
	
	public void processTurtle(Turtle turtleLoaded, MakelangeloRobotSettings settings) {
		if(turtleLoaded == null) return;
		if(settings == null) return;
		if(turtleLoaded.history.isEmpty()) return;
		
		while(turtleLoaded.isLocked()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				//e.printStackTrace();
				Log.message("processTurtle wait interrupted.");
				return;
			}
		}
		turtleLoaded.lock();
		Turtle newTurtle = new Turtle(turtleLoaded);
		lastTurtle=turtleLoaded;
		lastSettings=settings;
		turtleLoaded.unlock();
		
		double ang = settings.getRotation();
		if(ang != 0.0) rotatePicture(newTurtle,settings);
		if(shouldResizeFill) fitToPaper(newTurtle,settings,false);
		if(shouldResizeFit) fitToPaper(newTurtle,settings,true);
		if(shouldFlipV) flipV(newTurtle,settings);
		if(shouldFlipH) flipH(newTurtle,settings);
		if(shouldReorder) reorder(newTurtle,settings);
		if(shouldSimplify) simplify(newTurtle,settings);
		if(shouldCrop) cropToPageMargin(newTurtle,settings);
		removeRedundantToolChanges(newTurtle);

		notifyListeners(new ArtPipelineEvent(ArtPipelineEvent.FINISHED,this,newTurtle));
	}

	private void removeRedundantToolChanges(Turtle t) {
		ArrayList<TurtleMove> toKeep = new ArrayList<TurtleMove>();
		int size=t.history.size();
		for(int i=0;i<size;++i) {
			TurtleMove mi = t.history.get(i);
			if(mi.type != TurtleMoveType.TOOL_CHANGE) {
				toKeep.add(mi);
				continue;
			}
			// we found a tool change.
			// between this and the next tool change/eof are there any draw commands?
			boolean found=false;
			for(int j=i+1;j<size;++j) {
				TurtleMove mj = t.history.get(j);
				if(mj.type == TurtleMoveType.TOOL_CHANGE) break;
				if(mj.type == TurtleMoveType.DRAW) {
					found=true;
					break;
				}
			}
			if(found) {
				toKeep.add(mi);
			}
		}
		t.history = toKeep;
	}
	
	public boolean getShouldResizeFill() {
		return shouldResizeFill;
	}

	public void setShouldResizeFill(boolean shouldResizeFill) {
		this.shouldResizeFill = shouldResizeFill;
	}

	public boolean getShouldResizeFit() {
		return shouldResizeFit;
	}

	public void setShouldResizeFit(boolean shouldResizeFit) {
		this.shouldResizeFit = shouldResizeFit;
	}

	public boolean getShouldReorder() {
		return shouldReorder;
	}

	public void setShouldReorder(boolean shouldReorder) {
		this.shouldReorder = shouldReorder;
	}

	public boolean getShouldFlipV() {
		return shouldFlipV;
	}

	public void setShouldFlipV(boolean shouldFlipV) {
		this.shouldFlipV = shouldFlipV;
	}

	public boolean getShouldFlipH() {
		return shouldFlipH;
	}

	public void setShouldFlipH(boolean shouldFlipH) {
		this.shouldFlipH = shouldFlipH;
	}

	public boolean getShouldSimplify() {
		return shouldSimplify;
	}

	public void setShouldSimplify(boolean shouldSimplify) {
		this.shouldSimplify = shouldSimplify;
	}

	public boolean getShouldCrop() {
		return shouldCrop;
	}

	public void setShouldCrop(boolean shouldCrop) {
		this.shouldCrop = shouldCrop;
	}	
}
