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

package edu.umich.med.mrc2.datoolbox.data.compare;

import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;

public class TandemMassSpectraComparator extends ObjectCompatrator<TandemMassSpectrum> {

	/**
	 *
	 */
	private static final long serialVersionUID = -8243831285871365658L;

	public TandemMassSpectraComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
	}

	public TandemMassSpectraComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(TandemMassSpectrum o1, TandemMassSpectrum o2) {

		int result;

		switch (property) {

			case MZ:
				result = Double.compare(o1.getParent().getMz(), o2.getParent().getMz());

				if (direction == SortDirection.ASC)
					return result;
				else
					return -result;

			case Area:
				result = Double.compare(o1.getTotalIntensity(), o2.getTotalIntensity());

				if (direction == SortDirection.ASC)
					return result;
				else
					return -result;
				
			case spectrumEntropy:
				result = Double.compare(o1.getEntropy(), o2.getEntropy());

				if (direction == SortDirection.ASC)
					return result;
				else
					return -result;	

			default:
				break;
		}
		throw (new IllegalStateException());
	}
}





