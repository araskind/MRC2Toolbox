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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCodeBlock;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MotrpacReportEmptyFileGeneratorDialog extends JDialog implements ActionListener, BackedByPreferences, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5201210444410166905L;
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.gui.MotrpacReportEmptyFileGeneratorDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final Icon addProtocolIcon = GuiUtils.getIcon("addSop", 32);
	private static final Icon editProtocolIcon = GuiUtils.getIcon("editSop", 32);
	private static final String BROWSE_COMMAND = "BROWSE_COMMAND";

	private JComboBox studyComboBox;
	private JComboBox assayComboBox;
	private JComboBox tissueComboBox;
	private JComboBox namingComboBox;
	private JButton btnSave;
	private JTextField reportFolderTextField;
	private JLabel authorInfoLabel;
	private JLabel dateCreatedValueLabel;
	private JButton browseButton;
	private File baseDirectory;
	private ImprovedFileChooser chooser;
	private boolean allowDropFile;
	private Collection<MotrpacReportCodeSelectorPanel>codeSelectors;
	private JTextField expPrefixTextField;
	
	private static final String[]reportFileNames = new String[] {
			"metadata_experimentalDetails",
			"metadata_metabolites",
			"metadata_sample",
			"results_metabolites"
			};

	@SuppressWarnings("unchecked")
	public MotrpacReportEmptyFileGeneratorDialog() {
		super();

		setTitle("Create empty files for MoTrPAC data upload");
		setIconImage(((ImageIcon) addProtocolIcon).getImage());
		setPreferredSize(new Dimension(700, 700));
		setSize(new Dimension(700, 700));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[] {50, 100, 50, 100, 50, 100};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 0.0, 1.0};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		int rowCount = 0;
		JLabel lblName = new JLabel("MoTrPAC study");
		lblName.setForeground(Color.BLACK);
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = rowCount;
		dataPanel.add(lblName, gbc_lblName);

		studyComboBox = new JComboBox(
				new SortedComboBoxModel<MoTrPACStudy>(MoTrPACDatabaseCash.getMotrpacStudyList()));
		studyComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_studyComboBox = new GridBagConstraints();
		gbc_studyComboBox.gridwidth = 5;
		gbc_studyComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_studyComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_studyComboBox.gridx = 1;
		gbc_studyComboBox.gridy = rowCount;
		dataPanel.add(studyComboBox, gbc_studyComboBox);
		
		rowCount++;
		
		JLabel lblNewLabel = new JLabel("Experiment prefix");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = rowCount;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		rowCount++;
		
		expPrefixTextField = new JTextField();
		GridBagConstraints gbc_expPrefixTextField = new GridBagConstraints();
		gbc_expPrefixTextField.gridwidth = 4;
		gbc_expPrefixTextField.insets = new Insets(0, 0, 5, 5);
		gbc_expPrefixTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_expPrefixTextField.gridx = 1;
		gbc_expPrefixTextField.gridy = 1;
		dataPanel.add(expPrefixTextField, gbc_expPrefixTextField);
		expPrefixTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Assay");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = rowCount;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		assayComboBox = new JComboBox(
				new SortedComboBoxModel<MoTrPACAssay>(MoTrPACDatabaseCash.getMotrpacAssayList()));
		assayComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_assayComboBox = new GridBagConstraints();
		gbc_assayComboBox.gridwidth = 5;
		gbc_assayComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_assayComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_assayComboBox.gridx = 1;
		gbc_assayComboBox.gridy = rowCount;
		dataPanel.add(assayComboBox, gbc_assayComboBox);
		
		rowCount++;
			
		JLabel lblNewLabel_3 = new JLabel("Tissue");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = rowCount;
		dataPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		tissueComboBox = new JComboBox();
		GridBagConstraints gbc_tissueSelectorComboBox = new GridBagConstraints();
		gbc_tissueSelectorComboBox.gridwidth = 5;
		gbc_tissueSelectorComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_tissueSelectorComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_tissueSelectorComboBox.gridx = 1;
		gbc_tissueSelectorComboBox.gridy = rowCount;
		dataPanel.add(tissueComboBox, gbc_tissueSelectorComboBox);
		
		rowCount++;
		
		Collection<MoTrPACReportCodeBlock> codeBlocks = 
				MoTrPACDatabaseCash.getMotrpacReportCodeBlocks();
		codeSelectors = new ArrayList<MotrpacReportCodeSelectorPanel>();
		for(MoTrPACReportCodeBlock block : codeBlocks) {

			MotrpacReportCodeSelectorPanel mpcp = new MotrpacReportCodeSelectorPanel(block);
			codeSelectors.add(mpcp);
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.gridwidth = 6;
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = rowCount;
			dataPanel.add(mpcp, gbc_panel_1);
			rowCount++;
		}		
		
		rowCount++;
		
		JLabel lblNewLabel_5 = new JLabel("Naming");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = rowCount;
		dataPanel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		namingComboBox = new JComboBox<String>(
				new DefaultComboBoxModel<String>(
						new String[] {"named", "unnamed"}));
		GridBagConstraints gbc_namingComboBox = new GridBagConstraints();
		gbc_namingComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_namingComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_namingComboBox.gridx = 1;
		gbc_namingComboBox.gridy = rowCount;
		dataPanel.add(namingComboBox, gbc_namingComboBox);
		
		rowCount++;
		
		JLabel lblNewLabel_2 = new JLabel("  ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = rowCount;
		dataPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		rowCount++;
		
		JLabel lblAuthor = new JLabel("Author");
		GridBagConstraints gbc_lblAuthor = new GridBagConstraints();
		gbc_lblAuthor.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblAuthor.insets = new Insets(0, 0, 5, 5);
		gbc_lblAuthor.gridx = 0;
		gbc_lblAuthor.gridy = rowCount;
		dataPanel.add(lblAuthor, gbc_lblAuthor);

		authorInfoLabel = new JLabel(MRC2ToolBoxCore.getIdTrackerUser().getInfo());
		GridBagConstraints gbc_authorInfoLabel = new GridBagConstraints();
		gbc_authorInfoLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_authorInfoLabel.gridwidth = 2;
		gbc_authorInfoLabel.insets = new Insets(0, 0, 5, 5);
		gbc_authorInfoLabel.gridx = 1;
		gbc_authorInfoLabel.gridy = rowCount;
		dataPanel.add(authorInfoLabel, gbc_authorInfoLabel);

		JLabel lblDateCreated = new JLabel("Date created");
		GridBagConstraints gbc_lblDateCreated = new GridBagConstraints();
		gbc_lblDateCreated.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblDateCreated.insets = new Insets(0, 0, 5, 5);
		gbc_lblDateCreated.gridx = 4;
		gbc_lblDateCreated.gridy = rowCount;
		dataPanel.add(lblDateCreated, gbc_lblDateCreated);

		dateCreatedValueLabel = new JLabel(
				MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));
		GridBagConstraints gbc_dateValueLAbelLabel = new GridBagConstraints();
		gbc_dateValueLAbelLabel.anchor = GridBagConstraints.NORTH;
		gbc_dateValueLAbelLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateValueLAbelLabel.insets = new Insets(0, 0, 5, 0);
		gbc_dateValueLAbelLabel.gridx = 5;
		gbc_dateValueLAbelLabel.gridy = rowCount;
		dataPanel.add(dateCreatedValueLabel, gbc_dateValueLAbelLabel);
		
		rowCount++;
		
		JLabel lblNewLabel_4 = new JLabel("  ");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = rowCount;
		dataPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);

		rowCount++;
		
		reportFolderTextField = new JTextField();
		reportFolderTextField.setEditable(false);
		GridBagConstraints gbc_reportFolderTextField = new GridBagConstraints();
		gbc_reportFolderTextField.gridwidth = 6;
		gbc_reportFolderTextField.insets = new Insets(0, 0, 5, 0);
		gbc_reportFolderTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_reportFolderTextField.gridx = 0;
		gbc_reportFolderTextField.gridy = rowCount;
		dataPanel.add(reportFolderTextField, gbc_reportFolderTextField);
		
		rowCount++;
		
		browseButton = new JButton("Select report folder");
		browseButton.setActionCommand(BROWSE_COMMAND);	//	TODO
		browseButton.addActionListener(this);
		GridBagConstraints gbc_browseButton = new GridBagConstraints();
		gbc_browseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_browseButton.gridx = 5;
		gbc_browseButton.gridy = rowCount;
		dataPanel.add(browseButton, gbc_browseButton);

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

		btnSave = new JButton(MainActionCommands.CREATE_MOTRPAC_REPORT_FILES_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.CREATE_MOTRPAC_REPORT_FILES_COMMAND.getName());
		btnSave.addActionListener(this);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		allowDropFile = true;
		setDropTarget(new DropTarget() {
		    public synchronized void drop(DropTargetDropEvent evt) {
		    	
		    	if(!allowDropFile)
		    		return;
		    	
		        try {
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            List<File> droppedFiles = (List<File>)
		                evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		            if(droppedFiles == null || droppedFiles.isEmpty())
		            	return;
		            
		            File reportFolder = droppedFiles.get(0);
		            if(reportFolder.isDirectory()) {
			            reportFolderTextField.setText(reportFolder.getAbsolutePath());
			            baseDirectory = reportFolder;
			            savePreferences();
		            }
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		    }
		});		
		loadPreferences();
		initChooser();	
		studyComboBox.addItemListener(this);
		
		pack();
	}
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(baseDirectory);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			chooser.showOpenDialog(this);

		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
 
			baseDirectory = chooser.getSelectedFile();
			reportFolderTextField.setText(chooser.getSelectedFile().getAbsolutePath());
			savePreferences();
		}
		if(e.getActionCommand().equals(MainActionCommands.CREATE_MOTRPAC_REPORT_FILES_COMMAND.getName())) {
			generateEmtyReportFiles();
		}
	}
	
	public void generateEmtyReportFiles() {
		
		Collection<String>errors = validateReportData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		String reportFolder = getReportFolder().getAbsolutePath();
		Collection<String> fileNameParts = new ArrayList<String>();		
		fileNameParts.add(getLIMSExperiment());
		fileNameParts.add(getMotrpacStudy().getCode());
		fileNameParts.add(getMoTrPACTissueCode().getDescription());
		fileNameParts.add(getMoTrPACAssay().getCode());
		fileNameParts.add(getMoTrPACAssay().getPolarity());			
		for(MotrpacReportCodeSelectorPanel selector : codeSelectors)
			fileNameParts.add(selector.getSelectedMotracReportCode().getOptionCode());
		
		String fileName = StringUtils.join(fileNameParts, "_").trim().replaceAll("\\s+", "_") + ".txt";	
		for(String prefix : reportFileNames) {
			
			String prefixNamed = prefix + "_" + (String)namingComboBox.getSelectedItem();			
			Path reportPath = Paths.get(reportFolder, prefixNamed + "_" + fileName);
			try {
				Files.createFile(reportPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		fileNameParts.clear();		
		fileNameParts.add(getLIMSExperiment());
		fileNameParts.add(getMotrpacStudy().getCode());
		fileNameParts.add(getMoTrPACTissueCode().getDescription());
		fileNameParts.add(getMoTrPACAssay().getCode());
		fileNameParts.add(getMoTrPACAssay().getPolarity());
		fileName = StringUtils.join(fileNameParts, "_").trim().replaceAll("\\s+", "_") + ".txt";	
		Path missPath = Paths.get(getReportFolder().getParentFile().getAbsolutePath(), "metadata_failedsamples_" + fileName);
		if(!missPath.toFile().exists()) {
			try {
				Files.createFile(missPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {
			
			if(e.getItem() != null && e.getItem() instanceof MoTrPACStudy) {
				
				MoTrPACStudy study = (MoTrPACStudy)e.getItem();	
				Collection<MoTrPACTissueCode> tissueCodes = new TreeSet<MoTrPACTissueCode>();
				for(LIMSExperiment experiment : study.getExperiments())
					tissueCodes.addAll(study.getTissueCodesForExperiment(experiment));
				
				tissueComboBox.setModel(new SortedComboBoxModel<MoTrPACTissueCode>(tissueCodes));
				tissueComboBox.setSelectedIndex(-1);
				assayComboBox.setModel(new SortedComboBoxModel<MoTrPACAssay>(study.getAssays()));
				assayComboBox.setSelectedIndex(-1);		
			}
		}
	}
	
	public MoTrPACStudy getMotrpacStudy() {
		return (MoTrPACStudy)studyComboBox.getSelectedItem();
	}
	
	public MoTrPACAssay getMoTrPACAssay() {
		return (MoTrPACAssay)assayComboBox.getSelectedItem();
	}
	
	public MoTrPACTissueCode getMoTrPACTissueCode() {
		return (MoTrPACTissueCode)tissueComboBox.getSelectedItem();
	}
	
	public File getReportFolder() {
		
		String filePath = reportFolderTextField.getText().trim();
		if(filePath.isEmpty())
			return null;
		
		File reportFile = new File(reportFolderTextField.getText().trim());
		if(!reportFile.exists())
			return null;
		else
			return reportFile;
	}
	
	public Map<MoTrPACReportCodeBlock,MoTrPACReportCode>getReportStageDefinition(){
		
		Map<MoTrPACReportCodeBlock,MoTrPACReportCode>reportStageDefinition = 
				new TreeMap<MoTrPACReportCodeBlock,MoTrPACReportCode>();
		
		for(MotrpacReportCodeSelectorPanel selector : codeSelectors)
			reportStageDefinition.put(selector.getCodeBlock(), selector.getSelectedMotracReportCode());
		
		return reportStageDefinition;
	}
	
	public Collection<String>validateReportData(){
		
		Collection<String>errors = new ArrayList<String>();	
		
		if(getMotrpacStudy() == null)
			errors.add("Study not selected.");
		
		if(getLIMSExperiment() == null || getLIMSExperiment().isEmpty())
			errors.add("Experiment prefix not specified");
		
		if(getMoTrPACAssay() == null)
			errors.add("Assay not selected.");
		
		if(getMoTrPACTissueCode() == null)
			errors.add("Tissue not selected.");
		
		if(getReportFolder() == null)
			errors.add("Report file not selected or is not valid.");
			
		for(MotrpacReportCodeSelectorPanel selector : codeSelectors) {
			
			if(selector.getSelectedMotracReportCode() == null)
				errors.add("Value for section \"" + selector.getCodeBlock().getBlockId() + "\" not selected.");
		}		
		return errors;
	}

	private String getLIMSExperiment() {
		return expPrefixTextField.getText().trim();
	}


	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		loadPreferences();
	}

	@Override
	public void loadPreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	public void setAllowDropFile(boolean allowDropFile) {
		this.allowDropFile = allowDropFile;
	}
}
