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

package edu.umich.med.mrc2.datoolbox.gui.annotation;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ObjectAnnotationTablePopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2668753009483045817L;
	
	private static final Icon editAnnotationIcon = GuiUtils.getIcon("editCollection", 24);
	private static final Icon deleteAnnotationIcon = GuiUtils.getIcon("deleteCollection", 24);	
	private static final Icon previewIcon = GuiUtils.getIcon("previewDocument", 24);
	private static final Icon downloadDocumentIcon = GuiUtils.getIcon("downloadDocument", 24);
	
	private JMenuItem editAnnotationMenuItem;
	private JMenuItem deleteAnnotationMenuItem;
	private JMenuItem previewMenuItem;
	private JMenuItem downloadDocumentMenuItem;
	
	public ObjectAnnotationTablePopupMenu(ActionListener listener) {
		super();

		editAnnotationMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.EDIT_OBJECT_ANNOTATION_COMMAND.getName(), listener,
				MainActionCommands.EDIT_OBJECT_ANNOTATION_COMMAND.getName());
		editAnnotationMenuItem.setIcon(editAnnotationIcon);
		
		deleteAnnotationMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DELETE_OBJECT_ANNOTATION_COMMAND.getName(), listener,
				MainActionCommands.DELETE_OBJECT_ANNOTATION_COMMAND.getName());
		deleteAnnotationMenuItem.setIcon(deleteAnnotationIcon);

		this.addSeparator();

		previewMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.PREVIEW_ANNOTATION_COMMAND.getName(), listener,
				MainActionCommands.PREVIEW_ANNOTATION_COMMAND.getName());
		previewMenuItem.setIcon(previewIcon);
		
		downloadDocumentMenuItem = GuiUtils.addMenuItem(this,
				MainActionCommands.DOWNLOAD_ANNOTATION_COMMAND.getName(), listener,
				MainActionCommands.DOWNLOAD_ANNOTATION_COMMAND.getName());
		downloadDocumentMenuItem.setIcon(downloadDocumentIcon);
	}
}




