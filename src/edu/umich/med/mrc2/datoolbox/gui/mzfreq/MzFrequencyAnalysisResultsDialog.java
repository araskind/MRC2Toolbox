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
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class MzFrequencyAnalysisResultsDialog extends JDialog implements BackedByPreferences, ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2684929912807436860L;

	private static final Icon mzFrequencyIcon = GuiUtils.getIcon("mzFrequency", 32);
	
	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	
	private DockableMRC2ToolboxPanel parentPanel;
	private boolean enableTrackerCommands;
	private MzFrequencyDataTable table;
	private MzFequencyResultsToolbar toolBar;
	
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
		
		if (command.equals(MainActionCommands.ADD_ID_FOLLOWUP_STEP_DIALOG_COMMAND.getName())) 
			showIdFollowupStepDialog();
		
		if (command.equals(MainActionCommands.ADD_ID_FOLLOWUP_STEP_COMMAND.getName())) 
			addIdFollowupStep();
		
		if (command.equals(MainActionCommands.ADD_STANDARD_FEATURE_ANNOTATION_DIALOG_COMMAND.getName())) 
			showStandardFeatureAnnotationDialog();
		
		if (command.equals(MainActionCommands.ADD_STANDARD_FEATURE_ANNOTATION_COMMAND.getName())) 
			addStandardFeatureAnnotation();	
		
		if(enableTrackerCommands) {
			
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
	}

	private void saveMzFrequencyAnalysisResults() {
		// TODO Auto-generated method stub
		
	}

	private void createNewFeatureCollectionFromSelected() {
		// TODO Auto-generated method stub
		
	}
	
	private void addSelectedToExistingFeatureCollection() {
		// TODO Auto-generated method stub
		
	}
	
	private void showIdFollowupStepDialog() {
		// TODO Auto-generated method stub
		
	}

	private void addIdFollowupStep() {
		// TODO Auto-generated method stub
		
	}

	private void showStandardFeatureAnnotationDialog() {
		// TODO Auto-generated method stub
		
	}

	private void addStandardFeatureAnnotation() {
		// TODO Auto-generated method stub
		
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
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		//	TODO
	}

	public void setParentPanel(DockableMRC2ToolboxPanel parentPanel) {
		this.parentPanel = parentPanel;
	}

}
