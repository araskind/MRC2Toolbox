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

package edu.umich.med.mrc2.datoolbox.gui.library;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MsLibraryPanelToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 6706406228601692687L;

	private static final Icon libraryManagerIcon = GuiUtils.getIcon("libraryManager", 32);
	private static final Icon closeLibraryIcon = GuiUtils.getIcon("close", 32);
	private static final Icon importLibraryIcon = GuiUtils.getIcon("importLibraryToDb", 32);
	private static final Icon exportLibraryIcon = GuiUtils.getIcon("exportLibrary", 32);
	private static final Icon exportFilteredLibraryIcon = GuiUtils.getIcon("exportFilteredLibraryToFile", 32);
	private static final Icon mergeLibrariesIcon = GuiUtils.getIcon("mergeLibraries", 32);
	private static final Icon newFeatureIcon = GuiUtils.getIcon("newLibraryFeature", 32);
	private static final Icon editFeatureIcon = GuiUtils.getIcon("editLibraryFeature", 32);
	private static final Icon deleteFeatureIcon = GuiUtils.getIcon("deleteFeature", 32);
	private static final Icon importRtIcon = GuiUtils.getIcon("importLibraryRtValues", 32);
	private static final Icon libraryExportIcon = GuiUtils.getIcon("exportLibrary", 32);
	private static final Icon libraryImportIcon = GuiUtils.getIcon("importLibraryToDb", 32);
	
	@SuppressWarnings("unused")
	private JButton
			libraryManagerButton,
			closeLibraryButton,
			importLibraryButton,
			exportLibraryButton,
			exportFilteredLibraryButton,
			convertLibrarieForRecursionButton,
			newFeatureButton,
			editFeatureButton,
			deleteFeatureButton,
			importRtButton,
			exportRefMSMSLibraryButton,
			importDecoyRefMSMSLibraryButton;

	private JComboBox activeLibraryComboBox;

	@SuppressWarnings("unchecked")
	public MsLibraryPanelToolbar(ActionListener commandListener) {

		super(commandListener);

		libraryManagerButton = GuiUtils.addButton(this, null, libraryManagerIcon, commandListener,
				MainActionCommands.SHOW_LIBRARY_MANAGER_COMMAND.getName(),
				MainActionCommands.SHOW_LIBRARY_MANAGER_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		closeLibraryButton = GuiUtils.addButton(this, null, closeLibraryIcon, commandListener,
				MainActionCommands.CLOSE_LIBRARY_COMMAND.getName(),
				MainActionCommands.CLOSE_LIBRARY_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		importLibraryButton = GuiUtils.addButton(this, null, importLibraryIcon, commandListener,
				MainActionCommands.IMPORT_COMPOUND_LIBRARY_COMMAND.getName(),
				MainActionCommands.IMPORT_COMPOUND_LIBRARY_COMMAND.getName(), buttonDimension);

		importRtButton = GuiUtils.addButton(this, null, importRtIcon, commandListener,
				MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_DIALOG_COMMAND.getName(),
				MainActionCommands.IMPORT_LIBRARY_FEATURE_RT_DIALOG_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		exportLibraryButton = GuiUtils.addButton(this, null, exportLibraryIcon, commandListener,
				MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND.getName(),
				MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND.getName(), buttonDimension);

		exportFilteredLibraryButton = GuiUtils.addButton(this, null, exportFilteredLibraryIcon, commandListener,
				MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND.getName(),
				MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		convertLibrarieForRecursionButton = GuiUtils.addButton(this, null, mergeLibrariesIcon, commandListener,
				MainActionCommands.CONVERT_LIBRARY_FOR_RECURSION_DIALOG_COMMAND.getName(),
				MainActionCommands.CONVERT_LIBRARY_FOR_RECURSION_DIALOG_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		newFeatureButton = GuiUtils.addButton(this, null, newFeatureIcon, commandListener,
				MainActionCommands.NEW_LIBRARY_FEATURE_DIAOG_COMMAND.getName(),
				MainActionCommands.NEW_LIBRARY_FEATURE_DIAOG_COMMAND.getName(), buttonDimension);
		newFeatureButton.setEnabled(false);

/*		editFeatureButton = GuiUtils.addButton(this, null, editFeatureIcon, commandListener,
				MainActionCommands.EDIT_LIBRARY_FEATURE_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_LIBRARY_FEATURE_DIALOG_COMMAND.getName(), buttonDimension);*/

		deleteFeatureButton = GuiUtils.addButton(this, null, deleteFeatureIcon, commandListener,
				MainActionCommands.DELETE_LIBRARY_FEATURE_COMMAND.getName(),
				MainActionCommands.DELETE_LIBRARY_FEATURE_COMMAND.getName(), buttonDimension);
		
		addSeparator(buttonDimension);
		
		exportRefMSMSLibraryButton = GuiUtils.addButton(this, null, libraryExportIcon, commandListener,
				MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName(),
				MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName(), buttonDimension);
//		exportRefMSMSLibraryButton.setEnabled(false);
		
		importDecoyRefMSMSLibraryButton = GuiUtils.addButton(this, null, libraryImportIcon, commandListener,
				MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName(),
				MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName(), buttonDimension);
		importDecoyRefMSMSLibraryButton.setEnabled(false);
		
		addSeparator(buttonDimension);

		Component horizontalGlue = Box.createHorizontalGlue();
		add(horizontalGlue);

		JLabel lblNewLabel = new JLabel("Active library: ");
		add(lblNewLabel);

		activeLibraryComboBox = new JComboBox();
		activeLibraryComboBox.setFont(new Font("Tahoma", Font.BOLD, 12));
		activeLibraryComboBox.setModel(new SortedComboBoxModel<CompoundLibrary>());
		activeLibraryComboBox.setMinimumSize(new Dimension(250, 26));
		activeLibraryComboBox.addItemListener((ItemListener) commandListener);
		add(activeLibraryComboBox);
	}

	@SuppressWarnings("unchecked")
	public void updateLibraryName(CompoundLibrary selectedLibrary) {

		activeLibraryComboBox.removeItemListener((ItemListener) commandListener);
		CompoundLibrary[] libs = 
				MRC2ToolBoxCore.getActiveMsLibraries().toArray(
						new CompoundLibrary[MRC2ToolBoxCore.getActiveMsLibraries().size()]);
		activeLibraryComboBox.setModel(new SortedComboBoxModel<CompoundLibrary>(libs));

		if(selectedLibrary == null || !MRC2ToolBoxCore.getActiveMsLibraries().contains(selectedLibrary)) {
			activeLibraryComboBox.setSelectedIndex(-1);
		}
		else {
			activeLibraryComboBox.setSelectedItem(selectedLibrary);
			activeLibraryComboBox.revalidate();
			activeLibraryComboBox.repaint();
		}
		activeLibraryComboBox.addItemListener((ItemListener) commandListener);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
