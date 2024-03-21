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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureInformationBundleCollectionFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.TimestampRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class FeatureCollectionsTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6499296198033405250L;

	public FeatureCollectionsTable() {
		super();
		model = new FeatureCollectionsTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSorter = new TableRowSorter<FeatureCollectionsTableModel>(
				(FeatureCollectionsTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(FeatureCollectionsTableModel.OWNER_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		
		WordWrapCellRenderer ltr = new WordWrapCellRenderer();
		columnModel.getColumnById(FeatureCollectionsTableModel.COLLECTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(FeatureCollectionsTableModel.DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		
		columnModel.getColumnById(FeatureCollectionsTableModel.COLLECTION_COLUMN)
			.setMinWidth(120);
		columnModel.getColumnById(FeatureCollectionsTableModel.DESCRIPTION_COLUMN)
			.setMinWidth(120);
		createInteractiveUserRenderer(Arrays.asList(
				FeatureCollectionsTableModel.OWNER_COLUMN));
		
		setDefaultRenderer(Date.class, new TimestampRenderer());
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(MsFeatureInfoBundleCollection.class, 
				new MsFeatureInformationBundleCollectionFormat(SortProperty.Name));
		thf.getParserModel().setComparator(MsFeatureInfoBundleCollection.class, 
				new MsFeatureInfoBundleCollectionComparator(SortProperty.Name));
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, 
				new LIMSUserComparator(SortProperty.Name));
		
		finalizeLayout();
	}

	public void setTableModelFromFeatureCollectionList(
			Collection<MsFeatureInfoBundleCollection>featureCollections) {
		
		thf.setTable(null);
		((FeatureCollectionsTableModel)model).setTableModelFromFeatureCollectionList(featureCollections);			
		thf.setTable(this);
		adjustColumns();
	}
	
	public MsFeatureInfoBundleCollection getSelectedCollection() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (MsFeatureInfoBundleCollection)model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(FeatureCollectionsTableModel.COLLECTION_COLUMN));
	}
	
	public void selectCollection(MsFeatureInfoBundleCollection toSelect) {
		
		int col = getColumnIndex(FeatureCollectionsTableModel.COLLECTION_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(toSelect.equals(getValueAt(i, col))) {
				setRowSelectionInterval(i, i);
//				scrollToSelected();
				return;
			}
		}
	}
	
	public void updateCollectionData(MsFeatureInfoBundleCollection edited) {
		thf.setTable(null);
		((FeatureCollectionsTableModel)model).updateCollectionData(edited);
		thf.setTable(this);
		adjustColumns();
	}
}















