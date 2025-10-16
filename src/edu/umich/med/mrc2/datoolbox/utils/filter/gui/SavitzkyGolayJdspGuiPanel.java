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

package edu.umich.med.mrc2.datoolbox.utils.filter.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterClass;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayWidth;
import edu.umich.med.mrc2.datoolbox.utils.filter.sgjdsp.SavitzkyGolayJdsp;
import edu.umich.med.mrc2.datoolbox.utils.filter.sgjdsp.SavitzkyGolayMode;

public class SavitzkyGolayJdspGuiPanel extends FilterGuiPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7232557580813526489L;
	
	private Preferences preferences;
	private static final String FILTER_WIDTH = "FILTER_WIDTH";
	private static final String POLYNOMIAL_ORDER = "POLYNOMIAL_ORDER";
	private static final String SG_MODE = "SG_MODE";
	
	private JComboBox filterWidthComboBox;
//	private JSpinner widthSpinner;
	private JSpinner polynomialOrderSpinner;
	private JComboBox sgModeComboBox;

	public SavitzkyGolayJdspGuiPanel() {
		super(FilterClass.SAVITZKY_GOLAY_JDSP);
	}

	@Override
	protected void createGui() {

		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{90, 97, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Window width");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		filterWidthComboBox = new JComboBox<SavitzkyGolayWidth>(
				new DefaultComboBoxModel<SavitzkyGolayWidth>(SavitzkyGolayWidth.values()));
//		widthSpinner = new JSpinner();
//		widthSpinner.setModel(new SpinnerNumberModel(5, 1, 100, 1));
		GridBagConstraints gbc_widthSpinner = new GridBagConstraints();
		gbc_widthSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_widthSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_widthSpinner.gridx = 1;
		gbc_widthSpinner.gridy = 0;
		add(filterWidthComboBox, gbc_widthSpinner);
		
		JLabel lblNewLabel_2 = new JLabel("Polynomial order");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		polynomialOrderSpinner = new JSpinner();
		polynomialOrderSpinner.setModel(new SpinnerNumberModel(3, 2, 25, 1));
		GridBagConstraints gbc_polynomialDegreeSpinner = new GridBagConstraints();
		gbc_polynomialDegreeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_polynomialDegreeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_polynomialDegreeSpinner.gridx = 1;
		gbc_polynomialDegreeSpinner.gridy = 1;
		add(polynomialOrderSpinner, gbc_polynomialDegreeSpinner);
		
		JLabel lblNewLabel_1 = new JLabel("Mode");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		sgModeComboBox = new JComboBox<SavitzkyGolayMode>(
				new DefaultComboBoxModel<SavitzkyGolayMode>(SavitzkyGolayMode.values()));
		sgModeComboBox.setSelectedItem(SavitzkyGolayMode.nearest);		
		GridBagConstraints gbc_sgModeComboBox = new GridBagConstraints();
		gbc_sgModeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_sgModeComboBox.gridx = 1;
		gbc_sgModeComboBox.gridy = 2;
		add(sgModeComboBox, gbc_sgModeComboBox);
	}

	@Override
	public Filter getFilter() {

		Collection<String> errors= validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return null;
		}
		SavitzkyGolayJdsp filter = new SavitzkyGolayJdsp(
				((SavitzkyGolayWidth)filterWidthComboBox.getSelectedItem()).getWidth(),				
				(int)polynomialOrderSpinner.getValue(),
				(SavitzkyGolayMode)sgModeComboBox.getSelectedItem());
		return filter;
	}
	
	@Override
	public void loadFilterParameters(Filter newFilter) {

		if(!SavitzkyGolayJdsp.class.isAssignableFrom(newFilter.getClass()))
			return;

		int fWidth = ((SavitzkyGolayJdsp)newFilter).getWindowSize();
		SavitzkyGolayWidth sgWidth = 
				SavitzkyGolayWidth.getSavitzkyGolayWidthByValue(fWidth);
		if(sgWidth != null)
			filterWidthComboBox.setSelectedItem(sgWidth);
		
		sgModeComboBox.setSelectedItem(((SavitzkyGolayJdsp)newFilter).getMode());
	}

	@Override
	protected Collection<String> validateParameters() {
		
		Collection<String>errors = new ArrayList<String>();
		
		int filterWidth = ((SavitzkyGolayWidth)filterWidthComboBox.getSelectedItem()).getWidth();
		int polynomialDegree = (int)polynomialOrderSpinner.getValue();
		if(filterWidth < polynomialDegree)
			errors.add("Polynomial order must be less that window size");
		
		return errors;
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		SavitzkyGolayWidth sgw = SavitzkyGolayWidth.getSavitzkyGolayWidthByName(
				preferences.get(FILTER_WIDTH, SavitzkyGolayWidth.NINE.name()));
		filterWidthComboBox.setSelectedItem(sgw);
		polynomialOrderSpinner.setValue(preferences.getInt(POLYNOMIAL_ORDER, 4));		
		sgModeComboBox.setSelectedItem(
				SavitzkyGolayMode.valueOf(preferences.get(SG_MODE, SavitzkyGolayMode.nearest.name())));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());	
		preferences.put(FILTER_WIDTH, ((SavitzkyGolayWidth)filterWidthComboBox.getSelectedItem()).name());
		preferences.putInt(POLYNOMIAL_ORDER, (int) polynomialOrderSpinner.getValue());
		preferences.put(SG_MODE, ((SavitzkyGolayMode)sgModeComboBox.getSelectedItem()).name());
	}
}












