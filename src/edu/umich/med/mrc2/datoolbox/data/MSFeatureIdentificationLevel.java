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

package edu.umich.med.mrc2.datoolbox.data;

import java.awt.Color;
import java.io.Serializable;

import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;

public class MSFeatureIdentificationLevel implements Serializable, Comparable<MSFeatureIdentificationLevel>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2332142638147818866L;
	private String id;
	private String name;
	private int rank;
	private Color colorCode;
	private boolean allowToReplaceAsDefault;
	private String shorcut;
	public static final String SET_PRIMARY = "SET_PRIMARY_";
	private boolean locked;
		
	public MSFeatureIdentificationLevel(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public MSFeatureIdentificationLevel(String name) {
		super();
		this.name = name;
	}

	public MSFeatureIdentificationLevel(String name, int rank) {
		super();
		this.name = name;
		this.rank = rank;
	}

	public MSFeatureIdentificationLevel(String id, String name, int rank) {
		super();
		this.id = id;
		this.name = name;
		this.rank = rank;
	}

	public MSFeatureIdentificationLevel(
			String id, 
			String name, 
			int rank, 
			String colorCode,
			boolean allowToReplaceAsDefault,
			boolean isLocked) {
		super();
		this.id = id;
		this.name = name;
		this.rank = rank;
		this.allowToReplaceAsDefault = allowToReplaceAsDefault;
		this.locked = isLocked;
		setColorCodeFromHex(colorCode);		
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public int compareTo(MSFeatureIdentificationLevel o) {
		return Integer.compare(rank, o.getRank());
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;
        
		if (obj == this)
			return true;

        if (!MSFeatureIdentificationLevel.class.isAssignableFrom(obj.getClass()))
            return false;

        final MSFeatureIdentificationLevel other = (MSFeatureIdentificationLevel) obj;

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
	
    @Override
	public String toString() {
		return name;
	}

	/**
	 * @return the colorCode
	 */
	public Color getColorCode() {
		return colorCode;
	}
	
	public String getHexColorCode() {
		return ColorUtils.rgb2hex(colorCode);
	}

	/**
	 * @param colorCode the colorCode to set
	 */
	public void setColorCode(Color colorCode) {
		this.colorCode = colorCode;
	}
	
	public void setColorCodeFromHex(String hexColorCode) {
		this.colorCode = ColorUtils.hex2rgb(hexColorCode);
	}

	public boolean isAllowToReplaceAsDefault() {
		return allowToReplaceAsDefault;
	}

	public void setAllowToReplaceAsDefault(boolean allowToReplaceAsDefault) {
		this.allowToReplaceAsDefault = allowToReplaceAsDefault;
	}

	public String getShorcut() {
		return shorcut;
	}

	public void setShorcut(String shorcut) {
		this.shorcut = shorcut;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
