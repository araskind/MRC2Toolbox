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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms1;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableReferenceMsOneFeatureTable extends DefaultSingleCDockable {

	private ReferenceMsOneFeatureTable featureTable;
	private static final Icon componentIcon = GuiUtils.getIcon("msOne", 16);

	public DockableReferenceMsOneFeatureTable(DockableMRC2ToolboxPanel parentPanel) {

		super("DockableReferenceMsOneFeatureTable", componentIcon, "MS1 features", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		featureTable = new ReferenceMsOneFeatureTable();
		add(new JScrollPane(featureTable));
		featureTable.getSelectionModel().addListSelectionListener(parentPanel);

	}

	/**
	 * @return the libraryFeatureTable
	 */
	public ReferenceMsOneFeatureTable getTable() {
		return featureTable;
	}

	public MSFeatureInfoBundle getSelectedBundle() {
		return featureTable.getSelectedBundle();
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleAtPopup() {
		return featureTable.getMSFeatureInfoBundleAtPopup();
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

	public void selectBundle(MSFeatureInfoBundle toSelect) {
		
		featureTable.selectBundle(toSelect);
		featureTable.scrollToSelected();
	}
}
