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

public class ClusterPair implements Comparable<ClusterPair> {

	private static long globalIndex = 0;

	private Cluster lCluster;
	private Cluster rCluster;
	private Double linkageDistance;

	public ClusterPair() {
	}

	public ClusterPair(Cluster left, Cluster right, Double distance) {
		lCluster = left;
		rCluster = right;
		linkageDistance = distance;
	}

	public Cluster agglomerate(String name, Object userObject) {
		if (name == null) {
			name = "clstr#" + (++globalIndex);

			/*
			 * StringBuilder sb = new StringBuilder(); if (lCluster != null) {
			 * sb.append(lCluster.getName()); } if (rCluster != null) { if
			 * (sb.length() > 0) { sb.append("&"); }
			 * sb.append(rCluster.getName()); } name = sb.toString();
			 */
		}
		Cluster cluster = new Cluster(name, userObject);
		cluster.setDistance(new Distance(getLinkageDistance()));

		// New clusters will track their children's leaf names; i.e. each
		// cluster knows what part of the original data it contains
		cluster.appendLeafNames(lCluster.getLeafNames());
		cluster.appendLeafObjects(lCluster.getLeafObjects());
		cluster.appendLeafNames(rCluster.getLeafNames());
		cluster.appendLeafObjects(rCluster.getLeafObjects());
		cluster.addChild(lCluster);
		cluster.addChild(rCluster);
		lCluster.setParent(cluster);
		rCluster.setParent(cluster);

		Double lWeight = lCluster.getWeightValue();
		Double rWeight = rCluster.getWeightValue();
		double weight = lWeight + rWeight;
		cluster.getDistance().setWeight(weight);

		return cluster;
	}

	@Override
	public int compareTo(ClusterPair o) {
		int result;
		if (o == null || o.getLinkageDistance() == null) {
			result = -1;
		} else if (getLinkageDistance() == null) {
			result = 1;
		} else {
			result = getLinkageDistance().compareTo(o.getLinkageDistance());
		}

		return result;
	}

	public Cluster getlCluster() {
		return lCluster;
	}

	public Double getLinkageDistance() {
		return linkageDistance;
	}

	public Cluster getOtherCluster(Cluster c) {
		return lCluster == c ? rCluster : lCluster;
	}

	public Cluster getrCluster() {
		return rCluster;
	}

	/**
	 * @return a new ClusterPair with the two left/right inverted
	 */
	public ClusterPair reverse() {
		return new ClusterPair(getrCluster(), getlCluster(), getLinkageDistance());
	}

	public void setlCluster(Cluster lCluster) {
		this.lCluster = lCluster;
	}

	public void setLinkageDistance(Double distance) {
		this.linkageDistance = distance;
	}

	public void setrCluster(Cluster rCluster) {
		this.rCluster = rCluster;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (lCluster != null) {
			sb.append(lCluster.getName());
		}
		if (rCluster != null) {
			if (sb.length() > 0) {
				sb.append(" + ");
			}
			sb.append(rCluster.getName());
		}
		sb.append(" : ").append(linkageDistance);
		return sb.toString();
	}

}
