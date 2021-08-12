package edu.umich.med.mrc2.datoolbox.gui.owl.graph.io;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormatImpl;


public class OWLJsonLDFormat extends OWLDocumentFormatImpl {


	public OWLJsonLDFormat() {
	}

	@Nonnull
	@Override
	public String getKey() {
		return "OWL JSON-LD Format";
	}

	public boolean isPrefixOWLOntologyFormat() {
		return false;
	}

	public PrefixDocumentFormat asPrefixOWLOntologyFormat() {
		throw new UnsupportedOperationException(getClass().getName()
				+ " is not a PrefixDocumentFormat");
	}
}
