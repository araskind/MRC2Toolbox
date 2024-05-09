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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.utils.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class CompoundIdentity implements Serializable, Comparable<CompoundIdentity> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1771511877339304356L;
	private String commonName;
	private String className;
	private String sysName;
	private String formula;
	private double exactMass;
	private String smiles;
	private String inChi;
	private String inChiKey;
	private HashMap<CompoundDatabaseEnum, String> dbIdMap;
	private CompoundDatabaseEnum primaryDatabase;	
	private int charge;

	public CompoundIdentity(){
		dbIdMap = new HashMap<CompoundDatabaseEnum, String>();
	}

	public CompoundIdentity(CompoundDatabaseEnum dbSource, String dbId) {
		dbIdMap = new HashMap<CompoundDatabaseEnum, String>();
		dbIdMap.put(dbSource, dbId);
		primaryDatabase = dbSource;
	}

	public CompoundIdentity(
			CompoundDatabaseEnum dbSource,
			String dbId,
			String commonName,
			String sysName,
			String formula,
			double exactMass,
			String smiles) {
		this.commonName = commonName;
		this.sysName = sysName;
		this.formula = formula;
		this.exactMass = exactMass;
		this.smiles = smiles;
		dbIdMap = new HashMap<CompoundDatabaseEnum, String>();
		dbIdMap.put(dbSource, dbId);
		primaryDatabase = dbSource;
	}

	public CompoundIdentity(
			CompoundDatabaseEnum dbSource,
			String dbId,
			String commonName,
			String formula,
			double exactMass,
			String smiles,
			String inChiKey) {
		this.commonName = commonName;
		this.formula = formula;
		this.exactMass = exactMass;
		this.smiles = smiles;
		this.inChiKey = inChiKey;

		dbIdMap = new HashMap<CompoundDatabaseEnum, String>();
		dbIdMap.put(dbSource, dbId);
		primaryDatabase = dbSource;
	}

	public CompoundIdentity(
			CompoundDatabaseEnum dbSource,
			String dbId,
			String commonName,
			String formula,
			String smiles,
			String inChiKey) {
		this.commonName = commonName;
		this.formula = formula;
		this.smiles = smiles;
		this.inChiKey = inChiKey;
		this.exactMass = getExactMass();

		dbIdMap = new HashMap<CompoundDatabaseEnum, String>();
		dbIdMap.put(dbSource, dbId);
		primaryDatabase = dbSource;
	}

	public CompoundIdentity(ResultSet rs, CompoundDatabaseEnum dbSource) throws SQLException {

		this.commonName = rs.getString(3);
		this.sysName = rs.getString(2);
		this.formula = rs.getString(4);
		this.exactMass = rs.getDouble(5);
		this.smiles = rs.getString(6);
		dbIdMap = new HashMap<CompoundDatabaseEnum, String>();
		dbIdMap.put(dbSource, rs.getString(1));
		primaryDatabase = dbSource;
	}

	public CompoundIdentity(String commonName, String formula) {

		super();
		this.commonName = commonName;
		this.formula = formula;
		if(formula != null) {
			
			if(formula.contains("D"))
				formula = formula.replaceAll("D", "[2H]");
			
			IMolecularFormula mf = 
					MolecularFormulaManipulator.getMolecularFormula(
							formula, DefaultChemObjectBuilder.getInstance());
			exactMass = MolecularFormulaManipulator.getMass(
					mf, MolecularFormulaManipulator.MonoIsotopic);
			
			this.formula = MolecularFormulaManipulator.getString(mf);
		}
		dbIdMap = new HashMap<CompoundDatabaseEnum, String>();
	}
	
	public void addDbId(CompoundDatabaseEnum dbSource, String id) {

		if(primaryDatabase == null)
			primaryDatabase = dbSource;

		dbIdMap.put(dbSource, id);
	}

	@Override
	public int compareTo(CompoundIdentity o) {
		return this.getName().compareTo(o.getName());
	}

	public String getClassName() {
		return className;
	}

	public String getCommonName() {
		return commonName;
	}

	public String getDbId(CompoundDatabaseEnum dbSource) {
		return dbIdMap.get(dbSource);
	}

	public HashMap<CompoundDatabaseEnum, String> getDbIdMap() {
		return dbIdMap;
	}

	public double getExactMass() {

		if(exactMass == 0.0)
			exactMass = MsUtils.getExactMassForCompoundIdentity(this);
			
		return exactMass;
	}

	public String getFormula() {
		return formula;
	}

	public String getInChi() {
		return inChi;
	}

	public String getInChiKey() {
		return inChiKey;
	}

	// Get common name, if not available - systematic name (this should always
	// be present)
	public String getName() {

		String name = this.commonName;

		if (name == null)
			name = this.sysName;
		else if (name.isEmpty())
			name = this.sysName;

		if (name == null)
			name  = "";

		return name;
	}

	public CompoundDatabaseEnum getPrimaryDatabase(){
		return primaryDatabase;
	}

	public String getPrimaryLinkAddress() {

		String primaryLinkId = null;
		String primaryLinkAddress = "";
		if(primaryDatabase != null){

			//	TODO handle ID cleanup in a more centralized way
			primaryLinkId = dbIdMap.get(primaryDatabase).replace("METLIN:","").replace("ALDRICH:", "");
			if(primaryLinkId != null) {

				if(primaryDatabase.equals(CompoundDatabaseEnum.LIPIDMAPS_BULK)) {

					String[] split = StringUtils.split(primaryLinkId, '-');
					primaryLinkAddress =
						primaryDatabase.getDbLinkPrefix() + split[0] +
						"&Formula=" + split[1] +
						"&ExactMass=" + this.getExactMass() +
						"&ExactMassOffSet=0.1";
					return primaryLinkAddress;
				}
				else if(primaryDatabase.equals(CompoundDatabaseEnum.REFMET)) {
					try {
						primaryLinkAddress = URLEncoder.encode(primaryLinkId, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return primaryDatabase.getDbLinkPrefix() + primaryLinkAddress;
				}
				else
					return primaryDatabase.getDbLinkPrefix() + primaryLinkId + primaryDatabase.getDbLinkSuffix();
			}
		}
		return primaryLinkAddress;
	}

	public String getPrimaryDatabaseId() {

		if(primaryDatabase != null)
			return dbIdMap.get(primaryDatabase);
		else
			return null;
	}

	@Override
	public boolean equals(Object cpdId) {

        if (cpdId == this)
            return true;

		if(cpdId == null)
			return false;

        if (!CompoundIdentity.class.isAssignableFrom(cpdId.getClass()))
            return false;

        CompoundIdentity cid = (CompoundIdentity)cpdId;

        //	If the same database & ID return true
        if(this.primaryDatabase != null && cid.getPrimaryDatabase() != null) {

        	if(primaryDatabase.equals(cid.getPrimaryDatabase())
        			&& this.getPrimaryDatabaseId().equals(cid.getPrimaryDatabaseId()))
        		return true;
        }
        if ((this.getPrimaryDatabaseId() == null) ? (cid.getPrimaryDatabaseId() != null) :
        	!this.getPrimaryDatabaseId().equals(cid.getPrimaryDatabaseId()))
        	return false;

        if ((this.inChiKey == null) ? (cid.getInChiKey() != null) :
        	!this.inChiKey.equals(cid.getInChiKey()))
        	return false;

        if ((this.inChi == null) ? (cid.getInChi() != null) :
        	!this.inChi.equals(cid.getInChi()))
        	return false;

        if ((this.smiles == null) ? (cid.getSmiles() != null) :
        	!this.smiles.equals(cid.getSmiles()))
        	return false;

        if ((this.sysName == null) ? (cid.getSysName() != null) :
        	!this.sysName.equalsIgnoreCase(cid.getSysName()))
        	return false;

        if ((this.commonName == null) ? (cid.getCommonName() != null) :
        	!this.commonName.equalsIgnoreCase(cid.getCommonName()))
        	return false;

        return true;
	}

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash
        		+ (this.getPrimaryDatabase() != null ? this.getPrimaryDatabase().name().hashCode() : 0)
        		+ (this.getPrimaryDatabaseId() != null ? this.getPrimaryDatabaseId().hashCode() : 0);
        return hash;
    }

	public String getSmiles() {
		return smiles;
	}

	public String getSysName() {
		return sysName;
	}

	public void removeDbId(CompoundDatabaseEnum dbSource) {
		dbIdMap.remove(dbSource);
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public void setExactMass(double exactMass) {
		this.exactMass = exactMass;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public void setInChi(String inChi) {
		this.inChi = inChi;
	}

	public void setInChiKey(String inChiKey) {
		this.inChiKey = inChiKey;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public void setSysName(String sysName) {
		this.sysName = sysName;
	}

	/**
	 * @param primaryDatabase the primaryDatabase to set
	 */
	public void setPrimaryDatabase(CompoundDatabaseEnum newPrimaryDatabase) {

		if(dbIdMap.containsKey(newPrimaryDatabase))
			this.primaryDatabase = newPrimaryDatabase;
	}

	public void addDatabaseIds(Map<CompoundDatabaseEnum, String>extraIds, boolean replace) {

		if(replace) {
			extraIds.entrySet().stream().
			forEach(e -> dbIdMap.put(e.getKey(), e.getValue()));
		}
		else {
			for(Entry<CompoundDatabaseEnum, String> entry : extraIds.entrySet()) {

				if(!dbIdMap.containsKey(entry.getKey()))
					dbIdMap.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}
	
	public Entry<CompoundDatabaseEnum, String>getTopRankingDatabaseId(){		
		return IdentificationUtils.getTopRankingDatabaseId(this);
	}
}
















