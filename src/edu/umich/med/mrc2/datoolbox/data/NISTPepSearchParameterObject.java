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

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchThreshold;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HitRejectionOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.OutputInclusionOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PeptideScoreOption;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PreSearchType;
import edu.umich.med.mrc2.datoolbox.utils.Range;

//	TODO rewrite this whole thing using XSTREAM or simple manual mapping in properties file
public class NISTPepSearchParameterObject implements Serializable, Comparable<NISTPepSearchParameterObject>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4288504728496913193L;
	
	private String id;
	
	//	Libraries
	private File libraryDirectory;
	
	//	Input
	private Set<File>libraryFiles;
	private boolean useInputFile;
	
	//	Search parameters
	private PreSearchType preSearchType;
	private HiResSearchType hiResSearchType;
	private HiResSearchOption hiResSearchOption;
	private HitRejectionOption hitRejectionOption;
	private boolean enableReverseSearch;
	private boolean enableAlternativePeakMatching;
	private boolean ignorePeaksAroundPrecursor;
	private double ignoreAroundPrecursorWindow;
	private MassErrorType ignoreAroundPrecursorAccuracyUnits;
	private HiResSearchThreshold hiResSearchThreshold;
	private PeptideScoreOption peptideScoreOption;
	private double precursorMzErrorValue;
	private MassErrorType precursorMzErrorType;	
	private double fragmentMzErrorValue;
	private MassErrorType fragmentMzErrorType;
	private Range mzRange;
	private int minimumIntensityCutoff;
	private double hybridSearchMassLoss;
	private boolean matchPolarity;
	private boolean matchCharge;
	
	//	Run options
	private boolean highExecutionPriority;
	private boolean loadLibrariesInMemory;
	
	//	Output filters	
	private int minMatchFactor;
	private int maxNumberOfHits;	
	private OutputInclusionOption outputInclusionOption;
	
	//	Output table columns
	private Map<String, Boolean>outputColumns;
	
	public static final double INFINITE_MZ_UPPER_LIMIT = 100000.0d;
	
	public NISTPepSearchParameterObject() {
		super();
		libraryFiles = new TreeSet<File>();
		outputColumns = new TreeMap<String, Boolean>();
	}

	public List<String>getLibraryNames(){
		
		return libraryFiles.stream().
				map(f -> f.getName()).
				sorted().
				collect(Collectors.toList());		
	}
	
	public String getSearchParametersMD5string() {
		
		StringBuffer params = new StringBuffer();
		
		//	Add search parameters as strings
		if(preSearchType != null) params.append(preSearchType.name());
		if(hiResSearchType != null) params.append(hiResSearchType.name());
		if(hiResSearchOption != null) params.append(hiResSearchOption.name());
		if(hitRejectionOption != null) params.append(hitRejectionOption.name());
		params.append(Boolean.toString(enableReverseSearch));
		params.append(Boolean.toString(enableAlternativePeakMatching));
		params.append(Boolean.toString(ignorePeaksAroundPrecursor));
		params.append(Double.toString(ignoreAroundPrecursorWindow));
		if(ignoreAroundPrecursorAccuracyUnits != null) params.append(ignoreAroundPrecursorAccuracyUnits.name());
		if(hiResSearchThreshold != null) params.append(hiResSearchThreshold.name());
		if(peptideScoreOption != null) params.append(peptideScoreOption.name());
		params.append(Double.toString(precursorMzErrorValue));
		if(precursorMzErrorType != null) params.append(precursorMzErrorType.name());
		params.append(Double.toString(fragmentMzErrorValue));
		if(fragmentMzErrorType != null) params.append(fragmentMzErrorType.name());
		params.append(mzRange.toString());
		params.append(Integer.toString(minimumIntensityCutoff));
		params.append(Double.toString(hybridSearchMassLoss));
		params.append(Boolean.toString(matchPolarity));
		params.append(Boolean.toString(matchCharge));
	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(params.toString().getBytes(Charset.forName("windows-1252")));
			return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	public String printSearchParameters() {
		
		StringBuffer params = new StringBuffer();
		params.append("Search parameters for " + id + "\n-------------------------------\n");
		params.append("PreSearch Type: ");
		if(preSearchType != null) 
			params.append(preSearchType.toString() + "\n");
		else
			params.append("-\n");
		
		params.append("HighRes Search Type: ");
		if(hiResSearchType != null) 
			params.append(hiResSearchType.toString() + "\n");
		else
			params.append("-\n");

		params.append("HighRes Search Option: ");
		if(hiResSearchOption != null) 
			params.append(hiResSearchOption.toString() + "\n");
		else
			params.append("-\n");
		
		params.append("HighRes Search Option: ");
		if(hitRejectionOption != null) 
			params.append(hitRejectionOption.toString() + "\n");
		else
			params.append("-\n");
		
		params.append("Enable Reverse Search: " + Boolean.toString(enableReverseSearch) + "\n");	
		params.append("Enable Alternative Peak Matching: " + Boolean.toString(enableAlternativePeakMatching) + "\n");
		params.append("Ignore Peaks Around Precursor: " + Boolean.toString(ignorePeaksAroundPrecursor) + "\n");
		params.append("Ignore Peaks Around Precursor Window: " + Double.toString(ignoreAroundPrecursorWindow) + "\n");
		params.append("Ignore Peaks Around Precursor Units: ");
		if(ignoreAroundPrecursorAccuracyUnits != null) 
			params.append(ignoreAroundPrecursorAccuracyUnits.name() + "\n");
		else
			params.append("-\n");

		params.append("HiRes Search Threshold: ");
		if(hiResSearchThreshold != null) 
			params.append(hiResSearchThreshold.name() + "\n");
		else
			params.append("-\n");
		
		params.append("Peptide Score Option: ");
		if(peptideScoreOption != null) 
			params.append(peptideScoreOption.name() + "\n");
		else
			params.append("-\n");
		
		params.append("Precursor Mz Error Value: " + Double.toString(precursorMzErrorValue) + "\n");
		params.append("Precursor Mz Error Type: ");
		if(precursorMzErrorType != null) 
			params.append(precursorMzErrorType.name() + "\n");
		else
			params.append("-\n");

		params.append("Fragment Mz Error Value: " + Double.toString(fragmentMzErrorValue) + "\n");
		params.append("Fragment Mz Error Type: ");
		if(fragmentMzErrorType != null) 
			params.append(fragmentMzErrorType.name() + "\n");
		else
			params.append("-\n");	

		params.append("M/Z range: " + mzRange.toString() + "\n");
		params.append("Minimum Intensity Cutoff: " + Integer.toString(minimumIntensityCutoff) + "\n");
		params.append("Hybrid Search Mass Loss: " + Double.toString(hybridSearchMassLoss) + "\n");
		params.append("Match Polarity: " + Boolean.toString(matchPolarity) + "\n");
		params.append("Match Charge: " + Boolean.toString(matchCharge) + "\n");
		params.append("MD5: " + getSearchParametersMD5string() + "\n");
		
		return params.toString();
	}
	
	public String getResultFilteringParametersMD5string() {
		
		StringBuffer params = new StringBuffer();
		
		//	Add result filtering parameters as strings
		params.append(Integer.toString(minMatchFactor));
		params.append(Integer.toString(maxNumberOfHits));
		if(outputInclusionOption != null) 
			params.append(outputInclusionOption.name());	
	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(params.toString().getBytes(Charset.forName("windows-1252")));
			return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}		
		return null;
	}

	/**
	 * @return the libraryFiles
	 */
	public Set<File> getLibraryFiles() {
		return libraryFiles;
	}

	/**
	 * @return the preSearchType
	 */
	public PreSearchType getPreSearchType() {
		return preSearchType;
	}

	/**
	 * @return the hiResSearchType
	 */
	public HiResSearchType getHiResSearchType() {
		return hiResSearchType;
	}

	/**
	 * @return the hiResSearchOption
	 */
	public HiResSearchOption getHiResSearchOption() {
		return hiResSearchOption;
	}

	/**
	 * @return the hitRejectionOption
	 */
	public HitRejectionOption getHitRejectionOption() {
		return hitRejectionOption;
	}

	/**
	 * @return the enableReverseSearch
	 */
	public boolean isEnableReverseSearch() {
		return enableReverseSearch;
	}

	/**
	 * @return the enableAlternativePeakMatching
	 */
	public boolean isEnableAlternativePeakMatching() {
		return enableAlternativePeakMatching;
	}

	/**
	 * @return the ignoreAroundPrecursor
	 */
	public boolean isIgnorePeaksAroundPrecursor() {
		return ignorePeaksAroundPrecursor;
	}

	/**
	 * @return the ignoreAroundPrecursorWindow
	 */
	public double getIgnorePeaksAroundPrecursorWindow() {
		return ignoreAroundPrecursorWindow;
	}

	/**
	 * @return the ignoreAroundPrecursorAccuracyUnits
	 */
	public MassErrorType getIgnorePeaksAroundPrecursorAccuracyUnits() {
		return ignoreAroundPrecursorAccuracyUnits;
	}

	/**
	 * @return the hiResSearchThreshold
	 */
	public HiResSearchThreshold getHiResSearchThreshold() {
		return hiResSearchThreshold;
	}

	/**
	 * @return the peptideScoreOption
	 */
	public PeptideScoreOption getPeptideScoreOption() {
		return peptideScoreOption;
	}

	/**
	 * @return the precursorMzErrorValue
	 */
	public double getPrecursorMzErrorValue() {
		return precursorMzErrorValue;
	}

	/**
	 * @return the precursorMzErrorType
	 */
	public MassErrorType getPrecursorMzErrorType() {
		return precursorMzErrorType;
	}

	/**
	 * @return the fragmentMzErrorValue
	 */
	public double getFragmentMzErrorValue() {
		return fragmentMzErrorValue;
	}

	/**
	 * @return the fragmentMzErrorType
	 */
	public MassErrorType getFragmentMzErrorType() {
		return fragmentMzErrorType;
	}

	/**
	 * @return the mzRange
	 */
	public Range getMzRange() {
		return mzRange;
	}

	/**
	 * @return the minimumIntensityCutoff
	 */
	public int getMinimumIntensityCutoff() {
		return minimumIntensityCutoff;
	}

	/**
	 * @return the hybridSearchMassLoss
	 */
	public double getHybridSearchMassLoss() {
		return hybridSearchMassLoss;
	}

	/**
	 * @return the matchPolarity
	 */
	public boolean isMatchPolarity() {
		return matchPolarity;
	}

	/**
	 * @return the matchCharge
	 */
	public boolean isMatchCharge() {
		return matchCharge;
	}

	/**
	 * @return the setHighExecutionPriority
	 */
	public boolean isHighExecutionPriority() {
		return highExecutionPriority;
	}

	/**
	 * @return the loadLibrariesInMemory
	 */
	public boolean isLoadLibrariesInMemory() {
		return loadLibrariesInMemory;
	}

	/**
	 * @return the minMatchFactor
	 */
	public int getMinMatchFactor() {
		return minMatchFactor;
	}

	/**
	 * @return the maxNumberOfHits
	 */
	public int getMaxNumberOfHits() {
		return maxNumberOfHits;
	}

	/**
	 * @return the outputInclusionOption
	 */
	public OutputInclusionOption getOutputInclusionOption() {
		return outputInclusionOption;
	}

	/**
	 * @return the outputColumns
	 */
	public Map<String, Boolean> getOutputColumns() {
		return outputColumns;
	}

	/**
	 * @param preSearchType the preSearchType to set
	 */
	public void setPreSearchType(PreSearchType preSearchType) {
		this.preSearchType = preSearchType;
	}

	/**
	 * @param hiResSearchType the hiResSearchType to set
	 */
	public void setHiResSearchType(HiResSearchType hiResSearchType) {
		this.hiResSearchType = hiResSearchType;
	}

	/**
	 * @param hiResSearchOption the hiResSearchOption to set
	 */
	public void setHiResSearchOption(HiResSearchOption hiResSearchOption) {
		this.hiResSearchOption = hiResSearchOption;
	}

	/**
	 * @param hitRejectionOption the hitRejectionOption to set
	 */
	public void setHitRejectionOption(HitRejectionOption hitRejectionOption) {
		this.hitRejectionOption = hitRejectionOption;
	}

	/**
	 * @param enableReverseSearch the enableReverseSearch to set
	 */
	public void setEnableReverseSearch(boolean enableReverseSearch) {
		this.enableReverseSearch = enableReverseSearch;
	}

	/**
	 * @param enableAlternativePeakMatching the enableAlternativePeakMatching to set
	 */
	public void setEnableAlternativePeakMatching(boolean enableAlternativePeakMatching) {
		this.enableAlternativePeakMatching = enableAlternativePeakMatching;
	}

	/**
	 * @param ignoreAroundPrecursor the ignoreAroundPrecursor to set
	 */
	public void setIgnorePeaksAroundPrecursor(boolean ignoreAroundPrecursor) {
		this.ignorePeaksAroundPrecursor = ignoreAroundPrecursor;
	}

	/**
	 * @param ignoreAroundPrecursorWindow the ignoreAroundPrecursorWindow to set
	 */
	public void setIgnorePeaksAroundPrecursorWindow(double ignoreAroundPrecursorWindow) {
		this.ignoreAroundPrecursorWindow = ignoreAroundPrecursorWindow;
	}

	/**
	 * @param ignoreAroundPrecursorAccuracyUnits the ignoreAroundPrecursorAccuracyUnits to set
	 */
	public void setIgnorePeaksAroundPrecursorAccuracyUnits(MassErrorType ignoreAroundPrecursorAccuracyUnits) {
		this.ignoreAroundPrecursorAccuracyUnits = ignoreAroundPrecursorAccuracyUnits;
	}

	/**
	 * @param hiResSearchThreshold the hiResSearchThreshold to set
	 */
	public void setHiResSearchThreshold(HiResSearchThreshold hiResSearchThreshold) {
		this.hiResSearchThreshold = hiResSearchThreshold;
	}

	/**
	 * @param peptideScoreOption the peptideScoreOption to set
	 */
	public void setPeptideScoreOption(PeptideScoreOption peptideScoreOption) {
		this.peptideScoreOption = peptideScoreOption;
	}

	/**
	 * @param precursorMzErrorValue the precursorMzErrorValue to set
	 */
	public void setPrecursorMzErrorValue(double precursorMzErrorValue) {
		this.precursorMzErrorValue = precursorMzErrorValue;
	}

	/**
	 * @param precursorMzErrorType the precursorMzErrorType to set
	 */
	public void setPrecursorMzErrorType(MassErrorType precursorMzErrorType) {
		this.precursorMzErrorType = precursorMzErrorType;
	}

	/**
	 * @param fragmentMzErrorValue the fragmentMzErrorValue to set
	 */
	public void setFragmentMzErrorValue(double fragmentMzErrorValue) {
		this.fragmentMzErrorValue = fragmentMzErrorValue;
	}

	/**
	 * @param fragmentMzErrorType the fragmentMzErrorType to set
	 */
	public void setFragmentMzErrorType(MassErrorType fragmentMzErrorType) {
		this.fragmentMzErrorType = fragmentMzErrorType;
	}

	/**
	 * @param mzRange the mzRange to set
	 */
	public void setMzRange(Range mzRange) {
		this.mzRange = mzRange;
	}

	/**
	 * @param minimumIntensityCutoff the minimumIntensityCutoff to set
	 */
	public void setMinimumIntensityCutoff(int minimumIntensityCutoff) {
		this.minimumIntensityCutoff = minimumIntensityCutoff;
	}

	/**
	 * @param hybridSearchMassLoss the hybridSearchMassLoss to set
	 */
	public void setHybridSearchMassLoss(double hybridSearchMassLoss) {
		this.hybridSearchMassLoss = hybridSearchMassLoss;
	}

	/**
	 * @param matchPolarity the matchPolarity to set
	 */
	public void setMatchPolarity(boolean matchPolarity) {
		this.matchPolarity = matchPolarity;
	}

	/**
	 * @param matchCharge the matchCharge to set
	 */
	public void setMatchCharge(boolean matchCharge) {
		this.matchCharge = matchCharge;
	}

	/**
	 * @param setHighExecutionPriority the setHighExecutionPriority to set
	 */
	public void setHighExecutionPriority(boolean setHighExecutionPriority) {
		this.highExecutionPriority = setHighExecutionPriority;
	}

	/**
	 * @param loadLibrariesInMemory the loadLibrariesInMemory to set
	 */
	public void setLoadLibrariesInMemory(boolean loadLibrariesInMemory) {
		this.loadLibrariesInMemory = loadLibrariesInMemory;
	}

	/**
	 * @param minMatchFactor the minMatchFactor to set
	 */
	public void setMinMatchFactor(int minMatchFactor) {
		this.minMatchFactor = minMatchFactor;
	}

	/**
	 * @param maxNumberOfHits the maxNumberOfHits to set
	 */
	public void setMaxNumberOfHits(int maxNumberOfHits) {
		this.maxNumberOfHits = maxNumberOfHits;
	}

	/**
	 * @param outputInclusionOption the outputInclusionOption to set
	 */
	public void setOutputInclusionOption(OutputInclusionOption outputInclusionOption) {
		this.outputInclusionOption = outputInclusionOption;
	}

	/**
	 * @return the libraryDirectory
	 */
	public File getLibraryDirectory() {
		return libraryDirectory;
	}

	/**
	 * @param libraryDirectory the libraryDirectory to set
	 */
	public void setLibraryDirectory(File libraryDirectory) {
		this.libraryDirectory = libraryDirectory;
	}

	/**
	 * @return the useInputFile
	 */
	public boolean isUseInputFile() {
		return useInputFile;
	}

	/**
	 * @param useInputFile the useInputFile to set
	 */
	public void setUseInputFile(boolean useInputFile) {
		this.useInputFile = useInputFile;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int compareTo(NISTPepSearchParameterObject o) {
		return id.compareTo(o.getId());
	}
	
	@Override
	public String toString() {
		return id;
	}
}
