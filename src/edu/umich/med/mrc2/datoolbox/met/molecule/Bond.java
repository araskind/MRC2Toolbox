package edu.umich.med.mrc2.datoolbox.met.molecule;

/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

/**
 * Model a bond between two atoms.
 */
public class Bond {

    private Atom v;
    private Atom w;

    /**
     * Create a bond between two atoms.
     * @param v
     * @param w
     */
    public Bond(Atom v, Atom w) {
        this.v = v;
        this.w = w;
    }

    /**
     * Return the first atom.
     * @return
     */
    public Atom getOne() {
        return v;
    }

    /**
     * Return the second atom.
     * @return
     */
    public Atom getOther() {
        return w;
    }
}
