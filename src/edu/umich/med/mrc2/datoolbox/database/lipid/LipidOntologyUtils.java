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

package edu.umich.med.mrc2.datoolbox.database.lipid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.LipidMapsClassifier;
import edu.umich.med.mrc2.datoolbox.data.compare.LipidMapsClassifierComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;

public class LipidOntologyUtils {

	public static Collection<String>getUniqueLipidMapsSubFingerprints(Connection conn) throws Exception {
		
		Collection<String>subFp = new TreeSet<String>();
		String sql = "SELECT DISTINCT SUB_FINGERPRINT FROM LIPIDMAPS_ONTOLOGY ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			subFp.add(rs.getString(1));		
		
		rs.close();
		ps.close();
		return subFp;
	}
	
	public static Map<String, Collection<LipidMapsClassifier>>mapLipidMapsSubFingerprintsToClassifiers(
			Collection<String>subFingerprints, Connection conn) throws Exception {
		
		Map<String, Collection<LipidMapsClassifier>>fpMap = new TreeMap<String, Collection<LipidMapsClassifier>>();
		String sql = 
				"SELECT D.CATEGORY, D.MAIN_CLASS, D.SUB_CLASS, D.CLASS_LEVEL4,  " +
				"D.ABBREVIATION, COUNT(D.LM_ID) AS REPS " +
				"FROM LIPIDMAPS_COMPOUND_DATA D, " +
				"LIPIDMAPS_ONTOLOGY O " +
				"WHERE D.LM_ID = O.LM_ID " +
				"AND O.SUB_FINGERPRINT = ? " +
				"GROUP BY D.CATEGORY, D.MAIN_CLASS,  " +
				"D.SUB_CLASS, D.CLASS_LEVEL4, D.ABBREVIATION " +
				"ORDER BY COUNT(D.LM_ID) DESC";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = null;
		for(String fp : subFingerprints) {
			
			Collection<LipidMapsClassifier>fpClassifiers = new ArrayList<LipidMapsClassifier>();
			ps.setString(1, fp);
			rs = ps.executeQuery();
			while(rs.next()) {
				
				LipidMapsClassifier lc = new LipidMapsClassifier(
						rs.getString("CATEGORY"), 
						rs.getString("MAIN_CLASS"), 
						rs.getString("SUB_CLASS"), 
						rs.getString("CLASS_LEVEL4"), 
						rs.getString("ABBREVIATION"), 
						rs.getInt("REPS"));
				fpClassifiers.add(lc);
			}
			rs.close();
			if(!fpClassifiers.isEmpty())
				fpMap.put(fp, fpClassifiers);
		}
		return fpMap;
	}
	
	public static Map<String, LipidMapsClassifier>findBestLipidMapsClassifiers(Map<String, Collection<LipidMapsClassifier>>classifiersMap) {
		
		Map<String, LipidMapsClassifier>bestClassMap = new TreeMap<String, LipidMapsClassifier>();
		LipidMapsClassifierComparator lcComparator = new LipidMapsClassifierComparator(SortProperty.featureCount, SortDirection.DESC);
		for (Entry<String, Collection<LipidMapsClassifier>> entry : classifiersMap.entrySet()) {
			
			if(entry.getValue().size() == 1) {
				bestClassMap.put(entry.getKey(), entry.getValue().iterator().next());
				continue;
			}
			LipidMapsClassifier[] lcList = entry.getValue().stream().
					sorted(lcComparator).toArray(size -> new LipidMapsClassifier[size]);
			if(lcList[0].getAbbreviation() != null) {
				bestClassMap.put(entry.getKey(), lcList[0]);
				continue;
			}
			if(lcList[0].getClassLevel4()!= null) {
				bestClassMap.put(entry.getKey(), lcList[0]);
				continue;
			}
			LipidMapsClassifier[] lcListSc = entry.getValue().stream().
					filter(c -> c.getSubClass() != null).
					sorted(lcComparator).toArray(size -> new LipidMapsClassifier[size]);
			if(lcListSc.length > 0) {
				bestClassMap.put(entry.getKey(), lcList[0]);
				continue;
			}	
			LipidMapsClassifier[] mcListSc = entry.getValue().stream().
					filter(c -> c.getMainClass() != null).
					sorted(lcComparator).toArray(size -> new LipidMapsClassifier[size]);
			if(mcListSc.length > 0) {
				bestClassMap.put(entry.getKey(), mcListSc[0]);
				continue;
			}
		}
		return bestClassMap;
	}
	
	//	
}

















