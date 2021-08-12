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

package edu.umich.med.mrc2.datoolbox.gui.main;

import edu.umich.med.mrc2.datoolbox.gui.automator.AutomatorPanel;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.CompoundDatabasePanel;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.CorrelationResultsPanel;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates.DuplicatesPanel;
import edu.umich.med.mrc2.datoolbox.gui.expdesign.ExperimentDesignPanel;
import edu.umich.med.mrc2.datoolbox.gui.fdata.FeatureDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.integration.DataIntegratorPanel;
import edu.umich.med.mrc2.datoolbox.gui.labnote.LabNoteBookPanel;
import edu.umich.med.mrc2.datoolbox.gui.library.MsLibraryPanel;
import edu.umich.med.mrc2.datoolbox.gui.lims.METLIMSPanel;
import edu.umich.med.mrc2.datoolbox.gui.mgf.MgfPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.MoTrPACDataTrackingPanel;
import edu.umich.med.mrc2.datoolbox.gui.qc.QCPanel;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.worklist.WorklistPanel;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;

public enum PanelList {

	DESIGN("Experiment design", ExperimentDesignPanel.class, Boolean.TRUE),
	FEATURE_DATA("Feature data", FeatureDataPanel.class, Boolean.FALSE),
	WORKLIST("Assay worklist", WorklistPanel.class, Boolean.FALSE),
	QC("Quality control", QCPanel.class, Boolean.FALSE),
	DUPLICATES("Duplicate features", DuplicatesPanel.class, Boolean.FALSE),
	CORRELATIONS("Correlation analysis", CorrelationResultsPanel.class, Boolean.FALSE),
	INTEGRATION("Data integration", DataIntegratorPanel.class, Boolean.FALSE),
	MS_LIBRARY("MS library", MsLibraryPanel.class, Boolean.FALSE),
	DATABASE("Databases", CompoundDatabasePanel.class, Boolean.FALSE),
	ID_WORKBENCH("IDTracker workbench", IDWorkbenchPanel.class, Boolean.FALSE),
	ID_TRACKER_LIMS("ID tracker LIMS", IDTrackerLimsManagerPanel.class, Boolean.FALSE),
	//	LIPIDS("Lipid identification", LipidIdPanel.class, Boolean.FALSE),
	MGF("MGF explorer", MgfPanel.class, Boolean.FALSE),
	AUTOMATOR("Batch data extraction", AutomatorPanel.class, Boolean.FALSE),
	LIMS("METLIMS access", METLIMSPanel.class, Boolean.FALSE),
	LAB_NOTEBOOK("Lab notebook", LabNoteBookPanel.class, Boolean.FALSE),
	MOTRPAC_REPORT_TRACKER("MoTrPAC reports", MoTrPACDataTrackingPanel.class, Boolean.FALSE),
	RAW_DATA_EXAMINER("Raw data examiner", RawDataExaminerPanel.class, Boolean.FALSE),
	;

	private final String name;
	private final Class<?> panelClass;
	private final Boolean visibleByDefault;

	PanelList(String type, Class<?> pClass, Boolean visibleByDefault) {

		this.name = type;
		this.panelClass = pClass;
		this.visibleByDefault = visibleByDefault;
	}

	public String getName() {
		return name;
	}

	public Class<?> getPanelClass() {
		return panelClass;
	}

	public Boolean isVisibleByDefault() {
		return visibleByDefault;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public static PanelList[] getPanelListForConfiguration(StartupConfiguration configuration) {
		
		if(configuration.equals(StartupConfiguration.COMPLETE_TOOLBOX))
			return PanelList.values();
		
		if(configuration.equals(StartupConfiguration.IDTRACKER)) {			
			return new PanelList[] {
					PanelList.ID_TRACKER_LIMS,
					PanelList.ID_WORKBENCH,
					PanelList.DATABASE,
					PanelList.RAW_DATA_EXAMINER,
//					PanelList.MS_LIBRARY,
			};
		}
		return new PanelList[] {};			
	}
}















