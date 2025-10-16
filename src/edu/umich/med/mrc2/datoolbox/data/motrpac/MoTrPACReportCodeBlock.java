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

package edu.umich.med.mrc2.datoolbox.data.motrpac;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

public class MoTrPACReportCodeBlock implements Serializable, Comparable<MoTrPACReportCodeBlock>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5825514951704374722L;
	private String blockId;
	private int blockOrder;
	private Collection<MoTrPACReportCode>blockCodes;
	
	public MoTrPACReportCodeBlock(String blockId, int blockOrder) {
		super();
		this.blockId = blockId;
		this.blockOrder = blockOrder;
		blockCodes = new TreeSet<MoTrPACReportCode>();
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	public int getBlockOrder() {
		return blockOrder;
	}

	public void setBlockOrder(int blockOrder) {
		this.blockOrder = blockOrder;
	}

	public Collection<MoTrPACReportCode> getBlockCodes() {
		return blockCodes;
	}

	@Override
	public int compareTo(MoTrPACReportCodeBlock o) {
		return Integer.compare(blockOrder, o.getBlockOrder());
	}
	
	@Override
	public String toString() {
		return blockId;
	}
	
	public MoTrPACReportCode getMoTrPACReportCodeByName(String name) {
		return blockCodes.stream().
				filter(c -> c.getOptionName().equals(name)).
				findFirst().orElse(null);
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MoTrPACReportCodeBlock.class.isAssignableFrom(obj.getClass()))
            return false;

        final MoTrPACReportCodeBlock other = (MoTrPACReportCodeBlock) obj;

        if ((this.blockId == null) ? (other.getBlockId() != null) : !this.blockId.equals(other.getBlockId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.blockId != null ? this.blockId.hashCode() : 0);
        return hash;
    }
}
