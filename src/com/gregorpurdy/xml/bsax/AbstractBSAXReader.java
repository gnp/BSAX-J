/*
 * AbstractBSAXReader.java
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
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt;
 *         http://www.gregorpurdy.com/gregor/
 * @version $Id: $
 */
public abstract class AbstractBSAXReader {
  
  private InputStream stream = null;

  private byte[] magic = BSAXConstants.MAGIC;
  
  protected final byte[] getMagic() {
    return magic;
  }
  
  private int version = BSAXConstants.VERSION_UNKNOWN;
  
  protected int getVersion() {
    return version;
  }
  
  private int maxStringTableSize = BSAXConstants.UNLIMITED_STRING_TABLE_SIZE;
  
  protected int getMaxStringTableSize() {
    return maxStringTableSize;
  }
  
  /**
   * Keeps track of the current string table size, either a constant number
   * for the stream, or the number of string table entries used so far when
   * the string table size is unlimited.
   */
  private int currentStringTableSize = BSAXConstants.STARTING_STRING_TABLE_SIZE;
  
  /**
   * No arguments are needed because the magic byte sequence, version
   * and maximum string table size are already stored in private instance
   * variables, and accessible via getters.
   */
  protected abstract void doStartStream();
  
  /**
   * @param attrs
   * @param i
   * @throws SAXException
   */
  protected abstract void doOpAttribute(int i, int attrUri, int attrLocalName, int attrQName, int attrType, int attrValue)
  throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpCharacters(int characters)
  throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpEndDocument() throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpEndElement(int uri, int localName, int qName) throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpEndPrefixMapping(int prefix) throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpIgnorableWhitespace(int characters) throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpProcessingInstruction(int target, int data) throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpSkippedEntity(int name) throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpStartDocument() throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpStartElement(int uri, int localName, int qName, int attributeCount)
  throws SAXException;
  
  /**
   * @throws SAXException
   */
  protected abstract void doOpStartElementFinalize() throws SAXException;

  /**
   * @throws SAXException
   */
  protected abstract void doOpStartPrefixMapping(int prefix, int uri) throws SAXException;  

  /**
   * @throws SAXException
   */
  protected abstract void doOpString(int id, String value) throws SAXException;
  
  /**
   * @param stream
   * @throws IOException
   * @throws SAXException
   */
  public void parse(InputStream inputStream) throws IOException, SAXException {
    try {
      this.stream = inputStream;
      
      //
      // Check the input stream for the Binary SAX magic pattern:
      //
      
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
      
      version = BSAXUtil.readInt(stream);
      
      if (version != BSAXConstants.VERSION_LATEST) {
        throw new SAXException("Input stream's Binary SAX version number was "
            + version + " (expected " + BSAXConstants.VERSION_LATEST + ")");
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

      if (maxStringTableSize == BSAXConstants.UNLIMITED_STRING_TABLE_SIZE) {
        currentStringTableSize = BSAXConstants.STARTING_STRING_TABLE_SIZE;
      }
      else {
        currentStringTableSize = maxStringTableSize;
      }
      
      //
      // Give the subclass a chance to do something with the header information:
      //
      
      doStartStream();
      
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
          internalOpString();
        } else if (opCode == BSAXConstants.OP_START_DOCUMENT) {
          doOpStartDocument();
        } else if (opCode == BSAXConstants.OP_END_DOCUMENT) {
          doOpEndDocument();
        } else if (opCode == BSAXConstants.OP_START_ELEMENT) {
          internalOpStartElement();
        } else if (opCode == BSAXConstants.OP_ATTRIBUTE) {
          throw new SAXException(
          "Cannot define an attribute outside a start-element operation");
        } else if (opCode == BSAXConstants.OP_END_ELEMENT) {
          int uri = readInt();
          int localName = readInt();
          int qName = readInt();
          
          doOpEndElement(uri, localName, qName);
        } else if (opCode == BSAXConstants.OP_CHARACTERS) {
          int characters = BSAXUtil.readInt(stream);

          doOpCharacters(characters);
        } else if (opCode == BSAXConstants.OP_IGNORABLE_WHITESPACE) {
          int characters = readInt();

          doOpIgnorableWhitespace(characters);
        } else if (opCode == BSAXConstants.OP_START_PREFIX_MAPPING) {
          int prefix = readInt();
          int uri = readInt();
          
          doOpStartPrefixMapping(prefix, uri);
        } else if (opCode == BSAXConstants.OP_END_PREFIX_MAPPING) {
          int prefix = readInt();
          
          doOpEndPrefixMapping(prefix);
        } else if (opCode == BSAXConstants.OP_PROCESSING_INSTRUCTION) {
          int target = readInt();
          int data = readInt();
          
          doOpProcessingInstruction(target, data);
        } else if (opCode == BSAXConstants.OP_SKIPPED_ENTITY) {
          int name = readInt();

          doOpSkippedEntity(name);
        } else {
          throw new SAXException("Unrecognized Binary SAX opcode " + opCode);
        }
      }
    } finally {
      this.stream = null;
    }
  }

  /**
   * @param stream
   * @throws SAXException
   */
  private void internalOpStartElement() throws SAXException {
    int uri = readInt();
    int localName = readInt();
    int qName = readInt();
    
    int attributeCount = readInt();
    
    doOpStartElement(uri, localName, qName, attributeCount);
    
    for (int i = 0; i < attributeCount; i++) {
      int attrOpCode;
      
      while ((attrOpCode = readInt()) == BSAXConstants.OP_STRING) {
        internalOpString();
      }
      
      if (attrOpCode != BSAXConstants.OP_ATTRIBUTE) {
        throw new SAXException("Illegal op code " + attrOpCode
            + " while reading attributes for start-element operation");
      }
      
      int attrUri = readInt();
      int attrLocalName = readInt();
      int attrQName = readInt();
      int attrType = readInt();
      int attrValue = readInt();
      
      doOpAttribute(i, attrUri, attrLocalName, attrQName, attrType, attrValue);
    }
    
    doOpStartElementFinalize();
  }

  /**
   * @param stream
   * @throws SAXException
   */
  private void internalOpString() throws SAXException {
    int id = BSAXUtil.readInt(stream);
    
    if (id < 2) {
      throw new SAXException("Cannot modify string table entry 0 (null string) or 1 (empty string)");
    }
    
    if ((maxStringTableSize != BSAXConstants.UNLIMITED_STRING_TABLE_SIZE) && (id >= maxStringTableSize)) {
      throw new SAXException(
          "Cannot create a string table entry " + id + ". It is beyond the end of the fixed string tables size of "
          + maxStringTableSize + " for this stream");
    }
    
    String value = BSAXUtil.readString(stream);
    
    if (maxStringTableSize == BSAXConstants.UNLIMITED_STRING_TABLE_SIZE) {
      if (id == currentStringTableSize) {
        currentStringTableSize++;
      }
      else if (id > currentStringTableSize) {
        throw new SAXException("Stream with unlimited string table size attempted to write to a string table entry (index " + id + ") more than one position beyond the end of the string table (" + (currentStringTableSize - 1) + ")");              
      }
    }
    
    doOpString(id, value);
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
