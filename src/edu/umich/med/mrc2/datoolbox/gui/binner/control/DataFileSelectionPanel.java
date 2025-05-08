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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.gui.utils.ValidatableForm;

public class DataFileSelectionPanel extends JPanel implements ValidatableForm{

	private static final long serialVersionUID = 1L;
	
	private DataFileTable dataFileTable;

	public DataFileSelectionPanel() {
		
		super(new BorderLayout(0, 0));
		dataFileTable = new DataFileTable();
		add(new JScrollPane(dataFileTable), BorderLayout.CENTER);
	}
	
	public void setTableModelFromFileCollection(Collection<DataFile> files) {
		dataFileTable.setTableModelFromFileCollection(files);
	}
	
	public Collection<DataFile> getEnabledFiles() {
		return dataFileTable.getEnabledFiles();
	}

	@Override
	public Collection<String> validateFormData() {

		Collection<String>errors = new ArrayList<String>();
		if(getEnabledFiles().isEmpty())
			errors.add("No data files selected");
		
		return errors;
	}
}
