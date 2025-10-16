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

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class BinnerAnnotationsEditorToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -7270184336598621941L;
	
	private static final Icon newAnnotationFromAdductIcon = GuiUtils.getIcon("addBinnerAdduct", 24);
	private static final Icon newAnnotationFromExchangeIcon = GuiUtils.getIcon("addBinnerAdductFromExchange", 24);
	private static final Icon newAnnotationFromBinnerMassDiffIcon = GuiUtils.getIcon("addBinnerAdductFromMassDiff", 24);
	
	private static final Icon addModificationIcon = GuiUtils.getIcon("addBinnerAdduct", 32);
	private static final Icon editModificationIcon = GuiUtils.getIcon("editBinnerAdduct", 32);
	private static final Icon deleteModificationIcon = GuiUtils.getIcon("deleteBinnerAdduct", 32);
	private static final Icon exportModificationIcon = GuiUtils.getIcon("saveDuplicates", 32);
	private static final Icon editBinnerMassDifferencesIcon = GuiUtils.getIcon("editBinnerMassDiff", 32);
	private static final Icon refreshIcon = GuiUtils.getIcon("rerun", 32);

	@SuppressWarnings("unused")
	private JButton
		newModificationButton,
		editModificationButton,
		deleteModificationButton,
		exportModificationsButton,
		editBinnerMassDifferencesButton,
		refreshButton;

	private JPopupMenu newAnnotationMenu;
	private JMenuItem newAnnotationFromAdductMenuItem;
	private JMenuItem newAnnotationFromExchangeMenuItem;
	private JMenuItem newAnnotationFromBinnerMassDiffMenuItem;
	
	public BinnerAnnotationsEditorToolbar(ActionListener listener) {

		super(listener);
		
		refreshButton = GuiUtils.addButton(this, null, refreshIcon, commandListener,
				MainActionCommands.REFRESH_BINNER_ADDUCT_LIST_COMMAND.getName(),
				MainActionCommands.REFRESH_BINNER_ADDUCT_LIST_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		newAnnotationMenu = new JPopupMenu("New annotation");
		newAnnotationFromAdductMenuItem = GuiUtils.addMenuItem(newAnnotationMenu,
				MainActionCommands.NEW_BINNER_ADDUCT_COMMAND.getName(), commandListener,
				MainActionCommands.NEW_BINNER_ADDUCT_COMMAND.getName(), newAnnotationFromAdductIcon);

		newAnnotationFromExchangeMenuItem = GuiUtils.addMenuItem(newAnnotationMenu,
				MainActionCommands.NEW_BINNER_ADDUCT_FROM_EXCHANGE_COMMAND.getName(), commandListener,
				MainActionCommands.NEW_BINNER_ADDUCT_FROM_EXCHANGE_COMMAND.getName(), newAnnotationFromExchangeIcon);

		newAnnotationFromBinnerMassDiffMenuItem = GuiUtils.addMenuItem(newAnnotationMenu,
				MainActionCommands.NEW_BINNER_ADDUCT_FROM_MASS_DIFF_COMMAND.getName(), commandListener,
				MainActionCommands.NEW_BINNER_ADDUCT_FROM_MASS_DIFF_COMMAND.getName(), newAnnotationFromBinnerMassDiffIcon);

		newModificationButton = GuiUtils.addButton(this, null, 
				addModificationIcon, null, null, null, new Dimension(105, buttonDimension.height));
		newModificationButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				newAnnotationMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		});

//		editModificationButton = GuiUtils.addButton(this, null, editModificationIcon, commandListener,
//				MainActionCommands.EDIT_BINNER_ADDUCT_COMMAND.getName(),
//				MainActionCommands.EDIT_BINNER_ADDUCT_COMMAND.getName(),
//				buttonDimension);

		deleteModificationButton = GuiUtils.addButton(this, null, deleteModificationIcon, commandListener,
				MainActionCommands.DELETE_BINNER_ADDUCT_COMMAND.getName(),
				MainActionCommands.DELETE_BINNER_ADDUCT_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);
		
		editBinnerMassDifferencesButton = GuiUtils.addButton(this, null, editBinnerMassDifferencesIcon, commandListener,
				MainActionCommands.SHOW_BINNER_MASS_DIFFERENCE_MANAGER_COMMAND.getName(),
				MainActionCommands.SHOW_BINNER_MASS_DIFFERENCE_MANAGER_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		exportModificationsButton = GuiUtils.addButton(this, null, exportModificationIcon, listener,
				MainActionCommands.EXPORT_BINNER_ADDUCTS_COMMAND.getName(),
				MainActionCommands.EXPORT_BINNER_ADDUCTS_COMMAND.getName(),
				buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
