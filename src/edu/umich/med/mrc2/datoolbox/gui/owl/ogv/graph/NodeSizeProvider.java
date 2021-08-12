package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph;

import java.awt.Dimension;

import org.semanticweb.owlapi.model.OWLObject;

public interface NodeSizeProvider {
	public Dimension getSize(OWLObject lo);
}
