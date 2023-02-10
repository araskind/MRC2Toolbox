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
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ObjectAnnotationToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 4797279874278719042L;

	private static final Icon newAnnotationIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon attachDocumentIcon = GuiUtils.getIcon("attachDocument", 32);
	private static final Icon editAnnotationIcon = GuiUtils.getIcon("editCollection", 32);
	private static final Icon deleteAnnotationIcon = GuiUtils.getIcon("deleteCollection", 32);	
	private static final Icon previewIcon = GuiUtils.getIcon("previewDocument", 32);
	private static final Icon downloadDocumentIcon = GuiUtils.getIcon("downloadDocument", 32);
	private static final Icon structuralAnnotationIcon = GuiUtils.getIcon("editLibraryFeature", 32);

	private JButton
		newAnnotationButton,
		attachDocumentButton,
		structuralAnnotationButton,
		editAnnotationButton,
		deleteAnnotationButton,
		previewButton,
		downloadDocumentButton;

	public ObjectAnnotationToolbar(ActionListener commandListener) {

		super(commandListener);

		newAnnotationButton = GuiUtils.addButton(this, null, newAnnotationIcon, commandListener,
				MainActionCommands.ADD_OBJECT_ANNOTATION_COMMAND.getName(),
				MainActionCommands.ADD_OBJECT_ANNOTATION_COMMAND.getName(), buttonDimension);

		attachDocumentButton = GuiUtils.addButton(this, null, attachDocumentIcon, commandListener,
				MainActionCommands.ATTACH_DOCUMENT_ANNOTATION_COMMAND.getName(),
				MainActionCommands.ATTACH_DOCUMENT_ANNOTATION_COMMAND.getName(), buttonDimension);
		
		structuralAnnotationButton = GuiUtils.addButton(this, null, structuralAnnotationIcon, commandListener,
				MainActionCommands.ADD_STRUCTURAL_ANNOTATION_COMMAND.getName(),
				MainActionCommands.ADD_STRUCTURAL_ANNOTATION_COMMAND.getName(), buttonDimension);
		structuralAnnotationButton.setEnabled(false);

		addSeparator(buttonDimension);

		editAnnotationButton = GuiUtils.addButton(this, null, editAnnotationIcon, commandListener,
				MainActionCommands.EDIT_OBJECT_ANNOTATION_COMMAND.getName(),
				MainActionCommands.EDIT_OBJECT_ANNOTATION_COMMAND.getName(), buttonDimension);

		deleteAnnotationButton = GuiUtils.addButton(this, null, deleteAnnotationIcon, commandListener,
				MainActionCommands.DELETE_OBJECT_ANNOTATION_COMMAND.getName(),
				MainActionCommands.DELETE_OBJECT_ANNOTATION_COMMAND.getName(), buttonDimension);
		
		addSeparator(buttonDimension);

		previewButton = GuiUtils.addButton(this, null, previewIcon, commandListener,
				MainActionCommands.PREVIEW_ANNOTATION_COMMAND.getName(),
				MainActionCommands.PREVIEW_ANNOTATION_COMMAND.getName(), buttonDimension);

		downloadDocumentButton = GuiUtils.addButton(this, null, downloadDocumentIcon, commandListener,
				MainActionCommands.DOWNLOAD_ANNOTATION_COMMAND.getName(),
				MainActionCommands.DOWNLOAD_ANNOTATION_COMMAND.getName(), buttonDimension);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

//		boolean active = true;
//		if(project == null || newAssay == null)
//			active = false;
//
//		newAnnotationButton.setEnabled(active);
//		editAnnotationButton.setEnabled(active);
//		deleteAnnotationButton.setEnabled(active);
	}
}
