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

package edu.umich.med.mrc2.datoolbox.gui.assay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.UUID;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.idt.AssayDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class AssayManagerDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 5685218262277207511L;
	private AssayManagerToolbar toolbar;
	private AssayTable assayMethodsTable;
	private AssayEditorDialog assayEditorDialog;

	private static final Icon acqMethodIcon = GuiUtils.getIcon("acqMethod", 32);

	public AssayManagerDialog() {

		super(MRC2ToolBoxCore.getMainWindow(), "Manage assays");
		setIconImage(((ImageIcon) acqMethodIcon).getImage());
		setPreferredSize(new Dimension(640, 480));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 480));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		toolbar  = new AssayManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		assayMethodsTable = new AssayTable();
		LIMSDataCache.refreshAssayList();		
		assayMethodsTable.setTableModelFromAssayCollection(
					LIMSDataCache.getAssays());
		JScrollPane scrollPane = new JScrollPane(assayMethodsTable);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(toolbar);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if(command.equals(MainActionCommands.ADD_ASSAY_METHOD_DIALOG_COMMAND.getName()))
			showAddAssayMethodDialog();

		if(command.equals(MainActionCommands.EDIT_ASSAY_METHOD_DIALOG_COMMAND.getName()))
			showEditAssayMethodDialog();

		if(command.equals(MainActionCommands.ADD_ASSAY_METHOD_COMMAND.getName()))
			addAssayMethod();

		if(command.equals(MainActionCommands.EDIT_ASSAY_METHOD_COMMAND.getName()))
			updateAssayMethod();

		if(command.equals(MainActionCommands.DELETE_ASSAY_METHOD_COMMAND.getName()))
			deleteSelectedMethod();
	}

	private void updateAssayMethod() {

		Assay method = assayEditorDialog.getActiveMethod();
		String name = assayEditorDialog.getMethodName();
		if(name.isEmpty()) {
			MessageDialog.showErrorMsg("Assay name can not be empty!", this);
			return;
		}
		method.setName(name);
		try {
			AssayDatabaseUtils.updateAssay(method);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LIMSDataCache.refreshAssayList();		
		assayMethodsTable.setTableModelFromAssayCollection(
					LIMSDataCache.getAssays());
		assayEditorDialog.dispose();
	}

	private void addAssayMethod() {

		Assay method = assayEditorDialog.getActiveMethod();
		String name = assayEditorDialog.getMethodName();
		if(name.isEmpty()) {
			MessageDialog.showErrorMsg("Assay name can not be empty!", this);
			return;
		}
		method.setName(name);
		try {
			AssayDatabaseUtils.addNewAssay(method);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LIMSDataCache.refreshAssayList();		
		assayMethodsTable.setTableModelFromAssayCollection(
					LIMSDataCache.getAssays());
		assayEditorDialog.dispose();
	}

	private void showEditAssayMethodDialog() {
		
		Assay method = assayMethodsTable.getSelectedAssay();
		if(method == null)
			return;

		assayEditorDialog = new AssayEditorDialog(this);
		assayEditorDialog.loadMethodData(method, false);
		assayEditorDialog.setLocationRelativeTo(this);
		assayEditorDialog.setVisible(true);
	}

	private void showAddAssayMethodDialog() {

		String methodId = DataPrefix.ASSAY_METHOD.getName() + 
				UUID.randomUUID().toString().substring(0, 10);
		Assay method = new Assay(methodId, "New method");
		assayEditorDialog = new AssayEditorDialog(this);
		assayEditorDialog.loadMethodData(method, true);
		assayEditorDialog.setLocationRelativeTo(this);
		assayEditorDialog.setVisible(true);
	}

	private void deleteSelectedMethod() {

		Assay method = assayMethodsTable.getSelectedAssay();
		if(method == null)
			return;

		int approve = MessageDialog.showChoiceWithWarningMsg(
				"Delete selected assay from database?\n"
				+ "(NO UNDO!)", this);

		if (approve == JOptionPane.YES_OPTION) {

			try {
				AssayDatabaseUtils.deleteAssay(method);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LIMSDataCache.refreshAssayList();		
			assayMethodsTable.setTableModelFromAssayCollection(
						LIMSDataCache.getAssays());
		}	
	}
}
























