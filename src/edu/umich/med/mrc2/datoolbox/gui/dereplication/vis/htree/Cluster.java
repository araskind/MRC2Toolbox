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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

	private String name;

	private Object userObject;

	private Cluster parent;

	private List<Cluster> children;

	private List<String> leafNames;

	private List<Object> leafObjects;

	private Distance distance = new Distance();

	public Cluster(String name) {
		this.name = name;
		leafNames = new ArrayList<String>();
		leafObjects = new ArrayList<Object>();
	}

	public Cluster(String name, Object userObject) {
		this.name = name;
		this.userObject = userObject;
		leafNames = new ArrayList<String>();
		leafObjects = new ArrayList<Object>();
	}

	public void addChild(Cluster cluster) {
		getChildren().add(cluster);

	}

	public void addLeafName(String lname) {
		leafNames.add(lname);
	}

	public void addLeafObject(Object lobject) {
		leafObjects.add(lobject);
	}

	public void appendLeafNames(List<String> lnames) {
		leafNames.addAll(lnames);
	}

	public void appendLeafObjects(List<Object> lobjects) {
		leafObjects.addAll(lobjects);
	}

	public boolean contains(Cluster cluster) {
		return getChildren().contains(cluster);
	}

	public int countLeafs() {
		return countLeafs(this, 0);
	}

	public int countLeafs(Cluster node, int count) {
		if (node.isLeaf())
			count++;
		for (Cluster child : node.getChildren()) {
			count += child.countLeafs();
		}
		return count;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Cluster other = (Cluster) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public List<Cluster> getChildren() {
		if (children == null) {
			children = new ArrayList<Cluster>();
		}

		return children;
	}

	public Distance getDistance() {
		return distance;
	}

	public Double getDistanceValue() {
		return distance.getDistance();
	}

	public List<String> getLeafNames() {
		return leafNames;
	}

	public List<Object> getLeafObjects() {
		return leafObjects;
	}

	public String getName() {
		return name;
	}

	public Cluster getParent() {
		return parent;
	}

	public double getTotalDistance() {
		Double dist = getDistance() == null ? 0 : getDistance().getDistance();
		if (getChildren().size() > 0) {
			dist += children.get(0).getTotalDistance();
		}
		return dist;

	}

	public Object getUserObject() {
		return userObject;
	}

	public Double getWeightValue() {
		return distance.getWeight();
	}

	@Override
	public int hashCode() {
		return (name == null) ? 0 : name.hashCode();
	}

	public boolean isLeaf() {
		return getChildren().size() == 0;
	}

	public void setChildren(List<Cluster> children) {
		this.children = children;
	}

	public void setDistance(Distance distance) {
		this.distance = distance;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(Cluster parent) {
		this.parent = parent;
	}

	public void toConsole(int indent) {
		for (int i = 0; i < indent; i++) {
			System.out.print("  ");

		}
		String name = getName() + (isLeaf() ? " (leaf)" : "") + (distance != null ? "  distance: " + distance : "");
		System.out.println(name);
		for (Cluster child : getChildren()) {
			child.toConsole(indent + 1);
		}
	}

	@Override
	public String toString() {
		return "Cluster " + name;
	}

}
