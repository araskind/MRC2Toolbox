/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
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

import org.jfree.chart.renderer.LookupPaintScale;

import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class ColorCodingUtils {

	public static LookupPaintScale createLookupPaintScale(
			Range dataRange, 
			ColorGradient colorGradient, 
			ColorScale colorScale,
			int numSteps) {
		
		Color lvc = colorGradient.getLowValueColor();
		Color hvc = colorGradient.getHighValueColor();
				
		int r1 = lvc.getRed();
		int g1 = lvc.getGreen();
		int b1 = lvc.getBlue();
		int r2 = hvc.getRed();
		int g2 = hvc.getGreen();
		int b2 = hvc.getBlue();

		int colorValueDistance = Math.abs(r1 - r2);
		colorValueDistance += Math.abs(g1 - g2);
		colorValueDistance += Math.abs(b1 - b2);
		
		LookupPaintScale lookupPaintScale = 
				new LookupPaintScale(
						dataRange.getMin(), dataRange.getMax(), Color.black);
		double step = (dataRange.getSize()) / ((double)numSteps);
		double position = dataRange.getMin();
		for (int i = 0; i < numSteps; i++) {

			lookupPaintScale.add(
					position, getCellColor(
							position, dataRange, lvc, hvc, colorScale, colorValueDistance));
			position = position + step;
		}
		return lookupPaintScale;
	}
	
	private static Color getCellColor(
			double data, 
			Range dataRange,
			Color lowValueColor,
			Color highValueColor,
			ColorScale colorScale,
			int colorValueDistance) {

		double range = dataRange.getSize();
		double position = data - dataRange.getMin();
		double percentPosition = position / range;
		int colourPosition = 
				getColorPosition(percentPosition, colorValueDistance, colorScale);

		int r = lowValueColor.getRed();
		int g = lowValueColor.getGreen();
		int b = lowValueColor.getBlue();

		for (int i = 0; i < colourPosition; i++) {
			
			int rDistance = r - highValueColor.getRed();
			int gDistance = g - highValueColor.getGreen();
			int bDistance = b - highValueColor.getBlue();

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
		return new Color(r, g, b);
	}
	
	private static int getColorPosition(
			double percentPosition, 
			int colorValueDistance,
			ColorScale colorScale) {
		return (int) Math.round(colorValueDistance * 
				Math.pow(percentPosition, colorScale.getValue()));
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
}
