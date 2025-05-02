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

package edu.umich.med.mrc2.datoolbox.gui.binner;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableBinnerAnnotationDetailsPanel extends DefaultSingleCDockable {

	
	private static final Icon componentIcon = GuiUtils.getIcon("bin", 16);

	private BinnerAnnotationDetailsTable binnerAnnotationDetailsTable;

	public DockableBinnerAnnotationDetailsPanel() {

		super("DockableBinnerAnnotationDetailsPanel", componentIcon, 
				"Binner annotations", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		binnerAnnotationDetailsTable = new BinnerAnnotationDetailsTable();
		JScrollPane designScrollPane = new JScrollPane(binnerAnnotationDetailsTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
	}
	
	public void clearPanel() {
		binnerAnnotationDetailsTable.clearTable();
	}
	
	public void setTableModelFromBinnerAnnotationCluster(
			BinnerBasedMsFeatureInfoBundleCluster baCluster) {
		binnerAnnotationDetailsTable.setTableModelFromBinnerAnnotationCluster(baCluster);
	}
	
	public void setTableModelFromBinnerAnnotations(
			Collection<BinnerAnnotation> annotations) {
		binnerAnnotationDetailsTable.setTableModelFromBinnerAnnotations(annotations);
	}

	public BinnerAnnotation getSelectedBinnerAnnotation() {
		return binnerAnnotationDetailsTable.getSelectedBinnerAnnotation();
	}
}































