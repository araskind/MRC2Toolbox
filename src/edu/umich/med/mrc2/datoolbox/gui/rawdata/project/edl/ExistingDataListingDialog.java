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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.edl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ExistingDataListingDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6199504713874745447L;
	private static final Icon fileListIcon = GuiUtils.getIcon("dataAnalysisPipeline", 32);
	private static final Icon strongWarningIcon = GuiUtils.getIcon("stopSign", 64);
	
	public ExistingDataListingDialog(
			Map<LIMSExperiment, Collection<DataFile>> existingDataFiles) {
		super();
		setPreferredSize(new Dimension(800, 400));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(800, 400);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Data files already in database");
		setIconImage(((ImageIcon) fileListIcon).getImage());
		
		ExistingDataFilesTable dfTable = 
				new ExistingDataFilesTable();
		dfTable.setTableModelFromExistingDataFiles(existingDataFiles);
		JScrollPane scroll = new JScrollPane(dfTable);
		getContentPane().add(scroll, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Close");
		panel.add(btnCancel);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_1.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		getContentPane().add(panel_1, BorderLayout.NORTH);
		
		JLabel lblNewLabel = new JLabel(
				"MSMS data for some of the files in the project already uploaded.", 
				strongWarningIcon, 
				SwingConstants.LEFT);
		panel_1.add(lblNewLabel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(btnCancel);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
	}
}




