package edu.umich.med.mrc2.datoolbox.met;

import java.util.HashMap;
import java.util.Map;

/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.met.algorithm.METDefault;
import edu.umich.med.mrc2.datoolbox.met.interfaces.Algorithm;
import edu.umich.med.mrc2.datoolbox.met.molecule.Atom;
import edu.umich.med.mrc2.datoolbox.met.molecule.Molecule;

/**
 * Main class for testing equivalence of CDK molecules.
 */
public class MoleculeEquivalence {

    // the result of the equivalence test
    private boolean equivalent = false;     // whether the two molecules are equivalent (in 2D)
    private Map<IAtom, IAtom> mapping;      // a mapping from mol1 to mol2 (isomorphism function)

    /**
     * Test whether two CDK molecules are equivalent.
     *
     * @param mol1 CDK container.
     * @param mol2 CDK container.
     */
    public MoleculeEquivalence(IAtomContainer mol1, IAtomContainer mol2) {

        // transform CDK containers into molecule graphs
        Molecule m1 = new Molecule(mol1);
        Molecule m2 = new Molecule(mol2);

        // run an equivalence algorithm
        Algorithm alg = new METDefault(m1, m2);

        // evaluate results
        equivalent = alg.areEquivalent();

        if (equivalent) {

            // create atom mapping
            mapping = new HashMap<>();
            for (Map.Entry<Atom, Atom> kv : alg.getAtomMapping().entrySet()) {
                Atom k = kv.getKey();
                Atom v = kv.getValue();
                mapping.put(k.getIAtom(), v.getIAtom());
            }
        }
    }

    /**
     * Return the result of the equivalence test.
     *
     * @return True, if and only if both molecules are equivalent.
     */
    public boolean areEquivalent() {
        return equivalent;
    }


    /**
     * Return a mapping between the atoms from mol1 to those of mol2, or null if the molecules are not equivalent.
     *
     * @return Mapping of iAtoms from mol1 to mol2
     */
    public Map<IAtom, IAtom> getAtomMapping() {
        return mapping;
    }

}
