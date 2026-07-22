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

package edu.umich.med.mrc2.datoolbox.gui.rgen.mcr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ImputationFactorSelectorDialog extends JDialog {

	private static final Icon dialogIcon = GuiUtils.getIcon("alignment", 32);
	
	private Set<String>allFactors;
	private Set<String>selectedFactors;
	private JList<String> factorList;
	
	public ImputationFactorSelectorDialog(
			Set<String>allFactors,
			Set<String>selectedFactors,
			ActionListener actionListener) {
		super();
		setTitle("Select factors for imputation algorithm");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(600, 250));
		setSize(new Dimension(600, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.allFactors = allFactors;
		this.selectedFactors = selectedFactors;

		JPanel dataPanel = new JPanel(new BorderLayout(0, 0));
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		
		factorList = new JList<String>(allFactors.toArray(new String[allFactors.size()]));
		factorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		dataPanel.add(new JScrollPane(factorList), BorderLayout.CENTER);	
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(e -> dispose());
		buttonPanel.add(btnCancel);

		JButton btnSave = new JButton(
				MainActionCommands.ACCEPT_EXP_FACTORS_4MC_ALIGNMENT_IMPUTE_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.ACCEPT_EXP_FACTORS_4MC_ALIGNMENT_IMPUTE_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		buttonPanel.add(btnSave);
		
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(al -> { dispose(); }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		selectFactors();
		pack();
	}
	
	private void selectFactors() {

        ListModel<String> model = factorList.getModel();
        ArrayList<Integer> matchingIndices = new ArrayList<>();

        for (int i = 0; i < model.getSize(); i++) {
            if (selectedFactors.contains(model.getElementAt(i))) {
                matchingIndices.add(i);
            }
        }
        int[] indicesToSelect = matchingIndices.stream().mapToInt(i -> i).toArray();
        factorList.setSelectedIndices(indicesToSelect);
	}
	
	public List<String> getUserSelectedFactors() {
		return factorList.getSelectedValuesList();
	}
}
