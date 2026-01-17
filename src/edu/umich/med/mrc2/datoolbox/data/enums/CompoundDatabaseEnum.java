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

package edu.umich.med.mrc2.datoolbox.data.enums;

//	TODO move this to database
public enum CompoundDatabaseEnum {

	ACTOR("Actor","", "", null),
	AGRICOLA("Agricola", "http://europepmc.org/abstract/AGR/", "", null),
	ALDRICH("Aldrich", "https://www.sigmaaldrich.com/catalog/product/aldrich/", "?lang=en&region=US", null),
	ARRAYEXPRESS("ArrayExpress","https://www.ebi.ac.uk/arrayexpress/experiments/", "/", null),
	BEILSTEIN("Beilstein Registry","", "", null),
	BIGG("BiGG","", "", null),
	BINDINGDB("BindingDB","", "", null),
	BIOCYC("BioCyc","https://biocyc.org/compound?orgid=META&id=", "", null),
	BIOMODELS("BioModels","http://www.ebi.ac.uk/biomodels-main/", "", null),
	BPDB("Bio-Pesticides DataBase","https://sitem.herts.ac.uk/aeru/bpdb/Reports/", ".htm", null),
	BRENDA("BRaunschweig ENzyme DAtabase, enzymes","https://www.brenda-enzymes.info/enzyme.php?ecno=", "", null),
	BRENDA_LIGAND("BRaunschweig ENzyme DAtabase, ligands","https://www.brenda-enzymes.org/ligand.php?brenda_ligand_id=", "", null),
	CAROTENOIDS_DB("Carotenoids Database","http://carotenoiddb.jp/Entries/", ".html", null),
	CAS("CAS","", "", null),
	CHEBI("ChEBI","https://www.ebi.ac.uk/chebi/searchId.do?chebiId=", "", 5),
	CHEBI_SECONDARY("ChEBI secondary ID","https://www.ebi.ac.uk/chebi/searchId.do?chebiId=", "", null),
	CHEMBL("ChEMBL","https://www.ebi.ac.uk/chembl/compound/inspect/", "", null),
	CHEMSPIDER("ChemSpider","http://www.chemspider.com/Chemical-Structure.", ".html", 11),
	CHINESE_ABSTRACTS("Chinese Abstracts", "http://europepmc.org/abstract/CBA/", "", null),
	CIL("Cambridge Isotope", "https://shop.isotope.com/productdetails.aspx?itemno=", "", null),
	CITEXPLORE("CiteXplore", "http://europepmc.org/abstract/CTX/", "", null),
	COCONUT("COlleCtion of Open Natural ProdUcTs","https://coconut.naturalproducts.net/compound/coconut_id/","", null),
	COMPTOX("CompTox Chemicals Dashboard","https://comptox.epa.gov/dashboard/chemical/details/","", null),
	CTD("CTD","", "", null),
	CUSTOM("Custom-undefined", "", "", null),
	DFC("DFC","", "", null),
	DPD("Drugs Product Database","", "", null),
	DRUGBANK("DrugBank","https://go.drugbank.com/drugs/", "", 7),
	DRUGBANK_SECONDARY("DrugBank","https://go.drugbank.com/drugs/", "", null),
	DRUGCENTRAL("DrugCentral","https://drugcentral.org/drugcard/","", null),
	DRUGBANKMET("DrugBankMet","", "", null),
	DUKE("Duke","", "", null),
	EAFUS("EAFUS","", "", null),
	ECMDB("EAFUS","http://ecmdb.ca/compounds/", "", null),
	FDA_UNII("FDA UNII", "https://precision.fda.gov/uniisearch/srs/unii/", "", null),
	FLAVORNET("FlavorNet","http://www.flavornet.org/info/", "", null),
	FOODB("FoodDB","http://foodb.ca/compounds/", "", 8),
	GENATLAS("GenAtlas","", "", null),
	GENBANK("GenBank","", "", null),
	GENBANKPROTEIN("GenBank Protein","", "", null),
	GENEONTOLOGY("Gene Ontology and GO Annotations", "https://www.ebi.ac.uk/QuickGO/term/", "", null),
	GLYGEN("GlyGen", "https://www.glygen.org/glycan/", "", null),
	GLYTOUCAN("GlyTouCan", "https://glytoucan.org/Structures/Glycans/", "", null),
	GMELIN("Gmelin Registry", "", "", null),
	GNPS_SPECTRA("GNPS spectra library","http://gnps.ucsd.edu/ProteoSAFe/gnpslibraryspectrum.jsp?SpectrumID=","", null),
	GOLM("Golm Metabolome Database", "http://gmd.mpimp-golm.mpg.de/Spectrums/", "", null),
	GOODSCENT("GoodScent","", "", null),
	GUIDE2PHARMACOLOGY("Guide to Pharmacology","", "", null),
	HET("HET","", "", null),
	HGNC("HUGO Gene Nomenclature Committee","", "", null),
	HMDB("HMDB","http://www.hmdb.ca/metabolites/", "", 2),
	HMDB_SECONDARY("HMDB secondary ID","http://www.hmdb.ca/metabolites/", "", null),
	IEDB("Immune Epitope Database", "http://www.iedb.org/epitope/", "", null),
	INTACT("IntAct","https://www.ebi.ac.uk/intact/interaction/","", null),
	INTENZ("IntEnz","https://www.ebi.ac.uk/intenz/query?cmd=SearchEC&ec=","", null), //	TODO Strip 'EC ' from enzyme ID
	ISDB("ISDB","","", null),
	IUPHAR("IUPHAR","", "", null),
	KEGG("KEGG Compound","https://www.genome.jp/dbget-bin/www_bget?", "", 1),
	KEGGDRUG("KEGG Drug","https://www.genome.jp/dbget-bin/www_bget?", "", 6),
	KEGG_GLYCAN("KEGG Glycan","https://www.genome.jp/dbget-bin/www_bget?", "", null),
	KNAPSACK("KNApSAcK","http://kanaya.naist.jp/knapsack_jsp/information.jsp?word=", "", null),
	LINCS("LINCS", "http://identifiers.org/lincs.smallmolecule/", "", null),
	LIPIDBANK("LipidBank","http://lipidbank.jp/cgi-bin/detail.cgi?id=", "", null),
	LIPIDMAPS("LipidMaps","http://www.lipidmaps.org/data/LMSDRecord.php?LMID=", "", 3),
	LIPIDMAPS_BULK("LipidMapsBulk","http://www.lipidmaps.org/data/structure/LMSDSearch.php?Mode=ProcessTextSearch&LMID=", "", 4),
	MASS_BANK("MassBank","https://massbank.eu/MassBank/jsp/RecordDisplay.jsp?id=", "", null),
	METABOLIGHTS("", "https://www.ebi.ac.uk/metabolights/", "", null),
	METABOLOMICS("Metabolomics","", "", null),
	META_CYC("MetaCyc","https://metacyc.org/compound?orgid=META&id=", "", null),
	METAGENE("MetaGene","", "", null),
	METLIN("METLIN","https://metlin.scripps.edu/metabo_info.php?molid=", "", null),
	MIBIG("Minimum Information about a Biosynthetic Gene cluster (MIBiG)","https://mibig.secondarymetabolites.org/repository/","", null),
	MOLDB("MolDB","", "", null),
	MONA("MONA","http://mona.fiehnlab.ucdavis.edu/spectra/display/", "", null),
	MOTRPAC("MoTrPAC","","", null),
	MSDIAL_LIPIDS("MS-DIAL LipidBlast","","", null),	
	MSDIAL_METABOLITES("MS-DIAL Metabolites","","", null),
	NATURAL_PRODUCTS_ATLAS("The Natural Products Atlas","https://www.npatlas.org/explore/compounds/", "", null),
	NIST_MS("NIST MS","", "", null),
	NIST_MS_PEP("NIST MSMS2","", "", null),
	NUGOWIKI("NuGOwiki","", "", null),
	OMIM("OMIM","", "", null),
	PATENT("Patent", "http://v3.espacenet.com/textdoc?DB=EPODOC&IDX=", "", null),
	PEP_BANK("PepBank","", "", null),
	PDB("PDB","", "", null),
	PDBECHEM("PDBeChem", "http://www.ebi.ac.uk/pdbe-srv/pdbechem/chemicalCompound/show/", "", null),
	PHARMGKB("PharmGKB","", "", null),
	PHEXCPD("PhExCpd","", "", null),
	PHEXMET("PhExMet","", "", null),
	PLANTFA_ID("PlantFAdb","https://plantfadb.org/fatty_acids/", "", null),
	PPR("Europe PMC Preprints", "https://bioregistry.io/reference/ppr:", "", null),
	PUBCHEM("PubChem","https://pubchem.ncbi.nlm.nih.gov/compound/", "", 10),
	PUBCHEMSUBSTANCE("PubChem Substance","https://pubchem.ncbi.nlm.nih.gov/substance/", "", null),
	PUBMED("PubMed", "https://www.ncbi.nlm.nih.gov/pubmed/", "", null),
	PUBMED_CENTRAL("PubMed Central", "https://www.ncbi.nlm.nih.gov/pmc/articles/", "/", null),
	REACTOME("Reactome", "https://reactome.org/content/detail/", "", null),
	REFMET("RefMet", "https://www.metabolomicsworkbench.org/databases/refmet/refmet_details.php?REFMET_ID=", "", null),
	RESID("RESID", "http://pir0.georgetown.edu/cgi-bin/resid?id=", "", null),
	RHEA("Rhea", "https://www.rhea-db.org/reaction?id=", "", null),
	SABIO_RK("SABIO-RK Database Links", "http://sabio.h-its.org/reacdetails.jsp?reactid=", "", null),
	STITCH("STITCH","", "", null),
	SUPERSCENT("SuperScent","", "", null),
	SWISS_LIPIDS("Swiss-Lipids","http://www.swisslipids.org/#/entity/SLM:", "/", null),
	SWISS_PROT("Swiss-Prot","", "", null),
	T3DB("T3DB","http://www.t3db.ca/toxins/", "", 9),
	TTD("Therapeutic Targets Database","", "", null),
	UM_BBD("UM-BBD", "http://eawag-bbd.ethz.ch/servlets/pageservlet?ptype=c&compID=", "", null),
	UNII("UNII","https://fdasis.nlm.nih.gov/srs/unii/", "", null),
	UNIPROT("UniProt","http://www.uniprot.org/uniprot/", "", null),
	UNIPROTKB("UniProtKB","", "", null),
	WIKI("Wiki","https://en.wikipedia.org/wiki/", "", null),
	YMDB("YMDB", "http://www.ymdb.ca/compounds/", "", null),
	MRC2_MSMS("MRC2 MSMS compound data","", "", null),
	;

	private final String uiName;
	private final String dbLinkPrefix;
	private final String dbLinkSuffix;
	private final Integer rank;

	CompoundDatabaseEnum(
			String uiName, 
			String dbLinkPrefix, 
			String dbLinkSuffix,
			Integer rank) {

		this.uiName = uiName;
		this.dbLinkPrefix = dbLinkPrefix;
		this.dbLinkSuffix = dbLinkSuffix;
		this.rank = rank;
	}

	public String getDbLinkPrefix() {
		return dbLinkPrefix;
	}

	public String getDbLinkSuffix() {
		return dbLinkSuffix;
	}

	public String getName() {
		return uiName;
	}

	public Integer getRank() {
		return rank;
	}	
	
	@Override
	public String toString() {
		return uiName;
	}
	
	public static CompoundDatabaseEnum getCompoundDatabaseByName(String name) {

		for(CompoundDatabaseEnum db : CompoundDatabaseEnum.values()) {

			if(db.name().equals(name))
				return db;
		}
		return null;
	}
}
















