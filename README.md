# Flat File to XML Conversion Library

Transforms flat files to XML with support of options similar to content conversion on SAP PI.

## Why To Use This Library?

Migration from SAP PI/PO to SAP Cloud Integration (CPI) has many challenges. 
One of them is when PI File channel has a content conversion configured, 
it's expected to have the same behavior after migration to CPI. This library aims to smoothen the
transition, mitigating the need for additional development efforts.

## How To Use This Library on SAP CPI platform
1. Add a library to classpath by adding it as resource to IFlow and create a groovy script that executes conversion
2. In the groovy script create and configure `ConversionConfig` (see examples below)
3. Initialize the instance of `ContentConverter` and execute conversion of the flat document:

There are 3 ways of initializing the converter :

1.Direct initialization with the 'Flat to XML Content' converter.
```
ConversionConfig config = ...; // configure conversion config

// for documents as byte arrays
byte[] flatDocument = ...; // take it from message
ContentConverter converter = new FlatToXmlContentConverter();
byte[] convertedXml = converter.convert(flatDocument, config);

// for documents as strings
String flatDocument = ...; // take it from message
ContentConverter converter = new FlatToXmlContentConverter();
String convertedXml = converter.convert(flatDocument, config);
```

2.Direct initialization with the 'XML to Flat Content' converter.
```
ConversionConfig config = ...; // configure conversion config

// for documents as byte arrays
byte[] flatDocument = ...; // take it from message
ContentConverter converter = new XmlToFlatContentConverter();
byte[] convertedTxt = converter.convert(flatDocument, config);

// for documents as strings
String flatDocument = ...; // take it from message
ContentConverter converter = new XmlToFlatContentConverter();
String convertedTxt = converter.convert(flatDocument, config);
```

3.Dynamic initialization of converters based on the converter types specified in the ConversionConfig object.
```
ConversionConfig config = ...; // configure conversion config, make sure that ConversionConfig has ContentConversionType(XML_TO_FLAT or FLAT_TO_XML)

byte[] flatDocument = ...; // take it from message
ContentConverter converter = ContentConverterFactory.initializeContentConverter(config.getContentConversionType());
byte[] convertedDocument = converter.convert(flatDocument, config);
```

## Supported Content Conversion Use Cases

1. **CSV with Headers to XML**: Transforms CSV data, preserving headers, into standard XML format. 

**Example:**

Input flat document:
```csv
Start Time;Duration;IFlow;Message ID;Correlation ID;Application Message ID;Application Message Type;Status;Sender;Receiver;DOC;ORDERNR;Vendor
11/10/2022 15:12:32;1 s 334 ms;GetCustomersOrder2_Uiveri5;AGNFa8BD00KAOwctLiGjAxtPpB5t;AGNFa8Aw7xxwyqoZJ_HK1UMj1Eax;6188950;;COMPLETED;sender2;receiver2;;;
11/10/2022 15:12:29;2 s 163 ms;GetCustomersOrder2_Uiveri5;AGNFa73d5tZgqem97vMzpZ537dSk;AGNFa71CLLER0rg-BatTpOe5zU6u;6188949;;COMPLETED;sender2;receiver2;;;
```
**The above CSV is transformed into xml using the below configuration:**

**Document Name**: CPIList  
**Namespace**: http://figaf.com/CPILIST  
**Recordset Structure**: row,\*  
**Field Names**:

- StartTime
- Duration
- IFlow
- MessageID
- CorrelationID
- ApplicationMessageID
- ApplicationMessageType
- Status
- Sender
- Receiver
- DOC
- ORDERNR
- Vendor

