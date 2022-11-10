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

package edu.umich.med.mrc2.datoolbox.gui.adducts.bindif;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerAnnotationBase;
import edu.umich.med.mrc2.datoolbox.data.enums.BinnerAnnotationFileField;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class DockableBinnerAnnotationsEditor 
	extends DefaultSingleCDockable implements ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("editBinnerAdduct", 16);
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.gui.DockableBinnerAnnotationsEditor";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	
	private BinnerAnnotationsTable binnerAnnotationsTable;
	private JScrollPane adductScrollPane;
	private BinnerAnnotationsEditorToolbar toolBar;
	private BinnerNeutralMassDifferenceEditorDialog binnerNeutralMassDifferenceEditorDialog;
	private BinnerAdduct activeAdduct;
	private File baseDirectory;
	private BinnerAnnotationEditorDialog binnerAnnotationEditorDialog;
	private BinnerNeutralMassDifferenceManagerDialog binnerNeutralMassDifferenceManagerDialog;
	
	public DockableBinnerAnnotationsEditor() {

		super("DockableBinnerAnnotationsEditor", componentIcon, "Binner annotations manager", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		toolBar = new BinnerAnnotationsEditorToolbar(this);
		add(toolBar, BorderLayout.NORTH);

		binnerAnnotationsTable = new BinnerAnnotationsTable();
		adductScrollPane = new JScrollPane(binnerAnnotationsTable);
		add(adductScrollPane, BorderLayout.CENTER);
		activeAdduct = null;

		baseDirectory = new File(MRC2ToolBoxCore.referenceDir);
		binnerAnnotationsTable.setTableModelFromBinnerAdducttList(
				AdductManager.getBinnerAdductList());
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if (command.equals(MainActionCommands.REFRESH_BINNER_ADDUCT_LIST_COMMAND.getName()))
			refreshBinnerAdductList();

		if (command.equals(MainActionCommands.NEW_BINNER_ADDUCT_COMMAND.getName()))
			initNewModificationEditor(BinnerAnnotationBase.ADDUCT);
		
		if (command.equals(MainActionCommands.NEW_BINNER_ADDUCT_FROM_EXCHANGE_COMMAND.getName()))
			initNewModificationEditor(BinnerAnnotationBase.EXCHANGE);
		
		if (command.equals(MainActionCommands.NEW_BINNER_ADDUCT_FROM_MASS_DIFF_COMMAND.getName()))
			initNewModificationEditor(BinnerAnnotationBase.MASS_DIFFERENCE);
		
		if (command.equals(MainActionCommands.EDIT_BINNER_ADDUCT_COMMAND.getName()))
			editSelectedBinnerAdduct();

		if (command.equals(MainActionCommands.DELETE_BINNER_ADDUCT_COMMAND.getName()))
			deleteSelectedBinnerAdduct();
		
		if (command.equals(MainActionCommands.SAVE_BINNER_ADDUCT_COMMAND.getName()))
			saveBinnerAdduct();

		if (command.equals(MainActionCommands.SHOW_BINNER_MASS_DIFFERENCE_MANAGER_COMMAND.getName()))
			showBinnerMassDifferenceManager();
				
		if (command.equals(MainActionCommands.EXPORT_BINNER_ADDUCTS_COMMAND.getName()))
			exportBinnerAnnotationsFile();	
	}
	
	private void editSelectedBinnerAdduct() {
		
		BinnerAdduct binAdd = binnerAnnotationsTable.getSelectedBinnerAdduct();
		if (binAdd == null)
			return;

		BinnerAnnotationBase annotationBase = null;
		if (binAdd.getChargeCarrier() != null)
			annotationBase = BinnerAnnotationBase.ADDUCT;

		if (binAdd.getAdductExchange() != null)
			annotationBase = BinnerAnnotationBase.EXCHANGE;

		if (binAdd.getBinnerNeutralMassDifference() != null)
			annotationBase = BinnerAnnotationBase.MASS_DIFFERENCE;

		binnerAnnotationEditorDialog = new BinnerAnnotationEditorDialog(this, binAdd, annotationBase);
		binnerAnnotationEditorDialog.setLocationRelativeTo(this.getContentPane());
		binnerAnnotationEditorDialog.setVisible(true);
	}
	
	private void initNewModificationEditor(BinnerAnnotationBase base) {
		
		String noDataMessage = null;
		if(base.equals(BinnerAnnotationBase.ADDUCT)) {
			
			Set<Adduct> usedAdducts = AdductManager.getBinnerAdductList().stream().
					filter(a -> Objects.nonNull(a.getChargeCarrier())).
					map(a -> a.getChargeCarrier()).distinct().
					collect(Collectors.toSet());
			List<Adduct> availableAdducts = AdductManager.getAdductList().stream().
					filter(a -> !usedAdducts.contains(a)).
					sorted(AdductManager.adductTypeNameSorter).collect(Collectors.toList());
			
			if(availableAdducts.isEmpty())
				noDataMessage = "All defined charge carriers already assigned to Binner annotations.";
		}
		if(base.equals(BinnerAnnotationBase.EXCHANGE)) {
			
			Set<AdductExchange> usedAdductExchanges = 
					AdductManager.getBinnerAdductList().stream().
					filter(a -> Objects.nonNull(a.getAdductExchange())).
					map(a -> a.getAdductExchange()).distinct().
					collect(Collectors.toSet());
			List<AdductExchange> availableAdductExchanges = 
					AdductManager.getAdductExchangeList().stream().
					filter(a -> !usedAdductExchanges.contains(a)).
					sorted().collect(Collectors.toList());
			
			if(availableAdductExchanges.isEmpty())
				noDataMessage = "All defined adduct exchanges already assigned to Binner annotations.";
		}
		if(base.equals(BinnerAnnotationBase.MASS_DIFFERENCE)) {
			
			Set<BinnerNeutralMassDifference> usedBinnerNeutralMassDifferences = 
					AdductManager.getBinnerAdductList().stream().
					filter(a -> Objects.nonNull(a.getBinnerNeutralMassDifference())).
					map(a -> a.getBinnerNeutralMassDifference()).distinct().
					collect(Collectors.toSet());
			List<BinnerNeutralMassDifference> availableBinnerNeutralMassDifferences = 
					AdductManager.getBinnerNeutralMassDifferenceList().stream().
					filter(a -> !usedBinnerNeutralMassDifferences.contains(a)).
					sorted().collect(Collectors.toList());
			
			if(availableBinnerNeutralMassDifferences.isEmpty())
				noDataMessage = "All defined mass differences already assigned to Binner annotations.";
		}
		if(noDataMessage != null) {
			MessageDialog.showWarningMsg(noDataMessage, this.getContentPane());
			return;
		}
		binnerAnnotationEditorDialog = new BinnerAnnotationEditorDialog(this, null, base);
		binnerAnnotationEditorDialog.setLocationRelativeTo(this.getContentPane());
		binnerAnnotationEditorDialog.setVisible(true);
	}
	
	private void saveBinnerAdduct() {
		
		Collection<String> errors = binnerAnnotationEditorDialog.validateBinnerAdduct();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), binnerAnnotationEditorDialog);
			return;
		}
		BinnerAdduct activeAdduct = binnerAnnotationEditorDialog.getBinAdduct();
		BinnerAdduct editedAdduct = binnerAnnotationEditorDialog.getEditedBinAdduct();
		if(activeAdduct  == null) {	//	Add new Binner annotation
			
			try {
				BinnerUtils.addNewBinnerAdduct(editedAdduct);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			AdductManager.refreshBinnerAdductList();
			binnerAnnotationsTable.setTableModelFromBinnerAdducttList(AdductManager.getBinnerAdductList());
			binnerAnnotationsTable.selectBinnerAdduct(editedAdduct);
		}
		else {	//	Edit existing Binner annotation
			try {
				BinnerUtils.editBinnerAdduct(activeAdduct);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			AdductManager.refreshBinnerAdductList();
			binnerAnnotationsTable.setTableModelFromBinnerAdducttList(AdductManager.getBinnerAdductList());
			binnerAnnotationsTable.selectBinnerAdduct(activeAdduct);
		}
		binnerAnnotationEditorDialog.dispose();
	}

	private void deleteSelectedBinnerAdduct() {

		BinnerAdduct toDelete = binnerAnnotationsTable.getSelectedBinnerAdduct();
		if(toDelete == null)
			return;
				
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;
		
		int approve = MessageDialog.showChoiceWithWarningMsg(
				"Delete " + toDelete.getName() + "? (NO UNDO!)",
				this.getContentPane());

		if (approve == JOptionPane.YES_OPTION) {
			AdductManager.deleteBinnerAdduct(toDelete);
			binnerAnnotationsTable.removeBinnerAdduct(toDelete);
		}		
	}
	
	private void showBinnerMassDifferenceManager() {
		
		binnerNeutralMassDifferenceManagerDialog = new BinnerNeutralMassDifferenceManagerDialog();
		binnerNeutralMassDifferenceManagerDialog.setLocationRelativeTo(this.getContentPane());
		binnerNeutralMassDifferenceManagerDialog.setVisible(true);
	}
	
	private void refreshBinnerAdductList() {
		
		AdductManager.refreshBinnerNeutralMassDifference();
		AdductManager.refreshBinnerAdductList();
		binnerAnnotationsTable.setTableModelFromBinnerAdducttList(
				AdductManager.getBinnerAdductList());
	}

	private void exportBinnerAnnotationsFile() {

//		JFileChooser chooser = new ImprovedFileChooser();
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setDialogTitle("Save Binner annotations to file:");
//		chooser.setApproveButtonText("Save annotations");
//		chooser.setCurrentDirectory(baseDirectory);		
//		File outputFile = new File("Binner_annotations_" + FIOUtils.getTimestamp() + ".TXT");
//		chooser.setSelectedFile(outputFile);
//		if (chooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
//
//			outputFile = chooser.getSelectedFile();
//			try {
//				writeBinnerAnnotationsToFile(outputFile);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			baseDirectory = outputFile.getParentFile();
//			savePreferences();
//			if(MessageDialog.showChoiceMsg("Annotation file created, do you want to open containing folder?",
//					this.getContentPane()) == JOptionPane.YES_OPTION) {
//				try {
//					Desktop.getDesktop().open(baseDirectory);
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//			}
//		}
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Save Binner annotations to text file");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = "Binner_annotation_list_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".TXT";
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File outputFile = fc.getSelectedFile();
			FIOUtils.changeExtension(outputFile, "TXT");
			try {
				writeBinnerAnnotationsToFile(outputFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			baseDirectory = outputFile.getParentFile();
			savePreferences();
			if(MessageDialog.showChoiceMsg("Annotation file created, do you want to open containing folder?",
					this.getContentPane()) == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().open(baseDirectory);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private void writeBinnerAnnotationsToFile(File outputFile) throws Exception {
		
		FileWriter fWriter = new FileWriter(outputFile);
		final Writer writer = new BufferedWriter(new FileWriter(outputFile));
		char columnSeparator = MRC2ToolBoxConfiguration.getTabDelimiter();

		// Create header			
		writer.append(BinnerAnnotationFileField.ANNOTATION.getName());
		writer.append(columnSeparator);
		writer.append(BinnerAnnotationFileField.MASS.getName());
		writer.append(columnSeparator);
		writer.append(BinnerAnnotationFileField.MODE.getName());
		writer.append(columnSeparator);
		writer.append(BinnerAnnotationFileField.CHARGE.getName());
		writer.append(columnSeparator);
		writer.append(BinnerAnnotationFileField.TIER.getName());
		writer.append("\n");

		for (BinnerAdduct mod : AdductManager.getBinnerAdductList()) {

			writer.append(mod.getBinnerName());
			writer.append(columnSeparator);
			writer.append(MRC2ToolBoxConfiguration.getMzFormat().format(mod.getMass()));
			writer.append(columnSeparator);
			
			String polarity = mod.getPolarity().name();
			if(mod.getPolarity().equals(Polarity.Neutral))
				polarity = "Both";
			
			writer.append(polarity);
			writer.append(columnSeparator);
			writer.append(Integer.toString(mod.getCharge()));
			writer.append(columnSeparator);
			writer.append(Integer.toString(mod.getTier()));
			writer.append("\n");
		}
		writer.flush();
		fWriter.close();
		writer.close();
	}
	
	public BinnerAnnotationsTable getAdductTable() {
		return binnerAnnotationsTable;
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}























