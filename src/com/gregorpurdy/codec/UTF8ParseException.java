package com.gregorpurdy.codec;

/**
 * @author gregor
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
