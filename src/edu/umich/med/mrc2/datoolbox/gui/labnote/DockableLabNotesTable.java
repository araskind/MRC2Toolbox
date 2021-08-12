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

package edu.umich.med.mrc2.datoolbox.gui.labnote;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisQcEventAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableLabNotesTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);
	private LabNotesTable labNotesTable;

	public DockableLabNotesTable(ListSelectionListener tableSelectionListener) {

		super("DockableLabNotesTable", componentIcon, "Lab notes listing", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		labNotesTable = new LabNotesTable();
		labNotesTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
		JScrollPane designScrollPane = new JScrollPane(labNotesTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
	}

	public void setTableModelFromAnnotations(Collection<AnalysisQcEventAnnotation>annotations) {
		labNotesTable.setTableModelFromAnnotations(annotations);
	}

	public void addAnnotations (Collection<AnalysisQcEventAnnotation>annotations) {
		labNotesTable.addAnnotations(annotations);
	}

	public AnalysisQcEventAnnotation getSelectedAnnotation(){
		return labNotesTable.getSelectedAnnotation();
	}

	public void clearTable() {
		labNotesTable.clearTable();
	}

	public void selectAnnotation(AnalysisQcEventAnnotation annotation) {
		labNotesTable.selectAnnotation(annotation);
	}

	public void removeAnnotations(Collection<AnalysisQcEventAnnotation> annotations) {
		labNotesTable.removeAnnotations(annotations);
	}

	/**
	 * @return the labNotesTable
	 */
	public LabNotesTable getLabNotesTable() {
		return labNotesTable;
	}
}








