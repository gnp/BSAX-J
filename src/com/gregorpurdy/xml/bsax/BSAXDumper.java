/*
 * BSAXDumper.java
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

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * This class reads in a BSAX stream and dumps a textual representation
 * of it to System.out. Its intended use is as a debugging aid.
 * 
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt;
 *         http://www.gregorpurdy.com/gregor/
 * @version $Id: BSAXReader.java 1925 2005-01-31 00:57:49Z gregor $
 */
public class BSAXDumper {
  
  private InputStream stream = null;
  
  /**
   * @param attrs
   * @param i
   * @throws SAXException
   */
  private void doOpAttribute(int i) throws SAXException {
    int attrUri = readInt();
    int attrLocalName = readInt();
    int attrQName = readInt();
    int attrType = readInt();
    int attrValue = readInt();
   
    System.out.println("ATTR(" + attrUri + ", " + attrLocalName + ", " + attrQName + ", " + attrType + ", " + attrValue + ") /* " + i + " */");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpCharacters() throws SAXException {
    int characters = BSAXUtil.readInt(stream);
    
    System.out.println("CHAR(" + characters + ")");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpEndDocument() throws SAXException {
    System.out.println("END_DOC()");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpEndElement() throws SAXException {
    int uri = readInt();
    int localName = readInt();
    int qName = readInt();
    
    System.out.println("END_ELEMENT(" + uri + ", " + localName + ", " + qName + ")");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpEndPrefixMapping() throws SAXException {
    int prefix = readInt();
    
    System.out.println("END_PREFIX_MAPPING(" + prefix + ")");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpIgnorableWhitespace() throws SAXException {
    int characters = readInt();
    
    System.out.println("IGNORABLE_WHITESPACE(" + characters + ")");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpProcessingInstruction() throws SAXException {
    int target = readInt();
    int data = readInt();
    
    System.out.println("PROCESSING_INSTRUCTION(" + target + ", " + data + ")");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpSkippedEntity() throws SAXException {
    int name = readInt();
    
    System.out.println("SKIPPED_ENTITY(" + name + ")");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpStartDocument() throws SAXException {
    System.out.println("START_DOCUMENT()");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpStartElement() throws SAXException {
    int uri = readInt();
    int localName = readInt();
    int qName = readInt();
    
    int attributeCount = BSAXUtil.readInt(stream);
        
    System.out.println("START_ELEMENT(" + uri + ", " + localName + ", " + qName + ") /* " + attributeCount + " attributes */");

    for (int i = 0; i < attributeCount; i++) {
      int opCode;
      
      while ((opCode = BSAXUtil.readInt(stream)) == BSAXConstants.OP_STRING) {
        doOpString();
      }
      
      if (opCode != BSAXConstants.OP_ATTRIBUTE) {
        throw new SAXException("Illegal op code " + opCode
            + " while reading attributes for start-element operation");
      }
      
      doOpAttribute(i);
    }
  }
  
  /**
   * @throws SAXException
   */
  private void doOpStartPrefixMapping() throws SAXException {
    int prefix = readInt();
    int uri = readInt();
    
    System.out.println("START_PREFIX_MAPPING(" + prefix + ", " + uri + ")");
  }
  
  /**
   * @throws SAXException
   */
  private void doOpString() throws SAXException {
    int stringId = BSAXUtil.readInt(stream);
    
    String stringValue = BSAXUtil.readString(stream);
    
    System.out.println("STR(" + stringId + ", /* " + stringValue.length() + " characters */)");
  }
  
  /**
   * @param stream
   * @throws IOException
   * @throws SAXException
   */
  public void parse(InputStream stream) throws IOException, SAXException {
    try {
      this.stream = stream;
      
      //
      // Check the input stream for the Binary SAX magic pattern:
      //
      
      byte[] magic = BSAXConstants.MAGIC; // Make a copy.
      
      if (stream.read(magic) != BSAXConstants.MAGIC.length) {
        throw new SAXException(
        "Not enough bytes in the stream to read in a Binary SAX magic byte pattern");
      }
      
      for (int i = 0; i < BSAXConstants.MAGIC.length; i++) {
        if (magic[i] != BSAXConstants.MAGIC[i]) {
          throw new SAXException(
          "Input stream's magic initial bytes don't match the Binary SAX magic byte pattern");
        }
      }
      
      System.out.println("MAGIC()");
      
      //
      // Make sure the version number is a known value:
      //
      
      int version = BSAXUtil.readInt(stream);
      
      if (version != BSAXConstants.VERSION) {
        throw new SAXException("Input stream's Binary SAX version number was "
            + version + " (expected " + BSAXConstants.VERSION + ")");
      }
      
      System.out.println("VERSION(" + version + ")");
      
      //
      // Make sure the maximum string table size is reasonable:
      //
      
      int maxStringTableSize = BSAXUtil.readInt(stream);
      
      if ((maxStringTableSize > 0)
          && (maxStringTableSize < BSAXConstants.MINIMUM_STRING_TABLE_SIZE)) {
        throw new SAXException(
            "Maximum string table size must be zero, or at least "
            + BSAXConstants.MINIMUM_STRING_TABLE_SIZE);
      }
      
      System.out.println("MAX_STRING_TABLE_SIZE(" + maxStringTableSize + ")");
      
      //
      // Process the stream's opcodes:
      //
      
      while (true) {
        final boolean allowEof = true;
        int opCode = BSAXUtil.readInt(stream, allowEof);
        
        if (opCode == -1) {
          break;
        }
                
        if (opCode == BSAXConstants.OP_STRING) {
          doOpString();
        } else if (opCode == BSAXConstants.OP_START_DOCUMENT) {
          doOpStartDocument();
        } else if (opCode == BSAXConstants.OP_END_DOCUMENT) {
          doOpEndDocument();
        } else if (opCode == BSAXConstants.OP_START_ELEMENT) {
          doOpStartElement();
        } else if (opCode == BSAXConstants.OP_ATTRIBUTE) {
          throw new SAXException(
          "Cannot define an attribute outside a start-element operation");
        } else if (opCode == BSAXConstants.OP_END_ELEMENT) {
          doOpEndElement();
        } else if (opCode == BSAXConstants.OP_CHARACTERS) {
          doOpCharacters();
        } else if (opCode == BSAXConstants.OP_IGNORABLE_WHITESPACE) {
          doOpIgnorableWhitespace();
        } else if (opCode == BSAXConstants.OP_START_PREFIX_MAPPING) {
          doOpStartPrefixMapping();
        } else if (opCode == BSAXConstants.OP_END_PREFIX_MAPPING) {
          doOpEndPrefixMapping();
        } else if (opCode == BSAXConstants.OP_PROCESSING_INSTRUCTION) {
          doOpProcessingInstruction();
        } else if (opCode == BSAXConstants.OP_SKIPPED_ENTITY) {
          doOpSkippedEntity();
        } else {
          throw new SAXException("Unrecognized Binary SAX opcode " + opCode);
        }
      }
    } finally {
      this.stream = null;
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
   */
  public void parse(InputSource input) throws IOException, SAXException {
    parse(input.getByteStream());
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#parse(java.lang.String)
   */
  public void parse(String systemId) throws IOException, SAXException {
    parse(new InputSource(systemId));
  }
  
  /**
   * @return
   * @throws SAXException
   */
  private int readInt() throws SAXException {
    return BSAXUtil.readInt(stream);
  }
  
}
