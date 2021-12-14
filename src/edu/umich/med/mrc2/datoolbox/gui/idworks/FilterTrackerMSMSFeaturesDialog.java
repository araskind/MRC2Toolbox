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

package edu.umich.med.mrc2.datoolbox.gui.idworks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class FilterTrackerMSMSFeaturesDialog extends JDialog implements ActionListener, BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = -5028494778833454970L;

	private static final String RESET_COMMAND = "Reset";
	
	private static final NumberFormat twoDecFormat = new DecimalFormat("###.##");
	private static final NumberFormat wholeNumDecFormat = new DecimalFormat("###");
	
	private Preferences preferences;
	public static final String MZ_MIN = "MZ_MIN";
	public static final String MZ_MAX = "MZ_MAX";
	public static final String MZ_BP_PRECURSOR = "MZ_BP_PRECURSOR";
	public static final String MZ_ERROR_VALUE = "MZ_ERROR_VALUE";
	public static final String MZ_ERROR_TYPE = "MZ_ERROR_TYPE";
	public static final String RT_MIN = "RT_MIN";
	public static final String RT_MAX = "RT_MAX";
	public static final String NAME_STRING = "NAME_STRING";
	public static final String ID_STATUS = "ID_STATUS";
	public static final String S_ENTROPY_MIN = "S_ENTROPY_MIN";
	public static final String S_ENTROPY_MAX = "S_ENTROPY_MAX";
	public static final String PEAK_AREA_MIN = "PEAK_AREA_MIN";
	public static final String PEAK_AREA_MAX = "PEAK_AREA_MAX";
	public static final String MIN_SCORE = "MIN_SCORE";
	public static final String INCLUDE_NORMAL_MATCH = "INCLUDE_NORMAL_MATCH";
	public static final String INCLUDE_IN_SOURCE_MATCH = "INCLUDE_IN_SOURCE_MATCH";
	public static final String INCLUDE_HYBRID_MATCH = "INCLUDE_HYBRID_MATCH";
	public static final String SEARCH_ALL_IDS = "SEARCH_ALL_IDS";

	private JButton cancelButton;
	private JButton filterButton;
	private JButton resetButton;
	private JCheckBox hybridMatchCheckBox;
	private JCheckBox inSourceCheckBox;
	private JCheckBox regularMatchCheckBox;
	private JCheckBox searchAllIDsCheckBox;
	private JComboBox idStatusComboBox;
	private JComboBox massErrorTypeComboBox;
	private JFormattedTextField bpMzMaxTextField;
	private JFormattedTextField bpmzMinTextField;
	private JFormattedTextField massErrorTextField;
	private JFormattedTextField minScoreTextField;
	private JFormattedTextField peakAreaMaxTextField;
	private JFormattedTextField peakAreaMinTextField;
	private JFormattedTextField rtFromTextField;
	private JFormattedTextField rtToTextField;
	private JFormattedTextField seMaxTextField;
	private JFormattedTextField seMinTextField;
	private JFormattedTextField targetMassTextField;
	private JTextField nameTextField;

	public FilterTrackerMSMSFeaturesDialog(ActionListener listener) {

		super();
		setTitle("Filter features");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(700, 400));
		setPreferredSize(new Dimension(700, 400));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 98, 32, 88, 37, 78, 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblBasePeakMz = new JLabel("Precursor M/Z from ");
		GridBagConstraints gbc_lblBasePeakMz = new GridBagConstraints();
		gbc_lblBasePeakMz.anchor = GridBagConstraints.EAST;
		gbc_lblBasePeakMz.insets = new Insets(0, 0, 5, 5);
		gbc_lblBasePeakMz.gridx = 0;
		gbc_lblBasePeakMz.gridy = 0;
		panel.add(lblBasePeakMz, gbc_lblBasePeakMz);

		bpmzMinTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		bpmzMinTextField.setColumns(10);
		GridBagConstraints gbc_bpmzMinTextField = new GridBagConstraints();
		gbc_bpmzMinTextField.insets = new Insets(0, 0, 5, 5);
		gbc_bpmzMinTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_bpmzMinTextField.gridx = 1;
		gbc_bpmzMinTextField.gridy = 0;
		panel.add(bpmzMinTextField, gbc_bpmzMinTextField);

		JLabel lblTo_1 = new JLabel("to");
		GridBagConstraints gbc_lblTo_1 = new GridBagConstraints();
		gbc_lblTo_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo_1.gridx = 2;
		gbc_lblTo_1.gridy = 0;
		panel.add(lblTo_1, gbc_lblTo_1);

		bpMzMaxTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		bpMzMaxTextField.setColumns(10);
		GridBagConstraints gbc_bpMxMaxTextField = new GridBagConstraints();
		gbc_bpMxMaxTextField.insets = new Insets(0, 0, 5, 5);
		gbc_bpMxMaxTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_bpMxMaxTextField.gridx = 3;
		gbc_bpMxMaxTextField.gridy = 0;
		panel.add(bpMzMaxTextField, gbc_bpMxMaxTextField);
		
		JLabel lblNewLabel_7 = new JLabel("OR");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_7.gridx = 4;
		gbc_lblNewLabel_7.gridy = 0;
		panel.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		targetMassTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		targetMassTextField.setColumns(10);
		GridBagConstraints gbc_targetMassTextField = new GridBagConstraints();
		gbc_targetMassTextField.insets = new Insets(0, 0, 5, 5);
		gbc_targetMassTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_targetMassTextField.gridx = 5;
		gbc_targetMassTextField.gridy = 0;
		panel.add(targetMassTextField, gbc_targetMassTextField);
		
		JLabel lblNewLabel_8 = new JLabel("+/-");
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_8.gridx = 6;
		gbc_lblNewLabel_8.gridy = 0;
		panel.add(lblNewLabel_8, gbc_lblNewLabel_8);
		
		massErrorTextField = new JFormattedTextField(twoDecFormat);
		GridBagConstraints gbc_massErrorTextField = new GridBagConstraints();
		gbc_massErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_massErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTextField.gridx = 7;
		gbc_massErrorTextField.gridy = 0;
		panel.add(massErrorTextField, gbc_massErrorTextField);
		
		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		GridBagConstraints gbc_massErrorTypeComboBox = new GridBagConstraints();
		gbc_massErrorTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_massErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTypeComboBox.gridx = 8;
		gbc_massErrorTypeComboBox.gridy = 0;
		panel.add(massErrorTypeComboBox, gbc_massErrorTypeComboBox);

		JLabel lblRetentionTimeFrom = new JLabel("Retention time from ");
		GridBagConstraints gbc_lblRetentionTimeFrom = new GridBagConstraints();
		gbc_lblRetentionTimeFrom.anchor = GridBagConstraints.EAST;
		gbc_lblRetentionTimeFrom.insets = new Insets(0, 0, 5, 5);
		gbc_lblRetentionTimeFrom.gridx = 0;
		gbc_lblRetentionTimeFrom.gridy = 1;
		panel.add(lblRetentionTimeFrom, gbc_lblRetentionTimeFrom);

		rtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtFromTextField.setColumns(10);
		GridBagConstraints gbc_rtFromTextField = new GridBagConstraints();
		gbc_rtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFromTextField.gridx = 1;
		gbc_rtFromTextField.gridy = 1;
		panel.add(rtFromTextField, gbc_rtFromTextField);

		JLabel lblTo = new JLabel(" to");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.gridx = 2;
		gbc_lblTo.gridy = 1;
		panel.add(lblTo, gbc_lblTo);

		rtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtToTextField.setColumns(10);
		GridBagConstraints gbc_rtToTextField = new GridBagConstraints();
		gbc_rtToTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtToTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtToTextField.gridx = 3;
		gbc_rtToTextField.gridy = 1;
		panel.add(rtToTextField, gbc_rtToTextField);

		JLabel lblNewLabel = new JLabel("Feature name contains: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 2;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 6;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 2;
		panel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		searchAllIDsCheckBox = new JCheckBox("Search all IDs");
		GridBagConstraints gbc_searchAllIDsCheckBox = new GridBagConstraints();
		gbc_searchAllIDsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_searchAllIDsCheckBox.gridx = 7;
		gbc_searchAllIDsCheckBox.gridy = 2;
		panel.add(searchAllIDsCheckBox, gbc_searchAllIDsCheckBox);

		JLabel lblNewLabel_1 = new JLabel("Identification ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		idStatusComboBox = new JComboBox<FeatureSubsetByIdentification>(
				new DefaultComboBoxModel<FeatureSubsetByIdentification>(FeatureSubsetByIdentification.values()));
		GridBagConstraints gbc_idStatusComboBox = new GridBagConstraints();
		gbc_idStatusComboBox.gridwidth = 3;
		gbc_idStatusComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_idStatusComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_idStatusComboBox.gridx = 1;
		gbc_idStatusComboBox.gridy = 3;
		panel.add(idStatusComboBox, gbc_idStatusComboBox);

		JLabel lblNewLabel_3 = new JLabel("Spectrum entropy from ");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 4;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);

		seMinTextField = new JFormattedTextField(twoDecFormat);
		seMinTextField.setColumns(10);
		GridBagConstraints gbc_seMinTextField = new GridBagConstraints();
		gbc_seMinTextField.insets = new Insets(0, 0, 5, 5);
		gbc_seMinTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_seMinTextField.gridx = 1;
		gbc_seMinTextField.gridy = 4;
		panel.add(seMinTextField, gbc_seMinTextField);

		JLabel lblNewLabel_4 = new JLabel("to");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 4;
		panel.add(lblNewLabel_4, gbc_lblNewLabel_4);

		seMaxTextField = new JFormattedTextField(twoDecFormat);
		seMaxTextField.setColumns(10);
		GridBagConstraints gbc_seMaxTextField = new GridBagConstraints();
		gbc_seMaxTextField.insets = new Insets(0, 0, 5, 5);
		gbc_seMaxTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_seMaxTextField.gridx = 3;
		gbc_seMaxTextField.gridy = 4;
		panel.add(seMaxTextField, gbc_seMaxTextField);

		JLabel lblNewLabel_5 = new JLabel("Peak area from ");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 5;
		panel.add(lblNewLabel_5, gbc_lblNewLabel_5);

		peakAreaMinTextField = new JFormattedTextField(wholeNumDecFormat);
		peakAreaMinTextField.setColumns(10);
		GridBagConstraints gbc_peakAreaMinTextField = new GridBagConstraints();
		gbc_peakAreaMinTextField.insets = new Insets(0, 0, 5, 5);
		gbc_peakAreaMinTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_peakAreaMinTextField.gridx = 1;
		gbc_peakAreaMinTextField.gridy = 5;
		panel.add(peakAreaMinTextField, gbc_peakAreaMinTextField);

		JLabel lblNewLabel_6 = new JLabel("to");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 2;
		gbc_lblNewLabel_6.gridy = 5;
		panel.add(lblNewLabel_6, gbc_lblNewLabel_6);

		peakAreaMaxTextField = new JFormattedTextField(wholeNumDecFormat);
		peakAreaMaxTextField.setColumns(10);
		GridBagConstraints gbc_peakAreaMaxTextField = new GridBagConstraints();
		gbc_peakAreaMaxTextField.insets = new Insets(0, 0, 5, 5);
		gbc_peakAreaMaxTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_peakAreaMaxTextField.gridx = 3;
		gbc_peakAreaMaxTextField.gridy = 5;
		panel.add(peakAreaMaxTextField, gbc_peakAreaMaxTextField);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(
				new CompoundBorder(
						new TitledBorder(
								new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255),
										new Color(160, 160, 160)),
								"MSMS library match parameters", TitledBorder.LEADING, TitledBorder.TOP, null,
								new Color(0, 0, 0)),
						new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 9;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 7;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		regularMatchCheckBox = new JCheckBox("Regular match");
		GridBagConstraints gbc_regularMatchCheckBox = new GridBagConstraints();
		gbc_regularMatchCheckBox.anchor = GridBagConstraints.WEST;
		gbc_regularMatchCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_regularMatchCheckBox.gridx = 0;
		gbc_regularMatchCheckBox.gridy = 0;
		panel_1.add(regularMatchCheckBox, gbc_regularMatchCheckBox);

		inSourceCheckBox = new JCheckBox("In-source match");
		GridBagConstraints gbc_inSourceCheckBox = new GridBagConstraints();
		gbc_inSourceCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_inSourceCheckBox.anchor = GridBagConstraints.WEST;
		gbc_inSourceCheckBox.gridx = 1;
		gbc_inSourceCheckBox.gridy = 0;
		panel_1.add(inSourceCheckBox, gbc_inSourceCheckBox);

		hybridMatchCheckBox = new JCheckBox("Hybrid match");
		GridBagConstraints gbc_hybridMatchCheckBox = new GridBagConstraints();
		gbc_hybridMatchCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_hybridMatchCheckBox.anchor = GridBagConstraints.WEST;
		gbc_hybridMatchCheckBox.gridx = 2;
		gbc_hybridMatchCheckBox.gridy = 0;
		panel_1.add(hybridMatchCheckBox, gbc_hybridMatchCheckBox);

		JLabel lblNewLabel_2 = new JLabel("Minimal score");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);

		minScoreTextField = new JFormattedTextField(twoDecFormat);
		GridBagConstraints gbc_minScoreTextField = new GridBagConstraints();
		gbc_minScoreTextField.insets = new Insets(0, 0, 5, 5);
		gbc_minScoreTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minScoreTextField.gridx = 1;
		gbc_minScoreTextField.gridy = 1;
		panel_1.add(minScoreTextField, gbc_minScoreTextField);

		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 0;
		gbc_cancelButton.gridy = 8;
		panel.add(cancelButton, gbc_cancelButton);

		resetButton = new JButton("Clear filters");
		resetButton.addActionListener(this);
		resetButton.setActionCommand(RESET_COMMAND);
		GridBagConstraints gbc_resetButton = new GridBagConstraints();
		gbc_resetButton.anchor = GridBagConstraints.EAST;
		gbc_resetButton.insets = new Insets(0, 0, 0, 5);
		gbc_resetButton.gridx = 1;
		gbc_resetButton.gridy = 8;
		panel.add(resetButton, gbc_resetButton);

		filterButton = new JButton("Filter");
		filterButton.addActionListener(listener);
		filterButton.setActionCommand(MainActionCommands.FILTER_FEATURES_COMMAND.getName());
		GridBagConstraints gbc_filterButton = new GridBagConstraints();
		gbc_filterButton.gridwidth = 5;
		gbc_filterButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_filterButton.gridx = 4;
		gbc_filterButton.gridy = 8;
		panel.add(filterButton, gbc_filterButton);

		JRootPane rootPane = SwingUtilities.getRootPane(filterButton);
		rootPane.setDefaultButton(filterButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(al);
		loadPreferences();
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		if (command.equals(RESET_COMMAND))
			resetFilters() ;	
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();	
	}
	
	private void resetFilters() {
		
		hybridMatchCheckBox.setSelected(false);
		inSourceCheckBox.setSelected(false);
		regularMatchCheckBox.setSelected(false);
		searchAllIDsCheckBox.setSelected(false);
		idStatusComboBox.setSelectedItem(FeatureSubsetByIdentification.ALL);
		bpMzMaxTextField.setText("");
		bpmzMinTextField.setText("");
		massErrorTextField.setText("");
		minScoreTextField.setText("");
		peakAreaMaxTextField.setText("");
		peakAreaMinTextField.setText("");
		rtFromTextField.setText("");
		rtToTextField.setText("");
		seMaxTextField.setText("");
		seMinTextField.setText("");
		targetMassTextField.setText("");
		nameTextField.setText("");	
	}

	public String getFeatureNameSubstring() {

		if (!nameTextField.getText().trim().isEmpty())
			return nameTextField.getText().trim();
		else
			return null;
	}
	
	public boolean doSearchAllIds() {
		return searchAllIDsCheckBox.isSelected();
	}

	public Range getMzRange() {
		
		Range mzRange = getMzRangeForError();		
		if(mzRange != null)
			return mzRange;
		
		if(bpmzMinTextField.getText().isEmpty() && bpMzMaxTextField.getText().isEmpty())
			return null;
		
		double min = 0.0d;
		double max = Double.MAX_VALUE;
		if(!bpmzMinTextField.getText().isEmpty())
			min =  Double.parseDouble(bpmzMinTextField.getText());
		
		if(!bpMzMaxTextField.getText().isEmpty())
			max =  Double.parseDouble(bpMzMaxTextField.getText());

		if (min >= max) {
			MessageDialog.showErrorMsg("M/Z range invalid!");
			return null;
		} else {
			return new Range(min, max);
		}
	}

	public Range getRtRange() {

		if (rtFromTextField.getText().isEmpty() && rtToTextField.getText().isEmpty()) 
			return null;
			
		double min = 0.0d;
		double max = Double.MAX_VALUE;
		if(!rtFromTextField.getText().isEmpty())
			min =  Double.parseDouble(rtFromTextField.getText());
		
		if(!rtToTextField.getText().isEmpty())
			max =  Double.parseDouble(rtToTextField.getText());

		if (min >= max) {
			MessageDialog.showErrorMsg("RT range invalid!");
			return null;
		} else {
			return new Range(min, max);
		}
	}
	
	public Range getEntropyRange() {

		if (seMinTextField.getText().isEmpty() && seMaxTextField.getText().isEmpty()) 
			return null;
			
		double min = 0.0d;
		double max = Double.MAX_VALUE;
		if(!seMinTextField.getText().isEmpty())
			min =  Double.parseDouble(seMinTextField.getText());
		
		if(!seMaxTextField.getText().isEmpty())
			max =  Double.parseDouble(seMaxTextField.getText());

		if (min >= max) {
			MessageDialog.showErrorMsg("RT range invalid!");
			return null;
		} else {
			return new Range(min, max);
		}
	}
	
	public Range getPeakAreaRange() {

		if (peakAreaMinTextField.getText().isEmpty() && peakAreaMaxTextField.getText().isEmpty()) 
			return null;
			
		double min = 0.0d;
		double max = Double.MAX_VALUE;
		if(!peakAreaMinTextField.getText().isEmpty())
			min =  Double.parseDouble(peakAreaMinTextField.getText());
		
		if(!peakAreaMaxTextField.getText().isEmpty())
			max =  Double.parseDouble(peakAreaMaxTextField.getText());

		if (min >= max) {
			MessageDialog.showErrorMsg("Peak area range invalid!");
			return null;
		} else {
			return new Range(min, max);
		}
	}
		
	private Range getMzRangeForError() {
		
		if(!targetMassTextField.getText().trim().isEmpty() && 
				!massErrorTextField.getText().trim().isEmpty()) {
			double mz = Double.parseDouble(targetMassTextField.getText().trim());
			double error = Double.parseDouble(massErrorTextField.getText().trim());
			MassErrorType eType =  (MassErrorType)massErrorTypeComboBox.getSelectedItem();
			return MsUtils.createMassRange(mz, error, eType);
		}
		else
			return null;				
	}
	
	public FeatureSubsetByIdentification getIdStatusSubset() {
		return (FeatureSubsetByIdentification)idStatusComboBox.getSelectedItem();
	}
	
	public Collection<HiResSearchOption>getMSMSSearchTypes(){
		
		Collection<HiResSearchOption>searchTypes = new ArrayList<HiResSearchOption>();
		if(regularMatchCheckBox.isSelected())
			searchTypes.add(HiResSearchOption.z);
		
		if(inSourceCheckBox.isSelected())
			searchTypes.add(HiResSearchOption.u);
		
		if(hybridMatchCheckBox.isSelected())
			searchTypes.add(HiResSearchOption.y);
		
		return searchTypes;
	}
	
	public double getMinimalMSMSScore() {
				
		if(minScoreTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(minScoreTextField.getText());
	}
	
	public Collection<String>verifyParameters(){
		
		Collection<String>errors = new ArrayList<String>();
		if(!nameTextField.getText().trim().isEmpty() && nameTextField.getText().trim().length() < 4) {
			errors.add("Compound name string has to be at least 4 characters long");
		}
		if(getMzRangeForError() == null) {
			if (!bpmzMinTextField.getText().isEmpty() && !bpMzMaxTextField.getText().isEmpty()) {
				
				double from = Double.parseDouble(bpmzMinTextField.getText());
				double to = Double.parseDouble(bpMzMaxTextField.getText());
				if (from >= to)
					errors.add("Invalid mass range.");
			}
		}
		if (!rtFromTextField.getText().isEmpty() && !rtToTextField.getText().isEmpty()) {

			double from = Double.parseDouble(rtFromTextField.getText());
			double to = Double.parseDouble(rtToTextField.getText());
			if (from >= to) 
				errors.add("Invalid retention time range.");
		}
		if (!peakAreaMinTextField.getText().isEmpty() && !peakAreaMaxTextField.getText().isEmpty()) {

			double from = Double.parseDouble(peakAreaMinTextField.getText());
			double to = Double.parseDouble(peakAreaMaxTextField.getText());
			if (from >= to) 
				errors.add("Invalid peak area range.");
		}
		if (!seMinTextField.getText().isEmpty() && !seMaxTextField.getText().isEmpty()) {

			double from = Double.parseDouble(seMinTextField.getText());
			double to = Double.parseDouble(seMaxTextField.getText());
			if (from >= to) 
				errors.add("Invalid spectrum entropy range.");
		}
		return errors;
	}	

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		double mzMin = preferences.getDouble(MZ_MIN, 0.0d);
		if(mzMin > 0)
			bpmzMinTextField.setText(Double.toString(mzMin));

		double mzMax = preferences.getDouble(MZ_MAX, 0.0d);
		if(mzMax > 0)
			bpMzMaxTextField.setText(Double.toString(mzMax));
		
		double mzBpPrecursor = preferences.getDouble(MZ_BP_PRECURSOR, 0.0d);
		if(mzBpPrecursor > 0)
			targetMassTextField.setText(Double.toString(mzBpPrecursor));
		
		double mzErrorValue = preferences.getDouble(MZ_ERROR_VALUE, 0.0d);
		if(mzErrorValue > 0)
			massErrorTextField.setText(Double.toString(mzErrorValue));
		
		massErrorTypeComboBox.setSelectedItem(
				MassErrorType.getTypeByName(
						preferences.get(MZ_ERROR_TYPE, MassErrorType.ppm.name())));
		
		double rtMin = preferences.getDouble(RT_MIN, 0.0d);
		if(rtMin > 0)
			rtFromTextField.setText(Double.toString(rtMin));

		double rtMax = preferences.getDouble(RT_MAX, 0.0d);
		if(rtMax > 0)
			rtToTextField.setText(Double.toString(rtMax));
		
		nameTextField.setText(preferences.get(NAME_STRING, ""));
		searchAllIDsCheckBox.setSelected(
				preferences.getBoolean(SEARCH_ALL_IDS, false));
		
		idStatusComboBox.setSelectedItem(
				FeatureSubsetByIdentification.getOptionByName(
						preferences.get(ID_STATUS, FeatureSubsetByIdentification.ALL.name())));
		
		double seMin = preferences.getDouble(S_ENTROPY_MIN, 0.0d);
		if(seMin > 0)
			seMinTextField.setText(Double.toString(seMin));

		double seMax = preferences.getDouble(S_ENTROPY_MAX, 0.0d);
		if(seMax > 0)
			seMaxTextField.setText(Double.toString(seMax));
		
		double paMin = preferences.getDouble(PEAK_AREA_MIN, 0.0d);
		if(paMin > 0)
			peakAreaMinTextField.setText(Double.toString(paMin));

		double paMax = preferences.getDouble(PEAK_AREA_MAX, 0.0d);
		if(paMax > 0)
			peakAreaMaxTextField.setText(Double.toString(paMax));
		
		double minScore = preferences.getDouble(MIN_SCORE, 0.0d);
		if(minScore > 0)
			minScoreTextField.setText(Double.toString(minScore));
		
		regularMatchCheckBox.setSelected(
				preferences.getBoolean(INCLUDE_NORMAL_MATCH, false));
		inSourceCheckBox.setSelected(
				preferences.getBoolean(INCLUDE_IN_SOURCE_MATCH, false));
		hybridMatchCheckBox.setSelected(
				preferences.getBoolean(INCLUDE_HYBRID_MATCH, false));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		double mzMin = 0.0d;
		if(!bpmzMinTextField.getText().trim().isEmpty())
			mzMin = Double.parseDouble(bpmzMinTextField.getText().trim());

		preferences.putDouble(MZ_MIN, mzMin);
		
		double mzMax = 0.0d;
		if(!bpMzMaxTextField.getText().trim().isEmpty())
			mzMax = Double.parseDouble(bpMzMaxTextField.getText().trim());

		preferences.putDouble(MZ_MAX, mzMax);
		
		double mzBpPrecursor = 0.0d;
		if(!targetMassTextField.getText().trim().isEmpty())
			mzBpPrecursor = Double.parseDouble(targetMassTextField.getText().trim());

		preferences.putDouble(MZ_BP_PRECURSOR, mzBpPrecursor);
		
		double mzErrorValue = 0.0d;
		if(!massErrorTextField.getText().trim().isEmpty())
			mzErrorValue = Double.parseDouble(massErrorTextField.getText().trim());

		preferences.putDouble(MZ_ERROR_VALUE, mzErrorValue);
		
		if(massErrorTypeComboBox.getSelectedItem() != null)
			preferences.put(MZ_ERROR_TYPE, ((MassErrorType)massErrorTypeComboBox.getSelectedItem()).name());
		
		double rtMin = 0.0d;
		if(!rtFromTextField.getText().trim().isEmpty())
			rtMin = Double.parseDouble(rtFromTextField.getText().trim());
		
		preferences.putDouble(RT_MIN, rtMin);
		
		double rtMax = 0.0d;
		if(!rtToTextField.getText().trim().isEmpty())
			rtMax = Double.parseDouble(rtToTextField.getText().trim());
		
		preferences.putDouble(RT_MAX, rtMax);
		
		preferences.put(NAME_STRING, nameTextField.getText().trim());
		preferences.putBoolean(SEARCH_ALL_IDS, searchAllIDsCheckBox.isSelected());
		
		if(idStatusComboBox.getSelectedItem() != null)
			preferences.put(ID_STATUS, ((FeatureSubsetByIdentification)idStatusComboBox.getSelectedItem()).name());
		
		double seMin = 0.0d;
		if(!seMinTextField.getText().trim().isEmpty())
			seMin = Double.parseDouble(seMinTextField.getText().trim());
		
		preferences.putDouble(S_ENTROPY_MIN, seMin);
		
		double seMax = 0.0d;
		if(!seMaxTextField.getText().trim().isEmpty())
			seMax = Double.parseDouble(seMaxTextField.getText().trim());
		
		preferences.putDouble(S_ENTROPY_MAX, seMax);
		
		double paMin = 0.0d;
		if(!peakAreaMinTextField.getText().trim().isEmpty())
			paMin = Double.parseDouble(peakAreaMinTextField.getText().trim());
		
		preferences.putDouble(PEAK_AREA_MIN, paMin);
		
		double paMax = 0.0d;
		if(!peakAreaMaxTextField.getText().trim().isEmpty())
			paMax = Double.parseDouble(peakAreaMaxTextField.getText().trim());
		
		preferences.putDouble(PEAK_AREA_MAX, paMax);
		
		double minScore = 0.0d;
		if(!minScoreTextField.getText().trim().isEmpty())
			minScore = Double.parseDouble(minScoreTextField.getText().trim());
		
		preferences.putDouble(MIN_SCORE, minScore);
		
		preferences.putBoolean(INCLUDE_NORMAL_MATCH, regularMatchCheckBox.isSelected());
		preferences.putBoolean(INCLUDE_IN_SOURCE_MATCH, inSourceCheckBox.isSelected());
		preferences.putBoolean(INCLUDE_HYBRID_MATCH, hybridMatchCheckBox.isSelected());
	}
}















