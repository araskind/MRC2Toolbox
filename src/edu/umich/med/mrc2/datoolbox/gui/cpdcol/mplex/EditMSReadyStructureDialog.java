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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixtureComponent;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class EditMSReadyStructureDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9112162512964319092L;
	private CompoundMultiplexMixtureComponent mmComponent;
	
	private JTextField smilesTextField;
	private SmilesParser smipar;
	private DepictionGenerator dptgen;
	private SmilesGenerator smilesGenerator;
	private Aromaticity aromaticity;
	private JButton validateButton;
	private JLabel structureLabel;
	private IChemObjectBuilder bldr;
	private InChIGeneratorFactory igfactory;
	private InChIGenerator inChIGenerator;
	private JTextField formulaTextField;
	private JLabel lblFormula;
		
	public EditMSReadyStructureDialog(
			CompoundMultiplexMixtureComponent mmComponent,
			ActionListener actionListener) {
		super();
		this.mmComponent = mmComponent;
		
		setPreferredSize(new Dimension(640, 480));
		setSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("SMILES");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		smilesTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		dataPanel.add(smilesTextField, gbc_textField);
		
		JLabel lblNewLabel_1 = new JLabel("Formula");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		formulaTextField = new JTextField();
		formulaTextField.setEditable(false);
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 0);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 1;
		dataPanel.add(formulaTextField, gbc_textField_1);
		
		structureLabel = new JLabel();
		structureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		structureLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		JScrollPane scrollPane = new JScrollPane(structureLabel);
		scrollPane.setBorder(
				new TitledBorder(null, "Structure", 
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane.getViewport().setBackground(Color.WHITE);
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.gridwidth = 2;
		gbc_lblNewLabel_2.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		dataPanel.add(scrollPane, gbc_lblNewLabel_2);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton validateButton = new JButton("Validate input");
		validateButton.setActionCommand(MainActionCommands.VALIDATE_CUSTOM_COMPOUND_DATA.getName());
		validateButton.addActionListener(this);
		panel.add(validateButton);
		
		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		bldr = SilentChemObjectBuilder.getInstance();
		smipar = new SmilesParser(bldr);
		smilesGenerator = new SmilesGenerator(SmiFlavor.Canonical|SmiFlavor.UseAromaticSymbols);
		dptgen = new DepictionGenerator().withAtomColors();
		aromaticity = new Aromaticity(
				ElectronDonation.cdk(),
                Cycles.or(Cycles.all(), Cycles.all(6)));
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(mmComponent.getCCComponent().getMsReadySmiles() != null) {
			smilesTextField.setText(mmComponent.getCCComponent().getMsReadySmiles());
			showStructure();
		}
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.VALIDATE_CUSTOM_COMPOUND_DATA.getName())) {

			Collection<String>errors = validateInputData();
			if(!errors.isEmpty())
				MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
		}
	}

	public Collection<String> validateInputData() {

		Collection<String>errors = new ArrayList<String>();
		if(getSmiles().isEmpty())
			errors.add("Please enter SMILES string");
		
		IAtomContainer mol = showStructure();	
		if(mol == null)
			errors.add("SMILES string not valid.");
//		else {
//			String inchiKey = null;			
//			try {
//				inChIGenerator = igfactory.getInChIGenerator(mol);
//				InchiStatus inchiStatus = inChIGenerator.getStatus();
//				if (inchiStatus.equals(InchiStatus.WARNING)) {
//					System.out.println("InChI warning: " + inChIGenerator.getMessage());
//				} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
//					errors.add("InChI failed: [" + inChIGenerator.getMessage() + "]");
//				}
//				inchiKey = inChIGenerator.getInchiKey();
//			} catch (CDKException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}		
		return errors;
	}
	
	private IAtomContainer showStructure() {
		
		String smiles = getSmiles();
		if(smiles.isEmpty())
			return null;
			
		structureLabel.setIcon(null);
		Depiction dpic = null;
		IAtomContainer mol = null;
		try {
			mol = smipar.parseSmiles(smiles);
		} catch (Exception e1) {
//			e1.printStackTrace();
			return null;
		}
		if (mol != null) {
			try {
				dpic = dptgen.depict(mol);
				IMolecularFormula molFormula = 
						MolecularFormulaManipulator.getMolecularFormula(mol);
				formulaTextField.setText(MolecularFormulaManipulator.getString(molFormula));
			}
			catch (CDKException e) {
				// e.printStackTrace();
			}
		}
		if (dpic != null) {
			structureLabel.setIcon(new ImageIcon(dpic.toImg()));
			structureLabel.setHorizontalAlignment(SwingConstants.CENTER);
			structureLabel.setVerticalAlignment(SwingConstants.CENTER);
		}
		return mol;
	}

	public String getSmiles(){
		return smilesTextField.getText().trim();
	}
	
	public String getFormula(){
		return formulaTextField.getText().trim();
	}
	
	public CompoundMultiplexMixtureComponent getMmComponent() {
		return mmComponent;
	}
}
