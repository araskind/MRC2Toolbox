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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.binner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class BinnerAnnotationClusterTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7387427636234648515L;

	public static final String CLUSTER_COLUMN = "Name";
	public static final String MZ_COLUMN = "M/Z";
	public static final String RT_COLUMN = "RT";
	public static final String NUM_ANNOTATIONS_COLUMN = "#Annotations";
	public static final String LIST_OF_ANNOTATIONS_COLUMN = "List of annotations";

	public BinnerAnnotationClusterTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(CLUSTER_COLUMN, "Feature name", BinnerAnnotationCluster.class, false),
			new ColumnContext(MZ_COLUMN, "Monoisotopic M/Z", Double.class, false),
			new ColumnContext(RT_COLUMN, "Retention time", Double.class, false),
			new ColumnContext(NUM_ANNOTATIONS_COLUMN, "Number of annotations in the cluster", Integer.class, false),
			new ColumnContext(LIST_OF_ANNOTATIONS_COLUMN, LIST_OF_ANNOTATIONS_COLUMN, Double.class, false),
		};
	}

	public void setTableModelFromBinnerAnnotationClusterCollection(
			Collection<BinnerAnnotationCluster>annotationClusters) {

		setRowCount(0);
		if(annotationClusters == null || annotationClusters.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (BinnerAnnotationCluster cluster : annotationClusters) {

			Object[] obj = {
					cluster,
					cluster.getPrimaryFeatureAnnotation().getBinnerMz(),
					cluster.getPrimaryFeatureAnnotation().getBinnerRt(),
					cluster.getAnnotations().size(),
					cluster.getAllAnnotationsAsString(),
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}















