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

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class XicMethodGenerator {

	private File qualMethodTemplate, methodXmlFile, methodInfoFile, methodSaveDir;
	private MsFeatureCluster cluster;
	private boolean templateValid;
	private boolean clearXics;
	private String massAccuracy;
	private DecimalFormat df;

	public XicMethodGenerator(MsFeatureCluster inputCluster, boolean clearExisting) {

		templateValid = false;
		clearXics = clearExisting;
		cluster = inputCluster;

		df = new DecimalFormat("#.0");
		massAccuracy = df.format(MRC2ToolBoxConfiguration.getXicMassAcuracy());
		qualMethodTemplate = new File(MRC2ToolBoxConfiguration.getXicTemplateFile());
		methodSaveDir = new File(MRC2ToolBoxConfiguration.getQualXicMethodDir());

		if (methodSaveDir == null || !methodSaveDir.exists())
			methodSaveDir = new File(MRC2ToolBoxCore.qualMethodsDir);

		if (qualMethodTemplate != null) {

			if (qualMethodTemplate.exists()) {

				methodXmlFile = new File(qualMethodTemplate.getPath() + File.separator + "DaMethod" + File.separator
						+ "Qual" + File.separator + "qualitative.xml");

				if (methodXmlFile.exists()) {

					templateValid = true;

					methodInfoFile = new File(qualMethodTemplate.getPath() + File.separator + "DaMethod"
							+ File.separator + "Qual" + File.separator + "info.xml");
				}
			}
		}
	}

	//	TODO?
	private Element createIncludeMassRangeElement(double mz, Document methodDom) {

//		Element massRangeElement = methodDom.createElement("Parameter");
//		massRangeElement.setAttribute("id", "IncludeMassRanges");
//		Element mzdndn = methodDom.createElement("DisplayName");
//		mzdndn.appendChild(methodDom.createTextNode("m/z value(s):"));
//		massRangeElement.appendChild(mzdndn);
//		massRangeElement.appendChild(
//				createValueElement("Thompsons", "MassToCharge", "DigitsAfterDecimal", "4", true, methodDom));
//
//		Element valueElement = createValueElement("Thompsons", "MassToCharge", "DigitsAfterDecimal", "4", false,
//				methodDom);
//		String mzString = MRC2ToolBoxConfiguration.getMzFormat().format(mz);
//
//		Element mzRangeElement = createRangeElement(mzString, massAccuracy, "Thompsons", "MassToCharge",
//				"DigitsAfterDecimal", "4", methodDom);
//
//		valueElement.appendChild(mzRangeElement);
//
//		massRangeElement.appendChild(valueElement);
//
//		Element mzLimits = methodDom.createElement("Limits");
//		Element mzLimitsOverall = methodDom.createElement("Limits");
//		mzLimitsOverall.setAttribute("id", "overall");
//		Element mzLimitsOverallMin = methodDom.createElement("Minimum");
//		mzLimitsOverallMin.setAttribute("type", "Inclusive");
//		mzLimitsOverallMin.appendChild(methodDom.createTextNode("0"));
//		mzLimitsOverall.appendChild(mzLimitsOverallMin);
//		mzLimits.appendChild(mzLimitsOverall);
//		Element mzLimitsWidth = methodDom.createElement("Limits");
//		mzLimitsWidth.setAttribute("id", "width");
//		Element mzLimitsWidthMin = methodDom.createElement("Minimum");
//		mzLimitsWidthMin.setAttribute("type", "Inclusive");
//		mzLimitsWidthMin.appendChild(methodDom.createTextNode("0"));
//		mzLimitsWidth.appendChild(mzLimitsWidthMin);
//		mzLimits.appendChild(mzLimitsWidth);
//
//		massRangeElement.appendChild(mzLimits);
//
//		Element mzeConvSupport = methodDom.createElement("ConversionSupport");
//		mzeConvSupport.appendChild(methodDom.createTextNode("PrecisionOnly"));
//		massRangeElement.appendChild(mzeConvSupport);
//
//		return massRangeElement;
		
		return null;
	}

	//	TODO?
	private Element createMzOfInterestElement(Document methodDom) {

//		Element mzOfInterestElement = methodDom.createElement("Parameter");
//		mzOfInterestElement.setAttribute("id", "MzOfInterest");
//		Element mzdndn = methodDom.createElement("DisplayName");
//		mzdndn.appendChild(methodDom.createTextNode("m/z of interest:"));
//		mzOfInterestElement.appendChild(mzdndn);
//		mzOfInterestElement.appendChild(
//				createValueElement("Thompsons", "MassToCharge", "DigitsAfterDecimal", "4", true, methodDom));
//		mzOfInterestElement.appendChild(
//				createValueElement("Thompsons", "MassToCharge", "DigitsAfterDecimal", "4", false, methodDom));
//
//		Element mzLimits = methodDom.createElement("Limits");
//		Element mzLimitsOverall = methodDom.createElement("Limits");
//		mzLimitsOverall.setAttribute("id", "overall");
//		Element mzLimitsOverallMin = methodDom.createElement("Minimum");
//		mzLimitsOverallMin.setAttribute("type", "Inclusive");
//		mzLimitsOverallMin.appendChild(methodDom.createTextNode("0"));
//		mzLimitsOverall.appendChild(mzLimitsOverallMin);
//		mzLimits.appendChild(mzLimitsOverall);
//		Element mzLimitsWidth = methodDom.createElement("Limits");
//		mzLimitsWidth.setAttribute("id", "width");
//		Element mzLimitsWidthMin = methodDom.createElement("Minimum");
//		mzLimitsWidthMin.setAttribute("type", "Inclusive");
//		mzLimitsWidthMin.appendChild(methodDom.createTextNode("0"));
//		mzLimitsWidth.appendChild(mzLimitsWidthMin);
//		mzLimits.appendChild(mzLimitsWidth);
//
//		Element mzeConvSupport = methodDom.createElement("ConversionSupport");
//		mzeConvSupport.appendChild(methodDom.createTextNode("PrecisionOnly"));
//		mzOfInterestElement.appendChild(mzeConvSupport);
//
//		return mzOfInterestElement;
		
		return null;
	}

	private Element createOrdinalNumberElement(Document methodDom) {

//		Element ordinalNumberElement = methodDom.createElement("Parameter");
//		ordinalNumberElement.setAttribute("id", "OrdinalNumber");
//
//		Element dName = methodDom.createElement("DisplayName");
//		dName.appendChild(methodDom.createTextNode("Ordinal number:"));
//		ordinalNumberElement.appendChild(dName);
//
//		Element valueElement = methodDom.createElement("Value");
//		valueElement.appendChild(methodDom.createTextNode("1"));
//		ordinalNumberElement.appendChild(valueElement);
//
//		Element swLimits = methodDom.createElement("Limits");
//		Element swLimitsMin = methodDom.createElement("Minimum");
//		swLimitsMin.setAttribute("type", "Inclusive");
//		swLimitsMin.appendChild(methodDom.createTextNode("1"));
//		swLimits.appendChild(swLimitsMin);
//		Element swLimitsMax = methodDom.createElement("Maximum");
//		swLimitsMax.setAttribute("type", "Inclusive");
//		swLimitsMax.appendChild(methodDom.createTextNode("9"));
//		swLimits.appendChild(swLimitsMax);
//		ordinalNumberElement.appendChild(swLimits);
//
//		Element xuElement = methodDom.createElement("XUnits");
//		xuElement.appendChild(methodDom.createTextNode("Unspecified"));
//		ordinalNumberElement.appendChild(xuElement);
//
//		Element dvtElement = methodDom.createElement("DataValueType");
//		dvtElement.appendChild(methodDom.createTextNode("Unspecified"));
//		ordinalNumberElement.appendChild(dvtElement);
//
//		Element ptElement = methodDom.createElement("PrecisionType");
//		ptElement.appendChild(methodDom.createTextNode("DigitsAfterDecimal"));
//		ordinalNumberElement.appendChild(ptElement);
//
//		Element pdigElement = methodDom.createElement("PrecisionDigits");
//		pdigElement.appendChild(methodDom.createTextNode("0"));
//		ordinalNumberElement.appendChild(pdigElement);
//
//		Element pwConvSupport = methodDom.createElement("ConversionSupport");
//		pwConvSupport.appendChild(methodDom.createTextNode("None"));
//		ordinalNumberElement.appendChild(pwConvSupport);
//
//		return ordinalNumberElement;
		
		return null;
	}

	private Element createParameterElement(String id, String displayName, String value, Document methodDom) {

//		Element parameterElement = methodDom.createElement("Parameter");
//		parameterElement.setAttribute("id", id);
//
//		Element dn = methodDom.createElement("DisplayName");
//		dn.appendChild(methodDom.createTextNode(displayName));
//		parameterElement.appendChild(dn);
//
//		Element val = methodDom.createElement("Value");
//		val.appendChild(methodDom.createTextNode(value));
//		parameterElement.appendChild(val);
//
//		return parameterElement;
		
		return null;
	}

	private Element createRangeElement(String center, String widthPpm, String dataUnit, String dataValueType,
			String precisionType, String precisionDigits, Document methodDom) {

//		Element rangeElement = methodDom.createElement("Range");
//
//		Element centerElement = methodDom.createElement("Center");
//		centerElement.appendChild(methodDom.createTextNode(center));
//		rangeElement.appendChild(centerElement);
//
//		Element widthElement = methodDom.createElement("WidthPpm");
//		widthElement.appendChild(methodDom.createTextNode(widthPpm));
//		rangeElement.appendChild(widthElement);
//
//		Element duElement = methodDom.createElement("DataUnit");
//		duElement.appendChild(methodDom.createTextNode(dataUnit));
//		rangeElement.appendChild(duElement);
//
//		Element dvtElement = methodDom.createElement("DataValueType");
//		dvtElement.appendChild(methodDom.createTextNode(dataValueType));
//		rangeElement.appendChild(dvtElement);
//
//		Element ptElement = methodDom.createElement("PrecisionType");
//		ptElement.appendChild(methodDom.createTextNode(precisionType));
//		rangeElement.appendChild(ptElement);
//
//		Element pdigElement = methodDom.createElement("PrecisionDigits");
//		pdigElement.appendChild(methodDom.createTextNode(precisionDigits));
//		rangeElement.appendChild(pdigElement);
//
//		return rangeElement;
		
		return null;
	}

	private Element createScanRangeElement(Document methodDom) {

//		Element scanRangeElement = methodDom.createElement("Parameter");
//		scanRangeElement.setAttribute("id", "ScanRange");
//
//		Element dName = methodDom.createElement("DisplayName");
//		dName.appendChild(methodDom.createTextNode("Scan range:"));
//		scanRangeElement.appendChild(dName);
//
//		Element ssnLimits = methodDom.createElement("Limits");
//		Element ssnLimitsFirst = methodDom.createElement("Limits");
//		ssnLimitsFirst.setAttribute("id", "first");
//		ssnLimits.appendChild(ssnLimitsFirst);
//		Element ssnLimitsSecond = methodDom.createElement("Limits");
//		ssnLimitsSecond.setAttribute("id", "second");
//		ssnLimits.appendChild(ssnLimitsSecond);
//		scanRangeElement.appendChild(ssnLimits);
//
//		Element pwConvSupport = methodDom.createElement("ConversionSupport");
//		pwConvSupport.appendChild(methodDom.createTextNode("None"));
//		scanRangeElement.appendChild(pwConvSupport);
//
//		return scanRangeElement;
		
		return null;
	}

	private Element createScanSegmentNumberElement(Document methodDom) {

//		Element scanSegmentNumberElement = methodDom.createElement("Parameter");
//		scanSegmentNumberElement.setAttribute("id", "ScanSegmentNumber");
//		Element ssndn = methodDom.createElement("DisplayName");
//		ssndn.appendChild(methodDom.createTextNode("Scan segment:"));
//		scanSegmentNumberElement.appendChild(ssndn);
//		Element ssnLimits = methodDom.createElement("Limits");
//		Element ssnLimitsFirst = methodDom.createElement("Limits");
//		ssnLimitsFirst.setAttribute("id", "first");
//		ssnLimits.appendChild(ssnLimitsFirst);
//		Element ssnLimitsSecond = methodDom.createElement("Limits");
//		ssnLimitsSecond.setAttribute("id", "second");
//		ssnLimits.appendChild(ssnLimitsSecond);
//		scanSegmentNumberElement.appendChild(ssnLimits);
//		Element ssnConvSupport = methodDom.createElement("ConversionSupport");
//		ssnConvSupport.appendChild(methodDom.createTextNode("None"));
//		scanSegmentNumberElement.appendChild(ssnConvSupport);
//
//		return scanSegmentNumberElement;
		
		return null;
	}

	private Element createSinglePpmWidthElement(Document methodDom) {

//		Element singlePpmWidthElement = methodDom.createElement("Parameter");
//		singlePpmWidthElement.setAttribute("id", "SinglePpmWidth");
//
//		Element dName = methodDom.createElement("DisplayName");
//		dName.appendChild(methodDom.createTextNode("Single ppm width:"));
//		singlePpmWidthElement.appendChild(dName);
//
//		String swValue = df.format(MRC2ToolBoxConfiguration.getXicMassAcuracy() / 2);
//
//		Element valueElement = methodDom.createElement("Value");
//		valueElement.appendChild(methodDom.createTextNode(swValue));
//		singlePpmWidthElement.appendChild(valueElement);
//
//		Element swLimits = methodDom.createElement("Limits");
//		Element swLimitsMin = methodDom.createElement("Minimum");
//		swLimitsMin.setAttribute("type", "Inclusive");
//		swLimitsMin.appendChild(methodDom.createTextNode("0"));
//		swLimits.appendChild(swLimitsMin);
//		Element swLimitsMax = methodDom.createElement("Maximum");
//		swLimitsMax.setAttribute("type", "Inclusive");
//		swLimitsMax.appendChild(methodDom.createTextNode("1000"));
//		swLimits.appendChild(swLimitsMax);
//		singlePpmWidthElement.appendChild(swLimits);
//
//		Element xuElement = methodDom.createElement("XUnits");
//		xuElement.appendChild(methodDom.createTextNode("Unspecified"));
//		singlePpmWidthElement.appendChild(xuElement);
//
//		Element dvtElement = methodDom.createElement("DataValueType");
//		dvtElement.appendChild(methodDom.createTextNode("Unspecified"));
//		singlePpmWidthElement.appendChild(dvtElement);
//
//		Element ptElement = methodDom.createElement("PrecisionType");
//		ptElement.appendChild(methodDom.createTextNode("DigitsAfterDecimal"));
//		singlePpmWidthElement.appendChild(ptElement);
//
//		Element pdigElement = methodDom.createElement("PrecisionDigits");
//		pdigElement.appendChild(methodDom.createTextNode("1"));
//		singlePpmWidthElement.appendChild(pdigElement);
//
//		Element pwConvSupport = methodDom.createElement("ConversionSupport");
//		pwConvSupport.appendChild(methodDom.createTextNode("None"));
//		singlePpmWidthElement.appendChild(pwConvSupport);
//
//		return singlePpmWidthElement;
		
		return null;
	}

	private Element createValueElement(String dataUnit, String dataValueType, String precisionType,
			String precisionDigits, boolean isDefault, Document methodDom) {

//		String name = "Value";
//
//		if (isDefault)
//			name = "DefaultValue";
//
//		Element valueElement = methodDom.createElement(name);
//
//		Element duElement = methodDom.createElement("DataUnit");
//		duElement.appendChild(methodDom.createTextNode(dataUnit));
//		valueElement.appendChild(duElement);
//
//		Element dvtElement = methodDom.createElement("DataValueType");
//		dvtElement.appendChild(methodDom.createTextNode(dataValueType));
//		valueElement.appendChild(dvtElement);
//
//		Element ptElement = methodDom.createElement("PrecisionType");
//		ptElement.appendChild(methodDom.createTextNode(precisionType));
//		valueElement.appendChild(ptElement);
//
//		Element pdigElement = methodDom.createElement("PrecisionDigits");
//		pdigElement.appendChild(methodDom.createTextNode(precisionDigits));
//		valueElement.appendChild(pdigElement);
//
//		return valueElement;
		
		return null;
	}

	private Element createXicElement(MsFeature cf, Document methodDom) {

//		Element xicElement = methodDom.createElement("ParameterSet");
//		xicElement.setAttribute("usagekey", "1");
//		xicElement.setAttribute("displayName", "");
//		xicElement.setAttribute("assembly", "");
//		xicElement.setAttribute("class", "Agilent.MassSpectrometry.DataAnalysis.PSetExtractChrom");
//
//		Element xicParams = methodDom.createElement("Parameters");
//		xicElement.appendChild(xicParams);
//
//		String polarity = "Positive";
//
//		if (cf.getPolarity().equals(Polarity.Negative))
//			polarity = "Negative";
//
//		xicParams.appendChild(createParameterElement("ChromatogramType", "Type:", "ExtractedIon", methodDom));
//		xicParams.appendChild(createParameterElement("Integrate", "Integrate when extracted", "true", methodDom));
//		xicParams.appendChild(createParameterElement("MSLevel", "MS level:", "MS", methodDom));
//		xicParams.appendChild(createParameterElement("MSScanType", "Scans:", "AllMS", methodDom));
//		xicParams.appendChild(createParameterElement("IonPolarity", "Polarity:", polarity, methodDom));
//
//		// MZ of interest
//		xicParams.appendChild(createMzOfInterestElement(methodDom));
//
//		// Scan segment number
//		xicParams.appendChild(createScanSegmentNumberElement(methodDom));
//
//		// Include mass ranges
//		xicParams.appendChild(createIncludeMassRangeElement(cf.getMonoisotopicMz(), methodDom));
//
//		xicParams.appendChild(createParameterElement("SingleChromatogram",
//				"Merge multiple masses into one chromatogram", "false", methodDom));
//		xicParams.appendChild(createParameterElement("DoCycleSum", "Do cycle sum", "true", methodDom));
//		xicParams.appendChild(createParameterElement("IonizationMode", "Ionization:", "Unspecified", methodDom));
//		xicParams.appendChild(createParameterElement("SingleMzExpansionMode", "Single Mz expansion mode:",
//				"SymmetricPpm", methodDom));
//
//		xicParams.appendChild(createSinglePpmWidthElement(methodDom));
//
//		xicParams.appendChild(createScanRangeElement(methodDom));
//
//		xicParams.appendChild(createParameterElement("ExtractOneChromPerScanSeg", "Extract one chrom per scan seg:",
//				"false", methodDom));
//		xicParams.appendChild(createParameterElement("YValueInCounts", "Y value in counts:", "true", methodDom));
//
//		xicParams.appendChild(createOrdinalNumberElement(methodDom));
//
//		return xicElement;
		
		return null;
	}

	public String createXicMethod() {

//		String newMethodName = null;
//		int xicCount;
//
//		if (!templateValid)
//			return null;
//
//		try {
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			Document methodDom = dBuilder.parse(methodXmlFile);
//
//			XPathFactory factory = XPathFactory.newInstance();
//			XPath xpath = factory.newXPath();
//
//			XPathExpression expr = xpath
//					.compile("//Method/Method/ParameterSet[@usagekey=\"ChromatogramDefinitions\"]/Parameters");
//
//			NodeList nl = (NodeList) expr.evaluate(methodDom, XPathConstants.NODESET);
//			Node xicParams = nl.item(0);
//
//			// Clear existing XICs
//			if (xicParams.hasChildNodes()) {
//
//				if (clearXics) {
//
//					while (xicParams.hasChildNodes())
//						xicParams.removeChild(xicParams.getFirstChild());
//
//					xicCount = 0;
//				} else {
//					xicCount = xicParams.getChildNodes().getLength();
//				}
//			} else {
//				xicCount = 0;
//			}
//			for (MsFeature cf : cluster.getFeatures()) {
//
//				xicCount++;
//
//				// Order index element
//				Element orderIndex = methodDom.createElement("OrderIndex");
//				orderIndex.appendChild(methodDom.createTextNode(Integer.toString(xicCount)));
//				xicParams.appendChild(orderIndex);
//
//				// XIC element
//				Element xicElement = createXicElement(cf, methodDom);
//				xicParams.appendChild(xicElement);
//			}
//			File outputDir = new File(methodSaveDir.getPath() + File.separator + cluster.getNameForXicMethod() + ".m");
//			saveMethod(methodDom, outputDir);
//		} catch (Exception e) {
//
//			e.printStackTrace();
//		}
//		return newMethodName;
		
		return null;
	}

	private void saveMethod(Document methodDom, File outputDir) throws TransformerException, IOException {

//		TransformerFactory transfac = TransformerFactory.newInstance();
//		Transformer transformer = transfac.newTransformer();
//		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//
//		String newMethofPath = outputDir + File.separator + "DaMethod" + File.separator + "Qual";
//		FileUtils.forceMkdir(new File(newMethofPath));
//
//		// Copy info file
//		FileUtils.copyFile(methodInfoFile, new File(newMethofPath + File.separator + "info.xml"));
//
//		StreamResult result = new StreamResult(
//				new FileOutputStream(newMethofPath + File.separator + "qualitative.xml"));
//		DOMSource source = new DOMSource(methodDom);
//		transformer.transform(source, result);
//		result.getOutputStream().close();
	}
}
