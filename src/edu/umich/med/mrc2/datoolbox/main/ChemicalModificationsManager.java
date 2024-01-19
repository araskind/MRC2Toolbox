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

package edu.umich.med.mrc2.datoolbox.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.MolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ElementaryAdducts;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.ChemicalModificationDatabaseUtils;

public class ChemicalModificationsManager {

	private static Collection<Adduct> chemicalModifications;
	private static Collection<AdductExchange> adductExchangeList;
	private static Map<String, Adduct> adductNameMap;
	
	public static boolean isInitialized() {	
		return (chemicalModifications != null);
	}

	public static void addChemicalModification(Adduct modToAdd) {

//		getAllModifications().add(modToAdd);
//		adductNameMap.put(modToAdd.getName(), modToAdd);
//		try {
//			ChemicalModificationDatabaseUtils.addNewChemicalModification(modToAdd);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static void addAdductExchange(AdductExchange newExchange) {

		getAdductExchangeList().add(newExchange);
		try {
			ChemicalModificationDatabaseUtils.addNewAdductExchange(newExchange);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<Adduct> createAdductSetFromElementaryAdducts(Polarity polarity, int maxCharge, int maxOligomer) {

		Set<Adduct> adductSet = new HashSet<Adduct>();

		for (int o = 1; o <= maxOligomer; o++) {

			String prefix = "M";

			if (o > 1)
				prefix = Integer.toString(o) + "M";

			for (int ch = 1; ch <= maxCharge; ch++) {

				int refCharge = ch * polarity.getSign();
				HashSet<String> uniqueAdducts = new HashSet<String>();

				int maxReps = 4;

				for (int i = 0; i < ElementaryAdducts.values().length - 1; i++) {

					ElementaryAdducts eaOne = ElementaryAdducts.values()[i];
					IMolecularFormula formulaOne = MolecularFormulaManipulator
							.getMolecularFormula(eaOne.getName(), DefaultChemObjectBuilder.getInstance());

					for (int j = i + 1; j < ElementaryAdducts.values().length; j++) {

						ElementaryAdducts eaTwo = ElementaryAdducts.values()[j];
						IMolecularFormula formulaTwo = MolecularFormulaManipulator.getMolecularFormula(
								eaTwo.getName(), DefaultChemObjectBuilder.getInstance());

						if (i != j
								&& !(eaOne.equals(ElementaryAdducts.PROTON_GAIN)
										&& eaTwo.equals(ElementaryAdducts.PROTON_LOSS))
								&& !(eaTwo.equals(ElementaryAdducts.PROTON_GAIN)
										&& eaOne.equals(ElementaryAdducts.PROTON_LOSS))) {

							for (int rep = 1; rep <= maxReps; rep++) {

								for (int repTwo = 0; repTwo <= maxReps - rep; repTwo++) {

									int charge = eaOne.getCharge() * rep + eaTwo.getCharge() * repTwo;

									if (charge == refCharge) {

										String name = "";

										if (eaOne.allowToRemove())
											name = name + "-";
										else
											name = name + "+";

										if (rep > 1)
											name = name + Integer.toString(rep);

										name = name + eaOne.getName();

										if (repTwo > 0) {
											if (eaTwo.allowToRemove())
												name = name + "-";
											else
												name = name + "+";
										}
										if (repTwo > 1)
											name = name + Integer.toString(repTwo);
										if (repTwo > 0)
											name = name + eaTwo.getName();

										name = prefix + name;

										if (!uniqueAdducts.contains(name)) {

											uniqueAdducts.add(name);

											// Create actual adduct
											// System.out.println(name);
											Adduct newAdduct = new SimpleAdduct(
													null, name, name, refCharge, o, 0.0d, ModificationType.ADDUCT);

											// Calculate group string to remove
											IMolecularFormula toRemove = null;
											if (eaOne.allowToRemove() || (eaTwo.allowToRemove() && repTwo > 0)) {

												toRemove = new MolecularFormula();

												if (eaOne.allowToRemove()) {

													for (IIsotope isotope : formulaOne.isotopes())
														toRemove.addIsotope(isotope,
																formulaOne.getIsotopeCount(isotope) * rep);
												}
												if ((eaTwo.allowToRemove() && repTwo > 0)) {

													for (IIsotope isotope : formulaTwo.isotopes())
														toRemove.addIsotope(isotope,
																formulaTwo.getIsotopeCount(isotope) * repTwo);
												}
											}
											if (toRemove != null)
												newAdduct.setRemovedGroup(
														MolecularFormulaManipulator.getString(toRemove));

											// Calculate group string to add
											IMolecularFormula toAdd = new MolecularFormula();

											if (!eaOne.allowToRemove()) {

												for (IIsotope isotope : formulaOne.isotopes())
													toAdd.addIsotope(isotope,
															formulaOne.getIsotopeCount(isotope) * rep);
											}
											if (!eaTwo.allowToRemove() && repTwo > 0) {

												for (IIsotope isotope : formulaTwo.isotopes())
													toAdd.addIsotope(isotope,
															formulaTwo.getIsotopeCount(isotope) * repTwo);
											}
											newAdduct.setAddedGroup(MolecularFormulaManipulator.getString(toAdd));
											adductSet.add(newAdduct);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		Adduct[] sorted = adductSet.toArray(new Adduct[adductSet.size()]);
		Arrays.sort(sorted);
		return Arrays.asList(sorted);
	}

	public static Adduct createComplexModification(Adduct adduct,
			Adduct repeat, int numReps, Adduct repeatTwo, int numRepsTwo) {

		Adduct complexMod = null;

		if (!adduct.getModificationType().equals(ModificationType.ADDUCT))
			return null;

		if (repeat != null && !repeat.getModificationType().equals(ModificationType.REPEAT)
				&& !repeat.getModificationType().equals(ModificationType.LOSS))
			return null;

		if (repeatTwo != null && !repeatTwo.getModificationType().equals(ModificationType.REPEAT)
				&& !repeatTwo.getModificationType().equals(ModificationType.LOSS))
			return null;

		String signOne = "+";
		if (repeat != null) {
			if (repeat.getMassCorrection() < 0.0d)
				signOne = "";
		}

		String signTwo = "+";
		if (repeatTwo != null) {
			if (repeatTwo.getMassCorrection() < 0.0d)
				signTwo = "";
		}
		String multiplier = "";
		if (numReps > 1)
			multiplier = Integer.toString(numReps) + " X ";

		String multiplierTwo = "";
		if (numRepsTwo > 1)
			multiplierTwo = Integer.toString(numRepsTwo) + " X ";

		String adductName = adduct.getName();

		if (repeat != null && numReps > 0)
			adductName = adductName + " " + signOne + " " + multiplier + repeat.getName();
		if (repeatTwo != null && numRepsTwo > 0)
			adductName = adductName + " " + signTwo + " " + multiplierTwo + repeatTwo.getName();

		String description = adductName;

		complexMod = new SimpleAdduct(
				null, adductName, description, adduct.getCharge(), adduct.getOligomericState(),
				ModificationType.COMPOSITE);

		String addedGroup = adduct.getAddedGroup();
		String removedGroup = adduct.getRemovedGroup();

		if (repeat != null && numReps > 0) {

			for (int i = 0; i < numReps; i++) {

				addedGroup = addedGroup + repeat.getAddedGroup();
				removedGroup = removedGroup + repeat.getRemovedGroup();
			}
		}
		if (repeatTwo != null && numRepsTwo > 0) {

			for (int i = 0; i < numRepsTwo; i++) {

				addedGroup = addedGroup + repeatTwo.getAddedGroup();
				removedGroup = removedGroup + repeatTwo.getRemovedGroup();
			}
		}
		IMolecularFormula addedFormula = MolecularFormulaManipulator.getMolecularFormula(addedGroup,
				DefaultChemObjectBuilder.getInstance());
		IMolecularFormula removedFormula = MolecularFormulaManipulator.getMolecularFormula(removedGroup,
				DefaultChemObjectBuilder.getInstance());

		if (addedFormula != null)
			complexMod.setAddedGroup(MolecularFormulaManipulator.getString(addedFormula));

		if (removedFormula != null)
			complexMod.setRemovedGroup(MolecularFormulaManipulator.getString(removedFormula));

		return complexMod;
	}

	public static Collection<AdductExchange> getAdductExchangeList() {

		if(adductExchangeList == null) {
			try {
				adductExchangeList = ChemicalModificationDatabaseUtils.getAdductExchangeList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return adductExchangeList;
	}

	public static Map<String, Adduct> getAdductNameMap() {

		if(adductNameMap == null) {

			adductNameMap =
					getAllModifications().stream().
					collect(Collectors.toMap(Adduct::getName, Function.identity()));

//			adductNameMap.putAll(
//					getAllModifications().stream().
//						filter(mod -> Objects.nonNull(mod.getNotationForType(AdductNotationType.CEF))).
//						collect(Collectors.toMap(Adduct::getNotationForType, Function.identity())));
		}
		return adductNameMap;
	}

	public static List<Adduct> getAdducts(Polarity adductPolarity) {

		return getAllModifications().stream().
			filter(mod -> mod.getModificationType().equals(ModificationType.ADDUCT)).
			filter(mod -> mod.getPolarity().equals(adductPolarity)).
			filter(mod -> mod.isEnabled()).
			sorted().
			collect(Collectors.toList());
	}

	public static Collection<Adduct> getAllModifications() {

		if(chemicalModifications == null) {
			try {
//				chemicalModifications = ChemicalModificationDatabaseUtils.getChemicalModificationList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return chemicalModifications;
	}

	public static List<Adduct> getChargedModifications(Polarity adductPolarity) {

		return Stream.concat(
				getAllModifications().stream().filter(mod -> mod.getModificationType().equals(ModificationType.ADDUCT)),
				getAllModifications().stream().filter(mod -> mod.getModificationType().equals(ModificationType.COMPOSITE))).
			filter(mod -> mod.getPolarity().equals(adductPolarity)).
			filter(mod -> mod.isEnabled()).
			sorted().
			collect(Collectors.toList());
	}

	public static List<Adduct> getCompositModifications(Polarity adductPolarity) {

		return getAllModifications().stream().
				filter(mod -> mod.getModificationType().equals(ModificationType.COMPOSITE)).
				filter(mod -> mod.getPolarity().equals(adductPolarity)).
				filter(mod -> mod.isEnabled()).
				sorted().
				collect(Collectors.toList());
	}

	public static List<AdductExchange> getExchanges(Polarity adductPolarity) {

		return getAdductExchangeList().stream().
				filter(mod -> mod.getLeavingAdduct().getPolarity().equals(adductPolarity)).
				//	filter(mod -> mod.isEnabled()).
				sorted().
				collect(Collectors.toList());
	}

	public static List<Adduct> getLosses() {

		return getAllModifications().stream().
				filter(mod -> mod.getModificationType().equals(ModificationType.LOSS)).
				filter(mod -> mod.isEnabled()).
				sorted().
				collect(Collectors.toList());
	}

	public static Adduct getModificationByName(String name) {
		return getAdductNameMap().get(name);
	}

	public static List<Adduct> getModifications(ModificationType type) {

		return getAllModifications().stream().
				filter(mod -> mod.getModificationType().equals(type)).
				sorted().
				collect(Collectors.toList());
	}

	public static List<Adduct> getEnabledModifications(ModificationType type) {

		return getAllModifications().stream().
				filter(mod -> mod.getModificationType().equals(type)).
				filter(mod -> mod.isEnabled()).
				sorted().
				collect(Collectors.toList());
	}

	public static List<Adduct> getNeutralModifications(Polarity polarity, boolean activeOnly) {

		if(activeOnly) {
			return Stream.concat(
					getAllModifications().stream().
						filter(mod -> mod.getModificationType().equals(ModificationType.REPEAT)),
					getAllModifications().stream().
						filter(mod -> mod.getModificationType().equals(ModificationType.EXCHANGE))).
				filter(mod -> (mod.getPolarity().equals(polarity) 
						|| mod.getPolarity().equals(Polarity.Neutral))).
				filter(mod -> mod.isEnabled()).
				sorted().
				collect(Collectors.toList());
		}
		else {
			return Stream.concat(
					getAllModifications().stream().
						filter(mod -> mod.getModificationType().equals(ModificationType.REPEAT)),
					getAllModifications().stream().
						filter(mod -> mod.getModificationType().equals(ModificationType.EXCHANGE))).
				filter(mod -> (mod.getPolarity().equals(polarity) 
						|| mod.getPolarity().equals(Polarity.Neutral))).
				sorted().
				collect(Collectors.toList());
		}
	}

	public static List<Adduct> getRepeats() {

		return getAllModifications().stream().
				filter(mod -> mod.getModificationType().equals(ModificationType.REPEAT)).
				filter(mod -> mod.isEnabled()).
				sorted().
				collect(Collectors.toList());
	}

	public static void refreshNameMap() {

		adductNameMap =
				getAllModifications().stream().
				collect(Collectors.toMap(Adduct::getName, Function.identity()));

//		adductNameMap.putAll(
//				getAllModifications().stream().
//					filter(mod -> Objects.nonNull(mod.getCefNotation())).
//					collect(Collectors.toMap(Adduct::getCefNotation, Function.identity())));
	}

	public static void removeChemicalModification(Adduct modToRemove) {

		getAllModifications().remove(modToRemove);
		getAdductNameMap().remove(modToRemove.getName());
		try {
			ChemicalModificationDatabaseUtils.deleteChemicalModification(modToRemove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void updateChemicalModification(String originalName, Adduct activeModification) {

		try {
//			ChemicalModificationDatabaseUtils.updateChemicalModification(originalName, activeModification);
			getAdductNameMap().remove(originalName);
			getAdductNameMap().put(activeModification.getName(), activeModification);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeAdductExchange(AdductExchange exchangeToRemove) {

		getAdductExchangeList().remove(exchangeToRemove);
		try {
			ChemicalModificationDatabaseUtils.deleteAdductExchange(exchangeToRemove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void updateAdductExchange(
			AdductExchange exchangeToUpdate,
			Adduct newLeavingAddduct,
			Adduct newComingAddduct) {
//		try {
//			ChemicalModificationDatabaseUtils.deleteAdductExchange(exchangeToUpdate);
//			AdductExchange updated = new AdductExchange(newLeavingAddduct, newComingAddduct);
//			updated.setEnabled(exchangeToUpdate.isEnabled());
//			ChemicalModificationDatabaseUtils.addNewAdductExchange(updated);
//			getAdductExchangeList().remove(exchangeToUpdate);
//			getAdductExchangeList().add(updated);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static boolean adductExchangeExists(AdductExchange newExchange) {

		return getAdductExchangeList().stream().
			filter(e -> (e.getComingAdduct().equals(newExchange.getComingAdduct()) &&
					e.getLeavingAdduct().equals(newExchange.getLeavingAdduct()))).
			findFirst().isPresent();
	}

	public static void populateDataLists() throws Exception {

//		chemicalModifications = 
//				ChemicalModificationDatabaseUtils.getChemicalModificationList();
//
//		adductNameMap =
//				chemicalModifications.stream().
//				collect(Collectors.toMap(Adduct::getName, Function.identity()));

//		adductNameMap.putAll(chemicalModifications.stream().
//			filter(mod -> Objects.nonNull(mod.getCefNotation())).
//			filter(mod -> !mod.getCefNotation().trim().isEmpty()).
//			collect(Collectors.toMap(Adduct::getCefNotation, Function.identity())));

//		adductExchangeList = ChemicalModificationDatabaseUtils.getAdductExchangeList();
	}

	public static Adduct getDafaultAdductForPolarity(Polarity pol) {
		
		if(pol == null)
			throw new IllegalArgumentException("Polarity can not be null!");
		
		if(pol.equals(Polarity.Positive))
			return getAdductNameMap().get("[M+H]+");
		else if(pol.equals(Polarity.Negative))
			return getAdductNameMap().get("[M-H]-");
		else
			return getAdductNameMap().get("[M]");
	}
	
	public static Adduct getDefaultAdductForCharge(int charge) {
		
		if(charge == 1)
			return getAdductNameMap().get("[M+H]+");
		else if(charge == 2)
			return getAdductNameMap().get("[M+2H]2+");
		else if(charge == 2)
			return getAdductNameMap().get("[M+3H]3+");
		else if(charge == -1)
			return getAdductNameMap().get("[M-H]-");
		else if(charge == -2)
			return getAdductNameMap().get("[M-2H]2-");
		else if(charge == -3)
			return getAdductNameMap().get("[M-3H]3-");
		else
			return getAdductNameMap().get("[M]");
	}
}



























