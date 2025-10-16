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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class ChemicalModificationEditor extends DefaultCellEditor {

	/**
	 *
	 */
	private static final long serialVersionUID = -2435888270932720819L;
	private JComboBox<Adduct> comboBox = new JComboBox<Adduct>();
	private SortedComboBoxModel boxModel;

	/**
	 * @param comboBox
	 */
	public ChemicalModificationEditor() {
		super(new JComboBox<>());
	}

	@Override
	public Object getCellEditorValue() {
		return comboBox.getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (value instanceof MsFeature) {

			MsFeature feature = (MsFeature) value;
			Adduct[] modelOptions = new Adduct[0];

			//	Set<ChemicalModification> set = feature.getParentCluster().getAnnotationMap().get(feature);
			//	TODO this whole section needs rewrite to deal with cluster/feature connection
			Set<Adduct> set = new HashSet<Adduct>();

			if(set != null)
				modelOptions = set.toArray(new Adduct[set.size()]);

			boxModel = new SortedComboBoxModel(modelOptions);
			comboBox.setModel(boxModel);
			comboBox.setSelectedItem(feature.getSuggestedModification());
			comboBox.setToolTipText("Click to select value");

			comboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent arg0) {

					if (arg0.getStateChange() == ItemEvent.SELECTED) {
						stopCellEditing();
						((DefaultTableModel) table.getModel()).fireTableDataChanged();
					}

				}
			});
		}
		return comboBox;
	}
}
