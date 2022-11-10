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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;

import edu.umich.med.mrc2.datoolbox.database.load.XMLdatabase;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GlassPane;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.dbparsers.DrugBankParserTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.dbparsers.HMDBParserTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.dbparsers.T3DBParserTask;

public class DbParserFrame extends JFrame 
		implements ActionListener, TaskListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -1588870352349217881L;
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.DbParserFrame";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	
	private static final String SELECT_INPUT_FILE = "SELECT_INPUT_FILE";
	private static final String START_PARSER = "START_PARSER";
	private static final String CLEAR_LOG = "CLEAR_LOG";
	private JTextArea textArea;
	private PrintStream printStream, standardOut, standardError;

	private JButton selectInputButton, startButton, clearButton;
	private File inputFile;
	private JLabel inputFileLabel;

//	private JFileChooser chooser;
	private FileFilter txtFilter, xmlFilter;
	private File baseDirectory;
	private JLabel lblNewLabel_1;
	private JComboBox<XMLdatabase> dbTypecomboBox;

	private Component defaultGlassPane;

	private static final Icon xml2DatabaseIcon = GuiUtils.getIcon("xml2Database", 32);

	public DbParserFrame() {

		setSize(new Dimension(800, 640));
		setTitle("Database XML parser");
		setIconImage(((ImageIcon) xml2DatabaseIcon).getImage());
		defaultGlassPane = this.getGlassPane();

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{65, 0, 1, 0};
		gbl_panel.rowHeights = new int[]{23, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		lblNewLabel_1 = new JLabel("Database: ");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1_1_1 = new GridBagConstraints();
		gbc_lblNewLabel_1_1_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1_1_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1_1_1.gridx = 0;
		gbc_lblNewLabel_1_1_1.gridy = 0;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1_1_1);

		dbTypecomboBox = new JComboBox<XMLdatabase>();
		dbTypecomboBox.setPreferredSize(new Dimension(200, 25));
		dbTypecomboBox.setModel(new DefaultComboBoxModel<XMLdatabase>(XMLdatabase.values()));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 2;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel.add(dbTypecomboBox, gbc_comboBox);

		JLabel lblNewLabel = new JLabel("Input file: ");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		inputFileLabel = new JLabel("");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 1;
		panel.add(inputFileLabel, gbc_lblNewLabel_2);

		selectInputButton = new JButton("Select input file");
		selectInputButton.addActionListener(this);
		selectInputButton.setActionCommand(SELECT_INPUT_FILE);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 1;
		panel.add(selectInputButton, gbc_btnNewButton);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		startButton = new JButton("Start parser");
		startButton.addActionListener(this);
		startButton.setActionCommand(START_PARSER);
		panel_1.add(startButton);

		clearButton = new JButton("Clear console");
		clearButton.addActionListener(this);
		clearButton.setActionCommand(CLEAR_LOG);
		panel_1.add(clearButton);

//		initChooser();

		textArea = new JTextArea();
		getContentPane().add(textArea, BorderLayout.CENTER);

		printStream = new PrintStream(new CustomOutputStream(textArea));

		standardOut = System.out;
		standardError = System.err;
		
		loadPreferences();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(SELECT_INPUT_FILE))
			selectInputFile();

		if (command.equals(START_PARSER)){
			try {
				parseDataFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (command.equals(CLEAR_LOG))
			clearConsole();
	}
	
	private void selectInputFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("SDF files", "SDF", "sdf");
		fc.addFilter("XML files", "xml", "XML");
		fc.addFilter("Text files", "txt", "TXT", "TSV", "CSV", "data");
		fc.addFilter("MSP files", "msp", "MSP");
		fc.setTitle("Select input file");
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(this)) {
			
			inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			inputFileLabel.setText(fc.getSelectedFile().getAbsolutePath());
			savePreferences();
		}
	}

	private void clearConsole(){
        try {
            textArea.getDocument().remove(0, textArea.getDocument().getLength());
            //standardOut.println("Text area cleared");
        }
        catch (BadLocationException ex) {
            ex.printStackTrace();
        }
	}

//	private void initChooser() {
//
//		chooser = new ImprovedFileChooser();
//		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
//		chooser.addActionListener(this);
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//
//		xmlFilter = new FileNameExtensionFilter("XML files", "xml", "cef", "wkl");
//		chooser.addChoosableFileFilter(xmlFilter);
//
//		txtFilter = new FileNameExtensionFilter("Text files", "txt", "tsv");
//		chooser.addChoosableFileFilter(txtFilter);
//	}

	private void parseDataFile() throws Exception{

		this.setGlassPane(new GlassPane());
		clearConsole();
		System.setOut(printStream);
		System.setErr(printStream);

		if(inputFile != null){

			if(inputFile.exists() && inputFile.canRead()){

				XMLdatabase database = (XMLdatabase) dbTypecomboBox.getSelectedItem();
				Task parserTask = null;

				if(database.equals(XMLdatabase.DRUGBANK))
					parserTask = new DrugBankParserTask(inputFile);

				if(database.equals(XMLdatabase.HMDB))
					parserTask = new HMDBParserTask(inputFile);

				if(database.equals(XMLdatabase.T3DB))
					parserTask = new T3DBParserTask(inputFile);

				parserTask.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(parserTask);
			}
		}
		this.setGlassPane(defaultGlassPane);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(DrugBankParserTask.class)) {

			}
			if (e.getSource().getClass().equals(HMDBParserTask.class)) {

			}
			if (e.getSource().getClass().equals(T3DBParserTask.class)) {

			}
			System.setOut(standardOut);
			System.setErr(standardError);
		}
		if (e.getStatus() == TaskStatus.CANCELED)
			MRC2ToolBoxCore.getMainWindow().hideProgressDialog();

		if (e.getStatus() == TaskStatus.ERROR)
			MRC2ToolBoxCore.getMainWindow().hideProgressDialog();

	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  
				new File(preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

//    public static void main(String[] args) {
//
//    	DbParserFrame frame = new DbParserFrame();
//    	frame.setLocationRelativeTo(null);
//    	frame.setVisible(true);
//    }
}

