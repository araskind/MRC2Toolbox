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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scan.StorageStrategy;
import umich.ms.datatypes.scancollection.IScanCollection;
import umich.ms.fileio.exceptions.FileParsingException;

public class RawDataTreeModel extends DefaultTreeModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2864104877072789151L;
	
	public static final String rootNodeName = "Project";
	public static final String dataFilesNodeName = "Data files";
	public static final String chromatogramNodeName = "Chromatograms";
	public static final String scansNodeName = "Scans";
	public static final String spectraNodeName = "Spectra";

	private DefaultMutableTreeNode rootNode;
	private final DefaultMutableTreeNode dataFilesNode = new DefaultMutableTreeNode(dataFilesNodeName);
	private final DefaultMutableTreeNode chromatogramNode = new DefaultMutableTreeNode(chromatogramNodeName);
	private final DefaultMutableTreeNode spectraNode = new DefaultMutableTreeNode(spectraNodeName);

	private TreeGrouping treeGrouping;
	
	protected ConcurrentHashMap<Object, DefaultMutableTreeNode> treeObjects;

	public RawDataTreeModel(TreeNode root) {
		super(root);
		treeObjects = new ConcurrentHashMap<Object, DefaultMutableTreeNode>();
	}
	
	public RawDataTreeModel() {

		super(new DefaultMutableTreeNode(rootNodeName));
		treeObjects = new ConcurrentHashMap<Object, DefaultMutableTreeNode>();
		treeGrouping = TreeGrouping.BY_DATA_FILE;
		rootNode = (DefaultMutableTreeNode) super.getRoot();
		insertNodeInto(dataFilesNode, rootNode, 0);		
	}
	
	public RawDataTreeModel(TreeGrouping grouping) {

		super(new DefaultMutableTreeNode(rootNodeName));
		treeObjects = new ConcurrentHashMap<Object, DefaultMutableTreeNode>();
		treeGrouping = grouping;
		rootNode = (DefaultMutableTreeNode) super.getRoot();
		if(grouping.equals(TreeGrouping.BY_DATA_FILE)) {			
			insertNodeInto(dataFilesNode, rootNode, 0);
		}
		if(grouping.equals(TreeGrouping.BY_OBJECT_TYPE)) {			
			insertNodeInto(dataFilesNode, rootNode, 0);
			insertNodeInto(chromatogramNode, rootNode, 1);
			insertNodeInto(spectraNode, rootNode, 2);
		}
	}
	
	@Override
	public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {

		treeObjects.put(((DefaultMutableTreeNode) newChild).getUserObject(), (DefaultMutableTreeNode) newChild);
		super.insertNodeInto(newChild, parent, index);
	}
	
	@Override
    public void removeNodeFromParent(MutableTreeNode node) {

		treeObjects.remove(((DefaultMutableTreeNode) node).getUserObject());
		if(node.getParent() != null)		
			super.removeNodeFromParent(node);
    }
	
	public DefaultMutableTreeNode getNodeForObject(Object object) {
		return treeObjects.get(object);
	}
	
	public void clearObjectMap() {
		treeObjects.clear();
	}
	
	public Collection<Object>getTreeObjects(){
		return treeObjects.keySet();
	}
	
	public void updateNode(Object object) {
		DefaultMutableTreeNode node = getNodeForObject(object);
		if(node != null)
			this.nodeChanged(node);
	}
	
	/**
	 * This method must be called from Swing thread
	 */
	public boolean removeObject(final Object object) {

		if (!SwingUtilities.isEventDispatchThread())
			throw new IllegalStateException("This method must be called from Swing thread");
		
		if(object != null){

			final DefaultMutableTreeNode node = treeObjects.get(object);
			if( node != null){
				
				// Remove all children from treeObjects
				Enumeration e = node.depthFirstEnumeration();
				while (e.hasMoreElements()) {
					
					DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) e.nextElement();
					Object nodeObject = childNode.getUserObject();				
					if(nodeObject != null)
						treeObjects.remove(nodeObject);
					
					removeNodeFromParent(childNode);
				}	
				removeNodeFromParent(node);
				treeObjects.remove(object);		
				return true;
			}
		}
		return false;
	}

	public boolean addObject(Object object) {

		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("This method must be called from Swing thread");
		}
		if(treeObjects.containsKey(object))
			return false;
		
		if (object instanceof DataFile) {
			addDataFile((DataFile)object);
			return true;
		}
		if (object instanceof ExtractedChromatogram) {
			addCromatogram((ExtractedChromatogram)object);
			return true;
		}
		if (object instanceof AverageMassSpectrum) {
			addSpectrum((AverageMassSpectrum)object);
			return true;
		}		
		return false;
	}
	
	private void addDataFile(DataFile dataFile){
		
		final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(dataFile);
		insertNodeInto(newNode, dataFilesNode, getChildCount(dataFilesNode));
		
		if(treeGrouping.equals(TreeGrouping.BY_DATA_FILE)) {
			
			DefaultMutableTreeNode fileChromatogramNode = new DefaultMutableTreeNode(chromatogramNodeName);
			insertNodeInto(fileChromatogramNode, newNode, getChildCount(newNode));
			if(!dataFile.getChromatograms().isEmpty()) {
				dataFile.getChromatograms().stream().sorted().
					forEach(c -> insertNodeInto(new DefaultMutableTreeNode(c), fileChromatogramNode, getChildCount(fileChromatogramNode)));
			}
//			for (ExtractedChromatogram chrom : dataFile.getChromatograms())
//				insertNodeInto(new DefaultMutableTreeNode(chrom), fileChromatogramNode, getChildCount(fileChromatogramNode));								
				
			DefaultMutableTreeNode userSpectraNode = new DefaultMutableTreeNode(spectraNodeName);
			insertNodeInto(userSpectraNode, newNode, getChildCount(newNode));
			if(!dataFile.getAverageSpectra().isEmpty()) {
				dataFile.getAverageSpectra().stream().sorted().
					forEach(s -> insertNodeInto(new DefaultMutableTreeNode(s), userSpectraNode, getChildCount(userSpectraNode)));
			}
//			for (AverageMassSpectrum uSpec : dataFile.getAverageSpectra())
//				insertNodeInto(new DefaultMutableTreeNode(uSpec), userSpectraNode, getChildCount(userSpectraNode));											
		}
		if(treeGrouping.equals(TreeGrouping.BY_OBJECT_TYPE)) {
			
			if (!dataFile.getChromatograms().isEmpty()) {
				
				dataFile.getChromatograms().stream().sorted().
					forEach(c -> insertNodeInto(new DefaultMutableTreeNode(c), chromatogramNode, getChildCount(chromatogramNode)));
				
//				for (ExtractedChromatogram chrom : dataFile.getChromatograms())
//					insertNodeInto(new DefaultMutableTreeNode(chrom), chromatogramNode, getChildCount(chromatogramNode));								
			}
			if (!dataFile.getAverageSpectra().isEmpty()) {
				
				dataFile.getAverageSpectra().stream().sorted().
					forEach(s -> insertNodeInto(new DefaultMutableTreeNode(s), spectraNode, getChildCount(spectraNode)));
				
//				for (AverageMassSpectrum uSpec : dataFile.getAverageSpectra())
//					insertNodeInto(new DefaultMutableTreeNode(uSpec), spectraNode, getChildCount(spectraNode));								
			}
		}
		//	Add scans
		DefaultMutableTreeNode rawScansNode = new DefaultMutableTreeNode(scansNodeName);
		insertNodeInto(rawScansNode, newNode, getChildCount(newNode));
		try {
			addScans(dataFile, rawScansNode);
		} catch (FileParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addScans(DataFile dataFile, DefaultMutableTreeNode rawScansNode) throws FileParsingException {
		
		LCMSData data = RawDataManager.getRawData(dataFile);
		IScanCollection scans = data.getScans();
		scans.isAutoloadSpectra(true);
		scans.setDefaultStorageStrategy(StorageStrategy.SOFT);		
		Map<Integer, IScan> scanIndex = RawDataUtils.getCompleteScanMap(scans);
		scanIndex.forEach((scanNum, scan) -> {
			insertNodeInto(new DefaultMutableTreeNode(scan), rawScansNode, getChildCount(rawScansNode));
		});
	}
	
	private void addCromatogram(ExtractedChromatogram chrom){
		
		if(treeGrouping.equals(TreeGrouping.BY_DATA_FILE)) {
			
			DefaultMutableTreeNode fileNode = treeObjects.get(chrom.getDataFile());
			if(fileNode != null) {
				
				for(int i=0; i<fileNode.getChildCount(); i++) {
					
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)fileNode.getChildAt(i);
					if(node.getUserObject().equals(chromatogramNodeName)) {
						DefaultMutableTreeNode msNode = new DefaultMutableTreeNode(chrom);
						insertNodeInto(msNode, node, getChildCount(node));
						break;
					}
				}
			}
		}
//		if(treeGrouping.equals(TreeGrouping.BY_DATA_FILE)) {
//			
//			DefaultMutableTreeNode fileNode = treeObjects.get(chrom.getDataFile());
//			DefaultMutableTreeNode chromNode = null;
//			if(fileNode != null) {
//				
//				while (fileNode.children().hasMoreElements()) {
//					
//					DefaultMutableTreeNode node = (DefaultMutableTreeNode)fileNode.children().nextElement();
//					
//				   if(node.getUserObject().equals(chromatogramNodeName)) {
//					   chromNode = node;
//					   break;
//				   }
//				}
//				if(chromNode == null) {
//					chromNode = new DefaultMutableTreeNode(chromatogramNodeName);
//					insertNodeInto(chromNode, fileNode, getChildCount(fileNode));
//				}
//				DefaultMutableTreeNode xicNode = new DefaultMutableTreeNode(chrom);
//				insertNodeInto(xicNode, chromNode, getChildCount(chromNode));
//			}		
//		}
		if(treeGrouping.equals(TreeGrouping.BY_OBJECT_TYPE)) {
			
			DefaultMutableTreeNode xicNode = new DefaultMutableTreeNode(chrom);
			insertNodeInto(xicNode, chromatogramNode, getChildCount(chromatogramNode));
		}
	}
	
	private void addSpectrum(AverageMassSpectrum spectrum){
		
		if(treeGrouping.equals(TreeGrouping.BY_DATA_FILE)) {
		
			DefaultMutableTreeNode fileNode = treeObjects.get(spectrum.getDataFile());
			if(fileNode != null) {
				
				for(int i=0; i<fileNode.getChildCount(); i++) {
					
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)fileNode.getChildAt(i);
					if(node.getUserObject().equals(spectraNodeName)) {
						DefaultMutableTreeNode msNode = new DefaultMutableTreeNode(spectrum);
						insertNodeInto(msNode, node, getChildCount(node));
						break;
					}
				}
			}
		}
		if(treeGrouping.equals(TreeGrouping.BY_OBJECT_TYPE)) {
			
			DefaultMutableTreeNode msNode = new DefaultMutableTreeNode(spectrum);
			insertNodeInto(msNode, spectraNode, getChildCount(chromatogramNode));
		}
	}
	
	public void removeDataFiles(Collection<DataFile> selectedFiles) {

		for(DataFile f : selectedFiles) {
			
			for(ExtractedChromatogram chrom : f.getChromatograms())
				removeObject(chrom);
			
			for(AverageMassSpectrum spec : f.getAverageSpectra())
				removeObject(spec);
			
			removeObject(f);
		}
	}
	
	public void clear() {
		rootNode.removeAllChildren();
		dataFilesNode.removeAllChildren();
		chromatogramNode.removeAllChildren();
		spectraNode.removeAllChildren();
		treeObjects.clear();
		reload();
	}

	public TreeGrouping getTreeGrouping() {
		return treeGrouping;
	}

	public void setTreeGrouping(TreeGrouping treeGrouping) {
		this.treeGrouping = treeGrouping;
	}

	public static String getSpectranodename() {
		return spectraNodeName;
	}

	public DefaultMutableTreeNode getDataFilesNode() {
		return dataFilesNode;
	}

	public DefaultMutableTreeNode getChromatogramNode() {
		return chromatogramNode;
	}

	public DefaultMutableTreeNode getSpectraNode() {
		return spectraNode;
	}

	public DefaultMutableTreeNode getRootNode() {
		return rootNode;
	}
}
