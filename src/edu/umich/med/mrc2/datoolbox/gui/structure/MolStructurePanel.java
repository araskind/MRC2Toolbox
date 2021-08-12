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
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

public class MolStructurePanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = -1781587354092859755L;
//	private static final Dimension structureDimension = new Dimension(200, 200);
//	int width, height;
	private String smiles;
//	private Rectangle drawArea;
	private Font labelFont = new Font("Tahoma", Font.BOLD, 12);
	private JLabel structureLabel;
	private IChemObjectBuilder bldr;
	private SmilesParser smipar;
	private DepictionGenerator dptgen;

	public MolStructurePanel() {

		super(new BorderLayout(0, 0));

		setBackground(Color.WHITE);
//		setPreferredSize(structureDimension);
		setAlignmentY(Component.TOP_ALIGNMENT);
		setForeground(new Color(236, 233, 216));
		setLayout(new BorderLayout(0, 0));
		structureLabel = new JLabel();
		structureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		structureLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		add(structureLabel, BorderLayout.CENTER);

//		width = (int) structureDimension.getWidth();
//		height = (int) structureDimension.getHeight();
//		drawArea = new Rectangle(0, 0, width, height);

		bldr = SilentChemObjectBuilder.getInstance();
		smipar = new SmilesParser(bldr);
		dptgen = new DepictionGenerator().withAtomColors();
	}

	public void clearPanel() {
		structureLabel.setIcon(null);
	}

	public void showStructure(IAtomContainer mol) {

		structureLabel.setIcon(null);
		Depiction dpic = null;
		if (mol != null) {
			try {
				dpic = dptgen.depict(mol);
			} catch (CDKException e) {
				// e.printStackTrace();
			}
		}
		if (dpic != null) {
			structureLabel.setIcon(new ImageIcon(dpic.toImg()));
			structureLabel.setHorizontalAlignment(SwingConstants.CENTER);
			structureLabel.setVerticalAlignment(SwingConstants.CENTER);
		}
	}

	public void showStructure(String cpdSmiles) {

		smiles = cpdSmiles;
		structureLabel.setIcon(null);

		Depiction dpic = null;
		IAtomContainer mol = null;
		if (smiles != null && !smiles.isEmpty() && !smiles.equals("NoSmile")) {

			try {
				mol = smipar.parseSmiles(smiles);
				//	IMolecularFormula molFormula = MolecularFormulaManipulator.getMolecularFormula(mol);
				//	System.out.println("Charge: " + Integer.toString(molFormula.getCharge()));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (mol != null) {
				try {
					dpic = dptgen.depict(mol);
				} catch (CDKException e) {
					// e.printStackTrace();
				}
			}
			if (dpic != null) {
				structureLabel.setIcon(new ImageIcon(dpic.toImg()));
				structureLabel.setHorizontalAlignment(SwingConstants.CENTER);
				structureLabel.setVerticalAlignment(SwingConstants.CENTER);
			}
		}
	}
	
	public boolean smilesStringIsValid(String smiles) {
		
		if (smiles == null || smiles.isEmpty())
			return false;
		
		try {
			smipar.parseSmiles(smiles);
		} catch (Exception e1) {
			return false;
		}
		return true;
	}
}
















