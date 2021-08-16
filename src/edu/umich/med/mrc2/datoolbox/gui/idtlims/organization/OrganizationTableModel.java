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

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class OrganizationTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 38251890009730840L;

	public static final String ORGANIZATION_ID_COLUMN = "ID";
	public static final String ORGANIZATION_COLUMN = "Organization";
	public static final String DEPARTMENT_COLUMN = "Department";
	public static final String LABORATORY_COLUMN = "Laboratory";
	public static final String PI_COLUMN = "Principal Investigator";
	public static final String CONTACT_PERSON_COLUMN = "Contact person";
	public static final String MAILING_ADDRESS_COLUMN = "Mailing address";

	public OrganizationTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ORGANIZATION_ID_COLUMN, String.class, false),
			new ColumnContext(ORGANIZATION_COLUMN, IdTrackerOrganization.class, false),
			new ColumnContext(DEPARTMENT_COLUMN, String.class, false),
			new ColumnContext(LABORATORY_COLUMN, String.class, false),			
			new ColumnContext(PI_COLUMN, LIMSUser.class, false),
			new ColumnContext(CONTACT_PERSON_COLUMN, LIMSUser.class, false),
			new ColumnContext(MAILING_ADDRESS_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromOrganizations(Collection<IdTrackerOrganization> organizations) {

		setRowCount(0);

		if(organizations.isEmpty())
			return;

		for (IdTrackerOrganization organization : organizations) {

			Object[] obj = {
					organization.getId(),
					organization,
					organization.getDepartment(),
					organization.getLaboratory(),
					organization.getPrincipalInvestigator(),
					organization.getContactPerson(),
					organization.getMailingAddress(),
			};
			super.addRow(obj);
		}
	}
}
