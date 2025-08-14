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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.fdata.FeatureDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.MultipleFeaturesFollowupStepAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.stan.MultipleFeaturesStandardAnnotationAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MzFrequencyAnalysisResultsDialog extends JFrame implements BackedByPreferences, ActionListener{
	
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
	private MzFrequencyTablePopupMenu tablePopup;
	
	public MzFrequencyAnalysisResultsDialog(
			ActionListener actionListener,
			Collection<MzFrequencyObject>mzFrequencyObjects,
			String binningParameter) {
		super();
		
		setTitle("M/Z frequency analysis results, binning at " + binningParameter);
		setIconImage(((ImageIcon) mzFrequencyIcon).getImage());
		setSize(new Dimension(800, 800));
		setPreferredSize(new Dimension(800, 800));
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
		tablePopup = new MzFrequencyTablePopupMenu(this, table, enableTrackerCommands);
		table.addTablePopupMenu(tablePopup);
		
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
			
			if (command.equals(level.getName())) {
				
				setPrimaryIdLevel(level);
				break;
			}
		}	
		if(command.startsWith(MainActionCommands.ADD_FEATURES_TO_RECENT_FEATURE_COLLECTION_COMMAND.name()))
			addSelectedFeaturesToRecentCollection(command);	
	}

	private void addSelectedFeaturesToRecentCollection(String command) {
		
		Collection<MsFeature> selected = table.getMsFeaturesForSelectedLines();
		if(selected == null || selected.isEmpty())
			return;
		
		Collection<MSFeatureInfoBundle>selectedMSMSFeatures = 
				((IDWorkbenchPanel)parentPanel).getMsFeatureBundlesForFeatures(selected, 2);
		
		if(selectedMSMSFeatures == null || selectedMSMSFeatures.isEmpty())
			return;
		
		String fsId = command.replace(
				MainActionCommands.ADD_FEATURES_TO_RECENT_FEATURE_COLLECTION_COMMAND.name() + "|", "");
		MsFeatureInfoBundleCollection fColl = 
				FeatureCollectionManager.getMsFeatureInfoBundleCollectionById(fsId);
		if(fColl == null) {
			MessageDialog.showErrorMsg(
					"Requested feature collection not found", this.getContentPane());
			return;
		}
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null)
			FeatureCollectionManager.addFeaturesToCollection(fColl, selectedMSMSFeatures);
		else
			fColl.addFeatures(selectedMSMSFeatures);
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

		Collection<MsFeature>selectedFeatures = table.getMsFeaturesForSelectedLines();
		if(selectedFeatures == null || selectedFeatures.isEmpty())
			return;
		
		if(parentPanel instanceof IDWorkbenchPanel) {
			
			Collection<MSFeatureInfoBundle>bundles = 
					((IDWorkbenchPanel)parentPanel).getMsFeatureBundlesForFeatures(
								selectedFeatures, 2);
			((IDWorkbenchPanel)parentPanel).
					createNewMsmsFeatureCollectionFromSelectedFeatures(bundles);
		}
		if(parentPanel instanceof FeatureDataPanel) {
			
    		createFeatureSubsetForMetabolomicsExperiment(selectedFeatures);
		}
	}
	
	private void createFeatureSubsetForMetabolomicsExperiment(Collection<MsFeature> selectedFeatures) {
		
		if(selectedFeatures == null || selectedFeatures.isEmpty())
			return;
		
		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(experiment == null)
			return;
		
		DataPipeline pipeline = experiment.getActiveDataPipeline();
		if(pipeline == null)
			return;
		
		AddFeatureSubsetDialog addFeatureSubsetDialog = 
				new AddFeatureSubsetDialog(experiment, pipeline, selectedFeatures);
		addFeatureSubsetDialog.setLocationRelativeTo(this);
		addFeatureSubsetDialog.setVisible(true);
	}

	private void addSelectedToExistingFeatureCollection() {

		Collection<MsFeature>selectedFeatures = table.getMsFeaturesForSelectedLines();
		if(selectedFeatures == null || selectedFeatures.isEmpty())
			return;
		
		if(parentPanel instanceof IDWorkbenchPanel) {
			
			Collection<MSFeatureInfoBundle>bundles = 
					((IDWorkbenchPanel)parentPanel).getMsFeatureBundlesForFeatures(
								selectedFeatures, 2);
			((IDWorkbenchPanel)parentPanel).
				addSelectedFeaturesToExistingMsMsFeatureCollection(bundles);
		}
	}
	
	private void setPrimaryIdLevel(MSFeatureIdentificationLevel level) {
		
		Collection<MsFeature> selected = table.getMsFeaturesForSelectedLines();
		if(selected == null || selected.isEmpty())
			return;

		if(parentPanel instanceof IDWorkbenchPanel) {
			
			UpdateMultipleFeatureMetadataTask task = 
					new UpdateMultipleFeatureMetadataTask(
							table.getMsFeaturesForSelectedLines(), level, null, null);
			IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
					"Setting primary ID level for selected MSMS features ...", this, task);
			idp.setLocationRelativeTo(this);
			idp.setVisible(true);
		}
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
		
		if(parentPanel instanceof IDWorkbenchPanel) {
			
			UpdateMultipleFeatureMetadataTask task = 
					new UpdateMultipleFeatureMetadataTask(
							table.getMsFeaturesForSelectedLines(), null, null, fuSteps);
			IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
					"Adding ID followup steps to selected MSMS features ...", this, task);
			idp.setLocationRelativeTo(this);
			idp.setVisible(true);
		}		
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

		Collection<MsFeature> selected = table.getMsFeaturesForSelectedLines();
		if(selected == null || selected.isEmpty())
			return;
		
		Collection<StandardFeatureAnnotation> annotations = 
				standardFeatureAnnotationAssignmentDialog.getSelectedAnnotations();
		if(annotations == null || annotations.isEmpty())
			return;
		
		if(parentPanel instanceof IDWorkbenchPanel) {
			
			UpdateMultipleFeatureMetadataTask task = 
					new UpdateMultipleFeatureMetadataTask(
							table.getMsFeaturesForSelectedLines(), null, annotations, null);
			IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
					"Adding standard annotations to selected MSMS features ...", this, task);
			idp.setLocationRelativeTo(this);
			idp.setVisible(true);
		}
		standardFeatureAnnotationAssignmentDialog.dispose();
	}
	
	class UpdateMultipleFeatureMetadataTask extends LongUpdateTask {

		private Collection<MsFeature>features;
		private Collection<StandardFeatureAnnotation> annotations;
		private MSFeatureIdentificationLevel idLevel;
		private Collection<MSFeatureIdentificationFollowupStep> fuSteps;
		
		public UpdateMultipleFeatureMetadataTask(
				Collection<MsFeature>features,
				MSFeatureIdentificationLevel idLevel,
				Collection<StandardFeatureAnnotation> annotations,
				Collection<MSFeatureIdentificationFollowupStep> fuSteps) {
			super();
			this.features = features;
			this.idLevel = idLevel;
			this.annotations = annotations;
			this.fuSteps = fuSteps;
		}
	
		@Override
		public Void doInBackground() {
			
			if(features == null || features.isEmpty())
				return null;
			
			((IDWorkbenchPanel)parentPanel).toggleTableListeners(false);

			if (annotations != null && !annotations.isEmpty()) {
				
				try {
					((IDWorkbenchPanel)parentPanel).setStandardAnnotationsForMultipleFeatures(
							annotations, 
							2,
							features,
							false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(idLevel != null) {
				
				try {
					((IDWorkbenchPanel)parentPanel).setPrimaryIdLevelForMultipleFeatures(
							idLevel, 
							2,
							features,
							false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fuSteps != null && !fuSteps.isEmpty()) {
				
				try {
					Collection<MSFeatureInfoBundle> fBundles = 
							((IDWorkbenchPanel)parentPanel).getMsFeatureBundlesForFeatures(features,2);
					((IDWorkbenchPanel)parentPanel).setIDFollowUpStepsForMultipleFeatures(
							fuSteps, 
							2,
							fBundles,
							false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}   				
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

	public void updateRecentFeatureCollectionList() {
		tablePopup.updateRecentFeatureCollectionList();
	}
}
