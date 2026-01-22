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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DeleteFactorDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -3536716877470378445L;

	private static final Icon deleteFactorIcon = GuiUtils.getIcon("deleteFactor", 32);
	private JPanel panel;
	private FactorDeleteTable factorDeleteTable;
	private JPanel panel_1;
	private JButton deleteButton;
	private JButton cancelButton;
	protected Component defaultGlassPane;

	public DeleteFactorDialog() {

		super();
		setTitle("Delete experimental factors");

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(400, 300));
		setPreferredSize(new Dimension(400, 300));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(((ImageIcon) deleteFactorIcon).getImage());
		defaultGlassPane = ((JComponent) getContentPane()).getRootPane().getGlassPane();

		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);

		panel.setBorder(null);
		panel.setLayout(new BorderLayout(0, 0));

		factorDeleteTable = new FactorDeleteTable();

		JScrollPane factorScrollPane = new JScrollPane(factorDeleteTable);
		factorScrollPane.setViewportView(factorDeleteTable);
		factorScrollPane.setPreferredSize(factorDeleteTable.getPreferredScrollableViewportSize());
		panel.add(factorScrollPane, BorderLayout.CENTER);

		panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel.add(panel_1, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(al);
		panel_1.add(cancelButton);

		deleteButton = new JButton("Delete selected factors");
		deleteButton.addActionListener(this);
		deleteButton.setActionCommand(MainActionCommands.DELETE_FACTOR_COMMAND.getName());
		panel_1.add(deleteButton);

		JRootPane rootPane = SwingUtilities.getRootPane(deleteButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(deleteButton);

		ExperimentDesignSubset completeDesign =
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getCompleteDesignSubset();
		factorDeleteTable.setTableModelFromDesignSubset(completeDesign);

		pack();
	}

	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.DELETE_FACTOR_COMMAND.getName()))
			 deleteSelectedFactor();		
	}
	
	private void deleteSelectedFactor() {
		
		if(factorDeleteTable.getSelectedFactors().isEmpty())
			return;
		
		int approve = MessageDialog.showChoiceWithWarningMsg(
				"Delete selected factor(s) from the experiment design?\n"
				+ "(NO UNDO!)", this);
		
		if (approve == JOptionPane.YES_OPTION) {
			
			ExperimentDesign design = 
					MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign();
			for(ExperimentDesignFactor factor : factorDeleteTable.getSelectedFactors())
				design.deleteFactor(factor,false);
			
			design.fireExperimentDesignEvent(ParameterSetStatus.CHANGED);

			factorDeleteTable.setTableModelFromDesignSubset(design.getCompleteDesignSubset());
		}
	}
}




























