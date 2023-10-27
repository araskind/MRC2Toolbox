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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RenamableObjectEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalLevelRenderer;

public class LevelsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5773952561737781351L;

	private ExperimentDesignFactor activeFactor;
	private ExperimentalLevelRenderer elRenderer;
	private RenamableObjectEditor editor;

	public LevelsTable() {

		super();
		model = new LevelsTableModel();
		setModel(model);

		elRenderer = new ExperimentalLevelRenderer();
		editor = new RenamableObjectEditor(new JTextField());

		getColumnModel().getColumn(model.getColumnIndex(LevelsTableModel.LEVEL_COLUMN))
			.setCellRenderer(elRenderer);

		getColumnModel().getColumn(model.getColumnIndex(LevelsTableModel.LEVEL_COLUMN))
			.setCellEditor(editor);

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		finalizeLayout();
	}

	public void setTableModelFromFactor(ExperimentDesignFactor factor) {

		activeFactor = factor;
		((LevelsTableModel)model).setTableModelFromFactor(factor);
		adjustColumns();
	}

	public void addLevel(ExperimentDesignLevel newLevel) {

		((LevelsTableModel)model).addLevel(newLevel);
		adjustColumns();
	}

	public Collection<ExperimentDesignLevel>getLevels(){

		int levelsColumn = model.getColumnIndex(LevelsTableModel.LEVEL_COLUMN);
		ArrayList<ExperimentDesignLevel> levels = new ArrayList<ExperimentDesignLevel>();
		for(int i=0; i<model.getRowCount(); i++)
			levels.add((ExperimentDesignLevel) model.getValueAt(i, levelsColumn));

		return levels;
	}

	public ExperimentDesignLevel getLevelByName(String name) {

		return getLevels().stream().
				filter(l -> l.getName().equals(name)).
				findFirst().orElse(null);
	}
}






















