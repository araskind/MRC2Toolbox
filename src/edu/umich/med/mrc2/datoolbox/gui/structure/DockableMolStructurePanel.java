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

package edu.umich.med.mrc2.datoolbox.gui.structure;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import org.openscience.cdk.interfaces.IAtomContainer;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMolStructurePanel extends DefaultSingleCDockable {

//	private String smiles;
//	private Font labelFont = new Font("Tahoma", Font.BOLD, 12);
//	private JLabel structureLabel;
//	private IChemObjectBuilder bldr;
//	private SmilesParser smipar;
//	private DepictionGenerator dptgen;
	
	private MolStructurePanel structurePanel;

	private static final Icon componentIcon = GuiUtils.getIcon("showKnowns", 16);

	public DockableMolStructurePanel(String id) {

		super(id, componentIcon, "Molecular structure", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		structurePanel = new MolStructurePanel();
		JScrollPane scrollPane = new JScrollPane(structurePanel);
		scrollPane.getViewport().setBackground(Color.WHITE);
		add(scrollPane, BorderLayout.CENTER);

//		setLayout(new BorderLayout(0, 0));
//
//		structureLabel = new JLabel();
//		structureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//		structureLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
//
//		JScrollPane scrollPane = new JScrollPane(structureLabel);
//		scrollPane.getViewport().setBackground(Color.WHITE);
//		add(scrollPane, BorderLayout.CENTER);
//
//		bldr = SilentChemObjectBuilder.getInstance();
//		smipar = new SmilesParser(bldr);
//		dptgen = new DepictionGenerator().withAtomColors();
	}

	public synchronized void clearPanel() {
//		structureLabel.setIcon(null);
		structurePanel.clearPanel();
	}

	public void showStructure(String cpdSmiles) {

		structurePanel.showStructure(cpdSmiles);
		
//		smiles = cpdSmiles;
//		structureLabel.setIcon(null);
//
//		Depiction dpic = null;
//		IAtomContainer mol = null;
//
//		if (smiles != null && !smiles.isEmpty() && !smiles.equals("NoSmile")) {
//
//			try {
//				mol = smipar.parseSmiles(smiles);
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
//			if (mol != null) {
//				try {
//					dpic = dptgen.depict(mol);
//				}
//				catch (CDKException e) {
//
//					// e.printStackTrace();
//				}
//			}
//			if (dpic != null) {
//				structureLabel.setIcon(new ImageIcon(dpic.toImg()));
//				structureLabel.setHorizontalAlignment(SwingConstants.CENTER);
//				structureLabel.setVerticalAlignment(SwingConstants.CENTER);
//			}
//		}
	}
	
	public void showStructure(IAtomContainer mol) {
		structurePanel.showStructure(mol);
	}
}














