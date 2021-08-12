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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import umich.ms.datatypes.scan.IScan;

public class MsMsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5088486364694169431L;
	private MsMsTableModel model;

	public MsMsTable() {

		super();
		model = new MsMsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MsMsTableModel>(model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		columnModel.getColumnById(MsMsTableModel.MZ_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(MsMsTableModel.INTENSITY_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getSpectrumIntensityFormat()));

		thf = new TableFilterHeader(this, AutoChoices.DISABLED);
		finalizeLayout();
	}

	public void setTableModelFromTandemMs(TandemMassSpectrum msms) {

		model.setTableModelFromTandemMs(msms);
		tca.adjustColumns();
	}

	public void setTableModelFromScan(IScan scan) {
		model.setTableModelFromScan(scan);
	}
	
	public void setTableModelFromDataPoints(Collection<MsPoint> points, MsPoint parent) {
		model.setTableModelFromDataPoints(points, parent);
	}
}
