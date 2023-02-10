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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ExperimentDesignEditorMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon loadDesignIcon = GuiUtils.getIcon("loadDesign", 24);
	private static final Icon loadDesignIconSmall = GuiUtils.getIcon("loadDesign", 16);
	private static final Icon appendDesignIcon = GuiUtils.getIcon("appendDesign", 24);
	private static final Icon clearDesignIcon = GuiUtils.getIcon("clearDesign", 24);
	private static final Icon exportDesignIcon = GuiUtils.getIcon("exportDesign", 24);
	private static final Icon addFactorIcon = GuiUtils.getIcon("addFactor", 24);
	private static final Icon editFactorIcon = GuiUtils.getIcon("editFactor", 24);
	private static final Icon editFactorIconSmall = GuiUtils.getIcon("editFactor", 16);
	private static final Icon deleteFactorIcon = GuiUtils.getIcon("deleteFactor", 24);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 24);
	private static final Icon editSampleIcon = GuiUtils.getIcon("editSample", 24);
	private static final Icon editSampleIconSmall = GuiUtils.getIcon("editSample", 24);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteSample", 24);
	private static final Icon editReferenceSamplesIcon = GuiUtils.getIcon("standardSample", 24);

	// Menus
	private JMenu
		desingnMenu,
		factorMenu,
		sampleMenu;

	// Design items
	private JMenuItem
		loadDesignMenuItem,
		appendDesignMenuItem,
		clearDesignMenuItem,
		exportDesignMenuItem;

	// Factor items
	private JMenuItem
		addFactorMenuItem,
		editFactorMenuItem,
		deleteFactorMenuItem;

	// SampleItems
	private JMenuItem
		addSamplesMenuItem,
		deleteSamplesMenuItem,
		editReferenceSamplesMenuItem;

	public ExperimentDesignEditorMenuBar(ActionListener listener) {

		super(listener);

		// Design
		desingnMenu = new JMenu("Design actions");
		desingnMenu.setIcon(loadDesignIconSmall);
		
		loadDesignMenuItem = addItem(desingnMenu, 
				MainActionCommands.LOAD_DESIGN_COMMAND, 
				loadDesignIcon);
		appendDesignMenuItem = addItem(desingnMenu, 
				MainActionCommands.APPEND_DESIGN_COMMAND, 
				appendDesignIcon);
		clearDesignMenuItem = addItem(desingnMenu, 
				MainActionCommands.CLEAR_DESIGN_COMMAND, 
				clearDesignIcon);
		
		desingnMenu.addSeparator();
		
		exportDesignMenuItem = addItem(desingnMenu, 
				MainActionCommands.EXPORT_DESIGN_COMMAND, 
				exportDesignIcon);
		
		add(desingnMenu);

		// Factor
		factorMenu = new JMenu("Factors");
		factorMenu.setIcon(editFactorIconSmall);
		
		addFactorMenuItem = addItem(factorMenu, 
				MainActionCommands.ADD_FACTOR_COMMAND, 
				addFactorIcon);
		editFactorMenuItem = addItem(factorMenu, 
				MainActionCommands.EDIT_FACTOR_COMMAND, 
				editFactorIcon);
		deleteFactorMenuItem = addItem(factorMenu, 
				MainActionCommands.DELETE_FACTOR_COMMAND, 
				deleteFactorIcon);
		
		add(factorMenu);
		
		// Sample
		sampleMenu = new JMenu("Samples");
		sampleMenu.setIcon(editSampleIconSmall);
		
		addSamplesMenuItem = addItem(sampleMenu, 
				MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND, 
				addSampleIcon);
		deleteSamplesMenuItem = addItem(sampleMenu, 
				MainActionCommands.DELETE_SAMPLE_COMMAND, 
				deleteSampleIcon);
		
		sampleMenu.addSeparator();
		
		editReferenceSamplesMenuItem = addItem(sampleMenu, 
				MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND, 
				editReferenceSamplesIcon);
		
		add(sampleMenu);
	}

	@Override
	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
}
