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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.util.Date;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.IonizationType;
import edu.umich.med.mrc2.datoolbox.data.MassAnalyzerType;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.DataAcquisitionMethodFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;

public class DataAcquisitionMethod extends AnalysisMethod implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2933812231905650460L;
	
	private String description;
	private LIMSUser createdBy;
	private Date createdOn;	
	private IonizationType ionizationType;
	private MassAnalyzerType massAnalyzerType;
	private MsType msType;
	private ChromatographicSeparationType separationType;
	private LIMSChromatographicColumn column;
	private ChromatographicGradient chromatographicGradient;
	private String motrPacMsMode;
	private Polarity polarity;

	public DataAcquisitionMethod(

			String id,
			String name,
			String methodDescription,
			LIMSUser createdBy,
			Date createdOn) {

		super(id, name);
		this.description = methodDescription;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
	}

	/**
	 * @return the methodDescription
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the createdBy
	 */
	public LIMSUser getCreatedBy() {
		return createdBy;
	}

	/**
	 * @return the createdOn
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	/**
	 * @return the ionizationType
	 */
	public IonizationType getIonizationType() {
		return ionizationType;
	}

	/**
	 * @return the column
	 */
	public LIMSChromatographicColumn getColumn() {
		return column;
	}

	/**
	 * @param methodDescription the methodDescription to set
	 */
	public void setDescription(String methodDescription) {
		this.description = methodDescription;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(LIMSUser createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @param column the column to set
	 */
	public void setColumn(LIMSChromatographicColumn column) {
		this.column = column;
	}

	/**
	 * @return the massAnalyzerType
	 */
	public MassAnalyzerType getMassAnalyzerType() {
		return massAnalyzerType;
	}

	/**
	 * @return the msType
	 */
	public MsType getMsType() {
		return msType;
	}

	/**
	 * @param ionizationType the ionizationType to set
	 */
	public void setIonizationType(IonizationType ionizationType) {
		this.ionizationType = ionizationType;
	}

	/**
	 * @param massAnalyzerType the massAnalyzerType to set
	 */
	public void setMassAnalyzerType(MassAnalyzerType massAnalyzerType) {
		this.massAnalyzerType = massAnalyzerType;
	}

	/**
	 * @param msType the msType to set
	 */
	public void setMsType(MsType msType) {
		this.msType = msType;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!DataAcquisitionMethod.class.isAssignableFrom(obj.getClass()))
            return false;

        final DataAcquisitionMethod other = (DataAcquisitionMethod) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }

	public ChromatographicSeparationType getSeparationType() {
		return separationType;
	}

	public void setSeparationType(ChromatographicSeparationType separationType) {
		this.separationType = separationType;
	}

	/**
	 * @return the chromatographicGradient
	 */
	public ChromatographicGradient getChromatographicGradient() {
		
		if(chromatographicGradient == null)
			chromatographicGradient = new ChromatographicGradient();
		
		return chromatographicGradient;
	}

	/**
	 * @param chromatographicGradient the chromatographicGradient to set
	 */
	public void setChromatographicGradient(ChromatographicGradient chromatographicGradient) {
		this.chromatographicGradient = chromatographicGradient;
	}
	
	/**
	 * @return the motrPacMsMode
	 */
	public String getMotrPacMsMode() {
		return motrPacMsMode;
	}

	/**
	 * @param motrPacMsMode the motrPacMsMode to set
	 */
	public void setMotrPacMsMode(String motrPacMsMode) {
		this.motrPacMsMode = motrPacMsMode;
	}

	public Polarity getPolarity() {
		return polarity;
	}

	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}
	
	public DataAcquisitionMethod(Element dataAcquisitionMethodElement) {

		super(dataAcquisitionMethodElement.getAttributeValue(CommonFields.Id.name()), 
				dataAcquisitionMethodElement.getAttributeValue(CommonFields.Name.name()));
		
		//	TODO remove
		description = dataAcquisitionMethodElement.getAttributeValue(
				CommonFields.Description.name());	
		if(description == null)
			description = 
				ProjectStoreUtils.getDescriptionFromElement(dataAcquisitionMethodElement);
		
		createdOn = ProjectStoreUtils.getDateFromAttribute(
				dataAcquisitionMethodElement, CommonFields.DateCreated);
		createdBy = ProjectStoreUtils.getUserFromAttribute(
				dataAcquisitionMethodElement);
		
		Element ionizationTypeElement = 
				dataAcquisitionMethodElement.getChild(ObjectNames.IonizationType.name());
		if(ionizationTypeElement != null)
			ionizationType = new IonizationType(ionizationTypeElement);
		
		Element massAnalyzerTypeElement = 
				dataAcquisitionMethodElement.getChild(ObjectNames.MassAnalyzerType.name());
		if(massAnalyzerTypeElement != null)
			massAnalyzerType = new MassAnalyzerType(massAnalyzerTypeElement);
		
		Element msTypeElement = 
				dataAcquisitionMethodElement.getChild(ObjectNames.MsType.name());
		if(msTypeElement != null)
			msType = new MsType(msTypeElement);
		
		Element separationTypeElement = 
				dataAcquisitionMethodElement.getChild(ObjectNames.ChromatographicSeparationType.name());
		if(separationTypeElement != null)
			separationType = new ChromatographicSeparationType(separationTypeElement);
		
		Element columnElement = 
				dataAcquisitionMethodElement.getChild(ObjectNames.LIMSChromatographicColumn.name());
		if(columnElement != null)
			column = new LIMSChromatographicColumn(columnElement);
		
		Element chromatographicGradientElement = 
				dataAcquisitionMethodElement.getChild(ObjectNames.ChromatographicGradient.name());
		if(chromatographicGradientElement != null)
			chromatographicGradient = new ChromatographicGradient(chromatographicGradientElement);
	}
	
	@Override
	public Element getXmlElement() {
		
		Element dataAcquisitionMethodElement = super.getXmlElement();
		dataAcquisitionMethodElement.setName(ObjectNames.DataAcquisitionMethod.name());
		ProjectStoreUtils.addDescriptionElement(description, dataAcquisitionMethodElement);
		ProjectStoreUtils.setUserIdAttribute(createdBy, dataAcquisitionMethodElement);
		ProjectStoreUtils.setDateAttribute(
				createdOn, CommonFields.DateCreated, dataAcquisitionMethodElement);
		
		if(motrPacMsMode != null)
			dataAcquisitionMethodElement.setAttribute(
				DataAcquisitionMethodFields.motrPacMsMode.name(), motrPacMsMode);
		
		if(polarity != null)
			dataAcquisitionMethodElement.setAttribute(
					DataAcquisitionMethodFields.polarity.name(), polarity.getCode());
		
		if(ionizationType != null)
			dataAcquisitionMethodElement.addContent(ionizationType.getXmlElement());
		
		if(massAnalyzerType != null)
			dataAcquisitionMethodElement.addContent(massAnalyzerType.getXmlElement());
		
		if(msType != null)
			dataAcquisitionMethodElement.addContent(msType.getXmlElement());
		
		if(separationType != null)
			dataAcquisitionMethodElement.addContent(separationType.getXmlElement());
		
		if(column != null)
			dataAcquisitionMethodElement.addContent(column.getXmlElement());
		
		if(chromatographicGradient != null)
			dataAcquisitionMethodElement.addContent(chromatographicGradient.getXmlElement());
		
		return dataAcquisitionMethodElement;
	}
}









