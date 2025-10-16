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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;

public class ChemicalStructureRenderer extends DefaultTableCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1286363309166744954L;
	private static final IChemObjectBuilder bldr = 
			SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(bldr);
	private static final DepictionGenerator dptgen = 
			new DepictionGenerator().withAtomColors().withMolTitle();
	private static final Border selectionBorder = new LineBorder(Color.RED, 2);

	public ChemicalStructureRenderer() {
		super();
		setBackground(Color.WHITE);
		setLayout(new BorderLayout(0, 0));
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		setText("");
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Component rendererComponent = 
				table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());

		if(value == null){
			setText("");
			return this;
		}
		if (value instanceof CompoundIdentity)			
			generateMoleculeDepiction((CompoundIdentity)value);					
		else
			setIcon(null);
		
		if(isSelected)
			setBorder(selectionBorder);
		else
			setBorder(null);
		
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
        if (table.getRowHeight(row) != getPreferredSize().height)
            table.setRowHeight(row, getPreferredSize().height);
		
        setText("");
		return this;
	}
	
	private void generateMoleculeDepiction(CompoundIdentity id) {
		
		if(id.getSmiles() == null || id.getSmiles().isEmpty()) {
			setIcon(null);
			return;
		}
		Depiction dpic = null;
		IAtomContainer mol = null;
		try {
			mol = smipar.parseSmiles(id.getSmiles());
		} catch (Exception e1) {			
			System.out.println("Error parsing SMILES " + id.getSmiles());
			setIcon(null);
			return;		
		}
		if (mol != null) {
			mol.setProperty(CDKConstants.TITLE, id.getName());
			try {
				dpic = dptgen.depict(mol);
			} catch (CDKException e) {
				System.out.println("Error ceating graphics " + id.getSmiles());
				setIcon(null);
				return;	
			}
		}
		if (dpic != null) {
			setIcon(new ImageIcon(dpic.toImg()));
		}
		else
			setIcon(null);
	}
}

















