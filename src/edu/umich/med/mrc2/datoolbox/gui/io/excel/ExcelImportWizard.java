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

package edu.umich.med.mrc2.datoolbox.gui.io.excel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.gui.expsetup.dpl.DataPipelineDefinitionPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;


public class ExcelImportWizard extends JDialog
	implements ActionListener, ItemListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -6725190979313397664L;
	private static final Icon excelIcon = GuiUtils.getIcon("excel", 32);
	private ExcelImportToolbar toolbar;
	private JButton btnImportData;
	private Workbook workbook;
	private WorksheetPreviewTable previewTable;
	private JTextField inputFileTextField;
	private File baseDirectory;
	private File inputFile;
	private JComboBox worksheetComboBox;
	private JComboBox directionComboBox;
	private SampleMatchingDialog sampleMatchingDialog;
	private DataPipelineDefinitionPanel dataPipelineDefinitionPanel;

	private Preferences preferences;
	private JComboBox unitsComboBox;
	public static final String PREFS_NODE = ExcelImportWizard.class.getName();
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	@SuppressWarnings("unchecked")
	public ExcelImportWizard() {

		super();
		setTitle("Import data from Excel file");
		setPreferredSize(new Dimension(1200, 800));
		setIconImage(((ImageIcon) excelIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(1200, 800));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		getContentPane().setLayout(new BorderLayout(0, 0));
		toolbar = new ExcelImportToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 78, 638, 125, 638, 638, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 44, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		buttonsPanel.setLayout(gridBagLayout);

		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		JLabel lblInputFile = new JLabel("Input file: ");
		GridBagConstraints gbc_lblInputFile = new GridBagConstraints();
		gbc_lblInputFile.anchor = GridBagConstraints.EAST;
		gbc_lblInputFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblInputFile.gridx = 0;
		gbc_lblInputFile.gridy = 0;
		buttonsPanel.add(lblInputFile, gbc_lblInputFile);

		inputFileTextField = new JTextField();
		inputFileTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 5;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		buttonsPanel.add(inputFileTextField, gbc_textField);
		inputFileTextField.setColumns(10);

		JLabel lblWorksheet = new JLabel("Worksheet: ");
		GridBagConstraints gbc_lblWorksheet = new GridBagConstraints();
		gbc_lblWorksheet.anchor = GridBagConstraints.EAST;
		gbc_lblWorksheet.insets = new Insets(0, 0, 5, 5);
		gbc_lblWorksheet.gridx = 0;
		gbc_lblWorksheet.gridy = 1;
		buttonsPanel.add(lblWorksheet, gbc_lblWorksheet);

		worksheetComboBox = new JComboBox();
		worksheetComboBox.setMinimumSize(new Dimension(200, 30));
		worksheetComboBox.setPreferredSize(new Dimension(200, 30));
		worksheetComboBox.setSize(new Dimension(200, 30));
		GridBagConstraints gbc_worksheetComboBox = new GridBagConstraints();
		gbc_worksheetComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_worksheetComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_worksheetComboBox.gridx = 1;
		gbc_worksheetComboBox.gridy = 1;
		buttonsPanel.add(worksheetComboBox, gbc_worksheetComboBox);

		JLabel lblDirection = new JLabel("Direction: ");
		GridBagConstraints gbc_lblDirection = new GridBagConstraints();
		gbc_lblDirection.anchor = GridBagConstraints.EAST;
		gbc_lblDirection.insets = new Insets(0, 0, 5, 5);
		gbc_lblDirection.gridx = 2;
		gbc_lblDirection.gridy = 1;
		buttonsPanel.add(lblDirection, gbc_lblDirection);

		directionComboBox = new JComboBox(new DefaultComboBoxModel<>(DataDirection.values()));
		directionComboBox.setMinimumSize(new Dimension(150, 30));
		directionComboBox.setMaximumSize(new Dimension(150, 30));
		directionComboBox.setPreferredSize(new Dimension(150, 30));
		directionComboBox.setSize(new Dimension(150, 30));
		directionComboBox.addItemListener(this);

		GridBagConstraints gbc_directionComboBox = new GridBagConstraints();
		gbc_directionComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_directionComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_directionComboBox.gridx = 3;
		gbc_directionComboBox.gridy = 1;
		buttonsPanel.add(directionComboBox, gbc_directionComboBox);

		JLabel lblMeasurementUnits = new JLabel("Measurement units: ");
		GridBagConstraints gbc_lblMeasurementUnits = new GridBagConstraints();
		gbc_lblMeasurementUnits.anchor = GridBagConstraints.EAST;
		gbc_lblMeasurementUnits.insets = new Insets(0, 0, 5, 5);
		gbc_lblMeasurementUnits.gridx = 4;
		gbc_lblMeasurementUnits.gridy = 1;
		buttonsPanel.add(lblMeasurementUnits, gbc_lblMeasurementUnits);

		unitsComboBox = new JComboBox<String>();
		GridBagConstraints gbc_unitsComboBox = new GridBagConstraints();
		gbc_unitsComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_unitsComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_unitsComboBox.gridx = 5;
		gbc_unitsComboBox.gridy = 1;
		buttonsPanel.add(unitsComboBox, gbc_unitsComboBox);

		dataPipelineDefinitionPanel = new DataPipelineDefinitionPanel();
		GridBagConstraints gbc_assayComboBox = new GridBagConstraints();
		gbc_assayComboBox.gridwidth = 6;
		gbc_assayComboBox.insets = new Insets(5, 5, 5, 5);
		gbc_assayComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_assayComboBox.gridx = 0;
		gbc_assayComboBox.gridy = 2;
		buttonsPanel.add(dataPipelineDefinitionPanel, gbc_assayComboBox);
		populateComboBoxes();

		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 4;
		gbc_btnCancel.gridy = 3;
		buttonsPanel.add(btnCancel, gbc_btnCancel);

		btnImportData = new JButton("Import data");
		btnImportData.setActionCommand(
				MainActionCommands.IMPORT_DATA_FROM_EXCEL_WORKSHEET_COMMAND.getName());
		btnImportData.addActionListener(this);
		GridBagConstraints gbc_btnImportData = new GridBagConstraints();
		gbc_btnImportData.gridx = 5;
		gbc_btnImportData.gridy = 3;
		buttonsPanel.add(btnImportData, gbc_btnImportData);

		previewTable = new WorksheetPreviewTable();
		JScrollPane tableScroll = new JScrollPane(previewTable);
		getContentPane().add(tableScroll, BorderLayout.CENTER);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(btnImportData);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		btnCancel.addActionListener(al);

		loadPreferences();
		pack();
	}

	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateComboBoxes() {

		Collection<String> units = new TreeSet<String>();
		try {
			units = LIMSUtils.getConcentrationUnits();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unitsComboBox.setModel(new SortedComboBoxModel(units));
		unitsComboBox.setSelectedIndex(-1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.LOAD_EXCEL_DATA_FOR_PREVIEW_COMMAND.getName()))
			selectExcelFile();

		if(command.equals(MainActionCommands.CLEAR_EXCEL_IMPORT_WIZARD_COMMAND.getName()))
			clearPanel();

		if(command.equals(MainActionCommands.MATCH_IMPORTED_TO_DESIGN_COMMAND.getName()))
			showSampleAssignmentDialog();

		if(command.equals(MainActionCommands.ACCEPT_EXCEL_SAMPLE_MATCH_COMMAND.getName()))
			acceptSampleAssignment();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void selectExcelFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Excel files", "xlsx", "XLSX");
		fc.setTitle("Select input Excel file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			inputFile = fc.getSelectedFile();
			if(inputFile.canRead()) {

				inputFileTextField.setText(inputFile.getPath());
				baseDirectory = inputFile.getParentFile();
				savePreferences();

				try {
					workbook =  WorkbookFactory.create(new FileInputStream(inputFile));
				} catch (EncryptedDocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(workbook == null)
					return;

				Collection<String>workshetNames = new ArrayList<String>();
				for (Sheet sheet : workbook)
					workshetNames.add(sheet.getSheetName());

				worksheetComboBox.removeItemListener(this);
				worksheetComboBox.setModel(new SortedComboBoxModel(workshetNames));
				worksheetComboBox.addItemListener(this);
				worksheetComboBox.setSelectedIndex(0);

				//	TODO
				//assayDesignPanel.reloadDesign();
				showSelectedDataSheet();
			}
		}
	}

	private void showSampleAssignmentDialog() {

		if(previewTable.getRowCount() == 0)
			return;

		Collection<String> errors = dataPipelineDefinitionPanel.validatePipelineDefinition();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}	
		DataPipeline pipeline =  dataPipelineDefinitionPanel.getDataPipeline();
		String[] sampleIds = previewTable.getReportDataByDataType(ReportDataType.SAMPLE_ID);
		String[] sampleNames = previewTable.getReportDataByDataType(ReportDataType.SAMPLE_NAME);
		String[] fileNames = previewTable.getReportDataByDataType(ReportDataType.DATA_FILE_NAME);
		//	String[] compoundNames = previewTable.getReportDataByDataType(ReportDataType.COMPOUND_NAME);

		sampleMatchingDialog = new SampleMatchingDialog(this);
		sampleMatchingDialog.setTableModelFromReportData(
				sampleIds, sampleNames, fileNames, pipeline.getAcquisitionMethod());
		sampleMatchingDialog.setLocationRelativeTo(this);
		sampleMatchingDialog.setVisible(true);
	}

	private void acceptSampleAssignment() {
		// TODO Auto-generated method stub



		sampleMatchingDialog.dispose();
	}

	@SuppressWarnings("unchecked")
	public synchronized void clearPanel() {

		previewTable.clearTable();
		inputFileTextField.setText("");
		workbook = null;
		worksheetComboBox.removeItemListener(this);
		worksheetComboBox.setModel(new SortedComboBoxModel());
		worksheetComboBox.addItemListener(this);
		revalidate();
		repaint();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED)
			showSelectedDataSheet();
	}

	private void showSelectedDataSheet() {

		if(workbook == null)
			return;

		if(worksheetComboBox.getSelectedItem() == null)
			return;

		String selectedName = (String) worksheetComboBox.getSelectedItem();
		for (Sheet sheet : workbook) {

			if(sheet.getSheetName().equals(selectedName))
				previewTable.setTableModelFromWorksheet(sheet, getDataDirection());
		}
	}

	public DataDirection getDataDirection() {
		return (DataDirection)directionComboBox.getSelectedItem();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =
			new File(preferences.get(BASE_DIRECTORY,
				MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory())).getAbsoluteFile();
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

	public String getConcentrationUnits() {
		return (String)unitsComboBox.getSelectedItem();
	}
}













