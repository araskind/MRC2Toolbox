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

package edu.umich.med.mrc2.datoolbox.gui.preferences;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.enums.IntensityFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.RetentionUnits;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableGeneralPreferencesPanel extends DefaultSingleCDockable implements BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -4978637455862585320L;

	private static final Icon componentIcon = GuiUtils.getIcon("script", 16);

	private Preferences prefs;

	private JTextField timeStampPatternTextField;
	private JSpinner taskNumberSpinner;
	private JSpinner massFormatSpinner;
	private JComboBox rtFormatComboBox;
	private JSpinner rtFormatSpiner;
	private JComboBox intensityFormatComboBox;
	private JSpinner sciNotationDecimalsSpinner;
	private JFormattedTextField massErrorTextField;
	private JFormattedTextField rtErrorTextField;

	public DockableGeneralPreferencesPanel() {

		super("DockableGeneralPreferencesPanel", componentIcon, "General preferences", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Parallel processing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BorderLayout(0, 0));

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel_2.add(panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{147, 0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);

		JLabel lblNewLabel = new JLabel("Maximal number of tasks");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_3.add(lblNewLabel, gbc_lblNewLabel);

		taskNumberSpinner = new JSpinner();
		taskNumberSpinner.setModel(new SpinnerNumberModel(1, 1, null, 1));
		taskNumberSpinner.setPreferredSize(new Dimension(80, 26));
		taskNumberSpinner.setMinimumSize(new Dimension(80, 26));
		GridBagConstraints gbc_taskNumberSpinner = new GridBagConstraints();
		gbc_taskNumberSpinner.gridx = 1;
		gbc_taskNumberSpinner.gridy = 0;
		panel_3.add(taskNumberSpinner, gbc_taskNumberSpinner);

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(null, "Data formatting", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.add(panel_6, BorderLayout.CENTER);
		GridBagLayout gbl_panel_6 = new GridBagLayout();
		gbl_panel_6.columnWidths = new int[]{0, 145, 117, 0, 0};
		gbl_panel_6.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_6.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_6.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_6.setLayout(gbl_panel_6);

		JLabel lblAccurateMass = new JLabel("Accurate mass, # decimals");
		GridBagConstraints gbc_lblAccurateMass = new GridBagConstraints();
		gbc_lblAccurateMass.anchor = GridBagConstraints.EAST;
		gbc_lblAccurateMass.insets = new Insets(0, 0, 5, 5);
		gbc_lblAccurateMass.gridx = 0;
		gbc_lblAccurateMass.gridy = 0;
		panel_6.add(lblAccurateMass, gbc_lblAccurateMass);

		massFormatSpinner = new JSpinner();
		massFormatSpinner.setModel(new SpinnerNumberModel(3, 1, 6, 1));
		massFormatSpinner.setPreferredSize(new Dimension(80, 26));
		massFormatSpinner.setMinimumSize(new Dimension(80, 26));
		GridBagConstraints gbc_massFormatSpinner = new GridBagConstraints();
		gbc_massFormatSpinner.anchor = GridBagConstraints.WEST;
		gbc_massFormatSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_massFormatSpinner.gridx = 1;
		gbc_massFormatSpinner.gridy = 0;
		panel_6.add(massFormatSpinner, gbc_massFormatSpinner);

		JLabel lblRetentionTimeUnits = new JLabel("Retention time units");
		GridBagConstraints gbc_lblRetentionTimeUnits = new GridBagConstraints();
		gbc_lblRetentionTimeUnits.anchor = GridBagConstraints.EAST;
		gbc_lblRetentionTimeUnits.insets = new Insets(0, 0, 5, 5);
		gbc_lblRetentionTimeUnits.gridx = 0;
		gbc_lblRetentionTimeUnits.gridy = 1;
		panel_6.add(lblRetentionTimeUnits, gbc_lblRetentionTimeUnits);

		rtFormatComboBox = new JComboBox(new DefaultComboBoxModel<RetentionUnits>(RetentionUnits.values()));
		rtFormatComboBox.setPreferredSize(new Dimension(80, 26));
		rtFormatComboBox.setMinimumSize(new Dimension(80, 26));
		GridBagConstraints gbc_rtFormatComboBox = new GridBagConstraints();
		gbc_rtFormatComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_rtFormatComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFormatComboBox.gridx = 1;
		gbc_rtFormatComboBox.gridy = 1;
		panel_6.add(rtFormatComboBox, gbc_rtFormatComboBox);

		JLabel lblRtOf = new JLabel("RT, # of decimals");
		GridBagConstraints gbc_lblRtOf = new GridBagConstraints();
		gbc_lblRtOf.anchor = GridBagConstraints.EAST;
		gbc_lblRtOf.insets = new Insets(0, 0, 5, 5);
		gbc_lblRtOf.gridx = 2;
		gbc_lblRtOf.gridy = 1;
		panel_6.add(lblRtOf, gbc_lblRtOf);

		rtFormatSpiner = new JSpinner();
		rtFormatSpiner.setModel(new SpinnerNumberModel(0, 0, 4, 1));
		rtFormatSpiner.setPreferredSize(new Dimension(80, 26));
		rtFormatSpiner.setMinimumSize(new Dimension(80, 26));
		GridBagConstraints gbc_rtFormatSpiner = new GridBagConstraints();
		gbc_rtFormatSpiner.insets = new Insets(0, 0, 5, 0);
		gbc_rtFormatSpiner.gridx = 3;
		gbc_rtFormatSpiner.gridy = 1;
		panel_6.add(rtFormatSpiner, gbc_rtFormatSpiner);

		JLabel lblPeakIntensityFormat = new JLabel("Peak intensity format");
		GridBagConstraints gbc_lblPeakIntensityFormat = new GridBagConstraints();
		gbc_lblPeakIntensityFormat.anchor = GridBagConstraints.EAST;
		gbc_lblPeakIntensityFormat.insets = new Insets(0, 0, 5, 5);
		gbc_lblPeakIntensityFormat.gridx = 0;
		gbc_lblPeakIntensityFormat.gridy = 2;
		panel_6.add(lblPeakIntensityFormat, gbc_lblPeakIntensityFormat);

		intensityFormatComboBox = new JComboBox(new DefaultComboBoxModel<IntensityFormat>(IntensityFormat.values()));
		intensityFormatComboBox.setPreferredSize(new Dimension(80, 26));
		intensityFormatComboBox.setMinimumSize(new Dimension(80, 26));
		GridBagConstraints gbc_intensityFormatomboBox = new GridBagConstraints();
		gbc_intensityFormatomboBox.insets = new Insets(0, 0, 5, 5);
		gbc_intensityFormatomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_intensityFormatomboBox.gridx = 1;
		gbc_intensityFormatomboBox.gridy = 2;
		panel_6.add(intensityFormatComboBox, gbc_intensityFormatomboBox);

		JLabel lblOfDecimals = new JLabel("# of decimals (exp)");
		GridBagConstraints gbc_lblOfDecimals = new GridBagConstraints();
		gbc_lblOfDecimals.insets = new Insets(0, 0, 5, 5);
		gbc_lblOfDecimals.gridx = 2;
		gbc_lblOfDecimals.gridy = 2;
		panel_6.add(lblOfDecimals, gbc_lblOfDecimals);

		sciNotationDecimalsSpinner = new JSpinner();
		sciNotationDecimalsSpinner.setModel(new SpinnerNumberModel(0, 0, 6, 1));
		sciNotationDecimalsSpinner.setPreferredSize(new Dimension(80, 26));
		sciNotationDecimalsSpinner.setMinimumSize(new Dimension(80, 26));
		GridBagConstraints gbc_sciNotationDecimalsSpinner = new GridBagConstraints();
		gbc_sciNotationDecimalsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_sciNotationDecimalsSpinner.gridx = 3;
		gbc_sciNotationDecimalsSpinner.gridy = 2;
		panel_6.add(sciNotationDecimalsSpinner, gbc_sciNotationDecimalsSpinner);

		JLabel lblFileTimestampFormat = new JLabel("File timestamp format");
		GridBagConstraints gbc_lblFileTimestampFormat = new GridBagConstraints();
		gbc_lblFileTimestampFormat.anchor = GridBagConstraints.EAST;
		gbc_lblFileTimestampFormat.insets = new Insets(0, 0, 0, 5);
		gbc_lblFileTimestampFormat.gridx = 0;
		gbc_lblFileTimestampFormat.gridy = 3;
		panel_6.add(lblFileTimestampFormat, gbc_lblFileTimestampFormat);

		timeStampPatternTextField = new JTextField();
		timeStampPatternTextField.setPreferredSize(new Dimension(100, 26));
		timeStampPatternTextField.setMinimumSize(new Dimension(100, 26));
		GridBagConstraints gbc_timeStampPatternTextField = new GridBagConstraints();
		gbc_timeStampPatternTextField.gridwidth = 2;
		gbc_timeStampPatternTextField.insets = new Insets(0, 0, 0, 5);
		gbc_timeStampPatternTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_timeStampPatternTextField.gridx = 1;
		gbc_timeStampPatternTextField.gridy = 3;
		panel_6.add(timeStampPatternTextField, gbc_timeStampPatternTextField);
		timeStampPatternTextField.setColumns(10);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Data accuracy windows", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.add(panel, BorderLayout.SOUTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblMassErrorPpm = new JLabel("Mass error, ppm");
		GridBagConstraints gbc_lblMassErrorPpm = new GridBagConstraints();
		gbc_lblMassErrorPpm.insets = new Insets(0, 0, 0, 5);
		gbc_lblMassErrorPpm.anchor = GridBagConstraints.EAST;
		gbc_lblMassErrorPpm.gridx = 0;
		gbc_lblMassErrorPpm.gridy = 0;
		panel.add(lblMassErrorPpm, gbc_lblMassErrorPpm);

		massErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massErrorTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField.anchor = GridBagConstraints.WEST;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 0;
		panel.add(massErrorTextField, gbc_formattedTextField);

		JLabel lblRetentionErrorMin = new JLabel("Retention error, min");
		GridBagConstraints gbc_lblRetentionErrorMin = new GridBagConstraints();
		gbc_lblRetentionErrorMin.insets = new Insets(0, 0, 0, 5);
		gbc_lblRetentionErrorMin.anchor = GridBagConstraints.EAST;
		gbc_lblRetentionErrorMin.gridx = 2;
		gbc_lblRetentionErrorMin.gridy = 0;
		panel.add(lblRetentionErrorMin, gbc_lblRetentionErrorMin);

		rtErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtErrorTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.anchor = GridBagConstraints.WEST;
		gbc_formattedTextField_1.gridx = 3;
		gbc_formattedTextField_1.gridy = 0;
		panel.add(rtErrorTextField, gbc_formattedTextField_1);
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub
		prefs = preferences;

		taskNumberSpinner.setValue(MRC2ToolBoxConfiguration.getMaxThreadNumber());

		rtFormatComboBox.setSelectedItem(MRC2ToolBoxConfiguration.getRtUnits());
		int rtDecimals = getDecimals(prefs.get(MRC2ToolBoxConfiguration.RT_FORMAT, MRC2ToolBoxConfiguration.RT_FORMAT_DEFAULT));
		rtFormatSpiner.setValue(rtDecimals);

		int mzDecimals = getDecimals(prefs.get(MRC2ToolBoxConfiguration.MZ_FORMAT, MRC2ToolBoxConfiguration.MZ_FORMAT_DEFAULT));
		massFormatSpinner.setValue(mzDecimals);

		intensityFormatComboBox.setSelectedItem(MRC2ToolBoxConfiguration.getIntensityNotation());
		sciNotationDecimalsSpinner.setValue(MRC2ToolBoxConfiguration.getIntensityDecimals());

		timeStampPatternTextField.setText(prefs.get(MRC2ToolBoxConfiguration.FILE_TIMESTAMP_FORMAT, MRC2ToolBoxConfiguration.FILE_TIMESTAMP_FORMAT_DEFAULT));

		massErrorTextField.setText(Double.toString(MRC2ToolBoxConfiguration.getMassAccuracy()));
		rtErrorTextField.setText(Double.toString(MRC2ToolBoxConfiguration.getRtWindow()));
	}

	@Override
	public void loadPreferences() {

		if(prefs != null)
			loadPreferences(prefs);
	}

	@Override
	public void savePreferences() {

		MRC2ToolBoxConfiguration.setMaxThreadNumber((int) taskNumberSpinner.getValue());
		MRC2ToolBoxConfiguration.setRtUnits((RetentionUnits) rtFormatComboBox.getSelectedItem());

		String rtFormatString = "#." + StringUtils.repeat("#", (int) rtFormatSpiner.getValue());
		MRC2ToolBoxConfiguration.setRtFormat(rtFormatString);

		String mzFormatString = "#." + StringUtils.repeat("#", (int) massFormatSpinner.getValue());
		MRC2ToolBoxConfiguration.setMzFormat(mzFormatString);

		MRC2ToolBoxConfiguration.setIntensityNotation((IntensityFormat) intensityFormatComboBox.getSelectedItem());
		MRC2ToolBoxConfiguration.setIntensityDecimals((int) sciNotationDecimalsSpinner.getValue());

		String intensityFormatString = "#,###";

		if(((IntensityFormat)intensityFormatComboBox.getSelectedItem()).equals(IntensityFormat.SCIENTIFIC))
			intensityFormatString = "0." + StringUtils.repeat("#", (int) sciNotationDecimalsSpinner.getValue()) + "E00";

		MRC2ToolBoxConfiguration.setIntensityFormat(intensityFormatString);

		String ftString = timeStampPatternTextField.getText().trim();
		if(!ftString.isEmpty()) {

			try {
				SimpleDateFormat fileTimeStampFormat = new SimpleDateFormat(ftString);
				MRC2ToolBoxConfiguration.setFileTimeStampFormat(ftString);
			}
			catch (Exception e) {

				timeStampPatternTextField.setText(prefs.get(MRC2ToolBoxConfiguration.FILE_TIMESTAMP_FORMAT, MRC2ToolBoxConfiguration.FILE_TIMESTAMP_FORMAT_DEFAULT));
				MessageDialog.showErrorMsg("Invalid timestamp format!", this.getContentPane());
				return;
			}
		}
		else {
			timeStampPatternTextField.setText(prefs.get(MRC2ToolBoxConfiguration.FILE_TIMESTAMP_FORMAT, MRC2ToolBoxConfiguration.FILE_TIMESTAMP_FORMAT_DEFAULT));
		}
		MRC2ToolBoxConfiguration.setMassAccuracy(Double.parseDouble(massErrorTextField.getText()));
		MRC2ToolBoxConfiguration.setRtWindow(Double.parseDouble(rtErrorTextField.getText()));
	}

	private int getDecimals(String numberFormat) {
		return numberFormat.substring(numberFormat.lastIndexOf(".") + 1).length();
	}

}
