package edu.umich.med.mrc2.datoolbox.met.algorithm;

import edu.umich.med.mrc2.datoolbox.met.molecule.Molecule;
import edu.umich.med.mrc2.datoolbox.met.molecule.MoleculeProperties;

/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

public class PreTest {

    boolean cannotBeEquivalent(Molecule g1, Molecule g2) {

        // extract the associated met.molecule properties
        MoleculeProperties prop_x = g1.getProperties();
        MoleculeProperties prop_y = g2.getProperties();

        // the molecules cannot be equivalent if their properties differ
        return !prop_x.equals(prop_y);
    }
}
