/*
 * UTF8Codec.java
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
package com.gregorpurdy.codec;

/**
 * This class implements an UTF-8 codec per the description
 * given in Section 2 of RFC 2279 (also ISO/IEC 10646). RFC 3629
 * and The Unicode Standard 4.0, section 3.9 define the output of
 * UTF-8 encoding as 1-4 bytes, leaving off the extensions that
 * let the encoding cover the full 31-bit unsigned integer range
 * that the encoding can naturally cover if you follow the scheme
 * to its natural conclusion.
 * 
 * There are other implementations of the UTF-8 scheme, including
 * one built in to Java, but the Java version is the 1-4 byte
 * version, and also encodes the byte value 0x00 as {0xc0, 0x80},
 * which is illegal per the true UTF-8 rules (this case is explicitly
 * tested in the test suite for this class).
 * 
 * 32-bit integer                       1st Byte 2nd Byte 3rd Byte 4th Byte 5th Byte 6th Byte
 * -----------------------------------  -------- -------- -------- -------- -------- --------
 * 00000000 00000000 00000000 0aaaaaaa  0aaaaaaa
 * 00000000 00000000 00000bbb bbaaaaaa  110bbbbb 10aaaaaa
 * 00000000 00000000 ccccbbbb bbaaaaaa  1110cccc 10bbbbbb 10aaaaaa
 * 00000000 000dddcc ccccbbbb bbaaaaaa  11110ddd 10cccccc 10bbbbbb 10aaaaaa
 * 000000ee ddddddcc ccccbbbb bbaaaaaa  111110ee 10dddddd 10cccccc 10bbbbbb 10aaaaaa
 * 0feeeeee ddddddcc ccccbbbb bbaaaaaa  1111110f 10eeeeee 10dddddd 10cccccc 10bbbbbb 10aaaaaa
 * 
 * While the ability to cover such a large range of numbers may
 * not be relevant to the application of encoding code numbers for
 * the characters in a string, it is relevant to representing
 * unsigned integers in a binary data file, since smaller numbers
 * encode into fewer bytes in a relatively simple way. This
 * compaction is great for saving space without resorting to
 * compression (which you can always apply after the fact if you
 * want to for greater reduction is space utilization).
 * 
 * The byte values 0xfe and 0xff never appear in a valid UTF-8
 * sequence (the broader set 0xc0, 0xc1, 0xf5-0xff don't appear
 * in the four-byte restricted UTF-8 of RFC 3629 and Unicode 4.0).
 * 
 * In addition to the mapping given in the table above, one
 * additional restriction is needed. A value must be encoded
 * into the minimum number of bytes per the schemea above for
 * the encoding to be valid. For example, following the rules
 * above, {0xc0, 0x80} would decode as the value 0x00, but that
 * would not be a valid sequence, since 0x00 encodes canonically
 * as 0x00. This matters less for the application to encoding of
 * general integers, and more for the encoding of code points
 * for characters of strings (for security reasons you can read
 * about at http://en.wikipedia.org/wiki/UTF-8).
 * 
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt; http://www.gregorpurdy.com/gregor
 * @version $Id$
 */
public class UTF8Codec {

  private static final int MINIMUM_TWO_BYTE_VALUE = 0x00000080;
  private static final int MINIMUM_THREE_BYTE_VALUE = 0x00000800;
  private static final int MINIMUM_FOUR_BYTE_VALUE = 0x00010000;
  private static final int MINIMUM_FIVE_BYTE_VALUE = 0x00200000;
  private static final int MINIMUM_SIX_BYTE_VALUE = 0x04000000;
  
  /**
   * Return the number of bytes in the UTF-8 encoding of
   * the value. This is used in the encoding process, and
   * also for validation in the decoding process.
   * 
   * @param arg
   * @return The number of bytes in the UTF-8 encoding of <code>arg</code>.
   */
  private static int utf8EncodedLength(int arg) {
    if (arg < MINIMUM_TWO_BYTE_VALUE) {
      return 1;
    }
    else if (arg < MINIMUM_THREE_BYTE_VALUE) {
      return 2;
    }
    else if (arg < MINIMUM_FOUR_BYTE_VALUE) {
      return 3;
    }
    else if (arg < MINIMUM_FIVE_BYTE_VALUE) {
      return 4;
    }
    else if (arg < MINIMUM_SIX_BYTE_VALUE) {
      return 5;
    }
    else {
      return 6;
    }
  }
  
  private static final int VALUE_BITS_PER_CONTINUATION_BYTE = 6;
  
  private static final byte CONTINUATION_BYTE_TEMPLATE = (byte)0x80; // 10xxxxxx
  private static final byte CONTINUATION_BYTE_VALUE_BIT_MASK = 0x3f; // 00111111
  private static final byte CONTINUATION_BYTE_TEMPLATE_BIT_MASK = (byte)0xc0; // 11000000
  
  private static final byte INITIAL_BYTE_VALUE_BIT_MASK[] = {
    0x00, // Not used
    0x7f, // 1-Byte --> 01111111 (7 bits)
    0x1f, // 2-Byte --> 00011111 (5 bits)
    0x0f, // 3-Byte --> 00001111 (4 bits)
    0x07, // 4-Byte --> 00000111 (3 bits)
    0x03, // 5-Byte --> 00000011 (2 bits)
    0x01, // 6-Byte --> 00000001 (1 bit)
  };
  
