package edu.umich.med.mrc2.datoolbox.gui.owl.graph.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.umich.med.mrc2.datoolbox.gui.owl.graph.OWLGraphWrapper;

/**
 * reads in a table (e.g. tab-delimited table) converting each row to an OWL Axiom.
 * 
 * currently only reads first two columns (sub and ob)
 * 
 * @author cjm
 *
 */
public class GMTParser {

	private static Logger LOG = Logger.getLogger(GMTParser.class);
	public String prefix = "http://x.org#";
	OWLGraphWrapper graph;

	public GMTParser(OWLGraphWrapper graph) {
		super();
		this.graph = graph;
	}

	public void parse(String fn) throws IOException {
		File myFile = new File(fn);
		FileReader fileReader = new FileReader(myFile);
		BufferedReader reader = new BufferedReader(fileReader);
		String line;
		while ((line = reader.readLine()) != null) {
			String[] row = line.split("\t");
			parseRow(row);
		}
	}

	private void parseRow(String[] row) {
		OWLDataFactory df = graph.getDataFactory();
		OWLOntologyManager mgr = graph.getManager();
		String geneSetId = row[0];
		IRI geneSetIRI = IRI.create(prefix + geneSetId);
		String desc = row[1];
		OWLClass geneSetCls = df.getOWLClass(geneSetIRI);
		OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(df.getRDFSLabel(),geneSetIRI, literal(desc));
		mgr.addAxiom(graph.getSourceOntology(), ax);
				
		// assume each value is an entity, e.g. gene
		for (int i=2; i < row.length; i++) {
			OWLNamedIndividual individual = df.getOWLNamedIndividual(IRI.create(prefix + row[i]));
			mgr.addAxiom(graph.getSourceOntology(), df.getOWLClassAssertionAxiom(geneSetCls, individual));
		}
	}

	private OWLAnnotationValue literal(String obj) {
		return graph.getDataFactory().getOWLLiteral(obj);
	}

}
