'''
Program to generate mapping from units to Domains
i.e. metre -> LENGTH, centimeter -> LENGTH etc.
Requires mapping from Domain -> Unit (Present in file units.txt)
'''

import json
f=open("units.txt","r")
data=eval(f.read())
f2=open("unitMap.json","w")
unit={}
for k,v in data.items():
	print k,v
	for i in v:
		unit[i]=k

keys=json.dumps(unit, sort_keys=True)
f2.write(keys)
f2.close()
