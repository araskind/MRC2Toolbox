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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;
import java.util.Date;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisQcEventAnnotationComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.AssayMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.InstrumentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisQcEventAnnotationFormat;
import edu.umich.med.mrc2.datoolbox.data.format.AssayMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.InstrumentFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSExperimentFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisQcEventAnnotation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisQcEventAnnotationRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AssayMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.InstrumentRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSUserRenderer;

public class LabNotesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1870878007998038229L;

	private LabNotesTableModel model;
	private LIMSUserRenderer userRenderer;

	private MouseMotionAdapter mma;

	public LabNotesTable() {
		super();
		model = new LabNotesTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<LabNotesTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(LabNotesTableModel.INSTRUMENT_COLUMN),
				new InstrumentComparator(SortProperty.Description));
		rowSorter.setComparator(model.getColumnIndex(LabNotesTableModel.CREATED_BY_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(LabNotesTableModel.ASSAY_COLUMN),
				new AnalysisMethodComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(LabNotesTableModel.EXPERIMENT_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		setDefaultRenderer(Date.class, dtRenderer);
		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRenderer(SortProperty.ID));
		setDefaultRenderer(Assay.class, new AssayMethodRenderer());
		setDefaultRenderer(AnalysisQcEventAnnotation.class, new AnalysisQcEventAnnotationRenderer(SortProperty.Quality));
		setDefaultRenderer(LIMSInstrument.class, new InstrumentRenderer(SortProperty.Description));

		userRenderer = new LIMSUserRenderer();
		setDefaultRenderer(LIMSUser.class, userRenderer);
		mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();
				if(columnModel.isColumnVisible(columnModel.getColumnById(LabNotesTableModel.CREATED_BY_COLUMN)) &&
						columnAtPoint(p) == columnModel.getColumnIndex(LabNotesTableModel.CREATED_BY_COLUMN))
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(userRenderer);
		addMouseMotionListener(userRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);

		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, new LIMSUserComparator(SortProperty.Name));

		thf.getParserModel().setComparator(Assay.class, new AssayMethodComparator(SortProperty.Name));
		thf.getParserModel().setFormat(Assay.class, new AssayMethodFormat(SortProperty.Name));

		thf.getParserModel().setFormat(LIMSInstrument.class, new InstrumentFormat(SortProperty.Description));
		thf.getParserModel().setComparator(LIMSInstrument.class, new InstrumentComparator(SortProperty.Description));

		thf.getParserModel().setFormat(LIMSExperiment.class, new LIMSExperimentFormat());
		thf.getParserModel().setComparator(LIMSExperiment.class, new LIMSExperimentComparator(SortProperty.ID));

		thf.getParserModel().setFormat(ExperimentalSample.class, new ExperimentalSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ExperimentalSample.class, new ExperimentalSampleComparator(SortProperty.ID));

		thf.getParserModel().setFormat(AnalysisQcEventAnnotation.class, new AnalysisQcEventAnnotationFormat(SortProperty.Quality));
		thf.getParserModel().setComparator(AnalysisQcEventAnnotation.class, new AnalysisQcEventAnnotationComparator(SortProperty.Quality));

		finalizeLayout();
	}

	public void setTableModelFromAnnotations(Collection<AnalysisQcEventAnnotation> annotations) {
		thf.setTable(null);
		model.setTableModelFromAnnotations(annotations, true);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public void addAnnotations(Collection<AnalysisQcEventAnnotation> annotations) {
		thf.setTable(null);
		model.setTableModelFromAnnotations(annotations, false);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public AnalysisQcEventAnnotation getSelectedAnnotation() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (AnalysisQcEventAnnotation)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(LabNotesTableModel.EVENT_CATEGORY_COLUMN));
	}

	public void selectAnnotation(AnalysisQcEventAnnotation annotation) {

		int annotationIdx = model.getColumnIndex(LabNotesTableModel.EVENT_CATEGORY_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {

			if(model.getValueAt(convertRowIndexToModel(i), annotationIdx).equals(annotation)) {
				setRowSelectionInterval(i, i);
				return;
			}
		}
	}

	public void removeAnnotations(Collection<AnalysisQcEventAnnotation> annotations) {
		model.removeAnnotations(annotations);
	}
}












