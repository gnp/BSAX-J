/*
 *  Copyright 2005 Gregor N. Purdy
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * This class lives in the *.xml.bsax package in analogy with
 * the org.xml.sax.XMLReader class, which reads XML and produces
 * SAX events: what it reads as input is in the class name, and
 * what it produces as output is in the package name.
 * 
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt;, http://www.gregorpurdy.com/gregor
 * @version $Id$
 */
public class SAXWriter extends DefaultHandler {
  
  /**
   * @param out
   */
  public SAXWriter(OutputStream out) {
    super();
    this.out = out;
  }
  
  
  private static final int NULL_STRING_ID = 0;

  private static final int EMPTY_STRING_ID = 1;
  
  private static final int FIRST_FREE_STRING_ID = 2;

  private static int nextStringId = FIRST_FREE_STRING_ID;
  
  private OutputStream out = null;
  
  private Map<String, Integer> stringMap = new HashMap<String, Integer>();
  
  /**
   * Generate a string id for the character sequence and emit
   * the binary code for it.
   * 
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char[] ch, int start, int length) throws SAXException {
    int stringId = getStringId(ch, start, length);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_CHARACTERS);
    BSAXUtil.writeInt(out, stringId);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  public void endDocument() throws SAXException {
    BSAXUtil.writeInt(out, BSAXConstants.OP_END_DOCUMENT);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String localName, String qName)
  throws SAXException {
    int uriId = getStringId(uri);
    int localNameId = getStringId(localName);
    int qNameId = getStringId(qName);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_END_ELEMENT);
    BSAXUtil.writeInt(out, uriId);
    BSAXUtil.writeInt(out, localNameId);
    BSAXUtil.writeInt(out, qNameId);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
   */
  public void endPrefixMapping(String prefix) throws SAXException {
    int prefixId = getStringId(prefix);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_END_PREFIX_MAPPING);
    BSAXUtil.writeInt(out, prefixId);
  }
  
  /**
   * @param ch
   * @param start
   * @param length
   * @return
   * @throws SAXException
   */
  private int getStringId(char[] ch, int start, int length) throws SAXException {
    if (start < 0) {
      throw new IllegalArgumentException("Start must be non-negative");
    }
    
    if (length < 0) {
      throw new IllegalArgumentException("Length must be non-negative");
    }
    
    if (length == 0) {
      return EMPTY_STRING_ID;
    }
    
    String temp = new String(ch, start, length);
    
    return getStringId(temp);
  }
  
  /**
   * Determines the integer id (index) for the string, returning
   * the existing id if the string has been seen before, or creating
   * a new id-string mapping, recording that mapping for later use,
   * and writing the definition to the output stream in the format:
   * 
   *   * STRING DEFINITION OPERATOR CODE
   *   * Id (index) of the string
   *   * Length of the string (number of bytes of its UTF-8 encoding)
   *   * The UTF-8 encoding of the string
   * 
   * For simple operation, writing the id of the string is actually
   * redundant, since you can consider the id implied by the ordering
   * of the STRING DEFINITION opcodes in the input stream on reading.
   * But, by having the format include an id here, a producer of a
   * stream can build a stream that requires a smaller string table
   * on reading by reusing string table entries for strings that are
   * used fewer times. Also, a stream producer could allocate more
   * frequently used strings to lower indexes, since indexes less than
   * 128 take only one byte to encode.
   * 
   * A *very* simple stream producer could reuse 7 string table entries,
   * since the most string table entries needed for any operator is 5 (for
   * the attribute operator used as part of the start element operator),
   * and index zero is for null and index one is for empty string.
   * 
   * TODO: Subclasses should implement the various policies.
   * 
   * @param string The string to find the id (index) for.
   * @return The integer id (index) for the string.
   * @throws SAXException
   */
  private int getStringId(String string) throws SAXException {
    if (string == null) {
      return NULL_STRING_ID;
    }
    
    if (string.equals("")) {
      return EMPTY_STRING_ID;
    }
    
    Integer id = stringMap.get(string);
    
    if (id == null) {
      id = new Integer(nextStringId++);
      
      BSAXUtil.writeInt(out, BSAXConstants.OP_STRING);
      BSAXUtil.writeInt(out, id.intValue());
      BSAXUtil.writeString(out, string);
      
      stringMap.put(string, id);
    }
    
    return id.intValue();
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
   */
  public void ignorableWhitespace(char[] ch, int start, int length)
  throws SAXException {
    int wsId = getStringId(ch, start, length);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_IGNORABLE_WHITESPACE);
    BSAXUtil.writeInt(out, wsId);
  }
  
  /**
   * Get ready to process a new input.
   * 
   * TODO: Should we have one with a starting string table? It could be derived
   * automatically from a DTD or schema...
   */
  public void init() {
    stringMap.clear();
    nextStringId = 1;
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.DTDHandler#notationDecl(java.lang.String, java.lang.String, java.lang.String)
   */
  public void notationDecl(String name, String publicId, String systemId)
  throws SAXException {
    int nameId = getStringId(name);
    int publicIdId = getStringId(publicId);
    int systemIdId = getStringId(systemId);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_NOTATION_DECL);
    BSAXUtil.writeInt(out, nameId);
    BSAXUtil.writeInt(out, publicIdId);
    BSAXUtil.writeInt(out, systemIdId);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
   */
  public void processingInstruction(String target, String data)
  throws SAXException {
    int targetId = getStringId(target);
    int dataId = getStringId(data);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_PROCESSING_INSTRUCTION);
    BSAXUtil.writeInt(out, targetId);
    BSAXUtil.writeInt(out, dataId);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
   */
  public InputSource resolveEntity(String publicId, String systemId)
  throws IOException, SAXException {
    // TODO Auto-generated method stub (what should we do here?)
    return super.resolveEntity(publicId, systemId);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
   */
  public void setDocumentLocator(Locator locator) {
    // TODO Auto-generated method stub (what should we do here?)
    super.setDocumentLocator(locator);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
   */
  public void skippedEntity(String name) throws SAXException {
    int nameId = getStringId(name);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_SKIPPED_ENTITY);
    BSAXUtil.writeInt(out, nameId);
  }
  
  /**
   * Write the magic byte sequence for Binary SAX files ("BSAX" in ASCII)
   * to the stream, followed by a UTF-8 encoded integer representing the
   * version of the BSAX, followec by a UTF-8 encoded integer representing
   * the maximum size of the string table (zero, or a number at least 7 --
   * zero means unlimited).
   * 
   * @see org.xml.sax.ContentHandler#startDocument()
   */
  public void startDocument() throws SAXException {
    try {
      out.write(BSAXConstants.MAGIC);
    } catch (IOException e) {
      throw new SAXException(e);
    }
    
    BSAXUtil.writeInt(out, BSAXConstants.VERSION);
    BSAXUtil.writeInt(out, BSAXConstants.UNLIMITED_STRING_TABLE_SIZE);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_START_DOCUMENT);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    int uriId = getStringId(uri);
    int localNameId = getStringId(localName);
    int qNameId = getStringId(qName);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_START_ELEMENT);
    BSAXUtil.writeInt(out, uriId);
    BSAXUtil.writeInt(out, localNameId);
    BSAXUtil.writeInt(out, qNameId);
    
    BSAXUtil.writeInt(out, attributes.getLength());
    
    for (int i = 0; i < attributes.getLength(); i++) {
      int attrUriId = getStringId(attributes.getURI(i));
      int attrLocalNameId = getStringId(attributes.getLocalName(i));
      int attrQNameId = getStringId(attributes.getQName(i));
      int attrTypeId = getStringId(attributes.getType(i));
      int attrValueId = getStringId(attributes.getValue(i));
      
      BSAXUtil.writeInt(out, BSAXConstants.OP_ATTRIBUTE);
      BSAXUtil.writeInt(out, attrUriId);
      BSAXUtil.writeInt(out, attrLocalNameId);
      BSAXUtil.writeInt(out, attrQNameId);
      BSAXUtil.writeInt(out, attrTypeId);
      BSAXUtil.writeInt(out, attrValueId);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
   */
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    int prefixId = getStringId(prefix);
    int uriId = getStringId(uri);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_START_PREFIX_MAPPING);
    BSAXUtil.writeInt(out, prefixId);
    BSAXUtil.writeInt(out, uriId);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public void unparsedEntityDecl(String name, String publicId, String systemId,
      String notationName) throws SAXException {
    int nameId = getStringId(name);
    int publicIdId = getStringId(publicId);
    int systemIdId = getStringId(systemId);
    int notationNameId = getStringId(notationName);
    
    BSAXUtil.writeInt(out, BSAXConstants.OP_UNPARSED_ENTITY_DECL);
    BSAXUtil.writeInt(out, nameId);
    BSAXUtil.writeInt(out, publicIdId);
    BSAXUtil.writeInt(out, systemIdId);
    BSAXUtil.writeInt(out, notationNameId);
  }
  
}
