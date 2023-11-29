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

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.MajorClusterFeatureDefiningProperty;

public interface IMsFeatureInfoBundleCluster {

	Collection<MSFeatureInfoBundle> getComponents();

	long getFeatureNumber();

	void updateNameFromPrimaryIdentity();

	void replaceStoredPrimaryIdentityFromFeatures();

	void addComponent(BinnerAnnotation ba, MSFeatureInfoBundle newComponent);

	void removeComponent(BinnerAnnotation ba, MSFeatureInfoBundle toRemove);

	String getId();

	MsFeatureIdentity getPrimaryIdentity();

	void setPrimaryIdentity(MsFeatureIdentity primaryIdentity);

	String getName();

	boolean isLocked();

	void setLocked(boolean locked);

	boolean addNewBundle(BinnerAnnotation ba, MSFeatureInfoBundle b, MSMSClusteringParameterSet params);

	Element getXmlElement();

	Collection<String> getFeatureIds();

	boolean hasAnnotations();

	boolean hasIdFollowupSteps();

	MSFeatureInfoBundle getMSFeatureInfoBundleForPrimaryId();

	MSFeatureInfoBundle getMSFeatureInfoBundleWithLargestMSMSArea();

	MSFeatureInfoBundle getMSFeatureInfoBundleWithHihgestMSMSScore(boolean includeInSourceHits);

	MSFeatureInfoBundle getMSFeatureInfoBundleWithSmallestParentIonMassError(BinnerAnnotation ba);

	MSFeatureInfoBundle getDefiningFeature(MajorClusterFeatureDefiningProperty property);

	double getMz();

	double getRt();

	double getMedianArea();

	double getRank();

	void setId(String clusterId);

	MinimalMSOneFeature getLookupFeature();
	
	BinnerAnnotationCluster getBinnerAnnotationCluster();
}





