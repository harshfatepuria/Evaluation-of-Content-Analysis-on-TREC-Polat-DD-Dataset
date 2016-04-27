'''
Script to generate mapping from file name to MIME type
eg: xyz -> application/text

Requires all the JSON path and MIME type files present in "fulldump-path-all-json/" folder
Creates a file fileMap.json containing the mapping- This will be used in main program mea.py
'''

from glob import iglob
import os
import json


filemap={}

fileR="fulldump-path-all-json/"
for filepath in iglob(os.path.join(fileR, '*.json')): 
	with open(filepath) as f:
		
		data= json.loads(f.read())
		#print data["type"]
		for i in data.get("files"):
			f= i.split("/")
			#print str(f[-1:][0]) 
			filemap[(f[-1:][0] )]=data["type"]
			#print filemap
#print map

fp=open("fileMap.json","w")
#keys=json.dumps(filemap, sort_keys=True)
fp.write(str(filemap))
fp.close()
