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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMsMsInfoPanel extends DefaultSingleCDockable {

	private JLabel msmsSourceValue;
	private JLabel msmsIdValue;
	private JFormattedTextField fragVoltageTextField;
	private JFormattedTextField cidTextField;
	private FeatuteTandemMsListingTable tandemMsListingTable;

	private static final Icon componentIcon = GuiUtils.getIcon("cog", 16);

	public DockableMsMsInfoPanel(ListSelectionListener selectionListener) {

		super("DockableMsMsInfoPanel", componentIcon, "MSMS metadata", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));

		JPanel infoPanel = new JPanel();
		add(infoPanel, BorderLayout.NORTH);

		infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, 0.0};
		infoPanel.setLayout(gbl_panel_2);

		JLabel lblMsmsSource = new JLabel("MSMS source");
		GridBagConstraints gbc_lblMsmsSource = new GridBagConstraints();
		gbc_lblMsmsSource.anchor = GridBagConstraints.EAST;
		gbc_lblMsmsSource.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsmsSource.gridx = 0;
		gbc_lblMsmsSource.gridy = 0;
		infoPanel.add(lblMsmsSource, gbc_lblMsmsSource);

		msmsSourceValue = new JLabel("");
		GridBagConstraints gbc_msmsSourceValue = new GridBagConstraints();
		gbc_msmsSourceValue.insets = new Insets(0, 0, 5, 5);
		gbc_msmsSourceValue.gridx = 1;
		gbc_msmsSourceValue.gridy = 0;
		infoPanel.add(msmsSourceValue, gbc_msmsSourceValue);

		JLabel lblMsmsId = new JLabel("MSMS ID");
		GridBagConstraints gbc_lblMsmsId = new GridBagConstraints();
		gbc_lblMsmsId.anchor = GridBagConstraints.EAST;
		gbc_lblMsmsId.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsmsId.gridx = 0;
		gbc_lblMsmsId.gridy = 1;
		infoPanel.add(lblMsmsId, gbc_lblMsmsId);

		msmsIdValue = new JLabel("");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		infoPanel.add(msmsIdValue, gbc_lblNewLabel);

		JLabel lblFragmentorVoltage = new JLabel("Fragmentor voltage");
		GridBagConstraints gbc_lblFragmentorVoltage = new GridBagConstraints();
		gbc_lblFragmentorVoltage.insets = new Insets(0, 0, 5, 5);
		gbc_lblFragmentorVoltage.anchor = GridBagConstraints.EAST;
		gbc_lblFragmentorVoltage.gridx = 0;
		gbc_lblFragmentorVoltage.gridy = 2;
		infoPanel.add(lblFragmentorVoltage, gbc_lblFragmentorVoltage);

		fragVoltageTextField = new JFormattedTextField();
		fragVoltageTextField.setMinimumSize(new Dimension(100, 20));
		fragVoltageTextField.setColumns(10);
		GridBagConstraints gbc_fragVoltageTextField = new GridBagConstraints();
		gbc_fragVoltageTextField.anchor = GridBagConstraints.WEST;
		gbc_fragVoltageTextField.insets = new Insets(0, 0, 5, 5);
		gbc_fragVoltageTextField.gridx = 1;
		gbc_fragVoltageTextField.gridy = 2;
		infoPanel.add(fragVoltageTextField, gbc_fragVoltageTextField);

		JLabel lblCid = new JLabel("CID");
		GridBagConstraints gbc_lblCid = new GridBagConstraints();
		gbc_lblCid.anchor = GridBagConstraints.EAST;
		gbc_lblCid.insets = new Insets(0, 0, 5, 5);
		gbc_lblCid.gridx = 0;
		gbc_lblCid.gridy = 3;
		infoPanel.add(lblCid, gbc_lblCid);

		cidTextField = new JFormattedTextField();
		cidTextField.setMinimumSize(new Dimension(100, 20));
		cidTextField.setColumns(10);
		GridBagConstraints gbc_cidTextField = new GridBagConstraints();
		gbc_cidTextField.anchor = GridBagConstraints.WEST;
		gbc_cidTextField.insets = new Insets(0, 0, 5, 5);
		gbc_cidTextField.gridx = 1;
		gbc_cidTextField.gridy = 3;
		infoPanel.add(cidTextField, gbc_cidTextField);

		tandemMsListingTable = new FeatuteTandemMsListingTable();
		tandemMsListingTable.getSelectionModel().addListSelectionListener(selectionListener);
		Component scrollPane = new JScrollPane(tandemMsListingTable);

		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridwidth = 3;
		gbc_scrollPane_1.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 4;
		infoPanel.add(scrollPane, gbc_scrollPane_1);
	}

	public void clearPanel() {

		msmsSourceValue.setText("");
		msmsIdValue.setText("");
		cidTextField.setText("");
		fragVoltageTextField.setText("");
		tandemMsListingTable.clearTable();
	}

	public void loadFeatureData(MsFeature activeFeature) {
		tandemMsListingTable.setTableModelFromFeature(activeFeature);
	}

	public void showMsMsParameters(TandemMassSpectrum msms) {

		msmsSourceValue.setText(msms.getSpectrumSource().getName());
		msmsIdValue.setText("");
		cidTextField.setText(Double.toString(msms.getCidLevel()));
		fragVoltageTextField.setText(Double.toString(msms.getFragmenterVoltage()));
	}

	public TandemMassSpectrum getSelectedMsMs() {
		return tandemMsListingTable.getSelectedMsMs();
	}
}






















