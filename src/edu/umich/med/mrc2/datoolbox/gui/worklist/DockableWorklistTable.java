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

package edu.umich.med.mrc2.datoolbox.gui.worklist;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.table.TableCellRenderer;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableWorklistTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("worklist", 16);
	private WorklistTable worklistTable;

	public DockableWorklistTable() {

		super("DockableWorklistTable", componentIcon, "Assay worklist", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		worklistTable = new WorklistTable();
		add(new JScrollPane(worklistTable));
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public WorklistTable getTable() {
		return worklistTable;
	}

	public void setTableModelFromWorklist(Worklist worklist) {
		worklistTable.setTableModelFromWorklist(worklist);
	}

	public String getWorklistsAsString() {		
		return worklistTable.getWorklistsAsString();
	}

	public void copyWorklistToClipboard() {

		StringBuffer worklistData = new StringBuffer();
		int numCols = worklistTable.getColumnCount();

		worklistData.append(worklistTable.getColumnName(0));

		for(int i=1; i<numCols; i++)
			worklistData.append("\t" + worklistTable.getColumnName(i));

		worklistData.append("\n");

		for(int i=0;  i<worklistTable.getRowCount(); i++){

			for(int j=0; j<numCols; j++){

                final TableCellRenderer renderer = worklistTable.getCellRenderer(i, j);
                final Component comp = worklistTable.prepareRenderer(renderer, i, j);
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
		StringSelection stringSelection = new StringSelection(worklistData.toString());
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
}










