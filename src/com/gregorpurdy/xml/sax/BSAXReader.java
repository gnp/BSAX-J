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
package com.gregorpurdy.xml.sax;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import com.gregorpurdy.xml.bsax.BSAXConstants;
import com.gregorpurdy.xml.bsax.BSAXUtil;

/**
 * This class lives in the *.xml.sax package in analogy with
 * the org.xml.sax.XMLReader class, which reads XML and produces
 * SAX events. What it reads as input is in the class name, and
 * what it produces as output is in the package name.
 * 
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt;
 *         http://www.gregorpurdy.com/gregor/
 * @version $Id$
 */
public class BSAXReader implements XMLReader {
  
  private ContentHandler contentHandler;
  
  private DTDHandler dtdHandler;
  
  private EntityResolver entityResolver;
  
  private ErrorHandler errorHandler;
  
  private int maxStringTableSize = BSAXConstants.UNLIMITED_STRING_TABLE_SIZE;
  
  private InputStream stream = null;
  
  private List stringTable = null;
  
  /**
   * @param attrs
   * @param i
   * @throws SAXException
   */
  private void doOpAttribute(AttributesImpl attrs, int i) throws SAXException {
    String attrUri = readString();
    String attrLocalName = readString();
    String attrQName = readString();
    String attrType = readString();
    String attrValue = readString();
    
    attrs.addAttribute(attrUri, attrLocalName, attrQName, attrType,
        attrValue);
  }
  
  /**
   * @throws SAXException
   */
  private void doOpCharacters() throws SAXException {
    String characters = readString();
    
    if (characters != null) {
      contentHandler.characters(characters.toCharArray(), 0, characters.length());
    }
  }
  
  /**
   * @throws SAXException
   */
  private void doOpEndDocument() throws SAXException {
    contentHandler.endDocument();
  }
  
  /**
   * @throws SAXException
   */
  private void doOpEndElement() throws SAXException {
    String uri = readString();
    String localName = readString();
    String qName = readString();
    
    contentHandler.endElement(uri, localName, qName);
  }
  
  /**
   * @throws SAXException
   */
  private void doOpEndPrefixMapping() throws SAXException {
    String prefix = readString();
    
    contentHandler.endPrefixMapping(prefix);
  }
  
  /**
   * @throws SAXException
   */
  private void doOpIgnorableWhitespace() throws SAXException {
    String characters = readString();
    
    contentHandler.ignorableWhitespace(characters.toCharArray(), 0, characters
        .length());
  }
  
  /**
   * @throws SAXException
   */
  private void doOpProcessingInstruction() throws SAXException {
    String target = readString();
    String data = readString();
    
    contentHandler.processingInstruction(target, data);
  }
  
  /**
   * @throws SAXException
   */
  private void doOpSkippedEntity() throws SAXException {
    String name = readString();
    
    contentHandler.skippedEntity(name);
  }
  
  /**
   * @throws SAXException
   */
  private void doOpStartDocument() throws SAXException {
    contentHandler.startDocument();
  }
  
  /**
   * @throws SAXException
   */
  private void doOpStartElement() throws SAXException {
    String uri = readString();
    String localName = readString();
    String qName = readString();
    
    int attributeCount = BSAXUtil.readInt(stream);
    
    AttributesImpl attrs = new AttributesImpl();
    
    for (int i = 0; i < attributeCount; i++) {
      int opCode;
      
      while ((opCode = BSAXUtil.readInt(stream)) == BSAXConstants.OP_STRING) {
        doOpString();
      }
      
      if (opCode != BSAXConstants.OP_ATTRIBUTE) {
        throw new SAXException("Illegal op code " + opCode
            + " while reading attributes for start-element operation");
      }
      
      doOpAttribute(attrs, i);
    }
    
    contentHandler.startElement(uri, localName, qName, attrs);
  }
  
  /**
   * @throws SAXException
   */
  private void doOpStartPrefixMapping() throws SAXException {
    String prefix = readString();
    String uri = readString();
    
    contentHandler.startPrefixMapping(prefix, uri);
  }
  
