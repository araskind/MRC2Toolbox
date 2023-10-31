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

package edu.umich.med.mrc2.datoolbox.gui.lims.experiment;

import java.util.Arrays;
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
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSExperimentRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class ExperimentListingTable extends BasicTable {

	private static final long serialVersionUID = 5953976894424046681L;

	public ExperimentListingTable() {

		super();
		model = new ExperimentListingTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ExperimentListingTableModel>((ExperimentListingTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(ExperimentListingTableModel.EXPERIMENT_ID_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID, SortDirection.DESC));
		rowSorter.setComparator(model.getColumnIndex(ExperimentListingTableModel.CONTACT_PERSON_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(ExperimentListingTableModel.PRINCIPAL_INVESTIGATOR_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		longTextRenderer = new WordWrapCellRenderer();
		columnModel.getColumnById(ExperimentListingTableModel.EXPERIMENT_NAME_COLUMN)
			.setCellRenderer(longTextRenderer);
		columnModel.getColumnById(ExperimentListingTableModel.PROJECT_COLUMN)
			.setCellRenderer(longTextRenderer);

		setDefaultRenderer(LIMSExperiment.class, new LIMSExperimentRenderer());
		createInteractiveUserRenderer(Arrays.asList(
				ExperimentListingTableModel.PRINCIPAL_INVESTIGATOR_COLUMN, 
				ExperimentListingTableModel.CONTACT_PERSON_COLUMN));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSExperiment.class, new LIMSExperimentFormat());
		thf.getParserModel().setComparator(LIMSExperiment.class, new LIMSExperimentComparator(SortProperty.ID));
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, new LIMSUserComparator(SortProperty.Name));

		finalizeLayout();
	}

	public void setModelFromExperimentCollection(Collection<LIMSExperiment>experimentCollection) {
		thf.setTable(null);
		((ExperimentListingTableModel)model).setModelFromExperimentCollection(experimentCollection);
		rowSorter.sort();
		thf.setTable(this);
		adjustColumns();
	}

	public LIMSExperiment getSelectedExperiment() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		int column = model.getColumnIndex(ExperimentListingTableModel.EXPERIMENT_ID_COLUMN);
		return (LIMSExperiment) model.getValueAt(convertRowIndexToModel(row), column);
	}
}
