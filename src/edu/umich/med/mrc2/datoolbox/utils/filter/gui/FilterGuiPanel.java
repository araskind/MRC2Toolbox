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

package edu.umich.med.mrc2.datoolbox.utils.filter.gui;

import java.util.Collection;

import javax.swing.JPanel;

import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterClass;

public abstract class FilterGuiPanel extends JPanel implements BackedByPreferences {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7753167930059241676L;
	protected FilterClass filterClass;
		
	public FilterGuiPanel(FilterClass filterClass) {
		super();
		this.filterClass = filterClass;
		createGui();
	}
		
	protected abstract void createGui();

	public abstract Filter getFilter();
	
	protected abstract Collection<String>validateParameters();

	public FilterClass getFilterClass() {
		return filterClass;
	}

	public abstract void loadFilterParameters(Filter newFilter);
}
