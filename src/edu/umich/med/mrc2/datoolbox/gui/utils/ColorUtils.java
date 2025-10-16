/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotools.brewer.color.ColorBrewer;
import org.jfree.chart.renderer.LookupPaintScale;

import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;

public class ColorUtils {

	public static Color hex2rgb(String colorStr) {
		
		if(colorStr == null)
			return null;
		
		if(colorStr.length() != 7)
			return null;
		
	    return new Color(
	            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
	}
	
	public static String rgb2hex(Color color) {
		
		if(color == null)
			return null;

		return String.format("#%02X%02X%02X", 
				color.getRed(), color.getGreen(), color.getBlue());
	}
	
	private static final Color[] PLOT_COLORS = {
			Color.black,
			Color.red,
			Color.green,
			Color.blue,
			Color.orange,
			Color.cyan,
			Color.magenta,
			Color.pink
		};
	
	private static final Color[] brewedColors = 
			ColorBrewer.instance().getPalette("Paired").getColors();
	
	public static Color getColor(int seriesNumber) {

		int j = 0;
		if (seriesNumber < PLOT_COLORS.length) {
			j = seriesNumber;
		} else {
			j = seriesNumber - Math.floorDiv(seriesNumber, PLOT_COLORS.length) * PLOT_COLORS.length;
		}
		return PLOT_COLORS[j];
	}

	public static Color getBrewerColor(int seriesNumber) {

		int j = 0;
		if (seriesNumber < brewedColors.length) {
			j = seriesNumber;
		} else {
			j = seriesNumber - Math.floorDiv(seriesNumber, brewedColors.length) * brewedColors.length;
		}
		return brewedColors[j];
	}
	
	public static Color getNextFreeBrewerColor(Collection<Color>usedColors) {
		
		for(int i=0; i < brewedColors.length; i++) {
			if(!usedColors.contains(brewedColors[i]))
				return brewedColors[i];
		}
		return brewedColors[0];
	}
	
	public static Color getComplementaryColor(Color inputColor) {
		
		return new Color(
				255-inputColor.getRed(),
                255-inputColor.getGreen(),
                255-inputColor.getBlue());
	}
	
	public static LookupPaintScale createColorLookupScale(
			double rangeMin, 
			double rangeMax, 
			int numSteps,
			ColorGradient gradient,
			ColorScale colorScale) {
		
		Color lowValueColor = gradient.getLowValueColor();
		Color highValueColor = gradient.getHighValueColor();
		int rl = lowValueColor.getRed();
		int gl = lowValueColor.getGreen();
		int bl = lowValueColor.getBlue();
		int rh = highValueColor.getRed();
		int gh = highValueColor.getGreen();
		int bh = highValueColor.getBlue();

		int colorValueDistance = Math.abs(rl - rh);
		colorValueDistance += Math.abs(gl - gh);
		colorValueDistance += Math.abs(bl - bh);
		
		LookupPaintScale paintScale = new LookupPaintScale(rangeMin, rangeMax, gradient.getHighValueColor());
		double range = rangeMax - rangeMin;
		double step = range / numSteps;
		double position = rangeMin;		
		for (int i = 0; i < 256; i++) {
			
			double percentPosition = position / range;
			int colourPosition =  (int) Math.round(colorValueDistance * Math.pow(percentPosition, colorScale.getValue()));
			
			int r = rl;
			int g = gl;
			int b = bl;
			
			for (int j = 0; j < colourPosition; j++) {
				
				int rDistance = r - rh;
				int gDistance = g - gh;
				int bDistance = b - bh;

				if ((Math.abs(rDistance) >= Math.abs(gDistance)) && (Math.abs(rDistance) >= Math.abs(bDistance))) {
					// Red must be the largest.
					r = changeColorValue(r, rDistance);
				} else if (Math.abs(gDistance) >= Math.abs(bDistance)) {
					// Green must be the largest.
					g = changeColorValue(g, gDistance);
				} else {
					// Blue must be the largest.
					b = changeColorValue(b, bDistance);
				}
			}
			paintScale.add(position, new Color(r, g, b));
			position = position + step;
		}
		return paintScale;
	}
	
	private static int changeColorValue(int colourValue, int colourDistance) {
		if (colourDistance < 0) {
			return colourValue + 1;
		} else if (colourDistance > 0) {
			return colourValue - 1;
		} else {
			// This shouldn't actually happen here.
			return colourValue;
		}
	}	
	
	public static List<Color> getColorBands(
			Color color, 
			int bands, 
			SortDirection direction) {

	    List<Color> colorBands = new ArrayList<>(bands);
	    if(direction.equals(SortDirection.ASC)) {
	    	
		    for (int index = 0; index < bands; index++)
		        colorBands.add(lighten(color, (double) index / (double) bands));
	    }
	    if(direction.equals(SortDirection.DESC)) {
	    	
		    for (int index = 0; index < bands; index++)
		        colorBands.add(darken(color, (double) index / (double) bands));
	    }	    
	    return colorBands;
	}

	public static Color darken(Color color, double fraction) {

	    int red = (int) Math.round(Math.max(0, color.getRed() - 255 * fraction));
	    int green = (int) Math.round(Math.max(0, color.getGreen() - 255 * fraction));
	    int blue = (int) Math.round(Math.max(0, color.getBlue() - 255 * fraction));

	    int alpha = color.getAlpha();

	    return new Color(red, green, blue, alpha);
	}
	
	public static Color lighten(Color color, double fraction) {

	    int red = (int) Math.round(Math.min(255, color.getRed() + 255 * fraction));
	    int green = (int) Math.round(Math.min(255, color.getGreen() + 255 * fraction));
	    int blue = (int) Math.round(Math.min(255, color.getBlue() + 255 * fraction));

	    int alpha = color.getAlpha();

	    return new Color(red, green, blue, alpha);
	}
	
	public static Color addTrasparency(Color color, int alpha) { 
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}

















