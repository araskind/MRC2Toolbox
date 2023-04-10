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
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;

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
	
	public static MsFeatureIdentity getTopMSMSLibraryHit(MsFeatureInfoBundleCluster cluster) {
		
		long numHits = cluster.getComponents().stream().
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
				NISTPepSearchUtils.getSearchTypeMap(cluster.getComponents());	
		List<MsFeature> msFeatures = cluster.getComponents().stream().
				map(b -> b.getMsFeature()).collect(Collectors.toList());
		
		for(MsFeature msf : msFeatures) {
			
			Map<HiResSearchOption,Collection<MsFeatureIdentity>>hitTypeMap = 
					NISTPepSearchUtils.getSearchTypeIdentityMap(msf, searchTypeMap);				
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
}
