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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.ArrayList;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class SimpleMsMs extends SimpleMs implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1247143214195799523L;
	private MsPoint parentMass;
	private int charge;
	private double retention;
	private String title;
	private boolean assignedToCluster;

	public SimpleMsMs(MsPoint parent, ArrayList<MsPoint> msPoints, double rt, int charge2) {

		spectrumPoints = new ArrayList<MsPoint>();

		parentMass = parent;
		spectrumPoints.add(parentMass);
		spectrumPoints.addAll(msPoints);
		retention = rt;
		charge = charge2;
		assignedToCluster = false;

		finalizeSpectrum();
	}

	public int getCharge() {
		return charge;
	}

	public String getMsMsString() {

		String msString = "";

		for (MsPoint p : spectrumPoints)
			msString += MRC2ToolBoxConfiguration.getMzFormat().format(p.getMz()) + " "
					+ MRC2ToolBoxConfiguration.getSpectrumIntensityFormat().format(p.getIntensity()) + "\n";

		return msString;
	}

	public MsPoint getParentMass() {
		return parentMass;
	}

	public double getRetention() {
		return retention;
	}

	public String getTitle() {
		return title;
	}

	public double getTopFragmentIntensity() {

		double topIntensity = 0.0d;

		for (MsPoint p : spectrumPoints) {

			if (!p.equals(parentMass) && p.getIntensity() > topIntensity)
				topIntensity = p.getIntensity();
		}
		return topIntensity;
	}

	public double getTotalFragmentIntensity() {

		double totalIntensity = 0.0d;

		for (MsPoint p : spectrumPoints) {

			if (!p.equals(parentMass))
				totalIntensity += p.getIntensity();
		}
		return totalIntensity;
	}

	public boolean isAssignedToCluster() {
		return assignedToCluster;
	}

	public void setAssignedToCluster(boolean assignedToCluster) {
		this.assignedToCluster = assignedToCluster;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public void setParentMass(MsPoint parentMass) {
		this.parentMass = parentMass;
	}

	public void setRetention(double retention) {
		this.retention = retention;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {

		return title;
	}

}
