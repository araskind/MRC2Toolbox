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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.IdFollowupStepTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.stan.StandardFeatureAnnotationTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class AnnotationFilterDialog extends JDialog implements BackedByPreferences, ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Icon filterIDLIcon = GuiUtils.getIcon("filterIdStatus", 32);
	private static final String RESET_COMMAND = "Reset";
	private static final String CLEAR_ID_LEVELS_SELECTION_COMMAND = 
			"CLEAR_ID_LEVELS_SELECTION_COMMAND";
	private static final String CLEAR_STD_ANNOTATION_SELECTION_COMMAND = 
			"CLEAR_STD_ANNOTATION_SELECTION_COMMAND";
	private static final String CLEAR_FOLLOWUP_STEPS_SELECTION_COMMAND = 
			"CLEAR_FOLLOWUP_STEPS_SELECTION_COMMAND";
	
	private Preferences preferences;
	private static final String PROPERTIES_DELIMITER = "@";
	private static final String INCLUDE_ID_LEVEL = "INCLUDE_ID_LEVEL";
	private static final String INCLUDE_STD_ANNOTATION = "INCLUDE_STD_ANNOTATION";
	private static final String INCLUDE_FOLLOWUP = "INCLUDE_FOLLOWUP";
	private static final String ID_LEVEL_LIST = "ID_LEVEL_LIST";
	private static final String STD_ANNOTATION_LIST = "STD_ANNOTATION_LIST";
	private static final String FOLLOWUP_LIST = "FOLLOWUP_LIST";
	
	private Collection<MSFeatureInfoBundle> featuresToFilter;
	private MinimalIdLevelTable idLevelTable;
	private StandardFeatureAnnotationTable standardFeatureAnnotationTable;
	private IdFollowupStepTable idFollowupStepTable;
	private JRadioButton 
		includeIdLevelRadioButton,
		excludeIdLevelRadioButton,
		includeStdAnnotationRadioButton,
		excludeStdAnnotationRadioButton,
		includeFollowupRadioButton,
		excludeFollowupRadioButton;
	
	public AnnotationFilterDialog(
			ActionListener listener, Collection<MSFeatureInfoBundle>featuresToFilter) {

		super();
		setTitle("Filter MSMS features using ID levels / annotations");
		setIconImage(((ImageIcon)filterIDLIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 640));
		setPreferredSize(new Dimension(800, 640));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.featuresToFilter = featuresToFilter;
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		idLevelTable = new MinimalIdLevelTable();
		JScrollPane scrollPane = new JScrollPane(idLevelTable);
		scrollPane.setBorder(new TitledBorder(null, "ID levels", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel.add(scrollPane, gbc_scrollPane);
		
		standardFeatureAnnotationTable = new StandardFeatureAnnotationTable();
		standardFeatureAnnotationTable.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane_1 = new JScrollPane(standardFeatureAnnotationTable);
		scrollPane_1.setBorder(new TitledBorder(null, "Standard annotations", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridwidth = 3;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 3;
		gbc_scrollPane_1.gridy = 0;
		panel.add(scrollPane_1, gbc_scrollPane_1);
		
		idFollowupStepTable = new IdFollowupStepTable();
		idFollowupStepTable.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane_2 = new JScrollPane(idFollowupStepTable);
		scrollPane_2.setBorder(new TitledBorder(null, "Followup steps", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.gridwidth = 3;
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 6;
		gbc_scrollPane_2.gridy = 0;
		panel.add(scrollPane_2, gbc_scrollPane_2);
		
		ButtonGroup idLevelButtonGroup = new ButtonGroup();
				
		includeIdLevelRadioButton = new JRadioButton("Include");
		includeIdLevelRadioButton.setForeground(new Color(0, 128, 0));
		includeIdLevelRadioButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_includeIdLevelRadioButton = new GridBagConstraints();
		gbc_includeIdLevelRadioButton.anchor = GridBagConstraints.WEST;
		gbc_includeIdLevelRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_includeIdLevelRadioButton.gridx = 0;
		gbc_includeIdLevelRadioButton.gridy = 1;
		panel.add(includeIdLevelRadioButton, gbc_includeIdLevelRadioButton);		
		idLevelButtonGroup.add(includeIdLevelRadioButton);
		
		excludeIdLevelRadioButton = new JRadioButton("Exclude");
		excludeIdLevelRadioButton.setForeground(new Color(255, 0, 0));
		excludeIdLevelRadioButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_excludeIdLevelRadioButton = new GridBagConstraints();
		gbc_excludeIdLevelRadioButton.anchor = GridBagConstraints.WEST;
		gbc_excludeIdLevelRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_excludeIdLevelRadioButton.gridx = 1;
		gbc_excludeIdLevelRadioButton.gridy = 1;
		panel.add(excludeIdLevelRadioButton, gbc_excludeIdLevelRadioButton);		
		idLevelButtonGroup.add(excludeIdLevelRadioButton);
		
		ButtonGroup stdAnnotationButtonGroup = new ButtonGroup();
		
		JButton clearIdLevelsButton = new JButton("Clear");
		clearIdLevelsButton.setActionCommand(CLEAR_ID_LEVELS_SELECTION_COMMAND);
		clearIdLevelsButton.addActionListener(this);
		GridBagConstraints gbc_clearIdLevelsButton = new GridBagConstraints();
		gbc_clearIdLevelsButton.anchor = GridBagConstraints.WEST;
		gbc_clearIdLevelsButton.insets = new Insets(0, 0, 0, 5);
		gbc_clearIdLevelsButton.gridx = 2;
		gbc_clearIdLevelsButton.gridy = 1;
		panel.add(clearIdLevelsButton, gbc_clearIdLevelsButton);
		
		includeStdAnnotationRadioButton = new JRadioButton("Include");
		includeStdAnnotationRadioButton.setForeground(new Color(0, 128, 0));
		includeStdAnnotationRadioButton.setFont(new Font("Tahoma", Font.BOLD, 11));		
		GridBagConstraints gbc_includeStdAnnotationRadioButton = new GridBagConstraints();
		gbc_includeStdAnnotationRadioButton.anchor = GridBagConstraints.WEST;
		gbc_includeStdAnnotationRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_includeStdAnnotationRadioButton.gridx = 3;
		gbc_includeStdAnnotationRadioButton.gridy = 1;
		panel.add(includeStdAnnotationRadioButton, gbc_includeStdAnnotationRadioButton);
		stdAnnotationButtonGroup.add(includeStdAnnotationRadioButton);
				
		excludeStdAnnotationRadioButton = new JRadioButton("Exclude");
		excludeStdAnnotationRadioButton.setForeground(new Color(255, 0, 0));
		excludeStdAnnotationRadioButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_excludeStdAnnotationRadioButton = new GridBagConstraints();
		gbc_excludeStdAnnotationRadioButton.anchor = GridBagConstraints.WEST;
		gbc_excludeStdAnnotationRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_excludeStdAnnotationRadioButton.gridx = 4;
		gbc_excludeStdAnnotationRadioButton.gridy = 1;
		panel.add(excludeStdAnnotationRadioButton, gbc_excludeStdAnnotationRadioButton);
		stdAnnotationButtonGroup.add(excludeStdAnnotationRadioButton);
		
		ButtonGroup followupButtonGroup = new ButtonGroup();
		
		JButton clearStdAnnotButton = new JButton("Clear");
		clearStdAnnotButton.setActionCommand(CLEAR_STD_ANNOTATION_SELECTION_COMMAND);
		clearStdAnnotButton.addActionListener(this);
		GridBagConstraints gbc_clearStdAnnotButton = new GridBagConstraints();
		gbc_clearStdAnnotButton.anchor = GridBagConstraints.WEST;
		gbc_clearStdAnnotButton.insets = new Insets(0, 0, 0, 5);
		gbc_clearStdAnnotButton.gridx = 5;
		gbc_clearStdAnnotButton.gridy = 1;
		panel.add(clearStdAnnotButton, gbc_clearStdAnnotButton);
		
		includeFollowupRadioButton = new JRadioButton("Include");
		includeFollowupRadioButton.setForeground(new Color(0, 128, 0));
		includeFollowupRadioButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_includeFollowupRadioButton = new GridBagConstraints();
		gbc_includeFollowupRadioButton.anchor = GridBagConstraints.WEST;
		gbc_includeFollowupRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_includeFollowupRadioButton.gridx = 6;
		gbc_includeFollowupRadioButton.gridy = 1;
		panel.add(includeFollowupRadioButton, gbc_includeFollowupRadioButton);
		followupButtonGroup.add(includeFollowupRadioButton);
		
		excludeFollowupRadioButton = new JRadioButton("Exclude");
		excludeFollowupRadioButton.setForeground(new Color(255, 0, 0));
		excludeFollowupRadioButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_excludeFollowupRadioButton = new GridBagConstraints();
		gbc_excludeFollowupRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_excludeFollowupRadioButton.anchor = GridBagConstraints.WEST;
		gbc_excludeFollowupRadioButton.gridx = 7;
		gbc_excludeFollowupRadioButton.gridy = 1;
		panel.add(excludeFollowupRadioButton, gbc_excludeFollowupRadioButton);
		followupButtonGroup.add(excludeFollowupRadioButton);
		
		JButton clearFollowupsButton = new JButton("Clear");
		clearFollowupsButton.setActionCommand(CLEAR_FOLLOWUP_STEPS_SELECTION_COMMAND);
		clearFollowupsButton.addActionListener(this);
		GridBagConstraints gbc_clearFollowupsButton = new GridBagConstraints();
		gbc_clearFollowupsButton.anchor = GridBagConstraints.WEST;
		gbc_clearFollowupsButton.gridx = 8;
		gbc_clearFollowupsButton.gridy = 1;
		panel.add(clearFollowupsButton, gbc_clearFollowupsButton);
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);
		
		JButton resetButton = new JButton("Clear filters");
		resetButton.addActionListener(this);
		resetButton.setActionCommand(RESET_COMMAND);
		buttonPanel.add(resetButton);

		JButton filterButton = new JButton("Filter");
		filterButton.addActionListener(listener);
		filterButton.setActionCommand(
				MainActionCommands.FILTER_FEATURES_BY_IDL_ANNOTATION_COMMAND.getName());
		buttonPanel.add(filterButton);

		JRootPane rootPane = SwingUtilities.getRootPane(filterButton);
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(filterButton);
		populateTables();
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {		
		savePreferences();
		super.dispose();
	}
	
	private void populateTables() {
		
		idLevelTable.setTableModelFromLevelList(
				IDTDataCache.getMsFeatureIdentificationLevelList());
		idFollowupStepTable.setTableModelFromFollowupStepList(
				IDTDataCache.getMsFeatureIdentificationFollowupStepList());
		standardFeatureAnnotationTable.setTableModelFromStandardFeatureAnnotationList(
				IDTDataCache.getStandardFeatureAnnotationList());
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if (command.equals(RESET_COMMAND))
			resetFilters();
		
		if (command.equals(CLEAR_ID_LEVELS_SELECTION_COMMAND))
			idLevelTable.clearSelection();
		
		if (command.equals(CLEAR_STD_ANNOTATION_SELECTION_COMMAND))
			standardFeatureAnnotationTable.clearSelection();
		
		if (command.equals(CLEAR_FOLLOWUP_STEPS_SELECTION_COMMAND))
			idFollowupStepTable.clearSelection();
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;	
		
		String idLevelListString = preferences.get(ID_LEVEL_LIST, "");
		if(!idLevelListString.isEmpty()) {
			
			 String[] idLevelList =  
					 idLevelListString.split(PROPERTIES_DELIMITER);
			 
			 Collection<MSFeatureIdentificationLevel>selectedLevels = 
					 new ArrayList<MSFeatureIdentificationLevel>();
			 for(String id : idLevelList) {
				 MSFeatureIdentificationLevel level = 
						 IDTDataCache.getMSFeatureIdentificationLevelById(id);
				 if(level != null)
					 selectedLevels.add(level);
			 }
			 if(!selectedLevels.isEmpty())
				 idLevelTable.selectLevels(selectedLevels);
		}
		
		String stdAnnotListString = preferences.get(STD_ANNOTATION_LIST, "");
		if(!stdAnnotListString.isEmpty()) {
			
			 String[] stdAnnotList =  
					 stdAnnotListString.split(PROPERTIES_DELIMITER);
			 
			 Collection<StandardFeatureAnnotation>stdAnnotations = 
					 new ArrayList<StandardFeatureAnnotation>();
			 for(String id : stdAnnotList) {
				 StandardFeatureAnnotation stdAnn = 
						 IDTDataCache.getStandardFeatureAnnotationById(id);
				 if(stdAnn != null)
					 stdAnnotations.add(stdAnn);
			 }
			 if(!stdAnnotations.isEmpty())
				 standardFeatureAnnotationTable.selectStandardFeatureAnnotations(stdAnnotations);
		}
		
		String followUpStepListString = preferences.get(FOLLOWUP_LIST, "");
		if(!followUpStepListString.isEmpty()) {
			
			 String[] followUpStepList =  
					 followUpStepListString.split(PROPERTIES_DELIMITER);
			 
			 Collection<MSFeatureIdentificationFollowupStep>followUps = 
					 new ArrayList<MSFeatureIdentificationFollowupStep>();
			 for(String id : followUpStepList) {
				 MSFeatureIdentificationFollowupStep fus = 
						 IDTDataCache.getMSFeatureIdentificationFollowupStepById(id);
				 if(fus != null)
					 followUps.add(fus);
			 }
			 if(!followUps.isEmpty())
				 idFollowupStepTable.selectFollowupSteps(followUps);
		}
		
		boolean includeIdLevel = preferences.getBoolean(INCLUDE_ID_LEVEL, true);
		if(includeIdLevel)
			includeIdLevelRadioButton.setSelected(true);
		else
			excludeIdLevelRadioButton.setSelected(true);
		
		boolean includeStdAnnotation = preferences.getBoolean(INCLUDE_STD_ANNOTATION, true);
		if(includeStdAnnotation)
			includeStdAnnotationRadioButton.setSelected(true);
		else
			excludeStdAnnotationRadioButton.setSelected(true);
		
		boolean includeFollowup = preferences.getBoolean(INCLUDE_FOLLOWUP, true);
		if(includeFollowup)
			includeFollowupRadioButton.setSelected(true);
		else
			excludeFollowupRadioButton.setSelected(true);
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		
		Collection<String>idLevelSet = idLevelTable.getSelectedLevels().stream().
				map(l -> l.getId()).collect(Collectors.toSet());
		preferences.put(ID_LEVEL_LIST, 
				StringUtils.join(idLevelSet, PROPERTIES_DELIMITER));
		
		Collection<String>stdAnnotSet = 
				standardFeatureAnnotationTable.getSelectedStandardFeatureAnnotations().stream().
				map(l -> l.getId()).collect(Collectors.toSet());
		preferences.put(STD_ANNOTATION_LIST, 
				StringUtils.join(stdAnnotSet, PROPERTIES_DELIMITER));
		
		Collection<String>fusSet = idFollowupStepTable.getSelectedFollowupSteps().stream().
				map(l -> l.getId()).collect(Collectors.toSet());
		preferences.put(FOLLOWUP_LIST, 
				StringUtils.join(fusSet, PROPERTIES_DELIMITER));
		
		preferences.putBoolean(INCLUDE_ID_LEVEL, includeIdLevelRadioButton.isSelected());
		preferences.putBoolean(INCLUDE_STD_ANNOTATION, includeStdAnnotationRadioButton.isSelected());
		preferences.putBoolean(INCLUDE_FOLLOWUP, includeFollowupRadioButton.isSelected());
	}

	private void resetFilters() {

		idLevelTable.clearSelection();
		idFollowupStepTable.clearSelection();
		standardFeatureAnnotationTable.clearSelection();
	}
	
	public Collection<MSFeatureIdentificationLevel> getSelectedIdLevels() {
		return idLevelTable.getSelectedLevels();
	}
	
	public boolean includeSelectedIdLevels() {
		return includeIdLevelRadioButton.isSelected();
	}
	
	public Collection<StandardFeatureAnnotation> getSelectedStandardFeatureAnnotations() {
		return standardFeatureAnnotationTable.getSelectedStandardFeatureAnnotations();
	}
	
	public boolean includeStandardFeatureAnnotations() {
		return includeStdAnnotationRadioButton.isSelected();
	}
	
	public Collection<MSFeatureIdentificationFollowupStep> getSelectedFollowupSteps() {
		return idFollowupStepTable.getSelectedFollowupSteps();
	}
	
	public boolean includeFollowupSteps() {
		return includeFollowupRadioButton.isSelected();
	}

	public Collection<String> verifyParameters() {

		Collection<String>errors = new ArrayList<String>();
		if(idLevelTable.getSelectedLevels().isEmpty()
				&& idFollowupStepTable.getSelectedFollowupSteps().isEmpty()
				&& standardFeatureAnnotationTable.getSelectedStandardFeatureAnnotations().isEmpty())
			errors.add("No values selected to filter the data.");
		
		return errors;
	}

	public Collection<MSFeatureInfoBundle> getFeaturesToFilter() {
		return featuresToFilter;
	}
	
	public IDLAnnotationFilterParameters getFilterParameters() {
		
		if(!verifyParameters().isEmpty())
			return null;
		else 
			return new IDLAnnotationFilterParameters(
				getSelectedIdLevels(),
				getSelectedStandardFeatureAnnotations(),
				getSelectedFollowupSteps(),
				includeSelectedIdLevels(),
				includeStandardFeatureAnnotations(),
				includeFollowupSteps());
	}
}








