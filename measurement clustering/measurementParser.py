'''
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

	iv. This Program prepares the files for D3 visualizations- Circle Packing (Mike Bostok's Library; see- http://bl.ocks.org/mbostock/4063530 )

For queries/ comments/ suggestions/ doubts, contact the collaborators of the project:
Rahul Agrawal(rahulagr@usc.edu), Harsh Fatepuria(fatepuri@usc.edu), Warut Roadrungwasinkul(roadrung@usc.edu)
Grad Students, Dept. of CSE, University of Southern California, CA 90089

'''

import sys
import time
from glob import iglob
import json
import os

CURSOR_UP_ONE = '\x1b[1A'
ERASE_LINE = '\x1b[2K'

print time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()), " script started... loading unitMap... "

f= open("unitMap.json")
unitMap= json.loads(f.read())
f.close()

print time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()), " unitMap loaded... loading fileMap... "

f= open("fileMap.json")
filemap=eval(f.read())
f.close()

print time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()), " fileMap loaded... started parsing the files...\n"

meaDomain={}
meaMime={}
mea={}
cnt=0

fileR="result/"
for path, subdirs, files in os.walk(fileR):
	for FP in files:
		filepath= path+'/'+FP
		#print filepath
		f = open(filepath,"r")
		data= eval(f.read())
		#print data
		for i in data["metadata"]["measurement_unit"]:
			#print i

			############################################################################################
			#Finding measurement values form the files in 'measurement/' folder and adding in dictionary
			############################################################################################
			if i in mea:
				mea[i].append(float(data["metadata"]["measurement_value"][data["metadata"]["measurement_unit"].index(i)]))
			else:
				mea[i]=[]
				mea[i].append(float(data["metadata"]["measurement_value"][data["metadata"]["measurement_unit"].index(i)]))
			
			################################
			#Clustering based on Domain Type
			################################
			if i in unitMap:
				if unitMap[i] in meaDomain:
					if i in meaDomain[unitMap[i]]:
						meaDomain[unitMap[i]][i]= meaDomain[unitMap[i]][i] +1
					else:
						meaDomain[unitMap[i]][i]=1
				else:
					meaDomain[unitMap[i]]={}
					meaDomain[unitMap[i]][i]=1
			
			#############################
			#Clusterig based on MIME type
			#############################
			fileType= filemap[FP.split('.')[0]]
			
			if fileType in meaMime:
				meaMime[fileType]
				if i in meaMime[fileType]:
					meaMime[fileType][i]=meaMime[fileType][i]+1
				else:
					meaMime[fileType][i]=1
			else:
				meaMime[fileType]={}
				meaMime[fileType][i]=1
		
		cnt=cnt+1
		#print(CURSOR_UP_ONE + ERASE_LINE+ CURSOR_UP_ONE)
		print "Parsing file ", cnt
		#break

print time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()), " ",cnt," files parsed... writing logs...\n"


# i. Creating a dictionary of min, max and average values for each unit and dumping results in a file
maxmin={}
op= open("meaMaxMinOutputJSON.json","w")
for k,v in mea.items():
	maxmin[k]=[0,0,0]
	maxmin[k][0]=min(v)
	maxmin[k][1]=max(v)
	maxmin[k][2]=sum(v)/float(len(v))
keys=json.dumps(maxmin, sort_keys=True)
op.write(keys)
op.close()
#print "MEA Data:\n--------\n",maxmin


# ii. Writing Measuremnt count data clustered based on Doamin of units
'''
op= open("meaDomainOutputJSON.json","w")
keys=json.dumps(meaDomain, sort_keys=True)
op.write(keys)
op.close()
'''
flare={}
flare["name"]="Measurement Domain Cluster"
flare["children"]=[]
for k,v in meaDomain.items():
	x={}
	x["name"]=k
	x["children"]=[]
	for a,b in v.items():
		y={}
		y["name"]=a
		y["size"]=b
		x["children"].append(y)

	flare["children"].append(x)
#print "\n",flare,"\n"
op= open("meaDomainOutputFlareJSONForD3.json","w")
keys=json.dumps(flare, sort_keys=True)
op.write(keys)
op.close()


# iii. Writing Measuremnt count data clustered based on Mime type of files
'''
op= open("meaMimeOutputJSON.json","w")
keys=json.dumps(meaMime, sort_keys=True)
op.write(str(keys))
op.close()
'''
flare={}
flare["name"]="Measurement MIME Cluster"
flare["children"]=[]
for k,v in meaMime.items():
	x={}
	x["name"]=k
	x["children"]=[]
	for a,b in v.items():
		y={}
		y["name"]=a
		y["size"]=b
		x["children"].append(y)

	flare["children"].append(x)
#print "\n",flare,"\n"
op= open("meaMimeOutputFlareJSONForD3.json","w")
keys=json.dumps(flare, sort_keys=True)
op.write(keys)
op.close()

print time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()), "ending script...\n"
