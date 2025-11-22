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

package edu.umich.med.mrc2.datoolbox.gui.rgen.mcr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.PeakAbundanceMeasure;
import edu.umich.med.mrc2.datoolbox.data.enums.RtFittingModelType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.rqc.MetabCombinerAlignmentScriptGenerator;
import edu.umich.med.mrc2.datoolbox.rqc.SummaryInputColumns;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableMetabCombinerScriptGenerator extends DefaultSingleCDockable 
	implements ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Icon componentIcon = GuiUtils.getIcon("rScriptMC", 32);
	private Preferences preferences;
	public static final String WORK_DIRECTORY = "WORK_DIRECTORY";
	public static final String USE_EXISTING_ALIGNMENT = "USE_EXISTING_ALIGNMENT";
	public static final String ALIGNMENT_RT_RANGE = "ALIGNMENT_RT_RANGE";
	public static final String MAX_MISSING_PERCENT = "MAX_MISSING_PERCENT";
	public static final String PEAK_ABUNDANCE_MEASURE = "PEAK_ABUNDANCE_MEASURE";
	public static final String BIN_GAP = "BIN_GAP";
	public static final String DATA_SET_RT_ORDER_FLAG = "DATA_SET_RT_ORDER_FLAG";
	public static final String IMPUTE_MISSING = "IMPUTE_MISSING";
	public static final String ANCHOR_MZ_TOLERANCE = "ANCHOR_MZ_TOLERANCE";
	public static final String ANCHOR_AREA_QUANTILE_TOLERANCE = "ANCHOR_AREA_QUANTILE_TOLERANCE";
	public static final String ANCHOR_RT_QUANTILE_TOLERANCE = "ANCHOR_RT_QUANTILE_TOLERANCE";
	public static final String PRIMARY_DATASET_ANCHOR_RT_EXCLUSION_WINDOW = "PRIMARY_DATASET_ANCHOR_RT_EXCLUSION_WINDOW";
	public static final String SECONDARY_DATASET_ANCHOR_RT_EXCLUSION_WINDOW = "SECONDARY_DATASET_ANCHOR_RT_EXCLUSION_WINDOW";
	public static final String SCORING_MZ_WEIGHT = "SCORING_MZ_WEIGHT";
	public static final String SCORING_RT_WEIGHT = "SCORING_RT_WEIGHT";
	public static final String SCORING_ABUNDANCE_WEIGHT = "SCORING_ABUNDANCE_WEIGHT";	
	public static final String MAX_MISSING_BATCH_COUNT = "MAX_MISSING_BATCH_COUNT";
	public static final String USE_PPM_FOR_SCORING_MZ = "USE_PPM_FOR_SCORING_MZ";
	public static final String RT_FITTING_MODEL_TYPE = "RT_FITTING_MODEL_TYPE";
	public static final String USE_ADDUCTS_TO_ADJUST_SCORE = "USE_ADDUCTS_TO_ADJUST_SCORE";
	public static final String MINIMAL_ALIGNMENT_SCORE = "MINIMAL_ALIGNMENT_SCORE";
	public static final String MAX_FEATURE_RANK_FOR_PRIMARY_DATASET = "MAX_FEATURE_RANK_FOR_PRIMARY_DATASET";	
	public static final String MAX_FEATURE_RANK_FOR_SECONDARY_DATASET = "MAX_FEATURE_RANK_FOR_SECONDARY_DATASET";
	public static final String SUBGROUP_SCORE_CUTOFF = "SUBGROUP_SCORE_CUTOFF";
	public static final String MAX_RT_ERROR_FOR_ALIGNED_FEATURES = "MAX_RT_ERROR_FOR_ALIGNED_FEATURES";
	public static final String RESOLVE_ALIGNMENT_CONFLICTS_IN_OUTPUT = "RESOLVE_ALIGNMENT_CONFLICTS_IN_OUTPUT";
	public static final String RT_ORDER_FLAG_IN_OUTPUT = "RT_ORDER_FLAG_IN_OUTPUT";
	
	public static final String BROWSE_COMMAND = "Browse";
	
	private File workDirectory;	
	private File projectDirectory;
	private MetabCombinerScriptDialogToolbar toolbar;	
	private MetabCombinerInputFileListingTable fileListingTable;
	private JTextField workDirectoryTextField;
	private JCheckBox useExistingAlignmentCheckBox;
	private JCheckBox imputeCheckBox;
	private JComboBox<PeakAbundanceMeasure> abundanceMeasureComboBox;
	private JCheckBox rtOrderCheckBox;
	private JSpinner maxPercentMissingSpinner;
	private JFormattedTextField binGapTextField;
	private JFormattedTextField rtMaxTextField;
	private JFormattedTextField rtMinTextField;
	private JCheckBox limitRtSpanCheckBox;
	private JFormattedTextField anchorMzToleranceTextField;
	private JFormattedTextField areaQuantileToleranceTextField;
	private JFormattedTextField primaryDSanchorRtEclusionWindowTextField;
	private JFormattedTextField rtQuantileToleranceTextField;
	private JFormattedTextField secondaryDSanchorRtEclusionWindowTextField;
	private JFormattedTextField scoringMzWeightTextField;
	private JComboBox<MassErrorType> mzDifferenceTypeComboBox;
	private JFormattedTextField scoringRtWeightTextField;
	private JComboBox<RtFittingModelType> rtFittingModelComboBox;
	private JFormattedTextField scoringAbundanceWeightTextField;
	private JCheckBox useAdductsCheckBox;
	private JSpinner minimalAlignmentScoreSpinner;
	private JSpinner subgroupScoreCutoffSpinner;
	private JSpinner primaryMaxRankSpinner;
	private JSpinner secondaryMaxRankSpinner;
	private JCheckBox tableFilterRtOrderCheckBox;
	private JCheckBox resolveConflictsCheckBox;
	private JSpinner maxMissingBatchesSpinner;
	private JFormattedTextField maxRTerrTextField;
	private JTextField existingAlignmentFolderField;
	
	public DockableMetabCombinerScriptGenerator() {
		
		super("DockableMetabCombinerScriptGenerator", componentIcon, 
				"Generate MetabCombiner script for multiple batch alignment", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		toolbar =new MetabCombinerScriptDialogToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{111, 0, 0, 0, 0, 105, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("Save alignment project in");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		workDirectoryTextField = new JTextField();	
		workDirectoryTextField.setEditable(false);
		GridBagConstraints gbc_workDirectoryTextField = new GridBagConstraints();
		gbc_workDirectoryTextField.gridwidth = 5;
		gbc_workDirectoryTextField.insets = new Insets(0, 0, 5, 5);
		gbc_workDirectoryTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_workDirectoryTextField.gridx = 1;
		gbc_workDirectoryTextField.gridy = 0;
		dataPanel.add(workDirectoryTextField, gbc_workDirectoryTextField);
		workDirectoryTextField.setColumns(10);
		
		JButton selectWorkingDirButton = new JButton(BROWSE_COMMAND);
		selectWorkingDirButton.setActionCommand(BROWSE_COMMAND);
		selectWorkingDirButton.addActionListener(this);
		GridBagConstraints gbc_selectWorkingDirButton = new GridBagConstraints();
		gbc_selectWorkingDirButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectWorkingDirButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectWorkingDirButton.gridx = 6;
		gbc_selectWorkingDirButton.gridy = 0;
		dataPanel.add(selectWorkingDirButton, gbc_selectWorkingDirButton);
		
		fileListingTable = new MetabCombinerInputFileListingTable();
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.gridwidth = 7;
		gbc_table.insets = new Insets(0, 0, 5, 0);
		gbc_table.fill = GridBagConstraints.BOTH;
		gbc_table.gridx = 0;
		gbc_table.gridy = 1;
		JScrollPane scrollPane = new JScrollPane(fileListingTable);
		scrollPane.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "MetabCombiner input files for alignment", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		dataPanel.add(scrollPane, gbc_table);
		
		JButton selectMCFilesButton = new JButton(
				MainActionCommands.SELECT_METAB_COMBINER_INPUT_FILES_COMMAND.getName());
		selectMCFilesButton.setActionCommand(
				MainActionCommands.SELECT_METAB_COMBINER_INPUT_FILES_COMMAND.getName());
		selectMCFilesButton.addActionListener(this);
		
		useExistingAlignmentCheckBox = new JCheckBox("Use existing alignment data");
		GridBagConstraints gbc_useExistingAlignmentCheckBox = new GridBagConstraints();
		gbc_useExistingAlignmentCheckBox.gridwidth = 2;
		gbc_useExistingAlignmentCheckBox.anchor = GridBagConstraints.WEST;
		gbc_useExistingAlignmentCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_useExistingAlignmentCheckBox.gridx = 0;
		gbc_useExistingAlignmentCheckBox.gridy = 2;
		dataPanel.add(useExistingAlignmentCheckBox, gbc_useExistingAlignmentCheckBox);
		
		JButton clearMCFilesButton = new JButton(
				MainActionCommands.CLEAR_METAB_COMBINER_INPUT_FILES_COMMAND.getName());
		clearMCFilesButton.setActionCommand(
				MainActionCommands.CLEAR_METAB_COMBINER_INPUT_FILES_COMMAND.getName());
		clearMCFilesButton.addActionListener(this);
		
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 2;
		gbc_btnNewButton_1.gridy = 2;
		dataPanel.add(clearMCFilesButton, gbc_btnNewButton_1);
		
		JButton importFromFileButton = new JButton(
				MainActionCommands.IMPORT_METAB_COMBINER_INPUTS_FROM_FILE_COMMAND.getName());
		importFromFileButton.setActionCommand(
				MainActionCommands.IMPORT_METAB_COMBINER_INPUTS_FROM_FILE_COMMAND.getName());
		importFromFileButton.addActionListener(this);
		GridBagConstraints gbc_importFromFileButton = new GridBagConstraints();
		gbc_importFromFileButton.insets = new Insets(0, 0, 5, 5);
		gbc_importFromFileButton.gridx = 4;
		gbc_importFromFileButton.gridy = 2;
		dataPanel.add(importFromFileButton, gbc_importFromFileButton);
		GridBagConstraints gbc_selectMCFilesButton = new GridBagConstraints();
		gbc_selectMCFilesButton.gridwidth = 2;
		gbc_selectMCFilesButton.anchor = GridBagConstraints.EAST;
		gbc_selectMCFilesButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectMCFilesButton.gridx = 5;
		gbc_selectMCFilesButton.gridy = 2;
		dataPanel.add(selectMCFilesButton, gbc_selectMCFilesButton);
		
		JPanel dataImportParametersPanel = new JPanel();
		dataImportParametersPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "Data import and preprocessing parameters", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		dataPanel.add(scrollPane, gbc_table);
		
		JButton selectExistingAlignmentButton = new JButton("Select alignment project");
		selectExistingAlignmentButton.setActionCommand(
				MainActionCommands.SELECT_EXISTING_MC_ALIGNMENT_PROJECT_COMMAND.getName());
		selectExistingAlignmentButton.addActionListener(this);
		
		GridBagConstraints gbc_selectExistingAlignmentButton = new GridBagConstraints();
		gbc_selectExistingAlignmentButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectExistingAlignmentButton.insets = new Insets(0, 0, 5, 5);
		gbc_selectExistingAlignmentButton.gridx = 0;
		gbc_selectExistingAlignmentButton.gridy = 3;
		dataPanel.add(selectExistingAlignmentButton, gbc_selectExistingAlignmentButton);
		
		existingAlignmentFolderField = new JTextField();
		existingAlignmentFolderField.setEditable(false);
		GridBagConstraints gbc_existingAlignmentFolderField = new GridBagConstraints();
		gbc_existingAlignmentFolderField.gridwidth = 6;
		gbc_existingAlignmentFolderField.insets = new Insets(0, 0, 5, 0);
		gbc_existingAlignmentFolderField.fill = GridBagConstraints.HORIZONTAL;
		gbc_existingAlignmentFolderField.gridx = 1;
		gbc_existingAlignmentFolderField.gridy = 3;
		dataPanel.add(existingAlignmentFolderField, gbc_existingAlignmentFolderField);
		existingAlignmentFolderField.setColumns(10);
		GridBagConstraints gbc_dataImportParametersPanel = new GridBagConstraints();
		gbc_dataImportParametersPanel.gridwidth = 7;
		gbc_dataImportParametersPanel.insets = new Insets(0, 0, 5, 0);
		gbc_dataImportParametersPanel.fill = GridBagConstraints.BOTH;
		gbc_dataImportParametersPanel.gridx = 0;
		gbc_dataImportParametersPanel.gridy = 4;
		dataPanel.add(dataImportParametersPanel, gbc_dataImportParametersPanel);
		GridBagLayout gbl_dataImportParametersPanel = new GridBagLayout();
		gbl_dataImportParametersPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataImportParametersPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_dataImportParametersPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_dataImportParametersPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataImportParametersPanel.setLayout(gbl_dataImportParametersPanel);
		
		limitRtSpanCheckBox = new JCheckBox("Limit alignment to RT span, min");
		GridBagConstraints gbc_limitRtSpanCheckBox = new GridBagConstraints();
		gbc_limitRtSpanCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_limitRtSpanCheckBox.gridx = 0;
		gbc_limitRtSpanCheckBox.gridy = 0;
		dataImportParametersPanel.add(limitRtSpanCheckBox, gbc_limitRtSpanCheckBox);
		
		JLabel lblNewLabel_1 = new JLabel("from");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 0;
		dataImportParametersPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		rtMinTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtMinTextField.setMinimumSize(new Dimension(80, 20));
		rtMinTextField.setPreferredSize(new Dimension(80, 20));
		rtMinTextField.setColumns(10);
		GridBagConstraints gbc_rtMinTextField = new GridBagConstraints();
		gbc_rtMinTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtMinTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtMinTextField.gridx = 2;
		gbc_rtMinTextField.gridy = 0;
		dataImportParametersPanel.add(rtMinTextField, gbc_rtMinTextField);
		
		JLabel lblNewLabel_2 = new JLabel("to");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 3;
		gbc_lblNewLabel_2.gridy = 0;
		dataImportParametersPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		rtMaxTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtMaxTextField.setPreferredSize(new Dimension(80, 20));
		rtMaxTextField.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_rtMaxTextField = new GridBagConstraints();
		gbc_rtMaxTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtMaxTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtMaxTextField.gridx = 4;
		gbc_rtMaxTextField.gridy = 0;
		dataImportParametersPanel.add(rtMaxTextField, gbc_rtMaxTextField);
		
		JPanel anchorSelectionParametersPanel = new JPanel();
		anchorSelectionParametersPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "Anchor selection parameters", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_anchorSelectionParametersPanel = new GridBagConstraints();
		gbc_anchorSelectionParametersPanel.gridheight = 5;
		gbc_anchorSelectionParametersPanel.insets = new Insets(0, 0, 5, 0);
		gbc_anchorSelectionParametersPanel.fill = GridBagConstraints.BOTH;
		gbc_anchorSelectionParametersPanel.gridx = 6;
		gbc_anchorSelectionParametersPanel.gridy = 0;
		dataImportParametersPanel.add(anchorSelectionParametersPanel, gbc_anchorSelectionParametersPanel);
		
		GridBagLayout gbl_anchorSelectionParametersPanel = new GridBagLayout();
		gbl_anchorSelectionParametersPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_anchorSelectionParametersPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_anchorSelectionParametersPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_anchorSelectionParametersPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		anchorSelectionParametersPanel.setLayout(gbl_anchorSelectionParametersPanel);
		
		JLabel lblNewLabel_7 = new JLabel("M/Z tolerance");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_7.gridx = 0;
		gbc_lblNewLabel_7.gridy = 0;
		anchorSelectionParametersPanel.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		anchorMzToleranceTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		anchorMzToleranceTextField.setToolTipText("m/z tolerance for prospective anchors");
		anchorMzToleranceTextField.setMinimumSize(new Dimension(80, 20));
		anchorMzToleranceTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_anchorMzToleranceTextField = new GridBagConstraints();
		gbc_anchorMzToleranceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_anchorMzToleranceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_anchorMzToleranceTextField.gridx = 1;
		gbc_anchorMzToleranceTextField.gridy = 0;
		anchorSelectionParametersPanel.add(anchorMzToleranceTextField, gbc_anchorMzToleranceTextField);
		
		JLabel lblNewLabel_13 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_13 = new GridBagConstraints();
		gbc_lblNewLabel_13.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_13.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_13.gridx = 2;
		gbc_lblNewLabel_13.gridy = 0;
		anchorSelectionParametersPanel.add(lblNewLabel_13, gbc_lblNewLabel_13);
		
		JLabel lblNewLabel_10 = new JLabel("RT exclusion window around each anchor, min");
		GridBagConstraints gbc_lblNewLabel_10 = new GridBagConstraints();
		gbc_lblNewLabel_10.gridwidth = 2;
		gbc_lblNewLabel_10.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_10.gridx = 4;
		gbc_lblNewLabel_10.gridy = 0;
		anchorSelectionParametersPanel.add(lblNewLabel_10, gbc_lblNewLabel_10);
		
		JLabel lblNewLabel_8 = new JLabel("Area quantile tolerance");
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_8.gridx = 0;
		gbc_lblNewLabel_8.gridy = 1;
		anchorSelectionParametersPanel.add(lblNewLabel_8, gbc_lblNewLabel_8);
		
		areaQuantileToleranceTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		areaQuantileToleranceTextField.setToolTipText("Quantile Q tolerance for prospective anchors");
		areaQuantileToleranceTextField.setMinimumSize(new Dimension(80, 20));
		areaQuantileToleranceTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_areaQuantileToleranceTextField = new GridBagConstraints();
		gbc_areaQuantileToleranceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_areaQuantileToleranceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_areaQuantileToleranceTextField.gridx = 1;
		gbc_areaQuantileToleranceTextField.gridy = 1;
		anchorSelectionParametersPanel.add(areaQuantileToleranceTextField, gbc_areaQuantileToleranceTextField);
		
		JLabel lblNewLabel_11 = new JLabel("Primary data set");
		GridBagConstraints gbc_lblNewLabel_11 = new GridBagConstraints();
		gbc_lblNewLabel_11.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_11.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_11.gridx = 4;
		gbc_lblNewLabel_11.gridy = 1;
		anchorSelectionParametersPanel.add(lblNewLabel_11, gbc_lblNewLabel_11);
		
		primaryDSanchorRtEclusionWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		primaryDSanchorRtEclusionWindowTextField.setToolTipText("Optimal values are between 0.01 and 0.05 min (1-3s)");
		primaryDSanchorRtEclusionWindowTextField.setPreferredSize(new Dimension(80, 20));
		primaryDSanchorRtEclusionWindowTextField.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_primaryDSanchorRtEclusionWindowTextField = new GridBagConstraints();
		gbc_primaryDSanchorRtEclusionWindowTextField.insets = new Insets(0, 0, 5, 0);
		gbc_primaryDSanchorRtEclusionWindowTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_primaryDSanchorRtEclusionWindowTextField.gridx = 5;
		gbc_primaryDSanchorRtEclusionWindowTextField.gridy = 1;
		anchorSelectionParametersPanel.add(
				primaryDSanchorRtEclusionWindowTextField, gbc_primaryDSanchorRtEclusionWindowTextField);
		
		JLabel lblNewLabel_9 = new JLabel("RT quantile tolerance");
		GridBagConstraints gbc_lblNewLabel_9 = new GridBagConstraints();
		gbc_lblNewLabel_9.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_9.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_9.gridx = 0;
		gbc_lblNewLabel_9.gridy = 2;
		anchorSelectionParametersPanel.add(lblNewLabel_9, gbc_lblNewLabel_9);
		
		rtQuantileToleranceTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtQuantileToleranceTextField.setToolTipText("Linear RT quantile tolerance for prosepctive anchors");
		rtQuantileToleranceTextField.setPreferredSize(new Dimension(80, 20));
		rtQuantileToleranceTextField.setMinimumSize(new Dimension(80, 20));
		rtQuantileToleranceTextField.setColumns(10);
		GridBagConstraints gbc_rtQuantileToleranceTextField = new GridBagConstraints();
		gbc_rtQuantileToleranceTextField.insets = new Insets(0, 0, 0, 5);
		gbc_rtQuantileToleranceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtQuantileToleranceTextField.gridx = 1;
		gbc_rtQuantileToleranceTextField.gridy = 2;
		anchorSelectionParametersPanel.add(rtQuantileToleranceTextField, gbc_rtQuantileToleranceTextField);
		
		JLabel lblNewLabel_12 = new JLabel("Secondary data set");
		GridBagConstraints gbc_lblNewLabel_12 = new GridBagConstraints();
		gbc_lblNewLabel_12.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_12.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_12.gridx = 4;
		gbc_lblNewLabel_12.gridy = 2;
		anchorSelectionParametersPanel.add(lblNewLabel_12, gbc_lblNewLabel_12);
		
		secondaryDSanchorRtEclusionWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		secondaryDSanchorRtEclusionWindowTextField.setToolTipText("Optimal values are between 0.01 and 0.05 min (1-3s)");
		secondaryDSanchorRtEclusionWindowTextField.setMinimumSize(new Dimension(80, 20));
		secondaryDSanchorRtEclusionWindowTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_secondaryDSanchorRtEclusionWindowTTextField = new GridBagConstraints();
		gbc_secondaryDSanchorRtEclusionWindowTTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_secondaryDSanchorRtEclusionWindowTTextField.gridx = 5;
		gbc_secondaryDSanchorRtEclusionWindowTTextField.gridy = 2;
		anchorSelectionParametersPanel.add(
				secondaryDSanchorRtEclusionWindowTextField, gbc_secondaryDSanchorRtEclusionWindowTTextField);
		
		JLabel lblNewLabel_3 = new JLabel("Max % missing");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.gridwidth = 2;
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 1;
		dataImportParametersPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		maxPercentMissingSpinner = new JSpinner();
		maxPercentMissingSpinner.setModel(new SpinnerNumberModel(50, 0, 100, 1));
		GridBagConstraints gbc_maxPercentMissingSpinner = new GridBagConstraints();
		gbc_maxPercentMissingSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxPercentMissingSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_maxPercentMissingSpinner.gridx = 2;
		gbc_maxPercentMissingSpinner.gridy = 1;
		dataImportParametersPanel.add(maxPercentMissingSpinner, gbc_maxPercentMissingSpinner);
		
		JLabel lblNewLabel_4 = new JLabel("Peak abundance measure");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.gridwidth = 2;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 2;
		dataImportParametersPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		abundanceMeasureComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(PeakAbundanceMeasure.values()));
		GridBagConstraints gbc_abundanceMeasureComboBox = new GridBagConstraints();
		gbc_abundanceMeasureComboBox.gridwidth = 3;
		gbc_abundanceMeasureComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_abundanceMeasureComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_abundanceMeasureComboBox.gridx = 2;
		gbc_abundanceMeasureComboBox.gridy = 2;
		dataImportParametersPanel.add(abundanceMeasureComboBox, gbc_abundanceMeasureComboBox);
		
		JLabel lblNewLabel_5 = new JLabel("M/Z window for feature grouping");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.gridwidth = 2;
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 3;
		dataImportParametersPanel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		binGapTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		binGapTextField.setToolTipText("Parameter used for grouping features by m/z");
		binGapTextField.setMinimumSize(new Dimension(80, 20));
		binGapTextField.setPreferredSize(new Dimension(80, 20));
		binGapTextField.setColumns(10);
		GridBagConstraints gbc_binGapTextField = new GridBagConstraints();
		gbc_binGapTextField.insets = new Insets(0, 0, 5, 5);
		gbc_binGapTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_binGapTextField.gridx = 2;
		gbc_binGapTextField.gridy = 3;
		dataImportParametersPanel.add(binGapTextField, gbc_binGapTextField);
		
		JLabel lblNewLabel_6 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 3;
		gbc_lblNewLabel_6.gridy = 3;
		dataImportParametersPanel.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		rtOrderCheckBox = new JCheckBox("Enforce RT order");
		rtOrderCheckBox.setToolTipText("<HTML>If selected, retention order consistency expected <BR>when resolving conflicting alignments for metabCombiner object inputs.");
		GridBagConstraints gbc_rtOrderCheckBox = new GridBagConstraints();
		gbc_rtOrderCheckBox.anchor = GridBagConstraints.EAST;
		gbc_rtOrderCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_rtOrderCheckBox.gridx = 0;
		gbc_rtOrderCheckBox.gridy = 4;
		dataImportParametersPanel.add(rtOrderCheckBox, gbc_rtOrderCheckBox);
		
		imputeCheckBox = new JCheckBox("Impute missing data");
		imputeCheckBox.setToolTipText("<HTML>If selected imputes the mean mz/rt/Q values for missing features <BR>in metabCombiner object inputs before use in alignment <BR>(not recommended for disparate data alignment).<BR>Otherwise, features with missing information are dropped.");
		GridBagConstraints gbc_imputeCheckBox = new GridBagConstraints();
		gbc_imputeCheckBox.gridwidth = 2;
		gbc_imputeCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_imputeCheckBox.anchor = GridBagConstraints.WEST;
		gbc_imputeCheckBox.gridx = 2;
		gbc_imputeCheckBox.gridy = 4;
		dataImportParametersPanel.add(imputeCheckBox, gbc_imputeCheckBox);
		
		JPanel alignmentAndFilteringParametersPanel = new JPanel();
		alignmentAndFilteringParametersPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "Alignment and filtering parameters", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_alignmentAndFilteringParametersPanel = new GridBagConstraints();
		gbc_alignmentAndFilteringParametersPanel.gridwidth = 7;
		gbc_alignmentAndFilteringParametersPanel.fill = GridBagConstraints.BOTH;
		gbc_alignmentAndFilteringParametersPanel.gridx = 0;
		gbc_alignmentAndFilteringParametersPanel.gridy = 5;
		dataPanel.add(alignmentAndFilteringParametersPanel, gbc_alignmentAndFilteringParametersPanel);
		GridBagLayout gbl_alignmentAndFilteringParametersPanel = new GridBagLayout();
		gbl_alignmentAndFilteringParametersPanel.columnWidths = new int[]{0, 0, 0};
		gbl_alignmentAndFilteringParametersPanel.rowHeights = new int[]{0, 0};
		gbl_alignmentAndFilteringParametersPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_alignmentAndFilteringParametersPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		alignmentAndFilteringParametersPanel.setLayout(gbl_alignmentAndFilteringParametersPanel);
		
		JPanel qualityScoringParametersPanel = new JPanel();
		qualityScoringParametersPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "Quality score calculation parameters", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_qualityScoringParametersPanel = new GridBagConstraints();
		gbc_qualityScoringParametersPanel.insets = new Insets(0, 0, 0, 5);
		gbc_qualityScoringParametersPanel.fill = GridBagConstraints.BOTH;
		gbc_qualityScoringParametersPanel.gridx = 0;
		gbc_qualityScoringParametersPanel.gridy = 0;
		alignmentAndFilteringParametersPanel.add(qualityScoringParametersPanel, gbc_qualityScoringParametersPanel);
		GridBagLayout gbl_qualityScoringParametersPanel = new GridBagLayout();
		gbl_qualityScoringParametersPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_qualityScoringParametersPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_qualityScoringParametersPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_qualityScoringParametersPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		qualityScoringParametersPanel.setLayout(gbl_qualityScoringParametersPanel);
		
		JLabel lblNewLabel_14 = new JLabel("M/Z difference weight");
		GridBagConstraints gbc_lblNewLabel_14 = new GridBagConstraints();
		gbc_lblNewLabel_14.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_14.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_14.gridx = 0;
		gbc_lblNewLabel_14.gridy = 0;
		qualityScoringParametersPanel.add(lblNewLabel_14, gbc_lblNewLabel_14);
		
		scoringMzWeightTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		scoringMzWeightTextField.setToolTipText("Weight for penalizing m/z differences.");
		scoringMzWeightTextField.setMinimumSize(new Dimension(80, 20));
		scoringMzWeightTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_scoringMzWeightTextField = new GridBagConstraints();
		gbc_scoringMzWeightTextField.insets = new Insets(0, 0, 5, 5);
		gbc_scoringMzWeightTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_scoringMzWeightTextField.gridx = 1;
		gbc_scoringMzWeightTextField.gridy = 0;
		qualityScoringParametersPanel.add(scoringMzWeightTextField, gbc_scoringMzWeightTextField);
		
		JLabel lblNewLabel_18 = new JLabel("M/Z units");
		GridBagConstraints gbc_lblNewLabel_18 = new GridBagConstraints();
		gbc_lblNewLabel_18.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_18.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_18.gridx = 3;
		gbc_lblNewLabel_18.gridy = 0;
		qualityScoringParametersPanel.add(lblNewLabel_18, gbc_lblNewLabel_18);
		
		mzDifferenceTypeComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(MassErrorType.values()));
		mzDifferenceTypeComboBox.setMinimumSize(new Dimension(80, 22));
		mzDifferenceTypeComboBox.setPreferredSize(new Dimension(80, 22));
		GridBagConstraints gbc_mzDifferenceTypeComboBox = new GridBagConstraints();
		gbc_mzDifferenceTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_mzDifferenceTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzDifferenceTypeComboBox.gridx = 4;
		gbc_mzDifferenceTypeComboBox.gridy = 0;
		qualityScoringParametersPanel.add(mzDifferenceTypeComboBox, gbc_mzDifferenceTypeComboBox);
		
		JLabel lblNewLabel_15 = new JLabel("RT difference weight");
		GridBagConstraints gbc_lblNewLabel_15 = new GridBagConstraints();
		gbc_lblNewLabel_15.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_15.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_15.gridx = 0;
		gbc_lblNewLabel_15.gridy = 1;
		qualityScoringParametersPanel.add(lblNewLabel_15, gbc_lblNewLabel_15);
		
		scoringRtWeightTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		scoringRtWeightTextField.setToolTipText("Weight for penalizing differences between fitted & observed retention times");
		scoringRtWeightTextField.setMinimumSize(new Dimension(80, 20));
		scoringRtWeightTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_scoringRtWeightTextField = new GridBagConstraints();
		gbc_scoringRtWeightTextField.insets = new Insets(0, 0, 5, 5);
		gbc_scoringRtWeightTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_scoringRtWeightTextField.gridx = 1;
		gbc_scoringRtWeightTextField.gridy = 1;
		qualityScoringParametersPanel.add(scoringRtWeightTextField, gbc_scoringRtWeightTextField);
		
		JLabel lblNewLabel_17 = new JLabel("RT fitting model");
		GridBagConstraints gbc_lblNewLabel_17 = new GridBagConstraints();
		gbc_lblNewLabel_17.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_17.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_17.gridx = 3;
		gbc_lblNewLabel_17.gridy = 1;
		qualityScoringParametersPanel.add(lblNewLabel_17, gbc_lblNewLabel_17);
		
		rtFittingModelComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(RtFittingModelType.values()));
		rtFittingModelComboBox.setToolTipText("Choice of fitted rt model.");
		GridBagConstraints gbc_rtFittingModelComboBox = new GridBagConstraints();
		gbc_rtFittingModelComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_rtFittingModelComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFittingModelComboBox.gridx = 4;
		gbc_rtFittingModelComboBox.gridy = 1;
		qualityScoringParametersPanel.add(rtFittingModelComboBox, gbc_rtFittingModelComboBox);
		
		JLabel lblNewLabel_16 = new JLabel("Abundance difference weight");
		GridBagConstraints gbc_lblNewLabel_16 = new GridBagConstraints();
		gbc_lblNewLabel_16.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_16.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_16.gridx = 0;
		gbc_lblNewLabel_16.gridy = 2;
		qualityScoringParametersPanel.add(lblNewLabel_16, gbc_lblNewLabel_16);
		
		scoringAbundanceWeightTextField = new JFormattedTextField();
		scoringAbundanceWeightTextField.setToolTipText("Weight for differences in Q (abundance quantiles).");
		scoringAbundanceWeightTextField.setPreferredSize(new Dimension(80, 20));
		scoringAbundanceWeightTextField.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_scoringAbundanceWeightTextField = new GridBagConstraints();
		gbc_scoringAbundanceWeightTextField.insets = new Insets(0, 0, 5, 5);
		gbc_scoringAbundanceWeightTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_scoringAbundanceWeightTextField.gridx = 1;
		gbc_scoringAbundanceWeightTextField.gridy = 2;
		qualityScoringParametersPanel.add(scoringAbundanceWeightTextField, gbc_scoringAbundanceWeightTextField);
		
		useAdductsCheckBox = new JCheckBox("Use adducts to adjust scores");
		useAdductsCheckBox.setToolTipText("<HTML>Option to penalize mismatches in (non-empty, non-bracketed)<BR>adduct column annotations.");
		GridBagConstraints gbc_useAdductsCheckBox = new GridBagConstraints();
		gbc_useAdductsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_useAdductsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_useAdductsCheckBox.gridwidth = 2;
		gbc_useAdductsCheckBox.gridx = 3;
		gbc_useAdductsCheckBox.gridy = 2;
		qualityScoringParametersPanel.add(useAdductsCheckBox, gbc_useAdductsCheckBox);
		
		JLabel lblNewLabel_23 = new JLabel("Max. missing batches allowed");
		GridBagConstraints gbc_lblNewLabel_23 = new GridBagConstraints();
		gbc_lblNewLabel_23.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_23.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_23.gridx = 0;
		gbc_lblNewLabel_23.gridy = 3;
		qualityScoringParametersPanel.add(lblNewLabel_23, gbc_lblNewLabel_23);
		
		maxMissingBatchesSpinner = new JSpinner();
		maxMissingBatchesSpinner.setModel(new SpinnerNumberModel(0, 0, 5, 1));
		GridBagConstraints gbc_maxMissingBatchesSpinner = new GridBagConstraints();
		gbc_maxMissingBatchesSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxMissingBatchesSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_maxMissingBatchesSpinner.gridx = 1;
		gbc_maxMissingBatchesSpinner.gridy = 3;
		qualityScoringParametersPanel.add(maxMissingBatchesSpinner, gbc_maxMissingBatchesSpinner);
		
		JPanel outputFilteringParametersPanel = new JPanel();
		outputFilteringParametersPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "Output filtering parameters", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_outputFilteringParametersPanel = new GridBagConstraints();
		gbc_outputFilteringParametersPanel.fill = GridBagConstraints.BOTH;
		gbc_outputFilteringParametersPanel.gridx = 1;
		gbc_outputFilteringParametersPanel.gridy = 0;
		alignmentAndFilteringParametersPanel.add(
				outputFilteringParametersPanel, gbc_outputFilteringParametersPanel);
		GridBagLayout gbl_outputFilteringParametersPanel = new GridBagLayout();
		gbl_outputFilteringParametersPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_outputFilteringParametersPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_outputFilteringParametersPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_outputFilteringParametersPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		outputFilteringParametersPanel.setLayout(gbl_outputFilteringParametersPanel);
		
		JLabel lblNewLabel_19 = new JLabel("Minimal score");
		GridBagConstraints gbc_lblNewLabel_19 = new GridBagConstraints();
		gbc_lblNewLabel_19.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_19.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_19.gridx = 0;
		gbc_lblNewLabel_19.gridy = 0;
		outputFilteringParametersPanel.add(lblNewLabel_19, gbc_lblNewLabel_19);
		
		minimalAlignmentScoreSpinner = new JSpinner();
		minimalAlignmentScoreSpinner.setToolTipText(
				"Minimum allowable score (between 0 & 1) for metabolomics feature pair alignments");
		minimalAlignmentScoreSpinner.setModel(new SpinnerNumberModel(0.5d, 0.0, 1.0, 0.01));
		minimalAlignmentScoreSpinner.setMinimumSize(new Dimension(80, 20));
		minimalAlignmentScoreSpinner.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_minimalAlignmentScoreSpinner = new GridBagConstraints();
		gbc_minimalAlignmentScoreSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_minimalAlignmentScoreSpinner.gridx = 1;
		gbc_minimalAlignmentScoreSpinner.gridy = 0;
		outputFilteringParametersPanel.add(
				minimalAlignmentScoreSpinner, gbc_minimalAlignmentScoreSpinner);
		
		JLabel lblNewLabel_21 = new JLabel("Subgroup score cutoff");
		GridBagConstraints gbc_lblNewLabel_21 = new GridBagConstraints();
		gbc_lblNewLabel_21.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_21.gridx = 3;
		gbc_lblNewLabel_21.gridy = 0;
		outputFilteringParametersPanel.add(lblNewLabel_21, gbc_lblNewLabel_21);
		
		subgroupScoreCutoffSpinner = new JSpinner();
		subgroupScoreCutoffSpinner.setModel(new SpinnerNumberModel(0.1d, 0.0, 1.0, 0.01));
		subgroupScoreCutoffSpinner.setToolTipText(
				"<HTML>Score or mz/rt distances (between 0 & 1)  used to define subgroups."
				+ "<BR>Score difference between a pair of conflicting FPAs.");
		subgroupScoreCutoffSpinner.setPreferredSize(new Dimension(80, 20));
		subgroupScoreCutoffSpinner.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_subgroupScoreCutoffSpinner = new GridBagConstraints();
		gbc_subgroupScoreCutoffSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_subgroupScoreCutoffSpinner.gridx = 4;
		gbc_subgroupScoreCutoffSpinner.gridy = 0;
		outputFilteringParametersPanel.add(
				subgroupScoreCutoffSpinner, gbc_subgroupScoreCutoffSpinner);
		
		JLabel lblNewLabel_20 = new JLabel("Maximal feature rank");
		GridBagConstraints gbc_lblNewLabel_20 = new GridBagConstraints();
		gbc_lblNewLabel_20.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_20.gridwidth = 2;
		gbc_lblNewLabel_20.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_20.gridx = 0;
		gbc_lblNewLabel_20.gridy = 1;
		outputFilteringParametersPanel.add(lblNewLabel_20, gbc_lblNewLabel_20);
		
		JLabel lblNewLabel_22 = new JLabel("Max RT error, min");
		lblNewLabel_22.setToolTipText("");
		GridBagConstraints gbc_lblNewLabel_22 = new GridBagConstraints();
		gbc_lblNewLabel_22.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_22.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_22.gridx = 3;
		gbc_lblNewLabel_22.gridy = 1;
		outputFilteringParametersPanel.add(lblNewLabel_22, gbc_lblNewLabel_22);
		
		maxRTerrTextField = new JFormattedTextField();
		maxRTerrTextField.setToolTipText(
				"Maximum allowable error between model-projected and observed retention time");
		maxRTerrTextField.setMinimumSize(new Dimension(80, 20));
		maxRTerrTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_maxRTerrTextField = new GridBagConstraints();
		gbc_maxRTerrTextField.insets = new Insets(0, 0, 5, 0);
		gbc_maxRTerrTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxRTerrTextField.gridx = 4;
		gbc_maxRTerrTextField.gridy = 1;
		outputFilteringParametersPanel.add(maxRTerrTextField, gbc_maxRTerrTextField);
		
		JLabel lblNewLabel_11_1 = new JLabel("Primary data set");
		GridBagConstraints gbc_lblNewLabel_11_1 = new GridBagConstraints();
		gbc_lblNewLabel_11_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_11_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_11_1.gridx = 0;
		gbc_lblNewLabel_11_1.gridy = 2;
		outputFilteringParametersPanel.add(lblNewLabel_11_1, gbc_lblNewLabel_11_1);
		
		primaryMaxRankSpinner = new JSpinner();
		primaryMaxRankSpinner.setModel(new SpinnerNumberModel(2, 1, 8, 1));
		primaryMaxRankSpinner.setToolTipText("Maximum allowable rank for primary dataset features.");
		GridBagConstraints gbc_primaryMaxRankSpinner = new GridBagConstraints();
		gbc_primaryMaxRankSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_primaryMaxRankSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_primaryMaxRankSpinner.gridx = 1;
		gbc_primaryMaxRankSpinner.gridy = 2;
		outputFilteringParametersPanel.add(primaryMaxRankSpinner, gbc_primaryMaxRankSpinner);
		
		resolveConflictsCheckBox = new JCheckBox("Resolve conflicts");
		resolveConflictsCheckBox.setToolTipText(
				"<HTML>Option to computationally resolve conflicting rows<BR>"
				+ "to a final set of 1-1 feature pair alignments");
		GridBagConstraints gbc_resolveConflictsCheckBox = new GridBagConstraints();
		gbc_resolveConflictsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_resolveConflictsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_resolveConflictsCheckBox.gridx = 3;
		gbc_resolveConflictsCheckBox.gridy = 2;
		outputFilteringParametersPanel.add(resolveConflictsCheckBox, gbc_resolveConflictsCheckBox);
		
		JLabel lblNewLabel_12_1 = new JLabel("Secondary data set");
		GridBagConstraints gbc_lblNewLabel_12_1 = new GridBagConstraints();
		gbc_lblNewLabel_12_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_12_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_12_1.gridx = 0;
		gbc_lblNewLabel_12_1.gridy = 3;
		outputFilteringParametersPanel.add(lblNewLabel_12_1, gbc_lblNewLabel_12_1);
		
		secondaryMaxRankSpinner = new JSpinner();
		secondaryMaxRankSpinner.setToolTipText(
				"Maximum allowable rank for secondary dataset features.");
		secondaryMaxRankSpinner.setModel(new SpinnerNumberModel(2, 1, 8, 1));
		GridBagConstraints gbc_secondaryMaxRankSpinner = new GridBagConstraints();
		gbc_secondaryMaxRankSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_secondaryMaxRankSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_secondaryMaxRankSpinner.gridx = 1;
		gbc_secondaryMaxRankSpinner.gridy = 3;
		outputFilteringParametersPanel.add(secondaryMaxRankSpinner, gbc_secondaryMaxRankSpinner);
		
		tableFilterRtOrderCheckBox = new JCheckBox("Enforce RT order");
		tableFilterRtOrderCheckBox.setToolTipText(
				"<HTML>If \"Resolve Conflicts\" is selected, then this imposes<BR>"
				+ "retention order consistency on rows deemed \"RESOLVED\" within subgroups.");
		GridBagConstraints gbc_tableFilterRtOrderCheckBox = new GridBagConstraints();
		gbc_tableFilterRtOrderCheckBox.anchor = GridBagConstraints.WEST;
		gbc_tableFilterRtOrderCheckBox.gridwidth = 2;
		gbc_tableFilterRtOrderCheckBox.gridx = 3;
		gbc_tableFilterRtOrderCheckBox.gridy = 3;
		outputFilteringParametersPanel.add(tableFilterRtOrderCheckBox, gbc_tableFilterRtOrderCheckBox);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(buttonPanel, BorderLayout.SOUTH);

		JButton btnSave = new JButton(
				MainActionCommands.GENERATE_METAB_COMBINER_SCRIPT_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.GENERATE_METAB_COMBINER_SCRIPT_COMMAND.getName());
		btnSave.addActionListener(this);
		buttonPanel.add(btnSave);