Java Configuration Object:
```java 
public ConversionConfig createConversionConfig() {
    ConversionConfig config = new ConversionConfig();
    config.setDocumentName("CPIList");
    config.setDocumentNamespace("http://figaf.com/CPILIST");
    config.setRecordsetStructure("row,*");
    
    Map<String, ConversionConfig.SectionParameters> sections = new HashMap<>();
    ConversionConfig.SectionParameters section = new ConversionConfig.SectionParameters();
    section.setFieldNames("StartTime,Duration,IFlow,MessageID,CorrelationID,ApplicationMessageID,ApplicationMessageType,Status,Sender,Receiver,DOC,ORDERNR,Vendor");
    section.setFieldSeparator(";");
    sections.put("row", section);
    config.setSectionParameters(sections);

    return config;
}
```
Result:
```xml
<ns:CPIList xmlns:ns="http://figaf.com/CPILIST">
    <row>
        <StartTime>Start Time</StartTime>
        <Duration>Duration</Duration>
        <IFlow>IFlow</IFlow>
        <MessageID>Message ID</MessageID>
        <CorrelationID>Correlation ID</CorrelationID>
        <ApplicationMessageID>Application Message ID</ApplicationMessageID>
        <ApplicationMessageType>Application Message Type</ApplicationMessageType>
        <Status>Status</Status>
        <Sender>Sender</Sender>
        <Receiver>Receiver</Receiver>
        <DOC>DOC</DOC>
        <ORDERNR>ORDERNR</ORDERNR>
        <Vendor>Vendor</Vendor>
    </row>
    <row>
        <StartTime>11/10/2022 15:12:32</StartTime>
        <Duration>1 s 334 ms</Duration>
        <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
        <MessageID>AGNFa8BD00KAOwctLiGjAxtPpB5t</MessageID>
        <CorrelationID>AGNFa8Aw7xxwyqoZJ_HK1UMj1Eax</CorrelationID>
        <ApplicationMessageID>6188950</ApplicationMessageID>
        <ApplicationMessageType/>
        <Status>COMPLETED</Status>
        <Sender>sender2</Sender>
        <Receiver>receiver2</Receiver>
        <DOC/>
        <ORDERNR/>
        <Vendor/>
    </row>
    <row>
        <StartTime>11/10/2022 15:12:29</StartTime>
        <Duration>2 s 163 ms</Duration>
        <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
        <MessageID>AGNFa73d5tZgqem97vMzpZ537dSk</MessageID>
        <CorrelationID>AGNFa71CLLER0rg-BatTpOe5zU6u</CorrelationID>
        <ApplicationMessageID>6188949</ApplicationMessageID>
        <ApplicationMessageType/>
        <Status>COMPLETED</Status>
        <Sender>sender2</Sender>
        <Receiver>receiver2</Receiver>
        <DOC/>
        <ORDERNR/>
        <Vendor/>
    </row>
</ns:CPIList>
```

2. **Structured Text to XML**: Adapts text with specific markers (e.g., H, D, T) enclosed in double quotes to XML.

**Example:**

Input flat document:
```
"H","","DANIEL","Graversen,Daniel"
"D","birthday","0802"
"D","shoe","43"
T;1
"H","","Hans","John,Hans"
"D","bir,thday","10,06"
"D","shoe","10,06"
T;1
...
```

**The above Structured Text is transformed into xml using the below configuration:**

- **Document Name**: Employees
- **Document Namespace**: http://figaf.com/2
- **Recordset name**: Employee
- **Recordset namespace**: http://figaf.com/daniel
- **Recordset Structure**: H,1,D,\*,T,1
    - H:
        - Field Names: TYPE, Dummy, Name, FullName
    - D:
        - Field Names: TYPE, Qualifier, Num
    - T:
        - Field Separator: ';'
        - Field Names: TYPE, Status
      
Java Configuration Object:
```java
public ConversionConfig createConversionConfig() {
    ConversionConfig config = new ConversionConfig();
    config.setDocumentName("Employees");
    config.setDocumentNamespace("http://figaf.com/2");
    config.setRecordsetName("Employee");
    config.setRecordsetNamespace("http://figaf.com/daniel");
    config.setRecordsetStructure("H,1,D,*,T,1");
    
    Map<String, ConversionConfig.SectionParameters> sections = new HashMap<>();
    
    // Section for H
    ConversionConfig.SectionParameters sectionH = new ConversionConfig.SectionParameters();
    sectionH.setFieldNames("TYPE,Dummy,Name,FullName");
    sectionH.setFieldSeparator(",");
    sections.put("H", sectionH);

    // Section for D
    ConversionConfig.SectionParameters sectionD = new ConversionConfig.SectionParameters();
    sectionD.setFieldNames("TYPE,Qualifier,Num");
    sectionD.setFieldSeparator(",");
    sections.put("D", sectionD);

    // Section for T
    ConversionConfig.SectionParameters sectionT = new ConversionConfig.SectionParameters();
    sectionT.setFieldNames("TYPE,Status");
    sectionT.setFieldSeparator(",");
    sections.put("T", sectionT);

    config.setSectionParameters(sections);

    return config;
}
```
Result:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<ns:Employees xmlns:ns="http://figaf.com/2">
    <ns:Employee xmlns:ns="http://figaf.com/daniel">
        <H>
            <TYPE>H</TYPE>
            <Dummy/>
            <Name>DANIEL</Name>
            <FullName>Graversen,Daniel</FullName>
        </H>
        <D>
            <TYPE>D</TYPE>
            <Qualifier>birthday</Qualifier>
            <Num>0802</Num>
        </D>
        <D>
            <TYPE>D</TYPE>
            <Qualifier>shoe</Qualifier>
            <Num>43</Num>
        </D>
        <T>
            <TYPE>T</TYPE>
            <Status>1</Status>
        </T>
    </ns:Employee>
