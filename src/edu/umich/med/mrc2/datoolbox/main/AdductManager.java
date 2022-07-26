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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.compare.AdductComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.AdductExchangeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.AdductDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerUtils;

public class AdductManager {

	/*
	 * Adduct related functions
	 * */
	private static Collection<Adduct> adductList;
	private static Collection<BinnerNeutralMassDifference>binnerNeutralMassDifferenceList;
	private static Collection<BinnerAdduct>binnerAdductList;
	
	public static final AdductComparator adductTypeNameSorter = 
			new AdductComparator(SortProperty.Name);
	public static final AdductExchangeComparator adductExchangeNameComparator = 
			new AdductExchangeComparator(SortProperty.Name);
	
	public static void refreshAlldata() {
		refreshAdductList();
		refreshAdductExchangeList();
		refreshBinnerNeutralMassDifference();
		refreshBinnerAdductList();
	}

	public static Collection<Adduct> getAdductList() {

		if(adductList == null || adductList.isEmpty()) {
			try {
				adductList = AdductDatabaseUtils.getAdductList();				
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				adductList.addAll(AdductDatabaseUtils.getNeutralLossList());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				adductList.addAll(AdductDatabaseUtils.getNeutralAdductList());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				adductList.addAll(AdductDatabaseUtils.getCompositeAdductList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return adductList;
	}
	
	public static Collection<Adduct> getChargedAdductList() {
		return getAdductList().stream().
				filter(a -> a.getCharge() != 0).
				sorted(adductTypeNameSorter).
				collect(Collectors.toList());
	}
	
	public static Collection<Adduct> getChargeCarriers() {
		return getAdductList().stream().
				filter(a -> a.getModificationType().equals(ModificationType.ADDUCT)).
				sorted(adductTypeNameSorter).collect(Collectors.toList());
	}
	
	public static void refreshAdductList() {
		
		if(adductList != null)
			adductList.clear();
		
		getAdductList();
	}
	
	public static void addAdduct(Adduct newAdduct) {
				
		if(newAdduct instanceof SimpleAdduct) {
			SimpleAdduct adduct = (SimpleAdduct)newAdduct;
			if(adduct.getModificationType().equals(ModificationType.ADDUCT)) {
				try {
					AdductDatabaseUtils.addNewAdduct(adduct);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(adduct.getModificationType().equals(ModificationType.LOSS)) {
				try {
					AdductDatabaseUtils.addNewNeutralLoss(adduct);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(adduct.getModificationType().equals(ModificationType.REPEAT)) {
				try {
					AdductDatabaseUtils.addNewNeutralAdduct(adduct);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(newAdduct instanceof CompositeAdduct) {
			try {
				AdductDatabaseUtils.addNewCompositeAdduct((CompositeAdduct)newAdduct);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		getAdductList().add(newAdduct);
		Collections.sort((List)adductList, adductTypeNameSorter);
	}
	
	public static void deleteAdduct(Adduct adductToDelete) {
		
		getAdductList().remove(adductToDelete);
		if(adductToDelete instanceof SimpleAdduct) {
			SimpleAdduct adduct = (SimpleAdduct)adductToDelete;
			if(adduct.getModificationType().equals(ModificationType.ADDUCT)) {
				try {
					AdductDatabaseUtils.deleteAdduct(adduct);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(adduct.getModificationType().equals(ModificationType.LOSS)) {
				try {
					AdductDatabaseUtils.deleteNeutralLoss(adduct);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(adduct.getModificationType().equals(ModificationType.REPEAT)) {
				try {
					AdductDatabaseUtils.deleteNeutralAdduct(adduct);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(adductToDelete instanceof CompositeAdduct) {
			try {
				AdductDatabaseUtils.deleteCompositeAdduct((CompositeAdduct)adductToDelete);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void updateAdduct(Adduct adductToUpdate) {

		if(adductToUpdate instanceof SimpleAdduct) {
			SimpleAdduct adduct = (SimpleAdduct)adductToUpdate;
			if(adduct.getModificationType().equals(ModificationType.ADDUCT)) {
				try {
					AdductDatabaseUtils.updateAdduct(adduct);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(adduct.getModificationType().equals(ModificationType.LOSS)) {
				try {
					AdductDatabaseUtils.updateNeutralLoss(adduct);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(adduct.getModificationType().equals(ModificationType.REPEAT)) {
				try {
					AdductDatabaseUtils.updateNeutralAdduct(adduct);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(adductToUpdate instanceof CompositeAdduct) {
			try {
				AdductDatabaseUtils.updateCompositeAdduct((CompositeAdduct)adductToUpdate);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		refreshAdductList();
	}
	
	public static boolean adductNameExists(String newName) {

		return getAdductList().stream().
			filter(a -> a.getName().equals(newName)).
			findFirst().isPresent();
	}
	
	public static Collection<Adduct>getAdductsForType(ModificationType type){
		
		return getAdductList().stream().
				filter(a -> a.getModificationType().equals(type)).
				sorted(adductTypeNameSorter).collect(Collectors.toList());	
	}
	
	public static Collection<Adduct>getAdductsForPolarity(Polarity polarity){
		
		return getAdductList().stream().
				filter(a -> a.getPolarity().equals(polarity)).
				sorted(adductTypeNameSorter).collect(Collectors.toList());	
	}
	
	public static Collection<Adduct>getAdductsForTypeAndPolarity(
			ModificationType type, Polarity polarity){
		
		return getAdductList().stream().
				filter(a -> a.getModificationType().equals(type)).
				filter(a -> a.getPolarity().equals(polarity)).
				sorted(adductTypeNameSorter).collect(Collectors.toList());	
	}
	
	public static Collection<Adduct>getAdductsForCharge(int charge){
		
		return getAdductList().stream().
				filter(a -> a.getCharge() == charge).
				sorted(adductTypeNameSorter).collect(Collectors.toList());	
	}
	
	public static Collection<Adduct>getAdductsForTypeAndCharge(
			ModificationType type, int charge){
		
		return getAdductList().stream().
				filter(a -> a.getModificationType().equals(type)).
				filter(a -> a.getCharge() == charge).
				sorted(adductTypeNameSorter).collect(Collectors.toList());	
	}
	
	public static Adduct getAdductById(String id) {
		return getAdductList().stream().
				filter(a -> a.getId().equals(id)).
				findFirst().orElse(null);
	}
	
	public static Adduct getAdductByName(String name) {
		return getAdductList().stream().
				filter(a -> a.getName().equals(name)).
				findFirst().orElse(null);
	}
	
	public static Adduct getAdductByCefNotation(String cefNotation) {
		return getAdductList().stream().
				filter(f -> Objects.nonNull(f.getCefNotation())).
				filter(a -> a.getCefNotation().equals(cefNotation)).
				findFirst().orElse(null);
	}
	
	public static Collection<Adduct> getNeutralLosses() {
		return getAdductsForType(ModificationType.LOSS);
	}
	
	public static Collection<Adduct> getNeutralAdducts() {
		return getAdductsForType(ModificationType.REPEAT);
	}
	
	public static Collection<Adduct> getNeutralModifications(){
		
		return getAdductList().stream().
				filter(a -> (a.getModificationType().equals(ModificationType.LOSS) 
						|| a.getModificationType().equals(ModificationType.REPEAT))).
				sorted(adductTypeNameSorter).collect(Collectors.toList());	
	}
	
	public static Collection<CompositeAdduct> getCompositeAdducts() {
		
		return getAdductList().stream().
			filter(CompositeAdduct.class::isInstance).map(CompositeAdduct.class::cast).
			sorted(adductTypeNameSorter).collect(Collectors.toList());
	}
	
	public static Collection<SimpleAdduct> getSimpleAdducts() {
		
		return getAdductList().stream().
			filter(SimpleAdduct.class::isInstance).map(SimpleAdduct.class::cast).
			sorted(adductTypeNameSorter).collect(Collectors.toList());
	}
	
	public static Adduct getDefaultAdductForPolarity(Polarity pol) {
		
		if(pol == null)
			throw new IllegalArgumentException("Polarity can not be null!");
		
		if(pol.equals(Polarity.Positive))
			return getAdductByName("[M+H]+");
		else if(pol.equals(Polarity.Negative))
			return getAdductByName("[M-H]-");
		else
			return getAdductByName("[M]");
	}
	
	public static Adduct getDefaultAdductForCharge(int charge) {
		
		if(charge > 3 || charge < -3)
			throw new IllegalArgumentException("Charge should be between -3 and 3!");
		
		if(charge == 3)
			return getAdductByName("[M+3H]3+");
		else if(charge == 2)
			return getAdductByName("[M+2H]2+");
		else if(charge == 1)
			return getAdductByName("[M+H]+");
		else if(charge == -1)
			return getAdductByName("[M-H]-");
		else if(charge == -2)
			return getAdductByName("[M-2H]2-");
		else if(charge == -3)
			return getAdductByName("[M-3H]3-");
		else
			return getAdductByName("[M]");
	}
	
	/*
	 * Adduct exchange related functions
	 * */
	private static Collection<AdductExchange> adductExchangeList;
	
	public static Collection<AdductExchange> getAdductExchangeList() {

		if(adductExchangeList == null || adductExchangeList.isEmpty()) {
			try {
				adductExchangeList = AdductDatabaseUtils.getAdductExchangeList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//	Debug only
//		for(AdductExchange ex : adductExchangeList) 
//			System.out.println(ex.getId() + "\\t" + ex.getComingAdduct().getName() + "\\t" + ex.getLeavingAdduct().getName());
		
		return adductExchangeList;
	}
	
	public static void refreshAdductExchangeList() {
		
		if(adductExchangeList != null)
			adductExchangeList.clear();
		
		getAdductExchangeList();
	}	
	
	public static void refreshBinnerNeutralMassDifference() {
	
		if(binnerNeutralMassDifferenceList != null)
			binnerNeutralMassDifferenceList.clear();
		
		getBinnerNeutralMassDifferenceList();
	}
	
	public static Collection<BinnerNeutralMassDifference> getBinnerNeutralMassDifferenceList() {

		if(binnerNeutralMassDifferenceList == null || binnerNeutralMassDifferenceList.isEmpty()) {
			try {
				binnerNeutralMassDifferenceList = BinnerUtils.getBinnerNeutralMassDifferences();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return binnerNeutralMassDifferenceList;
	}
	
	public static void refreshBinnerAdductList() {
		
		if(binnerAdductList != null)
			binnerAdductList.clear();
		
		getBinnerAdductList();
	}
	
	public static Collection<BinnerAdduct> getBinnerAdductList() {

		if(binnerAdductList == null || binnerAdductList.isEmpty()) {
			try {
				binnerAdductList = BinnerUtils.getBinnerAdducts();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return binnerAdductList;
	}
	
	public static void deleteBinnerAdduct(BinnerAdduct adductToDelete) {
		
		getBinnerAdductList().remove(adductToDelete);
		try {
			BinnerUtils.deleteBinnerAdduct(adductToDelete);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<AdductExchange> getAdductExchangeListForPolarity(Polarity adductPolarity) {

		return getAdductExchangeList().stream().
				filter(mod -> mod.getLeavingAdduct().getPolarity().equals(adductPolarity)).
				sorted(adductExchangeNameComparator).
				collect(Collectors.toList());
	}
	
	public static void addAdductExchange(AdductExchange newExchange) {

		getAdductExchangeList().add(newExchange);
		try {
			AdductDatabaseUtils.addNewAdductExchange(newExchange);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateAdductExchange(AdductExchange originalExchange, AdductExchange modifiedExchange) {
		try {
			AdductDatabaseUtils.updateAdductExchange(originalExchange, modifiedExchange);
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		getAdductExchangeList().remove(originalExchange);
		modifiedExchange.setId(originalExchange.getId());
		getAdductExchangeList().add(modifiedExchange);
	}
	
	public static void deleteAdductExchange(AdductExchange exchangeToRemove) {

		getAdductExchangeList().remove(exchangeToRemove);
		try {
			AdductDatabaseUtils.deleteAdductExchange(exchangeToRemove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean adductExchangeExists(AdductExchange newExchange) {

		return getAdductExchangeList().stream().
			filter(e -> (e.getComingAdduct().equals(newExchange.getComingAdduct()) 
							&& e.getLeavingAdduct().equals(newExchange.getLeavingAdduct())) 
					|| (e.getLeavingAdduct().equals(newExchange.getComingAdduct()) 
							&& e.getComingAdduct().equals(newExchange.getLeavingAdduct()))).
			findFirst().isPresent();
	}
	
	public static boolean adductExchangeExists(String editedExchangeId, Adduct newComingAdduct, Adduct newLeavingAduct) {

		return getAdductExchangeList().stream().
			filter(e -> !e.getId().equals(editedExchangeId)).
			filter(e -> (e.getComingAdduct().equals(newComingAdduct) 
					&& e.getLeavingAdduct().equals(newLeavingAduct)) 
			|| (e.getLeavingAdduct().equals(newComingAdduct) 
					&& e.getComingAdduct().equals(newLeavingAduct))).
			findFirst().isPresent();
	}
	
	public static AdductExchange getAdductExchangeById(String id) {
		
		return getAdductExchangeList().stream().
				filter(a -> a.getId().equals(id)).
				findFirst().orElse(null);
	}
	
	public static BinnerNeutralMassDifference getBinnerNeutralMassDifferenceById(String id) {
		
		return getBinnerNeutralMassDifferenceList().stream().
				filter(a -> a.getId().equals(id)).
				findFirst().orElse(null);
	}
	
	public static BinnerAdduct getBinnerAdductById(String id) {
		
		return getBinnerAdductList().stream().
				filter(a -> a.getId().equals(id)).
				findFirst().orElse(null);
	}
}











