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

/**
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt; http://www.gregorpurdy.com/gregor
 * @version $Id$

 */
public class UTF8ParseException extends Exception {

  /**
   * Comment for <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 3257007648594409270L;
  
  private String message;
  
  public UTF8ParseException(String message) {
    this.message = message;
  }
  
  public String getMessage() { return message; }
  
}
