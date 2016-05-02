import json, os, sys
from collections import Counter, defaultdict
import traceback

def addTerm(term, termCompare, detector):
    termCompare[term][detector] += 1;

def writeJsonFile(jsonObj, filename):
    with open(filename, 'w', encoding="utf8") as f:
        json.dump(jsonObj, f, sort_keys=False, indent=4, separators=(',', ': '), ensure_ascii=False)

# baseFolder = "C:\\cs599\\a3\\ner\\ttr\\result\\"
baseFolder = sys.argv[1]
termCounter = Counter()
termCompare = defaultdict(Counter)
detectors = set()

for root, dirnames, files in os.walk(baseFolder):
    for filename in files:
        try:
            with open(os.path.join(root, filename), 'rt', encoding="utf8", errors='ignore') as f:
                data = json.load(f)
                metadata = data['metadata']
                
                terms = set()
                
                for (key, value) in metadata.items():
                    if key.startswith("NER"):
                        detector = key[key.find("_") + 1:key.rfind("_")]
                        detectors.add(detector)
                        for term in value:
                            addTerm(term, termCompare, detector)
                            terms.add(term)
                
                for term in terms:
                    termCounter[term] += 1

        except:
            print(os.path.join(root, filename))
            traceback.print_exc()

detectors = sorted(detectors)

obj = {"labels" : [], "series" : []}
freqDict = defaultdict(list)    

for (term, count) in termCounter.most_common(200):
    obj["labels"].append(term)
    for detector in detectors:
        freqDict[detector].append(termCompare[term][detector])
    
for detector in detectors:
    val = {"name": detector, "value" : freqDict[detector]}
    obj["series"].append(val)

writeJsonFile(obj, "nerAgreement.json")
