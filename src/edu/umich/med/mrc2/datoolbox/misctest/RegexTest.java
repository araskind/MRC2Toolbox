/*******************************************************************************
 *
System.out.println(smiles); * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.ProcessBuilder.Redirect;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBioPolymer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Writer;
import org.openscience.cdk.io.inchi.INChIContentProcessorTool;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.ProteinBuilderTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.Ostermiller.util.CSVParser;
//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundNameSet;
import edu.umich.med.mrc2.datoolbox.data.LibraryEntrySource;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescription;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescriptionBundle;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSComponentTableFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSResolution;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACmetaboliteMetaDataFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoCDStudy;
import edu.umich.med.mrc2.datoolbox.data.thermo.ThermoCDWorkflow;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDbConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.DocumentUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTMsDataUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.RemoteMsLibraryUtils;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.database.load.massbank.MassBankFileParser;
import edu.umich.med.mrc2.datoolbox.database.load.massbank.MassBankTandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.database.load.mine.MINEMSPParser;
import edu.umich.med.mrc2.datoolbox.database.load.msdial.LipidBlastRikenParser;
import edu.umich.med.mrc2.datoolbox.database.load.msdial.MSDialMSMSRecord;
import edu.umich.med.mrc2.datoolbox.database.load.msdial.MSDialMetabolomicsLibraryParser;
import edu.umich.med.mrc2.datoolbox.database.load.nist.NISTDataUploader;
import edu.umich.med.mrc2.datoolbox.database.load.nist.NISTMSPParser;
import edu.umich.med.mrc2.datoolbox.database.load.nist.NISTTandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.database.load.pubchem.PubChemFields;
import edu.umich.med.mrc2.datoolbox.database.load.pubchem.PubChemParser;
import edu.umich.med.mrc2.datoolbox.database.thermo.CompoundDiscovererUtils;
import edu.umich.med.mrc2.datoolbox.database.thermo.ThermoSqliteConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTPepSearchOutputFields;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTReferenceLibraries;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsfdr.NISTPepSearchResultManipulator;
import edu.umich.med.mrc2.datoolbox.rawdata.PeakFinder;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.LIMSReportingUtils;
import edu.umich.med.mrc2.datoolbox.utils.MGFUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.NumberArrayUtils;
import edu.umich.med.mrc2.datoolbox.utils.PeptideUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;
import edu.umich.med.mrc2.datoolbox.utils.WebUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.AgilentAcquisitionMethodReportParser;
import edu.umich.med.mrc2.datoolbox.utils.acqmethod.AgilentDevicesParser;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayFilter;
import edu.umich.med.mrc2.datoolbox.utils.filter.sgfilter.SGFilter;
import net.sf.jniinchi.INCHI_RET;

public class RegexTest {

	public static String dataDir = "." + File.separator + "data" + File.separator;
	private static String dbHome = dataDir + "database" + File.separator + "CefAnalyzerDB";
	private static String dbUser = "CefAnalyzer";
	private static String dbPassword = "CefAnalyzer";

	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(builder);
	private static final MDLV2000Reader molReader  = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			compressMoTrPACRawDataFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void compressMoTrPACRawDataFiles() {
		
		List<String>tissueTypes = new ArrayList<String>(
				Arrays.asList(
						"T69 - Adipose brown",
						"T70 - Adipose white",
						"T58 - Heart",
						"T59 - Kidney",
						"T68 - Liver",
						"T66 - Lung",
						"T55 - Muscle",
						"T31 - Plasma"));
		List<String>assayTypes = new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS1AC");	
		File rawBaseDir = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C\\4BIC\\PASS1AC\\RAW_AC");	
		try {
			String batchId = "BATCH1_20210603";	
			String processedFolderId  = "PROCESSED_20210806";
			for(String tissue : tissueTypes) {
							
				for(String assay : assayTypes) {
					
					Path zippedRawFilesDirPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, "RAW");
					Path namedDirPath = Paths.get(parentDirectory.getAbsolutePath(), tissue, assay, batchId, processedFolderId, "NAMED");
					Path sampleInfo = Files.find(namedDirPath, Integer.MAX_VALUE, (p, basicFileAttributes) ->
			                        p.getFileName().toString().startsWith("metadata_sample_")).findFirst().orElse(null);
					if(sampleInfo != null) {
						
						Collection<String>toCompress = new TreeSet<String>();
						Collection<String>processed = new TreeSet<String>();
						TreeMap<File,Long> fileSizeMap = new TreeMap<File,Long>();
						TreeMap<File,String> fileHashMap = new TreeMap<File,String>();
						File manifest = Paths.get(zippedRawFilesDirPath.toString(), "manifest_" + assay + ".txt").toFile();
						File checkSumFile = Paths.get(zippedRawFilesDirPath.toString(), " checksum.txt").toFile();
						FileUtils.copyFile(sampleInfo.toFile(), manifest);						
						String[][] manifestData = null;
						try {
							manifestData = DelimitedTextParser.parseTextFileWithEncoding(manifest, MRC2ToolBoxConfiguration.getTabDelimiter());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (manifestData != null) {
							String[] header = manifestData[0];
							int fileNameColumn = -1;
							for(int i=0; i<header.length; i++) {
								if(header[i].equals("raw_file")) {
									fileNameColumn = i;
									break;
								}
							}							
							for(int i=1; i<manifestData.length; i++) 
								toCompress.add(manifestData[i][fileNameColumn]);
							
							System.out.println(sampleInfo.getFileName().toString());
						}
						Path rawDataDirectory = Paths.get(rawBaseDir.getAbsolutePath(), tissue, assay);
						List<Path> pathList = Files.find(rawDataDirectory,
								1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
							collect(Collectors.toList());
						for(Path rdp : pathList) {
							
							FileUtils.deleteDirectory(Paths.get(rdp.toString(), "Results").toFile());
							String rawFileName = FilenameUtils.getBaseName(rdp.toString());
							if(toCompress.contains(rawFileName)) {
								
								File destination = Paths.get(zippedRawFilesDirPath.toString(),
										FilenameUtils.getBaseName(rdp.toString()) + ".zip").toFile();
								CompressionUtils.zipFolder(rdp.toFile(), destination);
								if(destination.exists()) {
									try {
										String zipHash = DigestUtils.sha256Hex(
												new FileInputStream(destination.getAbsolutePath()));

										fileHashMap.put(destination, zipHash);
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									fileSizeMap.put(destination, destination.length());
									processed.add(rawFileName);
								}								
							}
						}
						@SuppressWarnings({ "unchecked" })
						Collection<String> missing = CollectionUtils.removeAll(toCompress, processed);
						if(!missing.isEmpty()) {
							System.out.println("Missing files in " + zippedRawFilesDirPath.toString());
							for(String m : missing)
								System.out.println(m);
						}					
						//	Write checksum file
						StringBuffer checkSumData = new StringBuffer();
						checkSumData.append("File name\t");
						checkSumData.append("SHA256\t");
						checkSumData.append("Size\n");
						for(Entry<File, String> entry : fileHashMap.entrySet()) {
							
							if(fileSizeMap.get(entry.getKey()) != null) {
								
								checkSumData.append(entry.getKey().getName() +"\t");
								checkSumData.append(entry.getValue() +"\t");
								checkSumData.append(Long.toString(fileSizeMap.get(entry.getKey())) + "\n");
							}
						}
						FileUtils.writeStringToFile(checkSumFile, checkSumData.toString(), Charset.defaultCharset());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private static void createMotrpacDataUploadDirectoryStructure() {
		
		List<String>tissueTypes = new ArrayList<String>(
				Arrays.asList(
						"T69 - Adipose brown",
						"T70 - Adipose white",
						"T58 - Heart",
						"T52 - Hippocampus",
						"T59 - Kidney",
						"T68 - Liver",
						"T66 - Lung",
						"T55 - Muscle",
						"T31 - Plasma"));
		List<String>assayTypes = new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C Shipment ANI870 10082\\4BIC\\PASS1AC");	
		try {
			LIMSReportingUtils.createMotrpacDataUploadDirectoryStructure(
					tissueTypes, 
					assayTypes, 
					parentDirectory,
					1,
					"20210603",
					"20210806");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createManifestsDirectoryStructure() {
		
		List<String>tissueTypes = new ArrayList<String>(
				Arrays.asList(
						"T69 - Adipose brown",
						"T70 - Adipose white",
						"T58 - Heart",
						"T59 - Kidney",
						"T68 - Liver",
						"T66 - Lung",
						"T55 - Muscle",
						"T31 - Plasma"));
		List<String>assayTypes = new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C Shipment ANI870 10082\\4BIC\\PASS1AC\\Manifests");
		for(String tissue : tissueTypes) {
			
			for(String assay : assayTypes) {
				
				try {
					Files.createDirectories(
							Paths.get(parentDirectory.getAbsolutePath(), tissue, assay));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void createRawDataVaultDirectoryStructure() {
		
		List<String>tissueTypes = new ArrayList<String>(
				Arrays.asList(
						"T69 - Adipose brown",
						"T70 - Adipose white",
						"T58 - Heart",
						"T59 - Kidney",
						"T68 - Liver",
						"T66 - Lung",
						"T55 - Muscle",
						"T31 - Plasma"));
		List<String>assayTypes = new ArrayList<String>(Arrays.asList("IONPNEG", "RPNEG", "RPPOS"));
		File parentDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX01117 - PASS 1C Shipment ANI870 10082\\4BIC\\PASS1AC\\RAW_AC");
		for(String tissue : tissueTypes) {
			
			for(String assay : assayTypes) {
				
				try {
					Files.createDirectories(
							Paths.get(parentDirectory.getAbsolutePath(), tissue, assay));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void extractInstrumentData() {
		
		File[] methodDirs = new File("E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20220104").listFiles(File::isDirectory);
		for(File methodDir : methodDirs) {
			
		}
	}
	
	private static void downloadAllAcqMethods() {
		File destination = new File("E:\\DataAnalysis\\METHODS\\Acquisition\\Uploaded\\AS_OF_20220104_2");
		Collection<DataAcquisitionMethod> list = null;
		try {
			list = AcquisitionMethodUtils.getAcquisitionMethodList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(DataAcquisitionMethod method : list) {
			
			if (method.getIonizationType() != null && method.getIonizationType().getId().equals("ESI")) {
				try {
					AcquisitionMethodUtils.getAcquisitionMethodFile(method, destination);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void deviceParserTest() {
		
		File deviceFile = new File(
				"Y:\\DataAnalysis\\_Reports\\EX00602 (L reuteri lipids)\\A003 - Untargeted\\"
				+ "Raw data\\POS\\20160819-EX00602-A003-IN0002-S00023699-P.d\\AcqData\\Devices.xml");
		AgilentDevicesParser.parseDevicesFile(deviceFile);
	}
	
	private static void validateEntropyCalc() {
		
		MsPoint p1 = new MsPoint(41.04, 37.16);
		MsPoint p2 = new MsPoint(69.07, 66.83);
		MsPoint p3 = new MsPoint(86.1, 999.0);
		Collection<MsPoint>spectrum = new TreeSet<MsPoint>(MsUtils.mzSorter);
		spectrum.add(p1);
		spectrum.add(p2);
		spectrum.add(p3);
		
		double entropy = MsUtils.calculateSpectrumEntropyNatLog(spectrum);
		System.err.println(Double.toString(entropy));
	}
	
	private static void testEncodeDecode() {

		double[]x = new double[]{0.0, 0.126933037, 0.253866073, 0.38079911, 0.507732146, 0.634665183, 
				0.761598219, 0.888531256, 1.015464292, 1.142397329, 1.269330365, 
				1.396263402, 1.523196438, 1.650129475, 1.777062511, 1.903995548, 2.030928584, 2.157861621,
				2.284794657, 2.411727694, 2.53866073, 
				2.665593767, 2.792526803, 2.91945984, 3.046392876, 3.173325913, 3.300258949, 3.427191986, 
				3.554125022, 3.681058059, 3.807991095, 
				3.934924132, 4.061857168, 4.188790205, 4.315723241, 4.442656278, 4.569589314, 4.696522351, 
				4.823455387, 4.950388424, 5.07732146, 
				5.204254497, 5.331187533, 5.45812057, 5.585053606, 5.711986643, 5.838919679, 5.965852716, 
				6.092785752, 6.219718789, 6.346651825, 
				6.473584862, 6.600517898, 6.727450935, 6.854383971, 6.981317008, 7.108250044, 7.235183081, 
				7.362116118, 7.489049154, 7.615982191, 
				7.742915227, 7.869848264, 7.9967813, 8.123714337, 8.250647373, 8.37758041, 8.504513446, 
				8.631446483, 8.758379519, 8.885312556, 
				9.012245592, 9.139178629, 9.266111665, 9.393044702, 9.519977738, 9.646910775, 9.773843811, 
				9.900776848, 10.02770988, 10.15464292, 
				10.28157596, 10.40850899, 10.53544203, 10.66237507, 10.7893081, 10.91624114, 11.04317418, 
				11.17010721, 11.29704025, 11.42397329, 
				11.55090632, 11.67783936, 11.8047724, 11.93170543, 12.05863847, 12.1855715, 12.31250454, 
				12.43943758, 12.56637061};
		
		String enc = "";
		try {
			enc = NumberArrayUtils.encodeNumberArray(x);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println(enc);
		double[] decoded = null;		
		try {
			decoded = NumberArrayUtils.decodeNumberArray(enc);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println(x.length);
		System.err.println(decoded.length);
	}
	
	private static void testSgFilterImpl() {
		
		double[]x = new double[]{0.0, 0.126933037, 0.253866073, 0.38079911, 0.507732146, 0.634665183, 
				0.761598219, 0.888531256, 1.015464292, 1.142397329, 1.269330365, 
				1.396263402, 1.523196438, 1.650129475, 1.777062511, 1.903995548, 2.030928584, 2.157861621,
				2.284794657, 2.411727694, 2.53866073, 
				2.665593767, 2.792526803, 2.91945984, 3.046392876, 3.173325913, 3.300258949, 3.427191986, 
				3.554125022, 3.681058059, 3.807991095, 
				3.934924132, 4.061857168, 4.188790205, 4.315723241, 4.442656278, 4.569589314, 4.696522351, 
				4.823455387, 4.950388424, 5.07732146, 
				5.204254497, 5.331187533, 5.45812057, 5.585053606, 5.711986643, 5.838919679, 5.965852716, 
				6.092785752, 6.219718789, 6.346651825, 
				6.473584862, 6.600517898, 6.727450935, 6.854383971, 6.981317008, 7.108250044, 7.235183081, 
				7.362116118, 7.489049154, 7.615982191, 
				7.742915227, 7.869848264, 7.9967813, 8.123714337, 8.250647373, 8.37758041, 8.504513446, 
				8.631446483, 8.758379519, 8.885312556, 
				9.012245592, 9.139178629, 9.266111665, 9.393044702, 9.519977738, 9.646910775, 9.773843811, 
				9.900776848, 10.02770988, 10.15464292, 
				10.28157596, 10.40850899, 10.53544203, 10.66237507, 10.7893081, 10.91624114, 11.04317418, 
				11.17010721, 11.29704025, 11.42397329, 
				11.55090632, 11.67783936, 11.8047724, 11.93170543, 12.05863847, 12.1855715, 12.31250454, 
				12.43943758, 12.56637061};
		
		double[]y = new double[] {
				-0.490570301, 2.536284821, 2.109922579, 3.432542001, 1.923443578, 
				1.699784562, 2.729253684, 4.367166214, 1.499211857, 3.179151687, 
				2.829241617, 0.895607733, -1.528976649, 3.768937599, -1.894263879, 
				-2.690879241, 0.3113558, -2.848725674, -5.32307835, -3.522089608, 
				-4.087501291, -1.4882235, -4.243784082, -2.153299909, -0.9728344, 
				2.985463288, 0.287496245, 0.861370078, 1.402215194, 2.482739626, 
				4.822819791, 1.246663481, 2.030382054, 2.421853505, 2.978549285, 
				2.392880104, 4.848569418, 0.788226172, -1.557310038, 1.601292092, 
				-2.027981737, -7.621533276, -3.234544741, -4.418584766, -4.751026328, 
				-2.429422344, -0.1264004, -1.937823052, -2.069273531, 2.339161412, 
				1.087334155, -4.793833386, 0.523563469, -1.463262425, 4.276701902, 
				4.187878739, -1.390483, 6.169949305, 1.081581438, -0.219102748, 
				5.077521347, 1.274737421, -0.640368931, -2.125510927, -0.164740118, 
				-1.351941121, -1.651215585, -4.102619229, -3.614013049, -4.705534892, 
				-0.847089928, -2.922535638, 0.313855396, -3.641052116, 4.65616623, 
				-1.25709448, 3.519158682, 1.207421768, 3.381560119, 6.650095743, 
				1.37732372, 3.534901892, 5.03269543, 6.470289807, 0.808708184, 
				-1.357149532, -1.289611861, 2.867187554, 0.900398107, -3.919795796, 
				-2.295655831, -2.231584552, -4.040356519, -3.784012738, 0.671670268, 
				-0.782393421, -2.326181592, 0.691850912, -1.786074992, 0.48115285};		

		SGFilter sgFilter = new SGFilter(9, 9); 
		double[] coeffs =SGFilter.computeSGCoefficients(9, 9, 4); 
		double[] smooth = sgFilter.smooth(y, coeffs); 
		
		for(int i=0; i<y.length; i++)
			System.err.println(x[i] + "\t" + y[i] + "\t" + smooth[i]);
		
		PeakFinder pf = new PeakFinder(x,smooth);
		pf.findMinAndMax();
		
		System.err.println("Maxima");
		double[][] maxCoord = pf.getMaximaCoordinates();
		for(int i=0; i<maxCoord[0].length; i++) {
			System.err.println(maxCoord[0][i] + "\t" + maxCoord[1][i]);
		}
	}
	
	private static void testSgFilter() {
		
		SavitzkyGolayFilter f = new SavitzkyGolayFilter(13);		
		double[]y = new double[] {
				-0.490570301, 2.536284821, 2.109922579, 3.432542001, 1.923443578, 
				1.699784562, 2.729253684, 4.367166214, 1.499211857, 3.179151687, 
				2.829241617, 0.895607733, -1.528976649, 3.768937599, -1.894263879, 
				-2.690879241, 0.3113558, -2.848725674, -5.32307835, -3.522089608, 
				-4.087501291, -1.4882235, -4.243784082, -2.153299909, -0.9728344, 
				2.985463288, 0.287496245, 0.861370078, 1.402215194, 2.482739626, 
				4.822819791, 1.246663481, 2.030382054, 2.421853505, 2.978549285, 
				2.392880104, 4.848569418, 0.788226172, -1.557310038, 1.601292092, 
				-2.027981737, -7.621533276, -3.234544741, -4.418584766, -4.751026328, 
				-2.429422344, -0.1264004, -1.937823052, -2.069273531, 2.339161412, 
				1.087334155, -4.793833386, 0.523563469, -1.463262425, 4.276701902, 
				4.187878739, -1.390483, 6.169949305, 1.081581438, -0.219102748, 
				5.077521347, 1.274737421, -0.640368931, -2.125510927, -0.164740118, 
				-1.351941121, -1.651215585, -4.102619229, -3.614013049, -4.705534892, 
				-0.847089928, -2.922535638, 0.313855396, -3.641052116, 4.65616623, 
				-1.25709448, 3.519158682, 1.207421768, 3.381560119, 6.650095743, 
				1.37732372, 3.534901892, 5.03269543, 6.470289807, 0.808708184, 
				-1.357149532, -1.289611861, 2.867187554, 0.900398107, -3.919795796, 
				-2.295655831, -2.231584552, -4.040356519, -3.784012738, 0.671670268, 
				-0.782393421, -2.326181592, 0.691850912, -1.786074992, 0.48115285};
		double[]x = new double[]{0.0, 0.126933037, 0.253866073, 0.38079911, 0.507732146, 0.634665183, 
				0.761598219, 0.888531256, 1.015464292, 1.142397329, 1.269330365, 
				1.396263402, 1.523196438, 1.650129475, 1.777062511, 1.903995548, 2.030928584, 2.157861621,
				2.284794657, 2.411727694, 2.53866073, 
				2.665593767, 2.792526803, 2.91945984, 3.046392876, 3.173325913, 3.300258949, 3.427191986, 
				3.554125022, 3.681058059, 3.807991095, 
				3.934924132, 4.061857168, 4.188790205, 4.315723241, 4.442656278, 4.569589314, 4.696522351, 
				4.823455387, 4.950388424, 5.07732146, 
				5.204254497, 5.331187533, 5.45812057, 5.585053606, 5.711986643, 5.838919679, 5.965852716, 
				6.092785752, 6.219718789, 6.346651825, 
				6.473584862, 6.600517898, 6.727450935, 6.854383971, 6.981317008, 7.108250044, 7.235183081, 
				7.362116118, 7.489049154, 7.615982191, 
				7.742915227, 7.869848264, 7.9967813, 8.123714337, 8.250647373, 8.37758041, 8.504513446, 
				8.631446483, 8.758379519, 8.885312556, 
				9.012245592, 9.139178629, 9.266111665, 9.393044702, 9.519977738, 9.646910775, 9.773843811, 
				9.900776848, 10.02770988, 10.15464292, 
				10.28157596, 10.40850899, 10.53544203, 10.66237507, 10.7893081, 10.91624114, 11.04317418, 
				11.17010721, 11.29704025, 11.42397329, 
				11.55090632, 11.67783936, 11.8047724, 11.93170543, 12.05863847, 12.1855715, 12.31250454, 
				12.43943758, 12.56637061};
		
		double[]yFiltered = f.filter(x, y);
		System.err.println("Length " + y.length + "\t" + yFiltered.length);		
		for(int i=0; i<x.length; i++)
			System.err.println(x[i] + "\t" + y[i] + "\t" + yFiltered[i]);
	}
	
	private static void testGaussianWindow() {

		double[] mz = new double[] {99.5, 99.9, 100, 100.1, 100.5};
		DescriptiveStatistics ds = new DescriptiveStatistics(mz);
		double medMz = ds.getMax() - (ds.getMax() - ds.getMin())/2.0d;		
		double[] mzCentered = new double[mz.length];
		for(int i=0; i<mz.length; i++)
			mzCentered[i] = mz[i] - medMz;
		
		double[] intensity = new double[] {1000.0, 1000.0, 1000.0, 1000.0, 1000.0};	
		double maxIntensity = Arrays.stream(intensity).max().getAsDouble();
		
		double sd = 0.5/4.0d;
		NormalDistribution nd = new NormalDistribution(0.0, sd);
		
		double[] adjIntensity = new double[intensity.length];
		for(int i=0; i<mz.length; i++)
			adjIntensity[i] = nd.density(mzCentered[i]) * intensity[i];
			
		double maxAdjIntensity = Arrays.stream(adjIntensity).max().getAsDouble();
		for(int i=0; i<mz.length; i++) {
			double normInt  = adjIntensity[i] / maxAdjIntensity * maxIntensity;
			System.err.println(mz[i] + "\t" + mzCentered[i] + "\t" + intensity[i] + "\t" +  + adjIntensity[i] + "\t" + normInt);
		}
	}
	
	public static void extractMSMSisolationWindows() throws Exception {
		
		List<File> methodFiles = Files.find(Paths.get("C:\\Users\\Sasha\\Downloads\\MSMSMethods\\AcqReports"), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".xls") && fileAttr.isRegularFile()).
				map(p -> p.toFile()).collect(Collectors.toList());
		
		String mn = "Method Name";
		String iw = "Isolation Width MS/MS";
		
		Map<String,String>isolWindowMap = new TreeMap<String,String>();
		for(File methodFile : methodFiles) {
			
			Map<String,String>paramMap = 
					AgilentAcquisitionMethodReportParser.parseAgilentAcquisitionMethodReportFile(methodFile);
			if(paramMap.get(mn) != null && paramMap.get(iw) != null)
				isolWindowMap.put(paramMap.get(mn), paramMap.get(iw));
			
			System.out.println(methodFile.getName());
		}
		List<String>paramLines = new ArrayList<String>();
		for(Entry<String,String> e : isolWindowMap.entrySet())
			paramLines.add(e.getKey() + "\t" + e.getValue());
					
		Path scriptPath = Paths.get("C:\\Users\\Sasha\\Downloads\\MSMSMethods\\IsolationWindowMap.txt");
	    try {
			Files.write(scriptPath, 
					paramLines, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void getMSMSAcquisitionMethodFiles() throws Exception {

		File destinationFolder = new File("C:\\Users\\Sasha\\Downloads\\MSMSMethods");
		//	Get zip from database
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT METHOD_NAME, METHOD_CONTAINER "
				+ "FROM DATA_ACQUISITION_METHOD "
				+ "WHERE MS_TYPE = 'HRMSMS'";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			File zipFile = 
					Paths.get(destinationFolder.getAbsolutePath(), rs.getString("METHOD_NAME") + ".zip").toFile();
			BufferedInputStream is = new BufferedInputStream(rs.getBinaryStream("METHOD_CONTAINER"));
			FileOutputStream fos = new FileOutputStream(zipFile);
			byte[] buffer = new byte[2048];
			int r = 0;
			while ((r = is.read(buffer)) != -1)
				fos.write(buffer, 0, r);

			fos.flush();
			fos.close();
			is.close();
			
			//	Extract archive and delete zip;
			if(zipFile.exists()) {

	            ZipArchiveInputStream zipStream = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
	            ZipArchiveEntry entry;
	            while ((entry = zipStream.getNextZipEntry()) != null) {

	                if (entry.isDirectory())
	                	continue;

	                File curfile = new File(destinationFolder, entry.getName());
	                File parent = curfile.getParentFile();
	                if (!parent.exists())
	                    parent.mkdirs();

	                fos = new FileOutputStream(curfile);
	                IOUtils.copy(zipStream, fos);
	                fos.close();
	            }
	            zipStream.close();
	    		Path path = Paths.get(zipFile.getAbsolutePath());
	    	    Files.delete(path);
			}
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void generateCorrelationsRScript() throws IOException {
		
		String dirPath = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\RSD-stats-calculation";
		String[]namingList = new String[] {"Unnamed", "Named"};
		ArrayList<String>scriptLines = new ArrayList<String>();
		scriptLines.add("library(tidyverse)");
		scriptLines.add("library(matrixStats)");
		scriptLines.add("library(gridExtra)");
		scriptLines.add("library(ggpubr)");
		scriptLines.add("lm_eqn <- function(df){ m <- lm(Mean ~ RSD, df); ");
		scriptLines.add("r2 = format(summary(m)$r.squared, digits = 3)");
		scriptLines.add(" paste(\"R^2 = \", r2); }");
		scriptLines.add("");	
		scriptLines.add("plotList <- list()");
		scriptLines.add("");
		
		for(String naming : namingList) {
			
			List<File> dataFiles = Files.find(Paths.get(dirPath, naming, "DataRenamed"), 1,
					(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".txt") && fileAttr.isRegularFile()).
					map(p -> p.toFile()).collect(Collectors.toList());
			
			scriptLines.add("#################################################");
			scriptLines.add("# " + naming + "##########");
			scriptLines.add("#################################################");
			int plotCount = 1;
			for(File dataFile : dataFiles) {
				
				String baseName = FilenameUtils.getBaseName(dataFile.getName()).replace("_", " ");
				String plotId = "p"+ Integer.toString(plotCount);
				String tableName = "stats.pools.table"+ Integer.toString(plotCount);
				
				scriptLines.add("# " + baseName + "##########");
				scriptLines.add("metab.data <- read.delim(\"" + 
						dataFile.getAbsolutePath().replace("\\", "/") + "\", check.names=FALSE)");
				scriptLines.add("metab.data.pools <- select(metab.data, contains(\"CS00000MP\"))");
				scriptLines.add(tableName + " <- metab.data.pools %>% "
						+ "mutate(Mean = rowMeans(metab.data.pools, na.rm = TRUE), "
						+ "Median = rowMedians(as.matrix(metab.data.pools), na.rm = TRUE), "
						+ "stdev=rowSds(as.matrix(metab.data.pools), na.rm = TRUE), "
						+ "repr = rowSums(!is.na(metab.data.pools)) / (rowSums(is.na(metab.data.pools)) + rowSums(!is.na(metab.data.pools)))) %>% "
						+ "mutate(RSD = stdev / Mean * 100) %>% "
						+ "select(c(\"Mean\", \"Median\", \"stdev\", \"RSD\", \"repr\")) %>% filter(Median < 7000)");
				
//				scriptLines.add(tableName + " <- cbind(metab.data$metabolite_name, stats.pools)");
//				scriptLines.add("colnames(" + tableName + ")[1] <- \"metabolite_name\"");
				
				scriptLines.add(plotId + " <- ggplot(data = " + tableName + ", aes(x=Median,y=RSD)) + ");
				scriptLines.add("geom_point(alpha=0.5) + labs(x= \"Median area\", y=\"RSD%\") + ");
				scriptLines.add("geom_smooth() + ylim(0,60) + ");
				scriptLines.add("geom_text(x = 50000, y = 50, label = lm_eqn(" + tableName + "), parse = F) + ");
				scriptLines.add("ggtitle(\"" + baseName + "\")");
				scriptLines.add("plotList[[" + Integer.toString(plotCount) + "]] <- " + plotId);							
				scriptLines.add("");
				plotCount++;
			}
			//	Plots to PDF
			String pdfPath = Paths.get(dirPath, naming, "DataRenamed", 
					"EX00979 RSD to peak area correlation plots for areas below 100K (" + naming + " data).pdf").toString().replace("\\", "/");
			scriptLines.add("multi.page <- ggarrange(plotlist = plotList, nrow=3, ncol=1)");
			scriptLines.add("ggexport(multi.page, filename=\"" + pdfPath + "\", height=11, width=8.5)");
			scriptLines.add("");
		}
		Path scriptPath = Paths.get(
				dirPath, "PASS1B_CorrelationSummariesForAreasBelow7K.R");
	    try {
			Files.write(scriptPath, 
					scriptLines, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void generateRsdScript() throws IOException {
		
		String dirPath = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\RSD-stats-calculation";
		String[]namingList = new String[] {"Unnamed", "Named"};
		ArrayList<String>scriptLines = new ArrayList<String>();
		scriptLines.add("library(tidyverse)");
		scriptLines.add("library(matrixStats)");
		scriptLines.add("library(gridExtra)");
		scriptLines.add("");
	
		for(String naming : namingList) {
			
			List<File> dataFiles = Files.find(Paths.get(dirPath, naming), 1,
					(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".txt") && fileAttr.isRegularFile()).
					map(p -> p.toFile()).collect(Collectors.toList());
			
			scriptLines.add("#################################################");
			scriptLines.add("# " + naming + "##########");
			scriptLines.add("#################################################");
			for(File dataFile : dataFiles) {
				
				String baseName = FilenameUtils.getBaseName(dataFile.getName());
				scriptLines.add("# " + baseName + "##########");
				scriptLines.add("metab.data <- read.delim(\"" + 
						dataFile.getAbsolutePath().replace("\\", "/") + "\", check.names=FALSE)");
				scriptLines.add("metab.data.pools <- select(metab.data, contains(\"CS00000MP\"))");
				scriptLines.add("stats.pools <- metab.data.pools %>% "
								+ "mutate(Mean = rowMeans(metab.data.pools, na.rm = TRUE), "
								+ "stdev=rowSds(as.matrix(metab.data.pools), na.rm = TRUE)) %>% "
								+ "mutate(RSD = stdev / Mean * 100) %>% "
								+ "select(c(\"Mean\", \"stdev\", \"RSD\"))");
				scriptLines.add("stats.pools.table <- cbind(metab.data$metabolite_name, stats.pools)");
				scriptLines.add("colnames(stats.pools.table)[1] <- \"metabolite_name\"");
				
//				String tableFilePath = Paths.get(
//						dataFile.getParent(), "Stats", 
//						FilenameUtils.getBaseName(dataFile.getName()) + 
//						"_STATS.txt").toString().replace("\\", "/");
//				scriptLines.add("write.table(stats.pools.table, file = \"" + 
//						tableFilePath + "\", quote = F, sep = \"\\t\", na = \"\", row.names = F)");
				
				scriptLines.add("stats.pools$cut <- cut(stats.pools$RSD, seq(0,max(stats.pools$RSD, na.rm = T), by=5))");
//				scriptLines.add("rsdPlot <- ggplot(stats.pools, aes(cut)) + "
//						+ "geom_histogram(stat = \"count\", aes(y = stat(count) / sum(count), fill = ..count..)) + "
//						+ "labs(x = \"RSD%\", y = \"Frequency\") + "
//						+ "theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust=1)) + "
//						+ "ggtitle(\"RSD% distribution for " + FilenameUtils.getBaseName(dataFile.getName()) + "\")");
				scriptLines.add("sumStats <- stats.pools %>% "
						+ "group_by(Range=cut) %>% "
						+ "summarise(Counts= n()) %>% "
						+ "mutate(Frequency = Counts/sum(Counts)*100) %>% "
						+ "arrange(as.numeric(Range))");
				
//				String pdfFilePath = Paths.get(
//						dataFile.getParent(), "Graphics", 
//						FilenameUtils.getBaseName(dataFile.getName()) + 
//						"_STATS.pdf").toString().replace("\\", "/");
//				scriptLines.add("pdf(\"" + pdfFilePath + "\", height=11, width=8.5)");						
//				scriptLines.add("grid.table(sumStats, rows = NULL)");
//				scriptLines.add("rsdPlot");	
//				scriptLines.add("dev.off()");
				String summaryTableFilePath = Paths.get(
						dataFile.getParent(), "Summaries", 
						FilenameUtils.getBaseName(dataFile.getName()) + 
						"_SUMMARY.txt").toString().replace("\\", "/");
				scriptLines.add("write.table(sumStats, file = \"" + 
						summaryTableFilePath + "\", quote = F, sep = \"\\t\", na = \"\", row.names = F)");
				scriptLines.add("");
			}
		}
		Path scriptPath = Paths.get(
				dirPath, "PASS1B_RSDSummaries.R");
	    try {
			Files.write(scriptPath, 
					scriptLines, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void batchRunPassatuttoBPA() {
		
		String passatuttoPath = "E:\\Eclipse\\git2\\MRC2Toolbox\\data\\passatutto\\*";
		String searchResultsPath = "E:\\DataAnalysis\\MSMS\\DecoyDB\\Evaluation";
		String libResults = "Results\\Library";
		String ebaDir = "PassatuttoEBA";
		String[] modes = new String[] {
				"NEG",
				"POS",
			};
		String[] experiments = new String[] {
				"EX00663-",
				"EX00884-",
				"EX00930-MOTRPAC-Plasma-",
				"EX00930-MOTRPAC-liver-",
				"EX00930-MOTRPAC-muscle-",
				"EX00930-MOTRPAC-white-adipose-",
				"EX00953-MOTRPAC-Hippocampus-",
				"EX00953-MOTRPAC-Kidney-",
				"EX00953-MOTRPAC-Lung-",
				"EX00953-MOTRPAC-brown-adipose-",
				"EX00953-MOTRPAC-heart-",
				"EX00979-MOTRPAC-Hippocampus-",
				"EX00979-MOTRPAC-Kidney-",
				"EX00979-MOTRPAC-Liver-",
				"EX00979-MOTRPAC-Lung-",
				"EX00979-MOTRPAC-Muscle-",
				"EX00979-MOTRPAC-brown-adipose-",
				"EX00979-MOTRPAC-heart-",
				"EX00979-MOTRPAC-white-adipose-",
		};
		Map<String,String> searchModes = new HashMap<String,String>();
		searchModes.put("1_Normal", "");
//		searchModes.put("2_InSource", "_IN_SOURCE");
		for(String mode : modes) {
			
			for(String experiment : experiments) {
				
				for(Entry<String, String> searchMode : searchModes.entrySet()) {
					
					String libResFileName = 
							experiment + mode + searchMode.getValue() + "_PEPSEARCH_RESULTS.TXT";
					File librarySearchResult = Paths.get(
							searchResultsPath, 
							mode, 
							libResults, 
							searchMode.getKey(),
							libResFileName).toFile();
					
					System.out.println("Processing " + librarySearchResult.getName());
					
					//	Write Passatutto input file sorted by score descending
					String passatuttoInputFileName = 
							experiment + mode + searchMode.getValue() + "_PASSATUTTO_INPUT.TXT";
					File passatuttoInputFile = Paths.get(
							searchResultsPath, 
							mode, 
							libResults, 
							searchMode.getKey(),
							ebaDir,
							passatuttoInputFileName).toFile();
					
					NISTPepSearchResultManipulator.createPassatuttoEBAinputFile(
							librarySearchResult, passatuttoInputFile, true);

					String passatuttoOutputFileName = 
							experiment + mode + searchMode.getValue() + "_PASSATUTTO_OUTPUT.TXT";		
					File passatuttoOutputFile = Paths.get(
							searchResultsPath, 
							mode, 
							libResults, 
							searchMode.getKey(),
							ebaDir,
							passatuttoOutputFileName).toFile();
					
					 ProcessBuilder pb =
							   new ProcessBuilder(
									   "java", 
									   "-cp", 
									   passatuttoPath,
									   "QValueEstimator",
									   "-target",
									   passatuttoInputFile.getAbsolutePath(),
									   "-out",
									   passatuttoOutputFile.getAbsolutePath(),
									   "-method",
									   "EBA");
//					 System.out.println(StringUtils.join(pb.command(), " "));
					 
					try {
						Process p = pb.start();
						int exitCode = p.waitFor();
						if (exitCode == 0) {
							p.destroy();
						} else {
							System.out.println("Passatutto run failed for " + passatuttoInputFile.getName());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("Created " + passatuttoOutputFile.getName());
				}
			}
		}
	}
	
	private static void batchRunPercollator() {
		
		String searchResultsPath = "Y:\\Sasha\\MSMS-decoys\\Evaluation";
		String libResults = "Results\\Library";
		String decoyResults = "ResultsNew";
		String percollatorResults = "ResultsNew\\Percollator";
		String[] modes = new String[] {
				"NEG",
				"POS",
			};
		String[] experiments = new String[] {
				"EX00663-",
				"EX00884-",
				"EX00930-MOTRPAC-Plasma-",
				"EX00930-MOTRPAC-liver-",
				"EX00930-MOTRPAC-muscle-",
				"EX00930-MOTRPAC-white-adipose-",
				"EX00953-MOTRPAC-Hippocampus-",
				"EX00953-MOTRPAC-Kidney-",
				"EX00953-MOTRPAC-Lung-",
				"EX00953-MOTRPAC-brown-adipose-",
				"EX00953-MOTRPAC-heart-",
				"EX00979-MOTRPAC-Hippocampus-",
				"EX00979-MOTRPAC-Kidney-",
				"EX00979-MOTRPAC-Liver-",
				"EX00979-MOTRPAC-Lung-",
				"EX00979-MOTRPAC-Muscle-",
				"EX00979-MOTRPAC-brown-adipose-",
				"EX00979-MOTRPAC-heart-",
				"EX00979-MOTRPAC-white-adipose-",
		};
		Map<String,String> searchModes = new HashMap<String,String>();
		searchModes.put("1_Normal", "");
		searchModes.put("2_InSource", "_IN_SOURCE");

		String libType = "0_Library";
		Map<String,String> decoyTypes = new HashMap<String,String>();
		decoyTypes.put(libType, "_PEPSEARCH_RESULTS.TXT");
		decoyTypes.put("1_Decoy_PassatuttoTreeReroot", "-PASSATUTTO_TREE_REROOT_DECOY");
		decoyTypes.put("2_Decoy_PassatuttoConditional", "-PASSATUTTO_CONDITIONAL_DECOY");
		decoyTypes.put("3_Decoy_PassatuttoRandom", "-PASSATUTTO_RANDOM_DECOY");
		decoyTypes.put("4_Decoy_XYMeta", "-XYMeta_DECOY");

		for(String mode : modes) {
			
			for(String experiment : experiments) {
				
				for(Entry<String, String> searchMode : searchModes.entrySet()) {				
					
					for(Entry<String, String> decoyType : decoyTypes.entrySet()) {
						
						if(decoyType.getKey().equals(libType))
							continue;

						String pinFileName = 
								experiment + mode + decoyType.getValue() + searchMode.getValue() + 
								decoyTypes.get(libType).replace(".TXT", "_MERGED.pin");
						File percolatorInputFile = Paths.get(
								searchResultsPath, 
								mode, 
								percollatorResults, 
								decoyType.getKey(),
								searchMode.getKey(),
								pinFileName).toFile();

						if(percolatorInputFile != null && percolatorInputFile.exists()) {
							
							System.out.println("Processing " + percolatorInputFile.getName());
							
							File percolatorResultFile = Paths.get(percolatorInputFile.getParentFile().getAbsolutePath(), 
									FilenameUtils.getBaseName(percolatorInputFile.getName()) + "_percolator_psms.tsv").toFile();
							File percolatorDecoyResultFile = Paths.get(percolatorInputFile.getParentFile().getAbsolutePath(), 
									FilenameUtils.getBaseName(percolatorInputFile.getName()) + "_percolator_decoy_psms.tsv").toFile();
							File percolatorLogFile = Paths.get(percolatorInputFile.getParentFile().getAbsolutePath(), 
									FilenameUtils.getBaseName(percolatorInputFile.getName()) + "_percolator_log.txt").toFile();
							
							 ProcessBuilder pb =
									   new ProcessBuilder(MRC2ToolBoxConfiguration.getPercolatorBinaryPath(), 
											   "--results-psms", percolatorResultFile.getAbsolutePath(),
											   "--decoy-results-psms", percolatorDecoyResultFile.getAbsolutePath(),
											   "--only-psms",
											   "--post-processing-tdc",
											   "--num-threads", "6",
											   percolatorInputFile.getAbsolutePath());
							try {
								pb.redirectErrorStream(true);
								pb.redirectOutput(Redirect.appendTo(percolatorLogFile));
								Process p = pb.start();
								assert pb.redirectInput() == Redirect.PIPE;
								assert pb.redirectOutput().file() == percolatorLogFile;
								assert p.getInputStream().read() == -1;
								int exitCode = p.waitFor();
								if (exitCode == 0) {
									p.destroy();
								} else {
									System.out.println("Percolator run failed for " + percolatorInputFile.getName());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}		 
						}
						else {
							System.out.println("!!! File " + percolatorInputFile.getName() + " was not found.");
						}
						System.out.println("***");
					}
				}
			}
		}
	}
	
	private static void parsePepSearchResultsForPercollator() {
		
		String searchResultsPath = "E:\\DataAnalysis\\MSMS\\DecoyDB\\Evaluation";
		String libResults = "Results\\Library";
		String decoyResults = "ResultsNew";
		String percollatorResults = "ResultsNew\\Percollator2";
		String[] modes = new String[] {
				"NEG",
				"POS",
			};
		String[] experiments = new String[] {
				"EX00663-",
				"EX00884-",
				"EX00930-MOTRPAC-Plasma-",
				"EX00930-MOTRPAC-liver-",
				"EX00930-MOTRPAC-muscle-",
				"EX00930-MOTRPAC-white-adipose-",
				"EX00953-MOTRPAC-Hippocampus-",
				"EX00953-MOTRPAC-Kidney-",
				"EX00953-MOTRPAC-Lung-",
				"EX00953-MOTRPAC-brown-adipose-",
				"EX00953-MOTRPAC-heart-",
				"EX00979-MOTRPAC-Hippocampus-",
				"EX00979-MOTRPAC-Kidney-",
				"EX00979-MOTRPAC-Liver-",
				"EX00979-MOTRPAC-Lung-",
				"EX00979-MOTRPAC-Muscle-",
				"EX00979-MOTRPAC-brown-adipose-",
				"EX00979-MOTRPAC-heart-",
				"EX00979-MOTRPAC-white-adipose-",
		};
		Map<String,String> searchModes = new HashMap<String,String>();
		searchModes.put("1_Normal", "");
		//searchModes.put("2_InSource", "_IN_SOURCE");

		String libType = "0_Library";
		Map<String,String> decoyTypes = new HashMap<String,String>();
		decoyTypes.put(libType, "_PEPSEARCH_RESULTS.TXT");
		decoyTypes.put("1_Decoy_PassatuttoTreeReroot", "-PASSATUTTO_TREE_REROOT_DECOY");
		decoyTypes.put("2_Decoy_PassatuttoConditional", "-PASSATUTTO_CONDITIONAL_DECOY");
		decoyTypes.put("3_Decoy_PassatuttoRandom", "-PASSATUTTO_RANDOM_DECOY");
		decoyTypes.put("4_Decoy_XYMeta", "-XYMeta_DECOY");

		for(String mode : modes) {
			
			for(String experiment : experiments) {
				
				for(Entry<String, String> searchMode : searchModes.entrySet()) {
					
					String libResFileName = 
							experiment + mode + searchMode.getValue() + decoyTypes.get(libType);
					File librarySearchResult = Paths.get(
							searchResultsPath, 
							mode, 
							libResults, 
							searchMode.getKey(),
							libResFileName).toFile();
					
					if(!librarySearchResult.exists())
						System.out.println("Wrong name!!! " + librarySearchResult.getAbsolutePath());
					
					for(Entry<String, String> decoyType : decoyTypes.entrySet()) {
						
						if(decoyType.getKey().equals(libType))
							continue;

						String decoyResFileName = 
								experiment + mode + decoyType.getValue() + searchMode.getValue() + decoyTypes.get(libType);
						File decoySearchResult = Paths.get(
								searchResultsPath, 
								mode, 
								decoyResults, 
								decoyType.getKey(),
								searchMode.getKey(),
								decoyResFileName).toFile();
						
						if(!decoySearchResult.exists())
							System.out.println("Wrong name!!! " + decoySearchResult.getAbsolutePath());
						
						File percolatorOutputDirectory = Paths.get(
							searchResultsPath, 
							mode, 
							percollatorResults, 
							decoyType.getKey(),
							searchMode.getKey()).toFile();
						
						NISTPepSearchResultManipulator.evaluateDecoyLibrary(
								librarySearchResult, decoySearchResult, percolatorOutputDirectory);
						
//						String mergedFileName = 
//								decoySearchResult.getName().replace(".TXT", "_MERGED.TXT");
//						File mergeResult = Paths.get(
//								searchResultsPath, 
//								mode, 
//								percollatorResults, 
//								decoyType.getKey(),
//								searchMode.getKey(),
//								mergedFileName).toFile();
//						
//						NISTPepSearchResultManipulator.mergeTargetDecoySearchResultsForPercolator(
//								librarySearchResult, 
//								decoySearchResult,
//								MergeType.BEST_OVERALL,
//								mergeResult);
//						
//						String pinFileName = 
//								decoySearchResult.getName().replace(".TXT", "_MERGED.pin");
//						File pinFile = Paths.get(
//								searchResultsPath, 
//								mode, 
//								percollatorResults, 
//								decoyType.getKey(),
//								searchMode.getKey(),
//								pinFileName).toFile();
//						try {
//							NISTPepSearchResultManipulator.convertMergedResultFileToPinFormat(mergeResult, pinFile);
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						//	mergeResult.delete();
						System.gc();
						System.out.println("***");
					}
				}
			}
		}
	}
	
	private static void parseNistCompoundDataForDereplicator() throws Exception {
		
		ArrayList<String>libInfoLines = new ArrayList<String>();
		ArrayList<String>smiles = new ArrayList<String>();
		String query = 
				"SELECT DISTINCT C.ACCESSION, C.PRIMARY_NAME, C.MOL_FORMULA, C.EXACT_MASS, C.SMILES  " +
				"FROM COMPOUND_DATA C, " +
				"REF_MSMS_LIBRARY_COMPONENT L " +
				"WHERE L.LIBRARY_NAME = 'hr_msms_nist' " +
				"AND L.ACCESSION = C.ACCESSION "
				+ "AND C.SMILES IS NOT NULL";
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs  = ps.executeQuery();
		while(rs.next()){
			
			String accession = rs.getString("ACCESSION");
			String cleanAccession = accession.replaceAll("\\W+", "_");
			smiles.add(rs.getString("SMILES") + "\t" + cleanAccession);
			
			ArrayList<String>libInfoLineParts = new ArrayList<String>();
			libInfoLineParts.add("mols/" + cleanAccession + ".mol");			
			String molName = rs.getString("PRIMARY_NAME");
			libInfoLineParts.add(molName.replaceAll("\\s+", "_").replaceAll("\\W+", "_"));
			libInfoLineParts.add(MRC2ToolBoxConfiguration.getMzFormat().format(rs.getDouble("EXACT_MASS")));
			libInfoLineParts.add("1");
			libInfoLineParts.add("NIST");
			libInfoLines.add(StringUtils.join(libInfoLineParts, " "));			
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		Path libInfoPath = Paths.get("E:\\DataAnalysis\\MSMS\\_TOOLS\\DEREPLICATOR-PLUS\\NIST_LIB_BABEL\\library.info");
	    try {
			Files.write(libInfoPath, 
					libInfoLines, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Path smilesPath = Paths.get("E:\\DataAnalysis\\MSMS\\_TOOLS\\DEREPLICATOR-PLUS\\NIST_LIB_BABEL\\library.smiles");
	    try {
			Files.write(smilesPath, 
					smiles, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void splitMolFileForDereplicator() throws IOException {
		
		Path molDirPath = Paths.get("E:\\DataAnalysis\\MSMS\\_TOOLS\\DEREPLICATOR-PLUS\\NIST_LIB_BABEL\\mols");
		Path inputPath = Paths.get("E:\\DataAnalysis\\MSMS\\_TOOLS\\DEREPLICATOR-PLUS\\NIST_LIB_BABEL\\library_all_mols3.mol");
		
		List<String> molText = Files.readAllLines(inputPath);
		List<String> chunk = new ArrayList<String>();
		for(int i=0; i<molText.size(); i++) {
			
			if(molText.get(i).trim().equals("$$$$")) {
				
				Path molPath = Paths.get(molDirPath.toString(), chunk.get(0) + ".mol");
			    try {
					Files.write(molPath, 
							chunk, 
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.APPEND);
				} catch (IOException e) {
					e.printStackTrace();
				}
				chunk = new ArrayList<String>();
			}
			else {
				chunk.add(molText.get(i));
			}
		}	
		if(!chunk.isEmpty()) {
			Path molPath = Paths.get(molDirPath.toString(), chunk.get(0) + ".mol");
		    try {
				Files.write(molPath, 
						chunk, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void parseNistForDereplicator() throws IOException {
		
		File nist17SdfDir = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST17-export\\Export\\MSMS\\SDF_NORM");
		File nist20SdfDir = new File("E:\\DataAnalysis\\Databases\\NIST\\NIST20-export\\EXPORT\\SDF_NORM");
		Path molDirPath = Paths.get("E:\\DataAnalysis\\MSMS\\_TOOLS\\DEREPLICATOR-PLUS\\NIST_LIB2\\mols"); 
		Path libInfoPath = Paths.get("E:\\DataAnalysis\\MSMS\\_TOOLS\\DEREPLICATOR-PLUS\\NIST_LIB2\\library.info");
		
		List<File> sdfFiles = Files.find(Paths.get(nist17SdfDir.getAbsolutePath()), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".sdf") && fileAttr.isRegularFile()).
				map(p -> p.toFile()).collect(Collectors.toList());
		List<File> sdfFiles2 = Files.find(Paths.get(nist20SdfDir.getAbsolutePath()), 1,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".sdf") && fileAttr.isRegularFile()).
				map(p -> p.toFile()).collect(Collectors.toList());
		sdfFiles.addAll(sdfFiles2);
		ArrayList<String>libInfoLines = new ArrayList<String>();
		ArrayList<String>molErrorLog = new ArrayList<String>();
		Map<String,IAtomContainer>keyMolMap = new HashMap<String,IAtomContainer>();		
		for(File sdfFile : sdfFiles) {
			
			Map<String,IAtomContainer>submapMap = new HashMap<String,IAtomContainer>();
			try {
				submapMap = NISTDataUploader.createInChiKeyMolMapFromSdfOnly(sdfFile, molErrorLog);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			keyMolMap.putAll(submapMap);
			System.out.println("Processed " + sdfFile.getName());
		}
		System.out.println("Mapped " + keyMolMap.size() + " unique InChiKeys");
		for(Entry<String, IAtomContainer> keyMol : keyMolMap.entrySet()) {
			
			IAtomContainer molecule = keyMol.getValue();
			
			//	MOL file
			File exportFile = Paths.get(molDirPath.toString(), keyMol.getKey() + ".mol").toFile();	
			Writer writer = new BufferedWriter(new FileWriter(exportFile));
			//	SDFWriter sdfWriter = new SDFWriter(writer);		
			MDLV3000Writer molWriter = new MDLV3000Writer(writer);
	        try {
	        	molWriter.write(molecule);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer.flush();
			writer.close();
			molWriter.close();
			
			//	LibInfo line			
			IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molecule);
			double exactMass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);
			ArrayList<String>libInfoLineParts = new ArrayList<String>();
			libInfoLineParts.add("mols/" + keyMol.getKey() + ".mol");			
			String molName = molecule.getProperty(CDKConstants.TITLE);
			libInfoLineParts.add(molName.replaceAll("\\s+", "_").replaceAll("\\W+", "_"));
			libInfoLineParts.add(MRC2ToolBoxConfiguration.getMzFormat().format(exactMass));
			libInfoLineParts.add("1");
			libInfoLineParts.add("NIST");
			libInfoLines.add(StringUtils.join(libInfoLineParts, " "));
		}		
		//	Write LibInfo file
	    try {
			Files.createFile(libInfoPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			Files.write(libInfoPath, 
					libInfoLines, 
					StandardCharsets.UTF_8,
					StandardOpenOption.WRITE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    //	Write logs
	    Path molErrorPath = Paths.get("Y:\\Sasha\\MSMS-decoys\\4Dereplicator\\NIST_LIB\\molErrors.txt");
	    try {
			Files.createFile(molErrorPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			Files.write(molErrorPath, 
					molErrorLog, 
					StandardCharsets.UTF_8,
					StandardOpenOption.WRITE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void createRoboCopyScript() {
		
		List<File>expDirs = new ArrayList<File>();
		try {
			expDirs = Files.list(Paths.get("Y:\\DataAnalysis\\_Reports"))
			        .map(Path::toFile).filter(f -> f.getName().startsWith("EX"))
			        .collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String>commands = new ArrayList<String>();
		for(File expDir : expDirs) {
			
			String command = "robocopy \"" + expDir.getAbsolutePath() + "\\ \"" + 
			"\"R:\\Metabolomics-BRCF\\Shared\\_Reports\\" + expDir.getName() + " \" /mir /mt:16 /tbd /r:1 /w:3 /fft /np";
			commands.add(command);
		}
		Path mspOutputPath = Paths.get("Y:\\DataAnalysis\\_Reports\\robocopy.bat");
	    try {
			Files.createFile(mspOutputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			Files.write(mspOutputPath, 
					commands, 
					StandardCharsets.UTF_8,
					StandardOpenOption.WRITE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private static void parseOldPassatuttoDecoys() {

		File inputFile = new File("E:\\DataAnalysis\\MSMS\\DecoyDB\\Passatutto\\_OLD\\NIST_POS_DECOY_PASSATUTTO_CONDITIONAL.MS");
		List<List<String>> chunks = MassBankFileParser.parseLargeInputMassBankFile(inputFile);
		System.out.println("Parsed " + chunks.size() + " spectra");
		Polarity polarity = Polarity.Positive;
		Path mspOutputPath = Paths.get("E:\\DataAnalysis\\MSMS\\DecoyDB\\Passatutto\\_OLD\\NIST_POS_DECOY_PASSATUTTO_CONDITIONAL.MSP");
	    try {
			Files.createFile(mspOutputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(List<String>chunk : chunks) {
			
			List<String> mspEntry = passatuttoDecoyToMSP(chunk, polarity);
		    try {
				Files.write(mspOutputPath, 
						mspEntry, 
						StandardCharsets.UTF_8,
						StandardOpenOption.WRITE, 
						StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}  
		}		
	}
	
	private static List<String> passatuttoDecoyToMSP(List<String>chunk, Polarity polarity) {
		
		MassBankTandemMassSpectrum tandemMs = MassBankFileParser.parseMassBankDataSource(chunk);
		List<String>msp = new ArrayList<String>();
		
		msp.add(MSPField.NAME.getName() + ": " + tandemMs.getId());		
		msp.add(MSPField.ION_MODE.getName() + ": " + polarity.getCode());
		msp.add(MSPField.PRECURSORMZ.getName() + ": " +
			MRC2ToolBoxConfiguration.getMzFormat().format(tandemMs.getParent().getMz()));
		msp.add(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(tandemMs.getSpectrum().size()));
		int pointCount = 0;
		String msms = "";
		for(MsPoint point : tandemMs.getSpectrum()) {

			msms+= MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
				+ " " + intensityFormat.format(point.getIntensity()) + "; ";
			pointCount++;
			if(pointCount % 5 == 0) {
				msp.add(msms);
				msms = "";
			}
		}
		msp.add("");
		return msp;
	}
	
	private static void parseOldPassatuttoDecoysOld() {
		
		
		Collection<File> decoys = new ArrayList<File>();
		try {
			//	"E:\\DataAnalysis\\MSMS\\DecoyDB\\Passatutto\\_OLD\\NIST_NEG"
			//	"Y:\\Sasha\\MSMS-decoys\\Passatutto-old\\NIST_NEG_DECOY_CONDITIONAL"
			decoys = Files.list(Paths.get("Y:\\Sasha\\MSMS-decoys\\Passatutto-old\\NIST_NEG_DECOY_CONDITIONAL"))
                        .map(Path::toFile).filter(f -> f.getName().endsWith(".txt"))
                        .collect(Collectors.toList());
        } catch (IOException e) {
            // Error while reading the directory
        }	
		Collection<MassBankTandemMassSpectrum>spectra = 
				new ArrayList<MassBankTandemMassSpectrum>();
		for(File decoy : decoys ) {			
			List<List<String>> parts = MassBankFileParser.parseInputMassBankFile(decoy);
			for(List<String>part : parts) {
				MassBankTandemMassSpectrum msms = MassBankFileParser.parseMassBankDataSource(part);
				msms.setId(FileNameUtils.getBaseName(decoy.getName()));
				spectra.add(msms);
			}
		}
		System.out.println("Parsed " + spectra.size() + " spectra");
		String polarity = "N";
		File exportFile = new File("E:\\DataAnalysis\\MSMS\\DecoyDB\\Passatutto\\_OLD\\NIST_NEG_DECOY_CONDITIONAL.MSP");
		try {
			final Writer writer = new BufferedWriter(new FileWriter(exportFile));
			for(TandemMassSpectrum tandemMs : spectra) {

				writer.append(MSPField.NAME.getName() + ": " + tandemMs.getId() + "\n");		
				writer.append(MSPField.ION_MODE.getName() + ": " + polarity + "\n");
				writer.append(MSPField.PRECURSORMZ.getName() + ": " +
					MRC2ToolBoxConfiguration.getMzFormat().format(tandemMs.getParent().getMz()) + "\n");
				writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(tandemMs.getSpectrum().size()) + "\n");
				int pointCount = 0;
				for(MsPoint point : tandemMs.getSpectrum()) {

					writer.append(
						MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
						+ " " + intensityFormat.format(point.getIntensity()) + "; ") ;
					pointCount++;
					if(pointCount % 5 == 0)
						writer.append("\n");
				}
				writer.append("\n\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void parseXYMetaMGF() {
		
		File inputFile = new File("E:\\DataAnalysis\\MSMS\\_TOOLS\\XY-meta\\XY-Meta-master\\"
				+ "XY-Meta-Win\\database\\Libraries\\NIST17_20_HighRes_MSMS_N_20210916_Decoy.mgf");
		Polarity pol = Polarity.Negative;
		Collection<String[]>mgfBlocks = MGFUtils.getMGFTextBlocksFromFile(inputFile);		
		Collection<TandemMassSpectrum>spectra = new ArrayList<TandemMassSpectrum>();
		for(String[]block : mgfBlocks) {
			
			TandemMassSpectrum spectrum = MGFUtils.parseXYMetaMGFBlock(block, pol);
			spectra.add(spectrum);
		}
		System.out.println("Parsed " + spectra.size() + " spectra");
		try {
			File exportFile = Paths.get(inputFile.getParentFile().getAbsolutePath(), 
					FilenameUtils.getBaseName(inputFile.getName()) + 
					"." + MsLibraryFormat.MSP.getFileExtension()).toFile();
			
			final Writer writer = new BufferedWriter(new FileWriter(exportFile));
			for(TandemMassSpectrum tandemMs : spectra) {

				writer.append(MSPField.NAME.getName() + ": " + tandemMs.getId() + "\n");		
				writer.append(MSPField.ION_MODE.getName() + ": " + pol.getCode() + "\n");
				writer.append(MSPField.PRECURSORMZ.getName() + ": " +
					MRC2ToolBoxConfiguration.getMzFormat().format(tandemMs.getParent().getMz()) + "\n");
				writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(tandemMs.getSpectrum().size()) + "\n");
				int pointCount = 0;
				for(MsPoint point : tandemMs.getSpectrum()) {

					writer.append(
						MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
						+ " " + intensityFormat.format(point.getIntensity()) + "; ") ;
					pointCount++;
					if(pointCount % 5 == 0)
						writer.append("\n");
				}
				writer.append("\n\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void extractPassatuttoDecoys() throws Exception {
		
		File decoyFolder = new File("E:\\DataAnalysis\\MSMS\\_TOOLS\\Passatutto\\container-passatutto-master\\passatutto\\Passatutto\\Data\\_TEST");
		IOFileFilter decoyFileFilter = FileFilterUtils.makeFileOnly(
				new RegexFileFilter(DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}.txt$"));
		Collection<File> decoys = FileUtils.listFilesAndDirs(
				decoyFolder,
				decoyFileFilter,
				null);
		for(File decoy : decoys) {
			
			if(decoy.isDirectory())
				continue;
			
			List<List<String>> records = MassBankFileParser.parseInputMassBankFile(decoy);
			for(List<String>record : records) {
				MassBankTandemMassSpectrum msms = MassBankFileParser.parseMassBankDataSource(record);
				System.out.println(msms.getId());
			}
		}
	}
	
	private static void postgresTes() throws Exception {
		
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:5432/idtracker";
		Properties props = new Properties();
		props.setProperty("user","idtracker");
		props.setProperty("password","&IDTrack3r");
		props.setProperty("currentSchema","idtracker");	
		//	props.setProperty("ssl","true");
		Collection<ChromatographicSeparationType>chromTypes = null;
		try {
			Connection conn = DriverManager.getConnection(url, props);
			chromTypes = AcquisitionMethodUtils.getChromatographicSeparationTypes(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		System.out.println("Connected");
	}
	
	private static void thermoStuff() throws Exception {
		
		File sqliteDatabase = 
				new File("E:\\DataAnalysis\\Thermo\\Studies\\MS1-vs-Agilent-20210615\\2021-05-18_001_neg_QRESS+CH.cdResult");
		
		Connection conn = ThermoSqliteConnectionManager.getConnection(sqliteDatabase);
		ThermoCDStudy study = CompoundDiscovererUtils.parseAnalysisDefinition(conn);
		Collection<ThermoCDWorkflow>workflows = CompoundDiscovererUtils.getAnalysisWorkflows(conn);
		study.getWorkflows().addAll(workflows);
		CompoundDiscovererUtils.mapStudyFilesForWorkflows(study.getWorkflows(), study, conn);
		for(ThermoCDWorkflow workflow : study.getWorkflows())
			CompoundDiscovererUtils.getMsFeaturesForWorkflow(workflow, conn);
				
		ThermoSqliteConnectionManager.releaseConnection(conn);
	}
	
	private static void decodePeakModel() {
		
		String peakData = "UEsDBBQAAAAIABZw0FJLldoRcAIAADEKAAAHAAAAcHBkLnhtbMWV22rbQBBA3wv9B+P3S"
				+ "jM7O3sJikJKm1JIIKSF9lXEm1jUVoKl1Em/vhvZ0c2KJCg0r7Nn9jaHmejkcb2a/XabPL3LjucYwPwk"
				+ "fv8uurz8dOmSXxd3C7eaeSTLjx7zxfF8WRT3R2G43W6DLQV3m9tQAGD48+L82/XSrZN5Bafj8Ic0y4sk"
				+ "u3Zzf+RsFp3eu8er7zEFBg0zWYOoEIWxUbhfqrCvWeGyPC2eYiEkaRsAKwKwVli1o2uiTDp3N0W5t1aWl"
				+ "DEkULBmjsL9SkV9THK3SjMXS6IAmaTWhNZfQ6odXAFlylV6u9ztbJX0EBoymhilicKXtRqscpEDzYhG+nu"
				+ "QUlLSHm/v/nl9v0zy9I+Lb5JV7qKwDpTrZ2lRuMXZQ3Zd+ALGX5KHPI/CTrRBvvK91WIDHf7iLtNI/J"
				+ "EuimUMAaBVWgJbhRKlZcUvaTuikXKaP63Xrtg8xfDC1KGSu3CbW7d4djIvAz7UdHQfa0nEgMBaWEkMAt"
				+ "oSVWj9Am+F4kBYMAAKtKFekcrESibjy6wYtWGNhGRbNlVoVVKNASqlmRCs0MDUI1SZVUtljDdQCQPCGi0"
				+ "6TtXs0BGHWpV5I2qVzGS9GvQrn99SrIEPF6BPs0ZyrZoEq4QAkiyNMHhoWiNr1DZPhj12jRpnpBVKWgn+4+W"
				+ "YbwbZBoQsfR9CK/y1J/gGzL55gRS+xKrTvQ5962syY8INd7Ee46Z1sv+gXOf3x4Xrq8BU4Qi1JNBshX+69We/jXFD"
				+ "g7LHuWnDsu3c0MA8VG7S0Oz2OOVpi/Q8MHy30KPK9R3yJsoND9Je6aYO094uNz5Q/1m6KGyP2g7yF1BLAQIUABQAAAAI"
				+ "ABZw0FJLldoRcAIAADEKAAAHAAAAAAAAAAAAAAAAAAAAAABwcGQueG1sUEsFBgAAAAABAAEANQAAAJUCAAAAAA==";
		
		
		byte[] decodedmz = Base64.decodeBase64(peakData);
//		//	byte[] decodedmz = Base64.getDecoder().decode(peakData);
		ByteBuffer byteBuf = ByteBuffer.wrap(decodedmz);
		byteBuf.order( ByteOrder.LITTLE_ENDIAN );
		FloatBuffer dbuff = byteBuf.asFloatBuffer();
		float[] mzValues = new float[dbuff.remaining()];
		dbuff.get(mzValues);
		
//		String decodedString = new String(Base64.decodeBase64(peakData.getBytes()));
		
		System.out.print("Decoded");
	}
	
	private static void mapMs2IDsToMS1Ids() throws Exception {
		
		Path ms1idList = Paths.get("E:\\DataAnalysis\\MSMS\\DecoyDB\\LibAndDecoySearch\\_MultipleHits\\MS1_ID_LIST.txt");
		List<String> ms2list = Files.readAllLines(ms1idList);	
		ArrayList<String>mapping = new ArrayList<String>();
		Connection conn = ConnectionManager.getConnection();		 
		String sql = "select PARENT_FEATURE_ID from MSMS_FEATURE where MSMS_FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		for(String ms2id : ms2list) {
			ps.setString(1, ms2id);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				String ms1id = rs.getString("PARENT_FEATURE_ID");
				mapping.add(ms2id + "\t" + ms1id);
				break;
			}
			rs.close();
		}	
		ps.close();
		Path ms1ms2map = Paths.get("E:\\DataAnalysis\\MSMS\\DecoyDB\\LibAndDecoySearch\\_MultipleHits\\MS1_MS2_ID_MAP.txt");
		Files.write(ms1ms2map, mapping);
	}
	
	private static void calculateNormalizedMSMSIntensityStandardDeviation(Polarity polarity) throws Exception {
		
		System.out.println("Calculating standard deviations for normalized MSMS intensities for " + polarity.name() + " mode.");
		Connection conn = ConnectionManager.getConnection();
		 
		String sql = "SELECT DISTINCT F2.MSMS_FEATURE_ID " +
				"FROM MSMS_PARENT_FEATURE F, DATA_ANALYSIS_MAP M, DATA_ACQUISITION_METHOD A,  " +
				"INJECTION I, PREPARED_SAMPLE P, SAMPLE S, STOCK_SAMPLE T, MSMS_FEATURE F2  " +
				"WHERE F.DATA_ANALYSIS_ID = M.DATA_ANALYSIS_ID  " +
				"AND  F2.PARENT_FEATURE_ID = F.FEATURE_ID  " +
				"AND M.INJECTION_ID = I.INJECTION_ID  " +
				"AND A.ACQ_METHOD_ID = I.ACQUISITION_METHOD_ID  " +
				"AND F.POLARITY = ? " +
				"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID  " +
				"AND P.SAMPLE_ID = S.SAMPLE_ID  " +
				"AND S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID  " +
				"AND F.BASE_PEAK IS NOT NULL  " +
				"AND S.EXPERIMENT_ID = ? " +
				"ORDER BY 1 ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, polarity.getCode());
		
		String msSql = "SELECT MZ, HEIGHT FROM MSMS_FEATURE_PEAK WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement msps = conn.prepareStatement(msSql);
		
		
		String updSql = "UPDATE MSMS_FEATURE SET ENTROPY = ?, SPECTRUM_SD = ? WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement updps = conn.prepareStatement(updSql);	
		
		double msNorm = 999.0d;
		
		ArrayList<String>emptyMSMS = new ArrayList<String>();
		
		for(LIMSExperiment experiment : IDTDataCash.getExperiments()) {
			
			ps.setString(2, experiment.getId());
			System.out.println("\nProcessing experiment " + experiment.getId());
			int counter = 0;
			ResultSet fres = ps.executeQuery();	
			while(fres.next()) {

				String fid = fres.getString(1);
				Collection<MsPoint>spectrum = new ArrayList<MsPoint>();
				msps.setString(1, fid);
				ResultSet pres = msps.executeQuery();
				while(pres.next())
					spectrum.add(new MsPoint(pres.getDouble(1), pres.getDouble(2)));
								
				pres.close();
				if(spectrum.isEmpty()) {
					emptyMSMS.add(fid);
				}
				else {
					double entropy = MsUtils.calculateSpectrumEntropy(spectrum);
					entropy = Math.round(entropy * 100000.0d)/100000.0;
					updps.setDouble(1, entropy);
					
					Double mssd = MsUtils.calculateIntensityStandardDeviationForNormalizedSpectrum(spectrum, msNorm);
					if(mssd.equals(Double.NaN)) {
						updps.setNull(2, java.sql.Types.NULL);
					}
					else {
						mssd = Math.round(mssd * 1000.0d)/1000.0;				
						updps.setDouble(2, mssd);
					}
					updps.setString(3, fid);
					updps.addBatch();
					counter++;
					
					if(counter % 1000 == 0) {
						System.out.print(".");
						updps.executeBatch();
					}
					if(counter % 50000 == 0)
						System.out.print(".\n");
				}
			}
			updps.executeBatch();
			fres.close();			
		}
		ps.close();
		msps.close();
		updps.close();
		ConnectionManager.releaseConnection(conn);
		Path logPath = Paths.get("E:\\DataAnalysis\\MSMS\\MSMSQuality\\emptyMsMsFeatures.txt");
		try {
			Files.write(logPath, 
					emptyMSMS, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void calculateEntropyForMsmsLibraryFeatures() throws Exception {
		
		System.out.println("Updating entropy and hash data for MSMS libraries ...");
		Connection conn = ConnectionManager.getConnection();
		String[]libNames = new String[] {
				"hr_nist20_msms_pos_decoy",
				"hr_nist20_msms_neg_decoy",
				"hr_nist17_msms_pos_decoy",
				"hr_nist17_msms_neg_decoy",
		};
		
		String sql = "SELECT MRC2_LIB_ID FROM REF_MSMS_LIBRARY_COMPONENT WHERE LIBRARY_NAME = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		String msSql = "SELECT MZ, INTENSITY FROM REF_MSMS_LIBRARY_PEAK WHERE MRC2_LIB_ID = ?";
		PreparedStatement msps = conn.prepareStatement(msSql);
		
		String updSql = "UPDATE REF_MSMS_LIBRARY_COMPONENT SET ENTROPY = ?, SPECTRUM_HASH = ? WHERE MRC2_LIB_ID = ?";
		PreparedStatement updps = conn.prepareStatement(updSql);		

		for(int i=0; i<libNames.length; i++) {
			
			System.out.println("\nProcessing " + libNames[i]);
			
			ps.setString(1, libNames[i]);
			int counter = 0;
			ResultSet fres = ps.executeQuery();	
			while(fres.next()) {

				String fid = fres.getString(1);
				Collection<MsPoint>spectrum = new ArrayList<MsPoint>();
				msps.setString(1, fid);
				ResultSet pres = msps.executeQuery();
				while(pres.next())
					spectrum.add(new MsPoint(pres.getDouble(1), pres.getDouble(2)));
								
				pres.close();
				
				double entropy = MsUtils.calculateSpectrumEntropy(spectrum);
				String hash = MsUtils.calculateSpectrumHash(spectrum);
				updps.setDouble(1, entropy);
				updps.setString(2, hash);
				updps.setString(3, fid);
				updps.executeUpdate();
				counter++;
				
				if(counter % 1000 == 0)
					System.out.print(".");
				if(counter % 50000 == 0)
					System.out.print(".\n");
			}
			fres.close();
		}
		ps.close();	
		msps.close();		
		updps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static double calculateSpectrumEntropy(Collection<Double>spectrum, double totalIntensity) {
		
		double entropy = 0.0d;
		double log2 = Math.log(2);
		for(Double p : spectrum) {
			double norm = p / totalIntensity;
			entropy += norm * Math.log(norm) / log2;
		}	
		return -entropy;
	}
	
	private static void evaluateDecoyLibrary(
			File libraryPepSearchOutputFile, 
			File decoyPepSearchOutputFile) {
	
//		File libraryPepSearchOutputFile = 
//		new File("E:\\DataAnalysis\\MSMS\\DecoyDB\\LibAndDecoySearch\\Validation\\MSDIAL_POS_NIST_MSMS_PEPSEARCH_RESULTS.TXT");
//
//File decoyPepSearchOutputFile = 
//		new File("E:\\DataAnalysis\\MSMS\\DecoyDB\\LibAndDecoySearch\\Validation\\MSDIAL_POS_NIST_MSMS_DECOY_PEPSEARCH_RESULTS.TXT");
		
		Collection<PepSearchOutputObject>libraryObjects = null;
		try {
			libraryObjects = 
					NISTPepSearchResultManipulator.parsePepSearchResults(libraryPepSearchOutputFile, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		Collection<PepSearchOutputObject>decoyObjects = null;
		try {
			decoyObjects = 
					NISTPepSearchResultManipulator.parsePepSearchResults(decoyPepSearchOutputFile, false);
			decoyObjects.stream().forEach(p -> p.setDecoy(true));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(libraryObjects == null || decoyObjects == null) {
			System.out.println("Couldn't parse input files.");
			return;
		}
		Collection<PepSearchOutputObject>mergedData = 
				NISTPepSearchResultManipulator.mergeLibraryAndDecoyHitsByBestOverall(libraryObjects, decoyObjects);	
		int totalHits = mergedData.size();
		
		DoubleSummaryStatistics sumStats = 
				mergedData.stream().mapToDouble(o -> o.getScore()).summaryStatistics();
		Collection<PepSearchOutputObject>libHits = 
				mergedData.stream().filter(h -> !h.isDecoy()).collect(Collectors.toList());
		Collection<PepSearchOutputObject>decoyHits = 
				mergedData.stream().filter(h -> h.isDecoy()).collect(Collectors.toList());

		for(PepSearchOutputObject poo : libHits) {
			
			if(poo.getLibInchiKey() != null && poo.getUnknownInchiKey() != null) {
				boolean sameStructure = 
						poo.getLibInchiKey().substring(0, 14).equals(poo.getUnknownInchiKey().substring(0, 14));
				poo.setTrueHit(sameStructure);
			}
		}
		long wrongLibHitCount = libHits.stream().filter(p -> !p.isTrueHit()).count();
		double percentIncorrectHits = (double)wrongLibHitCount/libHits.size();
		System.out.println("% incorrect top hits = " + MRC2ToolBoxConfiguration.getPpmFormat().format(percentIncorrectHits));
		
		TreeMap<Double,Double>scoreFdrMap = new TreeMap<Double,Double>();
		TreeMap<Double,Double>scorePvalueMap = new TreeMap<Double,Double>();
		TreeMap<Double,Double>scorePvalueBaseAllMap = new TreeMap<Double,Double>();
		for(double score = sumStats.getMin(); score < sumStats.getMax(); score = score + 1.0d) {
			
			final double cutoff = score;
			long numDecoyHitsAboveCutoff = decoyHits.stream().filter(d -> d.getScore() > cutoff).count();
			long numLibHitsAboveCutoff = libHits.stream().filter(d -> d.getScore() > cutoff).count();
			double pValue = (double)numDecoyHitsAboveCutoff/(numDecoyHitsAboveCutoff + numLibHitsAboveCutoff);
			scorePvalueMap.put(cutoff, pValue);
			double pValueBaseAll = (double)numDecoyHitsAboveCutoff/totalHits;
			scorePvalueBaseAllMap.put(cutoff, pValueBaseAll);			
			double fdr = percentIncorrectHits * numDecoyHitsAboveCutoff / numLibHitsAboveCutoff;
			scoreFdrMap.put(cutoff, fdr);
		}
		for(PepSearchOutputObject poo : mergedData) {

			Double fdr = scoreFdrMap.get(poo.getScore());
			if(fdr != null)
				poo.setFdr(fdr);
			
			Double pVal = scorePvalueMap.get(poo.getScore());
			if(pVal != null)
				poo.setpValue(pVal);
			
			Double pValBaseAll = scorePvalueBaseAllMap.get(poo.getScore());
			if(pValBaseAll != null)
				poo.setpValueBaseAll(pValBaseAll);
		}
//		//	Calculate q-values based on pValues
//		double[] pValueList = mergedData.stream().mapToDouble(p -> p.getpValue()).sorted().toArray();
		
//		Calculate q-values based on pValues BaseAll
		double[] pValueList = mergedData.stream().mapToDouble(p -> p.getpValueBaseAll()).sorted().toArray();
		TreeMap<Double,Double>pValueQvalueMap = new TreeMap<Double,Double>();
		DescriptiveStatistics ds = new DescriptiveStatistics();
		
		//	Wrong, map will screw up, have to use Integer/ Double?
		for(int i=pValueList.length-1; i>=0; i--) {
			
			double qVal = (pValueList[i]) * totalHits / (i+1);
			pValueQvalueMap.put(pValueList[i], qVal);
			if(qVal > 0.0d) {
				
				ds.addValue(qVal);
				if(qVal > ds.getMin())
					pValueQvalueMap.put(pValueList[i], ds.getMin());
			}
		}
		for(PepSearchOutputObject poo : mergedData) {
			
//			Double qVal = pValueQvalueMap.get(poo.getpValue());
			Double qVal = pValueQvalueMap.get(poo.getpValueBaseAll());
			if(qVal != null)
				poo.setqValue(qVal);
		}		
		File mergedFile = Paths.get(decoyPepSearchOutputFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(decoyPepSearchOutputFile.getName()) + "_merged.txt").toFile();
		try {
			NISTPepSearchResultManipulator.writeMergedDataToFile(mergedData, mergedFile);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		File percolatorInputFile = Paths.get(decoyPepSearchOutputFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(decoyPepSearchOutputFile.getName()) + ".pin").toFile();
		try {
			NISTPepSearchResultManipulator.convertMergedResultFileToPinFormat(mergedFile, percolatorInputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			runPercolator(percolatorInputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void runPercolator(File percolatorInputFile) throws Exception {
				
		File percolatorResultFile = Paths.get(percolatorInputFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(percolatorInputFile.getName()) + "_percolator_psms.tsv").toFile();
		File percolatorDecoyResultFile = Paths.get(percolatorInputFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(percolatorInputFile.getName()) + "_percolator_decoy_psms.tsv").toFile();
		File percolatorLogFile = Paths.get(percolatorInputFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(percolatorInputFile.getName()) + "_percolator_log.txt").toFile();			

		if(percolatorInputFile != null && percolatorInputFile.exists()) {
			
			 ProcessBuilder pb =
					   new ProcessBuilder(MRC2ToolBoxConfiguration.getPercolatorBinaryPath(), 
							   "--results-psms", percolatorResultFile.getAbsolutePath(),
							   "--decoy-results-psms", percolatorDecoyResultFile.getAbsolutePath(),
							   "--only-psms",
							   "--post-processing-tdc",
							   "--num-threads", "6",
							   percolatorInputFile.getAbsolutePath());
			try {
				pb.redirectErrorStream(true);
				pb.redirectOutput(Redirect.appendTo(percolatorLogFile));
				Process p = pb.start();
				assert pb.redirectInput() == Redirect.PIPE;
				assert pb.redirectOutput().file() == percolatorLogFile;
				assert p.getInputStream().read() == -1;
				int exitCode = p.waitFor();
				if (exitCode == 0) {

					p.destroy();
					if (!percolatorResultFile.exists() || !percolatorDecoyResultFile.exists()) {
						System.out.println("Percolator failed to create result files.");
					}
				} else {
					System.out.println("Percolator run failed.");
				}
			} catch (IOException e) {
				System.out.println("Percolator run failed.");
				e.printStackTrace();
			}		 
		}
	}
	
	private static void verifyFileNames() throws Exception {
		
		IOFileFilter dotDfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
		File sourceDirectory = new File("Y:\\_QUALTMP\\EX01094\\RP\\POS\\BATCH2");
		Collection<File> dotDfiles = FileUtils.listFilesAndDirs(
				sourceDirectory,
				DirectoryFileFilter.DIRECTORY,
				dotDfilter);

		if (!dotDfiles.isEmpty()) {

			for(File rdf : dotDfiles) {
				
				String baseName = FilenameUtils.getBaseName(rdf.getName());
				String sinfoBaseName = FilenameUtils.getBaseName(getFileNameFromSampleInfo(rdf));
				
				if(sinfoBaseName != null && !baseName.equals(sinfoBaseName)) {
					System.out.println("name mismatch in " + baseName + " " + sinfoBaseName);					
					renameFileInSampleInfo(rdf, sinfoBaseName, baseName);
					System.out.println("Renamed.");
				}
			}
		}
	}
	
	private static String getFileNameFromSampleInfo(File inputFile) throws Exception {
		
		File sampleInfoFile = Paths.get(inputFile.getAbsolutePath(), "AcqData", "sample_info.xml").toFile();
		if (sampleInfoFile.exists()) {

			Document sampleInfo = XmlUtils.readXmlFile(sampleInfoFile);
			if (sampleInfo != null) {
				
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				XPathExpression expr = xpath.compile("//SampleInfo/Field");
				NodeList fieldNodes = (NodeList) expr.evaluate(sampleInfo, XPathConstants.NODESET);
				for (int i = 0; i < fieldNodes.getLength(); i++) {

					Element fieldElement = (Element) fieldNodes.item(i);
					String name = fieldElement.getElementsByTagName("Name").
							item(0).getFirstChild().getNodeValue().trim();
					String value = fieldElement.getElementsByTagName("Value").
							item(0).getFirstChild().getNodeValue().trim();

					if (name != null && name.equals("Data File"))
						return value;
				}
			}
		}
		return null;
	}
	
	private static void fixMetaboliteNames() throws Exception {
		
		File sourceDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix");
		String destinationDirName = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed";
		IOFileFilter metNameFilter = 
				FileFilterUtils.makeFileOnly(new RegexFileFilter("^metadata_metabolites_named_.+\\.(txt)|(TXT)$"));
		Collection<File> metNamefiles = FileUtils.listFiles(
				sourceDirectory,
				metNameFilter,
				null);
		
		Path bucketFileListing = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metadata_metabolites_named_listing.txt");
		List<String>bucketAddresses = Files.readAllLines(bucketFileListing);
		List<String>copyCommands = new ArrayList<String>();		
		String[]lipidsWithNoIsomers = new String[] {"LPC(15:0)", "MG(14:0)", "LPC(18:1)", "LPC(17:0)",};
		if (!metNamefiles.isEmpty()) {

			for(File mnf : metNamefiles) {
				
				boolean hasChanged = false;
				//	Read file and replace names if necessary
				Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata = 
						parseMetaboliteMetadataFile(mnf);
				
				for(Map<MoTrPACmetaboliteMetaDataFields, String> mData : metaboliteMetadata) {
					
					String mName = mData.get(MoTrPACmetaboliteMetaDataFields.METABOLITE_NAME);
					for(String lipid : lipidsWithNoIsomers) {
						
						if(mName.contains(lipid) && !mName.equals(lipid)) {
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, lipid);
							hasChanged = true;
						}
					}
					if(mName.endsWith("_a")) {
						
						if(mName.endsWith("_rp_a"))
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName);
						else
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_a", "_rp_a"));
						
						hasChanged = true;
					}
					if(mName.endsWith("_b")) {
						
						if(mName.endsWith("_rp_b"))
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName);
						else
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_b", "_rp_b"));
						
						hasChanged = true;
					}
					if(mName.endsWith("_a_b")) {
						if(mName.endsWith("_rp_a_b"))
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_rp_a_b", ""));
						else
							mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, mName.replace("_a_b", ""));
						hasChanged = true;
					}
					if(mName.equals("Car(5:0) isomers")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)_rp_a_b");
						hasChanged = true;
					}
				}			
				if(hasChanged) {
					Path newFilePath = Paths.get(destinationDirName, mnf.getName());
					try {
						writeMetaboliteMetadataFile(metaboliteMetadata, newFilePath);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String fName = mnf.getName();
					String bucketAddres = bucketAddresses.stream().
							filter(l -> l.contains(fName)).
							findFirst().orElse(null);
					
					if(bucketAddres != null) {
						copyCommands.add("gsutil cp \"" + newFilePath.toString() + "\" " + bucketAddres.replace("PROCESSED_20191008", "PROCESSED_20210629"));
					}
				}
			}
		}
		Path copyCommandsFilePath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed\\copyCommands.txt");
		try {
			Files.write(copyCommandsFilePath, 
					copyCommands, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void fixMetaboliteNamesStepTwo() throws Exception {
		
		File sourceDirectory = new File("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix");
		String destinationDirName = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed";
		IOFileFilter metNameFilter = 
				FileFilterUtils.makeFileOnly(new RegexFileFilter("^metadata_metabolites_named_.+\\.(txt)|(TXT)$"));
		Collection<File> metNamefiles = FileUtils.listFiles(
				sourceDirectory,
				metNameFilter,
				null);
		
		Path bucketFileListing = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metadata_metabolites_named_listing.txt");
		List<String>bucketAddresses = Files.readAllLines(bucketFileListing);
		List<String>copyCommands = new ArrayList<String>();		
		if (!metNamefiles.isEmpty()) {

			for(File mnf : metNamefiles) {
				
				boolean hasChanged = false;
				//	Read file and replace names if necessary
				Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata = 
						parseMetaboliteMetadataFile(mnf);
				
				for(Map<MoTrPACmetaboliteMetaDataFields, String> mData : metaboliteMetadata) {
					
					String refmetOld = mData.get(MoTrPACmetaboliteMetaDataFields.REFMET_NAME);
					if(refmetOld.equalsIgnoreCase("CAR(3:0(2Me))")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(4:0)_rp_a");
						hasChanged = true;
					}				
					if(refmetOld.equalsIgnoreCase("Car(5:0)")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)_rp_b");
						hasChanged = true;
					}
					if(refmetOld.equalsIgnoreCase("Car(5:0) isomers") || refmetOld.equals("CAR(5:0)_rp_a_b")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)");
						hasChanged = true;
					}
					
					if(refmetOld.equalsIgnoreCase("Car(4:0)")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(4:0)_rp_b");
						hasChanged = true;
					}
					if(refmetOld.equalsIgnoreCase("Car(4:0) isomers") || refmetOld.equals("CAR(4:0)_rp_a_b")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(4:0)");
						hasChanged = true;
					}
					if(refmetOld.equalsIgnoreCase("CAR(4:0(3Me))")) {
						mData.put(MoTrPACmetaboliteMetaDataFields.REFMET_NAME, "CAR(5:0)_rp_a");
						hasChanged = true;
					}
				}			
				if(hasChanged) {
					Path newFilePath = Paths.get(destinationDirName, mnf.getName());
					try {
						writeMetaboliteMetadataFile(metaboliteMetadata, newFilePath);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String fName = mnf.getName();
					String bucketAddres = bucketAddresses.stream().
							filter(l -> l.contains(fName)).
							findFirst().orElse(null);
					
					if(bucketAddres != null) {
						copyCommands.add("gsutil cp \"" + newFilePath.toString() + "\" " + bucketAddres.replace("PROCESSED_20191008", "PROCESSED_20210629"));
					}
				}
			}
		}
		Path copyCommandsFilePath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\UploadPrep docs\\metaboliteData2fix\\Fixed\\copyCommands.txt");
		try {
			Files.write(copyCommandsFilePath, 
					copyCommands, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void writeMetaboliteMetadataFile(
			Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata,
			Path metaboliteMetadataFilePath) {
		
		ArrayList<String>lines = new ArrayList<String>();
		ArrayList<String>header = new ArrayList<String>();
		for(MoTrPACmetaboliteMetaDataFields field : MoTrPACmetaboliteMetaDataFields.values())
			header.add(field.getName());
		
		lines.add(StringUtils.join(header, MRC2ToolBoxConfiguration.getTabDelimiter()));
		for(Map<MoTrPACmetaboliteMetaDataFields, String> mmd : metaboliteMetadata) {
			
			ArrayList<String>line = new ArrayList<String>();
			for(MoTrPACmetaboliteMetaDataFields field : MoTrPACmetaboliteMetaDataFields.values()) {
				
				String value = mmd.get(field);
				if(value != null)
					line.add(value);
				else
					line.add("");				
			}
			lines.add(StringUtils.join(line, MRC2ToolBoxConfiguration.getTabDelimiter()));
		}
		try {
			Files.write(metaboliteMetadataFilePath, 
					lines, 					
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>parseMetaboliteMetadataFile(File metaboliteMetadataFile) throws Exception{
		
		Collection<Map<MoTrPACmetaboliteMetaDataFields,String>>metaboliteMetadata = 
				new ArrayList<Map<MoTrPACmetaboliteMetaDataFields,String>>();
		String[][] metaboliteData = null;
		try {
			metaboliteData =
				DelimitedTextParser.parseTextFileWithEncodingSkippingComments(
						metaboliteMetadataFile, MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(metaboliteData == null) {
			throw new Exception("Unable to read metabolite data file!");
		}
		Map<MoTrPACmetaboliteMetaDataFields,Integer>columnMap = 
				new TreeMap<MoTrPACmetaboliteMetaDataFields,Integer>();
		String[]header = metaboliteData[0];
		for(int i=0; i<header.length; i++) {
			if(header[i].trim().isEmpty())
				continue;
			
			MoTrPACmetaboliteMetaDataFields field = 
					MoTrPACmetaboliteMetaDataFields.getMoTrPACmetaboliteMetadataFieldByName(header[i].trim());
			
			if(field != null)
				columnMap.put(field, i);
		}
		for(int i=1; i<metaboliteData.length; i++) {
			Map<MoTrPACmetaboliteMetaDataFields,String>mDataMap = new TreeMap<MoTrPACmetaboliteMetaDataFields,String>();
			for(Entry<MoTrPACmetaboliteMetaDataFields, Integer> col : columnMap.entrySet())				
				mDataMap.put(col.getKey(), metaboliteData[i][col.getValue()]);

			metaboliteMetadata.add(mDataMap);
		}
		return metaboliteMetadata;
	}
	
	private static void batchRenameCefs() {
		
		File renameMapFile = new File("C:\\Users\\Sasha\\Downloads\\_2_rename\\EX01117-Gastroc-NEG-rename-map.txt");
		String[][] renameMapData = DelimitedTextParser.parseTextFile(renameMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>renameMap = new TreeMap<String,String>();
		for(int i=0; i<renameMapData.length; i++)
			renameMap.put(renameMapData[i][0], renameMapData[i][1]);
		
		File sourceDirectory = new File("C:\\Users\\Sasha\\Downloads\\_2_rename\\FBF\\NEG");
		String destinationDirName = "C:\\Users\\Sasha\\Downloads\\_2_rename\\FBF\\NEG\\Renamed";
		IOFileFilter cefFilter = 
				FileFilterUtils.makeFileOnly(new RegexFileFilter(".+\\.(cef)|(CEF)$"));
		Collection<File> cefFfiles = FileUtils.listFiles(
				sourceDirectory,
				cefFilter,
				null);
		
		if (!cefFfiles.isEmpty()) {

			for(File rdf : cefFfiles) {
				
				String baseName = FilenameUtils.getBaseName(rdf.getName());
				String newName = renameMap.get(baseName);
				if(newName == null) {
					System.out.println("No new name for " + baseName);
					continue;
				}
				File renamedFile = Paths.get(destinationDirName, newName + "." + FileNameUtils.getExtension(rdf.getName())).toFile();
				try {
					FileUtils.copyFile(rdf, renamedFile);
					System.out.println("Renamed " + baseName + " to " + newName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("***");
	}
	
	private static void batchRename() {
		//
		File renameMapFile = new File("C:\\Users\\Sasha\\Downloads\\_2_rename\\EX01117-Gastroc-POS-rename-map.txt");
		String[][] renameMapData = DelimitedTextParser.parseTextFile(renameMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>renameMap = new TreeMap<String,String>();
		for(int i=0; i<renameMapData.length; i++)
			renameMap.put(renameMapData[i][0], renameMapData[i][1]);
		
		IOFileFilter dotDfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
		File sourceDirectory = new File("C:\\Users\\Sasha\\Downloads\\_2_rename\\POS");
		String destinationDirName = "C:\\Users\\Sasha\\Downloads\\_2_rename\\POS\\Renamed";
		Collection<File> dotDfiles = FileUtils.listFilesAndDirs(
				sourceDirectory,
				DirectoryFileFilter.DIRECTORY,
				dotDfilter);

		if (!dotDfiles.isEmpty()) {

			for(File rdf : dotDfiles) {
				
				String baseName = FilenameUtils.getBaseName(rdf.getName());
				String newName = renameMap.get(baseName);
				if(newName == null) {
					System.out.println("No new name for " + baseName);
					continue;
				}
				File renamedFile = Paths.get(destinationDirName, newName + "." + FileNameUtils.getExtension(rdf.getName())).toFile();
				try {
					FileUtils.copyDirectory(rdf, renamedFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(renamedFile.exists()) {
					try {
						renameFileInSampleInfo(renamedFile, baseName, newName);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Renamed " + baseName + " to " + newName);
				}
			}
		}
	}
	
	private static void renameFileInSampleInfo(File inputFile, String oldName, String newName) throws Exception {
		
		File sampleInfoFile = Paths.get(inputFile.getAbsolutePath(), "AcqData", "sample_info.xml").toFile();
		if(sampleInfoFile.setWritable(true)) {
		    System.out.println("Re-enabled writing for " + inputFile.getName() + " sample_info.xml");
		}
		else {
		    System.out.println("Failed to re-enable writing on file.");
		}
		if (sampleInfoFile.exists()) {

			Document sampleInfo = XmlUtils.readXmlFile(sampleInfoFile);
			if (sampleInfo != null) {
				
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				XPathExpression expr = xpath.compile("//SampleInfo/Field");
				NodeList fieldNodes = (NodeList) expr.evaluate(sampleInfo, XPathConstants.NODESET);
				for (int i = 0; i < fieldNodes.getLength(); i++) {

					Element fieldElement = (Element) fieldNodes.item(i);
					String name = fieldElement.getElementsByTagName("Name").
							item(0).getFirstChild().getNodeValue().trim();
					String value = fieldElement.getElementsByTagName("Value").
							item(0).getFirstChild().getNodeValue().trim();

					if (name != null) {
						if(name.equals("Data File")) {
							String nvalue = value.replace(oldName, newName);
							fieldElement.getElementsByTagName("Value").item(0).getFirstChild().setNodeValue(nvalue);
							break;
						}
					}						
				}
			    Transformer xformer = TransformerFactory.newInstance().newTransformer();
			    xformer.transform
			        (new DOMSource(sampleInfo), new StreamResult(sampleInfoFile));
			}
		}
	}
		
	private static void removeUpdatedExperiments() {
		
		String[]idsToRemove = new String[] { };
		for(String id : idsToRemove) {
			try {
				LIMSUtils.deleteMRC2LIMSExperiment(id);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void updateMotrPacRefSampleAssignment() throws Exception {
		
		//	Read list of files
		Path listPath = Paths.get("Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\RefSampleCorrection\\PASS1A-06_sample_metadata_file_list.txt");
		String tmpDir = "Y:\\DataAnalysis\\_Reports\\EX00979 - PASS 1B\\RefSampleCorrection\\TMP";
		String gsUtilBinary = "C:\\Users\\Sasha\\AppData\\Local\\Google\\Cloud SDK\\google-cloud-sdk\\bin\\gsutil.cmd";
		List<String>sampleMetaDataList = new ArrayList<String>();
		try {
			sampleMetaDataList = Files.readAllLines(listPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		for (String bucketPath : sampleMetaDataList) {

			Path tmpFilePath = Paths.get(tmpDir, "sample_metadata.txt").toAbsolutePath();
			Path correctedTmpFilePath = Paths.get(tmpDir, "sample_metadata_corrected.txt");
			ProcessBuilder pb = new ProcessBuilder(
					gsUtilBinary, "cp", bucketPath, tmpFilePath.toString());
			try {
				Process p = pb.start();
				int exitCode = p.waitFor();
				if (exitCode == 0) {
					p.destroy();
					List<String> sampleDataLines = new ArrayList<String>();
					if(tmpFilePath.toFile() != null && tmpFilePath.toFile().exists())
						sampleDataLines  = Files.readAllLines(tmpFilePath, Charset.forName("ISO-8859-1"));
					
					boolean corrected = false;
					List<String> correctedSampleDataLines = new ArrayList<String>();
					for(String sdl : sampleDataLines) {
						
						if(sdl.startsWith("CS0UM")) {
							correctedSampleDataLines.add(sdl.replace("QC-Reference", "QC-ReCAS"));
							corrected = true;
						}
						else {
							correctedSampleDataLines.add(sdl);
						}
					}
					if(corrected) {						
					    try {
							Files.write(correctedTmpFilePath, 
									correctedSampleDataLines, 
									StandardCharsets.UTF_8,
									StandardOpenOption.CREATE, 
									StandardOpenOption.APPEND);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}					    
					    //	Upload corrected file 
					    if(correctedTmpFilePath.toFile() != null && correctedTmpFilePath.toFile().exists()) {
							pb = new ProcessBuilder(
									gsUtilBinary, "cp", correctedTmpFilePath.toString(), bucketPath);
							p = pb.start();
							exitCode = p.waitFor();
							if (exitCode == 0) {
								System.out.println("Updated " + bucketPath);
							}
					    }
					    //	Delete corrected file					    
					    Files.delete(correctedTmpFilePath);
					}
					Files.delete(tmpFilePath);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}

	public static void runPercolator() {
		
		String percolatorExePath = "E:\\Program Files\\Percolator\\percolator-v3-05\\bin\\percolator.exe";
		String inputDir = "E:\\DataAnalysis\\MSMS\\DecoyDB\\LibAndDecoySearch\\NEG\\Results\\Merged\\Overall\\Normal";
		List<File> inputFiles = new ArrayList<File>();
		Path outBatch = Paths.get(inputDir, "PIN", "percolatorHybrid.bat");
		try {
			inputFiles = Files.list(Paths.get(inputDir)).
				filter(file -> !Files.isDirectory(file)).
				map(p -> p.toFile()).collect(Collectors.toList());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<String>batchCommands = new ArrayList<String>();
		for(File inputFile : inputFiles) {
			
			File outputFile = Paths.get(inputDir, "PIN2", FilenameUtils.getBaseName(inputFile.getName()) + ".pin").toFile();
			File percolatorResultFile = Paths.get(inputDir, "PIN2", FilenameUtils.getBaseName(inputFile.getName()) + "_percolator_psms.tsv").toFile();
			File percolatorDecoyResultFile = Paths.get(inputDir, "PIN2", FilenameUtils.getBaseName(inputFile.getName()) + "_percolator_decoy_psms.tsv").toFile();
			File percolatorLogFile = Paths.get(inputDir, "PIN2", FilenameUtils.getBaseName(inputFile.getName()) + "_percolator_log.txt").toFile();			
			try {
				NISTPepSearchResultManipulator.convertMergedResultFileToPinFormat(inputFile, outputFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(outputFile.exists()) {
				
				 ProcessBuilder pb =
						   new ProcessBuilder(percolatorExePath, 
								   "--results-psms", percolatorResultFile.getAbsolutePath(),
								   "--decoy-results-psms", percolatorDecoyResultFile.getAbsolutePath(),
								   "--only-psms",
								   "--post-processing-tdc",
								   "--num-threads", "6",
								   outputFile.getAbsolutePath());
				 try {
					 pb.redirectErrorStream(true);
					 pb.redirectOutput(Redirect.appendTo(percolatorLogFile));
					 Process p = pb.start();
					 assert pb.redirectInput() == Redirect.PIPE;
					 assert pb.redirectOutput().file() == percolatorLogFile;
					 assert p.getInputStream().read() == -1;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(inputFile.getName());		 
			}
		}
	}
	
	public static void updateMSDIALMetaboliteCompoundDataFromPubChemManual() throws Exception {
		
		Path filePath = Paths.get("E:\\DataAnalysis\\Databases\\MSDIAL\\Metabolomics-VS15\\smilesWithPubChemIds2.txt");
		List<String> lines = Files.readAllLines(filePath);
		
		Connection conn = ConnectionManager.getConnection();
		String sql = "UPDATE MSDIAL_METABOLITE_COMPONENTS SET ACCESSION = ?, "
				+ "ACCESSION_MATCHED_ON = ? WHERE SMILES = ? AND ACCESSION IS NULL";
		PreparedStatement ps = conn.prepareStatement(sql);	
		for(String line : lines) {
			
			String[]parts = line.split("\\t");
			String matchedOn = null;
			if(parts.length == 3 && !parts[2].isEmpty())
				matchedOn = parts[2];
			
			ps.setString(1, parts[1]);
			if(matchedOn != null)
				ps.setString(2, matchedOn);
			else
				ps.setNull(2, java.sql.Types.NULL);
			
			ps.setString(3, parts[0]);
			ps.executeUpdate();
		}		
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	//
	
	public static void fetchMissingMSDIALMetaboliteCompoundDataFromPubChemBySmiles() throws Exception {

		ArrayList<String>noResponse = new ArrayList<String>();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Canonical);
		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT DISTINCT SMILES FROM MSDIAL_METABOLITE_COMPONENTS "
				+ "WHERE ACCESSION IS NULL AND SMILES IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(sql);	
		
		String updSql = "UPDATE MSDIAL_METABOLITE_COMPONENTS "
				+ "SET ACCESSION = ? WHERE SMILES = ? AND ACCESSION IS NULL";
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String smiles = rs.getString("SMILES");	
			String smilesToSearch = smiles;
			String canonicalized = null;
			try {
				canonicalized = smilesGenerator.create(smipar.parseSmiles(smiles));
			} catch (InvalidSmilesException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CDKException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(canonicalized != null)
				smilesToSearch = canonicalized;
				
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/" + smilesToSearch + "/record/SDF";

			IteratingSDFReader reader = null;
			IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
			InputStream pubchemDataStream = null;
			InputStream synonymStream = null;
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
			} catch (Exception e) {
				//	e.printStackTrace();
			}
			if(pubchemDataStream != null) {
				reader = new IteratingSDFReader(pubchemDataStream, coBuilder);
				while (reader.hasNext()) {
					IAtomContainer molecule = (IAtomContainer)reader.next();
					String cid = molecule.getProperty(PubChemFields.PUBCHEM_ID.toString());
					if(cid == null) {
						System.out.println("Not found " + smiles);
						continue;
					}
					//	Check if CID already in the database
					String name = PubChemParser.idInDatabase(cid, conn);
					if(name != null) {
						updPs.setString(1, cid);
						updPs.setString(2, smiles);
						updPs.executeUpdate();
						break;
					}
					try {
						synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/synonyms/TXT");
					} catch (Exception e) {
						//	e.printStackTrace();
					}
					String[] synonyms = new String[0];
					if(synonymStream != null) {
						try {
							synonyms = IOUtils.toString(synonymStream, StandardCharsets.UTF_8).split("\\r?\\n");
						}
						catch (Exception e) {
							//	e.printStackTrace();
						}
					}
					if(synonyms.length == 0)
						synonyms = new String[] {cid};
					
					PubChemCompoundDescriptionBundle descBundle =  getCompoundDescription(cid);
					CompoundIdentity inserted = null;
					try {
						inserted = PubChemParser.insertPubchemRecord(molecule, synonyms, descBundle, conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(inserted != null) {
						updPs.setString(1, inserted.getPrimaryDatabaseId());
						updPs.setString(2, smiles);
						updPs.executeUpdate();
					}
				}
			}
			else {
				noResponse.add(smiles);
				System.out.println("No results for");
				System.out.println(smiles);
				System.out.println(smilesToSearch);
				System.out.println("-------------------------");
			}
		}
		rs.close();
		updPs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		Path logPath = Paths.get("E:\\DataAnalysis\\Databases\\MSDIAL\\Metabolomics-VS15", "smilesWithProblems.txt");
	    try {
			Files.write(logPath, 
					noResponse, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void fetchMissingMSDIALMetaboliteCompoundDataFromPubChem() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT DISTINCT INCHI_KEY FROM MSDIAL_METABOLITE_COMPONENTS "
				+ "WHERE ACCESSION IS NULL";
		PreparedStatement ps = conn.prepareStatement(sql);	
		
		String updSql = "UPDATE MSDIAL_METABOLITE_COMPONENTS "
				+ "SET ACCESSION = ? WHERE INCHI_KEY = ? AND ACCESSION IS NULL";
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String inchikey = rs.getString("INCHI_KEY");
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchikey + "/record/SDF";

			IteratingSDFReader reader = null;
			IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
			InputStream pubchemDataStream = null;
			InputStream synonymStream = null;
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
			} catch (Exception e) {
				//	e.printStackTrace();
			}
			if(pubchemDataStream != null) {
				reader = new IteratingSDFReader(pubchemDataStream, coBuilder);
				while (reader.hasNext()) {
					IAtomContainer molecule = (IAtomContainer)reader.next();
					String cid = molecule.getProperty(PubChemFields.PUBCHEM_ID.toString());
					try {
						synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/synonyms/TXT");
					} catch (Exception e) {
						//	e.printStackTrace();
					}
					String[] synonyms = new String[0];
					if(synonymStream != null) {
						try {
							synonyms = IOUtils.toString(synonymStream, StandardCharsets.UTF_8).split("\\r?\\n");
						}
						catch (Exception e) {
							//	e.printStackTrace();
						}
					}
					if(synonyms.length == 0)
						synonyms = new String[] {cid};
					
					PubChemCompoundDescriptionBundle descBundle =  getCompoundDescription(cid);
					CompoundIdentity inserted = null;
					try {
						inserted = PubChemParser.insertPubchemRecord(molecule, synonyms, descBundle, conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(inserted != null) {
						updPs.setString(1, inserted.getPrimaryDatabaseId());
						updPs.setString(2, inchikey);
						updPs.executeUpdate();
					}
				}
			}
			else {
				System.out.println(inchikey);
			}
		}
		rs.close();
		updPs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void uploadMSDialMetabolomicsLibrary(File inputFile, Polarity polarity) throws Exception {
		
		Collection<MSDialMSMSRecord>records = 
				MSDialMetabolomicsLibraryParser.processMSDialRecords(inputFile, polarity);
		
		MSDialMetabolomicsLibraryParser.insertRecords(records);
		
		System.out.println("\nFile " + inputFile.getName() + " parsed.");
	}	
	
	public static void updateNullPrimaryNames() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String sql = "SELECT ACCESSION FROM COMPOUND_DATA WHERE PRIMARY_NAME IS NULL ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(sql);	
		String updSql = "UPDATE COMPOUND_DATA SET PRIMARY_NAME = ? WHERE ACCESSION = ?";
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			String accession = rs.getString("ACCESSION");
			CompoundNameSet nameSet = CompoundDatabaseUtils.getSynonyms(accession, conn);
			if(nameSet.getPrimaryName() != null) {
				updPs.setString(1, nameSet.getPrimaryName());
				updPs.setString(2, accession);
				updPs.executeUpdate();
			}
			else {
				String primName = nameSet.getIupacName();
				if(primName == null)
					primName = nameSet.getSystematicName();
				
				if(primName == null)
					primName = nameSet.getNextSynonym();
				
				if(primName == null) {
					System.out.println("No name for accession " + accession);
					continue;
				}				
				nameSet.setPrimaryName(primName);
				CompoundDatabaseUtils.updateSynonyms(nameSet, conn);
				
				updPs.setString(1, nameSet.getPrimaryName());
				updPs.setString(2, accession);
				updPs.executeUpdate();
			}
			count++;
			if(count % 100 == 0)
				System.out.println(Integer.toString(count));
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateSynonymsWithPrimaryNames() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String sql = "SELECT ACCESSION, PRIMARY_NAME FROM COMPOUND_DATA ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(sql);			
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			String accession = rs.getString("ACCESSION");
			String primName = rs.getString("PRIMARY_NAME");
			CompoundNameSet nameSet = CompoundDatabaseUtils.getSynonyms(accession, conn);
			if(nameSet.getPrimaryName() == null || !primName.equals(nameSet.getPrimaryName())) {
				
				nameSet.setPrimaryName(primName);
				try {
					CompoundDatabaseUtils.updateSynonyms(nameSet, conn);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("\nError with" + accession +" - "+ primName);
				}
			}
			count++;
			if(count % 1000 == 0)
				System.out.print(".");
			if(count % 30000 == 0)
				System.out.println(Integer.toString(count));
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void calculateExactMonoisotopicMass(String formulaString) {
		
		IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);			
		double monoisotopicMass = MolecularFormulaManipulator.getMajorIsotopeMass(formula);
		System.out.println(Double.toString(monoisotopicMass));
	}
	
	public static void calculateExactMassesForMsDialLipids() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String sql = "SELECT DISTINCT FORMULA FROM LIPIDBLAST_RIKEN_COMPOUND_DATA ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		String updSql = "UPDATE LIPIDBLAST_RIKEN_COMPOUND_DATA SET EXACT_MASS = ? WHERE FORMULA = ?";
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			String formulaString = rs.getString("FORMULA");
			IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);			
			double monoisotopicMass = MolecularFormulaManipulator.getMajorIsotopeMass(formula);
			updPs.setDouble(1, monoisotopicMass);
			updPs.setString(2, formulaString);
			updPs.executeUpdate();
			count++;
			if(count % 200 == 0)
				System.out.println(Integer.toString(count));
		}
		rs.close();
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void fetchLipidBlastCompoundDataFromPubChemByInchiKey() throws Exception {
		
		Path logPath = Paths.get("E:\\DataAnalysis\\Databases\\LipidBlast\\Riken\\Pubchem\\MISSING_INCHI_KEYS.TXT");
		ArrayList<String>logStart = new ArrayList<String>();
		logStart.add(MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));
		logStart.add("The following InChiKeys not found in PubChem:");
		logStart.add("-------------------------------");
	    try {
			Files.write(logPath, 
					logStart, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Connection conn = ConnectionManager.getConnection();
		String sql = "SELECT DISTINCT INCHI_KEY FROM LIPIDBLAST_RIKEN_COMPONENTS WHERE ACCESSION IS NULL";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String inchikey = rs.getString("INCHI_KEY");
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchikey + "/record/SDF";
			InputStream pubchemDataStream = WebUtils.getInputStreamFromURLsilent(requestUrl);
			if(pubchemDataStream != null) {

				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\LipidBlast\\Riken\\Pubchem", inchikey + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			}
			else {
			    try {
					Files.writeString(logPath, 
							inchikey + "\n", 
							StandardCharsets.UTF_8,
							StandardOpenOption.WRITE, 
							StandardOpenOption.APPEND);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void moveLipidBlastRikenToMSMSLibrary() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String lbLibraryName = "LIPIDBLAST_RIKEN";
		
		//	LipidBlast RIKEN data
		String lbQuery = 
				"SELECT RLB_ID, NAME, PRECURSOR_MZ, PRECURSOR_TYPE, IONMODE  " +
				"FROM LIPIDBLAST_RIKEN_COMPONENTS ORDER BY 1 ";
		PreparedStatement lbPs = conn.prepareStatement(lbQuery);
		
		String lbPeaksQuery = "SELECT MZ, INTENSITY FROM LIPIDBLAST_RIKEN_PEAKS WHERE  RLB_ID = ? ORDER BY 1";
		PreparedStatement lbPeaksPs = conn.prepareStatement(lbPeaksQuery);
		
		//	MRC2 library ID
		String libId = null;
//		String libidQuery = "SELECT '" + DataPrefix.MSMS_LIBRARY_ENTRY.getName() +
//			"' || LPAD(MSMS_LIB_ENTRY_SEQ.NEXTVAL, 9, '0') AS MRC2ID FROM DUAL";
//		PreparedStatement libidPs = conn.prepareStatement(libidQuery);
		
		//	Insert new component 
		String componentQuery =
				"INSERT INTO REF_MSMS_LIBRARY_COMPONENT ( " +
				"MRC2_LIB_ID, POLARITY, IONIZATION, PRECURSOR_MZ, ADDUCT, SPECTRUM_TYPE,  " +
				"SPECTRUM_SOURCE, IONIZATION_TYPE, LIBRARY_NAME, ORIGINAL_LIBRARY_ID, SPECTRUM_HASH, ENTROPY) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement componentPs = conn.prepareStatement(componentQuery);

		// 	Insert spectrum peaks
		String peaksQuery =
			"INSERT INTO REF_MSMS_LIBRARY_PEAK (MRC2_LIB_ID, MZ, INTENSITY, IS_PARENT) " +
			"VALUES(?, ?, ?, ?) ";
		PreparedStatement peaksPs = conn.prepareStatement(peaksQuery);
	
		Collection<MsPoint>spectrum = new ArrayList<MsPoint>();
		int count = 0;
		ResultSet rs = lbPs.executeQuery();
		ResultSet rs2 = null;
		ResultSet peksRs = null;
		
		componentPs.setString(3, "ESI");
		componentPs.setString(6, "MS2");
		componentPs.setString(7, SpectrumSource.THEORETICAL.name());
		componentPs.setString(8, "ESI");
		componentPs.setString(9, lbLibraryName);
		
		while(rs.next()) {
			
			//	Get next library ID
//			rs2 = libidPs.executeQuery();
//			while(rs2.next())
//				libId = rs2.getString("MRC2ID");
				
			String rlLibId = rs.getString("RLB_ID");
			Double precursorMz = rs.getDouble("PRECURSOR_MZ");
			MsPoint parent = new MsPoint(precursorMz, 200.0d);
			spectrum.clear();
			
			//	Get LipidBlast peaks
			lbPeaksPs.setString(1, rlLibId);
			peksRs = lbPeaksPs.executeQuery();
			while(peksRs.next())
				spectrum.add(new MsPoint(peksRs.getDouble(1), peksRs.getDouble(2)));
			
			peksRs.close();
			
			//	Component
			libId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_LIB_ENTRY_SEQ",
					DataPrefix.MSMS_LIBRARY_ENTRY,
					"0",
					9);	
			componentPs.setString(1, libId);
			componentPs.setString(2, rs.getString("IONMODE"));			
			componentPs.setDouble(4, precursorMz);
			componentPs.setString(5, rs.getString("PRECURSOR_TYPE"));			
			componentPs.setString(10, rlLibId);
			componentPs.setString(11, MsUtils.calculateSpectrumHash(spectrum));		
			componentPs.setDouble(12, MsUtils.calculateSpectrumEntropy(spectrum));
			componentPs.addBatch();
			
			//	Peaks
			peaksPs.setString(1, libId);
			boolean parentInSpectrum = false;
			for(MsPoint p : spectrum) {

				peaksPs.setDouble(2, p.getMz());
				peaksPs.setDouble(3, p.getIntensity());
				peaksPs.setString(4, null);
				if(parent != null) {

					if(p.getMz() == parent.getMz()) {
						peaksPs.setString(4, "Y");
						parentInSpectrum = true;
					}
				}
				peaksPs.addBatch();
			}
			if(!parentInSpectrum && parent != null) {

				peaksPs.setDouble(2, parent.getMz());
				peaksPs.setDouble(3, parent.getIntensity());
				peaksPs.setString(4, "Y");
				peaksPs.addBatch();
			}
			count++;
			if(count % 200 == 0) {
				componentPs.executeBatch();
				peaksPs.executeBatch();
				System.out.print(".");
			}				
			if(count % 8000 == 0)
				System.out.println(".");
		}
		componentPs.executeBatch();
		peaksPs.executeBatch();
		rs.close();
//		libidPs.close();
		lbPs.close();
		lbPeaksPs.close();
		componentPs.close();
		peaksPs.close();
		ConnectionManager.releaseConnection(conn);
	}
		
	private static void uploadLipidBlastRikenToDatabase() throws Exception {
		File inputFile = new File("E:\\DataAnalysis\\Databases\\MSDIAL\\Lipidomics-V68\\MSDIAL-TandemMassSpectralAtlas-VS68-Neg.msp");
		Collection<MSDialMSMSRecord>recordList = null;
		try {
			recordList = LipidBlastRikenParser.processLipidBlastRecords(inputFile, Polarity.Negative);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(recordList != null) {
			try {
				LipidBlastRikenParser.insertRecords(recordList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void exportNist1720NegHighRes() throws Exception {
		
		Path mspFilePath = Paths.get("C:\\Users\\Sasha\\Downloads", "NIST20_HR_NEG.MSP");
		Connection conn = ConnectionManager.getConnection();
//		Collection<String>libIds = MSMSLibraryUtils.getFilteredLibraryIds(
//				 "nist_msms", 
//				 Polarity.Negative, 
//				 MSMSResolution.HIGH, 
//				 SpectrumSource.EXPERIMENTAL,
//				 conn);
		
		Collection<String>libIds  = MSMSLibraryUtils.getFilteredLibraryIds(
				 "hr_msms_nist", 
				 Polarity.Negative, 
				 MSMSResolution.HIGH, 
				 SpectrumSource.EXPERIMENTAL,
				 conn);
		
//		libIds.addAll(libIds20);
		
		Collection<MSPField>individual = new ArrayList<MSPField>();
		individual.add(MSPField.NAME);
		individual.add(MSPField.FORMULA);
		individual.add(MSPField.EXACTMASS);
		individual.add(MSPField.MW);
		individual.add(MSPField.INCHI_KEY);
		individual.add(MSPField.PRECURSORMZ);
		individual.add(MSPField.NUM_PEAKS);
		individual.add(MSPField.NOTES);

		System.out.println("Exporting " + Integer.toString(libIds.size()) + " library entries");
		int count = 0;
		ArrayList<String>entry = new ArrayList<String>();	
		for(String mrc2msmsId : libIds) {
			
			MsMsLibraryFeature feature = MSMSLibraryUtils.getMsMsLibraryFeatureById(mrc2msmsId);
			//	featuresToExport.add(feature);	
			entry.clear();
			CompoundIdentity cid = feature.getCompoundIdentity();
			entry.add(MSPField.NAME.getName() + ": " + cid.getName());

			if (cid.getFormula() != null)
				entry.add(MSPField.FORMULA.getName() + ": " + cid.getFormula());
			entry.add(MSPField.EXACTMASS.getName() + ": "
					+ MRC2ToolBoxConfiguration.getMzFormat().format(cid.getExactMass()));
			entry.add(MSPField.MW.getName() + ": " + 
					Integer.toString((int) Math.round(cid.getExactMass())));
			if (cid.getInChiKey() != null)
				entry.add(MSPField.INCHI_KEY.getName() + ": " + cid.getInChiKey());

			Map<String, String> properties = feature.getProperties();
			entry.add(MSPField.NIST_NUM.getName() + ": " + 
					properties.get(MSMSComponentTableFields.ORIGINAL_LIBRARY_ID.getName()));
							
			for (MSMSComponentTableFields field : MSMSComponentTableFields.values()) {

				if (individual.contains(field.getMSPField()))
					continue;
				
				String prop = properties.get(field.getName());
				if(prop == null || prop.isEmpty())
					continue;
					
				entry.add(field.getMSPField().getName() + ": " + prop);					
			}
			if(feature.getParent() == null) {
				
				double precmz = feature.getSpectrum().stream().mapToDouble(p -> p.getMz()).max().orElse(cid.getExactMass());
				entry.add(MSPField.PRECURSORMZ.getName() + ": "
						+ MRC2ToolBoxConfiguration.getMzFormat().format(precmz));
			}
			else {
				entry.add(MSPField.PRECURSORMZ.getName() + ": "
						+ MRC2ToolBoxConfiguration.getMzFormat().format(feature.getParent().getMz()));
			}
			entry.add(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(feature.getSpectrum().size()));

			for(MsPoint point : feature.getSpectrum()) {

				String msmsLine = MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
						+ " " + intensityFormat.format(point.getIntensity());
				String annotation = feature.getMassAnnotations().get(point);
				if(annotation != null)
					msmsLine += " \"" +  annotation + "\"";
				
				entry.add(msmsLine) ;
			}
			entry.add(" ");			
			try {
			    Files.write(mspFilePath, 
			    		entry, 
			    		StandardOpenOption.WRITE, StandardOpenOption.APPEND);
			    //StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			} catch (IOException e) {
			   e.printStackTrace();
			}
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 2500 == 0)
				System.out.println(".");
		}				
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void moveNist20DataToLibraryTables()  throws Exception {				
		//hr_msms_nist	
		Connection conn = ConnectionManager.getConnection();
		
		//	Get list of new NIST IDs to insert;
		String sql = 
				"SELECT L.NIST_ID " +
				"FROM NIST_LIBRARY_COMPONENT L  " +
				"WHERE L.NIST_ID NOT IN ( " +
				"SELECT I.ORIGINAL_LIBRARY_ID " +
				"FROM REF_MSMS_LIBRARY_COMPONENT I " +
				"WHERE I.LIBRARY_NAME = 'nist_msms') " +
				"ORDER BY 1 ";		
		PreparedStatement ps = conn.prepareStatement(sql);	
		String componentUpdateQuery =
				"INSERT INTO REF_MSMS_LIBRARY_COMPONENT ( " +
				"MRC2_LIB_ID, POLARITY, IONIZATION, COLLISION_ENERGY, COLLISION_GAS, INSTRUMENT,  " +
				"INSTRUMENT_TYPE, IN_SOURCE_VOLTAGE, MSN_PATHWAY, PRESSURE, SAMPLE_INLET,  " +
				"SPECIAL_FRAGMENTATION, SPECTRUM_TYPE, SPECTRUM_SOURCE, LIBRARY_NAME,  " +
				"ORIGINAL_LIBRARY_ID, SPECTRUM_HASH, MAX_DIGITS, ENTROPY) " +
				"SELECT ?, ION_MODE, IONIZATION, COLLISION_ENERGY, COLLISION_GAS,  " +
				"INSTRUMENT, INSTRUMENT_TYPE, IN_SOURCE_VOLTAGE, MSN_PATHWAY, PRESSURE,  " +
				"SAMPLE_INLET, SPECIAL_FRAGMENTATION, SPECTRUM_TYPE, ?, ?, NIST_ID,  " +
				"SPECTRUM_HASH, MAX_DIGITS, ENTROPY " +
				"FROM NIST_LIBRARY_COMPONENT WHERE NIST_ID = ? ";
		PreparedStatement comps = conn.prepareStatement(componentUpdateQuery);
		
		String msmsUpdateQuery =
				"INSERT INTO REF_MSMS_LIBRARY_PEAK ( " +
				"MRC2_LIB_ID, MZ, INTENSITY, FRAGMENT_COMMENT, IS_PARENT, MZ_ACCURACY) " +
				"SELECT ?, MZ, INTENSITY, ADDUCT, IS_PARENT, MZ_ACCURACY " +
				"FROM NIST_LIBRARY_PEAK WHERE NIST_ID = ? ";
		PreparedStatement msmsps = conn.prepareStatement(msmsUpdateQuery);
		
		String propQuery =
				"INSERT INTO REF_MSMS_PROPERTIES ( " +
				"MRC2_LIB_ID, PROPERTY_NAME, PROPERTY_VALUE) " +
				"SELECT ?, 'Annotation', NOTE_TEXT " +
				"FROM NIST_LIBRARY_ANNOTATION WHERE NIST_ID = ? ";
		PreparedStatement propps = conn.prepareStatement(propQuery);
		
		String cpdQuery =
				"INSERT INTO REF_MSMS_COMPOUND_DATA ( " +
				"MRC2_LIB_ID, NAME, FORMULA, INCHI_KEY, SMILES, ACCESSION) " +
				"SELECT ?, NAME, FORMULA, INCHI_KEY, SMILES, ACCESSION " +
				"FROM NIST_COMPOUND_DATA WHERE NIST_ID = ? ";
		PreparedStatement cpdps = conn.prepareStatement(cpdQuery);
				
		comps.setString(2, "EXPERIMENTAL");
		comps.setString(3, "hr_msms_nist");
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			String nistId = rs.getString(1);
			comps.setString(4, nistId);
			String nextmrcId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_LIB_ENTRY_SEQ",
					DataPrefix.MSMS_LIBRARY_ENTRY,
					"0",
					9);
			//	System.out.println(nextmrcId);
			comps.setString(1, nextmrcId);
			comps.executeUpdate();
			
			msmsps.setString(1, nextmrcId);
			msmsps.setString(2, nistId);
			msmsps.executeUpdate();
			
			propps.setString(1, nextmrcId);
			propps.setString(2, nistId);
			propps.executeUpdate();
			
			cpdps.setString(1, nextmrcId);
			cpdps.setString(2, nistId);
			cpdps.executeUpdate();
			
			count++;
			if(count % 50 == 0)
				System.out.println(Integer.toString(count) + " . ");
			if(count % 2000 == 0)
				System.out.println("");
		}
		rs.close();
		ps.close();	
		comps.close();
		propps.close();
		cpdps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addNist20SpectrumHashAndEntropy() throws Exception {

		Connection conn = ConnectionManager.getConnection();

		String idquery = "SELECT NIST_ID FROM NIST_LIBRARY_COMPONENT ";
				//	+ "WHERE SPECTRUM_HASH IS NULL";
		PreparedStatement idps = conn.prepareStatement(idquery);

		String updQuery = "UPDATE NIST_LIBRARY_COMPONENT SET "
				+ "SPECTRUM_HASH = ?, ENTROPY = ? WHERE NIST_ID = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);

		String query =
			"SELECT MZ, INTENSITY FROM NIST_LIBRARY_PEAK WHERE NIST_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		ResultSet idRes = idps.executeQuery();
		int counter = 0;
		int thouCounter = 0;
		while(idRes.next()) {

			String id = idRes.getString("NIST_ID");
			ps.setString(1, id);
			Collection<MsPoint>spectrum = new ArrayList<MsPoint>();
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				spectrum.add(new MsPoint(rs.getDouble("MZ"), rs.getDouble("INTENSITY")));

			String spectrumHash = MsUtils.calculateSpectrumHash(spectrum);
			double entropy = MsUtils.calculateSpectrumEntropy(spectrum);
			updPs.setString(1, spectrumHash);
			updPs.setDouble(2, entropy);
			updPs.setString(3, id);
			updPs.addBatch();
			counter++;
			if(counter % 1000 == 0) {
				updPs.executeBatch();
				updPs.clearBatch();
				thouCounter++;
				System.out.println(Integer.toString(thouCounter));
				counter = 0;
			}
		}
		updPs.executeBatch();
		updPs.clearBatch();
		updPs.close();
		idRes.close();
		idps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void updateNist20ManualAccessions()  throws Exception {
		
		File nameIdMapfile = new File("C:\\Users\\Sasha\\Downloads\\NIST20-name-id-map.txt");
		Reader inputStreamReader = new InputStreamReader(new FileInputStream(nameIdMapfile));
		String[][] parsednameIdMapData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>idNameMap = new TreeMap<String,String>();
		for(int i=1; i<parsednameIdMapData.length; i++)
			idNameMap.put(parsednameIdMapData[i][1], parsednameIdMapData[i][0]);
			
		File idRemapfile = new File("C:\\Users\\Sasha\\Downloads\\NIST20-id-remap.txt");
		inputStreamReader = new InputStreamReader(new FileInputStream(idRemapfile));
		String[][] parsedIdMRemapData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>idReMap = new TreeMap<String,String>();
		for(int i=1; i<parsedIdMRemapData.length; i++)
			idReMap.put(parsedIdMRemapData[i][0], parsedIdMRemapData[i][1]);
		
		System.out.println("***");
		Connection conn = ConnectionManager.getConnection();
		String sql = "UPDATE NIST_COMPOUND_DATA SET ACCESSION = ? WhERE NAME = ? AND ACCESSION IS NULL";
		PreparedStatement ps = conn.prepareStatement(sql);		
		for(Entry<String, String> entry : idNameMap.entrySet()) {
			
			String accession = entry.getValue();
			String existingAccession = idReMap.get(accession);
			if(existingAccession != null)
				accession = existingAccession;
			
			ps.setString(1, accession);
			ps.setString(2, entry.getKey());
			ps.executeUpdate();
		}
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void fetchMissingNIST2020CompoundDataFromPubChemBySmiles() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT DISTINCT SMILES FROM NIST_COMPOUND_DATA WHERE ACCESSION IS NULL AND SMILES IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(sql);	
		
		String updSql = "UPDATE NIST_COMPOUND_DATA SET ACCESSION = ? WHERE SMILES = ? AND ACCESSION IS NULL";
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String smiles = rs.getString("SMILES");
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/" + smiles + "/record/SDF";

			IteratingSDFReader reader = null;
			IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
			InputStream pubchemDataStream = null;
			InputStream synonymStream = null;
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
			} catch (Exception e) {
				//	e.printStackTrace();
			}
			if(pubchemDataStream != null) {
				reader = new IteratingSDFReader(pubchemDataStream, coBuilder);
				while (reader.hasNext()) {
					IAtomContainer molecule = (IAtomContainer)reader.next();
					String cid = molecule.getProperty(PubChemFields.PUBCHEM_ID.toString());
					if(cid == null) {
						System.out.println("Not found " + smiles);
						continue;
					}
					try {
						synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/synonyms/TXT");
					} catch (Exception e) {
						//	e.printStackTrace();
					}
					String[] synonyms = new String[0];
					if(synonymStream != null) {
						try {
							synonyms = IOUtils.toString(synonymStream, StandardCharsets.UTF_8).split("\\r?\\n");
						}
						catch (Exception e) {
							//	e.printStackTrace();
						}
					}
					if(synonyms.length == 0)
						synonyms = new String[] {cid};
					
					PubChemCompoundDescriptionBundle descBundle =  getCompoundDescription(cid);
					CompoundIdentity inserted = null;
					try {
						inserted = PubChemParser.insertPubchemRecord(molecule, synonyms, descBundle, conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(inserted != null) {
						updPs.setString(1, inserted.getPrimaryDatabaseId());
						updPs.setString(2, smiles);
						updPs.executeUpdate();
					}
				}
			}
			else {
				System.out.println(smiles);
			}
		}
		rs.close();
		updPs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void fetchMissingNIST2020CompoundDataFromPubChem() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT DISTINCT INCHI_KEY FROM NIST_COMPOUND_DATA "
				+ "WHERE ACCESSION IS NULL AND INCHI_KEY IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(sql);	
		
		String updSql = "UPDATE NIST_COMPOUND_DATA SET ACCESSION = ? WHERE INCHI_KEY = ? AND ACCESSION IS NULL";
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String inchikey = rs.getString("INCHI_KEY");
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchikey + "/record/SDF";

			IteratingSDFReader reader = null;
			IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
			InputStream pubchemDataStream = null;
			InputStream synonymStream = null;
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
			} catch (Exception e) {
				//	e.printStackTrace();
			}
			if(pubchemDataStream != null) {
				reader = new IteratingSDFReader(pubchemDataStream, coBuilder);
				while (reader.hasNext()) {
					IAtomContainer molecule = (IAtomContainer)reader.next();
					String cid = molecule.getProperty(PubChemFields.PUBCHEM_ID.toString());
					try {
						synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/synonyms/TXT");
					} catch (Exception e) {
						//	e.printStackTrace();
					}
					String[] synonyms = new String[0];
					if(synonymStream != null) {
						try {
							synonyms = IOUtils.toString(synonymStream, StandardCharsets.UTF_8).split("\\r?\\n");
						}
						catch (Exception e) {
							//	e.printStackTrace();
						}
					}
					if(synonyms.length == 0)
						synonyms = new String[] {cid};
					
					PubChemCompoundDescriptionBundle descBundle =  getCompoundDescription(cid);
					CompoundIdentity inserted = null;
					try {
						inserted = PubChemParser.insertPubchemRecord(molecule, synonyms, descBundle, conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(inserted != null) {
						updPs.setString(1, inserted.getPrimaryDatabaseId());
						updPs.setString(2, inchikey);
						updPs.executeUpdate();
					}
				}
			}
			else {
				System.out.println(inchikey);
			}
		}
		rs.close();
		updPs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void updayeNist2020bySmiles() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String sql = "SELECT DISTINCT SMILES FROM NIST_COMPOUND_DATA WHERE ACCESSION IS NULL AND SMILES IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		String sql2 = "SELECT ACCESSION FROM COMPOUND_DATA WHERE SMILES = ?";
		PreparedStatement ps2 = conn.prepareStatement(sql2);
		
		String updSql = "UPDATE NIST_COMPOUND_DATA SET ACCESSION = ? WHERE SMILES = ? AND ACCESSION IS NULL";
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String smiles = rs.getString("SMILES");
			ps2.setString(1, smiles);
			ResultSet assRs = ps2.executeQuery();
			while(assRs.next()) {
				updPs.setString(1, assRs.getString("ACCESSION"));
				updPs.setString(2, smiles);
				updPs.executeUpdate();
				break;
			}
			assRs.close();
		}
		updPs.close();
		ps.close();
		ps2.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	@SuppressWarnings("deprecation")
	private static void addMissingInchiKeys() throws Exception {
		
		Path missingInchiFilePath = Paths.get("C:\\Users\\Sasha\\Downloads", "inchi-without-key-export.txt");
		INChIContentProcessorTool inchiTool = new INChIContentProcessorTool();
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> allInchis = null;		
		try {
			allInchis = Files.readAllLines(missingInchiFilePath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Connection conn = ConnectionManager.getConnection();
		String sql = "UPDATE COMPOUND_DATA SET INCHI_KEY = ? WHERE INCHI_KEY IS NULL AND INCHI = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		for(String line : allInchis) {

            if (line.startsWith("INChI=") || line.startsWith("InChI=")) {

                final String INChI = line.substring(6);
                StringTokenizer tokenizer = new StringTokenizer(INChI, "/");
                // ok, we expect 4 tokens
                tokenizer.nextToken(); // 1.12Beta not stored since never used
                final String formula = tokenizer.nextToken(); // C6H6
                String connections = null;
                if (tokenizer.hasMoreTokens()) {
                    connections = tokenizer.nextToken().substring(1); // 1-2-4-6-5-3-1
                }
                //final String hydrogens = tokenizer.nextToken().substring(1); // 1-6H
				IAtomContainer parsedContent = inchiTool.processFormula(builder.newInstance(IAtomContainer.class), formula);
                if (connections != null) {
                    try {
						inchiTool.processConnections(connections, parsedContent, -1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//	e.printStackTrace();
						System.out.println("Could not convert " + line);
					}
                }
    			String inchiKey = null;
    			try {
    				inChIGenerator = igfactory.getInChIGenerator(parsedContent);
    				INCHI_RET ret = inChIGenerator.getReturnStatus();
    				if (ret == INCHI_RET.WARNING || ret == INCHI_RET.OKAY)
    					inchiKey = inChIGenerator.getInchiKey();
    			}
    			catch (Exception e) {
    				// TODO Auto-generated catch block
    				// e.printStackTrace();
    				System.out.println("Could not convert " + line);
    			}
    			if(inchiKey == null) {
    				System.out.println("Could not convert " + line);
    				continue;
    			}
    			ps.setString(1, inchiKey);
    			ps.setString(2, line);
    			ps.executeUpdate();
            }
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void normalizeNist17Sdf() {
		
		String mspDir = "E:\\DataAnalysis\\Databases\\NIST\\NIST17-export\\Export\\MSMS\\MSP";
		String sdfDir = "E:\\DataAnalysis\\Databases\\NIST\\NIST17-export\\Export\\MSMS\\SDF";		
		NISTDataUploader nistUpl = new NISTDataUploader(mspDir, sdfDir, NISTReferenceLibraries.nist_msms);
		
		try {
			nistUpl.normalizeSdfData(Paths.get("E:\\DataAnalysis\\Databases\\NIST\\NIST17-export\\Export\\MSMS\\SDF_NORM").toFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	private static void nist20upload() {
		
		String mspDir = "E:\\DataAnalysis\\Databases\\NIST20\\EXPORT\\MSP_TODO";
		String sdfDir = "E:\\DataAnalysis\\Databases\\NIST20\\EXPORT\\SDF_TODO";
//		String mspDir = "E:\\DataAnalysis\\Databases\\NIST20\\EXPORT\\MSP";
//		String sdfDir = "E:\\DataAnalysis\\Databases\\NIST20\\EXPORT\\SDF_NORM";
		
		NISTDataUploader nistUpl = new NISTDataUploader(mspDir, sdfDir, NISTReferenceLibraries.hr_msms_nist);
		nistUpl.mapDataFiles();
		try {
			//	nistUpl.normalizeSdfData(Paths.get("E:\\DataAnalysis\\Databases\\NIST20\\EXPORT\\SDF_NORM").toFile());
			nistUpl.uploadNistDataNoStructure();
		//	nistUpl.uploadNistData();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String mrc2msmsId = "MSL004020149";
//		try {
//			MsMsLibraryFeature feature = MSMSLibraryUtils.getMsMsLibraryFeatureById(mrc2msmsId);
//			System.out.println(mrc2msmsId);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private static void matchMs2Ms1forIdda() {
		
		String samplePrepId = "SPR0061";
		String sampleId = "IDS000077";
		String daMethodId = "DXM0198";
		String acquisitionMethodId = "DQM0559";	//	DQM0558 for NEG
		Polarity polarity = Polarity.Positive;
		String dataFileName = "NIST4-POS.txt";
		
		try {
			matchIddaData(
					samplePrepId,
					sampleId,
					daMethodId,
					acquisitionMethodId,
					polarity,
					dataFileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void exportLibrary() {
		
		Collection<MsMsLibraryFeature> featuresToExport = null;
		try {
			featuresToExport = fetchLibraryFeatures();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(featuresToExport != null) {
			
			File exportFile = new File("C:\\Users\\Sasha\\Downloads\\_2_rename\\NIST_MSMS2_NEG_HIGHRES.MSP");
			String libraryId = "nist_msms";
			try {
				writeMspFile(featuresToExport, exportFile, libraryId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static Collection<MsMsLibraryFeature> fetchLibraryFeatures() throws Exception {
		
		String libraryId = "nist_msms2";
		Polarity polarity = Polarity.Negative;
		MSMSResolution resolution = MSMSResolution.HIGH; 
		SpectrumSource spectrumSource = SpectrumSource.EXPERIMENTAL;
		
		Collection<MsMsLibraryFeature>featuresToExport = new ArrayList<MsMsLibraryFeature>();
		
		Connection conn = ConnectionManager.getConnection();
		Collection<String>libIds = MSMSLibraryUtils.getFilteredLibraryIds(
				 libraryId, 
				 polarity, 
				 resolution, 
				 spectrumSource,
				 conn);		
		
		System.out.println("Getting feature data for " + Integer.toString(libIds.size()) + " features");
		int count = 0;
		if(!libIds.isEmpty()) {
						
			for(String mrc2msmsId : libIds) {
				MsMsLibraryFeature feature = MSMSLibraryUtils.getMsMsLibraryFeatureById(mrc2msmsId);
				featuresToExport.add(feature);	
				count++;
				if(count % 50 == 0)
					System.out.print(".");
				if(count % 2000 == 0)
					System.out.println(".");
			}			
		}	
		ConnectionManager.releaseConnection(conn);	
		return featuresToExport;
	}
	
	private static void writeMspFile(
			Collection<MsMsLibraryFeature>featuresToExport, 
			File exportFile,
			String libraryId) throws IOException {
		
		if(featuresToExport.isEmpty())
			return;
		
		System.out.println("Writing MSP file");
		int count = 0;
		Collection<MSPField>individual = new ArrayList<MSPField>();
		individual.add(MSPField.NAME);
		individual.add(MSPField.FORMULA);
		individual.add(MSPField.EXACTMASS);
		individual.add(MSPField.MW);
		individual.add(MSPField.INCHI_KEY);
		individual.add(MSPField.PRECURSORMZ);
		individual.add(MSPField.NUM_PEAKS);
			
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		for(MsMsLibraryFeature feature : featuresToExport) {
	
			CompoundIdentity cid = feature.getCompoundIdentity();
			writer.append(MSPField.NAME.getName() + ": " + cid.getName() + "\n");

			if (cid.getFormula() != null)
				writer.append(MSPField.FORMULA.getName() + ": " + cid.getFormula() + "\n");
			writer.append(MSPField.EXACTMASS.getName() + ": "
					+ MRC2ToolBoxConfiguration.getMzFormat().format(cid.getExactMass()) + "\n");
			writer.append(MSPField.MW.getName() + ": " + 
					Integer.toString((int) Math.round(cid.getExactMass())) + "\n");
			if (cid.getInChiKey() != null)
				writer.append(MSPField.INCHI_KEY.getName() + ": " + cid.getInChiKey() + "\n");

			Map<String, String> properties = feature.getProperties();
			if(libraryId.equals("nist_msms") && properties.get(MSMSComponentTableFields.ORIGINAL_LIBRARY_ID.getName()) != null)
				writer.append(MSPField.NIST_NUM.getName() + ": " + 
						properties.get(MSMSComponentTableFields.ORIGINAL_LIBRARY_ID.getName()) + "\n");
							
			for (MSMSComponentTableFields field : MSMSComponentTableFields.values()) {

				if (individual.contains(field.getMSPField()))
					continue;
				
				String prop = properties.get(field.getName());
				if(prop == null || prop.isEmpty())
					continue;
					
				writer.append(field.getMSPField().getName() + ": " + prop + "\n");				
			}
			writer.append(MSPField.PRECURSORMZ.getName() + ": "
					+ MRC2ToolBoxConfiguration.getMzFormat().format(feature.getParent().getMz()) + "\n");
			writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(feature.getSpectrum().size()) + "\n");

			for(MsPoint point : feature.getSpectrum()) {

				writer.append(
					MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
					+ " " + intensityFormat.format(point.getIntensity())) ;
				
				String annotation = feature.getMassAnnotations().get(point);
				if(annotation != null)
					writer.append(" \"" + annotation + "\"");

				writer.append("\n");
			}
			writer.append("\n\n");
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			if(count % 2000 == 0)
				System.out.println(".");
		}
		writer.flush();
		writer.close();
	}
		
	private static void fixMissingPubchemPrimaryNamesFromSynonyms() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();

		String idQuery =
			"SELECT ACCESSION FROM COMPOUND_DATA WHERE PRIMARY_NAME IS NULL ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(idQuery);
		
		String nameQuery = 
				"SELECT S.NAME  " +
				"FROM COMPOUND_DATA C, " +
				"COMPOUND_SYNONYMS S " +
				"WHERE C.PRIMARY_NAME IS NULL  " +
				"AND C.ACCESSION = S.ACCESSION " +
				"AND S.NTYPE = 'IUP' " +
				"AND C.ACCESSION = ? " +
				"ORDER BY 1 ";
		PreparedStatement nameps = conn.prepareStatement(nameQuery);
		
		String updQueryOne = "UPDATE COMPOUND_DATA SET PRIMARY_NAME = ? WHERE ACCESSION = ?";
		PreparedStatement updpsOne = conn.prepareStatement(updQueryOne);
		
		String updQueryTwo = "UPDATE COMPOUND_SYNONYMS SET NTYPE = 'PRI' WHERE NAME =? AND ACCESSION = ?";
		PreparedStatement updpsTwo = conn.prepareStatement(updQueryTwo);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String cid = rs.getString("ACCESSION");
			//	PubChemCompoundDescriptionBundle bundle = getCompoundDescription(cid);
			nameps.setString(1, cid);
			ResultSet nameRes = nameps.executeQuery();
			while(nameRes.next()) {
				
				String name = nameRes.getString("NAME");
				
				updpsOne.setString(1, name);
				updpsOne.setString(2, cid);
				updpsOne.executeUpdate();
				
				updpsTwo.setString(1, name);
				updpsTwo.setString(2, cid);
				updpsTwo.executeUpdate();
				
				break;
			}
			nameRes.close();
		}
		rs.close();
		updpsOne.close();
		updpsTwo.close();
		nameps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static PubChemCompoundDescriptionBundle getCompoundDescription(String cid) {
		
		InputStream descStream = null;
		try {
			descStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/description/XML");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(descStream == null)
			return null;
			
		Document xmlDocument = XmlUtils.readXmlStream(descStream);		
		String title = ((Element) xmlDocument.getElementsByTagName("Title").item(0)).getTextContent();
		PubChemCompoundDescriptionBundle bundle = new PubChemCompoundDescriptionBundle(cid, title);
		NodeList infoElements = xmlDocument.getElementsByTagName("Information");
		for(int i=1; i<infoElements.getLength(); i++) {
			
			Element infoElement = (Element) infoElements.item(i);
			String descriptionText = ((Element) infoElement.getElementsByTagName("Description").item(0)).getTextContent();
			String sourceName = ((Element) infoElement.getElementsByTagName("DescriptionSourceName").item(0)).getTextContent();
			String url = ((Element) infoElement.getElementsByTagName("DescriptionURL").item(0)).getTextContent();
			
			PubChemCompoundDescription desc = new PubChemCompoundDescription(descriptionText, sourceName, url);
			bundle.addDescription(desc);
		}
		return bundle;
	}


	
	
	private static void calculateEntropyForMsmsFeatures() throws Exception {
		
		System.out.println("Updating total intensity and entropy data ...");
		Connection conn = ConnectionManager.getConnection();
		String sql = "SELECT MSMS_FEATURE_ID FROM MSMS_FEATURE";
		PreparedStatement ps = conn.prepareStatement(sql);
		String msSql = "SELECT HEIGHT FROM MSMS_FEATURE_PEAK WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement msps = conn.prepareStatement(msSql);
		String updSql = "UPDATE MSMS_FEATURE SET TOTAL_INTENSITY = ?, ENTROPY = ? WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement updps = conn.prepareStatement(updSql);
		String fid = null;		
		ArrayList<Double>intensities = null;
		int counter = 0;
		ResultSet pres = null;
		ResultSet fres = ps.executeQuery();	
		while(fres.next()) {
			intensities = new ArrayList<Double>();
			fid = fres.getString(1);
			msps.setString(1, fid);
			pres = msps.executeQuery();
			while(pres.next())
				intensities.add(pres.getDouble(1));
			
			pres.close();
			double totalIntensity = intensities.stream().mapToDouble(Double::doubleValue).sum();
			double entropy = calculateSpectrumEntropy(intensities, totalIntensity);
			updps.setDouble(1, totalIntensity);
			updps.setDouble(2, entropy);
			updps.setString(3, fid);
			updps.executeUpdate();
			counter++;
			if(counter % 50000 == 0)
				System.out.println(counter);
		}
		fres.close();
		msps.close();
		ps.close();	
		updps.close();
		ConnectionManager.releaseConnection(conn);
	}

	
	public static void insertDocument() throws Exception {
		
		File docFile = new File("C:\\Users\\Sasha\\Downloads\\Pavlic2006_Article_CombinedUseOfESIQqTOF-MSAndESI.pdf");
		DocumentFormat format = DocumentFormat.PDF;
		String documentTitle = "Test document upload";
		String docId = DocumentUtils.insertDocument(docFile, documentTitle, format);
		System.out.println(docId);
	}

	public static void insertNISTPepAsCompoundRecord() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NISTP_ID_IK.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());

		Connection conn = ConnectionManager.getConnection();
		Connection cpdConn = CompoundDbConnectionManager.getConnection();

		//	Insert primary data
		String dataQuery =
			"INSERT INTO COMPOUND_DATA " +
			"(ACCESSION, SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS, "
			+ "SMILES, INCHI_KEY) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = cpdConn.prepareStatement(dataQuery);

		String selectQuery =
			"SELECT DISTINCT D.NAME, D.FORMULA, D.SMILES  " +
			"FROM REF_MSMS_COMPOUND_DATA D, " +
			"REF_MSMS_LIBRARY_COMPONENT C " +
			"WHERE C.LIBRARY_NAME='nist_msms2' AND C.ORIGINAL_LIBRARY_ID = ? " +
			"AND D.MRC2_LIB_ID = C.MRC2_LIB_ID ";
		PreparedStatement selPs = conn.prepareStatement(selectQuery);

		String updQuery =
				"UPDATE REF_MSMS_COMPOUND_DATA SET ACCESSION = ? "
				+ "WHERE INCHI_KEY = ? AND ACCESSION IS NULL";
		PreparedStatement auPs = conn.prepareStatement(updQuery);

		for(int i=1; i<parsedData.length; i++) {

			String id = parsedData[i][1];
			String dbNum = id.replace("NISTP:", "");
			String inchiKey = parsedData[i][0];
			selPs.setString(1, dbNum);
			ResultSet rs = selPs.executeQuery();
			while(rs.next()) {

				IMolecularFormula mf =
					MolecularFormulaManipulator.getMolecularFormula(rs.getString("FORMULA"), builder);
				//	double exactMass = MolecularFormulaManipulator.getMass(mf, MolecularFormulaManipulator.MonoIsotopic);
				double exactMass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);

				ps.setString(1, id);
				ps.setString(2, CompoundDatabaseEnum.NIST_MS_PEP.name());
				ps.setString(3, rs.getString("NAME"));
				ps.setString(4, rs.getString("FORMULA"));
				ps.setDouble(5, exactMass);
				ps.setString(6, rs.getString("SMILES"));
				ps.setString(7, inchiKey);
				ps.executeUpdate();
			}
		}
		ps.close();
		auPs.close();
		selPs.close();
		ConnectionManager.releaseConnection(conn);
		CompoundDbConnectionManager.releaseConnection(cpdConn);
	}

	public static void addSpectrumHash() throws Exception {

		Connection conn = ConnectionManager.getConnection();

		String idquery = "SELECT MRC2_LIB_ID FROM REF_MSMS_LIBRARY_COMPONENT WHERE SPECTRUM_HASH IS NULL";
		PreparedStatement idps = conn.prepareStatement(idquery);

		String updQuery = "UPDATE REF_MSMS_LIBRARY_COMPONENT SET SPECTRUM_HASH = ? WHERE MRC2_LIB_ID = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);

		String query =
			"SELECT MZ, INTENSITY FROM REF_MSMS_LIBRARY_PEAK WHERE MRC2_LIB_ID = ? ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);

		ResultSet idRes = idps.executeQuery();
		int counter = 0;
		int thouCounter = 0;
		while(idRes.next()) {

			String id = idRes.getString("MRC2_LIB_ID");
			ps.setString(1, id);
			Collection<MsPoint>spectrum = new ArrayList<MsPoint>();
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				spectrum.add(new MsPoint(rs.getDouble("MZ"), rs.getDouble("INTENSITY")));

			String spectrumHash = MsUtils.calculateSpectrumHash(spectrum);
			updPs.setString(1, spectrumHash);
			updPs.setString(2, id);
			updPs.addBatch();
			counter++;
			if(counter % 1000 == 0) {
				updPs.executeBatch();
				updPs.clearBatch();
				thouCounter++;
				System.out.println(Integer.toString(thouCounter));
				counter = 0;
			}
		}
		updPs.executeBatch();
		updPs.clearBatch();
		updPs.close();
		idRes.close();
		idps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void addNistCompoundDataFromPubChem() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		IteratingSDFReader reader;
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
		InputStream pubchemDataStream = null;
		InputStream synonymStream = null;
		String requestUrl = null;
		String id = null;
		String idQuery =
			"SELECT DISTINCT L.ACCESSION " +
			"FROM REF_MSMS_LIBRARY_COMPONENT L  " +
			"LEFT JOIN  COMPOUND_DATA I ON L.ACCESSION = I.ACCESSION " +
			"WHERE I.ACCESSION IS NULL " +
			"AND L.LIBRARY_NAME ='nist_msms' " +
			"AND L.ACCESSION NOT LIKE 'NIST%' " +
			"ORDER BY 1 ";
		PreparedStatement idps = conn.prepareStatement(idQuery);

		// INCHI-KEY check
		String inchiQuery =
			"SELECT ACCESSION FROM COMPOUND_DATA WHERE INCHI_KEY = ?";
		PreparedStatement inchips = conn.prepareStatement(inchiQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE,
		         ResultSet.CONCUR_UPDATABLE);

		String accessionUpdateQuery = "UPDATE REF_MSMS_LIBRARY_COMPONENT SET ACCESSION = ? WHERE ACCESSION = ?";
		PreparedStatement auPs = conn.prepareStatement(accessionUpdateQuery);

		ResultSet idrs = idps.executeQuery();
		while(idrs.next()) {
			id = idrs.getString(1);
			requestUrl = pubchemCidUrl + id + "/SDF";
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(pubchemDataStream != null) {
				reader = new IteratingSDFReader(pubchemDataStream, coBuilder);
				while (reader.hasNext()) {
					IAtomContainer molecule = (IAtomContainer)reader.next();
					Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
					molecule.getProperties().forEach((k,v)->{

						if(pubchemDataMap.containsKey(k.toString()))
							pubchemDataMap.put(k.toString(), v.toString());
					});
					String inchiKey = pubchemDataMap.get(PubChemFields.INCHIKEY.toString());
					inchips.setString(1, inchiKey);
					ResultSet inchiRs = inchips.executeQuery();
					if(inchiRs.first()) {
						//	ID already in
						String currAccession = inchiRs.getString(1);
						auPs.setString(1, currAccession);
						auPs.setString(2, id);
						auPs.executeUpdate();
						System.out.println(id + " replaced by existing " + currAccession);
						inchiRs.close();
					}
					else {
						inchiRs.close();
						try {
							synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + id + "/synonyms/TXT");
						} catch (Exception e) {
							e.printStackTrace();
						}
						String[] synonyms = new String[0];
						if(synonymStream != null) {
							try {
								synonyms = IOUtils.toString(synonymStream).split("\\r?\\n");
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						if(synonyms.length == 0)
							synonyms = new String[] {id};
						PubChemCompoundDescriptionBundle bundle = getCompoundDescription(id);
						try {
							PubChemParser.insertPubchemRecord(molecule, synonyms, bundle, conn);
							System.out.println("Inserted " + id);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		idrs.close();
		auPs.close();
		idps.close();
		inchips.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void addNistMsToCrossref() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Connection cpdConn = CompoundDbConnectionManager.getConnection();
		String dataQuery =
			"SELECT DISTINCT 'NIST:' || ORIGINAL_LIBRARY_ID AS NIST_MS_ID, ACCESSION  " +
			"FROM REF_MSMS_LIBRARY_COMPONENT " +
			"WHERE LIBRARY_NAME ='nist_msms' " +
			"AND ACCESSION NOT LIKE 'NIST%' " +
			"ORDER BY 1 " ;
		PreparedStatement dataps = conn.prepareStatement(dataQuery);
		ResultSet datars = dataps.executeQuery();

		String insertQuery =
			"INSERT INTO COMPOUND_CROSSREF (ACCESSION, SOURCE_DB, SOURCE_DB_ID) VALUES (?, 'NIST_MS', ?)";
		PreparedStatement insertps = cpdConn.prepareStatement(insertQuery);

		String checkQuery =
				"SELECT SOURCE_DB_ID FROM COMPOUND_CROSSREF WHERE ACCESSION = ? AND SOURCE_DB = 'NIST_MS'";
		PreparedStatement checkps = cpdConn.prepareStatement(checkQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE,
		         ResultSet.CONCUR_UPDATABLE);
		ResultSet checkrs = null;
		while(datars.next()) {

			//	Check if ref already in
			String accession = datars.getString("ACCESSION");
			String nistId = datars.getString("NIST_MS_ID");
			checkps.setString(1, accession);
			checkrs = checkps.executeQuery();
			if(checkrs.first()) {
				System.out.println(nistId + " already referenced to " + accession);
				checkrs.close();
				continue;
			}
			else {
				checkrs.close();
				insertps.setString(1, accession);
				insertps.setString(2, nistId);
				insertps.executeUpdate();
				System.out.println(nistId + " => " + accession);
			}
		}
		datars.close();

		dataps.close();
		checkps.close();
		insertps.close();

		CompoundDbConnectionManager.releaseConnection(cpdConn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void addMetlinToCrossref() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Connection cpdConn = CompoundDbConnectionManager.getConnection();
		String dataQuery =
			"SELECT DISTINCT 'METLIN:' || ORIGINAL_LIBRARY_ID AS METLIN_ID, ACCESSION  " +
			"FROM REF_MSMS_LIBRARY_COMPONENT " +
			"WHERE LIBRARY_NAME ='METLIN' " +
			"AND ACCESSION NOT LIKE 'METLIN%' " +
			"ORDER BY 1";
		PreparedStatement dataps = conn.prepareStatement(dataQuery);
		ResultSet datars = dataps.executeQuery();

		String insertQuery =
			"INSERT INTO COMPOUND_CROSSREF (ACCESSION, SOURCE_DB, SOURCE_DB_ID) VALUES (?, 'METLIN', ?)";
		PreparedStatement insertps = cpdConn.prepareStatement(insertQuery);

		String checkQuery =
				"SELECT SOURCE_DB_ID FROM COMPOUND_CROSSREF WHERE ACCESSION = ? AND SOURCE_DB = 'METLIN'";
		PreparedStatement checkps = cpdConn.prepareStatement(checkQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE,
		         ResultSet.CONCUR_UPDATABLE);
		ResultSet checkrs = null;
		while(datars.next()) {

			//	Check if ref already in
			String accession = datars.getString("ACCESSION");
			String metlin = datars.getString("METLIN_ID");
			checkps.setString(1, accession);
			checkrs = checkps.executeQuery();
			if(checkrs.first()) {
				System.out.println(metlin + " already referenced to " + accession);
				checkrs.close();
				continue;
			}
			else {
				checkrs.close();
				insertps.setString(1, accession);
				insertps.setString(2, metlin);
				insertps.executeUpdate();
				System.out.println(metlin + " => " + accession);
			}
		}
		datars.close();

		dataps.close();
		checkps.close();
		insertps.close();

		CompoundDbConnectionManager.releaseConnection(cpdConn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void addNistManualSearchCompoundDataFromPubChem() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Connection cpdConn = CompoundDbConnectionManager.getConnection();
		String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS_UPD_NAME_ACCESSIONS_PUBCHEM.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());

		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		IteratingSDFReader reader;
		InputStream pubchemDataStream = null;
		InputStream synonymStream = null;

		// INCHI-KEY check
		String inchiQuery =
			"SELECT ACCESSION FROM COMPOUND_DATA WHERE INCHI_KEY = ?";
		PreparedStatement ps = cpdConn.prepareStatement(inchiQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE,
		         ResultSet.CONCUR_UPDATABLE);

		String updQuery =
			"UPDATE REF_MSMS_COMPOUND_DATA C SET C.ACCESSION = ? WHERE C.NAME = ? AND C.ACCESSION IS NULL";
		PreparedStatement cpdPs = conn.prepareStatement(updQuery);

		for(int i=1; i<parsedData.length; i++) {

			String requestUrl = pubchemCidUrl + parsedData[i][1] + "/SDF";
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(pubchemDataStream != null) {
				reader = new IteratingSDFReader(pubchemDataStream, coBuilder);
				while (reader.hasNext()) {
					IAtomContainer molecule = (IAtomContainer)reader.next();
					String cid = molecule.getProperty(PubChemFields.PUBCHEM_ID.toString());
					try {
						synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/synonyms/TXT");
					} catch (Exception e) {
						e.printStackTrace();
					}
					String[] synonyms = new String[0];
					if(synonymStream != null) {
						try {
							synonyms = IOUtils.toString(synonymStream).split("\\r?\\n");
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(synonyms.length == 0)
						synonyms = new String[] {cid};

					Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
					molecule.getProperties().forEach((k,v)->{

						if(pubchemDataMap.containsKey(k.toString()))
							pubchemDataMap.put(k.toString(), v.toString());
					});
					String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
					String inchiKey = pubchemDataMap.get(PubChemFields.INCHIKEY.toString());
					ps.setString(1, inchiKey);
					ResultSet rs = ps.executeQuery();
					if(rs.first()) {
						String currAccession = rs.getString(1);
						cpdPs.setString(1, currAccession);
						cpdPs.setString(2, parsedData[i][0]);
						cpdPs.executeUpdate();
						System.out.println(id + "\t" + rs.getString(1));
						rs.close();
					}
					else {
						if(!id.isEmpty()) {
							cpdPs.setString(1, id);
							cpdPs.setString(2, parsedData[i][0]);
							cpdPs.executeUpdate();
							PubChemCompoundDescriptionBundle bundle = getCompoundDescription(id);
							try {
								PubChemParser.insertPubchemRecord(molecule, synonyms, bundle, cpdConn);
								System.out.println(id + " was inserted");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		ps.close();
		cpdPs.close();
		ConnectionManager.releaseConnection(conn);
		CompoundDbConnectionManager.releaseConnection(cpdConn);
	}

	public static void addNistManualSearchCompoundData() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS_UPD_NAME_ACCESSIONS.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE REF_MSMS_COMPOUND_DATA C SET C.ACCESSION = ? WHERE C.NAME = ? AND C.ACCESSION IS NULL";

		PreparedStatement ps = conn.prepareStatement(query);

		for(int i=1; i<parsedData.length; i++) {
			ps.setString(1, parsedData[i][1]);
			ps.setString(2, parsedData[i][0]);
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}


	public static void addNistCompoundData() throws Exception {

		//	Read name-id list
		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS_NO_ACCESSION_NIST_ID.tsv")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		SmilesParser smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
		for(int i=1; i<parsedData.length; i++) {

			String name = parsedData[i][0];
			String inchiKey = parsedData[i][2];
			String smiles = parsedData[i][3];
			String nistId = parsedData[i][4];
			IAtomContainer molecule = null;
			try {
				molecule = smipar.parseSmiles(smiles);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (molecule != null) {
				CompoundIdentity newCompound = new CompoundIdentity(CompoundDatabaseEnum.NIST_MS, nistId);
				IMolecularFormula molFormula = MolecularFormulaManipulator.getMolecularFormula(molecule);
				newCompound.setCommonName(name);
				newCompound.setExactMass(MolecularFormulaManipulator.getTotalExactMass(molFormula));
				newCompound.setFormula(MolecularFormulaManipulator.getString(molFormula));
				newCompound.setSmiles(smiles);
				newCompound.setInChiKey(inchiKey);
				CompoundNameSet nameSet = new CompoundNameSet(nistId);
				nameSet.setPrimaryName(newCompound.getCommonName());
				CompoundIdentity inserted = null;
				try {
					inserted = CompoundDatabaseUtils.insertNewCompound(newCompound, nameSet, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void udateMetlinAccessionsFromFile() throws Exception {

		//	Insert INCHI keys
		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE METLIN_CPD_DATA SET ACCESSION = ? WHERE METLIN = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\metlin-with-manual-accessions.tsv")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		for(int i=1; i<parsedData.length; i++) {

			ps.setString(1, parsedData[i][1]);
			ps.setString(2, parsedData[i][0]);
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void parseMissingMSMSStructuresToMetlinFromPubChemById() throws Exception {

		File folder = Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_ID").toFile();
		String[] files = folder.list();
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		IteratingSDFReader reader;

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE METLIN_CPD_DATA SET SMILES = ?, PUBCHEM = ?, "
			+ "INCHI_KEY = ? WHERE CATALOG_NAME = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		String pcName;
		int count = 0;
		for (String file : files) {

			pcName = FilenameUtils.getBaseName(file);
			reader = new IteratingSDFReader(
					new FileInputStream(Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM\\NAME", file).toFile()), coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
				Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
				molecule.getProperties().forEach((k,v)->{

					if(pubchemDataMap.containsKey(k.toString()))
						pubchemDataMap.put(k.toString(), v.toString());

					if(namingMap.containsKey(k.toString()))
						namingMap.put(k.toString(), v.toString());
				});
				String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
				String smiles = pubchemDataMap.get(PubChemFields.SMILES_ISOMERIC.toString());
				if(smiles.isEmpty())
					pubchemDataMap.get(PubChemFields.SMILES_CANONICAL.toString());

				if(smiles.isEmpty()) {
					try {
						smiles = smilesGenerator.create(molecule);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ps.setString(1, smiles);
				ps.setString(2, id);
				ps.setString(3, pubchemDataMap.get(PubChemFields.INCHIKEY.toString()));
				ps.setString(4, pcName);
				int res = ps.executeUpdate();
				if(res == 0)
					System.out.println(pcName + " NOT UPDATED");
			}
			count++;
			//	System.out.println(Integer.toString(count));
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void parseMissingMSMSStructuresToMetlinFromPubChemByName() throws Exception {

		File folder = Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM\\NAME").toFile();
		String[] files = folder.list();
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		IteratingSDFReader reader;

		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE METLIN_CPD_DATA SET SMILES = ?, PUBCHEM = ?, "
			+ "INCHI_KEY = ? WHERE CATALOG_NAME = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		String pcName;
		int count = 0;
		for (String file : files) {

			pcName = FilenameUtils.getBaseName(file);
			reader = new IteratingSDFReader(
					new FileInputStream(Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM\\NAME", file).toFile()), coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
				Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
				molecule.getProperties().forEach((k,v)->{

					if(pubchemDataMap.containsKey(k.toString()))
						pubchemDataMap.put(k.toString(), v.toString());

					if(namingMap.containsKey(k.toString()))
						namingMap.put(k.toString(), v.toString());
				});
				String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
				String smiles = pubchemDataMap.get(PubChemFields.SMILES_ISOMERIC.toString());
				if(smiles.isEmpty())
					pubchemDataMap.get(PubChemFields.SMILES_CANONICAL.toString());

				if(smiles.isEmpty()) {
					try {
						smiles = smilesGenerator.create(molecule);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ps.setString(1, smiles);
				ps.setString(2, id);
				ps.setString(3, pubchemDataMap.get(PubChemFields.INCHIKEY.toString()));
				ps.setString(4, pcName);
				int res = ps.executeUpdate();
				if(res == 0)
					System.out.println(pcName + " NOT UPDATED");
			}
			count++;
			//	System.out.println(Integer.toString(count));
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void parseMissingMSMSStructuresToMetlinFromPubChem() throws Exception {

		//	Insert INCHI keys
		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE METLIN_CPD_DATA SET INCHI_KEY = ? WHERE METLIN = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\metlin_inchi.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		for(int i=1; i<parsedData.length; i++) {

			ps.setString(1, parsedData[i][1]);
			ps.setString(2, parsedData[i][0]);
			ps.addBatch();
		}
		ps.executeBatch();

		File folder = Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM\\IK").toFile();
		String[] files = folder.list();
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		IteratingSDFReader reader;

		query =
			"UPDATE METLIN_CPD_DATA SET SMILES = ?, PUBCHEM = ? WHERE INCHI_KEY = ?";
		ps = conn.prepareStatement(query);
		String inchiKey;
		int count = 0;
		for (String file : files) {

			inchiKey = FilenameUtils.getBaseName(file);
			reader = new IteratingSDFReader(
					new FileInputStream(Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM\\IK", file).toFile()), coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
				Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
				molecule.getProperties().forEach((k,v)->{

					if(pubchemDataMap.containsKey(k.toString()))
						pubchemDataMap.put(k.toString(), v.toString());

					if(namingMap.containsKey(k.toString()))
						namingMap.put(k.toString(), v.toString());
				});
				String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
				String smiles = pubchemDataMap.get(PubChemFields.SMILES_ISOMERIC.toString());
				if(smiles.isEmpty())
					pubchemDataMap.get(PubChemFields.SMILES_CANONICAL.toString());

				if(smiles.isEmpty()) {
					try {
						smiles = smilesGenerator.create(molecule);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ps.setString(1, smiles);
				ps.setString(2, id);
				ps.setString(3, inchiKey);
				ps.executeUpdate();
			}
			count++;
			System.out.println(Integer.toString(count));
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void fetchMissingMETLINCompoundDataFromPubChemBySmiles() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS_NO_ACCESSION_WID.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		for(int i=1; i<parsedData.length; i++) {

			String cid = parsedData[i][0].trim();
			String smiles = parsedData[i][4].trim();
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/" + smiles + "/record/SDF";

			InputStream pubchemDataStream = WebUtils.getInputStreamFromURLsilent(requestUrl);
			if(pubchemDataStream != null) {

				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_SMILES", cid + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			}
			else {
				System.out.println(requestUrl);
			}
		}
	}

	public static void parseMissingMSMSStructuresToNISTFromPubChemBySmiles() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Connection cpdConn = CompoundDbConnectionManager.getConnection();
		String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";

		//	Read name-id list
		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS_NO_ACCESSION_WID.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>idSmilesMap = new TreeMap<String,String>();
		for(int i=1; i<parsedData.length; i++)
			idSmilesMap.put(parsedData[i][0], parsedData[i][4]);

		File folder = Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_SMILES").toFile();
		String[] files = folder.list();
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		IteratingSDFReader reader;
		InputStream synonymStream = null;

		// INCHI-KEY check
		String inchiQuery =
			"SELECT ACCESSION FROM COMPOUND_DATA WHERE INCHI_KEY = ?";
		PreparedStatement ps = cpdConn.prepareStatement(inchiQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE,
		         ResultSet.CONCUR_UPDATABLE);

		String updQuery = "UPDATE REF_MSMS_COMPOUND_DATA SET ACCESSION = ? WHERE SMILES = ?";
		PreparedStatement cpdPs = conn.prepareStatement(updQuery);

		for (String file : files) {

			String tmpId = FilenameUtils.getBaseName(file);
			reader = new IteratingSDFReader(
					new FileInputStream(Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_SMILES", file).toFile()), coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
				Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
				molecule.getProperties().forEach((k,v)->{

					if(pubchemDataMap.containsKey(k.toString()))
						pubchemDataMap.put(k.toString(), v.toString());

					if(namingMap.containsKey(k.toString()))
						namingMap.put(k.toString(), v.toString());
				});
				String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
				String smiles = pubchemDataMap.get(PubChemFields.SMILES_ISOMERIC.toString());
				if(smiles.isEmpty())
					pubchemDataMap.get(PubChemFields.SMILES_CANONICAL.toString());

				String inchiKey = pubchemDataMap.get(PubChemFields.INCHIKEY.toString());
				if(smiles.isEmpty()) {
					try {
						smiles = smilesGenerator.create(molecule);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ps.setString(1, inchiKey);
				ResultSet rs = ps.executeQuery();
				if(rs.first()) {
					String currAccession = rs.getString(1);
					cpdPs.setString(1, currAccession);
					cpdPs.setString(2, idSmilesMap.get(tmpId));
					cpdPs.executeUpdate();
					System.out.println(id + "\t" + rs.getString(1));
					rs.close();
				}
				else {
					rs.close();
					if(!id.isEmpty()) {
						cpdPs.setString(1, id);
						cpdPs.setString(2, idSmilesMap.get(id));
						cpdPs.executeUpdate();

						//	Insert compound data
						try {
							synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + id + "/synonyms/TXT");
						} catch (Exception e) {
							e.printStackTrace();
						}
						String[] synonyms = new String[0];
						if(synonymStream != null) {
							try {
								synonyms = IOUtils.toString(synonymStream).split("\\r?\\n");
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						if(synonyms.length == 0)
							synonyms = new String[] {id};
						
						PubChemCompoundDescriptionBundle bundle = getCompoundDescription(id);
						try {
							PubChemParser.insertPubchemRecord(molecule, synonyms, bundle, cpdConn);
							System.out.println(id + " was inserted");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		ps.close();
		cpdPs.close();
		ConnectionManager.releaseConnection(conn);
		CompoundDbConnectionManager.releaseConnection(cpdConn);
	}

	public static void fetchMissingMETLINCompoundDataFromPubChemById() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("E:\\DataAnalysis\\Databases\\METLIN\\restofpubchem.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		for(int i=1; i<parsedData.length; i++) {

			String cid = parsedData[i][0].trim();
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + cid + "/record/SDF";

			InputStream pubchemDataStream = WebUtils.getInputStreamFromURLsilent(requestUrl);
			if(pubchemDataStream != null) {

				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_ID", cid + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			}
			else {
				System.out.println(requestUrl);
			}
		}
	}

	public static void insertMetlinAccessionsInMetlinCpd() throws Exception {

		Connection conn = ConnectionManager.getConnection();

		//	Read name-id list
		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("E:\\DataAnalysis\\Databases\\METLIN\\metlin_accessions.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());

		String metlinUpdQuery = "UPDATE METLIN_CPD_DATA SET ACCESSION = ? WHERE METLIN = ? AND ACCESSION IS NULL";
		PreparedStatement metlinPs = conn.prepareStatement(metlinUpdQuery);

		for(int i=1; i<parsedData.length; i++) {

			metlinPs.setString(1, parsedData[i][1]);
			metlinPs.setString(2, parsedData[i][1]);
			metlinPs.executeUpdate();
		}
		metlinPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void parseMissingMSMSStructuresToMetlinFromPubChemByNameId() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Connection cpdConn = CompoundDbConnectionManager.getConnection();
		String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";

		//	Read name-id list
		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("E:\\DataAnalysis\\Databases\\METLIN\\restofpubchem_names.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>idNameMap = new TreeMap<String,String>();
		for(int i=1; i<parsedData.length; i++)
			idNameMap.put(parsedData[i][1], parsedData[i][0]);

		File folder = Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_ID").toFile();
		String[] files = folder.list();
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		IteratingSDFReader reader;
		InputStream synonymStream = null;

		// INCHI-KEY check
		String inchiQuery =
			"SELECT ACCESSION FROM COMPOUND_DATA WHERE INCHI_KEY = ?";
		PreparedStatement ps = cpdConn.prepareStatement(inchiQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE,
		         ResultSet.CONCUR_UPDATABLE);

		String metlinUpdQuery = "UPDATE METLIN_CPD_DATA SET ACCESSION = ? WHERE CATALOG_NAME = ?";
		PreparedStatement metlinPs = conn.prepareStatement(metlinUpdQuery);

		for (String file : files) {

			reader = new IteratingSDFReader(
					new FileInputStream(Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_ID", file).toFile()), coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
				Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
				molecule.getProperties().forEach((k,v)->{

					if(pubchemDataMap.containsKey(k.toString()))
						pubchemDataMap.put(k.toString(), v.toString());

					if(namingMap.containsKey(k.toString()))
						namingMap.put(k.toString(), v.toString());
				});
				String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
				String smiles = pubchemDataMap.get(PubChemFields.SMILES_ISOMERIC.toString());
				if(smiles.isEmpty())
					pubchemDataMap.get(PubChemFields.SMILES_CANONICAL.toString());

				String inchiKey = pubchemDataMap.get(PubChemFields.INCHIKEY.toString());
				if(smiles.isEmpty()) {
					try {
						smiles = smilesGenerator.create(molecule);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ps.setString(1, inchiKey);
				ResultSet rs = ps.executeQuery();
				if(rs.first()) {
					String currAccession = rs.getString(1);
					metlinPs.setString(1, currAccession);
					metlinPs.setString(2, idNameMap.get(id));
					metlinPs.executeUpdate();
					System.out.println(id + "\t" + rs.getString(1));
					rs.close();
				}
				else {
					metlinPs.setString(1, id);
					metlinPs.setString(2, idNameMap.get(id));
					metlinPs.executeUpdate();

					//	Insert compound data
					try {
						synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + id + "/synonyms/TXT");
					} catch (Exception e) {
						e.printStackTrace();
					}
					String[] synonyms = new String[0];
					if(synonymStream != null) {
						try {
							synonyms = IOUtils.toString(synonymStream).split("\\r?\\n");
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(synonyms.length == 0)
						synonyms = new String[] {id};
					
					PubChemCompoundDescriptionBundle bundle = getCompoundDescription(id);
					try {
						PubChemParser.insertPubchemRecord(molecule, synonyms, bundle, cpdConn);
						System.out.println(id + " was inserted");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		ps.close();
		metlinPs.close();
		ConnectionManager.releaseConnection(conn);
		CompoundDbConnectionManager.releaseConnection(cpdConn);
	}

	public static void fetchMissingMETLINCompoundDataFromPubChem() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\metlin_inchi.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		for(int i=1; i<parsedData.length; i++) {

			String inchikey = parsedData[i][1];
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchikey + "/record/SDF";

			InputStream pubchemDataStream = WebUtils.getInputStreamFromURLsilent(requestUrl);
			if(pubchemDataStream != null) {

				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM", inchikey + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			}
			else {
				System.out.println(inchikey);
			}
		}
	}

	public static void fetchMissingNISTMSMS2CompoundDataFromPubChem() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS2_NO_ACCESSION_NAME_IK.tsv")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		for(int i=1; i<parsedData.length; i++) {

			String inchikey = parsedData[i][1];
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchikey + "/record/SDF";

			InputStream pubchemDataStream = WebUtils.getInputStreamFromURLsilent(requestUrl);
			if(pubchemDataStream != null) {

				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\NIST17\\MSMS2Compound", inchikey + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			}
			else {
				System.out.println(inchikey);
			}
		}
	}

	public static void fetchMissingNISTMSMS2CompoundDataFromPubChemByName() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS2_NO_ACCESSION_NAME_IK.tsv")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		for(int i=1; i<parsedData.length; i++) {

			String cpdName = parsedData[i][0];
			String inchikey = parsedData[i][1];
			String requestUrl =
					"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + URLEncoder.encode(cpdName, encoding) + "/record/SDF";

			InputStream pubchemDataStream = WebUtils.getInputStreamFromURLsilent(requestUrl);
			if(pubchemDataStream != null) {

				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\NIST17\\MSMS2CompoundNM", inchikey + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			}
			else {
				System.out.println(cpdName);
			}
		}
	}

	public static void parseMissingMSMSStructuresToNISTMS2FromPubChemById() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Connection cpdConn = CompoundDbConnectionManager.getConnection();
		String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";

		//	Read name-id list
		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NISTP_PUBCHEM_IK.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>ikIdMap = new TreeMap<String,String>();
		for(int i=1; i<parsedData.length; i++)
			ikIdMap.put(parsedData[i][0], parsedData[i][1]);

		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		IteratingSDFReader reader;
		InputStream synonymStream = null;

		// INCHI-KEY check
		String inchiQuery =
			"SELECT ACCESSION FROM COMPOUND_DATA WHERE INCHI_KEY = ?";
		PreparedStatement inchips = cpdConn.prepareStatement(inchiQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE,
		         ResultSet.CONCUR_UPDATABLE);

		String updQuery =
				"UPDATE REF_MSMS_COMPOUND_DATA SET ACCESSION = ? "
				+ "WHERE INCHI_KEY = ? AND ACCESSION IS NULL";
		PreparedStatement auPs = conn.prepareStatement(updQuery);

		InputStream pubchemDataStream = null;

		for (Map.Entry<String, String> entry : ikIdMap.entrySet()) {

			String originalInchiKey = entry.getKey();
			String id = entry.getValue();
			String requestUrl = pubchemCidUrl + id + "/SDF";
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(pubchemDataStream != null) {
				reader = new IteratingSDFReader(pubchemDataStream, coBuilder);
				while (reader.hasNext()) {
					IAtomContainer molecule = (IAtomContainer)reader.next();
					Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
					molecule.getProperties().forEach((k,v)->{

						if(pubchemDataMap.containsKey(k.toString()))
							pubchemDataMap.put(k.toString(), v.toString());
					});
					String inchiKey = pubchemDataMap.get(PubChemFields.INCHIKEY.toString());
					inchips.setString(1, inchiKey);
					ResultSet inchiRs = inchips.executeQuery();
					if(inchiRs.first()) {
						//	ID already in
						String currAccession = inchiRs.getString(1);
						auPs.setString(1, currAccession);
						auPs.setString(2, originalInchiKey);
						auPs.executeUpdate();
						System.out.println(currAccession + " set for " + originalInchiKey);
						inchiRs.close();
					}
					else {
						auPs.setString(1, id);
						auPs.setString(2, originalInchiKey);
						auPs.executeUpdate();
						inchiRs.close();
						try {
							synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + id + "/synonyms/TXT");
						} catch (Exception e) {
							e.printStackTrace();
						}
						String[] synonyms = new String[0];
						if(synonymStream != null) {
							try {
								synonyms = IOUtils.toString(synonymStream).split("\\r?\\n");
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						if(synonyms.length == 0)
							synonyms = new String[] {id};
						
						PubChemCompoundDescriptionBundle bundle = getCompoundDescription(id);
						try {
							PubChemParser.insertPubchemRecord(molecule, synonyms, bundle, cpdConn);
							System.out.println("Inserted " + id);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		inchips.close();
		auPs.close();
		ConnectionManager.releaseConnection(conn);
		CompoundDbConnectionManager.releaseConnection(cpdConn);
	}

	public static void parseMissingMSMSStructuresToNISTMS2FromPubChemByName() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Connection cpdConn = CompoundDbConnectionManager.getConnection();
		String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";

		//	Read name-id list
		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS2_NO_ACCESSION_IKF.tsv")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>ikFormulaMap = new TreeMap<String,String>();
		Map<String,String>ikNameMap = new TreeMap<String,String>();
		for(int i=1; i<parsedData.length; i++) {
			ikFormulaMap.put(parsedData[i][2], parsedData[i][1]);
			ikNameMap.put(parsedData[i][2], parsedData[i][0]);
		}

		File folder = Paths.get("E:\\DataAnalysis\\Databases\\NIST17\\MSMS2CompoundNM").toFile();
		String[] files = folder.list();
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		IteratingSDFReader reader;
		InputStream synonymStream = null;

		// INCHI-KEY check
		String inchiQuery =
			"SELECT ACCESSION FROM COMPOUND_DATA WHERE INCHI_KEY = ?";
		PreparedStatement ps = cpdConn.prepareStatement(inchiQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE,
		         ResultSet.CONCUR_UPDATABLE);

		String updQuery = "UPDATE REF_MSMS_COMPOUND_DATA SET ACCESSION = ? WHERE INCHI_KEY = ? AND ACCESSION IS NULL";
		PreparedStatement cpdPs = conn.prepareStatement(updQuery);

		for (String file : files) {

			String originalInchiKey = FilenameUtils.getBaseName(file);
			if(!ikFormulaMap.containsKey(originalInchiKey))
				continue;

			reader = new IteratingSDFReader(
					new FileInputStream(Paths.get("E:\\DataAnalysis\\Databases\\NIST17\\MSMS2CompoundNM", file).toFile()), coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
				Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
				molecule.getProperties().forEach((k,v)->{

					if(pubchemDataMap.containsKey(k.toString()))
						pubchemDataMap.put(k.toString(), v.toString());

					if(namingMap.containsKey(k.toString()))
						namingMap.put(k.toString(), v.toString());
				});
				String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
				String pubChemFormula = pubchemDataMap.get(PubChemFields.FORMULA.toString());
				String inchiKey = pubchemDataMap.get(PubChemFields.INCHIKEY.toString());
				String origFormula = ikFormulaMap.get(originalInchiKey);
				if(!origFormula.equals(pubChemFormula) ) {

					System.out.println(" PubChem formula does not match for name " + ikNameMap.get(originalInchiKey));
					break;
				}
				ps.setString(1, inchiKey);
				ResultSet rs = ps.executeQuery();
				if(rs.first()) {
					String currAccession = rs.getString(1);
					cpdPs.setString(1, currAccession);
					cpdPs.setString(2, originalInchiKey);
					cpdPs.executeUpdate();
					System.out.println(id + "\t" + rs.getString(1));
					rs.close();
				}
				else {
					rs.close();
					if(!id.isEmpty()) {
						cpdPs.setString(1, id);
						cpdPs.setString(2, originalInchiKey);
						cpdPs.executeUpdate();

						//	Insert compound data
						try {
							synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + id + "/synonyms/TXT");
						} catch (Exception e) {
							e.printStackTrace();
						}
						String[] synonyms = new String[0];
						if(synonymStream != null) {
							try {
								synonyms = IOUtils.toString(synonymStream).split("\\r?\\n");
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						if(synonyms.length == 0)
							synonyms = new String[] {id};
						
						PubChemCompoundDescriptionBundle bundle = getCompoundDescription(id);
						try {
							PubChemParser.insertPubchemRecord(molecule, synonyms, bundle, cpdConn);
							System.out.println(id + " was inserted");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		ps.close();
		cpdPs.close();
		ConnectionManager.releaseConnection(conn);
		CompoundDbConnectionManager.releaseConnection(cpdConn);
	}

	public static void fetchMissingNISTCompoundDataFromPubChemByName() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS_NO_ACCESSION_NIST_ID.tsv")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		for(int i=1; i<parsedData.length; i++) {

			String cpdName = parsedData[i][0];
			String inchiKey = parsedData[i][02];
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + URLEncoder.encode(cpdName, encoding) + "/record/SDF";

			InputStream pubchemDataStream = WebUtils.getInputStreamFromURLsilent(requestUrl);
			if(pubchemDataStream != null) {

				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_NIST_NAME", inchiKey + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			}
			else {
				System.out.println(cpdName);
			}
		}
	}

	public static void insertMissingNISTCompoundDataFromPubChemByName() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\NIST_MSMS_NO_ACCESSION_NIST_ID.tsv")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>nameMap = new HashMap<String,String>();
		Map<String,String>formulaMap = new HashMap<String,String>();
		for(int i=1; i<parsedData.length; i++) {
			nameMap.put(parsedData[i][02], parsedData[i][0]);
			formulaMap.put(parsedData[i][02], parsedData[i][1]);
		}
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		IteratingSDFReader reader;
		InputStream synonymStream = null;
		String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";

		Connection conn = ConnectionManager.getConnection();
		Connection cpdConn = CompoundDbConnectionManager.getConnection();
		// INCHI-KEY check
		String inchiQuery =
			"SELECT ACCESSION FROM COMPOUND_DATA WHERE INCHI_KEY = ?";
		PreparedStatement ps = cpdConn.prepareStatement(inchiQuery,
		         ResultSet.TYPE_SCROLL_INSENSITIVE,
		         ResultSet.CONCUR_UPDATABLE);

		String updQuery = "UPDATE REF_MSMS_COMPOUND_DATA SET ACCESSION = ? WHERE NAME = ? AND ACCESSION IS NULL";
		PreparedStatement cpdPs = conn.prepareStatement(updQuery);

		File folder = Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_NIST_NAME").toFile();
		String[] files = folder.list();
		for (String file : files) {

			String inchiKeyOrig = FilenameUtils.getBaseName(file);
			String name = nameMap.get(inchiKeyOrig);
			String expectedFormula = formulaMap.get(inchiKeyOrig);

			reader = new IteratingSDFReader(
					new FileInputStream(Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM_NIST_NAME", file).toFile()), coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molecule);
				String mfString = MolecularFormulaManipulator.getString(mf);
				if(!expectedFormula.equals(mfString)) {
					System.out.println(name + "\t" + expectedFormula + "\t" + mfString);
					break;
				}
				Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
				Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
				molecule.getProperties().forEach((k,v)->{

					if(pubchemDataMap.containsKey(k.toString()))
						pubchemDataMap.put(k.toString(), v.toString());

					if(namingMap.containsKey(k.toString()))
						namingMap.put(k.toString(), v.toString());
				});
				String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
				String smiles = pubchemDataMap.get(PubChemFields.SMILES_ISOMERIC.toString());
				String inchiKey = pubchemDataMap.get(PubChemFields.INCHIKEY.toString());
				if(smiles.isEmpty())
					pubchemDataMap.get(PubChemFields.SMILES_CANONICAL.toString());

				if(smiles.isEmpty()) {
					try {
						smiles = smilesGenerator.create(molecule);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ps.setString(1, inchiKey);
				ResultSet rs = ps.executeQuery();
				if(rs.first()) {
					String currAccession = rs.getString(1);
					cpdPs.setString(1, currAccession);
					cpdPs.setString(2, name);
					cpdPs.executeUpdate();
					System.out.println(name + "\t" + rs.getString(1));
					rs.close();
				}
				else {
					if(!id.isEmpty()) {
						cpdPs.setString(1, id);
						cpdPs.setString(2, name);
						cpdPs.executeUpdate();

						//	Insert compound data
						try {
							synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + id + "/synonyms/TXT");
						} catch (Exception e) {
							e.printStackTrace();
						}
						String[] synonyms = new String[0];
						if(synonymStream != null) {
							try {
								synonyms = IOUtils.toString(synonymStream).split("\\r?\\n");
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						if(synonyms.length == 0)
							synonyms = new String[] {id};
						
						PubChemCompoundDescriptionBundle bundle = getCompoundDescription(id);
						try {
							PubChemParser.insertPubchemRecord(molecule, synonyms, bundle, cpdConn);
							System.out.println(id + " was inserted");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		ps.close();
		cpdPs.close();
		ConnectionManager.releaseConnection(conn);
		CompoundDbConnectionManager.releaseConnection(cpdConn);
	}

	public static void fetchMissingMETLINCompoundDataFromPubChemByName() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(
				new FileInputStream(new File("C:\\Users\\Sasha\\Downloads\\metlin_name.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, MRC2ToolBoxConfiguration.getTabDelimiter());
		for(int i=1; i<parsedData.length; i++) {

			String cpdName = parsedData[i][0];
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + URLEncoder.encode(cpdName, encoding) + "/record/SDF";

			InputStream pubchemDataStream = WebUtils.getInputStreamFromURLsilent(requestUrl);
			if(pubchemDataStream != null) {

				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\METLIN\\PUBCHEM\\NAME", cpdName + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			}
			else {
				System.out.println(cpdName);
			}
		}
	}

//	public static void scrapeKeggData(File idFile) throws Exception {
//
//		WebClient client = new WebClient();
//		client.getOptions().setCssEnabled(false);
//		client.getOptions().setJavaScriptEnabled(false);
//		String[][] idList = DelimitedTextParser.parseTextFile(idFile, MRC2ToolBoxConfiguration.getDefaultDataDelimiter());
//		String cpdUrl = "https://www.genome.jp/dbget-bin/www_bget?cpd:";
//		String drugUrl = "https://www.genome.jp/dbget-bin/www_bget?dr:";
//		for (int i = 0; i < idList.length; i++) {
//
//			String keggId = idList[i][0];
//			String searchUrl = cpdUrl + keggId;
//			if(keggId.startsWith("D"))
//				searchUrl = drugUrl + keggId;
//
//			HtmlPage page = null;
//			try {
//				page = client.getPage(searchUrl);
//				page.getAnchorByHref("");
//				HtmlAnchor pubChemAnchor = page.getAnchorByText("pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?sid=");
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//	}


/*
	//	Rescan MSMS results and ad missing METLIN hits.
	public static void rescanAndUpdateCompoundDataForMetlinMsMsHits(File cefDir) throws Exception {


		List<Path> cefList = Files.find(Paths.get(cefDir.getAbsolutePath()), Integer.MAX_VALUE,
				(filePath, fileAttr) -> (filePath.toString().endsWith(".CEF") ||
						filePath.toString().endsWith("cef")) && fileAttr.isRegularFile()).
			sorted().collect(Collectors.toList());

		Connection conn = ConnectionManager.getConnection();
		String idQuery =
			"SELECT C.MRC2_LIB_ID  " +
			"FROM  REF_MSMS_LIBRARY_COMPONENT C  " +
			"WHERE C.ORIGINAL_LIBRARY_ID = ? " +
			"AND C.LIBRARY_NAME = 'METLIN' " +
			"AND C.COLLISION_ENERGY = ? " +
			"AND C.POLARITY = ?";
		PreparedStatement idPs = conn.prepareStatement(idQuery);

		String mrc2idQuery = "SELECT 'MSL' || LPAD(MSMS_LIB_ENTRY_SEQ.NEXTVAL, 9, '0') AS MRC2ID FROM DUAL";
		PreparedStatement mrc2idPs = conn.prepareStatement(mrc2idQuery);

		String specInsertQuery =
			"INSERT INTO REF_MSMS_LIBRARY_COMPONENT ( " +
			"MRC2_LIB_ID, " +
			"POLARITY, " +
			"IONIZATION, " +
			"COLLISION_ENERGY, " +
			"SPECTRUM_TYPE, " +
			"SPECTRUM_SOURCE, " +
			"IONIZATION_TYPE, " +
			"LIBRARY_NAME, " +
			"ORIGINAL_LIBRARY_ID) VALUES (?,?,'ESI',?,'MS2','EXPERIMENTAL','ESI','METLIN',?) ";
		PreparedStatement specPs = conn.prepareStatement(specInsertQuery);

		String msmsInsertQuery = "INSERT INTO REF_MSMS_LIBRARY_PEAK (MRC2_LIB_ID, MZ, INTENSITY) VALUES (?, ?, ?)";
		PreparedStatement msmsPs = conn.prepareStatement(msmsInsertQuery);

		String cpdInsertQuery = "INSERT INTO REF_MSMS_COMPOUND_DATA (MRC2_LIB_ID, NAME, FORMULA, ACCESSION) VALUES (?, ?, ?, ?)";
		PreparedStatement cpdPs = conn.prepareStatement(cpdInsertQuery);

		Document dataDocument;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//CEF/CompoundList/Compound");
		for(Path p : cefList) {

			dataDocument = XmlUtils.readXmlFile(p.toFile());
			NodeList targetNodes = (NodeList) expr.evaluate(dataDocument, XPathConstants.NODESET);
			for (int i = 0; i < targetNodes.getLength(); i++) {

				Element cpdElement = (Element) targetNodes.item(i);

				//	Find ID and check that it contains METLIN ID
				if(cpdElement.getElementsByTagName("Molecule").getLength() == 0)
					continue;

				Element molecule = (Element) cpdElement.getElementsByTagName("Molecule").item(0);
				String cpdName = molecule.getAttribute("name").trim();
				String molecularFormula = molecule.getAttribute("formula").replaceAll("\\s+", "");
				if(molecularFormula.isEmpty())
					molecularFormula = null;

				NodeList iDlist = cpdElement.getElementsByTagName("Accession");
				if (iDlist.getLength() == 0)
					continue;

				String metlinId = null;
				TandemMassSpectrum msms = null;
				String mrc2LibId = null;

				for (int j = 0; j < iDlist.getLength(); j++) {

					Element idElement = (Element) iDlist.item(j);
					String database = idElement.getAttribute("db").trim();
					String accession = idElement.getAttribute("id").trim();
					if (!database.isEmpty() && !accession.isEmpty()) {

						CompoundDatabase db = AgilentDatabaseFields.getDatabaseByName(database);
						if(db == null)
							db = CompoundDatabase.getCompoundDatabaseByName(database);

						if(db != null) {
							if(db.equals(CompoundDatabase.METLIN));
								metlinId = accession;
						}
					}
				}
				if(metlinId == null)
					continue;

				Polarity pol = null;
				String polarity = null;
				String collisionEnergy = null;
				NodeList spectra = cpdElement.getElementsByTagName("Spectrum");
				for (int j = 0; j < spectra.getLength(); j++) {

					Element spectrumElement = (Element) spectra.item(j);
					//	Get collision energy from experimental spectrum
					if(spectrumElement.getAttribute("type").equals(AgilentCefFields.MS2_SPECTRUM.getName())) {

						Element msDetails = (Element) spectrumElement.getElementsByTagName("MSDetails").item(0);
						String sign = msDetails.getAttribute("p");

						if(sign.equals("+")) {
							pol = Polarity.Positive;
							polarity = "P";
						}
						if(sign.equals("-")) {
							pol = Polarity.Negative;
							polarity = "N";
						}
						collisionEnergy = msDetails.getAttribute("ce").replaceAll("V", "");
					}
				}
				//	Parse library spectrum
				for (int j = 0; j < spectra.getLength(); j++) {

					Element spectrumElement = (Element) spectra.item(j);
					if(spectrumElement.getAttribute("type").equals(AgilentCefFields.LIBRARY_MS2_SPECTRUM.getName())) {

						NodeList precursors = spectrumElement.getElementsByTagName("mz");
						double mz = Double.parseDouble(precursors.item(0).getFirstChild().getNodeValue());
						msms = new TandemMassSpectrum(2, new MsPoint(mz, 999.0d));
						msms.setSpectrumSource(SpectrumSource.LIBRARY);
						NodeList msmsPeaks = spectrumElement.getElementsByTagName("p");
						Collection<MsPoint>msmsPoints = new ArrayList<MsPoint>();
						for (int k = 0; k < msmsPeaks.getLength(); k++) {

							Element peakElement = (Element) msmsPeaks.item(k);
							msmsPoints.add(new MsPoint(
									Double.parseDouble(peakElement.getAttribute("x")),
									Double.parseDouble(peakElement.getAttribute("y"))));
						}
						msms.setSpectrum(msmsPoints);
						if(!collisionEnergy.isEmpty()) {
							double ce = Double.parseDouble(collisionEnergy);
							msms.setCidLevel(ce);
						}
					}
				}
				//	Check if ID/CID/Polarity combination is not already in database
				if(msms == null)
					continue;

				idPs.setString(1,metlinId);
				idPs.setDouble(2, msms.getCidLevel());
				idPs.setString(3,polarity);
				ResultSet rs = idPs.executeQuery();
				while(rs.next())
					mrc2LibId = rs.getString("MRC2_LIB_ID");

				rs.close();

				//	Insert new entry
				if(mrc2LibId == null) {

					System.out.println(metlinId + "\t" + Double.toString(msms.getCidLevel())  + "\t" + polarity + "\t" + cpdName);

					//	Get ID
					String mrc2id = null;
					ResultSet mrc2idRes = mrc2idPs.executeQuery();
					while(mrc2idRes.next())
						mrc2id = mrc2idRes.getString(1);

					mrc2idRes.close();

					//	Insert component
					specPs.setString(1, mrc2id);
					specPs.setString(2, polarity);
					specPs.setDouble(3, msms.getCidLevel());
					specPs.setString(4, metlinId);
					specPs.executeUpdate();

					//	Insert MSMS
					msmsPs.setString(1, mrc2id);
					for(MsPoint point : msms.getSpectrum()) {

						msmsPs.setDouble(2, point.getMz());
						msmsPs.setDouble(3, point.getIntensity());
						msmsPs.addBatch();
					}
					msmsPs.executeBatch();

					//	Insert compound data
					cpdPs.setString(1, mrc2id);
					cpdPs.setString(2, cpdName);
					cpdPs.setString(3, molecularFormula);
					cpdPs.setString(4, metlinId);
					cpdPs.executeUpdate();
				}
			}
		}
		idPs.close();
		specPs.close();
		msmsPs.close();
		cpdPs.close();
		mrc2idPs.close();
		ConnectionManager.releaseConnection(conn);
	}
*/
	public static void updateCompoundDataForMetlinMsMs() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String compoundQuery =
			"SELECT DISTINCT M.LIBRARY_ENTRY_ID, M.ACCESSION,  " +
			"D.PRIMARY_NAME, D.MOL_FORMULA, D.INCHI_KEY, D.SMILES " +
			"FROM MSMS_LIBRARY_MATCH M, " +
			"COMPOUND_DATA D " +
			"WHERE D.ACCESSION = M.ACCESSION ";
		PreparedStatement ps = conn.prepareStatement(compoundQuery);

		String idQuery =
				"SELECT MRC2_LIB_ID FROM REF_MSMS_LIBRARY_COMPONENT "
				+ "WHERE LIBRARY_NAME='METLIN' AND ORIGINAL_LIBRARY_ID = ?";
		PreparedStatement idps = conn.prepareStatement(idQuery);

		String insertQuery =
				"INSERT INTO REF_MSMS_COMPOUND_DATA ("
				+ "MRC2_LIB_ID, NAME, FORMULA, INCHI_KEY, SMILES, ACCESSION) " +
				"VALUES(?, ?, ?, ?, ?, ?)";
		PreparedStatement insps = conn.prepareStatement(insertQuery);

		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			idps.setString(1, rs.getString("LIBRARY_ENTRY_ID"));
			ResultSet idrs = idps.executeQuery();
			while(idrs.next()){

				insps.setString(1, idrs.getString("MRC2_LIB_ID"));
				insps.setString(2, rs.getString("PRIMARY_NAME"));
				insps.setString(3, rs.getString("MOL_FORMULA"));
				insps.setString(4, rs.getString("INCHI_KEY"));
				insps.setString(5, rs.getString("SMILES"));
				insps.setString(6, rs.getString("ACCESSION"));
				insps.addBatch();
			}
			insps.executeBatch();
			idrs.close();
		}
		rs.close();
		insps.close();
		idps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void scrubMetlinMsMsData() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT DISTINCT M.LIBRARY_ENTRY_ID, Q.POLARITY, F.COLLISION_ENERGY " +
			"FROM MSMS_LIBRARY_MATCH M,  " +
			"MSMS_FEATURE F,  " +
			"MS_FEATURE FM, " +
			"DATA_ANALYSIS_MAP D, " +
			"INJECTION I, " +
			"DATA_ACQUISITION_METHOD Q " +
			"WHERE F.MSMS_FEATURE_ID = M.MSMS_FEATURE_ID " +
			"AND F.PARENT_FEATURE_ID = FM.MS_FEATURE_ID " +
			"AND FM.DATA_ANALYSIS_ID = D.DATA_ANALYSIS_ID " +
			"AND D.INJECTION_ID = I.INJECTION_ID " +
			"AND I.ACQUISITION_METHOD_ID = Q.ACQ_METHOD_ID " +
			"ORDER BY 1,2,3";
		PreparedStatement ps = conn.prepareStatement(query);

		String msQuery =
			"SELECT DISTINCT P.MZ, P.HEIGHT " +
			"FROM MSMS_LIBRARY_MATCH M,  " +
			"MSMS_FEATURE F, " +
			"MSMS_FEATURE_PEAK P, " +
			"MS_FEATURE FM, " +
			"DATA_ANALYSIS_MAP D, " +
			"INJECTION I, " +
			"DATA_ACQUISITION_METHOD Q " +
			"WHERE F.MSMS_FEATURE_ID = M.MSMS_FEATURE_ID " +
			"AND P.FEATURE_ID = M.MATCH_FEATURE_ID " +
			"AND M.LIBRARY_ENTRY_ID = ? " +
			"AND F.COLLISION_ENERGY = ? " +
			"AND F.PARENT_FEATURE_ID = FM.MS_FEATURE_ID " +
			"AND FM.DATA_ANALYSIS_ID = D.DATA_ANALYSIS_ID " +
			"AND D.INJECTION_ID = I.INJECTION_ID " +
			"AND I.ACQUISITION_METHOD_ID = Q.ACQ_METHOD_ID " +
			"AND Q.POLARITY = ? " +
			"ORDER BY 1,2 ";
		PreparedStatement msPs = conn.prepareStatement(msQuery);

		String insertComponentQuery =
			"INSERT INTO REF_MSMS_LIBRARY_COMPONENT( " +
			"MRC2_LIB_ID, " +
			"POLARITY, " +
			"IONIZATION, " +
			"COLLISION_ENERGY, " +
			"SPECTRUM_TYPE, " +
			"SPECTRUM_SOURCE, " +
			"IONIZATION_TYPE, " +
			"LIBRARY_NAME, " +
			"ORIGINAL_LIBRARY_ID) " +
			"VALUES(?, ?, 'ESI', ?, 'MS2', 'EXPERIMENTAL', 'ESI', 'METLIN', ?)";
		PreparedStatement insertComponentPs = conn.prepareStatement(insertComponentQuery);

		String msPeaksQuery = "INSERT INTO REF_MSMS_LIBRARY_PEAK(MRC2_LIB_ID, MZ, INTENSITY) VALUES(?, ?, ?)";
		PreparedStatement msPeaksPs = conn.prepareStatement(msPeaksQuery);

