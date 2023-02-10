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

package edu.umich.med.mrc2.datoolbox.gui.io;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureSetSelectionPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6201959842328662794L;
	private DataPipeline dataPipeline;
	private JComboBox<MsFeatureSet> featureSetSelector;

	public FeatureSetSelectionPanel() {
		
		super();
		setPreferredSize(new Dimension(400, 70));
		setMinimumSize(new Dimension(350, 70));
		setBorder(new TitledBorder(null, "", 
				TitledBorder.LEADING, TitledBorder.TOP, 
				new Font("Tahoma", Font.BOLD, 11), null));
		this.dataPipeline = null;		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);

		featureSetSelector = new JComboBox<MsFeatureSet>();
		featureSetSelector.setPreferredSize(new Dimension(300, 25));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		add(featureSetSelector, gbc_comboBox);
	}
	
	public DataPipeline getDataPipeline() {		
		return dataPipeline;
	}
	
	public MsFeatureSet getSelectedFeatureSet(){
		
		if(featureSetSelector.getSelectedIndex() > -1)
			return (MsFeatureSet) featureSetSelector.getSelectedItem();
		else
			return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateFeatureSetSelector() {

		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		
		if(project != null && dataPipeline != null){
			
			MsFeatureSet[] fSets = project.getMsFeatureSetsForDataPipeline(dataPipeline)
					.toArray(new MsFeatureSet[project.getMsFeatureSetsForDataPipeline(dataPipeline).size()]);
			featureSetSelector.setModel(new SortedComboBoxModel(fSets));
			
			if(project.getActiveFeatureSetForDataPipeline(dataPipeline) != null)
				featureSetSelector.setSelectedItem(
						project.getActiveFeatureSetForDataPipeline(dataPipeline));
			else
				featureSetSelector.setSelectedIndex(-1);
		}
		else{
			featureSetSelector.setModel(new SortedComboBoxModel());
		}
	}

	public void setDataPipeline(DataPipeline newDataPipeline){
		
		this.dataPipeline = newDataPipeline;
		((TitledBorder)getBorder()).setTitle(newDataPipeline.getName());
		populateFeatureSetSelector();
	}
		
	public void setSelectorEnabled(boolean enabled){
		
		featureSetSelector.setEnabled(enabled);
	}
}
