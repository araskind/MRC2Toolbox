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
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUploadUtils;


public class MotrPacDataCleaner extends JFrame implements ActionListener, WindowListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -5003115022942169489L;
	private JTextField rawDataDirTextField;
	private JTextField zipDirTextField;
	private JTextField manifestTextField;
	private JFileChooser chooser;
	private File inputFile;
	private File baseDirectory;
	private JButton cleanAndZipButton;
	private JButton btnCreateChecksumFile;
	private JButton manifestBrowseButton;
	private JButton zipDirBrowseButton;
	private JButton rawDataBrowseButton;

	public static final String BROWSE_FOR_RAW_DATA_DIR = "Select raw data directory";
	public static final String BROWSE_FOR_ZIP_DIR = "Select ZIP destination directory";
	public static final String BROWSE_FOR_MANIFEST_FILE = "Select input manifest file";
	public static final String CLEAN_AND_ZIP = "CLEAN_AND_ZIP";
	public static final String HASH_AND_MANIFEST = "HASH_AND_MANIFEST";

	private String fileSelectType;
	private IndeterminateProgressDialog idp;
	private JTextArea textArea;

	public static void main(String[] args) {

		MotrPacDataCleaner sm = new MotrPacDataCleaner();
		sm.setVisible(true);
	}

	public MotrPacDataCleaner() {

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
		setTitle("MOTRPAC raw data upload preparation");
		setSize(new Dimension(800, 400));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 86, 86, 86, 89, 0};
		gbl_panel.rowHeights = new int[]{23, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblSmiles = new JLabel("Data directory");
		GridBagConstraints gbc_lblSmiles = new GridBagConstraints();
		gbc_lblSmiles.insets = new Insets(0, 0, 5, 5);
		gbc_lblSmiles.anchor = GridBagConstraints.EAST;
		gbc_lblSmiles.gridx = 0;
		gbc_lblSmiles.gridy = 0;
		panel.add(lblSmiles, gbc_lblSmiles);

		rawDataDirTextField = new JTextField();
		//rawDataDirTextField.setEditable(false);
		GridBagConstraints gbc_rawDataTextField = new GridBagConstraints();
		gbc_rawDataTextField.gridwidth = 3;
		gbc_rawDataTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rawDataTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rawDataTextField.gridx = 1;
		gbc_rawDataTextField.gridy = 0;
		panel.add(rawDataDirTextField, gbc_rawDataTextField);
		rawDataDirTextField.setColumns(10);

		rawDataBrowseButton = new JButton("Browse ...");
		rawDataBrowseButton.setActionCommand(BROWSE_FOR_RAW_DATA_DIR);
		rawDataBrowseButton.addActionListener(this);
		GridBagConstraints gbc_rawDataBrowseButton = new GridBagConstraints();
		gbc_rawDataBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_rawDataBrowseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_rawDataBrowseButton.anchor = GridBagConstraints.NORTH;
		gbc_rawDataBrowseButton.gridx = 4;
		gbc_rawDataBrowseButton.gridy = 0;
		panel.add(rawDataBrowseButton, gbc_rawDataBrowseButton);

		JLabel lblPeptide = new JLabel("ZIP directory");
		GridBagConstraints gbc_lblPeptide = new GridBagConstraints();
		gbc_lblPeptide.anchor = GridBagConstraints.EAST;
		gbc_lblPeptide.insets = new Insets(0, 0, 5, 5);
		gbc_lblPeptide.gridx = 0;
		gbc_lblPeptide.gridy = 1;
		panel.add(lblPeptide, gbc_lblPeptide);

		zipDirTextField = new JTextField();
		//zipDirTextField.setEditable(false);
		GridBagConstraints gbc_zipDirTextField = new GridBagConstraints();
		gbc_zipDirTextField.gridwidth = 3;
		gbc_zipDirTextField.insets = new Insets(0, 0, 5, 5);
		gbc_zipDirTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_zipDirTextField.gridx = 1;
		gbc_zipDirTextField.gridy = 1;
		panel.add(zipDirTextField, gbc_zipDirTextField);
		zipDirTextField.setColumns(10);

		zipDirBrowseButton = new JButton("Browse ...");
		zipDirBrowseButton.setActionCommand(BROWSE_FOR_ZIP_DIR);
		zipDirBrowseButton.addActionListener(this);
		GridBagConstraints gbc_zipDirBrowseButton = new GridBagConstraints();
		gbc_zipDirBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_zipDirBrowseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_zipDirBrowseButton.gridx = 4;
		gbc_zipDirBrowseButton.gridy = 1;
		panel.add(zipDirBrowseButton, gbc_zipDirBrowseButton);

		JLabel lblManifestInput = new JLabel("Manifest input");
		GridBagConstraints gbc_lblManifestInput = new GridBagConstraints();
		gbc_lblManifestInput.anchor = GridBagConstraints.EAST;
		gbc_lblManifestInput.insets = new Insets(0, 0, 5, 5);
		gbc_lblManifestInput.gridx = 0;
		gbc_lblManifestInput.gridy = 2;
		panel.add(lblManifestInput, gbc_lblManifestInput);

		manifestTextField = new JTextField();
		manifestTextField.setEditable(false);
		GridBagConstraints gbc_manifestTextField = new GridBagConstraints();
		gbc_manifestTextField.gridwidth = 3;
		gbc_manifestTextField.insets = new Insets(0, 0, 5, 5);
		gbc_manifestTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_manifestTextField.gridx = 1;
		gbc_manifestTextField.gridy = 2;
		panel.add(manifestTextField, gbc_manifestTextField);
		manifestTextField.setColumns(10);

		manifestBrowseButton = new JButton("Browse ...");
		manifestBrowseButton.setActionCommand(BROWSE_FOR_MANIFEST_FILE);
		manifestBrowseButton.addActionListener(this);
		GridBagConstraints gbc_manifestBrowseButton = new GridBagConstraints();
		gbc_manifestBrowseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_manifestBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_manifestBrowseButton.gridx = 4;
		gbc_manifestBrowseButton.gridy = 2;
		panel.add(manifestBrowseButton, gbc_manifestBrowseButton);

		cleanAndZipButton = new JButton("Remove \"Results\" and ZIP");
		cleanAndZipButton.setActionCommand(CLEAN_AND_ZIP);
		cleanAndZipButton.addActionListener(this);

		textArea = new JTextArea();
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 5;
		gbc_textArea.insets = new Insets(0, 0, 5, 5);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 3;
		panel.add(textArea, gbc_textArea);
		GridBagConstraints gbc_cleanAndZipButton = new GridBagConstraints();
		gbc_cleanAndZipButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_cleanAndZipButton.insets = new Insets(0, 0, 0, 5);
		gbc_cleanAndZipButton.gridx = 2;
		gbc_cleanAndZipButton.gridy = 4;
		panel.add(cleanAndZipButton, gbc_cleanAndZipButton);

		btnCreateChecksumFile = new JButton("Calculate checksums and create manifest");
		btnCreateChecksumFile.setActionCommand(HASH_AND_MANIFEST);
		btnCreateChecksumFile.addActionListener(this);
		GridBagConstraints gbc_btnCreateChecksumFile = new GridBagConstraints();
		gbc_btnCreateChecksumFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCreateChecksumFile.gridwidth = 2;
		gbc_btnCreateChecksumFile.gridx = 3;
		gbc_btnCreateChecksumFile.gridy = 4;
		panel.add(btnCreateChecksumFile, gbc_btnCreateChecksumFile);

		addWindowListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(BROWSE_FOR_RAW_DATA_DIR) ||
				command.equals(BROWSE_FOR_ZIP_DIR) ||
				command.equals(BROWSE_FOR_MANIFEST_FILE)) {

			fileSelectType = command;
			selectFileOrDirectory(command);
		}
		if (command.equals(CLEAN_AND_ZIP))
			cleanAndZip();

		if (command.equals(HASH_AND_MANIFEST))
			hashAndManifest();

		if (event.getSource().equals(chooser)) {

			if (event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {

				inputFile = chooser.getSelectedFile();
				baseDirectory = inputFile.getParentFile();

				if(fileSelectType.equals(BROWSE_FOR_RAW_DATA_DIR))
					rawDataDirTextField.setText(inputFile.getAbsolutePath());

				if(fileSelectType.equals(BROWSE_FOR_ZIP_DIR))
					zipDirTextField.setText(inputFile.getAbsolutePath());

				if(fileSelectType.equals(BROWSE_FOR_MANIFEST_FILE))
					manifestTextField.setText(inputFile.getAbsolutePath());
			}
		}
	}
	
	private void selectFileOrDirectory(String command) {

		chooser = new ImprovedFileChooser();
		chooser.setPreferredSize(new Dimension(800, 640));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setDialogTitle(command);
		chooser.setApproveButtonText(command);

		if(command.equals(BROWSE_FOR_MANIFEST_FILE))
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		else
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if(baseDirectory == null)
			baseDirectory = new File(".");

		chooser.setCurrentDirectory(baseDirectory);
		chooser.showOpenDialog(this);
	}

	private void hashAndManifest() {
		// TODO Auto-generated method stub

	}

	private void cleanAndZip() {

		if(rawDataDirTextField.getText().isEmpty()) {
			MessageDialog.showErrorMsg("Raw data directory not specified.", this);
			return;
		}
		if(zipDirTextField.getText().isEmpty()) {
			MessageDialog.showErrorMsg("ZIP output directory not specified.", this);
			return;
		}
		DataProcessingTask task = new DataProcessingTask(CLEAN_AND_ZIP);
		idp = new IndeterminateProgressDialog("Cleaning and compressing data ...", this, task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	class DataProcessingTask extends LongUpdateTask {

		private String command;
		public DataProcessingTask(String command) {
			super();
			this.command = command;
		}

		@Override
		public Void doInBackground() {

			if(command.equals(CLEAN_AND_ZIP)) {

				RawDataUploadUtils.deleteResultsFolders(rawDataDirTextField.getText());
//				RawDataUploadUtils.zipIndividualRawDataFiles(
//					rawDataDirTextField.getText(), zipDirTextField.getText());
//
//				RawDataUploadUtils.calculateChecksums(zipDirTextField.getText());
				File zipDir = null;
				try {
					zipDir = RawDataUploadUtils.createOrRetrieveDirectory(zipDirTextField.getText());
				} catch (IOException e) {
					e.printStackTrace();
				}
				String command = "";
				if(zipDir != null) {

					command =
						"for /D %d in (\"" + rawDataDirTextField.getText() + File.separator +
						"*.*\") do 7z a -tzip \"" + zipDirTextField.getText() + File.separator +"%~nd.zip\" \"%d\"";
				}
				String checksumCommand =
					"7z h -scrcsha256 \"" + zipDirTextField.getText() +
					File.separator + "*.zip\" > \"" + zipDirTextField.getText() + File.separator + "checksum.txt\"";

				textArea.setText(command + "\n" + checksumCommand);
			}
			return null;
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























