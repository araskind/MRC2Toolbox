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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DuplicateMergeDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 5636049256294767415L;
	private JComboBox mergeTypeComboBox;
	private JButton cancelButton, mergeButton;
	private JLabel lblNewLabel;
	private Preferences preferences;

	private static final Icon clearDuplicatesIcon = GuiUtils.getIcon("clearDuplicates", 32);

	public static final String MERGE_OPTION = "MERGE_OPTION";

	public DuplicateMergeDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Merge duplicate features");
		setIconImage(((ImageIcon) clearDuplicatesIcon).getImage());

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(350, 150));
		setPreferredSize(new Dimension(350, 150));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel mergeOptionsLabel = new JLabel("Merging type ");
		GridBagConstraints gbc_mergeOptionsLabel = new GridBagConstraints();
		gbc_mergeOptionsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mergeOptionsLabel.anchor = GridBagConstraints.EAST;
		gbc_mergeOptionsLabel.gridx = 0;
		gbc_mergeOptionsLabel.gridy = 0;
		panel.add(mergeOptionsLabel, gbc_mergeOptionsLabel);

		mergeTypeComboBox = new JComboBox<DuplicatesCleanupOptions>();
		SortedComboBoxModel<DuplicatesCleanupOptions> options = new SortedComboBoxModel<DuplicatesCleanupOptions>(
				DuplicatesCleanupOptions.values());
		mergeTypeComboBox.setModel(options);
		GridBagConstraints gbc_mergeTypeComboBox = new GridBagConstraints();
		gbc_mergeTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_mergeTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_mergeTypeComboBox.gridx = 1;
		gbc_mergeTypeComboBox.gridy = 0;
		panel.add(mergeTypeComboBox, gbc_mergeTypeComboBox);

		lblNewLabel = new JLabel(" ");
		lblNewLabel.setPreferredSize(new Dimension(3, 25));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 0;
		gbc_cancelButton.gridy = 2;
		panel.add(cancelButton, gbc_cancelButton);

		mergeButton = new JButton("Merge duplicates");
		mergeButton.addActionListener(listener);
		mergeButton.setActionCommand(MainActionCommands.MERGE_DUPLICATES_COMMAND.getName());
		GridBagConstraints gbc_mergeButton = new GridBagConstraints();
		gbc_mergeButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_mergeButton.gridx = 1;
		gbc_mergeButton.gridy = 2;
		panel.add(mergeButton, gbc_mergeButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(mergeButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(mergeButton);

		loadPreferences();

		pack();
	}

	public DuplicatesCleanupOptions getMergeOption() {

		return (DuplicatesCleanupOptions) mergeTypeComboBox.getSelectedItem();
	}

	public void loadPreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		int mergeOptionIndex = preferences.getInt(MERGE_OPTION, 1);
		mergeTypeComboBox.setSelectedIndex(mergeOptionIndex);
	}

	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.putInt(MERGE_OPTION, mergeTypeComboBox.getSelectedIndex());
	}

	@Override
	public void setVisible(boolean visible) {

		if(!visible)
			savePreferences();

		super.setVisible(visible);
	}
}
