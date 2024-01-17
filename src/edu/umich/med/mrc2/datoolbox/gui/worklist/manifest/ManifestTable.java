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

package edu.umich.med.mrc2.datoolbox.gui.worklist.manifest;

import java.util.Date;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataFileCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DateTimeCellRenderer;

public class ManifestTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7297021115977628847L;
	private ManifestTableModel model;
	private DataFileCellRenderer fileRenderer;

	public ManifestTable() {

		super();

		setAutoCreateColumnsFromModel(false);
		model = new ManifestTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ManifestTableModel>(model);
		setRowSorter(rowSorter);
		
		dtRenderer = new DateTimeCellRenderer();
		setDefaultRenderer(Date.class, dtRenderer);

		fileRenderer = new DataFileCellRenderer();
		setDefaultRenderer(DataFile.class, dtRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public ManifestTable(Worklist listToShow) {

		super();
		setAutoCreateColumnsFromModel(false);

		model = new ManifestTableModel();
		setModel(model);
		dtRenderer = new DateTimeCellRenderer();
		setDefaultRenderer(Date.class, dtRenderer);

		fileRenderer = new DataFileCellRenderer();
		setDefaultRenderer(DataFile.class, fileRenderer);

		model.setTableModelFromWorklist(listToShow);
		createDefaultColumnsFromModel();
		rowSorter = new TableRowSorter<ManifestTableModel>(model);
		setRowSorter(rowSorter);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromWorklist(Worklist worklist) {

		if(worklist != null) {

			Runnable swingCode = new Runnable() {

				public void run() {

					JTable table = thf.getTable();
					thf.setTable(null);
					model.removeTableModelListener(table);
					model.setTableModelFromWorklist(worklist);
					createDefaultColumnsFromModel();
					setDefaultRenderer(Date.class, dtRenderer);
					setDefaultRenderer(DataFile.class, fileRenderer);
					model.addTableModelListener(table);
					model.fireTableDataChanged();
					thf.setTable(table);
					tca.adjustColumns();
					finalizeLayout();
				}
			};
			try {
				if (SwingUtilities.isEventDispatchThread())
					swingCode.run();
				else
					SwingUtilities.invokeAndWait(swingCode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
			clearTable();
	}
	
	@Override
	public void clearTable() {
		
		thf.setTable(null);
		model = new ManifestTableModel();
		setModel(model);
		createDefaultColumnsFromModel();
		rowSorter = new TableRowSorter<ManifestTableModel>(model);
		setRowSorter(rowSorter);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
}
