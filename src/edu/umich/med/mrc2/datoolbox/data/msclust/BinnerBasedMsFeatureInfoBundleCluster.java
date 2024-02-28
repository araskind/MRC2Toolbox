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

package edu.umich.med.mrc2.datoolbox.data.msclust;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.MajorClusterFeatureDefiningProperty;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsFeatureStatsUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class BinnerBasedMsFeatureInfoBundleCluster implements IMsFeatureInfoBundleCluster{

	private String id;
	private String name;	
	private BinnerAnnotationCluster binnerAnnotationCluster;
	private Map<BinnerAnnotation,Set<MSFeatureInfoBundle>>componentMap;
	private Map<BinnerAnnotation,Double>mzMap;
	private Map<BinnerAnnotation,Double>rtMap;
	private Map<BinnerAnnotation,Double>medianAreaMap;
	private MsFeatureIdentity primaryIdentity;
	private boolean locked;
	private Collection<String>featureIds;
	
	public BinnerBasedMsFeatureInfoBundleCluster(
			BinnerAnnotationCluster binnerAnnotationCluster) {
		
		this.binnerAnnotationCluster = binnerAnnotationCluster;
		this.id = DataPrefix.BINNER_MSMS_CLUSTER.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
		this.name = binnerAnnotationCluster.toString();
		componentMap = new TreeMap<BinnerAnnotation,Set<MSFeatureInfoBundle>>();
		mzMap = new TreeMap<BinnerAnnotation,Double>();
		rtMap = new TreeMap<BinnerAnnotation,Double>();
		medianAreaMap = new TreeMap<BinnerAnnotation,Double>();
		for(BinnerAnnotation ba : binnerAnnotationCluster.getAnnotations()) {
			
			componentMap.put(ba, new HashSet<MSFeatureInfoBundle>());
			mzMap.put(ba, ba.getBinnerMz());
			rtMap.put(ba, ba.getBinnerRt());
			medianAreaMap.put(ba, 0.0d);
		}
	}
	
	public Collection<MSFeatureInfoBundle>getComponents(){
		return componentMap.values().stream().
				flatMap(v -> v.stream()).collect(Collectors.toList());
	}
	
	public long getFeatureNumber() {		
		return componentMap.values().stream().flatMap(v -> v.stream()).count();
	}
	
	private void updateName() {
		
		primaryIdentity = 
				MSMSClusteringUtils.getTopMSMSLibraryHit(getComponents());
		updateNameFromPrimaryIdentity();
	}
	
	public void updateNameFromPrimaryIdentity() {
		
		BinnerAnnotation ba = binnerAnnotationCluster.getPrimaryFeatureAnnotation();
		String mzRtName = "MZ " + MRC2ToolBoxConfiguration.getMzFormat().format(ba.getBinnerMz()) + 
				" | RT " + MRC2ToolBoxConfiguration.getRtFormat().format(ba.getBinnerRt());
		
		if(primaryIdentity != null && primaryIdentity.getCompoundIdentity() != null)
			name = primaryIdentity.getCompoundName() + " | " + mzRtName;
		else 
			name = binnerAnnotationCluster.toString();
		
		long fNumber = getFeatureNumber();
		if(fNumber > 1)
			name += " [" + Long.toString(fNumber) + "]";
	}
	
	public boolean isIdentified() {
		
		if(primaryIdentity != null && primaryIdentity.getCompoundIdentity() != null)
			return true;
		else
			return false;
	}
	
	public void replaceStoredPrimaryIdentityFromFeatures() {
		
		if(primaryIdentity == null) {
			updateNameFromPrimaryIdentity();
			return;
		}
		String uniquePrimaryId = primaryIdentity.getUniqueId();		
		primaryIdentity = getComponents().stream().
				flatMap(c -> c.getMsFeature().getIdentifications().stream()).
				filter(i -> i.getUniqueId().equals(uniquePrimaryId)).findFirst().orElse(null);
		updateNameFromPrimaryIdentity();
	}
	
	public void addComponent(BinnerAnnotation ba, MSFeatureInfoBundle newComponent) {
		componentMap.get(ba).add(newComponent);		
		updateStats(ba);
		updateName();
	}

	public void removeComponent(BinnerAnnotation ba, MSFeatureInfoBundle toRemove) {
		componentMap.get(ba).remove(toRemove);
		updateStats(ba);
		updateName();
	}
	
	private void updateStats(BinnerAnnotation ba) {
		
		double mz = MsFeatureStatsUtils.getMedianParentIonMzForFeatureCollection(componentMap.get(ba));
		mzMap.put(ba, mz);
		
		double rt = MsFeatureStatsUtils.getMedianRtForFeatureCollection(componentMap.get(ba));
		rtMap.put(ba, rt);
		
		double medianArea = MsFeatureStatsUtils.getMedianMSMSAreaForFeatureCollection(componentMap.get(ba));
		medianAreaMap.put(ba, medianArea);
	}
	
	public String getId() {
		return id;
	}

	public MsFeatureIdentity getPrimaryIdentity() {
		return primaryIdentity;
	}

	public void setPrimaryIdentity(MsFeatureIdentity primaryIdentity) {
		this.primaryIdentity = primaryIdentity;
		updateNameFromPrimaryIdentity();
	}

   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!BinnerBasedMsFeatureInfoBundleCluster.class.isAssignableFrom(obj.getClass()))
            return false;

        final BinnerBasedMsFeatureInfoBundleCluster other = (BinnerBasedMsFeatureInfoBundleCluster) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean addNewBundle(
			BinnerAnnotation ba,
			MSFeatureInfoBundle b, 
			MSMSClusteringParameterSet params) {
		
		TandemMassSpectrum msms = 
				b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
		if(msms == null || msms.getParent() == null)
			return false;
		
		if(componentMap.get(ba).isEmpty()) {
			addComponent(ba, b);
			return true;
		}
		Range rtRange = new Range(
				ba.getBinnerRt() - params.getRtErrorValue(), 
				ba.getBinnerRt() + params.getRtErrorValue());
		if(!rtRange.contains(b.getRetentionTime()))
			return false;
		
		Range mzRange = MsUtils.createMassRange(
				ba.getBinnerMz(), params.getMzErrorValue(), params.getMassErrorType());
		if(!mzRange.contains(msms.getParent().getMz()))
			return false;
		
		boolean spectrumMatches = false;
		for(MSFeatureInfoBundle component : componentMap.get(ba)) {
			
			Collection<MsPoint>refMsMs = component.getMsFeature().getSpectrum().
					getExperimentalTandemSpectrum().getSpectrum();		
			double score = MSMSScoreCalculator.calculateEntropyBasedMatchScore(
					msms.getSpectrum(), refMsMs, params.getMzErrorValue(), params.getMassErrorType(), 
					MSMSScoreCalculator.DEFAULT_MS_REL_INT_NOISE_CUTOFF);
			if(score > params.getMsmsSimilarityCutoff()) {
				addComponent(ba,b);
				spectrumMatches = true;
				break;
			}
		}
		return spectrumMatches;
	}

	public Element getXmlElement() {

		//	TODO
		
//		Element msmsClusterElement = 
//				new Element(MsFeatureInfoBundleClusterFields.MsFeatureInfoBundleCluster.name());
//		msmsClusterElement.setAttribute(
//				MsFeatureInfoBundleClusterFields.Id.name(), id);	
////		msmsClusterElement.setAttribute(
////				MsFeatureInfoBundleClusterFields.Name.name(), name);
//		msmsClusterElement.setAttribute(
//				MsFeatureInfoBundleClusterFields.MZ.name(), Double.toString(mz));
//		msmsClusterElement.setAttribute(
//				MsFeatureInfoBundleClusterFields.RT.name(), Double.toString(rt));
//		msmsClusterElement.setAttribute(
//				MsFeatureInfoBundleClusterFields.MedianArea.name(), Double.toString(medianArea));
//		msmsClusterElement.setAttribute(
//				MsFeatureInfoBundleClusterFields.IsLocked.name(), Boolean.toString(locked));
//		
//		Collection<String>componentFeatureIds = components.stream().
//				map(c -> c.getMSFeatureId()).collect(Collectors.toSet());
//		msmsClusterElement.addContent(       		
//        		new Element(MsFeatureInfoBundleClusterFields.FeatureIdList.name()).
//        		setText(StringUtils.join(componentFeatureIds, ",")));
//		
//		if(primaryIdentity != null)
//			msmsClusterElement.addContent(primaryIdentity.getXmlElement());
//		
//		return msmsClusterElement;
		
		return null;
	}
	
	public BinnerBasedMsFeatureInfoBundleCluster(Element clusterElement) {
		
		//	TODO 
//		id = clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.Id.name());
//		//	name = clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.Name.name());
//		mz = Double.parseDouble(
//				clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.MZ.name()));
//		rt = Double.parseDouble(
//				clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.RT.name()));
//		medianArea = Double.parseDouble(
//				clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.MedianArea.name()));
//		locked = Boolean.parseBoolean(
//				clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.IsLocked.name()));
//		
//		featureIds = new TreeSet<String>();
//		String compoundIdList = 
//				clusterElement.getChild(
//						MsFeatureInfoBundleClusterFields.FeatureIdList.name()).getText();
//		featureIds.addAll(ExperimentUtils.getIdList(compoundIdList));
//		Element primaryIdElement = 
//				clusterElement.getChild(MsFeatureIdentityFields.MSFID.name());
//		if(primaryIdElement != null)
//			primaryIdentity = new MsFeatureIdentity(primaryIdElement);
	}

	public Collection<String> getFeatureIds() {
		
		if(featureIds == null)
			featureIds = new TreeSet<String>();
			
		return featureIds;
	}
	
