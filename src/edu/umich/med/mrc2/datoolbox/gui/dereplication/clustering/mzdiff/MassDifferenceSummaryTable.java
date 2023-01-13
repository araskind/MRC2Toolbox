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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.mzdiff;

import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DoubleValueBin;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;

public class MassDifferenceSummaryTable extends BasicTable{

	/**
	 *
	 */
	private static final long serialVersionUID = -2732608561036372556L;
	private MassDifferenceSummaryTableModel model;

	public MassDifferenceSummaryTable() {
		super();
		model = new MassDifferenceSummaryTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MassDifferenceSummaryTableModel>(model);
		setRowSorter(rowSorter);

		columnModel.getColumnById(MassDifferenceSummaryTableModel.DELTA_MZ_COLUMN).
				setCellRenderer(mzRenderer);
		columnModel.getColumnById(MassDifferenceSummaryTableModel.BIN_WIDTH_COLUMN).
				setCellRenderer(mzRenderer);
		columnModel.getColumnById(MassDifferenceSummaryTableModel.STDEV_COLUMN).
				setCellRenderer(mzRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setModelFromBins(Collection<DoubleValueBin>bins) {
		model.setModelFromBins(bins);
	}
}
