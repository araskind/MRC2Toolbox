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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MotrpacProjectTreeRenderer extends DefaultTreeCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = -926952639135504257L;

	static final Icon projectIcon = GuiUtils.getIcon("idProject", 24);
	static final Icon experimentIcon = GuiUtils.getIcon("idExperiment", 24);
	static final Icon samplePrepIcon = GuiUtils.getIcon("samplePrep", 24);
	static final Icon acquisitionIcon = GuiUtils.getIcon("dataAcquisition", 24);
	static final Icon dataProcessingIcon = GuiUtils.getIcon("dataProcessing", 24);

	static final Font bigFont = new Font("SansSerif", Font.BOLD, 12);
	static final Font smallerFont = new Font("SansSerif", Font.PLAIN, 11);
	static final Font smallFont = new Font("SansSerif", Font.PLAIN, 10);

	static final Color defaultColor = Color.BLACK;
	static final Color lockedColor = Color.BLUE;

	private static final Border border = BorderFactory.createEmptyBorder(1, 1, 1, 1);

	public MotrpacProjectTreeRenderer() {
		setOpenIcon(null);
		setClosedIcon(null);
		setLeafIcon(null);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object node, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {

		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, node, sel, expanded, leaf, row, hasFocus);
		label.setBorder(border);

		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		Object embeddedObject = treeNode.getUserObject();

		if (embeddedObject instanceof MoTrPACStudy) {

			label.setText(((MoTrPACStudy)embeddedObject).toString());
			label.setIcon(projectIcon);
			label.setFont(bigFont);
		}
		if (embeddedObject instanceof LIMSExperiment) {

			label.setText(((LIMSExperiment)embeddedObject).getName());
			label.setIcon(experimentIcon);
			label.setFont(smallerFont);
		}
		if (embeddedObject instanceof LIMSSamplePreparation) {

			label.setText(((LIMSSamplePreparation)embeddedObject).getName());
			label.setIcon(samplePrepIcon);
			label.setFont(smallerFont);
		}
		if (embeddedObject instanceof DataAcquisitionMethod) {

			label.setText(((DataAcquisitionMethod)embeddedObject).getName());
			label.setIcon(acquisitionIcon);
			label.setFont(smallerFont);
		}
		if (embeddedObject instanceof DataExtractionMethod) {

			label.setText(((DataExtractionMethod)embeddedObject).getName());
			label.setIcon(dataProcessingIcon);
			label.setFont(smallerFont);
		}
		return label;
	}
}








