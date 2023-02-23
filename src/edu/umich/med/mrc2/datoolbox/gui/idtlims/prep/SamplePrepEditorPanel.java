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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.commons.io.FilenameUtils;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.AnnotationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.annotation.editors.DocumentAnnotationDialog;
import edu.umich.med.mrc2.datoolbox.gui.communication.SamplePrepEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.SamplePrepListener;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.user.UserSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RDPMetadataDefinitionStage;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class SamplePrepEditorPanel extends JPanel 
		implements ActionListener, PersistentLayout, BackedByPreferences, DateChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3179269432134132136L;

	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "SamplePrepEditorDialog.layout");
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.SamplePrepEditorPanel";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;	

	private LIMSSamplePreparation prep;
	private LIMSExperiment experiment;
	private LIMSUser prepUser;
	private JTextField nameTextField;
	private JLabel prepUserLabel;
	private JLabel idValueLabel;
	private JButton btnSelectUser;
	private DatePicker datePicker;
	private PrepSopSelectorDialog prepSopSelectorDialog;
	private UserSelectorDialog userSelectorDialog;
	private DockableSopPanel sopPanel;
	private DockableDocumentsPanel documentsPanel;
	private DockablePrepSampleTable prepSampleTable;
	private CControl control;
	private CGrid grid;
	
	private ExistingPrepSelectorDialog existingPrepSelectorDialog;
	private DocumentAnnotationDialog documentAnnotationDialog;
	private Set<SamplePrepListener> eventListeners;
	private boolean isWizardStep;
	private boolean limitEditor;
	private JButton editSaveNameButton;
	
	private static final Pattern prepIdPattern = 
			Pattern.compile(DataPrefix.SAMPLE_PREPARATION.name() +  "\\d4");
	
	/**
	 * This constructor is for the creation of the new sample preparation;
	 * @param experiment
	 */
	public SamplePrepEditorPanel(LIMSExperiment experiment, boolean limitEditor) {
		
		super(new BorderLayout(0, 0));
		this.experiment = experiment;
		if(experiment == null) {
			throw new IllegalArgumentException(
					"Experiment can not be null!");
		}	
		this.prep = null;
		isWizardStep = false;
		this.limitEditor = limitEditor;
		initGui();
		loadPrepData(null);		
	}
	
	/**
	 * This constructor is for the editing of the existing sample preparation;
	 * @param prep
	 */
	public SamplePrepEditorPanel(LIMSSamplePreparation prep, boolean limitEditor) {
		
		super(new BorderLayout(0, 0));
		this.prep = prep;
		this.limitEditor = limitEditor;
		if(prep == null) {
			throw new IllegalArgumentException(
					"SamplePrep can not be null!");
		}
		this.experiment = IDTDataCash.getExperimentForSamplePrep(prep);
		if(experiment == null) {
			throw new IllegalArgumentException(
					"Can not find experiment for selected sample prep!");
		}	
		initGui();
		loadPrepData(prep);
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public SamplePrepEditorPanel(boolean limitEditor) {
		super();
		this.limitEditor = limitEditor;
		setBorder(new EmptyBorder(10, 10, 10, 10));
		initGui();
	}

	private void initGui() {
	
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{244, 223, 176};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gbl_dataPanel);
		
		JLabel lblName = new JLabel("Name");
		lblName.setForeground(Color.BLACK);
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);
		
		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 1;
		gbc_lblId.gridy = 0;
		add(lblId, gbc_lblId);
		
		idValueLabel = new JLabel("");
		idValueLabel.setForeground(Color.BLACK);
		idValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.anchor = GridBagConstraints.WEST;
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_idValueLabel.gridx = 2;
		gbc_idValueLabel.gridy = 0;
		add(idValueLabel, gbc_idValueLabel);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 2;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 0;
		gbc_nameTextField.gridy = 1;
		add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		editSaveNameButton = new JButton("Edit");
		editSaveNameButton.setActionCommand(
				MainActionCommands.EDIT_SAMPLE_PREP_NAME_COMMAND.getName());
		editSaveNameButton.addActionListener(this);
		GridBagConstraints gbc_editSaveNameButton = new GridBagConstraints();
		gbc_editSaveNameButton.anchor = GridBagConstraints.WEST;
		gbc_editSaveNameButton.insets = new Insets(0, 0, 5, 0);
		gbc_editSaveNameButton.gridx = 2;
		gbc_editSaveNameButton.gridy = 1;
		add(editSaveNameButton, gbc_editSaveNameButton);
		
		JLabel lblType = new JLabel("Prepared by");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 2;
		add(lblType, gbc_lblType);

		prepUserLabel = new JLabel("      ");
		prepUserLabel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_prepUserLabel = new GridBagConstraints();
		gbc_prepUserLabel.insets = new Insets(0, 0, 5, 5);
		gbc_prepUserLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_prepUserLabel.gridx = 0;
		gbc_prepUserLabel.gridy = 3;
		add(prepUserLabel, gbc_prepUserLabel);
		
		btnSelectUser = new JButton("Select user");
		btnSelectUser.setActionCommand(MainActionCommands.SELECT_USER_DIALOG_COMMAND.getName());
		btnSelectUser.addActionListener(this);
		GridBagConstraints gbc_btnSelectUser = new GridBagConstraints();
		gbc_btnSelectUser.anchor = GridBagConstraints.WEST;
		gbc_btnSelectUser.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectUser.gridx = 1;
		gbc_btnSelectUser.gridy = 3;
		add(btnSelectUser, gbc_btnSelectUser);
		
		JLabel lblPreparedOn = new JLabel("Prepared on");
		GridBagConstraints gbc_lblPreparedOn = new GridBagConstraints();
		gbc_lblPreparedOn.anchor = GridBagConstraints.WEST;
		gbc_lblPreparedOn.insets = new Insets(0, 0, 5, 5);
		gbc_lblPreparedOn.gridx = 0;
		gbc_lblPreparedOn.gridy = 4;
		add(lblPreparedOn, gbc_lblPreparedOn);

		datePicker = new DatePicker();
		datePicker.addDateChangeListener(this);
		GridBagConstraints gbc_datePicker = new GridBagConstraints();
		gbc_datePicker.insets = new Insets(0, 0, 5, 5);
		gbc_datePicker.fill = GridBagConstraints.BOTH;
		gbc_datePicker.gridx = 0;
		gbc_datePicker.gridy = 5;
		add(datePicker, gbc_datePicker);

		JPanel panel_1 = new JPanel(new BorderLayout(0, 0));
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		documentsPanel = new DockableDocumentsPanel(this);
		sopPanel = new DockableSopPanel(this);		
		prepSampleTable =  new DockablePrepSampleTable();
		grid.add(0, 0, 100, 100, documentsPanel, sopPanel, prepSampleTable);

		control.getContentArea().deploy(grid);						
		panel_1.add(control.getContentArea(), BorderLayout.CENTER);
		
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.gridwidth = 3;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 6;
		add(panel_1, gbc_panel_1);		
		
		if(!limitEditor) {
			
			JButton selectPrepButton = new JButton(
					MainActionCommands.SELECT_SAMPLE_PREP_FROM_DATABASE_COMMAND.getName());
			selectPrepButton.setActionCommand(
					MainActionCommands.SELECT_SAMPLE_PREP_FROM_DATABASE_COMMAND.getName());
			selectPrepButton.addActionListener(this);
			selectPrepButton.setEnabled(false);
			GridBagConstraints gbc_selectPrepButton = new GridBagConstraints();
			gbc_selectPrepButton.fill = GridBagConstraints.HORIZONTAL;
			gbc_selectPrepButton.insets = new Insets(0, 0, 0, 5);
			gbc_selectPrepButton.gridx = 0;
			gbc_selectPrepButton.gridy = 7;
			add(selectPrepButton, gbc_selectPrepButton);
			
			JButton clearPanelButton = new JButton(
					MainActionCommands.CLEAR_SAMPLE_PREP_DEFINITION_COMMAND.getName());
			clearPanelButton.setActionCommand(
					MainActionCommands.CLEAR_SAMPLE_PREP_DEFINITION_COMMAND.getName());
			clearPanelButton.addActionListener(this);
			GridBagConstraints gbc_clearPanelButton = new GridBagConstraints();
			gbc_clearPanelButton.insets = new Insets(0, 0, 0, 5);
			gbc_clearPanelButton.fill = GridBagConstraints.HORIZONTAL;
			gbc_clearPanelButton.gridx = 1;
			gbc_clearPanelButton.gridy = 7;
			add(clearPanelButton, gbc_clearPanelButton);
		}
		loadLayout(layoutConfigFile);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.ADD_SOP_PROTOCOL_DIALOG_COMMAND.getName()))
			showAddSopDialog();

		if(command.equals(MainActionCommands.ADD_SOP_PROTOCOL_COMMAND.getName()))
			addSelectedSops();

		if(command.equals(MainActionCommands.DELETE_SOP_PROTOCOL_COMMAND.getName()))
			deleteSelectedSops();

		if(command.equals(MainActionCommands.SELECT_USER_DIALOG_COMMAND.getName()))
			showUserSelector();

		if(command.equals(MainActionCommands.SELECT_USER_COMMAND.getName()))
			setNewPrepUser();

		if(command.equals(MainActionCommands.ADD_DOCUMENT_DIALOG_COMMAND.getName()))
			showAddDocumentDialog();
		
		if(command.equals(MainActionCommands.SAVE_OBJECT_DOCUMENT_ANNOTATION_COMMAND.getName()))
			addDocument();

		if(command.equals(MainActionCommands.DELETE_DOCUMENT_COMMAND.getName()))
			deleteDocument();
		
		if(command.equals(MainActionCommands.SELECT_SAMPLE_PREP_FROM_DATABASE_COMMAND.getName()))
			selectExistingSamplePrep();
		
		if(command.equals(MainActionCommands.LOAD_SAMPLE_PREP_FROM_DATABASE_COMMAND.getName()))
			loadSelectedSamplePrep();
		
		if(command.equals(MainActionCommands.CLEAR_SAMPLE_PREP_DEFINITION_COMMAND.getName()))
			clearPanelWithWarning();
		
		if(command.equals(MainActionCommands.EDIT_SAMPLE_PREP_NAME_COMMAND.getName())) {
			nameTextField.setEditable(true);
			editSaveNameButton.setText("Save");
			editSaveNameButton.setActionCommand(
					MainActionCommands.SAVE_SAMPLE_PREP_NAME_COMMAND.getName());
		}
		if(command.equals(MainActionCommands.SAVE_SAMPLE_PREP_NAME_COMMAND.getName()))
			savePrepName();
	}
	
	private void savePrepName() {

		nameTextField.setEditable(false);
		editSaveNameButton.setText("Edit");
		editSaveNameButton.setActionCommand(
				MainActionCommands.EDIT_SAMPLE_PREP_NAME_COMMAND.getName());
		
		if(prep != null) {
			
			prep.setName(getPrepName());
			
			if(prep.getId() != null && prepIdPattern.matcher(prep.getId()).find()) {

				try {
					IDTUtils.updateBasicSamplePrepData(prep);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fireSamplePrepEvent(prep, ParameterSetStatus.CHANGED);
			}
		}
	}

	@Override
	public void setVisible(boolean b) {
		
		if(b)
			control.getController().setFocusedDockable(prepSampleTable.intern(), true);
		
		super.setVisible(b);
	}
	
	public void loadPrepData(LIMSSamplePreparation samplePrep) {
		
		datePicker.removeDateChangeListener(this);
		this.prep = samplePrep;
		if(prep == null) {
			
			if(experiment != null)
				prepSampleTable.setTableModelFromSamples(
						experiment.getExperimentDesign().getSamples());
		}
		else {
			idValueLabel.setText(prep.getId());
			nameTextField.setText(prep.getName());
			prepUserLabel.setText(prep.getCreator().getInfo());
			prepUser = prep.getCreator();
			if (prep.getPrepDate() != null) {
				LocalDate localDate = prep.getPrepDate().
						toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				datePicker.setDate(localDate);
			}
			//	Load samples for prep
			try {
				prepSampleTable.setTableModelFromSamples(IDTUtils.getSamplesForPrep(prep));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//	Load SOPs
			try {
				sopPanel.setTableModelFromProtocols(IDTUtils.getSamplePrepSops(prep));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//	Load annotations
			try {
				Collection<ObjectAnnotation> prepAnnotations = 
						AnnotationUtils.getObjetAnnotations(AnnotatedObjectType.SAMPLE_PREP, prep.getId());
				if(!prepAnnotations.isEmpty())
					prep.getAnnotations().addAll(prep.getAnnotations());
					
				documentsPanel.setModelFromAnnotations(prep.getAnnotations());
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fireSamplePrepEvent(prep, ParameterSetStatus.ADDED);
		}
		datePicker.addDateChangeListener(this);
		updateNameEditorFromPrep();
	}
	
	private void updateNameEditorFromPrep() {

		if(prep == null || prep.getId() == null) {
			nameTextField.setEditable(true);
			editSaveNameButton.setEnabled(false);
		}
		else {
			nameTextField.setEditable(false);
			editSaveNameButton.setEnabled(true);
		}
	}

	public void loadPrepDataForExperiment(
			LIMSSamplePreparation samplePrep, 
			LIMSExperiment prepExperiment) {
		
		datePicker.removeDateChangeListener(this);
		this.prep = samplePrep;
		this.experiment = prepExperiment;
		prepSampleTable.setTableModelFromSamples(
				experiment.getExperimentDesign().getSamples());
		
		if(prep != null) {
			
			idValueLabel.setText(prep.getId());
			nameTextField.setText(prep.getName());
			prepUser = prep.getCreator();
			if(prepUser != null)
				prepUserLabel.setText(prep.getCreator().getInfo());
			
			if (prep.getPrepDate() != null) {
				LocalDate localDate = 
						prep.getPrepDate().toInstant().
							atZone(ZoneId.systemDefault()).toLocalDate();
				datePicker.setDate(localDate);
			}
			sopPanel.setTableModelFromProtocols(prep.getProtocols());
			documentsPanel.setModelFromAnnotations(prep.getAnnotations());
		}
		updateNameEditorFromPrep();
		datePicker.addDateChangeListener(this);
	}

	private void selectExistingSamplePrep() {
		
		if(isWizardStep) {
			
			if(experiment == null) {
				MessageDialog.showErrorMsg(
						"Please complete the experiment definition step first.", this);
				return;
			}
			if(experiment != null && experiment.getId() != null) {
				MessageDialog.showErrorMsg(
						"The parent experiment is already in the database\n "
						+ "and it's design can't be altered through this wizard.", this);
				return;
			}
		}
		int res = MessageDialog.showChoiceWithWarningMsg(
				"This operation will automatically load experiment design "
				+ "associated with the selected sample preparation.\nProceed?",
				this);
		if(res != JOptionPane.YES_OPTION)
			return;
		
		existingPrepSelectorDialog = new ExistingPrepSelectorDialog(this);
		existingPrepSelectorDialog.setLocationRelativeTo(this);
		existingPrepSelectorDialog.setVisible(true);
	}
	
	private void loadSelectedSamplePrep() {
		
		LIMSSamplePreparation selectedPrep = 
				existingPrepSelectorDialog.getSelectedPrep();
		if(selectedPrep == null)
			return;
		
		loadPrepData(selectedPrep);
		existingPrepSelectorDialog.dispose();
	}
	
	private void clearPanelWithWarning() {
		
		if(prep != null) {
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to reset the sample prep definition?\n"
					+ "This will also clear the experiment design "
					+ "associated with the sample preparation.", this);
			if(res != JOptionPane.YES_OPTION)
				return;
			else {
				clearGui();
				fireSamplePrepEvent(prep, ParameterSetStatus.REMOVED);
				prep = null;
			}
		}
		else {
			clearGui();
		}
	}
	
	public void clearPanel() {
		clearGui();
		prep = null;
	}
	
	private void clearGui() {
		
		datePicker.removeDateChangeListener(this);
		prepSampleTable.clearTable();;
		idValueLabel.setText("");
		nameTextField.setText("");
		prepUserLabel.setText("    ");
		prepUser = null;
		LocalDate localDate = 
				new Date().toInstant().
					atZone(ZoneId.systemDefault()).toLocalDate();
		datePicker.setDate(localDate);		
		sopPanel.clearPanel();
		documentsPanel.clearPanel();
		datePicker.addDateChangeListener(this);
	}

	private void setNewPrepUser() {

		if(userSelectorDialog.getSelectedUser() == null)
			return;

		prepUser = userSelectorDialog.getSelectedUser();
		prepUserLabel.setText(prepUser.getInfo());		
		if(prep != null) {
			
			prep.setCreator(prepUser);
			
			if(prep.getId() != null && prepIdPattern.matcher(prep.getId()).find()) {

				try {
					IDTUtils.updateBasicSamplePrepData(prep);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fireSamplePrepEvent(prep, ParameterSetStatus.CHANGED);
			}
		}
		userSelectorDialog.dispose();
	}

	private void showUserSelector() {

		userSelectorDialog = new UserSelectorDialog(this);
		userSelectorDialog.setTitle("Select user in charge of sample preparation");
		userSelectorDialog.setLocationRelativeTo(this);
		userSelectorDialog.setVisible(true);
	}

	private void addSelectedSops() {

		Collection<LIMSProtocol> selected = 
				prepSopSelectorDialog.getSelectedProtocols();
		if(selected == null || selected.isEmpty())
			return;

		Collection<LIMSProtocol>protocols = new TreeSet<LIMSProtocol>();
		if(prep != null) {
			
			protocols.addAll(prep.getProtocols());
			
			if(prep.getId() != null && prepIdPattern.matcher(prep.getId()).find()) {
				
				prep.getProtocols().addAll(selected);
				try {
					IDTUtils.updateSamplePrepProtocols(prep);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fireSamplePrepEvent(prep, ParameterSetStatus.CHANGED);
			}
		}
		protocols.addAll(selected);
		sopPanel.setTableModelFromProtocols(protocols);
		prepSopSelectorDialog.dispose();
	}

	private void deleteSelectedSops() {

		Collection<LIMSProtocol>selected = sopPanel.getSelectedProtocols();
		if(selected == null || selected.isEmpty())
			return;

		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to remove selected SOPs from sample prep?", this);
		if(res == JOptionPane.YES_OPTION) {
			
			Collection<LIMSProtocol>protocols = new TreeSet<LIMSProtocol>();
			if(prep != null) {
				
				protocols.addAll(prep.getProtocols());
				
				if(prep.getId() != null && prepIdPattern.matcher(prep.getId()).find()) {
					
					prep.getProtocols().removeAll(selected);
					try {
						IDTUtils.updateSamplePrepProtocols(prep);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fireSamplePrepEvent(prep, ParameterSetStatus.CHANGED);
				}
			}
			protocols.removeAll(selected);
			sopPanel.setTableModelFromProtocols(protocols);
		}
	}

	private void showAddSopDialog() {

		prepSopSelectorDialog = new PrepSopSelectorDialog(this);
		prepSopSelectorDialog.setLocationRelativeTo(this);
		prepSopSelectorDialog.setVisible(true);
	}
	
	private void showAddDocumentDialog() {;
		
		LIMSSamplePreparation annotated = prep;
		if(prep == null) {
			annotated =  new LIMSSamplePreparation(
				null, 
				getPrepName(), 
				new Date(), 
				MRC2ToolBoxCore.getIdTrackerUser());
		}
		ObjectAnnotation prepAnnotation = new ObjectAnnotation(annotated,
			MRC2ToolBoxCore.getIdTrackerUser());	
		documentAnnotationDialog = new DocumentAnnotationDialog(this);
		documentAnnotationDialog.loadAnnotation(prepAnnotation);
		documentAnnotationDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		documentAnnotationDialog.setSaveButtonText("Add document");
		documentAnnotationDialog.setVisible(true);
	}

	private void addDocument() {
			
		ObjectAnnotation newAnnotation = 
				documentAnnotationDialog.getAnnotation();
		if(newAnnotation == null)
			return;
		
		if(documentAnnotationDialog.getDocumentTitle().isEmpty()) {
			MessageDialog.showErrorMsg(
					"Please specify document title.", documentAnnotationDialog);
			return;
		}
		if(documentAnnotationDialog.getDocumentSourceFile() == null) {
			MessageDialog.showErrorMsg(
					"Please specify document source file.", 
					documentAnnotationDialog);
			return;
		}
		newAnnotation.setLinkedDocumentName(documentAnnotationDialog.getDocumentTitle());
		File docFile = documentAnnotationDialog.getDocumentSourceFile();
		newAnnotation.setLinkedDocumentFile(docFile);		
		DocumentFormat docFormat = DocumentFormat.getFormatByFileExtension(
				FilenameUtils.getExtension(docFile.getName()).toUpperCase());
		newAnnotation.setLinkedDocumentFormat(docFormat);
		
		Collection<ObjectAnnotation> annotations = new ArrayList<ObjectAnnotation>();
		if(prep != null) {
			
			annotations.addAll(prep.getAnnotations());
			
			if(prep.getId() != null && prepIdPattern.matcher(prep.getId()).find()) {
				
				prep.getAnnotations().add(newAnnotation);
				try {
					AnnotationUtils.insertNewAnnotation(newAnnotation);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fireSamplePrepEvent(prep, ParameterSetStatus.CHANGED);
			}
		}
		annotations.add(newAnnotation);
		documentsPanel.setModelFromAnnotations(annotations);			
		documentAnnotationDialog.dispose();	
	}
	
	private void deleteDocument() {

		Collection<ObjectAnnotation>selected = documentsPanel.getSelectedAnnotations();
		if(selected == null || selected.isEmpty())
			return;

		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to remove selected annotation documents from sample prep?", this);
		if(res == JOptionPane.YES_OPTION) {
			
			Collection<ObjectAnnotation>annotations = new ArrayList<ObjectAnnotation>();
			if(prep != null) {
				
				annotations.addAll(prep.getAnnotations());
				
				if(prep.getId() != null && prepIdPattern.matcher(prep.getId()).find()) {
					
					prep.getAnnotations().removeAll(selected);
					try {
						AnnotationUtils.updateObjectAnnotationsList(prep);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fireSamplePrepEvent(prep, ParameterSetStatus.CHANGED);
				}
			}
			annotations.removeAll(selected);
			documentsPanel.setModelFromAnnotations(annotations);
		}
	}

	public LIMSSamplePreparation getSamplePrep() {
		return prep;
	}

	public LIMSExperiment getExperiment() {
		return experiment;
	}
	
	public void setExperiment(LIMSExperiment experiment) {
		this.experiment = experiment;
	}
	
	public String getPrepName() {
		return nameTextField.getText().trim();
	}

	public Date getPrepDate() {

		if(datePicker.getDate() == null)
			return null;

		return Date.from(datePicker.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * @return the prepUser
	 */
	public LIMSUser getPrepUser() {
		return prepUser;
	}

	public Collection<LIMSProtocol> getPrepSops(){
		return sopPanel.getAllProtocols();
	}

	public Collection<ObjectAnnotation> getPrepAnnotations(){
		return documentsPanel.getAllAnnotations();
	}

	public Collection<IDTExperimentalSample>getSelectedSamples(){
		return prepSampleTable.getSelectedSamples();
	}
	
	public Collection<String>vaidateSamplePrepData() {
		
		Collection<String>errors = new ArrayList<String>();
		String prepName = getPrepName();
		LIMSUser prepUser = getPrepUser();
		Date prepDate = getPrepDate();
		Collection<IDTExperimentalSample>selectedSamples = getSelectedSamples();
		Collection<LIMSProtocol>prepSops = getPrepSops();
		Collection<ObjectAnnotation>prepAnnotations = getPrepAnnotations();
		
		if(prepName.isEmpty())
			errors.add("Preparation name can not be empty.");

		if(prepDate == null)
			errors.add("Preparation date should be specified.");

		if(prepSops.isEmpty())
			errors.add("Preparation SOP(s) should be specified.");

		if(selectedSamples.isEmpty())
			errors.add("Preparation doesn't include any samples.");

		if(prepUser == null)
			errors.add("User in charge of preparation should be specified.");
		
		return errors;
	}
	
	public void addSamplePrepListener(SamplePrepListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.add(listener);
	}

	public void removeSamplePrepListener(SamplePrepListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.remove(listener);
	}
	
	public void fireSamplePrepEvent(
			LIMSSamplePreparation prep, 
			ParameterSetStatus status) {

		if(eventListeners == null){
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		SamplePrepEvent event = new SamplePrepEvent(
				prep, status, RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA);
		eventListeners.stream().forEach(l -> l.samplePrepStatusChanged(event));		
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  new File(preferences.get(
				BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
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

			for(int i=0; i<control.getCDockableCount(); i++) {

				CDockable uiObject = control.getCDockable(i);
				if(uiObject instanceof PersistentLayout)
					((PersistentLayout)uiObject).saveLayout(((PersistentLayout)uiObject).getLayoutFile());
			}
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

	public boolean isWizardStep() {
		return isWizardStep;
	}

	public void setWizardStep(boolean isWizardStep) {
		this.isWizardStep = isWizardStep;
	}
	
	public void setPrepEditable(boolean b) {

		sopPanel.setEditable(b);
		documentsPanel.setEditable(b);		
		nameTextField.setEditable(b);
		btnSelectUser.setEnabled(b);
		datePicker.setEnabled(b);
	}

	@Override
	public void dateChanged(DateChangeEvent event) {

		if(prep != null) {
			
			prep.setPrepDate(getPrepDate());
			
			if(prep.getId() != null && prepIdPattern.matcher(prep.getId()).find()) {

				try {
					IDTUtils.updateBasicSamplePrepData(prep);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fireSamplePrepEvent(prep, ParameterSetStatus.CHANGED);
			}
		}
	}
}
