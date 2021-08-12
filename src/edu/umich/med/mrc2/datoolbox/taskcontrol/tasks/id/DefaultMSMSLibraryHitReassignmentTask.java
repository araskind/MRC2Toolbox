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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.tophit.TopHitReassignmentOption;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class DefaultMSMSLibraryHitReassignmentTask extends AbstractTask {
	
	private Collection<MsFeatureInfoBundle>featuresToUpdate;
	private TopHitReassignmentOption topHitReassignmentOption;
	private boolean commitChangesToDatabase;
	private static final MsFeatureIdentityComparator idScoreComparator = 
			new MsFeatureIdentityComparator(SortProperty.msmsScore, SortDirection.DESC);
	
	public DefaultMSMSLibraryHitReassignmentTask(
			Collection<MsFeatureInfoBundle> featuresToExport,
			TopHitReassignmentOption topHitReassignmentOption, 
			boolean commitChangesToDatabase) {
		super();
		this.featuresToUpdate = featuresToExport;
		this.topHitReassignmentOption = topHitReassignmentOption;
		this.commitChangesToDatabase = commitChangesToDatabase;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		IDTDataCash.refreshNISTPepSearchParameters();
		try {
			removeLockedFeatures();
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
	}
	
	private void reassignDefaultHit() throws Exception {
		
		taskDescription = "Assigning default MSMS library hit";
		total = featuresToUpdate.size();
		processed = 0;	
		Connection conn = ConnectionManager.getConnection();

		Map<String,HiResSearchOption>searchTypeMap = getSearchTypeMap();	
		String metlinLibId = IDTDataCash.getReferenceMsMsLibraryByName("Metlin_AMRT_PCDL").getUniqueId();		
		MSFeatureIdentificationLevel tentativeLevel = IDTDataCash.getMSFeatureIdentificationLevelById("IDS002");
				
		for(MsFeatureInfoBundle bundle : featuresToUpdate) {
			
			 List<MsFeatureIdentity>metlinHits = 
						bundle.getMsFeature().getIdentifications().stream().
						filter(id -> id.getReferenceMsMsLibraryMatch() != null).
						filter(id -> id.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getMsmsLibraryIdentifier().equals(metlinLibId)).
						sorted(idScoreComparator).
						collect(Collectors.toList());
			 
			if(topHitReassignmentOption.equals(TopHitReassignmentOption.PREFER_METLIN)  
					&& !metlinHits.isEmpty()) {				
				bundle.getMsFeature().setPrimaryIdentity(metlinHits.get(0));
			}
			else {
				Map<HiResSearchOption,Collection<MsFeatureIdentity>>hitTypeMap = 
						getSearchTypeIdentityMap(bundle.getMsFeature(), searchTypeMap);
				
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
					
					TreeSet<MsFeatureIdentity>topHits = new TreeSet<MsFeatureIdentity>(idScoreComparator);
					if(topNormalHit != null)
						topHits.add(topNormalHit);
					
					if(topInSourceHit != null)
						topHits.add(topInSourceHit);
					
					if(topHybridHit != null)
						topHits.add(topHybridHit);
					
					bundle.getMsFeature().setPrimaryIdentity(topHits.iterator().next());
				}
			}	
			MsFeatureIdentity primaryId = bundle.getMsFeature().getPrimaryIdentity();
			if(primaryId.getIdentificationLevel() == null)
				primaryId.setIdentificationLevel(tentativeLevel);
			
			if(commitChangesToDatabase) {
				TandemMassSpectrum msmsFeature = 
						bundle.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
				try {
					IdentificationUtils.setMSMSFeaturePrimaryIdentity(msmsFeature.getId(), primaryId, conn);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			processed++;
		}	
		ConnectionManager.releaseConnection(conn);
	}
	
	private Map<HiResSearchOption,Collection<MsFeatureIdentity>>getSearchTypeIdentityMap(
			MsFeature feature, 
			Map<String,HiResSearchOption>searchTypeMap) {
		
		Map<HiResSearchOption,Collection<MsFeatureIdentity>>typeMap = 
				new TreeMap<HiResSearchOption,Collection<MsFeatureIdentity>>();
		for(HiResSearchOption o : HiResSearchOption.values())
			typeMap.put(o, new TreeSet<MsFeatureIdentity>(idScoreComparator));
		
		List<MsFeatureIdentity> nistSearchHits = feature.getIdentifications().stream().
			filter(i -> i.getReferenceMsMsLibraryMatch() != null).
			filter(i -> i.getReferenceMsMsLibraryMatch().getSearchParameterSetId() != null).
			collect(Collectors.toList());
		
		for(MsFeatureIdentity hit : nistSearchHits) {
			
			String parSetId = hit.getReferenceMsMsLibraryMatch().getSearchParameterSetId();
			typeMap.get(searchTypeMap.get(parSetId)).add(hit);	
		}		
		return typeMap;
	}
	
	private Map<String,HiResSearchOption>getSearchTypeMap(){
		
		Set<String> searchParamSet = featuresToUpdate.stream().
				flatMap(f -> f.getMsFeature().getIdentifications().stream()).
				filter(i -> i.getReferenceMsMsLibraryMatch() != null).
				filter(i -> i.getReferenceMsMsLibraryMatch().getSearchParameterSetId() != null).
				map(i -> i.getReferenceMsMsLibraryMatch().getSearchParameterSetId()).collect(Collectors.toSet());
			
		Map<String,HiResSearchOption>searchTypeMap = 
					new TreeMap<String,HiResSearchOption>();
		for(String spId : searchParamSet) {
			NISTPepSearchParameterObject pepSearchParams = 
					IDTDataCash.getNISTPepSearchParameterObjectById(spId);
			searchTypeMap.put(spId, pepSearchParams.getHiResSearchOption());
		}
		return searchTypeMap;
	}
	
	private void removeLockedFeatures() {
		
		List<MsFeatureInfoBundle> filteredFeatures = featuresToUpdate.stream().
			filter(f -> f.getMsFeature().getPrimaryIdentity() != null).
			filter(f -> f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch() != null).			
			filter(f -> f.getMsFeature().getMSMSLibraryMatchCount() > 1).
			filter(f -> f.getIdFollowupSteps().isEmpty()).
			filter(f -> f.getStandadAnnotations().isEmpty()).
			filter(f -> f.getMsFeature().getAnnotations().isEmpty()).
			filter(f -> f.getMsFeature().getPrimaryIdentity().getAssignedBy() == null).
			filter(f -> (f.getMsFeature().getPrimaryIdentity().getIdentificationLevel() == null 
				|| f.getMsFeature().getPrimaryIdentity().getIdentificationLevel().getId().equals("IDS002"))).
			collect(Collectors.toList());
		
		featuresToUpdate = filteredFeatures;
	}
	
	@Override
	public Task cloneTask() {

		return new DefaultMSMSLibraryHitReassignmentTask(
				 featuresToUpdate,
				 topHitReassignmentOption,
				 commitChangesToDatabase);
	}
}
