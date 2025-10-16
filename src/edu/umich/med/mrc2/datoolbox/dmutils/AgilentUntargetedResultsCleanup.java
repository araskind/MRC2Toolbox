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

package edu.umich.med.mrc2.datoolbox.dmutils;

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
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import edu.umich.med.mrc2.datoolbox.gui.automator.TextAreaOutputStream;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;

public class AgilentUntargetedResultsCleanup extends JFrame implements ActionListener, WindowListener{


	/**
	 *
	 */
	private static final long serialVersionUID = 946976178706518706L;

	private File inputFile;
	private File baseDirectory;
	private JTextField textField;
	private JTextArea consoleTextArea;
	private TextAreaOutputStream taos;
	private PrintStream ps;
	private JButton fileBrowseButton;
	private JButton runButton;
	private FileFilter sdfFilter, xmlFilter, txtFilter;
	private JCheckBox removeProfileMsCheckBox;

	private FileNameExtensionFilter mspFilter;

	public static final String BROWSE_FOR_INPUT = "BROWSE_FOR_INPUT";
	public static final String REMOVE_RESULTS = "REMOVE_RESULTS";

	public static void main(String[] args) {

		AgilentUntargetedResultsCleanup sm = new AgilentUntargetedResultsCleanup();
		sm.setVisible(true);
	}

	public AgilentUntargetedResultsCleanup() throws HeadlessException {
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
		setSize(new Dimension(600, 300));
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

		runButton = new JButton("Cleanup data files");
		runButton.setActionCommand(REMOVE_RESULTS);
		runButton.addActionListener(this);
		
		removeProfileMsCheckBox = new JCheckBox("Remove MS profile data");
		GridBagConstraints gbc_removeProfileMsCheckBox = new GridBagConstraints();
		gbc_removeProfileMsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_removeProfileMsCheckBox.gridwidth = 2;
		gbc_removeProfileMsCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_removeProfileMsCheckBox.gridx = 0;
		gbc_removeProfileMsCheckBox.gridy = 1;
		panel.add(removeProfileMsCheckBox, gbc_removeProfileMsCheckBox);
		GridBagConstraints gbc_runButton = new GridBagConstraints();
		gbc_runButton.gridwidth = 2;
		gbc_runButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_runButton.gridx = 2;
		gbc_runButton.gridy = 1;
		panel.add(runButton, gbc_runButton);

		consoleTextArea = new JTextArea();
		JScrollPane areaScrollPane = new JScrollPane(consoleTextArea);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(600, 250));

		panel_1.add(areaScrollPane, BorderLayout.CENTER);
		initConsol();
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

		if (command.equals(BROWSE_FOR_INPUT))
			selectRawDataFolder();

		if (command.equals(REMOVE_RESULTS))
			removeResultsFolders();
	}
	
	private void selectRawDataFolder() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select directory containing raw data files:");
		fc.setOpenButtonText("Select folder");
		fc.setMultiSelectionEnabled(false);		
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane())))	{	
			
			inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			textField.setText(inputFile.getAbsolutePath());
		}
	}
	
	private void removeResultsFolders() {
		consoleTextArea.setText("");
		initConsol();
		List<Path> dDirs = null;
		try {
			dDirs = Files.find(Paths.get(textField.getText().trim()), Integer.MAX_VALUE,
					(filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d")) && 
					fileAttr.isDirectory()).collect(Collectors.toList());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dDirs != null) {

			for(Path dir : dDirs) {

				File resultsDir = Paths.get(dir.toString(), "Results").toFile();
				if(resultsDir.exists()) {
					try {
						FileUtils.deleteDirectory(resultsDir);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Cleaning " + dir.getFileName());
				}
				if(removeProfileMsCheckBox.isSelected()) {
					
					File profileMs = Paths.get(dir.toString(), "AcqData", "MSProfile.bin").toFile();
					if(profileMs.exists()) 
						FileUtils.deleteQuietly(profileMs);
				}
			}
		}
		MessageDialog.showInfoMsg("Data cleanup finished", this);
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
