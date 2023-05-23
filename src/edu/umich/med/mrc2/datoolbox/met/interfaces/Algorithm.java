package edu.umich.med.mrc2.datoolbox.met.interfaces;

/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

import java.util.Map;

import edu.umich.med.mrc2.datoolbox.met.molecule.Atom;

public interface Algorithm {


    /**
     * Test whether two met.molecule graphs are equivalent (with respect to their labels).
     */
    boolean areEquivalent();

    /**
     * If the two graphs are isomorphic, return the met.algorithm function that maps nodes from g1 to g2.
     * If the two graphs are non-isomorphic, return null.
     *
     * @return A mapping between nodes from g1 and g2, or null if g1 and g2 are non-isomorphic.
     */
    Map<Atom, Atom> getAtomMapping();

}
