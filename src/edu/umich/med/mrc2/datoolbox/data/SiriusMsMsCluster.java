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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.data.enums.AdductNotationType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class SiriusMsMsCluster implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5308020706362132359L;
	private MsPoint parentIon;
	private Collection<MSFeatureInfoBundle>msmsComponents;
	private Collection<MsPoint>msOneSpectrum;
	private Adduct adduct;
	private Range rtRange;
	private Range mzRange;
	private DescriptiveStatistics meanRt;
	private DescriptiveStatistics meanMz;
	private double rtError;
	private double mzError;
	private static final DecimalFormat ceFormat = new DecimalFormat("###.#");
	private static final DecimalFormat intensityFormat = new DecimalFormat("###.##");
		
	public SiriusMsMsCluster(MSFeatureInfoBundle firstBundle, double rtError, double mzError) {
		super();
		msmsComponents = new HashSet<MSFeatureInfoBundle>();
		this.rtError = rtError;
		this.mzError = mzError;	
		msmsComponents.add(firstBundle);		
		adduct = firstBundle.getMsFeature().getSpectrum().getPrimaryAdduct();
		if(adduct == null)
			adduct = AdductManager.getDefaultAdductForPolarity(firstBundle.getMsFeature().getPolarity());
			
		TandemMassSpectrum msms = 
				firstBundle.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
		parentIon = msms.getParent();
		rtRange = new Range(
				firstBundle.getMsFeature().getRetentionTime() - rtError, 
				firstBundle.getMsFeature().getRetentionTime() + rtError);		
		mzRange = MsUtils.createPpmMassRange(parentIon.getMz(), mzError);
		
		meanRt = new DescriptiveStatistics();
		meanRt.addValue(firstBundle.getMsFeature().getRetentionTime());
		meanMz = new DescriptiveStatistics();
		meanMz.addValue(parentIon.getMz());
		
		//	TODO check for compatible acquisition methods
	}
	
	public SiriusMsMsCluster(IMsFeatureInfoBundleCluster cluster) {

		super();
		msmsComponents = new HashSet<MSFeatureInfoBundle>();
		MSFeatureInfoBundle[] features = cluster.getComponents().
				toArray(new MSFeatureInfoBundle[cluster.getComponents().size()]);
		MSFeatureInfoBundle firstBundle = features[0];
		msmsComponents.add(firstBundle);		
		adduct = firstBundle.getMsFeature().getSpectrum().getPrimaryAdduct();
		if(adduct == null)
			adduct = AdductManager.getDefaultAdductForPolarity(firstBundle.getMsFeature().getPolarity());
			
		TandemMassSpectrum msms = 
				firstBundle.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
		parentIon = msms.getParent();
		
		meanRt = new DescriptiveStatistics();
		meanRt.addValue(firstBundle.getMsFeature().getRetentionTime());
		meanMz = new DescriptiveStatistics();
		meanMz.addValue(parentIon.getMz());
		
		for(int i=1; i<features.length; i++) {
			
			msmsComponents.add(features[i]);			
			meanRt.addValue(features[i].getMsFeature().getRetentionTime());
			double newParentMz = features[i].getMsFeature().getSpectrum().
					getExperimentalTandemSpectrum().getParent().getMz();
			meanMz.addValue(newParentMz);
		}
		rtRange = new Range(meanRt.getMin(), meanRt.getMax());		
		mzRange = new Range(meanMz.getMin(), meanMz.getMax());
	}

	public boolean addFeatureBundle(MSFeatureInfoBundle newBundle) {
		
		MsFeature feature = newBundle.getMsFeature();
		if(feature.getCharge() != adduct.getCharge())
			return false;
		
		if(!rtRange.contains(feature.getRetentionTime()))
			return false;
		
		TandemMassSpectrum msms = 
				feature.getSpectrum().getExperimentalTandemSpectrum();
		double newParentMz = msms.getParent().getMz();

		if(!mzRange.contains(newParentMz))
			return false;
		
		msmsComponents.add(newBundle);
		
		meanRt.addValue(feature.getRetentionTime());
		double medianRt = meanRt.getPercentile(50.0);
		meanMz.addValue(newParentMz);
		double medianMz = meanMz.getPercentile(50.0);
		
		rtRange = new Range(
				medianRt - rtError, 
				medianRt + rtError);		
		mzRange = MsUtils.createPpmMassRange(medianMz, mzError);
		return true;
	}
	
	public Collection<TandemMassSpectrum>getMsMsSpectra(){
		
		return msmsComponents.stream().
				map(b -> b.getMsFeature()).
				map(f -> f.getSpectrum().getExperimentalTandemSpectrum()).
				collect(Collectors.toList());	
	}
	
	public double getParentMz() {
		return mzRange.getAverage();
	}
	
	public String getName() {
		
		String name = 
				DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() + 
				MRC2ToolBoxConfiguration.getMzFormat().format(mzRange.getAverage()) + "_" +
				MRC2ToolBoxConfiguration.getRtFormat().format(rtRange.getAverage()) ;		
		return name;
	}
	
	public String getComment() {
		
		Collection<String>fids = msmsComponents.stream().
			map(b -> b.getMsFeature()).
			map(f -> f.getId()).
			collect(Collectors.toList());
		
		return StringUtils.join(fids, ";");
	}
	
	public String getSiriusMsBlock() {
		
		if(adduct == null 
				|| adduct.getNotationForType(AdductNotationType.SIRIUS) == null)
			return null;
		
		String adductName = adduct.getNotationForType(AdductNotationType.SIRIUS);
		
		Collection<String>msBlock = new ArrayList<String>();
		if(msmsComponents.size() == 1) {
			
			MSFeatureInfoBundle bundle = msmsComponents.iterator().next();
			msBlock.add(">compound " + bundle.getMSMSFeatureId());
			msBlock.add(">parentmass " + MRC2ToolBoxConfiguration.getMzFormat().format(mzRange.getAverage()));
			msBlock.add(">ionization " + adductName);
			msBlock.add(">comments " + getName());
			msBlock.add("");
			
			TandemMassSpectrum msms =  bundle.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();			
			msBlock.add(">collision " + ceFormat.format(msms.getCidLevel()));
			for(MsPoint p : msms.getMassSortedSpectrum()) {
				
				msBlock.add(
						MRC2ToolBoxConfiguration.getMzFormat().format(p.getMz()) + " " + 
								intensityFormat.format(p.getIntensity()));
			}
			msBlock.add("");			
			msBlock.add(">ms1");
			
			msBlock.add(
					MRC2ToolBoxConfiguration.getMzFormat().format(msms.getParent().getMz()) + " " + 
							intensityFormat.format(msms.getParent().getIntensity()));
		}
		else {
			msBlock.add(">compound " + getName());
			msBlock.add(">parentmass " + MRC2ToolBoxConfiguration.getMzFormat().format(mzRange.getAverage()));
			msBlock.add(">ionization " + adductName);
			msBlock.add(">comments " + getComment());
			msBlock.add("");
			
			Collection<TandemMassSpectrum>msmsList = getMsMsSpectra();
			for(TandemMassSpectrum msms : msmsList) {
				
				msBlock.add(">collision " + ceFormat.format(msms.getCidLevel()));
				for(MsPoint p : msms.getMassSortedSpectrum()) {
					
					msBlock.add(
							MRC2ToolBoxConfiguration.getMzFormat().format(p.getMz()) + " " + 
									intensityFormat.format(p.getIntensity()));
				}
				msBlock.add("");
			}
			msBlock.add(">ms1");
			msBlock.add(
					MRC2ToolBoxConfiguration.getMzFormat().format(mzRange.getAverage()) + " " + 
							intensityFormat.format(parentIon.getIntensity()));
		}
		msBlock.add("");
		return StringUtils.join(msBlock, "\n");
	}

	public Adduct getAdduct() {
		return adduct;
	}

	public void setAdduct(Adduct adduct) {
		this.adduct = adduct;
	}
	
}
















