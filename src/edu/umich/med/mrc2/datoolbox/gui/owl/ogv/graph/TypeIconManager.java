package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph;

import org.semanticweb.owlapi.model.OWLObjectProperty;

import edu.umd.cs.piccolo.PNode;

public interface TypeIconManager {
	public PNode getIcon(OWLObjectProperty type);
}
