package com.gregorpurdy.xml.bsax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

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
public class BinarySAXTest {

  public static void main(String[] args) throws SAXException, IOException {
    String[] args2 = {
     "/home/gregor/workspace/BinaryXML/test/com/gregorpurdy/xml/bsax/personnel.xml"
    };
    
    for (int i = 0; i < args2.length; i++) {
      System.out.println("Reading XML file '" + args2[i] +"' and converting to BSAX...");
      
      XMLReader xr = XMLReaderFactory.createXMLReader();
      
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      
      SAXWriter handler = new SAXWriter(output);

      xr.setContentHandler(handler);
      
      FileReader r = new FileReader(args2[i]);

      xr.parse(new InputSource(r));

      System.out.println("Writing BSAX back out as XML...");

      byte [] data = output.toByteArray();
      
      InputStream input = new ByteArrayInputStream(data);
      
      BSAXReader reader = new BSAXReader();
      
      sax.Writer writer = new sax.Writer();
      writer.setOutput(System.out, "UTF-8");
      
      reader.setContentHandler(writer);
      
      reader.parse(input);
    }
    
    
  }
  
}
