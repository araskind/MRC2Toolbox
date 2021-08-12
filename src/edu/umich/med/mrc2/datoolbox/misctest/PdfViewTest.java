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

package edu.umich.med.mrc2.datoolbox.misctest;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

public class PdfViewTest {

	private static String filePath;

	public PdfViewTest(String filePath) {
		super();
		this.filePath = filePath;

		// build a controller
		SwingController controller = new SwingController();

		// Build a SwingViewFactory configured with the controller
		SwingViewBuilder factory = new SwingViewBuilder(controller);

		// Use the factory to build a JPanel that is pre-configured
		//with a complete, active Viewer UI.
		JPanel viewerComponentPanel = factory.buildViewerPanel();

		// add copy keyboard command
		ComponentKeyBinding.install(controller, viewerComponentPanel);

		// add interactive mouse link annotation support via callback
		controller.getDocumentViewController().setAnnotationCallback(
		      new org.icepdf.ri.common.MyAnnotationCallback(
		             controller.getDocumentViewController()));

		// Create a JFrame to display the panel in
		JFrame window = new JFrame("Using the Viewer Component");
		window.getContentPane().add(viewerComponentPanel);
		window.pack();
		window.setVisible(true);

		// Open a PDF document to view
		controller.openDocument(filePath);
	}

	public static void main(String[] args) {

		String filePath = "C:\\Users\\Sasha\\Downloads\\Pavlic2006_Article_CombinedUseOfESIQqTOF-MSAndESI.pdf";
		PdfViewTest ptest = new PdfViewTest(filePath);
	}
}
