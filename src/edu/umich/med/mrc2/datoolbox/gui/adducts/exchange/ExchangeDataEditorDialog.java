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

package edu.umich.med.mrc2.datoolbox.gui.adducts.exchange;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class ExchangeDataEditorDialog extends JDialog implements ItemListener, ListSelectionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -6815822314059435072L;

	public static final Icon newExchangeIcon = GuiUtils.getIcon("addExchange", 32);
	public static final Icon editExchangeIcon = GuiUtils.getIcon("editExchange", 32);

	private JComboBox polarityComboBox;
	private AdductExchange activeExchange;
	private JLabel massDifferenceValueLabel;
	private JButton btnCancel, btnSave;
	private AdductSelectionTable comingAdductSelectionTable;
	private AdductSelectionTable leavingAdductSelectionTable;

	public ExchangeDataEditorDialog(ActionListener listener) {

		super();
		setTitle("Adduct exchange data editor");
		setIconImage(((ImageIcon) newExchangeIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 480));
		setPreferredSize(new Dimension(640, 480));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		getContentPane().add(panel, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("Polarity");
		panel.add(lblNewLabel);

		polarityComboBox = new JComboBox<Polarity>(new DefaultComboBoxModel<Polarity>(
				new Polarity[] {Polarity.Positive, Polarity.Negative}));
		polarityComboBox.setSelectedIndex(0);
		polarityComboBox.setPreferredSize(new Dimension(100, 20));
		polarityComboBox.addItemListener(this);
		panel.add(polarityComboBox);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));

		JPanel panel_2 = new JPanel(new BorderLayout(0,0));
		panel_2.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), 
						"Incoming", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.add(panel_2);
		
		comingAdductSelectionTable = new AdductSelectionTable();
		panel_2.add(new JScrollPane(comingAdductSelectionTable), BorderLayout.CENTER);

		JPanel panel_3 = new JPanel(new BorderLayout(0,0));
		panel_3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), 
				"Leaving", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.add(panel_3);
		
		leavingAdductSelectionTable = new AdductSelectionTable();
		panel_3.add(new JScrollPane(leavingAdductSelectionTable), BorderLayout.CENTER);

		JPanel panel_5 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_5.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_1.add(panel_5);

		JLabel lblMassDifference = new JLabel("Mass difference");
		panel_5.add(lblMassDifference);

		massDifferenceValueLabel = new JLabel("");
		panel_5.add(massDifferenceValueLabel);
		massDifferenceValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));

		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
		flowLayout_1.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_4, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		btnCancel = new JButton("Cancel");
		panel_4.add(btnCancel);
		btnCancel.addActionListener(al);

		btnSave = new JButton("Save");
		panel_4.add(btnSave);
		btnSave.addActionListener(listener);
		btnSave.setActionCommand(MainActionCommands.SAVE_EXCHANGE_DATA_COMMAND.getName());

		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.setDefaultButton(btnSave);

		populateAdductSelectors((Polarity)polarityComboBox.getSelectedItem());
		pack();
	}

	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	public void loadExchange(AdductExchange activeExchange) {

		this.activeExchange = activeExchange;

		if(activeExchange == null) {
			populateAdductSelectors(Polarity.Positive);
		}
		else {
			populateAdductSelectors(activeExchange.getComingAdduct().getPolarity());
			comingAdductSelectionTable.selectAdduct(activeExchange.getComingAdduct());
			leavingAdductSelectionTable.selectAdduct(activeExchange.getLeavingAdduct());
			calculateMassDifference();
		}
	}

	private void calculateMassDifference() {
		
		if(getComingAdduct() == null || getLeavingAdduct() == null) {
			massDifferenceValueLabel.setText("");
			return;
		}
		double diff = MsUtils.calculateMassCorrectionFromAddedRemovedGroups(getComingAdduct())
				- MsUtils.calculateMassCorrectionFromAddedRemovedGroups(getLeavingAdduct());
		massDifferenceValueLabel.setText(MRC2ToolBoxConfiguration.getMzFormat().format(diff));
	}
	
	public Adduct getComingAdduct() {
		return comingAdductSelectionTable.getSelectedModification();
	}

	public Adduct getLeavingAdduct() {
		return leavingAdductSelectionTable.getSelectedModification();
	}

	@Override
	public void itemStateChanged(ItemEvent event) {

		if (event.getStateChange() == ItemEvent.SELECTED && event.getSource().equals(polarityComboBox)) {

			if (event.getSource().equals(polarityComboBox))
				populateAdductSelectors((Polarity)polarityComboBox.getSelectedItem());
		}
	}

	private void populateAdductSelectors(Polarity newPolarity) {

		massDifferenceValueLabel.setText("");
		comingAdductSelectionTable.getSelectionModel().removeListSelectionListener(this);
		leavingAdductSelectionTable.getSelectionModel().removeListSelectionListener(this);
		
		Collection<Adduct> adductList = AdductManager.getAdductsForPolarity(newPolarity);
		comingAdductSelectionTable.setTableModelFromAdductList(adductList);
		leavingAdductSelectionTable.setTableModelFromAdductList(adductList);
		
		comingAdductSelectionTable.getSelectionModel().addListSelectionListener(this);
		leavingAdductSelectionTable.getSelectionModel().addListSelectionListener(this);
	}
	
	@Override
	public void valueChanged(ListSelectionEvent event) {

		if (!event.getValueIsAdjusting()) 
			calculateMassDifference();		
	}

	/**
	 * @return the activeExchange
	 */
	public AdductExchange getActiveExchange() {
		return activeExchange;
	}
}
