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

package edu.umich.med.mrc2.datoolbox.gui.adducts.bindif;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerAnnotationBase;
import edu.umich.med.mrc2.datoolbox.gui.adducts.adduct.SimpleAdductTable;
import edu.umich.med.mrc2.datoolbox.gui.adducts.exchange.AdductExchangeTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class BinnerAnnotationEditorDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5709464923199416050L;
	public static final Icon editIcon = GuiUtils.getIcon("editBinnerAdduct", 32);
	public static final Icon newIcon = GuiUtils.getIcon("addBinnerAdduct", 32);
	
	private BinnerAdduct binAdduct;
	private BinnerAnnotationBase base;
	private JPanel panel;
	private JTextField nameTextField;
	private JComboBox tierComboBox;
	
	private SimpleAdductTable simpleAdductTable;
	private AdductExchangeTable adductExchangeTable;
	private BinnerNeutralMassDifferenceSelectorPanel binnerNeutralMassDifferenceSelectorPanel;
	
	public BinnerAnnotationEditorDialog(
			ActionListener listener, 
			BinnerAdduct binAdduct, 
			BinnerAnnotationBase base) {

		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Binner annotation data editor");
		setIconImage(((ImageIcon) editIcon).getImage());
		setPreferredSize(new Dimension(800, 450));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.base = base;
		this.binAdduct = binAdduct;
		
		panel = new JPanel(new BorderLayout(0,0));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 0, 10, 0));
		panel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Name ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.insets = new Insets(0, 0, 0, 5);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		panel_1.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Tier");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		tierComboBox = new JComboBox<Integer>(
				new DefaultComboBoxModel<Integer>(new Integer[] {1,2,3,4}));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 3;
		gbc_comboBox.gridy = 0;
		panel_1.add(tierComboBox, gbc_comboBox);
		
		initEditorPanel();
		loadAdductData();
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(al);	
		buttonPanel.add(cancelButton);
		
		JButton saveButton = new JButton("Save");
		saveButton.setActionCommand(
				MainActionCommands.SAVE_BINNER_ADDUCT_COMMAND.getName());
		saveButton.addActionListener(listener);
		buttonPanel.add(saveButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);		
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	private void initEditorPanel() {
		
		if(base.equals(BinnerAnnotationBase.ADDUCT)) {
			
			Collection<Adduct> availableAdducts = AdductManager.getChargedAdductList();
			if(binAdduct == null) {				
				//	Filter out used adducts
				Set<Adduct> usedAdducts = 
						AdductManager.getBinnerAdductList().stream().
						filter(a -> Objects.nonNull(a.getChargeCarrier())).
						map(a -> a.getChargeCarrier()).distinct().
						collect(Collectors.toSet());
				availableAdducts = AdductManager.getChargedAdductList().stream().
						filter(a -> !usedAdducts.contains(a)).
						sorted(AdductManager.adductTypeNameSorter).
						collect(Collectors.toList());
			}
			simpleAdductTable = new SimpleAdductTable();
			simpleAdductTable.setTableModelFromAdductList(availableAdducts);
			panel.add(new JScrollPane(simpleAdductTable), BorderLayout.CENTER);
		}		
		if(base.equals(BinnerAnnotationBase.EXCHANGE)) {			
			
			Collection<AdductExchange> availableAdductExchanges = 
					AdductManager.getAdductExchangeList();
			if(binAdduct == null) {	
				//	Filter out used exchanges
				Set<AdductExchange> usedAdductExchanges = 
						AdductManager.getBinnerAdductList().stream().
						filter(a -> Objects.nonNull(a.getAdductExchange())).
						map(a -> a.getAdductExchange()).distinct().
						collect(Collectors.toSet());
				availableAdductExchanges = 
						AdductManager.getAdductExchangeList().stream().
						filter(a -> !usedAdductExchanges.contains(a)).
						sorted().collect(Collectors.toList());
			}
			adductExchangeTable = new AdductExchangeTable();
			adductExchangeTable
				.setTableModelFromAdductExchangeList(availableAdductExchanges);
			panel.add(new JScrollPane(adductExchangeTable), BorderLayout.CENTER);
		}				
		if(base.equals(BinnerAnnotationBase.MASS_DIFFERENCE)) {	
			
			Collection<BinnerNeutralMassDifference> availableBinnerNeutralMassDifferences = 
					AdductManager.getBinnerNeutralMassDifferenceList();
			if(binAdduct == null) {	
				//	Filter out used exchanges
				Set<BinnerNeutralMassDifference> usedBinnerNeutralMassDifferences = 
						AdductManager.getBinnerAdductList().stream().
						filter(a -> Objects.nonNull(a.getBinnerNeutralMassDifference())).
						map(a -> a.getBinnerNeutralMassDifference()).distinct().
						collect(Collectors.toSet());
				availableBinnerNeutralMassDifferences = 
						AdductManager.getBinnerNeutralMassDifferenceList().stream().
						filter(a -> !usedBinnerNeutralMassDifferences.contains(a)).
						sorted().collect(Collectors.toList());
			}
			binnerNeutralMassDifferenceSelectorPanel  = new BinnerNeutralMassDifferenceSelectorPanel();
			binnerNeutralMassDifferenceSelectorPanel.populateBinnerNeutralMassDifferenceTable(
					availableBinnerNeutralMassDifferences);
			panel.add(binnerNeutralMassDifferenceSelectorPanel, BorderLayout.CENTER);
		}
	}
	
	private void loadAdductData() {
		
		if(binAdduct == null)
			return;
		
		nameTextField.setText(binAdduct.getName());
		tierComboBox.setSelectedItem(binAdduct.getTier());
		
		if(base.equals(BinnerAnnotationBase.ADDUCT))
			simpleAdductTable.selectAdduct(binAdduct.getChargeCarrier());
			
		if(base.equals(BinnerAnnotationBase.EXCHANGE))		
			adductExchangeTable.selectExchange(binAdduct.getAdductExchange());
						
		if(base.equals(BinnerAnnotationBase.MASS_DIFFERENCE))	
			binnerNeutralMassDifferenceSelectorPanel.selectMassDifference(
					binAdduct.getBinnerNeutralMassDifference());		
	}

	public BinnerAdduct getBinAdduct() {
		return binAdduct;
	}

	public BinnerAdduct getEditedBinAdduct() {
		
		BinnerAdduct edited = new BinnerAdduct(
				null, 
				getAdductName(), 
				getCharge(), 
				getTier(), 
				getChargeCarrier(),
				getAdductExchange(), 
				getBinnerNeutralMassDifference());
				
		return edited;
	}
	
	public String getAdductName() {		
		return nameTextField.getText().trim();
	}
	
	public int getTier() {	
		return (int)tierComboBox.getSelectedItem();
	}
	
	public int getCharge() {
		
		if(getChargeCarrier() != null)
			return getChargeCarrier().getCharge();
		
		if(getAdductExchange() != null)
			return getAdductExchange().getCharge();
		
		return 0;
	}
	
	public Adduct getChargeCarrier() {
		
		if(base.equals(BinnerAnnotationBase.ADDUCT))
			return simpleAdductTable.getSelectedAdduct();
		else
			return null;
	}
	
	public AdductExchange getAdductExchange() { 
		
		if(base.equals(BinnerAnnotationBase.EXCHANGE))
			return adductExchangeTable.getSelectedExchange();
		else
			return null;
	}
	
	public BinnerNeutralMassDifference getBinnerNeutralMassDifference() {
		
		if(base.equals(BinnerAnnotationBase.MASS_DIFFERENCE))
			return binnerNeutralMassDifferenceSelectorPanel.getSelectedMassDifference();
		else
			return null;
	}
	
	public BinnerAnnotationBase getBase() {
		return base;
	}
	
	public Collection<String> validateBinnerAdduct(){
		
		Collection<String> errors = new ArrayList<String>();
		String name  = getAdductName();
		BinnerAdduct existingAdduct = null;
		//	Verify name
		if(name.isEmpty())
			errors.add("Name has to be defined.");
		else {
			existingAdduct = null;
			if(binAdduct == null) {
				existingAdduct  = AdductManager.getBinnerAdductList().stream().
						filter(a -> a.getBinnerName().equals(name)).
						findFirst().orElse(null);
				if(existingAdduct != null)
					errors.add("Binner annotation \"" + name + "\" already exists.");
			}
			else {	
				String id = binAdduct.getId();
				existingAdduct  = AdductManager.getBinnerAdductList().stream().
						filter(a -> !a.getId().equals(id)).
						filter(a -> a.getBinnerName().equals(name)).
						findFirst().orElse(null);
				if(existingAdduct != null)
					errors.add("A different Binner annotation \"" + name + "\" already exists.");
			}
		}		
		if(base.equals(BinnerAnnotationBase.ADDUCT)) {
			
			Adduct chargeCarrier = getChargeCarrier();
			if(chargeCarrier == null) {
				errors.add("Charge carrier aduct has to be selected.");
				return errors;
			}
			if(binAdduct == null) {
				
				existingAdduct  = AdductManager.getBinnerAdductList().stream().
						filter(a -> Objects.nonNull(a.getChargeCarrier())).
						filter(a -> a.getChargeCarrier().equals(chargeCarrier)).
						findFirst().orElse(null);
				if(existingAdduct != null)
					errors.add("Binner annotation \"" + existingAdduct.getBinnerName() + 
							"\" corresponding to charge carrier \"" + chargeCarrier.getName() + "\" already exists.");
			}
			else {
				String id = binAdduct.getId();
				existingAdduct  = AdductManager.getBinnerAdductList().stream().
						filter(a -> !a.getId().equals(id)).
						filter(a -> Objects.nonNull(a.getChargeCarrier())).
						filter(a -> a.getChargeCarrier().equals(chargeCarrier)).
						findFirst().orElse(null);
				if(existingAdduct != null)
					errors.add("Another Binner annotation \"" + existingAdduct.getBinnerName() + 
							"\" corresponding to charge carrier \"" + chargeCarrier.getName() + "\" already exists.");
			}
		}
		if(base.equals(BinnerAnnotationBase.EXCHANGE)) {
			
			AdductExchange exchange = getAdductExchange();
			if(exchange == null) {
				errors.add("Adduct exchange has to be selected.");
				return errors;
			}
			if(binAdduct == null) {
				
				existingAdduct  = AdductManager.getBinnerAdductList().stream().
						filter(a -> Objects.nonNull(a.getAdductExchange())).
						filter(a -> a.getAdductExchange().equals(exchange)).
						findFirst().orElse(null);
				if(existingAdduct != null)
					errors.add("Binner annotation \"" + existingAdduct.getBinnerName() + 
							"\" corresponding to adduct exchange \"" + exchange.getName() + "\" already exists.");
			}
			else {
				String id = binAdduct.getId();
				existingAdduct  = AdductManager.getBinnerAdductList().stream().
						filter(a -> !a.getId().equals(id)).
						filter(a -> Objects.nonNull(a.getAdductExchange())).
						filter(a -> a.getAdductExchange().equals(exchange)).
						findFirst().orElse(null);
				if(existingAdduct != null)
					errors.add("Another Binner annotation \"" + existingAdduct.getBinnerName() + 
							"\" corresponding to adduct exchange \"" + exchange.getName() + "\" already exists.");
			}
		}
		if(base.equals(BinnerAnnotationBase.MASS_DIFFERENCE)) {
			
			BinnerNeutralMassDifference mDiff = getBinnerNeutralMassDifference();
			if(mDiff == null) {
				errors.add("Mass difference has to be selected.");
				return errors;
			}
			if(binAdduct == null) {
				
				existingAdduct  = AdductManager.getBinnerAdductList().stream().
						filter(a -> Objects.nonNull(a.getBinnerNeutralMassDifference())).
						filter(a -> a.getBinnerNeutralMassDifference().equals(mDiff)).
						findFirst().orElse(null);
				if(existingAdduct != null)
					errors.add("Binner annotation \"" + existingAdduct.getBinnerName() + 
							"\" corresponding to mass difference \"" + mDiff.getName() + "\" already exists.");
			}
			else {
				String id = binAdduct.getId();
				existingAdduct  = AdductManager.getBinnerAdductList().stream().
						filter(a -> !a.getId().equals(id)).
						filter(a -> Objects.nonNull(a.getBinnerNeutralMassDifference())).
						filter(a -> a.getBinnerNeutralMassDifference().equals(mDiff)).
						findFirst().orElse(null);
				if(existingAdduct != null)
					errors.add("Another Binner annotation \"" + existingAdduct.getBinnerName() + 
							"\" corresponding to mass difference \"" + mDiff.getName() + "\" already exists.");
			}
		}
		return errors;
	}
}