</ns:Employees>
```

3. **Mixed Content to XML**: Processes content combining structured text and CSV-like data, identifying keys into a XML
   format.

**Example:**

Input flat document:
```
"HD2130003"
"PRAG123532DANIEL"
"PRBE214312SAP"
"LI42003.9"
"LI41003.1"
"LI49023.2"
"KK;11/10/2022 15:12:32;1 s 334 ms;GetCustomersOrder2_Uiveri5;AGNFa8BD00KAOwctLiGjAxtPpB5t;AGNFa8Aw7xxwyqoZJ_HK1UMj1Eax;6188950;;COMPLETED;sender2;receiver2;;;"
"KK;11/10/2022 15:12:29;2 s 163 ms;GetCustomersOrder2_Uiveri5;AGNFa73d5tZgqem97vMzpZ537dSk;AGNFa71CLLER0rg-BatTpOe5zU6u;6188949;;COMPLETED;sender2;receiver2;;;"
 ```

**The above Mixed Content is transformed into xml using the below configuration:**

- **Document Name**: CPIListFixed
- **Document Namespace**: http://figaf.com/CPIListFixed
- **Recordset Structure**: HR,1,PR,\*,LI,\*,KK,\*
    - HR:
        - Field Fixed Lengths 2,3,4 (#Note: The first 2 characters of text data map to the KEY tag, the next 3 map to ID1, the next 4 map to Doc.)
        - Field Names: KEY, ID1, Doc
    - PR:
        - Field Fixed Lengths 2,2,5,6 (#Note: The first 2 characters of text data map to the KEY tag, the next 2 map to TYPE, the next 5 map to ID and the last 6 map to NAME.)
        - Field Names: KEY, TYPE, ID, NAME
    - LI:
        - Field Names: KEY, LINE, AMOUNT
    - KK fieldNames:
        - KEY
        - StartTime
        - Duration
        - IFlow
        - MessageID
        - CorrelationID
        - ApplicationMessageID
        - ApplicationMessageType
        - Status
        - Sender
        - Receiver
        - DOC
        - ORDERNR
        - Vendor
       
Java Configuration Object:
```java
public ConversionConfig createConversionConfig() {
    ConversionConfig config = new ConversionConfig();
    // Set the main attributes of the config
    config.setDocumentName("CPIListFixed");
    config.setDocumentNamespace("http://figaf.com/CPIListFixed");
    config.setRecordsetStructure("HR,1,PR,*,LI,*,KK,*");

    // Create and set the section parameters
    Map<String, ConversionConfig.SectionParameters> sections = new HashMap<>();

    // Section for HR
    ConversionConfig.SectionParameters sectionHR = new ConversionConfig.SectionParameters();
    sectionHR.setFieldNames("KEY,ID1,Doc");
    sectionHR.setFieldFixedLengths("2,3,4");
    sections.put("HR", sectionHR);

    // Section for PR
    ConversionConfig.SectionParameters sectionPR = new ConversionConfig.SectionParameters();
    sectionPR.setFieldNames("KEY,TYPE,ID,NAME");
    sectionPR.setFieldFixedLengths("2,2,5,6");
    sections.put("PR", sectionPR);

    // Section for LI
    ConversionConfig.SectionParameters sectionLI = new ConversionConfig.SectionParameters();
    sectionLI.setFieldNames("KEY,LINE,AMOUNT");
    sectionLI.setFieldFixedLengths("2,2,6");
    sections.put("LI", sectionLI);

    // Section for KK
    ConversionConfig.SectionParameters sectionKK = new ConversionConfig.SectionParameters();
    sectionKK.setFieldSperator(";");
    sectionKK.setFieldNames("KEY,StartTime,Duration,IFlow,MessageID,CorrelationID,ApplicationMessageID,ApplicationMessageType,Status,Sender,Receiver,DOC,ORDERNR,Vendor");
    sections.put("KK", sectionKK);

    config.setSectionParameters(sections);

    return config;
}
 ```
Result:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<ns:CPIListFixed xmlns:ns="http://figaf.com/CPIListFixed">
    <Recordset>
        <!-- HR Section -->
        <HR>
            <KEY>HD</KEY>
            <ID1>213</ID1>
            <Doc>0003</Doc>
        </HR>

        <!-- PR Section -->
        <PR>
            <KEY>PR</KEY>
            <TYPE>AG</TYPE>
            <ID>12353</ID>
            <NAME>2DANIE</NAME>
        </PR>
        <PR>
            <KEY>PR</KEY>
            <TYPE>BE</TYPE>
            <ID>21431</ID>
            <NAME>2SAP</NAME>
        </PR>

        <!-- LI Section -->
        <LI>
            <KEY>LI</KEY>
            <LINE>42</LINE>
            <AMOUNT>003.9</AMOUNT>
        </LI>
        <LI>
            <KEY>LI</KEY>
            <LINE>41</LINE>
            <AMOUNT>003.1</AMOUNT>
        </LI>
        <LI>
            <KEY>LI</KEY>
            <LINE>49</LINE>
            <AMOUNT>023.2</AMOUNT>
        </LI>

        <!-- KK Section -->
        <KK>
            <KEY>KK</KEY>
            <StartTime>11/10/2022 15:12:32</StartTime>
            <Duration>1 s 334 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNFa8BD00KAOwctLiGjAxtPpB5t</MessageID>
            <CorrelationID>AGNFa8Aw7xxwyqoZJ_HK1UMj1Eax</CorrelationID>
            <ApplicationMessageID>6188950</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
        <KK>
            <KEY>KK</KEY>
            <StartTime>11/10/2022 15:12:29</StartTime>
            <Duration>2 s 163 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNFa73d5tZgqem97vMzpZ537dSk</MessageID>
            <CorrelationID>AGNFa71CLLER0rg-BatTpOe5zU6u</CorrelationID>
            <ApplicationMessageID>6188949</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
    </Recordset>
</ns:CPIListFixed>
```

