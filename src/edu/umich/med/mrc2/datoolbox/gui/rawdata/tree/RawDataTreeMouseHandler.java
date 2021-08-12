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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.FileColorDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class RawDataTreeMouseHandler extends MouseAdapter implements ActionListener {
	
	private static final Icon deleteIcon = GuiUtils.getIcon("delete", 24);
	private static final Icon colorPickerIcon = GuiUtils.getIcon("colorPicker", 24);
	private static final Icon sortTreeByNameIcon = GuiUtils.getIcon("sortByName", 24);
	private static final Icon sortTreeByMassIcon = GuiUtils.getIcon("sortByMass", 24);

	private FileColorDialog fcc;	
	private RawDataTree tree;
	private RawDataTreeModel treeModel;
	private RawDataExaminerPanel parentPanel;
	
	private JPopupMenu
		dataFilePopupMenu, 
		sortPopupMenu,
		chomatogramPopupMenu, 
		spectrumPopupMenu;

	@SuppressWarnings("unused")
	private JMenuItem 	
		selectFileColorItem,
		removeChromatogramItem, 
		removeSpectrumItem;
		
	//	TODO sort by type
	public RawDataTreeMouseHandler(RawDataTree tree, RawDataExaminerPanel parentPanel) {

		this.tree = tree;
		treeModel = (RawDataTreeModel)tree.getModel();
		this.parentPanel = parentPanel;

		dataFilePopupMenu = new JPopupMenu();		
		selectFileColorItem = GuiUtils.addMenuItem(dataFilePopupMenu, MainActionCommands.CHOOSE_FILE_COLOR.getName(),
				this, MainActionCommands.CHOOSE_FILE_COLOR.getName(), colorPickerIcon);
		
		chomatogramPopupMenu = new JPopupMenu();
		removeChromatogramItem = GuiUtils.addMenuItem(chomatogramPopupMenu, MainActionCommands.REMOVE_CHROMATOGRAM.getName(),
				this, MainActionCommands.REMOVE_CHROMATOGRAM.getName(), deleteIcon);
		
		spectrumPopupMenu = new JPopupMenu();
		removeSpectrumItem = GuiUtils.addMenuItem(spectrumPopupMenu, MainActionCommands.REMOVE_SPECTRUM.getName(),
				this, MainActionCommands.REMOVE_SPECTRUM.getName(), deleteIcon);

		fcc = new FileColorDialog(this);
	}

	public void actionPerformed(ActionEvent e) {
		
		treeModel = (RawDataTreeModel)tree.getModel();

		if (e.getActionCommand().equals(MainActionCommands.CHOOSE_FILE_COLOR.getName())) {			
			fcc.setLocationRelativeTo(tree);
			fcc.setVisible(true);
		}
		if (e.getActionCommand().equals(MainActionCommands.SET_FILE_COLOR.getName()))
			setFileColor();
		
		if (e.getActionCommand().equals(MainActionCommands.REMOVE_SPECTRUM.getName()))
			removeSpectrum();
		
		if (e.getActionCommand().equals(MainActionCommands.REMOVE_CHROMATOGRAM.getName()))
			removeChromatogram();
	}

	private void removeChromatogram() {

		List<ExtractedChromatogram> selectedChromatograms = 
				tree.getSelectedObjects().stream().
				filter(f -> (f instanceof ExtractedChromatogram)).
				map( ExtractedChromatogram.class::cast ).
				collect(Collectors.toList());
		
		if(selectedChromatograms.size() == 0)
			return;
		
		String yesNoQuestion = "Are you sure you want to delete selected cromatograms?";
		if(MessageDialog.showChoiceMsg(yesNoQuestion , MRC2ToolBoxCore.getMainWindow()) == JOptionPane.YES_OPTION) {
			
			for(ExtractedChromatogram ec : selectedChromatograms) {
				
				ec.getDataFile().getChromatograms().remove(ec);
				DefaultMutableTreeNode chrNode = treeModel.getNodeForObject(ec);
				if(chrNode != null)
					treeModel.removeNodeFromParent(chrNode);
			}
			parentPanel.clearChromatogramPanel();
		}
	}

	private void removeSpectrum() {

		List<AverageMassSpectrum> selectedSpectra = 
				tree.getSelectedObjects().stream().
				filter(f -> (f instanceof AverageMassSpectrum)).
				map( AverageMassSpectrum.class::cast ).
				collect(Collectors.toList());
		
		if(selectedSpectra.size() == 0)
			return;
	
		String yesNoQuestion = "Are you sure you want to delete selected spectra?";
		if(MessageDialog.showChoiceMsg(yesNoQuestion , MRC2ToolBoxCore.getMainWindow()) == JOptionPane.YES_OPTION) {
			
			for(AverageMassSpectrum ec : selectedSpectra) {
				
				ec.getDataFile().getAverageSpectra().remove(ec);
				DefaultMutableTreeNode chrNode = treeModel.getNodeForObject(ec);
				if(chrNode != null)
					treeModel.removeNodeFromParent(chrNode);
			}
			parentPanel.clearSpectraPanel();
		}	
	}

	private void setFileColor() {

		List<DataFile> selectedFiles = 
				tree.getSelectedObjects().stream().
				filter(f -> (f instanceof DataFile)).
				map( DataFile.class::cast ).
				collect(Collectors.toList());
		
		if(fcc.getNewColor() != null)
			selectedFiles.stream().forEach(df -> df.setColor(fcc.getNewColor()));
			
		fcc.setVisible(false);
	}

	@Override
	public void mousePressed(MouseEvent e) {

		if (e.isPopupTrigger())
			handlePopupTriggerEvent(e);

		if ((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1))
			handleDoubleClickEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger())
			handlePopupTriggerEvent(e);
	}

	private void handlePopupTriggerEvent(MouseEvent e) {

		TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());
		if (clickedPath == null)
			return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) clickedPath
				.getLastPathComponent();
		Object clickedObject = node.getUserObject();

		if (clickedObject instanceof DataFile)
			dataFilePopupMenu.show(e.getComponent(), e.getX(), e.getY());
		
		if (clickedObject instanceof ExtractedChromatogram)
			chomatogramPopupMenu.show(e.getComponent(), e.getX(), e.getY());
		
		if (clickedObject instanceof AverageMassSpectrum)
			spectrumPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	private void handleDoubleClickEvent(MouseEvent e) {
		TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());
		if (clickedPath == null)
			return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) clickedPath
				.getLastPathComponent();
		Object clickedObject = node.getUserObject();

		if (clickedObject instanceof DataFile) {
			//	TODO;
		}

		if (clickedObject instanceof MassSpectrum) {
			//	TODO
		}

		if (clickedObject instanceof ExtractedChromatogram) {
			//	TODO
		}
	}
}