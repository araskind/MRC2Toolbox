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

package edu.umich.med.mrc2.datoolbox.dbparse.load.t3db;

import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBRecord;

public class T3DBRecord extends HMDBRecord {

	private Collection<String>categories;
	private Collection<String>types;
	private String origin;
	private String status;
	private String routeOfExposure;
	private String mechanismOfToxicity;
	private String metabolism;
	private String toxicity;
	private String lethaldose;
	private String carcinogenicity;
	private String usage;
	private String minRiskLevel;
	private String healthEffects;
	private String symptoms;
	private String treatment;
	private Collection<T3DBTarget>targets;


	public T3DBRecord(String primaryId) {
		super(primaryId);

		types = new ArrayList<String>();
		categories = new ArrayList<String>();
		targets = new ArrayList<T3DBTarget>();
	}

	/**
	 * @return the categories
	 */
	public Collection<String> getCategories() {
		return categories;
	}


	/**
	 * @return the types
	 */
	public Collection<String> getTypes() {
		return types;
	}


	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}


	/**
	 * @return the routeOfExposure
	 */
	public String getRouteOfExposure() {
		return routeOfExposure;
	}


	/**
	 * @return the mechanism_of_toxicity
	 */
	public String getMechanismOfToxicity() {
		return mechanismOfToxicity;
	}


	/**
	 * @return the metabolism
	 */
	public String getMetabolism() {
		return metabolism;
	}


	/**
	 * @return the toxicity
	 */
	public String getToxicity() {
		return toxicity;
	}


	/**
	 * @return the lethaldose
	 */
	public String getLethaldose() {
		return lethaldose;
	}


	/**
	 * @return the carcinogenicity
	 */
	public String getCarcinogenicity() {
		return carcinogenicity;
	}


	/**
	 * @return the usage
	 */
	public String getUsage() {
		return usage;
	}


	/**
	 * @return the minRiskLevel
	 */
	public String getMinRiskLevel() {
		return minRiskLevel;
	}


	/**
	 * @return the healthEffects
	 */
	public String getHealthEffects() {
		return healthEffects;
	}


	/**
	 * @return the symptoms
	 */
	public String getSymptoms() {
		return symptoms;
	}


	/**
	 * @return the treatment
	 */
	public String getTreatment() {
		return treatment;
	}


	/**
	 * @return the targets
	 */
	public Collection<T3DBTarget> getTargets() {
		return targets;
	}


	/**
	 * @param categories the categories to set
	 */
	public void setCategories(Collection<String> categories) {
		this.categories = categories;
	}


	/**
	 * @param types the types to set
	 */
	public void setTypes(Collection<String> types) {
		this.types = types;
	}


	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}


	/**
	 * @param routeOfExposure the routeOfExposure to set
	 */
	public void setRouteOfExposure(String routeOfExposure) {
		this.routeOfExposure = routeOfExposure;
	}


	/**
	 * @param mechanism_of_toxicity the mechanism_of_toxicity to set
	 */
	public void setMechanismOfToxicity(String mechanism_of_toxicity) {
		this.mechanismOfToxicity = mechanism_of_toxicity;
	}


	/**
	 * @param metabolism the metabolism to set
	 */
	public void setMetabolism(String metabolism) {
		this.metabolism = metabolism;
	}


	/**
	 * @param toxicity the toxicity to set
	 */
	public void setToxicity(String toxicity) {
		this.toxicity = toxicity;
	}


	/**
	 * @param lethaldose the lethaldose to set
	 */
	public void setLethaldose(String lethaldose) {
		this.lethaldose = lethaldose;
	}


	/**
	 * @param carcinogenicity the carcinogenicity to set
	 */
	public void setCarcinogenicity(String carcinogenicity) {
		this.carcinogenicity = carcinogenicity;
	}


	/**
	 * @param usage the usage to set
	 */
	public void setUsage(String usage) {
		this.usage = usage;
	}


	/**
	 * @param minRiskLevel the minRiskLevel to set
	 */
	public void setMinRiskLevel(String minRiskLevel) {
		this.minRiskLevel = minRiskLevel;
	}


	/**
	 * @param healthEffects the healthEffects to set
	 */
	public void setHealthEffects(String healthEffects) {
		this.healthEffects = healthEffects;
	}


	/**
	 * @param symptoms the symptoms to set
	 */
	public void setSymptoms(String symptoms) {
		this.symptoms = symptoms;
	}


	/**
	 * @param treatment the treatment to set
	 */
	public void setTreatment(String treatment) {
		this.treatment = treatment;
	}

	/**
	 * @param targets the targets to set
	 */
	public void setTargets(Collection<T3DBTarget> targets) {
		this.targets = targets;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

}
