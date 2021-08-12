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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

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

public class XICSetupPanel extends JPanel
		implements ActionListener, ItemListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1598480456937212595L;
	private static final Icon chromIcon = GuiUtils.getIcon("ionChroms", 16);
	private RawDataFileTable rawDataFileTable;
	private JComboBox chromTypeComboBox;
	private JComboBox polarityComboBox;
	private JComboBox msLevelcomboBox;
	private JTextField mzTextField;
	private JFormattedTextField mzErrorTextField;
	private JFormattedTextField rtFromTextField;
	private JFormattedTextField rtToTextField;
	private JButton extractButton;
	private JCheckBox chckbxLimitRtRange;
	private JLabel lblMzValues;
	private JLabel lblMzWindow;
	private JComboBox mzWindowTypeComboBox;
	private JCheckBox sumAllMassesCheckBox;

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

	public XICSetupPanel(ActionListener listener) {

		super();

		setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 101, 90, 66, 67, 32, 53, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
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

		JLabel lblChromatogramType = new JLabel("Chromatogram type");
		GridBagConstraints gbc_lblChromatogramType = new GridBagConstraints();
		gbc_lblChromatogramType.anchor = GridBagConstraints.EAST;
		gbc_lblChromatogramType.insets = new Insets(0, 0, 5, 5);
		gbc_lblChromatogramType.gridx = 0;
		gbc_lblChromatogramType.gridy = 1;
		panel.add(lblChromatogramType, gbc_lblChromatogramType);

		chromTypeComboBox = 
				new JComboBox<ChromatogramPlotMode>(ChromatogramPlotMode.values());
		chromTypeComboBox.setSelectedItem(ChromatogramPlotMode.TIC);
		GridBagConstraints gbc_typecomboBox = new GridBagConstraints();
		gbc_typecomboBox.gridwidth = 5;
		gbc_typecomboBox.insets = new Insets(0, 0, 5, 0);
		gbc_typecomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_typecomboBox.gridx = 1;
		gbc_typecomboBox.gridy = 1;
		panel.add(chromTypeComboBox, gbc_typecomboBox);

		JLabel lblPolarity = new JLabel("Polarity");
		GridBagConstraints gbc_lblPolarity = new GridBagConstraints();
		gbc_lblPolarity.anchor = GridBagConstraints.EAST;
		gbc_lblPolarity.insets = new Insets(0, 0, 5, 5);
		gbc_lblPolarity.gridx = 0;
		gbc_lblPolarity.gridy = 2;
		panel.add(lblPolarity, gbc_lblPolarity);

		polarityComboBox = 
				new JComboBox<Polarity>(new Polarity[] { Polarity.Positive, Polarity.Negative });
		polarityComboBox.addItemListener(this);
		GridBagConstraints gbc_polarcomboBox_1 = new GridBagConstraints();
		gbc_polarcomboBox_1.gridwidth = 2;
		gbc_polarcomboBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_polarcomboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_polarcomboBox_1.gridx = 1;
		gbc_polarcomboBox_1.gridy = 2;
		panel.add(polarityComboBox, gbc_polarcomboBox_1);

		JLabel lblMsLevel = new JLabel("MS level");
		GridBagConstraints gbc_lblMsLevel = new GridBagConstraints();
		gbc_lblMsLevel.anchor = GridBagConstraints.EAST;
		gbc_lblMsLevel.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsLevel.gridx = 3;
		gbc_lblMsLevel.gridy = 2;
		panel.add(lblMsLevel, gbc_lblMsLevel);

		msLevelcomboBox = new JComboBox<Integer>(new Integer[] { 1, 2, 3, 4, 5 });
		GridBagConstraints gbc_mslevcomboBox_2 = new GridBagConstraints();
		gbc_mslevcomboBox_2.gridwidth = 2;
		gbc_mslevcomboBox_2.insets = new Insets(0, 0, 5, 0);
		gbc_mslevcomboBox_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_mslevcomboBox_2.gridx = 4;
		gbc_mslevcomboBox_2.gridy = 2;
		panel.add(msLevelcomboBox, gbc_mslevcomboBox_2);

		lblMzValues = new JLabel("M/Z value(s)");
		GridBagConstraints gbc_lblMzValues = new GridBagConstraints();
		gbc_lblMzValues.anchor = GridBagConstraints.EAST;
		gbc_lblMzValues.insets = new Insets(0, 0, 5, 5);
		gbc_lblMzValues.gridx = 0;
		gbc_lblMzValues.gridy = 3;
		panel.add(lblMzValues, gbc_lblMzValues);

		mzTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 4;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 3;
		panel.add(mzTextField, gbc_textField);
		mzTextField.setColumns(10);

		sumAllMassesCheckBox = new JCheckBox("Sum all");
		GridBagConstraints gbc_sumAllMassesCheckBox = new GridBagConstraints();
		gbc_sumAllMassesCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_sumAllMassesCheckBox.gridx = 5;
		gbc_sumAllMassesCheckBox.gridy = 3;
		panel.add(sumAllMassesCheckBox, gbc_sumAllMassesCheckBox);

		lblMzWindow = new JLabel("M/Z window");
		GridBagConstraints gbc_lblMzWindow = new GridBagConstraints();
		gbc_lblMzWindow.anchor = GridBagConstraints.EAST;
		gbc_lblMzWindow.insets = new Insets(0, 0, 5, 5);
		gbc_lblMzWindow.gridx = 0;
		gbc_lblMzWindow.gridy = 4;
		panel.add(lblMzWindow, gbc_lblMzWindow);

		mzErrorTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		mzErrorTextField.setText(
				MRC2ToolBoxConfiguration.getPpmFormat().format(
						MRC2ToolBoxConfiguration.getMassAccuracy()));
		mzErrorTextField = new JFormattedTextField();
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 4;
		panel.add(mzErrorTextField, gbc_formattedTextField);

		mzWindowTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		GridBagConstraints gbc_mzWindowTypeComboBox = new GridBagConstraints();
		gbc_mzWindowTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_mzWindowTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzWindowTypeComboBox.gridx = 2;
		gbc_mzWindowTypeComboBox.gridy = 4;
		panel.add(mzWindowTypeComboBox, gbc_mzWindowTypeComboBox);

		chckbxLimitRtRange = new JCheckBox("Limit RT range");
		GridBagConstraints gbc_chckbxLimitRtRange_1 = new GridBagConstraints();
		gbc_chckbxLimitRtRange_1.anchor = GridBagConstraints.WEST;
		gbc_chckbxLimitRtRange_1.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxLimitRtRange_1.gridx = 1;
		gbc_chckbxLimitRtRange_1.gridy = 5;
		panel.add(chckbxLimitRtRange, gbc_chckbxLimitRtRange_1);

		JLabel lblFrom = new JLabel("from");
		GridBagConstraints gbc_lblFrom = new GridBagConstraints();
		gbc_lblFrom.insets = new Insets(0, 0, 5, 5);
		gbc_lblFrom.anchor = GridBagConstraints.EAST;
		gbc_lblFrom.gridx = 2;
		gbc_lblFrom.gridy = 5;
		panel.add(lblFrom, gbc_lblFrom);

		rtFromTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtFromTextField.setColumns(10);
		GridBagConstraints gbc_rtFromTextField = new GridBagConstraints();
		gbc_rtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFromTextField.gridx = 3;
		gbc_rtFromTextField.gridy = 5;
		panel.add(rtFromTextField, gbc_rtFromTextField);

		JLabel lblTo = new JLabel("to");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.gridx = 4;
		gbc_lblTo.gridy = 5;
		panel.add(lblTo, gbc_lblTo);

		rtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtToTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 5, 0);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 5;
		gbc_formattedTextField_1.gridy = 5;
		panel.add(rtToTextField, gbc_formattedTextField_1);

		extractButton = new JButton(MainActionCommands.EXTRACT_CHROMATOGRAM.getName());
		extractButton.setActionCommand(MainActionCommands.EXTRACT_CHROMATOGRAM.getName());
		extractButton.setToolTipText(MainActionCommands.EXTRACT_CHROMATOGRAM.getName());
		extractButton.addActionListener(listener);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridwidth = 5;
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 6;
		panel.add(extractButton, gbc_btnNewButton);

		//	loadPreferences();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}
	
	public Collection<String>veryfyParameters(){
		
		Collection<String>errors = new ArrayList<String>();
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
					errors.add("Invalid mass value(s)!\nOnly numbers, \".\" as decimal pont, and the following separator characters:\n"
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
		savePreferences();
		ChromatogramExtractionTask xicTask = new ChromatogramExtractionTask(
				files, 
				mode, 
				polarity, 
				msLevel, 
				mzList,
				sumAllMassChromatograms, 
				mzWindowValue, 
				massErrorType, 
				rtRange);
		
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
		if(dataFiles == null || dataFiles.isEmpty())
			return;
		
		DataAcquisitionMethod method = dataFiles.iterator().next().getDataAcquisitionMethod();
		if(method != null && method.getPolarity() != null)
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
		
		chromTypeComboBox.setSelectedItem(
				ChromatogramPlotMode.valueOf(preferences.get(CHROMATOGRAM_TYPE, CHROMATOGRAM_TYPE_DEFAULT)));
		msLevelcomboBox.setSelectedItem(preferences.getInt(MS_LEVEL, MS_LEVEL_DEFAULT));
		mzTextField.setText(preferences.get(MZ_LIST, MZ_LIST_DEFAULT));
		sumAllMassesCheckBox.setSelected(preferences.getBoolean(MZ_LIST_SUM_ALL, MZ_LIST_SUM_ALL_DEFAULT));
		mzErrorTextField.setText(Double.toString(preferences.getDouble(MASS_ERROR_VALUE, MASS_ERROR_VALUE_DEFAULT)));
		mzWindowTypeComboBox.setSelectedItem(
				MassErrorType.getTypeByName(preferences.get(MASS_ERROR_TYPE, MASS_ERROR_TYPE_DEFAULT)));
		chckbxLimitRtRange.setSelected(preferences.getBoolean(USE_RT_RANGE, USE_RT_RANGE_DEFAULT));
		rtFromTextField.setText(preferences.get(RT_MIN_VALUE, RT_MIN_VALUE_DEFAULT));
		rtToTextField.setText(preferences.get(RT_MAX_VALUE, RT_MAX_VALUE_DEFAULT));
	}

	@Override
	public void savePreferences() {

		if(preferences == null)
			preferences = Preferences.userRoot().node(CLASS_NAME);

		preferences.put(CHROMATOGRAM_TYPE, ((ChromatogramPlotMode) chromTypeComboBox.getSelectedItem()).name());
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
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	public void removeDataFiles(Collection<DataFile> filesToRemove) {
		rawDataFileTable.removeDataFiles(filesToRemove);
	}

	public void setMassList(String massListString) {
		mzTextField.setText(massListString);
	}
}
