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

package edu.umich.med.mrc2.datoolbox.gui.tables;

import java.util.Collection;
import java.util.Map;

import javax.swing.JCheckBox;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RadioButtonEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IntensityRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PercentValueRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;

public abstract class BasicFeatureTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 421614380932687896L;

	public BasicFeatureTable() {

		super();

		percentRenderer = new PercentValueRenderer();
		cfRenderer = new MsFeatureRenderer(SortProperty.Name);
		radioRenderer = new RadioButtonRenderer();
		radioEditor = new RadioButtonEditor(new JCheckBox());
		radioEditor.addCellEditorListener(this);
		intensityRenderer = new IntensityRenderer();		
	}

	public abstract Collection<MsFeature> getSelectedFeatures();

	public abstract MsFeature getSelectedFeature();

	public abstract int getFeatureRow(MsFeature f);

	public abstract void setTableModelFromFeatureCluster(MsFeatureCluster selectedCluster);
	
	public abstract Map<DataPipeline, Collection<MsFeature>>getSelectedFeaturesMap();
}
