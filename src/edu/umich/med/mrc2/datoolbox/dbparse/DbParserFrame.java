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

package edu.umich.med.mrc2.datoolbox.dbparse;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.gui.automator.TextAreaOutputStream;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskControlListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.gui.TaskProgressPanel;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.dbparse.DrugBankParserTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.dbparse.HMDBParseAndUploadTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.dbparse.T3DBParserTask;

public class DbParserFrame extends JFrame 
		implements ActionListener, WindowListener,
		ItemListener, TaskListener, TaskControlListener, 
		BackedByPreferences, PersistentLayout{

	/**
	 *
	 */
	private static final long serialVersionUID = -1588870352349217881L;
	
	private static TaskProgressPanel progressPanel;
	private static JDialog progressDialogue;
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.DbParserFrame";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	
	private static final String SELECT_INPUT_FILE = "SELECT_INPUT_FILE";
	private static final String START_PARSER = "START_PARSER";
	private static final String CLEAR_LOG = "CLEAR_LOG";
	
	private static TextAreaOutputStream stdOutTaos;
	private static TextAreaOutputStream stdErrTaos;
	
	private static PrintStream stdOutPrintStream; 
	private static PrintStream stdErrorPrintStream;
	public static final PrintStream sysout = System.out;
	public static final PrintStream syserr = System.err;

	
	private File inputFile;
	private JLabel inputFileLabel;

	private FileFilter txtFilter, xmlFilter;
	private File baseDirectory;

	private JComboBox<CompoundDatabaseEnum> dbTypecomboBox;

	private CControl control;
	private CGrid grid;
	private static DockableConsole outputConsole;
	private static DockableConsole errorConsole;
	
	private static final File layoutConfigFile = 
			new File(DbParserCore.configDir + "DbParserFrame.layout");

	private static final Icon xml2DatabaseIcon = GuiUtils.getIcon("xml2Database", 32);
	private static final Icon sysOutIcon = GuiUtils.getIcon("actions", 16);
	private static final Icon sysErrIcon = GuiUtils.getIcon("sysError", 16);

	public DbParserFrame() {

		super();
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
		setSize(new Dimension(800, 640));
		setTitle("Compound Database Parser");
		setIconImage(((ImageIcon) xml2DatabaseIcon).getImage());
		
		getContentPane().add(createDatabaseSelector(), BorderLayout.NORTH);
		
		control = new CControl( this );
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		getContentPane().add(control.getContentArea(), BorderLayout.CENTER);
				
		outputConsole = new DockableConsole("CpdDbParserOutConsole","System output", sysOutIcon);
		errorConsole = new DockableConsole("CpdDbParserErrorConsole","System errors", sysErrIcon);
		
		grid = new CGrid( control );
		grid.add(0, 0, 1, 1,
				outputConsole, 
				errorConsole);

		control.getContentArea().deploy(grid);
		grid.select(0, 0, 1, 1, outputConsole);
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton startButton = new JButton("Start parser");
		startButton.addActionListener(this);
		startButton.setActionCommand(START_PARSER);
		buttonPanel.add(startButton);

		JButton clearButton = new JButton("Clear console");
		clearButton.addActionListener(this);
		clearButton.setActionCommand(CLEAR_LOG);
		buttonPanel.add(clearButton);
		
		loadPreferences();
		DbParserCore.getTaskController().addTaskControlListener(this);
		initProgressDialog();
		addWindowListener(this);
		loadLayout(layoutConfigFile);
	}
	
	@Override
	public void dispose() {
		
		saveLayout(layoutConfigFile);
		savePreferences();
		super.dispose();
	}
	
	public static void bindSystemStreams() {

		try {
			stdOutTaos = new TextAreaOutputStream(outputConsole.getConsoleTextArea());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (stdOutTaos != null) {

			stdOutPrintStream = new PrintStream(stdOutTaos);
			System.setOut(stdOutPrintStream);
		}
		try {
			stdErrTaos = new TextAreaOutputStream(errorConsole.getConsoleTextArea());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (stdErrTaos != null) {

			stdErrorPrintStream = new PrintStream(stdErrTaos);
			System.setErr(stdErrorPrintStream);
		}
	}

	public static void unbindSystemStreams() {
		System.setOut(sysout);
		System.setErr(syserr);
	}
	
	private JPanel createDatabaseSelector() {
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{65, 0, 1, 0};
		gbl_panel.rowHeights = new int[]{23, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel_1 = new JLabel("Database: ");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1_1_1 = new GridBagConstraints();
		gbc_lblNewLabel_1_1_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1_1_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1_1_1.gridx = 0;
		gbc_lblNewLabel_1_1_1.gridy = 0;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1_1_1);

		dbTypecomboBox = new JComboBox<CompoundDatabaseEnum>();
		dbTypecomboBox.setPreferredSize(new Dimension(200, 25));
		DefaultComboBoxModel<CompoundDatabaseEnum> model = 
				new DefaultComboBoxModel<CompoundDatabaseEnum>(
					new CompoundDatabaseEnum[] {
							CompoundDatabaseEnum.HMDB,
							CompoundDatabaseEnum.DRUGBANK,
							CompoundDatabaseEnum.FOODB,
							CompoundDatabaseEnum.T3DB,
							CompoundDatabaseEnum.LIPIDMAPS,
							CompoundDatabaseEnum.CHEBI,
							CompoundDatabaseEnum.NATURAL_PRODUCTS_ATLAS,
					});
		dbTypecomboBox.setModel(model);
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

		JButton selectInputButton = new JButton("Select input file");
		selectInputButton.addActionListener(this);
		selectInputButton.setActionCommand(SELECT_INPUT_FILE);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 1;
		panel.add(selectInputButton, gbc_btnNewButton);
		
		return panel;
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
		fc.setTitle("Select input file(s)");
		
		CompoundDatabaseEnum db = getSelectedDatabase();
		if(db.equals(CompoundDatabaseEnum.HMDB) 
				|| db.equals(CompoundDatabaseEnum.DRUGBANK)
				|| db.equals(CompoundDatabaseEnum.T3DB)) {
			fc.addFilter("XML files", "xml", "XML");
			fc.setMultiSelectionEnabled(false);
		}
		if(db.equals(CompoundDatabaseEnum.NATURAL_PRODUCTS_ATLAS) 
				|| db.equals(CompoundDatabaseEnum.CHEBI)) {
			fc.addFilter("SDF files", "SDF", "sdf");
			fc.setMultiSelectionEnabled(false);
		}
		if(db.equals(CompoundDatabaseEnum.FOODB)) {
			fc.addFilter("Text files", "csv", "CSV");
			fc.setMultiSelectionEnabled(true);
		}
//		fc.addFilter("Text files", "txt", "TXT", "TSV", "CSV", "data");
//		fc.addFilter("MSP files", "msp", "MSP");
				
		if (fc.showOpenDialog(this)) {
			
			inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			inputFileLabel.setText(fc.getSelectedFile().getAbsolutePath());
			savePreferences();
		}
	}

	private void clearConsole(){
		
		outputConsole.setText("");
		errorConsole.setText("");
	}
	
	private CompoundDatabaseEnum getSelectedDatabase() {
		return (CompoundDatabaseEnum) dbTypecomboBox.getSelectedItem();
	}

	private void parseDataFile() throws Exception{
		
		if(inputFile == null || !inputFile.exists() || !inputFile.canRead())
			return;
		
		clearConsole();
		bindSystemStreams();
		
		CompoundDatabaseEnum database = getSelectedDatabase();

		Task parserTask = null;
		if(database.equals(CompoundDatabaseEnum.DRUGBANK))
			parserTask = new DrugBankParserTask(inputFile);

		if(database.equals(CompoundDatabaseEnum.HMDB))
			parserTask = new HMDBParseAndUploadTask(inputFile);

		if(database.equals(CompoundDatabaseEnum.T3DB))
			parserTask = new T3DBParserTask(inputFile);

		parserTask.addTaskListener(this);
		DbParserCore.getTaskController().addTask(parserTask);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			if(e.getSource().getClass().equals(HMDBParseAndUploadTask.class))
				finalizeHMDBParseAndUploadTask((HMDBParseAndUploadTask)e.getSource());

			unbindSystemStreams();
			hideProgressDialog();
		}
		if (e.getStatus() == TaskStatus.CANCELED)
			hideProgressDialog();

		if (e.getStatus() == TaskStatus.ERROR)
			hideProgressDialog();
	}
	
	private void finalizeHMDBParseAndUploadTask(HMDBParseAndUploadTask source) {
		// TODO Auto-generated method stub
		
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
	
	public static void showProgressDialog() {

		if (!progressDialogue.isVisible() && !DbParserCore.getTaskController().getTaskQueue().isEmpty()) {

			try {
				progressDialogue.setLocationRelativeTo(DbParserCore.getMainWindow());
				progressDialogue.setVisible(true);
			} catch (Exception e) {

				// e.printStackTrace();
			}
		}
	}
	public static void hideProgressDialog() {
		progressDialogue.setVisible(false);
	}

	private void initProgressDialog() {

		progressPanel = DbParserCore.getTaskController().getTaskPanel();
		progressDialogue = new JDialog(this, "Task in progress...", ModalityType.APPLICATION_MODAL);
		progressDialogue.setTitle("Operation in progress ...");
		progressDialogue.setSize(new Dimension(600, 150));
		progressDialogue.setPreferredSize(new Dimension(600, 150));
		progressDialogue.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		progressDialogue.getContentPane().setLayout(new BorderLayout());
		progressDialogue.getContentPane().add(progressPanel, BorderLayout.CENTER);
		progressDialogue.setLocationRelativeTo(this);
		progressDialogue.pack();
		progressDialogue.setVisible(false);
	}

	@Override
	public void allTasksFinished(boolean atf) {

		if (atf)
			hideProgressDialog();
	}

	@Override
	public void numberOfWaitingTasksChanged(int numOfTasks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		exitProgram();
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
	
	private void exitProgram() {
		// TODO Auto-generated method stub
		if (MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to exit?", this.getContentPane()) == JOptionPane.YES_OPTION) {
			DbParserCore.shutDown();
		}
	}
	
	@Override
	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					control.writeXML(layoutFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void saveLayout(File layoutFile) {

		if(control != null) {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
}

