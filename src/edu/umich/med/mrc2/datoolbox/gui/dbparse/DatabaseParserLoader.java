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

package edu.umich.med.mrc2.datoolbox.gui.dbparse;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDbConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.load.chebi.ChebiParser;
import edu.umich.med.mrc2.datoolbox.database.load.foodb.FooDbCompoundTableParser;
import edu.umich.med.mrc2.datoolbox.database.load.lipidblast.LipidBlastParser;
import edu.umich.med.mrc2.datoolbox.database.load.lipidmaps.LipidMapsParser;
import edu.umich.med.mrc2.datoolbox.database.load.mona.MonaParser;
import edu.umich.med.mrc2.datoolbox.database.load.refmet.RefMetFields;
import edu.umich.med.mrc2.datoolbox.database.load.refmet.RefMetParser;
import edu.umich.med.mrc2.datoolbox.gui.automator.TextAreaOutputStream;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;

public class DatabaseParserLoader extends JFrame implements ActionListener, WindowListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 946976178706518706L;

	private JFileChooser chooser;
	private File inputFile;
	private File baseDirectory;
	private JTextField textField;
	private JTextArea consoleTextArea;
	private TextAreaOutputStream taos;
	private PrintStream ps;
	private JButton fileBrowseButton;
	private JButton runButton;
	private FileFilter sdfFilter, xmlFilter, txtFilter;
	private FileNameExtensionFilter mspFilter;

	public static final String BROWSE_FOR_INPUT = "BROWSE_FOR_INPUT";
	public static final String UPLOAD_DATA = "UPLOAD_DATA";

	//	TODO update connection manager if needed to re-parse the data
	public static void main(String[] args) {

		DatabaseParserLoader sm = new DatabaseParserLoader();
		sm.setVisible(true);
	}

	public DatabaseParserLoader() throws HeadlessException {
		super("DrugBank loader");
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
		setSize(new Dimension(600, 400));
		addWindowListener(this);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel_1.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{71, 46, 46, 0, 0};
		gbl_panel.rowHeights = new int[]{14, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel = new JLabel("Database source file");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(textField, gbc_textField);
		textField.setColumns(10);

		fileBrowseButton = new JButton("Browse ...");
		fileBrowseButton.setActionCommand(BROWSE_FOR_INPUT);
		fileBrowseButton.addActionListener(this);
		GridBagConstraints gbc_fileBrowseButton = new GridBagConstraints();
		gbc_fileBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_fileBrowseButton.gridx = 3;
		gbc_fileBrowseButton.gridy = 0;
		panel.add(fileBrowseButton, gbc_fileBrowseButton);

		runButton = new JButton("Upload data");
		runButton.setActionCommand(UPLOAD_DATA);
		runButton.addActionListener(this);
		GridBagConstraints gbc_runButton = new GridBagConstraints();
		gbc_runButton.gridwidth = 2;
		gbc_runButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_runButton.insets = new Insets(0, 0, 0, 5);
		gbc_runButton.gridx = 2;
		gbc_runButton.gridy = 1;
		panel.add(runButton, gbc_runButton);

		consoleTextArea = new JTextArea();
		JScrollPane areaScrollPane = new JScrollPane(consoleTextArea);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(600, 250));

		panel_1.add(areaScrollPane, BorderLayout.CENTER);

		//	Input chooser
		initChooser();
		initConsol();
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		baseDirectory = new File("E:\\DataAnalysis\\Databases\\MONA\\20180130").getAbsoluteFile();
		chooser.setCurrentDirectory(baseDirectory);

		sdfFilter = new FileNameExtensionFilter("SDF files", "SDF", "sdf");
		xmlFilter = new FileNameExtensionFilter("XML files", "xml", "XML");
		txtFilter = new FileNameExtensionFilter("Text files", "txt", "TXT", "TSV", "CSV", "data");
		mspFilter = new FileNameExtensionFilter("MSP files", "msp", "MSP");
	}

	private void initConsol() {

		try {
			taos = new TextAreaOutputStream(consoleTextArea);

		} catch (IOException e) {
			e.printStackTrace();
		}
		if (taos != null) {

			ps = new PrintStream(taos);
			System.setOut(ps);
			//	System.setErr(ps);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals(BROWSE_FOR_INPUT)) {

			chooser.resetChoosableFileFilters();
			//	chooser.setFileFilter(sdfFilter);
			//	chooser.setFileFilter(xmlFilter);
			chooser.setFileFilter(mspFilter);
			chooser.showOpenDialog(this);
		}
		if (command.equals(UPLOAD_DATA)) {
			//	uploadMonaData();
			uploadLipidBlast("P");
		}
		if (e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))  {

			inputFile = chooser.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			textField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void uploadMonaData() {

		File sdfFile = new File(textField.getText().trim());
		MonaParser.initParser();
		try {
			MonaParser.pareseInputFile(sdfFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

/*
//		int count = 0;
//		try {
//			count = MonaParser.getRecordCount(sdfFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(Integer.toString(count) + " records found");
		HashSet<String>idSet = new HashSet<String>();
		int count = 0;
		try {
			MonaIteratingSDFReader reader =
				new MonaIteratingSDFReader(
					new BufferedInputStream(new FileInputStream(sdfFile)),
					DefaultChemObjectBuilder.getInstance());
			while (reader.hasNext()) {

				IAtomContainer molecule = (IAtomContainer)reader.next();
				String id = (String) molecule.getProperties().get(MonaNameFields.ID.getName());
				idSet.add(id);
				count++;
			}
			reader.close();
			System.out.println(Integer.toString(count) + " SDF records and " + Integer.toString(idSet.size()) +" unique ids found");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialogue.showErrorMsg("Error!", this);
		}

		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			MonaIteratingSDFReader reader = new MonaIteratingSDFReader(new BufferedInputStream(new FileInputStream(sdfFile)), DefaultChemObjectBuilder.getInstance());
			int count = 0;
			Collection<IAtomContainer>molecules = new ArrayList<IAtomContainer>();
			while (reader.hasNext()) {

				IAtomContainer molecule = (IAtomContainer)reader.next();
				molecules.add(molecule);
				count++;
				if((count % 1000) == 0) {

					try {
						MonaParser.insertMonaRecords(molecules, conn);
						System.out.println(count + " records inserted");
						ps.flush();
						molecules.clear();
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//	final batch of records
			try {
				MonaParser.insertMonaRecords(molecules, conn);
				System.out.println(count + " records inserted");
				ps.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CompoundDbConnectionManager.releaseConnection(conn);
			MessageDialogue.showInfoMsg("MONA data upload completed.", this);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialogue.showErrorMsg("Error!", this);
		}*/
	}

	private void uploadLipidBlast(String msMode) {

		File inputFile = new File(textField.getText().trim());
		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			LipidBlastParser.processLipidBlastRecords(inputFile, conn, msMode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageDialog.showInfoMsg("LipidBlast NEG data upload completed.", this);
	}

	private void uploadFooDbCompoundData() {

		File inputFile = new File(textField.getText().trim());
		try {
			FooDbCompoundTableParser.parseAndUploadFooDbCompounsFile(inputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void uploadRefMetData() {

		File inputFile = new File(textField.getText().trim());
		Collection<Map<RefMetFields, String>> recordList = RefMetParser.parseRefMetDataFile(inputFile);

		try {
			RefMetParser.uploadRecordsToDatabase(recordList);
			MessageDialog.showInfoMsg("RefMet data upload completed.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.showErrorMsg("RefMet data upload failed!" , this);
		}
	}

	private void uploadHMDBData() {

		File inputFile = new File(textField.getText().trim());
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        try {
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(inputFile));
			xsr.nextTag(); // Advance to statements element

			//	TransformerFactory tf = TransformerFactory.newInstance( "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null );
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DocumentBuilderFactory dbactory = DocumentBuilderFactory.newInstance();
			dbactory.setNamespaceAware(true);
			
			//	TODO with jdom2
//			Connection conn = CompoundDbConnectionManager.getConnection();
//
//			while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
//
//			    DOMResult result = new DOMResult();
//			    t.transform(new StAXSource(xsr), result);
//			    Node domNode = result.getNode();
//
//			    if(domNode.getFirstChild().getNodeName().equals("metabolite")){
//
//			    	HMDBRecord record = HMDBParser.parseRecord(domNode.getFirstChild());
//			    	try {
//			    		HMDBParser.insertRecord(record, conn);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			    }
//			}
//			CompoundDbConnectionManager.releaseConnection(conn);
			MessageDialog.showInfoMsg("HMDB data upload completed.", this);
		}
        catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void uploadDrugBankData() {

		File inputFile = new File(textField.getText().trim());
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        try {
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(inputFile));
			xsr.nextTag(); // Advance to statements element

			//	TransformerFactory tf = TransformerFactory.newInstance( 
			//	"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null );
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DocumentBuilderFactory dbactory = DocumentBuilderFactory.newInstance();
			dbactory.setNamespaceAware(true);
			//	TODO with jdom2
//			Connection conn = CompoundDbConnectionManager.getConnection();
//
//			while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
//
//			    DOMResult result = new DOMResult();
//			    t.transform(new StAXSource(xsr), result);
//			    Node domNode = result.getNode();
//
//			    if(domNode.getFirstChild().getNodeName().equals("drug")){
//
//			    	DrugBankRecord record = DrugBankParser.parseRecord(domNode.getFirstChild());
//			    	try {
//						DrugBankParser.insertRecord(record, conn);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			    }
//			}
//			CompoundDbConnectionManager.releaseConnection(conn);
		}
        catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void uploadT3DBData() {

		File inputFile = new File(textField.getText().trim());
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        try {
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(inputFile));
			xsr.nextTag(); // Advance to statements element

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DocumentBuilderFactory dbactory = DocumentBuilderFactory.newInstance();
			dbactory.setNamespaceAware(true);
			//	TODO with jdom2
//			Connection conn = CompoundDbConnectionManager.getConnection();
//
//			while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
//
//			    DOMResult result = new DOMResult();
//			    t.transform(new StAXSource(xsr), result);
//			    Node domNode = result.getNode();
//
//			    if(domNode.getFirstChild().getNodeName().equals("compound")){
//
//			    	T3DBRecord record = T3DBParser.parseRecord(domNode.getFirstChild());
//			    	try {
//			    		T3DBParser.insertRecord(record, conn);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			    }
//			}
//			CompoundDbConnectionManager.releaseConnection(conn);
			MessageDialog.showInfoMsg("T3DB data upload completed.", this);
		}
        catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void uploadChebiData() {

		File sdfFile = new File(textField.getText().trim());
		IteratingSDFReader reader;

		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			reader = new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
			int count = 1;
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				try {
					ChebiParser.insertChebiRecord(molecule, conn);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count++;

				if((count % 1000) == 0) {
					System.out.println(count + " records inserted");
					ps.flush();
				}
			}
			CompoundDbConnectionManager.releaseConnection(conn);
			MessageDialog.showInfoMsg("CHEBI data upload completed.", this);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.showErrorMsg("Error!", this);
		}
	}

	private void uploadLipidMapsData() {

		File sdfFile = new File(textField.getText().trim());
		IteratingSDFReader reader;
		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			reader = new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
			int count = 1;
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				try {
					LipidMapsParser.insertLipidMapsRecord(molecule, conn);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count++;

				if((count % 1000) == 0) {
					System.out.println(count + " records inserted");
					ps.flush();
				}
			}
			CompoundDbConnectionManager.releaseConnection(conn);
			MessageDialog.showInfoMsg("LipidMaps data upload completed.", this);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.showErrorMsg("Error!", this);
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

		this.dispose();
		System.gc();
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}


}
