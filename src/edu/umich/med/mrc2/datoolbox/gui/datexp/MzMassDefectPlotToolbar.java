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

package edu.umich.med.mrc2.datoolbox.gui.datexp;

import edu.umich.med.mrc2.datoolbox.gui.plot.PlotToolbar;

public class MzMassDefectPlotToolbar extends PlotToolbar{

	/**
	 *
	 */
	private static final long serialVersionUID = -8756954637925601169L;
	
	public MzMassDefectPlotToolbar(DataExplorerPlotPanel parentPlot) {

		super(parentPlot);

//		toggleLabelsButton = GuiUtils.addButton(this, null, labelInactiveIcon, commandListener,
//				LCMSPlotPanel.TOGGLE_ANNOTATIONS_COMMAND, "Hide labels", buttonDimension);
//
//		addSeparator(buttonDimension);
		
		createZoomBlock();
		addSeparator(buttonDimension);
		createLegendToggle();
		addSeparator(buttonDimension);
		createServiceBlock();
//		toggleLegendIcon(parentPlot.isLegendVisible());
//		toggleAnnotationsIcon(parentPlot.areAnnotationsVisible());
		parentPlot.setToolbar(this);
	}
}


