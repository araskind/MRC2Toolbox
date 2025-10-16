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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.tables.PropertiesTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.lcmsrun.LCMSRunInfo;
import umich.ms.datatypes.lcmsrun.MsSoftware;
import umich.ms.datatypes.scan.props.Instrument;

public class DockableRawDataFilePropertiesTable extends DefaultSingleCDockable {

	private PropertiesTable propertiesTable;
	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);

	public DockableRawDataFilePropertiesTable() {

		super("DockableRawDataFilePropertiesTable", componentIcon, "Data file properties", null, Permissions.MIN_MAX_STACK);
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

	public synchronized void clearTable() {
		propertiesTable.clearTable();
	}
	
	public void setTableModelFromPropertyMap(Map<? extends Object,? extends Object>properties) {
		propertiesTable.setTableModelFromPropertyMap(properties);
	}
	
	public void showDataFileProperties(LCMSData data) {
		
		propertiesTable.clearTable();
		if(data == null)
			return;
		
		LCMSRunInfo runInfo = data.getSource().getRunInfo();
		if(runInfo == null)
			return;
		
		Map<String,String>properties = new TreeMap<String,String>();
		Date acqDate = runInfo.getRunStartTime();
		Map<String, Instrument> instruments = runInfo.getInstruments();
		List<MsSoftware> softwareList = runInfo.getSoftware();
		String timestamp = "";
		try {
			timestamp = MRC2ToolBoxConfiguration.getDateTimeFormat().format(acqDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
		}
		properties.put("Start timestamp", timestamp);
		properties.put("Sample name", RawDataUtils.getSampleName(data));
		
		for(Entry<String, Instrument> entry : instruments.entrySet()) {			
			properties.put("Instrument " + entry.getKey(), entry.getValue().getManufacturer() + " " + entry.getValue().getModel());
		}
		for(MsSoftware software : softwareList) {
			properties.put("Software  ", software.name + " " + software.version) ;
		}
		propertiesTable.setTableModelFromPropertyMap(properties);
	}
}
