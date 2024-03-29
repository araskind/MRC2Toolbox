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

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;

public interface IMSMSClusterDataSet {

	String getId();

	void setId(String id);

	String getName();

	void setName(String name);
	
	MSMSClusterDataSetType getDataSetType();
	
	void setDataSetType(MSMSClusterDataSetType dataSetType);

	String getDescription();

	void setDescription(String description);

	LIMSUser getCreatedBy();

	void setCreatedBy(LIMSUser createdBy);

	Date getDateCreated();

	void setDateCreated(Date dateCreated);

	MSMSClusteringParameterSet getParameters();

	void setParameters(MSMSClusteringParameterSet parameters);

	Collection<String> getInjectionIds();

	Collection<DataExtractionMethod> getDataExtractionMethods();

	Date getLastModified();

	void setLastModified(Date lastModified);

	Element getXmlElement();

	Collection<MSFeatureInfoBundle> getAllFeatures();
	
	Set<IMsFeatureInfoBundleCluster> getClusters();

	Set<String> getClusterIds();
	
	void setFeatureLookupDataSet(FeatureLookupList featureLookupDataSet);
	
	FeatureLookupList getFeatureLookupDataSet();
	
	void setBinnerAnnotationDataSet(BinnerAnnotationLookupDataSet binnerAnnotationDataSet);
	
	BinnerAnnotationLookupDataSet getBinnerAnnotationDataSet();

	void clearDataSet();

	Collection<MinimalMSOneFeature> getMatchedLookupFeatures();

	String getFormattedMetadata();
}