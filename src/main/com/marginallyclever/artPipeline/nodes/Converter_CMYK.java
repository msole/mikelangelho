package com.marginallyclever.artPipeline.nodes;


import java.util.ArrayList;

import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.artPipeline.TurtleNodePanel;
import com.marginallyclever.artPipeline.imageFilters.Filter_CMYK;
import com.marginallyclever.artPipeline.nodes.panels.Converter_CMYK_Panel;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;


/**
 * See also http://the-print-guide.blogspot.ca/2009/05/halftone-screen-angles.html
 * @author Dan Royer
 */
public class Converter_CMYK extends ImageConverter {
	static protected int passes=1;
	// Color values are from 0...255 inclusive.  255 is white, 0 is black.
	// Lift the pen any time the color value is > cutoff
	
	@Override
	public String getName() {
		return Translator.get("ConverterCMYKName");
	}

	@Override
	public TurtleNodePanel getPanel() {
		return new Converter_CMYK_Panel(this);
	}
	
	public int getPasses() {
		return passes;
	}
	public void setPasses(int value) {
		if(passes<1) passes=1;
		passes=value;
	}
	
	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public boolean iterate() {
		Filter_CMYK cmyk = new Filter_CMYK();
		cmyk.filter(sourceImage);
		
		ArrayList<Turtle> list = new ArrayList<Turtle>();
		
		Log.message("Yellow...");		list.add(outputChannel(cmyk.getY(),0 ,new ColorRGB(255,255,  0)));
		Log.message("Cyan...");			list.add(outputChannel(cmyk.getC(),15,new ColorRGB(  0,255,255)));
		Log.message("Magenta...");		list.add(outputChannel(cmyk.getM(),75,new ColorRGB(255,  0,255)));
		Log.message("Black...");		list.add(outputChannel(cmyk.getK(),45,new ColorRGB(  0,  0,  0)));
		Log.message("Finishing...");

		setTurtleResult(list);
		return false;
	}
	
	protected Turtle outputChannel(TransformedImage img,float angle,ColorRGB newColor) {
		Turtle turtle = new Turtle();
		
		// The picture might be in color.  Smash it to 255 shades of grey.
		double dx = Math.cos(Math.toRadians(angle));
		double dy = Math.sin(Math.toRadians(angle));
		double [] channelCutoff = {0,153,51,102,204};
		
		turtle.setColor(newColor);

		// figure out how many lines we're going to have on this image.
		double stepSize = (double)passes;

		// from top to bottom of the margin area...
		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		
		double height  = yTop-yBottom;
		double width   = xRight-xLeft;
		
		double maxLen  = Math.sqrt(width*width+height*height);

		double [] error0 = new double[(int)Math.ceil(maxLen)];
		double [] error1 = new double[(int)Math.ceil(maxLen)];
		
		double px,py,x0,y0,x1,y1,a;
		
		boolean useError=false;
		
		int i=0;
		for(a = -maxLen;a<maxLen;a+=stepSize) {
			px = dx * a;
			py = dy * a;
			// p0-p1 is at a right angle to dx/dy
			x0 = px - dy * maxLen;
			y0 = py + dx * maxLen;
			x1 = px + dy * maxLen;
			y1 = py - dx * maxLen;

			double cutoff=channelCutoff[i%channelCutoff.length];
			if ((i % 2) == 0) {
				if(!useError) convertAlongLine(turtle,x0,y0,x1,y1,stepSize,cutoff,img);
				else convertAlongLineErrorTerms(turtle,x0,y0,x1,y1,stepSize,cutoff,error0,error1,img);
			} else {
				if(!useError) convertAlongLine(turtle,x1,y1,x0,y0,stepSize,cutoff,img);
				else convertAlongLineErrorTerms(turtle,x1,y1,x0,y0,stepSize,cutoff,error0,error1,img);
			}
			
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			++i;
		}
		return turtle;
	}
}


/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */