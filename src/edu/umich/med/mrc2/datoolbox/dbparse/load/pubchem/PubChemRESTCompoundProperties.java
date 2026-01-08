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

package edu.umich.med.mrc2.datoolbox.dbparse.load.pubchem;

public enum PubChemRESTCompoundProperties {

	MolecularFormula,
	MolecularWeight,
	SMILES,
	ConnectivitySMILES,
	InChI,
	InChIKey,
	IUPACName,
	Title,
	XLogP,
	ExactMass,
	MonoisotopicMass,
	TPSA,
	Complexity,
	Charge,
	HBondDonorCount,
	HBondAcceptorCount,
	RotatableBondCount,
	HeavyAtomCount,
	IsotopeAtomCount,
	AtomStereoCount,
	DefinedAtomStereoCount,
	UndefinedAtomStereoCount,
	BondStereoCount,
	DefinedBondStereoCount,
	UndefinedBondStereoCount,
	CovalentUnitCount,
	PatentCount,
	PatentFamilyCount,
	AnnotationTypes,
	AnnotationTypeCount,
	SourceCategories,
	LiteratureCount,
	Volume3D,
	XStericQuadrupole3D,
	YStericQuadrupole3D,
	ZStericQuadrupole3D,
	FeatureCount3D,
	FeatureAcceptorCount3D,
	FeatureDonorCount3D,
	FeatureAnionCount3D,
	FeatureCationCount3D,
	FeatureRingCount3D,
	FeatureHydrophobeCount3D,
	ConformerModelRMSD3D,
	EffectiveRotorCount3D,
	ConformerCount3D,
	Fingerprint2D,
	;
}
