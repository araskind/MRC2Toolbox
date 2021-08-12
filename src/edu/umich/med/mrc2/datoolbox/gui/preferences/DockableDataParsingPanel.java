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

package edu.umich.med.mrc2.datoolbox.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTextField;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableDataParsingPanel extends DefaultSingleCDockable implements BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = 8823305722073755939L;

	private static final Icon componentIcon = GuiUtils.getIcon("loadList", 16);

	private JTextField sampleNameMaskTextField;
	private JTextField sampleIdMaskTextField;

	private Preferences prefs;

	public DockableDataParsingPanel() {

		super("DockableDataParsingPanel", componentIcon, "Data parsing", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel sampleIdMaskLabel = new JLabel("Sample ID mask");
		GridBagConstraints gbc_sampleIdMaskLabel = new GridBagConstraints();
		gbc_sampleIdMaskLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sampleIdMaskLabel.anchor = GridBagConstraints.EAST;
		gbc_sampleIdMaskLabel.gridx = 0;
		gbc_sampleIdMaskLabel.gridy = 0;
		add(sampleIdMaskLabel, gbc_sampleIdMaskLabel);

		sampleIdMaskTextField = new JTextField();
		GridBagConstraints gbc_sampleIdMaskTextField = new GridBagConstraints();
		gbc_sampleIdMaskTextField.insets = new Insets(0, 0, 5, 0);
		gbc_sampleIdMaskTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleIdMaskTextField.gridx = 1;
		gbc_sampleIdMaskTextField.gridy = 0;
		add(sampleIdMaskTextField, gbc_sampleIdMaskTextField);
		sampleIdMaskTextField.setColumns(10);

		// TODO get this from preferences
		sampleIdMaskTextField.setText("S\\d{8}|R\\d{9}|CS\\d{7}|R\\d{8}");

		JLabel sampleNameLabel = new JLabel("Sample name mask");
		GridBagConstraints gbc_sampleNameLabel = new GridBagConstraints();
		gbc_sampleNameLabel.anchor = GridBagConstraints.EAST;
		gbc_sampleNameLabel.insets = new Insets(0, 0, 0, 5);
		gbc_sampleNameLabel.gridx = 0;
		gbc_sampleNameLabel.gridy = 1;
		add(sampleNameLabel, gbc_sampleNameLabel);

		sampleNameMaskTextField = new JTextField();
		GridBagConstraints gbc_sampleNameMaskTextField = new GridBagConstraints();
		gbc_sampleNameMaskTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleNameMaskTextField.gridx = 1;
		gbc_sampleNameMaskTextField.gridy = 1;
		add(sampleNameMaskTextField, gbc_sampleNameMaskTextField);
		sampleNameMaskTextField.setColumns(10);
	}

	@Override
	public void loadPreferences(Preferences preferences) {

		prefs = preferences;
		sampleIdMaskTextField.setText(MRC2ToolBoxConfiguration.getSampleIdMask());
		sampleNameMaskTextField.setText(MRC2ToolBoxConfiguration.getSampleNameMask());
	}

	@Override
	public void loadPreferences() {

		if(prefs != null)
			loadPreferences(prefs);
	}

	@Override
	public void savePreferences() {

		MRC2ToolBoxConfiguration.setSampleIdMask(sampleIdMaskTextField.getText().trim());
		MRC2ToolBoxConfiguration.setSampleNameMask(sampleNameMaskTextField.getText().trim());
	}
}
