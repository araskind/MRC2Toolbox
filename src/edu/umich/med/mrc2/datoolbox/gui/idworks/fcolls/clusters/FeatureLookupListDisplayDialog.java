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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.clusters;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupList;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.FeatureListImportPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class FeatureLookupListDisplayDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3062896101017122798L;
	
	private static final Icon lookupFeatureListIcon = GuiUtils.getIcon("searchLibrary", 32);
	
	public FeatureLookupListDisplayDialog(FeatureLookupList featureLookupDataSet) {
		super();
		setPreferredSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);		
		setTitle(featureLookupDataSet.getName());
		setIconImage(((ImageIcon) lookupFeatureListIcon).getImage());
		
		FeatureListImportPanel featureListImportPanel = new FeatureListImportPanel();
		featureListImportPanel.disableLoadingFeatures();
		getContentPane().add(featureListImportPanel, BorderLayout.CENTER);
		featureListImportPanel.loadDataSet(featureLookupDataSet);
		pack();
	}
}











