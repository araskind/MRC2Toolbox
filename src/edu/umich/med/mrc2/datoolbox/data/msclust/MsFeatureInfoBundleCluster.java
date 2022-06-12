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

package edu.umich.med.mrc2.datoolbox.data.msclust;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;

public class MsFeatureInfoBundleCluster {

	private String id;
	private Set<MsFeatureInfoBundle>components;
	private double mz;
	private double rt;
	private CompoundIdentity prinmaryIdentity;
	
	public MsFeatureInfoBundleCluster() {
		this(null, 0.0d, 0.0d, null);
		this.id = DataPrefix.MSMS_CLUSTER.getName() + 
				UUID.randomUUID().toString().substring(0, 12);
	}
	
	public MsFeatureInfoBundleCluster(
			String id, 
			double mz, 
			double rt, 
			CompoundIdentity prinmaryIdentity) {
		super();
		this.id = id;
		this.mz = mz;
		this.rt = rt;
		this.prinmaryIdentity = prinmaryIdentity;
		components = new HashSet<MsFeatureInfoBundle>();
	}
	
	public void addComponent(MsFeatureInfoBundle newComponent) {
		components.add(newComponent);
	}

	public void removeComponent(MsFeatureInfoBundle toRemove) {
		components.remove(toRemove);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getMz() {
		return mz;
	}

	public void setMz(double mz) {
		this.mz = mz;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public CompoundIdentity getPrinmaryIdentity() {
		return prinmaryIdentity;
	}

	public void setPrinmaryIdentity(CompoundIdentity prinmaryIdentity) {
		this.prinmaryIdentity = prinmaryIdentity;
	}

	public Set<MsFeatureInfoBundle> getComponents() {
		return components;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MsFeatureInfoBundleCluster.class.isAssignableFrom(obj.getClass()))
            return false;

        final MsFeatureInfoBundleCluster other = (MsFeatureInfoBundleCluster) obj;

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
