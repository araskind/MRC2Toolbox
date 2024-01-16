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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACRawDataManifestFields;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;

public class Worklist implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1339977247417448217L;
	private Collection<WorklistItem> items;

	public Worklist() {
		super();
		items = new HashSet<WorklistItem>();
	}

	public void addItem(WorklistItem newItem) {
		items.add(newItem);
	}

	public HashMap<DataFile, Double> getTimeline() {

		HashMap<DataFile, Double> timeLine = new HashMap<DataFile, Double>();
		Date start = items.iterator().next().getTimeStamp();
		for (WorklistItem i : items) {

			if (i.getTimeStamp().before(start))
				start = i.getTimeStamp();
		}
		for (WorklistItem i : items) {

			long diff = i.getTimeStamp().getTime() - start.getTime();
			double inj = diff / 3600000.0d;
			timeLine.put(i.getDataFile(), inj);
		}
		return timeLine;
	}
	
	public Collection<WorklistItem> getWorklistItems() {
		return items;
	}

	public Collection<WorklistItem> getTimeSortedWorklistItems() {

		return items.stream().
			sorted(Comparator.comparing(WorklistItem::getTimeStamp, 
					Comparator.nullsLast(Comparator.naturalOrder()))).
			collect(Collectors.toList());
	}

	public void appendWorklist(Worklist wlToAppend) {
		items.addAll(wlToAppend.getTimeSortedWorklistItems());
	}
	
	public void removeDataFile(DataFile df) {
		Collection<WorklistItem>toRemove = 
				items.stream().filter(i -> i.getDataFile().equals(df)).
				collect(Collectors.toList());
		items.removeAll(toRemove);
	}
	
	public void updateExistingWorklistItem(WorklistItem newItem) {
		
		DataFile df = newItem.getDataFile();
		if(df == null)
			return;
		
		WorklistItem itemToUpdate = 
				items.stream().
				filter(i -> i.getDataFile().getBaseName().equals(df.getBaseName())).
				findFirst().orElse(null);
		if(itemToUpdate == null)
			return;
		
		itemToUpdate.setTimeStamp(newItem.getTimeStamp());
		for(Entry<String, String>property : newItem.getProperties().entrySet())
			itemToUpdate.getProperties().put(property.getKey(), property.getValue());
		
		if((newItem instanceof LIMSWorklistItem) 
				&& (itemToUpdate instanceof LIMSWorklistItem)) {
			
			LIMSWorklistItem nlwki = (LIMSWorklistItem)newItem;
			LIMSWorklistItem tupd = (LIMSWorklistItem)itemToUpdate;
			tupd.setInjectionVolume(nlwki.getInjectionVolume());
			tupd.setAcquisitionMethod(nlwki.getAcquisitionMethod());
			tupd.setSample(nlwki.getSample());
			tupd.setSamplePrep(nlwki.getSamplePrep());
			tupd.setPrepItemId(nlwki.getPrepItemId());
		}
	}
	
	public void setRunOrder() {
		
		Collection<WorklistItem>sorted = getTimeSortedWorklistItems();
		int injectionOrder = 1;
		for(WorklistItem item : sorted) {
			
			item.setProperty(
					MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ORDER.getName(), 
					Integer.toString(injectionOrder));
			injectionOrder++;
		}
	}
}













