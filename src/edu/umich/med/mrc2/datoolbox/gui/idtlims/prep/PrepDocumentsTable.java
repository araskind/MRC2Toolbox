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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSUserRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ObjectAnnotationDocumentTypeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ObjectAnnotationRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class PrepDocumentsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6975693354223437089L;
	
	public static final int iconSize = 24;
	private PrepDocumentsTableModel model;
	private LIMSUserRenderer userRenderer;
	private MouseMotionAdapter mma;

	public PrepDocumentsTable() {
		super();
		model =  new PrepDocumentsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<PrepDocumentsTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(PrepDocumentsTableModel.ADDED_BY_COLUMN),
				new LIMSUserComparator(SortProperty.Name));

		columnModel.getColumnById(PrepDocumentsTableModel.DOCUMENT_DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(PrepDocumentsTableModel.ANNOTATION_ID_COLUMN)
			.setCellRenderer(new ObjectAnnotationRenderer(SortProperty.ID, -1));
		
		ObjectAnnotationDocumentTypeRenderer annotationRenderer = 
				new ObjectAnnotationDocumentTypeRenderer(this);
		columnModel.getColumnById(PrepDocumentsTableModel.FILE_DOWNLOAD_COLUMN)
			.setCellRenderer(annotationRenderer);

		userRenderer = new LIMSUserRenderer();
		setDefaultRenderer(LIMSUser.class, userRenderer);
		mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(PrepDocumentsTableModel.ADDED_BY_COLUMN)) &&
					columnAtPoint(p) == columnModel.getColumnIndex(PrepDocumentsTableModel.ADDED_BY_COLUMN))
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else if(columnModel.isColumnVisible(columnModel.getColumnById(PrepDocumentsTableModel.FILE_DOWNLOAD_COLUMN)) &&
						columnAtPoint(p) == columnModel.getColumnIndex(PrepDocumentsTableModel.FILE_DOWNLOAD_COLUMN))
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(userRenderer);
		addMouseMotionListener(userRenderer);
		addMouseListener(annotationRenderer);		
		finalizeLayout();
	}

	public void setModelFromAnnotations(Collection<ObjectAnnotation>annotations) {
		model.setModelFromAnnotations(annotations);
		getColumnModel().getColumn(
				model.getColumnIndex(PrepDocumentsTableModel.FILE_DOWNLOAD_COLUMN)).setWidth(iconSize * 2);
		tca.adjustColumn(getColumnIndex(PrepDocumentsTableModel.DOCUMENT_DESCRIPTION_COLUMN));
	}

	public Collection<ObjectAnnotation>getSelectedAnnotations() {

		Collection<ObjectAnnotation>selected = new TreeSet<ObjectAnnotation>();
		if(getSelectedRowCount() == 0)
			return selected;
		
		int prColumn = model.getColumnIndex(PrepDocumentsTableModel.ANNOTATION_ID_COLUMN);
		for(int i : getSelectedRows())
			selected.add((ObjectAnnotation)model.getValueAt(convertRowIndexToModel(i), prColumn));

		return selected;
	}

	public Collection<ObjectAnnotation>getAllAnnotations() {

		Collection<ObjectAnnotation>selected = new TreeSet<ObjectAnnotation>();
		int prColumn = model.getColumnIndex(PrepDocumentsTableModel.ANNOTATION_ID_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			selected.add((ObjectAnnotation)model.getValueAt(i, prColumn));

		return selected;
	}
}









