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

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IBioPolymer;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.templates.AminoAcids;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.ProteinBuilderTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import net.sf.jniinchi.INCHI_RET;

public class PeptideUtils {

	public static String translateThreeLetterToOneLetterCode(String peptideSequence) {

		ArrayList<String> oneLetter = new ArrayList<String>();
		String[] treeLetterValues = peptideSequence.trim().replaceAll("\\s+", "-").toUpperCase().split("-");

		for(int i=0; i<treeLetterValues.length; i++)
			oneLetter.add(AminoAcids.convertThreeLetterCodeToOneLetterCode(treeLetterValues[i]));

		return StringUtils.join(oneLetter,"");
	}

	public static CompoundIdentity generatePeptideIdentifiers(String oneLetterCodeSeq) {

		CompoundIdentity pepId = new CompoundIdentity();
		//SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Canonical | SmiFlavor.UseAromaticSymbols);
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Canonical);
		InChIGeneratorFactory igfactory = null;
		InChIGenerator inChIGenerator = null;
		String smiles = null;
		String inchiKey = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IBioPolymer peptide = null;
		try {
			peptide = ProteinBuilderTool.createProtein(oneLetterCodeSeq);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (peptide != null) {
			try {
				AtomContainer mpep = new AtomContainer(peptide);
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mpep);
				CDKHydrogenAdder.getInstance(mpep.getBuilder()).addImplicitHydrogens(mpep);
				smiles = smilesGenerator.create(mpep);


				inChIGenerator = igfactory.getInChIGenerator(mpep);
				INCHI_RET ret = inChIGenerator.getReturnStatus();
				if (ret == INCHI_RET.WARNING) {

					System.out.println("InChI warning: " + inChIGenerator.getMessage());
				} else if (ret != INCHI_RET.OKAY) {

					throw new CDKException(
							"InChI failed: " + ret.toString() + " [" + inChIGenerator.getMessage() + "]");
				}
				inchiKey = inChIGenerator.getInchiKey();
			}
			catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		pepId.setSmiles(smiles);
		pepId.setInChiKey(inchiKey);
		return pepId;
	}
}
