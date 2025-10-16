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

package edu.umich.med.mrc2.datoolbox.gui.plot.mzdiff;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DoubleValueBin;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMzDiffPlot  extends DefaultSingleCDockable {

	private MzDiffPlotPanel mzDifferencePlot;
	private MzDiffPlotToolbar mzDifferencePlotToolbar;

	private static final Icon componentIcon = GuiUtils.getIcon("chromatogramDotted", 16);

	public DockableMzDiffPlot(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0,0));

		mzDifferencePlot = new MzDiffPlotPanel();
		add(mzDifferencePlot, BorderLayout.CENTER);

		mzDifferencePlotToolbar = new MzDiffPlotToolbar(mzDifferencePlot);
		add(mzDifferencePlotToolbar, BorderLayout.NORTH);
	}

	public synchronized void clearPanel() {
		mzDifferencePlot.removeAllDataSets();
	}

	public void showMzDifferenceDistribution(Collection<DoubleValueBin> massDifferenceBins) {

		mzDifferencePlot.removeAllDataSets();
		mzDifferencePlot.showMzDifferenceDistribution(massDifferenceBins);
	}
}
