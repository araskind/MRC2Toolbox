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

import java.util.Collection;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class ExperimentListingTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 8721014012518397650L;

	public static final String EXPERIMENT_ID_COLUMN = "ID";
	public static final String SERVICE_REQUEST_ID_COLUMN = "Service req ID";
	public static final String EXPERIMENT_NAME_COLUMN = "Name";
	public static final String PROJECT_COLUMN = "Project";
	public static final String LAB_DATA_COLUMN = "Laboratory";
	public static final String PRINCIPAL_INVESTIGATOR_COLUMN = "Principal investigator";
	public static final String CONTACT_PERSON_COLUMN = "Contact person";
	public static final String START_DATE_COLUMN = "Start date";
	public static final String NIH_GRANT_NUMBER_COLUMN = "NIH Grant";

	public ExperimentListingTableModel() {

		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(EXPERIMENT_ID_COLUMN, LIMSExperiment.class, false),
			new ColumnContext(SERVICE_REQUEST_ID_COLUMN, String.class, false),
			new ColumnContext(EXPERIMENT_NAME_COLUMN, String.class, false),
			new ColumnContext(PROJECT_COLUMN, String.class, false),
			new ColumnContext(LAB_DATA_COLUMN, String.class, false),
			new ColumnContext(PRINCIPAL_INVESTIGATOR_COLUMN, LIMSUser.class, false),
			new ColumnContext(CONTACT_PERSON_COLUMN, LIMSUser.class, false),
			new ColumnContext(START_DATE_COLUMN, Date.class, false),
			new ColumnContext(NIH_GRANT_NUMBER_COLUMN, String.class, false),
		};
	}

	public void setModelFromExperimentCollection(Collection<LIMSExperiment>experimentCollection) {

		setRowCount(0);
		for(LIMSExperiment e : experimentCollection) {

			LIMSUser pi = null;
			LIMSUser contact = null;
			String labName = "";
			if(e.getProject().getOrganization()!= null) {

				labName = e.getProject().getClient().getLaboratory();
				pi = e.getProject().getClient().getPrincipalInvestigator();
				contact = e.getProject().getClient().getContactPerson();
			}
			Object[] obj = {
					e,
					e.getServiceRequestId(),
					e.getName(),
					e.getProject().getName(),
					labName,
					pi,
					contact,
					e.getStartDate(),
					e.getNihGrant()
			};
			super.addRow(obj);
		}
	}
}
