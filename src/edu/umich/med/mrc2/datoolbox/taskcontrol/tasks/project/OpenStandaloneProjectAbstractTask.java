/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project;

import java.sql.Connection;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.OfflineExperimentLoadCache;
import edu.umich.med.mrc2.datoolbox.project.store.IDTrackerProjectFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;


public abstract class OpenStandaloneProjectAbstractTask extends AbstractTask {

	protected Set<String>uniqueCompoundIds;
	protected Set<String>uniqueMSMSLibraryIds;
	protected Set<String>uniqueMSRTLibraryIds;
	protected Set<String>uniqueSampleIds;
	
	protected void collectIdsForRetrievalFromDatabase(Element experimentElement) {
		
		uniqueCompoundIds = new TreeSet<String>();
		uniqueMSMSLibraryIds = new TreeSet<String>();
		uniqueMSRTLibraryIds = new TreeSet<String>();
		uniqueSampleIds = new TreeSet<String>();
		
		Element compoundIdListElement =  
				experimentElement.getChild(IDTrackerProjectFields.UniqueCIDList.name());
		if(compoundIdListElement != null)
			uniqueCompoundIds.addAll(ProjectUtils.getIdList(compoundIdListElement.getText()));
		
		Element msmsLibIdIdListElement =  
				experimentElement.getChild(IDTrackerProjectFields.UniqueMSMSLibIdList.name());
		if(msmsLibIdIdListElement != null)
			uniqueMSMSLibraryIds.addAll(ProjectUtils.getIdList(msmsLibIdIdListElement.getText()));
		
		Element msRtLibIdListElement =  
				experimentElement.getChild(IDTrackerProjectFields.UniqueMSRTLibIdList.name());
		if(msRtLibIdListElement != null)
			uniqueMSRTLibraryIds.addAll(ProjectUtils.getIdList(msRtLibIdListElement.getText()));
		
		Element sampleIdIdListElement =  
				experimentElement.getChild(IDTrackerProjectFields.UniqueSampleIdList.name());
		if(sampleIdIdListElement != null)
			uniqueSampleIds.addAll(ProjectUtils.getIdList(sampleIdIdListElement.getText()));
	}

	protected void populateDatabaseCacheData() throws Exception {
		
		OfflineExperimentLoadCache.reset();
		Connection conn = ConnectionManager.getConnection();
		if(!uniqueCompoundIds.isEmpty()) {
			try {
				getCompoundIdentities(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!uniqueMSMSLibraryIds.isEmpty()) {
			try {
				getMSMSLibraryEntries(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!uniqueMSRTLibraryIds.isEmpty()) {
			try {
				getMSRTLibraryEntries(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!uniqueSampleIds.isEmpty()) {
			try {
				getExperimentalSamples(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void getCompoundIdentities(Connection conn) throws Exception {
		
		taskDescription = "Populating compound data cache ...";
		total = uniqueCompoundIds.size();
		processed = 0;
		
		//	TODO this is a stopgap solution before ID lookup is all done through compound database
		Pattern pattern = Pattern.compile("RM\\d{7}");
		
		for(String cid : uniqueCompoundIds) {
			
			CompoundIdentity compId = null;
			if(pattern.matcher(cid).matches())
				compId = CompoundDatabaseUtils.getRefMetCompoundById(cid, conn);	
			else				
				compId = CompoundDatabaseUtils.getCompoundById(cid, conn);
			
			if(compId != null)
				OfflineExperimentLoadCache.addCompoundIdentity(compId);
			
			processed++;
		}		
	}

	protected void getMSMSLibraryEntries(Connection conn) throws Exception {
		
		taskDescription = "Populating MSMS library data cache ...";
		total = uniqueMSMSLibraryIds.size();
		processed = 0;		
		for(String libId : uniqueMSMSLibraryIds) {
			
			MsMsLibraryFeature libFeature = 
					MSMSLibraryUtils.getMsMsLibraryFeatureById(libId, conn);
			if(libFeature != null)
				OfflineExperimentLoadCache.addMsMsLibraryFeature(libFeature);
			
			processed++;
		}	
	}
	
	protected void getMSRTLibraryEntries(Connection conn) throws Exception {

		taskDescription = "Populating MS-RT library data cache ...";
		total = uniqueMSRTLibraryIds.size();
		processed = 0;		

		for(String targetId : uniqueMSRTLibraryIds) {
			
			LibraryMsFeatureDbBundle fBundle =  
					MSRTLibraryUtils.createFeatureBundleForFeature(targetId, conn);
			if(fBundle != null) {

				LibraryMsFeature newTarget = fBundle.getFeature();

				//	Add identity
				if(fBundle.getConmpoundDatabaseAccession() != null)
					MSRTLibraryUtils.attachIdentity(
							newTarget, fBundle.getConmpoundDatabaseAccession(), fBundle.isQcStandard(), conn);

				// Attach spectrum
				MSRTLibraryUtils.attachMassSpectrum(newTarget, conn);


				//	Attach annotations
				MSRTLibraryUtils.attachAnnotations(newTarget, conn);
				
				OfflineExperimentLoadCache.addLibraryMsFeatureDbBundle(fBundle);
			}			
			processed++;
		}
	}
	
	protected void getExperimentalSamples(Connection conn) throws Exception {

		Collection<IDTExperimentalSample>samples = 
				IDTUtils.getExperimentalSamples(uniqueSampleIds, conn);
		
		for(IDTExperimentalSample sample :samples)
			OfflineExperimentLoadCache.addExperimentalSample(sample);		
	}
}
