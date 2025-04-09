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
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class LibraryMsFeature extends MsFeature implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4462209780734866111L;
	private String libraryId;
	private Date dateCreated;
	private Date lastModified;

	// Copy constructor to create new entry
	public LibraryMsFeature(LibraryMsFeature source) {

		super(source);
		dateCreated = new Date();
		lastModified = new Date();
		id = DataPrefix.MS_LIBRARY_TARGET.getName() + UUID.randomUUID().toString();

			//	TODO handle "copy annotations" to save them to the database as new and linked to the current feature
//		for(ObjectAnnotation annotation : annotations)
//			annotation.setUniqueId(DataPrefix.ANNOTATION.getName() + UUID.randomUUID().toString());
	}

	public LibraryMsFeature(MsFeature source) {

		super(source);
		dateCreated = new Date();
		lastModified = new Date();	
		id = source.getTargetId();
		
		if(id == null)
			id = DataPrefix.MS_LIBRARY_TARGET.getName() + UUID.randomUUID().toString();
		
		if(spectrum == null && source.getSpectrum() != null)
			spectrum = source.getSpectrum();

		if(source.getStatsSummary() != null) {
			double observedRt = source.getStatsSummary().getMeanObservedRetention();
			if(observedRt > 0)
				retentionTime = observedRt;
		}
	}

	public LibraryMsFeature(
			String name,
			double rt,
			String enabled,
			String dbTargetId,
			long created,
			long modified) {

		super(name, 0.0d, rt);

		active = false;
		if(enabled != null)
			active = true;

		id = dbTargetId;
		dateCreated = new Date(created);
		lastModified = new Date(modified);
	}

	public LibraryMsFeature(String name, MassSpectrum spectrum, double retentionTime) {

		super(name, retentionTime);
		this.spectrum = spectrum;
		dateCreated = new Date();
		lastModified = new Date();
		id = DataPrefix.MS_LIBRARY_TARGET.getName() + UUID.randomUUID().toString();
	}

	public LibraryMsFeature(String name, double retentionTime) {

		super(name, retentionTime);
		dateCreated = new Date();
		lastModified = new Date();
		id = DataPrefix.MS_LIBRARY_TARGET.getName() + UUID.randomUUID().toString();
	}

	public LibraryMsFeature(String name, double mass, double rt) {

		super(name, mass, rt);
		dateCreated = new Date();
		lastModified = new Date();
		id = DataPrefix.MS_LIBRARY_TARGET.getName() + UUID.randomUUID().toString();
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public String getLibraryId() {
		return libraryId;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public void setLibraryId(String libraryId) {
		this.libraryId = libraryId;
	}

	public Range getLibraryMatchRtRange(
			double rtWindow, boolean useCustomRange) {

		if(useCustomRange && rtRange.getSize() > 0.0d)
			return rtRange;
		
		if(retentionTime == 0.0d)
			return new Range(0.0d, rtWindow * 2.0d);
		else
			return new Range(
					retentionTime - rtWindow, 
					retentionTime  + rtWindow);
	}
	
	@Override
	public String getName() {
		
		if(name != null && !name.isEmpty())
			return name;
		else if(getPrimaryIdentity() != null)
			return getPrimaryIdentity().getCompoundName();
		else
			return id;			
	}
	
	@Override
	public AnnotatedObjectType getAnnotatedObjectType() {
		return AnnotatedObjectType.MS_LIB_FEATURE;
	}
	
	public LibraryMsFeature(Element libraryMsFeatureElement) {
		
		super(libraryMsFeatureElement);
		
		String dateCreatedString = 
				libraryMsFeatureElement.getAttributeValue(CommonFields.DateCreated.name());
		if(dateCreatedString != null) {
			try {
				dateCreated = ExperimentUtils.dateTimeFormat.parse(dateCreatedString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String lastModifiedString = 
				libraryMsFeatureElement.getAttributeValue(CommonFields.DateCreated.name());
		if(lastModifiedString != null) {
			try {
				lastModified = ExperimentUtils.dateTimeFormat.parse(lastModifiedString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public Element getXmlElement() {
		
		Element libraryMsFeatureElement = super.getXmlElement();
		libraryMsFeatureElement.setName(ObjectNames.LibraryMsFeature.name());
		
		if(dateCreated != null)
			libraryMsFeatureElement.setAttribute(CommonFields.DateCreated.name(), 
					ExperimentUtils.dateTimeFormat.format(dateCreated));
		
		if(lastModified != null)
			libraryMsFeatureElement.setAttribute(CommonFields.LastModified.name(), 
					ExperimentUtils.dateTimeFormat.format(lastModified));
		
		return libraryMsFeatureElement;
	}
}





























