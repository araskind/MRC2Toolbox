/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.library.manager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.AdductSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.adducts.adduct.AdductSelectorPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class LibraryInfoDialog extends JDialog implements ItemListener{

	private static final long serialVersionUID = 7917036676205745402L;
	
	private static final Icon newLibraryIcon = GuiUtils.getIcon("newLibrary", 32);
	private static final Icon editLibInfoIcon = GuiUtils.getIcon("editLibrary", 32);
	private static final Icon duplicateLibraryIcon = GuiUtils.getIcon("duplicateLibrary", 32);
	
	private JTextField nameTextField;
	private JTextArea libraryDescriptionTextArea;
	private JButton cancelButton, saveButton;
	private JLabel createDefaultAdductsLabel;
	private JCheckBox clearRtCheckBox;
	private JCheckBox clearAnnotationsCheckBox;
	private JLabel neutralPolarityWarningLabel;
	private JCheckBox preserveSpectraOnCopyCheckBox;
	private JLabel spacerLabel;	
	private AdductSelectorPanel adductSelectorPanel;
	
	private int rowCount;
	private CompoundLibrary currentLibrary;
	
	public enum LibraryAction{
		CreateNew,
		EditInfo,
		Duplicate,
		;
	}
	
	public LibraryInfoDialog(
			ActionListener listener, 
			LibraryAction libraryAction,
			CompoundLibrary library) {

		super();
		currentLibrary = library;
		
		int height = 200;
		if(libraryAction.equals(LibraryAction.Duplicate))
			height = 500;
			
		setSize(new Dimension(665, 658));
		setPreferredSize(new Dimension(650, height));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new BorderLayout(0, 0));
		rowCount = 0;	

		JPanel panel = new JPanel();
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();	
		panel.setLayout(gbl_panel);		

		JLabel nameLabel = new JLabel("Name");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nameLabel.anchor = GridBagConstraints.EAST;
		gbc_nameLabel.gridx = 0;
		gbc_nameLabel.gridy = rowCount;
		panel.add(nameLabel, gbc_nameLabel);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 3;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.BOTH;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = rowCount;
		panel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		rowCount++;
		
		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTH;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = rowCount;
		panel.add(lblDescription, gbc_lblDescription);

		libraryDescriptionTextArea = new JTextArea();
		libraryDescriptionTextArea.setRows(3);
		libraryDescriptionTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		libraryDescriptionTextArea.setLineWrap(true);
		libraryDescriptionTextArea.setWrapStyleWord(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridheight = 2;
		gbc_textArea.gridwidth = 3;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = rowCount;
		panel.add(libraryDescriptionTextArea, gbc_textArea);
		
		rowCount++;
		rowCount++;
		
		if(libraryAction.equals(LibraryAction.Duplicate))
			createLibraryDuplicationBlock(panel);
			
		gbl_panel.columnWidths = new int[]{0, 151, 166, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};	
		
		gbl_panel.rowHeights = new int[rowCount + 1];
		Arrays.fill(gbl_panel.rowHeights, 0);
			
		gbl_panel.rowWeights = new double[rowCount + 2];
		Arrays.fill(gbl_panel.rowWeights, 0.0d);
		gbl_panel.rowWeights[rowCount + 1] = Double.MIN_VALUE;
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		cancelButton.addActionListener(e -> dispose());

		saveButton = new JButton("Save");
		saveButton.addActionListener(listener);
		buttonPanel.add(saveButton);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al -> dispose(), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		
		if(libraryAction.equals(LibraryAction.CreateNew))
			configureForNewLibrary();
		
		if(libraryAction.equals(LibraryAction.EditInfo))
			configureForEditInfo();
		
		if(libraryAction.equals(LibraryAction.Duplicate))
			configureForDuplication();
		
		pack();
	}
	
	private void createLibraryDuplicationBlock(JPanel panel) {
			
		preserveSpectraOnCopyCheckBox = 
				new JCheckBox("Preserve spectra when creating library copy");
		GridBagConstraints gbc_preserveSpectraOnCopyCheckBox = new GridBagConstraints();
		gbc_preserveSpectraOnCopyCheckBox.anchor = GridBagConstraints.WEST;
		gbc_preserveSpectraOnCopyCheckBox.gridwidth = 3;
		gbc_preserveSpectraOnCopyCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_preserveSpectraOnCopyCheckBox.gridx = 0;
		gbc_preserveSpectraOnCopyCheckBox.gridy = rowCount;
		panel.add(preserveSpectraOnCopyCheckBox, gbc_preserveSpectraOnCopyCheckBox);
		preserveSpectraOnCopyCheckBox.addItemListener(this);
		
		rowCount++;
		
		createDefaultAdductsLabel = 
				new JLabel("Create selected adducts when duplicating the library:");
		GridBagConstraints gbc_createDefaultAdductsLabel = new GridBagConstraints();
		gbc_createDefaultAdductsLabel.anchor = GridBagConstraints.WEST;
		gbc_createDefaultAdductsLabel.gridwidth = 3;
		gbc_createDefaultAdductsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_createDefaultAdductsLabel.gridx = 0;
		gbc_createDefaultAdductsLabel.gridy = rowCount;
		panel.add(createDefaultAdductsLabel, gbc_createDefaultAdductsLabel);
		
		rowCount++;		
		
		neutralPolarityWarningLabel = new JLabel("Create template library without spectra");
		neutralPolarityWarningLabel.setForeground(Color.RED);
		neutralPolarityWarningLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_neutralPolarityWarningLabel = new GridBagConstraints();
		gbc_neutralPolarityWarningLabel.anchor = GridBagConstraints.WEST;
		gbc_neutralPolarityWarningLabel.gridwidth = 2;
		gbc_neutralPolarityWarningLabel.insets = new Insets(0, 0, 5, 0);
		gbc_neutralPolarityWarningLabel.gridx = 2;
		gbc_neutralPolarityWarningLabel.gridy = rowCount;
		panel.add(neutralPolarityWarningLabel, gbc_neutralPolarityWarningLabel);
		neutralPolarityWarningLabel.setVisible(false);

		rowCount++;
		
		adductSelectorPanel = new AdductSelectorPanel(true);
		GridBagConstraints gbc_adductSelectorPanel = new GridBagConstraints();
		gbc_adductSelectorPanel.anchor = GridBagConstraints.WEST;
		gbc_adductSelectorPanel.gridwidth = 4;
		gbc_adductSelectorPanel.insets = new Insets(0, 0, 5, 0);
		gbc_adductSelectorPanel.fill = GridBagConstraints.BOTH;
		gbc_adductSelectorPanel.gridx = 0;
		gbc_adductSelectorPanel.gridy = rowCount;
		gbc_adductSelectorPanel.weighty = 1.0d;
		panel.add(adductSelectorPanel, gbc_adductSelectorPanel);
		adductSelectorPanel.addPolarityListener(this);
				
		rowCount++;
			
		clearRtCheckBox = new JCheckBox("Clear retention times");
		GridBagConstraints gbc_clearRtCheckBox = new GridBagConstraints();
		gbc_clearRtCheckBox.anchor = GridBagConstraints.WEST;
		gbc_clearRtCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_clearRtCheckBox.gridx = 1;
		gbc_clearRtCheckBox.gridy = rowCount;
		panel.add(clearRtCheckBox, gbc_clearRtCheckBox);
		
		clearAnnotationsCheckBox = new JCheckBox("Clear annotations");
		GridBagConstraints gbc_clearAnnotationsCheckBox = new GridBagConstraints();
		gbc_clearAnnotationsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_clearAnnotationsCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_clearAnnotationsCheckBox.gridx = 2;
		gbc_clearAnnotationsCheckBox.gridy = rowCount;
		
		panel.add(clearAnnotationsCheckBox, gbc_clearAnnotationsCheckBox);
		
		rowCount++;
				
		spacerLabel = new JLabel("   ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_adductSelectorPanel.insets = new Insets(0, 0, 5, 0);
		gbc_adductSelectorPanel.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = rowCount;
		gbc_lblNewLabel_2.weighty = 1.0d;
		panel.add(spacerLabel, gbc_lblNewLabel_2);
	}
	
	private void configureForNewLibrary(){

		setTitle("Create new library");
		setIconImage(((ImageIcon) newLibraryIcon).getImage());
		currentLibrary = new CompoundLibrary("New library");
		nameTextField.setText(currentLibrary.getLibraryName());
		libraryDescriptionTextArea.setText(currentLibrary.getLibraryDescription());
		saveButton.setActionCommand(MainActionCommands.CREATE_NEW_LIBRARY_COMMAND.getName());
	}
	
	private void configureForDuplication() {
		
		setTitle("Duplicate library \"" + currentLibrary.getLibraryName() + "\"");
		setIconImage(((ImageIcon) duplicateLibraryIcon).getImage());
		
		nameTextField.setText(currentLibrary.getLibraryName() + 
				" Copy-" + MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()));
		libraryDescriptionTextArea.setText(currentLibrary.getLibraryDescription() + 
				"\nCopy-" + MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()));
		adductSelectorPanel.setPolarity(currentLibrary.getPolarity());
		
		boolean isNeutral = currentLibrary.getPolarity().equals(Polarity.Neutral);
		neutralPolarityWarningLabel.setVisible(isNeutral);
		preserveSpectraOnCopyCheckBox.setEnabled(!isNeutral);
		preserveSpectraOnCopyCheckBox.setSelected(!isNeutral);
		if(!isNeutral)
			adductSelectorPanel.setPolarity(currentLibrary.getPolarity());
		
		saveButton.setActionCommand(MainActionCommands.DUPLICATE_LIBRARY_COMMAND.getName());
	}
	
	private void configureForEditInfo() {
		
		setTitle("Edit details for library \"" + currentLibrary.getLibraryName() + "\"");
		setIconImage(((ImageIcon) editLibInfoIcon).getImage());

		nameTextField.setText(currentLibrary.getLibraryName());
		libraryDescriptionTextArea.setText(currentLibrary.getLibraryDescription());
		saveButton.setActionCommand(MainActionCommands.EDIT_MS_LIBRARY_INFO_COMMAND.getName());
	}
	
	public CompoundLibrary getLibrary(){
		return currentLibrary;
	}

	public String getLibraryDescription(){
		return libraryDescriptionTextArea.getText().trim();
	}

	public String getLibraryName(){
		return nameTextField.getText().trim();
	}
	
	public Polarity getPolarity() {
		return adductSelectorPanel.getPolarity();
	}
	
	public AdductSubset getAdductSubset() {
		return adductSelectorPanel.getAdductSubset();
	}

	public Collection<String>validateLibraryData(){ 
		
		Collection<String>errors = new ArrayList<String>();
		String name = getLibraryName();
		if(name == null || name.isEmpty()) {
			errors.add("Library name cannot be empty");
		}
		else {
			//	Check for name conflict
			Collection<CompoundLibrary> libList = 
					IDTDataCache.getMsRtLibraryList();			
			for (CompoundLibrary l : libList) {
				
				if(l.getLibraryName().equals(name) 
						&& !l.getLibraryId().equals(currentLibrary.getLibraryId())) {
					errors.add("A different library with name \"" + name + "\" already exists");
				}				
			}
		}
		if(!saveButton.getActionCommand().equals(
				MainActionCommands.DUPLICATE_LIBRARY_COMMAND.getName())				
				&& getPolarity() == null) {
			errors.add("Library polarity must be specified");
		}
		return errors;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if(e.getSource().equals(preserveSpectraOnCopyCheckBox))			
			toggleAdductSelector(!preserveSpectraOnCopyCheckBox.isSelected());
		
		if(e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Polarity) {
				
			boolean isNeutral = getPolarity().equals(Polarity.Neutral);
			if(preserveSpectraOnCopyCheckBox.isSelected() 
					&& (isNeutral || !getPolarity().equals(currentLibrary.getPolarity()))) {
				preserveSpectraOnCopyCheckBox.setSelected(false);					
			}
			preserveSpectraOnCopyCheckBox.setEnabled(
					!(isNeutral || !getPolarity().equals(currentLibrary.getPolarity())));

			neutralPolarityWarningLabel.setVisible(isNeutral);
			
			if(isNeutral)
				adductSelectorPanel.clearAdductList();
		}		
	}
	
	private void toggleAdductSelector(boolean enabled) {
		
		adductSelectorPanel.setVisible(enabled);
		createDefaultAdductsLabel.setVisible(enabled);
		spacerLabel.setVisible(!enabled);
		revalidate();
		repaint();
	}

	public Collection<Adduct>getSelectedAdducts(){
		return adductSelectorPanel.getSelectedAdducts();
	}
	
	public boolean clearRetention() {
		return clearRtCheckBox.isSelected();
	}
	
	public boolean clearAnnotations() {
		return clearAnnotationsCheckBox.isSelected();
	}
	
	public boolean preserveSpectraOnCopy() {
		
		if(preserveSpectraOnCopyCheckBox.isVisible() 
				&& preserveSpectraOnCopyCheckBox.isEnabled())		
			return preserveSpectraOnCopyCheckBox.isSelected();
		else
			return false;
	}	
}

















