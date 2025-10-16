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

package edu.umich.med.mrc2.datoolbox.gui.fdata.noid;

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundIdentificationConfidenceComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.CompoundLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundLibraryRenderer;

public class MissingIdsTable extends BasicFeatureTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 405761804010862253L;

	private CompoundLibraryRenderer compoundLibraryRenderer;
	private TableRowSorter<MissingIdsTableModel> featureSorter;

	public MissingIdsTable(ActionListener listener) {
		super();

		model = new MissingIdsTableModel();
		setModel(model);

		featureSorter = new TableRowSorter<MissingIdsTableModel>(
				(MissingIdsTableModel)model);
		setRowSorter(featureSorter);
		featureSorter.setComparator(model.getColumnIndex(MissingIdsTableModel.FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name));

		featureSorter.setComparator(model.getColumnIndex(MissingIdsTableModel.ID_CONFIDENCE_COLUMN),
				new CompoundIdentificationConfidenceComparator());

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		compoundLibraryRenderer = new CompoundLibraryRenderer(SortProperty.Name);
		columnModel.getColumnById(MissingIdsTableModel.LIBRARY_COLUMN).setCellRenderer(compoundLibraryRenderer);
		columnModel.getColumnById(MissingIdsTableModel.FEATURE_COLUMN).setCellRenderer(cfRenderer);
		columnModel.getColumnById(MissingIdsTableModel.RT_COLUMN).setCellRenderer(rtRenderer); // Retention time
		columnModel.getColumnById(MissingIdsTableModel.MASS_COLUMN).setCellRenderer(mzRenderer); // Neutral mass

		addTablePopupMenu(new MissingIdTablePopupMenu(listener));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(MsFeature.class, new MsFeatureFormat(SortProperty.Name));
		thf.getParserModel().setComparator(MsFeature.class, new MsFeatureComparator(SortProperty.Name));
		thf.getParserModel().setFormat(CompoundLibrary.class, new CompoundLibraryFormat(SortProperty.Name));
		finalizeLayout();
	}

	@Override
	public Collection<MsFeature> getSelectedFeatures() {

		Collection<MsFeature>selected = new HashSet<MsFeature>();
		if(getSelectedRow() == -1)
			return selected;

		for(int i : getSelectedRows())
			selected.add((MsFeature) getValueAt(getSelectedRow(),
					getColumnIndex(MissingIdsTableModel.FEATURE_COLUMN)));

		return selected;
	}

	@Override
	public MsFeature getSelectedFeature() {

		if(getSelectedRow() == -1)
			return null;

		return (MsFeature) getValueAt(getSelectedRow(),
			getColumnModel().getColumnIndex(MissingIdsTableModel.FEATURE_COLUMN));
	}

	@Override
	public int getFeatureRow(MsFeature f) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTableModelFromFeatureCluster(MsFeatureCluster selectedCluster) {
		// TODO Auto-generated method stub

	}

	public void setTableModelFromFeatureMap(HashMap<CompoundLibrary, Collection<MsFeature>> unidentified) {

		((MissingIdsTableModel)model).setTableModelFromFeatureMap(unidentified);
		adjustColumns();
	}

	@Override
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {
		// TODO Auto-generated method stub
		return null;
	}

}