//		PreparedStatement idPs = conn.prepareStatement("SELECT '" + DataPrefix.MSMS_LIBRARY_ENTRY.name() +
//				"' || LPAD(MSMS_LIB_ENTRY_SEQ.NEXTVAL, 9, '0') AS MSMS_LIB_ID FROM DUAL");
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			// Create next MS lib ID
//			ResultSet idrs = idPs.executeQuery();
//			String nextId = null;
//			while(idrs.next())
//				nextId = idrs.getString(1);
//			
//			idrs.close();
			
//			PreparedStatement idPs = conn.prepareStatement("SELECT '" + DataPrefix.MSMS_LIBRARY_ENTRY.name() +
//			"' || LPAD(MSMS_LIB_ENTRY_SEQ.NEXTVAL, 9, '0') AS MSMS_LIB_ID FROM DUAL");
			
			String polarity = rs.getString("POLARITY");
			double collisionEnergy = rs.getDouble("COLLISION_ENERGY");
			String metlinId =  rs.getString("LIBRARY_ENTRY_ID");

			//	Insert component
			String nextId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_LIB_ENTRY_SEQ",
					DataPrefix.MSMS_LIBRARY_ENTRY,
					"0",
					9);
			insertComponentPs.setString(1, nextId);
			insertComponentPs.setString(2, polarity);
			insertComponentPs.setDouble(3, collisionEnergy);
			insertComponentPs.setString(4, metlinId);
			insertComponentPs.executeUpdate();

			//	Get MSMS peaks
			msPs.setString(1,metlinId);
			msPs.setDouble(2, collisionEnergy);
			msPs.setString(3, polarity);
			ResultSet msrs = msPs.executeQuery();

			msPeaksPs.setString(1,nextId);
			while(msrs.next()) {
				msPeaksPs.setDouble(2, msrs.getDouble("MZ"));
				msPeaksPs.setDouble(3, msrs.getDouble("HEIGHT"));
				msPeaksPs.addBatch();
			}
			msPeaksPs.executeBatch();
			msrs.close();
		}
		rs.close();
