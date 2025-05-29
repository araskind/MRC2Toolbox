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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.organization;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.IDTrackerOrganizationComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.IDTrackerOrganizationFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IdTrackerOrganizationRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class OrganizationTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4961051611998862332L;

	public OrganizationTable() {
		super();
		model =  new OrganizationTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<OrganizationTableModel>((OrganizationTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(OrganizationTableModel.PI_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(OrganizationTableModel.CONTACT_PERSON_COLUMN),
				new LIMSUserComparator(SortProperty.Name));		
		rowSorter.setComparator(model.getColumnIndex(OrganizationTableModel.ORGANIZATION_COLUMN),
				new IDTrackerOrganizationComparator(SortProperty.Name));

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(DataExtractionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(IdTrackerOrganization.class, new IdTrackerOrganizationRenderer(SortProperty.Name));
		createInteractiveUserRenderer(Arrays.asList(
				OrganizationTableModel.PI_COLUMN, 
				OrganizationTableModel.CONTACT_PERSON_COLUMN));
		columnModel.getColumnById(OrganizationTableModel.MAILING_ADDRESS_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, new LIMSUserComparator(SortProperty.Name));
		thf.getParserModel().setComparator(IdTrackerOrganization.class, new IDTrackerOrganizationComparator(SortProperty.Name));
		thf.getParserModel().setFormat(IdTrackerOrganization.class, new IDTrackerOrganizationFormat(SortProperty.Name));
		
		addTablePopupMenu(new BasicTablePopupMenu(null, this, true));
		
		finalizeLayout();
	}

	public void setTableModelFromOrganizations(Collection<IdTrackerOrganization> organizations) {
		thf.setTable(null);
		((OrganizationTableModel)model).setTableModelFromOrganizations(organizations);
		thf.setTable(this);
		adjustColumns();
	}

	public IdTrackerOrganization getSelectedOrganization() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (IdTrackerOrganization)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(OrganizationTableModel.ORGANIZATION_COLUMN));
	}
}




















