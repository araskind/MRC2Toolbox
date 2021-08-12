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

package edu.umich.med.mrc2.datoolbox.gui.clustertree;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public abstract class ClusterFeaturePopupMenu extends JPopupMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -3995417142700448020L;

	protected static final Icon newClusterIcon = GuiUtils.getIcon("newCluster", 24);
	protected static final Icon deleteFromClusterIcon = GuiUtils.getIcon("deleteFromCluster", 24);
	protected static final Icon deleteFromListIcon = GuiUtils.getIcon("deleteCollection", 24);
	protected static final Icon searchLibraryIcon = GuiUtils.getIcon("searchLibrary", 24);
	protected static final Icon searchDatabaseIcon = GuiUtils.getIcon("searchDatabase", 24);
	protected static final Icon textAnnotationIcon = GuiUtils.getIcon("edit", 24);
	protected static final Icon mergeSelectedFeaturesIcon = GuiUtils.getIcon("merge", 24);

	protected JMenuItem newClusterFromSelectedMenuItem;
	protected JMenuItem removeSelecteFromClusterMenuItem;
	protected JMenuItem removeSelecteFromListMenuItem;
	protected JMenuItem matchFeatureToLibraryMenuItem;
	protected JMenuItem searchFeatureAgainstDatabaseMenuItem;
	protected JMenuItem textAnnotationMenuItem;
	protected JMenuItem mergeSelectedFeaturesMenuItem;

	protected ActionListener listener;

	public ClusterFeaturePopupMenu(ActionListener listener) {
		super();
		this.listener = listener;
	}

	protected abstract void populateMenu();
}
