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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
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
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DuplicateFindDialog extends JDialog implements BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -4242598486190961903L;

	private static final Icon showDuplicatesIcon = GuiUtils.getIcon("findDuplicates", 32);
	private JButton btnCancel;
	private JButton findDuplicatesButton;
	private JFormattedTextField massWidowTextField;
	private JFormattedTextField rtWindowTextField;
	private Preferences preferences;

	public static final String MASS_WINDOW_PPM = "MASS_WINDOW_PPM";
	public static final String RT_WINDOW_MIN = "RT_WINDOW_MIN";

	public DuplicateFindDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Merge duplicate features");
		setIconImage(((ImageIcon) showDuplicatesIcon).getImage());

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(350, 150));
		setPreferredSize(new Dimension(350, 150));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblMassWindow = new JLabel("Mass window");
		GridBagConstraints gbc_lblMassWindow = new GridBagConstraints();
		gbc_lblMassWindow.insets = new Insets(0, 0, 5, 5);
		gbc_lblMassWindow.anchor = GridBagConstraints.EAST;
		gbc_lblMassWindow.gridx = 0;
		gbc_lblMassWindow.gridy = 0;
		panel.add(lblMassWindow, gbc_lblMassWindow);

		massWidowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massWidowTextField.setColumns(10);
		GridBagConstraints gbc_massWidowTextField = new GridBagConstraints();
		gbc_massWidowTextField.gridwidth = 2;
		gbc_massWidowTextField.insets = new Insets(0, 0, 5, 5);
		gbc_massWidowTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_massWidowTextField.gridx = 1;
		gbc_massWidowTextField.gridy = 0;
		panel.add(massWidowTextField, gbc_massWidowTextField);

		JLabel lblPpm = new JLabel("ppm");
		GridBagConstraints gbc_lblPpm = new GridBagConstraints();
		gbc_lblPpm.anchor = GridBagConstraints.WEST;
		gbc_lblPpm.insets = new Insets(0, 0, 5, 5);
		gbc_lblPpm.gridx = 3;
		gbc_lblPpm.gridy = 0;
		panel.add(lblPpm, gbc_lblPpm);

		JLabel lblRetentionWindow = new JLabel("Retention window");
		GridBagConstraints gbc_lblRetentionWindow = new GridBagConstraints();
		gbc_lblRetentionWindow.anchor = GridBagConstraints.EAST;
		gbc_lblRetentionWindow.insets = new Insets(0, 0, 5, 5);
		gbc_lblRetentionWindow.gridx = 0;
		gbc_lblRetentionWindow.gridy = 1;
		panel.add(lblRetentionWindow, gbc_lblRetentionWindow);

		rtWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtWindowTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.gridwidth = 2;
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 1;
		panel.add(rtWindowTextField, gbc_formattedTextField);

		JLabel lblMin = new JLabel("min");
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.insets = new Insets(0, 0, 5, 5);
		gbc_lblMin.anchor = GridBagConstraints.WEST;
		gbc_lblMin.gridx = 3;
		gbc_lblMin.gridy = 1;
		panel.add(lblMin, gbc_lblMin);

		JLabel label = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 4;
		gbc_label.gridy = 2;
		panel.add(label, gbc_label);

		btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 3;
		panel.add(btnCancel, gbc_btnCancel);

		findDuplicatesButton = new JButton("Find duplicate features");
		findDuplicatesButton.setActionCommand(MainActionCommands.FIND_DUPLICATES_COMMAND.getName());
		findDuplicatesButton.addActionListener(listener);
		GridBagConstraints gbc_findDuplicatesButton = new GridBagConstraints();
		gbc_findDuplicatesButton.anchor = GridBagConstraints.EAST;
		gbc_findDuplicatesButton.gridwidth = 3;
		gbc_findDuplicatesButton.gridx = 2;
		gbc_findDuplicatesButton.gridy = 3;
		panel.add(findDuplicatesButton, gbc_findDuplicatesButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(findDuplicatesButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(findDuplicatesButton);

		loadPreferences();

		pack();
	}

	public double getMassWindow() {
		return Double.parseDouble(massWidowTextField.getText());
	}

	public double getRetentionWindow() {
		return Double.parseDouble(rtWindowTextField.getText());
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;

		double massError = preferences.getDouble(MASS_WINDOW_PPM, 20.0d);
		massWidowTextField.setText(Double.toString(massError));

		double rtWindow = preferences.getDouble(RT_WINDOW_MIN, 0.05d);
		rtWindowTextField.setText(Double.toString(rtWindow));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());

		if(!massWidowTextField.getText().isEmpty())
			preferences.putDouble(MASS_WINDOW_PPM, getMassWindow());

		if(!rtWindowTextField.getText().isEmpty())
			preferences.putDouble(RT_WINDOW_MIN, getRetentionWindow());
	}

	@Override
	public void dispose() {

		savePreferences();
		super.dispose();
	}
}












