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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.spec;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.DataFileToAcquisitionMethodTable;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.MassSpectraAveragingTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableMsExtractorPanel extends DefaultSingleCDockable implements ActionListener, BackedByPreferences {

	private static final Icon chromIcon = GuiUtils.getIcon("filterMsMs", 16);
	
//	private JList<DataFile> sampleFileList;
//	private DefaultListModel<DataFile> fileListModel;
	
	private DataFileToAcquisitionMethodTable rawDataFileTable;
	private RawDataExaminerPanel rawDataPanel;
	private JButton extractAverageSpectrumButton;
	private JButton findByFragmentButton;
	private JButton findByParentIonButton;	
	private JFormattedTextField avgMsRtFromTextField;
	private JFormattedTextField avgMsRtToTextField;
	private JFormattedTextField mzBinningValueTextField;
	private JComboBox mzBinningUnitsComboBox;	
	private JTextField findMzInMsMsTextField;
	private JFormattedTextField findMzWindowTextField;
	private JComboBox findMzUnitsComboBox;
	private JCheckBox findMzUseRtLimitCheckBox;
	private JFormattedTextField findMzRtFromTextField;
	private JFormattedTextField findMzRtToTextField;
	private JSpinner msLevelComboBox;
	private JTextField findMzInMsTextField;
	private JCheckBox findMsOneMzUseRtLimitCheckBox;
	private JFormattedTextField findMsOneMzRtFromTextField;
	private JFormattedTextField findMsOneMzRtToTextField;
	private JFormattedTextField findMsOneMzWindowTextField;
	private JComboBox findMsOneMzUnitsComboBox;
	private JButton findMsOneMzButton;

	private Preferences preferences;
	
	private static final String CLASS_NAME = "clusterfinder.gui.rawdata.MsExtractorPanel";
	
	//	MS averaging preferences
	private static final String MS_AVG_MASS_ERROR_VALUE = "MS_AVG_MASS_ERROR_VALUE";
	private static final Double MS_AVG_MASS_ERROR_VALUE_DEFAULT = 20.0d;
	private static final String MS_AVG_MASS_ERROR_TYPE = "MS_AVG_MASS_ERROR_TYPE";
	private static final String MS_AVG_MASS_ERROR_TYPE_DEFAULT = MassErrorType.ppm.name();
	private static final String MS_AVG_RT_MIN_VALUE = "RT_MIN_VALUE";
	private static final String MS_AVG_RT_MIN_VALUE_DEFAULT = "";
	private static final String MS_AVG_RT_MAX_VALUE = "RT_MAX_VALUE";
	private static final String MS_AVG_RT_MAX_VALUE_DEFAULT = "";
	private static final String MS_AVG_MS_LEVEL_VALUE = "MS_AVG_MS_LEVEL_VALUE";
	private static final Integer MS_AVG_MS_LEVEL_VALUE_DEFAULT = 1;
	
	//	MSMS search preferences	
	private static final String MZ_LIST = "MZ_LIST";
	private static final String MZ_LIST_DEFAULT = "";
	private static final String MSMS_SEARCH_MASS_ERROR_VALUE = "MSMS_SEARCH_MASS_ERROR_VALUE";
	private static final Double MSMS_SEARCH_MASS_ERROR_VALUE_DEFAULT = 20.0d;
	private static final String MSMS_SEARCH_MASS_ERROR_TYPE = "MSMS_SEARCH_MASS_ERROR_TYPE";
	private static final String MSMS_SEARCH_MASS_ERROR_TYPE_DEFAULT = MassErrorType.ppm.name();	
	private static final String MSMS_SEARCH_USE_RT_RANGE = "MSMS_SEARCH_USE_RT_RANGE";
	private static final Boolean MSMS_SEARCH_USE_RT_RANGE_DEFAULT = false;
	private static final String MSMS_SEARCH_RT_MIN_VALUE = "MSMS_SEARCH_RT_MIN_VALUE";
	private static final String MSMS_SEARCH_RT_MIN_VALUE_DEFAULT = "";
	private static final String MSMS_SEARCH_RT_MAX_VALUE = "MSMS_SEARCH_RT_MAX_VALUE";
	private static final String MSMS_SEARCH_RT_MAX_VALUE_DEFAULT = "";
	private JButton btnGetFromSelection;



	public DockableMsExtractorPanel(RawDataExaminerPanel parentPanel) {

		super("DockableMsExtractorPanel", chromIcon, "Extract/search MS", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		this.rawDataPanel = parentPanel;

		setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 101, 90, 66, 67, 32, 53, 0 };
		gbl_panel.rowHeights = new int[] { 146, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

//		fileListModel = new DefaultListModel<DataFile>();
//		sampleFileList = new JList<DataFile>();
//		sampleFileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		sampleFileList.setModel(fileListModel);
//		JScrollPane scrollPane =new JScrollPane(sampleFileList);
//		scrollPane.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Data files",
//				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		rawDataFileTable = new DataFileToAcquisitionMethodTable();
		JScrollPane scrollPane = new JScrollPane(rawDataFileTable);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridwidth = 6;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		panel.add(scrollPane, gbc_scrollPane_1);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.gridwidth = 6;
		gbc_tabbedPane.insets = new Insets(0, 0, 0, 5);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		panel.add(tabbedPane, gbc_tabbedPane);
		
		JPanel avgMsPanel = createAverageMsPanel();
		tabbedPane.addTab("Average spectrum", avgMsPanel);
		
		JPanel msmsSearchPanel = createSearchMsMSPanel();
		tabbedPane.addTab("Search MSMS", msmsSearchPanel);
		
//		JPanel msOneSearchPanel = createSearchMsOnePanel();
//		tabbedPane.addTab("Search MS1", msOneSearchPanel);
		
		loadPreferences();
	}
	
	private JPanel createAverageMsPanel() {
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 5, 5, 0));
//		panel_1.setBorder(new TitledBorder(
//				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
//				"Average spectrum", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JLabel lblRtRange = new JLabel("RT range from");
		GridBagConstraints gbc_lblRtRange = new GridBagConstraints();
		gbc_lblRtRange.anchor = GridBagConstraints.EAST;
		gbc_lblRtRange.insets = new Insets(0, 0, 5, 5);
		gbc_lblRtRange.gridx = 0;
		gbc_lblRtRange.gridy = 0;
		panel_1.add(lblRtRange, gbc_lblRtRange);

		avgMsRtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		avgMsRtFromTextField.setMinimumSize(new Dimension(50, 20));
		GridBagConstraints gbc_avgMsRtFromTextField = new GridBagConstraints();
		gbc_avgMsRtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_avgMsRtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_avgMsRtFromTextField.gridx = 1;
		gbc_avgMsRtFromTextField.gridy = 0;
		panel_1.add(avgMsRtFromTextField, gbc_avgMsRtFromTextField);
		avgMsRtFromTextField.setColumns(10);

		JLabel lblTo = new JLabel("to");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.gridx = 2;
		gbc_lblTo.gridy = 0;
		panel_1.add(lblTo, gbc_lblTo);

		avgMsRtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		avgMsRtToTextField.setMinimumSize(new Dimension(50, 20));
		GridBagConstraints gbc_avgMsRtToTextField = new GridBagConstraints();
		gbc_avgMsRtToTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_avgMsRtToTextField.insets = new Insets(0, 0, 5, 5);
		gbc_avgMsRtToTextField.gridx = 3;
		gbc_avgMsRtToTextField.gridy = 0;
		panel_1.add(avgMsRtToTextField, gbc_avgMsRtToTextField);
		avgMsRtToTextField.setColumns(10);

		JLabel lblMin = new JLabel("min");
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.insets = new Insets(0, 0, 5, 5);
		gbc_lblMin.anchor = GridBagConstraints.WEST;
		gbc_lblMin.gridx = 4;
		gbc_lblMin.gridy = 0;
		panel_1.add(lblMin, gbc_lblMin);
		
		btnGetFromSelection = new JButton("Get from selection");
		btnGetFromSelection.setActionCommand(MainActionCommands.GET_RT_RANGE_FROM_CHROMATOGRAM_SELECTION.getName());	
		btnGetFromSelection.addActionListener(this);
		btnGetFromSelection.setToolTipText("Get from selection on chromatogram");
		GridBagConstraints gbc_btnGetFromSelection = new GridBagConstraints();
		gbc_btnGetFromSelection.insets = new Insets(0, 0, 5, 0);
		gbc_btnGetFromSelection.gridx = 5;
		gbc_btnGetFromSelection.gridy = 0;
		panel_1.add(btnGetFromSelection, gbc_btnGetFromSelection);

		JLabel lblMzBinningWindow = new JLabel("M/Z binning window");
		GridBagConstraints gbc_lblMzBinningWindow = new GridBagConstraints();
		gbc_lblMzBinningWindow.anchor = GridBagConstraints.EAST;
		gbc_lblMzBinningWindow.insets = new Insets(0, 0, 5, 5);
		gbc_lblMzBinningWindow.gridx = 0;
		gbc_lblMzBinningWindow.gridy = 1;
		panel_1.add(lblMzBinningWindow, gbc_lblMzBinningWindow);

		mzBinningValueTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		mzBinningValueTextField.setMinimumSize(new Dimension(50, 20));
		mzBinningValueTextField.setText(MRC2ToolBoxConfiguration.getPpmFormat().format(MRC2ToolBoxConfiguration.getMassAccuracy()));
		GridBagConstraints gbc_mzBinningValueTextField = new GridBagConstraints();
		gbc_mzBinningValueTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzBinningValueTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzBinningValueTextField.gridx = 1;
		gbc_mzBinningValueTextField.gridy = 1;
		panel_1.add(mzBinningValueTextField, gbc_mzBinningValueTextField);

		mzBinningUnitsComboBox = new JComboBox(new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		mzBinningUnitsComboBox.setMinimumSize(new Dimension(50, 20));
		GridBagConstraints gbc_mzBinningUnitsComboBox = new GridBagConstraints();
		gbc_mzBinningUnitsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_mzBinningUnitsComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzBinningUnitsComboBox.gridx = 3;
		gbc_mzBinningUnitsComboBox.gridy = 1;
		panel_1.add(mzBinningUnitsComboBox, gbc_mzBinningUnitsComboBox);
	
		JLabel lblNewLabel = new JLabel("Level");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 4;
		gbc_lblNewLabel.gridy = 1;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		msLevelComboBox = new JSpinner();
		msLevelComboBox.setMinimumSize(new Dimension(40, 20));
		msLevelComboBox.setModel(new SpinnerNumberModel(1, 1, 5, 1));
		GridBagConstraints gbc_msLevelComboBox = new GridBagConstraints();
		gbc_msLevelComboBox.anchor = GridBagConstraints.WEST;
		gbc_msLevelComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_msLevelComboBox.gridx = 5;
		gbc_msLevelComboBox.gridy = 1;
		panel_1.add(msLevelComboBox, gbc_msLevelComboBox);
		
		extractAverageSpectrumButton = new JButton(MainActionCommands.EXTRACT_AVERAGE_MS.getName());
		extractAverageSpectrumButton.setActionCommand(MainActionCommands.EXTRACT_AVERAGE_MS.getName());
		extractAverageSpectrumButton.addActionListener(this);
		GridBagConstraints gbc_extractAverageSpectrumButton = new GridBagConstraints();
		gbc_extractAverageSpectrumButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_extractAverageSpectrumButton.gridwidth = 3;
		gbc_extractAverageSpectrumButton.gridx = 3;
		gbc_extractAverageSpectrumButton.gridy = 2;
		panel_1.add(extractAverageSpectrumButton, gbc_extractAverageSpectrumButton);
		
		return panel_1;
	}
	
	private JPanel createSearchMsMSPanel() {
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 5, 5, 0));
//		panel_2.setBorder(new TitledBorder(null, "Find MSMS", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0, 64, 41, 0, 66, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JLabel label = new JLabel("M/Z value(s)");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 0;
		panel_2.add(label, gbc_label);

		findMzInMsMsTextField = new JTextField();
		GridBagConstraints gbc_findMzInMsMsTextField = new GridBagConstraints();
		gbc_findMzInMsMsTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzInMsMsTextField.gridwidth = 5;
		gbc_findMzInMsMsTextField.insets = new Insets(0, 0, 5, 0);
		gbc_findMzInMsMsTextField.gridx = 2;
		gbc_findMzInMsMsTextField.gridy = 0;
		panel_2.add(findMzInMsMsTextField, gbc_findMzInMsMsTextField);
		findMzInMsMsTextField.setColumns(10);

		findMzUseRtLimitCheckBox = new JCheckBox("Limit RT range from");
		findMzUseRtLimitCheckBox.setSelected(false);
		GridBagConstraints gbc_findMzUseRtLimitCheckBox = new GridBagConstraints();
		gbc_findMzUseRtLimitCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_findMzUseRtLimitCheckBox.gridx = 1;
		gbc_findMzUseRtLimitCheckBox.gridy = 1;
		panel_2.add(findMzUseRtLimitCheckBox, gbc_findMzUseRtLimitCheckBox);

		findMzRtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		findMzRtFromTextField.setText("");
		findMzRtFromTextField.setColumns(10);
		GridBagConstraints gbc_findMzRtFromTextField = new GridBagConstraints();
		gbc_findMzRtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_findMzRtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzRtFromTextField.gridx = 2;
		gbc_findMzRtFromTextField.gridy = 1;
		panel_2.add(findMzRtFromTextField, gbc_findMzRtFromTextField);

		JLabel label_1 = new JLabel("to");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 3;
		gbc_label_1.gridy = 1;
		panel_2.add(label_1, gbc_label_1);

		findMzRtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		findMzRtToTextField.setText("");
		findMzRtToTextField.setColumns(10);
		GridBagConstraints gbc_findMzRtToTextField = new GridBagConstraints();
		gbc_findMzRtToTextField.gridwidth = 2;
		gbc_findMzRtToTextField.insets = new Insets(0, 0, 5, 5);
		gbc_findMzRtToTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzRtToTextField.gridx = 4;
		gbc_findMzRtToTextField.gridy = 1;
		panel_2.add(findMzRtToTextField, gbc_findMzRtToTextField);

		JLabel label_2 = new JLabel("min");
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.anchor = GridBagConstraints.WEST;
		gbc_label_2.insets = new Insets(0, 0, 5, 0);
		gbc_label_2.gridx = 6;
		gbc_label_2.gridy = 1;
		panel_2.add(label_2, gbc_label_2);

		JLabel label_3 = new JLabel("M/Z window");
		GridBagConstraints gbc_label_3 = new GridBagConstraints();
		gbc_label_3.anchor = GridBagConstraints.EAST;
		gbc_label_3.insets = new Insets(0, 0, 5, 5);
		gbc_label_3.gridx = 1;
		gbc_label_3.gridy = 2;
		panel_2.add(label_3, gbc_label_3);

		findMzWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		findMzWindowTextField.setText(MRC2ToolBoxConfiguration.getPpmFormat().format(MRC2ToolBoxConfiguration.getMassAccuracy()));
		GridBagConstraints gbc_findMzWindowTextField = new GridBagConstraints();
		gbc_findMzWindowTextField.insets = new Insets(0, 0, 5, 5);
		gbc_findMzWindowTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzWindowTextField.gridx = 2;
		gbc_findMzWindowTextField.gridy = 2;
		panel_2.add(findMzWindowTextField, gbc_findMzWindowTextField);

		findMzUnitsComboBox = new JComboBox(new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		findMzUnitsComboBox.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_findMzUnitsComboBox = new GridBagConstraints();
		gbc_findMzUnitsComboBox.gridwidth = 3;
		gbc_findMzUnitsComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzUnitsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_findMzUnitsComboBox.gridx = 3;
		gbc_findMzUnitsComboBox.gridy = 2;
		panel_2.add(findMzUnitsComboBox, gbc_findMzUnitsComboBox);

		findByParentIonButton = new JButton(MainActionCommands.FIND_MSMS_BY_PARENT_ION.getName());
		findByParentIonButton.setActionCommand(MainActionCommands.FIND_MSMS_BY_PARENT_ION.getName());
		findByParentIonButton.addActionListener(this);
		GridBagConstraints gbc_findByParentIonButton = new GridBagConstraints();
		gbc_findByParentIonButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_findByParentIonButton.gridwidth = 2;
		gbc_findByParentIonButton.insets = new Insets(0, 0, 0, 5);
		gbc_findByParentIonButton.gridx = 1;
		gbc_findByParentIonButton.gridy = 3;
		panel_2.add(findByParentIonButton, gbc_findByParentIonButton);

		findByFragmentButton = new JButton(MainActionCommands.FIND_MSMS_BY_FRAGMENTS.getName());
		findByFragmentButton.setActionCommand(MainActionCommands.FIND_MSMS_BY_FRAGMENTS.getName());
		findByFragmentButton.addActionListener(this);
		GridBagConstraints gbc_findByFragmentButton = new GridBagConstraints();
		gbc_findByFragmentButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_findByFragmentButton.gridwidth = 2;
		gbc_findByFragmentButton.gridx = 5;
		gbc_findByFragmentButton.gridy = 3;
		panel_2.add(findByFragmentButton, gbc_findByFragmentButton);
		
		return panel_2;
	}
	
	private JPanel createSearchMsOnePanel() {
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 5, 5, 0));
//		panel_2.setBorder(new TitledBorder(null, "Find MSMS", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0, 64, 41, 0, 66, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JLabel label = new JLabel("M/Z value(s)");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 0;
		panel_2.add(label, gbc_label);

		findMzInMsTextField = new JTextField();
		GridBagConstraints gbc_findMzInMsMsTextField = new GridBagConstraints();
		gbc_findMzInMsMsTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzInMsMsTextField.gridwidth = 5;
		gbc_findMzInMsMsTextField.insets = new Insets(0, 0, 5, 0);
		gbc_findMzInMsMsTextField.gridx = 2;
		gbc_findMzInMsMsTextField.gridy = 0;
		panel_2.add(findMzInMsTextField, gbc_findMzInMsMsTextField);
		findMzInMsTextField.setColumns(10);

		findMsOneMzUseRtLimitCheckBox = new JCheckBox("Limit RT range from");
		findMsOneMzUseRtLimitCheckBox.setSelected(false);
		GridBagConstraints gbc_findMzUseRtLimitCheckBox = new GridBagConstraints();
		gbc_findMzUseRtLimitCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_findMzUseRtLimitCheckBox.gridx = 1;
		gbc_findMzUseRtLimitCheckBox.gridy = 1;
		panel_2.add(findMsOneMzUseRtLimitCheckBox, gbc_findMzUseRtLimitCheckBox);

		findMsOneMzRtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		findMsOneMzRtFromTextField.setText("");
		findMsOneMzRtFromTextField.setColumns(10);
		GridBagConstraints gbc_findMzRtFromTextField = new GridBagConstraints();
		gbc_findMzRtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_findMzRtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzRtFromTextField.gridx = 2;
		gbc_findMzRtFromTextField.gridy = 1;
		panel_2.add(findMsOneMzRtFromTextField, gbc_findMzRtFromTextField);

		JLabel label_1 = new JLabel("to");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 3;
		gbc_label_1.gridy = 1;
		panel_2.add(label_1, gbc_label_1);

		findMsOneMzRtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		findMsOneMzRtToTextField.setText("");
		findMsOneMzRtToTextField.setColumns(10);
		GridBagConstraints gbc_findMzRtToTextField = new GridBagConstraints();
		gbc_findMzRtToTextField.gridwidth = 2;
		gbc_findMzRtToTextField.insets = new Insets(0, 0, 5, 5);
		gbc_findMzRtToTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzRtToTextField.gridx = 4;
		gbc_findMzRtToTextField.gridy = 1;
		panel_2.add(findMsOneMzRtToTextField, gbc_findMzRtToTextField);

		JLabel label_2 = new JLabel("min");
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.anchor = GridBagConstraints.WEST;
		gbc_label_2.insets = new Insets(0, 0, 5, 0);
		gbc_label_2.gridx = 6;
		gbc_label_2.gridy = 1;
		panel_2.add(label_2, gbc_label_2);

		JLabel label_3 = new JLabel("M/Z window");
		GridBagConstraints gbc_label_3 = new GridBagConstraints();
		gbc_label_3.anchor = GridBagConstraints.EAST;
		gbc_label_3.insets = new Insets(0, 0, 5, 5);
		gbc_label_3.gridx = 1;
		gbc_label_3.gridy = 2;
		panel_2.add(label_3, gbc_label_3);

		findMsOneMzWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		findMsOneMzWindowTextField.setText(MRC2ToolBoxConfiguration.getPpmFormat().format(MRC2ToolBoxConfiguration.getMassAccuracy()));
		GridBagConstraints gbc_findMzWindowTextField = new GridBagConstraints();
		gbc_findMzWindowTextField.insets = new Insets(0, 0, 5, 5);
		gbc_findMzWindowTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzWindowTextField.gridx = 2;
		gbc_findMzWindowTextField.gridy = 2;
		panel_2.add(findMsOneMzWindowTextField, gbc_findMzWindowTextField);

		findMsOneMzUnitsComboBox = new JComboBox(new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		findMsOneMzUnitsComboBox.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_findMzUnitsComboBox = new GridBagConstraints();
		gbc_findMzUnitsComboBox.gridwidth = 3;
		gbc_findMzUnitsComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_findMzUnitsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_findMzUnitsComboBox.gridx = 3;
		gbc_findMzUnitsComboBox.gridy = 2;
		panel_2.add(findMsOneMzUnitsComboBox, gbc_findMzUnitsComboBox);

		findMsOneMzButton = new JButton(MainActionCommands.FIND_MZ_IN_MS_ONE.getName());
		findMsOneMzButton.setActionCommand(MainActionCommands.FIND_MZ_IN_MS_ONE.getName());
		findMsOneMzButton.addActionListener(this);
		GridBagConstraints gbc_findByParentIonButton = new GridBagConstraints();
		gbc_findByParentIonButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_findByParentIonButton.gridwidth = 2;
		gbc_findByParentIonButton.insets = new Insets(0, 0, 0, 5);
		gbc_findByParentIonButton.gridx = 1;
		gbc_findByParentIonButton.gridy = 3;
		panel_2.add(findMsOneMzButton, gbc_findByParentIonButton);
		
		return panel_2;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(MainActionCommands.EXTRACT_AVERAGE_MS.getName()))
			extractAverageMs();

		if(e.getActionCommand().equals(MainActionCommands.FIND_MSMS_BY_PARENT_ION.getName()))
			findMsMsByParentIon();	
		
		if(e.getActionCommand().equals(MainActionCommands.FIND_MSMS_BY_FRAGMENTS.getName()))
			findMsMsByFragments();
		
		if(e.getActionCommand().equals(MainActionCommands.GET_RT_RANGE_FROM_CHROMATOGRAM_SELECTION.getName()))
			getRtRangeFromChromatogramSelection();
	}

	private void getRtRangeFromChromatogramSelection() {

		Range selectedRange = rawDataPanel.getSelectedRTRange();
		if(selectedRange.getMin() == selectedRange.getMax()) {
			MessageDialog.showWarningMsg("No selection on chromatogram panel available!", this.getContentPane());
			return;
		}
		avgMsRtFromTextField.setText(MRC2ToolBoxConfiguration.getRtFormat().format(selectedRange.getMin()));
		avgMsRtToTextField.setText(MRC2ToolBoxConfiguration.getRtFormat().format(selectedRange.getMax()));
	}
	
	public void selectFiles(Collection<DataFile> dataFiles) {
		rawDataFileTable.selectFiles(dataFiles);
//		sampleFileList.clearSelection();
//		ArrayList<Integer>indices = new ArrayList<Integer>();
//		for(DataFile f : dataFiles) {
//			Integer idx = fileListModel.indexOf(f);
//			if(idx > -1)
//				indices.add(idx);
//		}
//		int[] toSelect = indices.stream().mapToInt(i->i).toArray();			
//		sampleFileList.setSelectedIndices(toSelect);
	}

	private void findMsMsByParentIon() {
		// TODO Auto-generated method stub
		
	}

	private void findMsMsByFragments() {
		// TODO Auto-generated method stub
		
	}	

	private void extractAverageMs() {
		
		ArrayList<String>errors = new ArrayList<String>();
//		Collection<DataFile> files = sampleFileList.getSelectedValuesList();
		Collection<DataFile> files = rawDataFileTable.getSelectedFiles();
		if (files.isEmpty())			
			errors.add("No data files selected for processing");

		Double rtMin = null;
		if(!avgMsRtFromTextField.getText().isEmpty())
			rtMin = Double.parseDouble(avgMsRtFromTextField.getText());
		
		Double rtMax = null;
		if(!avgMsRtToTextField.getText().isEmpty())
			rtMax = Double.parseDouble(avgMsRtToTextField.getText());
		
		if(rtMin == null)
			errors.add("Lower RT border not specified.");
		
		if(rtMax == null)
			errors.add("Upper RT border not specified.");
		
		if(rtMin != null && rtMax != null) {
			if(rtMin >= rtMax)
				errors.add("Invalid RT range.");
		}
		
		Double mzBinWidth = null;
		if(!mzBinningValueTextField.getText().isEmpty())
			mzBinWidth = Double.parseDouble(mzBinningValueTextField.getText());
		
		if(mzBinWidth == null || mzBinWidth == 0.0d)
			errors.add("M/Z binning window should be > 0.");
		
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this.getContentPane());
			return;
		}
		int msLevel = (int)msLevelComboBox.getValue();
		//	Remember the settings
		savePreferences();
		Range rtRange = new Range(rtMin, rtMax);
		MassErrorType errorType = (MassErrorType)mzBinningUnitsComboBox.getSelectedItem();
		
		//	Extract MS
		MassSpectraAveragingTask msavTask = 
				new MassSpectraAveragingTask(files, rtRange, mzBinWidth, errorType, msLevel);
		msavTask.addTaskListener(rawDataPanel);
		MRC2ToolBoxCore.getTaskController().addTask(msavTask);
	}

	public synchronized void clearPanel() {

//		fileListModel.clear();
//		sampleFileList.revalidate();
//		sampleFileList.repaint();
		
		rawDataFileTable.clearTable();
		avgMsRtFromTextField.setText("");
		avgMsRtToTextField.setText("");
		
		findMzInMsMsTextField.setText("");
		findMzRtFromTextField.setText("");
		findMzRtToTextField.setText("");
		findMzWindowTextField.setText("");
		mzBinningValueTextField.setText("");
	}
	
	public void loadData(Collection<DataFile> dataFiles, boolean append) {
		
		rawDataFileTable.setModelFromDataFiles(dataFiles, append);
		
//		if(!append)
//			fileListModel.clear();
//
//		dataFiles.stream().forEach(f -> fileListModel.addElement(f));
//		sampleFileList.revalidate();
//		sampleFileList.repaint();		
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(CLASS_NAME));
	}

	@Override
	public void loadPreferences(Preferences preferences) {

		this.preferences = preferences;
			
		//	MS averaging
		String mzAvgError = Double.toString(preferences.getDouble(MS_AVG_MASS_ERROR_VALUE, MS_AVG_MASS_ERROR_VALUE_DEFAULT));
		mzBinningValueTextField.setText(mzAvgError);
		mzBinningUnitsComboBox.setSelectedItem(
				MassErrorType.getTypeByName(preferences.get(MS_AVG_MASS_ERROR_TYPE, MS_AVG_MASS_ERROR_TYPE_DEFAULT)));
		
		avgMsRtFromTextField.setText(preferences.get(MS_AVG_RT_MIN_VALUE, MS_AVG_RT_MIN_VALUE_DEFAULT));
		avgMsRtToTextField.setText(preferences.get(MS_AVG_RT_MAX_VALUE, MS_AVG_RT_MAX_VALUE_DEFAULT));
		msLevelComboBox.setValue(preferences.getInt(MS_AVG_MS_LEVEL_VALUE, MS_AVG_MS_LEVEL_VALUE_DEFAULT));
		
		//	MSMS search
		findMzInMsMsTextField.setText(preferences.get(MZ_LIST, MZ_LIST_DEFAULT));
		findMzUseRtLimitCheckBox.setSelected(preferences.getBoolean(MSMS_SEARCH_USE_RT_RANGE, MSMS_SEARCH_USE_RT_RANGE_DEFAULT));
		findMzRtFromTextField.setText(preferences.get(MSMS_SEARCH_RT_MIN_VALUE, MSMS_SEARCH_RT_MIN_VALUE_DEFAULT));
		findMzRtToTextField.setText(preferences.get(MSMS_SEARCH_RT_MAX_VALUE, MSMS_SEARCH_RT_MAX_VALUE_DEFAULT));
		String mzSearchError = Double.toString(preferences.getDouble(MSMS_SEARCH_MASS_ERROR_VALUE, MSMS_SEARCH_MASS_ERROR_VALUE_DEFAULT));
		findMzWindowTextField.setText(mzSearchError);
		findMzUnitsComboBox.setSelectedItem(
				MassErrorType.getTypeByName(preferences.get(MSMS_SEARCH_MASS_ERROR_TYPE, MSMS_SEARCH_MASS_ERROR_TYPE_DEFAULT)));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userRoot().node(CLASS_NAME);

		//	MS averaging
		double mzAvgError = MS_AVG_MASS_ERROR_VALUE_DEFAULT;
		try {
			mzAvgError = Double.parseDouble(mzBinningValueTextField.getText().trim());
		} catch (NumberFormatException e) {
		}
		preferences.putDouble(MS_AVG_MASS_ERROR_VALUE, mzAvgError);
		preferences.put(MS_AVG_MASS_ERROR_TYPE, ((MassErrorType) mzBinningUnitsComboBox.getSelectedItem()).name());
		preferences.put(MS_AVG_RT_MIN_VALUE, avgMsRtFromTextField.getText().trim());
		preferences.put(MS_AVG_RT_MAX_VALUE, avgMsRtToTextField.getText().trim());
		preferences.putInt(MS_AVG_MS_LEVEL_VALUE, (int)msLevelComboBox.getValue());
		
		//	MSMS search
		preferences.put(MZ_LIST, findMzInMsMsTextField.getText().trim());
		preferences.putBoolean(MSMS_SEARCH_USE_RT_RANGE, findMzUseRtLimitCheckBox.isSelected());
		preferences.put(MSMS_SEARCH_RT_MIN_VALUE, findMzRtFromTextField.getText().trim());
		preferences.put(MSMS_SEARCH_RT_MAX_VALUE, findMzRtToTextField.getText().trim());	
		double msmsSearchMzError = MSMS_SEARCH_MASS_ERROR_VALUE_DEFAULT;
		try {
			msmsSearchMzError = Double.parseDouble(findMzWindowTextField.getText().trim());
		} catch (NumberFormatException e) {
		}
		preferences.putDouble(MSMS_SEARCH_MASS_ERROR_VALUE, msmsSearchMzError);
		preferences.put(MSMS_SEARCH_MASS_ERROR_TYPE, ((MassErrorType) findMzUnitsComboBox.getSelectedItem()).name());
	}
	
	public void removeDataFiles(Collection<DataFile> filesToRemove) {
		//files.stream().forEach(f -> fileListModel.removeElement(f));
		rawDataFileTable.removeDataFiles(filesToRemove);
	}
}
