Evaluation of Content Analysis on TREC Polar Dynamic Domain Dataset
===================

All java files are present in the cs599-evaluation/src/main/java directory. One can import 'cs599-evaluation' directory as a Maven Project and run the respective codes also.



1. Classification path from Request to Content:

	To run the Parser: java ccaparser.CCADetailParserRunner baseFolder resultFolder markerFile
	
	To create data for visualization: python urlKeywordToNer.py ccaDataBaseFolder

	From the extracted data, we develop a script to summarize file size statistics of each file type. D3 visualization of this information is also provided:

	To create data for visualization: python viz-data-gen-script/sizeSummary.py ccaDataBaseFolder





2. File size diversity of Common Crawl (CCA) dataset by MIME type:

	python viz-data-gen-script/sizeAnalysisSolr.py listOfAllFilesDirectory ccaDataBaseFolder





3. Parser Call Chain:

	To run the Parser: java ccaparser.FileDetailParserRunner baseFolder resultFolder markerFile
	
	To create data for visualization: python viz-data-gen-script/parserChain.py ccaDataBaseFolder fullDumpDataFolder




4. Language Identification and diversity:

	To run the language detector: java language.LanguageDetectRunner baseFolder resultFolder minSize
	
	To create data (csv) for visualization: python viz-data-gen-script/languageCompare.py languageDetectedFolder
	
	To run the mixed language detector: java language.MixedLanguageDetectRunner baseFolder resultFolder




5. Named Entity Recognition Agreement:

	To run the parser: java ner.NERAgreementParserRunner baseFolder resultFolder markerFile typeJsonFolder

	To create data for visualization: python viz-data-gen-script/nerAgreement.py extrectedNERFolder

	To run the Grobid Quantity parser and create data for visualization: java ner.GrobidQuantitiesNER





6. Measurement Analysis:

Script to generate:
a. measurement units, their max min and avg values from the data/ metadata generated in previous steps.
b. clustering of units based on domain. eg: metre, centimeter -> LENGTH, joule ->ENERGY etc.
c. clustering of units based on MIME type

Requirements:
	i.  A map of units and domains- available in unitMap.json
	ii. A map of filename to MIME type- available in fileMap.json (Built on my system on our data, 
		pls see the steps below to understand how you can build it for your use)

Flow of this program (IMPORTANT that you read this in order to build all the requirements):
------------------------------------------------------------------------------------------	
	i.  Use ./mappingUnitToDomain.py to generate mapping from UNIT->DOMAIN (already created)
		This requires a file unit.txt which has a dictionary of Domain name and related units.
		Creates a file unitMap.json which will be used in this program. 
		Note: No need to do anything unless you wish to add a unit not present here!

	ii. Use ./mappingFileNameToMimeType.py to generate mapping from FILE_NAME->MIME Type.
		To do this we require all the file paths and Mime Types present in fulldump-path-all-json folder.
		Note1: These files having mime type and file_names were created using Apache Tika (see https://github.com/harshfatepuria/data-analysis-test)
		Creates a file fileMap.json which will be used in this program.
		Note2:  The folder contains the fileMap.json, which can be used as baseline off the TREC Polar Dynamic Domain Dataset Fulldump (~1.7 M files)
				So, if you are using the same dataset, no need to change anything.
		Note3:  In case using Polar fulldump dataset, can use same fileMap. It is ~111 MB, and can be downloaded from:
				https://drive.google.com/file/d/0ByYnDjKhosqbNWhEVVFnVmJXYWs/view?usp=sharing 

	iii.Program requires the extracted measurement metadata  present in folder 'measurement/'
		This metadata was generated using a hybrid measurement parser (wrapped as a Tika parser) which leverages Tika's text extraction capabilities
		and a new measurement extractor model developed as a part of the previous project- Scientific Content Enrichment. 
		(See https://github.com/harshfatepuria/Scientific-Content-Enrichment-in-the-Text-Retrieval-Conference-TREC-Polar-Dynamic-Domain-Dataset/tree/master/cs599-content-enrichment/src/main/java/measurement)

	iv. This Program prepares the files for D3 visualizations- Circle Packing (Mike Bostok's Library; see- http://bl.ocks.org/mbostock/4063530)


	