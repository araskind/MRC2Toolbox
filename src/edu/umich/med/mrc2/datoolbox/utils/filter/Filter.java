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

package edu.umich.med.mrc2.datoolbox.utils.filter;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.SmoothingFilterFields;

/**
 * Interface for classes implementing a filter on a signal (a combination between
 * x-values and y-values). All classes implementing a filter should inherit from
 * this interface as it can be used at various location like {@link peakml.math#Signal}.
 */
public abstract class Filter
{
	/**
	 * With this method actual filtering is performed. The xvals and yvals arrays
	 * should be of equal length, otherwise an IllegalArgumentException is thrown.
	 * The return value is the new yvals array, with smoothed out values. The
	 * returned array is of the same size as the x and y value arrays.
	 * 
	 * @param xvals		The x-values of the signal.
	 * @param yvals		The y-values of the signal.
	 * @return			The resulting smoothed version of the y-values.
	 */
	public abstract double[] filter(double xvals[], double yvals[]) throws IllegalArgumentException;
	
	public abstract FilterClass getFilterClass();
	
	public abstract boolean equals(Filter otherFilter);
	
	public Element getXmlElement() {
		
		Element filterElement = new Element(ObjectNames.Filter.name());
		filterElement.setAttribute(
				SmoothingFilterFields.FilterCode.name(), getFilterClass().getCode());
		
		return filterElement;
	}
	
	protected abstract void parseParameters(Element xmlElement);
	
	public Filter(Element xmlElement) {
		parseParameters(xmlElement);
	}
}