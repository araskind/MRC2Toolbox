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

package edu.umich.med.mrc2.datoolbox.gui.plot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

public class PieChart extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6664288452759764809L;
	private int width;

	enum Type {
		STANDARD, 
		SIMPLE_INDICATOR, 
		GRADED_INDICATOR
	}

	private Type type = null; // the type of pie chart
	private ArrayList<Double> values;
	private ArrayList<Color> colors;
	private ArrayList<Double> gradingValues;
	private ArrayList<Color> gradingColors;

	double percent = 0; // percent is used for simple indicator and graded indicator

	public PieChart(int percent) {

		type = Type.SIMPLE_INDICATOR;
		this.percent = percent;
	}
	
	public PieChart(int percent, int width) {

		type = Type.SIMPLE_INDICATOR;
		this.percent = percent;
		this.width = width;
		setSize(width,width);
	}

	public PieChart(ArrayList<Double> values, ArrayList<Color> colors) {

		type = Type.STANDARD;

		this.values = values;
		this.colors = colors;
	}

	public PieChart(int percent, ArrayList<Double> gradingValues, ArrayList<Color> gradingColors) {
		
		type = Type.GRADED_INDICATOR;

		this.gradingValues = gradingValues;
		this.gradingColors = gradingColors;
		this.percent = percent;
	}

	@Override
	protected void paintComponent(Graphics g) {
	

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (type == Type.SIMPLE_INDICATOR) {

			// colours used for simple indicator
			Color backgroundColor = Color.WHITE;
			Color mainColor = Color.BLUE;

			g2d.setColor(backgroundColor);
			g2d.fillOval(0, 0, width, width);
			g2d.setColor(mainColor);
			Double angle = (percent / 100) * 360;
			g2d.fillArc(0, 0, width, width, -270, -angle.intValue());
			
		
		} 
		else if (type == Type.STANDARD) {

			int lastPoint = -270;

			for (int i = 0; i < values.size(); i++) {
				g2d.setColor(colors.get(i));

				Double val = values.get(i);
				Double angle = (val / 100) * 360;

				g2d.fillArc(0, 0, width, width, lastPoint, -angle.intValue());
				System.out.println("fill arc " + lastPoint + " " + -angle.intValue());

				lastPoint = lastPoint + -angle.intValue();
			}
		} 
		else if (type == Type.GRADED_INDICATOR) {

			int lastPoint = -270;

			double gradingAccum = 0;

			for (int i = 0; i < gradingValues.size(); i++) {
				
				g2d.setColor(gradingColors.get(i));
				Double val = gradingValues.get(i);
				gradingAccum = gradingAccum + val;
				Double angle = null;
				/**
				 * * If the sum of the gradings is greater than the percent, then we want to
				 * recalculate * the last wedge, and break out of drawing.
				 */
				if (gradingAccum > percent) {

					//	System.out.println("gradingAccum > percent");
					// get the previous accumulated segments. Segments minus last one
					
					double gradingAccumMinusOneSegment = gradingAccum - val;

					// make an adjusted calculation of the last wedge
					angle = ((percent - gradingAccumMinusOneSegment) / 100) * 360;
					g2d.fillArc(0, 0, width, width, lastPoint, -angle.intValue());
					lastPoint = lastPoint + -angle.intValue();
					break;
				} 
				else {
					//	System.out.println("normal");
					angle = (val / 100) * 360;
					g2d.fillArc(0, 0, width, width, lastPoint, -angle.intValue());
					//	System.out.println("fill arc " + lastPoint + " " + -angle.intValue());
					lastPoint = lastPoint + -angle.intValue();
				}
			}
		}
	}
}
