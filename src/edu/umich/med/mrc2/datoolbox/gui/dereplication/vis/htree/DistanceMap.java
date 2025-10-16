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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.htree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Container for linkages with the minimal methods needed in the package Created
 * by Alexandre Masselot on 7/18/14.
 */
public class DistanceMap {

	private class Item implements Comparable<Item> {
		final ClusterPair pair;
		final String hash;
		boolean removed = false;

		Item(ClusterPair p) {
			pair = p;
			hash = hashCodePair(p);
		}

		@Override
		public int compareTo(Item o) {
			return pair.compareTo(o.pair);
		}

		@Override
		public String toString() {
			return hash;
		}
	}
	private Map<String, Item> pairHash;

	private PriorityQueue<Item> data;

	public DistanceMap() {
		data = new PriorityQueue<Item>();
		pairHash = new HashMap<String, Item>();
	}

	public boolean add(ClusterPair link) {
		Item e = new Item(link);
		Item existingItem = pairHash.get(e.hash);
		if (existingItem != null) {
			System.err.println("hashCode = " + existingItem.hash + " adding redundant link:" + link + " (exist:"
					+ existingItem + ")");
			return false;
		} else {
			pairHash.put(e.hash, e);
			data.add(e);
			return true;
		}
	}

	public ClusterPair findByCodePair(Cluster c1, Cluster c2) {
		String inCode = hashCodePair(c1, c2);
		return pairHash.get(inCode).pair;
	}

	String hashCodePair(Cluster lCluster, Cluster rCluster) {
		return hashCodePairNames(lCluster.getName(), rCluster.getName());
	}

	/**
	 * Compute some kind of unique ID for a given cluster pair.
	 * 
	 * @return The ID
	 */
	String hashCodePair(ClusterPair link) {
		return hashCodePair(link.getlCluster(), link.getrCluster());
	}

	String hashCodePairNames(String lName, String rName) {
		if (lName.compareTo(rName) < 0) {
			return lName + "~~~" + rName;// getlCluster().hashCode() + 31 *
											// (getrCluster().hashCode());
		} else {
			return rName + "~~~" + lName;// return getrCluster().hashCode() + 31
											// * (getlCluster().hashCode());
		}
	}

	public List<ClusterPair> list() {
		List<ClusterPair> l = new ArrayList<ClusterPair>();
		for (Item clusterPair : data) {
			l.add(clusterPair.pair);
		}
		return l;
	}

	/**
	 * Peak into the minimum distance
	 * 
	 * @return
	 */
	public Double minDist() {
		Item peek = data.peek();
		if (peek != null)
			return peek.pair.getLinkageDistance();
		else
			return null;
	}

	public boolean remove(ClusterPair link) {
		Item remove = pairHash.remove(hashCodePair(link));
		if (remove == null) {
			return false;
		}
		remove.removed = true;
		data.remove(remove);
		return true;
	}

	public ClusterPair removeFirst() {
		Item poll = data.poll();
		while (poll != null && poll.removed) {
			poll = data.poll();
		}
		if (poll == null) {
			return null;
		}
		ClusterPair link = poll.pair;
		pairHash.remove(poll.hash);
		return link;
	}

	@Override
	public String toString() {
		return data.toString();
	}
}
