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

package edu.umich.med.mrc2.datoolbox.gui.idworks.stan;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.compare.StandardFeatureAnnotationComparator;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.StandardFeatureAnnotationRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class StandardFeatureAnnotationTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;

	public StandardFeatureAnnotationTable() {
		super();
		model =  new StandardFeatureAnnotationTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<StandardFeatureAnnotationTableModel>(
				(StandardFeatureAnnotationTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(StandardFeatureAnnotationTableModel.CODE_COLUMN),
				new StandardFeatureAnnotationComparator(SortProperty.Name));
		
		columnModel.getColumnById(StandardFeatureAnnotationTableModel.CODE_COLUMN).
			setCellRenderer(new StandardFeatureAnnotationRenderer(SortProperty.Name));
		
		columnModel.getColumnById(StandardFeatureAnnotationTableModel.TEXT_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
				
		columnModel.getColumnById(StandardFeatureAnnotationTableModel.CODE_COLUMN).setMaxWidth(80);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		finalizeLayout();
	}

	public void setTableModelFromStandardFeatureAnnotationList(
			Collection<StandardFeatureAnnotation> annotationList) {
		((StandardFeatureAnnotationTableModel)model).setTableModelFromStandardFeatureAnnotationList(annotationList);
		adjustColumns();
	}

	public StandardFeatureAnnotation getSelectedStandardFeatureAnnotation() {

		if(getSelectedRow() == -1)
			return null;

		return (StandardFeatureAnnotation) getValueAt(getSelectedRow(),
				getColumnIndex(StandardFeatureAnnotationTableModel.CODE_COLUMN));
	}
	
	public Collection<StandardFeatureAnnotation> getSelectedStandardFeatureAnnotations() {

		Collection<StandardFeatureAnnotation>selected = 
				new ArrayList<StandardFeatureAnnotation>();
		int col = model.getColumnIndex(StandardFeatureAnnotationTableModel.CODE_COLUMN);
		for(int i : getSelectedRows())
			selected.add((StandardFeatureAnnotation) model.getValueAt(convertRowIndexToModel(i),col));

		return selected;
	}
	
	public void selectStandardFeatureAnnotations(
			Collection<StandardFeatureAnnotation>annotationList) {
		
		int col = model.getColumnIndex(StandardFeatureAnnotationTableModel.CODE_COLUMN);
		if(col == -1)
			return;
		
		for(int i=0; i<getRowCount(); i++) {
			
			if(annotationList.contains(model.getValueAt(convertRowIndexToModel(i), col)))
				addRowSelectionInterval(i, i);
		}
	}
}
















