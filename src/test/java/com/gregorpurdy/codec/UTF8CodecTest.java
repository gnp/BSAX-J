/*
 * Copyright 2005-2010 Gregor N. Purdy, Sr.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.gregorpurdy.codec;


import junit.framework.TestCase;

/**
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt; http://www.gregorpurdy.com/gregor
 * @version $Id$
 */
public class UTF8CodecTest extends TestCase {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(UTF8CodecTest.class);
  }

  /**
   * For testIntToUtf8Uniform(), a COVERAGE_FACTOR of 1 takes
   * too long on my machine. But, 43 is prime and takes 28.402
   * seconds to test on my machine as of 2005-01-16 (dual Athlon
   * 850s running Fedora Core 3), covering 49,941,481 cases.
   * 
   * For testIntToUtf8LowBand(), COVERAGE_FACTOR of 43 takes
   * 25.598 seconds, covering 49,941,480.
   */
  private static final int COVERAGE_FACTOR = 2591; // 43
  
  /**
   * Cover the sample space (non-negative integers) uniformly.
   * 
   * @throws UTF8ParseException
   */
  public void testIntToUtf8Uniform() throws UTF8ParseException {
    int count = 0;
    
    for (int i = 0; (i >= 0) && (i < Integer.MAX_VALUE); i += COVERAGE_FACTOR) {
      byte [] encoded = UTF8Codec.intToUtf8(i);

      int temp = UTF8Codec.utf8ToInt(encoded);
      
      assertEquals(i, temp);
      count++;
    }
    
    System.out.println("Tested " + count + " cases.");
  }

  public void testIntToUtf8LowBand() throws UTF8ParseException {
    int count = 0;
    
    for (int i = 0; (i >= 0) && (i < Integer.MAX_VALUE / COVERAGE_FACTOR); i++) {
      byte [] encoded = UTF8Codec.intToUtf8(i);
      
      int temp = UTF8Codec.utf8ToInt(encoded);
      
      assertEquals(i, temp);
      count++;
    }
    
    System.out.println("Tested " + count + " cases.");
  }

  private void executeTestFromValue(byte[] utf8, int arg) {
    System.out.print("Testing conversion of 0x" + Integer.toHexString(arg) + " to " + utf8.length + " bytes: {");
    
    for (int i = 0; i < utf8.length; i++) {
      if (i != 0) {
        System.out.print(", ");
      }
      
      System.out.print(UTF8Codec.byteToString(utf8[i]));
    }
    
    System.out.println("}...");
    
    byte [] temp = UTF8Codec.intToUtf8(arg);
    
    assertEquals(utf8.length, temp.length);
    
    for (int i = 0; i < utf8.length; i++) {
      assertEquals(utf8[i], temp[i]);
    }
  }
  
  private void executeTestFromUtf8(byte[] utf8, int arg) throws UTF8ParseException {
    System.out.print("Testing conversion of " + utf8.length + " bytes {");
    
    for (int i = 0; i < utf8.length; i++) {
      if (i != 0) {
        System.out.print(", ");
      }
      
      System.out.print(UTF8Codec.byteToString(utf8[i]));
    }
    
    System.out.println("} to: 0x" + Integer.toHexString(arg) + "...");
    
    int temp = UTF8Codec.utf8ToInt(utf8);
    
    assertEquals(arg, temp);
  }
  
  
  public void testIntToUtf8Vectors() {
    byte [] temp;
    
    temp = new byte[2];
    temp[0] = (byte)0xc2;
    temp[1] = (byte)0x80;
    executeTestFromValue(temp, 128);
    
    //
    // These test cases from RFC 3629. The example is the
    // "A<NOT IDENTICAL TO><ALPHA>" example of the character
    // sequence U+0041 U+2262 U+0391 U+002E.
    //
    
    temp = new byte[1];
    temp[0] = (byte)0x41;
    executeTestFromValue(temp, 0x0041);
    
    temp = new byte[3];
    temp[0] = (byte)0xe2;
    temp[1] = (byte)0x89;
    temp[2] = (byte)0xa2;
    executeTestFromValue(temp, 0x2262);
    
    temp = new byte[2];
    temp[0] = (byte)0xce;
    temp[1] = (byte)0x91;
    executeTestFromValue(temp, 0x0391);
    
    temp = new byte[1];
    temp[0] = (byte)0x2e;
    executeTestFromValue(temp, 0x002e);
    
    try {
      System.out.println("EXPECTING THIS NEXT ONE TO FAIL (AND CATCHING IT):");
      temp = new byte[2];
      temp[0] = (byte)0xc0;
      temp[1] = (byte)0x80;
      executeTestFromUtf8(temp, 0x00);
      fail();
    }
    catch (UTF8ParseException e) {
      // success: 0xc0 0x80 is not a valid encoding of 0x00.
    }
  }
  
}
