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

package edu.umich.med.mrc2.datoolbox.gui.idworks.tophit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ReassignDefaultMSMSLibraryHitDialog extends JDialog 
		implements BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -4829500028249958394L;
	private static final Icon reassignTopHitsIcon = GuiUtils.getIcon("recalculateScores", 32);

	public static final String TOP_HIT_REASSIGNMENT_OPTION = "TOP_HIT_REASSIGNMENT_OPTION";
	public static final String COMMIT_CHANGES = "COMMIT_CHANGES";
	public static final String USE_ENTROPY_SCORE = "USE_ENTROPY_SCORE";
	public static final String PREFERENCES_NODE = "edu.umich.med.mrc2.datoolbox.ReassignDefaultMSMSLibraryHitDialog";
	
	private JCheckBox commitChangesCheckBox;
	private JCheckBox useEntropyScoreCheckBox;
	private JComboBox topHitRuleComboBox;
	
	public ReassignDefaultMSMSLibraryHitDialog(ActionListener listener) {

		super();

		setTitle("Reassign default MSMS library matches");
		setIconImage(((ImageIcon) reassignTopHitsIcon).getImage());
		setPreferredSize(new Dimension(400, 200));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(400, 200));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel setupPanel = new JPanel();
		setupPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(setupPanel, BorderLayout.CENTER);
		GridBagLayout gbl_setupPanel = new GridBagLayout();
		gbl_setupPanel.columnWidths = new int[]{0, 0};
		gbl_setupPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_setupPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_setupPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setupPanel.setLayout(gbl_setupPanel);
		
		JLabel lblNewLabel = new JLabel("Top hit selection rule:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		setupPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		topHitRuleComboBox = new JComboBox<TopHitReassignmentOption>(
				new DefaultComboBoxModel<TopHitReassignmentOption>(
						TopHitReassignmentOption.values()));
		GridBagConstraints gbc_topHitRuleComboBox = new GridBagConstraints();
		gbc_topHitRuleComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_topHitRuleComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_topHitRuleComboBox.gridx = 0;
		gbc_topHitRuleComboBox.gridy = 1;
		setupPanel.add(topHitRuleComboBox, gbc_topHitRuleComboBox);
		
		useEntropyScoreCheckBox = 
				new JCheckBox("Use entropy-based score as quality measure");
		GridBagConstraints gbc_useEntropyScoreCheckBox = new GridBagConstraints();
		gbc_useEntropyScoreCheckBox.anchor = GridBagConstraints.WEST;
		gbc_useEntropyScoreCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_useEntropyScoreCheckBox.gridx = 0;
		gbc_useEntropyScoreCheckBox.gridy = 2;
		setupPanel.add(useEntropyScoreCheckBox, gbc_useEntropyScoreCheckBox);
		
		commitChangesCheckBox = new JCheckBox("Commit changes to database");
		GridBagConstraints gbc_commitChangesCheckBox = new GridBagConstraints();
		gbc_commitChangesCheckBox.anchor = GridBagConstraints.WEST;
		gbc_commitChangesCheckBox.gridx = 0;
		gbc_commitChangesCheckBox.gridy = 3;
		setupPanel.add(commitChangesCheckBox, gbc_commitChangesCheckBox);

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
		JButton btnSave = new JButton(MainActionCommands.REASSIGN_DEFAULT_MSMS_LIBRARY_MATCHES.getName());
		btnSave.setActionCommand(MainActionCommands.REASSIGN_DEFAULT_MSMS_LIBRARY_MATCHES.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {		
		savePreferences();
		super.dispose();
	}
	
	public boolean commitChangesTodatabase() {
		return commitChangesCheckBox.isSelected();
	}
	
	public boolean useEntropyScore() {
		return useEntropyScoreCheckBox.isSelected();
	}
	
	public TopHitReassignmentOption getTopHitReassignmentOption() {
		return (TopHitReassignmentOption)topHitRuleComboBox.getSelectedItem();
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFERENCES_NODE));
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		useEntropyScoreCheckBox.setSelected(preferences.getBoolean(USE_ENTROPY_SCORE, true));
		commitChangesCheckBox.setSelected(preferences.getBoolean(COMMIT_CHANGES, true));
		TopHitReassignmentOption top =TopHitReassignmentOption.getTopHitReassignmentOptionByName(
				preferences.get(TOP_HIT_REASSIGNMENT_OPTION, 
						TopHitReassignmentOption.PREFER_NORMAL_HITS.name()));
		topHitRuleComboBox.setSelectedItem(top);
	}

	@Override
	public void savePreferences() {
		
		Preferences preferences = Preferences.userRoot().node(PREFERENCES_NODE);
		
		preferences.putBoolean(COMMIT_CHANGES, commitChangesCheckBox.isSelected());
		preferences.putBoolean(USE_ENTROPY_SCORE, useEntropyScoreCheckBox.isSelected());
		preferences.put(TOP_HIT_REASSIGNMENT_OPTION, getTopHitReassignmentOption().name());
	}

	public void blockCommitToDatabase() {
		commitChangesCheckBox.setSelected(false);
		commitChangesCheckBox.setEnabled(false);
	}
}
































