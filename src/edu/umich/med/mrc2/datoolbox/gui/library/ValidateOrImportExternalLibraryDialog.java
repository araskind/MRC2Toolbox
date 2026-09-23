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

package edu.umich.med.mrc2.datoolbox.gui.library;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.database.idt.BasePCDLutils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.library.manager.LibraryListingTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class ValidateOrImportExternalLibraryDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = -4474046301546161666L;

	private static final Icon dialogIcon = GuiUtils.getIcon("alignment", 32);
	private JTextField libFileTextField;
	private LibraryListingTable libraryListingTable;
	private File libraryFile;
	private File baseDirectory;
	
	private static final String BROWSE_COMMAND = "Browse";

	public ValidateOrImportExternalLibraryDialog(ActionListener actionListener) {
		super();
		setTitle("");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(600, 250));
		setSize(new Dimension(600, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		libraryListingTable = new LibraryListingTable();
		
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		dataPanel.add(new JScrollPane(libraryListingTable), gbc_scrollPane);
		
		libFileTextField = new JTextField();
		GridBagConstraints gbc_libFileTextField = new GridBagConstraints();
		gbc_libFileTextField.insets = new Insets(0, 0, 0, 5);
		gbc_libFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_libFileTextField.gridx = 0;
		gbc_libFileTextField.gridy = 1;
		dataPanel.add(libFileTextField, gbc_libFileTextField);
		libFileTextField.setColumns(10);
		
		JButton btnNewButton = new JButton(BROWSE_COMMAND);
		btnNewButton.setActionCommand(BROWSE_COMMAND);
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 1;
		dataPanel.add(btnNewButton, gbc_btnNewButton);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(e -> dispose());
		buttonPanel.add(btnCancel);

		JButton btnSave = new JButton(
				MainActionCommands.RUN_PRESCAN_AGAINST_MASTER_LIBRARY_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.RUN_PRESCAN_AGAINST_MASTER_LIBRARY_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		buttonPanel.add(btnSave);
		
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(al -> { dispose(); }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		baseDirectory = new File(MRC2ToolBoxCore.dataDir);
		refreshLibraryListing();
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectLibraryFile();	
	}

	private void selectLibraryFile() {

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.addFilter("Library Editor files", "xml", "XML");	
		fc.addFilter("TAB-separated text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select library file to scan");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {		

			libraryFile = fc.getSelectedFile();
			baseDirectory = libraryFile.getParentFile();
			
			//	TODO Agilent Library Editor files
			if (libraryFile.getName().toLowerCase().endsWith("mslibrary.xml")) {

				MessageDialog.showWarningMsg(
						"Library Editor files validation under development.", this.getContentPane());
				libraryFile = null;
				return;
			}
			//	TODO Agilent CEF files
			if (libraryFile.getName().toLowerCase().endsWith(".cef")) {
				MessageDialog.showWarningMsg(
						"CEF library validation under development.", this.getContentPane());
				libraryFile = null;
				return;
			}
			if (libraryFile.getName().toLowerCase().endsWith(".txt")
					|| libraryFile.getName().toLowerCase().endsWith(".tsv")) {
				libFileTextField.setText(libraryFile.getAbsolutePath());
			}
		}		
	}
	
	private synchronized void refreshLibraryListing(){

		IDTDataCache.refreshMsRtLibraryList();		
		Collection<CompoundLibrary> libList = IDTDataCache.getMsRtLibraryList();
		String masterId = null;
		try {
			masterId = BasePCDLutils.getMasterLibraryID();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		libraryListingTable.setTableModelFromLibraryCollection(
				libList, IDTDataCache.getMSRTLibraryById(masterId));
	}
	
	public CompoundLibrary getSelectedLibrary() {
		return libraryListingTable.getSelectedLibrary();
	}
	
	public File getInputLibraryFile() {
		return libraryFile;
	}
	
	public Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<String>();
	    
	    if(getSelectedLibrary() == null)
	        errors.add("Reference library not specified.");
	    
	    if(libraryFile == null || !libraryFile.exists())
	        errors.add("Input library file not specified.");
	    		
	    return errors;
	}
}










