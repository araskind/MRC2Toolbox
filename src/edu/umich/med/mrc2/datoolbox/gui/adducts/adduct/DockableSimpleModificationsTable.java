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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class DockableSimpleModificationsTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("showKnowns", 16);
	private SimpleNeutraModificationTable simpleNeutraModificationlTable;

	public DockableSimpleModificationsTable(ModificationType modType) {

		super("DockableSimpleModificationsTable", componentIcon, "Molecular structure", null,
				Permissions.MIN_MAX_STACK);
		setCloseable(false);

		if (modType.equals(ModificationType.LOSS))
			setTitleText("Neutral losses");
		if (modType.equals(ModificationType.REPEAT))
			setTitleText("Neutral adducts");
		
		simpleNeutraModificationlTable = new SimpleNeutraModificationTable();
		if(modType.equals(ModificationType.LOSS))
			simpleNeutraModificationlTable.setTableModelFromAdductList(
					AdductManager.getNeutralLosses());
		
		if(modType.equals(ModificationType.REPEAT))
			simpleNeutraModificationlTable.setTableModelFromAdductList(
					AdductManager.getNeutralAdducts());
		
		getContentPane().add(new JScrollPane(simpleNeutraModificationlTable), 
				BorderLayout.CENTER);
	}

	public SimpleNeutraModificationTable getAdductTable() {
		return simpleNeutraModificationlTable;
	}
}
