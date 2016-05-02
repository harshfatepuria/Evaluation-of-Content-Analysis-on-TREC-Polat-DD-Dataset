import json, os, sys
from stemming.porter2 import stem
from collections import Counter, OrderedDict
import traceback
from stop_words import get_stop_words

def cleanKeywords(keywords):
    ret = []
    for keyword in keywords:
#        Remove special characters
        keyword = ''.join(e for e in keyword if e.isalnum())
        
#        Stemming
        keyword = stem(keyword);
        
#        Stopword removal
        if (len(keyword) > 0 and keyword not in stopwords):
            ret.append(keyword)
    
    return ret

def addNerToMap(keyword, keywordMap, personNer, organizationNer, locationNer):
    if (keyword not in keywordMap):
        keywordMap[keyword] = {"person": Counter(), "organization": Counter(), "location": Counter()}
    
    for word in personNer:
        keywordMap[keyword]["person"][word] += 1
    
    for word in organizationNer:
        keywordMap[keyword]["organization"][word] += 1
        
    for word in locationNer:
        keywordMap[keyword]["location"][word] += 1    

def transformToJsonObject(keywordMap, keywordCounter, top):
    root = {"name": "keyword", "children" : []}
     
    for (keyword, keywordCount) in keywordCounter.most_common(top):
        obj = keywordMap[keyword]
        keyRoot = {"name" : keyword, "children" : []}
        
        for (nerType, counter) in obj.items():
            if (len(counter) == 0):
                continue
            
            typeRoot = {"name": nerType, "children" : []}
            
            for (entity, count) in counter.most_common(10):
                nerNode = {"name": entity, "size": count}
                typeRoot["children"].append(nerNode)
            
            keyRoot["children"].append(typeRoot)
        
        root["children"].append(keyRoot)
    
    return root

def writeJsonFile(jsonObj, filename):
    with open(filename, 'w', encoding="utf8") as f:
        json.dump(jsonObj, f, sort_keys=False, indent=4, separators=(',', ': '), ensure_ascii=False)


# baseFolder = "C:\\cs599\\a3\\cbor_detail\\result\\au\\gov\\bom\\www\\3ad75a78e5b2c4d27093eed93d8ca979270054d9"
# baseFolder = "C:\\cs599\\a3\\cbor_detail\\result\\"
baseFolder = sys.argv[1]
stopwords = get_stop_words('en')

keywordMap = {}
keywordFreqCounter = Counter()
keywordNerCounter = Counter()

for root, dirnames, files in os.walk(baseFolder):
    for filename in files:
        try:
            with open(os.path.join(root, filename), 'rt', encoding="utf8", errors='ignore') as f:
                data = json.load(f)
                metadata = data['metadata']
                
                if 'cca_urlKeyword' not in metadata:
                    continue
                
                keywords = metadata['cca_urlKeyword']
                keywords = cleanKeywords(keywords)
                
                personNer = metadata["ner_PERSON"] if "ner_PERSON" in metadata else []
                organizationNer = metadata["ner_ORGANIZATION"] if "ner_ORGANIZATION" in metadata else []
                locationNer = metadata["ner_LOCATION"] if "ner_LOCATION" in metadata else []
                
                for keyword in keywords:
                    addNerToMap(keyword, keywordMap, personNer, organizationNer, locationNer)
                    keywordNerCounter[keyword] += (len(personNer) + len(organizationNer) + len(locationNer))
                    keywordFreqCounter[keyword] += 1
                
        except:
            print(os.path.join(root, filename))
            traceback.print_exc()
            
keywordMap = OrderedDict(sorted(keywordMap.items()))

jsonObj = transformToJsonObject(keywordMap, keywordFreqCounter, 30)
writeJsonFile(jsonObj, "flare-freqKeyword.json")

jsonObj = transformToJsonObject(keywordMap, keywordNerCounter, 30)
writeJsonFile(jsonObj, "flare-mostNer.json")
