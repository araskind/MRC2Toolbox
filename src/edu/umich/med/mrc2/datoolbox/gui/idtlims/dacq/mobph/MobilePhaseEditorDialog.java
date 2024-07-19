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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.mobph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MobilePhaseEditorDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -8958656243635154039L;

	private static final Icon addMobilePhaseIcon = GuiUtils.getIcon("newMobilePhase", 32);
	private static final Icon editMobilePhaseIcon = GuiUtils.getIcon("editMobilePhase", 32);

	private MobilePhase mobilePhase;
	private JButton btnSave;
	private JLabel idValueLabel;
	private JTextField mobPhaseNameTextField;
	private MobilePhaseSynonymsTable synonymsTable;
	private MobilePhaseSynonymsToolbar toolBar;

	public MobilePhaseEditorDialog(MobilePhase mobilePhase, ActionListener actionListener) {
		super();
		this.mobilePhase = mobilePhase;

		setPreferredSize(new Dimension(640, 480));
		setSize(new Dimension(600, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		dataPanel.add(lblId, gbc_lblId);

		idValueLabel = new JLabel("");
		idValueLabel.setForeground(Color.BLACK);
		idValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idValueLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel lblName = new JLabel("Mobile phase description");
		lblName.setForeground(Color.BLACK);
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.gridwidth = 2;
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);
		
		mobPhaseNameTextField = new JTextField();
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.insets = new Insets(0, 0, 5, 5);
		gbc_textArea.gridwidth = 2;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 2;
		dataPanel.add(mobPhaseNameTextField, gbc_textArea);
		
		JPanel panel_1 = new JPanel(new BorderLayout(0, 0));
		panel_1.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, 
						new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Synonyms", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
				new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.gridwidth = 2;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		dataPanel.add(panel_1, gbc_panel_1);

		synonymsTable = new MobilePhaseSynonymsTable();
		panel_1.add(new JScrollPane(synonymsTable), BorderLayout.CENTER);
		
		toolBar = new MobilePhaseSynonymsToolbar(this);
		toolBar.setOrientation(SwingConstants.VERTICAL);
		panel_1.add(toolBar, BorderLayout.EAST);

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

		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadMobilePhaseData();
	}

	private void loadMobilePhaseData() {

		if(mobilePhase == null) {

			setTitle("Add new mobile phase");
			setIconImage(((ImageIcon) addMobilePhaseIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_MOBILE_PHASE_COMMAND.getName());
		}
		else {
			setTitle("Edit information for " + mobilePhase.getName());
			setIconImage(((ImageIcon) editMobilePhaseIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_MOBILE_PHASE_COMMAND.getName());
			mobPhaseNameTextField.setText(mobilePhase.getName());
			synonymsTable.setTableModelFromSynonymList(mobilePhase.getSynonyms());
			idValueLabel.setText(mobilePhase.getId());
		}
		pack();
	}

	public MobilePhase getMobilePhase() {
		return mobilePhase;
	}

	public String getmobilePhaseDescription() {
		return mobPhaseNameTextField.getText().trim();
	}
	
	public Set<String> getmobilePhaseSynonyms() {
		return synonymsTable.getSynonymList();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_MOBILE_PHASE_SYNONYM_COMMAND.getName()))
			synonymsTable.addNewSynonym();
		
		if(command.equals(MainActionCommands.DELETE_MOBILE_PHASE_SYNONYM_COMMAND.getName())) {
			
			if(!IDTUtils.isSuperUser(this.getContentPane()))
				return;
			
			synonymsTable.removeSelectedSynonym();
		}
	}
}

































