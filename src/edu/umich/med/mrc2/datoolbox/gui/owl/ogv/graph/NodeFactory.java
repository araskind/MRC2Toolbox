package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph;

import java.awt.Shape;

import org.semanticweb.owlapi.model.OWLObject;

import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.LinkDatabase.Link;

public interface NodeFactory {
	
	public OELink createLink(Link link, Shape s);
	
	public OENode createNode(OWLObject obj, Shape s);
}
