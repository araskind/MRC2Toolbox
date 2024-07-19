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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import java.text.DecimalFormat;

import javax.swing.ListSelectionModel;

import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ChromatographicGradientTable extends BasicTable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2695577206283079527L;

	public ChromatographicGradientTable() {
		super();
		model =  new ChromatographicGradientTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowSorter(null);
		
		columnModel.getColumnById(ChromatographicGradientTableModel.START_TIME_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getRtFormat(), false));
		
		FormattedDecimalRenderer percentPhaseRenderer = new FormattedDecimalRenderer(new DecimalFormat("###.#"), true);
		columnModel.getColumnById(ChromatographicGradientTableModel.PERCENT_A_COLUMN)
			.setCellRenderer(percentPhaseRenderer);
		columnModel.getColumnById(ChromatographicGradientTableModel.PERCENT_B_COLUMN)
			.setCellRenderer(percentPhaseRenderer);
		columnModel.getColumnById(ChromatographicGradientTableModel.PERCENT_C_COLUMN)
			.setCellRenderer(percentPhaseRenderer);
		columnModel.getColumnById(ChromatographicGradientTableModel.PERCENT_D_COLUMN)
			.setCellRenderer(percentPhaseRenderer);

		columnModel.getColumnById(ChromatographicGradientTableModel.START_TIME_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getPpmFormat(), false));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromGradient(ChromatographicGradient gradient) {
		thf.setTable(null);
		((ChromatographicGradientTableModel)model).setTableModelFromGradient(gradient);
		thf.setTable(this);
		adjustColumns();
	}
}
















