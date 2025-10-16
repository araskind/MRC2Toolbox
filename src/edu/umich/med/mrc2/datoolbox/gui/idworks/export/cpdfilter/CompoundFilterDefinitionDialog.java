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

package edu.umich.med.mrc2.datoolbox.gui.idworks.export.cpdfilter;

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

import edu.umich.med.mrc2.datoolbox.data.CompoundIdFilter;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class CompoundFilterDefinitionDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("compoundCollection", 32);
	private static final String CLEAR_FILTER = "Clear filter";
	
	private CompoundIdFilterDefinitionPanel compoundIdFilterDefinitionPanel;
		
	public CompoundFilterDefinitionDialog(ActionListener listener) {
		super();
		setTitle("Define compound filter for feature export");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(700, 640));
		setPreferredSize(new Dimension(700, 640));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		compoundIdFilterDefinitionPanel = new CompoundIdFilterDefinitionPanel();		
		compoundIdFilterDefinitionPanel.loadPreferences();
		getContentPane().add(compoundIdFilterDefinitionPanel, BorderLayout.CENTER);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		btnCancel.addActionListener(al);

		JButton addFilterButton = new JButton(
				MainActionCommands.ADD_COMPOUND_FILTER_FOR_EXPORT_COMMAND.getName());
		addFilterButton.setActionCommand(
				MainActionCommands.ADD_COMPOUND_FILTER_FOR_EXPORT_COMMAND.getName());
		addFilterButton.addActionListener(listener);
		buttonPanel.add(addFilterButton);
		
		JButton clearFilterButton = new JButton(CLEAR_FILTER);
		clearFilterButton.setActionCommand(CLEAR_FILTER);
		clearFilterButton.addActionListener(this);
		buttonPanel.add(clearFilterButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(addFilterButton);
		rootPane.setDefaultButton(addFilterButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		pack();
	}
	
	public CompoundIdFilter getCompoundIdFilter() {
		return compoundIdFilterDefinitionPanel.getCompoundIdFilter();
	}
	
	@Override
	public void dispose() {
		
		compoundIdFilterDefinitionPanel.savePreferences();
		super.dispose();
	}
	
	public void loadFilter(CompoundIdFilter compoundIdFilter) {
		compoundIdFilterDefinitionPanel.loadFilter(compoundIdFilter);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(CLEAR_FILTER))
			compoundIdFilterDefinitionPanel.clearFilterData();		
	}
}


