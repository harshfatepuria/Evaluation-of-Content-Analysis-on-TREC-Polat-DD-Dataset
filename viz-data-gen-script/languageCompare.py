import json, os, csv, sys
from collections import defaultdict

languageMap = defaultdict(dict)
detectors = []
# baseFolder = "C:\\cs599\\a3\\language\\multi"
baseFolder = sys.argv[1]

for root, dirnames, files in os.walk(baseFolder):
    for filename in files:
        with open(os.path.join(root, filename), 'rt', encoding="utf8", errors='ignore') as f:
            data = json.load(f)
            detector = filename[:filename.rfind(".")]
            detectors.append(detector)
            
            for (key,value) in data.items():
                languageMap[key][detector] = value

languages = sorted(languageMap.keys())

with open('languageCompare.csv', 'w') as csvfile:
    writer = csv.writer(csvfile, lineterminator='\n')
    header = ["Language"] + detectors
    writer.writerow(header)
    
    for lang in languages:
        if lang == 'unknown':
            continue
        
        row = [lang]
        for detector in detectors:
            if detector in languageMap[lang]:
                row.append(languageMap[lang][detector])
            else:
                row.append(0)
        writer.writerow(row)
    
    row = ["n/a"]
    for detector in detectors:
        if detector in languageMap["unknown"]:
            row.append(languageMap["unknown"][detector])
        else:
            row.append(0)
    writer.writerow(row)
    
    
