package com.gregorpurdy.xml.bsax;

/**
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt; http://www.gregorpurdy.com/gregor
 * @version $Id; $
 */
public final class BSAXConstants {

  public static final byte[] MAGIC = { 0x42, 0x53, 0x41, 0x58 }; // "BSAX" in ASCII
  
  public static final int VERSION = 1;
  
  public static final int UNLIMITED_STRING_TABLE_SIZE = 0;
  public static final int MINIMUM_STRING_TABLE_SIZE = 7;
  
  public static final int NULL_STRING_ID = 0;
  public static final int EMPTY_STRING_ID = 1;
  
  public static final int OP_STRING = 0; // int for length + utf-8 encoded string
  public static final int OP_START_DOCUMENT = 1; // NO ARGS
  public static final int OP_END_DOCUMENT = 2; // NO ARGS
  public static final int OP_START_ELEMENT = 3; // 4 args: uri, localName, qName, #attrs (5 each)
  public static final int OP_ATTRIBUTE = 4; // 5 args: uri, localName, qName, type, value
  public static final int OP_END_ELEMENT = 5; // 3 args: uri, localName, qName
  public static final int OP_CHARACTERS = 6; // 1 arg: string
  public static final int OP_IGNORABLE_WHITESPACE = 7; // 1 arg: string
  public static final int OP_START_PREFIX_MAPPING = 8; // 2 args: prefix, uri
  public static final int OP_END_PREFIX_MAPPING = 9; // 1 arg: prefix
  public static final int OP_NOTATION_DECL = 10; // 3 args: name, publidId, systemId
  public static final int OP_PROCESSING_INSTRUCTION = 11; // 2 args: target, data
  public static final int OP_SKIPPED_ENTITY = 12; // 1 arg: name
  public static final int OP_UNPARSED_ENTITY_DECL = 13; //  4 args: name, publicId, systemId, notationName
  
  private BSAXConstants() { }
  
}
