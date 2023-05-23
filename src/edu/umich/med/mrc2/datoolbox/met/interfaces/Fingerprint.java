package edu.umich.med.mrc2.datoolbox.met.interfaces;
/**
 * From MET: A Faster Java Package for Molecule Equivalence Testing
 * https://github.com/jaschueler/MET/
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * 
 * */

/**
 * Interface for fingerprint functions that map items of type T to integers.
 *
 * Two equivalent items need to have the same fingerprint.
 *
 */
public interface Fingerprint<T> {

    /**
     * Create the fingerprint of item x.
     * @param x
     * @return
     */
    int fingerprint(T x);

}
