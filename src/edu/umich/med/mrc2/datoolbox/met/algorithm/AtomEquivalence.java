package edu.umich.med.mrc2.datoolbox.met.algorithm;

/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

import edu.umich.med.mrc2.datoolbox.met.interfaces.EquivalenceRelation;
import edu.umich.med.mrc2.datoolbox.met.molecule.Atom;
import edu.umich.med.mrc2.datoolbox.met.molecule.AtomProperties;

/**
 * Two atoms are equivalent iff they have the same atom properties.
 */
public class AtomEquivalence implements EquivalenceRelation<Atom> {

    @Override
    public boolean equivalent(Atom x, Atom y) {

        AtomProperties xp = x.getProperties();
        AtomProperties yp = y.getProperties();

        return xp.equals(yp);
    }
}
