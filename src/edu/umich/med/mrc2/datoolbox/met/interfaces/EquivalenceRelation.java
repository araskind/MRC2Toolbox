package edu.umich.med.mrc2.datoolbox.met.interfaces;

/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

public interface EquivalenceRelation<T> {

    /**
     * Test whether two items are equivalent.
     *
     * @param x Item.
     * @param y Another item.
     * @return True, if and only if x is equivalent to y.
     */
    boolean equivalent(T x, T y);
}
