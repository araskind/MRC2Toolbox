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

package edu.umich.med.mrc2.datoolbox.utils.acqmethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;

public class AgilentDevicesParser {
	
	public static final String DEVICES_NODE = "Devices";
	public static final String DEVICE_NODE = "Device";
	public static final String NAME_NODE = "Name";
	public static final String MODEL_NODE = "ModelNumber";
	public static final String SN_NODE = "SerialNumber";
	
	public static Collection<LIMSInstrument>parseDevicesFile(File devicesFile){
		
		if(devicesFile == null || !devicesFile.exists())
			return null;
		
		Collection<LIMSInstrument>instruments = new ArrayList<LIMSInstrument>();
		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(devicesFile);
			Element rootNode = doc.getRootElement();
			List<Element> list = rootNode.getChildren(DEVICE_NODE);
			for (Element instrumentElement : list) {

				String name = instrumentElement.getChildText(NAME_NODE);
				String model = instrumentElement.getChildText(MODEL_NODE);
				String sn = instrumentElement.getChildText(SN_NODE);
				LIMSInstrument instrument = new LIMSInstrument(name, "Agilent", model, sn);
				instruments.add(instrument);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instruments;
	}
}