  /**
   * @throws SAXException
   */
  private void doOpString() throws SAXException {
    int stringId = BSAXUtil.readInt(stream);
    
    if (stringId < 2) {
      throw new SAXException("Cannot modify string table entry 0 or 1");
    }
    
    if ((maxStringTableSize != 0) && (stringId >= maxStringTableSize)) {
      throw new SAXException(
          "Cannot create a string table entry beyond the end of the fixed string tables size of "
          + maxStringTableSize + " for this stream");
    }
    
    String stringValue = BSAXUtil.readString(stream);
    
    //
    // Store the string in the string table. If the table size is
    // unlimited, then it is only allowed to overwrite existing
    // entries or tack one on the end. If the table size is fixed
    // then writing anywhere in the string table is permitted (with
    // auto setting of null values for any intervening entries).
    //
    
    if (maxStringTableSize == BSAXConstants.UNLIMITED_STRING_TABLE_SIZE) {
      if (stringId < stringTable.size()) {
        stringTable.set(stringId, stringValue);
      }
      else if (stringId == stringTable.size()) {
        stringTable.add(stringValue);
      }
      else {
        throw new SAXException("Stream with unlimited string table size attempted to create string entry more than one position beyond the end of the string table");
      }
    }
    else {
      while (stringId >= stringTable.size()) {
        stringTable.add(null);
      }
      
      stringTable.set(stringId, stringValue);
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#getContentHandler()
   */
  public ContentHandler getContentHandler() {
    return contentHandler;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#getDTDHandler()
   */
  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#getEntityResolver()
   */
  public EntityResolver getEntityResolver() {
    return entityResolver;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#getErrorHandler()
   */
  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }
  
  /**
   * This class doesn't support any SAX "features".
   * 
   * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
   */
  public boolean getFeature(String name) throws SAXNotRecognizedException,
  SAXNotSupportedException {
    throw new SAXNotRecognizedException("Feature '" + name + "'");
  }
  
  /**
   * This class doesn't support any SAX "properties".
   * 
   * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
   */
  public Object getProperty(String name) throws SAXNotRecognizedException,
  SAXNotSupportedException {
    throw new SAXNotRecognizedException("Property '" + name + "'");
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
      
      //
      // Make sure the version number is a known value:
      //
      
      int version = BSAXUtil.readInt(stream);
      
      if (version != BSAXConstants.VERSION) {
        throw new SAXException("Input stream's Binary SAX version number was "
            + version + " (expected " + BSAXConstants.VERSION + ")");
      }
      
      //
      // Make sure the maximum string table size is reasonable:
      //
      
      maxStringTableSize = BSAXUtil.readInt(stream);
      
      if ((maxStringTableSize > 0)
          && (maxStringTableSize < BSAXConstants.MINIMUM_STRING_TABLE_SIZE)) {
        throw new SAXException(
            "Maximum string table size must be zero, or at least "
            + BSAXConstants.MINIMUM_STRING_TABLE_SIZE);
      }
      
      stringTable = new ArrayList(maxStringTableSize);
      
      stringTable.add(null); // Index zero
      stringTable.add(""); // Index one
      
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
   * Reads a string from the stream by reading the string id (index) and then
   * looking that string up in the string table.
   * 
   * @return
   * @throws SAXException
   */
  private String readString() throws SAXException {
    int stringId = BSAXUtil.readInt(stream);
    
    if (stringId == 0) {
      return null;
    } else if (stringId == 1) {
      return "";
    } else if ((maxStringTableSize != BSAXConstants.UNLIMITED_STRING_TABLE_SIZE)
        && (stringId >= maxStringTableSize)) {
      throw new SAXException(
      "Illegal reference to string index beyond the end of the fixed-size string table");
    } else if (stringId >= stringTable.size()) {
      throw new SAXException(
      "Illegal reference to string index beyond the current end of the variable-size string table");
    }
    
    return (String) stringTable.get(stringId);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
   */
  public void setContentHandler(ContentHandler handler) {
    this.contentHandler = handler;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
   */
  public void setDTDHandler(DTDHandler handler) {
    this.dtdHandler = handler;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
   */
  public void setEntityResolver(EntityResolver resolver) {
    this.entityResolver = resolver;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
   */
  public void setErrorHandler(ErrorHandler handler) {
    this.errorHandler = handler;
  }
  
  /**
   * This class doesn't support any SAX "features".
   * 
   * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
   */
  public void setFeature(String name, boolean value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    throw new SAXNotRecognizedException("Feature '" + name + "'");
  }
  
  /**
   * This class does't support any SAX "properties".
   * 
   * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
   */
  public void setProperty(String name, Object value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    throw new SAXNotRecognizedException("Property '" + name + "'");
  }
  
}
