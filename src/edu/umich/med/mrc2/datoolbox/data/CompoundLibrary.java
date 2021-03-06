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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;

public class CompoundLibrary implements Serializable, Comparable<CompoundLibrary> {

	/**
	 *
	 */
	private static final long serialVersionUID = 8587732551371736548L;
	
	private String id;
	private String name;
	private String description;
	private DataPipeline dataPipeline;
	private Date dateCreated;
	private Date lastModified;
	private boolean enabled;

	private Collection<LibraryMsFeature> libraryFeatures;

	public CompoundLibrary(
			String libraryId,
			String libraryName,
			String libraryDescription,
			Date dateCreated,
			Date lastModified,
			boolean enabled){

		super();
		this.id = libraryId;
		this.name = libraryName;
		this.description = libraryDescription;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		this.enabled = enabled;
		dataPipeline = null;

		libraryFeatures = new HashSet<LibraryMsFeature>();
	}

	public CompoundLibrary(String libraryName) {
		super();
		this.name = libraryName;
		description = libraryName;
		dataPipeline = null;
		id = DataPrefix.MS_LIBRARY.getName() + UUID.randomUUID().toString();
		dateCreated = new Date();
		lastModified = dateCreated;
		enabled = true;
		libraryFeatures = new HashSet<LibraryMsFeature>();
	}

	public CompoundLibrary(String libraryName, String libraryDescription) {
		super();
		this.name = libraryName;
		this.description = libraryDescription;
		dataPipeline = null;
		id = DataPrefix.MS_LIBRARY.getName() + UUID.randomUUID().toString();
		dateCreated = new Date();
		lastModified = dateCreated;
		enabled = true;
		libraryFeatures = new HashSet<LibraryMsFeature>();
	}

	public CompoundLibrary(
			String libraryId,
			String libraryName,
			String libraryDescription,
			DataPipeline dataPipeline) {

		super();
		this.id = libraryId;
		this.name = libraryName;
		this.description = libraryDescription;
		this.dataPipeline = dataPipeline;
		dateCreated = new Date();
		lastModified = dateCreated;
		enabled = true;

		libraryFeatures = new HashSet<LibraryMsFeature>();
	}

	public void addFeature(LibraryMsFeature newFeature) {
		libraryFeatures.add(newFeature);
	}

	public void addFeatures(Set<LibraryMsFeature> newFeatures) {
			libraryFeatures.addAll(newFeatures);
	}

	public Map<String, MsFeature>getNameMap(){

		Map<String, MsFeature>idMap = new TreeMap<String, MsFeature>();
		libraryFeatures.stream().forEach(f -> idMap.put(f.getName(), f));
		return idMap;
	}

	public void removeFeature(LibraryMsFeature newFeature) {

		libraryFeatures.remove(newFeature);
	}

	public void removeFeatures(Collection<LibraryMsFeature> toremove) {
		libraryFeatures.removeAll(toremove);
	}

	@Override
	public int compareTo(CompoundLibrary o) {

		String current = name + id;
		String newLib = o.getLibraryName() + o.getLibraryId();

		return current.compareToIgnoreCase(newLib);
	}

	public Collection<LibraryMsFeature>getFeatures() {
		return libraryFeatures;
	}

	public Collection<LibraryMsFeature>getSortedFeatures(Comparator<MsFeature>sorter) {
		return libraryFeatures.stream().sorted(sorter).collect(Collectors.toList());
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public Date getDateCreated() {
		return dateCreated;
	}
	
	public LibraryMsFeature getFeatureByName(String name) {
		return libraryFeatures.stream().
				filter(f -> f.getName().equals(name)).findFirst().get();
	}

	public LibraryMsFeature getFeatureById(String targetId) {
		return libraryFeatures.stream().
				filter(f -> f.getId().equals(targetId)).findFirst().get();
	}

	public Date getLastModified() {
		return lastModified;
	}

	public String getLibraryDescription() {
		return description;
	}

	public String getLibraryId() {
		return id;
	}

	public String getLibraryName() {
		return name;
	}

	public String toString() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setDataPipeline(DataPipeline dataPipeline) {
		this.dataPipeline = dataPipeline;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public void setLibraryDescription(String libraryDescription) {
		this.description = libraryDescription;
	}

	public void setLibraryName(String libraryName) {
		this.name = libraryName;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!CompoundLibrary.class.isAssignableFrom(obj.getClass()))
            return false;

        final CompoundLibrary other = (CompoundLibrary) obj;

        if ((this.name == null) ? (other.getLibraryName() != null) : !this.name.equals(other.getLibraryName()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
