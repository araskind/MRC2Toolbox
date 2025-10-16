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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;

public class MsFeatureInfoBundleRenderer extends DefaultTableCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = 3136340273175023112L;
	private SortProperty idField;
	private static final Border highlightBorder = new LineBorder(new Color(255, 0, 0), 2, true);

	public MsFeatureInfoBundleRenderer(SortProperty idField) {

		super();
		this.idField = idField;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Component rendererComponent = 
				table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());

		if(value == null){
			setText("");
			setBorder(null);
			return this;
		}
		if (value instanceof MSFeatureInfoBundle) {

			MsFeature msf = ((MSFeatureInfoBundle) value).getMsFeature();
			int fontSize = getFont().getSize();
			if(msf.isQcStandard()) {
				setFont(new Font("Default", Font.BOLD, fontSize));
				setForeground(Color.RED);
			}
			else {
				setFont(new Font("Default", Font.PLAIN, fontSize));
				setForeground(Color.BLACK);
			}
			if(msf.getPrimaryIdentity() != null && idField.equals(SortProperty.pimaryId))
				setText(msf.getPrimaryIdentity().getCompoundName());
			else
				setText(msf.getName());
			
			if(((MSFeatureInfoBundle) value).isUsedAsMatchingTarget())
				setBorder(highlightBorder);
			else
				setBorder(null);
		}
		return this;
	}
}
