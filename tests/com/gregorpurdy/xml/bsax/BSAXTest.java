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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.gregorpurdy.xml.bsax.SAXWriter;
import com.gregorpurdy.xml.sax.BSAXReader;

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

      byte[] data = convertXmlToBsax(files[i]);

      System.out.println("Writing BSAX back out as XML...");

      convertBsaxToXml(data);
    }
  }

  /**
   * @param data
   * @throws UnsupportedEncodingException
   * @throws IOException
   * @throws SAXException
   */
  private static void convertBsaxToXml(byte[] data) throws UnsupportedEncodingException, IOException, SAXException {
    sax.Writer writer = new sax.Writer();
    writer.setOutput(System.out, "UTF-8");

    BSAXReader reader = new BSAXReader();
    reader.setContentHandler(writer);
    reader.parse(new ByteArrayInputStream(data));
  }

  /**
   * @return
   * @throws SAXException
   * @throws Exception
   * @throws IOException
   */
  private static byte[] convertXmlToBsax(String resourceName) throws SAXException, Exception, IOException {
    XMLReader xr = XMLReaderFactory.createXMLReader();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    SAXWriter handler = new SAXWriter(output);
    
    xr.setContentHandler(handler);
    
    InputStream stream = ClassLoader.getSystemResourceAsStream(resourceName);
    
    if (stream == null) {
      throw new Exception("Could not locate the input file on the class path!");
    }
    
    InputSource source = new InputSource(stream);
    xr.parse(source);
    
    return output.toByteArray();
  }
  
}
