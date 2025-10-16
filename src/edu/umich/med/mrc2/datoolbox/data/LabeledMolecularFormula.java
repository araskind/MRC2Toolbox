/*******************************************************************************
 * 
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *  
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

public class LabeledMolecularFormula implements IMolecularFormula, Serializable {
	
	/**
     * Determines if a de-serialized object is compatible with this class.
     *
     * This value must only be changed if and only if the new version
     * of this class is imcompatible with the old version. See Sun docs
     * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html>details</a>.
     */
    private static final long serialVersionUID = -2011407700837295287L;

    private Map<IIsotope, Integer> isotopes;
    /**
     *  The partial charge of the molecularFormula. The default value is Double.NaN.
     */
    private Integer charge = (Integer) CDKConstants.UNSET;

    /**
     *  A hashtable for the storage of any kind of properties of this IChemObject.
     */
    private Map<Object, Object>    properties;

    /**
     *  Constructs an empty MolecularFormula.
     */
	public LabeledMolecularFormula(IMolecularFormula formula) {
		super();
		isotopes = new HashMap<IIsotope, Integer>();
		for (IIsotope iso : formula.isotopes())
			addIsotope(iso, formula.getIsotopeCount(iso));
		
		setCharge(formula.getCharge());
	}
	
	public LabeledMolecularFormula(IAtomContainer container) {
		super();
		isotopes = new HashMap<IIsotope, Integer>();
		for (IAtom a : container.atoms())
			addIsotope(a);
	}

    /**
     * Adds an molecularFormula to this MolecularFormula.
     *
     * @param  formula  The molecularFormula to be added to this chemObject
     * @return          The IMolecularFormula
     */
    @Override
    public IMolecularFormula add(IMolecularFormula formula) {
        for (IIsotope newIsotope : formula.isotopes()) {
            addIsotope(newIsotope, formula.getIsotopeCount(newIsotope));
        }
        if (formula.getCharge() != null)
            if (charge != null)
                charge += formula.getCharge();
            else
                charge = formula.getCharge();
        return this;
    }

    /**
     *  Adds an Isotope to this MolecularFormula one time.
     *
     * @param  isotope  The isotope to be added to this MolecularFormula
     * @see             #addIsotope(IIsotope, int)
     */
    @Override
    public IMolecularFormula addIsotope(IIsotope isotope) {
        return this.addIsotope(isotope, 1);
    }

    /**
     *  Adds an Isotope to this MolecularFormula in a number of occurrences.
     *
     * @param  isotope  The isotope to be added to this MolecularFormula
     * @param  count    The number of occurrences to add
     * @see             #addIsotope(IIsotope)
     */
    @Override
    public IMolecularFormula addIsotope(IIsotope isotope, int count) {
        boolean flag = false;
        for (IIsotope thisIsotope : isotopes()) {
            if (isTheSame(thisIsotope, isotope)) {
                isotopes.put(thisIsotope, isotopes.get(thisIsotope) + count);
                flag = true;
                break;
            }
        }
        if (!flag) {
            isotopes.put(isotope, count);
        }

        return this;
    }

    /**
     *  True, if the MolecularFormula contains the given IIsotope object and not
     *  the instance. The method looks for other isotopes which has the same
     *  symbol, natural abundance and exact mass.
     *
     * @param  isotope  The IIsotope this MolecularFormula is searched for
     * @return          True, if the MolecularFormula contains the given isotope object
     */
    @Override
    public boolean contains(IIsotope isotope) {
        for (IIsotope thisIsotope : isotopes()) {
            if (isTheSame(thisIsotope, isotope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCharge() {
        return charge;
    }

    /**
     *  Checks a set of Nodes for the occurrence of the isotope in the
     *  IMolecularFormula from a particular isotope. It returns 0 if the does not exist.
     *
     * @param   isotope          The IIsotope to look for
     * @return                   The occurrence of this isotope in this IMolecularFormula
     * @see                      #getIsotopeCount()
     */
    @Override
    public int getIsotopeCount(IIsotope isotope) {
        return !contains(isotope) ? 0 : isotopes.get(getIsotope(isotope));
    }

    /**
     *  Checks a set of Nodes for the number of different isotopes in the
     *  IMolecularFormula.
     *
     * @return        The the number of different isotopes in this IMolecularFormula
     * @see           #getIsotopeCount(IIsotope)
     */
    @Override
    public int getIsotopeCount() {
        return isotopes.size();
    }

    /**
     *  Get the isotope instance given an IIsotope. The instance is those
     *  that has the isotope with the same symbol, natural abundance and
     *  exact mass.
     *
     * @param  isotope The IIsotope for looking for
     * @return         The IIsotope instance
    * @see            #isotopes
     */
    private IIsotope getIsotope(IIsotope isotope) {
        for (IIsotope thisIsotope : isotopes()) {
            if (isTheSame(isotope, thisIsotope)) return thisIsotope;
        }
        return null;
    }
    
    private IIsotope getIsotopeElement(IIsotope isotope) {
        for (IIsotope thisIsotope : isotopes()) {
            if (isTheSameElement(isotope, thisIsotope)) return thisIsotope;
        }
        return null;
    }
    
    private IIsotope getOtherIsotopeForElement(IIsotope isotope) {
    	
        for (IIsotope thisIsotope : isotopes()) {
        	
        	if(thisIsotope.getSymbol().equals(isotope.getSymbol())) {
        		if(thisIsotope.getMassNumber() == null && isotope.getMassNumber() != null)
        			return thisIsotope;
        	}
        }
        for (IIsotope thisIsotope : isotopes()) {
        	
        	if(thisIsotope.getSymbol().equals(isotope.getSymbol())) {
        		if(thisIsotope.getMassNumber() != null 
        				&& isotope.getMassNumber() != null
        				&& thisIsotope.getMassNumber() != isotope.getMassNumber()) {
        			return thisIsotope;
        		}     			
        	}
        }
        return null;
    }  

    /**
     *  Returns an Iterator for looping over all isotopes in this IMolecularFormula.
     *
     * @return    An Iterator with the isotopes in this IMolecularFormula
     */
    @Override
    public Iterable<IIsotope> isotopes() {
        return isotopes.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCharge(Integer charge) {
        this.charge = charge;
    }

    /**
     * Removes all isotopes of this molecular formula.
     */
    @Override
    public void removeAllIsotopes() {
        isotopes.clear();
    }

    /**
     *  Removes the given isotope from the MolecularFormula.
     *
     * @param isotope  The IIsotope to be removed
     */
    @Override
    public void removeIsotope(IIsotope isotope) {
    	
    	IIsotope currentIso = getIsotope(isotope);
    	if(currentIso != null)   	
    		isotopes.remove(currentIso);
    }
    
	public void removeIsotope(IIsotope isotope, int countToReemove) throws Exception {

		IIsotope currentIso = getIsotope(isotope);
		if (currentIso != null) {

			int currentCount = getIsotopeCount(currentIso);
			if (countToReemove > currentCount) {
				throw new IllegalArgumentException(
						"Can't remove more atoms of the element than present in the molecule!");
			}
			if (countToReemove == currentCount) {
				isotopes.remove(currentIso);
			}
			if (countToReemove < currentCount) {
				isotopes.put(currentIso, currentCount - countToReemove);
			}
			// TODO - deal with partially labeled when removed more than unlabeled
		} else {
			// TODO make sure
			IIsotope element = getOtherIsotopeForElement(isotope);
			if (element == null) {
				throw new IllegalArgumentException(
						"Can't remove \"" + isotope.getSymbol() + "\" atoms, none are present in the molecule!");
			}
			int elementCount = getIsotopeCount(element);
			if (countToReemove > elementCount) {
				throw new IllegalArgumentException(
						"Can't remove more \"" + element.getSymbol() + "\" atoms than present in the molecule!");
			}
			if (countToReemove == elementCount) {
				isotopes.remove(element);
			}
			if (countToReemove < elementCount) {
				isotopes.put(element, elementCount - countToReemove);
			}
		}
	}
   
    public void removeIsotopeOld(IIsotope isotope, int countToReemove) throws Exception {
    	
    	IIsotope currentIso = getIsotope(isotope);
    	if(currentIso != null) { 
    		
        	int currentCount = getIsotopeCount(currentIso);
        	if(countToReemove > currentCount) {
        		throw new IllegalArgumentException(
        				"Can't remove more atoms of the element than present in the molecule!");
        	}
        	if(countToReemove == currentCount) {
        		isotopes.remove(currentIso);
        	}
        	if(countToReemove < currentCount) {       		
        		isotopes.put(currentIso, currentCount - countToReemove);
        	} 
        	//	TODO - deal with partially labeled when removed more than unlabeled
    	}
    	else {	//	Fully labeled
    		// Deuterated
    		if(isotope.getSymbol().equals("H") && isotope.getMassNumber() == null) {   			
    			IIsotope hydrogen =  getIsotopeElement(isotope);
    			if(hydrogen == null) {
            		throw new IllegalArgumentException(
            				"Molecule doesn't contain hydrogen atoms!");
    			}
            	int hydrogenCount = getIsotopeCount(hydrogen);
            	if(countToReemove > hydrogenCount) {
            		throw new IllegalArgumentException(
            				"Can't remove more hydrogenCount atoms than present in the molecule!");
            	}
            	if(countToReemove == hydrogenCount) {
            		isotopes.remove(hydrogen);
            	}
            	if(countToReemove < hydrogenCount) {       		
            		isotopes.put(hydrogen, hydrogenCount - countToReemove);
            	} 
    		}
    		else {
        		throw new IllegalArgumentException(
        				"This function can only handle the loss of labeled hydrogen atoms!");
    		}
    	}
     	//	C13
    	
    	//	N15   	
    }

    /**
     * Clones this MolecularFormula object and its content. I should
     * integrate into ChemObject.
     *
     * @return    The cloned object
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

    	LabeledMolecularFormula clone = new LabeledMolecularFormula(this);
        return clone;
    }

    /**
     * Lazy creation of properties hash. I should
     * integrate into ChemObject.
     *
     * @return    Returns in instance of the properties
     */
    private Map<Object, Object> lazyProperties() {
        if (properties == null) {
            properties = new Hashtable<Object, Object>();
        }
        return properties;
    }

    /**
     *  Sets a property for a IChemObject. I should
     * integrate into ChemObject.
     *
     *@param  description  An object description of the property (most likely a
     *      unique string)
     *@param  property     An object with the property itself
     *@see                 #getProperty
     *@see                 #removeProperty
     */
    @Override
    public void setProperty(Object description, Object property) {
        lazyProperties().put(description, property);
    }

    /**
     *  Removes a property for a IChemObject. I should
     * integrate into ChemObject.
     *
     *@param  description  The object description of the property (most likely a
     *      unique string)
     *@see                 #setProperty
     *@see                 #getProperty
     */
    @Override
    public void removeProperty(Object description) {
        if (properties == null) {
            return;
        }
        lazyProperties().remove(description);
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public <T> T getProperty(Object description) {
        if (properties == null) return null;
        // can't check the type
        @SuppressWarnings("unchecked")
        T value = (T) lazyProperties().get(description);
        return value;
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public <T> T getProperty(Object description, Class<T> c) {
        Object value = lazyProperties().get(description);

        if (c.isInstance(value)) {

            @SuppressWarnings("unchecked")
            T typed = (T) value;
            return typed;

        } else if (value != null) {
            throw new IllegalArgumentException("attempted to access a property of incorrect type, expected "
                    + c.getSimpleName() + " got " + value.getClass().getSimpleName());
        }

        return null;
    }

    /**
     *  Returns a Map with the IChemObject's properties.I should
     * integrate into ChemObject.
     *
     *@return    The object's properties as an Hashtable
     *@see       #setProperties
     */
    @Override
    public Map<Object, Object> getProperties() {
        return lazyProperties();
    }

    /**
     *  Sets the properties of this object.
     *
     *@param  properties  a Hashtable specifying the property values
     *@see                #getProperties
     */
    @Override
    public void setProperties(Map<Object, Object> properties) {

        Iterator<Object> keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            lazyProperties().put(key, properties.get(key));
        }
    }

    /**
     * Compare to IIsotope. The method doesn't compare instance but if they
     * have the same symbol, natural abundance and exact mass.
     *
     * @param isotopeOne   The first Isotope to compare
     * @param isotopeTwo   The second Isotope to compare
     * @return             True, if both isotope are the same
     */
    protected boolean isTheSame(IIsotope isotopeOne, IIsotope isotopeTwo) {

        Double natAbund1 = isotopeOne.getNaturalAbundance();
        Double natAbund2 = isotopeTwo.getNaturalAbundance();
        Double exactMass1 = isotopeOne.getExactMass();
        Double exactMass2 = isotopeTwo.getExactMass();      
        Integer massNum1 = isotopeOne.getMassNumber();
        Integer massNum2 = isotopeTwo.getMassNumber();

        if (natAbund1 == null) natAbund1 = -1.0;
        if (natAbund2 == null) natAbund2 = -1.0;
        if (exactMass1 == null) exactMass1 = -1.0;
        if (exactMass2 == null) exactMass2 = -1.0;
        if (massNum1 == null) massNum1 = -1;
        if (massNum2 == null) massNum2 = -1;

        if (!isotopeOne.getSymbol().equals(isotopeTwo.getSymbol())) return false;
        if (natAbund1.doubleValue() != natAbund2) return false;
        if (massNum1.intValue() != massNum2.intValue()) return false;
        return exactMass1.doubleValue() == exactMass2;
    }
    
    protected boolean isTheSameElement(IIsotope isotopeOne, IIsotope isotopeTwo) {

        Double natAbund1 = isotopeOne.getNaturalAbundance();
        Double natAbund2 = isotopeTwo.getNaturalAbundance();

        Double exactMass1 = isotopeOne.getExactMass();
        Double exactMass2 = isotopeTwo.getExactMass();

        if (natAbund1 == null) natAbund1 = -1.0;
        if (natAbund2 == null) natAbund2 = -1.0;
        if (exactMass1 == null) exactMass1 = -1.0;
        if (exactMass2 == null) exactMass2 = -1.0;

        if (!isotopeOne.getSymbol().equals(isotopeTwo.getSymbol())) return false;
        if (natAbund1.doubleValue() != natAbund2) return false;
        return exactMass1.doubleValue() == exactMass2;
    }
    
    @Override
    public IChemObjectBuilder getBuilder() {
        return SilentChemObjectBuilder.getInstance();
    }  
}
