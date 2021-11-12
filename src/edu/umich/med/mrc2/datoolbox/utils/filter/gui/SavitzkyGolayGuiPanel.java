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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterClass;
import edu.umich.med.mrc2.datoolbox.utils.filter.sgfilter.SGFilter;

public class SavitzkyGolayGuiPanel extends FilterGuiPanel implements ChangeListener, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7232557580813526489L;
	
	private JSpinner pointsBeforeSpinner;
	private JSpinner polynomialDegreeSpinner;
	private JSpinner pointsAfterSpinner;
	private JCheckBox assymmetricCheckBox;
	
	private Preferences preferences;
	private static final String POINTS_BEFORE = "POINTS_BEFORE";
	private static final String POINTS_AFTER = "POINTS_AFTER";
	private static final String POLYNOMIAL_DEGREE = "POLYNOMIAL_DEGREE";
	private static final String IS_ASSYMMETRIC = "IS_ASSYMMETRIC";

	public SavitzkyGolayGuiPanel() {		
		super(FilterClass.SAVITZKY_GOLAY);
	}

	@Override
	protected void createGui() {

		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{90, 60, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Points before");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		pointsBeforeSpinner = new JSpinner();
		pointsBeforeSpinner.setModel(new SpinnerNumberModel(5, 1, 100, 1));
		modifySpinner(pointsBeforeSpinner);
		pointsBeforeSpinner.addChangeListener(this);
		GridBagConstraints gbc_pointsBeforeSpinner = new GridBagConstraints();
		gbc_pointsBeforeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_pointsBeforeSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_pointsBeforeSpinner.gridx = 1;
		gbc_pointsBeforeSpinner.gridy = 0;
		add(pointsBeforeSpinner, gbc_pointsBeforeSpinner);
		
		JLabel lblNewLabel_1 = new JLabel("Points after");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		pointsAfterSpinner = new JSpinner();
		pointsAfterSpinner.setModel(new SpinnerNumberModel(5, 1, 100, 1));
		modifySpinner(pointsAfterSpinner);
		pointsAfterSpinner.addChangeListener(this);
		GridBagConstraints gbc_pointsAfterSpinner = new GridBagConstraints();
		gbc_pointsAfterSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_pointsAfterSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_pointsAfterSpinner.gridx = 1;
		gbc_pointsAfterSpinner.gridy = 1;
		add(pointsAfterSpinner, gbc_pointsAfterSpinner);
		
		assymmetricCheckBox = new JCheckBox("Assymmetric");
		assymmetricCheckBox.addItemListener(this);
		GridBagConstraints gbc_assymmetricCheckBox = new GridBagConstraints();
		gbc_assymmetricCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_assymmetricCheckBox.gridx = 2;
		gbc_assymmetricCheckBox.gridy = 1;
		add(assymmetricCheckBox, gbc_assymmetricCheckBox);
		
		JLabel lblNewLabel_2 = new JLabel("polynomial order");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		polynomialDegreeSpinner = new JSpinner();
		polynomialDegreeSpinner.setModel(new SpinnerNumberModel(4, 2, 25, 1));
		GridBagConstraints gbc_polynomialDegreeSpinner = new GridBagConstraints();
		gbc_polynomialDegreeSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_polynomialDegreeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_polynomialDegreeSpinner.gridx = 1;
		gbc_polynomialDegreeSpinner.gridy = 2;
		add(polynomialDegreeSpinner, gbc_polynomialDegreeSpinner);
	}
	
	private void modifySpinner(final JSpinner spinner) {
		
		JComponent comp = spinner.getEditor();
	    JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
	    DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
	    formatter.setCommitsOnValidEdit(true);
	}

	@Override
	public Filter getFilter() {

		Collection<String> errors= validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return null;
		}
		int pointsBefore = (int)pointsBeforeSpinner.getValue();
		int pointsAfter = (int)pointsAfterSpinner.getValue();
		int polynomialDegree = (int)polynomialDegreeSpinner.getValue();
		SGFilter sgFilter = new SGFilter(pointsBefore, pointsAfter);
		double[] coeffs = SGFilter.computeSGCoefficients(
				pointsBefore, pointsAfter, polynomialDegree);
		sgFilter.setPrecalculatedCoefficients(coeffs);
		
		return sgFilter;
	}

	@Override
	protected Collection<String> validateParameters() {
		
		int pointsBefore = (int)pointsBeforeSpinner.getValue();
		int pointsAfter = (int)pointsAfterSpinner.getValue();
		int polynomialDegree = (int)polynomialDegreeSpinner.getValue();
		
		Collection<String>errors = new ArrayList<String>();
		if(pointsBefore + pointsAfter < polynomialDegree)
			errors.add("");
		
		return errors;
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		Object source = e.getSource();
		if(!assymmetricCheckBox.isSelected()) {
			
			if(source.equals(pointsBeforeSpinner)) {
			
				pointsAfterSpinner.removeChangeListener(this);
				pointsAfterSpinner.setValue(pointsBeforeSpinner.getValue());
				pointsAfterSpinner.addChangeListener(this);
			}
			if(source.equals(pointsAfterSpinner)) {
				pointsBeforeSpinner.removeChangeListener(this);
				pointsBeforeSpinner.setValue(pointsAfterSpinner.getValue());
				pointsBeforeSpinner.addChangeListener(this);
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.DESELECTED) {
			
			int pointsBefore = (int)pointsBeforeSpinner.getValue();
			int pointsAfter = (int)pointsAfterSpinner.getValue();
			if(pointsBefore != pointsAfter) {
				int points = Math.max(pointsAfter, pointsBefore);
				pointsBeforeSpinner.removeChangeListener(this);
				pointsAfterSpinner.removeChangeListener(this);
				pointsBeforeSpinner.setValue(points);
				pointsAfterSpinner.setValue(points);
				pointsBeforeSpinner.addChangeListener(this);
				pointsAfterSpinner.addChangeListener(this);
			}
		}
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		pointsBeforeSpinner.removeChangeListener(this);
		pointsAfterSpinner.removeChangeListener(this);
		pointsBeforeSpinner.setValue(preferences.getInt(POINTS_BEFORE, 5));
		pointsAfterSpinner.setValue(preferences.getInt(POINTS_AFTER, 5));
		pointsBeforeSpinner.addChangeListener(this);
		pointsAfterSpinner.addChangeListener(this);
		
		polynomialDegreeSpinner.setValue(preferences.getInt(POLYNOMIAL_DEGREE, 4));
		assymmetricCheckBox.setSelected(preferences.getBoolean(IS_ASSYMMETRIC, false));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());	
		preferences.putInt(POINTS_BEFORE, (int) pointsBeforeSpinner.getValue());
		preferences.putInt(POINTS_AFTER, (int) pointsAfterSpinner.getValue());
		preferences.putInt(POLYNOMIAL_DEGREE, (int) polynomialDegreeSpinner.getValue());
		preferences.putBoolean(IS_ASSYMMETRIC, assymmetricCheckBox.isSelected());
	}


}










