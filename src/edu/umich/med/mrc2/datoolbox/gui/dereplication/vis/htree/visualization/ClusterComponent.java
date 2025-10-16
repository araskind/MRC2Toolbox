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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree.visualization;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree.Cluster;
import edu.umich.med.mrc2.datoolbox.main.config.NumberFormatStore;

public class ClusterComponent implements Paintable {

	private Cluster cluster;
	private VCoord linkPoint;
	private VCoord initPoint;
	private boolean printName;
	private int dotRadius = 2;
	private int namePadding = 6;

	private List<ClusterComponent> children;
	private HashMap<Object, Rectangle> leafObjectMap;

	public ClusterComponent(Cluster cluster, boolean printName, VCoord initPoint) {

		this.printName = printName;
		this.cluster = cluster;
		this.initPoint = initPoint;
		this.linkPoint = initPoint;
		leafObjectMap = new HashMap<Object, Rectangle>();
	}

	public ClusterComponent(Cluster cluster, boolean printName, VCoord initPoint,
			HashMap<Object, Rectangle> leafObjectMap) {

		this.printName = printName;
		this.cluster = cluster;
		this.initPoint = initPoint;
		this.linkPoint = initPoint;
		this.leafObjectMap = leafObjectMap;
	}

	public List<ClusterComponent> getChildren() {
		if (children == null) {
			children = new ArrayList<ClusterComponent>();
		}
		return children;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public int getDotRadius() {
		return dotRadius;
	}

	public VCoord getInitPoint() {
		return initPoint;
	}

	public Map<Object, Rectangle> getLeafObjectMap() {

		return leafObjectMap;
	}

	public VCoord getLinkPoint() {
		return linkPoint;
	}

	public int getMaxNameWidth(Graphics2D g, boolean includeNonLeafs) {
		int width = getNameWidth(g, includeNonLeafs);
		for (ClusterComponent comp : getChildren()) {
			int childWidth = comp.getMaxNameWidth(g, includeNonLeafs);
			if (childWidth > width) {
				width = childWidth;
			}
		}
		return width;
	}

	public int getNamePadding() {
		return namePadding;
	}

	public int getNameWidth(Graphics2D g, boolean includeNonLeafs) {
		int width = 0;
		if (includeNonLeafs || cluster.isLeaf()) {
			Rectangle2D rect = g.getFontMetrics().getStringBounds(cluster.getName(), g);
			width = (int) rect.getWidth();
		}
		return width;
	}

	public double getRectMaxX() {

		// TODO Better use closure here
		assert initPoint != null && linkPoint != null;
		double val = Math.max(initPoint.getX(), linkPoint.getX());
		for (ClusterComponent child : getChildren()) {
			val = Math.max(val, child.getRectMaxX());
		}
		return val;
	}

	public double getRectMaxY() {

		// TODO Better use closure here
		assert initPoint != null && linkPoint != null;
		double val = Math.max(initPoint.getY(), linkPoint.getY());
		for (ClusterComponent child : getChildren()) {
			val = Math.max(val, child.getRectMaxY());
		}
		return val;
	}

	public double getRectMinX() {

		// TODO Better use closure / callback here
		assert initPoint != null && linkPoint != null;
		double val = Math.min(initPoint.getX(), linkPoint.getX());
		for (ClusterComponent child : getChildren()) {
			val = Math.min(val, child.getRectMinX());
		}
		return val;
	}

	public double getRectMinY() {

		// TODO Better use closure here
		assert initPoint != null && linkPoint != null;
		double val = Math.min(initPoint.getY(), linkPoint.getY());
		for (ClusterComponent child : getChildren()) {
			val = Math.min(val, child.getRectMinY());
		}
		return val;
	}

	public boolean isPrintName() {
		return printName;
	}

	@Override
	public void paint(Graphics2D g, int xDisplayOffset, int yDisplayOffset, double xDisplayFactor,
			double yDisplayFactor, boolean decorated) {

		Rectangle2D rect = null;
		Rectangle objRect = null;

		int x1, y1, x2, y2;
		FontMetrics fontMetrics = g.getFontMetrics();
		x1 = (int) (initPoint.getX() * xDisplayFactor + xDisplayOffset);
		y1 = (int) (initPoint.getY() * yDisplayFactor + yDisplayOffset);
		x2 = (int) (linkPoint.getX() * xDisplayFactor + xDisplayOffset);
		y2 = y1;
		g.fillOval(x1 - dotRadius, y1 - dotRadius, dotRadius * 2, dotRadius * 2);
		g.drawLine(x1, y1, x2, y2);

		if (cluster.isLeaf()) {
			g.drawString(cluster.getName(), x1 + namePadding, y1 + (fontMetrics.getHeight() / 2) - 2);
			rect = fontMetrics.getStringBounds(cluster.getName(), g);
			objRect = new Rectangle(x1 + namePadding, y1, (int) rect.getWidth(), (int) rect.getHeight());
			leafObjectMap.put(cluster.getUserObject(), objRect);
		}
		if (decorated && cluster.getDistance() != null && !cluster.getDistance().isNaN()
				&& cluster.getDistance().getDistance() > 0) {
			String s = NumberFormatStore.getDecimalFormatWithPrecision(2).format(cluster.getDistance());
			rect = fontMetrics.getStringBounds(s, g);
			g.drawString(s, x1 - (int) rect.getWidth(), y1 - 2);
		}

		x1 = x2;
		y1 = y2;
		y2 = (int) (linkPoint.getY() * yDisplayFactor + yDisplayOffset);
		g.drawLine(x1, y1, x2, y2);

		for (ClusterComponent child : children) {
			child.paint(g, xDisplayOffset, yDisplayOffset, xDisplayFactor, yDisplayFactor, decorated);

			// if(child.getCluster().isLeaf()){
			//
			// Object uo = child.getCluster().getUserObject();
			// Rectangle r = child.getLeafObjectMap().get(uo);
			// leafObjectMap.put(uo, r);
			// }
		}
	}

	public void setChildren(List<ClusterComponent> children) {
		this.children = children;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public void setDotRadius(int dotRadius) {
		this.dotRadius = dotRadius;
	}

	public void setInitPoint(VCoord initPoint) {
		this.initPoint = initPoint;
	}

	public void setLinkPoint(VCoord linkPoint) {
		this.linkPoint = linkPoint;
	}

	public void setNamePadding(int namePadding) {
		this.namePadding = namePadding;
	}

	public void setPrintName(boolean printName) {
		this.printName = printName;
	}
}
