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

package edu.umich.med.mrc2.datoolbox.gui.io.msms;

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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MSMSDecoyGenerationMethod;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.msms.MSMSDecoyLibraryImportTask;

public class DecoyMSMSLibraryImportDialog extends JDialog 
		implements ActionListener, BackedByPreferences {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2275045088442494127L;
	
	private static final Icon libraryImportIcon = GuiUtils.getIcon("importLibraryToDb", 32);
	private TaskListener taskListener;
	public static final String BROWSE = "BROWSE";
	private File baseDirectory;	
	private Preferences preferences;
	public static final String BASE_DIR = "BASE_DIR";
	private JTextField intputFileTextField;
	private JTextField libraryNameTextField;
	private JComboBox polarityComboBox;
	private JTextArea descriptionTextArea;
	private JCheckBox appendCheckBox;
	private JComboBox libraryComboBox;
	private JComboBox decoyGenerationMethodComboBox;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DecoyMSMSLibraryImportDialog(TaskListener taskListener) {
		super();
		this.taskListener = taskListener;
		setPreferredSize(new Dimension(800, 300));
		setSize(new Dimension(800, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Import decoy MSMS library");
		setIconImage(((ImageIcon) libraryImportIcon).getImage());
					
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{112, 0, 246, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Input file");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		intputFileTextField = new JTextField();
		intputFileTextField.setEditable(false);
		GridBagConstraints gbc_intputFileTextField = new GridBagConstraints();
		gbc_intputFileTextField.gridwidth = 3;
		gbc_intputFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_intputFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_intputFileTextField.gridx = 1;
		gbc_intputFileTextField.gridy = 0;
		panel_1.add(intputFileTextField, gbc_intputFileTextField);
		intputFileTextField.setColumns(10);
		
		JButton btnNewButton = new JButton("Browse ...");
		btnNewButton.setActionCommand(BROWSE);
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 0;
		panel_1.add(btnNewButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_3 = new JLabel("Decoy generation method");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.gridwidth = 2;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 1;
		panel_1.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		Collection<MSMSDecoyGenerationMethod>decoyMethods = 
				IDTDataCash.getMsmsDecoyGenerationMethodList();
		DefaultComboBoxModel dgmodel = 
				new DefaultComboBoxModel<MSMSDecoyGenerationMethod>(
						decoyMethods.toArray(new MSMSDecoyGenerationMethod[decoyMethods.size()]));
		decoyGenerationMethodComboBox = new JComboBox(dgmodel);
		decoyGenerationMethodComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 3;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 1;
		panel_1.add(decoyGenerationMethodComboBox, gbc_comboBox);
		
		appendCheckBox = new JCheckBox("Append to existing library");
		GridBagConstraints gbc_appendCheckBox = new GridBagConstraints();
		gbc_appendCheckBox.anchor = GridBagConstraints.EAST;
		gbc_appendCheckBox.gridwidth = 2;
		gbc_appendCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_appendCheckBox.gridx = 0;
		gbc_appendCheckBox.gridy = 2;
		panel_1.add(appendCheckBox, gbc_appendCheckBox);
		
		Collection<ReferenceMsMsLibrary>decoys = IDTDataCash.getDecoyLibraries();
		DefaultComboBoxModel model = 
				new DefaultComboBoxModel<ReferenceMsMsLibrary>(decoys.toArray(new ReferenceMsMsLibrary[decoys.size()]));
		libraryComboBox = new JComboBox(model);
		libraryComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_libraryComboBox = new GridBagConstraints();
		gbc_libraryComboBox.gridwidth = 3;
		gbc_libraryComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_libraryComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_libraryComboBox.gridx = 2;
		gbc_libraryComboBox.gridy = 2;
		panel_1.add(libraryComboBox, gbc_libraryComboBox);
		
		JLabel lblNewLabel = new JLabel("New library name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 3;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		libraryNameTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 3;
		panel_1.add(libraryNameTextField, gbc_textField);
		libraryNameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Polarity");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 3;
		gbc_lblNewLabel_1.gridy = 3;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		polarityComboBox = new JComboBox(new DefaultComboBoxModel<Polarity>(
				new Polarity[] {
						Polarity.Positive, 
						Polarity.Negative}));
		polarityComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_polarityComboBox = new GridBagConstraints();
		gbc_polarityComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_polarityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_polarityComboBox.gridx = 4;
		gbc_polarityComboBox.gridy = 3;
		panel_1.add(polarityComboBox, gbc_polarityComboBox);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new TitledBorder(null, "Description", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		panel_1.add(scrollPane, gbc_scrollPane);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setWrapStyleWord(true);
		scrollPane.setViewportView(descriptionTextArea);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		
		JButton createDecoyButton = new JButton(
				MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName());
		createDecoyButton.setActionCommand(
				MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName());
		createDecoyButton.addActionListener(this);
		panel.add(createDecoyButton);

		JRootPane rootPane = SwingUtilities.getRootPane(createDecoyButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(createDecoyButton);
		
		loadPreferences();
		pack();
	}

	@Override
	public void dispose() {		
		savePreferences();
		super.dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE))
			selectLibraryFile();
		
		if(e.getActionCommand().equals(MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName()))
			importDecoyLibrary();
	}
	
	private void selectLibraryFile() {
		
		ImprovedFileChooser intputFileChooser = new ImprovedFileChooser();
		intputFileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		intputFileChooser.addActionListener(this);
		intputFileChooser.setAcceptAllFileFilterUsed(false);
		intputFileChooser.resetChoosableFileFilters();
		intputFileChooser.setFileFilter(
				new FileNameExtensionFilter("NIST MS files", 
						MsLibraryFormat.MSP.getFileExtension()));
		intputFileChooser.setMultiSelectionEnabled(false);
		intputFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		intputFileChooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
		intputFileChooser.setCurrentDirectory(baseDirectory);
		intputFileChooser.setApproveButtonText("Select input file");		
		if(intputFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			
			intputFileTextField.setText(intputFileChooser.getSelectedFile().getAbsolutePath());
			baseDirectory = intputFileChooser.getSelectedFile();
			savePreferences();
		}
	}
	
	private void importDecoyLibrary() {
		
		File  libraryFile = null;
		String libFilePath = intputFileTextField.getText().trim();
		if(libFilePath.isEmpty())
			return;
		
		Collection<String>errors = verifyInput();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		libraryFile = Paths.get(libFilePath).toFile();
		Polarity polarity = (Polarity) polarityComboBox.getSelectedItem();
		
		MSMSDecoyGenerationMethod method = 
				(MSMSDecoyGenerationMethod)decoyGenerationMethodComboBox.getSelectedItem();
		
		ReferenceMsMsLibrary refLib = null;
		boolean append = appendCheckBox.isSelected();
		if(append) {
			refLib = (ReferenceMsMsLibrary) libraryComboBox.getSelectedItem();
		}
		else {
			String libraryName = libraryNameTextField.getText().trim();
			String searchOutputCode = libraryName.replaceAll("\\W+", "_").toLowerCase();		
			String libraryDescription = descriptionTextArea.getText().trim();
			
			refLib = new ReferenceMsMsLibrary(
					null,
					libraryName,
					libraryDescription,
					searchOutputCode,
					null,
					new Date(),
					new Date(),
					false,
					true);
		}
		MSMSDecoyLibraryImportTask task = 
				new MSMSDecoyLibraryImportTask(libraryFile, refLib, polarity, method, append);
		task.addTaskListener(taskListener);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		savePreferences();
		dispose();
	}
	
	private Collection<String>verifyInput(){
		
		Collection<String>errors = new ArrayList<String>();
		
		File libraryFile = Paths.get(intputFileTextField.getText().trim()).toFile();
		if(!libraryFile.exists() || !libraryFile.canRead()) 
			errors.add("Library file missing or unreadable.");
		
		if(polarityComboBox.getSelectedItem() == null) 
			errors.add("Polarity has to be specified.");
		
		if(decoyGenerationMethodComboBox.getSelectedItem() == null) 
			errors.add("Decoy generation method has to be specified.");
		
		if(appendCheckBox.isSelected()) {
			
			if(libraryComboBox.getSelectedIndex() == -1)
				errors.add("Please select the library to append data.");
		}
		else {
			String libraryName = libraryNameTextField.getText().trim();
			if(libraryNameTextField.getText().trim().isEmpty())
				errors.add("Library name has to be specified.");

			if(descriptionTextArea.getText().trim().isEmpty())
				errors.add("Library description has to be specified.");
			
			ReferenceMsMsLibrary existisngLib = 
					IDTDataCash.getReferenceMsMsLibraryByName(libraryName);
			if(existisngLib != null) 
				errors.add("Referemce library \"" + libraryName + "\" already exists.");
		}			
		return errors;
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory =
				new File(preferences.get(BASE_DIR, 
						Paths.get(System.getProperty("user.dir"), "data", "mssearch").toString()));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIR, baseDirectory.getAbsolutePath());
	}
}
