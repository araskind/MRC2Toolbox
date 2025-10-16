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

package edu.umich.med.mrc2.datoolbox.gui.tables.editors;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class ExperimentalLevelSelectorEditor extends DefaultCellEditor {

	/**
	 *
	 */
	private static final long serialVersionUID = -2826530332761287294L;
	private SortedComboBoxModel boxModel;
	private ExperimentDesignLevel[] modelOptions = null;
	private ExperimentDesignLevel selectedLevel = null;

	/**
	 * @param comboBox
	 */
	public ExperimentalLevelSelectorEditor(JComboBox comboBox) {
		super(comboBox);
	}

	@Override
	public Object getCellEditorValue() {
		return ((JComboBox)editorComponent).getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		modelOptions = null;
		selectedLevel = null;
		JComboBox comboBox = ((JComboBox)editorComponent);

		if (value instanceof ExperimentDesignLevel) {

			selectedLevel = (ExperimentDesignLevel) value;
			modelOptions = selectedLevel.getParentFactor().getLevels()
					.toArray(new ExperimentDesignLevel[selectedLevel.getParentFactor().getLevels().size()]);

			boxModel = new SortedComboBoxModel(modelOptions);
			comboBox.setModel(boxModel);

			if(selectedLevel == null)
				comboBox.setSelectedIndex(-1);
			else
				comboBox.setSelectedItem(selectedLevel);
		}
		else {
			if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null) {

				for(ExperimentDesignFactor factor : MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getFactors()) {

					if(table.getColumnName(column).equals(factor.getName())){

						modelOptions = factor.getLevels().toArray(new ExperimentDesignLevel[factor.getLevels().size()]);
						boxModel = new SortedComboBoxModel(modelOptions);
						comboBox.setModel(boxModel);
						comboBox.setSelectedIndex(-1);
						break;
					}
				}
			}
		}
//		comboBox.addItemListener(new ItemListener() {
//
//			public void itemStateChanged(ItemEvent arg0) {
//
//				if (arg0.getStateChange() == ItemEvent.SELECTED) {
//					stopCellEditing();
//					//	TODO this will be removed and replaced by exp design event
//					//	((DefaultTableModel) table.getModel()).fireTableDataChanged();
//				}
//			}
//		});
		comboBox.setToolTipText("Click to select value");

		return comboBox;
	}
}
