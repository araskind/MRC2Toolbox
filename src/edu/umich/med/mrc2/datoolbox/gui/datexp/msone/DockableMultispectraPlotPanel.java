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

package edu.umich.med.mrc2.datoolbox.gui.datexp.msone;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DockableMultispectraPlotPanel extends DefaultSingleCDockable {

	private static final Icon multiSpectraIcon = GuiUtils.getIcon("multiSpectra", 16);
	
	protected DataAnalysisProject currentExperiment;
	protected DataPipeline dataPipeline;
	protected MultispectraPlotPanel mspPlotPanel;
	
	public DockableMultispectraPlotPanel() {

		super("DockableMultispectrumPlotPanel", multiSpectraIcon, 
				"Mass spectra for individual features", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		mspPlotPanel = new MultispectraPlotPanel();
		add(mspPlotPanel, BorderLayout.CENTER);
	}	
	
	public void loadFeatureData(MsFeature feature) {
		// TODO Auto-generated method stub
		
	}
	
	public void loadFeatureData(
			MsFeature feature, 
			Map<DataFile, SimpleMsFeature> fileFeatureMap) {
		
	}
	
	public void clearPanel() {
		
	}

	public void setCurrentExperiment(DataAnalysisProject currentExperiment) {
		this.currentExperiment = currentExperiment;
	}

	public void setDataPipeline(DataPipeline dataPipeline) {
		this.dataPipeline = dataPipeline;
	}
}
