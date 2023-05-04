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
//	private static final Icon editIdStatusIcon = GuiUtils.getIcon("editIdStatus", 32);
//	private static final Icon deleteIdStatusIcon = GuiUtils.getIcon("deleteIdStatus", 32);	
	
	@SuppressWarnings("unused")
	private JButton
		simpleDataPullButton,
		editIdStatusButton,
		deleteIdStatusButton;

	private JComboBox<CompoundDatabaseEnum> dbTypecomboBox;

	public CompoundMsReadyCuratorToolbar(ActionListener commandListener) {

		super(commandListener);
		
		dbTypecomboBox = new JComboBox<CompoundDatabaseEnum>();
		dbTypecomboBox.setPreferredSize(new Dimension(200, 25));
		DefaultComboBoxModel<CompoundDatabaseEnum> model = 
				new DefaultComboBoxModel<CompoundDatabaseEnum>(
					new CompoundDatabaseEnum[] {
							CompoundDatabaseEnum.HMDB,
							CompoundDatabaseEnum.DRUGBANK,
							CompoundDatabaseEnum.FOODB,
							CompoundDatabaseEnum.T3DB,
							CompoundDatabaseEnum.LIPIDMAPS,
							CompoundDatabaseEnum.CHEBI,
							CompoundDatabaseEnum.NATURAL_PRODUCTS_ATLAS,
							CompoundDatabaseEnum.COCONUT,
					});
		dbTypecomboBox.setModel(model);
		
		add(dbTypecomboBox);
		
		addSeparator();

		simpleDataPullButton = GuiUtils.addButton(this, null, simpleDataPullIcon, commandListener,
				MainActionCommands.FETCH_COMPOUND_DATA_FOR_CURATION.getName(),
				MainActionCommands.FETCH_COMPOUND_DATA_FOR_CURATION.getName(),
				buttonDimension);

//		editIdStatusButton = GuiUtils.addButton(this, null, editIdStatusIcon, commandListener,
//				MainActionCommands.EDIT_ID_LEVEL_DIALOG_COMMAND.getName(),
//				MainActionCommands.EDIT_ID_LEVEL_DIALOG_COMMAND.getName(),
//				buttonDimension);
//
//		deleteIdStatusButton = GuiUtils.addButton(this, null, deleteIdStatusIcon, commandListener,
//				MainActionCommands.DELETE_ID_LEVEL_COMMAND.getName(),
//				MainActionCommands.DELETE_ID_LEVEL_COMMAND.getName(),
//				buttonDimension);
	}
	
	public CompoundDatabaseEnum getSelectedDatabase() {
		return (CompoundDatabaseEnum) dbTypecomboBox.getSelectedItem();
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
