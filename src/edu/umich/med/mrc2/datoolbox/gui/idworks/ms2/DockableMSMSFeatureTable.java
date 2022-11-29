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
import bibliothek.gui.dock.action.DockAction;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.action.actions.SimpleMenuAction;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMSMSFeatureTable extends DefaultSingleCDockable {

	private MSMSFeatureTable featureTable;
	private static final Icon componentIcon = GuiUtils.getIcon("msTwo", 16);
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 16);
	private static final Icon filterClustersIcon = GuiUtils.getIcon("filterClusters", 16);	
	private static final Icon reloadIcon = GuiUtils.getIcon("rerun", 16);
	private static final Icon statsIcon = GuiUtils.getIcon("calcStats", 16);
	private static final Icon clusteredStatsIcon = GuiUtils.getIcon("calcClusteredStats", 16);	
	private static final Icon reloadClusterSetFeaturesIcon = GuiUtils.getIcon("reloadClusterSetFeatures", 16);
	
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
		
		DefaultDockActionSource filterMenuActions = new DefaultDockActionSource();		
		SimpleMenuAction filterActionMenu = new SimpleMenuAction(filterMenuActions);
		filterActionMenu.setIcon(filterIcon);
		filterActionMenu.setText("Filter");       
		actions.add((DockAction)filterActionMenu);
		
		SimpleButtonAction filterFeaturesButton = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName(),
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName(), 
				filterIcon, l);
		filterMenuActions.add(filterFeaturesButton);
		
		SimpleButtonAction filterClusteredFeaturesButton = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_CLUSTERED_FEATURE_FILTER_COMMAND.getName(),
				MainActionCommands.SHOW_CLUSTERED_FEATURE_FILTER_COMMAND.getName(), 
				filterClustersIcon, l);
		filterMenuActions.add(filterClusteredFeaturesButton);
		
		DefaultDockActionSource reloadMenuActions = new DefaultDockActionSource();		
		SimpleMenuAction reloadActionMenu = new SimpleMenuAction(reloadMenuActions);
		reloadActionMenu.setIcon(reloadIcon);
		reloadActionMenu.setText("Reload");       
		actions.add((DockAction)reloadActionMenu);
				
		SimpleButtonAction reloadFeaturesButton = GuiUtils.setupButtonAction(
				MainActionCommands.RELOAD_ACTIVE_MSMS_FEATURES.getName(), 
				MainActionCommands.RELOAD_ACTIVE_MSMS_FEATURES.getName(), 
				reloadIcon, l);
		reloadMenuActions.add(reloadFeaturesButton);
		
		SimpleButtonAction reloadClusterSetFeaturesButton = GuiUtils.setupButtonAction(
				MainActionCommands.RELOAD_ACTIVE_MSMS_CLUSTER_SET_FEATURES.getName(), 
				MainActionCommands.RELOAD_ACTIVE_MSMS_CLUSTER_SET_FEATURES.getName(), 
				reloadClusterSetFeaturesIcon, l);
		reloadMenuActions.add(reloadClusterSetFeaturesButton);
		
		DefaultDockActionSource statsMenuActions = new DefaultDockActionSource();		
		SimpleMenuAction statsActionMenu = new SimpleMenuAction(statsMenuActions);
		statsActionMenu.setIcon(statsIcon);
		statsActionMenu.setText("Summary");       
		actions.add((DockAction)statsActionMenu);
		
		SimpleButtonAction showDataSetStatsButton = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_MSMS_DATA_SET_STATISTICS_COMMAND.getName(),
				MainActionCommands.SHOW_MSMS_DATA_SET_STATISTICS_COMMAND.getName(), 
				statsIcon, l);
		statsMenuActions.add(showDataSetStatsButton);
		
		SimpleButtonAction showClusteredDataSetStatsButton = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_CLUSTERED_MSMS_DATA_SET_STATISTICS_COMMAND.getName(),
				MainActionCommands.SHOW_CLUSTERED_MSMS_DATA_SET_STATISTICS_COMMAND.getName(), 
				clusteredStatsIcon, l);
		statsMenuActions.add(showClusteredDataSetStatsButton);
				
		actions.addSeparator();
		
		intern().setActionOffers(actions);
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public MSMSFeatureTable getTable() {
		return featureTable;
	}

	public MSFeatureInfoBundle getSelectedBundle() {
		return featureTable.getSelectedBundle();
	}

	public void selectBundle(MSFeatureInfoBundle toSelect) {
		featureTable.selectBundle(toSelect);
	}

	public Collection<MSFeatureInfoBundle>getBundles(TableRowSubset subset){

		if(subset.equals(TableRowSubset.SELECTED))
			return featureTable.getMultipleSelectedBundles();
		else if(subset.equals(TableRowSubset.FILTERED))
			return featureTable.getFilteredBundles();
		else
			return featureTable.getAllBundles();
	}

	public Collection<MsFeature>getTableFeatures(TableRowSubset subset){
		
		Collection<MSFeatureInfoBundle>bundles = getBundles(subset);
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
