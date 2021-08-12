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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableExcelWorksheetPreviewTable extends DefaultSingleCDockable implements ActionListener, ItemListener {

	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);
	private static final String BROWSE_FOR_INPUT = "BROWSE_FOR_INPUT";

	private WorksheetPreviewTable previewTable;
	private ExcelWorksheetPreviewToolbar toolbar;
	private JTextField inputFileTextField;
	private JFileChooser chooser;
	private File baseDirectory;
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

		initChooser();
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
		chooser.setCurrentDirectory(baseDirectory);
		chooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xls", "xlsx"));
		chooser.addActionListener(this);
	}

	public void setTableModelFromWorksheet(Sheet sheet, DataDirection direction) {
		previewTable.setTableModelFromWorksheet(sheet, direction);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if(command.equals(BROWSE_FOR_INPUT))
			chooser.showOpenDialog(this.getContentPane());

		if (e.getSource().equals(chooser) && command.equals(JFileChooser.APPROVE_SELECTION)) {
			try {
				importExcelFile();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

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

	private void importExcelFile() throws Exception {

		inputFile = chooser.getSelectedFile();
		if(inputFile.canRead()) {

			inputFileTextField.setText(inputFile.getPath());
			baseDirectory = inputFile.getParentFile();
			chooser.setCurrentDirectory(baseDirectory);

			InputStream is = new FileInputStream(inputFile);
			workbook =  WorkbookFactory.create(is);

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
}
