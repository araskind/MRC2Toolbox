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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.adducts.adduct.CompositeAdductComponentsTable;
import edu.umich.med.mrc2.datoolbox.gui.adducts.adduct.CompositeAdductDataEditorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class BinnerNeutralMassDifferenceManagerDialog extends JDialog 
		implements ActionListener, ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4708322719602052647L;

	private static final Icon dialogIcon = GuiUtils.getIcon("addBinnerAdductFromMassDiff", 32);
	
	private BinnerNeutralMassDifferenceEditorDialog binnerNeutralMassDifferenceEditorDialog;
	private BinnerMassDifferenceEditorToolbar toolbar;
	private BinnerNeutralMassDifferenceTable massDifferenceTable;
	private CompositeAdductComponentsTable compositAdductComponentsTable;
	private CompositeAdductDataEditorDialog compositeAdductDataEditorDialog;
	
	public BinnerNeutralMassDifferenceManagerDialog() {
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Binner mass difference manager");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(640, 480));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new BorderLayout(0,0));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		toolbar = new BinnerMassDifferenceEditorToolbar(this);
		mainPanel.add(toolbar, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		mainPanel.add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		massDifferenceTable = new BinnerNeutralMassDifferenceTable();
		massDifferenceTable.setTableModelFromBinnerNeutralMassDifferenceList(
				AdductManager.getBinnerNeutralMassDifferenceList());
		massDifferenceTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane scrollPane = new JScrollPane(massDifferenceTable);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel.add(scrollPane, gbc_scrollPane);
				
		compositAdductComponentsTable = new CompositeAdductComponentsTable();
		JScrollPane scrollPane_1 = new JScrollPane(compositAdductComponentsTable);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		panel.add(scrollPane_1, gbc_scrollPane_1);
		
		pack();
	}

	public BinnerNeutralMassDifference getSelectedMassDifference() {		
		return massDifferenceTable.getSelectedMassDifference();
	}

	public void selectMassDifference(BinnerNeutralMassDifference binnerNeutralMassDifference) {
		massDifferenceTable.selectMassDifference(binnerNeutralMassDifference);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.NEW_BINNER_MASS_DIFFERENCE_COMMAND.getName())) 
			editMassDifference(null);
		
		if(command.equals(MainActionCommands.EDIT_BINNER_MASS_DIFFERENCE_COMMAND.getName())) {
			
			BinnerNeutralMassDifference mdiff = massDifferenceTable.getSelectedMassDifference();
			if(mdiff == null)
				return;
			
			editMassDifference(mdiff);
		}
		if(command.equals(MainActionCommands.DELETE_BINNER_MASS_DIFFERENCE_COMMAND.getName())) 
			deleteSelectedMassDifference();
		
		if(command.equals(MainActionCommands.SAVE_BINNER_MASS_DIFFERENCE_COMMAND.getName())) 
			saveMassDifference();
		
		if(command.equals(MainActionCommands.NEW_COMPOSITE_MODIFICATION_FROM_MASS_DIFF_COMMAND.getName())) 
			createCompositeModificationFromSelectedMassDifference();

		if(command.equals(MainActionCommands.SAVE_COMPOSITE_MODIFICATION_DATA_COMMAND.getName())) 
			saveCompositeModification();	
	}
	
	private void createCompositeModificationFromSelectedMassDifference() {
		
		BinnerNeutralMassDifference mdiff = massDifferenceTable.getSelectedMassDifference();
		if(mdiff == null)
			return;
		
		compositeAdductDataEditorDialog = new CompositeAdductDataEditorDialog(this);
		compositeAdductDataEditorDialog.loadBinnerMassDiffData(mdiff);
		compositeAdductDataEditorDialog.setLocationRelativeTo(this);
		compositeAdductDataEditorDialog.setVisible(true);
	}
	
	private void saveCompositeModification() {
				
		Collection<String> errors = compositeAdductDataEditorDialog.validateModification();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), compositeAdductDataEditorDialog);
			return;
		}
		CompositeAdduct edited = compositeAdductDataEditorDialog.getEditedCompositeAdduct();
		AdductManager.addAdduct(edited);
		MainWindow.getAdductManagerFrame().refreshAdductList();
		compositeAdductDataEditorDialog.dispose();
	}

	private void editMassDifference(BinnerNeutralMassDifference massDiff) {
		
		binnerNeutralMassDifferenceEditorDialog = 
				new BinnerNeutralMassDifferenceEditorDialog(massDiff, this);
		binnerNeutralMassDifferenceEditorDialog.setLocationRelativeTo(this);
		binnerNeutralMassDifferenceEditorDialog.setVisible(true);
	}
	
	private void deleteSelectedMassDifference() {
		
		BinnerNeutralMassDifference mdiff = massDifferenceTable.getSelectedMassDifference();
		if(mdiff == null)
			return;
		
		if(!IDTUtils.isSuperUser(this))
			return;		
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you wasnt to delete selected mass difference?", this);
		if(res == JOptionPane.YES_OPTION) {
			try {
				BinnerUtils.deleteBinnerNeutralMassDifference(mdiff);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			AdductManager.getBinnerNeutralMassDifferenceList().remove(mdiff);
			massDifferenceTable.setTableModelFromBinnerNeutralMassDifferenceList(
					AdductManager.getBinnerNeutralMassDifferenceList());
		}
	}
	
	private void saveMassDifference() {
		
		Collection<String>errors = 
				binnerNeutralMassDifferenceEditorDialog.validateModification();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), binnerNeutralMassDifferenceEditorDialog);
			return;
		}
		BinnerNeutralMassDifference massDiff = 
				binnerNeutralMassDifferenceEditorDialog.getBinnerNeutralMassDifference();
		if(massDiff == null) {
			
			massDiff = 
					binnerNeutralMassDifferenceEditorDialog.getEditedBinnerNeutralMassDifference();
			try {
				BinnerUtils.addNewBinnerNeutralMassDifference(massDiff);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BinnerUtils.addNewBinnerNeutralMassDifferenceAsAnnotation(massDiff);
			AdductManager.getBinnerNeutralMassDifferenceList().add(massDiff);
			massDifferenceTable.setTableModelFromBinnerNeutralMassDifferenceList(
					AdductManager.getBinnerNeutralMassDifferenceList());
			massDifferenceTable.selectMassDifference(massDiff);
			DockableBinnerAnnotationsEditor.refreshBinnerAdductList();
		}
		else {
			BinnerNeutralMassDifference editedMassDiff = 
					binnerNeutralMassDifferenceEditorDialog.getEditedBinnerNeutralMassDifference();
			editedMassDiff.setId(massDiff.getId());
			try {
				BinnerUtils.editBinnerNeutralMassDifference(editedMassDiff);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			AdductManager.refreshBinnerNeutralMassDifference();
			massDifferenceTable.setTableModelFromBinnerNeutralMassDifferenceList(
					AdductManager.getBinnerNeutralMassDifferenceList());
			massDifferenceTable.selectMassDifference(editedMassDiff);
			DockableBinnerAnnotationsEditor.refreshBinnerAdductList();
		}		
		binnerNeutralMassDifferenceEditorDialog.dispose();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {
			
			BinnerNeutralMassDifference massDiff = massDifferenceTable.getSelectedMassDifference();
			compositAdductComponentsTable.setTableModelFromBinnerNeutralMassDifference(massDiff);
		}
	}

}
