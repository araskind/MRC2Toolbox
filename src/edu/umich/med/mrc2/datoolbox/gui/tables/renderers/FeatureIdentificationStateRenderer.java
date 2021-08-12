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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIdentificationState;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class FeatureIdentificationStateRenderer extends DefaultTableCellRenderer {

	private static final Border border = BorderFactory.createLineBorder(Color.GRAY, 3);
	
	public FeatureIdentificationStateRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setText("");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2442508175358508173L;
	
	private static final Icon singleIdIcon = GuiUtils.getIcon("singleId", 16);
	private static final Icon singleIdDisabledIcon = GuiUtils.getIcon("singleIdDisabled", 16);
	private static final Icon multipleIdsIcon = GuiUtils.getIcon("multipleIds", 16);
	private static final Icon multipleIdsDisabledIcon = GuiUtils.getIcon("multipleIdsDisabled", 16);
	private static final Icon unknownIcon = GuiUtils.getIcon("showUnknowns", 16);

	@Override
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		if(value == null) {	
			setIcon(null);
			return this;
		}
		if (value instanceof FeatureIdentificationState) {

			setBorder(null);
			FeatureIdentificationState state = (FeatureIdentificationState)value;
			if(state.equals(FeatureIdentificationState.SINGLE_ACTIVE_ID))
				setIcon(singleIdIcon);
			if(state.equals(FeatureIdentificationState.SINGLE_INACTIVE_ID)) {
				setIcon(singleIdDisabledIcon);
				setBorder(border);
			}
			if(state.equals(FeatureIdentificationState.MULTIPLE_ACTIVE_IDS))
				setIcon(multipleIdsIcon);
			if(state.equals(FeatureIdentificationState.MULTIPLE_INACTIVE_IDS)) {
				setIcon(multipleIdsDisabledIcon);
				setBorder(border);
			}
			if(state.equals(FeatureIdentificationState.NO_IDENTIFICATION))
				setIcon(unknownIcon);
		}
		return this;
	}
}
