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

package edu.umich.med.mrc2.datoolbox.gui.clustertree.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;

public class TransferableFeatureCluster implements Transferable {

	public static DataFlavor MS_FEATURE_CLUSTER_FLAVOR = new DataFlavor(MsFeatureCluster.class, "Feature cluster");

	DataFlavor flavors[] = { MS_FEATURE_CLUSTER_FLAVOR };
	MsFeatureCluster cluster;

	public TransferableFeatureCluster(MsFeatureCluster msc) {
		cluster = msc;
	}

	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(flavor)) {
			return (Object) cluster;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	public synchronized DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.getRepresentationClass() == MsFeatureCluster.class);
	}
}
