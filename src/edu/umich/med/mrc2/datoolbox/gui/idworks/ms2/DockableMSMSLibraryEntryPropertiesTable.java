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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2;

import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.tables.PropertiesTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMSMSLibraryEntryPropertiesTable extends DefaultSingleCDockable {

	private PropertiesTable propertiesTable;
	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);

	public DockableMSMSLibraryEntryPropertiesTable() {

		super("DockableMSMSLibraryEntryPropertiesTable", componentIcon, "MSMS library entry properties", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		propertiesTable = new PropertiesTable();
		propertiesTable.setPropertyValueRenderer(new WordWrapCellRenderer());
		add(new JScrollPane(propertiesTable));
		
	}

	/**
	 * @return the libraryFeatureTable
	 */
	public PropertiesTable getTable() {
		return propertiesTable;
	}

	public void clearTable() {
		propertiesTable.clearTable();
	}
	
	public void setTableModelFromPropertyMap(Map<? extends Object,? extends Object>properties) {
		propertiesTable.setTableModelFromPropertyMap(properties);
	}
	
	public void showMsMsLibraryFeatureProperties(MsMsLibraryFeature libFeature) {
		
		if(!libFeature.hasLibraryAnnotations()) {
			try {
				Collection<String>annotations = MSMSLibraryUtils.getMsMsFeatureLibraryAnnotations(libFeature.getUniqueId());
				int counter = 1;
				for(String annotation : annotations) {
					libFeature.getProperties().put(
							MSMSLibraryUtils.ANNOTATION_FIELD_NAME + " " + Integer.toString(counter), 
							annotation);
					counter++;
					}				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		propertiesTable.setTableModelFromPropertyMap(libFeature.getProperties());
	}
}
