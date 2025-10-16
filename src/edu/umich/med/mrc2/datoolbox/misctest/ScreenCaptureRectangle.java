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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Getting a Rectangle of interest on the screen. Requires the MotivatedEndUser
 * API - sold separately.
 */
public class ScreenCaptureRectangle {

	public static void main(String[] args) throws Exception {
		Robot robot = new Robot();
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final BufferedImage screen = robot.createScreenCapture(new Rectangle(screenSize));

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ScreenCaptureRectangle(screen);
			}
		});
	}

	Rectangle captureRect;

	ScreenCaptureRectangle(final BufferedImage screen) {

		final BufferedImage screenCopy = new BufferedImage(screen.getWidth(), screen.getHeight(), screen.getType());
		final JLabel screenLabel = new JLabel(new ImageIcon(screenCopy));
		JScrollPane screenScroll = new JScrollPane(screenLabel);

		screenScroll.setPreferredSize(new Dimension((int) (screen.getWidth() / 3), (int) (screen.getHeight() / 3)));

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(screenScroll, BorderLayout.CENTER);

		final JLabel selectionLabel = new JLabel("Drag a rectangle in the screen shot!");
		panel.add(selectionLabel, BorderLayout.SOUTH);

		repaint(screen, screenCopy);
		screenLabel.repaint();

		screenLabel.addMouseMotionListener(new MouseMotionAdapter() {

			Point start = new Point();

			@Override
			public void mouseDragged(MouseEvent me) {
				Point end = me.getPoint();
				captureRect = new Rectangle(start, new Dimension(end.x - start.x, end.y - start.y));
				repaint(screen, screenCopy);
				screenLabel.repaint();
				selectionLabel.setText("Rectangle: " + captureRect);
			}

			@Override
			public void mouseMoved(MouseEvent me) {
				start = me.getPoint();
				repaint(screen, screenCopy);
				selectionLabel.setText("Start Point: " + start);
				screenLabel.repaint();
			}
		});

		JOptionPane.showMessageDialog(null, panel);

		System.out.println("Rectangle of interest: " + captureRect);
	}

	public void repaint(BufferedImage orig, BufferedImage copy) {
		Graphics2D g = copy.createGraphics();
		g.drawImage(orig, 0, 0, null);
		if (captureRect != null) {
			g.setColor(Color.RED);
			g.draw(captureRect);
			g.setColor(new Color(255, 255, 255, 150));
			g.fill(captureRect);
		}
		g.dispose();
	}
}
