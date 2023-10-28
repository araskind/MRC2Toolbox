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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;

public class CloseRawDataFilesDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7706787312742576009L;
	private static final Icon closeDataFileIcon = GuiUtils.getIcon("closeDataFile", 24);
	private DataFileToAcquisitionMethodTable rawDataFileTable;

	public CloseRawDataFilesDialog(ActionListener listener) {
		
		super();
		setTitle("Close raw data files");
		setIconImage(((ImageIcon) closeDataFileIcon).getImage());
		setResizable(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(640, 480));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		rawDataFileTable = new DataFileToAcquisitionMethodTable();
		JScrollPane tableScroll = new JScrollPane(rawDataFileTable);
		getContentPane().add(tableScroll, BorderLayout.CENTER);
		rawDataFileTable.setModelFromDataFiles(RawDataManager.getRawDataMap().keySet(), false);
			
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);

		JButton closeFilesButton = new JButton("Close selected files");
		closeFilesButton.setActionCommand(MainActionCommands.FINALIZE_CLOSE_RAW_DATA_FILE_COMMAND.getName());
		closeFilesButton.addActionListener(listener);
		panel_1.add(closeFilesButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(closeFilesButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(closeFilesButton);

		pack();
	}
	
	public Collection<DataFile>getSelectedFiles(){
		return rawDataFileTable.getSelectedFiles();
	}	
}




