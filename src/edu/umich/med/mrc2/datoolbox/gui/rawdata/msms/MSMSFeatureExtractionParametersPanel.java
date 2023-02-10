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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.enums.IntensityMeasure;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsFeatureChromatogramExtractionTarget;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayWidth;

public class MSMSFeatureExtractionParametersPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1219847398047946074L;

	private static final NumberFormat twoDecFormat = new DecimalFormat("###.##");

	private static final String DESCRIPTION_DEFAULT = "MSMS extraction method " + 
			MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(new Date());
	private static final Polarity POLARITY_DEFAULT = Polarity.Positive;
	private static final double MIN_PRECURSOR_INTENSITY_DEFAULT = 0.0d;
	private static final boolean USE_RT_RANGE_DEFAULT = false;
	private static final double RT_BORDER_LEFT_DEFAULT = 0.0d;
	private static final double RT_BORDER_RIGHT_DEFAULT = 0.0d;
	private static final double ISOLATION_BORDER_MIN_DEFAULT = 0.65d;	
	private static final double ISOLATION_BORDER_MAX_DEFAULT = 0.65d;	
	private static final double PRECURSOR_GROUPING_RT_WINDOW_DEFAULT = 0.1d;
	private static final double PRECURSOR_GROUPING_MASS_ERROR_DEFAULT = 15.0d;
	private static final MassErrorType PRECURSOR_GROUPING_MASS_ERROR_TYPE_DEFAULT = MassErrorType.ppm;		
	private static final boolean REMOVE_MASSES_ABOVE_PARENT_DEFAULT = true;
	private static final boolean REMOVE_MASSES_BELOW_COUNT_DEFAULT = false;
	private static final double MINIMAL_COUNTS_DEFAULT = 0.0d;
	private static final boolean LEAVE_MAX_FRAGMENTS_DEFAULT = false;
	private static final int MAX_FRAGMENTS_COUNT_DEFAULT = 30;	
	private static final IntensityMeasure INTENSITY_MEASURE_DEFAULT = IntensityMeasure.ABSOLUTE;
	private static final boolean MERGE_COLLISION_ENERGIES_DEFAULT = false;
	private static final double SPECTRUM_SIMILARITY_CUTOFF_DEFAULT = 0.7d;	
	private static final boolean FLAG_MINOR_ISOTOPES_PRECURSORS_DEFAULT = true;	
	private static final int MAX_PRECURSOR_CHARGE_DEFAULT = 2;
	private static final SavitzkyGolayWidth FILTER_WIDTH_DEFAULT = SavitzkyGolayWidth.NINE;
	private static final double CHROMATOGRAM_WIDTH_DEFAULT = 0.3d;
	
	private JCheckBox chckbxLeaveOnly;
	private JCheckBox chckbxRemoveAllMassesAboveParent;
	private JCheckBox chckbxRemoveAllMassesBelowCounts;
	private JCheckBox flagMinorIsotopesPrecursorsCheckBox;
	private JCheckBox limitExtractionRtCheckBox;
	private JCheckBox mergeCollisionsCheckBox;
	private JComboBox filterWidthComboBox;
	private JComboBox intensityMeasureComboBox;
	private JComboBox polarityComboBox;
	private JComboBox precursorGroupingMassErrorTypeComboBox;
	private JFormattedTextField isolationWindowLowerTextField;
	private JFormattedTextField isolationWindowUpperTextField;
	private JFormattedTextField minPrecursorIntensityTextField;
	private JFormattedTextField minimalCountsTextField;
	private JFormattedTextField msmsGroupRtWindowTextField;
	private JFormattedTextField precursorGroupingMassErrorTextField;
	private JFormattedTextField rtFromTextField;
	private JFormattedTextField rtToTextField;
	private JFormattedTextField xicWindowTextField;
	private JFormattedTextField specSimCutoffTextField;
	private JSpinner maxChargeSpinner;
	private JSpinner maxFragmentsSpinner;
	private JTextArea descriptionTextArea;
	private JTextField nameTextField;
	private JLabel methodIdLabel;
	
	public MSMSFeatureExtractionParametersPanel() {
		super();

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{176, 68, 0, 78, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gbl_panel);
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JLabel lblNewLabel_14 = new JLabel("Parameter set name");
		GridBagConstraints gbc_lblNewLabel_14 = new GridBagConstraints();
		gbc_lblNewLabel_14.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_14.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_14.gridx = 0;
		gbc_lblNewLabel_14.gridy = 0;
		add(lblNewLabel_14, gbc_lblNewLabel_14);
		
		JLabel lblNewLabel_16 = new JLabel("Method ID:");
		GridBagConstraints gbc_lblNewLabel_16 = new GridBagConstraints();
		gbc_lblNewLabel_16.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_16.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_16.gridx = 3;
		gbc_lblNewLabel_16.gridy = 0;
		add(lblNewLabel_16, gbc_lblNewLabel_16);
		
		methodIdLabel = new JLabel("");
		methodIdLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_methodIdLabel = new GridBagConstraints();
		gbc_methodIdLabel.anchor = GridBagConstraints.WEST;
		gbc_methodIdLabel.insets = new Insets(0, 0, 5, 0);
		gbc_methodIdLabel.gridx = 4;
		gbc_methodIdLabel.gridy = 0;
		add(methodIdLabel, gbc_methodIdLabel);
		
		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 5;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 0;
		gbc_nameTextField.gridy = 1;
		add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		JLabel lblNewLabel_13 = new JLabel("Description ");
		GridBagConstraints gbc_lblNewLabel_13 = new GridBagConstraints();
		gbc_lblNewLabel_13.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel_13.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_13.gridx = 0;
		gbc_lblNewLabel_13.gridy = 2;
		add(lblNewLabel_13, gbc_lblNewLabel_13);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 5;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 3;
		add(descriptionTextArea, gbc_textArea);
				
		JLabel lblNewLabel_11 = new JLabel("Polarity");
		GridBagConstraints gbc_lblNewLabel_11 = new GridBagConstraints();
		gbc_lblNewLabel_11.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_11.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_11.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_11.gridx = 0;
		gbc_lblNewLabel_11.gridy = 4;
		add(lblNewLabel_11, gbc_lblNewLabel_11);
		
		polarityComboBox = new JComboBox<Polarity>(
				new DefaultComboBoxModel<Polarity>(
						new Polarity[] {Polarity.Positive, Polarity.Negative}));
		GridBagConstraints gbc_polarityComboBox = new GridBagConstraints();
		gbc_polarityComboBox.gridwidth = 3;
		gbc_polarityComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_polarityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_polarityComboBox.gridx = 1;
		gbc_polarityComboBox.gridy = 4;
		add(polarityComboBox, gbc_polarityComboBox);

		limitExtractionRtCheckBox = new JCheckBox("Extract data only for retention time from");
		GridBagConstraints gbc_limitExtractionRtCheckBox = new GridBagConstraints();
		gbc_limitExtractionRtCheckBox.anchor = GridBagConstraints.EAST;
		gbc_limitExtractionRtCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_limitExtractionRtCheckBox.gridx = 0;
		gbc_limitExtractionRtCheckBox.gridy = 5;
		add(limitExtractionRtCheckBox, gbc_limitExtractionRtCheckBox);

		rtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtFromTextField.setColumns(10);
		GridBagConstraints gbc_rtFromTextField = new GridBagConstraints();
		gbc_rtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFromTextField.gridx = 1;
		gbc_rtFromTextField.gridy = 5;
		add(rtFromTextField, gbc_rtFromTextField);
		
		JLabel lblTo = new JLabel(" to ");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.anchor = GridBagConstraints.EAST;
		gbc_lblTo.gridx = 2;
		gbc_lblTo.gridy = 5;
		add(lblTo, gbc_lblTo);

		rtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtToTextField.setColumns(10);
		GridBagConstraints gbc_rtToTextField = new GridBagConstraints();
		gbc_rtToTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtToTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtToTextField.gridx = 3;
		gbc_rtToTextField.gridy = 5;
		add(rtToTextField, gbc_rtToTextField);

		JLabel lblMin = new JLabel("min");
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.insets = new Insets(0, 0, 5, 0);
		gbc_lblMin.anchor = GridBagConstraints.WEST;
		gbc_lblMin.gridx = 4;
		gbc_lblMin.gridy = 5;
		add(lblMin, gbc_lblMin);
		
		JLabel lblNewLabel_12 = new JLabel("Minimal precursor intensity");
		GridBagConstraints gbc_lblNewLabel_12 = new GridBagConstraints();
		gbc_lblNewLabel_12.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_12.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_12.gridx = 0;
		gbc_lblNewLabel_12.gridy = 6;
		add(lblNewLabel_12, gbc_lblNewLabel_12);
		
		minPrecursorIntensityTextField = 
				new JFormattedTextField(new DecimalFormat("###"));
		GridBagConstraints gbc_minPrecursorIntensityTextField = new GridBagConstraints();
		gbc_minPrecursorIntensityTextField.insets = new Insets(0, 0, 5, 5);
		gbc_minPrecursorIntensityTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minPrecursorIntensityTextField.gridx = 1;
		gbc_minPrecursorIntensityTextField.gridy = 6;
		add(minPrecursorIntensityTextField, gbc_minPrecursorIntensityTextField);

		JLabel lblRtAlignmentWindow = new JLabel("Precursor isolation window (if not in data)");
		GridBagConstraints gbc_lblRtAlignmentWindow = new GridBagConstraints();
		gbc_lblRtAlignmentWindow.anchor = GridBagConstraints.EAST;
		gbc_lblRtAlignmentWindow.insets = new Insets(0, 0, 5, 5);
		gbc_lblRtAlignmentWindow.gridx = 0;
		gbc_lblRtAlignmentWindow.gridy = 7;
		add(lblRtAlignmentWindow, gbc_lblRtAlignmentWindow);
		
		JLabel lblNewLabel_4 = new JLabel("Below precursor M/Z");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 8;
		add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		isolationWindowLowerTextField = 
				new JFormattedTextField(twoDecFormat);
		isolationWindowLowerTextField.setColumns(10);
		GridBagConstraints gbc_precursorRtAlignTextField = new GridBagConstraints();
		gbc_precursorRtAlignTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precursorRtAlignTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorRtAlignTextField.gridx = 1;
		gbc_precursorRtAlignTextField.gridy = 8;
		add(isolationWindowLowerTextField, gbc_precursorRtAlignTextField);
		
		JLabel lblNewLabel_3 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 2;
		gbc_lblNewLabel_3.gridy = 8;
		add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		JLabel lblNewLabel_5 = new JLabel("Above precursor M/Z");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 9;
		add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		isolationWindowUpperTextField = 
				new JFormattedTextField(twoDecFormat);
		isolationWindowUpperTextField.setColumns(10);
		GridBagConstraints gbc_precursorMzAlignTextField = new GridBagConstraints();
		gbc_precursorMzAlignTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precursorMzAlignTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorMzAlignTextField.gridx = 1;
		gbc_precursorMzAlignTextField.gridy = 9;
		add(isolationWindowUpperTextField, gbc_precursorMzAlignTextField);
		
		JLabel lblNewLabel_6 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 2;
		gbc_lblNewLabel_6.gridy = 9;
		add(lblNewLabel_6, gbc_lblNewLabel_6);
		
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
		gbc_panel_3.gridy = 10;
		add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 87, 0, 0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		JLabel lblNewLabel = new JLabel("Group MSMS if within RT window of ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_3.add(lblNewLabel, gbc_lblNewLabel);
		
		msmsGroupRtWindowTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
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
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel_3.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Precursor mass tolerance");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel_3.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		precursorGroupingMassErrorTextField = new JFormattedTextField(twoDecFormat);
		precursorGroupingMassErrorTextField.setColumns(10);
		GridBagConstraints gbc_precursorMassToleranceTextField = new GridBagConstraints();
		gbc_precursorMassToleranceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precursorMassToleranceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorMassToleranceTextField.gridx = 1;
		gbc_precursorMassToleranceTextField.gridy = 1;
		panel_3.add(precursorGroupingMassErrorTextField, gbc_precursorMassToleranceTextField);
		
		precursorGroupingMassErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		precursorGroupingMassErrorTypeComboBox.setSize(new Dimension(80, 22));
		precursorGroupingMassErrorTypeComboBox.setPreferredSize(new Dimension(80, 22));
		GridBagConstraints gbc_precursorMassErrorTypeComboBox = new GridBagConstraints();
		gbc_precursorMassErrorTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_precursorMassErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorMassErrorTypeComboBox.gridx = 2;
		gbc_precursorMassErrorTypeComboBox.gridy = 1;
		panel_3.add(precursorGroupingMassErrorTypeComboBox, gbc_precursorMassErrorTypeComboBox);
		
		JLabel lblNewLabel_15 = new JLabel("Spectrum similarity cutoff ");
		GridBagConstraints gbc_lblNewLabel_15 = new GridBagConstraints();
		gbc_lblNewLabel_15.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_15.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_15.gridx = 0;
		gbc_lblNewLabel_15.gridy = 2;
		panel_3.add(lblNewLabel_15, gbc_lblNewLabel_15);
		
		specSimCutoffTextField = new JFormattedTextField(twoDecFormat);
		specSimCutoffTextField.setColumns(10);
		GridBagConstraints gbc_specSimCutoffTextField = new GridBagConstraints();
		gbc_specSimCutoffTextField.insets = new Insets(0, 0, 0, 5);
		gbc_specSimCutoffTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_specSimCutoffTextField.gridx = 1;
		gbc_specSimCutoffTextField.gridy = 2;
		panel_3.add(specSimCutoffTextField, gbc_specSimCutoffTextField);

		mergeCollisionsCheckBox = new JCheckBox("Merge collision energies");
		GridBagConstraints gbc_mergeCollisionsCheckBox = new GridBagConstraints();
		gbc_mergeCollisionsCheckBox.gridx = 3;
		gbc_mergeCollisionsCheckBox.gridy = 2;
		panel_3.add(mergeCollisionsCheckBox, gbc_mergeCollisionsCheckBox);

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
		gbc_panel.gridy = 11;
		add(panel, gbc_panel);

		GridBagLayout gbl_panel_9 = new GridBagLayout();
		gbl_panel_9.columnWidths = new int[]{0, 87, 87, 0, 0, 0};
		gbl_panel_9.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_9.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_9.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel_9);

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
		GridBagConstraints gbc_minimalCountsTextField = new GridBagConstraints();
		gbc_minimalCountsTextField.anchor = GridBagConstraints.WEST;
		gbc_minimalCountsTextField.insets = new Insets(0, 0, 5, 5);
		gbc_minimalCountsTextField.gridx = 3;
		gbc_minimalCountsTextField.gridy = 1;
		panel.add(minimalCountsTextField, gbc_minimalCountsTextField);

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
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, 
						new Color(255, 255, 255), new Color(160, 160, 160)), 
						"MS1 pattern extraction", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
				new EmptyBorder(10, 0, 10, 0)));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.gridwidth = 5;
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 12;
		add(panel_4, gbc_panel_4);
		
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel_4.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_4.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);
		
		flagMinorIsotopesPrecursorsCheckBox = 
				new JCheckBox("Flag MSMS features where precursor is not monoisotopic peak");
		GridBagConstraints gbc_flagMinorIsotopesPrecursorsCheckBox = new GridBagConstraints();
		gbc_flagMinorIsotopesPrecursorsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_flagMinorIsotopesPrecursorsCheckBox.gridx = 0;
		gbc_flagMinorIsotopesPrecursorsCheckBox.gridy = 0;
		panel_4.add(flagMinorIsotopesPrecursorsCheckBox, gbc_flagMinorIsotopesPrecursorsCheckBox);
		
		JLabel lblNewLabel_7 = new JLabel("Max. charge");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_7.gridx = 2;
		gbc_lblNewLabel_7.gridy = 0;
		panel_4.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		maxChargeSpinner = new JSpinner();
		GridBagConstraints gbc_maxChargeSpinner = new GridBagConstraints();
		gbc_maxChargeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_maxChargeSpinner.gridx = 3;
		gbc_maxChargeSpinner.gridy = 0;
		panel_4.add(maxChargeSpinner, gbc_maxChargeSpinner);
		maxChargeSpinner.setModel(new SpinnerNumberModel(2, 1, 3, 1));
		
		JLabel lblNewLabel_9 = new JLabel("Chromatogram extraction window");
		GridBagConstraints gbc_lblNewLabel_9 = new GridBagConstraints();
		gbc_lblNewLabel_9.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_9.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_9.gridx = 0;
		gbc_lblNewLabel_9.gridy = 1;
		panel_4.add(lblNewLabel_9, gbc_lblNewLabel_9);
		
		xicWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		GridBagConstraints gbc_xicWindowTextField = new GridBagConstraints();
		gbc_xicWindowTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_xicWindowTextField.gridwidth = 2;
		gbc_xicWindowTextField.insets = new Insets(0, 0, 5, 5);
		gbc_xicWindowTextField.gridx = 1;
		gbc_xicWindowTextField.gridy = 1;
		panel_4.add(xicWindowTextField, gbc_xicWindowTextField);
		
		JLabel lblNewLabel_10 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_10 = new GridBagConstraints();
		gbc_lblNewLabel_10.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_10.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_10.gridx = 3;
		gbc_lblNewLabel_10.gridy = 1;
		panel_4.add(lblNewLabel_10, gbc_lblNewLabel_10);
		
		JLabel lblNewLabel_8 = new JLabel("Smoothing filter width");
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_8.gridx = 0;
		gbc_lblNewLabel_8.gridy = 2;
		panel_4.add(lblNewLabel_8, gbc_lblNewLabel_8);
		
		filterWidthComboBox = new JComboBox<SavitzkyGolayWidth>(
				new DefaultComboBoxModel<SavitzkyGolayWidth>(SavitzkyGolayWidth.values()));
		GridBagConstraints gbc_filterWidthComboBox = new GridBagConstraints();
		gbc_filterWidthComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_filterWidthComboBox.gridwidth = 2;
		gbc_filterWidthComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_filterWidthComboBox.gridx = 1;
		gbc_filterWidthComboBox.gridy = 2;
		panel_4.add(filterWidthComboBox, gbc_filterWidthComboBox);
		
		disableMS1parameters();
	}
	
	private void disableMS1parameters() {
		
		flagMinorIsotopesPrecursorsCheckBox.setEnabled(false);
		maxChargeSpinner.setEnabled(false);
//		xicWindowTextField.setEnabled(false);
//		filterWidthComboBox.setEnabled(false);		
	}
	
	public String getParameterSetName() {
		return nameTextField.getText().trim();
	}
	
	public String getDescription() {
		return descriptionTextArea.getText().trim();
	}

	public Range getDataExtractionRtRange() {
		
		double fromRt = Double.valueOf(rtFromTextField.getText());
		double toRt = Double.valueOf(rtToTextField.getText());
		if(limitExtractionRtCheckBox.isSelected() && toRt > 0 && toRt > fromRt)
			return new Range(fromRt, toRt);
		else
			return null;
	}
	
	public Polarity getPolarity() {
		return (Polarity)polarityComboBox.getSelectedItem();
	}
	
	public double getMinimalPrecursorIntensity() {
		if(minPrecursorIntensityTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.valueOf(minPrecursorIntensityTextField.getText());
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
	
	public double getSpectrumSimilarityCutoff() {
		return Double.parseDouble(specSimCutoffTextField.getText());
	}
	
	public boolean mergeCollisionEnergies() {
		return mergeCollisionsCheckBox.isSelected();
	}
	
	public boolean flagMinorIsotopesPrecursors() {
		return flagMinorIsotopesPrecursorsCheckBox.isSelected();
	}
	
	public int getMaxPrecursorCharge() {
		return (int)maxChargeSpinner.getValue();
	}

	public int getSmoothingFilterWidth() {		
		return ((SavitzkyGolayWidth)filterWidthComboBox.getSelectedItem()).getWidth();
	}
	
	public double getChromatogramExtractionWindow() {
		return Double.valueOf(xicWindowTextField.getText());
	}
	
	public ChromatogramDefinition getCommonChromatogramDefinition() {
		
		ChromatogramDefinition ccd = new ChromatogramDefinition(
				ChromatogramPlotMode.XIC, 
				getPolarity(),
				1, 
				null,
				false, 
				getPrecursorGroupingMassError(),				
				getPrecursorGroupingMassErrorType(),  
				new Range(3.0d - getChromatogramExtractionWindow(), 
						3.0d + getChromatogramExtractionWindow()),
				null
				//	new SavitzkyGolayFilter(getSmoothingFilterWidth())
				);
		return ccd;
	}
	
	public void loadParameters(
			MSMSExtractionParameterSet ps,
			DataExtractionMethod deMethod) {

		if(ps.getName() == null)
			nameTextField.setText("");
		else
			nameTextField.setText(ps.getName());
		
		if(deMethod != null)
			methodIdLabel.setText(deMethod.getId());
		
		if(ps.getDescription() == null)
			descriptionTextArea.setText("");
		else
			descriptionTextArea.setText(ps.getDescription());
		
		polarityComboBox.setSelectedItem(ps.getPolarity());		
		Range rtRange = ps.getDataExtractionRtRange();
		if(rtRange == null) {
			limitExtractionRtCheckBox.setSelected(false);
			rtFromTextField.setText(Double.toString(0.0d));
			rtToTextField.setText(Double.toString(0.0d));
		}
		else {
			limitExtractionRtCheckBox.setSelected(true);
			rtFromTextField.setText(Double.toString(rtRange.getMin()));
			rtToTextField.setText(Double.toString(rtRange.getMax()));
		}
		minPrecursorIntensityTextField.setText(Double.toString(ps.getMinPrecursorIntensity()));		
		isolationWindowLowerTextField.setText(Double.toString(ps.getMsmsIsolationWindowLowerBorder()));
		isolationWindowUpperTextField.setText(Double.toString(ps.getMsmsIsolationWindowUpperBorder()));
		
		msmsGroupRtWindowTextField.setText(Double.toString(ps.getMsmsGroupingRtWindow()));
		precursorGroupingMassErrorTextField.setText(Double.toString(ps.getPrecursorGroupingMassError()));
		precursorGroupingMassErrorTypeComboBox.setSelectedItem(ps.getPrecursorGroupingMassErrorType());
		
		chckbxRemoveAllMassesAboveParent.setSelected(ps.isRemoveAllMassesAboveParent());
		minimalCountsTextField.setText(Double.toString(ps.getMsMsCountsCutoff()));
		chckbxRemoveAllMassesBelowCounts.setSelected(ps.getMsMsCountsCutoff() > 0.0d);
		intensityMeasureComboBox.setSelectedItem(ps.getFilterIntensityMeasure());
		
		maxFragmentsSpinner.setValue(ps.getMaxFragmentsCutoff());
		chckbxLeaveOnly.setSelected(ps.getMaxFragmentsCutoff() > 0);
		
		mergeCollisionsCheckBox.setSelected(ps.isMergeCollisionEnergies());
		specSimCutoffTextField.setText(Double.toString(ps.getSpectrumSimilarityCutoffForMerging()));
		
		flagMinorIsotopesPrecursorsCheckBox.setSelected(ps.isFlagMinorIsotopesPrecursors());
		maxChargeSpinner.setValue(ps.getMaxPrecursorCharge());
		xicWindowTextField.setText(Double.toString(ps.getChromatogramExtractionWindow()));
		
		//	TODO MS1 stuff
	}
	
	public void loadDefaultParameters() {
		
		nameTextField.setText(DESCRIPTION_DEFAULT);
		descriptionTextArea.setText(DESCRIPTION_DEFAULT);		
		polarityComboBox.setSelectedItem(POLARITY_DEFAULT);
		minPrecursorIntensityTextField.setText(
				Double.toString(MIN_PRECURSOR_INTENSITY_DEFAULT));
		limitExtractionRtCheckBox.setSelected(USE_RT_RANGE_DEFAULT);
		rtFromTextField.setText(Double.toString(RT_BORDER_LEFT_DEFAULT));
		rtToTextField.setText(Double.toString(RT_BORDER_RIGHT_DEFAULT));
		isolationWindowLowerTextField.setText(Double.toString(ISOLATION_BORDER_MIN_DEFAULT));
		isolationWindowUpperTextField.setText(Double.toString(ISOLATION_BORDER_MAX_DEFAULT));		
		msmsGroupRtWindowTextField.setText(Double.toString(PRECURSOR_GROUPING_RT_WINDOW_DEFAULT));
		
		precursorGroupingMassErrorTextField.setText(Double.toString(PRECURSOR_GROUPING_MASS_ERROR_DEFAULT));
		precursorGroupingMassErrorTypeComboBox.setSelectedItem(PRECURSOR_GROUPING_MASS_ERROR_TYPE_DEFAULT);	
		
		chckbxRemoveAllMassesAboveParent.setSelected(REMOVE_MASSES_ABOVE_PARENT_DEFAULT);
		chckbxRemoveAllMassesBelowCounts.setSelected(REMOVE_MASSES_BELOW_COUNT_DEFAULT);
		minimalCountsTextField.setText(Double.toString(MINIMAL_COUNTS_DEFAULT));
		chckbxLeaveOnly.setSelected(LEAVE_MAX_FRAGMENTS_DEFAULT);
		maxFragmentsSpinner.setValue(MAX_FRAGMENTS_COUNT_DEFAULT);		
		intensityMeasureComboBox.setSelectedItem(INTENSITY_MEASURE_DEFAULT);		
		mergeCollisionsCheckBox.setSelected(MERGE_COLLISION_ENERGIES_DEFAULT);		
		specSimCutoffTextField.setText(Double.toString(SPECTRUM_SIMILARITY_CUTOFF_DEFAULT));			
		flagMinorIsotopesPrecursorsCheckBox.setSelected(FLAG_MINOR_ISOTOPES_PRECURSORS_DEFAULT);	
		maxChargeSpinner.setValue(MAX_PRECURSOR_CHARGE_DEFAULT);		
		filterWidthComboBox.setSelectedItem(FILTER_WIDTH_DEFAULT);		
		xicWindowTextField.setText(Double.toString(CHROMATOGRAM_WIDTH_DEFAULT));
	}
			
	public MSMSExtractionParameterSet createParameterSet() { 
		
		MSMSExtractionParameterSet ps = new MSMSExtractionParameterSet();
		ps.setName(getParameterSetName());
		ps.setDescription(getDescription());
		ps.setPolarity(getPolarity());
		ps.setMinPrecursorIntensity(getMinimalPrecursorIntensity());
		ps.setDataExtractionRtRange(getDataExtractionRtRange());
		ps.setMsmsIsolationWindowLowerBorder(getIsolationWindowLowerBorder());
		ps.setMsmsIsolationWindowUpperBorder(getIsolationWindowUpperBorder());	
		ps.setMsmsGroupingRtWindow(getMsmsGroupingRtWindow());
		ps.setPrecursorGroupingMassError(getPrecursorGroupingMassError());
		ps.setPrecursorGroupingMassErrorType(getPrecursorGroupingMassErrorType());		
		ps.setRemoveAllMassesAboveParent(removeAllMassesAboveParent());
		ps.setMsMsCountsCutoff(getMsMsCountsCutoff());
		ps.setFilterIntensityMeasure(getFilterIntensityMeasure());
		ps.setMaxFragmentsCutoff(getMaxFragmentsCutoff());	
		ps.setMergeCollisionEnergies(mergeCollisionEnergies());
		ps.setSpectrumSimilarityCutoffForMerging(getSpectrumSimilarityCutoff());
		ps.setFlagMinorIsotopesPrecursors(flagMinorIsotopesPrecursors());
		ps.setMaxPrecursorCharge(getMaxPrecursorCharge());
		ps.setSmoothingFilterWidth(getSmoothingFilterWidth());		
		ps.setChromatogramExtractionWindow(getChromatogramExtractionWindow());
		ps.setCommonChromatogramDefinition(getCommonChromatogramDefinition());
		ps.setXicTarget(MsFeatureChromatogramExtractionTarget.MSMSParentIon);
		return ps;
	}
	
	public Collection<String>validateParameters(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getParameterSetName().isEmpty())
			errors.add("Please specify the name for the method.");
		
		if(getPolarity() == null)
			errors.add("Polarity has to be specified");
		
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
		if(getChromatogramExtractionWindow() <= 0)
			errors.add("Chromatogram extraction window should be > 0");
		
		return errors;
	}
	
	public void setEditingEnabled(boolean isEnabled) {
		
		chckbxLeaveOnly.setEnabled(isEnabled);
		chckbxRemoveAllMassesAboveParent.setEnabled(isEnabled);
		chckbxRemoveAllMassesBelowCounts.setEnabled(isEnabled);
		flagMinorIsotopesPrecursorsCheckBox.setEnabled(isEnabled);
		limitExtractionRtCheckBox.setEnabled(isEnabled);
		mergeCollisionsCheckBox.setEnabled(isEnabled);
		filterWidthComboBox.setEnabled(isEnabled);
		intensityMeasureComboBox.setEnabled(isEnabled);
		polarityComboBox.setEnabled(isEnabled);
		precursorGroupingMassErrorTypeComboBox.setEnabled(isEnabled);
		isolationWindowLowerTextField.setEnabled(isEnabled);
		isolationWindowUpperTextField.setEnabled(isEnabled);
		minPrecursorIntensityTextField.setEnabled(isEnabled);
		minimalCountsTextField.setEnabled(isEnabled);
		msmsGroupRtWindowTextField.setEnabled(isEnabled);
		precursorGroupingMassErrorTextField.setEnabled(isEnabled);
		rtFromTextField.setEnabled(isEnabled);
		rtToTextField.setEnabled(isEnabled);
		xicWindowTextField.setEnabled(isEnabled);
		specSimCutoffTextField.setEnabled(isEnabled);
		maxChargeSpinner.setEnabled(isEnabled);
		maxFragmentsSpinner.setEnabled(isEnabled);
		descriptionTextArea.setEnabled(isEnabled);
		nameTextField.setEnabled(isEnabled);
	}
}

