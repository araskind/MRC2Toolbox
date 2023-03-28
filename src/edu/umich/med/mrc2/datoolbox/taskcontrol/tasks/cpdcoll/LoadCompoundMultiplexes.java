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
import java.util.List;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollectionComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixtureComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpdcol.CompoundMultiplexUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LoadCompoundMultiplexes extends AbstractTask {
	
	private Collection<CompoundMultiplexMixture>multiplexes;
	private Collection<CpdMetadataField> metadataFields;
	private Collection<MobilePhase>solvents;

	public LoadCompoundMultiplexes() {
		super();
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
		}
		try {
			solvents  = CompoundMultiplexUtils.getSolventList();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
		}		
		try {
			multiplexes = CompoundMultiplexUtils.getCompoundMultiplexMixtureList();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
		}
		try {
			getMultiplexComponents();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
		}
		try {
			getCompoundComponents();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void getMultiplexComponents() throws Exception{
		
		taskDescription = "Getting multiplex mixtures components ... ";	
		total = multiplexes.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT CC_COMPONENT_ID, CPD_CONC_MKM, "
				+ "SOLVENT_ID, XLOGP, ALIQUOTE_VOLUME "
				+ "FROM COMPOUND_MULTIPLEX_MIXTURE_COMPONENTS "
				+ "WHERE MIX_ID = ? ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		for(CompoundMultiplexMixture mplex : multiplexes) {
			
			ps.setString(1, mplex.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				String solventId = rs.getString("SOLVENT_ID");
				MobilePhase solvent = solvents.stream().
						filter(s -> s.getId().equals(solventId)).
						findFirst().orElse(null);
				CompoundMultiplexMixtureComponent cmmc = 
						new CompoundMultiplexMixtureComponent(
								rs.getString("CC_COMPONENT_ID"), 
								rs.getDouble("CPD_CONC_MKM"), 
								solvent,
								rs.getDouble("XLOGP"), 
								rs.getDouble("ALIQUOTE_VOLUME"));
				mplex.addComponent(cmmc);
			}
			rs.close();
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	private void getCompoundComponents() throws Exception{

		taskDescription = "Getting compound collection components ... ";
		List<CompoundMultiplexMixtureComponent> compList = 
				multiplexes.stream().
				flatMap(m -> m.getComponents().stream()).
				collect(Collectors.toList());
		List<String> ccidList = compList.stream().
				map(c -> c.getCccid()).distinct().
				sorted().collect(Collectors.toList());
		total = ccidList.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT CC_ID, CAS, ACCESSION, NAME, SOURCE_DB FROM "
				+ "COMPOUND_COLLECTION_COMPONENTS WHERE CC_COMPONENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String metadataQueryuery = 
				"SELECT FIELD_ID, FIELD_VALUE FROM "
				+ "COMPOUND_COLLECTION_COMPONENT_METADATA WHERE CC_COMPONENT_ID = ?";
		PreparedStatement metadataPs = conn.prepareStatement(metadataQueryuery);
		ResultSet mdrs = null;
		ResultSet rs = null;
		for(String ccid : ccidList) {
			
			ps.setString(1, ccid);
			rs = ps.executeQuery();
			while(rs.next()) {
				
				CompoundCollectionComponent component = 
						new CompoundCollectionComponent(
								ccid, rs.getString("CC_ID"), rs.getString("CAS"));
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
				compList.stream().filter(c -> c.getCccid().equals(ccid)).
					forEach(c -> c.setCCComponent(component));
				processed++;
			}
			rs.close();
		}

		ps.close();
		metadataPs.close();		
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {
		return new LoadCompoundMultiplexes();
	}

	public Collection<CompoundMultiplexMixture> getMultiplexes() {
		return multiplexes;
	}
}
