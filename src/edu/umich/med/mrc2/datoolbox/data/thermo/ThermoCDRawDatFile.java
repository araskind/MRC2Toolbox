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

import java.util.Date;

public class ThermoCDRawDatFile  implements Comparable<ThermoCDRawDatFile> {

	private String name;
	private Date dateCreated;
	private int size;

	public ThermoCDRawDatFile(String name, Date dateCreated, int size) {
		super();
		this.name = name;
		this.dateCreated = dateCreated;
		this.size = size;
	}

	@Override
	public int compareTo(ThermoCDRawDatFile o) {
		return name.compareTo(o.getName());
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ThermoCDRawDatFile.class.isAssignableFrom(obj.getClass()))
            return false;

        final ThermoCDRawDatFile other = (ThermoCDRawDatFile) obj;

        if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName()))
            return false;

        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    @Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public int getSize() {
		return size;
	}
}
