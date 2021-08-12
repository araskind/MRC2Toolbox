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

package edu.umich.med.mrc2.datoolbox.gui.annotation.editors;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ObjectAnnotationEditorToolbar extends CommonToolbar{

	/**
	 *
	 */
	private static final long serialVersionUID = 8482889762452329788L;

	private static final Icon textDocumentIcon = GuiUtils.getDocumentFormatIcon(DocumentFormat.TXT, 32);
	private static final Icon rtfDocumentIcon = GuiUtils.getDocumentFormatIcon(DocumentFormat.RTF, 32);
	private static final Icon attachDocumentIcon = GuiUtils.getIcon("attachDocument", 32);
	private static final Icon saveAnnotationIcon = GuiUtils.getIcon("saveAnnotation", 32);

	private JButton
		annotationFormatButton,
		attachDocumentButton,
		saveAnnotationButton;

	public ObjectAnnotationEditorToolbar(ActionListener commandListener, ActionListener saveActionListener) {

		super(commandListener);

		annotationFormatButton = GuiUtils.addButton(this, "Plain text mode", textDocumentIcon, commandListener,
				MainActionCommands.RTF_ANNOTATION_FORMAT_COMMAND.getName(),
				MainActionCommands.RTF_ANNOTATION_FORMAT_COMMAND.getName(), buttonDimension);

		attachDocumentButton = GuiUtils.addButton(this, null, attachDocumentIcon, commandListener,
				MainActionCommands.ATTACH_DOCUMENT_ANNOTATION_COMMAND.getName(),
				MainActionCommands.ATTACH_DOCUMENT_ANNOTATION_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		saveAnnotationButton = GuiUtils.addButton(this, null, saveAnnotationIcon, saveActionListener,
				MainActionCommands.SAVE_OBJECT_ANNOTATION_COMMAND.getName(),
				MainActionCommands.SAVE_OBJECT_ANNOTATION_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

	public void switchAnnotationFormat(DocumentFormat newFormat) {

		if(newFormat.equals(DocumentFormat.RTF)) {

			annotationFormatButton.setIcon(rtfDocumentIcon);
			annotationFormatButton.setActionCommand(MainActionCommands.TEXT_ANNOTATION_FORMAT_COMMAND.getName());
			annotationFormatButton.setText("Rich text mode");
			annotationFormatButton.setToolTipText(MainActionCommands.TEXT_ANNOTATION_FORMAT_COMMAND.getName());
		}
		if(newFormat.equals(DocumentFormat.TXT)) {

			annotationFormatButton.setIcon(textDocumentIcon);
			annotationFormatButton.setActionCommand(MainActionCommands.RTF_ANNOTATION_FORMAT_COMMAND.getName());
			annotationFormatButton.setText("Plain text mode");
			annotationFormatButton.setToolTipText(MainActionCommands.RTF_ANNOTATION_FORMAT_COMMAND.getName());
		}
	}
}











