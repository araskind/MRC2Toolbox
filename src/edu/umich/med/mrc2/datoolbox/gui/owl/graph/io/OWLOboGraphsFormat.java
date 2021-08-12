package edu.umich.med.mrc2.datoolbox.gui.owl.graph.io;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormatImpl;


public class OWLOboGraphsFormat extends OWLDocumentFormatImpl {


	public OWLOboGraphsFormat() {
	}

	@Nonnull
	@Override
	public String getKey() {
		return "Obo Graphs Format";
	}

	public boolean isPrefixOWLOntologyFormat() {
		return true;
	}

	public PrefixDocumentFormat asPrefixOWLOntologyFormat() {
		throw new UnsupportedOperationException(getClass().getName()
				+ " is not a PrefixDocumentFormat");
	}
}
