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

package edu.umich.med.mrc2.datoolbox.gui.worklist;

import java.awt.Component;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataFileCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DateTimeCellRenderer;

public class WorklistTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7297021115977628847L;
	private WorklistTableModel model;
	private DataFileCellRenderer fileRenderer;

	public WorklistTable() {

		super();

		setAutoCreateColumnsFromModel(false);
		model = new WorklistTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<WorklistTableModel>(model);
		setRowSorter(rowSorter);
		
		dtRenderer = new DateTimeCellRenderer();
		setDefaultRenderer(Date.class, dtRenderer);

		fileRenderer = new DataFileCellRenderer();
		setDefaultRenderer(DataFile.class, dtRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public WorklistTable(Worklist listToShow) {

		super();
		setAutoCreateColumnsFromModel(false);

		model = new WorklistTableModel();
		setModel(model);
		dtRenderer = new DateTimeCellRenderer();
		setDefaultRenderer(Date.class, dtRenderer);

		fileRenderer = new DataFileCellRenderer();
		setDefaultRenderer(DataFile.class, fileRenderer);

		model.setTableModelFromWorklist(listToShow);
		createDefaultColumnsFromModel();
		rowSorter = new TableRowSorter<WorklistTableModel>(model);
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
	
	public String getWorklistsAsString() {

		StringBuffer worklistData = new StringBuffer();
		int numCols = getColumnCount();

		worklistData.append(getColumnName(0));

		for(int i=1; i<numCols; i++)
			worklistData.append("\t" + getColumnName(i));

		worklistData.append("\n");

		for(int i=0;  i<getRowCount(); i++){

			for(int j=0; j<numCols; j++){

                final TableCellRenderer renderer = getCellRenderer(i, j);
                final Component comp = prepareRenderer(renderer, i, j);
                String txt = null;

                if (comp instanceof JLabel)
                	txt = ((JLabel) comp).getText();

                if (comp instanceof JTextPane)
                	txt = ((JTextPane) comp).getText();

            	worklistData.append(txt.trim());

                if(j<numCols-1)
                	worklistData.append("\t");
                else
                	worklistData.append("\n");
			}
		}
		return worklistData.toString();
	}
}
