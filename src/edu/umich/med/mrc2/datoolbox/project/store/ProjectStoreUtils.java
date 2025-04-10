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

package edu.umich.med.mrc2.datoolbox.project.store;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class ProjectStoreUtils {
	
	private ProjectStoreUtils() {
		
	}
	
	private static final List<CommonFields>dateFields = 
			Arrays.asList(CommonFields.DateCreated, CommonFields.LastModified);

	public static void setDateAttribute(Date date, CommonFields field, Element element) {
		
		if(date != null && dateFields.contains(field))
			element.setAttribute(field.name(), ExperimentUtils.dateTimeFormat.format(date));
	}
	
	public static Date getDateFromAttribute(Element element, CommonFields field) {
		
		if(!dateFields.contains(field))
			return null;
		
		String dateString = element.getAttributeValue(field.name());
		Date date = null;
		if(dateString != null) {
			try {
				date = ExperimentUtils.dateTimeFormat.parse(dateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return date;
	}
	
	public static void setUserIdAttribute(LIMSUser user, Element element) {
		
		if(user != null)
			element.setAttribute(CommonFields.UserId.name(), user.getId());
	}
	
	public static LIMSUser getUserFromAttribute(Element element) {
		
		String userId = element.getAttributeValue(CommonFields.UserId.name());		
		LIMSUser user = null;
		if(userId != null && !userId.isBlank())
			user = IDTDataCache.getUserById(userId);
		
		return user;
	}
	
	public static void addDescriptionElement(String description, Element parent) {
		addTextElement(description, parent, CommonFields.Description);
	}
	
	public static String getDescriptionFromElement(Element parent) {
		return getTextFromElement(parent, CommonFields.Description);
	}

	public static void addTextElement(String text, Element parent, Enum field) {
		
		Element textElement = new Element(field.name());
		if(text != null && !text.isBlank()) {
			
			textElement.setText(text.trim());	
			parent.addContent(textElement);
		}
	}
	
	public static String getTextFromElement(Element parent, Enum field) {

		Element textElement = parent.getChild(field.name());
		if(textElement != null)
			return textElement.getText();
		else
			return null;
	}	
}