//		idPs.close();
		msPs.close();
		msPeaksPs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}


	public static void generatePeptideStructureData() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT DISTINCT PEPTIDE_SEQUENCE FROM REF_MSMS_COMPOUND_DATA_PEP WHERE SMILES IS NULL";
		PreparedStatement ps = conn.prepareStatement(query);

		String updQuery = "UPDATE REF_MSMS_COMPOUND_DATA_PEP SET SMILES = ?, INCHI_KEY = ? WHERE PEPTIDE_SEQUENCE = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);

		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			String pepSeq = rs.getString("PEPTIDE_SEQUENCE");
			CompoundIdentity cid = null;

			try {
				cid = PeptideUtils.generatePeptideIdentifiers(pepSeq);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(cid != null) {

				updPs.setString(1, cid.getSmiles());
				updPs.setString(2, cid.getInChiKey());
				updPs.setString(3, pepSeq);
				updPs.executeUpdate();
			}
		}
		rs.close();
		updPs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	/**
	 * Load NIST peptide MSMS spectral data
	 */
	public static void uploadNISTPeptideMsMsData() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		Files.find(Paths.get("E:\\DataAnalysis\\Databases\\NIST17\\Export\\MSMS2"), Integer.MAX_VALUE,
			(filePath, fileAttr) -> (filePath.toString().endsWith(".MSP")) && fileAttr.isRegularFile())
			.forEach(path -> {
				try {
					File inputFile = new File(path.toString());
					List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(inputFile);
					for(List<String> chunk : mspChunks) {

						NISTTandemMassSpectrum msms = NISTMSPParser.parseNistMspDataSource(chunk);
						if(!NISTMSPParser.isSpectrumInDatabase(
								NISTReferenceLibraries.nist_msms2.name(), Integer.toString(msms.getDbnum()), conn)) {
							NISTMSPParser.insertPeptideSpectrumRecord(msms, conn);
						}
						//	System.out.println("****");
					}
					System.out.println(inputFile.getName() + " was processed.");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			});
		ConnectionManager.releaseConnection(conn);
		System.out.println("MSMS upload completed");
	}

	public static void parseMissingMSMSStructuresFromPubChem() throws Exception {

		File folder = Paths.get("E:\\DataAnalysis\\Databases\\MSMS").toFile();
		String[] files = folder.list();
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		IteratingSDFReader reader;
		Connection conn = ConnectionManager.getConnection();
		String query =
			"UPDATE REF_MSMS_COMPOUND_DATA "
			+ "SET PUBCHEM_SMILES = ?, PUBCHEM_NAME = ?,  "
			+ "PUBCHEM_FORMULA = ?, ACCESSION = ? WHERE INCHI_KEY = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		String inchiKey;
		int count = 0;
		for (String file : files) {

			inchiKey = FilenameUtils.getBaseName(file);
			reader = new IteratingSDFReader(new FileInputStream(Paths.get("E:\\DataAnalysis\\Databases\\\\MSMS", file).toFile()), coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
				Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
				molecule.getProperties().forEach((k,v)->{

					if(pubchemDataMap.containsKey(k.toString()))
						pubchemDataMap.put(k.toString(), v.toString());

					if(namingMap.containsKey(k.toString()))
						namingMap.put(k.toString(), v.toString());
				});
				String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
				String smiles = pubchemDataMap.get(PubChemFields.SMILES_ISOMERIC.toString());
				if(smiles.isEmpty())
					pubchemDataMap.get(PubChemFields.SMILES_CANONICAL.toString());

				if(smiles.isEmpty()) {
					try {
						smiles = smilesGenerator.create(molecule);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ps.setString(1, smiles);
				ps.setString(2, namingMap.get(PubChemFields.IUPAC_NAME.toString()));
				ps.setString(3, pubchemDataMap.get(PubChemFields.FORMULA.toString()));
				ps.setString(4, id);
				ps.setString(5, inchiKey);
				ps.executeUpdate();
			}
			count++;
			System.out.println(Integer.toString(count));
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void fetchMissingMSMSCompoundDataFromPubChem() throws Exception {

		String encoding = StandardCharsets.UTF_8.toString();
		Reader inputStreamReader = new InputStreamReader(new FileInputStream(new File("E:\\DataAnalysis\\Databases\\MSMS\\inchik.txt")), encoding);
		String[][] parsedData = CSVParser.parse(inputStreamReader, ',');
		for(int i=1; i<parsedData.length; i++) {

			String inchikey = parsedData[i][0];
			String requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchikey + "/record/SDF";

			InputStream pubchemDataStream = WebUtils.getInputStreamFromURLsilent(requestUrl);
			if(pubchemDataStream != null) {

				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\MSMS", inchikey + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			}
			else {
				System.out.println(inchikey);
			}
		}
	}

	public static void fixMonaBugs() throws Exception {

		Connection conn = CompoundDbConnectionManager.getConnection();
		String query = "SELECT DISTINCT NAME FROM MONA_INCHI_BUGS ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		String requestUrl = "";
		String cpdName = "";
		String updQuery =
			"UPDATE MONA_INCHI_BUGS SET INCHIKEY = ?, SMILES = ?, FORMULA = ?, ACCESSION = ? WHERE NAME = ?";
		PreparedStatement ups = conn.prepareStatement(updQuery);
		InputStream pubchemDataStream = null;
		String encoding = StandardCharsets.UTF_8.toString();
		while(rs.next()) {

			cpdName = rs.getString("NAME");
			requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/"
				+ URLEncoder.encode(cpdName, encoding)
				+ "/property/MolecularFormula,InChIKey,CanonicalSMILES,IsomericSMILES/CSV";
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
				Reader inputStreamReader = new InputStreamReader(pubchemDataStream, encoding);
				String[][] parsedData = CSVParser.parse(inputStreamReader, ',');
				pubchemDataStream.close();
				if(parsedData[0][0].equals("CID")) {

					ups.setString(1, parsedData[1][2]);
					ups.setString(2, parsedData[1][4]);
					ups.setString(3, parsedData[1][1]);
					ups.setString(4, parsedData[1][0]);
					ups.setString(5, cpdName);
					ups.executeUpdate();
				}
			} catch (Exception e) {
				//System.out.println(cpdName);
				//e.printStackTrace();
			}
		}
		rs.close();
		ps.close();
		ups.close();
		CompoundDbConnectionManager.releaseConnection(conn);
	}

	/*String[] dirs = new String[] {
			"Y:\\DataAnalysis\\_Reports\\EX00962 - Plasma metabolomics in LID\\A003 - Untargeted\\Raw data\\NEG",
			"Y:\\DataAnalysis\\_Reports\\EX00962 - Plasma metabolomics in LID\\A003 - Untargeted\\Raw data\\POS",
	};

	for(String dir : dirs) {
		try {

			clearResultsFolders(dir);
			//parseMissingNistStructuresFromPubChemToNist();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	public static void clearResultsFolders(String rawDataDirectory) throws IOException {

		if(!Paths.get(rawDataDirectory).toFile().exists())
			return;

		List<Path> pathList = Files.find(Paths.get(rawDataDirectory),
				Integer.MAX_VALUE, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
				collect(Collectors.toList());


			for(Path p : pathList)
				FileUtils.deleteDirectory(Paths.get(p.toString(), "Results").toFile());

		System.out.println(rawDataDirectory + " processed.");
	}

	public static void parseMissingNistStructuresFromPubChemToNist() throws Exception {

		File folder = Paths.get("E:\\DataAnalysis\\Databases\\NIST17\\Compound").toFile();
		String[] files = folder.list();
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		IteratingSDFReader reader;
		Connection conn = CompoundDbConnectionManager.getConnection();
		String query = "UPDATE NIST_COMPOUND_DATA SET SMILES = ? WHERE INCHI_KEY = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		String inchiKey;
		for (String file : files) {

			inchiKey = FilenameUtils.getBaseName(file);
			reader = new IteratingSDFReader(new FileInputStream(Paths.get("E:\\DataAnalysis\\Databases\\NIST17\\Compound", file).toFile()), coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				Map<String, String> pubchemDataMap = PubChemParser.getCompoundDataMap();
				Map<String, String> namingMap = PubChemParser.getCompoundNamingMap();
				molecule.getProperties().forEach((k,v)->{

					if(pubchemDataMap.containsKey(k.toString()))
						pubchemDataMap.put(k.toString(), v.toString());

					if(namingMap.containsKey(k.toString()))
						namingMap.put(k.toString(), v.toString());
				});
				String id = pubchemDataMap.get(PubChemFields.PUBCHEM_ID.toString());
				String smiles = pubchemDataMap.get(PubChemFields.SMILES_ISOMERIC.toString());
				if(smiles.isEmpty())
					pubchemDataMap.get(PubChemFields.SMILES_CANONICAL.toString());

				if(smiles.isEmpty()) {
					try {
						smiles = smilesGenerator.create(molecule);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ps.setString(1, smiles);
				ps.setString(2, inchiKey);
				ps.executeUpdate();
			}
		}
		ps.close();
		CompoundDbConnectionManager.releaseConnection(conn);
	}

	public static void fetchMissingNistStructuresFromPubChem() throws Exception {

		//	Get missing structures
		Connection conn = CompoundDbConnectionManager.getConnection();
		String query = "SELECT DISTINCT INCHI_KEY FROM NIST_COMPOUND_DATA "
				+ "WHERE MOL IS NULL AND SMILES IS NULL AND INCHI_KEY IS NOT NULL";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		String requestUrl = "";
		String inchikey = "";
		InputStream pubchemDataStream = null;
		while(rs.next()) {

			inchikey = rs.getString("INCHI_KEY");

			requestUrl =
				"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" +
				inchikey + "/record/SDF";
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
				Files.copy(
						pubchemDataStream,
					      Paths.get("E:\\DataAnalysis\\Databases\\NIST17\\Compound", inchikey + ".SDF"),
					      StandardCopyOption.REPLACE_EXISTING);

				pubchemDataStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		rs.close();
		ps.close();
		CompoundDbConnectionManager.releaseConnection(conn);
	}

	private static void exportMsMsLibForExperiment() {

		String samplePrepId = "SPR0037";
		String sampleId = "IDS000041";
		String daMethodId = "DXM0091";
		String acquisitionMethodId = "DQM0524";
		Polarity polarity = Polarity.Positive;
		String dataFileName = "EX00961-iDDA-POS-MATCH.TXT";
		String libraryName = "EX00961-iDDA-based-POS-library";
		String libraryDescription = "EX00961 MS1 positive mode library based on iDDA identification data";
		String libId = null;

		matchIddaData(
				samplePrepId,
				sampleId,
				daMethodId,
				acquisitionMethodId,
				polarity,
				dataFileName);

		File libFile =
				Paths.get("C:\\Users\\Sasha\\Downloads\\MSMS", dataFileName + "_4LIB.TXT").toFile();

		String sourceFile = libFile.getAbsolutePath();
		try {
			libId = RemoteMsLibraryUtils.createNewLibrary(libraryName, libraryDescription);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(libFile.exists() && libId != null)
			insertLibraryFromIddaMatchedData(sourceFile, libId);
	}

	private static void MINEImport() {

		File exportFile = new File("C:\\Users\\Sasha\\Downloads\\MSMS\\MineExport\\MINE-POS.MSP");
		File exportFile2 = new File("C:\\Users\\Sasha\\Downloads\\MSMS\\MineExport\\MINE-NEG.MSP");
/*		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		try {
			exportMineForNist("Positive", exportFile);
			exportMineForNist("Negative", exportFile2);
			//updateMonaDataFromSmiles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void exportMineForNist(String ionMode, File exportFile) throws Exception {

		exportFile =
				FIOUtils.changeExtension(
					exportFile, MsLibraryFormat.MSP.getFileExtension());
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		String msMode = null;

		Connection conn = CompoundDbConnectionManager.getConnection();
		String query =
			"SELECT M.UNIQUE_ID, M.MINE_SPECTRUM_ID,  " +
			"M.INSTRUMENT, M.IONIZATION_MODE, M.FRAG_ENERGY, " +
			"C.NAME, C.EXACT_MASS, C.FORMULA, C.INCHI_KEY " +
			"FROM MINE_SPECTRUM_MAP M, MINE_COMPOUND_DATA C " +
			"WHERE M.IONIZATION_MODE = ? " +
			"AND M.MINE_SPECTRUM_ID = C.MINE_SPECTRUM_ID " +
			"ORDER BY M.MINE_SPECTRUM_ID, M.FRAG_ENERGY ";
		PreparedStatement ps = conn.prepareStatement(query);

		String msQuery =
			"SELECT MZ, INTENSITY, ADDUCT, IS_PARENT FROM MINE_PEAK WHERE UNIQUE_ID = ? ORDER BY MZ";
		PreparedStatement msps = conn.prepareStatement(msQuery);

		ps.setString(1, ionMode);
		ResultSet rs = ps.executeQuery();
		ResultSet msrs = null;
		StringBuffer sb  = null;
		while(rs.next()) {

			writer.append(MSPField.NAME.getName() + ": " + rs.getString("UNIQUE_ID") + "\n");
			if(rs.getString("NAME") != null)
				writer.append(MSPField.SYNONYM.getName() + ": " + rs.getString("NAME") + "\n");

			writer.append(MSPField.FORMULA.getName() + ": " + rs.getString("FORMULA") + "\n");

			msMode = "P";
			if(rs.getString("IONIZATION_MODE").equals("Negative"))
				msMode = "N";

			writer.append(MSPField.ION_MODE.getName() + ": " + msMode + "\n");
			writer.append(MSPField.EXACTMASS.getName() + ": "
					+ MRC2ToolBoxConfiguration.getMzFormat().format(rs.getDouble("EXACT_MASS")) + "\n");

			writer.append(MSPField.COLLISION_ENERGY.getName() + ": " + rs.getString("FRAG_ENERGY") + "\n");
			writer.append(MSPField.INSTRUMENT_TYPE.getName() + ": " + rs.getString("INSTRUMENT") + "\n");
			writer.append(MSPField.INCHI_KEY.getName() + ": " + rs.getString("INCHI_KEY") + "\n");

			msps.setString(1, rs.getString("UNIQUE_ID"));
			msrs = msps.executeQuery();
			int pointCount = 0;
			sb = new StringBuffer();
			while(msrs.next()) {

				if(msrs.getString("IS_PARENT").equals("Y")) {
					writer.append(MSPField.PRECURSORMZ.getName() + ": "
							+ MRC2ToolBoxConfiguration.getMzFormat().format(msrs.getDouble("MZ")) + "\n");

					if(msrs.getString("ADDUCT") != null)
						writer.append(MSPField.PRECURSOR_TYPE.getName() + ": " + msrs.getString("ADDUCT") + "\n");
				}
				sb.append(MRC2ToolBoxConfiguration.getMzFormat().format(msrs.getDouble("MZ"))
						+ " " + intensityFormat.format(msrs.getDouble("INTENSITY")) + "; ") ;
					pointCount++;

				if(pointCount % 5 == 0)
					sb.append("\n");
			}
			writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(pointCount) + "\n");
			writer.append(sb.toString());
			writer.append("\n\n");
			msrs.close();
		}
		rs.close();
		ps.close();
		msps.close();
		CompoundDbConnectionManager.releaseConnection(conn);

		writer.flush();
		writer.close();
	}

	private static void exportLipidBlastForNist(String ionMode, File exportFile) throws Exception {

		exportFile =
				FIOUtils.changeExtension(
					exportFile, MsLibraryFormat.MSP.getFileExtension());
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));

		Connection conn = CompoundDbConnectionManager.getConnection();
		String query =
			"SELECT LB_ID, CODE_NAME, FULL_NAME, MOL_FORMULA, PRECURSOR_MZ, ADDUCT, "
			+ "MS_MODE, CLASS_CODE, FORMULA_MASS FROM LIPIDBLAST_PRECURSOR WHERE MS_MODE = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		String msQuery =
			"SELECT FRAGMENT_MZ, REL_INTENSITY, FRAGMENT_NAME FROM LIPIDBLAST_FRAGMENT WHERE LB_ID = ? ORDER BY FRAGMENT_MZ";
		PreparedStatement msps = conn.prepareStatement(msQuery);

		ps.setString(1, ionMode);
		ResultSet rs = ps.executeQuery();
		ResultSet msrs = null;
		StringBuffer sb  = null;
		while(rs.next()) {

			writer.append(MSPField.NAME.getName() + ": " + rs.getString("LB_ID") + "\n");
			writer.append(MSPField.SYNONYM.getName() + ": " + rs.getString("CODE_NAME") + "\n");
			writer.append(MSPField.SYNONYM.getName() + ": " + rs.getString("FULL_NAME") + "\n");
			writer.append(MSPField.FORMULA.getName() + ": " + rs.getString("MOL_FORMULA") + "\n");
			writer.append(MSPField.ION_MODE.getName() + ": " + rs.getString("MS_MODE") + "\n");
			writer.append(MSPField.EXACTMASS.getName() + ": "
					+ MRC2ToolBoxConfiguration.getMzFormat().format(rs.getDouble("FORMULA_MASS")) + "\n");

			if(rs.getString("ADDUCT") != null)
				writer.append(MSPField.PRECURSOR_TYPE.getName() + ": " + rs.getString("ADDUCT") + "\n");

			if(rs.getString("PRECURSOR_MZ") != null)
				writer.append(MSPField.PRECURSORMZ.getName() + ": " + rs.getString("PRECURSOR_MZ") + "\n");

			msps.setString(1, rs.getString("LB_ID"));
			msrs = msps.executeQuery();
			int pointCount = 0;
			sb = new StringBuffer();
			while(msrs.next()) {

				sb.append(
					MRC2ToolBoxConfiguration.getMzFormat().format(msrs.getDouble("FRAGMENT_MZ"))
					+ " " + intensityFormat.format(msrs.getDouble("REL_INTENSITY"))
					+ " \"" + msrs.getString("FRAGMENT_NAME") +"\"\n") ;
				pointCount++;
			}
			writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(pointCount) + "\n");
			writer.append(sb.toString());
			writer.append("\n\n");
			msrs.close();
		}
		rs.close();
		ps.close();
		msps.close();
		CompoundDbConnectionManager.releaseConnection(conn);

		writer.flush();
		writer.close();
	}

	private static void updateMonaDataFromSmiles() throws Exception {

		Connection conn = CompoundDbConnectionManager.getConnection();
		String query =
				"SELECT DISTINCT SMILES FROM MONA_SPECTRUM_METADATA "
				+ "WHERE SMILES IS NOT NULL AND INCHIKEY IS NULL";
		PreparedStatement ps = conn.prepareStatement(query);

		String updQuery =
			"UPDATE MONA_SPECTRUM_METADATA SET INCHIKEY = ? WHERE SMILES = ? ";
		PreparedStatement updPs = conn.prepareStatement(updQuery);

		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			IAtomContainer mol = null;
			String smiles = rs.getString("SMILES");
			try {
				mol = smipar.parseSmiles(smiles);
//				IMolecularFormula molFormula = MolecularFormulaManipulator.getMolecularFormula(mol);
//				System.out.println("Charge: " + Integer.toString(molFormula.getCharge()));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if(mol == null)
				continue;

			String inchiKey = null;
			try {
				inChIGenerator = igfactory.getInChIGenerator(mol);
				INCHI_RET ret = inChIGenerator.getReturnStatus();
				if (ret == INCHI_RET.WARNING || ret == INCHI_RET.OKAY)
					inchiKey = inChIGenerator.getInchiKey();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(inchiKey == null)
				continue;

			updPs.setString(1, inchiKey);
			updPs.setString(2, smiles);
			updPs.executeUpdate();
		}
		rs.close();
		ps.close();
		updPs.close();
		CompoundDbConnectionManager.releaseConnection(conn);
	}

	private static void exportMonaForNist(String ionMode, File exportFile) throws Exception {

		exportFile =
				FIOUtils.changeExtension(
					exportFile, MsLibraryFormat.MSP.getFileExtension());
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));

		Connection conn = CompoundDbConnectionManager.getConnection();
		String query =
			"SELECT ID, NAME, FORMULA, INCHIKEY, PRECURSOR_MZ, NUM_PEAKS, "
			+ "PRECURSOR_TYPE, COLLISION_ENERGY, INSTRUMENT, INSTRUMENT_TYPE, ION_MODE "
			+ "FROM MONA_SPECTRUM_METADATA WHERE RESOLUTION='HIGH' AND ION_MODE = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		String msQuery =
			"SELECT MZ, INTENSITY FROM MONA_PEAK WHERE ID = ? ORDER BY MZ";
		PreparedStatement msps = conn.prepareStatement(msQuery);

		ps.setString(1, ionMode);
		ResultSet rs = ps.executeQuery();
		ResultSet msrs = null;
		StringBuffer sb  = null;
		while(rs.next()) {
			writer.append(MSPField.NAME.getName() + ": " + rs.getString("ID") + "\n");
			writer.append(MSPField.SYNONYM.getName() + ": " + rs.getString("NAME") + "\n");
			writer.append(MSPField.FORMULA.getName() + ": " + rs.getString("FORMULA") + "\n");
			writer.append(MSPField.INCHI_KEY.getName() + ": " + rs.getString("INCHIKEY") + "\n");
			writer.append(MSPField.ION_MODE.getName() + ": " + rs.getString("ION_MODE") + "\n");

			if(rs.getString("COLLISION_ENERGY") != null)
				writer.append(MSPField.COLLISION_ENERGY.getName() + ": " + rs.getString("COLLISION_ENERGY") + "\n");

			if(rs.getString("PRECURSOR_TYPE") != null)
				writer.append(MSPField.PRECURSOR_TYPE.getName() + ": " + rs.getString("PRECURSOR_TYPE") + "\n");

			if(rs.getString("PRECURSOR_MZ") != null)
				writer.append(MSPField.PRECURSORMZ.getName() + ": " + rs.getString("PRECURSOR_MZ") + "\n");

			if(rs.getString("INSTRUMENT") != null)
				writer.append(MSPField.INSTRUMENT.getName() + ": " + rs.getString("INSTRUMENT") + "\n");

			if(rs.getString("INSTRUMENT_TYPE") != null)
				writer.append(MSPField.INSTRUMENT_TYPE.getName() + ": " + rs.getString("INSTRUMENT_TYPE") + "\n");

			msps.setString(1, rs.getString("ID"));
			msrs = msps.executeQuery();
			int pointCount = 0;
			sb = new StringBuffer();
			while(msrs.next()) {

				sb.append(
					MRC2ToolBoxConfiguration.getMzFormat().format(msrs.getDouble("MZ"))
					+ " " + intensityFormat.format(msrs.getDouble("INTENSITY")) + "; ") ;
				pointCount++;
				if(pointCount % 5 == 0)
					sb.append("\n");
			}
			writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(pointCount) + "\n");
			writer.append(sb.toString());
			writer.append("\n\n");
			msrs.close();
		}
		rs.close();
		ps.close();
		msps.close();
		CompoundDbConnectionManager.releaseConnection(conn);

		writer.flush();
		writer.close();
	}

	private static void parseNistSearchResults() {

		File fileToParse = new File("E:\\Eclipse\\git2\\CefAnalyzerMVN\\data\\mssearch\\NIST_MSMS_PEPSEARCH_RESULTS_20190726-131237.TXT");
		String[][] searchResultsArray = null;
		try {
			searchResultsArray = DelimitedTextParser.parseTextFileWithEncodingSkippingComments(
					fileToParse, MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<Integer,NISTPepSearchOutputFields>columnMap = new TreeMap<Integer,NISTPepSearchOutputFields>();
		if(searchResultsArray == null)
			return;

		String[] header = searchResultsArray[0];
		for(int i=0; i<header.length; i++) {

			NISTPepSearchOutputFields field = NISTPepSearchOutputFields.getFieldByColumnName(header[i]);
			if(field != null)
				columnMap.put(i, field);
		}
		ArrayList<Map<NISTPepSearchOutputFields,String>>searchResults =
				new ArrayList<Map<NISTPepSearchOutputFields,String>>();
		for(int i=1; i<searchResultsArray.length; i++) {

			Map<NISTPepSearchOutputFields,String>result = new TreeMap<NISTPepSearchOutputFields,String>();
			String[] line = searchResultsArray[i];
			for(Entry<Integer, NISTPepSearchOutputFields> entry : columnMap.entrySet()) {

				if(entry.getKey()<line.length)
					result.put(entry.getValue(),line[entry.getKey()]);
			}
			searchResults.add(result);
		}
		System.out.println("***");
	}

	private static void parseMineData() {

		File inputFile = Paths.get("E:\\DataAnalysis\\Databases\\MINE\\KEGG", "Positive_CFM_Spectra.msp").toFile();
		Connection connection = null;

		try {
			connection = CompoundDbConnectionManager.getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MINEMSPParser parser = new MINEMSPParser(Polarity.Positive, connection);

		try {
			MINEMSPParser.pareseInputFile(inputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void insertLibraryFromIddaMatchedData(String sourceFile, String libId) {

		MRC2ToolBoxConfiguration.initConfiguration();
		
		//	Read input library data
		String[][]nameArray = null;
		File nameFile = new File(sourceFile);
		nameArray = DelimitedTextParser.parseTextFile(nameFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		try {
			Connection conn = ConnectionManager.getConnection();
			Connection cpdDbConn = CompoundDbConnectionManager.getConnection();

			String query =
					"INSERT INTO MS_LIBRARY_COMPONENT " +
					"(TARGET_ID, CID, DATE_LOADED, LAST_MODIFIED, " +
					"RETENTION_TIME, RT_MIN, RT_MAX, NAME, ID_CONFIDENCE, LIBRARY_ID, POLARITY) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement stmt = conn.prepareStatement(query);
			java.sql.Date sqlCreated = new java.sql.Date(new Date().getTime());
			java.sql.Date sqlModified = sqlCreated;

			//	TODO
			String adductQuery =
					"INSERT INTO MS_LIBRARY_COMPONENT_ADDUCT (TARGET_ID, ADDUCT) VALUES (?, ?)";

			PreparedStatement adductStmt = conn.prepareStatement(adductQuery);

			for(int i=1; i<nameArray.length; i++) {

				String accession = nameArray[i][0];
				String adduct = nameArray[i][2];
				double rt = Double.parseDouble(nameArray[i][1]);
				int polarity = Integer.parseInt(nameArray[i][3]);
				String uniqueId = DataPrefix.MS_LIBRARY_TARGET.getName() + UUID.randomUUID().toString();
				CompoundIdentity cpdId = CompoundDatabaseUtils.getCompoundById(accession, cpdDbConn);

				stmt.setString(1,uniqueId);
				stmt.setString(2, accession);
				stmt.setDate(3, sqlCreated);
				stmt.setDate(4, sqlModified);
				stmt.setDouble(5, rt);
				stmt.setDouble(6, rt);
				stmt.setDouble(7, rt);
				stmt.setString(8, cpdId.getName());
				stmt.setString(9, CompoundIdentificationConfidence.ACCURATE_MASS_MSMS.getLevelId());
				stmt.setString(10, libId);
				stmt.setInt(11, polarity);

				stmt.executeUpdate();

				adductStmt.setString(1, uniqueId);
				adductStmt.setString(2, adduct);
				adductStmt.executeUpdate();
			}
			stmt.close();
			adductStmt.close();
			ConnectionManager.releaseConnection(conn);
			CompoundDbConnectionManager.releaseConnection(cpdDbConn);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Complete");
	}

	public static void matchIddaData(
			String samplePrepId,
			String sampleId,
			String daMethodId,
			String acquisitionMethodId,
			Polarity polarity,
			String dataFileName) {

		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			AdductManager.refreshAlldata();;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Collection<MsFeature> features = new ArrayList<MsFeature>();
		Collection<MsFeature> msmsFeatures = new ArrayList<MsFeature>();
		try {
			Connection conn = ConnectionManager.getConnection();
			features =
				IDTMsDataUtils.getReferenceMS1FeaturesForSample(
						sampleId, acquisitionMethodId, daMethodId, conn);

			msmsFeatures =
				IDTMsDataUtils.getIdentifiedMSMSFeaturesForSample(
						sampleId,
						samplePrepId,
						polarity,
						conn);

			ConnectionManager.releaseConnection(conn);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<MsFeature,Collection<MsFeature>> tandemMap =
			new HashMap<MsFeature,Collection<MsFeature>>();

		for(MsFeature f : features) {

			Range rtLookupRange =
				new Range(f.getRetentionTime() - 0.1d, f.getRetentionTime() + 0.1d);
			Collection<MsFeature>rtFiltered =
					msmsFeatures.stream().
					filter(tdf -> rtLookupRange.contains(tdf.getRetentionTime())).
					collect(Collectors.toList());

			if(rtFiltered.isEmpty())
				continue;

			Range parentMzLookupRange = MsUtils.createPpmMassRange(f.getSpectrum().getBasePeakMz(), 50.0d);
			Collection<MsFeature>mzFiltered =
					rtFiltered.stream().
					filter(tdf -> parentMzLookupRange.contains(
							tdf.getSpectrum().getTandemSpectra().iterator().next().getParent().getMz())).
					collect(Collectors.toList());

			if(mzFiltered.isEmpty())
				continue;

			tandemMap.put(f, mzFiltered);
		}
		Polarity pol = Polarity.Positive;
		if(polarity.equals("N"))
			pol = Polarity.Negative;

		Collection<String>compoundData = new ArrayList<String>();
		String header =
			"PARENT_FEATURE_ID	PARENT_MZ	PARENT_RT	ADUCT	MSMS_FEATURE_ID	MSMS_PARENT_MZ	"
			+ "MS_RT	Delta RT	MZ_ERROR_PPM	CID	ACCESSION	NAME	FORMULA	NEUTRAL MASS	BULK_ID";
		compoundData.add(header);
		Collection<String>fLine = new ArrayList<String>();
		HashSet<LibraryEntrySource>uniqueEntries = new HashSet<LibraryEntrySource>();
		for (Entry<MsFeature, Collection<MsFeature>> entry : tandemMap.entrySet()) {

			MsFeature parent = entry.getKey();
			for(MsFeature msms : entry.getValue()) {

				fLine.clear();
				fLine.add(parent.getId());
				fLine.add(MRC2ToolBoxConfiguration.getMzFormat().format(parent.getBasePeakMz()));
				fLine.add(MRC2ToolBoxConfiguration.getRtFormat().format(parent.getRetentionTime()));
				fLine.add(parent.getSpectrum().getPrimaryAdduct().getName());

				fLine.add(msms.getId());
				TandemMassSpectrum tdMs = msms.getSpectrum().getTandemSpectra().iterator().next();
				fLine.add(MRC2ToolBoxConfiguration.getMzFormat().format(tdMs.getParent().getMz()));
				fLine.add(MRC2ToolBoxConfiguration.getRtFormat().format(msms.getRetentionTime()));
				fLine.add(MRC2ToolBoxConfiguration.getRtFormat().format(parent.getRetentionTime() - msms.getRetentionTime()));

				double massError = Math.abs((parent.getBasePeakMz() - tdMs.getParent().getMz())/parent.getBasePeakMz()) * 1000000.0d;
				fLine.add(MRC2ToolBoxConfiguration.getPpmFormat().format(massError));

				fLine.add(Double.toString(tdMs.getCidLevel()));

				CompoundIdentity cid = msms.getPrimaryIdentity().getCompoundIdentity();
				fLine.add(cid.getPrimaryDatabaseId());
				fLine.add(cid.getName());
				fLine.add(cid.getFormula());
				fLine.add(MRC2ToolBoxConfiguration.getMzFormat().format(cid.getExactMass()));

				String lipidBulk = cid.getDbId(CompoundDatabaseEnum.LIPIDMAPS_BULK);
				if(lipidBulk == null)
					lipidBulk = cid.getPrimaryDatabaseId();
				fLine.add(lipidBulk);
				compoundData.add(StringUtils.join(fLine,  MRC2ToolBoxConfiguration.getTabDelimiter()));

				LibraryEntrySource les = new LibraryEntrySource(
						lipidBulk,
						parent.getRetentionTime(),
						pol,
						parent.getSpectrum().getPrimaryAdduct());

				uniqueEntries.add(les);
			}
		}
		File outputFile =
				Paths.get("C:\\Users\\Sasha\\Downloads\\MSMS", dataFileName + ".TXT").toFile();
		try {
			final Writer writer = new BufferedWriter(new FileWriter(outputFile));
			writer.append(StringUtils.join(compoundData,  System.getProperty("line.separator")));
			writer.flush();
			writer.close();
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File outputFile2 =
				Paths.get("C:\\Users\\Sasha\\Downloads\\MSMS", dataFileName + "_4LIB.TXT").toFile();
		try {
			final Writer writer = new BufferedWriter(new FileWriter(outputFile2));
			writer.append("ID	RT	ADUCT	POLARITY\n");
			for(LibraryEntrySource les : uniqueEntries) {

				fLine.clear();
				fLine.add(les.getBulkId());
				fLine.add(MRC2ToolBoxConfiguration.getRtFormat().format(les.getRt()));
				fLine.add(les.getAdduct().getName());
				fLine.add(Integer.toString(les.getPolarity().getSign()));
				writer.append(StringUtils.join(fLine,  MRC2ToolBoxConfiguration.getTabDelimiter()) + "\n");
			}
			writer.flush();
			writer.close();
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Complete");
	}

	private static void updateNistMSMS2PeptideData() {

		CompoundIdentity pepId = new CompoundIdentity();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		InChIGeneratorFactory igfactory = null;
		InChIGenerator inChIGenerator = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IBioPolymer peptide = null;
		try {
			Connection conn = ConnectionManager.getConnection();
			String query =
					"SELECT DISTINCT N.PEPTIDE_SEQUENCE FROM REF_MSMS_COMPOUND_DATA N "
					+ "WHERE N.SMILES IS NULL ORDER BY 1";
			PreparedStatement ps = conn.prepareStatement(query);

			String updQuery =
				"UPDATE REF_MSMS_COMPOUND_DATA SET SMILES = ?, INCHI_KEY = ? "
				+ "WHERE PEPTIDE_SEQUENCE = ? AND SMILES IS NULL";
			PreparedStatement updps = conn.prepareStatement(updQuery);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {

				String oneLetterSeq = rs.getString("PEPTIDE_SEQUENCE");
				String smiles = null;
				String inchi = null;
				String inchikey = null;
				peptide = null;
				if(!oneLetterSeq.isEmpty()) {
					try {
						peptide = ProteinBuilderTool.createProtein(oneLetterSeq);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						System.out.println("PEPTIDE failed: " + oneLetterSeq);
					}
				}
				if(peptide != null){
					try {
						AtomContainer mpep = new AtomContainer(peptide);
						AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mpep);
						CDKHydrogenAdder.getInstance(mpep.getBuilder()).addImplicitHydrogens(mpep);
						try {
							smiles =  smilesGenerator.create(mpep);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println("SMILES failed: " + oneLetterSeq);
						}
						inChIGenerator = igfactory.getInChIGenerator(mpep);
						INCHI_RET ret = inChIGenerator.getReturnStatus();
						if (ret == INCHI_RET.WARNING || ret == INCHI_RET.OKAY) {
							inchi = inChIGenerator.getInchi();
							inchikey = inChIGenerator.getInchiKey();
						}
						else{
							System.out.println("InChI failed: " + ret.toString() + " [" + inChIGenerator.getMessage() + "]");
						}
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				updps.setString(1, smiles);
				updps.setString(2, inchikey);
				updps.setString(3, oneLetterSeq);
				updps.executeUpdate();
			}
			rs.close();
			ps.close();
			updps.close();
			ConnectionManager.releaseConnection(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateMetlinPeptideData() {

		CompoundIdentity pepId = new CompoundIdentity();
		SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
		InChIGeneratorFactory igfactory = null;
		InChIGenerator inChIGenerator = null;
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IBioPolymer peptide = null;
		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			String query =
					"SELECT ACCESSION, PRIMARY_NAME FROM COMPOUND_DATA "
					+ "WHERE SOURCE_DB = ? AND SMILES IS NULL ORDER BY 1";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, "METLIN");

			String updQuery = "UPDATE COMPOUND_DATA SET SMILES = ?, INCHI = ?, INCHI_KEY = ? WHERE ACCESSION = ?";
			PreparedStatement updps = conn.prepareStatement(updQuery);
			String synQuery = "INSERT INTO COMPOUND_SYNONYMS (ACCESSION, NAME, NTYPE) VALUES (?, ?, ?)";
			PreparedStatement synps = conn.prepareStatement(synQuery);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {

				String accession = rs.getString("ACCESSION");
				String pepSeq = rs.getString("PRIMARY_NAME");
				String smiles = null;
				String inchi = null;
				String inchikey = null;
				String oneLetterSeq = PeptideUtils.translateThreeLetterToOneLetterCode(pepSeq);
				peptide = null;
				if(!oneLetterSeq.isEmpty()) {
					try {
						peptide = ProteinBuilderTool.createProtein(oneLetterSeq);
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						System.out.println("PEPTIDE failed: " + pepSeq + " [" + pepSeq + "]");
					}
				}
				if(peptide != null){
					try {
						AtomContainer mpep = new AtomContainer(peptide);
						AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mpep);
						CDKHydrogenAdder.getInstance(mpep.getBuilder()).addImplicitHydrogens(mpep);
						try {
							smiles =  smilesGenerator.create(mpep);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println("SMILES failed: " + pepSeq + " [" + pepSeq + "]");
						}
						inChIGenerator = igfactory.getInChIGenerator(mpep);
						INCHI_RET ret = inChIGenerator.getReturnStatus();
						if (ret == INCHI_RET.WARNING || ret == INCHI_RET.OKAY) {
							inchi = inChIGenerator.getInchi();
							inchikey = inChIGenerator.getInchiKey();
						}
						else{
							System.out.println("InChI failed: " + ret.toString() + " [" + inChIGenerator.getMessage() + "]");
						}
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				updps.setString(1, smiles);
				updps.setString(2, inchi);
				updps.setString(3, inchikey);
				updps.setString(4, accession);
				updps.executeUpdate();

				synps.setString(1, accession);
				synps.setString(2, pepSeq);
				synps.setString(3, "PRI");
				synps.executeUpdate();
				synps.setString(2, oneLetterSeq);
				synps.setString(3, "SYN");
				synps.executeUpdate();
			}
			rs.close();
			ps.close();
			updps.close();
			synps.close();
			CompoundDbConnectionManager.releaseConnection(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getCompoundsFromPubChem() {

		String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
		IteratingSDFReader reader;
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		InputStream pubchemDataStream = null;
		InputStream synonymStream = null;

		String[] idList = new String[] {"11652621","94701","146208","3001386","3270699","12993744"};
		String requestUrl = pubchemCidUrl + StringUtils.join(idList, ",") + "/SDF";
		try {
			pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(pubchemDataStream != null) {
			reader = new IteratingSDFReader(pubchemDataStream, coBuilder);
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				String cid = molecule.getProperty(PubChemFields.PUBCHEM_ID.toString());
				try {
					synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/synonyms/TXT");
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(synonymStream != null) {
					try {
						String[] synonyms = IOUtils.toString(synonymStream).split("\\r?\\n");
						System.out.println("***");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("***");
			}
		}
	}

	private void getNistDataFromNames() {

		String[][]nameArray = null;
		File nameFile = new File("C:\\Users\\Sasha\\Downloads\\hybrid search names.txt");
		try {
			nameArray = DelimitedTextParser.parseTextFileWithEncoding(
					nameFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TreeSet<String>sortedNames = new TreeSet<String>();
		for(int i=0; i<nameArray.length; i++)
			sortedNames.add(nameArray[i][0]);

		ArrayList<String>compoundData = new ArrayList<String>();
		ArrayList<String>line = new ArrayList<String>();
		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			String query =
				"SELECT CAS_NUMBER, RELATED_CAS, EXACT_MASS, FORMULA, "
				+ "INCHI_KEY, SMILES FROM NIST_COMPOUND_DATA WHERE NAME = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ResultSet rs = null;
			for(String name : sortedNames) {

				ps.setString(1, name);
				rs = ps.executeQuery();
				while(rs.next()) {
					line.clear();
					line.add(name);
					line.add(rs.getString("CAS_NUMBER"));
					line.add(rs.getString("RELATED_CAS"));
					line.add(Double.toString(rs.getDouble("EXACT_MASS")));
					line.add(rs.getString("FORMULA"));
					line.add(rs.getString("INCHI_KEY"));
					line.add(rs.getString("SMILES"));
					compoundData.add(StringUtils.join(line, "\t"));
				}
				rs.close();
			}
			rs.close();
			ps.close();
			CompoundDbConnectionManager.releaseConnection(conn);
			File outputFile =
					Paths.get(nameFile.getParent(),
							"NIST_NAMES_OUTPUT.TXT").toFile();
			try {
				final Writer writer = new BufferedWriter(new FileWriter(outputFile));
				writer.append(StringUtils.join(compoundData,  System.getProperty("line.separator")));
				writer.flush();
				writer.close();
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void addBulkIds() {

		Map<String,Collection<String>>accToBulkMap = new TreeMap<String,Collection<String>>();
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			String query =
					"SELECT DISTINCT D.BULK_ABBREV, D.MOL_FORMULA  " +
					"FROM COMPOUND_DATA D " +
					"WHERE D.BULK_ABBREV IS NOT NULL " +
					"ORDER BY 1,2 ";
			PreparedStatement ps = conn.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {

				String bulk = rs.getString("BULK_ABBREV");
				if(!accToBulkMap.containsKey(bulk))
					accToBulkMap.put(bulk, new TreeSet<String>());

				accToBulkMap.get(bulk).add(rs.getString("MOL_FORMULA"));
			}
			rs.close();
			query = "SELECT SMILES FROM COMPOUND_DATA  " +
					"WHERE BULK_ABBREV = ? " +
					"AND MOL_FORMULA = ? " +
					"AND ROWNUM=1 ";
			ps = conn.prepareStatement(query);

			String lipBulk = CompoundDatabaseEnum.LIPIDMAPS_BULK.name();
			String insQuery =
					"INSERT INTO COMPOUND_DATA_TMP (ACCESSION, SOURCE_DB, PRIMARY_NAME, "
					+ "MOL_FORMULA, EXACT_MASS, SMILES, BULK_ABBREV) "
					+ "VALUES (?,?,?,?,?,?,?)";
			PreparedStatement insps = conn.prepareStatement(insQuery);

			for (Entry<String, Collection<String>> entry : accToBulkMap.entrySet()) {

				for(String formula : entry.getValue()) {

					double mass =
						MolecularFormulaManipulator.getMajorIsotopeMass(
							MolecularFormulaManipulator.getMolecularFormula(formula, builder));

					ps.setString(1, entry.getKey());
					ps.setString(2, formula);
					rs = ps.executeQuery();
					while(rs.next()) {
						String bulkAccession = entry.getKey() + "-" + formula;
						insps.setString(1, bulkAccession);
						insps.setString(2, lipBulk);
						insps.setString(3, bulkAccession);
						insps.setString(4, formula);
						insps.setDouble(5, mass);
						insps.setString(6, rs.getString("SMILES"));
						insps.setString(7, entry.getKey());
						insps.executeUpdate();
					}
					rs.close();
				}
			}
			rs.close();
			insps.close();
			ps.close();
			CompoundDbConnectionManager.releaseConnection(conn);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void addT3dbCrossrefs() {

		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			String query =
				"SELECT DISTINCT C.ACCESSION, R.ACCESSION AS SDB_ID " +
				"FROM T3DB_CROSSREF R, " +
				"COMPOUND_DATA_CLEAN C " +
				"WHERE R.SOURCE_DB_ID = C.ACCESSION " +
				"AND R.SOURCE_DB = C.SOURCE_DB ";
			PreparedStatement ps = conn.prepareStatement(query);
			String insQuery =
				"INSERT INTO COMPOUND_CROSSREF_NEW "
				+ "SELECT ?, F.SOURCE_DB, F.SOURCE_DB_ID " +
				"FROM T3DB_CROSSREF F WHERE F.ACCESSION = ?";
			PreparedStatement insps = conn.prepareStatement(insQuery);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {

				insps.setString(1, rs.getString("ACCESSION"));
				insps.setString(2, rs.getString("SDB_ID"));
				insps.addBatch();
			}
			insps.executeBatch();
			insps.close();
			rs.close();
			ps.close();
			CompoundDbConnectionManager.releaseConnection(conn);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void addFooDbCrossrefs() {

		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			String query =
					"SELECT DISTINCT A.BULK_ABBR FROM COMPOUND_ABBREV A ORDER BY 1";
			PreparedStatement ps = conn.prepareStatement(query);
			String insQuery =
				"INSERT INTO COMPOUND_CROSSREF_NEW "
				+ "SELECT ?, F.SOURCE_DB, F.SOURCE_DB_ID " +
				"FROM FOODB_CROSSREF F WHERE F.ACCESSION = ?";
			PreparedStatement insps = conn.prepareStatement(insQuery);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {

				insps.setString(1, rs.getString("ACCESSION"));
				insps.setString(2, rs.getString("SDB_ID"));
				insps.addBatch();
			}
			insps.executeBatch();
			insps.close();
			rs.close();
			ps.close();
			CompoundDbConnectionManager.releaseConnection(conn);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void addSmilesToLib() {

		File nameFile = new File("C:\\Users\\Sasha\\Downloads\\Positive_Garrett_MetaboliteStd_Library_RP_CB_2.txt");
		String[][]libData = DelimitedTextParser.parseTextFile(nameFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
		InputStream pubchemDataStream = null;
		ArrayList<String>lines = new ArrayList<String>();
		ArrayList<String>line = new ArrayList<String>();
		line.addAll(Arrays.asList(libData[0]));
		line.add("SMILES");
		lines.add(StringUtils.join(line, "\t"));
		for(int i=1; i<libData.length; i++) {
			
			line.clear();
			line.addAll(Arrays.asList(libData[i]));
			String pubChemId = libData[i][9];
			String smiles = "";
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + pubChemId + "/property/IsomericSMILES/TXT");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(pubchemDataStream != null) {				
				try {
					smiles = IOUtils.toString(pubchemDataStream, StandardCharsets.UTF_8);
				}
				catch (Exception e) {
					//	e.printStackTrace();
				}
				try {
					pubchemDataStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(smiles == null || smiles.isEmpty()) {			
				try {
					pubchemDataStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + pubChemId + "/property/CanonicalSMILES/TXT");
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(pubchemDataStream != null) {
					
					try {
						smiles = IOUtils.toString(pubchemDataStream, StandardCharsets.UTF_8);
					}
					catch (Exception e) {
						//	e.printStackTrace();
					}
					try {
						pubchemDataStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			line.add(smiles);
			System.out.println(pubChemId + " " + smiles);
			lines.add(StringUtils.join(line, "\t"));
		}
		Path libOutPath = Paths.get("C:\\Users\\Sasha\\Downloads\\Positive_Garrett_MetaboliteStd_Library_RP_CB_SMILES_2.txt");
	    try {
			Files.write(libOutPath, 
					lines, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}


/*
//String toSearch = "UNK_561.1349@4.8963966 + 4.961731";
//String pat = "(\\d+.\\d+)@(\\d+.\\d+) \\+ (\\d+.\\d+)";

String toSearch = "Precursor Tolerance=20";
String pat = "(\\d+.*$)";

Pattern pattern = Pattern.compile(pat);
Matcher matcher = pattern.matcher(toSearch);

boolean found = false;
if (matcher.find()) {

	String fm = matcher.group(1);
//	String frt = matcher.group(2);
//	String cfrt = matcher.group(3);
//	System.out.print(fm + " | " + frt + " | " + " | " + cfrt);

	System.out.print(fm);

	// System.out.println(matcher.group());
	// System.out.println(matcher.start());
	// System.out.println(matcher.end());
	found = true;
}
if (!found) {
	System.out.println("No match found");
}*/
/*
try {
	LIMSUser user = IDTUtils.getUserLogon(uname, password);
	System.out.println(user.getFullName());
} catch (Exception e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}*/
/*		File source =
	new File("E:\\DataAnalysis\\_Reports\\Methods\\HiP-ALS_1_MethodMetaData.xml");
File destination =
	new File("E:\\DataAnalysis\\_Reports\\Methods\\HiP-ALS_1_MethodMetaData.zip");
try {
	CompressionUtils.zipFile(source, destination);
} catch (Exception e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}*/