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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2;

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMSMSFeatureTable extends DefaultSingleCDockable {

	private MSMSFeatureTable featureTable;
	private static final Icon componentIcon = GuiUtils.getIcon("msTwo", 16);
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 16);
	private static final Icon reloadIcon = GuiUtils.getIcon("rerun", 16);
	private static final Icon findMSMSFeaturesIcon = GuiUtils.getIcon("findMSMSFeatures", 16);
	private SimpleButtonAction 
			reloadFeaturesButton, 
			filterFeaturesButton,
			findMSMSFeaturesButton;
	
	public DockableMSMSFeatureTable(DockableMRC2ToolboxPanel parentPanel) {

		super("DockableMSMSFeatureTable", componentIcon, "MS2 features", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		featureTable = new MSMSFeatureTable();
		add(new JScrollPane(featureTable));
		featureTable.getSelectionModel().addListSelectionListener(parentPanel);
		initButtons(parentPanel);
	}
	
	private void initButtons(ActionListener l) {

		DefaultDockActionSource actions = new DefaultDockActionSource(
				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));

		findMSMSFeaturesButton = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_FEATURE_SEARCH_BY_RT_ID_COMMAND.getName(),
				MainActionCommands.SHOW_FEATURE_SEARCH_BY_RT_ID_COMMAND.getName(), 
				findMSMSFeaturesIcon, l);	
		actions.add(findMSMSFeaturesButton);
		
		filterFeaturesButton = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName(),
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName(), 
				filterIcon, l);
		actions.add(filterFeaturesButton);	
		
		reloadFeaturesButton = GuiUtils.setupButtonAction(
				MainActionCommands.RELOAD_ACTIVE_MSMS_FEATURES.getName(), 
				MainActionCommands.RELOAD_ACTIVE_MSMS_FEATURES.getName(), 
				reloadIcon, l);
		actions.add(reloadFeaturesButton);
		
		actions.addSeparator();
		intern().setActionOffers(actions);
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public MSMSFeatureTable getTable() {
		return featureTable;
	}

	public MsFeatureInfoBundle getSelectedBundle() {
		return featureTable.getSelectedBundle();
	}

	public void selectBundle(MsFeatureInfoBundle toSelect) {
		featureTable.selectBundle(toSelect);
	}

	public Collection<MsFeatureInfoBundle>getBundles(TableRowSubset subset){

		if(subset.equals(TableRowSubset.SELECTED))
			return featureTable.getMultipleSelectedBundles();
		else if(subset.equals(TableRowSubset.FILTERED))
			return featureTable.getFilteredBundles();
		else
			return featureTable.getAllBundles();
	}

	public Collection<MsFeature>getTableFeatures(TableRowSubset subset){
		
		Collection<MsFeatureInfoBundle>bundles = getBundles(subset);
		return bundles.stream().
				map(b -> b.getMsFeature()).collect(Collectors.toList());
	}
	
	public void selectFirstRow() {
		featureTable.clearSelection();
		if(featureTable.getRowCount() > 0) {
			featureTable.setRowSelectionInterval(0, 0);
			//	featureTable.scrollToSelected();
		}
	}
}
