import json, os, sys
from collections import defaultdict
import statistics
from operator import itemgetter

def writeJsonFile(jsonObj, filename):
    with open(filename, 'w', encoding="utf8") as f:
        json.dump(jsonObj, f, sort_keys=False, indent=4, separators=(',', ': '), ensure_ascii=False)

# baseFolder = "C:\\cs599\\a3\\cbor_detail\\result\\"
baseFolder = sys.argv[1]
d = defaultdict(list)

for root, dirnames, files in os.walk(baseFolder):
    for filename in files:
        with open(os.path.join(root, filename), 'rt', encoding="utf8", errors='ignore') as f:
            data = json.load(f)
            
            if 'cca_fileSize' in data['metadata']:
                size = int(data['metadata']['cca_fileSize'][0])
            else:
                continue
            
            if 'cca_mediaType' in data['metadata']:
                key = data['metadata']['cca_mediaType'][0]
            else:
                key = data['metadata']['tika_mediaType'][0]
            
            d[key].append(size)

l = []
for (key, value) in d.items():
    m = dict()
    m['type'] = key
    m['count'] = len(value)
#     m['size'] = value
    m['mean'] =  statistics.mean(value)
    m['sum'] = sum(value)
    if(len(value) > 1):
        m['sd'] =  statistics.stdev(value)
    else:
        m['sd'] =  "n/a"
    m['min'] =  min(value)
    m['max'] =  max(value)
    
    l.append(m)

l.sort(key=itemgetter('type'), reverse=False)

writeJsonFile(l, 'sizeSummary.json')
            