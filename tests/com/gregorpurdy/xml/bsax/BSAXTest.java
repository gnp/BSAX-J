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
import java.io.OutputStream;

import com.gregorpurdy.xml.bsax.BSAXUtil;

/**
 * @author gregor
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BSAXTest {
  
  public static void main(String[] args) throws Exception {
    String[] files = {
      "com/gregorpurdy/xml/bsax/personnel.xml"
    };
    
    for (int i = 0; i < files.length; i++) {
      System.out.println("Reading XML file '" + files[i] +"' and converting to BSAX...");

      InputStream xmlInput = ClassLoader.getSystemResourceAsStream(files[i]);
      
      if (xmlInput == null) {
        throw new Exception("Could not locate the input file on the class path!");
      }
      
      ByteArrayOutputStream bsaxOutput = new ByteArrayOutputStream();
      
      BSAXUtil.convertXmlToBsax(xmlInput, bsaxOutput);
      
      byte[] data = bsaxOutput.toByteArray();
      
      System.out.println("Writing BSAX back out as XML...");

      ByteArrayInputStream bsaxInput = new ByteArrayInputStream(data);
      OutputStream xmlOutput = System.out;
      
      BSAXUtil.convertBsaxToXml(bsaxInput, xmlOutput);
    }
  }
  
}
