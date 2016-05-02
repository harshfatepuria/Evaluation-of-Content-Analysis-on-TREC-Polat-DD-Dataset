# -*- coding: utf-8 -*-

import re
import os
import sys
import json
import math
from glob import iglob
from subprocess import call
from numpy.ma.bench import m1


json_path=sys.argv[1]
reference_path=sys.argv[2]
newDict=[]
fileR=json_path
count=0
icounter=1
for path, subdirs, files in os.walk(fileR):
    for name in files:
        fileName=os.path.join(path, name)
        if fileName[-4:]=="json":
            coreName="HW3File"+str(icounter)
            icounter=icounter+1
            call(["solr","create" ,"-c", coreName])
            with open(fileName) as f:
            	totalSize=0
            	content=eval(f.read())
            	count=len(content["files"])
            	if count>100:
                	count=100
                for i in range(count):
                	totalSize=totalSize+float(os.path.getsize(reference_path+content["files"][i]))
                	call(["post", "-c", coreName, fileName])
            
            
            
            
            if float(totalSize)>0:
            	print "Enter Solr Index size for core: "+coreName
            	solrIndexSize=raw_input()
                solrIndexSize=float(solrIndexSize)*1024*1024
            	tDict={}
            	tDict["solrFileSize"]=float(solrIndexSize)
            	tDict["allFileSize"]=float(totalSize)
            	tDict["type"]=str(name).replace("%2F","/")
            	tDict["ratio"]=float(float(solrIndexSize)/float(totalSize))
            	tDict["mean"]=float(float(totalSize)/count)
                newDict.append(tDict)

keys=json.dumps(newDict, sort_keys=True,indent=4)
print keys
output_file=open('sizeRatioSummary.json','w')
output_file.write(keys)
output_file.close()