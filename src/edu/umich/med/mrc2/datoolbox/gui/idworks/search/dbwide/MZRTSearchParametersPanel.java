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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MZRTSearchParametersPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7696352722919791838L;

	private JFormattedTextField bpmzTextField;
	private JTextField fragmentListTextField;
	private JFormattedTextField massErrorTextField;
	private JComboBox massErrorTypeComboBox;	
	private JComboBox polarityComboBox;
	private JComboBox collisionEnergyComboBox;
	private JCheckBox chckbxIgnoreMzsearch;
	private JComboBox msDepthComboBox;	
	private JFormattedTextField rtFromTextField;
	private JFormattedTextField rtToTextField;	

	public MZRTSearchParametersPanel() {
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0 };
		setLayout(gridBagLayout);

		JLabel lblBasePeakMz = new JLabel("Precursor M/Z ");
		lblBasePeakMz.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblBasePeakMz = new GridBagConstraints();
		gbc_lblBasePeakMz.anchor = GridBagConstraints.EAST;
		gbc_lblBasePeakMz.insets = new Insets(0, 0, 5, 5);
		gbc_lblBasePeakMz.gridx = 0;
		gbc_lblBasePeakMz.gridy = 0;
		add(lblBasePeakMz, gbc_lblBasePeakMz);

		bpmzTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		bpmzTextField.setColumns(10);
		GridBagConstraints gbc_bpmzMinTextField = new GridBagConstraints();
		gbc_bpmzMinTextField.insets = new Insets(0, 0, 5, 5);
		gbc_bpmzMinTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_bpmzMinTextField.gridx = 1;
		gbc_bpmzMinTextField.gridy = 0;
		add(bpmzTextField, gbc_bpmzMinTextField);

		JLabel lblTo_1 = new JLabel("<HTML>&#177;</HTML>");
		GridBagConstraints gbc_lblTo_1 = new GridBagConstraints();
		gbc_lblTo_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo_1.gridx = 2;
		gbc_lblTo_1.gridy = 0;
		add(lblTo_1, gbc_lblTo_1);

		massErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massErrorTextField.setColumns(10);
		GridBagConstraints gbc_bpMxMaxTextField = new GridBagConstraints();
		gbc_bpMxMaxTextField.insets = new Insets(0, 0, 5, 5);
		gbc_bpMxMaxTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_bpMxMaxTextField.gridx = 3;
		gbc_bpMxMaxTextField.gridy = 0;
		add(massErrorTextField, gbc_bpMxMaxTextField);

		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setPreferredSize(new Dimension(50, 25));
		massErrorTypeComboBox.setMinimumSize(new Dimension(50, 25));
		GridBagConstraints gbc_massErrorTypeComboBox = new GridBagConstraints();
		gbc_massErrorTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_massErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTypeComboBox.gridx = 4;
		gbc_massErrorTypeComboBox.gridy = 0;
		add(massErrorTypeComboBox, gbc_massErrorTypeComboBox);
		
		JLabel lblNewLabel = new JLabel("Fragments");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);
		
		fragmentListTextField = new JTextField();
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridwidth = 4;
		gbc_lblNewLabel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 1;
		add(fragmentListTextField, gbc_lblNewLabel_1);

		JLabel lblMsType = new JLabel("Polarity");
		lblMsType.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblMsType = new GridBagConstraints();
		gbc_lblMsType.anchor = GridBagConstraints.EAST;
		gbc_lblMsType.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsType.gridx = 0;
		gbc_lblMsType.gridy = 2;
		add(lblMsType, gbc_lblMsType);

		polarityComboBox = new JComboBox<Polarity>(
				new DefaultComboBoxModel<Polarity>(
						new Polarity[] { Polarity.Positive, Polarity.Negative }));
		polarityComboBox.setMinimumSize(new Dimension(70, 25));
		polarityComboBox.setPreferredSize(new Dimension(70, 25));
		GridBagConstraints gbc_polarityComboBox = new GridBagConstraints();
		gbc_polarityComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_polarityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_polarityComboBox.gridx = 1;
		gbc_polarityComboBox.gridy = 2;
		add(polarityComboBox, gbc_polarityComboBox);

		JLabel lblMsDepth = new JLabel("MS depth");
		lblMsDepth.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblMsDepth = new GridBagConstraints();
		gbc_lblMsDepth.anchor = GridBagConstraints.EAST;
		gbc_lblMsDepth.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsDepth.gridx = 3;
		gbc_lblMsDepth.gridy = 2;
		add(lblMsDepth, gbc_lblMsDepth);

		msDepthComboBox = new JComboBox<MsDepth>(
				new DefaultComboBoxModel<MsDepth>(MsDepth.values()));
		msDepthComboBox.setPreferredSize(new Dimension(50, 25));
		msDepthComboBox.setMinimumSize(new Dimension(50, 25));
		GridBagConstraints gbc_msDepthComboBox = new GridBagConstraints();
		gbc_msDepthComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_msDepthComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_msDepthComboBox.gridx = 4;
		gbc_msDepthComboBox.gridy = 2;
		add(msDepthComboBox, gbc_msDepthComboBox);

		JLabel lblCollisionEnergy = new JLabel("Collision energy ");
		lblCollisionEnergy.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblCollisionEnergy = new GridBagConstraints();
		gbc_lblCollisionEnergy.insets = new Insets(0, 0, 5, 5);
		gbc_lblCollisionEnergy.anchor = GridBagConstraints.EAST;
		gbc_lblCollisionEnergy.gridx = 0;
		gbc_lblCollisionEnergy.gridy = 3;
		add(lblCollisionEnergy, gbc_lblCollisionEnergy);

		collisionEnergyComboBox = new JComboBox();
		collisionEnergyComboBox.setPreferredSize(new Dimension(50, 25));
		collisionEnergyComboBox.setMinimumSize(new Dimension(50, 25));
		GridBagConstraints gbc_collisionEnergyComboBox = new GridBagConstraints();
		gbc_collisionEnergyComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_collisionEnergyComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_collisionEnergyComboBox.gridx = 1;
		gbc_collisionEnergyComboBox.gridy = 3;
		add(collisionEnergyComboBox, gbc_collisionEnergyComboBox);

		chckbxIgnoreMzsearch = new JCheckBox("Ignore M/Z (search by RT only)");
		chckbxIgnoreMzsearch.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_chckbxIgnoreMzsearch = new GridBagConstraints();
		gbc_chckbxIgnoreMzsearch.anchor = GridBagConstraints.WEST;
		gbc_chckbxIgnoreMzsearch.gridwidth = 2;
		gbc_chckbxIgnoreMzsearch.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIgnoreMzsearch.gridx = 0;
		gbc_chckbxIgnoreMzsearch.gridy = 4;
		add(chckbxIgnoreMzsearch, gbc_chckbxIgnoreMzsearch);

		JLabel lblRetentionTimeFrom = new JLabel("RT, min. from ");
		lblRetentionTimeFrom.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblRetentionTimeFrom = new GridBagConstraints();
		gbc_lblRetentionTimeFrom.anchor = GridBagConstraints.EAST;
		gbc_lblRetentionTimeFrom.insets = new Insets(0, 0, 5, 5);
		gbc_lblRetentionTimeFrom.gridx = 0;
		gbc_lblRetentionTimeFrom.gridy = 5;
		add(lblRetentionTimeFrom, gbc_lblRetentionTimeFrom);

		rtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtFromTextField.setColumns(10);
		GridBagConstraints gbc_rtFromTextField = new GridBagConstraints();
		gbc_rtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFromTextField.gridx = 1;
		gbc_rtFromTextField.gridy = 5;
		add(rtFromTextField, gbc_rtFromTextField);

		JLabel lblTo = new JLabel(" to ");
		lblTo.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
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
		
		JLabel lblNewLabel_2 = new JLabel(" ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 6;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JButton resetButton = new JButton(
				"Reset to default values", IDTrackerDataSearchDialog.resetIcon);
		resetButton.setActionCommand(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName());
		resetButton.addActionListener(this);
		GridBagConstraints gbc_resetButton = new GridBagConstraints();
		gbc_resetButton.anchor = GridBagConstraints.SOUTHWEST;
		gbc_resetButton.gridwidth = 2;
		gbc_resetButton.insets = new Insets(0, 0, 0, 5);
		gbc_resetButton.gridx = 0;
		gbc_resetButton.gridy = 7;
		add(resetButton, gbc_resetButton);
	}
	
	@SuppressWarnings("unchecked")
	public void populateCollisionEnergySelectorFromDatabase() {
			collisionEnergyComboBox.setModel(
					new SortedComboBoxModel<Double>(
							IDTDataCash.getCollisionEnergiesList()));
	}
	
	public Double getPrecursorMz() {
		
		Double bpMz = null;
		if(!bpmzTextField.getText().trim().isEmpty())
			bpMz = Double.parseDouble(bpmzTextField.getText());
		
		return bpMz;
	}
	
	public void setPrecursorMz(Double bpMz) {
		
		bpmzTextField.setText("");
		if(bpMz != null && bpMz > 0)		
			bpmzTextField.setText(Double.toString(bpMz));
	}
	
	public Double getMassErrorValue() {
		
		Double massError = null;
		if(!massErrorTextField.getText().trim().isEmpty())
			massError = Double.parseDouble(massErrorTextField.getText());
		
		return massError;
	}
	
	public void setMassErrorValue(Double massError) {
		
		massErrorTextField.setText("");
		if(massError != null && massError > 0)		
			massErrorTextField.setText(Double.toString(massError));
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();
	}
	
	public void setMassErrorType(MassErrorType errorType) {
		massErrorTypeComboBox.setSelectedItem(errorType);
	}

	public Polarity getPolarity() {
		return (Polarity) polarityComboBox.getSelectedItem();
	}
	
	public void setPolarity(Polarity polarity) {
		polarityComboBox.setSelectedItem(polarity);
	}
	
	public MsDepth getMsDepth() {
		return (MsDepth)msDepthComboBox.getSelectedItem();
	}
	
	public void setMsDepth(MsDepth depth) {
		msDepthComboBox.setSelectedItem(depth);
	}
	
	public Double getCollisionEnergy() {
		return (Double)collisionEnergyComboBox.getSelectedItem();
	}
	
	public void setCollisionEnergy(Double ce) {
		collisionEnergyComboBox.setSelectedIndex(-1);
		if(ce != null && ce > 0)
			collisionEnergyComboBox.setSelectedItem(ce);
	}
	
	public boolean ignoreMz() {
		return chckbxIgnoreMzsearch.isSelected();
	}
	
	public void setIgnoreMz(boolean ignoreMz) {
		chckbxIgnoreMzsearch.setSelected(ignoreMz);
	}

	public Range getRtRange() {

		if (rtFromTextField.getText().isEmpty() || rtToTextField.getText().isEmpty())
			return null;

		double startRt = Double.parseDouble(rtFromTextField.getText());
		double endRt = Double.parseDouble(rtToTextField.getText());
		if (startRt > endRt)
			return null;

		return new Range(startRt, endRt);
	}
	
	public void setRtRange(double startRt, double endRt) {
		rtFromTextField.setText("");
		rtToTextField.setText("");
		if(endRt > 0.0d && startRt >= 0.0d) {
			rtFromTextField.setText(Double.toString(startRt));
			rtToTextField.setText(Double.toString(endRt));
		}
	}

	public void resetPanel(Preferences preferences) {

		rtFromTextField.setText("");
		rtToTextField.setText("");
		bpmzTextField.setText("");
		fragmentListTextField.setText("");
		chckbxIgnoreMzsearch.setSelected(false);
		massErrorTextField.setText(
				Double.toString(preferences.getDouble(IDTrackerDataSearchDialog.MZ_ERROR, 20.0d)));
		massErrorTypeComboBox.setSelectedItem(MassErrorType.ppm);
		collisionEnergyComboBox.setSelectedIndex(-1);
		msDepthComboBox.setSelectedItem(MsDepth.MS2);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName())) {
			resetPanel(Preferences.userRoot().node(IDTrackerDataSearchDialog.PREFS_NODE));
		}
	}
	
	public String getFragmentListString() {
		return fragmentListTextField.getText().trim();		
	}
	
	public void setFragmentListString(String fragmentListString) {
		fragmentListTextField.setText(fragmentListString);		
	}
	
	public Collection<Double> getFragmentList() {
		
		Collection<Double>fragments = new ArrayList<Double>();
		if(fragmentListTextField.getText().trim().isEmpty())
			return fragments;
		
		String[] mzStrings = 
				StringUtils.split(fragmentListTextField.getText().
						trim().replaceAll("[\\s+,:,;\\,]", ";").trim(), ';');
		for (String mzs : mzStrings) {

			if(mzs.isEmpty() || mzs.equals(";"))
				continue;
			
			double mz = 0.0d;
			try {
				mz = Double.parseDouble(mzs);
			} catch (NumberFormatException e) {
				//	TODO
			}
			if (mz > 0.0d)
				fragments.add(mz);
		}		
		return fragments;		
	}
	
	public Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
		String fragError = validateFragmentsString();
		if(!ignoreMz() && getPrecursorMz() == null 
				&& (getFragmentListString().isEmpty() || fragError != null)) {
			errors.add("If \"Ignore M/Z\" checkbox is not selected on \"MS / RT / Identification\" panel \n"
					+ "you have to provide precursor M/Z and/or one or more fragment M/Z values.");
			
			if(fragError != null)
				errors.add(fragError);
			
			return errors;
		}
		if(ignoreMz() && getRtRange() == null) {
			errors.add("If \"Ignore M/Z\" checkbox is selected on \"MS / RT / Identification\" panel \n"
					+ "you have to specify the RT range.");
			return errors;
		}
		if(!ignoreMz() && !getFragmentListString().isEmpty()) {
			
			String fragmentsError = validateFragmentsString();
			if(fragmentsError != null) {
				errors.add(fragmentsError);
				return errors;
			}
		}
		return errors;
	}
	
	private String validateFragmentsString() {
		
		if(!getFragmentListString().isEmpty()) {
			
			String[] mzStrings = 
					StringUtils.split(fragmentListTextField.getText().
							trim().replaceAll("[\\s+,:,;\\,]", ";"), ';');
			for (String mzs : mzStrings) {

				double mz = 0.0d;
				try {
					mz = Double.parseDouble(mzs);
				} catch (NumberFormatException e) {
					return "Invalid mass value(s) for fragments on \"MS / RT / Identification\" panel! \n"
							+ "Only numbers, \".\" as decimal pont, and the following separator characters: \n"
							+ "space (   )  comma ( , ) colon ( : ) and semi-colon ( ; ) are allowed.";
				}
			}
			return null;
		}
		return null;
	}
	
	
}









