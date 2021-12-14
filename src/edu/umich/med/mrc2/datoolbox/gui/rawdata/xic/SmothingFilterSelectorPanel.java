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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.xic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterClass;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.FilterGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.LoessGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.MovingAverageGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.SavitzkyGolayJdspGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.SavitzkyGolayMZMineGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.SmoothingCubicSplineGuiPanel;
import edu.umich.med.mrc2.datoolbox.utils.filter.gui.WeightedMovingAverageGuiPanel;

public class SmothingFilterSelectorPanel extends JPanel implements ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8759075679935941289L;

	private JComboBox filterTypeComboBox;
	private JPanel filterParameters;
	private FilterGuiPanel filterGuiPanel;
	
	public SmothingFilterSelectorPanel() {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gbl_smoothingSettingsPanel = new GridBagLayout();
		gbl_smoothingSettingsPanel.columnWidths = new int[]{0, 0, 0};
		gbl_smoothingSettingsPanel.rowHeights = new int[]{0, 0, 0};
		gbl_smoothingSettingsPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_smoothingSettingsPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gbl_smoothingSettingsPanel);
		
		JLabel lblNewLabel_1 = new JLabel("Filter type ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		filterTypeComboBox = new JComboBox<FilterClass>(
				new DefaultComboBoxModel<FilterClass>(new FilterClass[] {
//						FilterClass.SAVITZKY_GOLAY_MZMINE, 
						FilterClass.SAVITZKY_GOLAY_JDSP, 
						FilterClass.MOVING_AVERAGE, 
						FilterClass.WEIGHTED_MOVING_AVERAGE 
					}));				
		filterTypeComboBox.setSelectedIndex(-1);	//	TODO read from preferences		
		filterTypeComboBox.addItemListener(this);
		GridBagConstraints gbc_filterTypeComboBox = new GridBagConstraints();
		gbc_filterTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_filterTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_filterTypeComboBox.gridx = 1;
		gbc_filterTypeComboBox.gridy = 0;
		add(filterTypeComboBox, gbc_filterTypeComboBox);
		
		filterParameters = new JPanel();
		filterParameters.setBorder(new CompoundBorder(
				new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
								new Color(160, 160, 160)), "Filter parameters", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
				new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_filterParameters = new GridBagConstraints();
		gbc_filterParameters.gridwidth = 2;
		gbc_filterParameters.insets = new Insets(0, 0, 0, 5);
		gbc_filterParameters.fill = GridBagConstraints.BOTH;
		gbc_filterParameters.gridx = 0;
		gbc_filterParameters.gridy = 1;
		add(filterParameters, gbc_filterParameters);
		filterParameters.setLayout(new BorderLayout(0, 0));
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

       if (e.getStateChange() == ItemEvent.SELECTED) {
           Object item = e.getItem();
           if(item instanceof FilterClass)
        	   showFilterClassParameters((FilterClass)item);
       }
	}
	
	private void showFilterClassParameters(FilterClass filterClass) {

		if(filterGuiPanel != null)			
			filterGuiPanel.savePreferences();
		
		filterGuiPanel = null;
		filterParameters.removeAll();
		
//		if(filterClass.equals(FilterClass.SAVITZKY_GOLAY))
//			filterGuiPanel = new SavitzkyGolayGuiPanel();	
		
		if(filterClass.equals(FilterClass.SAVITZKY_GOLAY_MZMINE))
			filterGuiPanel = new SavitzkyGolayMZMineGuiPanel();	
		
		if(filterClass.equals(FilterClass.SAVITZKY_GOLAY_JDSP))
			filterGuiPanel = new SavitzkyGolayJdspGuiPanel();
		
		if(filterClass.equals(FilterClass.MOVING_AVERAGE))
			filterGuiPanel = new MovingAverageGuiPanel();
		
		if(filterClass.equals(FilterClass.WEIGHTED_MOVING_AVERAGE))
			filterGuiPanel = new WeightedMovingAverageGuiPanel();
		
		if(filterClass.equals(FilterClass.LOESS))			
			filterGuiPanel = new LoessGuiPanel();
		
		if(filterClass.equals(FilterClass.SMOOTHING_CUBIC_SPLINE)) 
			filterGuiPanel = new SmoothingCubicSplineGuiPanel();
		
		if(filterGuiPanel != null) {			
			filterGuiPanel.loadPreferences();
			filterParameters.add(filterGuiPanel, BorderLayout.CENTER);
			//	Filter f = getSmoothingFilter();
			//	System.err.println("Filter " + filterGuiPanel.getFilterClass().getName() + " - " + f.toString());
		}
		else {
			filterParameters.add(new JPanel(), BorderLayout.CENTER);
		}
		filterParameters.revalidate();
		filterParameters.repaint();		
	}
	
	public Filter getSmoothingFilter() {
		
		if(filterGuiPanel == null)
			return null;
		else			
			return filterGuiPanel.getFilter();
	}
	
	public void setFilterClass(FilterClass fc) {
		filterTypeComboBox.setSelectedItem(fc);
	}
	
	public FilterClass getFilterClass() {
		return (FilterClass) filterTypeComboBox.getSelectedItem();
	}
	
	public void setFilter(Filter newFilter) {
		filterTypeComboBox.setSelectedItem(newFilter.getFilterClass());
		filterGuiPanel.loadFilterParameters(newFilter);
	}
}


