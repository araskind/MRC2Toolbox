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

package edu.umich.med.mrc2.datoolbox.gui.plot.qc.threed;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;

import org.jfree.chart3d.export.ExportUtils;
import org.jfree.chart3d.graphics3d.swing.Panel3D;

import edu.umich.med.mrc2.datoolbox.data.enums.ImageExportFormat;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.ArgumentChecker;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;


/**
 * An action that handles saving the content of a panel to a PNG image.
 * <br><br>
 * NOTE: This class is serializable, but the serialization format is subject
 * to change in future releases and should not be relied upon for persisting
 * instances of this class.
 */
@SuppressWarnings("serial")
public class ExportChartToFileAction extends AbstractAction {

    /** The panel to which this action applies. */
    private Panel3D panel;
    ImageExportFormat format;

    /**
     * Creates a new action instance.
     *
     * @param panel  the panel ({@code null} not permitted).
     */
    public ExportChartToFileAction(Panel3D panel, ImageExportFormat format) {

    	super(format.getName());
    	this.format = format;
        ArgumentChecker.nullNotPermitted(panel, "panel");
        this.panel = panel;
    }

    /**
     * Writes the content of the panel to a PNG image, using Java's ImageIO.
     *
     * @param e  the event.
     */
    @Override
	public void actionPerformed(ActionEvent e) {

//		JFileChooser fileChooser = new ImprovedFileChooser();
//		FileNameExtensionFilter filter = new FileNameExtensionFilter(format.getName(), format.getExtension());
//		fileChooser.addChoosableFileFilter(filter);
//		fileChooser.setFileFilter(filter);
//		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
//
//		File baseDirectory = 
//				new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
//		if (MRC2ToolBoxCore.getCurrentProject() != null) {
//			baseDirectory = MRC2ToolBoxCore.getCurrentProject().getExportsDirectory();
//
//			fileChooser.setCurrentDirectory(baseDirectory);
//			String fileName = "New image-" + MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + "." + format.getExtension();
//			fileChooser.setSelectedFile(new File(fileName));
//			int option = fileChooser.showSaveDialog(this.panel);
//
//			if (option == JFileChooser.APPROVE_OPTION) {
//
//				File imageFile = FIOUtils.changeExtension(fileChooser.getSelectedFile(), format.getExtension());
//
//				Dimension2D size = panel.getSize();
//				int w = (int) size.getWidth();
//				int h = (int) size.getHeight();
//
//				if(format.equals(ImageExportFormat.PNG)) {
//
//					BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//					Graphics2D g2 = image.createGraphics();
//					panel.getDrawable().draw(g2, new Rectangle(w, h));
//
//					try {
//						ImageIO.write(image, "png", imageFile);
//					} catch (IOException ex) {
//						throw new RuntimeException(ex);
//					}
//				}
//				if(format.equals(ImageExportFormat.PDF))
//					ExportUtils.writeAsPDF(panel.getDrawable(), w, h, imageFile);
//
//				if(format.equals(ImageExportFormat.SVG))
//					ExportUtils.writeAsSVG(panel.getDrawable(), w, h, imageFile);
//			}
//		}
		File baseDirectory = new File(
				MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()).getAbsoluteFile();
		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null)
			baseDirectory = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExportsDirectory();
		String defaultFileName = "New image-" + 
			MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + 
			"." + format.getExtension();
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter(format.getName(), format.getExtension());
		fc.setTitle("Export chart to image file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Export");
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(this.panel)) {
			
			File imageFile = FIOUtils.changeExtension(fc.getSelectedFile(), format.getExtension());
			Dimension2D size = panel.getSize();
			int w = (int) size.getWidth();
			int h = (int) size.getHeight();

			if(format.equals(ImageExportFormat.PNG)) {

				BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = image.createGraphics();
				panel.getDrawable().draw(g2, new Rectangle(w, h));

				try {
					ImageIO.write(image, "png", imageFile);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
			if(format.equals(ImageExportFormat.PDF))
				ExportUtils.writeAsPDF(panel.getDrawable(), w, h, imageFile);

			if(format.equals(ImageExportFormat.SVG))
				ExportUtils.writeAsSVG(panel.getDrawable(), w, h, imageFile);
		}
	}
}
