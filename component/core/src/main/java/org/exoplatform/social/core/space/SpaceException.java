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
package org.exoplatform.social.core.space;

/**
 * Created by IntelliJ IDEA.
 * User: jeremi
 * Date: Dec 4, 2008
 * Time: 11:50:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpaceException extends Exception{

  /**
   * The Enum Code.
   */
  public enum Code {

    /** The INTERNA l_ serve r_ error. */
    INTERNAL_SERVER_ERROR,

    /** The UNABL e_ t o_ lis t_ availabl e_ applications. */
    UNABLE_TO_LIST_AVAILABLE_APPLICATIONS,

    /** The UNABL e_ t o_ ad d_ application. */
    UNABLE_TO_ADD_APPLICATION,

    /** The UNABL e_ t o_ remov e_ application. */
    UNABLE_TO_REMOVE_APPLICATION,

    /** The UNABL e_ t o_ remov e_ applications. */
    UNABLE_TO_REMOVE_APPLICATIONS,

    /** The UNABL e_ t o_ creat e_ group. */
    UNABLE_TO_CREATE_GROUP,

    /** The UNABL e_ t o_ remov e_ group. */
    UNABLE_TO_REMOVE_GROUP,

    /** The UNABL e_ t o_ creat e_ page. */
    UNABLE_TO_CREATE_PAGE,

    /** The UNABL e_ t o_ ad d_ creator. */
    UNABLE_TO_ADD_CREATOR,

    /** The UNABL e_ t o_ remov e_ user. */
    UNABLE_TO_REMOVE_USER,

    /** The USE r_ onl y_ leader. */
    USER_ONLY_LEADER, //user is the only leader of a space
    /** The USE r_ no t_ member. */
 USER_NOT_MEMBER,

    /** The USE r_ no t_ invited. */
    USER_NOT_INVITED,

    /** The ERRO r_ datastore. */
    ERROR_DATASTORE,

    /** The SPAC e_ alread y_ exist. */
    SPACE_ALREADY_EXIST,

    /** The UNKNOW n_ spac e_ type. */
    UNKNOWN_SPACE_TYPE,

    /** The UNABL e_ t o_ crea t_ nav. */
    UNABLE_TO_CREAT_NAV,

    /** The UNABL e_ t o_ remov e_ nav. */
    UNABLE_TO_REMOVE_NAV,

    /** The UNABL e_ t o_ ad d_ user. */
    UNABLE_TO_ADD_USER,

    /** The ERRO r_ retrievin g_ membe r_ list. */
    ERROR_RETRIEVING_MEMBER_LIST,

    /** The ERRO r_ retrievin g_ user. */
    ERROR_RETRIEVING_USER,

    /** The USE r_ alread y_ invited. */
    USER_ALREADY_INVITED,

    /** The USE r_ alread y_ member. */
    USER_ALREADY_MEMBER,

    /** The ERRO r_ sendin g_ confirmatio n_ email. */
    ERROR_SENDING_CONFIRMATION_EMAIL,

    /** The USE r_ no t_ exist. */
    USER_NOT_EXIST,

    /** The ERRO r_ settin g_ leade r_ status. */
    ERROR_SETTING_LEADER_STATUS,

    /** The UNABL e_ reques t_ t o_ join. */
    UNABLE_REQUEST_TO_JOIN,

    /** The UNABL e_ reques t_ t o_ joi n_ hidden. */
    UNABLE_REQUEST_TO_JOIN_HIDDEN,

    /** The UNABL e_ t o_ ini t_ app. */
    UNABLE_TO_INIT_APP,

    /** The UNABL e_ t o_ deini t_ app. */
    UNABLE_TO_DEINIT_APP,

    /** The UNABL e_ t o_ delet e_ space. */
    UNABLE_TO_DELETE_SPACE
  }

  /** The code. */
  private final Code code;

  /**
   * Instantiates a new space exception.
   *
   * @param code the code
   */
  public SpaceException(Code code) {
    this.code = code;
  }

  /**
   * Instantiates a new space exception.
   *
   * @param code the code
   * @param cause the cause
   */
  public SpaceException(Code code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  /**
   * Instantiates a new space exception.
   *
   * @param code the code
   * @param msg the msg
   * @param cause the cause
   */
  public SpaceException(Code code, String msg, Throwable cause) {
    super(msg, cause);
    this.code = code;
  }

  /**
   * Instantiates a new space exception.
   *
   * @param code the code
   * @param msg the msg
   */
  public SpaceException(Code code, String msg) {
    super(msg);
    this.code = code;
  }

  /**
   * Gets the code.
   *
   * @return the code
   */
  public Code getCode() {
    return code;
  }
}

