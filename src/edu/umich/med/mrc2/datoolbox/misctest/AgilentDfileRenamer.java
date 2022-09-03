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
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class AgilentDfileRenamer extends JFrame 
		implements ActionListener, WindowListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 946976178706518706L;

	private JFileChooser chooser;
	private File inputFile;
	private File baseDirectory;
	private JTextField originalNameTextField;
	private JTextField newNameTextField;
	private JButton fileBrowseButton;
	private JButton runButton;

	public static final String BROWSE_FOR_INPUT = "BROWSE_FOR_INPUT";
	public static final String RENAME_FILE = "RENAME_FILE";
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.gui.test.AgilentDfileRenamer";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	public static void main(String[] args) {

		AgilentDfileRenamer sm = new AgilentDfileRenamer();
		sm.setVisible(true);
	}

	public AgilentDfileRenamer() throws HeadlessException {
		super("Rename .D file");
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "AgilentDfileRenamer.txt");
				
		initGui();
	}
	private void initGui() {
		
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
		setSize(new Dimension(800, 200));
		addWindowListener(this);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel_1.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{71, 46, 46, 0, 0};
		gbl_panel.rowHeights = new int[]{14, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel = new JLabel("Original name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		originalNameTextField = new JTextField();
		GridBagConstraints gbc_originalNameTextField = new GridBagConstraints();
		gbc_originalNameTextField.gridwidth = 2;
		gbc_originalNameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_originalNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_originalNameTextField.gridx = 1;
		gbc_originalNameTextField.gridy = 0;
		panel.add(originalNameTextField, gbc_originalNameTextField);
		originalNameTextField.setColumns(10);
		originalNameTextField.setEditable(false);

		fileBrowseButton = new JButton("Browse ...");
		fileBrowseButton.setActionCommand(BROWSE_FOR_INPUT);
		fileBrowseButton.addActionListener(this);
		GridBagConstraints gbc_fileBrowseButton = new GridBagConstraints();
		gbc_fileBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_fileBrowseButton.gridx = 3;
		gbc_fileBrowseButton.gridy = 0;
		panel.add(fileBrowseButton, gbc_fileBrowseButton);
	
		JLabel lblNewLabel_1 = new JLabel("New name");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		newNameTextField = new JTextField();
		GridBagConstraints gbc_newNameTextField = new GridBagConstraints();
		gbc_newNameTextField.gridwidth = 2;
		gbc_newNameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_newNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_newNameTextField.gridx = 1;
		gbc_newNameTextField.gridy = 1;
		panel.add(newNameTextField, gbc_newNameTextField);
		newNameTextField.setColumns(10);
		
		runButton = new JButton("Rename");
		runButton.setActionCommand(RENAME_FILE);
		runButton.addActionListener(this);	
		GridBagConstraints gbc_runButton = new GridBagConstraints();
		gbc_runButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_runButton.gridx = 2;
		gbc_runButton.gridy = 2;
		panel.add(runButton, gbc_runButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(runButton);
		rootPane.setDefaultButton(runButton);

		loadPreferences();
		pack();
	}
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
		if(baseDirectory != null)
			chooser.setCurrentDirectory(baseDirectory);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals(BROWSE_FOR_INPUT)) {
			
			if(chooser == null)
				initChooser();
			
			chooser.showOpenDialog(this);
		}
		if (command.equals(RENAME_FILE)) {
			renameFile();
		}
		if (e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))  {

			inputFile = chooser.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			originalNameTextField.setText(chooser.getSelectedFile().getName());
			newNameTextField.setText(chooser.getSelectedFile().getName());
		}
	}

	private void renameFile() {
		
		if(inputFile == null)
			return;
		
		if(!inputFile.getName().endsWith(".D") && !inputFile.getName().endsWith(".d")) {
			MessageDialog.showWarningMsg("Not valid .D Agilent file", this);
			return;
		}		
		String oldName = originalNameTextField.getText().trim();
		String newName = newNameTextField.getText().trim();
		
//		if(oldName.equals(newName)) {
//			MessageDialog.showWarningMsg("No name change!", this);
//			return;
//		}
		if(newName.isEmpty()) {
			MessageDialog.showWarningMsg("Name can not be empty!", this);
			return;
		}		
		if(!newName.endsWith(".D") && !newName.endsWith(".d"))
			newName += ".D";
		
		savePreferences();
		chooser.setCurrentDirectory(baseDirectory);
		try {
			renameFileInSampleInfo(oldName, newName);
		} catch (Exception e1) {
			MessageDialog.showWarningMsg("Could not edit sample_info.xml file!", this);
			e1.printStackTrace();
		}		
		Path source = Paths.get(inputFile.getAbsolutePath());
		try {
			Files.move(source, source.resolveSibling(newName));
		} catch (IOException e) {
			MessageDialog.showWarningMsg("Could not rename .D!", this);
			e.printStackTrace();
		}
	}

	private void renameFileInSampleInfo(String oldName, String newName) throws Exception {

		File sampleInfoFile = Paths.get(inputFile.getAbsolutePath(), "AcqData", "sample_info.xml").toFile();
		if (sampleInfoFile.setWritable(true)) {
			System.out.println("Re-enabled writing for " + inputFile.getName() + " sample_info.xml");
		} else {
			System.out.println("Failed to re-enable writing on file.");
		}
		if (sampleInfoFile.exists()) {

			Document sampleInfo = XmlUtils.readXmlFile(sampleInfoFile);
			if (sampleInfo != null) {

				List<Element> fieldNodes = sampleInfo.getRootElement().getChildren("Field");
				for (Element fieldElement : fieldNodes) {

					if (fieldElement.getChildText("Name").trim().equals("Data File")) {
						fieldElement.getChild("Value").setText(newName);
						break;
					}
				}
				XMLOutputter xmlOutputter = new XMLOutputter();
				try (FileOutputStream fileOutputStream = new FileOutputStream(sampleInfoFile)) {
					xmlOutputter.output(new Document(), fileOutputStream);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =
			new File(preferences.get(BASE_DIRECTORY,
					System.getProperty("user.dir")));
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

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {

		savePreferences();
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
