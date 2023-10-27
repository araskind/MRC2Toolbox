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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.spectrum;

import java.text.DecimalFormat;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsMsLibraryFeatureRenderer;

public class MSMSLibraryFeaturesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8853294038580302429L;
	public MSMSLibraryFeaturesTable() {
		super();
		model = new MSMSLibraryFeaturesTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MSMSLibraryFeaturesTableModel>(
				(MSMSLibraryFeaturesTableModel)model);
		setRowSorter(rowSorter);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(MsMsLibraryFeature.class, new MsMsLibraryFeatureRenderer(SortProperty.ID));
		columnModel.getColumnById(MSMSLibraryFeaturesTableModel.PARENT_MZ_COLUMN)
			.setCellRenderer(mzRenderer); // Parent mass

		FormattedDecimalRenderer scoreRenderer = 
				new FormattedDecimalRenderer(new DecimalFormat("#.##"), true);
		columnModel.getColumnById(MSMSLibraryFeaturesTableModel.ENTROPY_COLUMN)
			.setCellRenderer(scoreRenderer);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromLibraryFeatureCollection(Collection<MsMsLibraryFeature>libraryFeatures) {

		thf.setTable(null);
		((MSMSLibraryFeaturesTableModel)model).setTableModelFromLibraryFeatureCollection(libraryFeatures);
		thf.setTable(this);
		adjustColumns();		
	}

	public MsMsLibraryFeature getSelectedFeature() {

		if(getSelectedRow() == -1)
			return null;

		return (MsMsLibraryFeature)model.getValueAt(
				convertRowIndexToModel(getSelectedRow()),
				model.getColumnIndex(MSMSLibraryFeaturesTableModel.FEATURE_COLUMN));
	}
}




