4.**Multiple Recordsets to XML**:Transforms mixed content with several recordsets into an XML structure format.

**Example:**

Input flat document:
```
  HD2130003  ------ 1 iteration of recordsetStructure
  PRAG123532DANIEL
  PRBE214312SAP
  LI42003.9
  LI41003.1
  LI49023.2
  KK;11/10/2022 15:12:32;1 s 334 ms;GetCustomersOrder2_Uiveri5;AGNFa8BD00KAOwctLiGjAxtPpB5t;AGNFa8Aw7xxwyqoZJ_HK1UMj1Eax;6188950;;COMPLETED;sender2;receiver2;;;
  KK;11/10/2022 15:12:29;2 s 163 ms;GetCustomersOrder2_Uiveri5;AGNFa73d5tZgqem97vMzpZ537dSk;AGNFa71CLLER0rg-BatTpOe5zU6u;6188949;;COMPLETED;sender2;receiver2
  KK;10/10/2022 15:27:12;1 s 330 ms;GetCustomersOrder2_Uiveri5;AGNEHbBvEL56maMnwe-LVTjPhWKR;AGNEHbBYwOzaDVkf7bxbtN9cb8Qv;6188940;;COMPLETED;sender2;receiver2;;;
  KK;10/10/2022 15:27:09;2 s 96 ms;GetCustomersOrder2_Uiveri5;AGNEHa2VekzovJ5db3uS7XiMIWAT;AGNEHa3dNy2oHXrP2_e3Hadm6ptK;6188939;;COMPLETED;sender2;receiver2;;;
  HD2130003 ------ 2 iteration of recordsetStructure
  PRAG123532DANIEL
  PRBE214312SAP
  KK;11/10/2022 15:12:32;1 s 334 ms;GetCustomersOrder2_Uiveri5;AGNFa8BD00KAOwctLiGjAxtPpB5t;AGNFa8Aw7xxwyqoZJ_HK1UMj1Eax;6188950;;COMPLETED;sender2;receiver2;;;
  KK;11/10/2022 15:12:29;2 s 163 ms;GetCustomersOrder2_Uiveri5;AGNFa73d5tZgqem97vMzpZ537dSk;AGNFa71CLLER0rg-BatTpOe5zU6u;6188949;;COMPLETED;sender2;receiver2;;;
  KK;10/10/2022 15:27:12;1 s 330 ms;GetCustomersOrder2_Uiveri5;AGNEHbBvEL56maMnwe-LVTjPhWKR;AGNEHbBYwOzaDVkf7bxbtN9cb8Qv;6188940;;COMPLETED;sender2;receiver2;;;
  KK;10/10/2022 15:27:09;2 s 96 ms;GetCustomersOrder2_Uiveri5;AGNEHa2VekzovJ5db3uS7XiMIWAT;AGNEHa3dNy2oHXrP2_e3Hadm6ptK;6188939;;COMPLETED;sender2;receiver2;;;
```

