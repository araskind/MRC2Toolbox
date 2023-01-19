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

package edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb;

import java.io.Serializable;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;

public class HMDBCitation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -68004700630512378L;
	
	private String uniqueId;
	private String citationText;
	private String pubmedId;

	public HMDBCitation(String citationText, String pubmedId) {
		super();
		uniqueId = DataPrefix.HMDB_CITATION.getName() + UUID.randomUUID().toString();
		this.citationText = citationText;
		this.pubmedId = pubmedId;
	}

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @return the citationText
	 */
	public String getCitationText() {
		return citationText;
	}

	/**
	 * @return the pubmedId
	 */
	public String getPubmedId() {
		return pubmedId;
	}
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.citationText != null ? this.citationText.hashCode() : 0);
        return hash;
    }
}
