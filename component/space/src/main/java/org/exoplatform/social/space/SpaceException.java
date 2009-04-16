/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.space;

/**
 * Created by IntelliJ IDEA.
 * User: jeremi
 * Date: Dec 4, 2008
 * Time: 11:50:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpaceException extends Exception{
  public enum Code {
    INTERNAL_SERVER_ERROR,
    UNABLE_TO_LIST_AVAILABLE_APPLICATIONS,
    UNABLE_TO_ADD_APPLICATION,    
    UNABLE_TO_REMOVE_APPLICATION,
    UNABLE_TO_CREATE_GROUP,
    UNABLE_TO_CREATE_PAGE,
    UNABLE_TO_ADD_CREATOR,
    UNABLE_TO_REMOVE_USER,
    USER_NOT_MEMBER,
    USER_NOT_INVITED,
    ERROR_DATASTORE,
    SPACE_ALREADY_EXIST,
    UNKNOWN_SPACE_TYPE,
    UNABLE_TO_CREAT_NAV,
    UNABLE_TO_ADD_USER,
    ERROR_RETRIEVING_MEMBER_LIST,
    ERROR_RETRIEVING_USER,
    USER_ALREADY_INVITED,
    USER_ALREADY_MEMBER,
    ERROR_SENDING_CONFIRMATION_EMAIL,
    USER_NOT_EXIST,
    ERROR_SETTING_LEADER_STATUS,
    UNABLE_REQUEST_TO_JOIN,
    UNABLE_REQUEST_TO_JOIN_HIDDEN
  }

  private final Code code;

  public SpaceException(Code code) {
    this.code = code;
  }

  public SpaceException(Code code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public SpaceException(Code code, String msg, Throwable cause) {
    super(msg, cause);
    this.code = code;
  }

  public SpaceException(Code code, String msg) {
    super(msg);
    this.code = code;
  }

  public Code getCode() {
    return code;
  }
}

