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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
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
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureIdentityFields;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureInfoBundleClusterFields;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsFeatureStatsUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsFeatureInfoBundleCluster implements IMsFeatureInfoBundleCluster{

	private String id;
	private String name;
	private Set<MSFeatureInfoBundle>components;
	private double mz;
	private double rt;
	private double medianArea;
	private MsFeatureIdentity primaryIdentity;
	private boolean locked;
	private Collection<String>featureIds;
	private MinimalMSOneFeature lookupFeature;
	
	public MsFeatureInfoBundleCluster() {
		this(null, 0.0d, 0.0d, null);
		this.id = DataPrefix.MSMS_CLUSTER.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
	}
	
	public MsFeatureInfoBundleCluster(MinimalMSOneFeature parentFeature) {
		this(null, parentFeature.getMz(), parentFeature.getRt(), null);
		this.id = DataPrefix.MSMS_CLUSTER.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
	}
	
	public MsFeatureInfoBundleCluster(
			String id, 
			double mz, 
			double rt, 
			MsFeatureIdentity prinmaryIdentity) {
		super();
		this.id = id;
		this.mz = mz;
		this.rt = rt;
		this.primaryIdentity = prinmaryIdentity;
		components = new HashSet<MSFeatureInfoBundle>();
		updateName();
	}
	
	public MsFeatureInfoBundleCluster(MSFeatureInfoBundle b) {
		this();
		addComponent(null, b);
	}

	private void updateName() {
		
		String mzRtName = null;
		if(mz > 0.0d && rt > 0.0d)
			mzRtName = "MZ " + MRC2ToolBoxConfiguration.getMzFormat().format(mz) + 
				" | RT " + MRC2ToolBoxConfiguration.getRtFormat().format(rt);
		
		MsFeatureIdentity newId = 
				MSMSClusteringUtils.getTopMSMSLibraryHit(this.getComponents());
		if(newId != null)
			primaryIdentity = newId;
		
		if(primaryIdentity != null) {
			name = primaryIdentity.getCompoundName();
			if(mzRtName != null)
				name += " | " + mzRtName;
		}
		else {
			if(mzRtName != null)
				name = mzRtName;
			else
				name = id;
		}
		if(components.size() > 1)
			name += " [" + Integer.toString(components.size()) + "]";
	}
	
	public void updateNameFromPrimaryIdentity() {
		
		String mzRtName = null;
		if(mz > 0.0d && rt > 0.0d)
			mzRtName = "MZ " + MRC2ToolBoxConfiguration.getMzFormat().format(mz) + 
				" | RT " + MRC2ToolBoxConfiguration.getRtFormat().format(rt);
		
		if(primaryIdentity != null) {
			name = primaryIdentity.getCompoundName();
			if(mzRtName != null)
				name += " | " + mzRtName;
		}
		else {
			if(mzRtName != null)
				name = mzRtName;
			else
				name = id;
		}
		if(components.size() > 1)
			name += " [" + Integer.toString(components.size()) + "]";
	}
	
	public void replaceStoredPrimaryIdentityFromFeatures() {
		
		if(primaryIdentity == null) {
			updateNameFromPrimaryIdentity();
			return;
		}
		String uniquePrimaryId = primaryIdentity.getUniqueId();		
		primaryIdentity = components.stream().
				flatMap(c -> c.getMsFeature().getIdentifications().stream()).
				filter(i -> i.getUniqueId().equals(uniquePrimaryId)).findFirst().orElse(null);
		updateNameFromPrimaryIdentity();
	}
	
	@Override
	public void addComponent(BinnerAnnotation ba, MSFeatureInfoBundle newComponent) {
		components.add(newComponent);		
		updateStats();
		updateName();
	}

	@Override
	public void removeComponent(BinnerAnnotation ba, MSFeatureInfoBundle toRemove) {
		components.remove(toRemove);
		updateStats();
		updateName();
	}
	
	private void updateStats() {
		
		mz = MsFeatureStatsUtils.getMedianParentIonMzForFeatureCollection(components);
		rt = MsFeatureStatsUtils.getMedianRtForFeatureCollection(components);
		medianArea = MsFeatureStatsUtils.getMedianMSMSAreaForFeatureCollection(components);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getMz() {
		return mz;
	}

	public void setMz(double mz) {
		this.mz = mz;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public MsFeatureIdentity getPrimaryIdentity() {
		return primaryIdentity;
	}

	public void setPrimaryIdentity(MsFeatureIdentity primaryIdentity) {
		this.primaryIdentity = primaryIdentity;
		updateNameFromPrimaryIdentity();
	}

	public Set<MSFeatureInfoBundle> getComponents() {
		return components;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MsFeatureInfoBundleCluster.class.isAssignableFrom(obj.getClass()))
            return false;

        final MsFeatureInfoBundleCluster other = (MsFeatureInfoBundleCluster) obj;

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

//	public void setName(String name) {
//		this.name = name;
//	}
	
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

	public double getMedianArea() {
		return medianArea;
	}

	@Override
	public boolean addNewBundle(
			BinnerAnnotation ba,
			MSFeatureInfoBundle b, 
			MSMSClusteringParameterSet params) {
		
		TandemMassSpectrum msms = 
				b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
		if(msms == null || msms.getParent() == null)
			return false;
		
		if(components.isEmpty()) {
			addComponent(null, b);
			return true;
		}
		Range rtRange = new Range(
				rt - params.getRtErrorValue(), 
				rt + params.getRtErrorValue());
		if(!rtRange.contains(b.getRetentionTime()))
			return false;
		
		Range mzRange = MsUtils.createMassRange(
				mz, params.getMzErrorValue(), params.getMassErrorType());
		if(!mzRange.contains(msms.getParent().getMz()))
			return false;
		
		boolean spectrumMatches = false;
		for(MSFeatureInfoBundle component : components) {
			
			Collection<MsPoint>refMsMs = component.getMsFeature().getSpectrum().
					getExperimentalTandemSpectrum().getSpectrum();		
			double score = MSMSScoreCalculator.calculateEntropyBasedMatchScore(
					msms.getSpectrum(), refMsMs, params.getMzErrorValue(), params.getMassErrorType(), 
					MSMSScoreCalculator.DEFAULT_MS_REL_INT_NOISE_CUTOFF);
			if(score > params.getMsmsSimilarityCutoff()) {
				addComponent(null, b);
				spectrumMatches = true;
				break;
			}
		}
		return spectrumMatches;
	}

	public Element getXmlElement() {

		Element msmsClusterElement = 
				new Element(MsFeatureInfoBundleClusterFields.MsFeatureInfoBundleCluster.name());
		msmsClusterElement.setAttribute(
				MsFeatureInfoBundleClusterFields.Id.name(), id);	
//		msmsClusterElement.setAttribute(
//				MsFeatureInfoBundleClusterFields.Name.name(), name);
		msmsClusterElement.setAttribute(
				MsFeatureInfoBundleClusterFields.MZ.name(), Double.toString(mz));
		msmsClusterElement.setAttribute(
				MsFeatureInfoBundleClusterFields.RT.name(), Double.toString(rt));
		msmsClusterElement.setAttribute(
				MsFeatureInfoBundleClusterFields.MedianArea.name(), Double.toString(medianArea));
		msmsClusterElement.setAttribute(
				MsFeatureInfoBundleClusterFields.IsLocked.name(), Boolean.toString(locked));
		
		Collection<String>componentFeatureIds = components.stream().
				map(c -> c.getMSFeatureId()).collect(Collectors.toSet());
		msmsClusterElement.addContent(       		
        		new Element(MsFeatureInfoBundleClusterFields.FeatureIdList.name()).
        		setText(StringUtils.join(componentFeatureIds, ",")));
		
		if(primaryIdentity != null)
			msmsClusterElement.addContent(primaryIdentity.getXmlElement());
		
		return msmsClusterElement;
	}
	
	public MsFeatureInfoBundleCluster(Element clusterElement) {
		
		id = clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.Id.name());
		//	name = clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.Name.name());
		mz = Double.parseDouble(
				clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.MZ.name()));
		rt = Double.parseDouble(
				clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.RT.name()));
		medianArea = Double.parseDouble(
				clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.MedianArea.name()));
		locked = Boolean.parseBoolean(
				clusterElement.getAttributeValue(MsFeatureInfoBundleClusterFields.IsLocked.name()));
		
		featureIds = new TreeSet<String>();
		String compoundIdList = 
				clusterElement.getChild(
						MsFeatureInfoBundleClusterFields.FeatureIdList.name()).getText();
		featureIds.addAll(ExperimentUtils.getIdList(compoundIdList));
		Element primaryIdElement = 
				clusterElement.getChild(MsFeatureIdentityFields.MSFID.name());
		if(primaryIdElement != null)
			primaryIdentity = new MsFeatureIdentity(primaryIdElement);
	}

	public Collection<String> getFeatureIds() {
		
		if(featureIds == null)
			featureIds = new TreeSet<String>();
			
		return featureIds;
	}
	
	public void setFeatures(Collection<MSFeatureInfoBundle> featureBundles) {
		components = new HashSet<MSFeatureInfoBundle>();
		components.addAll(featureBundles);
	}

	public MinimalMSOneFeature getLookupFeature() {
		return lookupFeature;
	}

	public void setLookupFeature(MinimalMSOneFeature lookupFeature) {
		this.lookupFeature = lookupFeature;
	}

	public boolean hasAnnotations() {

		long saCount = components.stream().flatMap(b -> b.getStandadAnnotations().stream()).count();
		long aCount = components.stream().flatMap(b -> b.getMsFeature().getAnnotations().stream()).count();
		return (saCount + aCount) > 0;
	}

	public boolean hasIdFollowupSteps() {

		return components.stream().flatMap(b -> b.getIdFollowupSteps().stream()).count() > 0;
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleForPrimaryId() {
		
		if(primaryIdentity == null)
			return null;
		
		return components.stream().
			filter(c -> c.getMsFeature().getIdentifications().contains(primaryIdentity)).
			findFirst().orElse(null);
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleWithLargestMSMSArea() {
		
		return components.stream().
				sorted(new MsFeatureInfoBundleComparator(SortProperty.msmsIntensity, SortDirection.DESC)).
				findFirst().orElse(null);
	}
	
	public MSFeatureInfoBundle getMSFeatureInfoBundleWithHihgestMSMSScore(boolean includeInSourceHits) {
		
		MsFeatureIdentity bestId = null;
		List<MsFeatureIdentity> allIds = components.stream().
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
			return components.stream().
					filter(c -> c.getMsFeature().getIdentifications().contains(lookupId)).
					findFirst().orElse(null);
		}
		else
			return null;
	}
	
	public MSFeatureInfoBundle 
		getMSFeatureInfoBundleWithSmallestParentIonMassError(BinnerAnnotation ba) {
		
		if(lookupFeature == null)
			return null;
		
		double mz = lookupFeature.getMz();		
		double initError = 1000.0d;
		MSFeatureInfoBundle bestHit = null;
		for(MSFeatureInfoBundle b : components) {
			
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
			return getMSFeatureInfoBundleWithSmallestParentIonMassError(null);
		
		if(property.equals(MajorClusterFeatureDefiningProperty.CURRENT_PRIMARY_ID))
				return getMSFeatureInfoBundleForPrimaryId();
		
		return null;
	}
	
	@Override
	public long getFeatureNumber() {		
		return components.size();
	}

	@Override
	public double getRank() {
		
		if(lookupFeature == null)
			return 0;
		else
			return lookupFeature.getRank();
	}

	@Override
	public BinnerAnnotationCluster getBinnerAnnotationCluster() {
		return null;
	}	
}












