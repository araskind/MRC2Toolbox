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

package edu.umich.med.mrc2.datoolbox.data.enums;

//	TODO move this to database
public enum CompoundDatabaseEnum {

	ACTOR("Actor","", ""),
	AGRICOLA("Agricola", "http://europepmc.org/abstract/AGR/", ""),
	ALDRICH("Aldrich", "https://www.sigmaaldrich.com/catalog/product/aldrich/", "?lang=en&region=US"),
	ARRAYEXPRESS("ArrayExpress","https://www.ebi.ac.uk/arrayexpress/experiments/", "/"),
	BEILSTEIN("Beilstein Registry","", ""),
	BIGG("BiGG","", ""),
	BINDINGDB("BindingDB","", ""),
	BIOCYC("BioCyc","https://biocyc.org/compound?orgid=META&id=", ""),
	BIOMODELS("BioModels","http://www.ebi.ac.uk/biomodels-main/", ""),
	BPDB("Bio-Pesticides DataBase","https://sitem.herts.ac.uk/aeru/bpdb/Reports/", ".htm"),
	BRENDA("BRaunschweig ENzyme DAtabase, enzymes","https://www.brenda-enzymes.info/enzyme.php?ecno=", ""),
	BRENDA_LIGAND("BRaunschweig ENzyme DAtabase, ligands","https://www.brenda-enzymes.org/ligand.php?brenda_ligand_id=", ""),
	CAROTENOIDS_DB("Carotenoids Database","http://carotenoiddb.jp/Entries/", ".html"),
	CAS("CAS","", ""),
	CHEBI("ChEBI","https://www.ebi.ac.uk/chebi/searchId.do?chebiId=", ""),
	CHEBI_SECONDARY("ChEBI secondary ID","https://www.ebi.ac.uk/chebi/searchId.do?chebiId=", ""),
	CHEMBL("ChEMBL","https://www.ebi.ac.uk/chembl/compound/inspect/", ""),
	CHEMSPIDER("ChemSpider","http://www.chemspider.com/Chemical-Structure.", ".html"),
	CHINESE_ABSTRACTS("Chinese Abstracts", "http://europepmc.org/abstract/CBA/", ""),
	CIL("Cambridge Isotope", "https://shop.isotope.com/productdetails.aspx?itemno=", ""),
	CITEXPLORE("CiteXplore", "http://europepmc.org/abstract/CTX/", ""),
	COMPTOX("CompTox Chemicals Dashboard","https://comptox.epa.gov/dashboard/chemical/details/",""),
	CTD("CTD","", ""),
	CUSTOM("Custom-undefined", "", ""),
	DFC("DFC","", ""),
	DPD("Drugs Product Database","", ""),
	DRUGBANK("DrugBank","https://go.drugbank.com/drugs/", ""),
	DRUGBANK_SECONDARY("DrugBank","https://go.drugbank.com/drugs/", ""),
	DRUGCENTRAL("DrugCentral","https://drugcentral.org/drugcard/",""),
	DRUGBANKMET("DrugBankMet","", ""),
	DUKE("Duke","", ""),
	EAFUS("EAFUS","", ""),
	ECMDB("EAFUS","http://ecmdb.ca/compounds/", ""),
	FDA_UNII("FDA UNII", "https://precision.fda.gov/uniisearch/srs/unii/", ""),
	FLAVORNET("FlavorNet","http://www.flavornet.org/info/", ""),
	FOODB("FoodDB","http://foodb.ca/compounds/", ""),
	GENATLAS("GenAtlas","", ""),
	GENBANK("GenBank","", ""),
	GENBANKPROTEIN("GenBank Protein","", ""),
	GENEONTOLOGY("Gene Ontology and GO Annotations", "https://www.ebi.ac.uk/QuickGO/term/", ""),
	GLYGEN("GlyGen", "https://www.glygen.org/glycan/", ""),
	GLYTOUCAN("GlyTouCan", "https://glytoucan.org/Structures/Glycans/", ""),
	GMELIN("Gmelin Registry", "", ""),
	GNPS_SPECTRA("GNPS spectra library","http://gnps.ucsd.edu/ProteoSAFe/gnpslibraryspectrum.jsp?SpectrumID=",""),
	GOLM("Golm Metabolome Database", "http://gmd.mpimp-golm.mpg.de/Spectrums/", ""),
	GOODSCENT("GoodScent","", ""),
	GUIDE2PHARMACOLOGY("Guide to Pharmacology","", ""),
	HET("HET","", ""),
	HGNC("HUGO Gene Nomenclature Committee","", ""),
	HMDB("HMDB","http://www.hmdb.ca/metabolites/", ""),
	HMDB_SECONDARY("HMDB secondary ID","http://www.hmdb.ca/metabolites/", ""),
	IEDB("Immune Epitope Database", "http://www.iedb.org/epitope/", ""),
	INTACT("IntAct","https://www.ebi.ac.uk/intact/interaction/",""),
	INTENZ("IntEnz","https://www.ebi.ac.uk/intenz/query?cmd=SearchEC&ec=",""), //	TODO Strip 'EC ' from enzyme ID
	ISDB("ISDB","",""),
	IUPHAR("IUPHAR","", ""),
	KEGG("KEGG Compound","https://www.genome.jp/dbget-bin/www_bget?", ""),
	KEGGDRUG("KEGG Drug","https://www.genome.jp/dbget-bin/www_bget?", ""),
	KEGG_GLYCAN("KEGG Glycan","https://www.genome.jp/dbget-bin/www_bget?", ""),
	KNAPSACK("KNApSAcK","http://kanaya.naist.jp/knapsack_jsp/information.jsp?word=", ""),
	LINCS("LINCS", "http://identifiers.org/lincs.smallmolecule/", ""),
	LIPIDBANK("LipidBank","http://lipidbank.jp/cgi-bin/detail.cgi?id=", ""),
	LIPIDMAPS("LipidMaps","http://www.lipidmaps.org/data/LMSDRecord.php?LMID=", ""),
	LIPIDMAPS_BULK("LipidMapsBulk","http://www.lipidmaps.org/data/structure/LMSDSearch.php?Mode=ProcessTextSearch&LMID=", ""),
	MASS_BANK("MassBank","https://massbank.eu/MassBank/jsp/RecordDisplay.jsp?id=", ""),
	METABOLIGHTS("", "https://www.ebi.ac.uk/metabolights/", ""),
	METABOLOMICS("Metabolomics","", ""),
	META_CYC("MetaCyc","https://metacyc.org/compound?orgid=META&id=", ""),
	METAGENE("MetaGene","", ""),
	METLIN("METLIN","https://metlin.scripps.edu/metabo_info.php?molid=", ""),
	MIBIG("Minimum Information about a Biosynthetic Gene cluster (MIBiG)","https://mibig.secondarymetabolites.org/repository/",""),
	MOLDB("MolDB","", ""),
	MONA("MONA","http://mona.fiehnlab.ucdavis.edu/spectra/display/", ""),
	MOTRPAC("MoTrPAC","",""),
	MSDIAL_LIPIDS("MS-DIAL LipidBlast","",""),		
	NATURAL_PRODUCTS_ATLAS("The Natural Products Atlas","https://www.npatlas.org/explore/compounds/", ""),
	NIST_MS("NIST MS","", ""),
	NIST_MS_PEP("NIST MSMS2","", ""),
	NUGOWIKI("NuGOwiki","", ""),
	OMIM("OMIM","", ""),
	PATENT("Patent", "http://v3.espacenet.com/textdoc?DB=EPODOC&IDX=", ""),
	PEP_BANK("PepBank","", ""),
	PDB("PDB","", ""),
	PDBECHEM("PDBeChem", "http://www.ebi.ac.uk/pdbe-srv/pdbechem/chemicalCompound/show/", ""),
	PHARMGKB("PharmGKB","", ""),
	PHEXCPD("PhExCpd","", ""),
	PHEXMET("PhExMet","", ""),
	PLANTFA_ID("PlantFAdb","https://plantfadb.org/fatty_acids/", ""),
	PPR("Europe PMC Preprints", "https://bioregistry.io/reference/ppr:", ""),
	PUBCHEM("PubChem","https://pubchem.ncbi.nlm.nih.gov/compound/", ""),
	PUBCHEMSUBSTANCE("PubChem Substance","https://pubchem.ncbi.nlm.nih.gov/substance/", ""),
	PUBMED("PubMed", "https://www.ncbi.nlm.nih.gov/pubmed/", ""),
	PUBMED_CENTRAL("PubMed Central", "https://www.ncbi.nlm.nih.gov/pmc/articles/", "/"),
	REACTOME("Reactome", "https://reactome.org/content/detail/", ""),
	REFMET("RefMet", "https://www.metabolomicsworkbench.org/databases/refmet/refmet_details.php?REFMET_NAME=", ""),
	RESID("RESID", "http://pir0.georgetown.edu/cgi-bin/resid?id=", ""),
	RHEA("Rhea", "https://www.rhea-db.org/reaction?id=", ""),
	SABIO_RK("SABIO-RK Database Links", "http://sabio.h-its.org/reacdetails.jsp?reactid=", ""),
	STITCH("STITCH","", ""),
	SUPERSCENT("SuperScent","", ""),
	SWISS_LIPIDS("Swiss-Lipids","http://www.swisslipids.org/#/entity/SLM:", "/"),
	SWISS_PROT("Swiss-Prot","", ""),
	T3DB("T3DB","http://www.t3db.ca/toxins/", ""),
	TTD("Therapeutic Targets Database","", ""),
	UM_BBD("UM-BBD", "http://eawag-bbd.ethz.ch/servlets/pageservlet?ptype=c&compID=", ""),
	UNII("UNII","https://fdasis.nlm.nih.gov/srs/unii/", ""),
	UNIPROT("UniProt","http://www.uniprot.org/uniprot/", ""),
	UNIPROTKB("UniProtKB","", ""),
	WIKI("Wiki","https://en.wikipedia.org/wiki/", ""),
	YMDB("YMDB", "http://www.ymdb.ca/compounds/", ""),
	MRC2_MSMS("MRC2 MSMS compound data","", ""),
	;

	private final String name;
	private final String dbLinkPrefix;
	private final String dbLinkSuffix;

	CompoundDatabaseEnum(String name, String dbLinkPrefix, String dbLinkSuffix) {

		this.name = name;
		this.dbLinkPrefix = dbLinkPrefix;
		this.dbLinkSuffix = dbLinkSuffix;
	}

	public String getDbLinkPrefix() {

		return dbLinkPrefix;
	}

	public String getDbLinkSuffix() {

		return dbLinkSuffix;
	}

	public String getName() {

		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static CompoundDatabaseEnum getCompoundDatabaseByName(String name) {

		for(CompoundDatabaseEnum db : CompoundDatabaseEnum.values()) {

			if(db.name().equals(name))
				return db;
		}
		return null;
	}
}
















