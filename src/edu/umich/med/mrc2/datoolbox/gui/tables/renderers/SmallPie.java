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

public class SmallPie implements Icon {

	    private int SIZE;
	    private double frequency;
	    private Color backgroundColor = Color.RED;
	    private Color mainColor = Color.GREEN;

	    public SmallPie(int sIZE, double frequency) {

			super();
			SIZE = sIZE;
			this.frequency = frequency;
		}

		public SmallPie(int sIZE, double frequency, Color backgroundColor, Color mainColor) {
			super();
			SIZE = sIZE;
			this.frequency = frequency;
			this.backgroundColor = backgroundColor;
			this.mainColor = mainColor;
		}

		@Override
	    public void paintIcon(Component c, Graphics g, int x, int y) {
	        doDrawing(g, x, y);
	    }

	    public void doDrawing(Graphics g, int x, int y) {

			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING, // Anti-alias!
					RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(backgroundColor);
			g2d.fillOval(0, 0, SIZE, SIZE);
			g2d.setColor(mainColor);
			Double angle = frequency * 360;
			g2d.fillArc(0, 0, SIZE, SIZE, -270, -angle.intValue());
			g2d.setColor(Color.BLACK);
			g2d.drawArc(0, 0, SIZE, SIZE, 0, 360);

	        g2d.dispose();
	    }

	    @Override
	    public int getIconWidth() {
	        return SIZE;
	    }

	    @Override
	    public int getIconHeight() {
	        return SIZE;
	    }

}
