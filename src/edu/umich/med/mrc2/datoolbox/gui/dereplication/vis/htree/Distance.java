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

public class Distance implements Comparable<Distance>, Cloneable {

	private Double distance;
	private Double weight;

	public Distance() {
		this(0.0);
	}

	public Distance(Double distance) {
		this(distance, 1.0);
	}

	public Distance(Double distance, Double weight) {
		this.distance = distance;
		this.weight = weight;
	}

	@Override
	public int compareTo(Distance distance) {
		return distance == null ? 1 : getDistance().compareTo(distance.getDistance());
	}

	public Double getDistance() {
		return distance;
	}

	public Double getWeight() {
		return weight;
	}

	public boolean isNaN() {
		return distance == null || distance.isNaN();
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return String.format("distance : %.2f, weight : %.2f", distance, weight);
	}
}
