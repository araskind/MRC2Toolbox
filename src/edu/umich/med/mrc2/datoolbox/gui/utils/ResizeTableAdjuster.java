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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;

public class ResizeTableAdjuster extends ComponentAdapter {

	public static List<Component> getAllComponents(final Container c) {

		Component[] comps = c.getComponents();

		List<Component> compList = new ArrayList<Component>();

		for (Component comp : comps) {

			compList.add(comp);

			if (comp instanceof Container)
				compList.addAll(getAllComponents((Container) comp));
		}
		return compList;
	}

	@Override
	public void componentResized(ComponentEvent e) {

		List<Component> components = getAllComponents((Container) e.getSource());

		for (Component comp : components) {

			if (comp instanceof BasicTable)
				((BasicTable) comp).adjustColumns();
		}
	}
}
