/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tautomers.InChITautomerGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import edu.umich.med.mrc2.datoolbox.met.MoleculeEquivalence;
import io.github.dan2097.jnainchi.InchiStatus;

public class CompoundStructureUtils {

	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(builder);
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Canonical);
	private static final InChITautomerGenerator tautgen = new InChITautomerGenerator(InChITautomerGenerator.KETO_ENOL);
	private static final MDLV2000Reader mdlReader = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	private static Aromaticity aromaticity;
	
	public static boolean doSmilesMatchInchi(String smiles, String inchi) {
		
		//	From SMILES
		IAtomContainer smilesAtomContainer = null;
		try {
			smilesAtomContainer = smipar.parseSmiles(smiles);
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(smilesAtomContainer == null) {				
			System.out.println("\nFailed to parse SMILES " + smiles);
			return false;
		}			
		finalizeHydrogens(smilesAtomContainer);
		
		//	From InChi
		IAtomContainer inchiAtomContainer = null;
		try {
			inchiAtomContainer = convertInchi2Mol(inchi);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(inchiAtomContainer == null) {
			System.out.println("\nFailed to parse InChi " + inchi);
			return false;
		}
		finalizeHydrogens(inchiAtomContainer);
		
        MoleculeEquivalence eq = 
        		new MoleculeEquivalence(smilesAtomContainer, inchiAtomContainer);	        
		boolean isEquivalent = eq.areEquivalent();
		if(isEquivalent)
			return true;
			
		List<IAtomContainer>tautomers = new ArrayList<IAtomContainer>();
		try {
			tautomers = tautgen.getTautomers(smilesAtomContainer);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!tautomers.isEmpty()) {
			
			int count = 1;
			for(IAtomContainer tautomer : tautomers) {
				
				finalizeHydrogens(tautomer);
				String tautSmiles = "";		
				try {
					tautSmiles = smilesGenerator.create(tautomer);
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Tautomer " + Integer.toString(count) + ": "+ tautSmiles);
		        MoleculeEquivalence teq = 
		        		new MoleculeEquivalence(tautomer, inchiAtomContainer);
		        if(teq.areEquivalent())
		        	return true;
			}					
		}
		tautomers = new ArrayList<IAtomContainer>();
		try {
			tautomers = tautgen.getTautomers(inchiAtomContainer);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!tautomers.isEmpty()) {
			
			int count = 1;
			for(IAtomContainer tautomer : tautomers) {
				
				finalizeHydrogens(tautomer);
				String tautSmiles = "";		
				try {
					tautSmiles = smilesGenerator.create(tautomer);
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Tautomer " + Integer.toString(count) + ": "+ tautSmiles);
		        MoleculeEquivalence teq = 
		        		new MoleculeEquivalence(tautomer, smilesAtomContainer);
		        if(teq.areEquivalent())
		        	return true;
			}					
		}
				
		return false;
	}
	
	public static IAtomContainer convertInchi2Mol(String inchi) throws CDKException {

		if (inchi == null)
			throw new NullPointerException("Given InChI is null");
		if (inchi.isEmpty())
			throw new IllegalArgumentException("Empty string given as InChI");

		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final InChIToStructure converter = igfactory.getInChIToStructure(inchi, builder);
		if (converter.getStatus() == InchiStatus.SUCCESS || converter.getStatus() == InchiStatus.WARNING) {
			return converter.getAtomContainer();
		} else {
			System.out.println("Error while parsing InChI:\n'" + inchi + "'\n-> " + converter.getMessage());
			final IAtomContainer a = converter.getAtomContainer();
			if (a != null)
				return a;
			else
				throw new CDKException(converter.getMessage());
		}
	}
	
	public static void finalizeHydrogens(IAtomContainer atomContainer) {
		
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(atomContainer);
		} catch (CDKException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			CDKHydrogenAdder.getInstance(atomContainer.getBuilder()).addImplicitHydrogens(atomContainer);
		} catch (CDKException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			Kekulization.kekulize(atomContainer);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
























