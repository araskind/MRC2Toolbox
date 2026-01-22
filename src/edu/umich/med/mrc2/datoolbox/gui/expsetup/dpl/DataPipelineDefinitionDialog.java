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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.dpl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DataPipelineDefinitionDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6647789493232755799L;
	private static final Icon dataAnalysisPipelineIcon = GuiUtils.getIcon("dataAnalysisPipeline", 32);
	private JButton btnSave;
	private DataPipelineDefinitionPanel dataPipelineDefinitionPanel;

	public DataPipelineDefinitionDialog(ActionListener listener, DataPipeline dpl) {
		super();
		setTitle("Data pipeline definition");
		setIconImage(((ImageIcon) dataAnalysisPipelineIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 300));
		setPreferredSize(new Dimension(640, 300));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		
		dataPipelineDefinitionPanel = new DataPipelineDefinitionPanel();
		if(dpl != null)
			dataPipelineDefinitionPanel.setDataPipeline(dpl);
		
		getContentPane().add(dataPipelineDefinitionPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		btnCancel.addActionListener(al);
		btnSave = new JButton(MainActionCommands.SAVE_DATA_PIPELINE_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SAVE_DATA_PIPELINE_COMMAND.getName());
		btnSave.addActionListener(listener);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	public DataPipeline getDataPipeline() {
		return dataPipelineDefinitionPanel.getDataPipeline();
	}

	public DataPipelineDefinitionPanel getDataPipelineDefinitionPanel() {
		return dataPipelineDefinitionPanel;
	}
}

























