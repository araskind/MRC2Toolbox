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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.tree;

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

import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.scan.IScan;

public class RawDataTreeRenderer extends DefaultTreeCellRenderer {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 626574188837349442L;
	
	private static final Icon rawFilesCollectionIcon = GuiUtils.getIcon("xmlMsDataFileCollection", 24);
	private static final Icon rawFileIcon = GuiUtils.getIcon("xmlMsDataFile", 24);
	private static final Icon chromatogramCollectionIcon = GuiUtils.getIcon("chromatogramCollection", 24);
	private static final Icon chromatogramIcon = GuiUtils.getIcon("chromatogram", 24);
	private static final Icon averageSpectrumCollectionIcon = GuiUtils.getIcon("averageSpectrumCollection", 24);
	private static final Icon averageSpectrumIcon = GuiUtils.getIcon("avgSpectrum", 24);	
	private static final Icon scanCollectionIcon = GuiUtils.getIcon("scanCollection", 24);
	private static final Icon spectrumIcon = GuiUtils.getIcon("spectrumicon", 16);
	
	static final Font bigFont = new Font("SansSerif", Font.BOLD, 14);
	static final Font smallerFont = new Font("SansSerif", Font.BOLD, 12);
	static final Font smallerPlainFont = new Font("SansSerif", Font.PLAIN, 12);
	static final Font smallFont = new Font("SansSerif", Font.PLAIN, 10);

	private static final Border border = BorderFactory.createEmptyBorder ( 3, 3, 3, 3 );
	
	private static final Color positiveColor = new Color(255, 0, 0);
	private static final Color negativeColor = new Color(0, 0, 255);
	
	public RawDataTreeRenderer() {
		
		setOpenIcon(null);
		setClosedIcon(null);
		setLeafIcon(null);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object node,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, node,
				selected, expanded, leaf, row, hasFocus);

		label.setBorder ( border );
		if(selected)
			label.setForeground(Color.WHITE);
		
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		Object embeddedObject = treeNode.getUserObject();
		if (embeddedObject == RawDataTreeModel.dataFilesNodeName) {
			
			label.setFont(bigFont);
			label.setIcon(rawFilesCollectionIcon);
		}
		if (embeddedObject == RawDataTreeModel.scansNodeName) {
			label.setFont(smallerFont);
			label.setIcon(scanCollectionIcon);
		}		
		if (embeddedObject == RawDataTreeModel.chromatogramNodeName) {
			label.setFont(smallerFont);
			label.setIcon(chromatogramCollectionIcon);
		}		
		if (embeddedObject == RawDataTreeModel.spectraNodeName) {
			label.setFont(smallerFont);
			label.setIcon(averageSpectrumCollectionIcon);
		}
		if (embeddedObject instanceof DataFile) {
			
			label.setIcon(rawFileIcon);
			label.setFont(smallerFont);
			if(!selected)
				label.setForeground(((DataFile)embeddedObject).getColor());	
		}
		if (embeddedObject instanceof ExtractedChromatogram) {
			
			if(!selected) {
				DataFile df = ((ExtractedChromatogram)embeddedObject).getDataFile();
				label.setForeground(df.getColor());	
			}
			label.setFont(smallerPlainFont);
			label.setIcon(chromatogramIcon);
		}		
		if (embeddedObject instanceof AverageMassSpectrum) {
			
			if(!selected) {
				DataFile df = ((AverageMassSpectrum)embeddedObject).getDataFile();
				label.setForeground(df.getColor());	
			}
			label.setFont(smallerPlainFont);
			label.setIcon(averageSpectrumIcon);
		}
		if (embeddedObject instanceof IScan) {
			IScan s = (IScan) embeddedObject;
			label.setIcon(null);
			label.setFont(smallerPlainFont);
			label.setText(RawDataUtils.getScanLabel(s));
			if (!selected) {
				if (s.getMsLevel() > 1)
					label.setForeground(Color.red);
				else
					label.setForeground(Color.blue);
			}
		}
		return label;
	}
}
