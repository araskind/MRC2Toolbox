package edu.umich.med.mrc2.datoolbox.met.algorithm;

/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

import edu.umich.med.mrc2.datoolbox.met.interfaces.Fingerprint;
import edu.umich.med.mrc2.datoolbox.met.molecule.Atom;

/**
 * Create a fingerprint from the properties of an atom.
 */
public class AtomFingerprint implements Fingerprint<Atom> {

    @Override
    public int fingerprint(Atom x) {
    //   System.out.println("AtomFing: " + x.getID() + " " + x.getProperties().toString());
        return x.getProperties().hashCode();
    }
}
