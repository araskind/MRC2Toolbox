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

package edu.umich.med.mrc2.datoolbox.gui.mgf;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RadioButtonEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;

public class MsMsClusterTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3148919982635938249L;
	private MsMsClusterTableModel model;
	private MsMsClusterTableMouseHandler mouseHandler;
	private StringSelection msmsStringSelection;
	private Clipboard clipboard;

	public MsMsClusterTable() {

		model = new MsMsClusterTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MsMsClusterTableModel>(model);
		setRowSorter(rowSorter);
		
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		radioRenderer = new RadioButtonRenderer();
		radioEditor = new RadioButtonEditor(new JCheckBox());

		columnModel.getColumnById(MsMsClusterTableModel.PRIMARY_COLUMN)
				.setCellRenderer(radioRenderer);
		columnModel.getColumnById(MsMsClusterTableModel.PRIMARY_COLUMN)
				.setCellEditor(radioEditor);

		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		mouseHandler = new MsMsClusterTableMouseHandler(this);
		addMouseListener(mouseHandler);
		finalizeLayout();
	}
	
	public void setTableModelFromMsMsCluster(MsMsCluster featureCluster) {

		model.setTableModelFromFeatureCluster(featureCluster);
		tca.adjustColumns();
	}
	
	public SimpleMsMs getSelectedMsMs() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (SimpleMsMs) this.getValueAt(convertRowIndexToModel(row), 
					model.getColumnIndex(MsMsClusterTableModel.MSMS_COLUMN));
	}
	
	public void copyFeatureMsMs() {

		SimpleMsMs feature = getSelectedMsMs();
		if(feature == null)
			return;
		
		msmsStringSelection = new StringSelection(feature.getMsMsString());
		clipboard.setContents(msmsStringSelection, msmsStringSelection);
	}
}










