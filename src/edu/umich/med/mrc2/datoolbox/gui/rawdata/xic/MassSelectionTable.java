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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.xic;

import java.util.Collection;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MassSelectionTable  extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5800108674852597479L;
	
	public MassSelectionTable() {

		super();

		model = new MassSelectionTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MassSelectionTableModel>(
				(MassSelectionTableModel)model);
		setRowSorter(rowSorter);

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		columnModel.getColumnById(MassSelectionTableModel.MZ_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(MassSelectionTableModel.INTENSITY_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(
					MRC2ToolBoxConfiguration.getSpectrumIntensityFormat()));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromDataPoints(Collection<MsPoint>points) {

		thf.setTable(null);
		((MassSelectionTableModel)model).setTableModelFromDataPoints(points);
		thf.setTable(this);
		adjustColumns();
	}

	public Collection<Double> getSelectedMasses() {
		
		Collection<Double>selected = new TreeSet<Double>();
		int mzColumn = model.getColumnIndex(MassSelectionTableModel.MZ_COLUMN);
		for(int i : getSelectedRows()) 
			selected.add((Double)model.getValueAt(convertRowIndexToModel(i), mzColumn));		
			
		return selected;
	}
	
	public void selectMass(double mass) {
		
		int mzColumn = model.getColumnIndex(MassSelectionTableModel.MZ_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			if((double)model.getValueAt(convertRowIndexToModel(i), mzColumn) == mass) {
				setRowSelectionInterval(i, i);
				return;
			}
		}
	}
}






























