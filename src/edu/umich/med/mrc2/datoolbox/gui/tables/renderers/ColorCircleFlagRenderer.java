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

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelIcon;

public class ColorCircleFlagRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6441628179300521628L;
	private Icon levelIcon;
	private int size;
	
	public ColorCircleFlagRenderer(Color flagColor, int size) {
		super();
		setOpaque(true);
		levelIcon = new IdLevelIcon(size, flagColor);
//	    setVerticalTextPosition(JLabel.CENTER);
//	    setHorizontalTextPosition(JLabel.CENTER);
//        setHorizontalAlignment(SwingConstants.CENTER);
//        setVerticalAlignment(SwingConstants.CENTER);
	}
	
	public ColorCircleFlagRenderer(int size) {
		super();
		setIcon(null);
		this.size = size;
		setOpaque(true);
//	    setVerticalTextPosition(JLabel.CENTER);
//	    setHorizontalTextPosition(JLabel.CENTER);
//        setHorizontalAlignment(SwingConstants.CENTER);
//        setVerticalAlignment(SwingConstants.CENTER);
	}
	
	@Override
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		if(value == null) {
			setIcon(null);
			return this;
		}
		if(value instanceof Boolean) {			
			boolean flag = (Boolean)value;
			levelIcon = new IdLevelIcon(size, Color.RED);
			if(!flag)
				setIcon(null);
			else
				setIcon(levelIcon);
		}
		if(value instanceof ReferenceMsMsLibraryMatch) {
			
			ReferenceMsMsLibraryMatch match = (ReferenceMsMsLibraryMatch)value;	
			if(match.isDecoyMatch()) {
				
				if(match.getMatchType().equals(MSMSMatchType.Regular)) {
					setIcon(new SmallPie(size, 0.5d, Color.RED, Color.GREEN));
					setToolTipText("Regular (precursor) match / Decoy");
				}
				else if(match.getMatchType().equals(MSMSMatchType.InSource)) {
					setIcon(new SmallPie(size, 0.5d, Color.RED, Color.BLUE));
					setToolTipText("In-source match / Decoy");
				}
				else if(match.getMatchType().equals(MSMSMatchType.Hybrid)) {
					setIcon(new SmallPie(size, 0.5d, Color.RED, Color.ORANGE));
					setToolTipText("Hybrid match / Decoy");
				}
				else {
					setIcon(null);
					setToolTipText(null);
				}
			}
			else{
				if(match.getMatchType().equals(MSMSMatchType.Regular)) {
					setIcon(new SmallPie(size, 1.0d, Color.GREEN, Color.GREEN));
					setToolTipText("Regular (precursor) match");
				}
				else if(match.getMatchType().equals(MSMSMatchType.InSource)) {
					setIcon(new SmallPie(size, 1.0d, Color.BLUE, Color.BLUE));
					setToolTipText("In-source match");
				}
				else if(match.getMatchType().equals(MSMSMatchType.Hybrid)) {
					setIcon(new SmallPie(size, 1.0d, Color.ORANGE, Color.ORANGE));
					setToolTipText("Hybrid match");
				}
				else {
					setIcon(null);
					setToolTipText(null);
				}
			}
		}		
		return this;
	}
}
