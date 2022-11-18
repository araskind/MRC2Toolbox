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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBioPolymer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.ProteinBuilderTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDbConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.structure.MolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import net.sf.jniinchi.INCHI_RET;

public class SmilesVerifier extends JFrame implements ActionListener, WindowListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -2562063139898922971L;
	public static void main(String[] args) {

		SmilesVerifier sm = new SmilesVerifier();
		sm.setVisible(true);
	}
	private JTextField smilesTextField;

	private MolStructurePanel molStructurePanel;
	private JTextField pepSeqTextField;
	private SmilesGenerator smilesGenerator;
	private InChIGeneratorFactory igfactory;
	private InChIGenerator inChIGenerator;
	private File inputFile;
	private File baseDirectory;
	private JLabel pepInputFileLabel;
	private JTextField inchiTextField;

	private static final String SHOW_SMILES = "SHOW_SMILES";
	private static final String SHOW_INCHI = "SHOW_INCHI";
	private static final String SHOW_PEPTIDE = "SHOW_PEPTIDE";
	private static final String VERIFY_DB = "VERIFY_DB";
	private static final String SELECT_PEPTIDE_INPUT = "SELECT_PEPTIDE_INPUT";
	private static final String CREATE_PEPTIDE_SMILES = "CREATE_PEPTIDE_SMILES";

	public SmilesVerifier() {

		initGui();
		//smilesGenerator = new SmilesGenerator(SmiFlavor.Canonical | SmiFlavor.UseAromaticSymbols);
		smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initGui(){

		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		setSize(new Dimension(600, 600));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 86, 89, 0};
		gbl_panel_1.rowHeights = new int[]{23, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JLabel lblSmiles = new JLabel("SMILES");
		GridBagConstraints gbc_lblSmiles = new GridBagConstraints();
		gbc_lblSmiles.insets = new Insets(0, 0, 5, 5);
		gbc_lblSmiles.anchor = GridBagConstraints.EAST;
		gbc_lblSmiles.gridx = 0;
		gbc_lblSmiles.gridy = 0;
		panel_1.add(lblSmiles, gbc_lblSmiles);

		smilesTextField = new JTextField();
		GridBagConstraints gbc_smilesTextField = new GridBagConstraints();
		gbc_smilesTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_smilesTextField.insets = new Insets(0, 0, 5, 5);
		gbc_smilesTextField.gridx = 1;
		gbc_smilesTextField.gridy = 0;
		panel_1.add(smilesTextField, gbc_smilesTextField);
		smilesTextField.setColumns(10);

		JButton btnNewButton = new JButton("Show structure");
		btnNewButton.addActionListener(this);
		btnNewButton.setActionCommand(SHOW_SMILES);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.anchor = GridBagConstraints.NORTH;
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		panel_1.add(btnNewButton, gbc_btnNewButton);

		JLabel lblInchi = new JLabel("InChi");
		GridBagConstraints gbc_lblInchi = new GridBagConstraints();
		gbc_lblInchi.anchor = GridBagConstraints.EAST;
		gbc_lblInchi.insets = new Insets(0, 0, 5, 5);
		gbc_lblInchi.gridx = 0;
		gbc_lblInchi.gridy = 1;
		panel_1.add(lblInchi, gbc_lblInchi);

		inchiTextField = new JTextField();
		GridBagConstraints gbc_inchiTextField = new GridBagConstraints();
		gbc_inchiTextField.insets = new Insets(0, 0, 5, 5);
		gbc_inchiTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_inchiTextField.gridx = 1;
		gbc_inchiTextField.gridy = 1;
		panel_1.add(inchiTextField, gbc_inchiTextField);
		inchiTextField.setColumns(10);

		JButton showInchiButton = new JButton("Show structure");
		showInchiButton.setActionCommand(SHOW_INCHI);
		showInchiButton.addActionListener(this);
		GridBagConstraints gbc_showInchiButton = new GridBagConstraints();
		gbc_showInchiButton.insets = new Insets(0, 0, 5, 0);
		gbc_showInchiButton.gridx = 2;
		gbc_showInchiButton.gridy = 1;
		panel_1.add(showInchiButton, gbc_showInchiButton);

		JLabel lblPeptide = new JLabel("Peptide");
		GridBagConstraints gbc_lblPeptide = new GridBagConstraints();
		gbc_lblPeptide.anchor = GridBagConstraints.EAST;
		gbc_lblPeptide.insets = new Insets(0, 0, 0, 5);
		gbc_lblPeptide.gridx = 0;
		gbc_lblPeptide.gridy = 2;
		panel_1.add(lblPeptide, gbc_lblPeptide);

		pepSeqTextField = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 0, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 2;
		panel_1.add(pepSeqTextField, gbc_textField_1);
		pepSeqTextField.setColumns(10);

		JButton btnShowPeptide = new JButton("Show peptide");
		btnShowPeptide.addActionListener(this);
		btnShowPeptide.setActionCommand(SHOW_PEPTIDE);
		GridBagConstraints gbc_btnShowPeptide = new GridBagConstraints();
		gbc_btnShowPeptide.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnShowPeptide.gridx = 2;
		gbc_btnShowPeptide.gridy = 2;
		panel_1.add(btnShowPeptide, gbc_btnShowPeptide);

		molStructurePanel = new MolStructurePanel();
		panel.add(molStructurePanel, BorderLayout.CENTER);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 10, 10, 10));
		molStructurePanel.add(panel_2, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{108, 199, 143, 0};
		gbl_panel_2.rowHeights = new int[]{23, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		JButton btnNewButton_1 = new JButton("Verify compound database SMILES");
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.gridwidth = 3;
		gbc_btnNewButton_1.anchor = GridBagConstraints.NORTH;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_1.gridx = 0;
		gbc_btnNewButton_1.gridy = 0;
		panel_2.add(btnNewButton_1, gbc_btnNewButton_1);
		btnNewButton_1.addActionListener(this);
		btnNewButton_1.setActionCommand(VERIFY_DB);

		JButton pepInputButton = new JButton("Select peptide input");
		pepInputButton.setActionCommand(SELECT_PEPTIDE_INPUT);
		pepInputButton.addActionListener(this);
		GridBagConstraints gbc_pepInputButton = new GridBagConstraints();
		gbc_pepInputButton.insets = new Insets(0, 0, 0, 5);
		gbc_pepInputButton.gridx = 0;
		gbc_pepInputButton.gridy = 2;
		panel_2.add(pepInputButton, gbc_pepInputButton);

		pepInputFileLabel = new JLabel("");
		GridBagConstraints gbc_pepInputFileLabel = new GridBagConstraints();
		gbc_pepInputFileLabel.anchor = GridBagConstraints.WEST;
		gbc_pepInputFileLabel.insets = new Insets(0, 0, 0, 5);
		gbc_pepInputFileLabel.gridx = 1;
		gbc_pepInputFileLabel.gridy = 2;
		panel_2.add(pepInputFileLabel, gbc_pepInputFileLabel);

		JButton pepSmilesButton = new JButton("Create peptide SMILES");
		GridBagConstraints gbc_pepSmilesButton = new GridBagConstraints();
		gbc_pepSmilesButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_pepSmilesButton.gridx = 2;
		gbc_pepSmilesButton.gridy = 2;
		panel_2.add(pepSmilesButton, gbc_pepSmilesButton);
		pepSmilesButton.setActionCommand(CREATE_PEPTIDE_SMILES);
		pepSmilesButton.addActionListener(this);

		addWindowListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(SHOW_SMILES))
			showStructureFromSmiles();

		if (command.equals(SHOW_INCHI)) {
			try {
				showStructureFromInchi();
			} catch (CDKException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (command.equals(SHOW_PEPTIDE))
			showPepetideStructure();

		if (command.equals(VERIFY_DB))
			verifyDbSmiles();

		if (command.equals(SELECT_PEPTIDE_INPUT))
			selectPeptideInput();

		if (command.equals(CREATE_PEPTIDE_SMILES)) {
			try {
				createPeptideSmilesInchi();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void selectPeptideInput() {

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select input file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			pepInputFileLabel.setText(inputFile.getPath());
		}
	}

	private void showStructureFromInchi() throws CDKException {
		// TODO Auto-generated method stub
		String inchi = inchiTextField.getText().trim();
		InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
		InChIToStructure intostruct = factory.getInChIToStructure(
				inchi, DefaultChemObjectBuilder.getInstance());

		INCHI_RET ret = intostruct.getReturnStatus();
		if (ret == INCHI_RET.WARNING) {
			System.out.println("InChI warning: " + intostruct.getMessage());
		} else if (ret != INCHI_RET.OKAY) {
			throw new CDKException("Structure generation failed failed: " + ret.toString() + " [" + intostruct.getMessage() + "]");
		}
		IAtomContainer container = intostruct.getAtomContainer();
		molStructurePanel.clearPanel();
		try {
			molStructurePanel.showStructure(container);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
		CDKHydrogenAdder.getInstance(container.getBuilder()).addImplicitHydrogens(container);
		String smiles = smilesGenerator.create(container);
		System.out.println(smiles);
	}

	private void createPeptideSmilesInchi() throws IOException {

		String tab = "\t";
		char columnSeparator = tab.charAt(0);

		String[][] pepData = DelimitedTextParser.parseTextFile(inputFile, columnSeparator);
		TreeMap<String, String[]>pepDataMap = new TreeMap<String, String[]>();

		for (int i = 1; i < pepData.length; i++) {

			String[] pd = new String[5];
			for(int j=1; j<4; j++)
				pd[j-1] = pepData[i][j];

			String pepSeq = pepData[i][0].trim();
			IBioPolymer peptide = null;
			String smiles = null;

			try {
				peptide = ProteinBuilderTool.createProtein(pepSeq);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(peptide != null){
				try {
					AtomContainer mpep = new AtomContainer(peptide);
					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mpep);
					CDKHydrogenAdder.getInstance(mpep.getBuilder()).addImplicitHydrogens(mpep);
					smiles = smilesGenerator.create(mpep);
					pd[3] = smiles;
					inChIGenerator = igfactory.getInChIGenerator(mpep);
					INCHI_RET ret = inChIGenerator.getReturnStatus();
					if (ret == INCHI_RET.OKAY || ret == INCHI_RET.WARNING)
						pd[4] = inChIGenerator.getInchiKey();
					else
						pd[4] = "";

					pepDataMap.put(pepSeq, pd);
				}
				catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//	Create and write output
		File exportFile = 
				Paths.get(baseDirectory.getAbsolutePath(), 
						FilenameUtils.getBaseName(inputFile.getPath()) + "_output.txt").toFile();
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		for (Entry<String, String[]> entry : pepDataMap.entrySet()) {

			String line = entry.getKey() + columnSeparator + StringUtils.join(entry.getValue(), columnSeparator) + "\n";
			writer.append(line);
		}
		writer.flush();
		writer.close();
	}

	private void showPepetideStructure(){

		String pepSeq = pepSeqTextField.getText().trim();
		IBioPolymer peptide = null;
		String smiles = null;
		//String oneLetter = PeptideUtils.translateThreeLetterToOneLetterCode(pepSeq);
		String oneLetter = pepSeq;
		System.out.println(oneLetter);
		try {
			peptide = ProteinBuilderTool.createProtein(oneLetter);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(peptide != null){
			try {
				AtomContainer mpep = new AtomContainer(peptide);
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mpep);
				CDKHydrogenAdder.getInstance(mpep.getBuilder()).addImplicitHydrogens(mpep);
				smiles = smilesGenerator.create(mpep);
				smilesTextField.setText(smiles);
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
		try {
			molStructurePanel.showStructure(smiles);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			molStructurePanel.clearPanel();
			//e1.printStackTrace();
		}
	}

	private void showStructureFromSmiles(){

		String smiles = smilesTextField.getText().trim();

		try {
			molStructurePanel.showStructure(smiles);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			molStructurePanel.clearPanel();
			//e1.printStackTrace();
		}
	}

	private void verifyDbSmiles(){

		SmilesParser smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			String query = "SELECT ACCESSION, SMILES FROM COMPOUND_DATA D ORDER BY 1";
			ResultSet rs = CompoundDbConnectionManager.executeQueryNoParams(conn, query);
			while (rs.next()){

				String smiles = rs.getString("SMILES");
				String accession = rs.getString("ACCESSION");
				try {
					smipar.parseSmiles(smiles);
				}
				catch (Exception e1) {
					System.out.println(accession + "\t" + smiles);
				}
			}
			rs.close();
			CompoundDbConnectionManager.releaseConnection(conn);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {

		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException se) {

			if (((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState())))) {

				System.out.println("Derby was shut down normally");
			} else {
				System.err.println("Derby shut down error!");
			}
		}
		this.dispose();
		System.gc();
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}
}























