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

package edu.umich.med.mrc2.datoolbox.gui.io.excel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableExcelWorksheetPreviewTable extends DefaultSingleCDockable 
		implements ActionListener, ItemListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);
	private static final String BROWSE_FOR_INPUT = "BROWSE_FOR_INPUT";
	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;

	private WorksheetPreviewTable previewTable;
	private ExcelWorksheetPreviewToolbar toolbar;
	private JTextField inputFileTextField;
	private File inputFile;
	private JComboBox worksheetComboBox;
	private Workbook workbook;

	public DockableExcelWorksheetPreviewTable() {

		super("DockableExcelWorksheetPreviewTable", componentIcon, "Excel worksheet preview", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new ExcelWorksheetPreviewToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		previewTable = new WorksheetPreviewTable();
		JScrollPane designScrollPane = new JScrollPane(previewTable);
		add(designScrollPane, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 78, 638, 638, 638, 638, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		buttonsPanel.setLayout(gridBagLayout);

		add(buttonsPanel, BorderLayout.SOUTH);

		inputFile = null;

		JLabel lblInputFile = new JLabel("Input file");
		GridBagConstraints gbc_lblInputFile = new GridBagConstraints();
		gbc_lblInputFile.anchor = GridBagConstraints.EAST;
		gbc_lblInputFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblInputFile.gridx = 0;
		gbc_lblInputFile.gridy = 0;
		buttonsPanel.add(lblInputFile, gbc_lblInputFile);

		inputFileTextField = new JTextField();
		inputFileTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 4;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		buttonsPanel.add(inputFileTextField, gbc_textField);
		inputFileTextField.setColumns(10);

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setActionCommand(BROWSE_FOR_INPUT);
		btnBrowse.addActionListener(this);
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowse.gridx = 5;
		gbc_btnBrowse.gridy = 0;
		buttonsPanel.add(btnBrowse, gbc_btnBrowse);

		JLabel lblPage = new JLabel("Page");
		GridBagConstraints gbc_lblPage = new GridBagConstraints();
		gbc_lblPage.anchor = GridBagConstraints.EAST;
		gbc_lblPage.insets = new Insets(0, 0, 0, 5);
		gbc_lblPage.gridx = 0;
		gbc_lblPage.gridy = 1;
		buttonsPanel.add(lblPage, gbc_lblPage);

		worksheetComboBox = new JComboBox();
		worksheetComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 3;
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
		buttonsPanel.add(worksheetComboBox, gbc_comboBox);

		loadPreferences();
	}

	public void setTableModelFromWorksheet(Sheet sheet, DataDirection direction) {
		previewTable.setTableModelFromWorksheet(sheet, direction);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if(command.equals(BROWSE_FOR_INPUT))
			selectExcelFile();

		if(command.equals(MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName())) {

		}
		if(command.equals(MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName())) {

		}
		if(command.equals(MainActionCommands.ENABLE_ALL_SAMPLES_COMMAND.getName())) {

		}
		if(command.equals(MainActionCommands.DISABLE_ALL_SAMPLES_COMMAND.getName())) {

		}
		if(command.equals(MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName())) {

		}
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

			if(sheet.getSheetName().equals(selectedName)) {
				DataDirection direction = toolbar.getDataDirection();
				previewTable.setTableModelFromWorksheet(sheet, direction);
			}
		}
	}

	public DataDirection getDataDirection() {
		return toolbar.getDataDirection();
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {		
		preferences = prefs;		
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, 
				MRC2ToolBoxConfiguration.getDefaultDataDirectory()));		
	}

	@Override
	public void loadPreferences() {
		loadPreferences(
				Preferences.userRoot().node(DockableExcelWorksheetPreviewTable.class.getName()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(DockableExcelWorksheetPreviewTable.class.getName());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}
