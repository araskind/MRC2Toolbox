# MetIDTracker

Large-scale mass-spectrometry based metabolomics experiments became a common place in recent years. Data analysis for such experiments is a complex multi-step process which presents a number of challenges in quality control, within and between-batch normalization, proper alignment of mass-spectral features between the samples, dealing with missing data, identification of unknowns, removal of redundant signals, data integration from multiple assays, etc. It requires the use of multiple software tools, often with heavy emphasis on custom scripts and complex pipelines. The goal of this project is to put an intuitive user interface on a number of the common tasks in metabolomics data analysis in order to better organize and streamline the procedure.

##	Downloads
###	Software & database
- [MetIDTracker software](https://umich.box.com/s/xsceug1xqx1iggio3u6nu9w43isecv7w){:target="_blank" rel="noopener"}
- [PostgreSQL database dump](https://umich.box.com/s/52sjf3yu8rwmg2ieaqnmk23d4em2ohji)

###	Documentation

- [Database installation and configuration manual](https://umich.box.com/s/h46kuhiw373lo9s3xrao095wee091u57)
- [MetIDTracker user manual, preliminary draft](https://umich.box.com/s/3kp6j2dzt6vfyyelr5a95awvnz7egf1v)

###	MSMS libraries
Several open access MSMS libraries from [MS-DIAL](http://prime.psc.riken.jp/compms/msdial/main.html) were reformatted to include internal MetIDTracker spectrum identifiers and converted to NIST format. They may be used to search experimental data from MetIDTracker. 

- [MS-DIAL metabolomics MSP spectral kit, VS15, positive mode](https://umich.box.com/s/4p6usq555ab5f46l4bgheigamlbxvj95)
- [MS-DIAL metabolomics MSP spectral kit, VS15, negative mode](https://umich.box.com/s/2drqabukveaog1ers89qcu3qx9uocfie)
- [MS-DIAL LipidBlast, V68, positive mode](https://umich.box.com/s/dat5kj9xafg6z4qw4vnju9v1z01drsg9)
- [MS-DIAL LipidBlast, V68, negative mode](https://umich.box.com/s/gm4yf920uad53wpqv1gvsk82epxd2t4w)

###	Third party software
For a number of operations MetIDTracker relies on third party software. This software needs to be installed on the same computer and the location of each external program has to specified in the preferences.
-	[NIST MSPepSearch](https://chemdata.nist.gov/dokuwiki/doku.php?id=peptidew:mspepsearch). Please use “Current release (02/22/2019)”; the software is necessary to run MSMS library searches.
-	Msconvert is part of [ProteoWizard](https://proteowizard.sourceforge.io/index.html) package. The software is necessary if you are planning to work with raw MS data.
-	[SIRIUS](https://bio.informatik.uni-jena.de/software/sirius/) is not properly integrated in the workflow yet, but data may be exported from MetIDTracker in the format accepted by SIRIUS for MSMS  interpretation
-	lib2nist – the tool for converting MSMS libraries to NIST format for use with MSPepSearch. Can be downloaded as standalone or as a part of [NIST Search Software](https://chemdata.nist.gov/dokuwiki/doku.php?id=chemdata:nist17)

##	Open-source software used in the project
This list is probably incomplete and will be updated soon


