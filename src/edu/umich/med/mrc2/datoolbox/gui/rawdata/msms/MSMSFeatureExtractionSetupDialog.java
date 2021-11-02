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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.msms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.IntensityMeasure;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSFeatureExtractionSetupDialog extends JDialog  implements ActionListener, BackedByPreferences{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1021679311401746260L;
	private static final Icon extractMSMSFeaturesIcon = GuiUtils.getIcon("findMSMSFeatures", 32);
	
	private Preferences preferences;
	private JCheckBox limitExtractionRtCheckBox;
	private JFormattedTextField rtFromTextField;
	private JFormattedTextField rtToTextField;
	private JFormattedTextField isolationWindowLowerTextField;
	private JFormattedTextField isolationWindowUpperTextField;
	private JComboBox intensityMeasureComboBox;
	private JButton importButton;
	private JButton cancelButton;
	private JCheckBox chckbxRemoveAllMassesAboveParent;
	private JCheckBox chckbxRemoveAllMassesBelowCounts;
	private JFormattedTextField minimalCountsTextField;
	private JCheckBox chckbxLeaveOnly;
	private JSpinner maxFragmentsSpinner;

	private static final NumberFormat twoDecFormat = new DecimalFormat("###.##");
	
	public static final String USE_RT_RANGE = "USE_RT_RANGE";
	public static final String RT_BORDER_LEFT_MIN = "RT_BORDER_LEFT_MIN";
	public static final String RT_BORDER_RIGHT_MIN = "RT_BORDER_RIGHT_MIN";
	public static final String ISOLATION_BORDER_MAX = "PRECURSOR_MASS_WINDOW";
	public static final String PRECURSOR_MASS_WINDOW_UNITS = "PRECURSOR_MASS_WINDOW_UNITS";
	public static final String ISOLATION_BORDER_MIN = "PRECURSOR_RT_WINDOW_MIN";	
	public static final String PRECURSOR_GROUPING_RT_WINDOW = "PRECURSOR_GROUPING_RT_WINDOW";
	public static final String PRECURSOR_GROUPING_MASS_ERROR = "PRECURSOR_GROUPING_MASS_ERROR";
	public static final String PRECURSOR_GROUPING_MASS_ERROR_TYPE = "PRECURSOR_GROUPING_MASS_ERROR_TYPE";	
	public static final String REMOVE_MASSES_ABOVE_PARENT = "REMOVE_MASSES_ABOVE_PARENT";
	public static final String REMOVE_MASSES_BELOW_COUNT = "REMOVE_MASSES_BELOW_COUNT";
	public static final String MINIMAL_COUNTS = "MINIMAL_COUNTS";
	public static final String LEAVE_MAX_FRAGMENTS = "LEAVE_MAX_FRAGMENTS";
	public static final String MAX_FRAGMENTS_COUNT = "MAX_FRAGMENTS_COUNT";
	public static final String INTENSITY_MEASURE = "INTENSITY_MEASURE";
	public static final String FLAG_MINOR_ISOTOPES_PRECURSORS = "FLAG_MINOR_ISOTOPES_PRECURSORS";
	public static final String MAX_PRECURSOR_CHARGE = "MAX_PRECURSOR_CHARGE";

	private JFormattedTextField msmsGroupRtWindowTextField;
	private JFormattedTextField precursorGroupingMassErrorTextField;
	private JComboBox precursorGroupingMassErrorTypeComboBox;
	private JCheckBox flagMinorIsotopesPrecursorsCheckBox;
	private JSpinner maxChargeSpinner;
	
	public MSMSFeatureExtractionSetupDialog(ActionListener listener) {
		super();
		setTitle("MSMS feature extraction settings");
		setIconImage(((ImageIcon) extractMSMSFeaturesIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(640, 450));
		setSize(new Dimension(640, 450));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		preferences = Preferences.userNodeForPackage(this.getClass());

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{277, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		limitExtractionRtCheckBox = new JCheckBox("Extract data only for retention time from ");
		GridBagConstraints gbc_limitExtractionRtCheckBox = new GridBagConstraints();
		gbc_limitExtractionRtCheckBox.anchor = GridBagConstraints.EAST;
		gbc_limitExtractionRtCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_limitExtractionRtCheckBox.gridx = 0;
		gbc_limitExtractionRtCheckBox.gridy = 0;
		panel_1.add(limitExtractionRtCheckBox, gbc_limitExtractionRtCheckBox);

		rtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtFromTextField.setColumns(10);
		GridBagConstraints gbc_rtFromTextField = new GridBagConstraints();
		gbc_rtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFromTextField.gridx = 1;
		gbc_rtFromTextField.gridy = 0;
		panel_1.add(rtFromTextField, gbc_rtFromTextField);

		JLabel lblTo = new JLabel(" to ");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.anchor = GridBagConstraints.EAST;
		gbc_lblTo.gridx = 2;
		gbc_lblTo.gridy = 0;
		panel_1.add(lblTo, gbc_lblTo);

		rtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtToTextField.setColumns(10);
		GridBagConstraints gbc_rtToTextField = new GridBagConstraints();
		gbc_rtToTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtToTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtToTextField.gridx = 3;
		gbc_rtToTextField.gridy = 0;
		panel_1.add(rtToTextField, gbc_rtToTextField);

		JLabel lblMin = new JLabel("min");
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.insets = new Insets(0, 0, 5, 0);
		gbc_lblMin.anchor = GridBagConstraints.WEST;
		gbc_lblMin.gridx = 4;
		gbc_lblMin.gridy = 0;
		panel_1.add(lblMin, gbc_lblMin);

		JLabel lblRtAlignmentWindow = new JLabel("Precursor isolation window (if not in data)");
		GridBagConstraints gbc_lblRtAlignmentWindow = new GridBagConstraints();
		gbc_lblRtAlignmentWindow.anchor = GridBagConstraints.EAST;
		gbc_lblRtAlignmentWindow.insets = new Insets(0, 0, 5, 5);
		gbc_lblRtAlignmentWindow.gridx = 0;
		gbc_lblRtAlignmentWindow.gridy = 1;
		panel_1.add(lblRtAlignmentWindow, gbc_lblRtAlignmentWindow);
		
		JLabel lblNewLabel_4 = new JLabel("Below precursor M/Z");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 2;
		panel_1.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		isolationWindowLowerTextField = 
				new JFormattedTextField(twoDecFormat);
		isolationWindowLowerTextField.setColumns(10);
		GridBagConstraints gbc_precursorRtAlignTextField = new GridBagConstraints();
		gbc_precursorRtAlignTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precursorRtAlignTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorRtAlignTextField.gridx = 1;
		gbc_precursorRtAlignTextField.gridy = 2;
		panel_1.add(isolationWindowLowerTextField, gbc_precursorRtAlignTextField);
		
		JLabel lblNewLabel_3 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 2;
		gbc_lblNewLabel_3.gridy = 2;
		panel_1.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		JLabel lblNewLabel_5 = new JLabel("Above precursor M/Z");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 3;
		panel_1.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		isolationWindowUpperTextField = 
				new JFormattedTextField(twoDecFormat);
		isolationWindowUpperTextField.setColumns(10);
		GridBagConstraints gbc_precursorMzAlignTextField = new GridBagConstraints();
		gbc_precursorMzAlignTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precursorMzAlignTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorMzAlignTextField.gridx = 1;
		gbc_precursorMzAlignTextField.gridy = 3;
		panel_1.add(isolationWindowUpperTextField, gbc_precursorMzAlignTextField);
		
		JLabel lblNewLabel_6 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 2;
		gbc_lblNewLabel_6.gridy = 3;
		panel_1.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new CompoundBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "MSMS grouping", 
				TitledBorder.LEADING, TitledBorder.TOP, null, 
				new Color(0, 0, 0)), new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.gridwidth = 5;
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 4;
		panel_1.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		JLabel lblNewLabel = new JLabel("Group MSMS if within RT window of ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_3.add(lblNewLabel, gbc_lblNewLabel);
		
		msmsGroupRtWindowTextField = new JFormattedTextField((Format) null);
		msmsGroupRtWindowTextField.setText("0.0");
		msmsGroupRtWindowTextField.setColumns(10);
		GridBagConstraints gbc_msmsGroupRtWindowTextField = new GridBagConstraints();
		gbc_msmsGroupRtWindowTextField.insets = new Insets(0, 0, 5, 5);
		gbc_msmsGroupRtWindowTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_msmsGroupRtWindowTextField.gridx = 1;
		gbc_msmsGroupRtWindowTextField.gridy = 0;
		panel_3.add(msmsGroupRtWindowTextField, gbc_msmsGroupRtWindowTextField);
		
		JLabel lblNewLabel_1 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel_3.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Precursor mass tolerance");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel_3.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		precursorGroupingMassErrorTextField = new JFormattedTextField((Format) null);
		precursorGroupingMassErrorTextField.setText("0.65");
		precursorGroupingMassErrorTextField.setColumns(10);
		GridBagConstraints gbc_precursorMassToleranceTextField = new GridBagConstraints();
		gbc_precursorMassToleranceTextField.insets = new Insets(0, 0, 0, 5);
		gbc_precursorMassToleranceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorMassToleranceTextField.gridx = 1;
		gbc_precursorMassToleranceTextField.gridy = 1;
		panel_3.add(precursorGroupingMassErrorTextField, gbc_precursorMassToleranceTextField);
		
		precursorGroupingMassErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		precursorGroupingMassErrorTypeComboBox.setSize(new Dimension(80, 22));
		precursorGroupingMassErrorTypeComboBox.setPreferredSize(new Dimension(80, 22));
		GridBagConstraints gbc_precursorMassErrorTypeComboBox = new GridBagConstraints();
		gbc_precursorMassErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorMassErrorTypeComboBox.gridx = 2;
		gbc_precursorMassErrorTypeComboBox.gridy = 1;
		panel_3.add(precursorGroupingMassErrorTypeComboBox, gbc_precursorMassErrorTypeComboBox);

		//		MSMS filtering
		JPanel panel = new JPanel();
		panel.setBorder(
				new CompoundBorder(new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
								new Color(160, 160, 160)), "MSMS filtering", 
						TitledBorder.LEADING, TitledBorder.TOP, null, 
						new Color(0, 0, 0)), new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridwidth = 5;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 5;
		panel_1.add(panel, gbc_panel);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 87, 87, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		chckbxRemoveAllMassesAboveParent = 
				new JCheckBox("Remove all masses above parent ion");
		GridBagConstraints gbc_chckbxRemoveAllMassesAboveParent = new GridBagConstraints();
		gbc_chckbxRemoveAllMassesAboveParent.anchor = GridBagConstraints.WEST;
		gbc_chckbxRemoveAllMassesAboveParent.gridwidth = 3;
		gbc_chckbxRemoveAllMassesAboveParent.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRemoveAllMassesAboveParent.gridx = 0;
		gbc_chckbxRemoveAllMassesAboveParent.gridy = 0;
		panel.add(chckbxRemoveAllMassesAboveParent, gbc_chckbxRemoveAllMassesAboveParent);

		chckbxRemoveAllMassesBelowCounts = 
				new JCheckBox("Remove all masses below ");
		GridBagConstraints gbc_chckbxRemoveAllMasses = new GridBagConstraints();
		gbc_chckbxRemoveAllMasses.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRemoveAllMasses.anchor = GridBagConstraints.WEST;
		gbc_chckbxRemoveAllMasses.gridx = 0;
		gbc_chckbxRemoveAllMasses.gridy = 1;
		panel.add(chckbxRemoveAllMassesBelowCounts, gbc_chckbxRemoveAllMasses);
				
		intensityMeasureComboBox = new JComboBox<IntensityMeasure>(
				new DefaultComboBoxModel<IntensityMeasure>(IntensityMeasure.values()));
		GridBagConstraints gbc_intensityMeasureComboBox = new GridBagConstraints();
		gbc_intensityMeasureComboBox.gridwidth = 2;
		gbc_intensityMeasureComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_intensityMeasureComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_intensityMeasureComboBox.gridx = 1;
		gbc_intensityMeasureComboBox.gridy = 1;
		panel.add(intensityMeasureComboBox, gbc_intensityMeasureComboBox);

		minimalCountsTextField = new JFormattedTextField(twoDecFormat);
		minimalCountsTextField.setSize(new Dimension(80, 20));
		minimalCountsTextField.setPreferredSize(new Dimension(80, 20));
		minimalCountsTextField.setColumns(9);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.anchor = GridBagConstraints.WEST;
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.gridx = 3;
		gbc_formattedTextField.gridy = 1;
		panel.add(minimalCountsTextField, gbc_formattedTextField);

		chckbxLeaveOnly = new JCheckBox("Leave only");
		GridBagConstraints gbc_chckbxLeaveOnly = new GridBagConstraints();
		gbc_chckbxLeaveOnly.anchor = GridBagConstraints.EAST;
		gbc_chckbxLeaveOnly.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxLeaveOnly.gridx = 0;
		gbc_chckbxLeaveOnly.gridy = 2;
		panel.add(chckbxLeaveOnly, gbc_chckbxLeaveOnly);

		maxFragmentsSpinner = new JSpinner();
		maxFragmentsSpinner.setModel(new SpinnerNumberModel(20, 1, null, 1));
		maxFragmentsSpinner.setPreferredSize(new Dimension(80, 20));
		maxFragmentsSpinner.setSize(new Dimension(80, 20));
		GridBagConstraints gbc_maxFragmentsSpinner = new GridBagConstraints();
		gbc_maxFragmentsSpinner.anchor = GridBagConstraints.WEST;
		gbc_maxFragmentsSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_maxFragmentsSpinner.gridx = 1;
		gbc_maxFragmentsSpinner.gridy = 2;
		panel.add(maxFragmentsSpinner, gbc_maxFragmentsSpinner);

		JLabel lblMajorFragments = new JLabel("major fragments");
		GridBagConstraints gbc_lblMajorFragments = new GridBagConstraints();
		gbc_lblMajorFragments.gridwidth = 2;
		gbc_lblMajorFragments.anchor = GridBagConstraints.WEST;
		gbc_lblMajorFragments.gridx = 2;
		gbc_lblMajorFragments.gridy = 2;
		panel.add(lblMajorFragments, gbc_lblMajorFragments);
		
		flagMinorIsotopesPrecursorsCheckBox = 
				new JCheckBox("Flag MSMS features where precursor is not monoisotopic peak");
		GridBagConstraints gbc_flagMinorIsotopesPrecursorsCheckBox = new GridBagConstraints();
		gbc_flagMinorIsotopesPrecursorsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_flagMinorIsotopesPrecursorsCheckBox.gridwidth = 2;
		gbc_flagMinorIsotopesPrecursorsCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_flagMinorIsotopesPrecursorsCheckBox.gridx = 0;
		gbc_flagMinorIsotopesPrecursorsCheckBox.gridy = 6;
		panel_1.add(flagMinorIsotopesPrecursorsCheckBox, gbc_flagMinorIsotopesPrecursorsCheckBox);
		
		JLabel lblNewLabel_7 = new JLabel("Max. charge");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_7.gridx = 3;
		gbc_lblNewLabel_7.gridy = 6;
		panel_1.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		maxChargeSpinner = new JSpinner();
		maxChargeSpinner.setModel(new SpinnerNumberModel(2, 1, 3, 1));
		GridBagConstraints gbc_maxChargeSpinner = new GridBagConstraints();
		gbc_maxChargeSpinner.anchor = GridBagConstraints.WEST;
		gbc_maxChargeSpinner.gridx = 4;
		gbc_maxChargeSpinner.gridy = 6;
		panel_1.add(maxChargeSpinner, gbc_maxChargeSpinner);

		//	Buttons
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_2, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(al);
		panel_2.add(cancelButton);
		importButton = new JButton(MainActionCommands.MSMS_FEATURE_EXTRACTION_COMMAND.getName());
		importButton.setActionCommand(MainActionCommands.MSMS_FEATURE_EXTRACTION_COMMAND.getName());
		importButton.addActionListener(listener);
		panel_2.add(importButton);	
		JRootPane rootPane = SwingUtilities.getRootPane(importButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(importButton);
		loadPreferences();
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
//		if(e.getActionCommand().equals(MainActionCommands.IDDA_IMPORT_COMMAND.getName())) {
//
//		}
	}

	public Range getDataExtractionRtRange() {
		
		double fromRt = Double.valueOf(rtFromTextField.getText());
		double toRt = Double.valueOf(rtToTextField.getText());
		if(limitExtractionRtCheckBox.isSelected() && toRt > 0 && toRt > fromRt)
			return new Range(fromRt, toRt);
		else
			return null;
	}
	
	public double getIsolationWindowLowerBorder() {
		return Double.valueOf(isolationWindowLowerTextField.getText());
	}

	public double getIsolationWindowUpperBorder() {
		return Double.valueOf(isolationWindowUpperTextField.getText());
	}
	
	public double getMsmsGroupingRtWindow() {
		return Double.valueOf(msmsGroupRtWindowTextField.getText());
	}
	
	public double getPrecursorGroupingMassError() {
		return Double.valueOf(precursorGroupingMassErrorTextField.getText());
	}
	
	public MassErrorType getPrecursorGroupingMassErrorType() {
		return (MassErrorType) precursorGroupingMassErrorTypeComboBox.getSelectedItem();
	}

	public IntensityMeasure getFilterIntensityMeasure() {
		return (IntensityMeasure) intensityMeasureComboBox.getSelectedItem();
	}

	public boolean removeAllMassesAboveParent() {
		return chckbxRemoveAllMassesAboveParent.isSelected();
	}

	public double getMsMsCountsCutoff() {
		if(!chckbxRemoveAllMassesBelowCounts.isSelected())
			return 0.0d;
		else
			return Double.parseDouble(minimalCountsTextField.getText());
	}

	public int getMaxFragmentsCutoff() {

		if(!chckbxLeaveOnly.isSelected())
			return -1;
		else
			return (int) maxFragmentsSpinner.getValue();
	}
	
	public IntensityMeasure getIntensityMeasure() {
		return (IntensityMeasure)intensityMeasureComboBox.getSelectedItem();
	}
	
	public boolean flagMinorIsotopesPrecursors() {
		return flagMinorIsotopesPrecursorsCheckBox.isSelected();
	}
	
	public int getMaxPrecursorCharge() {
		return (int)maxChargeSpinner.getValue();
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		limitExtractionRtCheckBox.setSelected(preferences.getBoolean(USE_RT_RANGE, false));
		rtFromTextField.setText(Double.toString(preferences.getDouble(RT_BORDER_LEFT_MIN, 0.0d)));
		rtToTextField.setText(Double.toString(preferences.getDouble(RT_BORDER_RIGHT_MIN, 0.0d)));
		isolationWindowLowerTextField.setText(Double.toString(preferences.getDouble(ISOLATION_BORDER_MIN, 0.65d)));
		isolationWindowUpperTextField.setText(Double.toString(preferences.getDouble(ISOLATION_BORDER_MAX, 0.65d)));		
		msmsGroupRtWindowTextField.setText(Double.toString(preferences.getDouble(PRECURSOR_GROUPING_RT_WINDOW, 0.1d)));
		precursorGroupingMassErrorTextField.setText(Double.toString(preferences.getDouble(PRECURSOR_GROUPING_MASS_ERROR, 15.0d)));
		precursorGroupingMassErrorTypeComboBox.setSelectedItem(
				MassErrorType.getTypeByName(
						preferences.get(PRECURSOR_GROUPING_MASS_ERROR_TYPE, MassErrorType.ppm.name())));			
		chckbxRemoveAllMassesAboveParent.setSelected(preferences.getBoolean(REMOVE_MASSES_ABOVE_PARENT, true));
		chckbxRemoveAllMassesBelowCounts.setSelected(preferences.getBoolean(REMOVE_MASSES_BELOW_COUNT, false));
		minimalCountsTextField.setText(Double.toString(preferences.getDouble(MINIMAL_COUNTS, 0.0d)));
		chckbxLeaveOnly.setSelected(preferences.getBoolean(LEAVE_MAX_FRAGMENTS, false));
		maxFragmentsSpinner.setValue(preferences.getInt(MAX_FRAGMENTS_COUNT, 30));
		intensityMeasureComboBox.setSelectedItem(
				IntensityMeasure.geIntensityMeasureByName(
						preferences.get(INTENSITY_MEASURE, IntensityMeasure.ABSOLUTE.name())));	
		flagMinorIsotopesPrecursorsCheckBox.setSelected(preferences.getBoolean(FLAG_MINOR_ISOTOPES_PRECURSORS, true));	
		maxChargeSpinner.setValue(preferences.getInt(MAX_PRECURSOR_CHARGE, 2));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.putBoolean(USE_RT_RANGE, limitExtractionRtCheckBox.isSelected());
		preferences.putDouble(RT_BORDER_LEFT_MIN, Double.valueOf(rtFromTextField.getText()));
		preferences.putDouble(RT_BORDER_RIGHT_MIN, Double.valueOf(rtToTextField.getText()));
		preferences.putDouble(ISOLATION_BORDER_MIN, getIsolationWindowLowerBorder());
		preferences.putDouble(ISOLATION_BORDER_MAX, getIsolationWindowUpperBorder());		
		preferences.putDouble(PRECURSOR_GROUPING_RT_WINDOW, getMsmsGroupingRtWindow());
		preferences.putDouble(PRECURSOR_GROUPING_MASS_ERROR, getPrecursorGroupingMassError());
		preferences.put(PRECURSOR_GROUPING_MASS_ERROR_TYPE, getPrecursorGroupingMassErrorType().name());		
		preferences.put(PRECURSOR_MASS_WINDOW_UNITS, getFilterIntensityMeasure().name());
		preferences.putBoolean(REMOVE_MASSES_ABOVE_PARENT, removeAllMassesAboveParent());
		preferences.putBoolean(REMOVE_MASSES_BELOW_COUNT, chckbxRemoveAllMassesBelowCounts.isSelected());
		preferences.putDouble(MINIMAL_COUNTS, Double.parseDouble(minimalCountsTextField.getText()));
		preferences.putBoolean(LEAVE_MAX_FRAGMENTS, chckbxLeaveOnly.isSelected());
		preferences.putInt(MAX_FRAGMENTS_COUNT, (int) maxFragmentsSpinner.getValue());		
		preferences.put(INTENSITY_MEASURE, getIntensityMeasure().name());		
		preferences.putBoolean(FLAG_MINOR_ISOTOPES_PRECURSORS, flagMinorIsotopesPrecursorsCheckBox.isSelected());		
		preferences.putInt(MAX_PRECURSOR_CHARGE, (int) maxChargeSpinner.getValue());
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void dispose() {
		savePreferences();
		super.dispose();	
	}
	
	public Collection<String>validateParameters(){
		
		Collection<String>errors = new ArrayList<String>();
		if(limitExtractionRtCheckBox.isSelected()) {
			double lb = Double.valueOf(rtFromTextField.getText());
			double rb = Double.valueOf(rtToTextField.getText());
			if(lb >= rb)
				errors.add("Specified retention time range not valid");		
		}
		if(getFilterIntensityMeasure().equals(IntensityMeasure.RELATIVE)) {
			
			double minCounts = getMsMsCountsCutoff();
			if(minCounts > 100.0d || minCounts < 0.0d)
				errors.add("Relative intensity cutoff should be between 0 and 100");
		}
		return errors;
	}
}












