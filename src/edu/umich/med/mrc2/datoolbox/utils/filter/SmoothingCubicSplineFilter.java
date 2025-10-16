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

package edu.umich.med.mrc2.datoolbox.utils.filter;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.SmoothingFilterFields;

public class SmoothingCubicSplineFilter extends Filter {

	private SmoothingCubicSpline spline;
	private double rho;
		
	/**
	 * Creates smoothing cubic spline filter with the smoothing parameter rho
	 * 
	 * @param rho
	 *          the smoothing parameter
	 * 
	 * @throws IllegalArgumentException  
	 * 			if rho has wrong value.
	 */
	public SmoothingCubicSplineFilter(double rho) {
		super(null);
		this.rho = rho;
		if (rho < 0 || rho > 1)
			throw new IllegalArgumentException("rho not in [0, 1]");
	}

	@Override
	public double[] filter(double[] xvals, double[] yvals) throws IllegalArgumentException {

		spline  = new SmoothingCubicSpline(xvals, yvals, rho);
		double[]smooth = new double[xvals.length];
		for(int i=0; i<xvals.length; i++) {
			smooth[i] = spline.evaluate(xvals[i]);
		}		
		return smooth;
	}

	@Override
	public FilterClass getFilterClass() {
		return FilterClass.SMOOTHING_CUBIC_SPLINE;
	}

	public double getRho() {
		return rho;
	}
	
	@Override
	public boolean equals(Filter obj) {
		
		if (obj == this)
			return true;

        if (obj == null)
            return false;
        
        if (!SmoothingCubicSplineFilter.class.isAssignableFrom(obj.getClass()))
            return false;

        final SmoothingCubicSplineFilter other = (SmoothingCubicSplineFilter) obj;
        
       if(this.rho != other.getRho())
    	   return false;
       
		return true;
	}

	public SmoothingCubicSplineFilter(Element filterElement) {
		super(filterElement);
	}

	@Override
	public Element getXmlElement() {
		Element filterElement = super.getXmlElement();
		filterElement.setAttribute(
				SmoothingFilterFields.RelWidth.name(), Double.toString(rho));
		return filterElement;
	}

	@Override
	protected void parseParameters(Element xmlElement) {

		if(xmlElement == null)
			return;
		
		String rhoString = 
				xmlElement.getAttributeValue(SmoothingFilterFields.RelWidth.name());		
		rho = Double.parseDouble(rhoString);
	}
}
