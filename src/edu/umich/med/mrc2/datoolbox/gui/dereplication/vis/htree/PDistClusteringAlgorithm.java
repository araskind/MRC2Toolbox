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

public class PDistClusteringAlgorithm implements ClusteringAlgorithm {

	// Credit to this function goes to
	// http://stackoverflow.com/questions/13079563/how-does-condensed-distance-matrix-work-pdist
	private static int accessFunction(int i, int j, int n) {
		return n * j - j * (j + 1) / 2 + i - 1 - j;
	}

	private void checkArguments(double[][] distances, String[] clusterNames, LinkageStrategy linkageStrategy,
			Object[] clusterObjects) {

		if (distances == null || distances.length == 0) {
			throw new IllegalArgumentException("Invalid distance matrix");
		}
		if (distances[0].length != clusterNames.length * (clusterNames.length - 1) / 2) {
			throw new IllegalArgumentException("Invalid cluster name array");
		}
		if (linkageStrategy == null) {
			throw new IllegalArgumentException("Undefined linkage strategy");
		}
		if (clusterObjects != null) {

			if (clusterObjects.length != clusterNames.length) {
				throw new IllegalArgumentException("Invalid cluster objects array");
			}
		}
	}

	private List<Cluster> createClusters(String[] clusterNames) {
		List<Cluster> clusters = new ArrayList<Cluster>();
		for (String clusterName : clusterNames) {
			Cluster cluster = new Cluster(clusterName);
			cluster.addLeafName(clusterName);
			clusters.add(cluster);
		}
		return clusters;
	}

	private List<Cluster> createClusters(String[] clusterNames, Object[] clusterObjects) {

		List<Cluster> clusters = new ArrayList<Cluster>();

		for (int i = 0; i < clusterNames.length; i++) {

			Object userObject = null;

			if (clusterObjects != null)
				userObject = clusterObjects[i];

			Cluster cluster = new Cluster(clusterNames[i], userObject);
			clusters.add(cluster);
		}
		return clusters;
	}

	private DistanceMap createLinkages(double[][] distances, List<Cluster> clusters) {
		DistanceMap linkages = new DistanceMap();
		for (int col = 0; col < clusters.size(); col++) {
			Cluster cluster_col = clusters.get(col);
			for (int row = col + 1; row < clusters.size(); row++) {
				ClusterPair link = new ClusterPair();
				Double d = distances[0][accessFunction(row, col, clusters.size())];
				link.setLinkageDistance(d);
				link.setlCluster(cluster_col);
				link.setrCluster(clusters.get(row));
				linkages.add(link);
			}
		}
		return linkages;
	}

	@Override
	public Cluster performClustering(double[][] distances, String[] clusterNames, LinkageStrategy linkageStrategy) {

		/* Argument checks */
		checkArguments(distances, clusterNames, linkageStrategy, null);

		/* Setup model */
		List<Cluster> clusters = createClusters(clusterNames);
		DistanceMap linkages = createLinkages(distances, clusters);

		/* Process */
		HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
		while (!builder.isTreeComplete()) {
			builder.agglomerate(linkageStrategy);
		}
		return builder.getRootCluster();
	}

	// TODO
	@Override
	public Cluster performClustering(double[][] distances, String[] clusterNames, Object[] clusterObjects,
			LinkageStrategy linkageStrategy) {

		/* Argument checks */
		checkArguments(distances, clusterNames, linkageStrategy, null);

		/* Setup model */
		List<Cluster> clusters = createClusters(clusterNames, clusterObjects);
		DistanceMap linkages = createLinkages(distances, clusters);

		/* Process */
		HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
		while (!builder.isTreeComplete()) {
			builder.agglomerate(linkageStrategy);
		}
		return builder.getRootCluster();
	}

	@Override
	public List<Cluster> performFlatClustering(double[][] distances, String[] clusterNames,
			LinkageStrategy linkageStrategy, Double threshold) {

		/* Argument checks */
		checkArguments(distances, clusterNames, linkageStrategy, null);

		/* Setup model */
		List<Cluster> clusters = createClusters(clusterNames);
		DistanceMap linkages = createLinkages(distances, clusters);

		/* Process */
		HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
		return builder.flatAgg(linkageStrategy, threshold);
	}

	// TODO
	@Override
	public List<Cluster> performFlatClustering(double[][] distances, String[] clusterNames, Object[] clusterObjects,
			LinkageStrategy linkageStrategy, Double threshold) {

		/* Argument checks */
		checkArguments(distances, clusterNames, linkageStrategy, null);

		/* Setup model */
		List<Cluster> clusters = createClusters(clusterNames, clusterObjects);
		DistanceMap linkages = createLinkages(distances, clusters);

		/* Process */
		HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
		return builder.flatAgg(linkageStrategy, threshold);
	}

	@Override
	public Cluster performWeightedClustering(double[][] distances, String[] clusterNames, double[] weights,
			LinkageStrategy linkageStrategy) {

		return performClustering(distances, clusterNames, linkageStrategy);
	}

	@Override
	public Cluster performWeightedClustering(double[][] distances, String[] clusterNames, Object[] clusterObjects,
			double[] weights, LinkageStrategy linkageStrategy) {

		return performClustering(distances, clusterNames, clusterObjects, linkageStrategy);
	}

}
