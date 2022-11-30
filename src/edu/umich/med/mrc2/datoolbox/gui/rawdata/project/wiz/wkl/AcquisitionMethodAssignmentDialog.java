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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.wkl;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.methods.AcquisitionMethodTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class AcquisitionMethodAssignmentDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4163723314679517648L;
	private static final Icon assignAcqMethodIcon = GuiUtils.getIcon("addDataAcquisitionMethod", 24);
	private AcquisitionMethodTable methodTable;
	
	public AcquisitionMethodAssignmentDialog(
			ActionListener actionListener, 
			Collection<DataAcquisitionMethod> acquisitionMethods) {
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(600, 300);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Assign data acquisition method method");
		setIconImage(((ImageIcon) assignAcqMethodIcon).getImage());
		
		methodTable = new AcquisitionMethodTable();
		methodTable.setTableModelFromAcquisitionMethodsCollection(acquisitionMethods);

		JScrollPane scroll = new JScrollPane(methodTable);
		getContentPane().add(scroll, BorderLayout.CENTER);
		
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

		JButton btnSave = new JButton(
				MainActionCommands.ASSIGN_ACQ_METHOD_FOR_SELECTED_DATA_FILES_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.ASSIGN_ACQ_METHOD_FOR_SELECTED_DATA_FILES_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		methodTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							btnSave.doClick();
						}
					}
				});
	}
	
	public DataAcquisitionMethod getSelectedDataAcquisitionMethod() {
		return (DataAcquisitionMethod)methodTable.getSelectedAnalysisMethod();
	}
}
