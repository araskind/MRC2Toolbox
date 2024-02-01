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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MSMSExportUtils {
	
	private static final DateFormat dateFormat = 
			new SimpleDateFormat(MRC2ToolBoxConfiguration.DATE_TIME_FORMAT_DEFAULT);

	public static Collection<String>createFeatureMSPBlock(
			MSFeatureInfoBundle bundle, Injection injection){
		
		 Collection<String>featureMSPBlock = new ArrayList<String>();
		 MsFeature msf = bundle.getMsFeature();
		 if(msf.getSpectrum() == null 
				 || msf.getSpectrum().getExperimentalTandemSpectrum() == null)
			 return featureMSPBlock;
		 
		 Adduct adduct = msf.getSpectrum().getPrimaryAdduct();
		if(adduct == null)
			adduct = AdductManager.getDefaultAdductForPolarity(msf.getPolarity());
		
		 Collection<TandemMassSpectrum> tandemSpectra = 
			 	msf.getSpectrum().getTandemSpectra().stream().
				filter(t -> t.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL)).
				collect(Collectors.toList());
		 for(TandemMassSpectrum tandemMs : tandemSpectra) {

				if(tandemMs.getSpectrum().isEmpty())
					continue;

				featureMSPBlock.add(MSPField.NAME.getName() + ": " + tandemMs.getId());
				featureMSPBlock.add("Feature name: " + msf.getName());
				if(msf.isIdentified()) {
					CompoundIdentity cid = msf.getPrimaryIdentity().getCompoundIdentity();
					featureMSPBlock.add(MSPField.SYNONYM.getName() + ": " + cid.getName());
					if(cid.getFormula() != null)
						featureMSPBlock.add(MSPField.FORMULA.getName() + ": " + cid.getFormula());
					if(cid.getInChiKey() != null)
						featureMSPBlock.add(MSPField.INCHI_KEY.getName() + ": " + cid.getInChiKey());
				}
				String polarity = "P";
				if(msf.getPolarity().equals(Polarity.Negative))
					polarity = "N";
				featureMSPBlock.add(MSPField.ION_MODE.getName() + ": " + polarity);
				
				featureMSPBlock.add(MSPField.PRECURSOR_TYPE.getName() + ": " + adduct.getName());

				if(tandemMs.getCidLevel() >0)
					featureMSPBlock.add(MSPField.COLLISION_ENERGY.getName() + ": " 
							+ Double.toString(tandemMs.getCidLevel()));

				//	RT
				featureMSPBlock.add(MSPField.RETENTION_INDEX.getName() + ": " +
						MRC2ToolBoxConfiguration.getRtFormat().format(
								msf.getRetentionTime()) + " min.");

				//	Comments
				featureMSPBlock.add(createComment(bundle, injection));

				featureMSPBlock.add(MSPField.PRECURSORMZ.getName() + ": " +
					MRC2ToolBoxConfiguration.getMzFormat().format(
							tandemMs.getParent().getMz()));
				featureMSPBlock.add(MSPField.NUM_PEAKS.getName() + ": " 
						+ Integer.toString(tandemMs.getSpectrum().size()));

				MsPoint[] msms = MsUtils.normalizeAndSortMsPattern(tandemMs.getSpectrum());
				for(MsPoint point : msms) {

					featureMSPBlock.add(
						MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
						+ " " + MsUtils.mspIntensityFormat.format(point.getIntensity())) ;
				}
		 }
		 featureMSPBlock.add("   ");
		 return featureMSPBlock;
	}
	
	public static String createComment(MSFeatureInfoBundle bundle, Injection injection) {

		String comment = MSPField.COMMENT.getName() + ": ";
		comment += "RT "+ MRC2ToolBoxConfiguration.getRtFormat().format(
				bundle.getMsFeature().getRetentionTime()) + " min; ";

		if(injection != null) {
			comment += "Data file: " + injection.getDataFileName() + "; ";
			comment += "Timestamp: " + dateFormat.format(injection.getTimeStamp()) + "; ";
		}	
		if(bundle.getAcquisitionMethod() != null)
			comment += "Acq. method: " + bundle.getAcquisitionMethod().getName() + "; ";
		
		if(bundle.getDataExtractionMethod() != null)
			comment += "DA method: " + bundle.getDataExtractionMethod().getName();
		
		return comment;
	}
	
	public static Map<String,Injection> createInjectionMap(Collection<MSFeatureInfoBundle> msmsFeatures) {

		Map<String,Injection>injectionMap = new TreeMap<String,Injection>();
		List<String> injIds = msmsFeatures.stream().
				filter(f -> Objects.nonNull(f.getInjectionId())).
				map(f -> f.getInjectionId()).distinct().
				sorted().collect(Collectors.toList());

		if(injIds.isEmpty())
			return injectionMap;

		for(String id : injIds) {

			try {
				Injection injection = IDTUtils.getInjectionById(id);
				if(injection != null)
					injectionMap.put(id, injection);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return injectionMap;
	}
}
