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

package edu.umich.med.mrc2.datoolbox.dbparse;

import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;

import ambit2.tautomers.processor.StructureStandardizer;
import io.github.dan2097.jnainchi.InchiStatus;

public class StructureStandardizationUtils {
	
	private static final StructureStandardizer sStandardizer = getConfiguredStructureStandardizer();
	private static final IChemObjectBuilder bldr = SilentChemObjectBuilder.getInstance();
	private static final CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(bldr);
	private static final CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(bldr);
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);	
	private static final SmilesParser smilesParser = new SmilesParser(bldr);
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	
	public static StandardizedStructure standardizeStructure(String smiles, String inchi) {
		
		if((smiles == null || smiles.isEmpty() || smiles.equals("NoSmile")) 
				&& (inchi == null || inchi.isEmpty())) {
			return null;
		}
		IAtomContainer mol = null;
		try {
			mol = smilesParser.parseSmiles(smiles);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if(mol == null) {
			
			InChIToStructure intostruct = null;
			try {
				intostruct = igfactory.getInChIToStructure(inchi, bldr);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(intostruct != null) {
				
				InchiStatus ret = intostruct.getStatus();
				if (ret == InchiStatus.WARNING) {
					// Structure generated, but with warning message
					System.out.println("InChI warning: " + intostruct.getMessage());
				} else if (ret != InchiStatus.SUCCESS) {
					// Structure generation failed
					System.out.println("Structure generation failed failed: " 
							+ ret.toString() + " [" + intostruct.getMessage() + "]");
					return null;
				}
				mol = intostruct.getAtomContainer();
			}
		}
		if(mol == null)
			return null;
			
		IAtomContainer stdMol = null;
		try {
			stdMol = sStandardizer.process(mol);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(stdMol != null) 
			return new StandardizedStructure(stdMol);		
		else
			return null;
	}
	
	private static StructureStandardizer getConfiguredStructureStandardizer() {
		
		StructureStandardizer stdizer = new StructureStandardizer();
		stdizer.setGenerateSMILES_Canonical(false);
		stdizer.setGenerateSMILES_Aromatic(false);
		stdizer.setGenerateInChI(true);
		stdizer.setSplitFragments(false);
		stdizer.setGenerateTautomers(false);
		stdizer.setNeutralise(false);
		stdizer.setClearIsotopes(false);
		stdizer.setImplicitHydrogens(false);
		stdizer.setGenerate2D(false);
		stdizer.setGenerateStereofrom2D(false);
		
		igfactory = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stdizer;
	}
}
