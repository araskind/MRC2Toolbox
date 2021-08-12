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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import java.util.ArrayList;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

public class NamedXYSeries extends XYSeries {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7844451060631859409L;
	private ArrayList<String> labels;

	public NamedXYSeries(Comparable key) {

		super(key);

		labels = new ArrayList<String>();
	}

	public void add(Number x, Number y, String label) {

		add(x, y, true);
		labels.add(label);
	}

	public String getLabel(int index) {

		return labels.get(index);
	}

	@Override
	public XYDataItem remove(int index) {

		XYDataItem removed = super.remove(index);
		labels.remove(index);
		return removed;
	}
}
