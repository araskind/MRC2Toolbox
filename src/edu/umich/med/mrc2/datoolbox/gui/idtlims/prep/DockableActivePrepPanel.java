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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import com.github.lgooddatepicker.components.DatePicker;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.AnnotationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.user.UserSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableActivePrepPanel extends DefaultSingleCDockable
	implements ActionListener, PersistentLayout, BackedByPreferences{

	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "DockableActivePrepPanel.layout");
	private static final Icon editPrepIcon = GuiUtils.getIcon("editSamplePrep", 16);
	private LIMSSamplePreparation prep;
	private LIMSUser prepUser;

	private JTextField nameTextField;
	private JButton btnSave;
	private JLabel idValueLabel;
	private JLabel prepUserLabel;
	private JButton btnSelectUser;
	private DatePicker datePicker;
	private PrepSopSelectorDialog prepSopSelectorDialog;
	private UserSelectorDialog userSelectorDialog;
	private DockableSopPanel sopPanel;
	private DockableDocumentsPanel documentsPanel;
	private DockablePrepSampleTable prepSampleTable;
	private CControl control;
	private CGrid grid;

	public DockableActivePrepPanel(ActionListener actionListener) {

		super("DockableActivePrepPanel", editPrepIcon, "Active sample preparation", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 407, 407, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		dataPanel.add(lblId, gbc_lblId);

		idValueLabel = new JLabel("");
		idValueLabel.setForeground(Color.BLACK);
		idValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.gridwidth = 2;
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idValueLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel lblName = new JLabel("Name");
		lblName.setForeground(Color.BLACK);
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 3;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 1;
		dataPanel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);

		JLabel lblType = new JLabel("Prepared by");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 2;
		dataPanel.add(lblType, gbc_lblType);

		prepUserLabel = new JLabel("");
		GridBagConstraints gbc_prepUserLabel = new GridBagConstraints();
		gbc_prepUserLabel.gridwidth = 2;
		gbc_prepUserLabel.insets = new Insets(0, 0, 5, 5);
		gbc_prepUserLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_prepUserLabel.gridx = 1;
		gbc_prepUserLabel.gridy = 2;
		dataPanel.add(prepUserLabel, gbc_prepUserLabel);

		btnSelectUser = new JButton("Select user");
		btnSelectUser.setActionCommand(MainActionCommands.SELECT_USER_DIALOG_COMMAND.getName());
		btnSelectUser.addActionListener(this);
		GridBagConstraints gbc_btnSelectUser = new GridBagConstraints();
		gbc_btnSelectUser.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelectUser.gridx = 3;
		gbc_btnSelectUser.gridy = 2;
		dataPanel.add(btnSelectUser, gbc_btnSelectUser);

		JLabel lblPreparedOn = new JLabel("Prepared on");
		GridBagConstraints gbc_lblPreparedOn = new GridBagConstraints();
		gbc_lblPreparedOn.anchor = GridBagConstraints.EAST;
		gbc_lblPreparedOn.insets = new Insets(0, 0, 5, 5);
		gbc_lblPreparedOn.gridx = 0;
		gbc_lblPreparedOn.gridy = 3;
		dataPanel.add(lblPreparedOn, gbc_lblPreparedOn);

		datePicker = new DatePicker();
		GridBagConstraints gbc_datePicker = new GridBagConstraints();
		gbc_datePicker.insets = new Insets(0, 0, 5, 5);
		gbc_datePicker.fill = GridBagConstraints.BOTH;
		gbc_datePicker.gridx = 1;
		gbc_datePicker.gridy = 3;
		dataPanel.add(datePicker, gbc_datePicker);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 4;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 4;
		dataPanel.add(panel_1, gbc_panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		sopPanel = new DockableSopPanel(this);
		documentsPanel = new DockableDocumentsPanel(this);
		prepSampleTable =  new DockablePrepSampleTable();

		grid.add(0, 0, 100, 100, sopPanel, documentsPanel, prepSampleTable);
		control.getContentArea().deploy(grid);
		panel_1.add(control.getContentArea(), BorderLayout.CENTER);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

				//	Reset to original

			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton("Save");
		btnSave.setActionCommand(MainActionCommands.EDIT_SAMPLE_PREP_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);

		loadLayout(layoutConfigFile);
	}

	public void loadPrepData(LIMSSamplePreparation prep) {

		clearPanel();
		this.prep = prep;
		if(prep == null)
			return;

		idValueLabel.setText(prep.getId());
		nameTextField.setText(prep.getName());
		prepUserLabel.setText(prep.getCreator().getInfo());
		prepUser = prep.getCreator();
		if (prep.getPrepDate() != null) {
			LocalDate localDate = prep.getPrepDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
			documentsPanel.setModelFromAnnotations(
					AnnotationUtils.getObjetAnnotations(AnnotatedObjectType.SAMPLE_PREP, prep.getId()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void clearPanel() {

		idValueLabel.setText("");
		nameTextField.setText("");
		prepUserLabel.setText("");
		datePicker.clear();
		prepUser = null;
		prepSampleTable.clearTable();
		sopPanel.clearPanel();
		documentsPanel.clearPanel();
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

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub

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

		if(command.equals(MainActionCommands.ADD_DOCUMENT_COMMAND.getName()))
			addDocument();

		if(command.equals(MainActionCommands.DELETE_DOCUMENT_COMMAND.getName()))
			deleteDocument();
	}

	private void deleteDocument() {
		// TODO Auto-generated method stub

	}

	private void addDocument() {
		// TODO Auto-generated method stub

	}

	private void showAddDocumentDialog() {
		// TODO Auto-generated method stub

	}

	private void setNewPrepUser() {

		if(userSelectorDialog.getSelectedUser() == null)
			return;

		prepUser = userSelectorDialog.getSelectedUser();
		prepUserLabel.setText(prepUser.getInfo());
		userSelectorDialog.dispose();
	}

	private void showUserSelector() {

		userSelectorDialog = new UserSelectorDialog(this);
		userSelectorDialog.setTitle("Select user in charge of sample preparation");
		userSelectorDialog.setLocationRelativeTo(this.getContentPane());
		userSelectorDialog.setVisible(true);
	}

	private void addSelectedSops() {

		Collection<LIMSProtocol> selected = prepSopSelectorDialog.getSelectedProtocols();
		if(selected == null)
			return;

		Collection<LIMSProtocol>protocols = new TreeSet<LIMSProtocol>();
		if(prep != null)
			protocols.addAll(prep.getProtocols());

		protocols.addAll(selected);
		sopPanel.setTableModelFromProtocols(protocols);
		prepSopSelectorDialog.dispose();
	}

	private void deleteSelectedSops() {

		if(sopPanel.getSelectedProtocols() == null)
			return;

		if(MessageDialog.showChoiceWithWarningMsg(
				"Do you want to remove selected SOPs from sample prep?",
				this.getContentPane()) == JOptionPane.NO_OPTION)
			return;

		Collection<LIMSProtocol>protocols = new TreeSet<LIMSProtocol>();
		if(prep != null)
			protocols.addAll(prep.getProtocols());

		sopPanel.getSelectedProtocols().stream().forEach(p -> protocols.remove(p));
		sopPanel.setTableModelFromProtocols(protocols);
		prepSopSelectorDialog.dispose();
	}

	private void showAddSopDialog() {

		prepSopSelectorDialog = new PrepSopSelectorDialog(this);
		prepSopSelectorDialog.setLocationRelativeTo(this.getContentPane());
		prepSopSelectorDialog.setVisible(true);
	}

	public LIMSSamplePreparation getSamplePrep() {
		return prep;
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
}




























