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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.util.List;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.ChromatographicGradientStepFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class ChromatographicGradientStep implements Serializable, Comparable<ChromatographicGradientStep>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7515195422493600374L;
	
	private double startTime;
	private double flowRate;
	private double[] mobilePhaseStartingPercent;
	
	public ChromatographicGradientStep(double startTime, double flowRate) {
		this(startTime, 
			flowRate,
			0.0d,
			0.0d,
			0.0d,
			0.0d);
	}

	public ChromatographicGradientStep(
			double startTime, 
			double mobilePhaseBpercent, 
			double flowRate) {
		this(startTime, 
			flowRate,
			100.0d - mobilePhaseBpercent,
			mobilePhaseBpercent,
			0.0d,
			0.0d);
	}

	public ChromatographicGradientStep(
			double startTime, 
			double flowRate, 
			double mobilePhaseApercent,
			double mobilePhaseBpercent, 
			double mobilePhaseCpercent, 
			double mobilePhaseDpercent) {
		super();
		this.startTime = startTime;
		this.flowRate = flowRate;
		mobilePhaseStartingPercent = new double[]{
				mobilePhaseApercent,
				mobilePhaseBpercent,
				mobilePhaseCpercent,
				mobilePhaseDpercent
			};
	}

	/**
	 * @return the startTime
	 */
	public double getStartTime() {
		return startTime;
	}

	/**
	 * @return the flowRate
	 */
	public double getFlowRate() {
		return flowRate;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * @param flowRate the flowRate to set
	 */
	public void setFlowRate(double flowRate) {
		this.flowRate = flowRate;
	}

	@Override
	public int compareTo(ChromatographicGradientStep o) {
		return Double.compare(startTime, o.getStartTime());
	}

	/**
	 * @return the mobilePhaseStertingPercent
	 */
	public double[] getMobilePhaseStartingPercent() {
		return mobilePhaseStartingPercent;
	}
	
	public ChromatographicGradientStep(Element gradientStepElement) {
		
		this(0.0, 0.0);
		
		startTime = Double.parseDouble(gradientStepElement.getAttributeValue(
				ChromatographicGradientStepFields.startTime.name()));
		flowRate = Double.parseDouble(gradientStepElement.getAttributeValue(
				ChromatographicGradientStepFields.flowRate.name()));
		List<Element> mobilePhaseStartingPercentList = 
				gradientStepElement.getChild(ChromatographicGradientStepFields.mobilePhaseStartingPercentList.name()).
				getChildren(ChromatographicGradientStepFields.mpsp.name());
		for(int i=0; i<mobilePhaseStartingPercentList.size(); i++) {
			
			Element mpspElement = mobilePhaseStartingPercentList.get(i);
			if(mpspElement != null)
				mobilePhaseStartingPercent[i] = Double.parseDouble(mpspElement.getAttributeValue(
						ChromatographicGradientStepFields.spValue.name()));
		}
	}

	@Override
	public Element getXmlElement() {

		Element gradientStepElement = 
				new Element(ObjectNames.ChromatographicGradientStep.name());
		gradientStepElement.setAttribute(
				ChromatographicGradientStepFields.startTime.name(), String.format("%.f3", startTime));
		gradientStepElement.setAttribute(
				ChromatographicGradientStepFields.flowRate.name(), String.format("%.f3", flowRate));
		Element mobilePhaseStartingPercentList = 
				new Element(ChromatographicGradientStepFields.mobilePhaseStartingPercentList.name());
		for(int i=0; i<4; i++) {
			
			Element mpspElement = new Element(ChromatographicGradientStepFields.mpsp.name());
			mpspElement.setAttribute(
					ChromatographicGradientStepFields.spValue.name(), 
					String.format("%.f3", mobilePhaseStartingPercent[i]));
			mobilePhaseStartingPercentList.addContent(mpspElement);
		}
		gradientStepElement.addContent(mobilePhaseStartingPercentList);

		return gradientStepElement;
	}
}









