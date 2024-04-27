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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdcoll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollection;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollectionComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpdcol.CompoundMultiplexUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LoadCompoundCollectionTask extends AbstractTask {
	
	CompoundCollection collection;
	private Collection<CpdMetadataField> metadataFields;

	public LoadCompoundCollectionTask(CompoundCollection collection) {
		super();
		this.collection = collection;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		try {
			metadataFields = CompoundMultiplexUtils.getCpdMetadataFields();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
return;

		}
		try {
			getTemporaryComponents();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
return;

		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void getTemporaryComponents() throws Exception{
		
		taskDescription = "Getting compound collection components ... ";
		//CompoundCollectionComponentTmp
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT CC_COMPONENT_ID, CAS, ACCESSION, NAME, SOURCE_DB FROM "
				+ "COMPOUND_COLLECTION_COMPONENTS WHERE CC_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);
		
		String metadataQueryuery = 
				"SELECT FIELD_ID, FIELD_VALUE FROM "
				+ "COMPOUND_COLLECTION_COMPONENT_METADATA WHERE CC_COMPONENT_ID = ?";
		PreparedStatement metadataPs = conn.prepareStatement(metadataQueryuery);
		
		String collectionId = collection.getId();
		ResultSet mdrs = null;
				
		ps.setString(1, collection.getId());
		ResultSet rs = ps.executeQuery();
		total = 0;
		processed = 0;
		if (rs.last()) {
			total = rs.getRow();
			rs.beforeFirst();
		}
		while(rs.next()) {
			
			CompoundCollectionComponent component = 
					new CompoundCollectionComponent(
							rs.getString("CC_COMPONENT_ID"), collectionId, rs.getString("CAS"));
			CompoundIdentity cid = 
					new CompoundIdentity(CompoundDatabaseEnum.getCompoundDatabaseByName(
							rs.getString("SOURCE_DB")), rs.getString("ACCESSION"));
			cid.setCommonName(rs.getString("NAME"));			
			component.setCid(cid);
			
			metadataPs.setString(1, component.getId());
			mdrs = metadataPs.executeQuery();
			while(mdrs.next()) {
				String fieldId = mdrs.getString("FIELD_ID");
				CpdMetadataField field = metadataFields.stream().
						filter(f -> f.getId().equals(fieldId)).
						findFirst().orElse(null);
				if(fieldId != null)
					component.addMetadata(field, mdrs.getString("FIELD_VALUE"));
			}
			mdrs.close();
			collection.addTempComponent(component);
			processed++;
		}
		rs.close();
		ps.close();
		metadataPs.close();		
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {
		return new LoadCompoundCollectionTask(collection);
	}
	
	public CompoundCollection getCollection() {
		return collection;
	}
}
