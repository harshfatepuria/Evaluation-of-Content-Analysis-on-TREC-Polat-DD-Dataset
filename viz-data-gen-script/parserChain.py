import json, os, sys
from collections import defaultdict
import traceback
import statistics


def addValue(root, chain, filetype, value):
    for parser in chain:
        if parser not in root:
            root[parser] = {'fileType' : defaultdict(list)}
        root = root[parser]
    
    root['fileType'][filetype].append(value)

def transformToJsonObject(data, func):
    root = {"name": "parsers", "description": "parser hierarchy"}
    root['children'] = transformChildren(data, func)
    
    return root

def transformChildren(data, func):
    l = []
    
    if (len(data) == 1):
        l = []
        for (key, value) in data['fileType'].items():
            size = func(value)
            sizeStr = size                
            obj = {"name" : key, "description": key, "size": sizeStr}
            l.append(obj)
    else:
        for (key, value) in data.items():
            if key != 'fileType':
                node = {"name" : getParserName(key), "description": key}
                node["children"] = transformChildren(value, func) 
                l.append(node)
        
        if 'fileType' in data and len(data['fileType']) > 0:
            l.append(transformChildren({'fileType': data['fileType']}, func))
    
    return l

def getParserName(parser):
    return parser[parser.rfind(".")+1:]

def writeJsonFile(jsonObj, filename):
    with open(filename, 'w', encoding="utf8") as f:
        json.dump(jsonObj, f, sort_keys=False, indent=4, separators=(',', ': '), ensure_ascii=False)

def getRatio(metadata, key, size):
    if size == 0:
        return 0
    
    value = int(metadata[key][0])
    ratio = 1 * value / (size+1)
    
#     if ratio > 1:
#         print("{} = {}/{} = {}".format(key, value, size, ratio))
    
    return ratio
        
# baseFolder = "C:\\cs599\\a3\\metadata\\result\\aero"
# baseFolder = "C:\\cs599\\a3\\metadata\\result\\"
# dataFolder = "C:\\cs599\\polar-fulldump\\"

baseFolder = sys.argv[1]
dataFolder = sys.argv[2]

countKeeper = {}
textKeeper = {}
ttrKeeper = {}
metaKeeper = {}

for root, dirnames, files in os.walk(baseFolder):
    for filename in files:
        try:
            with open(os.path.join(root, filename), 'rt', encoding="utf8", errors='ignore') as f:
                data = json.load(f)
                metadata = data['metadata']
                
                relativePath = metadata["filePath"][0]
                path = os.path.join(dataFolder, relativePath)
                
                size = os.path.getsize(path)
                
                textRatio = getRatio(metadata, 'tika_extractedTextLength', size);
                ttrRatio = getRatio(metadata, 'ttr_extractedTextLength', size)
                metaRatio = getRatio(metadata, 'tika_metadataLength', size)
                
#                 if max(textRatio, ttrRatio, metaRatio) > 1:
#                     print(relativePath)
                
                addValue(countKeeper, metadata['X-Parsed-By'], metadata['tika_mediaType'][0], 1)
                addValue(textKeeper, metadata['X-Parsed-By'], metadata['tika_mediaType'][0], textRatio)
                addValue(ttrKeeper, metadata['X-Parsed-By'], metadata['tika_mediaType'][0], ttrRatio)
                addValue(metaKeeper, metadata['X-Parsed-By'], metadata['tika_mediaType'][0], metaRatio)
        except:
            print(os.path.join(root, filename))
            traceback.print_exc()


jsonObj = transformToJsonObject(countKeeper, sum)
writeJsonFile(jsonObj, "flare-parserVsCount.json")

jsonObj = transformToJsonObject(textKeeper, statistics.mean)
writeJsonFile(jsonObj, "flare-parserVsText.json")

jsonObj = transformToJsonObject(ttrKeeper, statistics.mean)
writeJsonFile(jsonObj, "flare-parserVsTTR.json")

jsonObj = transformToJsonObject(metaKeeper, statistics.mean)
writeJsonFile(jsonObj, "flare-parserVsMetadata.json")
                
        