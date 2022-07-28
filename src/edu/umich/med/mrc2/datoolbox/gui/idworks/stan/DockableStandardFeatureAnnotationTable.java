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

package edu.umich.med.mrc2.datoolbox.gui.idworks.stan;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableStandardFeatureAnnotationTable   extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	private StandardFeatureAnnotationTable standardAnnotationTable;

	public DockableStandardFeatureAnnotationTable() {

		super("DockableStandardFeatureAnnotationTable", componentIcon, "Standard annotations", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		standardAnnotationTable = new StandardFeatureAnnotationTable();
		add(new JScrollPane(standardAnnotationTable));
	}

	public synchronized void clearTable() {
		standardAnnotationTable.clearTable();
	}
	
	public void loadFeatureData(MSFeatureInfoBundle bundle) {
		standardAnnotationTable.setTableModelFromStandardFeatureAnnotationList(bundle.getStandadAnnotations());
	}

}
