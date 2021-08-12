package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.focus;

import org.semanticweb.owlapi.model.OWLObject;

public interface FocusedNodeListener {
	
	public void focusedChanged(OWLObject oldFocus, OWLObject newFocus);

}
