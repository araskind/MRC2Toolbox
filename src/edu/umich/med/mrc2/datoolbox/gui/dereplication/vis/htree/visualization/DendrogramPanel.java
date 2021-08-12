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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.CorrelationResultsPanel;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree.Cluster;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DendrogramPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	final static BasicStroke solidStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

	private Cluster model;
	private ClusterComponent component;
	private Color lineColor = Color.BLACK;
	private boolean showDistanceValues = false;
	private boolean showScale = true;
	private int borderTop = 20;
	private int borderLeft = 20;
	private int borderRight = 20;
	private int borderBottom = 20;
	private int scalePadding = 10;
	private int scaleTickLength = 4;
	private int scaleTickLabelPadding = 4;
	private double scaleValueInterval = 0;
	private int scaleValueDecimals = 0;

	private double xModelOrigin = 0.0;
	private double yModelOrigin = 0.0;
	private double wModel = 0.0;
	private double hModel = 0.0;

	private Rectangle captureRect;
	private Point start;
	private HashSet<Object> selectedObjects;
	private HashSet<MsFeature> selectedFeatures;

	private HashMap<Object, Rectangle> leafObjectMap;
	private CorrelationResultsPanel corrPanel;

	public DendrogramPanel() {

		super();

		selectedObjects = new HashSet<Object>();
		selectedFeatures = new HashSet<MsFeature>();
		leafObjectMap = new HashMap<Object, Rectangle>();

		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			public void mousePressed(MouseEvent e) {

				selectedObjects.clear();
				selectedFeatures.clear();
				corrPanel.clearSelection();
				captureRect = null;
				repaint();
				start = new Point(e.getX(), e.getY());
			}

			@Override
			public void mouseReleased(MouseEvent e) {

				selectedObjects.clear();
				selectedFeatures.clear();

				for (Map.Entry<Object, Rectangle> entry : leafObjectMap.entrySet()) {

					if (captureRect != null) {

						if (captureRect.intersects(entry.getValue())) {

							selectedObjects.add(entry.getKey());
							if (entry.getKey() instanceof MsFeature)
								selectedFeatures.add((MsFeature) entry.getKey());
						}
					}
				}
				if(!selectedObjects.isEmpty())
					corrPanel.selectFeatures(
							selectedObjects.stream().
							filter(MsFeature.class::isInstance).
							map(MsFeature.class::cast).
							collect(Collectors.toList()));
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent me) {

				Point end = me.getPoint();
				captureRect = new Rectangle(start, new Dimension(end.x - start.x, end.y - start.y));
				repaint();
			}

			@Override
			public void mouseMoved(MouseEvent me) {

			}
		});
	}

	private ClusterComponent createComponent(Cluster model) {

		double virtualModelHeight = 1;
		VCoord initCoord = new VCoord(0, virtualModelHeight / 2);

		ClusterComponent comp = createComponent(model, initCoord, virtualModelHeight);
		comp.setLinkPoint(initCoord);
		return comp;
	}

	private ClusterComponent createComponent(Cluster cluster, VCoord initCoord, double clusterHeight) {

		ClusterComponent comp = null;
		if (cluster != null) {
			comp = new ClusterComponent(cluster, cluster.isLeaf(), initCoord, leafObjectMap);
			double leafHeight = clusterHeight / cluster.countLeafs();
			double yChild = initCoord.getY() - (clusterHeight / 2);
			double distance = cluster.getDistanceValue() == null ? 0 : cluster.getDistanceValue();
			for (Cluster child : cluster.getChildren()) {
				int childLeafCount = child.countLeafs();
				double childHeight = childLeafCount * leafHeight;
				double childDistance = child.getDistanceValue() == null ? 0 : child.getDistanceValue();
				VCoord childInitCoord = new VCoord(initCoord.getX() + (distance - childDistance),
						yChild + childHeight / 2.0);
				yChild += childHeight;

				/* Traverse cluster node tree */
				ClusterComponent childComp = createComponent(child, childInitCoord, childHeight);

				childComp.setLinkPoint(initCoord);
				comp.getChildren().add(childComp);
			}
		}
		return comp;
	}

	public int getBorderBottom() {
		return borderBottom;
	}

	public int getBorderLeft() {
		return borderLeft;
	}

	public int getBorderRight() {
		return borderRight;
	}

	public int getBorderTop() {
		return borderTop;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public Cluster getModel() {
		return model;
	}

	public int getScalePadding() {
		return scalePadding;
	}

	public int getScaleTickLength() {
		return scaleTickLength;
	}

	public int getScaleValueDecimals() {
		return scaleValueDecimals;
	}

	public double getScaleValueInterval() {
		return scaleValueInterval;
	}

	public HashSet<MsFeature> getSelectedFeatures() {
		return selectedFeatures;
	}

	public HashSet<Object> getSelectedObjects() {
		return selectedObjects;
	}

	public boolean isShowDistanceValues() {
		return showDistanceValues;
	}

	public boolean isShowScale() {
		return showScale;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(lineColor);
		g2.setStroke(solidStroke);

		int wDisplay = getWidth() - borderLeft - borderRight;
		int hDisplay = getHeight() - borderTop - borderBottom;
		int xDisplayOrigin = borderLeft;
		int yDisplayOrigin = borderBottom;

		if (component != null) {

			int nameGutterWidth = component.getMaxNameWidth(g2, false) + component.getNamePadding();
			wDisplay -= nameGutterWidth;

			if (showScale) {
				Rectangle2D rect = g2.getFontMetrics().getStringBounds("0", g2);
				int scaleHeight = (int) rect.getHeight() + scalePadding + scaleTickLength + scaleTickLabelPadding;
				hDisplay -= scaleHeight;
				yDisplayOrigin += scaleHeight;
			}

			/* Calculate conversion factor and offset for display */
			double xFactor = wDisplay / wModel;
			double yFactor = hDisplay / hModel;
			int xOffset = (int) (xDisplayOrigin - xModelOrigin * xFactor);
			int yOffset = (int) (yDisplayOrigin - yModelOrigin * yFactor);
			component.paint(g2, xOffset, yOffset, xFactor, yFactor, showDistanceValues);

			if (showScale) {
				int x1 = xDisplayOrigin;
				int y1 = yDisplayOrigin - scalePadding;
				int x2 = x1 + wDisplay;
				int y2 = y1;
				g2.drawLine(x1, y1, x2, y2);

				double totalDistance = component.getCluster().getTotalDistance();
				double xModelInterval;
				if (scaleValueInterval <= 0) {
					xModelInterval = totalDistance / 10.0;
				} else {
					xModelInterval = scaleValueInterval;
				}

				int xTick = xDisplayOrigin + wDisplay;
				y1 = yDisplayOrigin - scalePadding;
				y2 = yDisplayOrigin - scalePadding - scaleTickLength;
				double distanceValue = 0;
				double xDisplayInterval = xModelInterval * xFactor;
				while (xTick >= xDisplayOrigin) {
					g2.drawLine(xTick, y1, xTick, y2);

					String distanceValueStr = String.format("%." + scaleValueDecimals + "f", distanceValue);
					Rectangle2D rect = g2.getFontMetrics().getStringBounds(distanceValueStr, g2);
					g2.drawString(distanceValueStr, (int) (xTick - (rect.getWidth() / 2)), y2 - scaleTickLabelPadding);
					xTick -= xDisplayInterval;
					distanceValue += xModelInterval;
				}

			}
			if (captureRect != null) {

				g2.setColor(Color.RED);
				g2.draw(captureRect);
				g2.setColor(new Color(255, 255, 255, 150));
				g2.fill(captureRect);
			}
		} else {

			/* No data available */
			String str = "No data";
			Rectangle2D rect = g2.getFontMetrics().getStringBounds(str, g2);
			int xt = (int) (wDisplay / 2.0 - rect.getWidth() / 2.0);
			int yt = (int) (hDisplay / 2.0 - rect.getHeight() / 2.0);
			g2.drawString(str, xt, yt);
		}
	}

	public void setBorderBottom(int borderBottom) {
		this.borderBottom = borderBottom;
	}

	public void setBorderLeft(int borderLeft) {
		this.borderLeft = borderLeft;
	}

	public void setBorderRight(int borderRight) {
		this.borderRight = borderRight;
	}

	public void setBorderTop(int borderTop) {
		this.borderTop = borderTop;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public void setModel(Cluster model) {

		selectedObjects = new HashSet<Object>();
		selectedFeatures = new HashSet<MsFeature>();
		leafObjectMap = new HashMap<Object, Rectangle>();
		corrPanel = (CorrelationResultsPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.CORRELATIONS);
		this.model = model;
		captureRect = null;
		component = createComponent(model);
		updateModelMetrics();
	}

	public void setScalePadding(int scalePadding) {
		this.scalePadding = scalePadding;
	}

	public void setScaleTickLength(int scaleTickLength) {
		this.scaleTickLength = scaleTickLength;
	}

	public void setScaleValueDecimals(int scaleValueDecimals) {
		this.scaleValueDecimals = scaleValueDecimals;
	}

	public void setScaleValueInterval(double scaleTickInterval) {
		this.scaleValueInterval = scaleTickInterval;
	}

	public void setShowDistances(boolean showDistanceValues) {
		this.showDistanceValues = showDistanceValues;
	}

	public void setShowScale(boolean showScale) {
		this.showScale = showScale;
	}

	private void updateModelMetrics() {
		double minX = component.getRectMinX();
		double maxX = component.getRectMaxX();
		double minY = component.getRectMinY();
		double maxY = component.getRectMaxY();

		xModelOrigin = minX;
		yModelOrigin = minY;
		wModel = maxX - minX;
		hModel = maxY - minY;
	}
}