  private static final byte INITIAL_BYTE_TEMPLATE_BIT_MASK[] = {
          0x00, // Not used
    (byte)0x80, // 1-Byte --> 10000000
    (byte)0xe0, // 2-Byte --> 11100000
    (byte)0xf0, // 3-Byte --> 11110000
    (byte)0xf8, // 4-Byte --> 11111000
    (byte)0xfc, // 5-Byte --> 11111100
    (byte)0xfe, // 6-Byte --> 11111110
  };
  
  private static final byte INITIAL_BYTE_TEMPLATE[] = {
          0x00, // Not used
          0x00, // 1-Byte --> 0xxxxxxx
    (byte)0xc0, // 2-Byte --> 110xxxxx
    (byte)0xe0, // 3-Byte --> 1110xxxx
    (byte)0xf0, // 4-Byte --> 11110xxx
    (byte)0xf8, // 5-Byte --> 111110xx
    (byte)0xfc, // 6-Byte --> 1111110x
  };
  
  /**
   * @param arg
   * @return
   */
  public static byte[] intToUtf8(int arg) {
    if (arg < 0) {
      throw new IllegalArgumentException("Cannot UTF-8 encode a negative number");
    }
    
    int length = utf8EncodedLength(arg);
    
//    if ((length < 0) || (length > 6)) {
//      throw new IllegalStateException("Expected UTF-8 encoding length to be 1-6, but got " + length);
//    }

    byte[] bytes = new byte[length];
    
    for (int i = 0; i < length; i++) {
      int shift = (length - i - 1) * VALUE_BITS_PER_CONTINUATION_BYTE;
      
      byte valueMask;
      byte template;
      
      if (i == 0) {
        valueMask = INITIAL_BYTE_VALUE_BIT_MASK[length];
        template = INITIAL_BYTE_TEMPLATE[length];
      }
      else {
        valueMask = CONTINUATION_BYTE_VALUE_BIT_MASK;
        template = CONTINUATION_BYTE_TEMPLATE;
      }
      
      byte valueBits = (byte)((byte)(arg >>> shift) & valueMask);
      
      bytes[i] = (byte)(template | valueBits);      
    }
    
    return bytes;
  }

  /**
   * Converts a byte value to a string, with "0x" prefix, and treating
   * it as an unsigned value.
   * 
   * This is here to support the generation of exception messages, but
   * is public in case other code wants to use it.
   * 
   * @param arg The byte value to convert to a string
   */
  public static String byteToString(byte arg) {
    final int BITS_PER_NIBBLE = 4;
    final byte LOW_NIBBLE_MASK = 0x0f;
    final String HEX_PREFIX = "0x";
    
    byte highNibble = (byte)((byte)(arg >>> BITS_PER_NIBBLE) & LOW_NIBBLE_MASK);
    byte lowNibble = (byte)((byte)(arg >>> 0) & LOW_NIBBLE_MASK);
    
    StringBuffer temp = new StringBuffer();
    
    temp.append(HEX_PREFIX);
    temp.append(Integer.toHexString(highNibble));
    temp.append(Integer.toHexString(lowNibble));
    
    return temp.toString();
  }
  
  private static final int MIN_ENCODED_LENGTH = 1;
  private static final int MAX_ENCODED_LENGTH = 6;
  
  public static int lengthFromInitialByte(final byte initialByte)
    throws UTF8ParseException
  {
    int length;
    
    for (length = MIN_ENCODED_LENGTH; length <= MAX_ENCODED_LENGTH; length++) {
      if ((initialByte & INITIAL_BYTE_TEMPLATE_BIT_MASK[length]) == INITIAL_BYTE_TEMPLATE[length]) {
        break;
      }
    }
    
    if (length > MAX_ENCODED_LENGTH) {
      throw new UTF8ParseException("Initial byte " + byteToString(initialByte) + " does not match a valid pattern");        
    }
    
    return length;
  }
  
  /**
   * @param bytes
   * @return
   * @throws UTF8ParseException
   */
  public static int utf8ToInt(final byte[] bytes) throws UTF8ParseException {
    if (bytes.length < MIN_ENCODED_LENGTH) {
      throw new UTF8ParseException("Attempt to convert byte array with length less than " + MIN_ENCODED_LENGTH + " from UTF-8 to integer");
    }
    
    if (bytes.length > MAX_ENCODED_LENGTH) {
      throw new UTF8ParseException("Attempt to convert byte array with length more than " + MAX_ENCODED_LENGTH + " from UTF-8 to integer");
    }
    
    int length = lengthFromInitialByte(bytes[0]);
    byte mask = INITIAL_BYTE_VALUE_BIT_MASK[length];
      
    int value = (bytes[0] & mask) << (VALUE_BITS_PER_CONTINUATION_BYTE * (length - 1));
    
    for(int i = 1; i < length; i++) {
      if ((bytes[i] & CONTINUATION_BYTE_TEMPLATE_BIT_MASK) != CONTINUATION_BYTE_TEMPLATE) {
        throw new UTF8ParseException("Illegal continuation byte " + byteToString(bytes[i]) + " at offset " + i + " in UTF-8 sequence");
      }
      
      value |= ((bytes[i] & CONTINUATION_BYTE_VALUE_BIT_MASK) << (VALUE_BITS_PER_CONTINUATION_BYTE * (length - i - 1)));
    }
    
    int canonicalLength = utf8EncodedLength(value);
    
    if (bytes.length != canonicalLength) {
      throw new UTF8ParseException("Illegal representation of value " + value + " as " + bytes.length + " bytes (should be " + canonicalLength + ")");
    }
    
    return value;
  }
  
}