//	public void setFeatures(Collection<MSFeatureInfoBundle> featureBundles) {
//		components = new HashSet<MSFeatureInfoBundle>();
//		components.addAll(featureBundles);
//	}

	public boolean hasAnnotations() {

		long saCount = getComponents().stream().
				flatMap(b -> b.getStandadAnnotations().stream()).count();
		long aCount =  getComponents().stream().
				flatMap(b -> b.getMsFeature().getAnnotations().stream()).count();
		return (saCount + aCount) > 0;
	}

	public boolean hasIdFollowupSteps() {

		return  getComponents().stream().
					flatMap(b -> b.getIdFollowupSteps().stream()).count() > 0;
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleForPrimaryId() {
		
		if(primaryIdentity == null)
			return null;
		
		return  getComponents().stream().
			filter(c -> c.getMsFeature().getIdentifications().contains(primaryIdentity)).
			findFirst().orElse(null);
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleWithLargestMSMSArea() {
		
		return  getComponents().stream().
				sorted(new MsFeatureInfoBundleComparator(SortProperty.msmsIntensity, SortDirection.DESC)).
				findFirst().orElse(null);
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleWithHihgestMSMSScore(boolean includeInSourceHits) {
		
		MsFeatureIdentity bestId = null;
		List<MsFeatureIdentity> allIds =  getComponents().stream().
				flatMap(c -> c.getMsFeature().getIdentifications().stream()).
				filter(id -> Objects.nonNull(id.getReferenceMsMsLibraryMatch())).
				collect(Collectors.toList());
		if(includeInSourceHits) {
			bestId = allIds.stream().
				filter(id -> !id.getReferenceMsMsLibraryMatch().getMatchType().equals(MSMSMatchType.Hybrid)).
				sorted(new MsFeatureIdentityComparator(SortProperty.msmsEntropyScore, SortDirection.DESC)).
				findFirst().orElse(null);
		}
		else {
			bestId = allIds.stream().
					filter(id -> id.getReferenceMsMsLibraryMatch().getMatchType().equals(MSMSMatchType.Regular)).
					sorted(new MsFeatureIdentityComparator(SortProperty.msmsEntropyScore, SortDirection.DESC)).
					findFirst().orElse(null);
		}
		if(bestId != null) {
			
			final MsFeatureIdentity lookupId = bestId;
			return  getComponents().stream().
					filter(c -> c.getMsFeature().getIdentifications().contains(lookupId)).
					findFirst().orElse(null);
		}
		else
			return null;
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleWithSmallestParentIonMassError(BinnerAnnotation ba) {
		
		double mz = ba.getBinnerMz();		
		double initError = 1000.0d;
		MSFeatureInfoBundle bestHit = null;
		for(MSFeatureInfoBundle b : componentMap.get(ba)) {
			
			TandemMassSpectrum msms = 
					b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
			if(msms != null && msms.getParent() != null) {
				
				double error = Math.abs(msms.getParent().getMz() - mz);
				if(error < initError) {
					initError = error;
					bestHit = b;
				}
			}			
		}		
		return bestHit;
	}
	
	public MSFeatureInfoBundle getDefiningFeature(MajorClusterFeatureDefiningProperty property) {
		
		if(property.equals(MajorClusterFeatureDefiningProperty.LARGEST_AREA))
			return getMSFeatureInfoBundleWithLargestMSMSArea();
		
		if(property.equals(MajorClusterFeatureDefiningProperty.HIGHEST_MSMS_SCORE))
			return getMSFeatureInfoBundleWithHihgestMSMSScore(false);
		
		if(property.equals(MajorClusterFeatureDefiningProperty.HIGHEST_MSMS_SCORE_WITH_IN_SOURCE))
			return getMSFeatureInfoBundleWithHihgestMSMSScore(true);
		
		if(property.equals(MajorClusterFeatureDefiningProperty.SMALLEST_MASS_ERROR))
			return getMSFeatureInfoBundleWithSmallestParentIonMassError(
					binnerAnnotationCluster.getPrimaryFeatureAnnotation());
		
		if(property.equals(MajorClusterFeatureDefiningProperty.CURRENT_PRIMARY_ID))
				return getMSFeatureInfoBundleForPrimaryId();
		
		return null;
	}

	public BinnerAnnotationCluster getBinnerAnnotationCluster() {
		return binnerAnnotationCluster;
	}
	
	@Override
	public double getMz() {
		return mzMap.get(binnerAnnotationCluster.getPrimaryFeatureAnnotation());
	}

	@Override
	public double getRt() {
		return rtMap.get(binnerAnnotationCluster.getPrimaryFeatureAnnotation());
	}

	@Override
	public double getMedianArea() {
		return medianAreaMap.get(binnerAnnotationCluster.getPrimaryFeatureAnnotation());
	}

	@Override
	public double getRank() {
		return 0;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public MinimalMSOneFeature getLookupFeature() {
		return null;
	}

	@Override
	public void setFeatures(Collection<MSFeatureInfoBundle> featureBundlesForIds) {
		// TODO Auto-generated method stub
		
	}

	public Map<BinnerAnnotation, Set<MSFeatureInfoBundle>> getComponentMap() {
		return componentMap;
	}

	@Override
	public void setLookupFeature(MinimalMSOneFeature lf) {
		// TODO Auto-generated method stub
		
	}
	
	public BinnerAnnotation getAnnotationForFeature(MSFeatureInfoBundle bundle) {
		
		for(Entry<BinnerAnnotation, Set<MSFeatureInfoBundle>> e : componentMap.entrySet()) {
			
			if(e.getValue().contains(bundle))
				return e.getKey();
		}
		return null;
	}
	
	public int getDetectedAnnotationsCount() {
		
		int count = 0;
		for(Entry<BinnerAnnotation, Set<MSFeatureInfoBundle>>e : componentMap.entrySet()) {
			
			if(!e.getValue().isEmpty())
				count++;
		}
		return count;
	}

	@Override
	public Collection<MSFeatureInfoBundle> containsFeaturesWithinRanges(
			Range rtRange, Range mzRange) {

		return getComponents().stream().
				filter(f -> rtRange.contains(f.getRetentionTime())).
				filter(f -> mzRange.contains(f.getPrecursorMz())).
				collect(Collectors.toList());
	}
}












