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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;

public class ExperimentalSampleComboboxRenderer extends JLabel 
		implements ListCellRenderer<ExperimentalSample> {

	/**
	 *
	 */
	private static final long serialVersionUID = -1755035408278132072L;


	@Override
	public Component getListCellRendererComponent(JList<? extends ExperimentalSample> list, ExperimentalSample value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		if(value == null){
			setText("SELECT ...");
			return this;
		}
		if (value instanceof ExperimentalSample) {
			
			ExperimentalSample sample = (ExperimentalSample) value;
			setText(sample.getId() + " (" + sample.getName() + ")");
		}
		return this;
	}
}
