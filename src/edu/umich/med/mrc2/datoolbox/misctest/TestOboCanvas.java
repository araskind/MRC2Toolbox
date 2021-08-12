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

//import org.geneontology.obo.GraphViewCanvas;
//import org.semanticweb.elk.owlapi.ElkReasonerFactory;
//import org.semanticweb.owlapi.model.OWLObject;
//import org.semanticweb.owlapi.model.OWLOntology;
//import org.semanticweb.owlapi.reasoner.OWLReasoner;
//
//import owltools.graph.OWLGraphWrapper;
//import owltools.io.ParserWrapper;


public class TestOboCanvas {

	public static void main(String[] args) throws Exception {

		//		loadAndShow("src/test/resources/simple.obo", "CARO:0007", "CARO:0003");
		//		loadAndShow("src/test/resources/go/gene_ontology_write.obo", "GO:0007406");
		//	loadAndShow("src/test/resources/caro.obo");
		loadAndShow("resources/edu/umich/med/mrc2/datoolbox/obo/ChemOnt_2_1.obo", 
				"CHEMONTID:0004722",
				"CHEMONTID:0001666",
				"CHEMONTID:0004139",
				"CHEMONTID:0004557",
				"CHEMONTID:0000278",
				"CHEMONTID:0004150",
				"CHEMONTID:0003094");
		
	}
	
	private static void loadAndShow(String resource, String...selected) throws Exception {
//		ParserWrapper pw = new ParserWrapper();
//		OWLGraphWrapper graph = pw.parseToOWLGraph(resource);
//
//		OWLOntology ontology = graph.getSourceOntology();
//
//		// create reasoner
//		OWLReasoner reasoner = null;
//		ElkReasonerFactory factory = new ElkReasonerFactory();
//		reasoner = factory.createReasoner(ontology);
//
//		Set<OWLObject> selectedObjects = null;
//		if (selected != null && selected.length > 0) {
//			selectedObjects = new HashSet<OWLObject>();
//			for(String s : selected) {
//				OWLObject owlObject = graph.getOWLObjectByIdentifier(s);
//				if (owlObject != null) {
//					selectedObjects.add(owlObject);
//				}
//			}
//		}
//		// create frame and exit behavior
//		final GraphViewCanvas canvas = new GraphViewCanvas(graph, reasoner, selectedObjects);
//
//		JFrame frame = new JFrame();
//		frame.setSize(1000, 800);
//		
//		// add a reset button
//		JPanel panel = new JPanel(new BorderLayout());
//		JButton reset = new JButton("Reset");
//		reset.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				canvas.reset();
//
//			}
//		});
//		panel.add(canvas, BorderLayout.CENTER);
//		panel.add(reset, BorderLayout.SOUTH);
//		frame.add(panel);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//
//		frame.addComponentListener(new ComponentListener() {
//
//			@Override
//			public void componentShown(ComponentEvent e) {
//				canvas.panToObjects();
//			}
//
//			@Override
//			public void componentResized(ComponentEvent e) {
//				// empty
//			}
//
//			@Override
//			public void componentMoved(ComponentEvent e) {
//				// empty
//			}
//
//			@Override
//			public void componentHidden(ComponentEvent e) {
//				// empty
//			}
//		});
//
//		// show
//		frame.setVisible(true);
	}
}
