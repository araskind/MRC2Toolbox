/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.data.thermo;

public class ThermoCDSample implements Comparable<ThermoCDSample> {

	private String id;
	private String name;
	private String fileSetId;
	
	public ThermoCDSample(String id, String name, String fileSetId) {
		super();
		this.id = id;
		this.name = name;
		this.fileSetId = fileSetId;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getFileSetId() {
		return fileSetId;
	}
	
	@Override
	public int compareTo(ThermoCDSample o) {
		return id.compareTo(o.getId());
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ThermoCDSample.class.isAssignableFrom(obj.getClass()))
            return false;

        final ThermoCDSample other = (ThermoCDSample) obj;

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
}
