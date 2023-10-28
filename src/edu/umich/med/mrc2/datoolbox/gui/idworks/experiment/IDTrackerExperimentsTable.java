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

package edu.umich.med.mrc2.datoolbox.gui.idworks.experiment;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSExperimentFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.lims.experiment.ExperimentListingTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSExperimentRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSUserRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class IDTrackerExperimentsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8965100101624914727L;

	public IDTrackerExperimentsTable() {
		super();
		model = new IDTrackerExperimentsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<IDTrackerExperimentsTableModel>(
				(IDTrackerExperimentsTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(IDTrackerExperimentsTableModel.EXPERIMENT_ID_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID, SortDirection.DESC));
		rowSorter.setComparator(model.getColumnIndex(IDTrackerExperimentsTableModel.CONTACT_PERSON_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		longTextRenderer = new WordWrapCellRenderer();
		columnModel.getColumnById(IDTrackerExperimentsTableModel.EXPERIMENT_NAME_COLUMN)
			.setCellRenderer(longTextRenderer);
		columnModel.getColumnById(IDTrackerExperimentsTableModel.EXPERIMENT_DESCRIPTION_COLUMN)
			.setCellRenderer(longTextRenderer);
		columnModel.getColumnById(IDTrackerExperimentsTableModel.PROJECT_COLUMN)
			.setCellRenderer(longTextRenderer);

		setDefaultRenderer(LIMSExperiment.class, new LIMSExperimentRenderer());

		userRenderer = new LIMSUserRenderer();
		setDefaultRenderer(LIMSUser.class, userRenderer);

		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();
				if(columnModel.isColumnVisible(columnModel.getColumnById(IDTrackerExperimentsTableModel.CONTACT_PERSON_COLUMN)) &&
						columnAtPoint(p) == columnModel.getColumnIndex(IDTrackerExperimentsTableModel.CONTACT_PERSON_COLUMN))
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(userRenderer);
		addMouseMotionListener(userRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSExperiment.class, new LIMSExperimentFormat());
		thf.getParserModel().setComparator(LIMSExperiment.class, new LIMSExperimentComparator(SortProperty.ID));

		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, new LIMSUserComparator(SortProperty.Name));

		finalizeLayout();
	}

	public void setModelFromExperimentCollection(Collection<LIMSExperiment>experimentCollection) {
		thf.setTable(null);
		((IDTrackerExperimentsTableModel)model).setModelFromExperimentCollection(experimentCollection);
		thf.setTable(this);
		rowSorter.sort();
		adjustColumns();
	}

	public LIMSExperiment getSelectedExperiment() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		int column = getColumnIndex(ExperimentListingTableModel.EXPERIMENT_ID_COLUMN);
		return (LIMSExperiment) model.getValueAt(convertRowIndexToModel(row), column);
	}
}
