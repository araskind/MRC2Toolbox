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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

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

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReport;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCodeBlock;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MotrpacReportUploadDialog extends JDialog implements ActionListener, BackedByPreferences, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5201210444410166905L;
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.gui.mptrack.report.MotrpacReportUploadDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final Icon addProtocolIcon = GuiUtils.getIcon("addSop", 32);
	private static final Icon editProtocolIcon = GuiUtils.getIcon("editSop", 32);
	private static final String BROWSE_COMMAND = "BROWSE_COMMAND";

	private JComboBox studyComboBox;
	private JComboBox experimentComboBox;
	private JComboBox assayComboBox;
	private JComboBox tissueComboBox;
	private JButton btnSave;
	private JTextField reportFileTextField;
	private JLabel authorInfoLabel;
	private JLabel dateCreatedValueLabel;
	private JButton browseButton;
	private File baseDirectory;
	private ImprovedFileChooser chooser;
	private boolean allowDropFile;

	private Collection<MotrpacReportCodeSelectorPanel>codeSelectors;

	@SuppressWarnings("unchecked")
	public MotrpacReportUploadDialog(ActionListener actionListener) {
		super();

		setTitle("Upload new MoTrPAC data analysis report");
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
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 0.0, 1.0};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
		
		JLabel lblNewLabel = new JLabel("Experiment");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = rowCount;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		experimentComboBox = new JComboBox();
		GridBagConstraints gbc_experimentComboBox = new GridBagConstraints();
		gbc_experimentComboBox.gridwidth = 5;
		gbc_experimentComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_experimentComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_experimentComboBox.gridx = 1;
		gbc_experimentComboBox.gridy = rowCount;
		dataPanel.add(experimentComboBox, gbc_experimentComboBox);
		
		rowCount++;
		
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
		
		reportFileTextField = new JTextField();
		reportFileTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 6;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = rowCount;
		dataPanel.add(reportFileTextField, gbc_textField);
		
		rowCount++;
		
		browseButton = new JButton("Select report file");
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

		btnSave = new JButton(MainActionCommands.UPLOAD_MOTRPAC_REPORT_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.UPLOAD_MOTRPAC_REPORT_COMMAND.getName());
		btnSave.addActionListener(actionListener);
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
		            
		            File reportFile = droppedFiles.get(0);
		            reportFileTextField.setText(reportFile.getAbsolutePath());
		            baseDirectory = reportFile.getParentFile();
		            savePreferences();
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		    }
		});
		
		loadPreferences();
		initChooser();
		
		studyComboBox.addItemListener(this);
		experimentComboBox.addItemListener(this);
		
		pack();
	}
	
	public void loadReportData(MoTrPACReport report) {
		
		if(report == null)
			return;
		
		setTitle("Edit report metadata for \"" + report.getLinkedDocumentName() + "\"");
		setIconImage(((ImageIcon) editProtocolIcon).getImage());
		studyComboBox.setSelectedItem(report.getStudy());
		experimentComboBox.setSelectedItem(report.getExperiment());
		assayComboBox.setSelectedItem(report.getAssay());
		tissueComboBox.setSelectedItem(report.getTissueCode());
		reportFileTextField.setText(report.getLinkedDocumentName());
		authorInfoLabel.setText(report.getCreateBy().getInfo());
		dateCreatedValueLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(report.getDateCreated()));
		allowDropFile = false;
		browseButton.setEnabled(false);
		for(Entry<MoTrPACReportCodeBlock, MoTrPACReportCode> stage : report.getReportStage().entrySet()) {
			
			for(MotrpacReportCodeSelectorPanel selector : codeSelectors) {
				
				if(selector.getCodeBlock().equals(stage.getKey()))
					selector.setMoTrPACReportCode(stage.getValue());
			}
		}
		btnSave.setText(MainActionCommands.SAVE_MOTRPAC_REPORT_METADATA_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SAVE_MOTRPAC_REPORT_METADATA_COMMAND.getName());		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {
			
			if(e.getItem() != null && e.getItem() instanceof MoTrPACStudy) {
				
				MoTrPACStudy study = (MoTrPACStudy)e.getItem();				
				assayComboBox.setModel(new SortedComboBoxModel<MoTrPACAssay>(study.getAssays()));
				assayComboBox.setSelectedIndex(-1);				
				experimentComboBox.setModel(new SortedComboBoxModel<MoTrPACAssay>(study.getExperiments()));
				LIMSExperiment experiment = getLIMSExperiment();
				if(experiment != null) {
					Collection<MoTrPACTissueCode> tissueCodes = study.getTissueCodesForExperiment(experiment);
					if(tissueCodes == null)
						tissueCodes = new TreeSet<MoTrPACTissueCode>();
					
					tissueComboBox.setModel(new SortedComboBoxModel<MoTrPACTissueCode>(tissueCodes));
					tissueComboBox.setSelectedIndex(-1);
				}
			}
			if(e.getItem() != null && e.getItem() instanceof LIMSExperiment) {
				
				MoTrPACStudy study = (MoTrPACStudy)studyComboBox.getSelectedItem();
				if(study == null)
					return;
				
				LIMSExperiment experiment = (LIMSExperiment)e.getItem();
				Collection<MoTrPACTissueCode> tissueCodes = study.getTissueCodesForExperiment(experiment);
				if(tissueCodes == null)
					tissueCodes = new TreeSet<MoTrPACTissueCode>();
				
				tissueComboBox.setModel(new SortedComboBoxModel<MoTrPACTissueCode>(tissueCodes));
				tissueComboBox.setSelectedIndex(-1);
			}
		}
	}
	
	public MoTrPACStudy getMotrpacStudy() {
		return (MoTrPACStudy)studyComboBox.getSelectedItem();
	}
	
	public LIMSExperiment getLIMSExperiment() {
		return (LIMSExperiment)experimentComboBox.getSelectedItem();
	}
	
	public MoTrPACAssay getMoTrPACAssay() {
		return (MoTrPACAssay)assayComboBox.getSelectedItem();
	}
	
	public MoTrPACTissueCode getMoTrPACTissueCode() {
		return (MoTrPACTissueCode)tissueComboBox.getSelectedItem();
	}
	
	public File getReportFile() {
		
		String filePath = reportFileTextField.getText().trim();
		if(filePath.isEmpty())
			return null;
		
		File reportFile = new File(reportFileTextField.getText().trim());
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
		
		if(getLIMSExperiment() == null)
			errors.add("Experiment not selected.");
		
		if(getMoTrPACAssay() == null)
			errors.add("Assay not selected.");
		
		if(getMoTrPACTissueCode() == null)
			errors.add("Tissue not selected.");
		
		if(getReportFile() == null && btnSave.getActionCommand().equals(
				MainActionCommands.UPLOAD_MOTRPAC_REPORT_COMMAND.getName()))
			errors.add("Report file not selected.");
			
		for(MotrpacReportCodeSelectorPanel selector : codeSelectors) {
			
			if(selector.getSelectedMotracReportCode() == null)
				errors.add("Value for section \"" + selector.getCodeBlock().getBlockId() + "\" not selected.");
		}		
		return errors;
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(baseDirectory);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			chooser.showOpenDialog(this);

		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {

			File inputFile = chooser.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			reportFileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
			savePreferences();
		}
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
