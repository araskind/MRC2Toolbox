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

package edu.umich.med.mrc2.datoolbox.gui.fdata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.refsamples.ExperimentReferenceSampleTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class FeatureDataCleanupDialog extends JDialog implements BackedByPreferences {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4026777237123043022L;
	private static final Icon featureCleanupIcon = GuiUtils.getIcon("clearAnnotation", 32);
	
	private Preferences preferences;
	public static final String FILTER_BY_POOL_FREQUENCY = "FILTER_BY_POOL_FREQUENCY";	
	public static final String POOL_FREQUENCY_CUTOFF = "POOL_FREQUENCY_CUTOFF";
	public static final String FILTER_BY_MASS_DEFECT_BELOW_RT = "FILTER_BY_MASS_DEFECT_BELOW_RT";
	public static final String MASS_DEFECT_FILTER_RT_CUTOFF = "MASS_DEFECT_FILTER_RT_CUTOFF";
	public static final String MASS_DEFECT_FILTER_DEFECT_CUTOFF = "MASS_DEFECT_FILTER_DEFECT_CUTOFF";	
	public static final String FILTER_BY_HIGH_MASS_BELOW_RT = "FILTER_BY_HIGH_MASS_BELOW_RT";
	public static final String HIGH_MASS_FILTER_RT_CUTOFF = "HIGH_MASS_FILTER_RT_CUTOFF";
	public static final String HIGH_MASS_FILTER_MASS_CUTOFF = "HIGH_MASS_FILTER_MASS_CUTOFF";
	
	private JCheckBox pooledFrequencyFilterCheckBox;
	private JSpinner pooledFrequencySpinner;
	private JCheckBox massDefectBelowRTCheckBox;
	private JFormattedTextField massDefectFilterRTCutoffTextField;
	private JFormattedTextField mdFilterMassDefectValueTextField;
	private JCheckBox highMassBelowRTCheckBox;
	private JFormattedTextField highMassRtCutoffTextField;
	private JFormattedTextField highMassValueTextField;
	
	private ExperimentReferenceSampleTable referenceSampleTable;	
	
	public FeatureDataCleanupDialog(ActionListener listener) {
		
		super();
		setTitle("Cleanup features");
		setIconImage(((ImageIcon) featureCleanupIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(550, 400));
		setPreferredSize(new Dimension(520, 350));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 87, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		pooledFrequencyFilterCheckBox = new JCheckBox("Remove features present in less than ");
		GridBagConstraints gbc_pooledFrequencyFilterCheckBox = new GridBagConstraints();
		gbc_pooledFrequencyFilterCheckBox.anchor = GridBagConstraints.WEST;
		gbc_pooledFrequencyFilterCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_pooledFrequencyFilterCheckBox.gridx = 0;
		gbc_pooledFrequencyFilterCheckBox.gridy = 0;
		panel.add(pooledFrequencyFilterCheckBox, gbc_pooledFrequencyFilterCheckBox);
		
		pooledFrequencySpinner = new JSpinner();
		pooledFrequencySpinner.setModel(new SpinnerNumberModel(50, 0, 100, 1));
		pooledFrequencySpinner.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_pooledFrequencySpinner = new GridBagConstraints();
		gbc_pooledFrequencySpinner.insets = new Insets(0, 0, 5, 5);
		gbc_pooledFrequencySpinner.gridx = 1;
		gbc_pooledFrequencySpinner.gridy = 0;
		panel.add(pooledFrequencySpinner, gbc_pooledFrequencySpinner);
		
		JLabel lblNewLabel_1 = new JLabel("% of pooled samples");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Pooled sample types to include in calculation: ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.gridwidth = 2;
		gbc_lblNewLabel_2.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		referenceSampleTable = new ExperimentReferenceSampleTable();
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null) {
			
			referenceSampleTable.loadReferenceSamples(
					MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign());
			
			referenceSampleTable.selectSampleRows(
					ReferenceSamplesManager.getReferencePoolSamples());
		}	
		JScrollPane scrollPane = new JScrollPane(referenceSampleTable);
		scrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		panel.add(scrollPane, gbc_scrollPane);
		
		massDefectBelowRTCheckBox = new JCheckBox("Remove any features with RT < ");
		GridBagConstraints gbc_massDefectBelowRTCheckBox = new GridBagConstraints();
		gbc_massDefectBelowRTCheckBox.anchor = GridBagConstraints.EAST;
		gbc_massDefectBelowRTCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_massDefectBelowRTCheckBox.gridx = 0;
		gbc_massDefectBelowRTCheckBox.gridy = 3;
		panel.add(massDefectBelowRTCheckBox, gbc_massDefectBelowRTCheckBox);
		
		massDefectFilterRTCutoffTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		massDefectFilterRTCutoffTextField.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_mdRtCutoffTextField = new GridBagConstraints();
		gbc_mdRtCutoffTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mdRtCutoffTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mdRtCutoffTextField.gridx = 1;
		gbc_mdRtCutoffTextField.gridy = 3;
		panel.add(massDefectFilterRTCutoffTextField, gbc_mdRtCutoffTextField);
		
		JLabel lblNewLabel = new JLabel("min. and mass defect > ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 2;
		gbc_lblNewLabel.gridy = 3;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		mdFilterMassDefectValueTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		mdFilterMassDefectValueTextField.setPreferredSize(new Dimension(50, 20));
		mdFilterMassDefectValueTextField.setColumns(5);
		GridBagConstraints gbc_mdRtValueTextField = new GridBagConstraints();
		gbc_mdRtValueTextField.insets = new Insets(0, 0, 5, 0);
		gbc_mdRtValueTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mdRtValueTextField.gridx = 3;
		gbc_mdRtValueTextField.gridy = 3;
		panel.add(mdFilterMassDefectValueTextField, gbc_mdRtValueTextField);
		
		highMassBelowRTCheckBox = new JCheckBox("Remove any features with RT < ");
		GridBagConstraints gbc_massBelowRTCheckBox = new GridBagConstraints();
		gbc_massBelowRTCheckBox.anchor = GridBagConstraints.EAST;
		gbc_massBelowRTCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_massBelowRTCheckBox.gridx = 0;
		gbc_massBelowRTCheckBox.gridy = 4;
		panel.add(highMassBelowRTCheckBox, gbc_massBelowRTCheckBox);
		
		highMassRtCutoffTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		highMassRtCutoffTextField.setColumns(5);
		highMassRtCutoffTextField.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_highMassRtCutoffTextField = new GridBagConstraints();
		gbc_highMassRtCutoffTextField.insets = new Insets(0, 0, 0, 5);
		gbc_highMassRtCutoffTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_highMassRtCutoffTextField.gridx = 1;
		gbc_highMassRtCutoffTextField.gridy = 4;
		panel.add(highMassRtCutoffTextField, gbc_highMassRtCutoffTextField);
		
		JLabel lblMinAndMass = new JLabel("min.        and M/Z > ");
		GridBagConstraints gbc_lblMinAndMass = new GridBagConstraints();
		gbc_lblMinAndMass.anchor = GridBagConstraints.WEST;
		gbc_lblMinAndMass.insets = new Insets(0, 0, 0, 5);
		gbc_lblMinAndMass.gridx = 2;
		gbc_lblMinAndMass.gridy = 4;
		panel.add(lblMinAndMass, gbc_lblMinAndMass);
		
		highMassValueTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		highMassValueTextField.setPreferredSize(new Dimension(80, 20));
		highMassValueTextField.setColumns(10);
		GridBagConstraints gbc_highMassBelowRtTextField = new GridBagConstraints();
		gbc_highMassBelowRtTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_highMassBelowRtTextField.gridx = 3;
		gbc_highMassBelowRtTextField.gridy = 4;
		panel.add(highMassValueTextField, gbc_highMassBelowRtTextField);
				
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(MainActionCommands.CLEANUP_FEATURE_DATA_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.CLEANUP_FEATURE_DATA_COMMAND.getName());
		btnSave.addActionListener(listener);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		pooledFrequencyFilterCheckBox.setSelected(
				preferences.getBoolean(FILTER_BY_POOL_FREQUENCY, true));
		pooledFrequencySpinner.setValue(
				preferences.getInt(POOL_FREQUENCY_CUTOFF, 50));
		massDefectBelowRTCheckBox.setSelected(
				preferences.getBoolean(FILTER_BY_MASS_DEFECT_BELOW_RT, true));
		
		double mdRtCutoff = preferences.getDouble(MASS_DEFECT_FILTER_RT_CUTOFF, 5.0d);
		massDefectFilterRTCutoffTextField.setText(Double.toString(mdRtCutoff));
		double mdValueCutoff = preferences.getDouble(MASS_DEFECT_FILTER_DEFECT_CUTOFF, 0.5d);
		mdFilterMassDefectValueTextField.setText(Double.toString(mdValueCutoff));
		
		highMassBelowRTCheckBox.setSelected(
				preferences.getBoolean(FILTER_BY_HIGH_MASS_BELOW_RT, true));
		double highMassRtCutoff = preferences.getDouble(HIGH_MASS_FILTER_RT_CUTOFF, 5.0d);
		highMassRtCutoffTextField.setText(Double.toString(highMassRtCutoff));
		double highMassValueCutoff = preferences.getDouble(HIGH_MASS_FILTER_MASS_CUTOFF, 500d);
		highMassValueTextField.setText(Double.toString(highMassValueCutoff));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(FeatureDataCleanupDialog.class.getName()));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(FeatureDataCleanupDialog.class.getName());
		preferences.putBoolean(FILTER_BY_POOL_FREQUENCY, 
				pooledFrequencyFilterCheckBox.isSelected());
		preferences.putInt(POOL_FREQUENCY_CUTOFF, 
				(int)pooledFrequencySpinner.getValue());
		preferences.getBoolean(FILTER_BY_MASS_DEFECT_BELOW_RT, 
				massDefectBelowRTCheckBox.isSelected());
	}
}













