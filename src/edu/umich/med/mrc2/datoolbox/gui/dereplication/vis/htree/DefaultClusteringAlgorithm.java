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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class DefaultClusteringAlgorithm implements ClusteringAlgorithm {

	private void checkArguments(double[][] distances, String[] clusterNames, LinkageStrategy linkageStrategy,
			Object[] clusterObjects) {
		if (distances == null || distances.length == 0 || distances[0].length != distances.length) {
			throw new IllegalArgumentException("Invalid distance matrix");
		}
		if (distances.length != clusterNames.length) {
			throw new IllegalArgumentException("Invalid cluster name array");
		}
		if (linkageStrategy == null) {
			throw new IllegalArgumentException("Undefined linkage strategy");
		}
		int uniqueCount = new HashSet<String>(Arrays.asList(clusterNames)).size();
		if (uniqueCount != clusterNames.length) {
			throw new IllegalArgumentException("Duplicate names");
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

	private List<Cluster> createClusters(String[] clusterNames, double[] weights) {
		List<Cluster> clusters = new ArrayList<Cluster>();
		for (int i = 0; i < weights.length; i++) {
			Cluster cluster = new Cluster(clusterNames[i]);
			cluster.setDistance(new Distance(0.0, weights[i]));
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
			for (int row = col + 1; row < clusters.size(); row++) {
				ClusterPair link = new ClusterPair();
				Cluster lCluster = clusters.get(col);
				Cluster rCluster = clusters.get(row);
				link.setLinkageDistance(distances[col][row]);
				link.setlCluster(lCluster);
				link.setrCluster(rCluster);
				linkages.add(link);
			}
		}
		return linkages;
	}

	@Override
	public Cluster performClustering(double[][] distances, String[] clusterNames, LinkageStrategy linkageStrategy) {

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

	@Override
	public Cluster performClustering(double[][] distances, String[] clusterNames, Object[] clusterObjects,
			LinkageStrategy linkageStrategy) {

		checkArguments(distances, clusterNames, linkageStrategy, clusterObjects);
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

		checkArguments(distances, clusterNames, linkageStrategy, null);
		/* Setup model */
		List<Cluster> clusters = createClusters(clusterNames);
		DistanceMap linkages = createLinkages(distances, clusters);

		/* Process */
		HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
		return builder.flatAgg(linkageStrategy, threshold);
	}

	@Override
	public List<Cluster> performFlatClustering(double[][] distances, String[] clusterNames, Object[] clusterObjects,
			LinkageStrategy linkageStrategy, Double threshold) {

		checkArguments(distances, clusterNames, linkageStrategy, clusterObjects);
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

		checkArguments(distances, clusterNames, linkageStrategy, null);

		if (weights.length != clusterNames.length) {
			throw new IllegalArgumentException("Invalid weights array");
		}

		/* Setup model */
		List<Cluster> clusters = createClusters(clusterNames, weights);
		DistanceMap linkages = createLinkages(distances, clusters);

		/* Process */
		HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
		while (!builder.isTreeComplete()) {
			builder.agglomerate(linkageStrategy);
		}

		return builder.getRootCluster();
	}

	@Override
	public Cluster performWeightedClustering(double[][] distances, String[] clusterNames, Object[] clusterObjects,
			double[] weights, LinkageStrategy linkageStrategy) {

		checkArguments(distances, clusterNames, linkageStrategy, null);

		if (weights.length != clusterNames.length) {
			throw new IllegalArgumentException("Invalid weights array");
		}

		/* Setup model */
		List<Cluster> clusters = createClusters(clusterNames, weights);
		DistanceMap linkages = createLinkages(distances, clusters);

		/* Process */
		HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
		while (!builder.isTreeComplete()) {
			builder.agglomerate(linkageStrategy);
		}

		return builder.getRootCluster();
	}

}
