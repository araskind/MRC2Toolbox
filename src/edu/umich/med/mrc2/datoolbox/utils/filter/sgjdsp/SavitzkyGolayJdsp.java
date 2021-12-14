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

package edu.umich.med.mrc2.datoolbox.utils.filter.sgjdsp;

import org.jdom2.Element;

import com.github.psambit9791.jdsp.filter.Savgol;
import com.github.psambit9791.jdsp.signal.Convolution;

import edu.umich.med.mrc2.datoolbox.project.store.SmoothingFilterFields;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterClass;

public class SavitzkyGolayJdsp extends Filter {
	
	private int windowSize;
	private int polynomialOrder;
	private double[] coeffs;
	private SavitzkyGolayMode mode;

	public SavitzkyGolayJdsp(
			int windowSize, 
			int polynomialOrder,
			SavitzkyGolayMode mode) {
		super(null);
        if (polynomialOrder >= windowSize) {
            throw new IllegalArgumentException(
            		"Polynomial order must be less that window size");
        }
		this.windowSize = windowSize;
		this.polynomialOrder = polynomialOrder;
		this.mode = mode;
		Savgol sgFilter = new Savgol(new double[0], windowSize, polynomialOrder);
		coeffs = sgFilter.savgolCoeffs();
	}

	public SavitzkyGolayJdsp(Element xmlElement) {
		super(xmlElement);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double[] filter(double[] xvals, double[] yvals) throws IllegalArgumentException {
		Convolution c = new Convolution(yvals, coeffs);
		return c.convolve1d(mode.name());
	}

	@Override
	public FilterClass getFilterClass() {
		return FilterClass.SAVITZKY_GOLAY_JDSP;
	}

	@Override
	public boolean equals(Filter otherFilter) {
		
		if (otherFilter == this)
			return true;

        if (otherFilter == null)
            return false;
        
        if (!SavitzkyGolayJdsp.class.isAssignableFrom(otherFilter.getClass()))
            return false;

        final SavitzkyGolayJdsp other = (SavitzkyGolayJdsp) otherFilter;
        
       if(this.windowSize != other.getWindowSize())
    	   return false;
       
       if(this.polynomialOrder != other.getPolynomialOrder())
    	   return false;
       
       if(!this.mode.equals(other.getMode()))
    	   return false;
       
		return true;
	}

	@Override
	protected void parseParameters(Element xmlElement) {

		if(xmlElement == null)
			return;
		
		windowSize = Integer.parseInt(
				xmlElement.getAttributeValue(SmoothingFilterFields.Width.name()));
		polynomialOrder = Integer.parseInt(
				xmlElement.getAttributeValue(SmoothingFilterFields.POrder.name()));
		mode = SavitzkyGolayMode.valueOf(
				xmlElement.getAttributeValue(SmoothingFilterFields.SGMode.name()));
		
		Savgol sgFilter = new Savgol(new double[0], windowSize, polynomialOrder);
		coeffs = sgFilter.savgolCoeffs();
	}
	
	@Override
	public Element getXmlElement() {
		
		Element filterElement = super.getXmlElement();
		filterElement.setAttribute(
				SmoothingFilterFields.Width.name(), Integer.toString(windowSize));
		filterElement.setAttribute(
				SmoothingFilterFields.POrder.name(), Integer.toString(polynomialOrder));
		filterElement.setAttribute(
				SmoothingFilterFields.SGMode.name(), mode.name());
		
		return filterElement;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public int getPolynomialOrder() {
		return polynomialOrder;
	}

	public SavitzkyGolayMode getMode() {
		return mode;
	}
}
