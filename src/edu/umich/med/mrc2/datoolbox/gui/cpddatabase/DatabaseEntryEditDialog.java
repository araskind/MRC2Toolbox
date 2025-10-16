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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DatabaseEntryEditDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -6905811601900477816L;

	private static final Icon editLibraryFeatureIcon = GuiUtils.getIcon("editLibraryFeature", 32);

	public DatabaseEntryEditDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Edit database entry");
		setIconImage(((ImageIcon) editLibraryFeatureIcon).getImage());

		setModalityType(ModalityType.MODELESS);
		setSize(new Dimension(450, 220));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void loadData(CompoundIdentity id) {
		// TODO Auto-generated method stub

	}

}
