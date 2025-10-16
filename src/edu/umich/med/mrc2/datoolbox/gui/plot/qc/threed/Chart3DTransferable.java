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

package edu.umich.med.mrc2.datoolbox.gui.plot.qc.threed;

import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.jfree.chart3d.Chart3D;

/**
 * Based on JFreeChart
 * @author Sasha
 *
 */
public class Chart3DTransferable implements Transferable {

    final DataFlavor imageFlavor = new DataFlavor(
            "image/x-java-image; class=java.awt.Image", "Image");

    private Chart3D chart;
    private int width;
    private int height;

    public Chart3DTransferable(Chart3D chart, int width, int height) {

    	this.chart = chart;
    	this.width = width;
    	this.height = height;
    }

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {this.imageFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return this.imageFlavor.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {

        if (this.imageFlavor.equals(flavor)) {
            return createBufferedImage(this.chart, this.width, this.height);
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
	}

    /**
     * A utility method that creates an image of a chart, with scaling.
     *
     * @param panel  the Panel3D object.
     * @param w  the image width.
     * @param h  the image height.
     *
     * @return  A chart image.
     */
    private BufferedImage createBufferedImage(Chart3D chart, int w, int h) {

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0.0, 0.0, w, h));
        g2.dispose();
        return image;
    }
}



















