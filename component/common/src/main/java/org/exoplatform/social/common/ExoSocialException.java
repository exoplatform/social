/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common;

import java.util.Arrays;

/**
 * The top exception class to handle exception for eXo Social.
 * <br>
 * Using this for displaying any error to user by using key for message bundle
 * display;
 * <br>
 * The exception message and cause for displaying more information.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Nov 9, 2010
 */
public class ExoSocialException extends RuntimeException {
  /**
   * messageKey used for gets resource bundle message. *
   */
  private String messageKey;

  /**
   * Provides messageArguments to be replaced on messageBundle.
   */
  private String[] messageArguments;

  /**
   * used for displaying, should be json format for easier data reading.
   */
  private String dataInput;


  /**
   * Only message key available.
   *
   * @param msgKey message key
   */
  public ExoSocialException(final String msgKey) {
    super();
    messageKey = msgKey;
  }

  /**
   * message key and messageArguments are available.
   *
   * @param msgKey  message key
   * @param msgArgs message arguments
   */
  public ExoSocialException(final String msgKey,
                            final String[] msgArgs) {
    super();
    messageKey = msgKey;
    messageArguments = Arrays.copyOf(msgArgs, msgArgs.length);
  }


  /**
   * messageKey and exception message is available.
   *
   * @param msgKey messageKey
   * @param msg    addition message (useful to developer).
   */
  public ExoSocialException(final String msgKey, final String msg) {
    super(msg);
    messageKey = msgKey;
  }

  /**
   * The message key with exception message and cause.
   *
   * @param msgKey message key
   * @param cause  the cause
   */
  public ExoSocialException(final String msgKey,
                            final Throwable cause) {
    super(cause);
    messageKey = msgKey;
  }

  /**
   * The message key with exception message and cause.
   *
   * @param msgKey message key
   * @param msg    the message which is useful for developers.
   * @param cause  the cause
   */
  public ExoSocialException(final String msgKey, final String msg,
                            final Throwable cause) {
    super(msg, cause);
    messageKey = msgKey;
  }

  /**
   * The message key with messageArguments, exception message and cause.
   *
   * @param msgKey  message key
   * @param msgArgs message arguments
   * @param msg     message, the message is useful for developer Example: Node
   *                not found.
   * @param cause   the cause
   */
  public ExoSocialException(final String msgKey, final String[] msgArgs,
                            final String msg, final Throwable cause) {
    super(msg, cause);
    messageKey = msgKey;
    messageArguments = Arrays.copyOf(msgArgs, msgArgs.length);
  }

  /**
   * Gets the message key.
   *
   * @return mesageKey
   */
  public final String getMessageKey() {
    return messageKey;
  }

  /**
   * Sets message key.
   *
   * @param msgKey the messag key
   */
  public final void setMessageKey(final String msgKey) {
    messageKey = msgKey;
  }

  /**
   * Gets the message arguments.
   *
   * @return the message arguments
   */
  public final String[] getMessageArguments() {
    if (messageArguments != null) {
      return Arrays.copyOf(messageArguments, messageArguments.length);
    }
    return messageArguments;
  }

  /**
   * Sets message arguments.
   *
   * @param msgArgs the mesage arguments
   */
  public final void setMessageArguments(final String[] msgArgs) {
    messageArguments = Arrays.copyOf(msgArgs, msgArgs.length);
  }

  /**
   * Gets the date input, which is useful for developers. Usually, this is a
   * json string serialized from objects.
   *
   * @return the data input.
   */
  public final String getDataInput() {
    return dataInput;
  }

  /**
   * Sets the json data input for displaying.
   * @param jsonDataInput the json data input
   */
  public final void setDataInput(final String jsonDataInput) {
    dataInput = jsonDataInput;
  }
}
