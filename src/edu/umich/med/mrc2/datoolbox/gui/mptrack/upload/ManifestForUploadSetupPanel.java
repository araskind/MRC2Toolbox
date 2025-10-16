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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.upload;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.github.lgooddatepicker.components.DatePicker;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp.CreateUploadManifestTask;

public class ManifestForUploadSetupPanel extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2020736082862742526L;
	
	private MoTrPACAssay assay;
	private File baseDirectory;
	private File batchDir;
	JDialog parent;

	private DatePicker datePicker;
	private JTextField batchPathTextField;
	private JLabel lblNewLabel;
	
	public ManifestForUploadSetupPanel(MoTrPACAssay assay, JDialog parent) {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		this.assay = assay;
		this.parent = parent;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JButton selectDestinationButton = new JButton(
				MainActionCommands.SELECT_MOTRPAC_UPLOAD_BATCH_FOR_MANIFEST_FILE_COMMAND.getName());
		selectDestinationButton.setActionCommand(
				MainActionCommands.SELECT_MOTRPAC_UPLOAD_BATCH_FOR_MANIFEST_FILE_COMMAND.getName());
		selectDestinationButton.addActionListener(this);
		
		batchPathTextField = new JTextField();
		batchPathTextField.setEditable(false);
		GridBagConstraints gbc_batchPathTextField = new GridBagConstraints();
		gbc_batchPathTextField.insets = new Insets(0, 0, 5, 5);
		gbc_batchPathTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_batchPathTextField.gridx = 0;
		gbc_batchPathTextField.gridy = 0;
		add(batchPathTextField, gbc_batchPathTextField);
		batchPathTextField.setColumns(10);
		
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.gridx = 1;
		gbc_btnNewButton_1.gridy = 0;
		add(selectDestinationButton, gbc_btnNewButton_1);
		
		datePicker = new DatePicker();
		LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		datePicker.setDate(localDate);
		
		lblNewLabel = new JLabel("Manifest date");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);
		
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 1;
		add(datePicker, gbc_lblNewLabel_1);
		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultDataDirectory());
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();	
		
		if(command.equals(MainActionCommands.SELECT_MOTRPAC_UPLOAD_BATCH_FOR_MANIFEST_FILE_COMMAND.getName()))
			selectBatchDirectory();
	}
	
	private void selectBatchDirectory() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select destination directory for compressed files");
		fc.setMultiSelectionEnabled(false);
		fc.setOpenButtonText("Select BATCH directory");
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(parent))) {
				
			File selected = fc.getSelectedFile();
			String fn = selected.getName();
			if(!selected.getName().startsWith("BATCH")) {
				
				MessageDialog.showErrorMsg(
						"Invalid batch folder, name must start with \"BATCH\"", parent);
				return;
			}
			batchDir = selected;
			baseDirectory = batchDir.getParentFile();
			batchPathTextField.setText(batchDir.getAbsolutePath());
		}
	}
	
	public MoTrPACAssay getAssay() {
		return assay;
	}
	
	public File getBatchDir() {
		return batchDir;
	}
	
	public Date getSelectedDate() {
		
		if(datePicker.getDate() == null)
			return null;

		return Date.from(datePicker.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	public CreateUploadManifestTask getManifestTask() {
		
		if(batchDir == null || getSelectedDate() == null)
			return null;
		
		CreateUploadManifestTask task = 
				new CreateUploadManifestTask(
				batchDir, 
				getSelectedDate(),
				assay.getDescription());
		
		return task;
	}	
}
