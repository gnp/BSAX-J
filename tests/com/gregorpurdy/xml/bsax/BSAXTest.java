/*
 * BSAXTest.java
 * 
 *  Copyright 2005 Gregor N. Purdy. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.gregorpurdy.xml.bsax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.gregorpurdy.xml.bsax.BSAXUtil;

/**
 * @author gregor
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BSAXTest {
  
  private static final int TRIAL_COUNT = 500;
  
  public static void main(String[] args) throws Exception {
  	System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");
  	
    String[] files = {
      "com/gregorpurdy/xml/bsax/personnel.xml",
//      "com/gregorpurdy/xml/bsax/book.xml"
    };
    
    for (int i = 0; i < files.length; i++) {
      System.out.println("Reading XML file '" + files[i] +"' and converting to BSAX...");

      InputStream xmlInput = ClassLoader.getSystemResourceAsStream(files[i]);
      
      if (xmlInput == null) {
        throw new Exception("Could not locate the input file on the class path!");
      }
      
      ByteArrayOutputStream bsaxOutput = new ByteArrayOutputStream();
      
      BSAXUtil.convertXmlToBsax(xmlInput, bsaxOutput);
      
      byte[] bsaxData = bsaxOutput.toByteArray();
      
//      System.out.println("Dumping BSAX events...");
      
      ByteArrayInputStream bsaxInput = new ByteArrayInputStream(bsaxData);
//      BSAXUtil.dumpBsax(bsaxInput);
      
      //
      // Convert back from BSAX to XML:
      //
      
      System.out.println("Writing " + bsaxData.length + " bytes of BSAX data back out as XML...");

      bsaxInput = new ByteArrayInputStream(bsaxData);
      ByteArrayOutputStream xmlOutput = new ByteArrayOutputStream();
      
      BSAXUtil.convertBsaxToXml(bsaxInput, xmlOutput);

      byte[] xmlData = xmlOutput.toByteArray();
      
      float sizeFactor = 100.0f * (float)bsaxData.length / (float)xmlData.length;
      
      System.out.println("Wrote " + xmlData.length + " bytes of XML data out based on " + bsaxData.length + " bytes of BSAX data (BSAX is " + sizeFactor + "% the size of the XML data).");
      
      //
      // Benchmark the XML parser method:
      //

      long xmlStartTime = System.currentTimeMillis();

      for (int j = 0; j < TRIAL_COUNT; j++) {
        xmlInput = new ByteArrayInputStream(xmlData);
        BSAXUtil.convertXmlToNothing(xmlInput);
      }
      
      long xmlEndTime = System.currentTimeMillis();
      
      float xmlElapsed = (xmlEndTime - xmlStartTime) / 1000.0f;

      System.out.println("It took " + xmlElapsed + " seconds to process " + TRIAL_COUNT + " XML streams.");      
      
      //
      // Benchmark the BSAX method:
      //
      
      long bsaxStartTime = System.currentTimeMillis();
      
      for (int j = 0; j < TRIAL_COUNT; j++) {
        bsaxInput = new ByteArrayInputStream(bsaxData);
        BSAXUtil.convertBsaxToNothing(bsaxInput);
      }
      
      long bsaxEndTime = System.currentTimeMillis();

      float bsaxElapsed = (bsaxEndTime - bsaxStartTime) / 1000.0f;
      
      System.out.println("It took " + bsaxElapsed + " seconds to process " + TRIAL_COUNT + " BSAX streams.");
      
      //
      // Show the final result:
      //
      
      float speedFactor = xmlElapsed / bsaxElapsed;
      
      System.out.println("BSAX processing is " + speedFactor + " times faster than XML parsing for this input.");
    }
  }
  
}
