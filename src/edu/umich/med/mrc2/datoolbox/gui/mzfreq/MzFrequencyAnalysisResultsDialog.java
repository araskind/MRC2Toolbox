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

package edu.umich.med.mrc2.datoolbox.gui.mzfreq;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.MultipleFeaturesFollowupStepAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.stan.MultipleFeaturesStandardAnnotationAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MzFrequencyAnalysisResultsDialog extends JDialog implements BackedByPreferences, ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2684929912807436860L;

	private static final Icon mzFrequencyIcon = GuiUtils.getIcon("mzFrequency", 32);
	
	private Preferences preferences;
	public static final String OUTPUT_DIR = "OUTPUT_DIR";	
	private File outputDir;
	
	private DockableMRC2ToolboxPanel parentPanel;
	private boolean enableTrackerCommands;
	private MzFrequencyDataTable table;
	private MzFequencyResultsToolbar toolBar;

	private MultipleFeaturesStandardAnnotationAssignmentDialog 
			standardFeatureAnnotationAssignmentDialog;

	private MultipleFeaturesFollowupStepAssignmentDialog followupStepAssignmentDialog;
	
	public MzFrequencyAnalysisResultsDialog(
			ActionListener actionListener,
			Collection<MzFrequencyObject>mzFrequencyObjects,
			String binningParameter) {
		super();
		
		setTitle("M/Z frequency analysis results, binning at " + binningParameter);
		setIconImage(((ImageIcon) mzFrequencyIcon).getImage());
		setSize(new Dimension(800, 800));
		setPreferredSize(new Dimension(800, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		if(actionListener instanceof DockableMRC2ToolboxPanel)
			parentPanel = (DockableMRC2ToolboxPanel)actionListener;
		
		enableTrackerCommands = false;
		if(parentPanel instanceof IDWorkbenchPanel)
			enableTrackerCommands = true;
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(null);
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		toolBar = new MzFequencyResultsToolbar(
				this, enableTrackerCommands);
		panel_1.add(toolBar, BorderLayout.NORTH);
		
		table = new MzFrequencyDataTable();
		panel_1.add(new JScrollPane(table), BorderLayout.CENTER);
		table.setTableModelFromMzFrequencyObjectCollection(mzFrequencyObjects);
		table.addTablePopupMenu(
				new MzFrequencyTablePopupMenu(table, this, enableTrackerCommands));
		
		loadPreferences();
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if (command.equals(MainActionCommands.SAVE_MZ_FREQUENCY_ANALYSIS_RESULTS_COMMAND.getName())) 
			saveMzFrequencyAnalysisResults();
		
		if (command.equals(MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName())) 
			createNewFeatureCollectionFromSelected();
		
		if (command.equals(MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName())) 
			addSelectedToExistingFeatureCollection();
		
		if (command.equals(MainActionCommands.ASSIGN_ID_FOLLOWUP_STEPS_TO_FEATURE_DIALOG_COMMAND.getName())) 
			showIdFollowupStepDialog();
		
		if (command.equals(MainActionCommands.SAVE_ID_FOLLOWUP_STEP_ASSIGNMENT_COMMAND.getName())) 
			addIdFollowupStep();
		
		if (command.equals(MainActionCommands.ASSIGN_STANDARD_FEATURE_ANNOTATIONS_TO_FEATURE_DIALOG_COMMAND.getName())) 
			showStandardFeatureAnnotationDialog();
		
		if (command.equals(MainActionCommands.SAVE_STANDARD_FEATURE_ANNOTATION_ASSIGNMENT_COMMAND.getName())) 
			addStandardFeatureAnnotation();	
			
		for(MSFeatureIdentificationLevel level : IDTDataCache.getMsFeatureIdentificationLevelList()) {
			
			if (command.equals(level.getName()) 
					|| command.equals(MSFeatureIdentificationLevel.SET_PRIMARY + level.getName())) {
				
				Collection<MsFeature>selectedFeatures = table.getMsFeaturesForSelectedLines();
				if(!selectedFeatures.isEmpty()){
					
					((IDWorkbenchPanel)parentPanel).
						setPrimaryIdLevelForMultipleFeatures(level, 2, selectedFeatures, true);
				}
				break;
			}
		}	
	}

	private void saveMzFrequencyAnalysisResults() {

		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null)
			outputDir = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExportsDirectory();
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null)
			outputDir = MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getExportsDirectory();
			
		JnaFileChooser fc = new JnaFileChooser(outputDir);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "TXT", "txt");
		fc.setTitle("Save M/Z Frequency Analysis Results");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = "MzFrequencyAnalysisResults_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".TXT";
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File exportFile  = fc.getSelectedFile();
			String tableData = table.getTableDataAsString();
			Path outputPath = Paths.get(exportFile.getAbsolutePath());
			try {
				Files.writeString(outputPath, 
						tableData, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}		
			outputDir = exportFile.getParentFile();
			savePreferences();
		}
	}

	private void createNewFeatureCollectionFromSelected() {
		// TODO Auto-generated method stub
		
	}
	
	private void addSelectedToExistingFeatureCollection() {
		// TODO Auto-generated method stub
		
	}
	
	private void showIdFollowupStepDialog() {
		
		Collection<MsFeature>selectedFeatures = table.getMsFeaturesForSelectedLines();
		if(selectedFeatures == null || selectedFeatures.isEmpty())
			return;

		followupStepAssignmentDialog = 
				new MultipleFeaturesFollowupStepAssignmentDialog(this);		
		followupStepAssignmentDialog.setLocationRelativeTo(this);
		followupStepAssignmentDialog.setVisible(true);		
	}

	private void addIdFollowupStep() {
		// TODO Auto-generated method stub
		Collection<MSFeatureIdentificationFollowupStep> fuSteps = 
				followupStepAssignmentDialog.getSelectedFollowupSteps();
		if(fuSteps == null || fuSteps.isEmpty())
			return;
		
		
		followupStepAssignmentDialog.dispose();
	}

	private void showStandardFeatureAnnotationDialog() {
			
		Collection<MsFeature>selectedFeatures = table.getMsFeaturesForSelectedLines();
		if(selectedFeatures == null || selectedFeatures.isEmpty())
			return;
		
		standardFeatureAnnotationAssignmentDialog = 
			new MultipleFeaturesStandardAnnotationAssignmentDialog(this);
		
		standardFeatureAnnotationAssignmentDialog.setLocationRelativeTo(this);
		standardFeatureAnnotationAssignmentDialog.setVisible(true);		
	}

	private void addStandardFeatureAnnotation() {
		// TODO Auto-generated method stub
		Collection<StandardFeatureAnnotation> annotations = 
				standardFeatureAnnotationAssignmentDialog.getSelectedAnnotations();
		if(annotations == null || annotations.isEmpty())
			return;
		
		
		standardFeatureAnnotationAssignmentDialog.dispose();
	}
	
	class UpdateMultipleFeatureMetadataTask extends LongUpdateTask {

		private Collection<MsFeature>features;
		private Collection<StandardFeatureAnnotation> annotations;
		private MSFeatureIdentificationLevel idLevel;
		
		public UpdateMultipleFeatureMetadataTask(
				Collection<MsFeature>features,
				MSFeatureIdentificationLevel idLevel,
				Collection<StandardFeatureAnnotation> annotations) {
			super();
			this.features = features;
			this.idLevel = idLevel;
			this.annotations = annotations;
		}
	
		@Override
		public Void doInBackground() {
			
			((IDWorkbenchPanel)parentPanel).toggleTableListeners(false);
			
//			if (command.equals(MainActionCommands.ADD_ID_FOLLOWUP_STEP_COMMAND.getName())) {
//				
//			}
			if (annotations != null && !annotations.isEmpty()) {
				
			}
			if(idLevel != null) {
				
			}
//        	if(idLevelCommand.startsWith(MSFeatureIdentificationLevel.SET_PRIMARY)) {
//				try {
//					setPrimaryIdLevelForMultipleSelectedFeatures(
//							idLevelCommand.replace(MSFeatureIdentificationLevel.SET_PRIMARY, ""));
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//        	}
//			else {
//        		try {
//					setIdLevelForIdentification(idLevelCommand);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}  
//			}    	
			
			((IDWorkbenchPanel)parentPanel).toggleTableListeners(true);
			
			return null;
		}
	}

	@Override
	public void dispose() {
		
		if(MessageDialog.showChoiceWithWarningMsg(
				"Unsaved results will be lost.\n"
				+ "Do you want to close the dialog?", this) == JOptionPane.YES_OPTION) {
			savePreferences();
			super.dispose();
		}
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		//	TODO
		outputDir = Paths.get(preferences.get(
				OUTPUT_DIR, MRC2ToolBoxCore.tmpDir)).toFile();
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		//	TODO
		if(outputDir != null)
			preferences.put(OUTPUT_DIR, outputDir.getAbsolutePath());
	}

	public void setParentPanel(DockableMRC2ToolboxPanel parentPanel) {
		this.parentPanel = parentPanel;
	}

}
