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

import org.apache.commons.math3.stat.descriptive.rank.Median;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsMsCluster implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4754560752535136105L;
	private ArrayList<SimpleMsMs> clusterFeatures;
	private String clusterName;
	private double parentMass;
	private Range rtRange;
	private Median median;

	protected final int LESS_THAN = -1;
	protected final int EQUAL_TO = 0;
	protected final int GREATER_THAN = 1;

	private MsPoint[] averageMsMs;

	public MsMsCluster() {

		clusterFeatures = new ArrayList<SimpleMsMs>();
		clusterName = "";
		parentMass = 0.0d;

		median = new Median();
	}

	public void addMsMs(SimpleMsMs msms) {

		clusterFeatures.add(msms);

		if (rtRange == null)
			rtRange = new Range(msms.getRetention());
		else
			rtRange.extendRange(msms.getRetention());

		msms.setAssignedToCluster(true);
		finalizeCluster();
	}

	public void calculateAverageMsMs(double massAccuracy) {

		averageMsMs = MsUtils.createAverageSpectrum(clusterFeatures, massAccuracy);
	}

	private void finalizeCluster() {

		String rtString = "";
		String numString = "";

		if (clusterFeatures.size() > 1) {

			double[] pilist = new double[clusterFeatures.size()];

			for (int i = 0; i < clusterFeatures.size(); i++)
				pilist[i] = clusterFeatures.get(i).getParentMass().getMz();

			rtString = MRC2ToolBoxConfiguration.getRtFormat().format(rtRange.getMin()) + " - "
					+ MRC2ToolBoxConfiguration.getRtFormat().format(rtRange.getMax());
			numString = " spectra";
			parentMass = median.evaluate(pilist);
		} else {
			rtString = MRC2ToolBoxConfiguration.getRtFormat().format(rtRange.getAverage());
			numString = " spectrum";
			parentMass = clusterFeatures.get(0).getParentMass().getMz();
		}
		clusterName = "MSMS of " + MRC2ToolBoxConfiguration.getMzFormat().format(parentMass) + " at " + rtString + " ("
				+ clusterFeatures.size() + numString + ")";
	}

	public MsPoint[] getAverageMsMs() {

		return averageMsMs;
	}

	public SimpleMsMs getBestCandidate() {

		SimpleMsMs topHit = null;
		double totalIntensity = 0.0d;

		for (SimpleMsMs msms : clusterFeatures) {

			if (msms.getTotalFragmentIntensity() > totalIntensity)
				topHit = msms;
		}
		return topHit;
	}

	public ArrayList<SimpleMsMs> getClusterFeatures() {
		return clusterFeatures;
	}

	public String getClusterMsString() {

		String msString = "";

		for (MsPoint p : averageMsMs)
			msString += MRC2ToolBoxConfiguration.getMzFormat().format(p.getMz()) + " "
					+ MRC2ToolBoxConfiguration.getSpectrumIntensityFormat().format(p.getIntensity()) + "\n";

		return msString;
	}

	public String getClusterName() {
		return clusterName;
	}

	public Median getMedian() {
		return median;
	}

	public double getParentMass() {
		return parentMass;
	}

	public double getRt() {
		return rtRange.getAverage();
	}

	public Range getRtRange() {
		return rtRange;
	}

	@Override
	public String toString() {

		return clusterName;
	}
}
