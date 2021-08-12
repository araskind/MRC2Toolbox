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

package edu.umich.med.mrc2.datoolbox.gui.mstools;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import org.openscience.cdk.formula.IsotopePattern;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.SimpleMs;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableIsotopePatternTable extends DefaultSingleCDockable {

	private MsTable isotopePatternTable;
	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);

	public DockableIsotopePatternTable(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		isotopePatternTable = new MsTable();
		add(new JScrollPane(isotopePatternTable));
	}

	public void setTableModelFromMsPointCollection(Collection<MsPoint> pattern) {
		isotopePatternTable.setTableModelFromMsPointCollection(pattern);
	}

	public void setTableModelFromIsotopePattern(IsotopePattern isoPattern) {
		isotopePatternTable.setTableModelFromIsotopePattern(isoPattern);
	}

	public void setTableModelFromSimpleMs(SimpleMs ms) {
		isotopePatternTable.setTableModelFromSimpleMs(ms);
	}

	public void clearTable() {
		isotopePatternTable.clearTable();
	}
}
