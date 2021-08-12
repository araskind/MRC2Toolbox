package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph;

import org.semanticweb.owlapi.model.OWLObject;

import edu.umich.med.mrc2.datoolbox.gui.owl.graph.OWLGraphWrapper;

public class DefaultNodeLabelProvider implements NodeLabelProvider {
	
	private final OWLGraphWrapper graph;

	public DefaultNodeLabelProvider(OWLGraphWrapper graph) {
		this.graph = graph;
	}

	@Override
	public String getLabel(OWLObject lo) {
		return graph.getLabelOrDisplayId(lo);
	}

}