**The mixed content transformed into XML using the following configuration:**

- **Document Name**: CPIListFixed
- **Document Namespace**: [http://figaf.com/daniel](http://figaf.com/daniel)
- **Recordset Name**: DANIELRecordSEt
- **Recordset Structure**: HR,1,PR,\*,LI,\*,KK,\*
    - HR:
        - Field Names: KEY, ID1, Doc
        - Field Fixed Lengths: 2,3,4 (#Note: The first 2 characters of text data map to the KEY tag, the next 3 map to ID1 and last 4 map to Doc.)
        - Key Field Value: HD key identifies HR recordset structure
    - PR:
        - Field Names: KEY, TYPE, ID, NAME
    - LI:
        - Field Names: KEY, LINE, AMOUNT
    - KK:
        - Field Names:
            - KEY
            - StartTime
            - Duration
            - IFlow
            - MessageID
            - CorrelationID
            - ApplicationMessageID
            - ApplicationMessageType
            - Status
            - Sender
            - Receiver
            - DOC
            - ORDERNR
            - Vendor

---

Java Configuration Object:
```java
public ConversionConfig createConversionConfig() {
    ConversionConfig config = new ConversionConfig();
    // Set the main attributes of the config
    config.setDocumentName("CPIListFixed");
    config.setDocumentNamespace("http://figaf.com/daniel");
    config.setRecordsetName("DANIELRecordSEt");
    config.setRecordsetStructure("HR,1,PR,*,LI,*,KK,*");
    config.setIgnoreRecordsetName(false);

    // Create and set the section parameters
    Map<String, ConversionConfig.SectionParameters> sections = new HashMap<>();

    // Section for HR
    ConversionConfig.SectionParameters sectionHR = new ConversionConfig.SectionParameters();
    sectionHR.setFieldNames("KEY,ID1,Doc");
    sectionHR.setFieldFixedLengths("2,3,4");
    sectionHR.setKeyFieldValue("HD"); // HD key identifies HR recordset structure
    sections.put("HR", sectionHR);

    // Section for PR
    ConversionConfig.SectionParameters sectionPR = new ConversionConfig.SectionParameters();
    sectionPR.setFieldNames("KEY,TYPE,ID,NAME");
    sectionPR.setFieldFixedLengths("2,2,5,6");
    sections.put("PR", sectionPR);

    // Section for LI
    ConversionConfig.SectionParameters sectionLI = new ConversionConfig.SectionParameters();
    sectionLI.setFieldNames("KEY,LINE,AMOUNT");
    sectionLI.setFieldFixedLengths("2,2,6");
    sections.put("LI", sectionLI);

    // Section for KK
    ConversionConfig.SectionParameters sectionKK = new ConversionConfig.SectionParameters();
    sectionKK.setFieldSperator(";");
    sectionKK.setFieldNames("KEY,StartTime,Duration,IFlow,MessageID,CorrelationID,ApplicationMessageID,ApplicationMessageType,Status,Sender,Receiver,DOC,ORDERNR,Vendor");
    sections.put("KK", sectionKK);

    config.setSectionParameters(sections);

    return config;
}
 ```
Result:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<ns:CPIListFixed xmlns:ns="http://figaf.com/CPIListFixed">
    <ns:DANIELRecordSEt xmlns:ns="http://figaf.com/daniel">
        <HR>
            <KEY>HD</KEY>
            <ID1>213</ID1>
            <Doc>0003</Doc>
        </HR>
        <PR>
            <KEY>PR</KEY>
            <TYPE>AG</TYPE>
            <ID>12353</ID>
            <NAME>2DANIE</NAME>
        </PR>
        <PR>
            <KEY>PR</KEY>
            <TYPE>BE</TYPE>
            <ID>21431</ID>
            <NAME>2SAP</NAME>
        </PR>
        <LI>
            <KEY>LI</KEY>
            <LINE>42</LINE>
            <AMOUNT>003.9</AMOUNT>
        </LI>
        <LI>
            <KEY>LI</KEY>
            <LINE>41</LINE>
            <AMOUNT>003.1</AMOUNT>
        </LI>
        <LI>
            <KEY>LI</KEY>
            <LINE>49</LINE>
            <AMOUNT>023.2</AMOUNT>
        </LI>
        <KK>
            <KEY>KK</KEY>
            <StartTime>11/10/2022 15:12:32</StartTime>
            <Duration>1 s 334 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNFa8BD00KAOwctLiGjAxtPpB5t</MessageID>
            <CorrelationID>AGNFa8Aw7xxwyqoZJ_HK1UMj1Eax</CorrelationID>
            <ApplicationMessageID>6188950</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
        <KK>
            <KEY>KK</KEY>
            <StartTime>11/10/2022 15:12:29</StartTime>
            <Duration>2 s 163 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNFa73d5tZgqem97vMzpZ537dSk</MessageID>
            <CorrelationID>AGNFa71CLLER0rg-BatTpOe5zU6u</CorrelationID>
            <ApplicationMessageID>6188949</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
        <KK>
            <KEY>KK</KEY>
            <StartTime>10/10/2022 15:27:12</StartTime>
            <Duration>1 s 330 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNEHbBvEL56maMnwe-LVTjPhWKR</MessageID>
            <CorrelationID>AGNEHbBYwOzaDVkf7bxbtN9cb8Qv</CorrelationID>
            <ApplicationMessageID>6188940</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
        <KK>
            <KEY>KK</KEY>
            <StartTime>10/10/2022 15:27:09</StartTime>
            <Duration>2 s 96 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNEHa2VekzovJ5db3uS7XiMIWAT</MessageID>
            <CorrelationID>AGNEHa3dNy2oHXrP2_e3Hadm6ptK</CorrelationID>
            <ApplicationMessageID>6188939</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
    </ns:DANIELRecordSEt>
    <ns:DANIELRecordSEt xmlns:ns="http://figaf.com/daniel">
        <HR>
            <KEY>HD</KEY>
            <ID1>213</ID1>
            <Doc>0003</Doc>
        </HR>
        <PR>
            <KEY>PR</KEY>
            <TYPE>AG</TYPE>
            <ID>12353</ID>
            <NAME>2DANIE</NAME>
        </PR>
        <PR>
            <KEY>PR</KEY>
            <TYPE>BE</TYPE>
            <ID>21431</ID>
            <NAME>2SAP</NAME>
        </PR>
        <KK>
            <KEY>KK</KEY>
            <StartTime>11/10/2022 15:12:32</StartTime>
            <Duration>1 s 334 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNFa8BD00KAOwctLiGjAxtPpB5t</MessageID>
            <CorrelationID>AGNFa8Aw7xxwyqoZJ_HK1UMj1Eax</CorrelationID>
            <ApplicationMessageID>6188950</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
        <KK>
            <KEY>KK</KEY>
            <StartTime>11/10/2022 15:12:29</StartTime>
            <Duration>2 s 163 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNFa73d5tZgqem97vMzpZ537dSk</MessageID>
            <CorrelationID>AGNFa71CLLER0rg-BatTpOe5zU6u</CorrelationID>
            <ApplicationMessageID>6188949</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
        <KK>
            <KEY>KK</KEY>
            <StartTime>10/10/2022 15:27:12</StartTime>
            <Duration>1 s 330 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNEHbBvEL56maMnwe-LVTjPhWKR</MessageID>
            <CorrelationID>AGNEHbBYwOzaDVkf7bxbtN9cb8Qv</CorrelationID>
            <ApplicationMessageID>6188940</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
        <KK>
            <KEY>KK</KEY>
            <StartTime>10/10/2022 15:27:09</StartTime>
            <Duration>2 s 96 ms</Duration>
            <IFlow>GetCustomersOrder2_Uiveri5</IFlow>
            <MessageID>AGNEHa2VekzovJ5db3uS7XiMIWAT</MessageID>
            <CorrelationID>AGNEHa3dNy2oHXrP2_e3Hadm6ptK</CorrelationID>
            <ApplicationMessageID>6188939</ApplicationMessageID>
            <ApplicationMessageType/>
            <Status>COMPLETED</Status>
            <Sender>sender2</Sender>
            <Receiver>receiver2</Receiver>
            <DOC/>
            <ORDERNR/>
            <Vendor/>
        </KK>
    </ns:DANIELRecordSEt>
</ns:CPIListFixed>
```

5.**Text-Based Data to XML**:Transforms pure text inputs into XML format.

**Example:**

Input flat document:
```
HD2130003
PRAG123532DANIEL
PRBE214312SAP
LI42003.9
LI41003.1
LI49023.2
```

**The above Text-Based data is transformed into XML using the following configuration:**

- **Document Name**: CPIListFixed
- **Document Namespace**: [http://figaf.com/CPIListFixed](http://figaf.com/CPIListFixed)
- **Recordset Structure**: HR,1,PR,\*,LI,\*
    - HR:
      - Field Names: KEY, ID1, Doc
      - Field Fixed Lengths: 2,3,4 (#Note: The first 2 characters of text data map to the KEY tag, the next 3 map to ID1 and last 4 map to Doc.)
      - Key Field Value: HD key identifies HR recordset structure
    - PR:
      - Field Names: KEY, TYPE, ID, NAME
      - Field Fixed Lengths: 2,2,5,6
    - LI:
      - Field Names: KEY, LINE, AMOUNT
---

Java Configuration Object:
```java
public ConversionConfig createConversionConfig() {
    ConversionConfig config = new ConversionConfig();
    config.setDocumentName("CPIListFixed");
    config.setDocumentNamespace("http://figaf.com/CPIListFixed");
    config.setRecordsetStructure("HR,1,PR,*,LI,*");
    config.setIgnoreRecordsetName(false);

    Map<String, ConversionConfig.SectionParameters> sections = new HashMap<>();
    ConversionConfig.SectionParameters sectionHR = new ConversionConfig.SectionParameters();
    sectionHR.setFieldNames("KEY,ID1,Doc");
    sectionHR.setFieldFixedLengths("2,3,4");
    sectionHR.setKeyFieldValue("HD");
    sections.put("HR", sectionHR);

    ConversionConfig.SectionParameters sectionPR = new ConversionConfig.SectionParameters();
    sectionPR.setFieldNames("KEY,TYPE,ID,NAME");
    sectionPR.setFieldFixedLengths("2,2,5,6");
    sections.put("PR", sectionPR);

    ConversionConfig.SectionParameters sectionLI = new ConversionConfig.SectionParameters();
    sectionLI.setFieldNames("KEY,LINE,AMOUNT");
    sectionLI.setFieldFixedLengths("2,2,6");
    sections.put("LI", sectionLI);

    config.setSectionParameters(sections);

    return config;
}
 ```
Result:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<ns:CPIListFixed xmlns:ns="http://figaf.com/CPIListFixed">
    <Recordset>
        <HR>
            <KEY>HD</KEY>
            <ID1>213</ID1>
            <Doc>0003</Doc>
        </HR>
        <PR>
            <KEY>PR</KEY>
            <TYPE>AG</TYPE>
            <ID>12353</ID>
            <NAME>2DANIE</NAME>
        </PR>
        <PR>
            <KEY>PR</KEY>
            <TYPE>BE</TYPE>
            <ID>21431</ID>
            <NAME>2SAP</NAME>
        </PR>
        <LI>
            <KEY>LI</KEY>
            <LINE>42</LINE>
            <AMOUNT>003.9</AMOUNT>
        </LI>
        <LI>
            <KEY>LI</KEY>
            <LINE>41</LINE>
            <AMOUNT>003.1</AMOUNT>
        </LI>
        <LI>
            <KEY>LI</KEY>
            <LINE>49</LINE>
            <AMOUNT>023.2</AMOUNT>
        </LI>
    </Recordset>
</ns:CPIListFixed>
```
6.**HeaderLine with field separator XML to txt**:Transforms XML into txt.

**Example:**

Input xml document:
```
<root>
<nameA>
<value1>value</value1>
<value2>value</value2>
<value3>value</value3>
</nameA>
<nameA>
<value1>valuekldshfaiosdljfio</value1>
<value2>valuesafjoæ</value2>
<value3>value23fdasd</value3>
</nameA>

<nameA>
<value1>value3423</value1>
<value2>value</value2>
<value3>valuefsdfs</value3>
</nameA>

<nameA>
<value1>value</value1>
<value2>value</value2>
<value3>value</value3>
</nameA>

<nameA>
<value1>value</value1>
<value2>value</value2>
<value3>value</value3>
</nameA>

</root>
```

**The above XML is transformed to txt using the following configuration:**

- **Recordset Structure**: nameA
    - nameA:
      -  addHeaderLine: 1
      -  fieldSeparator: ;
---

Java Configuration Object:
```java

 public ConversionConfig createConversionConfig() {
    ConversionConfig config = new ConversionConfig();
    config.setRecordsetStructure("nameA");
    config.setTargetFileName("xi_output.txt");

    Map<String, ConversionConfig.SectionParameters> sections = new HashMap<>();
    ConversionConfig.SectionParameters sectionParameters = new ConversionConfig.SectionParameters();
    
    sectionParameters.setFieldSeparator(";");
    sectionParameters.setAddHeaderLine(1);
    sections.put("nameA", sectionParameters);
    config.setSectionParameters(sections);
    return config;
}
 ```

Result:
```txt
value1;value2;value3
value;value;value
valuekldshfaiosdljfio;valuesafjoæ;value23fdasd
value3423;value;valuefsdfs
value;value;value
value;value;value
```

7.**Multiple recordset elements XML to txt**:Transforms XML into txt.

**Example:**

Input xml document:
```
<root>
  <nameA>
    <value1>value</value1>
    <value2>value</value2>
    <value3>value</value3>
  </nameA>
  <nameB>
    <value4>value</value4>
  </nameB>
  <nameB>
    <value4>value</value4>
  </nameB>
  <nameB>
    <value4>value</value4>
    <value5>value5</value5>
  </nameB>
  <nameC>
    <value1>value</value1>
    <value2>value</value2>
    <value3>value</value3>
  </nameC>
  <nameC>
    <value1>value12321312</value1>
    <value2>value421421</value2>
    <value3>value32532</value3>
  </nameC>
</root>
```

**The above XML is transformed to txt using the following configuration:**

- **Recordset Structure**: nameA,nameB,nameC
    - nameA:
        -  fieldFixedLengths: 10,5,3
        -  fixedLengthTooShortHandling: Cut
        -  addHeaderLine: 1
    - nameB:
        -  fieldSeparator: ;
        -  addHeaderLine: 2
    - nameC:
        -  addHeaderLine: 3
        -  headerLine: name1-value1
        -  fieldSeparator: ,
        -  beginSeparator: '
        -  endSeparator: '
---

Java Configuration Object:
```java

public ConversionConfig createConversionConfig() {
    ConversionConfig config = new ConversionConfig();
    config.setRecordsetStructure("nameA,nameB,nameC");
    config.setTargetFileName("xi_output.txt");
    
    Map<String, ConversionConfig.SectionParameters> sections = new HashMap<>();
    
    ConversionConfig.SectionParameters sectionA = new ConversionConfig.SectionParameters();
    sectionA.setFieldFixedLengths("10,5,3");
    sectionA.setFieldSeparator(null); // assuming null is intentional
    sectionA.setAddHeaderLine(1);
    sections.put("nameA", sectionA);
    
    ConversionConfig.SectionParameters sectionB = new ConversionConfig.SectionParameters();
    sectionB.setFieldSeparator(";");
    sectionB.setAddHeaderLine(2);
    sections.put("nameB", sectionB);
    
    ConversionConfig.SectionParameters sectionC = new ConversionConfig.SectionParameters();
    sectionC.setFieldSeparator(",");
    sectionC.setAddHeaderLine(3);
    sectionC.setHeaderLine("name1-value1");
    sectionC.setBeginSeparator("'");
    sectionC.setEndSeparator("'");
    sections.put("nameC", sectionC);

    config.setSectionParameters(sections);

    return config;
}
 ```

Result:
```txt
value     valueval
value
value
value;value5
'value,value,value''value12321312,value421421,value32532'
```