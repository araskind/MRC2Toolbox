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

package edu.umich.med.mrc2.datoolbox.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.SiriusMsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class MSMSClusteringUtils {
	
	public static final MsFeatureIdentityComparator entropyScoreComparator = 
			new MsFeatureIdentityComparator(SortProperty.msmsEntropyScore, SortDirection.DESC);

	public static String calculateCLusteringParametersMd5(MSMSClusteringParameterSet params){

		List<String> chunks = new ArrayList<String>();
		chunks.add(MsUtils.spectrumMzExportFormat.format(params.getMzErrorValue()));
		chunks.add(params.getMassErrorType().name());
		chunks.add(MsUtils.spectrumMzExportFormat.format(params.getRtErrorValue()));
		chunks.add(MsUtils.spectrumMzExportFormat.format(params.getMsmsSimilarityCutoff()));
	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(StringUtils.join(chunks).getBytes(Charset.forName("windows-1252")));
			return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public static MsFeatureIdentity getTopMSMSLibraryHit(
			Collection<MSFeatureInfoBundle>clusterComponents) {
		
		long numHits = clusterComponents.stream().
			flatMap(f -> f.getMsFeature().getIdentifications().stream()).
			filter(id -> Objects.nonNull(id.getReferenceMsMsLibraryMatch())).
			count();
		if(numHits == 0)
			return null;

		MsFeatureIdentity topHit = null;
		Collection<MsFeatureIdentity>idsToRank = 
				new TreeSet<MsFeatureIdentity>(entropyScoreComparator);
		Collection<MsFeatureIdentity>hybridIdsToRank = 
				new TreeSet<MsFeatureIdentity>(NISTPepSearchUtils.idScoreComparator);
		String metlinLibId = 
				IDTDataCache.getReferenceMsMsLibraryByName("Metlin_AMRT_PCDL").getUniqueId();
			
		Map<String,HiResSearchOption>searchTypeMap = 
				NISTPepSearchUtils.getSearchTypeMap(clusterComponents);	
		List<MsFeature> msFeatures = clusterComponents.stream().
				map(b -> b.getMsFeature()).collect(Collectors.toList());
		
		for(MsFeature msf : msFeatures) {
			
			Map<HiResSearchOption,Collection<MsFeatureIdentity>>hitTypeMap = 
					NISTPepSearchUtils.getSearchTypeIdentityMap(msf, searchTypeMap, true);				
			idsToRank.addAll(hitTypeMap.get(HiResSearchOption.z));
			idsToRank.addAll(hitTypeMap.get(HiResSearchOption.u));
			hybridIdsToRank.addAll(hitTypeMap.get(HiResSearchOption.y));
		}
		List<MsFeatureIdentity> metlinHits = 
				msFeatures.stream().flatMap(f -> f.getIdentifications().stream()).
				filter(id -> Objects.nonNull(id.getReferenceMsMsLibraryMatch())).
				filter(id -> id.getReferenceMsMsLibraryMatch()
						.getMatchedLibraryFeature().getMsmsLibraryIdentifier().equals(metlinLibId)).
				sorted(NISTPepSearchUtils.idScoreComparator).collect(Collectors.toList());		
		idsToRank.addAll(metlinHits);
		
		if(idsToRank.isEmpty())
			topHit = hybridIdsToRank.stream().findFirst().orElse(null);
		else
			topHit = idsToRank.stream().findFirst().orElse(null);
		
		return topHit;
	}
	
	public static Collection<SiriusMsMsCluster>createMultipleSiriusMsClustersFromBinnerAnnotattedCluster(
			BinnerBasedMsFeatureInfoBundleCluster bbc){
		
		Collection<SiriusMsMsCluster>siriusClusters = new ArrayList<SiriusMsMsCluster>();		
		for(Entry<BinnerAnnotation, Set<MSFeatureInfoBundle>> e : bbc.getComponentMap().entrySet()) {
			
			if(e.getValue().size() == 0)
				continue;
			
			MSFeatureInfoBundle[]bundles = 
					e.getValue().toArray(new MSFeatureInfoBundle[e.getValue().size()]);
			MsFeatureInfoBundleCluster c = new MsFeatureInfoBundleCluster(bundles[0]);
			if(bundles.length > 1) {

				for(int i=1; i<bundles.length; i++)
					c.addComponent(null, bundles[i]);
			}
			SiriusMsMsCluster csc = new SiriusMsMsCluster(c);
			Adduct siriusCompatible = 
					AdductManager.getSiriusCompatibleAdductByBinnerAnnotation(e.getKey());
			if(siriusCompatible != null) {
				
				csc.setAdduct(siriusCompatible);
				siriusClusters.add(csc);
			}
		}
		return siriusClusters;
	}
}



















