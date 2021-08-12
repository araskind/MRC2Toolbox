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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.owl.graph.OWLGraphWrapper;
import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.obo.GraphViewCanvas;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableCompoundClasyFireViewer  extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("cluster", 16);
	private OWLGraphWrapper graph;
	private GraphViewCanvas canvas;
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	
	public DockableCompoundClasyFireViewer() {

		super("DockableCompoundClasyFireViewer", componentIcon, "Compound classification", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		graph = MRC2ToolBoxCore.getClassyFireOntologyGraph();		
		ontology = graph.getSourceOntology();
		ElkReasonerFactory factory = new ElkReasonerFactory();
		reasoner = factory.createReasoner(ontology);	
		canvas = new GraphViewCanvas(graph, reasoner, null);
		add(new JScrollPane(canvas), BorderLayout.CENTER);
	}
	
	public void showCompoundData(String accession) {
		
		clearPanel();
		Collection<String>nodes = new TreeSet<String>();		
		try {
			nodes = CompoundDatabaseUtils.getClassyFireNodesForCompound(accession);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(nodes == null || nodes.isEmpty())
			return;
		
		Set<OWLObject> selectedObjects = new HashSet<OWLObject>();
		for(String s : nodes) {
			OWLObject owlObject = graph.getOWLObjectByIdentifier(s);
			if (owlObject != null) {
				selectedObjects.add(owlObject);
			}
		}
		try {
			canvas.setSelected(selectedObjects);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		canvas.panToObjects();
	}

	public void clearPanel() {
		
		canvas.setSelected(new HashSet<OWLObject>());
		try {
			canvas.reset();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
		}
	}
}
