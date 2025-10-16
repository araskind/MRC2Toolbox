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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.Arrays;

import org.openscience.cdk.Atom;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.isomorphism.Mappings;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smarts.SmartsPattern;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import io.github.dan2097.jnainchi.InchiStatus;

public class MSReadyUtils {
	
	private static final IChemObjectBuilder bldr = SilentChemObjectBuilder.getInstance();
	private static final CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(bldr);
	private static final CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(bldr);
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
	private static final SmilesParser smilesParser = new SmilesParser(bldr);
	private static final Aromaticity aromaticity = new Aromaticity(
			ElectronDonation.cdk(),
            Cycles.or(Cycles.all(), Cycles.all(6)));
	
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;

	public static CompoundIdentity neutralizePhosphoCholine(CompoundIdentity charged) {
		
		if(charged.getSmiles() == null || charged.getSmiles().isEmpty())
			return null;
		
		IAtomContainer atomContainer = null;
		try {
			atomContainer = smilesParser.parseSmiles(charged.getSmiles());
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(atomContainer == null)
			return null;
		
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
		SmartsPattern phosCholPattern = SmartsPattern.create("OP(OCC[N+](C)(C)C)(OC)=O");
		SmartsPattern poPattern = SmartsPattern.create("[P]O");
		int[] phosCholMapping = phosCholPattern.match(atomContainer);
		if(phosCholMapping.length == 0)
			return null;
		
		Mappings poMappings = poPattern.matchAll(atomContainer);
		for(int[] poMapping : poMappings) {
			
			int[] intersect = Arrays.stream(phosCholMapping).distinct()
					.filter(x -> Arrays.stream(poMapping).anyMatch(y -> y == x)).toArray();
			for (int i : intersect) {
				
				IAtom atom = atomContainer.getAtom(i);
				
				if (atom.getSymbol().equals("O") && atom.getImplicitHydrogenCount() == 1) {

					IAtom hydroxyl = new Atom("O");
					  hydroxyl.setImplicitHydrogenCount(0);
					  hydroxyl.setFormalCharge(-1);
					AtomContainerManipulator.replaceAtomByAtom(atomContainer, atom, hydroxyl);
					String smiles = "";		
					try {
						smiles = smilesGenerator.create(atomContainer);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					IMolecularFormula molFormula = 
							MolecularFormulaManipulator.getMolecularFormula(atomContainer);			
					String mfFromFStringFromSmiles = 
							MolecularFormulaManipulator.getString(molFormula);	
					double smilesMass = MolecularFormulaManipulator.getMass(
									molFormula, MolecularFormulaManipulator.MonoIsotopic);
					
					CompoundIdentity curatedId =  new CompoundIdentity(
							charged.getPrimaryDatabase(), 
							charged.getPrimaryDatabaseId(),
							charged.getName(), 					
							charged.getSysName(), 
							mfFromFStringFromSmiles, 
							smilesMass, 
							smiles);
					igfactory = null;
					try {
						igfactory = InChIGeneratorFactory.getInstance();
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						inChIGenerator = igfactory.getInChIGenerator(atomContainer);
						InchiStatus inchiStatus = inChIGenerator.getStatus();
						if (inchiStatus.equals(InchiStatus.WARNING)) {
							System.out.println("InChI warning: " + inChIGenerator.getMessage());
						} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
							System.out.println("InChI failed: [" + inChIGenerator.getMessage() + "]");
						}
						String inchiKey = inChIGenerator.getInchiKey();
						curatedId.setInChiKey(inchiKey);
					} 
					catch (CDKException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}					
					curatedId.setCharge(molFormula.getCharge());
					return curatedId;
				}
			}
		}
		return null;
	}
	
	public static CompoundIdentity neutralizeSmiles(CompoundIdentity charged) {
		
		if(charged.getSmiles() == null || charged.getSmiles().isEmpty())
			return null;
		
//		if(!charged.getSmiles().contains("[C-]") && !charged.getSmiles().contains("[CH-]") 
//				&& !charged.getSmiles().contains("[C+]"))
//			return null;
		
		if(!charged.getSmiles().contains("-]") && !charged.getSmiles().contains("+]"))
			return null;
		
		//[NH2+]
		String fixedSmiles = charged.getSmiles().
				replaceAll("\\[C-\\]", "[CH]").
				replaceAll("\\[O-\\]", "[OH]").
				replaceAll("\\[CH-\\]", "[CH2]").
				replaceAll("\\[C\\+\\]", "[CH]").
				replaceAll("\\[CH\\+\\]", "[CH2]").
				replaceAll("\\[N-]", "[NH]").
				replaceAll("\\[NH\\+\\]", "[N]").
				replaceAll("\\[FH\\+\\]", "[F]").
				replaceAll("\\[NH2\\+\\]", "[NH]").
				replaceAll("\\[OH2\\+\\]", "[OH]").
				replaceAll("\\[NH3\\+\\]", "[NH2]");
		
		IAtomContainer atomContainer = null;
		try {
			atomContainer = smilesParser.parseSmiles(fixedSmiles);
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(atomContainer == null)
			return null;
		
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
//		SmartsPattern chargedPyrrolePattern = SmartsPattern.create("[CH-]");
//		Mappings poMappings = chargedPyrrolePattern.matchAll(atomContainer);
//		if(poMappings.count() == 0)
//			return null;
//		
//		for(int[] poMapping : poMappings) {
//
//			for (int i : poMapping) {
//				
//				IAtom atom = atomContainer.getAtom(i);			
//				if (atom.getSymbol().equals("C") && Math.round(atom.getCharge()) == -1)
//					atom.setFormalCharge(0);
//			}
//		}
//		try {
//			CDKHydrogenAdder.getInstance(atomContainer.getBuilder()).addImplicitHydrogens(atomContainer);
//		} catch (CDKException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
		String smiles = "";		
		try {
			smiles = smilesGenerator.create(atomContainer);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IMolecularFormula molFormula = 
				MolecularFormulaManipulator.getMolecularFormula(atomContainer);			
		String mfFromFStringFromSmiles = 
				MolecularFormulaManipulator.getString(molFormula);	
		double smilesMass = MolecularFormulaManipulator.getMass(
						molFormula, MolecularFormulaManipulator.MonoIsotopic);
		
		CompoundIdentity curatedId =  new CompoundIdentity(
				charged.getPrimaryDatabase(), 
				charged.getPrimaryDatabaseId(),
				charged.getName(), 					
				charged.getSysName(), 
				mfFromFStringFromSmiles, 
				smilesMass, 
				smiles);
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			inChIGenerator = igfactory.getInChIGenerator(atomContainer);
			InchiStatus inchiStatus = inChIGenerator.getStatus();
			if (inchiStatus.equals(InchiStatus.WARNING)) {
				System.out.println("InChI warning: " + inChIGenerator.getMessage());
			} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
				System.out.println("InChI failed: [" + inChIGenerator.getMessage() + "]");
			}
			String inchiKey = inChIGenerator.getInchiKey();
			curatedId.setInChiKey(inchiKey);
		} 
		catch (CDKException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}					
		curatedId.setCharge(molFormula.getCharge());
		return curatedId;
	}	
}
