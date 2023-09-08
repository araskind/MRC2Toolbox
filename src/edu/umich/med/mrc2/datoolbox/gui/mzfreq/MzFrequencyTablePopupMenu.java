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

package edu.umich.med.mrc2.datoolbox.gui.mzfreq;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelIcon;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MzFrequencyTablePopupMenu extends BasicTablePopupMenu {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8828829260409589130L;

	protected static final Icon editIcon = GuiUtils.getIcon("editLibraryFeature", 24);
	private static final Icon setIdLevelIcon = GuiUtils.getIcon("editIdStatus", 24);
	private static final int MASK =
		    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
	
	private ActionListener secondaryListener;
	protected JMenuItem editCompoundMenuItem;

	private JMenu idLevelMenu;
		
	public MzFrequencyTablePopupMenu(
			ActionListener basicListener, 
			ActionListener secondaryListener, 
			boolean enableTrackerCommands) {
		super(basicListener);
		
		this.secondaryListener = secondaryListener;
		
		if(enableTrackerCommands) {
			
			idLevelMenu = new JMenu("Set ID confidence level for selected");
			idLevelMenu.setIcon(setIdLevelIcon);
			populateIdLevelMenu();
			add(idLevelMenu);
			
			addSeparator();
		}
		addCopyBlock();
	}
	
	private void populateIdLevelMenu() {
		
		for(MSFeatureIdentificationLevel level : IDTDataCache.getMsFeatureIdentificationLevelList()) {
			
			Icon levelIcon = new IdLevelIcon(24, level.getColorCode());
			JMenuItem levelItem = 
					GuiUtils.addMenuItem(idLevelMenu, level.getName(), secondaryListener, level.getName(), levelIcon);
			if(level.getShorcut() != null)
				levelItem.setAccelerator(KeyStroke.getKeyStroke(level.getShorcut().charAt(0), MASK | InputEvent.SHIFT_DOWN_MASK));
		}
	}
}



