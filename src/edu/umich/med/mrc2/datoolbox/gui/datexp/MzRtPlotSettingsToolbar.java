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

package edu.umich.med.mrc2.datoolbox.gui.datexp;

import java.awt.Dimension;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MzRtPlotSettingsToolbar extends CommonToolbar{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4281882650802316371L;
	private JComboBox<DataScale> dataScaleComboBox;

	public MzRtPlotSettingsToolbar(DataExplorerPlotPanel parentPlot, ItemListener dropdownListener) {

		super(parentPlot);

		// Add data scale options
		add(new JLabel("  Scale: "));
		dataScaleComboBox = new JComboBox<DataScale>();
		dataScaleComboBox.setModel(new DefaultComboBoxModel<DataScale>(
				new DataScale[] {DataScale.LN, DataScale.LOG10, DataScale.SQRT}));
		dataScaleComboBox.setSelectedItem(DataScale.LN);
		dataScaleComboBox.addItemListener(dropdownListener);
		dataScaleComboBox.setMaximumSize(new Dimension(120, 26));
		add(dataScaleComboBox);
	}
	
	public void setDataScale(DataScale newScale) {
		
		if(newScale.equals(DataScale.LN) || newScale.equals(DataScale.LOG10) ||  newScale.equals(DataScale.SQRT))
			dataScaleComboBox.setSelectedItem(newScale);
	}
	
	public DataScale getDataScale() {
		return (DataScale)dataScaleComboBox.getSelectedItem();
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(
			DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub
		
	}
	
}


