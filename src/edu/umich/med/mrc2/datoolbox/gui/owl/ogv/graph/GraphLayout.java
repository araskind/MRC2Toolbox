package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph;

import java.awt.Shape;

import org.semanticweb.owlapi.model.OWLObject;

import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.LinkDatabase.Link;

public interface GraphLayout {

	public void reset();
	
	public void addNode(OWLObject node);
	public void addEdge(Link link);
	
	public void setNodeDimensions(OWLObject node, int width, int height);
	
	public void doLayout();
	
	public Shape getEdgeShape(Link link);
	public Shape getNodeShape(OWLObject node);
	
	
}
