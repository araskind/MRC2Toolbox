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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class CompoundMsReadyCuratorToolbar extends CommonToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1934511837376564647L;
	
	private static final Icon simpleDataPullIcon = GuiUtils.getIcon("downloadDocument", 32);
	private static final Icon cpdStdIcon = GuiUtils.getIcon("standardCompound", 32);
	private static final Icon tautomerIcon = GuiUtils.getIcon("tautomerSettings", 32);
	private static final Icon zwitterIcon = GuiUtils.getIcon("gln-zwitter", 32);
	
	@SuppressWarnings("unused")
	private JButton
		simpleDataPullButton,
		editCompoundStandardizerSettingsButton,
		editTautomerGeneratorSettingsButton,
		editZwitterIonGeneratorSettingsButton;

	private JComboBox<CompoundDatabaseEnum> dbTypecomboBox;

	public CompoundMsReadyCuratorToolbar(ActionListener commandListener) {

		super(commandListener);
		
		dbTypecomboBox = new JComboBox<CompoundDatabaseEnum>();
		dbTypecomboBox.setMaximumSize(new Dimension(250, 30));
		//dbTypecomboBox.setPreferredSize(new Dimension(200, 25));
		DefaultComboBoxModel<CompoundDatabaseEnum> model = 
				new DefaultComboBoxModel<CompoundDatabaseEnum>(
					new CompoundDatabaseEnum[] {
							CompoundDatabaseEnum.HMDB,
							CompoundDatabaseEnum.DRUGBANK,
							CompoundDatabaseEnum.FOODB,
							CompoundDatabaseEnum.T3DB,
							CompoundDatabaseEnum.LIPIDMAPS,							
							CompoundDatabaseEnum.NIST_MS,
//							CompoundDatabaseEnum.CHEBI,
//							CompoundDatabaseEnum.NATURAL_PRODUCTS_ATLAS,
							CompoundDatabaseEnum.COCONUT,
					});
		dbTypecomboBox.setModel(model);
		
		add(dbTypecomboBox);

		simpleDataPullButton = GuiUtils.addButton(this, null, simpleDataPullIcon, commandListener,
				MainActionCommands.FETCH_COMPOUND_DATA_FOR_CURATION.getName(),
				MainActionCommands.FETCH_COMPOUND_DATA_FOR_CURATION.getName(),
				buttonDimension);
		addSeparator();

		editCompoundStandardizerSettingsButton = GuiUtils.addButton(this, null, cpdStdIcon, commandListener,
				MainActionCommands.EDIT_COMPOUND_STANDARDIZER_SETTINGS_COMMAND.getName(),
				MainActionCommands.EDIT_COMPOUND_STANDARDIZER_SETTINGS_COMMAND.getName(),
				buttonDimension);

		editTautomerGeneratorSettingsButton = GuiUtils.addButton(this, null, tautomerIcon, commandListener,
				MainActionCommands.EDIT_TAUTOMER_GENERATOR_SETTINGS_COMMAND.getName(),
				MainActionCommands.EDIT_TAUTOMER_GENERATOR_SETTINGS_COMMAND.getName(),
				buttonDimension);
		
		editZwitterIonGeneratorSettingsButton = GuiUtils.addButton(this, null, zwitterIcon, commandListener,
				MainActionCommands.EDIT_ZWITTER_ION_GENERATOR_SETTINGS_COMMAND.getName(),
				MainActionCommands.EDIT_ZWITTER_ION_GENERATOR_SETTINGS_COMMAND.getName(),
				buttonDimension);
		
		addSeparator();
		
		GuiUtils.addButton(this, null, tautomerIcon, commandListener,
				MainActionCommands.BATCH_GENERATE_TAUTOMERS.getName(),
				MainActionCommands.BATCH_GENERATE_TAUTOMERS.getName(),
				buttonDimension);
	}
	
	public CompoundDatabaseEnum getSelectedDatabase() {
		return (CompoundDatabaseEnum) dbTypecomboBox.getSelectedItem();
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
