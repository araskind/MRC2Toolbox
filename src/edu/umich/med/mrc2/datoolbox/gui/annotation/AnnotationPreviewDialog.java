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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.rtf.jwp.JWordProcessor;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class AnnotationPreviewDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4061118540815590952L;
	private ObjectAnnotation currentAnnotation;
	private static final Icon previewIcon = GuiUtils.getIcon("previewDocument", 32);
	private IndeterminateProgressDialog idp;

	public AnnotationPreviewDialog(ObjectAnnotation currentAnnotation) {
		super();
		this.currentAnnotation = currentAnnotation;
		
		setTitle("Annotation contents");
		setIconImage(((ImageIcon) previewIcon).getImage());
		setPreferredSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		loadAnnotationData();
		
		pack();
	}

	private void loadAnnotationData() {
		// RTF document
		if(currentAnnotation.getRtfDocument() != null) {
			
			JWordProcessor wordProcessor = new JWordProcessor(true);
			wordProcessor.loadDocument(currentAnnotation.getRtfDocument());
			getContentPane().add(new JScrollPane(wordProcessor), BorderLayout.CENTER);
		}
		else {
			if(currentAnnotation.getLinkedDocumentId() != null) {
				
				DocumentPreviewLoadTask task = new DocumentPreviewLoadTask();
				idp = new IndeterminateProgressDialog("Loading document preview ...", MRC2ToolBoxCore.getMainWindow(), task);
				idp.setLocationRelativeTo(this.getContentPane());
				idp.setVisible(true);
			}
		}
	}
	
	private void loadPdf(SwingController controller) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT DOCUMENT_CONTENTS FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, currentAnnotation.getLinkedDocumentId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

//		   Blob blob = rs.getBlob("DOCUMENT_CONTENTS");
		   BufferedInputStream is = new BufferedInputStream(rs.getBinaryStream("DOCUMENT_CONTENTS"));
		   controller.openDocument(is, currentAnnotation.getLinkedDocumentName(), 
	        		currentAnnotation.getLinkedDocumentName() + ".pdf");

		   is.close();
//		   blob.free();
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private void loadPowerPoitAsPdf(SwingController controller) throws Exception {
		
		AffineTransform at = new AffineTransform();		
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		int zoom = 2;
		at.setToScale(zoom, zoom);
		PDDocument doc = new PDDocument();
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT DOCUMENT_CONTENTS FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, currentAnnotation.getLinkedDocumentId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

//			Blob blob = rs.getBlob("DOCUMENT_CONTENTS");
			BufferedInputStream is = new BufferedInputStream(rs.getBinaryStream("DOCUMENT_CONTENTS"));
			XMLSlideShow ppt = new XMLSlideShow(is);
			is.close();
			
			Dimension pgsize = ppt.getPageSize();
			List<XSLFSlide> slides = ppt.getSlides();		
			int pageWidth = pgsize.width * zoom;
			int pageHeight = pgsize.height * zoom; 

			for (int i = 0; i < slides.size(); i++) {

				BufferedImage img = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = img.createGraphics();
				graphics.setTransform(at);
				graphics.setRenderingHints(rh);

				// clear the drawing area
				graphics.setPaint(Color.white);
				graphics.fill(new Rectangle2D.Float(0, 0, pageWidth, pageHeight));

				// render
				try {
					slides.get(i).draw(graphics);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				PDRectangle mediaBox = new PDRectangle(pageWidth, pageHeight);
	            PDPage page = new PDPage(mediaBox);
	            doc.addPage(page);            
	            PDPageContentStream contents = new PDPageContentStream(doc, page);
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            ImageIO.write( img, "png", baos );
	            baos.flush();
	            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, baos.toByteArray(), "Page " + i);
	            baos.close();
	            contents.drawImage(pdImage, 0, 0);
	            contents.close();
			}
//			blob.free();			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			doc.save(out);
			doc.close();
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			controller.openDocument(in, currentAnnotation.getLinkedDocumentName(), 
	        		currentAnnotation.getLinkedDocumentName() + ".pdf");
			out.close();
			in.close();
			break;
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	class DocumentPreviewLoadTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		public DocumentPreviewLoadTask() {
			super();
		}

		@Override
		public Void doInBackground() {

			if(currentAnnotation.getLinkedDocumentFormat().equals(DocumentFormat.PDF)) {
				
				SwingController controller = new SwingController();
		        SwingViewBuilder factory = new SwingViewBuilder(controller);
		        JPanel viewerComponentPanel = factory.buildViewerPanel();

		        // add interactive mouse link annotation support via callback
		        controller.getDocumentViewController().setAnnotationCallback(
		                new org.icepdf.ri.common.MyAnnotationCallback(
		                        controller.getDocumentViewController()));

		        getContentPane().add(viewerComponentPanel);

		        // Now that the GUI is all in place, we can try openning a PDF
		        try {
					loadPdf(controller);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//	Convert PPTX to PDF and load
			if(currentAnnotation.getLinkedDocumentFormat().equals(DocumentFormat.PPTX)) {
				
				SwingController controller = new SwingController();
		        SwingViewBuilder factory = new SwingViewBuilder(controller);
		        JPanel viewerComponentPanel = factory.buildViewerPanel();

		        // add interactive mouse link annotation support via callback
		        controller.getDocumentViewController().setAnnotationCallback(
		                new org.icepdf.ri.common.MyAnnotationCallback(
		                        controller.getDocumentViewController()));

		        getContentPane().add(viewerComponentPanel);

		        // Now that the GUI is all in place, we can try openning a PDF
		        try {
		        	loadPowerPoitAsPdf(controller);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}




