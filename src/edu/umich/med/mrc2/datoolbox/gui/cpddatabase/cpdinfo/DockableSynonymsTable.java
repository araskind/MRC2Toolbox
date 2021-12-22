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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundNameSet;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableSynonymsTable   extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("synonyms", 16);
	private CompoundSynonymTable compoundSynonymsTable;
	private SynonymsToolbar toolbar;
	private CompoundNameSet nameSet;

	public DockableSynonymsTable(ActionListener commandListener) {

		super("DockableSynonymsTable", componentIcon, "Synonyms", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));
		toolbar = new SynonymsToolbar(commandListener);
		add(toolbar, BorderLayout.NORTH);

		compoundSynonymsTable = new CompoundSynonymTable();
		add(new JScrollPane(compoundSynonymsTable), BorderLayout.CENTER);
	}
	
	public DockableSynonymsTable(ActionListener commandListener, boolean editable) {

		super("Synonyms", componentIcon, "Synonyms", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));
		if(editable) {
			toolbar = new SynonymsToolbar(commandListener);
			add(toolbar, BorderLayout.NORTH);
		}
		compoundSynonymsTable = new CompoundSynonymTable();
		if(!editable)
			compoundSynonymsTable.disableEditing();
		
		add(new JScrollPane(compoundSynonymsTable), BorderLayout.CENTER);
	}

	public void setModelFromCompoundNameSet(CompoundNameSet nameSet) {
		compoundSynonymsTable.setModelFromCompoundNameSet(nameSet);
	}

	public synchronized void clearTable() {
		compoundSynonymsTable.clearTable();
	}

	public void loadCompoundData(CompoundIdentity cpd) {

		nameSet = null;
		try {
			nameSet = CompoundDatabaseUtils.getSynonyms(cpd.getPrimaryDatabaseId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(nameSet != null)
			compoundSynonymsTable.setModelFromCompoundNameSet(nameSet);
	}

	public Map<String,Boolean>getSelectedNames(){
		return compoundSynonymsTable.getSelectedNames();
	}

	public CompoundNameSet getCurrentNameSet() {
		return compoundSynonymsTable.getCurrentNameSet();
	}

	/**
	 * @return the nameSet
	 */
	public CompoundNameSet getNameSet() {
		return nameSet;
	}

	public CompoundSynonymTable getTable() {
		return compoundSynonymsTable;
	}

	public void loadNameSet(CompoundNameSet nameSet2) {
		this.nameSet = nameSet2;
		compoundSynonymsTable.setModelFromCompoundNameSet(nameSet);
	}
}





