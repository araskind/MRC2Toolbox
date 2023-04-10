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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.report;

import java.awt.BorderLayout;
import java.awt.Desktop;
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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;

import com.github.lgooddatepicker.components.DatePicker;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.asssay.MotrpacAssayTable;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.tcode.TissueCodeTable;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class CreateUploadDirectoryStructureDialog extends JDialog 
		implements ActionListener, ItemListener, BackedByPreferences {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8281770101382812047L;

	private static final Icon createReportDirIcon = GuiUtils.getIcon("newProject", 32);
	public static final String SELECT_PARENT_DIR = "BROWSE_FOR_INPUT";
	
	private static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.gui.mptrack.CreateUploadDirectoryStructureDialog";
	private static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private Preferences preferences;
	
	public static final DateFormat defaultDateFormat = 
			new SimpleDateFormat("yyyyMMdd");
	
	private JTextField parentDirectoryTextField;
	private File baseDirectory, parentDir;
	private JComboBox studyCodeComboBox;
	private JSpinner batchNumberSpinner;
	private DatePicker batchDatePicker;
	private DatePicker processedDatePicker;
	private TissueCodeTable tissueCodeTable;
	private MotrpacAssayTable motrpacAssayTable;
	
	public CreateUploadDirectoryStructureDialog() {
		super();
		setSize(new Dimension(640, 640));
		setPreferredSize(new Dimension(640, 640));
		setIconImage(((ImageIcon) createReportDirIcon).getImage());
		setTitle("Create directory structure for MoTrPAC upload ");
		setModalityType(ModalityType.APPLICATION_MODAL);		
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{114, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Parent directory");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 4;
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		parentDirectoryTextField = new JTextField();
		parentDirectoryTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 4;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panel.add(parentDirectoryTextField, gbc_textField);
		parentDirectoryTextField.setColumns(10);
		
		JButton selectParentDirButton = new JButton("Browse");
		selectParentDirButton.setActionCommand(SELECT_PARENT_DIR);
		selectParentDirButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 1;
		panel.add(selectParentDirButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Study code");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		Collection<MoTrPACStudy> studyList = 
				MoTrPACDatabaseCache.getMotrpacStudyList();
		studyCodeComboBox = new JComboBox<MoTrPACStudy>(
				new DefaultComboBoxModel<MoTrPACStudy>(
						studyList.toArray(new MoTrPACStudy[studyList.size()])));
		studyCodeComboBox.setSelectedIndex(-1);
		studyCodeComboBox.addItemListener(this);
		GridBagConstraints gbc_studyCodeComboBox = new GridBagConstraints();
		gbc_studyCodeComboBox.gridwidth = 3;
		gbc_studyCodeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_studyCodeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_studyCodeComboBox.gridx = 1;
		gbc_studyCodeComboBox.gridy = 2;
		panel.add(studyCodeComboBox, gbc_studyCodeComboBox);
				
		JLabel lblNewLabel_2 = new JLabel("Tissue codes");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 3;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		tissueCodeTable = new TissueCodeTable();
		JScrollPane scrollPane = new JScrollPane(tissueCodeTable);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		panel.add(scrollPane, gbc_scrollPane);		
		
		JLabel lblNewLabel_5 = new JLabel("Assays");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 5;
		panel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		motrpacAssayTable = new MotrpacAssayTable();		
		JScrollPane scrollPane_1 = new JScrollPane(motrpacAssayTable);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridwidth = 5;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 6;
		panel.add(scrollPane_1, gbc_scrollPane_1);
		
		JLabel lblNewLabel_3 = new JLabel("Batch #");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 7;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		batchNumberSpinner = new JSpinner();
		batchNumberSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		batchNumberSpinner.setPreferredSize(new Dimension(60, 20));
		batchNumberSpinner.setSize(new Dimension(60, 20));
		batchNumberSpinner.setMinimumSize(new Dimension(60, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 7;
		panel.add(batchNumberSpinner, gbc_spinner);
		
		JLabel lblNewLabel_4 = new JLabel("Batch date");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 7;
		panel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		batchDatePicker = new DatePicker();
		batchDatePicker.setDate(localDate);

		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 3;
		gbc_btnNewButton_1.gridy = 7;
		panel.add(batchDatePicker, gbc_btnNewButton_1);
		
		JLabel lblNewLabel_4_1 = new JLabel("Processed date");
		GridBagConstraints gbc_lblNewLabel_4_1 = new GridBagConstraints();
		gbc_lblNewLabel_4_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4_1.gridx = 2;
		gbc_lblNewLabel_4_1.gridy = 8;
		panel.add(lblNewLabel_4_1, gbc_lblNewLabel_4_1);
		
		processedDatePicker = new DatePicker();
		processedDatePicker.setDate(localDate);

		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
		gbc_btnNewButton_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_2.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_2.gridx = 3;
		gbc_btnNewButton_2.gridy = 8;
		panel.add(processedDatePicker, gbc_btnNewButton_2);
	
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.CREATE_DIRECTORY_STRUCTURE_FOR_BIC_UPLOAD.getName());
		btnSave.setActionCommand(
				MainActionCommands.CREATE_DIRECTORY_STRUCTURE_FOR_BIC_UPLOAD.getName());
		btnSave.addActionListener(this);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadPreferences();
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if (command.equals(SELECT_PARENT_DIR))
			selectInputFile();
		
		if(command.equals(MainActionCommands.CREATE_DIRECTORY_STRUCTURE_FOR_BIC_UPLOAD.getName())) {
			try {
				createExperimentDirectoryStructure();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private void selectInputFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select parent directory");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			parentDir = fc.getSelectedFile();
			baseDirectory = parentDir.getParentFile();
			parentDirectoryTextField.setText(parentDir.getAbsolutePath());
			savePreferences();
		}
	}
	
	public Date getBatchDate() {
		
		if(batchDatePicker.getDate() == null)
			return null;

		return Date.from(batchDatePicker.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	private MoTrPACStudy getSelectedStudy() {
		return (MoTrPACStudy)studyCodeComboBox.getSelectedItem();
	}
	
	
	public Date getProcessedDate() {
		
		if(processedDatePicker.getDate() == null)
			return null;

		return Date.from(processedDatePicker.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	private Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
		if(parentDir == null)
			errors.add("Parent directory not specified");
				
		if(studyCodeComboBox.getSelectedItem() == null)
			errors.add("Study not specified");
		
		if(getBatchDate() == null)
			errors.add("Batch date not specified");
		
		if(getProcessedDate() == null)
			errors.add("Processed date not specified");
		
		return errors;
	}
	
	private void createExperimentDirectoryStructure() throws IOException {

		Collection<String>errors = validateInput();
		if(!errors.isEmpty()) {			
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		//	Create study code directory
		MoTrPACStudy study = getSelectedStudy();
		Path studyDirPath = Files.createDirectories(
				Paths.get(parentDir.getAbsolutePath(), study.getCode()));
		
		int batchNumber = (int)batchNumberSpinner.getValue();
		String batchId = "BATCH" + Integer.toString(batchNumber) + "_" + defaultDateFormat.format(getBatchDate());
		String processedFolderId  = "PROCESSED_" + defaultDateFormat.format(getProcessedDate());
		Collection<MoTrPACTissueCode> tissueCodes = study.getAllTissueCodes();
		MoTrPACTissueCode selectedTissue = tissueCodeTable.getSelectedCode();
		if(selectedTissue != null)
			tissueCodes = Collections.singleton(selectedTissue);

		for(MoTrPACTissueCode tissue : tissueCodes) {

			for( MoTrPACAssay assay : study.getAssays()) {
				
				Files.createDirectories(
						Paths.get(studyDirPath.toString(), tissue.getCode(), assay.getBucketCode(), batchId, "RAW"));
				Files.createDirectories(
						Paths.get(studyDirPath.toString(), tissue.getCode(), assay.getBucketCode(), batchId, processedFolderId, "NAMED"));
				Files.createDirectories(
						Paths.get(studyDirPath.toString(), tissue.getCode(), assay.getBucketCode(), batchId, processedFolderId, "UNNAMED"));
				
				//	Write metadata  phase file
				Path outputPath = 
						Paths.get(studyDirPath.toString(), tissue.getCode(), assay.getBucketCode(), batchId, "metadata_phase.txt");
				try {
					Files.write(outputPath, 
							Collections.singleton(study.getCode()), 
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if(studyDirPath.toFile() != null && studyDirPath.toFile().exists()) {

			if(MessageDialog.showChoiceMsg("Directory structure created, do you want to open containing folder?",
				this.getContentPane()) == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().open(studyDirPath.toFile());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof MoTrPACStudy) {
			
			MoTrPACStudy study = (MoTrPACStudy)e.getItem();
			tissueCodeTable.setTableModelFromTissueCodes(study.getAllTissueCodes());
			motrpacAssayTable.setTableModelFromAssays(study.getAssays());
		}
	}

	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
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


}
