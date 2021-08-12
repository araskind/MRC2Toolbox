package edu.umich.med.mrc2.datoolbox.gui.owl.graph.io;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.parameters.Imports;

import edu.umich.med.mrc2.datoolbox.gui.owl.graph.OWLGraphEdge;
import edu.umich.med.mrc2.datoolbox.gui.owl.graph.OWLGraphWrapper;
import edu.umich.med.mrc2.datoolbox.gui.owl.graph.OWLQuantifiedProperty;

/**
 * renders all edges in a simple TSV format
 * 
 * @author cjm
 *
 */
public class EdgeTableRenderer extends AbstractRenderer implements GraphRenderer {

	public EdgeTableRenderer(PrintStream stream) {
		super(stream);
	}

	public EdgeTableRenderer(String file) {
		super(file);
	}
	

	public void render(OWLGraphWrapper g) {
		graph = g;
		
		Set<OWLObject> objs = new HashSet<OWLObject>(g.getSourceOntology().getClassesInSignature(Imports.EXCLUDED));
		objs.addAll(g.getSourceOntology().getIndividualsInSignature(Imports.EXCLUDED));

		for (OWLObject obj : objs) {
			if (obj.equals(g.getDataFactory().getOWLNothing()))
				continue;
			if (obj.equals(g.getDataFactory().getOWLThing()))
				continue;
			if (obj instanceof OWLNamedObject)
				render((OWLNamedObject)obj);
		}
		stream.close();
	}
	

	// TODO - make this configurable
	private void render(OWLNamedObject obj) {
		String id = graph.getIdentifier(obj);
		for (OWLGraphEdge e : graph.getOutgoingEdges(obj)) {
			OWLQuantifiedProperty qp = e.getSingleQuantifiedProperty();
			String r;
			if (qp.getProperty() != null) {
				r = graph.getIdentifier(qp.getProperty());
			}
			else if (qp.isSubClassOf()) {
				r = "is_a";
			}
			else if (qp.isInstanceOf()) {
				r = "a";
			}
			else {
				continue;
			}
			print(id);
			sep();
			print(r);
			sep();
			print(graph.getIdentifier(e.getTarget()));
			nl();
			
		}
	}

	

}

