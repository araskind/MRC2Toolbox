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
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterClass;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayFilter;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayWidth;

public class SavitzkyGolayMZMineGuiPanel extends FilterGuiPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7232557580813526489L;
	
	private JComboBox filterWidthComboBox;	
	private Preferences preferences;
	private static final String FILTER_WIDTH = "FILTER_WIDTH";

	public SavitzkyGolayMZMineGuiPanel() {		
		super(FilterClass.SAVITZKY_GOLAY_MZMINE);
	}

	@Override
	protected void createGui() {

		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{90, 60, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Filter width");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		filterWidthComboBox = new JComboBox<SavitzkyGolayWidth>(
				new DefaultComboBoxModel<SavitzkyGolayWidth>(SavitzkyGolayWidth.values()));
		GridBagConstraints gbc_pointsBeforeSpinner = new GridBagConstraints();
		gbc_pointsBeforeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_pointsBeforeSpinner.gridx = 1;
		gbc_pointsBeforeSpinner.gridy = 0;
		add(filterWidthComboBox, gbc_pointsBeforeSpinner);
	}

	@Override
	public Filter getFilter() {

		Collection<String> errors= validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return null;
		}		
		int width = ((SavitzkyGolayWidth)filterWidthComboBox.getSelectedItem()).getWidth();
		return new SavitzkyGolayFilter(width);
	}
	
	@Override
	public void loadFilterParameters(Filter newFilter) {

		if(!SavitzkyGolayFilter.class.isAssignableFrom(newFilter.getClass()))
			return;
		
		int fWidth = ((SavitzkyGolayFilter)newFilter).getFilterWidth();
		SavitzkyGolayWidth sgWidth = 
				SavitzkyGolayWidth.getSavitzkyGolayWidthByValue(fWidth);
		if(sgWidth != null)
			filterWidthComboBox.setSelectedItem(sgWidth);
	}

	@Override
	protected Collection<String> validateParameters() {
		
		Collection<String>errors = new ArrayList<String>();
		
		return errors;
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		SavitzkyGolayWidth sgw = SavitzkyGolayWidth.getSavitzkyGolayWidthByName(
				preferences.get(FILTER_WIDTH, SavitzkyGolayWidth.NINE.name()));
		filterWidthComboBox.setSelectedItem(sgw);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());	
		preferences.put(FILTER_WIDTH, 
				((SavitzkyGolayWidth)filterWidthComboBox.getSelectedItem()).name());
	}
}