//		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
//		rootPane.setDefaultButton(btnSave);

		loadPreferences();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if (command.equals(BROWSE_COMMAND))
			selectWorkingDirectory();
		
		if (command.equals(MainActionCommands.SELECT_EXISTING_MC_ALIGNMENT_PROJECT_COMMAND.getName()))
			selectExistingMcAlignmentFolder();
		
		if (command.equals(MainActionCommands.SELECT_METAB_COMBINER_INPUT_FILES_COMMAND.getName()))
			selectMetabCombinerInputFiles();
		
		if (command.equals(MainActionCommands.IMPORT_METAB_COMBINER_INPUTS_FROM_FILE_COMMAND.getName()))
			importMetabCombinerInputsFromFile();
		
		if (command.equals(MainActionCommands.CLEAR_METAB_COMBINER_INPUT_FILES_COMMAND.getName()))
			cleartMetabCombinerInputFiles();
		
		if (command.equals(MainActionCommands.GENERATE_METAB_COMBINER_SCRIPT_COMMAND.getName()))
			generateMetabCombinerScript();
	}

	private void importMetabCombinerInputsFromFile() {

		JnaFileChooser fc = new JnaFileChooser(workDirectory.getParentFile());
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File inputListFile = fc.getSelectedFile();
			parseInputList(inputListFile);
		}
	}

	private void parseInputList(File inputListFile) {

		if(workDirectory == null || !workDirectory.exists()) {
			
			MessageDialog.showErrorMsg(
					"Work directory containing the input files for alignment must be specified first", 
					SwingUtilities.getWindowAncestor(this.getContentPane()));
			return;
		}
		String[][] mcInputList = DelimitedTextParser.parseTextFile(
				inputListFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<SummaryInputColumns,Integer>inputColumnMap = new TreeMap<>();
		for(int i=0; i<mcInputList[0].length; i++) {
			
			SummaryInputColumns column = SummaryInputColumns.getOptionByName(mcInputList[0][i]);
			if(column != null)
				inputColumnMap.put(column, i);
		}
		if(!inputColumnMap.containsKey(SummaryInputColumns.PEAK_AREAS)
				|| !inputColumnMap.containsKey(SummaryInputColumns.EXPERIMENT)
				|| !inputColumnMap.containsKey(SummaryInputColumns.BATCH)) {
			MessageDialog.showErrorMsg("Input list file doesn't have all required columns.", 
					SwingUtilities.getWindowAncestor(this.getContentPane()));
			return;
		}
		Collection<String>errors = new ArrayList<String>();
		Set<MetabCombinerFileInputObject>mcioSet = new TreeSet<>();
		for(int i=1; i<mcInputList.length; i++) {
			
			String fileName = mcInputList[i][inputColumnMap.get(SummaryInputColumns.PEAK_AREAS)];
			File inputFile = Paths.get(workDirectory.getAbsolutePath(), fileName).toFile();
			if(!inputFile.exists()) {
				errors.add("File " + inputFile.getAbsolutePath() + " not found");
				continue;
			}
			String experimentId = mcInputList[i][inputColumnMap.get(SummaryInputColumns.EXPERIMENT)];
			String batchId = mcInputList[i][inputColumnMap.get(SummaryInputColumns.BATCH)];
			MetabCombinerFileInputObject mcio = 
					new MetabCombinerFileInputObject(inputFile, experimentId, batchId);
			mcioSet.add(mcio);
		}
		fileListingTable.setModelFromInputObjects(mcioSet);
		if(!errors.isEmpty())
		    MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), 
		    		SwingUtilities.getWindowAncestor(this.getContentPane()));		
	}

	private void selectExistingMcAlignmentFolder() {

		JnaFileChooser fc = new JnaFileChooser(workDirectory.getParentFile());
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			validateExistingProject(projectDirectory);
			projectDirectory = fc.getSelectedFile();
			existingAlignmentFolderField.setText(projectDirectory.getPath());
			
			workDirectory = projectDirectory.getParentFile();
			workDirectoryTextField.setText(workDirectory.getPath());
			
			savePreferences();	
		}
	}

	private void validateExistingProject(File projectDirectory2) {
		// TODO Auto-generated method stub
		
	}

	private void selectWorkingDirectory() {

		JnaFileChooser fc = new JnaFileChooser(workDirectory.getParentFile());
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			workDirectory = fc.getSelectedFile();
			workDirectoryTextField.setText(workDirectory.getPath());
			savePreferences();	
		}
	}

	private void selectMetabCombinerInputFiles() {

		JnaFileChooser fc = new JnaFileChooser(workDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Select Input data files for MetabCombiner");
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File[]mcInputFiles = fc.getSelectedFiles();
			if(mcInputFiles.length > 0) {
				
				fileListingTable.addDataFiles(Arrays.asList(mcInputFiles));
				workDirectory = mcInputFiles[0].getParentFile();
				savePreferences();	
			}
		}
	}
	
	private void cleartMetabCombinerInputFiles() {

		if(!fileListingTable.getAllFiles().isEmpty()) {
			
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to clear input files table?", 
					SwingUtilities.getWindowAncestor(this.getContentPane()));
			if(res == JOptionPane.YES_OPTION)
				fileListingTable.clearTable();
		}
	}
	
	public File getWorkDirectory() {
		return workDirectory;
	}
	
	public File getProjectDirectory() {
		return projectDirectory;
	}
	
	public boolean useExistingAlignment() {
		return useExistingAlignmentCheckBox.isSelected();
	}
	
	public Range getAlignmentRTRange() {
		
		if(!limitRtSpanCheckBox.isSelected())
			return null;
		else {
			Double minRt = null;
			Double maxRt = null;
			try {
				minRt = Double.parseDouble(rtMinTextField.getText());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				maxRt = Double.parseDouble(rtMaxTextField.getText());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(minRt != null && maxRt != null && minRt < maxRt)
				return new Range(minRt, maxRt);
			else
				return null;
		}
	}
	
	public int getMaxMissingPercent() {
		return (int)maxPercentMissingSpinner.getValue();
	}
	
	public PeakAbundanceMeasure getPeakAbundanceMeasure() {
		return (PeakAbundanceMeasure)abundanceMeasureComboBox.getSelectedItem();
	}
	
	public double getBinGap() {
		
		if(binGapTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(binGapTextField.getText());
	}
	
	public boolean getMCDataSetRtOrderFlag() {
		return rtOrderCheckBox.isSelected();
	}
	
	public boolean imputeMissingData() {
		return imputeCheckBox.isSelected();
	}
	
	public double getAnchorMzTolerance(){
		
		if(anchorMzToleranceTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(anchorMzToleranceTextField.getText());
	}
	
	public double getAnchorAreaQuantileTolerance(){
		
		if(areaQuantileToleranceTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(areaQuantileToleranceTextField.getText());
	}
	
	public double getAnchorRtQuantileTolerance(){
		
		if(rtQuantileToleranceTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(rtQuantileToleranceTextField.getText());
	}
	
	public double getPrimaryDataSetAnchorRtExclusionWindow(){
		
		if(primaryDSanchorRtEclusionWindowTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(primaryDSanchorRtEclusionWindowTextField.getText());
	}
	
	public double getSecondaryDataSetAnchorRtExclusionWindow(){
		
		if(secondaryDSanchorRtEclusionWindowTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(secondaryDSanchorRtEclusionWindowTextField.getText());
	}
	
	public double getScoringMZweight(){
		
		if(scoringMzWeightTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(scoringMzWeightTextField.getText());
	}
	
	public double getScoringRTweight(){
		
		if(scoringRtWeightTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(scoringRtWeightTextField.getText());
	}
	
	public double getScoringAbundanceWeight(){
		
		if(scoringAbundanceWeightTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(scoringAbundanceWeightTextField.getText());
	}
	
	public int getMaxMissingBatchCount() {
		return (int)maxMissingBatchesSpinner.getValue();
	}
	
	public boolean usePPMforScoringMz() {
		return (mzDifferenceTypeComboBox.getSelectedItem().equals(MassErrorType.ppm));
	}

	public RtFittingModelType getRtFittingModelTypeForScoring() {
		return (RtFittingModelType)rtFittingModelComboBox.getSelectedItem();
	}
	
	public boolean useAdductsToAdjustScore() {
		return useAdductsCheckBox.isSelected();
	}
	
	public double getMinimalAlignmentScore(){
		return (double)minimalAlignmentScoreSpinner.getValue();
	}
	
	public int getMaxFeatureRankForPrimaryDataSet() {
		return (int)primaryMaxRankSpinner.getValue();
	}
	
	public int getMaxFeatureRankForSecondaryDataSet() {
		return (int)secondaryMaxRankSpinner.getValue();
	}
	
	public double getSubgroupScoreCutoff(){
		return (double)subgroupScoreCutoffSpinner.getValue();
	}
	
	public double getMaxRTerrorForAlignedFeatures(){
		
		if(maxRTerrTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(maxRTerrTextField.getText());
	}
	
	public boolean resolveAlignmentConflictsInOutput() {
		return resolveConflictsCheckBox.isSelected();
	}
	
	public boolean getRtOrderFlagInOutput() {
		return tableFilterRtOrderCheckBox.isSelected();
	}
	
	private void generateMetabCombinerScript() {

		Collection<String>errors = validateFormData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), 
		    		SwingUtilities.getWindowAncestor(this.getContentPane()));
		    return;
		}
		MetabCombinerParametersObject mcpo = new MetabCombinerParametersObject();
		mcpo.setProjectParentDirectory(workDirectory);		
		mcpo.setUseExistingAlignment(useExistingAlignment());
		mcpo.setProjectDirectory(projectDirectory);
		if(!useExistingAlignment())
			mcpo.setProjectDirectory(null);
		
		mcpo.setMetabCombinerFileInputObjectSet(
				fileListingTable.getMetabCombinerFileInputObjects());
		mcpo.setAlignmentRTRange(getAlignmentRTRange());
		mcpo.setMaxMissingPercent(getMaxMissingPercent());
		mcpo.setPeakAbundanceMeasure(getPeakAbundanceMeasure());
		mcpo.setBinGap(getBinGap());
		mcpo.setMcDataSetRtOrderFlag(getMCDataSetRtOrderFlag());
		mcpo.setImputeMissingData(imputeMissingData());
		mcpo.setAnchorMzTolerance(getAnchorMzTolerance());
		mcpo.setAnchorAreaQuantileTolerance(getAnchorAreaQuantileTolerance());
		mcpo.setAnchorRtQuantileTolerance(getAnchorRtQuantileTolerance());
		mcpo.setPrimaryDataSetAnchorRtExclusionWindow(getPrimaryDataSetAnchorRtExclusionWindow());
		mcpo.setSecondaryDataSetAnchorRtExclusionWindow(getSecondaryDataSetAnchorRtExclusionWindow());
		mcpo.setScoringMZweight(getScoringMZweight());
		mcpo.setScoringRTweight(getScoringRTweight());
		mcpo.setScoringAbundanceWeight(getScoringAbundanceWeight());
		mcpo.setMaxMissingBatchCount(getMaxMissingBatchCount());
		mcpo.setUsePPMforScoringMz(usePPMforScoringMz());
		mcpo.setRtFittingModelType(getRtFittingModelTypeForScoring());
		mcpo.setUseAdductsToAdjustScore(useAdductsToAdjustScore());
		mcpo.setMinimalAlignmentScore(getMinimalAlignmentScore());
		mcpo.setMaxFeatureRankForPrimaryDataSet(getMaxFeatureRankForPrimaryDataSet());
		mcpo.setMaxFeatureRankForSecondaryDataSet(getMaxFeatureRankForSecondaryDataSet());
		mcpo.setSubgroupScoreCutoff(getSubgroupScoreCutoff());
		mcpo.setMaxRTerrorForAlignedFeatures(getMaxRTerrorForAlignedFeatures());
		mcpo.setResolveAlignmentConflictsInOutput(resolveAlignmentConflictsInOutput());
		mcpo.setRtOrderFlagInOutput(getRtOrderFlagInOutput());
		
		savePreferences();
		
		MetabCombinerAlignmentScriptGenerator mcsg = 
				new MetabCombinerAlignmentScriptGenerator(mcpo);
		mcsg.createMetabCombinerAlignmentScript();
		if (Desktop.isDesktopSupported()) {
		    try {
		        Desktop.getDesktop().open(mcpo.getProjectParentDirectory());
		    } catch (IOException ex) {
		        ex.printStackTrace();
		    }
		}
	}
	
	private Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<>();
	    if(workDirectory == null || !workDirectory.exists())
	    	errors.add("Work directory not specified or not a valid directory");
	    
	    if(useExistingAlignment() && (projectDirectory == null || !projectDirectory.exists()))
	    	errors.add("Existing aligment project not found");
	    
	    Collection<MetabCombinerFileInputObject> ioList = 
	    		fileListingTable.getMetabCombinerFileInputObjects();
	    if(ioList.isEmpty())
	    	errors.add("No input data files for alignment specified");
	    else {
	    	for(MetabCombinerFileInputObject io : ioList) {
	    		
	    		if(!io.isDefined()) {
	    			errors.add("Experiment and batch must be specified for each input file");
	    			break;
	    		}
	    	}
	    }
	    if(limitRtSpanCheckBox.isSelected() && getAlignmentRTRange() == null)
	    	errors.add("Invalid RT range for data alignment is selected");
	    
	    if(getBinGap() <= 0.0d)
	    	errors.add("M/Z window for feature grouping must be > 0");
	    
	    if(getAnchorMzTolerance() <= 0.0d)
	    	errors.add("M/Z tolerance for anchor selection must be > 0");
		
	    if(getAnchorAreaQuantileTolerance() <= 0.0d)
	    	errors.add("Area quantile tolerance for anchor selection must be > 0");
	    
	    if(getAnchorRtQuantileTolerance() <= 0.0d)
	    	errors.add("Area quantile tolerance for anchor selection must be > 0");
	    
	    if(getPrimaryDataSetAnchorRtExclusionWindow() <= 0.0d)
	    	errors.add("Primary dataset anchor RT exclusion window must be > 0");
	    
	    if(getSecondaryDataSetAnchorRtExclusionWindow() <= 0.0d)
	    	errors.add("Secondary dataset anchor RT exclusion window must be > 0");
	    
	    if(getScoringMZweight() <= 0.0d)
	    	errors.add("M/Z weight for alignment score calculation must be > 0");
	    
	    if(getScoringRTweight() <= 0.0d)
	    	errors.add("RT weight for alignment score calculation must be > 0");
	    
	    if(getScoringAbundanceWeight() <= 0.0d)
	    	errors.add("Abundance weight for alignment score calculation must be > 0");
	    
	    if(getMaxRTerrorForAlignedFeatures() <= 0.0d)
	    	errors.add("Max RT error for output filtering must be > 0");
	    
	    return errors;
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		String baseDirPath = preferences.get(WORK_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
		try {
			workDirectory = Paths.get(baseDirPath).toFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		workDirectoryTextField.setText(workDirectory.getPath());
//		useExistingAlignmentCheckBox.setSelected(
//				preferences.getBoolean(USE_EXISTING_ALIGNMENT, Boolean.FALSE));
		setRtRangeFromPreferences(preferences);		
		maxPercentMissingSpinner.setValue(preferences.getInt(MAX_MISSING_PERCENT, 50));	
		String pamString = preferences.get(PEAK_ABUNDANCE_MEASURE, PeakAbundanceMeasure.median.name());
		abundanceMeasureComboBox.setSelectedItem(PeakAbundanceMeasure.valueOf(pamString));
		binGapTextField.setText(Double.toString(preferences.getDouble(BIN_GAP, 0.005d)));
		rtOrderCheckBox.setSelected(preferences.getBoolean(DATA_SET_RT_ORDER_FLAG, Boolean.TRUE));
		imputeCheckBox.setSelected(preferences.getBoolean(IMPUTE_MISSING, Boolean.FALSE));
		anchorMzToleranceTextField.setText(
				Double.toString(preferences.getDouble(ANCHOR_MZ_TOLERANCE, 0.003d)));
		areaQuantileToleranceTextField.setText(
				Double.toString(preferences.getDouble(ANCHOR_AREA_QUANTILE_TOLERANCE, 0.3d)));
		rtQuantileToleranceTextField.setText(
				Double.toString(preferences.getDouble(ANCHOR_RT_QUANTILE_TOLERANCE, 0.3d)));
		primaryDSanchorRtEclusionWindowTextField.setText(
				Double.toString(preferences.getDouble(PRIMARY_DATASET_ANCHOR_RT_EXCLUSION_WINDOW, 0.03d)));
		secondaryDSanchorRtEclusionWindowTextField.setText(
				Double.toString(preferences.getDouble(SECONDARY_DATASET_ANCHOR_RT_EXCLUSION_WINDOW, 0.03d)));
		scoringMzWeightTextField.setText(
				Double.toString(preferences.getDouble(SCORING_MZ_WEIGHT, 75.0d)));
		scoringRtWeightTextField.setText(
				Double.toString(preferences.getDouble(SCORING_RT_WEIGHT, 10.0d)));
		scoringAbundanceWeightTextField.setText(
				Double.toString(preferences.getDouble(SCORING_ABUNDANCE_WEIGHT, 0.25d)));
		maxMissingBatchesSpinner.setValue(preferences.getInt(MAX_MISSING_BATCH_COUNT, 0));
		if(preferences.getBoolean(USE_PPM_FOR_SCORING_MZ, Boolean.FALSE))
			mzDifferenceTypeComboBox.setSelectedItem(MassErrorType.ppm);
		else
			mzDifferenceTypeComboBox.setSelectedItem(MassErrorType.Da);
		
		String rtFitString = preferences.get(RT_FITTING_MODEL_TYPE, RtFittingModelType.gam.name());
		rtFittingModelComboBox.setSelectedItem(RtFittingModelType.valueOf(rtFitString));
		
		useAdductsCheckBox.setSelected(preferences.getBoolean(USE_ADDUCTS_TO_ADJUST_SCORE, Boolean.FALSE));
		minimalAlignmentScoreSpinner.setValue(preferences.getDouble(MINIMAL_ALIGNMENT_SCORE, 0.5d));	
		primaryMaxRankSpinner.setValue(preferences.getInt(MAX_FEATURE_RANK_FOR_PRIMARY_DATASET, 2));
		secondaryMaxRankSpinner.setValue(preferences.getInt(MAX_FEATURE_RANK_FOR_SECONDARY_DATASET, 2));
		subgroupScoreCutoffSpinner.setValue(preferences.getDouble(SUBGROUP_SCORE_CUTOFF, 0.1d));
		maxRTerrTextField.setValue(preferences.getDouble(MAX_RT_ERROR_FOR_ALIGNED_FEATURES, 10.0d));
		resolveConflictsCheckBox.setSelected(preferences.getBoolean(RESOLVE_ALIGNMENT_CONFLICTS_IN_OUTPUT, Boolean.TRUE));
		tableFilterRtOrderCheckBox.setSelected(preferences.getBoolean(RT_ORDER_FLAG_IN_OUTPUT, Boolean.TRUE));
	}
	
	private void setRtRangeFromPreferences(Preferences preferences) {
		
		String rtRangeString = preferences.get(ALIGNMENT_RT_RANGE, "");
		Range rtRange = null;
		if(!rtRangeString.isBlank()) {
			try {
				rtRange = new Range(rtRangeString);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(rtRange != null) {
				rtMinTextField.setText(Double.toString(rtRange.getMin()));
				rtMaxTextField.setText(Double.toString(rtRange.getMax()));
				limitRtSpanCheckBox.setSelected(true);
			}
			else {
				rtMinTextField.setText("");
				rtMaxTextField.setText("");
				limitRtSpanCheckBox.setSelected(false);
			}
		}
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(WORK_DIRECTORY, workDirectory.getAbsolutePath());
		//	preferences.putBoolean(USE_EXISTING_ALIGNMENT, useExistingAlignment());
		Range rtRange = getAlignmentRTRange();
		if(rtRange == null)
			preferences.put(ALIGNMENT_RT_RANGE, "");
		else
			preferences.put(ALIGNMENT_RT_RANGE, 
					rtRange.getFormattedString(MRC2ToolBoxConfiguration.getRtFormat()));
		
		preferences.putInt(MAX_MISSING_PERCENT, getMaxMissingPercent());
		preferences.put(PEAK_ABUNDANCE_MEASURE, getPeakAbundanceMeasure().name());
		preferences.putDouble(BIN_GAP, getBinGap());
		preferences.putBoolean(DATA_SET_RT_ORDER_FLAG, getMCDataSetRtOrderFlag());
		preferences.putBoolean(IMPUTE_MISSING, imputeMissingData());
		preferences.putDouble(ANCHOR_MZ_TOLERANCE, getAnchorMzTolerance());
		preferences.putDouble(ANCHOR_AREA_QUANTILE_TOLERANCE, getAnchorAreaQuantileTolerance());
		preferences.putDouble(ANCHOR_RT_QUANTILE_TOLERANCE, getAnchorRtQuantileTolerance());
		preferences.putDouble(PRIMARY_DATASET_ANCHOR_RT_EXCLUSION_WINDOW, 
				getPrimaryDataSetAnchorRtExclusionWindow());
		preferences.putDouble(SECONDARY_DATASET_ANCHOR_RT_EXCLUSION_WINDOW, 
				getSecondaryDataSetAnchorRtExclusionWindow());
		preferences.putDouble(SCORING_MZ_WEIGHT, getScoringMZweight());
		preferences.putDouble(SCORING_RT_WEIGHT, getScoringRTweight());
		preferences.putDouble(SCORING_ABUNDANCE_WEIGHT, getScoringAbundanceWeight());
		preferences.putInt(MAX_MISSING_BATCH_COUNT, getMaxMissingBatchCount());
		preferences.putBoolean(USE_PPM_FOR_SCORING_MZ, 
				mzDifferenceTypeComboBox.getSelectedItem().equals(MassErrorType.ppm));
		preferences.put(RT_FITTING_MODEL_TYPE, getRtFittingModelTypeForScoring().name());
		preferences.putBoolean(USE_ADDUCTS_TO_ADJUST_SCORE, useAdductsToAdjustScore());
		preferences.putDouble(MINIMAL_ALIGNMENT_SCORE, getMinimalAlignmentScore());
		preferences.putInt(MAX_FEATURE_RANK_FOR_PRIMARY_DATASET, getMaxFeatureRankForPrimaryDataSet());
		preferences.putInt(MAX_FEATURE_RANK_FOR_SECONDARY_DATASET, getMaxFeatureRankForSecondaryDataSet());
		preferences.putDouble(SUBGROUP_SCORE_CUTOFF, getSubgroupScoreCutoff());
		preferences.putDouble(MAX_RT_ERROR_FOR_ALIGNED_FEATURES, getMaxRTerrorForAlignedFeatures());
		preferences.putBoolean(RESOLVE_ALIGNMENT_CONFLICTS_IN_OUTPUT, resolveAlignmentConflictsInOutput());
		preferences.getBoolean(RT_ORDER_FLAG_IN_OUTPUT, getRtOrderFlagInOutput());
	}
}















