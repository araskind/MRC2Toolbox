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

import java.io.Serializable;

public class StandardFeatureAnnotation implements Serializable, Comparable<StandardFeatureAnnotation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3367072574552435159L;
	private String id;
	private String code;
	private String text;
	
	public StandardFeatureAnnotation(String id, String code, String text) {
		super();
		this.id = id;
		this.code = code;
		this.text = text;
	}

	@Override
	public int compareTo(StandardFeatureAnnotation o) {
		return code.compareTo(o.getCode());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!StandardFeatureAnnotation.class.isAssignableFrom(obj.getClass()))
            return false;

        final StandardFeatureAnnotation other = (StandardFeatureAnnotation) obj;

        if (!this.id.equals(other.getId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
         return 53 * 3 + id.hashCode();
    }	
}
