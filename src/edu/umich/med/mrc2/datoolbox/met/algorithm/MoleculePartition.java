package edu.umich.med.mrc2.datoolbox.met.algorithm;

/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

import edu.umich.med.mrc2.datoolbox.met.helper.Partition;
import edu.umich.med.mrc2.datoolbox.met.interfaces.Algorithm;
import edu.umich.med.mrc2.datoolbox.met.molecule.Molecule;

public class MoleculePartition extends Partition<Molecule> {

    public MoleculePartition() {

        super((x, y) -> {
            Algorithm alg = new METDefault(x, y);
            return alg.areEquivalent();
        }, new MoleculeFingerprint());
    }
}
