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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.io.ChemicalModificationsParser;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class DockableAdductEditor extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("chemModList", 16);
	private AdductTable adductTable;
	private JScrollPane adductScrollPane;
	private AdductEditorToolbar toolBar;
	private SimpleModificationDataEditorDialog simpleModificationEditor;
	private CompositeAdductDataEditorDialog compositeAdductEditor;
	private Adduct activeAdduct;
	private File baseDirectory;

	public DockableAdductEditor() {

		super("DockableAdductEditor", componentIcon, "Adduct/loss manager", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		toolBar = new AdductEditorToolbar(this);
		add(toolBar, BorderLayout.NORTH);

		adductTable = new AdductTable();
		adductTable.addTablePopupMenu(new AdductTablePopupMenu(this, adductTable));
		adductScrollPane = new JScrollPane(adductTable);
		add(adductScrollPane, BorderLayout.CENTER);
		activeAdduct = null;

		baseDirectory = new File(MRC2ToolBoxCore.referenceDir);
		adductTable.setTableModelFromAdductList(AdductManager.getAdductList());
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.NEW_MODIFICATION_COMMAND.getName()))
			initModificationEditor(null, true);
		
		if (command.equals(MainActionCommands.NEW_COMPOSITE_MODIFICATION_COMMAND.getName()))
			initModificationEditor(null, false);

		if (command.equals(MainActionCommands.EDIT_MODIFICATION_COMMAND.getName())) {
			
			Adduct mod = adductTable.getSelectedModification();
			if(mod == null)
				return;
			
			boolean isSimple = !mod.getModificationType().equals(ModificationType.COMPOSITE);		
			initModificationEditor(mod, isSimple);
		}
		if (command.equals(MainActionCommands.DELETE_MODIFICATION_COMMAND.getName()))
			deleteSelectedModification();

		if (command.equals(MainActionCommands.IMPORT_MODIFICATIONS_COMMAND.getName()))
			importModificationsFromFile();

		if (command.equals(MainActionCommands.EXPORT_MODIFICATIONS_COMMAND.getName()))
			exportModificationsToFile();

		if (command.equals(MainActionCommands.SAVE_SIMPLE_MODIFICATION_DATA_COMMAND.getName()))
			saveSimpleModification();

		if (command.equals(MainActionCommands.SAVE_COMPOSITE_MODIFICATION_DATA_COMMAND.getName()))
			saveCompositeModification();	
	}
	
	private void initModificationEditor(Adduct mod, boolean simple) {
		
		if(mod == null) {
			
			activeAdduct = null;
			if(simple) {				
				simpleModificationEditor = new SimpleModificationDataEditorDialog(this);
				simpleModificationEditor.setTitle("Create new modification");
				simpleModificationEditor.setIconImage(((ImageIcon) SimpleModificationDataEditorDialog.newIcon).getImage());
				simpleModificationEditor.clearPanel();
				simpleModificationEditor.setLocationRelativeTo(this.getContentPane());
				simpleModificationEditor.setVisible(true);
			}
			else {
				compositeAdductEditor = new CompositeAdductDataEditorDialog(this);
				compositeAdductEditor.setTitle("Create new composite modification");
				compositeAdductEditor.setIconImage(((ImageIcon) CompositeAdductDataEditorDialog.newIcon).getImage());
				compositeAdductEditor.clearPanel();
				compositeAdductEditor.setLocationRelativeTo(this.getContentPane());
				compositeAdductEditor.setVisible(true);
			}
		}
		else {
			activeAdduct = mod;
			if(simple) {				
				simpleModificationEditor = new SimpleModificationDataEditorDialog(this);
				simpleModificationEditor.setTitle("Edit " + activeAdduct.getName());
				simpleModificationEditor.setIconImage(((ImageIcon) SimpleModificationDataEditorDialog.editIcon).getImage());
				simpleModificationEditor.loadModificationData((SimpleAdduct) activeAdduct);
				simpleModificationEditor.setLocationRelativeTo(this.getContentPane());
				simpleModificationEditor.setVisible(true);	
			}
			else {
				compositeAdductEditor = new CompositeAdductDataEditorDialog(this);
				compositeAdductEditor.setTitle("Edit " + activeAdduct.getName());
				compositeAdductEditor.setIconImage(((ImageIcon) CompositeAdductDataEditorDialog.editIcon).getImage());
				compositeAdductEditor.loadCompositeAdduct((CompositeAdduct) activeAdduct);
				compositeAdductEditor.setLocationRelativeTo(this.getContentPane());
				compositeAdductEditor.setVisible(true);
			}
		}
	}
	
	public void refreshAdductList() {
		adductTable.setTableModelFromAdductList(AdductManager.getAdductList());
	}
	
	private void saveSimpleModification() {
		
		Collection<String> errors = simpleModificationEditor.validateModification();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), simpleModificationEditor);
			return;
		}
		SimpleAdduct current = simpleModificationEditor.getActiveModification();
		SimpleAdduct edited = simpleModificationEditor.getEditedModification();
		if(current == null)
			AdductManager.addAdduct(edited);
		else {

			edited.setId(current.getId());
			AdductManager.updateAdduct(edited);
		}
		adductTable.setTableModelFromAdductList(AdductManager.getAdductList());
		adductTable.selectAdduct(edited);
		simpleModificationEditor.dispose();
	}
	
	private void saveCompositeModification() {
		
		CompositeAdduct current = compositeAdductEditor.getCompositeAdduct();
		CompositeAdduct edited = compositeAdductEditor.getEditedCompositeAdduct();
		
		//	If no change
//		if(current != null && current.equals(edited) 
//				&& ((current.getDescription() == null && edited.getDescription() == null)
//						) || (current.getDescription() != null && edited.getDescription() != null)
//				&& current.getDescription().equals(edited.getDescription())) {
//			compositeAdductEditor.dispose();
//			return;
//		}
		Collection<String> errors = compositeAdductEditor.validateModification();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), compositeAdductEditor);
			return;
		}
		if(current == null)
			AdductManager.addAdduct(edited);
		else {
			edited.setId(current.getId());
			AdductManager.updateAdduct(edited);
		}
		adductTable.setTableModelFromAdductList(AdductManager.getAdductList());
		adductTable.selectAdduct(edited);
		compositeAdductEditor.dispose();
	}

	private void deleteSelectedModification() {
		
		Adduct toDelete = adductTable.getSelectedModification();
		if(toDelete == null)
			return;

		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;
		
		int approve = MessageDialog.showChoiceWithWarningMsg(
				"Delete " + toDelete.getName() + "? (NO UNDO!)",
				this.getContentPane());

		if (approve == JOptionPane.YES_OPTION) {

			AdductManager.deleteAdduct(toDelete);
			adductTable.removeAdduct(toDelete);
		}		
	}

	private void exportModificationsToFile() {

//		JFileChooser chooser = new ImprovedFileChooser();
//		File outputFile = null;
//
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setDialogTitle("Save modifications to file:");
//		chooser.setApproveButtonText("Save modifications");
//		chooser.setCurrentDirectory(baseDirectory);
//		if (chooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
//
//			outputFile = chooser.getSelectedFile();
//			FIOUtils.changeExtension(outputFile, "TXT");
//			try {
//				ChemicalModificationsParser.writeChemicalModificationsToFile(outputFile);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Save adducts/modifications to text file");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = "Chemical_modification_list_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".TXT";
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File outputFile = fc.getSelectedFile();
			FIOUtils.changeExtension(outputFile, "TXT");
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

	public AdductTable getAdductTable() {
		return adductTable;
	}
}























