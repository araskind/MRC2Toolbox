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

package edu.umich.med.mrc2.datoolbox.gui.idworks.xic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataFileTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ChromatogramExtractionTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterClass;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.FilterGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.LoessGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.MovingAverageGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.SavitzkyGolayGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.SavitzkyGolayMZMineGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.SmoothingCubicSplineGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.WeightedMovingAverageGuiPanel;

public class XICSetupPanel extends JPanel implements ActionListener, ItemListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1598480456937212595L;
	private static final Icon chromIcon = GuiUtils.getIcon("ionChroms", 16);
	
	private RawDataFileTable rawDataFileTable;
	private JComboBox chromTypeComboBox;
	private JComboBox polarityComboBox;
	private JComboBox msLevelcomboBox;
	private JComboBox smoothingComboBox;
	private JTextField mzTextField;
	private JFormattedTextField mzErrorTextField;
	private JFormattedTextField rtFromTextField;
	private JFormattedTextField rtToTextField;
	private JButton extractButton;
	private JCheckBox chckbxLimitRtRange;
	private JComboBox mzWindowTypeComboBox;
	private JCheckBox sumAllMassesCheckBox;
	private JComboBox filterTypeComboBox;
	private JPanel filterParameters;
	private FilterGuiPanel filterGuiPanel;

	private Preferences preferences;
	private static final String CLASS_NAME = "clusterfinder.gui.rawdata.XICSetupPanel";
	private static final String CHROMATOGRAM_TYPE = "CHROMATOGRAM_TYPE";
	private static final String CHROMATOGRAM_TYPE_DEFAULT = ChromatogramPlotMode.TIC.name();
	private static final String MS_LEVEL = "MS_LEVEL";
	private static final Integer MS_LEVEL_DEFAULT = 1;
	private static final String MZ_LIST = "MZ_LIST";
	private static final String MZ_LIST_DEFAULT = "";
	private static final String MZ_LIST_SUM_ALL = "MZ_LIST_SUM_ALL";
	private static final Boolean MZ_LIST_SUM_ALL_DEFAULT = false;
	private static final String MASS_ERROR_VALUE = "MASS_ERROR_VALUE";
	private static final Double MASS_ERROR_VALUE_DEFAULT = 20.0d;
	private static final String MASS_ERROR_TYPE = "MASS_ERROR_TYPE";
	private static final String MASS_ERROR_TYPE_DEFAULT = MassErrorType.ppm.name();
	private static final String USE_RT_RANGE = "USE_RT_RANGE";
	private static final Boolean USE_RT_RANGE_DEFAULT = false;
	private static final String RT_MIN_VALUE = "RT_MIN_VALUE";
	private static final String RT_MIN_VALUE_DEFAULT = "";
	private static final String RT_MAX_VALUE = "RT_MAX_VALUE";
	private static final String RT_MAX_VALUE_DEFAULT = "";
	private static final String FILTER_CLASS = "FILTER_CLASS";
	private static final String CHROMATOGRAM_EXTRACTION_TYPE = "CHROMATOGRAM_EXTRACTION_TYPE";

	public XICSetupPanel(ActionListener listener) {

		super();

		setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 101, 90, 66, 67, 32, 53, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		rawDataFileTable = new RawDataFileTable();
		JScrollPane scrollPane = new JScrollPane(rawDataFileTable);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridwidth = 6;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		panel.add(scrollPane, gbc_scrollPane_1);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.gridwidth = 6;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		panel.add(tabbedPane, gbc_tabbedPane);

		JPanel chromatogaramSettingsPanel = new JPanel();
		chromatogaramSettingsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		tabbedPane.addTab("Chromatogram extraction settings", null, chromatogaramSettingsPanel, null);
		GridBagLayout gbl_chromatogaramSettingsPanel = new GridBagLayout();
		gbl_chromatogaramSettingsPanel.columnWidths = new int[] { 127, 101, 101, 54, 101, 101, 0 };
		gbl_chromatogaramSettingsPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_chromatogaramSettingsPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_chromatogaramSettingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		chromatogaramSettingsPanel.setLayout(gbl_chromatogaramSettingsPanel);

		JLabel lblChromatogramType = new JLabel("Chromatogram type");
		GridBagConstraints gbc_lblChromatogramType = new GridBagConstraints();
		gbc_lblChromatogramType.anchor = GridBagConstraints.EAST;
		gbc_lblChromatogramType.insets = new Insets(0, 0, 5, 5);
		gbc_lblChromatogramType.gridx = 0;
		gbc_lblChromatogramType.gridy = 0;
		chromatogaramSettingsPanel.add(lblChromatogramType, gbc_lblChromatogramType);

		chromTypeComboBox = new JComboBox<ChromatogramPlotMode>(
				new DefaultComboBoxModel<ChromatogramPlotMode>(ChromatogramPlotMode.values()));
		GridBagConstraints gbc_chromTypeComboBox = new GridBagConstraints();
		gbc_chromTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_chromTypeComboBox.gridwidth = 4;
		gbc_chromTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_chromTypeComboBox.gridx = 1;
		gbc_chromTypeComboBox.gridy = 0;
		chromatogaramSettingsPanel.add(chromTypeComboBox, gbc_chromTypeComboBox);
		chromTypeComboBox.setSelectedItem(ChromatogramPlotMode.TIC);

		JLabel lblPolarity = new JLabel("Polarity");
		GridBagConstraints gbc_lblPolarity = new GridBagConstraints();
		gbc_lblPolarity.anchor = GridBagConstraints.EAST;
		gbc_lblPolarity.insets = new Insets(0, 0, 5, 5);
		gbc_lblPolarity.gridx = 0;
		gbc_lblPolarity.gridy = 1;
		chromatogaramSettingsPanel.add(lblPolarity, gbc_lblPolarity);

		polarityComboBox = new JComboBox<Polarity>(
				new DefaultComboBoxModel<Polarity>(
						new Polarity[] { Polarity.Positive, Polarity.Negative }));
		polarityComboBox.addItemListener(this);
		GridBagConstraints gbc_polarityComboBox = new GridBagConstraints();
		gbc_polarityComboBox.gridwidth = 2;
		gbc_polarityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_polarityComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_polarityComboBox.gridx = 1;
		gbc_polarityComboBox.gridy = 1;
		chromatogaramSettingsPanel.add(polarityComboBox, gbc_polarityComboBox);

		JLabel lblMsLevel = new JLabel("MS level");
		GridBagConstraints gbc_lblMsLevel = new GridBagConstraints();
		gbc_lblMsLevel.anchor = GridBagConstraints.EAST;
		gbc_lblMsLevel.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsLevel.gridx = 3;
		gbc_lblMsLevel.gridy = 1;
		chromatogaramSettingsPanel.add(lblMsLevel, gbc_lblMsLevel);

		msLevelcomboBox = new JComboBox<Integer>(
				new DefaultComboBoxModel<Integer>(new Integer[] { 1, 2, 3, 4, 5 }));
		GridBagConstraints gbc_msLevelcomboBox = new GridBagConstraints();
		gbc_msLevelcomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_msLevelcomboBox.insets = new Insets(0, 0, 5, 5);
		gbc_msLevelcomboBox.gridx = 4;
		gbc_msLevelcomboBox.gridy = 1;
		chromatogaramSettingsPanel.add(msLevelcomboBox, gbc_msLevelcomboBox);

		JLabel lblMzValues = new JLabel("M/Z value(s)");
		GridBagConstraints gbc_lblMzValues = new GridBagConstraints();
		gbc_lblMzValues.anchor = GridBagConstraints.EAST;
		gbc_lblMzValues.insets = new Insets(0, 0, 5, 5);
		gbc_lblMzValues.gridx = 0;
		gbc_lblMzValues.gridy = 2;
		chromatogaramSettingsPanel.add(lblMzValues, gbc_lblMzValues);

		mzTextField = new JTextField();
		GridBagConstraints gbc_mzTextField = new GridBagConstraints();
		gbc_mzTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzTextField.gridwidth = 4;
		gbc_mzTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzTextField.gridx = 1;
		gbc_mzTextField.gridy = 2;
		chromatogaramSettingsPanel.add(mzTextField, gbc_mzTextField);
		mzTextField.setColumns(10);
		
		sumAllMassesCheckBox = new JCheckBox("Sum all");
		GridBagConstraints gbc_sumAllMassesCheckBox = new GridBagConstraints();
		gbc_sumAllMassesCheckBox.anchor = GridBagConstraints.WEST;
		gbc_sumAllMassesCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_sumAllMassesCheckBox.gridx = 5;
		gbc_sumAllMassesCheckBox.gridy = 2;
		chromatogaramSettingsPanel.add(sumAllMassesCheckBox, gbc_sumAllMassesCheckBox);

		JLabel lblMzWindow = new JLabel("M/Z window");
		GridBagConstraints gbc_lblMzWindow = new GridBagConstraints();
		gbc_lblMzWindow.anchor = GridBagConstraints.EAST;
		gbc_lblMzWindow.insets = new Insets(0, 0, 5, 5);
		gbc_lblMzWindow.gridx = 0;
		gbc_lblMzWindow.gridy = 3;
		chromatogaramSettingsPanel.add(lblMzWindow, gbc_lblMzWindow);
		
		mzErrorTextField = new JFormattedTextField(new DecimalFormat("#.##"));
		GridBagConstraints gbc_mzErrorTextField = new GridBagConstraints();
		gbc_mzErrorTextField.gridwidth = 2;
		gbc_mzErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzErrorTextField.gridx = 1;
		gbc_mzErrorTextField.gridy = 3;
		chromatogaramSettingsPanel.add(mzErrorTextField, gbc_mzErrorTextField);

		mzWindowTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		GridBagConstraints gbc_mzWindowTypeComboBox = new GridBagConstraints();
		gbc_mzWindowTypeComboBox.gridwidth = 3;
		gbc_mzWindowTypeComboBox.anchor = GridBagConstraints.WEST;
		gbc_mzWindowTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_mzWindowTypeComboBox.gridx = 3;
		gbc_mzWindowTypeComboBox.gridy = 3;
		chromatogaramSettingsPanel.add(mzWindowTypeComboBox, gbc_mzWindowTypeComboBox);

		chckbxLimitRtRange = new JCheckBox("Limit RT range");
		GridBagConstraints gbc_chckbxLimitRtRange = new GridBagConstraints();
		gbc_chckbxLimitRtRange.anchor = GridBagConstraints.EAST;
		gbc_chckbxLimitRtRange.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxLimitRtRange.gridx = 0;
		gbc_chckbxLimitRtRange.gridy = 4;
		chromatogaramSettingsPanel.add(chckbxLimitRtRange, gbc_chckbxLimitRtRange);

		JLabel lblFrom = new JLabel("from");
		GridBagConstraints gbc_lblFrom = new GridBagConstraints();
		gbc_lblFrom.anchor = GridBagConstraints.EAST;
		gbc_lblFrom.insets = new Insets(0, 0, 5, 5);
		gbc_lblFrom.gridx = 1;
		gbc_lblFrom.gridy = 4;
		chromatogaramSettingsPanel.add(lblFrom, gbc_lblFrom);

		rtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		GridBagConstraints gbc_rtFromTextField = new GridBagConstraints();
		gbc_rtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtFromTextField.gridx = 2;
		gbc_rtFromTextField.gridy = 4;
		chromatogaramSettingsPanel.add(rtFromTextField, gbc_rtFromTextField);
		rtFromTextField.setColumns(10);

		JLabel lblTo = new JLabel("to");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.gridx = 3;
		gbc_lblTo.gridy = 4;
		chromatogaramSettingsPanel.add(lblTo, gbc_lblTo);

		rtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		GridBagConstraints gbc_rtToTextField = new GridBagConstraints();
		gbc_rtToTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtToTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtToTextField.gridx = 4;
		gbc_rtToTextField.gridy = 4;
		chromatogaramSettingsPanel.add(rtToTextField, gbc_rtToTextField);
		rtToTextField.setColumns(10);

		JLabel lblNewLabel = new JLabel("min");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridx = 5;
		gbc_lblNewLabel.gridy = 4;
		chromatogaramSettingsPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		JLabel lblNewLabel_2 = new JLabel("Smoothing");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 5;
		chromatogaramSettingsPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		smoothingComboBox = new JComboBox<ChromatogramExtractionType>(
				new DefaultComboBoxModel<ChromatogramExtractionType>(ChromatogramExtractionType.values()));
		GridBagConstraints gbc_smoothingComboBox = new GridBagConstraints();
		gbc_smoothingComboBox.gridwidth = 4;
		gbc_smoothingComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_smoothingComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_smoothingComboBox.gridx = 1;
		gbc_smoothingComboBox.gridy = 5;
		chromatogaramSettingsPanel.add(smoothingComboBox, gbc_smoothingComboBox);

		JPanel smoothingSettingsPanel = new JPanel();
		smoothingSettingsPanel.setBorder(new EmptyBorder(10, 0, 10, 10));
		tabbedPane.addTab("Smoothing settings", null, smoothingSettingsPanel, null);
		GridBagLayout gbl_smoothingSettingsPanel = new GridBagLayout();
		gbl_smoothingSettingsPanel.columnWidths = new int[]{0, 0, 0};
		gbl_smoothingSettingsPanel.rowHeights = new int[]{0, 0, 0};
		gbl_smoothingSettingsPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_smoothingSettingsPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		smoothingSettingsPanel.setLayout(gbl_smoothingSettingsPanel);
		
		JLabel lblNewLabel_1 = new JLabel("Filter type ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		smoothingSettingsPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		filterTypeComboBox = new JComboBox<FilterClass>(
				new DefaultComboBoxModel<FilterClass>(new FilterClass[] {
						FilterClass.SAVITZKY_GOLAY_MZMINE, 
						FilterClass.MOVING_AVERAGE, 
						FilterClass.WEIGHTED_MOVING_AVERAGE 
					}));				
		filterTypeComboBox.setSelectedIndex(-1);	//	TODO read from preferences		
		filterTypeComboBox.addItemListener(this);
		GridBagConstraints gbc_filterTypeComboBox = new GridBagConstraints();
		gbc_filterTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_filterTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_filterTypeComboBox.gridx = 1;
		gbc_filterTypeComboBox.gridy = 0;
		smoothingSettingsPanel.add(filterTypeComboBox, gbc_filterTypeComboBox);
		
		filterParameters = new JPanel();
		filterParameters.setBorder(new CompoundBorder(
				new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
								new Color(160, 160, 160)), "Filter parameters", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
				new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_filterParameters = new GridBagConstraints();
		gbc_filterParameters.gridwidth = 2;
		gbc_filterParameters.insets = new Insets(0, 0, 0, 5);
		gbc_filterParameters.fill = GridBagConstraints.BOTH;
		gbc_filterParameters.gridx = 0;
		gbc_filterParameters.gridy = 1;
		smoothingSettingsPanel.add(filterParameters, gbc_filterParameters);
		filterParameters.setLayout(new BorderLayout(0, 0));

		mzErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		mzErrorTextField
				.setText(MRC2ToolBoxConfiguration.getPpmFormat().format(MRC2ToolBoxConfiguration.getMassAccuracy()));

		extractButton = new JButton(MainActionCommands.EXTRACT_CHROMATOGRAM.getName());
		extractButton.setActionCommand(MainActionCommands.EXTRACT_CHROMATOGRAM.getName());
		extractButton.setToolTipText(MainActionCommands.EXTRACT_CHROMATOGRAM.getName());
		extractButton.addActionListener(listener);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridwidth = 5;
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 2;
		panel.add(extractButton, gbc_btnNewButton);

		// loadPreferences();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}
	
	private Filter getSmoothingFilter() {
		
		if(filterGuiPanel == null)
			return null;
		else
			return filterGuiPanel.getFilter();		
	}

	public Collection<String> veryfyParameters() {

		Collection<String> errors = new ArrayList<String>();
		ChromatogramPlotMode mode = (ChromatogramPlotMode) chromTypeComboBox.getSelectedItem();
		Collection<Double> mzList = new ArrayList<Double>();

		if (rawDataFileTable.getSelectedFiles().isEmpty()) {
			errors.add("No data files selected for processing!");
			return errors;
		}
		if (mode.equals(ChromatogramPlotMode.XIC)) {

			if (mzTextField.getText().trim().isEmpty()) {
				errors.add("No mases specified to create extracted ion chromatogram!");
				return errors;
			}
			String[] mzStrings = StringUtils.split(mzTextField.getText().trim().replaceAll("[\\s+,:,;\\,]", ";"), ';');
			for (String mzs : mzStrings) {

				double mz = 0.0d;
				try {
					mz = Double.parseDouble(mzs);
				} catch (NumberFormatException e) {
					errors.add(
							"Invalid mass value(s)!\nOnly numbers, \".\" as decimal pont, and the following separator characters:\n"
									+ "space (   )  comma ( , ) colon ( : ) and semi-colon ( ; ) are allowed");
					return errors;
				}
				if (mz > 0.0d)
					mzList.add(mz);
			}
			if (mzList.isEmpty()) {
				errors.add("No valid mases specified to create extracted ion chromatogram!");
				return errors;
			}
			if (mzErrorTextField.getText().trim().isEmpty()) {
				errors.add("Mass window value should be specified to create extracted ion chromatogram!");
				return errors;
			}
		}
		if (chckbxLimitRtRange.isSelected()) {

			if (rtFromTextField.getText().trim().isEmpty())
				rtFromTextField.setText("0.00");

			if (rtToTextField.getText().trim().isEmpty()) {
				errors.add("Upper retention range can not be empty if \"Limit RT range\" option is selected!");
				return errors;
			}
			double fromRt = Double.parseDouble(rtFromTextField.getText().trim());
			double toRt = Double.parseDouble(rtToTextField.getText().trim());
			if (fromRt >= toRt) {
				errors.add("Invalid retention time range (start RT must be smaller that end RT!");
				return errors;
			}
		}
		return errors;
	}

	public ChromatogramExtractionTask createChromatogramExtractionTask() {

		Collection<DataFile> files = rawDataFileTable.getSelectedFiles();
		ChromatogramPlotMode mode = (ChromatogramPlotMode) chromTypeComboBox.getSelectedItem();
		Polarity polarity = (Polarity) polarityComboBox.getSelectedItem();
		int msLevel = (int) msLevelcomboBox.getSelectedItem();
		Collection<Double> mzList = new ArrayList<Double>();
		Double mzWindowValue = Double.NaN;
		MassErrorType massErrorType = null;
		boolean sumAllMassChromatograms = sumAllMassesCheckBox.isSelected();
		if (mode.equals(ChromatogramPlotMode.XIC)) {

			String[] mzStrings = StringUtils.split(mzTextField.getText().trim().replaceAll("[\\s+,:,;\\,]", ";"), ';');
			for (String mzs : mzStrings) {

				double mz = 0.0d;
				try {
					mz = Double.parseDouble(mzs);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (mz > 0.0d)
					mzList.add(mz);
			}
			mzWindowValue = Double.parseDouble(mzErrorTextField.getText().trim());
			massErrorType = (MassErrorType) mzWindowTypeComboBox.getSelectedItem();
		}
		Range rtRange = null;
		if (chckbxLimitRtRange.isSelected()) {

			if (rtFromTextField.getText().trim().isEmpty())
				rtFromTextField.setText("0.00");

			double fromRt = Double.parseDouble(rtFromTextField.getText().trim());
			double toRt = Double.parseDouble(rtToTextField.getText().trim());
			rtRange = new Range(fromRt, toRt);
		}
		Filter smoothingFilter = filterGuiPanel.getFilter();
		ChromatogramExtractionType chexType = 
				(ChromatogramExtractionType)smoothingComboBox.getSelectedItem();
		if(chexType.equals(ChromatogramExtractionType.RAW))
				smoothingFilter = null;
		
		savePreferences();
		ChromatogramExtractionTask xicTask = 
				new ChromatogramExtractionTask(
						files, 
						mode, 
						polarity, 
						msLevel, 
						mzList,
						sumAllMassChromatograms, 
						mzWindowValue, 
						massErrorType, 
						rtRange,
						smoothingFilter,
						chexType);
		return xicTask;
	}

	public void clearPanel() {

		rawDataFileTable.clearTable();
		rtFromTextField.setText("");
		rtToTextField.setText("");
		mzTextField.setText("");
	}

	public void loadData(Collection<DataFile> dataFiles, boolean append) {

		rawDataFileTable.setModelFromDataFiles(dataFiles, append);
		if (dataFiles == null || dataFiles.isEmpty())
			return;

		DataAcquisitionMethod method = dataFiles.iterator().next().getDataAcquisitionMethod();
		if (method != null && method.getPolarity() != null)
			polarityComboBox.setSelectedItem(method.getPolarity());
	}

	public void selectFiles(Collection<DataFile> dataFiles) {
		rawDataFileTable.selectFiles(dataFiles);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(CLASS_NAME));
	}

	@Override
	public void loadPreferences(Preferences preferences) {

		this.preferences = preferences;

		ChromatogramPlotMode  chrMode = 
				ChromatogramPlotMode.getChromatogramPlotModeByName(
						preferences.get(CHROMATOGRAM_TYPE, CHROMATOGRAM_TYPE_DEFAULT));
		chromTypeComboBox.setSelectedItem(chrMode);
		msLevelcomboBox.setSelectedItem(preferences.getInt(MS_LEVEL, MS_LEVEL_DEFAULT));
		mzTextField.setText(preferences.get(MZ_LIST, MZ_LIST_DEFAULT));
		sumAllMassesCheckBox.setSelected(preferences.getBoolean(MZ_LIST_SUM_ALL, MZ_LIST_SUM_ALL_DEFAULT));
		mzErrorTextField.setText(Double.toString(preferences.getDouble(MASS_ERROR_VALUE, MASS_ERROR_VALUE_DEFAULT)));
		mzWindowTypeComboBox.setSelectedItem(
				MassErrorType.getTypeByName(preferences.get(MASS_ERROR_TYPE, MASS_ERROR_TYPE_DEFAULT)));
		chckbxLimitRtRange.setSelected(preferences.getBoolean(USE_RT_RANGE, USE_RT_RANGE_DEFAULT));
		rtFromTextField.setText(preferences.get(RT_MIN_VALUE, RT_MIN_VALUE_DEFAULT));
		rtToTextField.setText(preferences.get(RT_MAX_VALUE, RT_MAX_VALUE_DEFAULT));
		
		FilterClass fc = FilterClass.getFilterClassByName(
				preferences.get(FILTER_CLASS, FilterClass.SAVITZKY_GOLAY.name()));
		filterTypeComboBox.setSelectedItem(fc);
		
		ChromatogramExtractionType cexType = 
				ChromatogramExtractionType.getChromatogramExtractionTypeByName(
				preferences.get(CHROMATOGRAM_EXTRACTION_TYPE, ChromatogramExtractionType.SMOOTH.name()));
		smoothingComboBox.setSelectedItem(cexType);
	}

	@Override
	public void savePreferences() {

		if (preferences == null)
			preferences = Preferences.userRoot().node(CLASS_NAME);

		preferences.put(CHROMATOGRAM_TYPE, 
				((ChromatogramPlotMode) chromTypeComboBox.getSelectedItem()).name());
		preferences.putInt(MS_LEVEL, (int) msLevelcomboBox.getSelectedItem());
		preferences.put(MZ_LIST, mzTextField.getText().trim());
		preferences.putBoolean(MZ_LIST_SUM_ALL, sumAllMassesCheckBox.isSelected());
		double mzError = MASS_ERROR_VALUE_DEFAULT;
		try {
			mzError = Double.parseDouble(mzErrorTextField.getText().trim());
		} catch (NumberFormatException e) {
		}
		preferences.putDouble(MASS_ERROR_VALUE, mzError);
		preferences.put(MASS_ERROR_TYPE, ((MassErrorType) mzWindowTypeComboBox.getSelectedItem()).name());
		preferences.putBoolean(USE_RT_RANGE, chckbxLimitRtRange.isSelected());
		preferences.put(RT_MIN_VALUE, rtFromTextField.getText().trim());
		preferences.put(RT_MAX_VALUE, rtToTextField.getText().trim());
		preferences.put(FILTER_CLASS, ((FilterClass) filterTypeComboBox.getSelectedItem()).name());
		preferences.put(CHROMATOGRAM_EXTRACTION_TYPE, 
				((ChromatogramExtractionType) smoothingComboBox.getSelectedItem()).name());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

       if (e.getStateChange() == ItemEvent.SELECTED) {
           Object item = e.getItem();
           if(item instanceof FilterClass)
        	   showFilterClassParameters((FilterClass)item);
           
           if(item instanceof Polarity) {
        	   //	TODO - adjust file selection if necessary
           }
       }
	}

	private void showFilterClassParameters(FilterClass filterClass) {

		if(filterGuiPanel != null)			
			filterGuiPanel.savePreferences();
		
		filterGuiPanel = null;
		filterParameters.removeAll();
		
		if(filterClass.equals(FilterClass.SAVITZKY_GOLAY))
			filterGuiPanel = new SavitzkyGolayGuiPanel();	
		
		if(filterClass.equals(FilterClass.SAVITZKY_GOLAY_MZMINE))
			filterGuiPanel = new SavitzkyGolayMZMineGuiPanel();	
		
		if(filterClass.equals(FilterClass.MOVING_AVERAGE))
			filterGuiPanel = new MovingAverageGuiPanel();
		
		if(filterClass.equals(FilterClass.WEIGHTED_MOVING_AVERAGE))
			filterGuiPanel = new WeightedMovingAverageGuiPanel();
		
		if(filterClass.equals(FilterClass.LOESS))			
			filterGuiPanel = new LoessGuiPanel();
		
		if(filterClass.equals(FilterClass.SMOOTHING_CUBIC_SPLINE)) 
			filterGuiPanel = new SmoothingCubicSplineGuiPanel();
		
		if(filterGuiPanel != null) {			
			filterGuiPanel.loadPreferences();
			filterParameters.add(filterGuiPanel, BorderLayout.CENTER);
			Filter f = getSmoothingFilter();
			//	System.err.println("Filter " + filterGuiPanel.getFilterClass().getName() + " - " + f.toString());
		}
		else {
			filterParameters.add(new JPanel(), BorderLayout.CENTER);
		}
		filterParameters.revalidate();
		filterParameters.repaint();		
	}

	public void removeDataFiles(Collection<DataFile> filesToRemove) {
		rawDataFileTable.removeDataFiles(filesToRemove);
	}

	public void setMassList(String massListString) {
		mzTextField.setText(massListString);
	}
}
