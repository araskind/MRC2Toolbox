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

package edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

public class IdLevelIcon implements Icon {

    private int SIZE;
    private Color backgroundColor = Color.BLACK;
    private Color mainColor = Color.GREEN;

    public IdLevelIcon(int sIZE, Color mainColor) {

		super();
		SIZE = sIZE;
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

		g2d.setColor(mainColor);
		g2d.fillOval(0, 0, SIZE, SIZE);
		g2d.setColor(backgroundColor);
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
