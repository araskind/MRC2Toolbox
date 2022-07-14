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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.tophit.TopHitReassignmentOption;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;

public class DefaultMSMSLibraryHitReassignmentTask extends AbstractTask {
	
	private Collection<MsFeatureInfoBundle>featuresToUpdate;
	private TopHitReassignmentOption topHitReassignmentOption;
	private boolean useEntropyScore;
	private boolean commitChangesToDatabase;
	private String metlinLibId;
	private MSFeatureIdentificationLevel tentativeLevel;
	
	public DefaultMSMSLibraryHitReassignmentTask(
			Collection<MsFeatureInfoBundle> featuresToExport,
			TopHitReassignmentOption topHitReassignmentOption, 
			boolean useEntropyScore,
			boolean commitChangesToDatabase) {
		super();
		this.featuresToUpdate = featuresToExport;
		this.topHitReassignmentOption = topHitReassignmentOption;
		this.useEntropyScore = useEntropyScore;
		this.commitChangesToDatabase = commitChangesToDatabase;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		IDTDataCash.refreshNISTPepSearchParameters();
		metlinLibId = 
				IDTDataCash.getReferenceMsMsLibraryByName("Metlin_AMRT_PCDL").getUniqueId();	
		tentativeLevel = 
				IDTDataCash.getMSFeatureIdentificationLevelById("IDS002");
		try {
			featuresToUpdate = 
				NISTPepSearchUtils.removeLockedFeatures(featuresToUpdate);
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
		if(featuresToUpdate.isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		try {
			reassignDefaultHit();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
		if(commitChangesToDatabase) {
			
			try {
				commitIDChangesToDatabase();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private void reassignDefaultHit() {
		
		taskDescription = "Assigning default MSMS library hit";
		total = featuresToUpdate.size();
		processed = 0;	

		Map<String,HiResSearchOption>searchTypeMap = 
				NISTPepSearchUtils.getSearchTypeMap(featuresToUpdate);			
		
		boolean assigned = false;
		for(MsFeatureInfoBundle bundle : featuresToUpdate) {
			 
			assigned = false;			
			if(topHitReassignmentOption.equals(TopHitReassignmentOption.PREFER_METLIN)) {				
				if(assignMetlinTopHit(bundle))
					continue;
			}
			Map<HiResSearchOption,Collection<MsFeatureIdentity>>hitTypeMap = 
					NISTPepSearchUtils.getSearchTypeIdentityMap(bundle.getMsFeature(), searchTypeMap);				
			if(useEntropyScore) {				
				if(assignTopEntropyHit(bundle, searchTypeMap, hitTypeMap))
					continue;			
			}
			else
				assignNISTTopHit(bundle, searchTypeMap, hitTypeMap);
	
			processed++;
		}	
	}
	
	private boolean assignTopEntropyHit(
			MsFeatureInfoBundle bundle,
			Map<String,HiResSearchOption>searchTypeMap,
			Map<HiResSearchOption, Collection<MsFeatureIdentity>> hitTypeMap) {
		// TODO Auto-generated method stub
		
		
		return false;
	}

	private void assignNISTTopHit(
			MsFeatureInfoBundle bundle, 
			Map<String,HiResSearchOption>searchTypeMap, 			
			Map<HiResSearchOption,Collection<MsFeatureIdentity>>hitTypeMap) {		
		
		MsFeatureIdentity topNormalHit = null;
		MsFeatureIdentity topInSourceHit = null;
		MsFeatureIdentity topHybridHit = null;
		
		if(!hitTypeMap.get(HiResSearchOption.z).isEmpty())
			topNormalHit = hitTypeMap.get(HiResSearchOption.z).iterator().next();
		
		if(!hitTypeMap.get(HiResSearchOption.u).isEmpty()) 
			topInSourceHit = hitTypeMap.get(HiResSearchOption.u).iterator().next();
		
		if(!hitTypeMap.get(HiResSearchOption.y).isEmpty())
			topHybridHit = hitTypeMap.get(HiResSearchOption.y).iterator().next();
		
		if(topHitReassignmentOption.equals(TopHitReassignmentOption.PREFER_NORMAL_HITS)) {
			
			if(topNormalHit != null) {
				bundle.getMsFeature().setPrimaryIdentity(topNormalHit);
			}
			else {
				if(topInSourceHit != null) {
					bundle.getMsFeature().setPrimaryIdentity(topInSourceHit);
				}
				else {
					if(topHybridHit != null)
						bundle.getMsFeature().setPrimaryIdentity(topHybridHit);
				}
			}
		}
		if(topHitReassignmentOption.equals(TopHitReassignmentOption.ALLOW_IN_SOURCE_HITS)) {
			
			if(topNormalHit != null)
				bundle.getMsFeature().setPrimaryIdentity(topNormalHit);
			
			if(topInSourceHit != null) {
	
				if(topNormalHit == null) {
					bundle.getMsFeature().setPrimaryIdentity(topInSourceHit);
				}
				else {
					if(topInSourceHit.getReferenceMsMsLibraryMatch().getScore() > 
							topNormalHit.getReferenceMsMsLibraryMatch().getScore())
						bundle.getMsFeature().setPrimaryIdentity(topInSourceHit);
				}
			}
			if(topInSourceHit == null && topNormalHit == null && topHybridHit != null) 
					bundle.getMsFeature().setPrimaryIdentity(topHybridHit);									
		}
		if(topHitReassignmentOption.equals(TopHitReassignmentOption.ALLOW_HYBRID_HITS)) {
			
			TreeSet<MsFeatureIdentity>topHits = 
					new TreeSet<MsFeatureIdentity>(NISTPepSearchUtils.idScoreComparator);
			if(topNormalHit != null)
				topHits.add(topNormalHit);
			
			if(topInSourceHit != null)
				topHits.add(topInSourceHit);
			
			if(topHybridHit != null)
				topHits.add(topHybridHit);
			
			bundle.getMsFeature().setPrimaryIdentity(topHits.iterator().next());
		}
	}
	
	private boolean assignMetlinTopHit(MsFeatureInfoBundle bundle) {
			
		 List<MsFeatureIdentity>metlinHits = 
					bundle.getMsFeature().getIdentifications().stream().
					filter(id -> id.getReferenceMsMsLibraryMatch() != null).
					filter(id -> id.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getMsmsLibraryIdentifier().equals(metlinLibId)).
					sorted(NISTPepSearchUtils.idScoreComparator).
					collect(Collectors.toList());
		 
		if(!metlinHits.isEmpty()) {			
			bundle.getMsFeature().setPrimaryIdentity(metlinHits.get(0));
			return true;
		}
		else
			return false;
	}
	
	private void commitIDChangesToDatabase() throws Exception {

		taskDescription = "Writing changes to database ...";
		total = featuresToUpdate.size();
		processed = 0;	
		Connection conn = ConnectionManager.getConnection();
		
		for(MsFeatureInfoBundle bundle : featuresToUpdate) {
			
			MsFeatureIdentity primaryId = bundle.getMsFeature().getPrimaryIdentity();
			if(primaryId.getIdentificationLevel() == null)
				primaryId.setIdentificationLevel(tentativeLevel);
			
			TandemMassSpectrum msmsFeature = 
					bundle.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
			try {
				IdentificationUtils.setMSMSFeaturePrimaryIdentity(msmsFeature.getId(), primaryId, conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processed++;
		}	
		ConnectionManager.releaseConnection(conn);
	}
	
	@Override
	public Task cloneTask() {

		return new DefaultMSMSLibraryHitReassignmentTask(
				 featuresToUpdate,
				 topHitReassignmentOption,
				 useEntropyScore,
				 commitChangesToDatabase);
	}
}
