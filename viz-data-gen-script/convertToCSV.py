# -*- coding: utf-8 -*-


output_file=open('/Users/harshfatepuria/Documents/Github/Evaluation-of-Content-Analysis-on-TREC-Polat-DD-Dataset/result/5-SizeSummary/sizeRatioSummary123.csv','w')
output_file.write("State,Solr Index Size,Actual File Size\n")
with open("/Users/harshfatepuria/Documents/Github/Evaluation-of-Content-Analysis-on-TREC-Polat-DD-Dataset/result/5-SizeSummary/sizeRatioSummary.json") as f:
    newList=eval(f.read())
    for newDict in newList:
        output_file.write(newDict["type"]+","+str(newDict["ratio"])+"\n")
output_file.close()
