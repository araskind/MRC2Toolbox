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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.lib;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.IDTrackerDataSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class LibrarySearchPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8478606453844090542L;
	private MSMSLibraryListingTable msmsLibraryListingTable;
	private JTextField originalLibIdTextField;
	private JTextField mrc2libIdTextField;
	private JCheckBox searchAllMatchesCheckBox;

	public LibrarySearchPanel() {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Original library ID ");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		originalLibIdTextField = new JTextField();
		GridBagConstraints gbc_originalLibIdTextField = new GridBagConstraints();
		gbc_originalLibIdTextField.gridwidth = 2;
		gbc_originalLibIdTextField.insets = new Insets(0, 0, 5, 0);
		gbc_originalLibIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_originalLibIdTextField.gridx = 1;
		gbc_originalLibIdTextField.gridy = 0;
		add(originalLibIdTextField, gbc_originalLibIdTextField);
		originalLibIdTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("MRC2 library ID");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		mrc2libIdTextField = new JTextField();
		GridBagConstraints gbc_mrc2libIdTextField = new GridBagConstraints();
		gbc_mrc2libIdTextField.gridwidth = 2;
		gbc_mrc2libIdTextField.insets = new Insets(0, 0, 5, 0);
		gbc_mrc2libIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mrc2libIdTextField.gridx = 1;
		gbc_mrc2libIdTextField.gridy = 1;
		add(mrc2libIdTextField, gbc_mrc2libIdTextField);
		mrc2libIdTextField.setColumns(10);
		
		msmsLibraryListingTable = new MSMSLibraryListingTable();
		msmsLibraryListingTable.setTableModelFromReferenceMsMsLibraryList(
				IDTDataCash.getPrimaryReferenceMsMsLibraryList());
		JScrollPane scrollPane = new JScrollPane(msmsLibraryListingTable);
		scrollPane.setBorder(
				new TitledBorder(null, "Library matches should come only from selected libraries:", 
						TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 12), null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		add(scrollPane, gbc_scrollPane);
		
		searchAllMatchesCheckBox = new JCheckBox("Search all matches");
		searchAllMatchesCheckBox.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 3;
		add(searchAllMatchesCheckBox, gbc_chckbxNewCheckBox);
		
		JLabel lblNewLabel_2 = new JLabel("If not checked only default match will be considered");
		lblNewLabel_2.setForeground(Color.RED);
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.gridwidth = 2;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 3;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JButton resetButton = new JButton(
				"Reset to default values", IDTrackerDataSearchDialog.resetIcon);
		resetButton.setActionCommand(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName());
		resetButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnNewButton.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 4;
		add(resetButton, gbc_btnNewButton);		
	}

	public void resetPanel() {
		
		originalLibIdTextField.setText("");
		mrc2libIdTextField.setText("");
		msmsLibraryListingTable.clearSelection();
		searchAllMatchesCheckBox.setSelected(false);
	}
	
	public String getOriginalLibraryId() {
		return originalLibIdTextField.getText().trim();
	}
	
	public void setOriginalLibraryId(String id) {
		originalLibIdTextField.setText(id);
	}
	
	public String getMRC2LibraryId() {
		return mrc2libIdTextField.getText().trim();
	}
	
	public void setMRC2LibraryId(String id) {
		mrc2libIdTextField.setText(id);
	}
	
	public boolean searchAllLibraryMatches() {
		return searchAllMatchesCheckBox.isSelected();
	}
	
	public void setSearchAllLibraryMatches(boolean b) {
		searchAllMatchesCheckBox.setSelected(b);
	}
	
	public Collection<ReferenceMsMsLibrary>getSelectedLibraries() {
		return msmsLibraryListingTable.getSelectedLibraries();
	}
	
	public void selectLibraries(Collection<ReferenceMsMsLibrary>libList) {
		msmsLibraryListingTable.selectLibraries(libList);
		if(msmsLibraryListingTable.getSelectedRow() > -1)
			msmsLibraryListingTable.scrollToSelected();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName()))
			resetPanel();
	}
	
	public boolean hasLimitingInput() {
		
		if(!getOriginalLibraryId().isEmpty() || !getMRC2LibraryId().isEmpty())			
			return true;		
		else
			return false;
	}

	public Collection<String> validateInput() {
		
		Collection<String>errors = new ArrayList<String>();
		if(!getMRC2LibraryId().isEmpty()) {
			Pattern sp = Pattern.compile(DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}");
			Matcher regexMatcher = sp.matcher(getMRC2LibraryId());
			if(!regexMatcher.find())
				errors.add("Invalid MRC2 MSMS library ID on \"MSMS library\" panel."
						+ "\n The format is " + 
						DataPrefix.MSMS_LIBRARY_ENTRY.getName() + " followed by 9 digits");				
		}	
		return errors;
	}
}










