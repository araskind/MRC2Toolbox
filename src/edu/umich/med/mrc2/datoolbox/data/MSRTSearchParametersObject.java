/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.project.store.MSRTSearchParametersFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class MSRTSearchParametersObject implements XmlStorable{

	private boolean clearPreviousResults;
	private double massError;
	private MassErrorType massErrorType;
	private boolean relaxMassErrorForMinorIsotopes;
	private boolean ignoreAdductType;
	private double retentionWindow;
	private boolean useCustomRtWindow;
	private int maxHits;
	private final Collection<String>libraryIds;
	
	public MSRTSearchParametersObject() {
		super();
		libraryIds = new TreeSet<String>();
	}

	public MSRTSearchParametersObject(
			boolean clearPreviousResults,
			double massError, 
			MassErrorType massErrorType,
			boolean relaxMassErrorForMinorIsotopes, 
			boolean ignoreAdductType, 
			double retentionWindow,
			boolean useCustomRtWindow, 
			int maxHits) {
		super();
		this.clearPreviousResults = clearPreviousResults;
		this.massError = massError;
		this.massErrorType = massErrorType;
		this.relaxMassErrorForMinorIsotopes = relaxMassErrorForMinorIsotopes;
		this.ignoreAdductType = ignoreAdductType;
		this.retentionWindow = retentionWindow;
		this.useCustomRtWindow = useCustomRtWindow;
		this.maxHits = maxHits;
		libraryIds = new TreeSet<String>();
	}

	public double getMassError() {
		return massError;
	}

	public void setMassError(double massError) {
		this.massError = massError;
	}

	public MassErrorType getMassErrorType() {
		return massErrorType;
	}

	public void setMassErrorType(MassErrorType massErrorType) {
		this.massErrorType = massErrorType;
	}

	public boolean isRelaxMassErrorForMinorIsotopes() {
		return relaxMassErrorForMinorIsotopes;
	}

	public void setRelaxMassErrorForMinorIsotopes(
			boolean relaxMassErrorForMinorIsotopes) {
		this.relaxMassErrorForMinorIsotopes = 
				relaxMassErrorForMinorIsotopes;
	}

	public boolean isIgnoreAdductType() {
		return ignoreAdductType;
	}

	public void setIgnoreAdductType(boolean ignoreAdductType) {
		this.ignoreAdductType = ignoreAdductType;
	}

	public double getRetentionWindow() {
		return retentionWindow;
	}

	public void setRetentionWindow(double retentionWindow) {
		this.retentionWindow = retentionWindow;
	}

	public boolean isUseCustomRtWindow() {
		return useCustomRtWindow;
	}

	public void setUseCustomRtWindow(boolean useCustomRtWindow) {
		this.useCustomRtWindow = useCustomRtWindow;
	}

	public int getMaxHits() {
		return maxHits;
	}

	public void setMaxHits(int maxHits) {
		this.maxHits = maxHits;
	}

	public Collection<String> getLibraryIds() {
		return libraryIds;
	}
	
	public void addLibraryId(String libraryId) {
		libraryIds.add(libraryId);
	}

	public boolean isClearPreviousResults() {
		return clearPreviousResults;
	}

	public void setClearPreviousResults(boolean clearPreviousResults) {
		this.clearPreviousResults = clearPreviousResults;
	}

	@Override
	public Element getXmlElement() {

		Element msRTSearchParametersElement = 
				new Element(ObjectNames.MSRTSearchParameters.name());
		
		msRTSearchParametersElement.setAttribute(
				MSRTSearchParametersFields.clearPreviousResults.name(), 
				Boolean.toString(clearPreviousResults));
		
		msRTSearchParametersElement.setAttribute(
				MSRTSearchParametersFields.massError.name(), 
				Double.toString(massError));
		
		msRTSearchParametersElement.setAttribute(
				MSRTSearchParametersFields.massErrorType.name(), massErrorType.name());
		
		msRTSearchParametersElement.setAttribute(
				MSRTSearchParametersFields.retentionWindow.name(), 
				Double.toString(retentionWindow));
		
		msRTSearchParametersElement.setAttribute(
				MSRTSearchParametersFields.relaxMassErrorForMinorIsotopes.name(), 
				Boolean.toString(relaxMassErrorForMinorIsotopes));
		
		msRTSearchParametersElement.setAttribute(
				MSRTSearchParametersFields.ignoreAdductType.name(), 
				Boolean.toString(ignoreAdductType));
		
		msRTSearchParametersElement.setAttribute(
				MSRTSearchParametersFields.useCustomRtWindow.name(), 
				Boolean.toString(useCustomRtWindow));
		
		msRTSearchParametersElement.setAttribute(
				MSRTSearchParametersFields.maxHits.name(), 
				Integer.toString(maxHits));
		
		Element libraryListElement = 
				new Element(MSRTSearchParametersFields.libraryIdList.name());
		for(String libId : libraryIds) {
			
			Element libraryElement = new Element(MSRTSearchParametersFields.libraryId.name()); 
			libraryElement.setText(libId);
			libraryListElement.addContent(libraryElement);
		}
		msRTSearchParametersElement.addContent(libraryListElement);
		
		return msRTSearchParametersElement;
	}	
	
	public MSRTSearchParametersObject(Element msRTSearchParametersElement) {
		
		this.clearPreviousResults = Boolean.parseBoolean(msRTSearchParametersElement.getAttributeValue(
						MSRTSearchParametersFields.clearPreviousResults.name()));
		this.massError = Double.parseDouble(msRTSearchParametersElement.getAttributeValue(
						MSRTSearchParametersFields.massError.name()));
		this.massErrorType = MassErrorType.getTypeByName(MSRTSearchParametersFields.massErrorType.name()) ;
		this.relaxMassErrorForMinorIsotopes = Boolean.parseBoolean(msRTSearchParametersElement.getAttributeValue(
						MSRTSearchParametersFields.relaxMassErrorForMinorIsotopes.name()));
		this.ignoreAdductType = Boolean.parseBoolean(msRTSearchParametersElement.getAttributeValue(
						MSRTSearchParametersFields.ignoreAdductType.name()));
		this.retentionWindow = Double.parseDouble(msRTSearchParametersElement.getAttributeValue(
						MSRTSearchParametersFields.retentionWindow.name()));
		this.useCustomRtWindow = Boolean.parseBoolean(msRTSearchParametersElement.getAttributeValue(
						MSRTSearchParametersFields.useCustomRtWindow.name()));
		this.maxHits = Integer.parseInt(msRTSearchParametersElement.getAttributeValue(
						MSRTSearchParametersFields.maxHits.name()));
		libraryIds = new TreeSet<String>();
		List<Element>libIdElementList = msRTSearchParametersElement.getChild(
				MSRTSearchParametersFields.libraryIdList.name()).
				getChildren(MSRTSearchParametersFields.libraryId.name());
		for(Element libIdElement : libIdElementList)
			libraryIds.add(libIdElement.getText());
	}
}





