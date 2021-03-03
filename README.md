# Logger_Application

The Scala Package developed to parse events coming from different system in different formats like XML, JSON .
XML files along with XML Schema are validated and if any error , message will be printed as "Xml file or schema is not valid, check the file"

The resultant output files are stored as Json files which can be Published into a NoSql Database like HBase or DynamoDB.

Inputs:
    1) XML File having user's events on a particular website  ---  Stored in landing directory    input_xml
    2) CSV File having Activity Description for each Activity Code -- Stored in landing directory input_csv
    3) Json file having user's events oon a particular website -- Stored in landing directory     input_json

Outputs:
  1) Json files in the output Directory --- Output
