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

package edu.umich.med.mrc2.datoolbox.gui.structure;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import org.openscience.cdk.interfaces.IAtomContainer;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMolStructurePanel extends DefaultSingleCDockable {
	
	private MolStructurePanel structurePanel;

	private static final Icon componentIcon = GuiUtils.getIcon("showKnowns", 16);

	public DockableMolStructurePanel(String id, String name) {

		super(id, componentIcon, name, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		structurePanel = new MolStructurePanel();
		JScrollPane scrollPane = new JScrollPane(structurePanel);
		scrollPane.getViewport().setBackground(Color.WHITE);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	public DockableMolStructurePanel(String id) {
		this(id, "Molecular structure");
	}

	public synchronized void clearPanel() {
		structurePanel.clearPanel();
	}

	public IAtomContainer showStructure(String cpdSmiles) {
		return structurePanel.showStructure(cpdSmiles);
	}
	
	public void showStructure(IAtomContainer mol) {
		structurePanel.showStructure(mol);
	}
}














