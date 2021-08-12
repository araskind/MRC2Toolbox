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

package edu.umich.med.mrc2.datoolbox.gui.adducts.chemmod;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.io.ChemicalModificationsParser;
import edu.umich.med.mrc2.datoolbox.main.ChemicalModificationsManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class DockableChemModEditor extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("chemModList", 16);
	private ChemModificationTable adductTable;
	private JScrollPane adductScrollPane;
	private ChemModEditorToolbar toolBar;
	private ModificationDataEditorDialog modEditor;
	private Adduct activeModification;
	private File baseDirectory;

	public DockableChemModEditor() {

		super("DockableChemModEditor", componentIcon, "Chemical modifications manager", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		toolBar = new ChemModEditorToolbar(this);
		add(toolBar, BorderLayout.NORTH);

		adductTable = new ChemModificationTable();
		adductScrollPane = new JScrollPane(adductTable);
		add(adductScrollPane, BorderLayout.CENTER);
		modEditor = new ModificationDataEditorDialog(this);
		activeModification = null;

		baseDirectory = new File(MRC2ToolBoxCore.referenceDir);

		loadModifications();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.NEW_MODIFICATION_COMMAND.getName()))
			initNewModificationEditor();

		if (command.equals(MainActionCommands.EDIT_MODIFICATION_COMMAND.getName()))
			initModificationEditor();

		if (command.equals(MainActionCommands.DELETE_MODIFICATION_COMMAND.getName()))
			deleteSelectedModification();

		if (command.equals(MainActionCommands.IMPORT_MODIFICATIONS_COMMAND.getName()))
			importModificationsFromFile();

		if (command.equals(MainActionCommands.EXPORT_MODIFICATIONS_COMMAND.getName()))
			exportModificationsToFile();

		if (command.equals(MainActionCommands.SAVE_SIMPLE_MODIFICATION_DATA_COMMAND.getName())) {

			// Save new
			if (activeModification == null)
				createNewModification();
			else // Edit existing
				editSelectedModification();
		}
	}

	private void initNewModificationEditor() {

		activeModification = null;
		modEditor.setTitle("Create new modification");
		modEditor.setIconImage(((ImageIcon) ModificationDataEditorDialog.newIcon).getImage());
		modEditor.clear();
		modEditor.setLocationRelativeTo(this.getContentPane());
		modEditor.setVisible(true);
	}

	private void initModificationEditor() {
		
		Adduct mod = adductTable.getSelectedModification();
		if(mod == null)
			return;

		activeModification = mod;
		modEditor.setTitle("Edit " + activeModification.getName());
		modEditor.setIconImage(((ImageIcon) ModificationDataEditorDialog.editIcon).getImage());
		modEditor.loadModificationData(activeModification);
		modEditor.setLocationRelativeTo(this.getContentPane());
		modEditor.setVisible(true);		
	}

	private void createNewModification() {

		boolean dataValid = validateModificationData();

		if (dataValid) {

			Adduct newModification =
					new SimpleAdduct(
							null,	//	TODO temporary fix
							modEditor.getModName(),
							modEditor.getModDescription(),
							modEditor.getCharge(),
							modEditor.getOligomericState(),
							0.0d,
							modEditor.getModType());

			newModification.setAddedGroup(modEditor.getAddedGroup());
			newModification.setRemovedGroup(modEditor.getRemovedGroup());
			newModification.setCefNotation(modEditor.getCefNotation());

			if (!modEditor.getModType().equals(ModificationType.ADDUCT)) {

				double massCorr = 
						MsUtils.calculateMassCorrectionFromAddedRemovedGroups(newModification);
				newModification.setMassCorrection(massCorr);
			}
			try {
				ChemicalModificationsManager.addChemicalModification(newModification);
				adductTable.setTableModelFromAdductList(ChemicalModificationsManager.getAllModifications());
			} catch (Exception e) {
				e.printStackTrace();
			}
			activeModification = null;
			modEditor.setVisible(false);
		}
	}

	private void deleteSelectedModification() {

		if (adductTable.getSelectedRow() > -1) {

			ChemModificationTableModel model = (ChemModificationTableModel) adductTable.getModel();
			int column = model.getColumnIndex(ChemModificationTableModel.CHEM_MOD_COLUMN);
			int row = adductTable.convertRowIndexToModel(adductTable.getSelectedRow());
			activeModification = (Adduct) model.getValueAt(row, column);

			int approve = MessageDialog.showChoiceWithWarningMsg(
					"Delete selected modification?\n(NO UNDO!)",
					this.getContentPane());

			if (approve == JOptionPane.YES_OPTION) {

				// Delete from core object and database
				ChemicalModificationsManager.removeChemicalModification(activeModification);

				// Remove table row
				model.removeRow(row);
			}
		}
	}

	private void editSelectedModification() {

		boolean dataValid = validateModificationData();

		if (dataValid) {

			String originalName = activeModification.getName();

			activeModification.setName(modEditor.getModName());
			activeModification.setCefNotation(modEditor.getCefNotation());
			activeModification.setDescription(modEditor.getModDescription());
			activeModification.setAddedGroup(modEditor.getAddedGroup());
			activeModification.setRemovedGroup(modEditor.getRemovedGroup());
			activeModification.setCharge(modEditor.getCharge());
			activeModification.setOligomericState(modEditor.getOligomericState());
			activeModification.setModificationType(modEditor.getModType());

			if (!modEditor.getModType().equals(ModificationType.ADDUCT)) {

				double massCorr = 
						MsUtils.calculateMassCorrectionFromAddedRemovedGroups(activeModification);
				activeModification.setMassCorrection(massCorr);
			}
			try {
				ChemicalModificationsManager.updateChemicalModification(originalName, activeModification);
				adductTable.setTableModelFromAdductList(ChemicalModificationsManager.getAllModifications());
			} catch (Exception e) {
				e.printStackTrace();
			}
			activeModification = null;
			modEditor.setVisible(false);
		}
	}

	private void exportModificationsToFile() {

		JFileChooser chooser = new ImprovedFileChooser();
		File outputFile = null;

		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Save modifications to file:");
		chooser.setApproveButtonText("Save modifications");

		chooser.setCurrentDirectory(baseDirectory);

		if (chooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {

			outputFile = chooser.getSelectedFile();

			String filePath = outputFile.getAbsolutePath();

			if (!filePath.endsWith(".txt") && !filePath.endsWith(".TXT"))
				outputFile = new File(filePath + ".txt");

			try {
				ChemicalModificationsParser.writeChemicalModificationsToFile(outputFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void importModificationsFromFile() {
		// TODO Auto-generated method stub

	}

	public void loadModifications() {

		adductTable.setTableModelFromAdductList(ChemicalModificationsManager.getAllModifications());
		adductScrollPane.setPreferredSize(adductTable.getPreferredScrollableViewportSize());
	}

	// Validate new/edited modification data
	private boolean validateModificationData() {

		boolean valid = true;
		ArrayList<String> messages = new ArrayList<String>();

		IMolecularFormula addedFormula = null;
		IMolecularFormula lostFormula = null;

		// Test name
		if (modEditor.getModName().isEmpty()) {

			messages.add("Please specify a name for modification!");
			valid = false;
		}
		// Test added group
		if (!modEditor.getAddedGroup().isEmpty()) {

			try {
				addedFormula = MolecularFormulaManipulator.getMolecularFormula(modEditor.getAddedGroup(),
						DefaultChemObjectBuilder.getInstance());
				MolecularFormulaManipulator.getMajorIsotopeMass(addedFormula);
			} catch (Exception e) {

				messages.add("Added group formula invalid!");
				valid = false;
				// e.printStackTrace();
			}
		}
		// Test removed group
		if (!modEditor.getRemovedGroup().isEmpty()) {

			try {
				lostFormula = MolecularFormulaManipulator.getMolecularFormula(modEditor.getRemovedGroup(),
						DefaultChemObjectBuilder.getInstance());
				MolecularFormulaManipulator.getMajorIsotopeMass(lostFormula);
			} catch (Exception e) {

				messages.add("Removed group formula invalid!");
				valid = false;
				// e.printStackTrace();
			}
		}
		// Test if name already exists
		for (Adduct m : ChemicalModificationsManager.getAllModifications()) {

			if (m.getName().equals(modEditor.getModName()) && activeModification == null) {

				messages.add("Modification with this name already exists, please choose a different name!");
				valid = false;
			}
		}
		// Modification type
		if (modEditor.getModType() == null) {

			messages.add("Please select the type of modification!");
			valid = false;
		}
		if (!valid) {

			String errors = StringUtils.join(messages, "\n");
			MessageDialog.showWarningMsg(errors, modEditor);
		}
		return valid;
	}

	public ChemModificationTable getAdductTable() {
		return adductTable;
	}
}























