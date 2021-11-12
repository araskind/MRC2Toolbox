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

package edu.umich.med.mrc2.datoolbox.utils.filter.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterClass;
import edu.umich.med.mrc2.datoolbox.utils.filter.SmoothingCubicSplineFilter;

public class SmoothingCubicSplineGuiPanel extends FilterGuiPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7232557580813526489L;
	
	JFormattedTextField smoothingFactorTextField;
	private Preferences preferences;
	private static final String SMOOTHING_FACTOR = "SMOOTHING_FACTOR";

	public SmoothingCubicSplineGuiPanel() {		
		super(FilterClass.SMOOTHING_CUBIC_SPLINE);
	}

	@Override
	protected void createGui() {

		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{90, 60, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Smoothing factor");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		smoothingFactorTextField = new JFormattedTextField(new DecimalFormat("#.##"));
		GridBagConstraints gbc_pointsBeforeSpinner = new GridBagConstraints();
		gbc_pointsBeforeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_pointsBeforeSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_pointsBeforeSpinner.gridx = 1;
		gbc_pointsBeforeSpinner.gridy = 0;
		add(smoothingFactorTextField, gbc_pointsBeforeSpinner);
		
		JLabel lblNewLabel_1 = new JLabel("0.0 - 1.0");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
	}

	@Override
	public Filter getFilter() {

		Collection<String> errors= validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return null;
		}
		double smoothingFactor =  getSmoothingFactor();
		return new SmoothingCubicSplineFilter(smoothingFactor);
	}
	
	private double getSmoothingFactor() {
		return Double.parseDouble(smoothingFactorTextField.getText().trim());
	}

	@Override
	protected Collection<String> validateParameters() {
		Collection<String>errors = new ArrayList<String>();
		double smoothingFactor =  getSmoothingFactor();
		if(smoothingFactor < 0 || smoothingFactor > 1)
			errors.add("Smoothing factor should be in the range from 0 to 1");
		
		return errors;
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		smoothingFactorTextField.setText(
				Double.toString(preferences.getDouble(SMOOTHING_FACTOR, 0.5d)));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());	
		preferences.putDouble(SMOOTHING_FACTOR, getSmoothingFactor());
	}
}










