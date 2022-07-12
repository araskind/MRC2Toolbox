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

package edu.umich.med.mrc2.datoolbox.rawdata;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.IntensityMeasure;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsFeatureChromatogramExtractionTarget;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.project.store.XICDefinitionFields;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSExtractionParameterSet implements Comparable<MSMSExtractionParameterSet> {
	
	private String id;
	private String name;
	private String description;
	private Polarity polarity;
	private double minPrecursorIntensity;
	private Range dataExtractionRtRange;
	private boolean removeAllMassesAboveParent;
	private double msMsCountsCutoff;
	private int maxFragmentsCutoff;
	private IntensityMeasure filterIntensityMeasure;
	private double msmsIsolationWindowLowerBorder;
	private double msmsIsolationWindowUpperBorder;	
	private double msmsGroupingRtWindow;
	private double precursorGroupingMassError;
	private MassErrorType precursorGroupingMassErrorType;
	private boolean flagMinorIsotopesPrecursors;
	private int maxPrecursorCharge;
	private int smoothingFilterWidth;
	private double chromatogramExtractionWindow;
	private ChromatogramDefinition commonChromatogramDefinition;
	private MsFeatureChromatogramExtractionTarget xicTarget;
	private boolean mergeCollisionEnergies;
	private double spectrumSimilarityCutoffForMerging;
	
	public MSMSExtractionParameterSet(
			String name,
			String description,
			Polarity polarity,
			double minPrecursorIntensity,
			Range dataExtractionRtRange, 
			boolean removeAllMassesAboveParent, 
			double msMsCountsCutoff,
			int maxFragmentsCutoff, 
			IntensityMeasure filterIntensityMeasure, 
			double msmsIsolationWindowLowerBorder,
			double msmsIsolationWindowUpperBorder, 
			double msmsGroupingRtWindow, 
			double precursorGroupingMassError,
			MassErrorType precursorGroupingMassErrorType, 
			boolean flagMinorIsotopesPrecursors, 
			int maxPrecursorCharge,
			int smoothingFilterWidth,
			double chromatogramExtractionWindow,
			boolean mergeCollisionEnergies,
			double spectrumSimilarityCutoffForMerging) {
		super();
		this.id = DataPrefix.MSMS_EXTRACTION_PARAMETER_SET.getName() +
				UUID.randomUUID().toString().substring(0, 12);
		this.name = name;
		this.description = description;
		this.polarity = polarity;
		this.minPrecursorIntensity = minPrecursorIntensity;
		this.dataExtractionRtRange = dataExtractionRtRange;
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
		this.msMsCountsCutoff = msMsCountsCutoff;
		this.maxFragmentsCutoff = maxFragmentsCutoff;
		this.filterIntensityMeasure = filterIntensityMeasure;
		this.msmsIsolationWindowLowerBorder = msmsIsolationWindowLowerBorder;
		this.msmsIsolationWindowUpperBorder = msmsIsolationWindowUpperBorder;
		this.msmsGroupingRtWindow = msmsGroupingRtWindow;
		this.precursorGroupingMassError = precursorGroupingMassError;
		this.precursorGroupingMassErrorType = precursorGroupingMassErrorType;
		this.flagMinorIsotopesPrecursors = flagMinorIsotopesPrecursors;
		this.maxPrecursorCharge = maxPrecursorCharge;
		this.smoothingFilterWidth = smoothingFilterWidth;
		this.chromatogramExtractionWindow = chromatogramExtractionWindow;
		this.mergeCollisionEnergies = mergeCollisionEnergies;
		this.spectrumSimilarityCutoffForMerging = spectrumSimilarityCutoffForMerging;
	}

	public MSMSExtractionParameterSet() {
		super();
		this.id = DataPrefix.MSMS_EXTRACTION_PARAMETER_SET.getName() +
				UUID.randomUUID().toString().substring(0, 12);
	}

	public Range getDataExtractionRtRange() {
		return dataExtractionRtRange;
	}

	public void setDataExtractionRtRange(Range dataExtractionRtRange) {
		this.dataExtractionRtRange = dataExtractionRtRange;
	}

	public boolean isRemoveAllMassesAboveParent() {
		return removeAllMassesAboveParent;
	}

	public void setRemoveAllMassesAboveParent(boolean removeAllMassesAboveParent) {
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
	}

	public double getMsMsCountsCutoff() {
		return msMsCountsCutoff;
	}

	public void setMsMsCountsCutoff(double msMsCountsCutoff) {
		this.msMsCountsCutoff = msMsCountsCutoff;
	}

	public int getMaxFragmentsCutoff() {
		return maxFragmentsCutoff;
	}

	public void setMaxFragmentsCutoff(int maxFragmentsCutoff) {
		this.maxFragmentsCutoff = maxFragmentsCutoff;
	}

	public IntensityMeasure getFilterIntensityMeasure() {
		return filterIntensityMeasure;
	}

	public void setFilterIntensityMeasure(IntensityMeasure filterIntensityMeasure) {
		this.filterIntensityMeasure = filterIntensityMeasure;
	}

	public double getMsmsIsolationWindowLowerBorder() {
		return msmsIsolationWindowLowerBorder;
	}

	public void setMsmsIsolationWindowLowerBorder(double msmsIsolationWindowLowerBorder) {
		this.msmsIsolationWindowLowerBorder = msmsIsolationWindowLowerBorder;
	}

	public double getMsmsIsolationWindowUpperBorder() {
		return msmsIsolationWindowUpperBorder;
	}

	public void setMsmsIsolationWindowUpperBorder(double msmsIsolationWindowUpperBorder) {
		this.msmsIsolationWindowUpperBorder = msmsIsolationWindowUpperBorder;
	}

	public double getMsmsGroupingRtWindow() {
		return msmsGroupingRtWindow;
	}

	public void setMsmsGroupingRtWindow(double msmsGroupingRtWindow) {
		this.msmsGroupingRtWindow = msmsGroupingRtWindow;
	}

	public double getPrecursorGroupingMassError() {
		return precursorGroupingMassError;
	}

	public void setPrecursorGroupingMassError(double precursorGroupingMassError) {
		this.precursorGroupingMassError = precursorGroupingMassError;
	}

	public MassErrorType getPrecursorGroupingMassErrorType() {
		return precursorGroupingMassErrorType;
	}

	public void setPrecursorGroupingMassErrorType(MassErrorType precursorGroupingMassErrorType) {
		this.precursorGroupingMassErrorType = precursorGroupingMassErrorType;
	}

	public boolean isFlagMinorIsotopesPrecursors() {
		return flagMinorIsotopesPrecursors;
	}

	public void setFlagMinorIsotopesPrecursors(boolean flagMinorIsotopesPrecursors) {
		this.flagMinorIsotopesPrecursors = flagMinorIsotopesPrecursors;
	}

	public int getMaxPrecursorCharge() {
		return maxPrecursorCharge;
	}

	public void setMaxPrecursorCharge(int maxPrecursorCharge) {
		this.maxPrecursorCharge = maxPrecursorCharge;
	}

	public int getSmoothingFilterWidth() {
		return smoothingFilterWidth;
	}

	public void setSmoothingFilterWidth(int smoothingFilterWidth) {
		this.smoothingFilterWidth = smoothingFilterWidth;
	}

	public double getChromatogramExtractionWindow() {
		return chromatogramExtractionWindow;
	}

	public void setChromatogramExtractionWindow(double chromatogramExtractionWindow) {
		this.chromatogramExtractionWindow = chromatogramExtractionWindow;
	}

	public Polarity getPolarity() {
		return polarity;
	}	

	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}

	public ChromatogramDefinition getCommonChromatogramDefinition() {
		return commonChromatogramDefinition;
	}

	public void setCommonChromatogramDefinition(ChromatogramDefinition commonChromatogramDefinition) {
		this.commonChromatogramDefinition = commonChromatogramDefinition;
	}

	public MsFeatureChromatogramExtractionTarget getXicTarget() {
		return xicTarget;
	}

	public void setXicTarget(MsFeatureChromatogramExtractionTarget xicTarget) {
		this.xicTarget = xicTarget;
	}

	public double getMinPrecursorIntensity() {
		return minPrecursorIntensity;
	}

	public void setMinPrecursorIntensity(double minPrecursorIntensity) {
		this.minPrecursorIntensity = minPrecursorIntensity;
	}	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMergeCollisionEnergies() {
		return mergeCollisionEnergies;
	}

	public void setMergeCollisionEnergies(boolean mergeCollisionEnergies) {
		this.mergeCollisionEnergies = mergeCollisionEnergies;
	}

	public double getSpectrumSimilarityCutoffForMerging() {
		return spectrumSimilarityCutoffForMerging;
	}

	public void setSpectrumSimilarityCutoffForMerging(double spectrumSimilarityCutoffForMerging) {
		this.spectrumSimilarityCutoffForMerging = spectrumSimilarityCutoffForMerging;
	}
	
	public Element getXmlElement() {
		
		Element parametersElement = 
				new Element(MSMSExtractionParameters.MSMSExtractionParameterSet.name());
		
		if(this.id == null)
			this.id = DataPrefix.MSMS_EXTRACTION_PARAMETER_SET.getName() +
				UUID.randomUUID().toString().substring(0, 12);
		
		parametersElement.setAttribute(
				MSMSExtractionParameters.ID.name(), id);		
		
		if(name == null)
			name = "";
		
		parametersElement.addContent(       		
        		new Element(MSMSExtractionParameters.Name.name()).setText(name));
		
		if(description == null)
			description = "";
		
		parametersElement.addContent(       		
        		new Element(MSMSExtractionParameters.Description.name()).setText(description));					
		parametersElement.setAttribute(
					MSMSExtractionParameters.Polarity.name(), polarity.getCode());
		parametersElement.setAttribute(
				MSMSExtractionParameters.MinPrecursorIntensity.name(), 
				Double.toString(minPrecursorIntensity));
		
		if(dataExtractionRtRange != null)
			parametersElement.setAttribute(
					MSMSExtractionParameters.DataExtractionRtRange.name(), 
					dataExtractionRtRange.getStorableString());	

		parametersElement.setAttribute(
				MSMSExtractionParameters.RemoveAllMassesAboveParent.name(), 
				Boolean.toString(removeAllMassesAboveParent));
		parametersElement.setAttribute(
				MSMSExtractionParameters.MsMsCountsCutoff.name(), 
				Double.toString(msMsCountsCutoff));
		parametersElement.setAttribute(
				MSMSExtractionParameters.MaxFragmentsCutoff.name(), 
				Integer.toString(maxFragmentsCutoff));		
		parametersElement.setAttribute(
				MSMSExtractionParameters.FilterIntensityMeasure.name(), 
				filterIntensityMeasure.name());
		parametersElement.setAttribute(
				MSMSExtractionParameters.MsmsIsolationWindowLowerBorder.name(), 
				Double.toString(msmsIsolationWindowLowerBorder));
		parametersElement.setAttribute(
				MSMSExtractionParameters.MsmsIsolationWindowUpperBorder.name(), 
				Double.toString(msmsIsolationWindowUpperBorder));
		parametersElement.setAttribute(
				MSMSExtractionParameters.MsmsGroupingRtWindow.name(), 
				Double.toString(msmsGroupingRtWindow));
		parametersElement.setAttribute(
				MSMSExtractionParameters.PrecursorGroupingMassError.name(), 
				Double.toString(precursorGroupingMassError));
		parametersElement.setAttribute(
				MSMSExtractionParameters.PrecursorGroupingMassErrorType.name(), 
				precursorGroupingMassErrorType.name());
		parametersElement.setAttribute(
				MSMSExtractionParameters.FlagMinorIsotopesPrecursors.name(), 
				Boolean.toString(flagMinorIsotopesPrecursors));
		parametersElement.setAttribute(
				MSMSExtractionParameters.MaxPrecursorCharge.name(), 
				Integer.toString(maxPrecursorCharge));		
		parametersElement.setAttribute(
				MSMSExtractionParameters.MergeCollisionEnergies.name(), 
				Boolean.toString(mergeCollisionEnergies));		
		parametersElement.setAttribute(
				MSMSExtractionParameters.SpectrumSimilarityCutoffForMerging.name(), 
				Double.toString(spectrumSimilarityCutoffForMerging));
		
		//	TODO smoothing filter for MS1 and/or other MS1 stuff
		
		parametersElement.setAttribute(
				MSMSExtractionParameters.ChromatogramExtractionWindow.name(), 
				Double.toString(chromatogramExtractionWindow));	
		if(commonChromatogramDefinition != null)
			parametersElement.addContent(commonChromatogramDefinition.getXmlElement());	
		
		String xicTargetName = MsFeatureChromatogramExtractionTarget.MSMSParentIon.name();
		if(xicTarget != null)
			xicTargetName = xicTarget.name();
		
		parametersElement.setAttribute(
				MSMSExtractionParameters.MsFeatureChromatogramExtractionTarget.name(), 
				xicTargetName);
		
		return parametersElement;
	}
		
	public String getParameterSetHash(){

		List<String> chunks = new ArrayList<String>();
		
//		if(description != null)
//			chunks.add(description);
		
		chunks.add(polarity.getCode());
		chunks.add(MsUtils.spectrumMzExportFormat.format(minPrecursorIntensity));
		if(dataExtractionRtRange != null)
			chunks.add(dataExtractionRtRange.getStorableString());
		
		chunks.add(Boolean.toString(removeAllMassesAboveParent));
		chunks.add(MsUtils.spectrumMzExportFormat.format(msMsCountsCutoff));
		chunks.add(Integer.toString(maxFragmentsCutoff));
		chunks.add(filterIntensityMeasure.name());
		//	chunks.add(MsUtils.spectrumMzExportFormat.format(msmsIsolationWindowLowerBorder));	// ?
		//	chunks.add(MsUtils.spectrumMzExportFormat.format(msmsIsolationWindowUpperBorder));	// ?
		chunks.add(MsUtils.spectrumMzExportFormat.format(msmsGroupingRtWindow));
		chunks.add(MsUtils.spectrumMzExportFormat.format(precursorGroupingMassError));
		chunks.add(precursorGroupingMassErrorType.name());
		chunks.add(Boolean.toString(flagMinorIsotopesPrecursors));
		chunks.add(Integer.toString(maxPrecursorCharge));
		chunks.add(MsUtils.spectrumMzExportFormat.format(chromatogramExtractionWindow));
		chunks.add(Boolean.toString(mergeCollisionEnergies));
		chunks.add(MsUtils.spectrumMzExportFormat.format(spectrumSimilarityCutoffForMerging));
		
		if(commonChromatogramDefinition != null)
			chunks.add(commonChromatogramDefinition.getChromatogramDefinitionHash());
		
		chunks.add(xicTarget.name());
		
	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(StringUtils.join(chunks).getBytes(Charset.forName("windows-1252")));
			return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public MSMSExtractionParameterSet(Element parametersElement) {
		super();
		this.id = parametersElement.getAttributeValue(
				MSMSExtractionParameters.ID.name());	
		if(this.id == null)
			this.id = DataPrefix.MSMS_EXTRACTION_PARAMETER_SET.getName() +
			UUID.randomUUID().toString().substring(0, 12);
		
		Element nameElement = 
				parametersElement.getChild(MSMSExtractionParameters.Name.name());
		if(nameElement != null)
			this.name = nameElement.getText();
		else
			this.name = "";
		
		Element descElement = 
				parametersElement.getChild(MSMSExtractionParameters.Description.name());
		if(descElement != null)
			this.description = descElement.getText();
		else
			this.description = "";
		
		String polCode = parametersElement.getAttributeValue(
				MSMSExtractionParameters.Polarity.name());
		if(polCode != null)
			polarity = Polarity.getPolarityByCode(polCode);
		
		this.minPrecursorIntensity = Double.parseDouble(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.MinPrecursorIntensity.name()));		
		String rtRangeString = parametersElement.getAttributeValue(
				MSMSExtractionParameters.DataExtractionRtRange.name());
		if(rtRangeString != null)
			dataExtractionRtRange = new Range(rtRangeString);
				
		this.removeAllMassesAboveParent = Boolean.parseBoolean(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.RemoveAllMassesAboveParent.name()));		
		this.msMsCountsCutoff = Double.parseDouble(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.MsMsCountsCutoff.name()));
		this.maxFragmentsCutoff = Integer.parseInt(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.MaxFragmentsCutoff.name()));		
		this.filterIntensityMeasure = 
				IntensityMeasure.geIntensityMeasureByName(parametersElement.getAttributeValue(
				MSMSExtractionParameters.FilterIntensityMeasure.name()));		
		this.msmsIsolationWindowLowerBorder = Double.parseDouble(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.MsmsIsolationWindowLowerBorder.name()));
		this.msmsIsolationWindowUpperBorder = Double.parseDouble(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.MsmsIsolationWindowUpperBorder.name()));		
		this.msmsGroupingRtWindow = Double.parseDouble(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.MsmsGroupingRtWindow.name()));
		this.precursorGroupingMassError = Double.parseDouble(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.PrecursorGroupingMassError.name()));		
		this.precursorGroupingMassErrorType = 
				MassErrorType.getTypeByName(parametersElement.getAttributeValue(
				MSMSExtractionParameters.PrecursorGroupingMassErrorType.name()));
		this.flagMinorIsotopesPrecursors = Boolean.parseBoolean(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.FlagMinorIsotopesPrecursors.name()));			
		this.maxPrecursorCharge = Integer.parseInt(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.MaxPrecursorCharge.name()));
		
		String mceString = parametersElement.getAttributeValue(
				MSMSExtractionParameters.MergeCollisionEnergies.name());
		if(mceString != null)
			this.mergeCollisionEnergies = Boolean.parseBoolean(mceString);
		else
			this.mergeCollisionEnergies = false;
				
		String simCutoffString = parametersElement.getAttributeValue(
				MSMSExtractionParameters.SpectrumSimilarityCutoffForMerging.name());
		if(simCutoffString != null)
			spectrumSimilarityCutoffForMerging = Double.parseDouble(simCutoffString);
		else
			spectrumSimilarityCutoffForMerging = 0.70d;
				
		String xicTargetName = parametersElement.getAttributeValue(
				MSMSExtractionParameters.MsFeatureChromatogramExtractionTarget.name());
		if(xicTargetName == null || xicTargetName.isEmpty())
			this.xicTarget = MsFeatureChromatogramExtractionTarget.MSMSParentIon;
		else		
			this.xicTarget = MsFeatureChromatogramExtractionTarget.valueOf(xicTargetName);
		
//		TODO smoothing filter for MS1 and/or other MS1 stuff
		
		Element cdElement = parametersElement.getChild(
				XICDefinitionFields.XICDefinition.name());
		if(cdElement != null) {
			try {
				commonChromatogramDefinition = new ChromatogramDefinition(cdElement);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		this.chromatogramExtractionWindow = Double.parseDouble(
				parametersElement.getAttributeValue(
						MSMSExtractionParameters.ChromatogramExtractionWindow.name()));
	}
	
	public String getXMLString() {
		
		Document methodDocument = new Document(getXmlElement());
		return new XMLOutputter().outputString(methodDocument);
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MSMSExtractionParameterSet.class.isAssignableFrom(obj.getClass()))
            return false;

        final MSMSExtractionParameterSet other = (MSMSExtractionParameterSet) obj;

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
    
	@Override
	public int compareTo(MSMSExtractionParameterSet o) {
		return id.compareTo(o.getId());
	}
}


