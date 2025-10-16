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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

import edu.umich.med.mrc2.datoolbox.utils.Range;

public class ColorSlider implements Icon {

	private int height, width;
	private Range fullRange, innerRange;
	private Color backgroundColor;
	private Color mainColor;

	public ColorSlider(
			int width, 
			int height, 
			Range fullRange,
			Range innerRange,
			Color backgroundColor,
			Color mainColor) {
		super();
		this.height = height;
		this.width = width;
		this.fullRange = fullRange;
		this.innerRange = innerRange;
		this.backgroundColor = backgroundColor;
		this.mainColor = mainColor;
	}
	
	public ColorSlider(
			int width, 
			int height, 
			Range fullRange,
			Range innerRange,
			Color backgroundColor) {
		this(width, 
			height, 
			fullRange, 
			innerRange, 
			backgroundColor, 
			Color.decode("#deebf7"));
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {

		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(backgroundColor);
		g2d.fillRect(0, 0, width, height);
		
		int sliderStart = 
				(int) Math.round(((innerRange.getMin() - fullRange.getMin()) / fullRange.getSize()) * width);
		int sliderWidth = 
				(int) Math.round(width * innerRange.getSize() / fullRange.getSize());
		
		g2d.setColor(mainColor);
		g2d.fillRect(sliderStart, 0, sliderWidth, height);
        g2d.dispose();
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
}




