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

package edu.umich.med.mrc2.datoolbox.gui.jcp;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.jchempaint.JChemPaintPanel;
import org.openscience.jchempaint.application.JChemPaint;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableStructuralAnnotationPanel extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("editLibraryFeature", 16);
	private StructuralAnnotationToolbar toolbar;
	private JChemPaintPanel jcPanel;

	public DockableStructuralAnnotationPanel(String id, String title, ActionListener actionListener) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		
		toolbar =  new StructuralAnnotationToolbar(actionListener);
		add(toolbar, BorderLayout.NORTH);
		
		IChemModel chemModel = JChemPaint.emptyModel();
		List<String>ignoreItems = Arrays.asList("new", "close", "exit");
		jcPanel = new JChemPaintPanel(chemModel, JChemPaint.GUI_APPLICATION, false, null, ignoreItems);
		add(jcPanel, BorderLayout.CENTER);
	}

	/**
	 * @return the jcPanel
	 */
	public JChemPaintPanel getJcPanel() {
		return jcPanel;
	}
}
