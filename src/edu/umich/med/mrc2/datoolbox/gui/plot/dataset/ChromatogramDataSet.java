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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import org.jfree.data.xy.AbstractXYZDataset;

/**
 * XIC visualizer data set. One data set is created per file shown in this
 * visualizer. We need to create separate data set for each file because the
 * user may add/remove files later.
 */
public class ChromatogramDataSet extends AbstractXYZDataset implements Comparable<ChromatogramDataSet> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 266490862990872139L;

	@Override
	public int compareTo(ChromatogramDataSet arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemCount(int series) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSeriesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Comparable getSeriesKey(int series) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number getX(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number getY(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number getZ(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}
}
