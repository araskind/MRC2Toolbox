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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.se.IDTrackerExperimentListingTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerExperimentDataFetchTask;

public class DatabaseExperimentSelectorDialog extends JDialog implements ActionListener {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6620732877118312717L;
	private static final Icon openIcon = GuiUtils.getIcon("openRawDataAnalysisProjectFromDb", 24);
	private IDTrackerExperimentListingTable experimentsTable;
	
	public DatabaseExperimentSelectorDialog() {
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Search ID tracker data by experiment");
		setIconImage(((ImageIcon)openIcon).getImage());
		setPreferredSize(new Dimension(640, 480));
		
		experimentsTable = new IDTrackerExperimentListingTable();
		experimentsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getContentPane().add(new JScrollPane(experimentsTable), BorderLayout.CENTER);
		experimentsTable.setTableModelFromExperimentList(IDTDataCache.getExperiments());

		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);
		btnCancel.addActionListener(al);
		btnCancel.addActionListener(al);
		
		JButton searchButton = new JButton("Load selected experiment");
		searchButton.addActionListener(this);
		searchButton.setActionCommand(MainActionCommands.OPEN_RAW_DATA_EXPERIMENT_FROM_DATABASE_COMMAND.getName());
		panel_1.add(searchButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(searchButton);

		experimentsTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							searchButton.doClick();
						}
					}
				});
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(
				MainActionCommands.OPEN_RAW_DATA_EXPERIMENT_FROM_DATABASE_COMMAND.getName())) {
			openSelectedExperiment();
		}
	}

	private void openSelectedExperiment() {
		
		LIMSExperiment idTrackerExperiment = experimentsTable.getSelectedExperiment();
		if(idTrackerExperiment == null)
			return;

		IDTrackerExperimentDataFetchTask task = 
				new IDTrackerExperimentDataFetchTask(idTrackerExperiment);
		
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.ID_WORKBENCH);
		task.addTaskListener(MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH));
		MRC2ToolBoxCore.getTaskController().addTask(task);
		dispose();
	}
}
