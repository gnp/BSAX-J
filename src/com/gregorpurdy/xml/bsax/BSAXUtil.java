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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.gregorpurdy.codec.UTF8Codec;
import com.gregorpurdy.codec.UTF8ParseException;

import org.xml.sax.SAXException;

/**
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt; http://www.gregorpurdy.com/gregor
 * @version $Id$
 */
public class BSAXUtil {

  /**
   * @param stream
   * @param arg
   * @throws SAXException
   */
  public static void writeInt(OutputStream stream, int arg) throws SAXException {
    byte[] bytes = UTF8Codec.intToUtf8(arg);
    
    try {
      stream.write(bytes);
    }
    catch (IOException e) {
      throw new SAXException(e);
    }
  }

  /**
   * @param stream
   * @return
   * @throws SAXException
   */
  public static int readInt(InputStream stream) throws SAXException {
    return readInt(stream, false);
  }
  
  /**
   * Read a UTF-8 encoded 31-bit unsigned integer from the
   * byte steam given.
   * 
   * @param stream the byte stream to read from
   * @return the integer read from the InputStream
   * @throws SAXException on premature EOF or malformed UTF-8
   */
  public static int readInt(InputStream stream, boolean allowEof) throws SAXException {
    int temp;
    
    try {
      temp = stream.read();
    }
    catch (IOException e) {
      throw new SAXException(e);
    }
    
    int byteCount;
    
    if (temp == -1) {
      if (allowEof) {
        return -1;
      }
      else {
        throw new SAXException("Unexpected end of file");
      }
    }
    
    if ((temp & 0x80) == 0x00) { // One Byte
      return temp;
    }
    else if ((temp & 0xe0) == 0xc0) { // Two Byte
      byteCount = 1;
    }
    else if ((temp & 0xf0) == 0xe0) { // Three Byte
      byteCount = 2;
    }
    else if ((temp & 0xf8) == 0xf0) { // Four Byte
      byteCount = 3;
    }
    else if ((temp & 0xfc) == 0xf8) { // Five Byte
      byteCount = 4;
    }
    else if ((temp & 0xfe) == 0xfc) { // Six Byte
      byteCount = 5;
    }
    else {
      throw new SAXException("Illegal first byte to UTF-8 sequence: 0x" + Integer.toString(temp, 16));
    }
    
    byte[] bytes = new byte[byteCount + 1];
    bytes[0] = (byte)temp;
    
    try {
      if (stream.read(bytes, 1, byteCount) != byteCount) {
        throw new SAXException("Unexpected end of file reading multi-byte UTF-8 sequence");
      }
    }
    catch (IOException e) {
      throw new SAXException(e);
    }
    
    try {
      return UTF8Codec.utf8ToInt(bytes);
    }
    catch (UTF8ParseException e) {
      throw new SAXException(e);
    }
  }
  
  /**
   * @param stream
   * @return
   * @throws SAXException
   */
  public static String readString(InputStream stream) throws SAXException {
    int length = readInt(stream);
    
    if (length < 0) {
      throw new SAXException("Got negative length attempting to read a String"); 
    }
    
    if (length == 0) {
      return "";
    }
    
    //
    // TODO: What should we do here to prevent damaged or malicious streams
    // making us go completely haywire and allocate tons of memory? Perhaps
    // we should at least read in chunks in case we reach end of file before
    // we reach the large length?
    //
    
    byte [] bytes = new byte[length];
    
    try {
      if (stream.read(bytes) != length) {
        throw new SAXException("Unexpected end of file reading String");
      }
    }
    catch (IOException e) {
      throw new SAXException(e);
    }
    
    try {
      return new String(bytes, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new SAXException("Unexpected lack of UTF-8 encoding support in JVM");
    }
  }
  
  /**
   * @param stream
   * @param string
   */
  public static void writeString(OutputStream stream, String string) throws SAXException {
    byte[] utf8;
    
    try {
      utf8 = string.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new SAXException(e);
    }
    
    BSAXUtil.writeInt(stream, utf8.length);
    
    try {
      stream.write(utf8);
    }
    catch (IOException e) {
      throw new SAXException(e);
    }
  }
  
}
