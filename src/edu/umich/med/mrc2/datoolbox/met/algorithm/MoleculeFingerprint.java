package edu.umich.med.mrc2.datoolbox.met.algorithm;

import edu.umich.med.mrc2.datoolbox.met.interfaces.Fingerprint;
import edu.umich.med.mrc2.datoolbox.met.molecule.Molecule;

/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

/**
 * Create fingerprints for met.molecule graphs.
 */
public class MoleculeFingerprint implements Fingerprint<Molecule> {

    @Override
    public int fingerprint(Molecule x) {
        //System.out.println("MolFing: "+x.getProperties().hashCode());
        return x.getProperties().hashCode();
    }
}
