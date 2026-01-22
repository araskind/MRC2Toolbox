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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.dataimport;

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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBioPolymer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.ProteinBuilderTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.MoleculeProperties;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import io.github.dan2097.jnainchi.InchiStatus;
import net.sf.jniinchi.INCHI_RET;

public class AddCustomCompoundDialog extends JDialog implements ActionListener, ItemListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1895644188920830351L;
	private static final Icon addCompoundIcon = GuiUtils.getIcon("addCompound", 32);
	private JButton btnSave;
	private JTextField nameTextField;
	private JTextField idTextField;
	private JTextField smilesTextField;
	private SmilesParser smipar;
	private DepictionGenerator dptgen;
	private JButton validateButton;
	private JTextArea descriptionTextArea;
	private JTextArea synonymsTextArea;
	private JComboBox idSourceComboBox;

	private JLabel structureLabel;
	private IChemObjectBuilder bldr;
	private InChIGeneratorFactory igfactory;
	private InChIGenerator inChIGenerator;
	private JTextField formulaTextField;
	private JLabel lblFormula;
	private JLabel lblPeptideSequence;
	private JTextField peptideSequenceTextField;
	private SmilesGenerator smilesGenerator;
	private Aromaticity aromaticity;

	public AddCustomCompoundDialog(ActionListener actionListener) {
		super();
		setTitle("Add custom compound");
		setIconImage(((ImageIcon) addCompoundIcon).getImage());
		setSize(new Dimension(800, 480));
		setPreferredSize(new Dimension(800, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{96, 223, 83, 39, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		dataPanel.add(lblName, gbc_lblName);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 4;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		dataPanel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);

		JLabel lblDatabsevendor = new JLabel("Databse/vendor");
		GridBagConstraints gbc_lblDatabsevendor = new GridBagConstraints();
		gbc_lblDatabsevendor.anchor = GridBagConstraints.EAST;
		gbc_lblDatabsevendor.insets = new Insets(0, 0, 5, 5);
		gbc_lblDatabsevendor.gridx = 0;
		gbc_lblDatabsevendor.gridy = 1;
		dataPanel.add(lblDatabsevendor, gbc_lblDatabsevendor);
		
		idSourceComboBox = new JComboBox<CompoundDatabaseEnum>(
				new DefaultComboBoxModel<CompoundDatabaseEnum>(new CompoundDatabaseEnum[] {
										CompoundDatabaseEnum.MRC2_MSMS,
										CompoundDatabaseEnum.METLIN, 
										CompoundDatabaseEnum.NIST_MS,
										
									}));
		idSourceComboBox.setSelectedItem(CompoundDatabaseEnum.MRC2_MSMS);
		idSourceComboBox.addItemListener(this);				
		GridBagConstraints gbc_idSourceComboBox = new GridBagConstraints();
		gbc_idSourceComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_idSourceComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_idSourceComboBox.gridx = 1;
		gbc_idSourceComboBox.gridy = 1;
		dataPanel.add(idSourceComboBox, gbc_idSourceComboBox);

		JLabel lblId = new JLabel("ID");
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 2;
		gbc_lblId.gridy = 1;
		dataPanel.add(lblId, gbc_lblId);

		idTextField = new JTextField();
		idTextField.setText(CompoundDatabaseEnum.MRC2_MSMS.name() + ":");
		idTextField.setEnabled(false);
		GridBagConstraints gbc_idTextField_1 = new GridBagConstraints();
		gbc_idTextField_1.gridwidth = 2;
		gbc_idTextField_1.insets = new Insets(0, 0, 5, 0);
		gbc_idTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_idTextField_1.gridx = 3;
		gbc_idTextField_1.gridy = 1;
		dataPanel.add(idTextField, gbc_idTextField_1);
		idTextField.setColumns(10);

		JLabel lblSmiles = new JLabel("SMILES");
		GridBagConstraints gbc_lblSmiles = new GridBagConstraints();
		gbc_lblSmiles.anchor = GridBagConstraints.EAST;
		gbc_lblSmiles.insets = new Insets(0, 0, 5, 5);
		gbc_lblSmiles.gridx = 0;
		gbc_lblSmiles.gridy = 2;
		dataPanel.add(lblSmiles, gbc_lblSmiles);

		smilesTextField = new JTextField();
		GridBagConstraints gbc_smilesTextField = new GridBagConstraints();
		gbc_smilesTextField.insets = new Insets(0, 0, 5, 0);
		gbc_smilesTextField.gridwidth = 4;
		gbc_smilesTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_smilesTextField.gridx = 1;
		gbc_smilesTextField.gridy = 2;
		dataPanel.add(smilesTextField, gbc_smilesTextField);
		smilesTextField.setColumns(10);
		
		lblPeptideSequence = new JLabel("Peptide sequence");
		GridBagConstraints gbc_lblPeptideSequence = new GridBagConstraints();
		gbc_lblPeptideSequence.anchor = GridBagConstraints.EAST;
		gbc_lblPeptideSequence.insets = new Insets(0, 0, 5, 5);
		gbc_lblPeptideSequence.gridx = 0;
		gbc_lblPeptideSequence.gridy = 3;
		dataPanel.add(lblPeptideSequence, gbc_lblPeptideSequence);
		
		peptideSequenceTextField = new JTextField();
		GridBagConstraints gbc_peptideSequenceTextField = new GridBagConstraints();
		gbc_peptideSequenceTextField.gridwidth = 4;
		gbc_peptideSequenceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_peptideSequenceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_peptideSequenceTextField.gridx = 1;
		gbc_peptideSequenceTextField.gridy = 3;
		dataPanel.add(peptideSequenceTextField, gbc_peptideSequenceTextField);
		peptideSequenceTextField.setColumns(10);

		synonymsTextArea = new JTextArea();
		synonymsTextArea.setBorder(new TitledBorder(null, 
				"Synonyms", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_synonymsTextArea = new GridBagConstraints();
		gbc_synonymsTextArea.gridwidth = 2;
		gbc_synonymsTextArea.insets = new Insets(0, 0, 5, 5);
		gbc_synonymsTextArea.fill = GridBagConstraints.BOTH;
		gbc_synonymsTextArea.gridx = 0;
		gbc_synonymsTextArea.gridy = 4;
		dataPanel.add(synonymsTextArea, gbc_synonymsTextArea);

		structureLabel = new JLabel();
		structureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		structureLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

		JScrollPane scrollPane = new JScrollPane(structureLabel);
		scrollPane.setBorder(new TitledBorder(null, "Structure", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane.getViewport().setBackground(Color.WHITE);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 2;
		gbc_scrollPane.gridy = 4;
		dataPanel.add(scrollPane, gbc_scrollPane);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new TitledBorder(null, "Description", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
		gbc_descriptionTextArea.gridheight = 2;
		gbc_descriptionTextArea.insets = new Insets(0, 0, 0, 5);
		gbc_descriptionTextArea.gridwidth = 2;
		gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_descriptionTextArea.gridx = 0;
		gbc_descriptionTextArea.gridy = 5;
		dataPanel.add(descriptionTextArea, gbc_descriptionTextArea);

		lblFormula = new JLabel("Formula");
		GridBagConstraints gbc_lblFormula = new GridBagConstraints();
		gbc_lblFormula.insets = new Insets(0, 0, 0, 5);
		gbc_lblFormula.anchor = GridBagConstraints.EAST;
		gbc_lblFormula.gridx = 2;
		gbc_lblFormula.gridy = 6;
		dataPanel.add(lblFormula, gbc_lblFormula);

		formulaTextField = new JTextField();
		formulaTextField.setEditable(false);
		GridBagConstraints gbc_formulaTextField = new GridBagConstraints();
		gbc_formulaTextField.gridwidth = 2;
		gbc_formulaTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formulaTextField.gridx = 3;
		gbc_formulaTextField.gridy = 6;
		dataPanel.add(formulaTextField, gbc_formulaTextField);
		formulaTextField.setColumns(10);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		validateButton = new JButton("Validate input");
		validateButton.setActionCommand(MainActionCommands.VALIDATE_CUSTOM_COMPOUND_DATA.getName());
		validateButton.addActionListener(this);
		panel.add(validateButton);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton(MainActionCommands.SAVE_CUSTOM_COMPOUND_DATA.getName());
		btnSave.setActionCommand(MainActionCommands.SAVE_CUSTOM_COMPOUND_DATA.getName());
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
		pack();
	}

	private void disposeWithoutSavingPreferences() {
		super.dispose();
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
		if(getName().isEmpty())
			errors.add("Name can not be empty.");				

		if(getSmiles().isEmpty() && getPeptideSequence().isEmpty())
			errors.add("Please enter SMILES string or peptide sequence (single letter code).");
		
		IAtomContainer mol = showStructure();	
		if(mol == null)
			errors.add("SMILES string not valid.");
		else {
			CompoundIdentity existingId = null;
			String inchiKey = null;			
			try {
				inChIGenerator = igfactory.getInChIGenerator(mol);
//				INCHI_RET ret = inChIGenerator.getReturnStatus();
				InchiStatus inchiStatus = inChIGenerator.getStatus();
				if (inchiStatus.equals(InchiStatus.WARNING)) {
					System.out.println("InChI warning: " + inChIGenerator.getMessage());
				} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
					errors.add("InChI failed: [" + inChIGenerator.getMessage() + "]");
				}
				inchiKey = inChIGenerator.getInchiKey();
			} catch (CDKException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				existingId = CompoundDatabaseUtils.getCompoundBySmiles(getSmiles());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(existingId == null) {
				try {
					existingId = CompoundDatabaseUtils.getCompoundByInChiKey(inchiKey);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(existingId != null) {
				errors.add("Compound with the same structure/SMILE string (ID " +
						existingId.getPrimaryDatabaseId() +
						", " + existingId.getPrimaryDatabase().getName() + ") " +
						"is already in the database.");
			}
		}		
		if(getIdSourceDatabase() == null)
			errors.add("Source database has to be selected");

		if (getAccession().isEmpty())
			errors.add("Compound ID can not be empty.");
		else if (getAccession().endsWith(":") 
				&& !getIdSourceDatabase().equals(CompoundDatabaseEnum.MRC2_MSMS)) {
			errors.add("Compound ID contains only database prefix.");
		} else {
			if (!getIdSourceDatabase().equals(CompoundDatabaseEnum.MRC2_MSMS)) {

				CompoundIdentity existingId = null;
				try {
					existingId = CompoundDatabaseUtils.getCompoundById(getAccession());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (existingId != null) {
					errors.add("Compound with ID " + existingId.getPrimaryDatabaseId() + " ("
							+ existingId.getPrimaryDatabase().getName() + ") " + 
							"is already in the database.");
				}
			}
		}
		return errors;
	}

	public CompoundDatabaseEnum getIdSourceDatabase() {
		return (CompoundDatabaseEnum)idSourceComboBox.getSelectedItem();
	}

	public String getAccession() {
		return idTextField.getText().trim();
	}

	public String getName() {
		return nameTextField.getText().trim();
	}

	public String getSmiles(){
		return smilesTextField.getText().trim();
	}

	public String getDescription(){
		return descriptionTextArea.getText().trim();
	}
	
	public String getPeptideSequence(){
		return peptideSequenceTextField.getText().trim();
	}

	public Collection<String> getSynonymList() {

		String[] idArray =
				 synonymsTextArea.getText().trim().replaceAll("[\\,,;,:,\\(,\\),\\[,\\],\\{,\\}]", " ").
				 replaceAll("\\s+", " ").replace("\r", "\n").trim().split("\n", 0);
		Collection<String> idList = new TreeSet<String>();
		for (int i = 0; i < idArray.length; i++) {

			if (idArray[i].trim().isEmpty())
				continue;

			idList.add(idArray[i].trim());
		}
		return idList;
	}

	private IAtomContainer showStructure() {
		
		String smiles = getSmiles();
		if(smiles.isEmpty()) {
			
			if(getPeptideSequence().isEmpty())
				return null;
			
			smiles = getPeptideSmiles(getPeptideSequence());
			if(smiles == null)
				return null;
			
			smilesTextField.setText(smiles);
		}
		structureLabel.setIcon(null);
		Depiction dpic = null;
		IAtomContainer mol = null;
		try {
			mol = smipar.parseSmiles(smiles);
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		if (mol != null) {
			try {
				dpic = dptgen.depict(mol);
				IMolecularFormula molFormula = MolecularFormulaManipulator.getMolecularFormula(mol);
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
	
	private String getPeptideSmiles(String pepSeq) {
		
		IBioPolymer peptide = null;
		String smiles = null;
		String oneLetter = pepSeq;
		System.out.println(oneLetter);
		try {
			peptide = ProteinBuilderTool.createProtein(oneLetter);
	        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(peptide);
	        CDKHydrogenAdder.getInstance(peptide.getBuilder())
	                        .addImplicitHydrogens(peptide);
	        aromaticity.apply(peptide);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(peptide != null){
			try {
				AtomContainer mpep = new AtomContainer(peptide);
				smiles = smilesGenerator.create(mpep);
				inChIGenerator = igfactory.getInChIGenerator(mpep);
				INCHI_RET ret = inChIGenerator.getReturnStatus();
				if (ret == INCHI_RET.WARNING) {

					System.out.println("InChI warning: " + inChIGenerator.getMessage());
				} else if (ret != INCHI_RET.OKAY) {

					throw new CDKException(
							"InChI failed: " + ret.toString() + " [" + inChIGenerator.getMessage() + "]");
				}
				String inchi = inChIGenerator.getInchi();
				String auxinfo = inChIGenerator.getAuxInfo();
				System.out.println(inchi);
				System.out.println(inChIGenerator.getInchiKey());
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return smiles;	
	}
	
	private void drawPeptideStructure(String oneLetterSequence) {
		
		IChemObjectBuilder bldr = SilentChemObjectBuilder.getInstance();
		SmilesParser smipar = new SmilesParser(bldr);
		smilesGenerator = new SmilesGenerator(SmiFlavor.Canonical);
		DepictionGenerator dptgen = new DepictionGenerator().withAtomColors();
		IBioPolymer peptide = null;
		String smiles = null;
		try {
			peptide = ProteinBuilderTool.createProtein(oneLetterSequence);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		if(peptide != null){
			try {
				AtomContainer mpep = new AtomContainer(peptide);
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mpep);
				CDKHydrogenAdder.getInstance(mpep.getBuilder()).addImplicitHydrogens(mpep);
				smiles = smilesGenerator.create(mpep);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(smiles == null)
			return;
		
		structureLabel.setIcon(null);
		Depiction dpic = null;
		IAtomContainer mol = null;
		try {
			mol = smipar.parseSmiles(smiles);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (mol != null) {
			try {
				dpic = dptgen.depict(mol);
			}
			catch (CDKException e) {
				e.printStackTrace();
			}
		}
		if (dpic != null) {
			structureLabel.setIcon(new ImageIcon(dpic.toImg()));
			structureLabel.setHorizontalAlignment(SwingConstants.CENTER);
			structureLabel.setVerticalAlignment(SwingConstants.CENTER);
		}
	}

	public IAtomContainer generateMolData() throws CDKException {

		String smiles = getSmiles();
		if(smiles.isEmpty())
			smiles = getPeptideSmiles(getPeptideSequence());

		if (smiles == null)
			return null;

		smilesTextField.setText(smiles);
		structureLabel.setIcon(null);
		Depiction dpic = null;
		IAtomContainer mol = null;
		try {
			mol = smipar.parseSmiles(smiles);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		if(mol != null) {
			try {
				igfactory = InChIGeneratorFactory.getInstance();
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			inChIGenerator = igfactory.getInChIGenerator(mol);
			INCHI_RET ret = inChIGenerator.getReturnStatus();
			if (ret == INCHI_RET.WARNING || ret == INCHI_RET.OKAY) {

				mol.setProperty(MoleculeProperties.INCHI.name(), inChIGenerator.getInchi());
				mol.setProperty(MoleculeProperties.INCHIKEY.name(), inChIGenerator.getInchiKey());
				return mol;
			}
		}
		return null;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof CompoundDatabaseEnum) {
			
			CompoundDatabaseEnum db = (CompoundDatabaseEnum)e.getItem();
			idTextField.setText(db.name() + ":");
			if(db.equals(CompoundDatabaseEnum.MRC2_MSMS))
				idTextField.setEnabled(false);
			else
				idTextField.setEnabled(true);
				
		}
	}
}






















