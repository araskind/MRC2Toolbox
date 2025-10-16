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

package edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5088486364694169431L;

	public MsTable() {

		super();

		model = new MsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MsTableModel>((MsTableModel)model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		columnModel.getColumnById(MsTableModel.MZ_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(MsTableModel.INTENSITY_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(
					MRC2ToolBoxConfiguration.getSpectrumIntensityFormat()));
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromFeature(MsFeature feature, boolean scaleMs) {

		thf.setTable(null);
		((MsTableModel)model).setTableModelFromFeatureList(
				Collections.singleton(feature), scaleMs);
		//	sortByMz();
		thf.setTable(this);
		adjustColumns();
	}

	public void setTableModelFromFeatureList(Collection<MsFeature> featureList, boolean scaleMs) {

		((MsTableModel)model).setTableModelFromFeatureList(featureList, scaleMs);
		//	sortByMz();
		adjustColumns();
	}

	private void sortByMz() {

		ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		rowSorter.setSortKeys(sortKeys);
		rowSorter.sort();
	}
}
